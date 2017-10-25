$(document).ready(function(e) {
    $("#what-is-this").click(function() {
        $("#overlay").show();
    });

    $("#overlay").click(function() {
        $("#overlay").hide();
    });
});