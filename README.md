# xplane-fotografi
A photos viewer synchronized with current flight location on xPlane.

# Screenshot
I am currently in the SAWP airport in Argentina.

![Screenshot](https://raw.githubusercontent.com/miqwit/xplane-fotografi/master/screenshots/capture_01.png)

# Installation and usage

## Configure xPlane
To run properly, xPlane must send the latitude and longitude datarefs. The following configuration is for xPlane 11, but it is very similar for previous version.

1. Run xPlane and start a flight
2. Go to Settings, Output data, Main output data
3. Select the Network via UDP *20 Latitude, Longitude, & Altitude*
4. On the right panel, in network settings, put the IP address of the computer where xPlane Fotografi will be running (it can be the same computer or any other one) and any port (suggestion: 1000)

## Run xPlane Fotografi
This is a Java project. You need Java JRE (or JDK) to run this program.
1. [Download Java here](https://www.java.com/fr/download/).
2. Install Java
3. Download [xPlaneFotografi.zip](https://github.com/miqwit/xplane-fotografi/blob/master/xPlaneFotografi.zip) and extract it anywhere
4. Run the .jar archive with Java. An interface will pop up
5. In the settings tab, put the IP address of the computer running xPlane (it can be the same computer) and the port as set in previous part (when I suggested 10000)

![Screenshot](https://raw.githubusercontent.com/miqwit/xplane-fotografi/master/screenshots/capture_02.png)

# Technical details

This program uses the [Flickr Photo Search API](flickr.com/services/api/explore/flickr.photos.search), requesting by latitude and longitude.

I used the following very useful libraries to complete this project:
- [flickr4java](https://github.com/boncey/Flickr4Java): a great interface to request Flickr API from bouncey
- [scribe](https://github.com/scribejava/scribejava): an OAuth library, used by flickr4java
- log4j
- the inimitable swing-layout

# Feedbacks

Please drop me a line about this utility on the [xPlane.org download section](http://forums.x-plane.org/index.php?/files/file/36841-xplanefotografi/)
