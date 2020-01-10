/**
 * Created by LBM on 2017/12/18.
 */
let dbmc = {
    isMainKey: function (value) {
        return parseInt(value) > 0 ? '是' : '否';
    }
};

Ext.define('acsweb.view.entitymanager.EntityManager', {
    extend: 'Ext.Container',

    requires: [
        'Ext.button.Button',
        'Ext.container.Container',
        'Ext.form.FieldContainer',
        'Ext.form.Label',
        'Ext.form.Panel',
        'Ext.form.field.Checkbox',
        'Ext.form.field.ComboBox',
        'Ext.form.field.TextArea',
        'Ext.grid.Panel',
        'Ext.grid.plugin.RowWidget',
        'Ext.layout.container.HBox',
        'Ext.panel.Panel',
        'Ext.resizer.Splitter',
        'Ext.tab.Panel',
        'Ext.toolbar.Fill',
        'acsweb.view.entitymanager.EntityManagerController',
        'acsweb.view.entitymanager.EntityManagerModel',
        'acsweb.view.entitymanager.customizeentity.CustomizeEntity'
    ],

    /*
    Uncomment to give this component an xtype */
    xtype: 'entitymanager',

    viewModel: {
        type: 'entitymanager'
    },

    controller: 'entitymanager',

    layout: {
        type: 'hbox',
        pack: 'start',
        align: 'stretch'
    },
    scrollable: 'y',
    items: [
        /* include child components here */
        {
            xtype: 'tabpanel',
            border: true,
            flex: 4,
            ui: 'entity-tab-panel-ui',
            defaults: {
                bodyPadding: 0,
                scrollable: true
            },
            items: [
                {
                    title: '1.加载数据库表',
                    xtype: 'gridpanel',
                    ui: 'db-grid-panel-ui',
                    reference: 'dbResultGridRef',
                    margin: '0 0 0 0',
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
                    buttons: [
                        {
                            xtype: 'label',
                            id: 'tableInfoId',
                            style: 'color:red;font-size:14px;font-weight:bold;'
                        },
                        '->',
                        {
                            xtype: 'button',
                            iconCls: 'cloud icon-table',
                            text: '加载库表信息<b style="color: red">（必须★）</b>',
                            handler: 'loadDBTables'
                        },
                        {
                            xtype: 'button',
                            iconCls: 'cloud icon-more',
                            text: '当库表结构更新时刷新<b style="color: red">（执行时间长，请耐心等待☺）</b>',
                            tooltip: '当数据库表结构发生变化请刷新缓存，执行时间长，请耐心等待！吸根烟吧！',
                            handler: 'refreshDBTables'
                        }/*,
                    {xtype: 'button', text: '重置', handler: 'resetDBTables'}*/
                    ],
                    columns: [{
                        text: '模型表名',
                        flex: 1,
                        dataIndex: 'tablename',
                        hideable: false,
                        menuDisabled: true,
                        resizable: false,
                        sortable: false,
                        align: 'center'
                    }, {
                        text: '物理表名',
                        flex: 1,
                        dataIndex: 'dbtablename',
                        hideable: false,
                        menuDisabled: true,
                        resizable: false,
                        sortable: false,
                        align: 'center'
                    }, {
                        text: '模型主键类型',
                        flex: 1,
                        dataIndex: 'objectPkJavaType',
                        hideable: false,
                        menuDisabled: true,
                        resizable: false,
                        sortable: false,
                        align: 'center'
                    }, {
                        text: '模型类名',
                        flex: 1,
                        dataIndex: 'upperCaseName',
                        hideable: false,
                        menuDisabled: true,
                        resizable: false,
                        sortable: false,
                        align: 'center'
                    }, {
                        text: '模型实例名',
                        flex: 1,
                        dataIndex: 'lowerCaseName',
                        hideable: false,
                        menuDisabled: true,
                        resizable: false,
                        sortable: false,
                        align: 'center'
                    }, {
                        text: 'PG模式名',
                        flex: 1,
                        dataIndex: 'schemaname',
                        hideable: false,
                        menuDisabled: true,
                        resizable: false,
                        sortable: false,
                        align: 'center'
                    }
                    ],
                    /*leadingBufferZone: 8,
                    trailingBufferZone: 8,*/
                    plugins: [{
                        ptype: 'rowwidget',
                        widget: {
                            xtype: 'grid',
                            ui: 'sql-panel-ui',
                            border: true,
                            margin: '0 0 0 0',
                            columnLines: true,
                            viewConfig: {
                                stripeRows: false,
                                enableTextSelection: true
                            },
                            bind: {
                                store: '{record.columns}',
                                title: '{record.dbtablename} 字段列表'
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
                                    align: 'center'
                                },
                                {
                                    text: 'DB类型',
                                    dataIndex: 'dbType',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'center'
                                },
                                {
                                    text: 'Java名称',
                                    dataIndex: 'javaname',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'center'
                                },
                                {
                                    text: 'Java类型',
                                    dataIndex: 'javaType',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'center'
                                },
                                {
                                    text: 'Java名称(首字母大写)',
                                    dataIndex: 'upname',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'center'
                                },
                                {
                                    text: 'DB描述',
                                    dataIndex: 'desc',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'center'
                                },
                                {
                                    text: '是否主键',
                                    dataIndex: 'pk',
                                    flex: 1,
                                    hideable: false,
                                    menuDisabled: true,
                                    resizable: false,
                                    sortable: false,
                                    align: 'center',
                                    renderer: function (value) {
                                        return dbmc.isMainKey(value);
                                    }
                                }
                            ]
                        }
                    }]
                },
                {
                    title: '2.单表接口创建',
                    xtype: 'gridpanel',
                    ui: 'db-grid-panel-ui',
                    reference: 'entityGridRef',
                    margin: '0 0 0 0',
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
                    buttons: [
                        {
                            xtype: 'label',
                            id: 'entityMakeInfoId',
                            style: 'color:red;font-size:14px;font-weight:bold;'
                        },
                        '->',
                        {
                            xtype: 'checkboxfield',
                            id: 'isModuleCheckId1',
                            name: 'isModuling',
                            checked: true,
                            boxLabel: '启用JS模块'
                        },
                        {
                            xtype: 'checkboxfield',
                            id: 'isManageCheckId1',
                            name: 'isManaging',
                            margin: '0 0 0 10',
                            boxLabel: '启用记录管理'
                        },
                        {
                            xtype: 'checkboxfield',
                            id: 'isPageCheckId1',
                            name: 'isPaging',
                            margin: '0 0 0 10',
                            boxLabel: '启用查询分页'
                        },
                        {
                            xtype: 'button',
                            iconCls: 'cloud icon-algorithm',
                            margin: '0 10 0 10',
                            text: '一键生成',
                            handler: 'oneKeyCoder'
                        }
                    ],
                    columns: [{
                        text: '接口名称',
                        flex: 1,
                        dataIndex: 'apiName',
                        hideable: false,
                        menuDisabled: true,
                        resizable: false,
                        sortable: false,
                        align: 'center'
                    }, {
                        text: '接口描述',
                        flex: 1,
                        dataIndex: 'apiDesc',
                        hideable: false,
                        menuDisabled: true,
                        resizable: false,
                        sortable: false,
                        align: 'center'
                    }],
                    leadingBufferZone: 8,
                    trailingBufferZone: 8

                },
                {
                    title: '3.业务接口创建',
                    layout: {
                        type: 'hbox',
                        pack: 'start',
                        align: 'stretch'
                    },
                    items: [
                        {
                            xtype: 'form',
                            margin: '0 0 0 0',
                            flex: 2,
                            layout: {
                                type: 'vbox',
                                pack: 'start',
                                align: 'stretch'
                            },
                            title: 'SQL解译',
                            border: true,
                            ui: 'sql-panel-ui',
                            reference: 'sqlFormRef',
                            bodyPadding: 5,
                            defaultButton: 'testSqlRef',
                            defaultType: 'textfield',
                            fieldDefaults: {
                                msgTarget: 'side',
                                labelAlign: 'top',
                                labelWidth: 120,
                                labelStyle: 'font-weight:bold',
                                margin: '0 0 0 0'
                            },
                            scrollable: 'y',
                            listeners: {
                                afterrender: 'afterrender4SqlHandler'
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
                                            name: 'apiGroup',
                                            valueField: 'groupname',
                                            displayField: 'groupname',
                                            editable: true,
                                            typeAhead: false,
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
                                    name: 'sqlType',//作为提交表单各参数字段名
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
                                            name: 'apiName',
                                            flex: 1,
                                            emptyText: '类名(首字母大写驼峰式命名):TestData',
                                            bind: '{apiName}'
                                        },
                                        {
                                            allowBlank: false,
                                            fieldLabel: '接口描述',
                                            name: 'apiDesc',
                                            flex: 1,
                                            margin: '0 0 0 6',
                                            emptyText: '业务用途，使用范围等信息',
                                            bind: '{apiDesc}'
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
                                            name: 'requestName',
                                            flex: 1,
                                            bind: '{apiName}Request',
                                            editable: false,
                                            emptyText: '类名(首字母大写驼峰式命名)'
                                        },
                                        {
                                            allowBlank: false,
                                            fieldLabel: '请求类描述',
                                            name: 'requestDesc',
                                            flex: 1,
                                            margin: '0 0 0 6',
                                            bind: '{apiName}接口请求类',
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
                                            name: 'responseName',
                                            flex: 1,
                                            bind: '{apiName}Response',
                                            editable: false,
                                            emptyText: '类名(首字母大写驼峰式命名)'
                                        },
                                        {
                                            allowBlank: false,
                                            fieldLabel: '响应类描述',
                                            name: 'responseDesc',
                                            flex: 1,
                                            margin: '0 0 0 6',
                                            bind: '{apiName}接口响应类',
                                            editable: false,
                                            emptyText: '业务用途，使用范围等信息'
                                        }]
                                },
                                {
                                    xtype: 'textarea',
                                    // height: 200,
                                    flex: 1,
                                    allowBlank: false,
                                    fieldLabel: 'SQL语句',
                                    name: 'sqlText',
                                    emptyText: '请输入SQL语句，目前对select语句支持较好。'
                                },
                                {
                                    xtype: 'label',
                                    id: 'sqlInfoId',
                                    margin: '5 0 0 0',
                                    height: 20,
                                    style: 'color:red;font-size:14px;font-weight:bold;'
                                }
                            ],

                            buttons: [
                                '->',
                                {
                                    xtype: 'checkboxfield',
                                    id: 'isModuleCheckId',
                                    name: 'isModuling',
                                    checked: true,
                                    margin: '0 0 0 10',
                                    boxLabel: '启用JS模块'
                                },
                                {
                                    xtype: 'checkboxfield',
                                    id: 'isManageCheckId',
                                    name: 'isManaging',
                                    margin: '0 0 0 10',
                                    boxLabel: '启用记录管理'
                                },
                                {
                                    xtype: 'checkboxfield',
                                    id: 'isPageCheckId',
                                    name: 'isPaging',
                                    margin: '0 10 0 10',
                                    boxLabel: '启用查询分页'
                                },
                                {
                                    text: '重置',
                                    iconCls: 'cloud icon-reset',
                                    margin: '0 0 0 10',
                                    handler: 'sqlParserReset'
                                },
                                {
                                    text: 'SQL解译',
                                    iconCls: 'cloud icon-sqlparse',
                                    disabled: true,
                                    formBind: true,
                                    margin: '0 10 0 10',
                                    reference: 'testSqlRef',
                                    handler: 'sqlParser'
                                }
                            ],

                            defaults: {
                                anchor: '100%'
                            }
                        },
                        {
                            xtype: 'splitter',
                            width: 3
                        },
                        {
                            xtype: 'panel',
                            border: true,
                            margin: '0 0 0 0',
                            flex: 2,
                            html: '<iframe name="showCodeiFrame" src="resources/html/code.html" style="width: 100%; height: 100%;border: none"></iframe>'
                        },
                        {
                            xtype: 'splitter',
                            width: 3
                        },
                        {
                            xtype: 'panel',
                            border: true,
                            margin: '0 0 0 0',
                            bodyPadding: 10,
                            flex: 1,
                            scrollable: 'y',
                            html: '<h3><i class="fa fa-exclamation-circle"></i> 使用说明</h3><br/><span style="font-size: 13px; color: #212121; line-height: 150%;"><b>1</b>、本模块的所有操作需要完成前置模块的所有操作才能执行，（例外：本模块第3步操作可以跳过第2步直接执行）;<br/><br/><b>2</b>、本模块内容需要按照标题序号依次执行操作;<br/><br/><b>3</b>、关系管理中SQL语句限制条件说明：<br/><b style="color: #ff0000">a</b>、前提在编写sql语句时，所有表名均需要设置别名，标准格式如：select t.* from test t where t.test_id > 2,查询体 * 号之前的别名不能省略★；<br/><b style="color: #ff0000">b</b>、case子句保留原样，不进行参数化,并且case子句需要满足格式要求end之后有必须关键字as；<br/><b style="color: #ff0000">c</b>、like子句合并通配符并参数化，测试时需要将通配符与查询字符同时传入，如%减灾%；<br/><b style="color: #ff0000">d</b>、in子句保留原样，不进行参数化,注意这里的in子句必须满足格式，如in(10,20)；<br/><b style="color: #ff0000">e</b>、目前对大部分sql语句支持较为完善，update、delete、insert语句也已经支持，除Insert,Delete语句外，其他语句均需要采用表别名的方式编写；<br/><b style="color: #ff0000">f</b>、需要注意是否分页选项只针对select语句有效，请根据实际情况确定是否需要查询分页。</span>'
                        }
                    ]
                },
                {
                    title: '4.自定义接口创建',
                    xtype: 'customizeentity'
                },
                {
                    title: '5.多数据库配置模板',
                    xtype: 'panel',
                    scrollable: false,
                    html: '<iframe name="showXmliFrame" src="resources/html/xml.html" style="width: 100%; height: 100%;border: none;overflow: hidden"></iframe>'
                }
            ]
        }
    ],
    listeners: {
        afterrender: 'afterrenderHandler'
    }
});