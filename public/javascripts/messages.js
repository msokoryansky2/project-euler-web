function updateMessage(message) {
    update(message, "message");
}

function updateError(message) {
    update(message, "message error");
    $("#error").html(timestamp() + " " + message);
}

function clearError() {
    $("#error").html("");
}

function updateTrace(message) {
    update(message, "message trace");
}

function update(message, classes) {
    $("#messages").append( "<div class=\"" + classes + "\">" + timestamp() + " " + message + "</div>");
    $('#messages').animate({scrollTop: $('#messages').prop("scrollHeight")}, 500);
}

function timestamp() {
    var d = new Date();
    return d.toTimeString().split(' ')[0];
}
