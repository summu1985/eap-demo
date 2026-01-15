#!/usr/bin/env bash
set -eo pipefail
set -x
echo "Running $PWD/install.sh"
#injected_dir=$1
# copy any needed files into the target build.
#cp -rf ${injected_dir} $JBOSS_HOME/extensions

injected_dir="$1"
source /usr/local/s2i/install-common.sh

# WAR (if you keep it under deployments/ instead of target/)
# install_deployments "${injected_dir}/deployments"

install_modules "${injected_dir}/modules"
configure_drivers "${injected_dir}/drivers.env"