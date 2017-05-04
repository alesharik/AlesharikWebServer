AlesharikWebServer
==================

Master: ![Travis-ci](https://api.travis-ci.org/alesharik/AlesharikWebServer.svg?branch=master)

Develop: ![Travis-ci](https://travis-ci.org/alesharik/AlesharikWebServer.svg?branch=develop)

AlesharikWebServer is a web server with unique module system.
WARNING! This server might not work work on windows/mac!

Build
=====
1. `git clone https://github.com/alesharik/AlesharikWebServer.git`
2. `mvn clean install`
3. Copy `alesharikwebserver-main-VERSION-jar-with-dependencies.jar` and `alesharikwebserver-api-VERSION-jar-with-dependencies.jar` from `out/production`
4. For Linux users: `sudo -s java -javaagent:alesharikwebserver-api-VERSION-jar-with-dependencies.jar -jar alesharikwebserver-main-VERSION-jar-with-dependencies.jar`
  