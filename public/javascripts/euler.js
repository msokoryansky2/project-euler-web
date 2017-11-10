$(document).ready(function(e) {
    $("button.euler-problem").click(function() {
        // If this solution is in progress then ignore
        if ($(this).hasClass("in-progress")) return;
        var id = $(this).attr('data-id');
        clearAnswer(id);
        $("button#problem_" + id).removeClass("unsolved solved").addClass("in-progress");
        $.getJSON("project_euler/" + id, processProblemResponse).fail(function() { problemError(id, "HTTP Error :(") });
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

function isAnsweredNumerically(id) {
    return isAnswered(id) && !Number.isNaN($("span#answer_" + id).text);
}

function wait(id, counter) {
    if (isAnswered(id)) return;
    $("span#progress_" + id).text(counter + " sec")
    setTimeout(function() {
        wait(id, counter + 1);
    }, 1000);
}

function processProblemResponse(data) {
    for (var i = 0; i < data.length; i++) {
        var solution = data[i];
        processSolution(solution);
    }
}

function processSolution(solution) {
    // Sanity check that we are truly processing a solution message
    if (!solution || !solution.type || solution.type != "solution" || !solution.problemNumber) return;

    // If this problem is already solved with a numeric response then we ignore this new solution
    if (isAnsweredNumerically(problemNumber)) return;

    if (!!solution.answer) {
        if (!Number.isNaN(solution.answer) {
            problemSuccess(solution.problemNumber, solution.answer, solution.isMine, solution.by);
        } else {
            problemError(solution.problemNumber, solution.answer);
        }
    } else {
        problemError(solution.problemNumber, "Error :(");
    }
}

function problemError(id, responseText) {
    $("span#answer_" + id).text(responseText);
    $("span#by_" + id).text("");
    $("button#problem_" + id).removeClass("in-progress solved").addClass("unsolved");
}

function problemSuccess(id, answer, isMine, by) {
    $("span#answer_" + id).text(answer);
    $("span#by_" + id).text(isMine ? "" : by);
    $("button#problem_" + id).removeClass("in-progress unsolved").addClass("solved");
}
