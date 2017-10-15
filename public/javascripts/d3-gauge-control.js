$(document).ready(function(e) {
    function createMemoryGauge(memoryMax) {
        memoryGauge = gauge('#MemoryGauge', {
            size: 200,
            clipWidth: 200,
            clipHeight: 200,
            ringWidth: 40,
            maxValue: memoryMax,
            transitionMs: 1000,
        });
        memoryGauge.render();
    }

    function updateMemoryGauge(memoryFree, memoryMax) {
        // Create gauge if doesn't already exist
        if (memoryGauge === false) createMemoryGauge(memoryMax);
        // Update with current free memory
        memoryGauge.update(memoryFree);
    }
})
