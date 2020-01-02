Ext.define('acsweb.util.CustomWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.cwindow',

    requires: [
        'Ext.layout.container.Fit'
    ],

    layout: 'fit',
    closeAction: 'hide',//'destroy',
    autoShow: false,
    modal: false,
    constrain: true,
    resizable: false,
    toFrontOnShow: true,
    draggable: true,
    //scrollable: true,
    closeToolText: '关闭',
    onEsc: Ext.emptyFn,

    afterRender: function () {
        let me = this;
        me.callParent(arguments);
        me.syncSize();

        // Since we want to always be a %age of the viewport, we have to watch for
        // resize events.
        Ext.on(me.resizeListeners = {
            resize: me.onViewportResize,
            scope: me,
            buffer: 50
        });
    },

    doDestroy: function () {
        Ext.un(this.resizeListeners);
        this.callParent();
    },

    onViewportResize: function () {
        this.syncSize();
    },

    syncSize: function () {
        let width = Ext.Element.getViewportWidth(),
            height = Ext.Element.getViewportHeight();

        // We use percentage sizes so we'll never overflow the screen (potentially
        // clipping buttons and locking the user in to the dialog).

        //todo 暂时屏蔽这个功能,酌情开启代码，开启会导致设置的width height属性无效
        /*this.setSize(Math.floor(width * 0.8), Math.floor(height * 0.8));
        this.setXY([Math.floor(width * 0.1), Math.floor(height * 0.1)]);*/

        //默认全屏显示面板
        this.setSize(Math.floor(width), Math.floor(height));
        this.setXY([0, 0]);
    }
});
