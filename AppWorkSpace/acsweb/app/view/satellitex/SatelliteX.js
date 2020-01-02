/**
 * Created by winnerlbm on 2019/7/23.
 */
Ext.define('acsweb.view.satellitex.SatelliteX', {
    extend: 'Ext.Container',

    requires: [
        'Ext.button.Button',
        'Ext.container.Container',
        'Ext.form.field.Checkbox',
        'Ext.form.field.Text',
        'Ext.form.field.TextArea',
        'Ext.layout.container.Box',
        'Ext.layout.container.HBox',
        'Ext.layout.container.VBox',
        'Ext.panel.Panel',
        'Ext.resizer.Splitter',
        'Ext.toolbar.Fill',
        'Ext.tree.Panel',
        'acsweb.view.satellitex.SatelliteXController',
        'acsweb.view.satellitex.SatelliteXModel'
    ],

    /*
    Uncomment to give this component an xtype*/
    xtype: 'satellitex',

    viewModel: {
        type: 'satellitex'
    },

    controller: 'satellitex',

    border: false,

    layout: {
        type: 'hbox',
        pack: 'start',
        align: 'stretch'
    },
    listeners: {
        afterrender: 'sateAfterrender'
    },
    items: [
        /* include child components here */
        {
            xtype: 'panel',
            title: '卫星分组',
            reference: 'satelliteContainerRef',
            ui: 'project-panel-ui',
            iconCls: 'cloud icon-catalog',
            width: 260,
            layout: {
                type: 'vbox',
                pack: 'start',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'treepanel',
                    reference: 'satesGroupTreeRef',
                    flex: 2,
                    border: true,
                    rootVisible: false,
                    header: false,
                    titleCollapse: false,
                    useArrows: false,
                    frame: false,
                    frameHeader: false,
                    bufferedRenderer: false,
                    animate: true,
                    rowLines: false,
                    columnLines: false,
                    singleExpand: false,
                    expanderOnly: true,
                    expanderFirst: false,
                    itemRipple: false,
                    trackMouseOver: true,
                    disableSelection: false,
                    viewConfig: {
                        stripeRows: false,
                        enableTextSelection: false,
                        emptyText: '无卫星分组记录'
                    },
                    bbar: [
                        '->',
                        {
                            xtype: 'button',
                            text: '分组管理',
                            iconCls: 'cloud icon-satellite blueCls',
                            handler: 'showTlesManager'
                        }
                    ],
                    listeners: {
                        itemclick: 'ic4TleGroupHandler'
                    }
                },
                {
                    xtype: 'treepanel',
                    reference: 'satesTreeRef',
                    ui: 'project-panel-ui',
                    iconCls: 'cloud icon-satellite',
                    title: '分组卫星',
                    flex: 3,
                    border: true,
                    rootVisible: false,
                    header: true,
                    titleCollapse: false,
                    useArrows: false,
                    frame: false,
                    frameHeader: false,
                    bufferedRenderer: false,
                    animate: true,
                    rowLines: false,
                    columnLines: false,
                    singleExpand: false,
                    expanderOnly: true,
                    expanderFirst: false,
                    itemRipple: false,
                    trackMouseOver: true,
                    disableSelection: false,
                    scrollable: 'y',
                    viewConfig: {
                        stripeRows: false,
                        enableTextSelection: false,
                        emptyText: '请选择分组项目'
                    },
                    listeners: {
                        itemclick: 'ic4TleHandler'
                    },
                    bbar: [
                        '->',
                        {
                            xtype: 'checkbox',
                            reference: 'orbitDailyRef',
                            boxLabel: '未来24小时轨迹',
                            listeners: {
                                change: 'orbitDaily'
                            }
                        },
                        {
                            xtype: 'checkbox',
                            reference: 'autoTracingRef',
                            boxLabel: '自动追踪',
                            listeners: {
                                change: 'autoTracing'
                            }
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'container',
            id: 'globeParentContainerId',
            flex: 1,
            html: '<div id="globeContainerId" class="globeContainerCls"></div></div>',
            margin: '0 0 0 0',
            listeners: {
                afterrender: 'afterrenderHandler',
                resize: 'resizeHandler'
            }
        }
    ]
});