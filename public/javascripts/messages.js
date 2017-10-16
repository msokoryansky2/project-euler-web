function updateMessage(message) {
    update(message, "message");
}

function updateError(message) {
    update(message, "message error");
}

function update(message, classes) {
    $("#messages").append( "<div class=\"" + classes + "\">" + timestamp() + " " + message + "</div>");
}

function timestamp() {
    var d = new Date();
    return d.toTimeString().split(' ')[0];
}