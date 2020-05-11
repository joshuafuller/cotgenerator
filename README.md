# Cursor On Target Generator

## Quick Start
1. Download the installer APK from [the GitHub releases page](https://github.com/jonapoul/cotgenerator/releases), then copy the file to your device and open it in a file browser to install.
2. Open the app and grant permissions for GPS access.
3. Configure as required (see below for more explanation).
4. Tap the green "start" icon in the right hand side of the upper toolbar. This begins the configured packet transmissions.
5. When finished, tap the red "stop" icon on the toolbar, or the "STOP" button on the service notification.

## GPS Beacon Mode
This mode reads the device's GPS location and translates that to Cursor on Target, then periodically transmits that single packet to the specified destination.

GPS Beacon mode also includes the ability to send "911 Emergency" packets located on your current location. This is found by tapping the red Floating Action Button in the bottom right, then selecting "START EMERGENCY". These are single-transmission events, so they are not periodically re-transmitted unless you explicitly select the option multiple times. When finished, select the "CANCEL EMERGENCY" option to remove the emergency icon from your team's TAK map screen.

Note that there is a slight difference in behaviour between TAK Server and FreeTakServer (FTS) in this cancelling functionality, as of FTS v0.7:
* When TAK Server receives a cancel request, it halts the retransmission of the existing emergency and also removes the icon from the team's TAK map. This removal is almost instantaneous from the point of receiving the cancel request.
* FTS also halts the retransmission, but the "inert" emergency icon still remains on the map screen until it is manually cleared/deleted. FTS also takes up to 10 seconds to deactivate the emergency icon. This deactivation is shown when the icon stops flashing on the map screen, but is still visible.

## Fake Icons Mode
This mode generates a specified number of representative CoT PLI tracks around a configured latitude/longitude point. These tracks are randomly scattered within the configured "Radial Distribution". After each transmission, each track is shifted slightly to a random position within their configured "Movement Radius".

This mode is mostly intended for network/server stress testing, but could also be useful for demonstration purposes.
