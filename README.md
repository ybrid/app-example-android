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

## Copyright
Copyright (c) 2019-2021 nacamar GmbH, Germany. See [MIT License](LICENSE) for details.
