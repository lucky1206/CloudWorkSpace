/**
 * Created by winnerlbm on 2018/9/4.
 */
Ext.define('acsweb.view.entitymanager.customizeentity.CustomizeEntityController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.customizeentity',

    requires: [
        'Ext.data.Store'
    ],

    /**
     * Called when the view is created
     */
    init: function () {

    },
    afterrenderHandler: function () {
        if (conf.dbConfig && conf.projectConfig && conf.dbTables && conf.projectCatalogData) {
            //加载库表信息
            this.loadTable(conf.dbTables);

            this.onLoadApiGroupList();
        } else {
            Ext.Msg.alert('提示', "请先完成数据表加载操作！");
        }
    },
    loadTable: function (tbs) {
        let dbTableGridRef = this.lookupReference('dbTableGridRef');
        if (dbTableGridRef && tbs) {
            let dbTableStore = new Ext.create('Ext.data.Store', {
                data: tbs
            });
            dbTableGridRef.setStore(dbTableStore);

            //默认显示第一条记录详细信息
            let firstRecord = dbTableStore.getAt(0);
            dbTableGridRef.getSelectionModel().select(firstRecord);
            this.showTableInfo(firstRecord);
        }
    },
    //功能与模块渲染完成事件一致
    reloadCustomizeData: function () {
        if (conf.dbConfig && conf.projectConfig && conf.dbTables && conf.projectCatalogData) {
            //加载库表信息
            this.loadTable(conf.dbTables);

            this.onLoadApiGroupList();
        } else {
            Ext.Msg.alert('提示', "请先完成数据表加载操作！");
        }
    },
    createApi4Customize: function () {
        let cil = Ext.getCmp('customizeInfoId');
        cil.setHtml('');
        if (conf.dbConfig && conf.projectConfig && conf.dbTables && conf.projectCatalogData) {
            //表列表
            let dbTableGridRef = this.lookupReference('dbTableGridRef');
            //查询字段列表
            let dbQueryFieldGridRef = this.lookupReference('dbQueryFieldGridRef');
            //条件字段列表
            let dbConditionFieldGridRef = this.lookupReference('dbConditionFieldGridRef');
            //参数设置
            let cApiFormRef = this.lookupReference('cApiFormRef');
            if (dbTableGridRef && dbQueryFieldGridRef && dbConditionFieldGridRef && cApiFormRef) {
                let dbTableSelection = dbTableGridRef.getSelectionModel().getSelection(),
                    dbQueryFieldSelection = dbQueryFieldGridRef.getSelectionModel().getSelection(),
                    dbConditionFieldSelection = dbConditionFieldGridRef.getSelectionModel().getSelection(),
                    apiConfig = cApiFormRef.getForm().getValues();
                let tableJson, queryFieldJson, queryFields = [], conditionFieldJson, conditionFields = [],
                    apiConfigJson = Ext.JSON.encode(apiConfig);

                //查询表
                if (dbTableSelection && dbTableSelection.length > 0) {
                    let tempTable = {};
                    for (let key in dbTableSelection[0].getData()) {
                        if (key !== 'columns') {
                            tempTable[key] = dbTableSelection[0].getData()[key];
                        } else {
                            tempTable['columns'] = [];
                            let cols = dbTableSelection[0].getData()['columns'];
                            let len = cols.getCount();

                            for (let i = 0; i < len; i++) {
                                let tempColumn = {};
                                let colObj = cols.getAt(i);
                                tempColumn["dbType"] = colObj.get("dbType");
                                tempColumn["dbname"] = colObj.get("dbname");
                                tempColumn["desc"] = colObj.get("desc");
                                tempColumn["javaType"] = colObj.get("javaType");
                                tempColumn["javaname"] = colObj.get("javaname");
                                tempColumn["pk"] = colObj.get("pk");
                                tempColumn["upname"] = colObj.get("upname");
                                tempTable['columns'].push(tempColumn);
                            }
                        }
                    }
                    tableJson = Ext.JSON.encode(tempTable);
                }

                //查询字段
                if (dbQueryFieldSelection && dbQueryFieldSelection.length > 0) {
                    let tqfs = [];
                    for (let i = 0; i < dbQueryFieldSelection.length; i++) {
                        let tempField = {};
                        let fieldObj = dbQueryFieldSelection[i].getData();
                        tempField["dbType"] = fieldObj["dbType"];
                        tempField["dbname"] = fieldObj["dbname"];
                        tempField["desc"] = fieldObj["desc"];
                        tempField["javaType"] = fieldObj["javaType"];
                        tempField["javaname"] = fieldObj["javaname"];
                        tempField["pk"] = fieldObj["pk"];
                        tempField["upname"] = fieldObj["upname"];
                        tempField["operator"] = fieldObj["operator"];
                        tqfs.push(tempField);
                    }
                    queryFieldJson = Ext.JSON.encode(tqfs);
                }

                //条件字段
                if (dbConditionFieldSelection && dbConditionFieldSelection.length > 0) {
                    let tcfs = [];
                    for (let i = 0; i < dbConditionFieldSelection.length; i++) {
                        let tempField = {};
                        let tempCondition = dbConditionFieldSelection[i].getData();
                        for (let key in tempCondition) {
                            if (key !== 'operators' && key !== 'id') {
                                tempField[key] = tempCondition[key];
                            }
                        }
                        tcfs.push(tempField);
                    }
                    conditionFieldJson = Ext.JSON.encode(tcfs);
                }

                //工程配置
                let ppsJson = Ext.JSON.encode(conf.projectCatalogData);

                //提交创建
                let mask = ajax.fn.showMask(this.getView(), '代码创建中...');
                let params = {
                    pps: ppsJson,
                    table: tableJson,
                    queryFields: queryFieldJson,
                    conditionFields: conditionFieldJson,
                    apiConfig: apiConfigJson,
                    isPagging: Ext.getCmp('isPageCheckId4C').getValue(), //查询是否分页，默认不分页
                    isModuling: Ext.getCmp('isModuleCheckId4C').getValue(), //是否生成JS模块，默认开启
                    isManaging: Ext.getCmp('isManageCheckId4C').getValue() //是否启用表单管理,如增删改，默认不开启
                };

                //执行成功回调
                function successCallBack(response, opts) {
                    //查询结果转json对象
                    let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                    if (result['success']) {
                        cil.setHtml('接口创建成功');
                    } else {
                        cil.setHtml('接口创建失败');
                    }
                    ajax.fn.hideMask(mask);
                }

                //执行失败回调
                function failureCallBack(response, opts) {
                    cil.setHtml('接口创建失败');
                    ajax.fn.hideMask(mask);
                }

                ajax.fn.execute(params, 'POST', conf.serviceUrl + 'coding/coding4customize', successCallBack, failureCallBack);

            }
        } else {
            cil.setHtml('请先完成数据表加载操作！');
        }
    },
    //设置接口sql参数操作符
    operatorSelectHandler: function (combo, record, eOpts) {
        let dbConditionFieldGridRef = this.lookupReference('dbConditionFieldGridRef');
        if (dbConditionFieldGridRef && dbConditionFieldGridRef.getStore()) {
            let dbConditionFieldStore = dbConditionFieldGridRef.getStore();
            let index = parseInt(record.store.getStoreId().split('-')[1]);
            dbConditionFieldStore.getAt(index).set('operator', record.get('value'));
        }
    },
    //------接口分组配置-------------------------------------------
    onDeleteApiGroupList: function () {
        let groupList = this.lookupReference('apiGroupListRef');
        let groupName = groupList.getValue();
        if (groupName) {
            let me = this;
            let agcForm = this.lookupReference('cApiFormRef');
            let mask = ajax.fn.showMask(agcForm, '接口分组删除中...');

            let params = {
                groupName: groupName
            };

            //执行成功回调
            function successCallBack(response, opts) {
                me.onLoadApiGroupList();
                groupList.reset();
                ajax.fn.hideMask(mask);
            }

            //执行失败回调
            function failureCallBack(response, opts) {
                ajax.fn.hideMask(mask);
            }

            ajax.fn.execute(params, 'GET', conf.serviceUrl + 'coding/deleteapigroup', successCallBack, failureCallBack);
        }
    },
    onLoadApiGroupList: function () {
        let me = this;
        let agcForm = me.lookupReference('cApiFormRef');
        let mask = ajax.fn.showMask(agcForm, '接口配置加载中...');
        let params = {};

        //执行成功回调
        function successCallBack(response, opts) {
            //查询结果转json对象
            let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
            let ags = result['ags'];
            if (ags && ags.length > 0) {
                let dbsStore = new Ext.create('Ext.data.Store', {
                    data: ags
                });
                me.lookupReference('apiGroupListRef').setStore(dbsStore);
            }

            ajax.fn.hideMask(mask);
        }

        //执行失败回调
        function failureCallBack(response, opts) {
            ajax.fn.hideMask(mask);
        }

        ajax.fn.execute(params, 'GET', conf.serviceUrl + 'coding/getapigroup', successCallBack, failureCallBack);
    },
    dbTableRowClick: function (grid, record, element, rowIndex, e, eOpts) {
        this.showTableInfo(record);
    },
    showTableInfo: function (record) {
        let dbTableFieldGridRef = this.lookupReference('dbQueryFieldGridRef'),
            tableFields = record.get('columns').getData(), tableName = record.get('dbtablename'),
            dbConditionFieldGridRef = this.lookupReference('dbConditionFieldGridRef');
        if (dbTableFieldGridRef && tableFields && dbConditionFieldGridRef) {
            //显示字段
            let dbTableFieldStore = new Ext.create('Ext.data.Store', {
                data: tableFields
            });
            dbTableFieldGridRef.setStore(dbTableFieldStore);
            dbTableFieldGridRef.setTitle(tableName + '--查询字段');

            //默认全选
            dbTableFieldGridRef.ownerGrid.getSelectionModel().selectAll();

            //条件字段
            let mutiViewStore = new Ext.create('Ext.data.Store', {
                data: tableFields
            });
            for (let i = 0; i < tableFields.length; i++) {
                tableFields.getAt(i).set('operator', '=')
            }

            //创建条件字段操作符关联store
            for (let i = 0; i < tableFields.length; i++) {
                let operatorStore = new Ext.create('Ext.data.Store', {
                    data: conf.operatorTypes
                });
                operatorStore.setStoreId('operatorStoreIndex-' + i);
                mutiViewStore.getAt(i).set('operators', operatorStore);
            }
            dbConditionFieldGridRef.setStore(mutiViewStore);
            dbConditionFieldGridRef.setTitle(tableName + '--条件字段');
        }
    }
});