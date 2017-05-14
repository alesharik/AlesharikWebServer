AlesharikWebServer
==================

Master: [![Travis-ci](https://api.travis-ci.org/alesharik/AlesharikWebServer.svg?branch=master)](https://travis-ci.org/alesharik/AlesharikWebServer) [![codecov.io](https://codecov.io/github/alehsarik/AlesharikWebServer/coverage.svg?branch=master)](https://codecov.io/github/alesharik/AlesharikWebServer?branch=master)

Develop: [![Travis-ci](https://travis-ci.org/alesharik/AlesharikWebServer.svg?branch=develop)](https://travis-ci.org/alesharik/AlesharikWebServer) [![codecov.io](https://codecov.io/github/alehsarik/AlesharikWebServer/coverage.svg?branch=develop)](https://codecov.io/github/alesharik/AlesharikWebServer?branch=develop)

Status: [![Codacy Badge](https://api.codacy.com/project/badge/Grade/a8dc12bb283444998223d228061fa7a2)](https://www.codacy.com/app/alesharik/AlesharikWebServer?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=alesharik/AlesharikWebServer&amp;utm_campaign=Badge_Grade) 

AlesharikWebServer is a web server with unique module system.
WARNING! This server might not work work on windows/mac!

Build
=====
1. `git clone https://github.com/alesharik/AlesharikWebServer.git`
2. `mvn clean install`
3. Copy `alesharikwebserver-main-VERSION-jar-with-dependencies.jar` and `alesharikwebserver-api-VERSION-jar-with-dependencies.jar` from `out/production`
4. For Linux users: `sudo -s java -javaagent:alesharikwebserver-api-VERSION-jar-with-dependencies.jar -jar alesharikwebserver-main-VERSION-jar-with-dependencies.jar`
  