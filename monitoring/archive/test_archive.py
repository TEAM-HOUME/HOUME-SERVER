import gzip
import io
import json
import subprocess
import tempfile
import unittest
from datetime import datetime, timedelta
from pathlib import Path
from unittest.mock import patch
from urllib.parse import parse_qs, urlparse

import archive


class ArchiveTest(unittest.TestCase):
    def test_allowlist(self):
        line = json.dumps(dict(level="ERROR", traceId="job-ab231c09", event="payment_failed",
                               errorCode=40001, stack_trace="java.lang.IllegalStateException: FAKE_TOKEN",
                               message="Bearer FAKE_TOKEN", email="a@host", userId="123",
                               cookie="FAKE_TOKEN", password="FAKE_TOKEN", body="FAKE_TOKEN"))
        self.assertEqual(archive.record("prod", "green", 1, line), dict(
            timestamp="1970-01-01T00:00:00.000000001Z", env="prod", container="green",
            level="ERROR", traceId="job-ab231c09", event="payment_failed", errorCode=40001,
            exceptionType="java.lang.IllegalStateException"))
        result = archive.record("dev", "houme", 1, '{"traceId":"a@host","level":"secret"}')
        self.assertEqual(set(result), {"timestamp", "env", "container"})

    def test_exception_classes(self):
        for name in ("Exception", "Error", "java.lang.Exception", "java.lang.Error",
                     "java.lang.IllegalStateException", "AssertionError"):
            for fields in ({"exceptionType": name}, {"stack_trace": name + ": secret"}):
                with self.subTest(fields=fields):
                    row = archive.record("dev", "houme", 0, json.dumps(fields))
                    self.assertEqual(row["exceptionType"], name)

    def test_partial_day_queries_only_retained_interval(self):
        start = datetime(2026, 11, 19, tzinfo=archive.KST)
        cutoff = start + timedelta(hours=1, minutes=30, microseconds=123456)
        end = start + archive.DAY
        with tempfile.TemporaryDirectory() as directory, patch.object(archive, "windows") as query:
            folder = Path(directory)
            self.assertTrue(archive.collect("dev", start.date(), folder, cutoff))
            intervals = [call.args[1:] for call in query.call_args_list]
            self.assertEqual(intervals[0][0], archive.epoch_ns(cutoff))
            self.assertEqual(intervals[-1][1], archive.epoch_ns(end))
            self.assertEqual([b for a, b in intervals[:-1]], [a for a, b in intervals[1:]])
            self.assertEqual((folder / "2026-11-19.gap").read_text().strip(),
                             f"[{start.isoformat()}, {cutoff.isoformat()})")

    def test_midnight_cutoff_expires_only_the_previous_day(self):
        midnight = datetime(2026, 11, 19, tzinfo=archive.KST)
        previous = (midnight - archive.DAY).date()
        with tempfile.TemporaryDirectory() as directory, patch.object(archive, "windows") as query:
            folder = Path(directory)
            self.assertTrue(archive.collect("dev", previous, folder, midnight))
            query.assert_not_called()
            self.assertFalse((folder / "2026-11-18.jsonl.gz").exists())
            self.assertFalse(archive.collect("dev", midnight.date(), folder, midnight))
            self.assertEqual(query.call_count, 24)
            self.assertFalse((folder / "2026-11-19.gap").exists())

    def test_split_without_losing_entries_with_equal_timestamps(self):
        def query(url, timeout):
            params = parse_qs(urlparse(url).query)
            start, end = (int(params[k][0]) for k in ("start", "end"))
            values = [[str(ts), "{}"] for ts in [0, 1, 1, 2, 3] if start <= ts < end]
            return io.BytesIO(json.dumps(dict(status="success", data=dict(resultType="streams",
                result=[dict(stream=dict(container="houme"), values=values[:3])]))).encode())
        with patch.object(archive, "urlopen", query), patch.object(archive, "LIMIT", 3):
            rows = list(archive.windows("dev", 0, 4))
        self.assertEqual(len(rows), 5)
        self.assertEqual(rows[1], rows[2])

    def test_retry_after_loki_window_expires_and_no_duplicate_upload_on_rerun(self):
        now = datetime(2026, 11, 22, 1, 30, tzinfo=archive.KST)
        today = now.date()
        objects = {}
        def source(env, start, end):
            yield archive.record(env, archive.ENVIRONMENTS[env][0], start,
                                 '{"traceId":"deadbeef","message":"FAKE_TOKEN"}')
        def upload(args, check):
            objects[args[4]] = gzip.decompress(Path(args[3]).read_bytes())
        with tempfile.TemporaryDirectory() as directory, patch.object(archive, "windows", source):
            folder = Path(directory)
            with patch("subprocess.run", side_effect=subprocess.CalledProcessError(1, "aws")):
                self.assertEqual(archive.run(folder, "test", now), 1)
            later = now + 4 * archive.DAY
            with patch("subprocess.run", upload):
                self.assertEqual(archive.run(folder, "test", later), 1)
            for env in archive.ENVIRONMENTS:
                self.assertTrue((folder / env / (today.isoformat() + ".gap")).exists())
                key = f"s3://test/env={env}/date={today - archive.DAY}/logs.jsonl.gz"
                partial = today + archive.DAY
                partial_rows = objects[f"s3://test/env={env}/date={partial}/logs.jsonl.gz"].splitlines()
                self.assertEqual(len(partial_rows), 23)
                rows = objects[key].splitlines()
                self.assertEqual(len(rows), 24)
                self.assertEqual(json.loads(rows[0])["traceId"], "deadbeef")
                self.assertNotIn(b"FAKE_TOKEN", objects[key])
            with patch("subprocess.run") as uploader:
                self.assertEqual(archive.run(folder, "test", later), 0)
                uploader.assert_not_called()


if __name__ == "__main__":
    unittest.main()
