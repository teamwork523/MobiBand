# Author
- Haokun Luo (haokun@umich.edu)

# Install MobiBand

## Client

* Connect your Android device to your PC
* Download the zip file and unzip it on your PC
* Type in command "adb install client/bin/MobiBand.apk"
* Launch the application "MobiBand"
* "Hostname/IP" is either the hostname or IP address of your server (for TCP and ICMP)
* "Port #" is the port number that should be consistent with your server (for TCP only)
* "Packet Size" is the single packet size that will be repeated "Total number of Packet" times (for TCP and ICMP)
* "Integr-packet Time" is the inter-packet timing in between two consecutive packets.
  It is fine to leave as "0". (for TCP and ICMP)
* "Total number of Packet" is the total packet number of TCP packet that being send along the "chain" of
  TCP packets (for TCP only)
* "Up" and "Down" button is select either uplink or downlink measurements (for TCP only)
* "TCP" and "ICMP(Ping)" is to choose between TCP or ICMP measurements (for TCP and ICMP)
* "Run" button is to start the measurement (for TCP and ICMP)
* "Stop" button is to stop the measurement (for TCP only)
* NOTE: if your phone doesn't contain a default Ping Program,
        please copy "resource/ping" to "/system/bin/" to your
        device.

## Server

* "scp" the server folder to a remote server, i.e. ep2.eecs.umich.edu
* Make sure no applications are running on the port that MobiBand reserved 
  (default is 10100)
* Run "./start.sh"

# Run the application
## TCP
* Once you start the server process (make sure that server's hostname and
  port number match the "Hostname/IP" and "Port #" fields on the MobiBand)
* Choose either "Up" or "Down" button
* Select "TCP" radio button
* Press "Run"
* Results will be displayed on the screen

## UDP
* Select "ICMP(Ping)" button
* Press "Run"
* Results will be displayed on the screen
