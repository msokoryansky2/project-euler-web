function initMap() {
    var usCenter = {lat: 39.8283, lng: -98.5795};       // lat/lng of geocenter of US
    var map = new google.maps.Map(document.getElementById('map'), { zoom: 0, center: usCenter });
    var marker = new google.maps.Marker({ position: usCenter, map: map });
}