# HOUME nginx 설정 (dev / prod)

dev·prod EC2 의 nginx 설정을 코드로 관리한다. **이 디렉토리가 설정의 원본**이며,
서버에서 nginx 설정을 직접 수정하지 않는다 — 수정할 일이 있으면 여기를 고치고 서버에 반영한다.
배경과 기존 설정의 문제점은 [#590](https://github.com/TEAM-HOUME/HOUME-SERVER/issues/590) 참고.

```
[클라이언트] ── dev.houme.kr  ──> dev EC2 nginx(:443) ── blue/green ──> houme 컨테이너(:8080)
[클라이언트] ── prod.houme.kr ──> prod EC2 nginx(:443) ── blue/green ──> blue(:8080)/green(:8081)
```

## 디렉토리 구조
```
nginx/
├── dev/         ☁️ dev EC2  — nginx.conf, houme.conf, service-env.inc
├── prod/        ☁️ prod EC2 — 동일 구성
└── snapshots/   📸 전환 전(2026-07-12) 서버 원본 — 참고·복원용, 수정 금지
```
**dev/prod 는 구조가 완전히 동일**하다. 차이는 값(server_name·cert 경로·green 포트)뿐.
설정 수정 시 두 곳 모두 반영할 것 (`diff nginx/dev/houme.conf nginx/prod/houme.conf` 로 확인).

## 파일 → 서버 경로 매핑

| 레포 파일 | 서버 경로 | 비고 |
|---|---|---|
| `<env>/nginx.conf` | `/etc/nginx/nginx.conf` | 양 환경 동일 |
| `<env>/houme.conf` | `/etc/nginx/sites-available/houme.conf` | `sites-enabled/houme.conf` 로 심링크 |
| `<env>/service-env.inc` | `/etc/nginx/service-env.inc` | **초기값** — 서버에 이미 있으면 덮어쓰지 않는다 |

## blue/green 전환 구조

- `houme.conf` 의 `upstream blue/green` 이 트래픽 목적지 후보를 정의한다.
  dev 는 둘 다 8080(단일 컨테이너), prod 는 8080/8081.
- `/etc/nginx/service-env.inc` 의 `set $service_url blue;` 한 줄이 현재 활성 환경이다.
  prod 배포 시 CD(`prod-cd.yml`)가 이 파일을 덮어쓰고 `nginx -s reload` 해서 트래픽을 전환한다.
- `service-env.inc` 는 **런타임 상태 파일**이다. 레포의 것은 초기값일 뿐이므로
  서버 반영 시 기존 파일이 있으면 절대 덮어쓰지 않는다 (덮어쓰면 트래픽이 엉뚱한 쪽으로 간다).

## 서버 적용 절차 (dev 먼저 → 검증 → prod)

```bash
# 0. 백업
sudo cp -r /etc/nginx /etc/nginx.bak-$(date +%Y%m%d%H%M%S)

# 1. 파일 배치 (레포의 nginx/<env>/ 기준)
sudo cp nginx.conf   /etc/nginx/nginx.conf
sudo cp houme.conf   /etc/nginx/sites-available/houme.conf
sudo test -f /etc/nginx/service-env.inc || sudo cp service-env.inc /etc/nginx/service-env.inc

# 2. 사이트 활성화 + 구 설정 비활성화 (심링크만 정리, sites-available 원본은 유지)
sudo ln -sf /etc/nginx/sites-available/houme.conf /etc/nginx/sites-enabled/houme.conf
sudo rm -f /etc/nginx/sites-enabled/{default,dev.houme.kr,prod.houme.kr,service-env.inc}

# 3. 문법 검사 통과 시에만 reload (무중단)
sudo nginx -t && sudo nginx -s reload
```

문제 발생 시 복원: `sudo rm -rf /etc/nginx && sudo cp -r /etc/nginx.bak-<ts> /etc/nginx && sudo nginx -t && sudo nginx -s reload`

### prod 적용 시 추가 확인
- 트래픽 적은 시간대에 진행한다.
- 적용 전 `cat /etc/nginx/sites-enabled/service-env.inc` 로 현재 활성 환경(blue/green)을 확인하고,
  그 값 그대로 `/etc/nginx/service-env.inc` 를 만든다 (구 경로 파일은 심링크 정리 단계에서 제거).
- 적용 후 prod 배포를 한 번 돌려 blue↔green 전환이 정상인지 확인한다.
- upstream 이 기존 사설 IP(`10.0.1.11`)에서 `127.0.0.1` 로 바뀌었다 — 적용 직후
  `curl -s https://prod.houme.kr/actuator/health` 로 프록시 정상 동작을 확인할 것.

## 운영 원칙
- 서버에서 직접 수정 금지 — 레포 수정 → 리뷰 → 서버 반영 순서를 지킨다.
- `snapshots/` 는 전환 전 원본 기록이므로 갱신하지 않는다.
- 인증서는 certbot 이 관리한다 (`/etc/letsencrypt/` — 이 레포 범위 밖).
- `.gitattributes` 로 `nginx/**` 는 항상 LF 로 체크아웃된다 (CRLF 로 서버에 올리면 안 됨).
