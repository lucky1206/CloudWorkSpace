/**
 * Created by winnerlbm on 2019/7/23.
 */
Ext.define('acsweb.view.satellitex.SatelliteXController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.satellitex',

    requires: [
        'Ext.data.Store',
        'Ext.data.TreeStore',
        'Ext.grid.Panel',
        'Ext.grid.feature.Grouping',
        'Ext.tree.Panel',
        'Ext.util.TaskManager',
        'acsweb.util.CustomWindow'
    ],

    //tle数据管理面板
    tlesManagerPanel: null,
    //卫星模型实例
    sateModelInstance: null,
    //卫星足迹圆实例
    sateFootInstance: null,
    //自动追踪任务
    autoTraceTask: null,

    /**
     * Called when the view is created
     */
    init: function () {

    },
    sateAfterrender: function () {
        this.initEnvironment();
    },
    initEnvironment: function () {
        let params = {}, me = this;

        function successCallBack(response, opts) {
            let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
            if (result) {
                me.loadTleGroupList();
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
        }

        ajax.fn.execute(params, 'GET', conf.serviceUrl + 'satellite/initenvironment', successCallBack, failureCallBack);
    },
    afterrenderHandler: function () {
        globeUtil.initGlobe(conf.globe.containerId);
        globeUtil.createBaseLayerPicker4TDT(globeUtil.isShowBaseLayerLabel);

        //加载省界
        this.loadGeoJsonEx(conf.globe.instance, 'resources/data/geojson/province.json');

        //加载中国行政省界
        //globeUtil.layer.loadVectorTileMapServiceEx(conf.globe.instance, conf.geoMapRootUrl, 'province-boundary-ds', conf.regionLayerName, '#F50057'.colorRgba(), conf.chinaBoundRectangle, conf.tilingScheme, null, 0, 17);

        //加载省级GeoStore
        this.getChinaProvinceData('resources/data/geojson/province.json');

        //启用对象捕捉
        this.pickEntityEx(conf.globe.instance);
    },
    //获取中国省界面并创建空间对象索引树
    getChinaProvinceData: function (dataUrl) {
        let params = {};

        function successCallBack(response, opts) {
            let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
            if (result && result['features'] && result['features'].length > 0) {
                let fLen = result['features'].length;
                for (let i = 0; i < fLen; i++) {
                    result['features'][i]['id'] = guidUtil.guid(2);
                }
                let province = result;
                //创建GeoStore
                globeUtil.provinceStore = new Terraformer.GeoStore({
                    store: new Terraformer.GeoStore.Memory(),
                    index: new Terraformer.RTree()
                });
                // FeatureCollection放入GeoStore
                globeUtil.provinceStore.add(province, function (err, success) {
                    // callback when all features are added
                });
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
        }

        ajax.fn.execute(params, 'GET', dataUrl, successCallBack, failureCallBack);
    },
    //通过geojson加载中国省级行政区划面
    loadGeoJsonEx: function (viewer, geoJson) {
        let satelliteContainerRef = this.lookupReference('satelliteContainerRef'),
            mask = ajax.fn.showMask(satelliteContainerRef, "场景基础数据加载中，请耐心等待...");
        //直接记载geojson
        viewer.dataSources.add(Cesium.GeoJsonDataSource.load(geoJson, {
            stroke: Cesium.Color.fromCssColorString('#F50057'),
            fill: Cesium.Color.BLUE.withAlpha(0.5),
            strokeWidth: 3/*,
            clampToGround: true*/
        })).then(function (dataSource) {
            viewer.dataSources.add(dataSource);
            //追踪数据
            viewer.flyTo(dataSource);
            ajax.fn.hideMask(mask);
        }).otherwise(function (error) {
            ajax.fn.hideMask(mask);
            window.alert(error);
        });
    },
    pickEntityEx: function (viewer) {
        viewer.screenSpaceEventHandler.setInputAction(function onMouseHandler(movement) {
            let pickedFeature = viewer.scene.pick(movement.position);
            if (!Cesium.defined(pickedFeature)) {
                return;
            }

            if (pickedFeature && typeof (pickedFeature) != "undefined") {
                console.log(pickedFeature.id._id);
            }
        }, Cesium.ScreenSpaceEventType.LEFT_DOWN);
    },
    //cesium窗口自适应
    resizeHandler: function (con, width, height, oldWidth, oldHeight, eOpts) {
        let globeDom = Ext.getDom(conf.globe.containerId);
        if (globeDom) {
            globeDom.style.width = width + 'px';
            globeDom.style.height = height + 'px';
        }
    },
    //显示卫星分组管理面板
    showTlesManager: function () {
        let me = this;
        if (this.tlesManagerPanel == null) {
            let popupContent = Ext.create('widget.satesmanager');
            this.tlesManagerPanel = Ext.create('widget.cwindow', {
                title: '分组管理',
                iconCls: 'cloud icon-satellite',
                closeToolText: '关闭',
                bodyPadding: 1,
                listeners: {
                    hide: function () {
                        me.loadTleGroupList();
                    }
                }
            }).show();
            this.tlesManagerPanel.add(popupContent);
        }
        this.tlesManagerPanel.show();
    },
    //加载卫星分组
    loadTleGroupList: function () {
        let params = {}, satesGroupTreeRef = this.lookupReference('satesGroupTreeRef');

        function successCallBack(response, opts) {
            let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
            if (result && result['data'] && result['data'].length > 0) {
                let sgsStore = new Ext.create('Ext.data.TreeStore', {
                    data: result['data']
                });
                satesGroupTreeRef.setStore(sgsStore);
            } else {
                satesGroupTreeRef.setStore(null);
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
        }

        ajax.fn.execute(params, 'GET', conf.serviceUrl + 'satellite/gettlegrouplist', successCallBack, failureCallBack);
    },
    //选中分组
    ic4TleGroupHandler: function (gp, record, item, index, e, eOpts) {
        //清除已经添加的场景要素
        this.resetState();

        let groupId = record.get('gid'),
            groupName = record.get('text');

        //显示分组tle列表
        this.loadTlesByGroup(groupId, groupName);
    },
    //加载单个分组卫星
    loadTlesByGroup: function (groupId, groupName) {
        let params = {
            groupId: groupId
        }, satesTreeRef = this.lookupReference('satesTreeRef'), me = this;

        function successCallBack(response, opts) {
            let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
            if (result && result['data'] && result['data'].length > 0) {
                let tlesStore = new Ext.create('Ext.data.TreeStore', {
                    data: result['data']
                });
                satesTreeRef.setTitle(groupName + '卫星列表');
                satesTreeRef.setStore(null);
                satesTreeRef.setStore(tlesStore);
            } else {
                satesTreeRef.setTitle('分组卫星');
                satesTreeRef.setStore(null);
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
            satesTreeRef.setTitle('分组卫星列表');
            satesTreeRef.setStore(null);
        }

        ajax.fn.execute(params, 'GET', conf.serviceUrl + 'satellite/gettlesbygroup', successCallBack, failureCallBack);
    },
    //选择单颗卫星
    ic4TleHandler: function (gp, record, item, index, e, eOpts) {
        //清除已经添加的场景要素
        this.resetState();

        //地面基站信息,若不清楚，默认全部传入0
        let groundStation = {
            lon: 0,
            lat: 0,
            h: 0
        };
        let satPos = sateUtil.calc4SatPos(record, groundStation, null);
        //设置过境区域信息
        satPos['regions'] = [];
        this.showSatePos(satPos);

        //this.analysis4SatPos(satPos);

    },
    showSatePos: function (satPos) {
        if (satPos) {
            //创建卫星
            this.sateModelInstance = globeUtil.createModelEx(conf.globe.instance, "resources/data/models/satellite.gltf", satPos);
            //创建足迹圆
            this.sateFootInstance = globeUtil.createRangeCircleEx(conf.globe.instance, satPos);
        }
    },
    //根据tle信息按照行政省预测卫星位置及过境信息等
    /*analysis4SatPos: function (satPos) {
        let me = this;
        let fpc = satPos['fcps'];
        let fpcWkt = '';
        if (fpc != null && fpc.length > 0) {
            let rangelong;
            let rangelat;
            //格式化为wkt
            fpcWkt = "POLYGON ((";
            for (let i = 0; i < fpc.length; i++) {
                let rc = fpc[i], rangelong = rc['lon'],
                    rangelat = rc['lat'];
                fpcWkt += rangelong + " " + rangelat + ",";
            }
            //追加多边形闭合点
            let closePos = fpc[0];
            rangelong = closePos['lon'];
            rangelat = closePos['lat'];
            fpcWkt += rangelong + " " + rangelat;
            fpcWkt += "))";
        }
        let params = {
            fpcs: fpcWkt
        };

        //执行成功回调
        function successCallBack(response, opts) {
            //查询结果转json对象
            let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
            if (result) {
                //设置过境区域信息
                let regions = result['regions'];
                satPos['regions'] = regions;
                me.showSatePos(satPos);
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
        }

        ajax.fn.execute(params, 'POST', conf.serviceUrl + 'satellite/predict4satellite', successCallBack, failureCallBack);
    },*/
    orbitDaily: function (cb, newValue, oldValue, eOpts) {
        //获取当前选中卫星
        let satesGroupTreeRef = this.lookupReference('satesTreeRef');
        let sateSelection = satesGroupTreeRef.getSelection();
        if (sateSelection && sateSelection.length > 0) {
            let currentTle = sateSelection[0];
            if (newValue) {
                //地面基站信息,若不清楚，默认全部传入0
                let groundStation = {
                    lon: 0,
                    lat: 0,
                    h: 0
                };
                //绘制轨迹线,间隔20分钟计算一次
                let satposes = sateUtil.calcSatePos4Time(currentTle, groundStation, null, 1200);
                if (satposes) {
                    //绘制未来24小时星下点轨迹线
                    globeUtil.createTrailEx(conf.globe.instance, currentTle, satposes);
                }
            } else {
                let sateName = currentTle.get('text'),
                    entityId = sateName + "_satetrail_id";
                globeUtil.removeEntityFromViewer(conf.globe.instance, entityId);
            }
        }
    },
    autoTracing: function (cb, newValue, oldValue, eOpts) {
        //获取当前选中卫星
        let me = this;
        let satesGroupTreeRef = this.lookupReference('satesTreeRef');
        let sateSelection = satesGroupTreeRef.getSelection();
        if (sateSelection && sateSelection.length > 0 && this.sateModelInstance) {
            let currentTle = sateSelection[0];
            if (newValue) {
                //地面基站信息,若不清楚，默认全部传入0
                let groundStation = {
                    lon: 0,
                    lat: 0,
                    h: 0
                };
                //按照设定时间隔移动模型位置
                if (this.autoTraceTask == null) {
                    this.autoTraceTask = {
                        run: function () {
                            //计算卫星当前时刻位置信息
                            let satPos = sateUtil.calc4SatPos(currentTle, groundStation, null);
                            //更新卫星位置
                            globeUtil.autoTraceEx(me.sateModelInstance, me.sateFootInstance, satPos);
                        },
                        interval: conf.traceTimeGap //单位毫秒
                    };
                    Ext.TaskManager.start(this.autoTraceTask);
                }
            } else {
                if (this.autoTraceTask) {
                    //销毁时间器
                    Ext.TaskManager.stop(this.autoTraceTask);
                    this.autoTraceTask = null;
                }
            }
        }
    },
    resetState: function () {
        conf.globe.instance.entities.removeAll();
        let orbitDailyRef = this.lookupReference('orbitDailyRef'),
            autoTracingRef = this.lookupReference('autoTracingRef');
        if (orbitDailyRef && autoTracingRef) {
            orbitDailyRef.setValue(false);
            autoTracingRef.setValue(false);
        }
    }
});