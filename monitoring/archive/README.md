# 애플리케이션 로그 S3 아카이브

중앙 Loki 호스트에서 매일 01:00 KST에 전날 00:00 이상 다음 날 00:00 미만의 Docker 로그를 읽습니다. 발생 시각은 Loki가 수집한 Docker 타임스탬프이며 파일 안에는 UTC로 기록합니다. dev/houme와 prod/blue·green의 기존 Loki 스트림을 사용하므로 이미 수집된 컨테이너는 교체 후에도 내보낼 수 있습니다.

각 레코드는 시각, 환경, 컨테이너를 담으며, 구조화된 로그에 있는 레벨과 traceId를 보존합니다. 식별자 형식의 event, errorCode, exceptionType을 포함하고 stack_trace에서는 첫 예외 클래스 이름만 추출합니다. HTTP 요청 밖에서 발생한 로그는 traceId가 없을 수 있습니다. 요청 ID에는 자격 증명이나 개인 식별자를 사용하지 마세요. 자유 형식 본문과 사용자 ID는 저장 대상에서 제외합니다.

## 설치

1. 배포 권한이 있는 AWS CLI 세션에서 대상 계정·리전을 확인하고 전용 버킷을 생성합니다. ArchiveWriterRole에는 중앙 Loki EC2에 연결된 인스턴스 역할 이름을 전달합니다. 버킷은 비공개·기본 암호화·30일 만료로 생성됩니다.

   ```sh
   aws sts get-caller-identity
   aws cloudformation deploy --stack-name houme-log-archive \
     --template-file monitoring/archive/bucket.yml --capabilities CAPABILITY_NAMED_IAM \
     --parameter-overrides ArchiveWriterRole="$MONITORING_EC2_ROLE"
   aws cloudformation describe-stacks --stack-name houme-log-archive \
     --query 'Stacks[0].Outputs[?OutputKey==`BucketName`].OutputValue' --output text
   ```

2. 중앙 Loki 호스트에 Python 3.8+와 AWS CLI를 설치하고 아래 명령을 실행합니다. ARCHIVE_BUCKET은 위 출력값입니다. 시스템 서비스가 실행하는 aws가 시스템 PATH에 있어야 합니다.

   ```sh
   cd monitoring/archive
   sudo install -D -m 0644 archive.py /opt/houme-log-archive/archive.py
   printf 'ARCHIVE_BUCKET=%s\n' "$ARCHIVE_BUCKET" | sudo tee /etc/houme-log-archive.env
   sudo install -m 0644 houme-log-archive.{service,timer} /etc/systemd/system/
   sudo systemctl daemon-reload
   sudo systemctl enable --now houme-log-archive.timer
   sudo systemctl start houme-log-archive.service
   ```

## 운영 및 조회

- 최초 실행은 전날부터 시작합니다. 정제된 날짜별 gzip을 서비스 상태 디렉터리에 완성한 뒤 업로드합니다. 오류가 발생하면 30분 후 재시도합니다. 재시작 시 날짜 커서와 미전송 파일을 읽고 같은 버킷 키로 재전송합니다. 버킷은 버전 관리를 끈 전용 버킷으로 운영합니다.
- S3 전송이 장기간 중단되면 로컬 대기 파일이 늘어나므로 호스트 디스크 여유를 확인합니다. **Loki 수집 또는 내보내기 자체가 72시간 넘게 중단되면** 만료된 구간은 복원할 수 없습니다. 내보내기가 만료 구간을 만나면 해당 날짜의 .gap 파일과 실패 로그를 남깁니다. 전날 로그를 읽은 후 지연 도착한 로그는 그 파일에 포함되지 않습니다.
- `journalctl -u houme-log-archive.service`에서 환경별 완료 시각과 압축 바이트 수를 확인합니다. 읽기 권한이 있는 운영자는 아래 명령으로 환경별 S3 객체의 경로, 크기, 업로드 성공 시각을 확인할 수 있습니다.

  ```sh
  aws s3api list-objects-v2 --bucket "$ARCHIVE_BUCKET" --prefix env=prod/ \
    --query 'Contents[].[Key,Size,LastModified]' --output table
  ```

- 테스트 요청의 X-Trace-Id를 기록한 뒤 다음 일일 적재 후 아래와 같이 검색합니다. dev와 prod 각각 확인하고, 72시간이 지난 뒤 같은 S3 객체로 다시 확인합니다. 테스트용 가짜 토큰을 메시지에 포함한 경우 압축을 푼 파일에서 해당 문자열 검색 결과가 0건인지도 확인합니다.

  ```sh
  aws s3 cp "s3://$ARCHIVE_BUCKET/env=$ENV/date=$DAY/logs.jsonl.gz" - \
    | gzip -dc | jq -c --arg id "$TRACE_ID" 'select(.traceId == $id)'
  ```

- S3 Lifecycle은 업로드된 객체의 생성일 기준 30일 만료를 비동기로 처리합니다. 일별 파일에는 해당 날짜의 로그가 없으면 0개 레코드가 들어갑니다.
