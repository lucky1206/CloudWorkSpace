/**
 * Created by winnerlbm on 2019/6/17.
 */
Ext.define('acsweb.view.top.Top', {
    extend: 'Ext.panel.Panel',

    requires: [
        'acsweb.view.top.TopModel',
        'acsweb.view.top.TopController'
    ],

    /*
    Uncomment to give this component an xtype*/
    xtype: 'top',


    viewModel: {
        type: 'top'
    },

    controller: 'top',

    ui: 'banner-panel-bgcolor',
    layout: {
        type: 'hbox',
        pack: 'start',
        align: 'center'
    },
    defaults: {
        border: false
    },
    items: [
        /* include child components here */
        //系统logo
        {
            xtype: 'container',
            html: '<i class="cloud icon-codegen app-logo-cls"></i>',
            margin: '40 10 0 10'
        },
        //系统title
        {
            xtype: 'label',
            margin: '0 10 0 0',
            cls: 'app-title-cls',
            text: conf.title
        },
        {
            flex: 1
        },
        {
            xtype: 'segmentedbutton',
            id: 'sysMenuId',
            reference: 'sysMenuRef',
            vertical: false,
            allowMultiple: false,
            forceSelection: false,
            defaults: {
                scale: 'medium',
                border: false,
                xtype: 'button',
                ui: 'menu-button-ui',
                flex: 1,
                iconAlign: 'top',
                textAlign: 'center',
                margin: '0 0 0 0'
            },
            items: conf.menus
        }
    ]
});