Ext.define('acsweb.util.WrapUtil', {
    extend: 'Ext.container.Container',
    xtype: 'warputil',

    requires: [
        'Ext.layout.container.HBox'
    ],
    scrollable: true,
    layout: {
        type: 'hbox',
        align: 'stretchmax',
        animate: true,
        animatePolicy: {
            x: true,
            width: true
        }
    },
    beforeLayout: function () {
        this.height = Ext.Element.getViewportHeight();
        this.callParent(arguments);
    }
});
