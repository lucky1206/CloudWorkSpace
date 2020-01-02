/**
 * Created by winnerlbm on 2019/7/29.
 */
Ext.define('acsweb.view.satellitex.satesmanager.SatesManagerController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.satesmanager',

    requires: [
        'Ext.data.Store',
        'Ext.data.TreeStore'
    ],

    /**
     * Called when the view is created
     */
    init: function () {

    },
    afterrenderHandler: function () {
        //默认加载全部卫星分组
        this.loadTleGroupList();

        //默认加载全部卫星
        this.loadTleByName('');
    },
    loadTleByName: function (tleName) {
        let params = {
            tleName: tleName
        }, allSateGridRef = this.lookupReference('allSateGridRef');

        allSateGridRef.setStore(null);

        function successCallBack(response, opts) {
            let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
            if (result && result['data'] && result['data'].length > 0) {
                let tlesStore = new Ext.create('Ext.data.Store', {
                    data: result['data']
                });
                allSateGridRef.setStore(tlesStore);
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
            allSateGridRef.setStore(null);
        }

        ajax.fn.execute(params, 'GET', conf.serviceUrl + 'satellite/gettlesbyname', successCallBack, failureCallBack);
    },
    //关键字查询
    queryTlesByNameHandler: function () {
        let keywordsRef = this.lookupReference('keywordsRef'), keyword = keywordsRef.getValue();
        this.loadTleByName(keyword);
    },
    //加载卫星分组
    loadTleGroupList: function () {
        let params = {}, satesGroupTreeRef = this.lookupReference('satesGroupTreeRef');
        satesGroupTreeRef.setStore(null);

        function successCallBack(response, opts) {
            let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
            if (result && result['data'] && result['data'].length > 0) {
                let sgsStore = new Ext.create('Ext.data.TreeStore', {
                    data: result['data']
                });
                satesGroupTreeRef.setStore(sgsStore);
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
            satesGroupTreeRef.setStore(null);
        }

        ajax.fn.execute(params, 'GET', conf.serviceUrl + 'satellite/gettlegrouplist', successCallBack, failureCallBack);
    },
    sateGroupIcHandler: function (gp, record, item, index, e, eOpts) {
        if (record) {
            let sateGroupInfoFormRef = this.lookupReference('sateGroupInfoFormRef'),
                form = sateGroupInfoFormRef.getForm(), groupId = record.get('gid');

            //显示信息
            form.findField('text').setValue(record.get('text'));
            form.findField('guser').setValue(record.get('guser'));
            form.findField('gdesc').setValue(record.get('gdesc'));

            //请求组中卫星列表
            this.loadTlesByGroup(groupId);
        }
    },
    //加载单个分组卫星
    loadTlesByGroup: function (groupId) {
        let params = {
            groupId: groupId
        }, selectedSateGridRef = this.lookupReference('selectedSateGridRef');

        selectedSateGridRef.setStore(null);

        function successCallBack(response, opts) {
            let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
            if (result && result['data'] && result['data'].length > 0) {
                let tlesStore = new Ext.create('Ext.data.Store', {
                    data: result['data']
                });
                selectedSateGridRef.setStore(tlesStore);
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
            selectedSateGridRef.setStore(null);
        }

        ajax.fn.execute(params, 'GET', conf.serviceUrl + 'satellite/gettlesbygroup', successCallBack, failureCallBack);
    },
    //操作组
    onRemoveGroupHandler: function () {
        let me = this;
        //询问是否删除
        Ext.Msg.show({
            title: '温馨提示',
            closeToolText: '关闭',
            buttonText: {
                ok: '确定',
                cancel: '取消'
            },
            message: '是否删除当前分组?',
            buttons: Ext.Msg.OKCANCEL,
            icon: Ext.Msg.QUESTION,
            fn: function (btn) {
                if (btn === 'ok') {
                    //执行删除
                    me.executeRemoveGroup();
                }
            }
        });
    },
    //执行删除
    executeRemoveGroup: function () {
        let satesGroupTreeRef = this.lookupReference('satesGroupTreeRef'), me = this;
        let sateSelection = satesGroupTreeRef.getSelection();
        if (sateSelection && sateSelection.length > 0) {
            let sateGroup = sateSelection[0];
            let groupId = sateGroup.get('gid');
            let params = {
                groupId: groupId
            };

            function successCallBack(response, opts) {
                let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
                if (result['state'] === 1) {
                    //若增加成功则刷新分组列表
                    me.loadTleGroupList();
                }
            }

            //执行失败回调
            function failureCallBack(response, opts) {
            }

            ajax.fn.execute(params, 'POST', conf.serviceUrl + 'satellite/deletesategroup', successCallBack, failureCallBack);
        }
    },
    onEditGroupHandler: function () {
        let satesGroupTreeRef = this.lookupReference('satesGroupTreeRef'), me = this;
            let sateSelection = satesGroupTreeRef.getSelection();
            if (sateSelection && sateSelection.length > 0) {
            let sateGroup = sateSelection[0];
            let groupId = sateGroup.get('gid');//group id 不能变更
            let sateGroupInfoFormRef = this.lookupReference('sateGroupInfoFormRef'),
                form = sateGroupInfoFormRef.getForm(), groupName = form.findField('text').getValue(),
                groupUser = form.findField('guser').getValue(),
                groupDesc = form.findField('gdesc').getValue();

            //获取已选定的卫星列表
            let selectedSateGridRef = this.lookupReference('selectedSateGridRef'),
                selectedSateStore = selectedSateGridRef.getStore();
            if (groupName && selectedSateStore && selectedSateStore.getCount() > 0) {
                //至少需要一颗卫星被选定才能开始新建
                let sateGroupRefs = [], sates = selectedSateStore.getData();
                for (let i = 0; i < sates.length; i++) {
                    sate = sates.getAt(i);
                    sateGroupRefs.push(sate.get('tid'))
                }

                //判断是否存在节点与编辑之后的组同名
                let findRecord = satesGroupTreeRef.getStore().findRecord('text', groupName);
                if (findRecord && findRecord.get('gid') !== groupId) {
                    Ext.Msg.alert('提示', '已存在同名分组，请重新输入。');
                    return;
                }

                let params = {
                    groupId: groupId,
                    groupName: groupName,
                    groupUser: groupUser,
                    groupUserId: '',
                    groupDesc: groupDesc,
                    groupTles: sateGroupRefs.toString()
                };

                function successCallBack(response, opts) {
                    let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
                    if (result['state'] === 1) {
                        //若增加成功则刷新分组列表
                        me.loadTleGroupList();
                    }
                }

                //执行失败回调
                function failureCallBack(response, opts) {
                }

                ajax.fn.execute(params, 'POST', conf.serviceUrl + 'satellite/editsategroup', successCallBack, failureCallBack);
            } else {
                Ext.Msg.alert('提示', '请输入分组名并至少选择一颗卫星。');
            }
        }
    },
    onAddGroupHandler: function () {
        let me = this;
        //Group ID和时间日期由服务端自动生成
        let sateGroupInfoFormRef = this.lookupReference('sateGroupInfoFormRef'),
            form = sateGroupInfoFormRef.getForm(), groupName = form.findField('text').getValue(),
            groupUser = form.findField('guser').getValue(),
            groupDesc = form.findField('gdesc').getValue();
        //获取已选定的卫星列表
        let selectedSateGridRef = this.lookupReference('selectedSateGridRef'),
            selectedSateStore = selectedSateGridRef.getStore();
        if (groupName && selectedSateStore && selectedSateStore.getCount() > 0) {
            //至少需要一颗卫星被选定才能开始新建
            let sateGroupRefs = [], sates = selectedSateStore.getData();
            for (let i = 0; i < sates.length; i++) {
                sate = sates.getAt(i);
                sateGroupRefs.push(sate.get('tid'))
            }
            //派发请求，首先判断是否已经存同名的卫星分组
            let satesGroupTreeRef = this.lookupReference('satesGroupTreeRef');
            if (satesGroupTreeRef.getStore().findRecord('text', groupName) == null) {
                let params = {
                    groupName: groupName,
                    groupUser: groupUser,
                    groupUserId: '',
                    groupDesc: groupDesc,
                    groupTles: sateGroupRefs.toString()
                };

                function successCallBack(response, opts) {
                    let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
                    if (result['state'] === 1) {
                        //若增加成功则刷新分组列表
                        me.loadTleGroupList();
                    }
                }

                //执行失败回调
                function failureCallBack(response, opts) {
                }

                ajax.fn.execute(params, 'POST', conf.serviceUrl + 'satellite/addsategroup', successCallBack, failureCallBack);
            } else {
                Ext.Msg.alert('提示', '已存在同名分组，请重新输入。');
            }
        } else {
            Ext.Msg.alert('提示', '请输入分组名并至少选择一颗卫星。');
        }
    },
    //操作卫星
    onRemoveFromGroup: function (grid, rowIndex, colIndex) {
        let store = grid.getStore();
        store.removeAt(rowIndex);
    },
    onAddToGroup: function (grid, rowIndex, colIndex) {
        let store = grid.getStore(), rec = store.getAt(rowIndex), tid = rec.get('tid');
        let selectedSateGridRef = this.lookupReference('selectedSateGridRef'),
            tleDataItem = {
                checked: false,
                iconCls: 'cloud icon-sate',
                leaf: true,
                td: rec.get('td'),
                text: rec.get('text'),
                tfr: rec.get('tfr'),
                tsr: rec.get('tsr'),
                tid: tid
            },
            selectedStore = selectedSateGridRef.getStore();
        if (selectedStore.getCount() === 0) {
            let tlesStore = new Ext.create('Ext.data.Store', {
                data: [tleDataItem]
            });
            selectedSateGridRef.setStore(tlesStore);
        } else {
            if (selectedStore.findRecord('tid', tid) == null) {
                selectedStore.insert(0, tleDataItem);
            } else {
                Ext.Msg.alert('提示', '卫星已添加。');
            }
        }
    }
});