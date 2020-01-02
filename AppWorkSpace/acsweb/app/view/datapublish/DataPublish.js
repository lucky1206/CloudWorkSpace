/**
 * Created by winnerlbm on 2019/7/3.
 */
Ext.define('acsweb.view.datapublish.DataPublish', {
    extend: 'Ext.panel.Panel',

    requires: [
        'acsweb.view.datapublish.DataPublishModel',
        'acsweb.view.datapublish.DataPublishController'
    ],

    /*
    Uncomment to give this component an xtype*/
    xtype: 'datapublish',

    viewModel: {
        type: 'datapublish'
    },

    controller: 'datapublish',
    scrollable: false,
    html: '<iframe src="' + conf.mapServerUrl + '" style="width: 100%; height: 100%;border: none;overflow: hidden !important;"></iframe>'
});