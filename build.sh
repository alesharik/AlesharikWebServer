#!/usr/bin/env bash
sudo add-apt-repository -y ppa:h-rayflood/llvm
sudo apt-get update -qq
sudo apt-get install --allow-unauthenticated -qq clang-3.4 util-linux libblkid1 libblkid-dev systemd libsystemd-daemon-dev libsystemd-login-dev libsystemd-journal-dev libdbus-1-dev libsystemd-dev
export CXX="clang++-3.4"

curl https://sh.rustup.rs -sSf | sh -s -- --default-toolchain 1.27.0 -y
export PATH=$HOME/.cargo/bin:$PATH

cd /home
chmod +x gradlew
./gradlew codeCoverageReport

bash <(curl -s https://codecov.io/bash)