$(document).ready(function(e) {
    $("button.euler-problem").click(function() {
        // If this solution is in progress then ignore
        if ($(this).hasClass("in-progress")) return;
        var id = $(this).attr('data-id');
        clearAnswer(id);
        $("button#problem_" + id).removeClass("unsolved solved").addClass("in-progress");
        $.getJSON("project_euler/" + id, problemSolved).fail(function() { problemError(id) });
        wait(id, 0);
    })
})

function clearAnswer(id) {
    $("button#problem_" + id).removeClass("in-progress solved").addClass("unsolved");
    $("span#progress_" + id).text("");
    $("span#answer_" + id).text("");

}

function isAnswered(id) {
    return ($("span#answer_" + id).text()).length > 0;
}

function wait(id, counter) {
    if (isAnswered(id)) return;
    $("span#progress_" + id).text(counter + " sec")
    setTimeout(function() {
        wait(id, counter + 1);
    }, 1000);
}

function problemSolved(data) {
    for (var i = 0; i < data.length; i++) {
        var id = data[i][0];
        var answer = data[i][1];
        $("span#answer_" + id).text(answer);
        // The "solution" could be an error message
        if (Number.isNaN(Number(answer))) {
            $("button#problem_" + id).removeClass("in-progress solved").addClass("unsolved");
        } else {
            $("button#problem_" + id).removeClass("in-progress unsolved").addClass("solved");
        }
    }
}

function problemError(id) {
    $("span#answer_" + id).text("HTTP Error :(");
    $("button#problem_" + id).removeClass("in-progress solved").addClass("unsolved");
}