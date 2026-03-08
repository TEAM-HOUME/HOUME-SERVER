package or.sopt.houme.global.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@EnableAsync    // 비동기 기능 활성화
@Configuration
public class AsyncConfig {

    private static final String EXECUTOR_TAG_KEY = "executor";
    private static final String IMAGE_GENERATION_EXECUTOR = "imageGenerationExecutor";
    private static final String MODE_TAG_KEY = "mode";

    private final MeterRegistry meterRegistry;
    private final String executorMode;
    private final int platformCoreSize;
    private final int platformMaxSize;
    private final int platformQueueCapacity;
    private final int virtualConcurrencyLimit;

    public AsyncConfig(
            MeterRegistry meterRegistry,
            @Value("${houme.async.image-generation.mode:platform}") String executorMode,
            @Value("${houme.async.image-generation.platform.core-size:8}") int platformCoreSize,
            @Value("${houme.async.image-generation.platform.max-size:16}") int platformMaxSize,
            @Value("${houme.async.image-generation.platform.queue-capacity:8}") int platformQueueCapacity,
            @Value("${houme.async.image-generation.virtual.concurrency-limit:16}") int virtualConcurrencyLimit
    ) {
        this.meterRegistry = meterRegistry;
        this.executorMode = executorMode;
        this.platformCoreSize = platformCoreSize;
        this.platformMaxSize = platformMaxSize;
        this.platformQueueCapacity = platformQueueCapacity;
        this.virtualConcurrencyLimit = virtualConcurrencyLimit;
    }

    @Bean(name = "imageGenerationExecutor")
    public Executor imageGenerationExecutor(){
        if ("virtual".equalsIgnoreCase(executorMode)) {
            return virtualThreadExecutor();
        }
        return platformThreadExecutor();
    }

    private Executor platformThreadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(platformCoreSize);        // 동시에 실행할 기본 스레드 수, JVM 기본 스레드와 별도
        executor.setMaxPoolSize(platformMaxSize);        // 최대 스레드 수
        // maxPoolSize 확장이 실제로 일어나도록 대기 큐는 더 작게 유지한다.
        executor.setQueueCapacity(platformQueueCapacity);       // 대기 큐 크기
        executor.setThreadNamePrefix("ImageGenerator-");
        Tags tags = executorTags("platform");
        Counter rejectedCounter = meterRegistry.counter(
                "houme.async.executor.rejected.total",
                tags
        );
        // 스레드 풀이 꽉차면 남은 요청을 요청 스레드가 직접 실행해서 안정성이 높음
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 스레드 풀이 꽉차면 예외를 터뜨려 명시적 처리 가능
        executor.setRejectedExecutionHandler((r, executor1) -> {
            rejectedCounter.increment();
            throw new RejectedExecutionException("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
        });
        executor.initialize();
        ExecutorServiceMetrics.monitor(
                meterRegistry,
                executor.getThreadPoolExecutor(),
                "houme.async.executor",
                tags
        );
        Gauge.builder("houme.async.executor.active.tasks", executor, ThreadPoolTaskExecutor::getActiveCount)
                .tags(tags)
                .register(meterRegistry);
        Gauge.builder("houme.async.executor.queue.size", executor, taskExecutor ->
                        taskExecutor.getThreadPoolExecutor().getQueue().size())
                .tags(tags)
                .register(meterRegistry);
        Gauge.builder("houme.async.executor.queue.remaining", executor, taskExecutor ->
                        taskExecutor.getThreadPoolExecutor().getQueue().remainingCapacity())
                .tags(tags)
                .register(meterRegistry);
        Gauge.builder("houme.async.executor.available.permits", executor, taskExecutor ->
                        Math.max(taskExecutor.getMaxPoolSize() - taskExecutor.getActiveCount(), 0))
                .tags(tags)
                .register(meterRegistry);
        Gauge.builder("houme.async.executor.concurrency.limit", executor, ThreadPoolTaskExecutor::getMaxPoolSize)
                .tags(tags)
                .register(meterRegistry);
        Gauge.builder("houme.async.executor.pool.core", executor, ThreadPoolTaskExecutor::getCorePoolSize)
                .tags(tags)
                .register(meterRegistry);
        Gauge.builder("houme.async.executor.pool.max", executor, ThreadPoolTaskExecutor::getMaxPoolSize)
                .tags(tags)
                .register(meterRegistry);
        return executor;
    }

    private Executor virtualThreadExecutor() {
        Tags tags = executorTags("virtual");
        Counter rejectedCounter = meterRegistry.counter(
                "houme.async.executor.rejected.total",
                tags
        );
        ExecutorService executorService = createVirtualThreadExecutor();
        VirtualThreadBoundedExecutor executor = new VirtualThreadBoundedExecutor(
                executorService,
                virtualConcurrencyLimit,
                rejectedCounter
        );
        Gauge.builder("houme.async.executor.active.tasks", executor, VirtualThreadBoundedExecutor::getActiveTasks)
                .tags(tags)
                .register(meterRegistry);
        Gauge.builder("houme.async.executor.queue.size", executor, VirtualThreadBoundedExecutor::getQueueSize)
                .tags(tags)
                .register(meterRegistry);
        Gauge.builder("houme.async.executor.available.permits", executor, VirtualThreadBoundedExecutor::getAvailablePermits)
                .tags(tags)
                .register(meterRegistry);
        Gauge.builder("houme.async.executor.concurrency.limit", executor, VirtualThreadBoundedExecutor::getConcurrencyLimit)
                .tags(tags)
                .register(meterRegistry);
        return executor;
    }

    private Tags executorTags(String mode) {
        return Tags.of(
                EXECUTOR_TAG_KEY, IMAGE_GENERATION_EXECUTOR,
                MODE_TAG_KEY, mode
        );
    }

    private ExecutorService createVirtualThreadExecutor() {
        try {
            Method method = java.util.concurrent.Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            return (ExecutorService) method.invoke(null);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("가상 스레드는 Java 21 런타임이 필요합니다.");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("가상 스레드 실행기를 생성하지 못했습니다.", e);
        }
    }

    private static class VirtualThreadBoundedExecutor implements Executor {

        private final ExecutorService delegate;
        private final Semaphore semaphore;
        private final AtomicInteger activeTasks = new AtomicInteger();
        private final Counter rejectedCounter;
        private final int concurrencyLimit;

        private VirtualThreadBoundedExecutor(ExecutorService delegate, int concurrencyLimit, Counter rejectedCounter) {
            this.delegate = delegate;
            this.semaphore = new Semaphore(concurrencyLimit);
            this.concurrencyLimit = concurrencyLimit;
            this.rejectedCounter = rejectedCounter;
        }

        @Override
        public void execute(Runnable command) {
            if (!semaphore.tryAcquire()) {
                rejectedCounter.increment();
                throw new RejectedExecutionException("가상 스레드 동시 실행 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
            }
            delegate.execute(() -> {
                activeTasks.incrementAndGet();
                try {
                    command.run();
                } finally {
                    activeTasks.decrementAndGet();
                    semaphore.release();
                }
            });
        }

        public int getActiveTasks() {
            return activeTasks.get();
        }

        public int getAvailablePermits() {
            return semaphore.availablePermits();
        }

        public int getConcurrencyLimit() {
            return concurrencyLimit;
        }

        public int getQueueSize() {
            return 0;
        }
    }
}
