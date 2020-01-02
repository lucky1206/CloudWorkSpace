/**
 * Created by winnerlbm on 2018/6/12.
 */
Ext.define('acsweb.util.ChartUtil', {
    //统计图相关参数
    //颜色池
    colors: [
        '#0efffd',
        '#00ff00',
        '#ff0dde',
        '#ffff00',
        '#54ffa5',
        '#ab14ff',
        '#e01468',
        '#e167e6',
        '#f35f87',
        '#b24659',
        '#11aa65',
        '#5880ff',
        '#ffac00',
        '#8faa6d',
        '#dce2ff',
        '#b7ff25',
        '#e3b293',
        '#dd6b66',
        '#759aa0',
        '#e69d87',
        '#8dc1a9',
        '#ea7e53',
        '#eedd78',
        '#73a373',
        '#73b9bc',
        '#7289ab',
        '#91ca8c',
        '#f49f42'
    ],
    //根据配置生成chart
    generateChart: function (id, opts) {
        let chart = echarts.init(Ext.getDom(id), 'light');
        chart.setOption(opts);
        return chart;
    },
    //更新统计图尺寸
    resizeChart: function (chartInstance, width, height) {
        if (chartInstance) {
            chartInstance.resize(width, height);
        }
    }
});
let chartUtil = new acsweb.util.ChartUtil();