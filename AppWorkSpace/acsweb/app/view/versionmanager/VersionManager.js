/**
 * Created by LBM on 2018/1/5.
 */
Ext.define('acsweb.view.versionmanager.VersionManager', {
    extend: 'Ext.panel.Panel',

    requires: [
        'Ext.button.Button',
        'Ext.container.Container',
        'Ext.form.Panel',
        'Ext.layout.container.HBox',
        'Ext.layout.container.VBox',
        'Ext.panel.Panel',
        'Ext.resizer.Splitter',
        'Ext.toolbar.Fill',
        'Ext.tree.Panel',
        'Ext.ux.statusbar.StatusBar',
        'acsweb.view.versionmanager.VersionManagerController',
        'acsweb.view.versionmanager.VersionManagerModel'
    ],

    /*
    Uncomment to give this component an xtype*/
    xtype: 'versionmanager',


    viewModel: {
        type: 'versionmanager'
    },

    controller: 'versionmanager',
    layout: {
        type: 'hbox',
        pack: 'start',
        align: 'stretch'
    },

    items: [
        /* include child components here */
        {
            xtype: 'treepanel',
            ui: 'project-panel-ui',
            reference: 'projCatalogTreeRef',
            iconCls: 'cloud icon-schema',
            scrollable: 'y',
            title: '工程结构',
            bodyPadding: 5,
            flex: 1,
            rootVisible: false,
            rowLines: true,
            animate: true,
            frame: false,
            reserveScrollbar: true,
            multiSelect: false,
            border: true,
            listeners: {
                afterrender: 'loadProjCatalogTree',
                itemclick: 'itemclickHandler'
            }
        },
        {
            xtype: 'splitter',
            width: 3
        },
        {
            xtype: 'panel',
            reference: 'fileContentRef',
            title: '文件内容预览',
            border: true,
            flex: 1,
            // ui: 'sql-panel-ui',
            ui: 'project-panel-ui',
            iconCls: 'cloud icon-previewer',
            html: '<iframe name="showContentiFrame" src="resources/html/content.html" style="width: 100%; height: 100%;border: none"></iframe>'
        }
    ],
    buttons: [
        '->',
        {
            text: '刷新工程目录',
            iconCls: 'cloud icon-refresh',
            disabled: false,
            formBind: false,
            reference: 'loadProjTreeRef',
            handler: 'loadProjTreeHandler'
        },
        {
            xtype: 'button',
            iconCls: 'cloud icon-schema',
            text: '下载Java工程',
            margin: '0 0 0 10',
            handler: 'oneKey4Download'
        },
        {
            xtype: 'button',
            iconCls: 'cloud icon-java',
            text: '下载API文件',
            margin: '0 0 0 10',
            handler: 'oneKey4Source'
        },
        {
            xtype: 'button',
            iconCls: 'cloud icon-sql',
            text: '下载Mapper文件',
            margin: '0 0 0 10',
            handler: 'oneKey4Mapping'
        },
        {
            xtype: 'button',
            iconCls: 'cloud icon-service',
            text: '下载WEB工程',
            margin: '0 10 0 10',
            handler: 'oneKey4WebProject'
        }
    ]
});