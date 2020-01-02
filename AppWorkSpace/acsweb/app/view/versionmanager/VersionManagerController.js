/**
 * Created by LBM on 2018/1/5.
 */
Ext.define('acsweb.view.versionmanager.VersionManagerController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.versionmanager',

    requires: [
        'Ext.data.TreeStore'
    ],

    /**
     * Called when the view is created
     */
    init: function () {

    },
    oneKey4Download: function () {
        //文件下载--当前窗口
        /*window.location.href = conf.serviceUrl + 'version/download?projectName=' + conf.projectConfig['projectName'];*/
        //文件下载--新窗口
        window.open(conf.serviceUrl + 'version/download?projectName=' + conf.projectConfig['projectName']);
    },
    oneKey4Source: function () {
        //文件下载--当前窗口
        /*window.location.href = conf.serviceUrl + 'version/download?projectName=' + conf.projectConfig['projectName'];*/
        //文件下载--新窗口
        window.open(conf.serviceUrl + 'version/source?projectName=' + conf.projectConfig['projectName']);
    },
    oneKey4Mapping: function () {
        //文件下载--当前窗口
        /*window.location.href = conf.serviceUrl + 'version/download?projectName=' + conf.projectConfig['projectName'];*/
        //文件下载--新窗口
        window.open(conf.serviceUrl + 'version/mapping?projectName=' + conf.projectConfig['projectName']);
    },
    oneKey4WebProject: function () {
        //文件下载--新窗口
        window.open(conf.serviceUrl + 'version/webproject?projectName=' + conf.projectConfig['projectName']);
    },
    loadProjTreeHandler: function () {
        showContentiFrame.window.writeFileContent('', '');
        this.loadProjCatalogTree();
    },
    itemclickHandler: function (gp, record, item, index, e, eOpts) {
        let name = record.get('text'), type = record.get('type'), path = record.get('path'),
            fileContentPanel = this.lookupReference('fileContentRef');
        if (['properties', 'xml', 'java', 'html', 'jsp'].indexOf(type) > -1) {
            fileContentPanel.setTitle(name);

            let params = {
                path: path
            };
            let mask = ajax.fn.showMask(fileContentPanel, '内容加载中...');

            //执行成功回调
            function successCallBack(response, opts) {
                //查询结果转json对象
                let result = Ext.JSON.decode((response.responseText), false);
                let content = result['data'];
                if (content) {
                    showContentiFrame.window.writeFileContent(content, type);
                }

                ajax.fn.hideMask(mask);
            }

            //执行失败回调
            function failureCallBack(response, opts) {
                ajax.fn.hideMask(mask);
            }

            ajax.fn.execute(params, 'GET', conf.serviceUrl + 'version/getfilecontent', successCallBack, failureCallBack);
        } else {
            fileContentPanel.setTitle('文件内容预览');
        }
    },
    //加载工程目录树
    loadProjCatalogTree: function () {
        if (conf.projectConfig && conf.dbTables && conf.projectCatalogData) {
            let me = this;
            let pctRef = me.lookupReference('projCatalogTreeRef');
            let mask = ajax.fn.showMask(pctRef, '目录树加载中...');
            let params = {projectName: conf.projectConfig['projectName']};

            function successCallBack(response, opts) {
                //查询结果转json对象
                let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                if (result) {
                    let tbsStore = new Ext.create('Ext.data.TreeStore', {
                        expanded: true,
                        root: result
                    });
                    pctRef.rootVisible = true;
                    pctRef.setStore(tbsStore);
                }

                //pctRef.expandAll();

                ajax.fn.hideMask(mask);
            }

            function failureCallBack(response, opts) {
                ajax.fn.hideMask(mask);
            }

            ajax.fn.execute(params, 'GET', conf.serviceUrl + 'coding/getprojecttree', successCallBack, failureCallBack);
        }
    },
    commitProject: function () {
        let cp = Ext.getCmp('commitProject');
        let ls = Ext.getCmp('commit-status');

        if (conf.projectConfig) {
            cp.setDisabled(true);
            ls.showBusy();

            let params = {projectName: conf.projectConfig['projectName']};

            function successCallBack(response, opts) {
                cp.setDisabled(false);
                ls.clearStatus();
            }

            function failureCallBack(response, opts) {
                cp.setDisabled(false);
                ls.clearStatus();
            }

            ajax.fn.execute(params, 'GET', conf.serviceUrl + 'coding/getprojecttree', successCallBack, failureCallBack);
        }
    }
});