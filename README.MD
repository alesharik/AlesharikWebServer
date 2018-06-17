AlesharikWebServer
==================
<table>
<tr>
<td>    

Master:<br>
[![Travis-ci](https://api.travis-ci.org/alesharik/AlesharikWebServer.svg?branch=master)](https://travis-ci.org/alesharik/AlesharikWebServer) <br>
[![codecov](https://codecov.io/gh/alesharik/AlesharikWebServer/branch/master/graph/badge.svg)](https://codecov.io/gh/alesharik/AlesharikWebServer)
</td>

<td rowspan="4">

![Logo](https://raw.githubusercontent.com/wiki/alesharik/AlesharikWebServer/logo_static_scaled.png)
</td>
</tr>
<tr>
<td>

Develop: <rb>
[![Travis-ci](https://travis-ci.org/alesharik/AlesharikWebServer.svg?branch=develop)](https://travis-ci.org/alesharik/AlesharikWebServer) <br>
[![codecov](https://codecov.io/gh/alesharik/AlesharikWebServer/branch/develop/graph/badge.svg)](https://codecov.io/gh/alesharik/AlesharikWebServer)
</td>
</tr>
<tr>
<td>

Status: <br>
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a8dc12bb283444998223d228061fa7a2)](https://www.codacy.com/app/alesharik/AlesharikWebServer?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=alesharik/AlesharikWebServer&amp;utm_campaign=Badge_Grade) 
</td>
</tr>
<tr>
<td>

JCenter: <br>
 [![Download](https://api.bintray.com/packages/alesharik/com.alesharik/alesharikwebserver/images/download.svg)](https://bintray.com/alesharik/com.alesharik/alesharikwebserver/_latestVersion)
</td>
</tr>
</table>
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