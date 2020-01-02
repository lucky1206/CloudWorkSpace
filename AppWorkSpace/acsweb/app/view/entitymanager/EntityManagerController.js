/**
 * Created by LBM on 2017/12/18.
 */
Ext.define('acsweb.view.entitymanager.EntityManagerController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.entitymanager',

    requires: [
        'Ext.data.Store',
        'acsweb.util.CustomWindow'
    ],

    /**
     * Called when the view is created
     */
    init: function () {
    },
    afterrenderHandler: function () {
        this.loadDBTables();
    },
    afterrender4SqlHandler: function () {
        //加载接口分组列表
        this.onLoadApiGroupList();
    },
    //------接口分组配置-------------------------------------------
    onDeleteApiGroupList: function () {
        let groupList = this.lookupReference('apiGroupListRef');
        let groupName = groupList.getValue();
        if(groupName){
            let me = this;
            let agcForm = this.lookupReference('sqlFormRef');
            let mask = ajax.fn.showMask(agcForm, '接口分组删除中...');

            let params = {
                groupName:groupName
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
        let agcForm = me.lookupReference('sqlFormRef');
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
    loadDBTables: function () {
        let eml = Ext.get('tableInfoId');
        eml.setHtml('');
        if (conf.dbConfig && conf.projectConfig) {
            conf.dbConfig['isReload'] = false;
            this.getDBTables(conf.dbConfig);
        } else {
            eml.setHtml('请先完成“工程管理”操作！');
        }
    },
    refreshDBTables: function () {
        let eml = Ext.get('tableInfoId');
        eml.setHtml('');
        if (conf.dbConfig && conf.projectConfig) {
            conf.dbConfig['isReload'] = true;
            this.getDBTables(conf.dbConfig);
        } else {
            eml.setHtml('请先完成“工程管理”操作！');
        }
    },
    getDBTables: function (params) {
        let me = this;
        let emcGrid = me.lookupReference('dbResultGridRef');
        let mask = ajax.fn.showMask(emcGrid, '库表数据加载中...');
        //清空已有数据
        emcGrid.setStore(null);

        //执行成功回调
        function successCallBack(response, opts) {
            //查询结果转json对象
            let result = Ext.JSON.decode((response.responseText), true);
            //let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);//若中文乱码需要预处理
            let tbs = result['tbs'];
            if (tbs && tbs.length > 0) {
                conf.dbTables = tbs;

                let tbsStore = new Ext.create('Ext.data.Store', {
                    data: tbs
                });
                //创建关联store
                let index = 0;
                Ext.each(tbs, function (tb) {
                    if (tb != null) {
                        let colStore = new Ext.create('Ext.data.Store', {
                            data: tb['columns']
                        });
                        tbsStore.getAt(index).set('columns', colStore);
                    }
                    index++;
                });

                emcGrid.setStore(tbsStore);
            } else {
                //若缓存中没有数据则通过数据库重载库表
                me.refreshDBTables();
            }

            ajax.fn.hideMask(mask);
        }

        //执行失败回调
        function failureCallBack(response, opts) {
            ajax.fn.hideMask(mask);
        }

        ajax.fn.execute(params, 'POST', conf.serviceUrl + 'coding/gettables', successCallBack, failureCallBack);
    },
    //实体管理-一键生成数据表实体代码
    oneKeyCoder: function () {
        let eml = Ext.get('entityMakeInfoId');
        eml.setHtml('');
        if (conf.dbConfig && conf.projectConfig && conf.dbTables && conf.projectCatalogData) {
            let me = this;
            let egGrid = me.lookupReference('entityGridRef');
            //清空已有数据
            egGrid.setStore(null);
            let mask = ajax.fn.showMask(egGrid, '代码创建中...');

            //执行成功回调
            function successCallBack(response, opts) {
                //查询结果转json对象
                let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                let es = result['es'];
                if (es && es.length > 0) {
                    conf.projectCodeData = es;

                    let tbsStore = new Ext.create('Ext.data.Store', {
                        data: es
                    });
                    egGrid.setStore(tbsStore);
                }

                ajax.fn.hideMask(mask);
            }

            //执行失败回调
            function failureCallBack(response, opts) {
                ajax.fn.hideMask(mask);
            }

            ajax.fn.execute({
                pps: Ext.JSON.encode(conf.projectCatalogData),
                isPagging: Ext.getCmp('isPageCheckId1').getValue(), //查询是否分页，默认不分页
                isModuling: Ext.getCmp('isModuleCheckId1').getValue(), //是否生成JS模块，默认开启
                isManaging: Ext.getCmp('isManageCheckId1').getValue() //是否启用表单管理,如增删改，默认不开启
            }, 'POST', conf.serviceUrl + 'coding/coding4onekey', successCallBack, failureCallBack);
        }
        else {
            eml.setHtml('请先完成前置操作！');
        }
    },
    //实体管理-SQL语句解译，并生成对应的代码
    sqlParser: function () {
        let sml = Ext.get('sqlInfoId');
        sml.setHtml('');
        if (conf.dbConfig && conf.projectConfig && conf.dbTables && conf.projectCatalogData) {
            let me = this;
            let formPanel = me.lookupReference('sqlFormRef'),
                form = formPanel.getForm();

            let mask = ajax.fn.showMask(formPanel, 'SQL解译中...');

            form.submit({
                clientValidation: true,
                url: conf.serviceUrl + 'coding/sqlparse4all',
                method: "POST",
                params: {
                    pps: Ext.JSON.encode(conf.projectCatalogData),
                    isPagging: Ext.getCmp('isPageCheckId').getValue(), //查询是否分页，默认不分页
                    isModuling: Ext.getCmp('isModuleCheckId').getValue(), //是否生成JS模块，默认开启
                    isManaging: Ext.getCmp('isManageCheckId').getValue() //是否启用表单管理,如增删改，默认不开启
                },
                success: function (form, action) {
                    sml.setHtml('SQL解译成功！');

                    //显示SQL解译内容
                    let result = Ext.JSON.decode(action.response.responseText, true);
                    showCodeiFrame.window.writeCodeText(result);
                    ajax.fn.hideMask(mask);
                },
                failure: function (form, action) {
                    sml.setHtml('SQL解译失败，请检查SQL语句语法！');

                    ajax.fn.hideMask(mask);
                }
            });
        } else {
            sml.setHtml('请先完成前置操作！');
        }
    },
    //加载二次开发代码示例
    loadHtmlContent: function (iframe, url, mask, message, millisecond) {
        if (mask) {
            let loadMask = new Ext.LoadMask(iframe, {
                msg: message,
                style: {
                    width: '100%',
                    height: '100%',
                    background: '#FFFFFF'
                }
            });
            loadMask.show();
            Ext.defer(function () {
                loadMask.hide();
            }, millisecond);
        }

        iframe.load(url);
    },
    uxiframeRender: function (uxif, eOpts) {
        this.loadHtmlContent(uxif, 'resources/html/code.html', true, '内容格式化...', 1000);
        uxif.updateLayout();
    },
    //重置参数
    sqlParserReset: function () {
        let sml = Ext.get('sqlInfoId');
        sml.setHtml('');

        let st = Ext.getCmp('sourceTextId');
        if (st) {
            st.setValue('');
        }

        let me = this;
        let formPanel = me.lookupReference('sqlFormRef'),
            form = formPanel.getForm();

        form.reset();
    }
});