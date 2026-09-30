#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SITE_SOURCE="${ROOT_DIR}/build/pages-source"
JAVADOC_DIR="${ROOT_DIR}/MaDKit/build/docs/javadoc"

rm -rf "${SITE_SOURCE}"
mkdir -p "${SITE_SOURCE}/api/latest"
cp -R "${ROOT_DIR}/docs/." "${SITE_SOURCE}/"

"${ROOT_DIR}/gradlew" :MaDKit:javadoc --no-daemon --console=plain

if [[ ! -f "${JAVADOC_DIR}/index.html" ]]; then
  echo "Expected Javadoc index not found: ${JAVADOC_DIR}/index.html" >&2
  exit 1
fi

cp -R "${JAVADOC_DIR}/." "${SITE_SOURCE}/api/latest/"

for required_file in index.md getting-started.md concepts.md samples.md modules.md releases.md _config.yml; do
  if [[ ! -f "${SITE_SOURCE}/${required_file}" ]]; then
    echo "Required site file not found: ${required_file}" >&2
    exit 1
  fi
done

echo "Pages source assembled at ${SITE_SOURCE}"
echo "Javadoc available at ${SITE_SOURCE}/api/latest/"
