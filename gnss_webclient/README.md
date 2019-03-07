gnss_webclient: GNSS Web client

#Overview
"gnss_webclient" is a "function" of gnssfinder. These files of html and Javascript will provide Map function of GNSSFinder, as well as a user of web service of "gnssws".

	 
#Operation
## adding new satellite system
(Mightbe) add NORAD id of the satellites and information to "satelliteDataBase.txt"

#Design
##Javascript
plotGroundTrack*.js:
trackCoordinatesArray is two dimentional array.

initialize()

  roadSatellite();

  startPlot();

  create url and load_src(url);

  windows.callback() <- the callback of jsonp

  createAndDrawTrackCoordinateArray(data.values)

    createTrackCoordinateArray

##Drawing Rule
GNSS satellite pictures are used in this application. I would like to define rule of pictures here.

1. file: transparent png or gif file
2. width: 300px
3. height: 150px 
4. font size: 25px
5. Center of Mass should be center of picture
