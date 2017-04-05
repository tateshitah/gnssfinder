/**
 * 
 * plotGroundTrack2.js
 * 
 * This JavaScript is for sample05.html. sample05.html is for Android
 * application GNSSFinder. The application's Map function will call by using
 * following URL "http://braincopy.org/WebContent/sample05.html?gnss=***"
 * 
 * Google Map API Ver.3
 * 
 * K.Someya, Hiroaki Tateshita Reference from
 * http://www.ajaxtower.jp/googlemaps/ Reference from
 * https://developers.google.com/maps/documentation/javascript/events?hl=ja
 * 
 */

var map;
var gnssString = "JE";
var url_DateTime = "2014-03-01_00:00:00";
var update_timeout = null;
// var url_string = "localhost:8080";
var url_string = "braincopy.org";

/*
 * two dimensional array the number of trackCoordinatesArray[] is the number of
 * satellite the number of trackCoordinatesArray[][] is the number of the data
 * of each satellite.
 */
var trackCoordinatesArray = new Array();

/*
 * trackLine is PolyLine of each satellite. the number of this trackLineArray is
 * the number of satellite.
 */
var trackLineArray = new Array();

/*
 * this array is Marker including satellite image. so the number of this array
 * is the number of satellite.
 */
var markerArray = new Array();

/*
 * this array is data from satellite database from text file
 */
var satArray = new Array();

/*
 * this array is the number of data of each satellite get from web api
 */
var satNo = new Array();

var isDrawn = false;

function initialize() {

	/* Setting for initial map info. */
	var mapOptions = {
		zoom : 2,
		center : new google.maps.LatLng(32.068235, 131.129172),
		mapTypeId : google.maps.MapTypeId.SATELLITE,
		disableDoubleClickZoom : true,
		streetViewControl : false,
		mapTypeControl : false
	};

	/* Generating map */
	map = new google.maps.Map(document.getElementById('map_canvas'), mapOptions);

	/* Initializing track coordinates array */

	roadSatellite();

	/* Setting Current date and time */
	var currentDateTime = new Date();
	var dateStr = "";
	var timeStr = "";
	dateStr = currentDateTime.getUTCFullYear() + "-";
	if (currentDateTime.getUTCMonth() < 9) {
		dateStr += "0";
	}
	dateStr += (currentDateTime.getUTCMonth() + 1) + "-";
	if (currentDateTime.getUTCDate() < 10) {
		dateStr += "0";
	}
	dateStr += currentDateTime.getUTCDate();
	$('#datepicker').val(dateStr);

	if (currentDateTime.getUTCHours() < 10) {
		timeStr = "0";
	}
	timeStr += currentDateTime.getUTCHours() + ":";
	if (currentDateTime.getUTCMinutes() < 10) {
		timeStr += "0";
	}
	timeStr += currentDateTime.getUTCMinutes() + ":";
	if (currentDateTime.getUTCSeconds() < 10) {
		timeStr += "0";
	}
	timeStr += currentDateTime.getUTCSeconds();
	$('#timepicker').val(timeStr);

	var url = location.href;
	params = url.split("?");
	paramms = params[1].split("=");

	/*
	 * input check
	 */
	if (!paramms[1].match(/^[E-R]+$/)) {
		paramms[1] = "J";
		// alert("OK: "+paramms[1]);
	}

	if (!isDrawn) {
		startPlot(paramms[1]);
		isDrawn = true;
	}

	/*
	 * Event when click
	 */
	google.maps.event.addListener(map, 'click', function(event) {
		if (!isDrawn) {
			startPlot(paramms[1]);
			isDrawn = true;
		}
	});

	/*
	 * Event when double click
	 */
	google.maps.event.addListener(map, 'dblclick', function(event) {
		clearTimeout(update_timeout);
		// alert("here doubleclick event");
		trackCoordinatesArray.forEach(function(ele, index, array) {
			trackLineArray[index].setMap(null);
			markerArray[index].setMap(null);
			trackCoordinatesArray[index] = new Array();
		});
		for (var i = 0; i < satArray.length; i++) {
			satNo[i] = 0;
		}
		isDrawn = false;
	});

}

function startPlot(gnssString) {
	$("#loading")
			.append(
					'<p style="font-family:arial;color:red;">now loading <img src="res/drawable/loading.gif"></p>');

	update_timeout = setTimeout(function() {
		// alert("here click event");
		var url_Date_temp = $('#datepicker').val();
		if (url_Date_temp != "") {
			var url_Time_temp = $('#timepicker').val();
			if (url_Time_temp != "") {
				url_DateTime = url_Date_temp + "_" + url_Time_temp;
			}
		}
		var url = "http://" + url_string + "/gnssws/groundTrack?" + "dateTime="
				+ url_DateTime + "&gnss=" + gnssString
				+ "&format=jsonp&term=86400&step=900";
		load_src(url);
	}, 200);
}

window.callback = function(data) {
	// plotAllSatellites(data.values);
	createAndDrawTrackCoordinateArray(data.values);
};

function load_src(url) {
	var script = document.createElement('script');
	script.src = url;
	document.body.appendChild(script);
}

function colorString(value) {
	return '#FF4040';
}

function addInfowindow(text, latLng) {
	/* Add information window at click point */
	var text2 = url_Date + ": " + url_DataSource + "=" + text;
	var infowindow = new google.maps.InfoWindow({
		content : text2,
		position : latLng
	});
	infowindow.open(map);
}

/**
 * class for satellite.
 */
function Satellite(_catNo, _rnxStr, _imgStr, _description) {
	this.catNo = _catNo;
	this.rnxStr = _rnxStr;
	this.imgStr = _imgStr;
	this.description = _description;
}

/**
 * road satellite data from text file. output is array of Satellite objects.
 * This method contains initialization of trackCoordinatesArray and satArray.
 */
function roadSatellite() {

	var httpReq = new XMLHttpRequest();
	httpReq.onreadystatechange = function callback_inRoadSatDB() {
		var lines = new Array();
		if (httpReq.readyState == 4 && httpReq.status == 200) {
			// road database
			lines = httpReq.responseText.split("\n", 50);
			ele_line = new Array();
			lines.forEach(function(ele, index, array) {
				ele_line = ele.split("\t", 5);
				satArray[index] = new Satellite(ele_line[0], ele_line[1],
						ele_line[2], ele_line[3]);
			});
			for (var i = 0; i < satArray.length; i++) {
				trackCoordinatesArray[i] = new Array();
			}
			for (var i = 0; i < satArray.length; i++) {
				satNo[i] = 0;
			}
		}
	};
	// var url =
	// 'http://localhost:8080/gnss_webclient/assets/satelliteDataBase.txt';
	var url = 'http://braincopy.org/WebContent/assets/satelliteDataBase.txt';
	httpReq.open("GET", url, true);
	httpReq.send(null);
}

/**
 * 
 * @param values
 */
function createAndDrawTrackCoordinateArray(values) {
	values.forEach(createTrackCoordinateArray);
	trackCoordinatesArray.forEach(function(ele, index, array) {
		trackLineArray[index] = new google.maps.Polyline({
			path : trackCoordinatesArray[index],
			strokeColor : '#FF4040',
			strokeOpacity : 1.0,
			strokeWeight : 2
		});
		trackLineArray[index].setMap(map);

		var image = new google.maps.MarkerImage('res/drawable/ic_star.png',
				new google.maps.Size(40, 40), new google.maps.Point(0, 0),
				new google.maps.Point(10, 10), new google.maps.Size(20, 20));
		if (satArray[index].imgStr == "qzss") {
			image = new google.maps.MarkerImage('res/drawable/qzss.gif',
					new google.maps.Size(300, 160),
					new google.maps.Point(0, 0), new google.maps.Point(30, 20),
					new google.maps.Size(80, 40));
		} else if (satArray[index].imgStr == "galileo") {
			image = new google.maps.MarkerImage('res/drawable/galileo.gif',
					new google.maps.Size(300, 160),
					new google.maps.Point(0, 0),
					new google.maps.Point(60, 22.5), new google.maps.Size(90,
							45));
		} else if (satArray[index].imgStr == "galileofoc") {
			image = new google.maps.MarkerImage('res/drawable/GalileoFOC.gif',
					new google.maps.Size(300, 160),
					new google.maps.Point(0, 0),
					new google.maps.Point(60, 22.5), new google.maps.Size(90,
							45));
		} else if (satArray[index].imgStr == "gpsBlockIIF") {
			image = new google.maps.MarkerImage('res/drawable/IIF.gif',
					new google.maps.Size(300, 160),
					new google.maps.Point(0, 0),
					new google.maps.Point(60, 22.5), new google.maps.Size(80,
							40));
		}
		markerArray[index] = new google.maps.Marker({
			position : trackCoordinatesArray[index][0],
			map : map,
			icon : image
		});
		google.maps.event.addListener(markerArray[index], 'click', function() {
			new google.maps.InfoWindow({
				content : satArray[index].description,
				position : trackCoordinatesArray[index][0]
			}).open(markerArray[index].getMap());
		});

	});
	$("#loading").fadeOut(500);
}

/**
 * 
 * @param e
 *            element of values
 * @param index_val
 */
function createTrackCoordinateArray(e, index_val) {
	satArray.some(function(ele_sat, i) {
		if (e.SatObservation.SatelliteNumber == ele_sat.catNo) {
			trackCoordinatesArray[i][satNo[i]] = new google.maps.LatLng(
					e.SatObservation.Sensor.SensorLocation.Latitude,
					e.SatObservation.Sensor.SensorLocation.Longitude);
			satNo[i]++;
			return;
		}
	});
}

google.maps.event.addDomListener(window, 'load', initialize);
