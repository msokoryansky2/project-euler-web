$(document).ready(function(e) {
    $("#username").change(function() {
        $.getJSON("user/" + encodeURIComponent($("#username").text()));
    })
    .keypress(function(e) {
        if ((e.keyCode || e.which) == 13) {
            $.getJSON("user/" + encodeURIComponent($("#username").text()));
        }
    });
});