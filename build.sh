#!/usr/bin/env bash
set -e
apt-get update -qq
apt-get install --allow-unauthenticated -qq curl openjdk-8-jdk gcc
apt-get install --allow-unauthenticated -qq util-linux libblkid1 libblkid-dev systemd libdbus-1-dev libsystemd-dev libclang-dev
export CXX="clang++-3.4"

curl https://sh.rustup.rs -sSf | sh -s -- --default-toolchain 1.27.0 -y
export PATH=$HOME/.cargo/bin:$PATH

cd /home
chmod +x gradlew
./gradlew codeCoverageReport --console=plain
