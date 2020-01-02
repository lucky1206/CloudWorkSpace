/**
 * This class is the main view for the application. It is specified in app.js as the
 * "mainView" property. That setting automatically applies the "viewport"
 * plugin causing this view to become the body element (i.e., the viewport).
 *
 * TODO - Replace this content of this view to suite the needs of your application.
 */
Ext.define('acsweb.view.main.Main', {
    extend: 'Ext.panel.Panel',
    xtype: 'main',

    requires: [
        'Ext.layout.container.Card',
        'Ext.layout.container.VBox',
        'Ext.plugin.Viewport',
        'acsweb.util.WrapUtil',
        'acsweb.view.main.MainController',
        'acsweb.view.main.MainModel',
        'acsweb.view.top.Top'
    ],
    controller: 'main',
    viewModel: 'main',
    layout: {
        type: 'vbox',
        pack: 'start',
        align: 'stretch'
    },
    items: [
        /* include child components here */
        {
            xtype: 'top',
            height: 60
        },
        {
            xtype: 'warputil',
            reference: 'moduleContainerWrap',
            margin: '1 1 1 1',
            flex: 1,
            layout: {
                type: 'card',
                anchor: '100%'
            },
            listeners: {
                render: 'onModuleViewRender'
            }
        }
    ]
});
