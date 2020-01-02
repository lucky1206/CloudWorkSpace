/**
 * Created by winnerlbm on 2019/7/15.
 */
Ext.define('acsweb.view.preservice.PreService', {
    extend: 'Ext.panel.Panel',

    requires: [
        'Ext.container.Container',
        'Ext.layout.container.HBox',
        'acsweb.view.preservice.PreServiceController',
        'acsweb.view.preservice.PreServiceModel'
    ],

    /*
    Uncomment to give this component an xtype*/
    xtype: 'preservice',

    viewModel: {
        type: 'preservice'
    },

    controller: 'preservice',

    border: true,
    title: '服务预览（EPSG: 4490 暂时不支持，后续考虑地图引擎OpenLayers）',
    ui: 'project-panel-ui',
    iconCls: 'cloud icon-image',

    layout: {
        type: 'hbox',
        pack: 'start',
        align: 'stretch'
    },

    items: [
        /* include child components here */
        {
            xtype: 'container',
            flex: 1,
            html: '<div id="svrContainerId" class="svrContainerCls"></div></div>',
            margin: '0 0 0 0'
        }
    ],
    listeners: {
        afterrender: 'afterrenderHandler',
        resize: 'resizeHandler'
    }
});