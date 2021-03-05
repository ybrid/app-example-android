# app-example-android
Very simple example Android app integrating player-sdk-java to interact with Ybrid®. 

## Requirements
This project is based on the Ybrid® player-sdk-java project.
Dependencies are pulled in automatically.

## Included in this project
This project includes:
* A simple MainActivity with playback controls.
* A sample abstraction of the YbridPlayer to match Android's environment.
* A simple audio backend for Android

## Getting started
The project is ready to for compilation and running. By default it will play a test stream.

The player is used as part of the `MainActivity`. In a real world player, the player should live outside of
any activity. A background service could be useful here.

## Notes
This player prints stack traces on a number of non-fatal causes. This is for demonstration purpose only.
The calls to `printStackTrace()` should be removed accordingly in a real application.

### Supported Android versions

This demo supports API level 26 and above. As of 2021-03-05 the minimum version as supported by Google is 27.
Supporting outdated versions (versions no longer supported upstream) is not within the scope of this demo.
If you need support for those versions you may need to
* use our android-java-compat package located at https://github.com/ybrid/android-java-compat
* (before API level 24) convert to the usage of MediaEndpoint.setAcceptedLanguages() to the old API.

Versions before API level 22 are generally not supported.
Please consider contacting us if you want to use our SDK on API level lower than 24.

## Copyright
Copyright (c) 2019-2021 nacamar GmbH, Germany. See [MIT License](LICENSE) for details.
