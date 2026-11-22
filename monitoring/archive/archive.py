"""Daily Loki archive. Runs under the singleton systemd service on the Loki host."""
import gzip
import json
import os
import re
import subprocess
import sys
from datetime import date, datetime, time, timedelta, timezone
from pathlib import Path
from urllib.parse import urlencode
from urllib.request import urlopen

KST = timezone(timedelta(hours=9))
DAY = timedelta(days=1)
ENVIRONMENTS = {"dev": ["houme"], "prod": ["blue", "green"]}
LEVELS = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"}
LIMIT = 500
LOKI_URL = os.environ.get("LOKI_URL", "http://127.0.0.1:3100")


def windows(env, start, end):
    containers = "|".join(ENVIRONMENTS[env])
    params = urlencode(dict(query=f'{{app="houme",env="{env}",container=~"{containers}"}}',
                            start=start, end=end, limit=LIMIT, direction="forward"))
    with urlopen(LOKI_URL + "/loki/api/v1/query_range?" + params, timeout=90) as response:
        result = json.load(response)
    if result["status"] != "success" or result["data"]["resultType"] != "streams":
        raise RuntimeError("Loki query failed")
    streams = result["data"]["result"]
    if sum(len(stream["values"]) for stream in streams) >= LIMIT:
        if end - start <= 1:
            raise RuntimeError("Too many entries at one timestamp")
        middle = (start + end) // 2
        yield from windows(env, start, middle)
        yield from windows(env, middle, end)
        return
    for stream in streams:
        container = stream["stream"]["container"]
        if container not in ENVIRONMENTS[env]:
            raise ValueError("Unexpected container")
        for stamp, line in stream["values"]:
            if start <= int(stamp) < end:
                yield record(env, container, int(stamp), line)


def record(env, container, stamp, line):
    # Loki's Docker stream timestamp is the event clock for both partition and record.
    seconds, nanos = divmod(stamp, 10**9)
    iso = datetime.fromtimestamp(seconds, timezone.utc).strftime("%Y-%m-%dT%H:%M:%S")
    output = dict(timestamp=f"{iso}.{nanos:09d}Z", env=env, container=container)
    try:
        fields = json.loads(line)
    except ValueError:
        fields = {}
    if not isinstance(fields, dict):
        fields = {}
    level = fields.get("level")
    if isinstance(level, str) and level in LEVELS:
        output["level"] = level
    for name, pattern in (
        ("traceId", r"[A-Za-z0-9._-]{1,36}"),
        ("event", r"[A-Za-z][A-Za-z0-9_.-]{0,63}"),
        ("errorCode", r"[A-Za-z]{0,20}[-_]?[0-9]{1,8}"),
        ("exceptionType", r"(?:[a-zA-Z_$][\w$]*\.)*[A-Z][\w$]*(?:Error|Exception)"),
    ):
        value = fields.get(name)
        if name == "errorCode" and type(value) is int and 0 <= value <= 99999999:
            output[name] = value
            continue
        if name == "exceptionType" and not value:
            stack = fields.get("stack_trace")
            if isinstance(stack, str):
                value = stack.partition(":")[0].split("\n", 1)[0]
        if isinstance(value, str) and len(value) <= 200 and re.fullmatch(pattern, value, re.ASCII):
            output[name] = value
    return output


def collect(env, day, folder):
    target = folder / (day.isoformat() + ".jsonl.gz")
    if target.exists():
        return
    start = int(datetime.combine(day, time.min, KST).timestamp()) * 10**9
    hour = 3600 * 10**9
    temporary = target.with_suffix(".part")
    with gzip.open(temporary, "wt", encoding="utf-8") as output:
        for offset in range(24):
            for entry in windows(env, start + offset * hour, start + (offset + 1) * hour):
                output.write(json.dumps(entry, separators=(",", ":")) + "\n")
    temporary.replace(target)


def run(folder, bucket, today):
    failed = False
    for env in ENVIRONMENTS:
        spool = folder / env
        spool.mkdir(parents=True, exist_ok=True)
        cursor = spool / "next-day"
        if not cursor.exists():
            pending = cursor.with_suffix(".tmp")
            pending.write_text((today - DAY).isoformat())
            pending.replace(cursor)
        day = date.fromisoformat(cursor.read_text())
        while day < today:
            if day < today - 2 * DAY and not (spool / (day.isoformat() + ".jsonl.gz")).exists():
                print(f"Loki retention gap env={env} date={day}", file=sys.stderr)
                (spool / (day.isoformat() + ".gap")).touch()
                failed = True
            else:
                try:
                    collect(env, day, spool)
                except Exception as error:
                    print(f"collect failed env={env} date={day} type={type(error).__name__}", file=sys.stderr)
                    failed = True
                    break
            day += DAY
            pending = cursor.with_suffix(".tmp")
            pending.write_text(day.isoformat())
            pending.replace(cursor)
        for archive in sorted(spool.glob("*.jsonl.gz")):
            partition = archive.name.split(".")[0]
            key = f"env={env}/date={partition}/logs.jsonl.gz"
            try:
                subprocess.run(["aws", "s3", "cp", str(archive), f"s3://{bucket}/{key}",
                                "--only-show-errors", "--content-type", "application/x-ndjson",
                                "--content-encoding", "gzip"], check=True)
            except (OSError, subprocess.CalledProcessError):
                print(f"upload failed env={env} date={partition}", file=sys.stderr)
                failed = True
                continue
            print(f"uploaded env={env} date={partition} bytes={archive.stat().st_size} "
                  f"completed={datetime.now(timezone.utc).isoformat()}", flush=True)
            archive.unlink()
    return int(failed)


if __name__ == "__main__":
    sys.exit(run(Path(os.environ["STATE_DIRECTORY"]), os.environ["ARCHIVE_BUCKET"],
                 datetime.now(KST).date()))
