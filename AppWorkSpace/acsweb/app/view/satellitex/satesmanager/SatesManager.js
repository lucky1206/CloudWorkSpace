/**
 * Created by winnerlbm on 2019/7/29.
 */
Ext.define('acsweb.view.satellitex.satesmanager.SatesManager', {
    extend: 'Ext.panel.Panel',

    requires: [
        'Ext.button.Button',
        'Ext.container.Container',
        'Ext.form.Panel',
        'Ext.form.field.Text',
        'Ext.form.field.TextArea',
        'Ext.grid.Panel',
        'Ext.grid.column.Action',
        'Ext.layout.container.HBox',
        'Ext.panel.Panel',
        'Ext.toolbar.Fill',
        'Ext.tree.Panel',
        'acsweb.view.satellitex.satesmanager.SatesManagerController',
        'acsweb.view.satellitex.satesmanager.SatesManagerModel'
    ],

    /*
    Uncomment to give this component an xtype*/
    xtype: 'satesmanager',
    viewModel: {
        type: 'satesmanager'
    },

    controller: 'satesmanager',

    layout: {
        type: 'hbox',
        pack: 'start',
        align: 'stretch'
    },
    listeners: {
        afterrender: 'afterrenderHandler'
    },
    buttons: [
        '->',
        {
            text: '删除',
            iconCls: 'cloud icon-app-delete',
            handler: 'onRemoveGroupHandler'
        },
        {
            text: '保存',
            iconCls: 'cloud icon-app-edit',
            handler: 'onEditGroupHandler'
        },
        {
            text: '新建',
            iconCls: 'cloud icon-app-add',
            handler: 'onAddGroupHandler'
        }
    ],
    items: [
        /* include child components here */
        {
            xtype: 'treepanel',
            title: '分组列表',
            ui: 'project-panel-ui',
            iconCls: 'cloud icon-catalog',
            reference: 'satesGroupTreeRef',
            width: 260,
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
            listeners: {
                itemclick: 'sateGroupIcHandler'
            }
        },
        {
            xtype: 'panel',
            border: false,
            margin: '0 0 0 1',
            flex: 1,
            layout: {
                type: 'hbox',
                pack: 'start',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'form',
                    border: true,
                    width: 300,
                    title: '分组信息',
                    ui: 'project-panel-ui',
                    iconCls: 'cloud icon-detail',
                    reference: 'sateGroupInfoFormRef',
                    bodyPadding: 3,
                    defaultType: 'textfield',
                    scrollable: 'y',
                    fieldDefaults: {
                        msgTarget: 'side',
                        labelAlign: 'top',
                        labelWidth: 120,
                        labelStyle: 'font-weight:bold'
                    },
                    defaults: {
                        anchor: '100%'
                    },
                    items: [
                        {
                            allowBlank: false,
                            fieldLabel: '分组名称',
                            name: 'text',
                            emptyText: '分组名称',
                            beforeLabelTextTpl: [
                                '<span style="color:red;font-weight:bold" data-qtip="必填">*</span>'
                            ]
                        },
                        {
                            allowBlank: true,
                            fieldLabel: '用户名',
                            name: 'guser',
                            emptyText: '用户名'
                        },
                        {
                            xtype: 'textarea',
                            allowBlank: true,
                            fieldLabel: '分组描述',
                            name: 'gdesc',
                            emptyText: '分组描述'
                        },
                        {
                            xtype: 'gridpanel',
                            iconCls: 'cloud icon-sate',
                            title: '已选卫星',
                            ui: 'project-panel-ui',
                            reference: 'selectedSateGridRef',
                            margin: '0 0 0 0',
                            border: false,
                            columnLines: true,
                            reserveScrollbar: false,
                            multiSelect: false,
                            scrollable: 'y',
                            viewConfig: {
                                stripeRows: false,
                                enableTextSelection: true,
                                emptyText: '数据查询中...'
                            },
                            columns: [
                                {
                                    text: '卫星名',
                                    flex: 1,
                                    dataIndex: 'text',
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'center'
                                },
                                {
                                    xtype: 'actioncolumn',
                                    width: 40,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'center',
                                    items: [
                                        {
                                            iconCls: 'cloud icon-app-delete redCls',
                                            tooltip: '从当前组移除',
                                            handler: 'onRemoveFromGroup'
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'gridpanel',
                    iconCls: 'cloud icon-sate',
                    title: '可选卫星',
                    ui: 'project-panel-ui',
                    reference: 'allSateGridRef',
                    margin: '0 0 0 1',
                    flex: 1,
                    border: true,
                    columnLines: true,
                    reserveScrollbar: true,
                    multiSelect: false,
                    scrollable: 'y',
                    viewConfig: {
                        stripeRows: false,
                        enableTextSelection: true,
                        emptyText: '无记录'
                    },
                    tbar: [
                        '->',
                        {
                            xtype: 'textfield',
                            reference: 'keywordsRef',
                            emptyText: '请输入关键字'
                        },
                        {
                            xtype: 'button',
                            iconCls: 'cloud icon-search blueCls',
                            text: '查询',
                            handler: 'queryTlesByNameHandler'
                        }
                    ],
                    columns: [
                        {
                            text: '卫星名',
                            width: 200,
                            dataIndex: 'text',
                            hideable: false,
                            menuDisabled: true,
                            resizable: false,
                            sortable: false,
                            align: 'center'
                        },
                        {
                            text: '第一行根数',
                            flex: 1,
                            dataIndex: 'tfr',
                            hideable: false,
                            menuDisabled: true,
                            resizable: false,
                            sortable: false,
                            align: 'center'
                        },
                        {
                            text: '第二行根数',
                            flex: 1,
                            dataIndex: 'tsr',
                            hideable: false,
                            menuDisabled: true,
                            resizable: false,
                            sortable: false,
                            align: 'center'
                        },
                        {
                            xtype: 'actioncolumn',
                            width: 40,
                            hideable: false,
                            menuDisabled: true,
                            resizable: false,
                            sortable: false,
                            align: 'center',
                            items: [
                                {
                                    iconCls: 'cloud icon-app-add blueCls',
                                    tooltip: '添加到当前组',
                                    handler: 'onAddToGroup'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
});