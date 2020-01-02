/**
 * Created by winnerlbm on 2018/7/17.
 */
Ext.define('acsweb.util.LayoutUtil', {
    requires: [
        'Ext.util.HashMap'
    ],

    //面板属性viewState可选值-start-------------
    VIEW_STATE_MIN: 'view-state-min',//面板最小化状态
    VIEW_STATE_MAX: 'view-state-max',//面板最大化状态
    //----------------end--------------------
    containerHash: new Ext.util.HashMap(),
    curView: 'currentView',  //当前视窗widgetType数组
    fixView: 'fixedView', //固定不参与视角切换的视窗widgetType数组
    dockBarPanel: null,//系统停靠面板实例，非强制，如需要添加dock:true,则必须创建相应的停靠栏
    /**
     * 创建视图
     * @param layoutConfigs
     */
    layoutParser: function (layoutConfigs) {
        let me = this, p = [];
        Ext.each(layoutConfigs.panels, function (cfg) {
            let parentContainer = Ext.getDom(layoutConfigs.parentId);
            if (me.containerHash.get(cfg.widgetType) == null) {
                //存储当前面板组件类型，用于自动刷新定位、冲突处理等。
                p.push(cfg.widgetType);
                //存储已创建的视图实例
                me.containerHash.add(cfg.widgetType, new Ext.create('widget.' + cfg.widgetType, {
                    renderTo: parentContainer,
                    bodyPadding: 0,
                    title: cfg.title,
                    iconCls: cfg.iconCls,
                    //面板显隐控制
                    tools: (function () {
                        if (cfg.dock) {
                            return [{
                                iconCls: 'epcp icon-min',
                                tooltip: '最小化',
                                listeners: {
                                    click: function (tool, e, panel) {
                                        //面板直接隐藏
                                        panel.hide();
                                        panel['viewState'] = me.VIEW_STATE_MIN;

                                        //在停靠栏上追加关联按钮
                                        if (me.dockBarPanel) {
                                            me.dockBarPanel.getController().addDockItem(cfg);
                                        }
                                    }
                                }
                            }];
                        } else return [];
                    })()
                }));

                //默认为最大化状态
                me.containerHash.get(cfg.widgetType)['viewState'] = me.VIEW_STATE_MAX;
                //面板父容器
                me.containerHash.get(cfg.widgetType)['parentContainerRef'] = parentContainer;
                //面板自身参数
                me.containerHash.get(cfg.widgetType)['selfCfg'] = cfg;
                //存储属性fix=true的视图
                if (cfg.fix) {
                    if (me.containerHash.get(me.fixView) == null) {
                        me.containerHash.add(me.fixView, [cfg.widgetType]);
                    } else {
                        me.containerHash.replace(me.fixView, me.containerHash.get(me.fixView).concat([cfg.widgetType]));
                    }
                }
            } else {
                p.push(cfg.widgetType);
            }

            //面板默认显隐状态
            if (!cfg.show) {
                me.containerHash.get(cfg.widgetType).hide();
            } else {
                me.containerHash.get(cfg.widgetType).show();
            }

            me.locateView(parentContainer, me.containerHash.get(cfg.widgetType), cfg);
        });
        this.containerHash.add(layoutConfigs.cfgId, p);

        //存储当前视图数组
        if (this.containerHash.get(this.curView) == null) {
            this.containerHash.add(this.curView, p);
        } else {
            this.containerHash.replace(this.curView, p);
        }
    },
    /**
     * 面板压盖冲突解决
     * @param isShow 隐藏或显示面板
     */
    resolveConflict: function (isShow) {
        let me = this;
        //当前视图
        let currentView = this.containerHash.get(this.curView);
        if (currentView && currentView.length > 0) {
            Ext.each(currentView, function (cv) {
                let cvPanel = me.containerHash.get(cv);
                if (cvPanel) {
                    if (cvPanel['selfCfg']['conflict']) {
                        if (isShow) {
                            if (cvPanel['viewState'] === loUtil.VIEW_STATE_MAX) {
                                cvPanel.show();
                                me.locateView(cvPanel['parentContainerRef'], cvPanel, cvPanel['selfCfg']);
                            }
                        } else {
                            cvPanel.hide();
                        }
                    }
                }
            });
        }
        //固定视图
        let fixedView = this.containerHash.get(this.fixView);
        if (fixedView && fixedView.length > 0) {
            Ext.each(fixedView, function (fv) {
                let fvPanel = me.containerHash.get(fv);
                if (fvPanel) {
                    if (fvPanel['selfCfg']['conflict']) {
                        if (isShow) {
                            if (fvPanel['viewState'] === loUtil.VIEW_STATE_MAX) {
                                fvPanel.show();
                                me.locateView(fvPanel['parentContainerRef'], fvPanel, fvPanel['selfCfg']);
                            }
                        } else {
                            fvPanel.hide();
                        }
                    }
                }
            });
        }
    },
    /**
     * 切换视图
     * @param layoutConfigs
     */
    switchViewConfig: function (layoutConfigs) {
        if (layoutConfigs) {
            let me = this, cfgId = layoutConfigs.cfgId, currentView = this.containerHash.get(this.curView);
            if (cfgId) {
                let futureView = this.containerHash.get(cfgId);
                if (currentView == null && futureView == null) {
                    this.layoutParser(layoutConfigs);
                } else if (currentView != null && futureView == null) {
                    Ext.each(currentView, function (cv) {
                        let p = me.containerHash.get(cv), cfg = p.selfCfg;
                        if (cfg.fix == null || !cfg.fix) {
                            p.hide();//todo : 暂时采用模块隐藏机制，酌情考虑释放内存
                        }
                    });
                    this.layoutParser(layoutConfigs);
                } else if (currentView != null && futureView != null) {
                    if (!currentView.equals(futureView)) {
                        Ext.each(currentView, function (cv) {
                            let p = me.containerHash.get(cv), cfg = p.selfCfg;
                            if (cfg.fix == null || !cfg.fix) {
                                p.hide();
                            }
                        });

                        Ext.each(futureView, function (fv) {
                            let p = me.containerHash.get(fv), cfg = p.selfCfg;
                            if (cfg.fix == null || !cfg.fix) {
                                p.show();
                            }
                        });

                        me.containerHash.replace(me.curView, futureView);
                    }
                }

                //初始化停靠面板
                if (this.dockBarPanel) {
                    this.dockBarPanel.getController().switchDockView();
                }
            }
        }
    },
    /**
     * 自动刷新当前视图
     */
    refreshViewConfig: function () {
        let me = this;
        //当前视图
        let cvs = this.containerHash.get(this.curView);
        if (cvs != null && cvs.length > 0) {
            Ext.each(cvs, function (cv) {
                let p = me.containerHash.get(cv), cfg = p.selfCfg,
                    parentContainer = p['parentContainerRef'];
                if (!p.isHidden()) {
                    me.locateView(parentContainer, p, cfg);
                }
            });
        }
        //固定视图
        let fvs = this.containerHash.get(this.fixView);
        if (fvs != null && fvs.length > 0) {
            Ext.each(fvs, function (fv) {
                let p = me.containerHash.get(fv), cfg = p.selfCfg,
                    parentContainer = p['parentContainerRef'];
                if (!p.isHidden()) {
                    me.locateView(parentContainer, p, cfg);
                }
            });
        }
    },
    /**
     * 根据面板参数重新布局------------------------------
     */
    locateView: function (parentContainer, childContainer, floatParams) {
        if (parentContainer && childContainer && floatParams) {
            let w = floatParams['w'];
            let h = floatParams['h'], maxH = floatParams['maxH'], minH = floatParams['minH'];
            //不设置或0则获取实际宽高
            if (w === 0 && childContainer.getWidth(w) !== 2) {
                w = childContainer.getWidth();
            }
            if (h === 0 && childContainer.getHeight(h) !== 2) {
                h = childContainer.getHeight();
            }

            let align = floatParams['align'];
            let offsetX = floatParams['gapX'];
            let offsetY = floatParams['gapY'];
            let bottomOffsetY = floatParams['bottomY'];

            if (w && typeof (w) == 'string' && w.indexOf('%') > -1) {
                w = parentContainer.clientWidth * parseFloat(w.substr(0, w.indexOf('%'))) / 100 - 2 * offsetX;
            }

            if (h && typeof (h) == 'string' && h.indexOf('%') > -1) {
                if (bottomOffsetY == null) {
                    h = parentContainer.clientHeight * parseFloat(h.substr(0, h.indexOf('%'))) / 100 - 2 * offsetY;
                } else {
                    h = parentContainer.clientHeight * parseFloat(h.substr(0, h.indexOf('%'))) / 100 - offsetY - bottomOffsetY;
                }
            }
            if (w !== 0) {
                childContainer.setWidth(w);
            }
            if (h !== 0) {
                if (maxH && maxH < h) {
                    childContainer.setHeight(maxH);
                } else {
                    if (minH && minH > h) {
                        childContainer.setHeight(minH);
                    } else {
                        childContainer.setHeight(h);
                    }
                }
            }


            switch (align) {
                case 'tl': {
                    childContainer.el.alignTo(parentContainer, "tl?", [offsetX, offsetY], false);
                    break;
                }
                case 'bl': {
                    if (bottomOffsetY != null) {
                        offsetY = parentContainer.clientHeight - h - bottomOffsetY;
                    } else {
                        offsetY = parentContainer.clientHeight - h - offsetY;
                    }

                    childContainer.el.alignTo(parentContainer, "tl?", [offsetX, offsetY], false);
                    break;
                }
                case 'tr': {
                    offsetX = parentContainer.clientWidth - offsetX - w;
                    childContainer.el.alignTo(parentContainer, "tl?", [offsetX, offsetY], false);
                    break;
                }
                case 'br': {
                    offsetX = parentContainer.clientWidth - offsetX - w;
                    if (bottomOffsetY != null) {
                        offsetY = parentContainer.clientHeight - h - bottomOffsetY;
                    } else {
                        offsetY = parentContainer.clientHeight - h - offsetY;
                    }
                    childContainer.el.alignTo(parentContainer, "tl?", [offsetX, offsetY], false);
                    break;
                }
            }
        }
    }
});

let loUtil = new acsweb.util.LayoutUtil();
