#!/usr/bin/env bash
set -euo pipefail

cd /opt/xszn-ny/backend
mvn --batch-mode --quiet clean test package
install -o root -g xszn-comments -m 0640 target/xszn-comments.jar /opt/xszn-comments/xszn-comments.jar
systemctl restart xszn-comments

for attempt in {1..15}; do
  if curl --fail --silent --show-error http://127.0.0.1:8080/api/health >/dev/null; then
    exit 0
  fi
  sleep 2
done

journalctl -u xszn-comments --no-pager -n 60
exit 1
