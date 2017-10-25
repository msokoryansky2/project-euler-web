// Control from http://www.jqueryscript.net/chart-graph/Creating-Animated-Gauges-Using-jQuery-Raphael-js-kumaGauge.html

const PING_HISTORY_LENGTH = 15;

var pingHistory = [];

$(document).ready(function(e) {
    $('.responsiveness-gauge').kumaGauge({
        value: 100,
        radius: 80,
        gaugeWidth: 10,
        showNeedle: false,
        fill: '0-#A9A9A9:0-#008CBA:100',
        paddingY: 15,
        paddingX: 40,
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
            fontWeight: '300',
        },
    });

    // Update responsiveness every 2 seconds
    window.setInterval(function() {
        responsivenessGaugeUpdate();
    }, 2000);
})

function responsivenessGaugeUpdate() {
    // We allow up to 1.0 seconds interval per ping before penalizing responsiveness.
    // We also need to allow for the fact that there may be fewer than PING_HISTORY_LENGTH elements in array.
    // So the algo is:
    // -1. If websocket is done, set value as 0.
    // 0. If pingHistory is empty, set value as 100.
    // 1. Compute appropriate measurement timeWindow as array.length * 1.5
    // 2. Count number of elements timeWindowPings in pingHistory that fall within timeWindow seconds.
    // 3. Approximate responsiveness as = 100 * (timeWindowPings / pingHistory.length)
    var pct;
    // Special case of losing backend connectivity -- kind of hacky but works
    if (isWebsocketClosed()) {
        pct = 0;
    } else if (pingHistory.length <= 0) {
        pct = 100;
    } else {
        var timeWindow = 1.0 * pingHistory.length;
        var timeWindowStart = getCurrentSeconds() - timeWindow;
        var timeWindowPings = pingHistory.filter(function(p){return p >= timeWindowStart}).length;
        pct =  Math.round(100 * timeWindowPings / pingHistory.length);
    }

    $('.responsiveness-gauge').kumaGauge('update', {
      value: pct,
    });
}

function getCurrentSeconds() {
    return Math.round((new Date()).getTime() / 1000);
}

function systemStatusPing() {
    var s = getCurrentSeconds();
    // If it's a repeat value there's an excellent chance that this is a bunch of delayed pings coming through.
    // We ignore such dupes because they do not reflect high responsive -- quite the contrary, in fact.
    if (pingHistory.filter(function(p){return p == s}).length > 0) return;
    // if array is already at PING_HISTORY_LENGTH then we evict (pop) the first element before adding (pushing) this one
    if (pingHistory.length >= PING_HISTORY_LENGTH)  pingHistory.shift();
    pingHistory.push(s);
}