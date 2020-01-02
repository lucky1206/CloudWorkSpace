/**
 * Created by LBM on 2017/11/16.
 */
Ext.define('acsweb.view.main.MainController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.main',
    listen: {
        controller: {
            '#': {
                unmatchedroute: 'onRouteChange'
            }
        }
    },

    routes: {
        '#': 'onRouteChange'
    },

    lastView: null,

    //设置当前窗口视图
    setCurrentView: function (hashTag) {
        hashTag = (hashTag || '').toLowerCase();
        let me = this,
            refs = me.getReferences(),
            mainCard = refs.moduleContainerWrap,//获取主容器
            mainLayout = mainCard.getLayout(),
            view = hashTag,//hashTag默认值为viewType
            lastView = me.lastView,
            existingItem = mainCard.child('component[routeId=' + hashTag + ']'),
            newView;

        //若有滚动条，则每次切换视图时，滚动条位置置顶
        mainCard.scrollTo(0, 0, true);

        //清除前面已经路由的任何窗口
        if (lastView && lastView.isWindow) {
            lastView.destroy();
        }

        lastView = mainLayout.getActiveItem();

        if (!existingItem) {
            newView = Ext.create({
                xtype: view,
                routeId: hashTag,  // for existingItem search later
                hideMode: 'offsets'
            });
        }

        if (!newView || !newView.isWindow) {
            // !newView means we have an existing view, but if the newView isWindow
            // we don't add it to the card layout.
            if (existingItem) {
                // We don't have a newView, so activate the existing view.
                if (existingItem !== lastView) {
                    mainLayout.setActiveItem(existingItem);
                }
                newView = existingItem;
            } else {
                // newView is set (did not exist already), so add it and make it the
                // activeItem.
                Ext.suspendLayouts();
                mainLayout.setActiveItem(mainCard.add(newView));
                Ext.resumeLayouts(true);
            }
        }

        if (newView.isFocusable(true)) {
            newView.focus();
        }

        me.lastView = newView;
        //禁用浏览器历史回退功能
        window.history.forward(1);

        //整体刷新布局
        mainCard.updateLayout();
    },
    //路由切换
    onRouteChange: function (id) {
        //设置当前视图
        this.setCurrentView(id);
    },
    //主界面渲染入口
    onModuleViewRender: function () {
        //页面刷新加载首页模块
        //this.redirectTo(conf.initModule);

        //页面刷新加载当前模块
        /*if (!window.location.hash) {
        this.redirectTo(conf.initModule);
        }*/
    },
    /**
     * Called when the view is created
     */
    init: function () {

    }
});
