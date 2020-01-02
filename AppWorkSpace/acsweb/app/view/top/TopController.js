/**
 * Created by LBM on 2017/11/16.
 */
Ext.define('acsweb.view.top.TopController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.top',

    requires: [
        'Ext.button.Button'
    ],

    /**
     * Called when the view is created
     */
    init: function () {
        this.initSystemMenu();
    },
    //根据配置文件初始化系统主菜单项目
    initSystemMenu: function () {
        let sysMenuCmp = this.getView().down("segmentedbutton");
        let menus = this.getViewModel().data.menu;
        if (menus && menus.length > 0) {
            let sysMenuItems = [];
            //解析系统菜单配置参数
            Ext.each(menus, function (rec) {
                if (rec != null) {
                    let menuItem = Ext.create('Ext.button.Button', {
                        id: rec['key'],
                        text: rec['name'],
                        value: rec['url'],
                        pressed: rec['selected'],
                        hidden: rec['hide'],
                        iconCls: rec['icon']
                    });
                    menuItem['widgetType'] = rec['type'];
                    menuItem['init'] = rec['init'];
                    menuItem['mode'] = rec['mode'];
                    menuItem['widgetId'] = rec['widgetId'];
                    menuItem['floatContainerParams'] = rec['floatContainerParams'];
                    sysMenuItems.push(menuItem);
                }
            });

            if (sysMenuCmp) {
                sysMenuCmp.removeAll();
                sysMenuCmp.add(sysMenuItems);
            }
        }
    },
    highLightMenu: function (action) {
        let sysMenu = Ext.getCmp('sysMenuId'), me = this;
        if (sysMenu && sysMenu.items.items && sysMenu.items.items.length > 0) {
            let menus = sysMenu.items.items;
            Ext.each(menus, function (menu) {
                if (menu['action'] === action) {
                    let iconCls = menu['iconCls'];
                    menu.setIconCls(iconCls.replace('app-menu-cls', 'app-menu-high-cls'));
                    if (me.currentMenu) {
                        let iconCls = me.currentMenu['iconCls'];
                        me.currentMenu.setIconCls(iconCls.replace('app-menu-high-cls', 'app-menu-cls'));
                    }
                    me.currentMenu = menu;
                    return true;
                }
            })
        }
    }
});