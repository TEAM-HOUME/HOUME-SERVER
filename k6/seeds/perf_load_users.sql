-- perf 전용 이미지 생성 부하 테스트 사용자 seed
-- user 557을 기준으로 558~561을 만들고, 각 사용자에 ACTIVE 크레딧 100개를 보장한다.
DO $$
DECLARE
    test_user_id bigint;
BEGIN
    FOR test_user_id IN 558..561 LOOP
        INSERT INTO users (
            id, created_at, updated_at, birthday, email, gender, has_generated_image,
            name, nickname, nickname_tag, password, role, social_type, status
        )
        SELECT
            test_user_id,
            now(),
            now(),
            birthday,
            format('perf-load-%s@example.test', test_user_id),
            gender,
            false,
            format('perf-load-%s', test_user_id),
            format('perf%s', test_user_id),
            'LOAD',
            password,
            role,
            social_type,
            status
        FROM users
        WHERE id = 557
        ON CONFLICT (id) DO NOTHING;

        IF (SELECT count(*) FROM credits WHERE user_id = test_user_id AND status = 'ACTIVE') < 100 THEN
            INSERT INTO credits (created_at, updated_at, status, user_id)
            SELECT now(), now(), 'ACTIVE', test_user_id
            FROM generate_series(1, 100);
        END IF;
    END LOOP;
END $$;

SELECT setval('users_id_seq', (SELECT max(id) FROM users), true);
SELECT setval('credits_id_seq', (SELECT max(id) FROM credits), true);
