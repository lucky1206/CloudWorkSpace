/**
 * Created by winnerlbm on 2018/9/4.
 */
Ext.define('acsweb.view.entitymanager.customizeentity.CustomizeEntity', {
    extend: 'Ext.Container',

    requires: [
        'Ext.button.Button',
        'Ext.container.Container',
        'Ext.form.CheckboxGroup',
        'Ext.form.FieldContainer',
        'Ext.form.Label',
        'Ext.form.Panel',
        'Ext.form.field.Checkbox',
        'Ext.form.field.ComboBox',
        'Ext.grid.Panel',
        'Ext.grid.column.Widget',
        'Ext.layout.container.HBox',
        'Ext.layout.container.VBox',
        'Ext.selection.CheckboxModel',
        'Ext.toolbar.Fill',
        'acsweb.view.entitymanager.customizeentity.CustomizeEntityController',
        'acsweb.view.entitymanager.customizeentity.CustomizeEntityModel'
    ],

    /*
    Uncomment to give this component an xtype*/
    xtype: 'customizeentity',

    viewModel: {
        type: 'customizeentity'
    },

    controller: 'customizeentity',

    layout: {
        type: 'hbox'
    },
    scrollable: 'y',
    items: [
        {
            xtype: 'grid',
            ui: 'sql-panel-ui',
            title: '数据库表',
            reference: 'dbTableGridRef',
            height: 800,
            width: 300,
            margin: '0 0 0 0',
            border: true,
            columnLines: true,
            reserveScrollbar: true,
            multiSelect: true,
            scrollable: 'y',
            viewConfig: {
                stripeRows: false
            },
            listeners: {
                rowclick: 'dbTableRowClick'
            },
            columns: [
                {
                    text: '表名',
                    dataIndex: 'dbtablename',
                    flex: 1,
                    hideable: false,
                    menuDisabled: true,
                    resizable: false,
                    sortable: false,
                    align: 'left'
                }
            ]
        },
        {
            xtype: 'container',
            margin: '0 0 0 1',
            flex: 1,
            height: 800,
            layout: {
                type: 'vbox',
                pack: 'start',
                align: 'stretch'
            },
            items: [
                {
                    layout: {
                        type: 'hbox',
                        pack: 'start',
                        align: 'stretch'
                    },
                    flex: 1,
                    items: [
                        {
                            xtype: 'grid',
                            title: '查询字段列表',
                            ui: 'sql-panel-ui',
                            reference: 'dbQueryFieldGridRef',
                            flex: 1,
                            border: true,
                            columnLines: true,
                            reserveScrollbar: true,
                            multiSelect: false,
                            scrollable: 'y',
                            viewConfig: {
                                stripeRows: false,
                                enableTextSelection: true
                            },
                            selModel: {
                                type: 'checkboxmodel',
                                checkOnly: true
                            },
                            columns: [
                                {
                                    text: 'DB名称',
                                    dataIndex: 'dbname',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'left'
                                },
                                {
                                    text: 'DB类型',
                                    dataIndex: 'dbType',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'left'
                                },
                                {
                                    text: 'Java名称',
                                    dataIndex: 'javaname',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'left'
                                },
                                {
                                    text: 'Java类型',
                                    dataIndex: 'javaType',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'left'
                                },
                                {
                                    text: 'Java名称(首字母大写)',
                                    dataIndex: 'upname',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'left'
                                },
                                {
                                    text: 'DB描述',
                                    dataIndex: 'desc',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'left'
                                },
                                {
                                    text: '是否主键',
                                    dataIndex: 'pk',
                                    flex: 0.5,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'left',
                                    renderer: function (value) {
                                        return dbmc.isMainKey(value);
                                    }
                                }
                            ]
                        },
                        {
                            xtype: 'gridpanel',
                            title: '条件字段列表',
                            ui: 'sql-panel-ui',
                            reference: 'dbConditionFieldGridRef',
                            width: 300,
                            margin: '0 0 0 1',
                            border: true,
                            columnLines: true,
                            reserveScrollbar: false,
                            multiSelect: false,
                            scrollable: 'y',
                            viewConfig: {
                                stripeRows: false,
                                enableTextSelection: true
                            },
                            selModel: {
                                type: 'checkboxmodel',
                                checkOnly: true
                            },
                            columns: [
                                {
                                    text: 'DB名称',
                                    dataIndex: 'dbname',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'left'
                                },
                                {
                                    text: '操作符',
                                    dataIndex: 'operator',
                                    width: 100,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'center',
                                    padding: '0 0 0 0',
                                    xtype: 'widgetcolumn',
                                    widget: {
                                        xtype: 'combo',
                                        margin: '0 0 0 0',
                                        bind: {
                                            store: '{record.operators}'
                                        },
                                        multiSelect: false,
                                        editable: false,
                                        valueField: "value",
                                        displayField: "name",
                                        queryMode: 'local',
                                        listeners: {
                                            select: 'operatorSelectHandler'
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'form',
                    margin: '1 0 0 0',
                    title: '接口配置',
                    reference: 'cApiFormRef',
                    border: true,
                    ui: 'sql-panel-ui',
                    defaultButton: 'customizeApiButtonRef',
                    bodyPadding: 10,
                    defaultType: 'textfield',
                    fieldDefaults: {
                        msgTarget: 'side',
                        labelAlign: 'top',
                        labelWidth: 120,
                        labelStyle: 'font-weight:bold',
                        margin: '0 0 0 0'
                    },
                    items: [
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: '接口分组（按功能、业务等）',
                            layout: 'hbox',
                            height: 65,
                            combineErrors: false,
                            defaultType: 'textfield',
                            defaults: {
                                hideLabel: 'true'
                            },

                            items: [
                                {
                                    xtype: 'combobox',
                                    flex: 1,
                                    reference: 'apiGroupListRef',
                                    allowBlank: false,
                                    fieldLabel: '接口分组（按功能、业务等）',
                                    name: 'apiGroup4C',
                                    valueField: 'groupname',
                                    displayField: 'groupname',
                                    editable: true,
                                    typeAhead: true,
                                    queryMode: 'local',
                                    emptyText: '如：用户管理、系统管理等'
                                },
                                {
                                    xtype: 'button',
                                    iconCls: 'cloud icon-delete',
                                    text: '删除组',
                                    margin: '0 0 0 6',
                                    handler: 'onDeleteApiGroupList'
                                },
                                {
                                    xtype: 'button',
                                    iconCls: 'cloud icon-refresh',
                                    text: '刷新组',
                                    margin: '0 0 0 6',
                                    handler: 'onLoadApiGroupList'
                                }
                            ]
                        },
                        {
                            xtype: 'combobox',
                            allowBlank: false,
                            fieldLabel: 'SQL类型',
                            name: 'sqlType4C',//作为提交表单各参数字段名
                            store: {
                                data: conf.dbTypeList
                            },
                            valueField: 'type',
                            displayField: 'name',
                            value: 'Oracle',
                            editable: false,
                            typeAhead: false,
                            queryMode: 'local',
                            emptyText: '请选择与工程管理模块中同类型数据库！'
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: '接口类(类名勿重，尽量避免包含Query,Select,List等词语)',
                            layout: 'hbox',
                            height: 65,
                            combineErrors: false,
                            defaultType: 'textfield',
                            defaults: {
                                hideLabel: 'true'
                            },

                            items: [
                                {
                                    allowBlank: false,
                                    fieldLabel: '接口名',
                                    name: 'apiName4C',
                                    flex: 1,
                                    emptyText: '类名(首字母大写驼峰式命名):TestData',
                                    bind: '{apiName4C}'
                                },
                                {
                                    allowBlank: false,
                                    fieldLabel: '接口描述',
                                    name: 'apiDesc4C',
                                    flex: 1,
                                    margin: '0 0 0 6',
                                    emptyText: '业务用途，使用范围等信息',
                                    bind: '{apiDesc4C}'
                                }]
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: '请求类(类名勿重)',
                            layout: 'hbox',
                            height: 65,
                            combineErrors: false,
                            defaultType: 'textfield',
                            defaults: {
                                hideLabel: 'true'
                            },

                            items: [
                                {
                                    allowBlank: false,
                                    fieldLabel: '请求类名',
                                    name: 'requestName4C',
                                    flex: 1,
                                    bind: '{apiName4C}Request',
                                    editable: false,
                                    emptyText: '类名(首字母大写驼峰式命名)'
                                },
                                {
                                    allowBlank: false,
                                    fieldLabel: '请求类描述',
                                    name: 'requestDesc4C',
                                    flex: 1,
                                    margin: '0 0 0 6',
                                    bind: '{apiName4C}接口请求类',
                                    editable: false,
                                    emptyText: '业务用途，使用范围等信息'
                                }]
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: '响应类(类名勿重)',
                            layout: 'hbox',
                            height: 65,
                            combineErrors: false,
                            defaultType: 'textfield',
                            defaults: {
                                hideLabel: 'true'
                            },

                            items: [
                                {
                                    allowBlank: false,
                                    fieldLabel: '响应类名',
                                    name: 'responseName4C',
                                    flex: 1,
                                    bind: '{apiName4C}Response',
                                    editable: false,
                                    emptyText: '类名(首字母大写驼峰式命名)'
                                },
                                {
                                    allowBlank: false,
                                    fieldLabel: '响应类描述',
                                    name: 'responseDesc4C',
                                    flex: 1,
                                    margin: '0 0 0 6',
                                    bind: '{apiName4C}接口响应类',
                                    editable: false,
                                    emptyText: '业务用途，使用范围等信息'
                                }]
                        },
                        {
                            xtype: 'checkboxgroup',
                            fieldLabel: '接口动作',
                            cls: 'x-check-group-alt',
                            items: [
                                {boxLabel: 'SELECT', name: 'select4C', checked: true},
                                {boxLabel: 'UPDATE', name: 'update4C'},
                                {boxLabel: 'INSERT', name: 'insert4C'},
                                {boxLabel: 'DELETE', name: 'delete4C'}
                            ]
                        }
                    ],
                    buttons: [
                        {
                            xtype: 'label',
                            id: 'customizeInfoId',
                            style: 'color:red;font-size:14px;font-weight:bold;'
                        },
                        '->',
                        {
                            xtype: 'checkboxfield',
                            id: 'isModuleCheckId4C',
                            name: 'isModuling',
                            checked: true,
                            boxLabel: '启用JS模块'
                        },
                        {
                            xtype: 'checkboxfield',
                            id: 'isManageCheckId4C',
                            name: 'isManaging',
                            margin: '0 10 0 10',
                            boxLabel: '启用记录管理'
                        },
                        {
                            xtype: 'checkboxfield',
                            id: 'isPageCheckId4C',
                            name: 'isPaging',
                            margin: '0 10 0 10',
                            boxLabel: '启用查询分页'
                        },
                        {
                            text: '重载表',
                            iconCls: 'cloud icon-table',
                            margin: '0 10 0 10',
                            handler: 'reloadCustomizeData'
                        },
                        {
                            text: '创建接口',
                            iconCls: 'cloud icon-cubedata',
                            disabled: true,
                            formBind: true,
                            margin: '0 10 0 10',
                            reference: 'customizeApiButtonRef',
                            handler: 'createApi4Customize'
                        }
                    ],

                    defaults: {
                        anchor: '100%'
                    }
                }
            ]
        }
    ],
    listeners: {
        afterrender: 'afterrenderHandler'
    }
});
