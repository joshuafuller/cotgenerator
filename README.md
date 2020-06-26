# Cursor On Target Generator

## Note
This is the sister app of [CoT Beacon](https://github.com/jonapoul/cotbeacon). CoT Generator generates fake data, CoT Beacon generates a single self icon only.

## Quick Start
1. Download the installer APK from [the GitHub releases page](https://github.com/jonapoul/cotgenerator/releases), then copy the file to your device and open it in a file browser to install.
2. Open the app and grant permissions for GPS access.
3. Configure as required (see below for more explanation).
4. Tap the green "start" icon in the right hand side of the upper toolbar. This begins the configured packet transmissions.
5. When finished, tap the red "stop" icon on the toolbar, or the "STOP" button on the service notification.

## Function
This app generates a specified number of representative CoT PLI tracks around a configured latitude/longitude point. These tracks are randomly scattered within the configured "Radial Distribution" and are given randomised ATAK callsigns. After each transmission, each track is shifted slightly to a random position dependent on the icon movement speed. CoT Generator is mostly intended for network/server stress testing, but could also be useful for demonstration purposes.

It supports TCP and UDP traffic, but (currently!) includes no validation of connections for TCP traffic, so make sure you've got the right IP/port.

Similarly, if you're travelling between different networks whilst the server is running (e.g. losing Wi-Fi signal then regaining it), you should expect it to stop working as intended. Just stop/start the service and it should work again.

## Troubleshooting
Note that 100% of testing has been done on a OnePlus 6 running Android 10. If there are any compatibility issues or crashes (I'm sure there are), please raise an issue!
