# HTTP Ping Emulation

Make your SmartThings aware of the availability of things on your network.  This simple device attempts to open an HTTP GET request to a port of your choice on a LAN IP address of your choice.  No response?  The device will report "Down" in the status.  Did the port respond with valid content (even a 404 or Authentication Required?), then the device will report "Up" and the round trip packet time in the "ttl" field. 

## Installation

1. Create a new device type (https://graph.api.smartthings.com/ide/devices)
    *  Name: Ping
    *  Author: Kristopher Kubicki
    *  Capabilities:
      * Polling

2. Create a new device (https://graph.api.smartthings.com/device/list)
   * Name: Your Choice
   * Device Network Id: Your Choice
   *   Type: Ping (should be the last option)
   *   Location: Choose the correct location
   *   Hub/Group: Choose the correct Hub
 
3. Update device preferences
    * Click on the new device to see the details.
    * Click the edit button next to Preferences
    * Fill in the IP address and Port of the device you wish to "ping". I.e., 192.168.1.1, 80
    * You can only create one of these devices per IP:Port 

4. Hit Publish -> For Me
