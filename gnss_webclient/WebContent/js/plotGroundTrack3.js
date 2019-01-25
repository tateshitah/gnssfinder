/**
 * 
 * plotGroundTrack3.js
 * 
 * This JavaScript is for groundTrack.html. groundTrack.html is for Android
 * application GNSSFinder. The application's Map function will call by using
 * following URL "https://braincopy.org/gnssws/groundTrack.html?gnss=***"
 * 
 * OpenLayers API ver. 5.3.0
 * 
 * Hiroaki Tateshita 
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
 * 
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
 * this array is data (the object of the Satellite class) 
 * from satellite database from text file "satelliteDataBase.txt".
 */
var satNo = new Array();

var isDrawn = false;



function initialize() {


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


}

function startPlot(gnssString) {
	$("#loading")
		.append(
			'<p style="font-family:arial;color:red;">now loading <img src="res/drawable/loading.gif"></p>');

	update_timeout = setTimeout(function () {
		//alert("here click event #2");
		var url_Date_temp = $('#datepicker').val();
		if (url_Date_temp != "") {
			var url_Time_temp = $('#timepicker').val();
			if (url_Time_temp != "") {
				url_DateTime = url_Date_temp + "_" + url_Time_temp;
			}
		}
		var url = "https://" + url_string + "/gnssws/groundTrack?" + "dateTime="
			+ url_DateTime + "&gnss=" + gnssString
			+ "&format=jsonp&term=86400&step=900";
		load_src(url);
	}, 200);
}

window.callback = function (data) {
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
		content: text2,
		position: latLng
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
			lines.forEach(function (ele, index, array) {
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
	const url =
		'http://127.0.0.1:5500/WebContent/assets/satelliteDataBase.txt';
	//var url = 'https://braincopy.org/WebContent/assets/satelliteDataBase.txt';
	httpReq.open("GET", url, true);
	httpReq.send(null);
}

/**
 * 
 * @param values are the data array from json data of gnssws/groundTrack web api.
 * 
 */
function createAndDrawTrackCoordinateArray(values) {

	/*
	const test_ele = [
		[169.719785, 53.923339],
		[-178.87634, 55.489269],
		[171.418013, 56.617763],
		[-162.138846, 58.298914]];
*/

	/**
	 * trackCoordinatesArray will be updated in the 
	 * createTrackDoordinateArray function.
	 */
	values.forEach(createTrackCoordinateArray);

	trackCoordinatesArray.forEach(function (ele, index, array) {
		if (ele.length > 0) {
			let lineStrings = new ol.geom.MultiLineString([]); // line instance of the path
			let lineStrArray = new Array();                    // line data array as lineString format [[pt0,pt1],[pt1,pt2],[pt2,pt3],....]
			let lineStrArray2 = new Array();

			//lineStrArray2 = groundTrackFeatures(test_ele);
			lineStrArray2 = groundTrackFeatures(ele);

			lineStrArray2.forEach(function (val, i) {
				lineStrArray.push(val);
				lineStrings.setCoordinates(lineStrArray);
				trackLineArray.push(new ol.Feature(lineStrings.transform('EPSG:4326', 'EPSG:3857')));
			});

		}

		/*
		
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
*/
	});

	function convertCoordinate(longitude, latitude) {
		return ol.proj.transform([longitude, latitude], "EPSG:4326", "EPSG:900913");
	}
	function pointStyleFunction(feature, resolution) {
		return new ol.style.Style({
			image: new ol.style.Icon({
				src: 'res/drawable/qzss.gif',
				scale: 0.3
			}),
			text: new ol.style.Text({
				textAlign: 'center',
				textBaseline: 'middle',
				font: 'Arial',
				text: feature.get('name'),
				fill: new ol.style.Fill({ color: 'yellow' }),
				stroke: new ol.style.Stroke({ color: 'blue', width: 5 }),
				offsetX: 0,
				offsetY: 0,
				rotation: 0
			})
		});
	}
	var marker_array = [];
	var marker = new ol.Feature({
		geometry: new ol.geom.Point(convertCoordinate(139.622304, 35.7049394)),
		name: 'Location'
	});
	marker_array.push(marker);
	var markerSource = new ol.source.Vector({
		features: marker_array
	});
	var rabelLayer = new ol.layer.Vector({
		source: markerSource,
		style: pointStyleFunction
	});
	var osmLayer = new ol.layer.Tile({
		source: new ol.source.OSM()
	});


	const vectorSource = new ol.source.Vector({
		features: trackLineArray,
	}); // vector layer 用のソースの作成

	// 経路用の vector layer の作成
	const lineVector = new ol.layer.Vector({
		source: vectorSource,
		style: new ol.style.Style({
			stroke: new ol.style.Stroke({ color: 'blue', width: 2 })
		})
	});

	var map = new ol.Map({
		layers: [osmLayer, rabelLayer, lineVector],
		target: document.getElementById('map'),
		view: new ol.View({
			center: convertCoordinate(131.129172, 32.068235),
			zoom: 2
		})
	});

	//alert("#4 here!!");

	/*
	 * Event when click
	 */
	map.on('click', function (event) {
		//alert('here!!#1');
		if (!isDrawn) {
			startPlot(paramms[1]);
			isDrawn = true;
		}
	});

	/*
	 * Event when double click
	 */
	/*
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
*/
	var select = new ol.interaction.Select();
	map.addInteraction(select);
	select.on('select', function (e) {
		if (e.target.getFeatures().getLength() > 0) {
			var pagename = e.target.getFeatures().item(0).get('name');
			window.location.href = 'Wiki.jsp?page=' + pagename;
		}
	});

	$("#loading").fadeOut(500);
}

/**
 * input data to trackCoordinatesArray from values gotten via gnssws with jsonp format.
 * @param e
 *            element of values
 * @param index_val
 */
function createTrackCoordinateArray(e, index_val) {
	satArray.some(function (ele_sat, i) {
		if (e.SatObservation.SatelliteNumber == ele_sat.catNo) {
			if (e.SatObservation.Sensor.SensorLocation.Longitude < 0) {
				trackCoordinatesArray[i][satNo[i]] = [
					e.SatObservation.Sensor.SensorLocation.Longitude,
					e.SatObservation.Sensor.SensorLocation.Latitude];

			} else {
				trackCoordinatesArray[i][satNo[i]] = [
					e.SatObservation.Sensor.SensorLocation.Longitude,
					e.SatObservation.Sensor.SensorLocation.Latitude];
			}
			satNo[i]++;
			return;
		}
	});
}

//google.maps.event.addDomListener(window, 'load', initialize);
