$(document).ready(function(e) {
    $("#username").change(function() {
        $.getJSON("user/" + encodeURIComponent($("#username").val()));
    })
    .keypress(function(e) {
        if ((e.keyCode || e.which) == 13) {
            $.getJSON("user/" + encodeURIComponent($("#username").val()));
        }
    });
});