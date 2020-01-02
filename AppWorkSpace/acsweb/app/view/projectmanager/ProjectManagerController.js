/**
 * Created by LBM on 2017/12/18.
 */
Ext.define('acsweb.view.projectmanager.ProjectManagerController', {
        extend: 'Ext.app.ViewController',
        alias: 'controller.projectmanager',

        requires: [
            'Ext.data.Store'
        ],

        /**
         * Called when the view is created
         */
        init: function () {

        },
        afterrenderHandler: function () {
            //数据库配置
            this.onLoadDataBaseConfigList();

            //工程配置
            this.onLoadProjectConfigList();
        },
        //------数据库配置-------------------------------------------
        onLoadDataBaseConfigList: function () {
            let me = this;
            let dbcForm = me.lookupReference('dbInfoFormRef');
            let mask = ajax.fn.showMask(dbcForm, '数据库配置加载中...');
            let params = {};

            //执行成功回调
            function successCallBack(response, opts) {
                //查询结果转json对象
                let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                let dbs = result['dbs'];
                if (dbs && dbs.length > 0) {
                    let dbsStore = new Ext.create('Ext.data.Store', {
                        data: dbs
                    });
                    me.lookupReference('databaseConfigListRef').setStore(dbsStore);
                }

                ajax.fn.hideMask(mask);
            }

            //执行失败回调
            function failureCallBack(response, opts) {
                ajax.fn.hideMask(mask);
            }

            ajax.fn.execute(params, 'POST', conf.serviceUrl + 'coding/getdatabase', successCallBack, failureCallBack);
        },

        selectDBciHandler: function (combo, record, eOpts) {
            if (record) {
                //1、获取数据库配置信息并填充配置信息
                let dbc = record.getData();
                let formPanel = this.lookupReference('dbInfoFormRef'),
                    form = formPanel.getForm();
                let formData = form.getValues();
                for (let key in formData) {
                    form.findField(key).setValue(dbc[key]);
                }
            }
        },
        //------工程配置---------------------------------------------
        onLoadProjectConfigList: function () {
            let me = this;
            let pcrGrid = me.lookupReference('projectConfigRef');
            let mask = ajax.fn.showMask(pcrGrid, '工程配置加载中...');
            let params = {};

            function successCallBack(response, opts) {
                //查询结果转json对象
                let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                let pps = result['pps'];
                if (pps && pps.length > 0) {
                    let ppsStore = new Ext.create('Ext.data.Store', {
                        data: pps
                    });
                    me.lookupReference('projectConfigListRef').setStore(ppsStore);
                }

                ajax.fn.hideMask(mask);
            }

            function failureCallBack(response, opts) {
                ajax.fn.hideMask(mask);
            }

            ajax.fn.execute(params, 'POST', conf.serviceUrl + 'coding/getprojects', successCallBack, failureCallBack);
        },
        //加载已有工程方案
        onLoadProject: function () {
            this.onLoadProjectConfigList();
        },
        selectPciHandler: function (combo, record, eOpts) {
            if (record) {
                //1、获取工程配置信息
                let pc = Ext.JSON.decode(decodeURIComponent(record.get('projectconfig')), true);
                conf.projectCatalogData = pc;
                conf.projectConfig = pc['pci'];
                //2、填充配置信息
                let formPanel = this.lookupReference('projectConfigRef'),
                    form = formPanel.getForm();
                let formData = form.getValues();
                for (let key in formData) {
                    form.findField(key).setValue(pc['pci'][key]);
                }
            }
        },
        onDeleteProject: function () {
            let me = this;
            Ext.Msg.confirm('温馨提示', '是否删除当前工程?',
                function (choice) {
                    if (choice === 'yes') {
                        let pmi = Ext.get('projectMakeInfoId');
                        pmi.setHtml('');
                        let formPanel = me.lookupReference('projectConfigRef'),
                            form = formPanel.getForm();
                        //获取选中的工程方案名称
                        let projectName = form.findField('projectName').getValue();
                        let mask = ajax.fn.showMask(formPanel, '配置删除中...');
                        let params = {projectName: projectName};

                        function successCallBack(response, opts) {
                            //清空配置信息
                            me.onPCReset();

                            //刷新工程配置列表
                            me.onLoadProjectConfigList();

                            //查询结果转json对象
                            let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                            if (result && result['success']) {
                                pmi.setHtml(result['message']);
                            } else {
                                pmi.setHtml(result['message']);
                            }

                            ajax.fn.hideMask(mask);
                        }

                        function failureCallBack(response, opts) {
                            pmi.setHtml('配置删除失败!');
                            ajax.fn.hideMask(mask);
                        }

                        ajax.fn.execute(params, 'GET', conf.serviceUrl + 'coding/deleteproject', successCallBack, failureCallBack);
                    }
                }
            );
        },
        //工程制作
        onMakeProject: function () {
            let me = this;
            Ext.Msg.confirm('温馨提示', '若工程已创建则覆盖重建，是否创建工程?',
                function (choice) {
                    if (choice === 'yes') {
                        let pmi = Ext.get('projectMakeInfoId');
                        pmi.setHtml('');
                        if (conf.dbConfig) {
                            let formPanel = me.lookupReference('projectConfigRef'),
                                form = formPanel.getForm();

                            let mask = ajax.fn.showMask(formPanel, '工程制作中...');

                            form.submit({
                                clientValidation: true,
                                url: conf.serviceUrl + 'coding/makeproject',
                                method: "POST",
                                params: {
                                    dbConfig: Ext.JSON.encode(conf.dbConfig)//便于测试，直接转换为json字符串，该参数为追加内容，与form表单数据合并提交，后台springmvc接收字段为dbConfig。
                                },
                                success: function (form, action) {
                                    pmi.setHtml('恭喜，工程创建成功！');

                                    ajax.fn.hideMask(mask);

                                    //启用只读
                                    //me.enableFormReadOnly(formPanel, true);

                                    //全局存储工程配置
                                    conf.projectConfig = me.getFormData(formPanel);

                                    //获取根据配置生成工程的目录结构
                                    let result = Ext.JSON.decode(action.response.responseText, true);
                                    conf.projectCatalogData = result['pps'];
                                },
                                failure: function (form, action) {
                                    pmi.setHtml('糟糕，工程创建失败！');

                                    ajax.fn.hideMask(mask);

                                    //关闭只读
                                    //me.enableFormReadOnly(formPanel, false);

                                    conf.projectConfig = null;
                                }
                            });
                        } else {
                            pmi.setHtml('请先正确配置数据库！');
                        }
                    }
                });
        },
        onPCReset: function () {
            let pmi = Ext.get('projectMakeInfoId');
            pmi.setHtml('');

            let formPanel = this.lookupReference('projectConfigRef'),
                form = formPanel.getForm();
            form.reset();

            //关闭只读
            //this.enableFormReadOnly(formPanel, false);

            conf.projectConfig = null;
        },

        //----数据库配置-----------------------------------------------
        onDBCDelete: function () {
            let me = this;
            Ext.Msg.confirm('温馨提示', '是否删除当前数据库?',
                function (choice) {
                    if (choice === 'yes') {
                        let cl = Ext.get('connectInfoId');
                        cl.setHtml('');

                        //获取当前选中的数据库配置
                        let dbList = me.lookupReference('databaseConfigListRef');
                        if (dbList) {
                            //获取数据库名
                            let dbName = dbList.getValue();
                            if (dbName) {
                                let formPanel = me.lookupReference('dbInfoFormRef'),
                                    mask = ajax.fn.showMask(formPanel, '数据库删除中...');
                                let params = {dbName: dbName};

                                function successCallBack(response, opts) {
                                    //查询结果转json对象
                                    let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                                    if (result && result['success']) {
                                        //清空配置信息
                                        let form = formPanel.getForm();
                                        form.reset();
                                        //关闭只读
                                        me.enableFormReadOnly(formPanel, false);
                                        conf.dbConfig = null;
                                        //数据库配置
                                        me.onLoadDataBaseConfigList();
                                        cl.setHtml(result['message']);
                                    } else {
                                        cl.setHtml(result['message']);
                                    }

                                    ajax.fn.hideMask(mask);
                                }

                                function failureCallBack(response, opts) {
                                    cl.setHtml('数据库配置删除失败!');
                                    ajax.fn.hideMask(mask);
                                }

                                ajax.fn.execute(params, 'GET', conf.serviceUrl + 'coding/deletedatabase', successCallBack, failureCallBack);
                            }
                        }
                    }
                }
            );
        },

        onDBCReset: function () {
            let cl = Ext.get('connectInfoId');
            cl.setHtml('');

            let formPanel = this.lookupReference('dbInfoFormRef'),
                form = formPanel.getForm();
            form.reset();

            //关闭只读
            this.enableFormReadOnly(formPanel, false);

            conf.dbConfig = null;

            //数据库配置
            this.onLoadDataBaseConfigList();
        },
        onDBTest: function () {
            let me = this;
            let cl = Ext.get('connectInfoId');
            cl.setHtml('');

            let formPanel = this.lookupReference('dbInfoFormRef'),
                form = formPanel.getForm();

            let mask = ajax.fn.showMask(formPanel, '连接测试中...');

            form.submit({
                clientValidation: true,
                url: conf.serviceUrl + 'coding/testdb',
                method: "POST",
                success: function () {
                    cl.setHtml('恭喜，连接成功！');

                    ajax.fn.hideMask(mask);

                    //启用只读
                    me.enableFormReadOnly(formPanel, true);

                    //全局存储数据库配置
                    conf.dbConfig = me.getFormData(formPanel);
                },
                failure: function () {
                    cl.setHtml('糟糕，连接失败！');

                    ajax.fn.hideMask(mask);

                    //关闭只读
                    me.enableFormReadOnly(formPanel, false);

                    conf.dbConfig = null;
                }
            });
        },
        //是否启用表单只读
        enableFormReadOnly: function (form, isReadOnly) {
            let fts = form.items.items;
            Ext.each(fts, function (ft) {
                if (ft.xtype == 'textfield' || ft.xtype == 'combobox') {
                    ft.setReadOnly(isReadOnly);
                }
            });
        },
        //获取表单数据
        getFormData: function (form) {
            let formData = form.getForm().getValues();
            return formData;
        }
    }
);