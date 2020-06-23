Ext.define('acsweb.view.projectmanager.ProjectManager', {
    extend: 'Ext.Container',

    requires: [
        'Ext.button.Button',
        'Ext.form.FieldContainer',
        'Ext.form.FieldSet',
        'Ext.form.Label',
        'Ext.form.Panel',
        'Ext.form.field.ComboBox',
        'Ext.form.field.Text',
        'Ext.layout.container.Anchor',
        'Ext.layout.container.HBox',
        'Ext.panel.Panel',
        'Ext.toolbar.Fill',
        'acsweb.view.projectmanager.ProjectManagerController',
        'acsweb.view.projectmanager.ProjectManagerModel'
    ],

    /*
    Uncomment to give this component an xtype*/
    xtype: 'projectmanager',

    viewModel: {
        type: 'projectmanager'
    },

    controller: 'projectmanager',

    layout: {
        type: 'hbox',
        pack: 'start',
        align: 'start'
    },
    scrollable: 'y',
    items: [
        {
            xtype: 'form',
            border: true,
            flex: 1.5,
            height: 610,
            title: '1.数据库配置',
            ui: 'project-panel-ui',
            reference: 'dbInfoFormRef',
            bodyPadding: 10,
            defaultButton: 'testDBRef',
            defaultType: 'textfield',
            fieldDefaults: {
                msgTarget: 'side',
                labelWidth: 120,
                labelStyle: 'font-weight:bold'
            },

            items: [
                {
                    xtype: 'combobox',
                    reference: 'databaseConfigListRef',
                    allowBlank: false,
                    fieldLabel: '数据库列表',
                    name: 'dbDesc',
                    valueField: 'dbDesc',
                    displayField: 'dbDesc',
                    editable: true,
                    typeAhead: false,
                    queryMode: 'local',
                    emptyText: '请输入或选择数据库，如JZ数据库（不能重复）',
                    listeners: {
                        select: 'selectDBciHandler'
                    }
                },
                {
                    xtype: 'combobox',
                    allowBlank: false,
                    fieldLabel: '连接类型',
                    name: 'dbType',//作为提交表单各参数字段名
                    store: {
                        data: conf.dbTypeList
                    },
                    valueField: 'value',
                    displayField: 'name',
                    value: 'oracle.jdbc.driver.OracleDriver',
                    editable: false,
                    typeAhead: false,
                    queryMode: 'local',
                    emptyText: '请选择数据库类型...'
                },
                {
                    allowBlank: false,
                    fieldLabel: '地址',
                    name: 'dbAddress',
                    emptyText: '如：localhost,127.0.0.1'
                },
                {
                    allowBlank: false,
                    fieldLabel: '端口',
                    name: 'dbPort',
                    emptyText: '请输入端口...'
                },
                {
                    allowBlank: false,
                    fieldLabel: '用户名',
                    name: 'dbUser',
                    emptyText: '请输入用户名...'
                },
                {
                    allowBlank: false,
                    fieldLabel: '密码',
                    name: 'dbPassword',
                    emptyText: '请输入密码...',
                    inputType: 'password'
                },
                {
                    allowBlank: false,
                    fieldLabel: '数据库名',
                    name: 'dbName',
                    emptyText: '请输入数据库名...'
                },
                {
                    xtype: 'fieldset',
                    title: 'Redis配置',
                    layout: 'anchor',

                    defaults: {
                        anchor: '100%',
                        xtype: 'textfield'
                    },

                    items: [
                        {
                            allowBlank: true,
                            name: 'isNeedRedis',
                            hidden: true,
                            value: 'on'
                        },
                        {
                            allowBlank: true,
                            fieldLabel: '地址',
                            name: 'redisAddress',
                            emptyText: ''
                        },
                        {
                            allowBlank: true,
                            fieldLabel: '端口',
                            name: 'redisPort',
                            emptyText: ''
                        },
                        {
                            allowBlank: true,
                            fieldLabel: '密码',
                            name: 'redisPassword',
                            emptyText: '',
                            inputType: 'password'
                        }
                    ]
                }
            ],

            buttons: [
                {
                    xtype: 'label',
                    id: 'connectInfoId',
                    style: 'color:red;font-size:14px;font-weight:bold;'
                },
                '->',
                {
                    text: '删除',
                    iconCls: 'cloud icon-delete',
                    handler: 'onDBCDelete'
                },
                {
                    text: '刷新',
                    iconCls: 'cloud icon-refresh',
                    handler: 'onDBCReset'
                },
                {
                    text: '重置',
                    iconCls: 'cloud icon-reset',
                    handler: 'onDBCReset'
                },
                {
                    text: '<b style="color: red">连接</b>',
                    iconCls: 'cloud icon-connect redCls',
                    disabled: true,
                    formBind: true,
                    tooltip: '<b style="color: red">仅对数据库进行测试并连接，请自行确认Redis可用。</b>',
                    reference: 'testDBRef',
                    handler: 'onDBTest'
                }
            ],

            defaults: {
                anchor: '100%'
            }
        },
        {
            xtype: 'form',
            flex: 1.5,
            height: 610,
            border: true,
            margin: '0 0 0 1',
            title: '2.工程配置',
            ui: 'project-panel-ui',
            reference: 'projectConfigRef',
            defaultButton: 'projectMakeRef',
            bodyPadding: 10,
            defaultType: 'textfield',
            fieldDefaults: {
                msgTarget: 'side',
                labelWidth: 200,
                labelStyle: 'font-weight:bold'
            },
            listeners: {
                afterrender: 'afterrenderHandler'
            },
            items: [
                {
                    xtype: 'combobox',
                    reference: 'projectConfigListRef',
                    allowBlank: false,
                    fieldLabel: '工程名称',
                    name: 'projectName',
                    valueField: 'projectname',
                    displayField: 'projectname',
                    editable: true,
                    typeAhead: false,
                    queryMode: 'local',
                    emptyText: '请输入或选择工程名称，如：测试工程',
                    listeners: {
                        select: 'selectPciHandler'
                    }
                },
                {
                    allowBlank: false,
                    fieldLabel: '工程描述',
                    name: 'projectDesc',
                    emptyText: '如：应急管理、金民工程等'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '父包结构',

                    layout: 'hbox',
                    combineErrors: false,
                    defaultType: 'textfield',
                    defaults: {
                        hideLabel: 'true'
                    },

                    items: [{
                        fieldLabel: '顶级包名',
                        name: 'fpName',
                        flex: 2,
                        value: 'com',
                        editable: true,
                        emptyText: '顶级包名,如：com',
                        allowBlank: false
                    }, {
                        fieldLabel: '次级包名',
                        name: 'spName',
                        flex: 3,
                        margin: '0 0 0 6',
                        emptyText: '次级包名，如：工程名称',
                        allowBlank: false
                    }]
                },
                {
                    allowBlank: false,
                    fieldLabel: '拦截器包名',
                    name: 'interceptorName',
                    value: 'interceptor',
                    editable: true,
                    emptyText: '如：interceptor'
                },
                {
                    allowBlank: false,
                    fieldLabel: '控制器包名',
                    name: 'cpName',
                    value: 'controller',
                    editable: true,
                    emptyText: '如：controller'
                },
                {
                    allowBlank: false,
                    fieldLabel: '业务层包名',
                    name: 'servName',
                    value: 'service',
                    editable: true,
                    emptyText: '如：service'
                },
                {
                    allowBlank: false,
                    fieldLabel: 'Dao接口包名',
                    name: 'dpName',
                    value: 'dao',
                    editable: true,
                    emptyText: '如：idao、dao等'
                },
                {
                    allowBlank: false,
                    fieldLabel: 'Mapper文件目录名',
                    name: 'mfdName',
                    value: 'mapper',
                    editable: true,
                    emptyText: '如：mapping、mapper等'
                },
                {
                    allowBlank: false,
                    fieldLabel: 'Java实体模型包名',
                    name: 'epName',
                    value: 'entity',
                    editable: true,
                    emptyText: '如：entity、models等'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: 'Java实体合并打包为JS对象文件',
                    layout: 'hbox',
                    combineErrors: false,
                    defaultType: 'textfield',
                    defaults: {
                        hideLabel: 'true'
                    },

                    items: [{
                        fieldLabel: 'JS文件目录名',
                        name: 'jsfdName',
                        value: 'jsentity',
                        editable: true,
                        flex: 2,
                        emptyText: 'JS文件目录名，如：jsentity、jsmodel',
                        allowBlank: false
                    }, {
                        fieldLabel: 'JS文件名',
                        name: 'jsfName',
                        value: 'JsObject',
                        editable: true,
                        flex: 3,
                        margin: '0 0 0 6',
                        emptyText: 'JS文件名，如：工程名称',
                        allowBlank: false
                    }]
                },
                {
                    xtype: 'fieldset',
                    title: 'SwaggerAPI服务地址',
                    defaultType: 'textfield',
                    defaults: {
                        anchor: '100%',
                        labelWidth: 50
                    },

                    items: [
                        {
                            allowBlank: false,
                            fieldLabel: '地址',
                            name: 'swaggerServer',
                            emptyText: '后台服务发布网站或IP地址及端口号，如：localhost:8080'
                        }
                    ]
                }
            ],
            defaults: {
                anchor: '100%'
            },
            buttons: [
                {
                    xtype: 'label',
                    id: 'projectMakeInfoId',
                    style: 'color:red;font-size:14px;font-weight:bold;'
                },
                '->',
                {
                    text: '刷新',
                    iconCls: 'cloud icon-refresh',
                    reference: 'projectLoadRef',
                    disabled: false,
                    formBind: false,
                    tooltip: '加载已有工程配置方案，在工程名称列表中选择。',
                    handler: 'onLoadProject'
                },
                {
                    text: '删除工程',
                    iconCls: 'cloud icon-delete',
                    reference: 'projectDeleteRef',
                    disabled: true,
                    formBind: true,
                    handler: 'onDeleteProject'
                },
                {
                    text: '制作工程',
                    iconCls: 'cloud icon-settings',
                    reference: 'projectMakeRef',
                    disabled: true,
                    formBind: true,
                    handler: 'onMakeProject'
                },
                {
                    xtype: 'button',
                    iconCls: 'cloud icon-reset',
                    text: '重置',
                    handler: 'onPCReset'
                }
            ]
        },
        {
            xtype: 'panel',
            title: '使用说明',
            border: true,
            ui: 'project-panel-ui',
            iconCls: 'fa fa-exclamation-circle',
            bodyPadding: 10,
            margin: '0 0 0 1',
            flex: 1,
            height: 610,
            html: '<span style="font-size: 14px; color: #212121; line-height: 150%;"><b>1</b>、,目前已经支持Oracle、MySQL、PostgresSQL数据库，请配置正确的数据库连接信息并测试通过，方可进行下一步操作;<br/><br/><b>2</b>、系统初始化默认加载所有已经创建工程配置信息;<br/><br/><b>3</b>、可以在“工程名称”栏输入新的工程名称并填写其他配置信息，点击“制作工程”创建新的配置或直接选择已有配置开始下一步，注意不要点击“制作工程”，否则会重置配置并清空对应项目已有文件。</span>'
        }
    ]
});
