/**
 * Created by winnerlbm on 2019/7/3.
 */
Ext.define('acsweb.view.mapregedit.MapRegeditController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.mapregedit',

    requires: [
        'Ext.data.Store',
        'Ext.data.TreeStore',
        'Ext.form.Panel',
        'Ext.form.field.Checkbox',
        'Ext.form.field.Text',
        'Ext.layout.container.VBox',
        'Ext.toolbar.Fill',
        'acsweb.util.CustomWindow'
    ],

    //存储当前选中数据库配置
    dbConfig: null,
    /**
     * Called when the view is created
     */
    init: function () {

    },
    afterrenderHandler: function () {
        //数据库配置
        this.onLoadDataBaseConfigList();
        this.loadSvrTypeList('SERVICE');
        this.loadSvrCrsList('CRS');
    },
    openGroupWindow: function (action, record) {
        let win = Ext.create('widget.cwindow', {
            title: '服务分组管理',
            iconCls: 'cloud icon-layergroup',
            closeToolText: '关闭',
            height: 200,
            width: 300,
            bodyPadding: 5
        }).show(), me = this;

        let params = {
                dbAddress: me.dbConfig['dbAddress'],
                dbDesc: me.dbConfig['dbDesc'],
                dbName: me.dbConfig['dbName'],
                dbPassword: me.dbConfig['dbPassword'],
                dbPort: me.dbConfig['dbPort'],
                dbType: me.dbConfig['dbType'],
                dbUser: me.dbConfig['dbUser']
            }, svrCatalogTree = me.lookupReference('svrCatalogTreeRef'),
            svrTreeStore = svrCatalogTree.getStore(), targetNode = {
                catalogId: null,
                level: 1,
                children: [],
                nId: null,
                text: null,
                isGroup: true,
                iconCls: "cloud icon-layergroup",
                leaf: true,
                pnId: "n0"
            },
            targetParentNode = null;

        let formPanel = null;
        if (action === 'add') {
            formPanel = Ext.create('Ext.form.Panel', {
                border: true,
                bodyPadding: 5,
                fieldDefaults: {
                    labelAlign: 'left',
                    msgTarget: 'side',
                    labelWidth: 60,
                    labelStyle: 'font-weight:bold'
                },

                defaults: {
                    xtype: 'textfield',
                    readOnly: false,
                    border: false,
                    allowBlank: false
                },
                layout: {
                    type: 'vbox',
                    pack: 'start',
                    align: 'stretch'
                },
                items: [
                    {
                        fieldLabel: '分组名称',
                        name: 'nodeName',
                        margin: '0 0 10 0'
                    },
                    {
                        xtype: 'checkbox',
                        fieldLabel: '分组级别',
                        boxLabel: '作为第一级分组',
                        name: 'nodeLevel',
                        margin: '0 0 10 0'
                    }
                ],
                buttons: [
                    '->',
                    {
                        text: '取消', handler: function () {
                            if (win) {
                                win.close();
                            }
                        }
                    },
                    {
                        text: '保存', formBind: true, handler: function () {
                            if (me.dbConfig) {
                                let formData = formUtil.getFormData(formPanel);
                                params['nodeName'] = formData['nodeName'];
                                if (formData['nodeLevel'] === 'on') {
                                    targetParentNode = svrCatalogTree.getRootNode();
                                    //作为一级分组
                                    params['nodeLevel'] = 1;
                                    params['nodePid'] = 'n0';
                                } else {
                                    targetParentNode = svrCatalogTree.selModel.getSelection()[0];
                                    if (targetParentNode) {
                                        params['nodeLevel'] = targetParentNode.get('level') + 1;
                                        params['nodePid'] = targetParentNode.get('nId');
                                        targetParentNode.set('leaf', false);//追加子节点后修改非叶子节点
                                    } else {
                                        //若没有选择任何节点，作为一级分组
                                        targetParentNode = svrCatalogTree.getRootNode();
                                        params['nodeLevel'] = 1;
                                        params['nodePid'] = 'n0';
                                    }
                                }
                                params['nodeId'] = guidUtil.guid(4);
                                params['catalogId'] = guidUtil.guid(6);


                                //组装临时分组项
                                targetNode['catalogId'] = params['catalogId'];
                                targetNode['level'] = params['nodeLevel'];
                                targetNode['nId'] = params['nodeId'];
                                targetNode['text'] = params['nodeName'];
                                targetNode['pnId'] = params['nodePid'];


                                //判断是否已经存在同名节点
                                if (svrTreeStore.find("text", params['nodeName']) > -1) {
                                    Ext.Msg.alert('警告', '已存在同名节点，请重新输入。');
                                    return;
                                }

                                let mask = ajax.fn.showMask(formPanel, '保存中...');

                                //执行成功回调
                                function successCallBack(response, opts) {
                                    ajax.fn.hideMask(mask);
                                    //查询结果转json对象
                                    let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                                    //执行成功,更新Web表格
                                    if (result['state'] === 1) {
                                        let newNode = targetParentNode.insertChild(0, targetNode);

                                        if (!targetParentNode.isExpanded()) {
                                            targetParentNode.expand(false);
                                        }
                                        //是否选中新增组
                                        //svrCatalogTree.selModel.select(newNode);

                                        //关闭窗口
                                        win.close();
                                    }
                                }

                                //执行失败回调
                                function failureCallBack(response, opts) {
                                    ajax.fn.hideMask(mask);
                                }

                                ajax.fn.execute(params, 'GET', conf.serviceUrl + 'servicemanager/addcatalog', successCallBack, failureCallBack);
                            }
                        }
                    },
                    '->'
                ]
            });
            win.add(formPanel);
        } else if (action === 'edit') {
            formPanel = Ext.create('Ext.form.Panel', {
                border: true,
                bodyPadding: 10,
                fieldDefaults: {
                    labelAlign: 'left',
                    msgTarget: 'side',
                    labelWidth: 60,
                    labelStyle: 'font-weight:bold'
                },

                defaults: {
                    xtype: 'textfield',
                    readOnly: false,
                    border: false,
                    allowBlank: false
                },
                layout: {
                    type: 'vbox',
                    pack: 'start',
                    align: 'stretch'
                },
                items: [
                    {
                        fieldLabel: '分组名称',
                        name: 'nodeName',
                        margin: '0 0 10 0'
                    },
                    {
                        xtype: 'checkbox',
                        disabled: true,
                        fieldLabel: '分组级别',
                        boxLabel: '作为第一级分组',
                        name: 'nodeLevel',
                        margin: '0 0 10 0'
                    }],
                buttons: [
                    '->',
                    {
                        text: '取消', handler: function () {
                            if (win) {
                                win.close();
                            }
                        }
                    },
                    {
                        text: '保存', formBind: true, handler: function () {
                            //获取表单数据
                            let formData = formUtil.getFormData(formPanel);
                            params['nodeName'] = formData['nodeName'];
                            let currentNode = svrCatalogTree.selModel.getSelection()[0];
                            params['catalogId'] = currentNode.get('catalogId');

                            //判断是否已经存在同名节点
                            if (svrTreeStore.find("text", params['nodeName']) > -1) {
                                Ext.Msg.alert('警告', '已存在同名节点，请重新输入。');
                                return;
                            }

                            let mask = ajax.fn.showMask(formPanel, '保存中...');

                            //执行成功回调
                            function successCallBack(response, opts) {
                                ajax.fn.hideMask(mask);
                                //查询结果转json对象
                                let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                                //执行成功,更新Web表格对应的记录
                                if (result['state'] === 1) {
                                    //分组创建成功，执行前端Tree组件内容刷新
                                    currentNode.set('text', formData['nodeName']);

                                    //关闭窗口
                                    win.close();
                                }
                            }

                            //执行失败回调
                            function failureCallBack(response, opts) {
                                ajax.fn.hideMask(mask);
                            }

                            ajax.fn.execute(params, 'GET', conf.serviceUrl + 'servicemanager/editcatalog', successCallBack, failureCallBack);
                        }
                    },
                    '->'
                ]
            });
            win.add(formPanel);
            let record = svrCatalogTree.selModel.getSelection()[0];
            this.editGroup(formPanel, record);
        }
    },
    //执行新增操作
    onAddClick: function (button, eOpts) {
        this.openGroupWindow(button['action']);
    },
    //执行编辑操作
    onEditClick: function (button, eOpts) {
        if (this.dbConfig) {
            let currentNode = this.lookupReference('svrCatalogTreeRef').selModel.getSelection()[0];
            if (currentNode) {
                this.openGroupWindow(button['action']);
            }
        }
    },
    //执行删除操作
    onRemoveClick: function (button, eOpts) {
        if (this.dbConfig) {
            let currentNode = this.lookupReference('svrCatalogTreeRef').selModel.getSelection()[0];
            if (currentNode) {
                let me = this, children = currentNode.get('children');
                if (children && children.length > 0) {
                    //判断当前组是否包含子节点
                    Ext.Msg.alert('警告', '请先清空子节点再删除。');
                    return;
                }
                //询问是否删除
                Ext.Msg.show({
                    title: '温馨提示',
                    closeToolText: '关闭',
                    buttonText: {
                        ok: '确定',
                        cancel: '取消'
                    },
                    message: '是否删除当前分组?',
                    buttons: Ext.Msg.OKCANCEL,
                    icon: Ext.Msg.QUESTION,
                    fn: function (btn) {
                        if (btn === 'ok') {
                            //执行删除
                            me.removeGroup(currentNode);
                        }
                    }
                });
            }
        }
    },
    editGroup: function (formPanel, record) {
        if (record) {
            let data = record.getData();
            if (data) {
                let form = formPanel.getForm();
                form.findField('nodeName').setValue(data['text']);
                form.findField('nodeLevel').setValue(data['level']);
            }
        }
    },
    //删除记录
    removeGroup: function (record) {
        let params = {
            dbAddress: this.dbConfig['dbAddress'],
            dbDesc: this.dbConfig['dbDesc'],
            dbName: this.dbConfig['dbName'],
            dbPassword: this.dbConfig['dbPassword'],
            dbPort: this.dbConfig['dbPort'],
            dbType: this.dbConfig['dbType'],
            dbUser: this.dbConfig['dbUser'],
            catalogId: record.get('catalogId')
        }, me = this;

        let meView = this.getView();
        let mask = ajax.fn.showMask(meView, '删除中...');

        //执行成功回调
        function successCallBack(response, opts) {
            ajax.fn.hideMask(mask);
            //查询结果转json对象
            let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
            //执行成功,web表格释放对应的记录
            if (result['state'] === 1) {
                //获取父节点
                let svrCatalogTree = me.lookupReference('svrCatalogTreeRef'), parentNodeId = record.get('pnId'),
                    parentNode = null;
                if (parentNodeId === 'n0') {
                    parentNode = svrCatalogTree.getRootNode();
                } else {
                    parentNode = svrCatalogTree.getStore().findNode('nId', parentNodeId);
                }

                let childrenCount = 0;
                if (parentNode.get('children') && parentNode.get('children').length > 0) {
                    childrenCount = parentNode.get('children').length;
                }

                if (parentNode) {
                    parentNode.removeChild(record);
                    //节点移除之后需要释放内存
                    record.drop();

                    if (!parentNode.hasChildNodes() || childrenCount === 1) {
                        parentNode.set('children', []);
                        parentNode.set('leaf', true);
                    }
                }
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
            ajax.fn.hideMask(mask);
        }

        ajax.fn.execute(params, 'GET', conf.serviceUrl + 'servicemanager/deletecatalog', successCallBack, failureCallBack);
    },
    selectDBciHandler: function (combo, record, eOpts) {
        if (record) {
            //0、清空服务信息面板
            this.resetService();

            //1、获取数据库配置信息并填充配置信息
            let dbc = record.getData();
            this.dbConfig = dbc;
            if (this.dbConfig) {
                let params = {
                    dbAddress: this.dbConfig['dbAddress'],
                    dbDesc: this.dbConfig['dbDesc'],
                    dbName: this.dbConfig['dbName'],
                    dbPassword: this.dbConfig['dbPassword'],
                    dbPort: this.dbConfig['dbPort'],
                    dbType: this.dbConfig['dbType'],
                    dbUser: this.dbConfig['dbUser']
                }, me = this;

                //执行成功回调
                function successCallBack(response, opts) {
                    //初始化服务注册表
                    me.initSvrRegedit();

                    //启用组操作
                    me.switchGroupButtons(true);
                }

                //执行失败回调
                function failureCallBack(response, opts) {
                    alert("连接失败");
                }

                ajax.fn.execute(params, 'POST', conf.serviceUrl + 'coding/testdb', successCallBack, failureCallBack);
            }
        }
    },
    initSvrRegedit: function () {
        if (this.dbConfig) {
            let me = this;
            let params = {
                dbAddress: this.dbConfig['dbAddress'],
                dbDesc: this.dbConfig['dbDesc'],
                dbName: this.dbConfig['dbName'],
                dbPassword: this.dbConfig['dbPassword'],
                dbPort: this.dbConfig['dbPort'],
                dbType: this.dbConfig['dbType'],
                dbUser: this.dbConfig['dbUser']
            };

            //执行成功回调
            function successCallBack(response, opts) {
                //执行已注册服务目录树查询
                me.getSvrTree();
            }

            //执行失败回调
            function failureCallBack(response, opts) {
            }

            ajax.fn.execute(params, 'GET', conf.serviceUrl + 'servicemanager/initsvrregedit', successCallBack, failureCallBack);
        }
    },
    getSvrTree: function () {
        if (this.dbConfig) {
            let svrCatalogTree = this.lookupReference('svrCatalogTreeRef');
            let params = {
                dbAddress: this.dbConfig['dbAddress'],
                dbDesc: this.dbConfig['dbDesc'],
                dbName: this.dbConfig['dbName'],
                dbPassword: this.dbConfig['dbPassword'],
                dbPort: this.dbConfig['dbPort'],
                dbType: this.dbConfig['dbType'],
                dbUser: this.dbConfig['dbUser']
            };
            svrCatalogTree.setStore(null);

            //执行成功回调
            function successCallBack(response, opts) {
                //查询结果转json对象
                let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
                if (result && result['data']) {
                    //执行已注册服务目录树查询
                    let svrStore = new Ext.create('Ext.data.TreeStore', {
                        data: result['data']
                    });
                    svrCatalogTree.setStore(svrStore);

                    //默认展开第一个分组
                    let firstNode = svrStore.getAt(0);
                    svrCatalogTree.selModel.select(firstNode);
                    if (firstNode.get('isGroup') && !firstNode.get('leaf') && !firstNode.isExpanded()) {
                        firstNode.expand(false);
                    }

                    //展开所有分组
                    //svrCatalogTree.expandAll();
                }
            }

            //执行失败回调
            function failureCallBack(response, opts) {
            }

            ajax.fn.execute(params, 'GET', conf.serviceUrl + 'servicemanager/getsvrtree', successCallBack, failureCallBack);
        }
    },
    allDataBaseInfoList: function () {
        //数据库配置
        this.onLoadDataBaseConfigList();
    },
    onLoadDataBaseConfigList: function () {
        let me = this;
        let dbcList = me.lookupReference('databaseConfigListRef');
        let params = {};

        //执行成功回调
        function successCallBack(response, opts) {
            //查询结果转json对象
            let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
            let dbs = result['dbs'];
            if (dbs && dbs.length > 0) {
                //此处先过滤数据类型，目前仅支持MySQL数据库
                let dbs4MySQL = [];
                for (let i = 0; i < dbs.length; i++) {
                    if (dbs[i]['dbType'] === 'com.mysql.jdbc.Driver') {
                        dbs4MySQL.push(dbs[i]);
                    }
                }

                if (dbs4MySQL.length > 0) {
                    let dbsStore = new Ext.create('Ext.data.Store', {
                        data: dbs4MySQL
                    });
                    dbcList.setStore(dbsStore);
                }
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
        }

        ajax.fn.execute(params, 'POST', conf.serviceUrl + 'coding/getdatabase', successCallBack, failureCallBack);
    },
    loadSvrTypeList: function (dictType) {
        let me = this;
        let svrTypeList = me.lookupReference('svrTypeListRef');
        let params = {
            dictType: dictType
        };

        //执行成功回调
        function successCallBack(response, opts) {
            //查询结果转json对象
            let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
            let data = result['data'];
            if (data && data.length > 0) {
                let dataStore = new Ext.create('Ext.data.Store', {
                    data: data
                });
                svrTypeList.setStore(dataStore);
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
        }

        ajax.fn.execute(params, 'GET', conf.serviceUrl + 'servicemanager/svrtypelist', successCallBack, failureCallBack);
    },
    loadSvrCrsList: function (dictType) {
        let me = this;
        let svrCrsList = me.lookupReference('svrCrsListRef');
        let params = {
            dictType: dictType
        };

        //执行成功回调
        function successCallBack(response, opts) {
            //查询结果转json对象
            let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
            let data = result['data'];
            if (data && data.length > 0) {
                let dataStore = new Ext.create('Ext.data.Store', {
                    data: data
                });
                svrCrsList.setStore(dataStore);
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
        }

        ajax.fn.execute(params, 'GET', conf.serviceUrl + 'servicemanager/svrcrslist', successCallBack, failureCallBack);
    },
    itemclickHandler: function (tp, record, item, index, e, eOpts) {
        let isGroup = record.get('isGroup');
        this.switchGroupButtons(isGroup);

        //选择服务节点则显示服务信息
        if (!isGroup) {
            this.showServiceInfo(record);
        } else {
            this.resetService();
        }
    },
    switchGroupButtons: function (isGroup) {
        let addGroupRef = this.lookupReference('addGroupRef'), editGroupRef = this.lookupReference('editGroupRef'),
            deleteGroupRef = this.lookupReference('deleteGroupRef');
        let groupRefs = [addGroupRef, editGroupRef, deleteGroupRef];
        for (let i = 0; i < groupRefs.length; i++) {
            isGroup ? groupRefs[i].enable() : groupRefs[i].disable();
        }
    },
    //服务信息表单重置
    resetService: function () {
        let formPanel = this.lookupReference('svrInfoFormRef'),
            form = formPanel.getForm();

        form.reset();
    },
    //新建服务
    addService: function () {
        if (this.dbConfig) {
            let me = this, params = {
                    dbAddress: me.dbConfig['dbAddress'],
                    dbDesc: me.dbConfig['dbDesc'],
                    dbName: me.dbConfig['dbName'],
                    dbPassword: me.dbConfig['dbPassword'],
                    dbPort: me.dbConfig['dbPort'],
                    dbType: me.dbConfig['dbType'],
                    dbUser: me.dbConfig['dbUser']
                }, svrCatalogTree = me.lookupReference('svrCatalogTreeRef'),
                svrTreeStore = svrCatalogTree.getStore(), targetNode = {
                    svrId: null,
                    svrLayerName: null,
                    svrUrl: null,
                    svrDate: null,
                    svrProviderId: null,
                    text: null,
                    iconCls: "cloud icon-map",
                    leaf: true,
                    svrProvider: null,
                    svrType: null,
                    svrSrid: null,
                    cx: 0,
                    cy: 0,
                    west: 0,
                    south: 0,
                    east: 0,
                    north: 0,
                    svrDesc: null
                },
                targetParentNode = null;

            let formPanel = me.lookupReference('svrInfoFormRef');
            let formData = formUtil.getFormData(formPanel);
            params['svrName'] = formData['svrName'];
            params['svrLayerName'] = formData['svrLayerName'];
            params['svrType'] = formData['svrTypeName'];
            params['svrSrid'] = formData['svrCRSName'];
            params['svrUrl'] = formData['svrAddress'];
            params['svrProvider'] = formData['svrProvider'];
            params['svrProviderId'] = '';
            params['svrDescription'] = formData['svrDescription'];
            //地图服务范围相关
            params['cx'] = formData['cx'];
            params['cy'] = formData['cy'];
            params['west'] = formData['west'];
            params['south'] = formData['south'];
            params['east'] = formData['east'];
            params['north'] = formData['north'];

            targetParentNode = svrCatalogTree.selModel.getSelection()[0];
            if (targetParentNode) {
                //当前选中为Group
                if (targetParentNode.get('isGroup')) {
                    //当前选中节点为组,服务所在组目录ID
                    params['catalogId'] = targetParentNode.get('catalogId');
                    targetParentNode.set('leaf', false);//追加子节点后修改非叶子节点
                } else {
                    //若当前选中为服务，则获取该服务的父节点作为新建服务的父节点，新建服务与当前选中服务在同一分组下
                    targetParentNode = targetParentNode.parentNode;
                    params['catalogId'] = targetParentNode.get('catalogId');
                }
            } else {
                //创建服务之前需要先创建分组
                Ext.Msg.alert('警告', '只能在组节点下创建服务。');
                return;
            }

            params['serviceId'] = guidUtil.guid(4);

            //组装临时服务项
            targetNode['svrId'] = params['serviceId'];
            targetNode['svrUrl'] = params['svrUrl'];
            targetNode['svrDate'] = new Date().format('"yyyy-MM-dd hh:mm:ss');
            targetNode['svrProviderId'] = params['svrProviderId'];
            targetNode['text'] = params['svrName'];
            targetNode['svrLayerName'] = params['svrLayerName'];
            targetNode['leaf'] = true;
            targetNode['svrProvider'] = params['svrProvider'];
            targetNode['svrType'] = params['svrType'];
            targetNode['svrSrid'] = params['svrSrid'];
            targetNode['svrDesc'] = params['svrDescription'];
            targetNode['cx'] = params['cx'];
            targetNode['cy'] = params['cy'];
            targetNode['west'] = params['west'];
            targetNode['south'] = params['south'];
            targetNode['east'] = params['east'];
            targetNode['north'] = params['north'];

            //判断是否已经存在同名节点
            if (svrTreeStore.find("text", params['svrName']) > -1) {
                Ext.Msg.alert('警告', '已存在同名节点，请重新输入。');
                return;
            }

            let mask = ajax.fn.showMask(formPanel, '保存中...');

            //执行成功回调
            function successCallBack(response, opts) {
                ajax.fn.hideMask(mask);
                //查询结果转json对象
                let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                //执行成功,更新Web表格
                if (result['state'] === 1) {
                    let newNode = targetParentNode.insertChild(0, targetNode);

                    if (!targetParentNode.isExpanded()) {
                        targetParentNode.expand(false);
                    }
                    svrCatalogTree.selModel.select(newNode);
                }
            }

            //执行失败回调
            function failureCallBack(response, opts) {
                ajax.fn.hideMask(mask);
            }

            ajax.fn.execute(params, 'GET', conf.serviceUrl + 'servicemanager/addservice', successCallBack, failureCallBack);

        }
    },
    //保存变更
    saveService: function () {
        if (this.dbConfig) {
            let me = this, params = {
                    dbAddress: me.dbConfig['dbAddress'],
                    dbDesc: me.dbConfig['dbDesc'],
                    dbName: me.dbConfig['dbName'],
                    dbPassword: me.dbConfig['dbPassword'],
                    dbPort: me.dbConfig['dbPort'],
                    dbType: me.dbConfig['dbType'],
                    dbUser: me.dbConfig['dbUser']
                }, svrCatalogTree = me.lookupReference('svrCatalogTreeRef'),
                svrTreeStore = svrCatalogTree.getStore(), targetNode = svrCatalogTree.selModel.getSelection()[0];

            let formPanel = me.lookupReference('svrInfoFormRef');
            let formData = formUtil.getFormData(formPanel);
            params['svrName'] = formData['svrName'];
            params['svrLayerName'] = formData['svrLayerName'];
            params['svrType'] = formData['svrTypeName'];
            params['svrSrid'] = formData['svrCRSName'];
            params['svrUrl'] = formData['svrAddress'];
            params['svrProvider'] = formData['svrProvider'];
            params['svrProviderId'] = '';
            params['svrDescription'] = formData['svrDescription'];
            //地图服务范围相关
            params['cx'] = formData['cx'];
            params['cy'] = formData['cy'];
            params['west'] = formData['west'];
            params['south'] = formData['south'];
            params['east'] = formData['east'];
            params['north'] = formData['north'];
            params['serviceId'] = targetNode.get('svrId');

            //判断是否已经存在其他同名节点
            let poiNode = svrTreeStore.findNode("text", params['svrName']);
            if (poiNode && poiNode !== targetNode) {
                Ext.Msg.alert('警告', '已存在同名节点，请重新输入。');
                return;
            }

            let mask = ajax.fn.showMask(formPanel, '保存中...');

            //执行成功回调
            function successCallBack(response, opts) {
                ajax.fn.hideMask(mask);
                //查询结果转json对象
                let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                //执行成功,更新Web表格
                if (result['state'] === 1) {
                    //当前节点属性刷新
                    targetNode.set('svrUrl', params['svrUrl']);
                    targetNode.set('svrDate', new Date().format('"yyyy-MM-dd hh:mm:ss'));
                    targetNode.set('svrProviderId', params['svrProviderId']);
                    targetNode.set('text', params['svrName']);
                    targetNode.set('svrLayerName', params['svrLayerName']);
                    targetNode.set('leaf', true);
                    targetNode.set('svrProvider', params['svrProvider']);
                    targetNode.set('svrType', params['svrType']);
                    targetNode.set('svrSrid', params['svrSrid']);
                    targetNode.set('svrDesc', params['svrDescription']);
                    targetNode.set('cx', params['cx']);
                    targetNode.set('cy', params['cy']);
                    targetNode.set('west', params['west']);
                    targetNode.set('south', params['south']);
                    targetNode.set('east', params['east']);
                    targetNode.set('north', params['north']);
                }
            }

            //执行失败回调
            function failureCallBack(response, opts) {
                ajax.fn.hideMask(mask);
            }

            ajax.fn.execute(params, 'GET', conf.serviceUrl + 'servicemanager/editservice', successCallBack, failureCallBack);

        }
    },
    //删除服务
    deleteService: function () {
        if (this.dbConfig) {
            let currentNode = this.lookupReference('svrCatalogTreeRef').selModel.getSelection()[0];
            if (currentNode) {
                let me = this, isGroup = currentNode.get('isGroup');
                if (isGroup) {
                    //判断当前组是否为服务节点
                    Ext.Msg.alert('警告', '请选择服务节点再删除。');
                    return;
                }
                //询问是否删除
                Ext.Msg.show({
                    title: '温馨提示',
                    closeToolText: '关闭',
                    buttonText: {
                        ok: '确定',
                        cancel: '取消'
                    },
                    message: '是否删除当前服务?',
                    buttons: Ext.Msg.OKCANCEL,
                    icon: Ext.Msg.QUESTION,
                    fn: function (btn) {
                        if (btn === 'ok') {
                            //执行删除
                            me.removeService(currentNode);
                        }
                    }
                });
            }
        }
    },
    removeService: function (record) {
        let params = {
            dbAddress: this.dbConfig['dbAddress'],
            dbDesc: this.dbConfig['dbDesc'],
            dbName: this.dbConfig['dbName'],
            dbPassword: this.dbConfig['dbPassword'],
            dbPort: this.dbConfig['dbPort'],
            dbType: this.dbConfig['dbType'],
            dbUser: this.dbConfig['dbUser'],
            serviceId: record.get('svrId')
        };

        let meView = this.getView();
        let mask = ajax.fn.showMask(meView, '删除中...');

        //执行成功回调
        function successCallBack(response, opts) {
            ajax.fn.hideMask(mask);
            //查询结果转json对象
            let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
            //执行成功,web表格释放对应的记录
            if (result['state'] === 1) {
                //获取父节点
                let parentNode = record.parentNode;

                let childrenCount = 0;
                if (parentNode.get('children') && parentNode.get('children').length > 0) {
                    childrenCount = parentNode.get('children').length;
                }

                if (parentNode) {
                    parentNode.removeChild(record);
                    //节点移除之后需要释放内存
                    record.drop();

                    if (!parentNode.hasChildNodes() || childrenCount === 1) {
                        parentNode.set('children', []);
                        parentNode.set('leaf', true);
                    }
                }
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
            ajax.fn.hideMask(mask);
        }

        ajax.fn.execute(params, 'GET', conf.serviceUrl + 'servicemanager/deleteservice', successCallBack, failureCallBack);
    },
    //显示服务信息
    showServiceInfo: function (record) {
        if (record) {
            let formPanel = this.lookupReference('svrInfoFormRef'),
                form = formPanel.getForm();
            if (formPanel.isHidden) {
                formPanel.show();
            }

            form.findField('svrName').setValue(record.get('text'));
            form.findField('svrLayerName').setValue(record.get('svrLayerName'));
            form.findField('svrTypeName').setValue(record.get('svrType'));
            form.findField('svrCRSName').setValue(record.get('svrSrid'));
            form.findField('svrAddress').setValue(record.get('svrUrl'));
            form.findField('svrProvider').setValue(record.get('svrProvider'));
            form.findField('svrDescription').setValue(record.get('svrDesc'));

            //可选项--地图服务范围相关
            form.findField('cx').setValue(record.get('cx'));
            form.findField('cy').setValue(record.get('cy'));
            form.findField('west').setValue(record.get('west'));
            form.findField('south').setValue(record.get('south'));
            form.findField('east').setValue(record.get('east'));
            form.findField('north').setValue(record.get('north'));
        }
    }
});
