/**
 * Created by LBM on 2018/2/7.
 */
Ext.define('acsweb.controller.AppController', {
    extend: 'Ext.app.Controller',

    config: {
        //Uncomment to add references to view components
        refs: [
            {
                ref: 'topView',
                selector: 'top'
            },
            {
                ref: 'mapregeditView',
                selector: 'mapregedit'
            },
            {
                ref: 'preserviceView',
                selector: 'preservice'
            },
            {
                ref: 'mainView',
                selector: 'main'
            }
        ],
        //Uncomment to listen for events from view components
        control: {
            'top segmentedbutton[reference="sysMenuRef"]': {
                afterrender: function () {
                    let me = this;
                    if (conf.menus) {
                        Ext.each(conf.menus, function (menu) {
                            if (menu['pressed']) {
                                let mv = me.getMainView(), mvc = mv.getController(), tv = me.getTopView(),
                                    tvc = tv.getController();
                                mvc.redirectTo(menu['action']);
                                tvc.highLightMenu(menu['action']);
                                return true;
                            }
                        })
                    }
                },
                toggle: function (segButtonCom, button, isPressed, eOpts) {
                    if (isPressed) {
                        let action = button['action'];
                        let mv = this.getMainView(), mvc = mv.getController(), tv = this.getTopView(),
                            tvc = tv.getController();
                        mvc.redirectTo(action);
                        tvc.highLightMenu(action);
                    }
                }
            },
            'mapregedit treepanel[reference="svrCatalogTreeRef"]': {
                itemclick: function (tp, record, item, index, e, eOpts) {
                    let mrv = this.getMapregeditView(), mrvc = mrv.getController();
                    let isGroup = record.get('isGroup');
                    mrvc.switchGroupButtons(isGroup);

                    //选择服务节点则显示服务信息
                    if (!isGroup) {
                        mrvc.showServiceInfo(record);
                    } else {
                        mrvc.resetService();
                    }

                    let svrUrl = record.get('svrUrl');
                    if (svrUrl) {
                        let pv = this.getPreserviceView(), pvc = pv.getController();
                        let west = record.get('west'), south = record.get('south'), east = record.get('east'),
                            north = record.get('north'), svrType = record.get('svrType'),
                            svrLayerName = record.get('svrLayerName'), svrSrid = record.get('svrSrid');
                        let svrBounds = null;
                        if (west > 0 && south > 0 && east > 0 && north > 0) {
                            svrBounds = [
                                [south, west],
                                [north, east]
                            ];
                        }
                        pvc.loadVectorLayer(svrLayerName, svrUrl, svrType, svrBounds, svrSrid);
                    }
                }
            }
        }
    },

    /**
     * Called when the view is created
     */
    init: function () {

    }
});