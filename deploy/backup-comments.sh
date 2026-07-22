#!/usr/bin/env bash
set -euo pipefail
umask 0077

backup_dir=/var/backups/xszn-comments
timestamp=$(date +%Y%m%d-%H%M%S)
temporary_file="$backup_dir/comments-$timestamp.sql.gz.part"
final_file="$backup_dir/comments-$timestamp.sql.gz"

install -d -m 0700 "$backup_dir"
mysqldump \
  --defaults-extra-file=/etc/xszn-comments-backup.cnf \
  --single-transaction \
  --skip-lock-tables \
  --set-gtid-purged=OFF \
  xszn_comments | gzip -9 > "$temporary_file"
mv "$temporary_file" "$final_file"
