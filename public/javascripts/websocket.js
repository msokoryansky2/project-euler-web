$(document).ready(function(e) {

    var websocket = openWebsocket();

    function getWsUri() {
        var loc = window.location;
        var wsUri = (loc.protocol === "https:") ? "wss:" : "ws:";
        wsUri += "//" + loc.host;
        wsUri += loc.pathname + "system_monitor";
        return wsUri;
    }

    function openWebsocket() {
        websocket = new WebSocket(getWsUri());
        websocket.onopen = function(evt) { onOpen(evt) };
        websocket.onclose = function(evt) { onClose(evt) };
        websocket.onmessage = function(evt) { onMessage(evt) };
        websocket.onerror = function(evt) { onError(evt) };
        return websocket;
    }

    function onOpen(evt) {
        updateStatus("Websocket opened");
    }

    function onClose(evt) {
        updateStatus("Websocket closed");
    }

    function onMessage(evt) {
        updateStatus(evt.data + ' MB');
    }

    function onError(evt) {
        updateStatus("Error: " + evt.data);
        websocket.close();
    }

    function doSend(message) {
        websocket.send(message);
    }

    function updateStatus(message) {
        //console.log(message);
        $("#SystemStatusFreeMem").html(message)
    }
})

