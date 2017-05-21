AlesharikWebServer
==================

Master: [![Travis-ci](https://api.travis-ci.org/alesharik/AlesharikWebServer.svg?branch=master)](https://travis-ci.org/alesharik/AlesharikWebServer) [![codecov.io](https://codecov.io/github/alehsarik/AlesharikWebServer/coverage.svg?branch=master)](https://codecov.io/github/alesharik/AlesharikWebServer?branch=master)

Develop: [![Travis-ci](https://travis-ci.org/alesharik/AlesharikWebServer.svg?branch=develop)](https://travis-ci.org/alesharik/AlesharikWebServer) [![codecov.io](https://codecov.io/github/alehsarik/AlesharikWebServer/coverage.svg?branch=develop)](https://codecov.io/github/alesharik/AlesharikWebServer?branch=develop)

Status: [![Codacy Badge](https://api.codacy.com/project/badge/Grade/a8dc12bb283444998223d228061fa7a2)](https://www.codacy.com/app/alesharik/AlesharikWebServer?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=alesharik/AlesharikWebServer&amp;utm_campaign=Badge_Grade) 

AlesharikWebServer is a web server with unique module system.
WARNING! This server might not work work on windows/mac!

Build server
============
1. `git clone -b develop https://github.com/alesharik/AlesharikWebServer.git`
2. `sudo chmod +x ./gradlew`
3. `./gradlew prepareDeploy`<br>
You can start server by executing `start.sh` script in `deploy` folder

Setup artifact
==============
1. `git clone -b develop https://github.com/alesharik/AlesharikWebServer.git`
2. `sudo chmod +x ./gradlew`
3. `./gradlew publishArtifacts`