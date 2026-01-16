#!/usr/bin/env bash
set -eo pipefail
set -x
echo "Running $PWD/install.sh"

injected_dir="$1"
source /usr/local/s2i/install-common.sh

# 1) Install JDBC modules + register drivers
install_modules "${injected_dir}/modules"
configure_drivers "${injected_dir}/drivers.env"

# Stage runtime customization scripts (postconfigure + cli) into $JBOSS_HOME/extensions
# so they can run at container startup.
mkdir -p "${JBOSS_HOME}/extensions"

if [ -f "${injected_dir}/postconfigure.sh" ]; then
  cp -f "${injected_dir}/postconfigure.sh" "${JBOSS_HOME}/extensions/postconfigure.sh"
  chmod +x "${JBOSS_HOME}/extensions/postconfigure.sh"
fi

if [ -f "${injected_dir}/enable-app-debug.cli" ]; then
  cp -f "${injected_dir}/enable-app-debug.cli" "${JBOSS_HOME}/extensions/enable-app-debug.cli"
fi

echo "install.sh completed"