$(document).ready(function(e) {
    $("button.euler-problem").click(function() {
        var id = $(this).attr('data-id');
        problemClear(id);
        $("button#problem_" + id).removeClass("unsolved solved unsolved").addClass("in-progress");
        $.getJSON("project_euler/" + id, processProblemResponse).fail(function() { problemError(id, "HTTP Error :(") });
    })
})

function isAnswered(id) {
    return ($("span#answer_" + id).text()).length > 0;
}

function isAnsweredNumerically(id) {
    return isAnswered(id) && !Number.isNaN($("span#answer_" + id).text);
}

function processProblemResponse(data) {
    for (var i = 0; i < data.length; i++) {
        var solution = data[i];
        processSolution(solution);
    }
}

function processSolution(solution) {

    console.log(solution);

    // Sanity check that we are truly processing a solution message
    if (!solution || !solution.type || solution.type != "solution" || !solution.problemNumber) return;

    console.log("A: " + solution.problemNumber);

    // If this problem is already solved with a numeric response then we ignore this new solution
    if (isAnsweredNumerically(problemNumber)) return;

    console.log("B: " + solution.problemNumber);

    if (!!solution.answer) {
        if (!Number.isNaN(solution.answer)) {
            problemSuccess(solution.problemNumber, solution.answer, solution.isMine, solution.by);
        } else {
            problemError(solution.problemNumber, solution.answer);
        }
    } else {
        problemError(solution.problemNumber, "Error :(");
    }
}

function problemClear(id) {
    $("button#problem_" + id).removeClass("in-progress solved error").addClass("unsolved");
    $("span#duration_" + id).text("");
    $("span#by_" + id).text("");
    $("span#answer_" + id).text("");
}

function problemError(id, responseText) {
    $("span#answer_" + id).text(responseText);
    $("span#duration_" + id).text("");
    $("span#by_" + id).text("");
    $("button#problem_" + id).removeClass("in-progress solved unsolved").addClass("error");
}

function problemSuccess(id, answer, isMine, by, duration) {
    $("span#answer_" + id).text(answer);
    $("span#duration_" + id).text("" + duration + " sec");
    $("span#by_" + id).text(isMine ? "" : by);
    $("button#problem_" + id).removeClass("in-progress unsolved error").addClass("solved");
}
