var websocket = false;

$(document).ready(function(e) {

    websocket = openWebsocket();

    function getWsUri() {
        var loc = window.location;
        var wsUri = (loc.protocol === "https:") ? "wss:" : "ws:";
        wsUri += "//" + loc.host;
        wsUri += loc.pathname + "system_monitor";
        return wsUri;
    }

    function openWebsocket() {
        websocket = new WebSocket(getWsUri());
        updateMessage("Websocket created");
        websocket.onopen = function(evt) { onOpen(evt) };
        websocket.onclose = function(evt) { onClose(evt) };
        websocket.onmessage = function(evt) { onMessage(evt) };
        websocket.onerror = function(evt) { onError(evt) };
        return websocket;
    }

    function onOpen(evt) {
        updateMessage("Websocket opened");
    }

    function onClose(evt) {
        updateError("Lost backend connectivity. Backend likely down. Websocket closed.");
    }

    function onMessage(evt) {
        //console.log(evt.data);
        var data = JSON.parse(evt.data);
        if (!!data && !!data.type) switch(data.type) {
            case "system_status":
                updateTrace("JVM used memory: " +  data.memoryUsed + "MB");
                updateSystemStatus(data);
                break;
            case "message":
                var message = (!!data.message) ? data.message : data;
                updateMessage(message);
                break;
             case "error":
                var message = (!!data.message) ? data.message : JSON.stringify(data);
                updateError("Application error: " + message);
                break;
             default:
                updateError("Unknown message type " + data.type + " in message " + JSON.stringify(data));
                break;
        }
    }

    function onError(evt) {
        updateError("Websocket error event: " + JSON.stringify(evt));
        websocket.close();
    }

    function doSend(message) {
        websocket.send(message);
    }

    function updateSystemStatus(data) {
        if (!!data && !!data.memoryFree && !!data.memoryMax) memoryGaugeUpdate(data.memoryFree, data.memoryMax);
        systemStatusPing();
    }
})

function isWebsocketClosed() {
   return (!websocket || websocket.readyState === websocket.CLOSED || websocket.readyState === websocket.CLOSING);
}