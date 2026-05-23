(function () {
    var KEY = 'tixcore-theme';
    function apply(t) {
        document.documentElement.setAttribute('data-theme', t);
    }
    var saved = localStorage.getItem(KEY) || 'light';
    apply(saved);

    window.toggleTheme = function () {
        var current = document.documentElement.getAttribute('data-theme') || 'light';
        var next = current === 'dark' ? 'light' : 'dark';
        apply(next);
        localStorage.setItem(KEY, next);

        var gridColor = next === 'dark' ? '#334155' : '#f3f4f6';
        var tickColor = next === 'dark' ? '#94a3b8' : '#6b7280';
        if (window.__chartInstances && window.__chartInstances.length) {
            window.__chartInstances.forEach(function (chart) {
                if (!chart || !chart.options) return;
                var scales = chart.options.scales;
                if (scales) {
                    ['x', 'y'].forEach(function (axis) {
                        if (!scales[axis]) return;
                        if (scales[axis].grid) scales[axis].grid.color = gridColor;
                        if (scales[axis].ticks) scales[axis].ticks.color = tickColor;
                    });
                }
                var legend = chart.options.plugins && chart.options.plugins.legend;
                if (legend && legend.labels) legend.labels.color = tickColor;
                chart.update('none');
            });
        }
    };
})();
