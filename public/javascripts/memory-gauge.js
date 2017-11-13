// Control from http://www.jqueryscript.net/chart-graph/Creating-Animated-Gauges-Using-jQuery-Raphael-js-kumaGauge.html

$(document).ready(function(e) {
    $('.memory-gauge').kumaGauge({
        value: 0,
        radius: 80,
        gaugeWidth: 10,
        showNeedle: false,
        paddingY: 15,
        paddingX: 100,
        valueLabel: {
            display: false,
        },
        label: {
            display: true,
            left: '0%',
            right: '100%',
            fontFamily: 'Open Sans',
            fontColor: '#1E4147',
            fontSize: '10',
            fontWeight: 'normal',
        },
    });
})

function memoryGaugeUpdate(memoryFree, memoryMax) {
    var pct =  Math.round(100 * (memoryMax - memoryFree) / memoryMax);
    $('.memory-gauge').kumaGauge('update', {
      value: pct,
    });
}