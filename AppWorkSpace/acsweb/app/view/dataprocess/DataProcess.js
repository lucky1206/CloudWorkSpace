/**数据管理模块
 * Created by winnerlbm on 2019/6/24.
 */
Ext.define('acsweb.view.dataprocess.DataProcess', {
    extend: 'Ext.Container',

    requires: [
        'Ext.button.Button',
        'Ext.container.Container',
        'Ext.form.Panel',
        'Ext.form.field.ComboBox',
        'Ext.grid.Panel',
        'Ext.layout.container.HBox',
        'Ext.layout.container.VBox',
        'Ext.panel.Panel',
        'Ext.resizer.Splitter',
        'Ext.toolbar.Fill',
        'acsweb.util.CustomPageToolBar',
        'acsweb.view.dataprocess.DataProcessController',
        'acsweb.view.dataprocess.DataProcessModel'
    ],

    /*
    Uncomment to give this component an xtype*/
    xtype: 'dataprocess',

    viewModel: {
        type: 'dataprocess'
    },

    controller: 'dataprocess',

    layout: {
        type: 'hbox',
        pack: 'start',
        align: 'stretch'
    },

    items: [
        /* include child components here */
        //数据列表及数据上传、入库
        {
            xtype: 'panel',
            border: true,
            width: 422,
            layout: {
                type: 'vbox',
                pack: 'start',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'form',
                    border: false,
                    layout: {
                        type: 'hbox',
                        pack: 'start',
                        align: 'middle'
                    },
                    ui: 'project-panel-ui',
                    reference: 'dbInfoFormRef',
                    bodyPadding: 10,
                    fieldDefaults: {
                        msgTarget: 'side',
                        labelWidth: 50,
                        labelStyle: 'font-size:14px;font-weight:bold'
                    },
                    items: [
                        {
                            xtype: 'combobox',
                            fieldLabel: '数据库',
                            flex: 1,
                            reference: 'databaseConfigListRef',
                            allowBlank: false,
                            name: 'dbDesc',
                            valueField: 'dbDesc',
                            displayField: 'dbDesc',
                            editable: false,
                            typeAhead: false,
                            queryMode: 'local',
                            emptyText: '请选择数据库, 仅支持MySQL',
                            listeners: {
                                select: 'selectDBciHandler'
                            }
                        },
                        {
                            xtype: 'button',
                            text: '刷新列表',
                            iconCls: 'cloud icon-refresh',
                            margin: '0 0 0 10',
                            handler: 'allDataBaseInfoList'
                        }
                    ]
                },
                {
                    xtype: 'container',
                    flex: 1,
                    html: '<iframe name="uploadiFrame" src="resources/html/upload.html" style="width: 100%; height: 100%;border: none"></iframe>'
                }
            ]
        },
        //上传文件清单
        {
            xtype: 'gridpanel',
            reference: 'fileInfoGridRef',
            title: '已上传文件列表',
            ui: 'project-panel-ui',
            iconCls: 'cloud icon-table',
            margin: '0 0 0 1',
            width: 520,
            hidden: false,
            border: true,
            columnLines: false,
            reserveScrollbar: true,
            multiSelect: false,
            scrollable: 'y',
            store: {
                data: []
            },
            viewConfig: {
                stripeRows: false,
                enableTextSelection: true,
                emptyText: '没有上传文件记录'
            },
            /*selModel: {
                type: 'checkboxmodel',
                checkOnly: true
            },*/
            listeners: {
                rowclick: 'rowclickHandler'
            },
            columns: [
                {
                    text: '名称',
                    width: 200,
                    dataIndex: 'filename',
                    hideable: false,
                    menuDisabled: true,
                    resizable: false,
                    sortable: false,
                    align: 'center'
                }, {
                    text: '大小(MB)',
                    width: 100,
                    dataIndex: 'fsize',
                    hideable: false,
                    menuDisabled: true,
                    resizable: false,
                    sortable: false,
                    align: 'center'
                }, {
                    text: '上传时间',
                    width: 140,
                    dataIndex: 'fdate',
                    hideable: false,
                    menuDisabled: true,
                    resizable: false,
                    sortable: false,
                    align: 'center'
                }, {
                    text: '状态',
                    width: 80,
                    dataIndex: 'fstate',
                    hideable: false,
                    menuDisabled: true,
                    resizable: false,
                    sortable: false,
                    align: 'center'
                }
            ],
            leadingBufferZone: 8,
            trailingBufferZone: 8,
            buttons: [
                '->',
                {
                    text: '全部数据',
                    iconCls: 'cloud icon-refresh',
                    margin: '0 0 0 10',
                    handler: 'allFileInfoList'
                },
                {
                    text: '未入库数据',
                    iconCls: 'cloud icon-refresh',
                    margin: '0 0 0 10',
                    handler: 'unImportFileInfoList'
                },
                {
                    text: '数据入库',
                    iconCls: 'cloud icon-warehousing',
                    margin: '0 10 0 10',
                    handler: 'data2dbHandler'
                }
            ]
        },
        {
            xtype: 'splitter',
            width: 3
        },
        {
            xtype: 'panel',
            ui: 'project-panel-ui',
            iconCls: 'cloud icon-sql',
            border: true,
            title: '关联表创建SQL',
            flex: 1,
            html: '<iframe name="sqliFrame" src="resources/html/content.html" style="width: 100%; height: 100%;border-style: solid; border-width: 0 0 0 0;border-color:#009BFE;"></iframe>'
        }
    ],
    listeners: {
        afterrender: 'afterrenderHandler'
    }
});