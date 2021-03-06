const INTERVAL_PERSIST_MARKER_SEC = 10;

var map = false;

function initMap() {
    var usCenter = {lat: 39.8283, lng: -98.5795};       // lat/long of geocenter of US
    map = new google.maps.Map(
        document.getElementById('map'), {
            zoom: 0,
            center: usCenter,
            disableDefaultUI: true
        }
    );
}

/**
 * Plot a marker (and remove it after the timeout) in response to an event. Defined events:
 * "solution", "user_login"
 */
function mapEvent(event, geo, text) {
    // Sanity check that we have something to plot
    if (!geo || !geo.lat || !geo.long || !event) return;

    // Create a new marker
    var marker = new google.maps.Marker({
        // Red marker for self, blue markers for others
        icon: (event == "solution" ?
                    "http://maps.google.com/mapfiles/ms/icons/green-dot.png" :
                    "http://maps.google.com/mapfiles/ms/icons/blue-dot.png"),
        map: map,
        animation: google.maps.Animation.DROP,
        position: {lat: parseFloat("" + geo.lat), lng: parseFloat("" + geo.long)},
    });

    // Create a new info window
    var infoWindow = false;
    if (!!text) {
        infoWindow = new google.maps.InfoWindow({
            content: text
        });
        infoWindow.open(map, marker);
    }

    map.setZoom(3);
    map.panTo(marker.position);
    // Remove this marker after a timeout
    setTimeout(function() { marker.setMap(null); if (!!infoWindow) infoWindow.close() }, INTERVAL_PERSIST_MARKER_SEC * 1000);
}