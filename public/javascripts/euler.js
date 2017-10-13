$(document).ready(function(e) {
    $("button.EulerProblem").click(function() {
        // If this solution is in progress then ignore
        if ($(this).hasClass("InProgress")) return;
        var id = $(this).attr('data-id');
        clearAnswer(id);
        $("button#problem_" + id).removeClass("Unsolved Solved").addClass("InProgress");
        $.getJSON("project_euler/" + id, problemSolved).fail(function() { problemError(id) });
        wait(id, 0);
    })
})

function clearAnswer(id) {
    $("button#problem_" + id).removeClass("InProgress Solved").addClass("Unsolved");
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
        $("button#problem_" + id).removeClass("InProgress Unsolved").addClass("Solved");
    }
}

function problemError(id) {
    $("span#answer_" + id).text("Error :(");
    $("button#problem_" + id).removeClass("InProgress Solved").addClass("Unsolved");
}