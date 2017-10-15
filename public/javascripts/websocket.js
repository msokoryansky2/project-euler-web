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
        updateMessage("Websocket closed");
    }

    function onMessage(evt) {
        //console.log(evt.data);
        var data = JSON.parse(evt.data);
        if (!!data && !!data.type) switch(data.type) {
            case "system_status":
                updateSystemStatus(data);
                break;
            case "message":
                updateMessage(data);
                break;
             case "error":
                updateError(data);
                break;
             default:
                console.log("Unknown message type: " + data.type)
                break;
        }
    }

    function onError(evt) {
        updateError(evt.data);
        websocket.close();
    }

    function doSend(message) {
        websocket.send(message);
    }

    function updateMessage(data) {
        if (!!data && !!data.message) $("#Message").removeClass("Error").html(data.message);
    }

    function updateError(data) {
        if (!!data && !!data.message) $("#Message").addClass("Error").html(data.message);
    }

    function updateSystemStatus(data) {
        if (!!data && !!data.memoryFree && !!data.memoryMax) memoryGaugeUpdate(data.memoryFree, data.memoryMax);
    }
})

