#!/usr/bin/env bash
set -e
set -o pipefail
set -x

CLI="${JBOSS_HOME}/extensions/enable-app-debug.cli"

if [ -f "${CLI}" ]; then
  echo "postconfigure.sh: applying CLI config: ${CLI}"
  /opt/eap/bin/jboss-cli.sh -c --file="${CLI}"
else
  echo "postconfigure.sh: CLI file not found at ${CLI}; skipping"
fi
