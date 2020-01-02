/**
 * Created by winnerlbm on 2019/7/3.
 */
Ext.define('acsweb.view.mapregedit.MapRegedit', {
    extend: 'Ext.Container',

    requires: [
        'Ext.button.Button',
        'Ext.container.Container',
        'Ext.form.FieldContainer',
        'Ext.form.FieldSet',
        'Ext.form.Label',
        'Ext.form.Panel',
        'Ext.form.field.ComboBox',
        'Ext.form.field.TextArea',
        'Ext.layout.container.HBox',
        'Ext.layout.container.Table',
        'Ext.layout.container.VBox',
        'Ext.panel.Panel',
        'Ext.resizer.Splitter',
        'Ext.toolbar.Fill',
        'Ext.tree.Panel',
        'acsweb.view.mapregedit.MapRegeditController',
        'acsweb.view.mapregedit.MapRegeditModel',
        'acsweb.view.preservice.PreService'
    ],

    /*
    Uncomment to give this component an xtype*/
    xtype: 'mapregedit',

    viewModel: {
        type: 'mapregedit'
    },

    controller: 'mapregedit',

    layout: {
        type: 'hbox',
        pack: 'start',
        align: 'stretch'
    },
    listeners: {
        afterrender: 'afterrenderHandler'
    },
    items: [
        /* include child components here */
        {
            xtype: 'container',
            width: 400,
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
                    bodyPadding: 5,
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
                            margin: '0 0 0 5',
                            handler: 'allDataBaseInfoList'
                        }
                    ]
                },
                {
                    xtype: 'treepanel',
                    ui: 'project-panel-ui',
                    iconCls: 'cloud icon-catalog',
                    reference: 'svrCatalogTreeRef',
                    flex: 1,
                    title: '服务目录',
                    rootVisible: false,
                    rowLines: true,
                    animate: true,
                    frame: false,
                    reserveScrollbar: false,
                    multiSelect: false,
                    border: true,
                    viewConfig: {
                        stripeRows: false,
                        enableTextSelection: true,
                        emptyText: '没有已注册服务信息'
                    },
                    /* listeners: {
                         itemclick: 'itemclickHandler'
                     },*/
                    buttons: [
                        '->',
                        {
                            text: '删除组',
                            reference: 'deleteGroupRef',
                            disabled: true,
                            iconCls: 'cloud icon-app-delete',
                            action: 'remove',
                            handler: 'onRemoveClick'
                        },
                        {
                            text: '编辑组',
                            reference: 'editGroupRef',
                            disabled: true,
                            iconCls: 'cloud icon-app-edit',
                            action: 'edit',
                            handler: 'onEditClick'
                        },
                        {
                            text: '新增组',
                            reference: 'addGroupRef',
                            iconCls: 'cloud icon-app-add',
                            action: 'add',
                            handler: 'onAddClick'
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'splitter',
            width: 3
        },
        {
            xtype: 'form',
            closable: true,
            closeAction: 'hide',
            closeToolText: '关闭面板',
            border: true,
            width: 440,
            title: '服务信息',
            ui: 'project-panel-ui',
            iconCls: 'cloud icon-detail',
            reference: 'svrInfoFormRef',
            bodyPadding: 10,
            defaultType: 'textfield',
            scrollable: 'y',
            fieldDefaults: {
                msgTarget: 'side',
                labelAlign: 'top',
                labelWidth: 120,
                labelStyle: 'font-weight:bold'
            },
            items: [
                {
                    allowBlank: false,
                    fieldLabel: '服务名称',
                    name: 'svrName',
                    emptyText: '服务名称',
                    beforeLabelTextTpl: [
                        '<span style="color:red;font-weight:bold" data-qtip="必填">*</span>'
                    ]
                },
                {
                    allowBlank: false,
                    fieldLabel: '图层名称',
                    name: 'svrLayerName',
                    emptyText: '图层名称',
                    beforeLabelTextTpl: [
                        '<span style="color:red;font-weight:bold" data-qtip="必填">*</span>'
                    ]
                },
                {
                    xtype: 'textarea',
                    allowBlank: false,
                    fieldLabel: '服务地址',
                    name: 'svrAddress',
                    emptyText: '服务地址',
                    beforeLabelTextTpl: [
                        '<span style="color:red;font-weight:bold" data-qtip="必填">*</span>'
                    ]
                },
                {
                    xtype: 'combobox',
                    reference: 'svrTypeListRef',
                    fieldLabel: '服务类型',
                    flex: 1,
                    allowBlank: false,
                    name: 'svrTypeName',
                    valueField: 'svrTypeValue',
                    displayField: 'svrTypeValue',
                    editable: false,
                    typeAhead: false,
                    queryMode: 'local',
                    emptyText: '服务类型',
                    beforeLabelTextTpl: [
                        '<span style="color:red;font-weight:bold" data-qtip="必填">*</span>'
                    ]
                },
                {
                    xtype: 'combobox',
                    reference: 'svrCrsListRef',
                    fieldLabel: '服务坐标系',
                    flex: 1,
                    allowBlank: false,
                    name: 'svrCRSName',
                    valueField: 'svrCRSValue',
                    displayField: 'svrCRSValue',
                    editable: false,
                    typeAhead: false,
                    queryMode: 'local',
                    emptyText: '服务坐标系',
                    beforeLabelTextTpl: [
                        '<span style="color:red;font-weight:bold" data-qtip="必填">*</span>'
                    ]
                },
                {
                    xtype: 'fieldset',
                    title: '地图服务范围(可选)',
                    collapsible: true,
                    collapsed: true,
                    padding: '3 3 3 3',
                    defaults: {
                        labelWidth: 120,
                        anchor: '100%',
                        layout: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: '边界（°）',
                            layout: {
                                type: 'table',
                                columns: 3,
                                tableAttrs: {
                                    style: {
                                        width: '100%'
                                    }
                                }
                            },
                            combineErrors: false,
                            defaultType: 'textfield',
                            defaults: {
                                hideLabel: true,
                                width: 130
                            },

                            items: [
                                {
                                    fieldLabel: '最小经度',
                                    rowspan: 2,
                                    value: -180,
                                    name: 'west',
                                    editable: true,
                                    emptyText: '最小经度，默认值0',
                                    allowBlank: true
                                },
                                {
                                    fieldLabel: '最大纬度',
                                    value: 90,
                                    name: 'north',
                                    margin: '0 0 0 4',
                                    editable: true,
                                    emptyText: '最大纬度，默认值0',
                                    allowBlank: true
                                },
                                {
                                    fieldLabel: '最大经度',
                                    rowspan: 2,
                                    value: 180,
                                    name: 'east',
                                    margin: '0 0 0 4',
                                    editable: true,
                                    emptyText: '最大经度，默认值0',
                                    allowBlank: true
                                },
                                {
                                    fieldLabel: '最小纬度',
                                    value: -90,
                                    name: 'south',
                                    margin: '4 0 0 4',
                                    editable: true,
                                    emptyText: '最小纬度，默认值0',
                                    allowBlank: true
                                }
                            ]
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: '中心点（°）',

                            layout: 'hbox',
                            combineErrors: false,
                            defaultType: 'textfield',
                            defaults: {
                                hideLabel: true
                            },

                            items: [{
                                fieldLabel: '中心经度',
                                name: 'cx',
                                flex: 1,
                                value: 0,
                                editable: true,
                                emptyText: '经度，默认值0',
                                allowBlank: true
                            }, {
                                fieldLabel: '中心纬度',
                                name: 'cy',
                                flex: 1,
                                value: 0,
                                margin: '0 0 0 4',
                                editable: true,
                                emptyText: '纬度，默认值0',
                                allowBlank: true
                            }]
                        }]
                },
                {
                    allowBlank: true,
                    fieldLabel: '服务注册者',
                    name: 'svrProvider',
                    emptyText: '服务注册者'
                },
                {
                    xtype: 'textarea',
                    allowBlank: true,
                    fieldLabel: '服务描述',
                    name: 'svrDescription',
                    emptyText: '如：服务样式，格式如下：\n' +
                        '{\n' +
                        '     color: \'#ff00ff\',\n' +
                        '     fillOpacity: 0.5,\n' +
                        '     stroke: true,\n' +
                        '     opacity: 0.5,\n' +
                        '     radius: 4,\n' +
                        '     weight: 1,\n' +
                        '     fillColor: \'#0000ff\',\n' +
                        '     fill: true\n' +
                        ' }'
                }
            ],
            defaults: {
                anchor: '100%'
            },
            buttons: [
                {
                    text: '重置',
                    reference: 'resetSvrRef',
                    formBind: true,
                    iconCls: 'cloud icon-reset',
                    handler: 'resetService'
                },
                {
                    text: '删除服务',
                    reference: 'deleteSvrRef',
                    formBind: true,
                    iconCls: 'cloud icon-app-delete',
                    handler: 'deleteService'
                },
                {
                    text: '保存变更',
                    reference: 'saveSvrRef',
                    formBind: true,
                    iconCls: 'cloud icon-edit',
                    handler: 'saveService'
                },
                {
                    text: '新建服务',
                    reference: 'addSvrRef',
                    formBind: true,
                    iconCls: 'cloud icon-save',
                    handler: 'addService'
                }
            ]
        },
        {
            xtype: 'splitter',
            width: 3
        },
        {
            xtype: 'preservice',
            flex: 1
        }
    ]
});