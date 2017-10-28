const INTERVAL_PERSIST_MARKER_SEC = 60;

var markersOthers = [];
var map = false;

function initMap() {
    var usCenter = {lat: 39.8283, lng: -98.5795};       // lat/lng of geocenter of US
    map = new google.maps.Map(
        document.getElementById('map'),
        {
            zoom: 0,
            center: usCenter,
            disableDefaultUI: true
        }
    );
    // Show self
    requestIp2Geo("");
}

function requestIp2Geo(ip) {
    // If ip is specified, make a stub with it in markersOthers.
    // This way receiveIp2Geo can know if it's own IP (no corresponding stub) or another's IP.
    if (!!ip) {
        // Don't repeat already shown IPs
        if (!!markersOthers.ip) return;
        markersOthers.ip = false;
    }
    $.getJSON("https://ipapi.co/" + (!!ip ? ip + "/" : "") + "/json/", receiveIp2Geo);
}

function receiveIp2Geo(data) {
    if (!data.ip) return;
    var ip = data.ip;
    // If we have a stub for this ip, then it's another's IP. Otherwise we assume it's our IP.
    if (!!markersOthers.ip) {
        showMarker(data, true);
    } else {
        showMarker(data, false);
    }
}

function showMarker(geoData, markerOthers) {
    // Sanity check that we have something to plot
    if (!geoData || !geoData.latitude || !geoData.longitude) return;
    var ip = geoData.ip;
    // If we already have a marker for this IP, then do nothing
    if (!!markersOthers.ip && !!markersOthers.ip.marker) return;
    // Create a new marker
    var marker = new google.maps.Marker({
        // Red marker for self, blue markers for others
        icon: (markerOthers ?
                    "http://maps.google.com/mapfiles/ms/icons/green-dot.png" :
                    "http://maps.google.com/mapfiles/ms/icons/blue-dot.png"),
        map: map,
        animation: google.maps.Animation.DROP,
        position: {lat: geoData.latitude, lng: geoData.longitude}
    });
    map.setZoom(5);
    map.panTo(marker.position);
    // Save the marker in the marker array for future purposes and have a callback to remove it after a timeout
    if (markerOthers) {
        markersOthers.ip.marker = marker;
        setTimeout(function() {
            marker.setMap(null);
            marker = null;
            delete markerOthers.ip;
         }, INTERVAL_PERSIST_MARKER_SEC);
    }
}