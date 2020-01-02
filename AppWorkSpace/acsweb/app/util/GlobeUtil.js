/**
 * Created by winnerlbm on 2019/4/9.
 */
Ext.define('acsweb.util.GlobeUtil', {
    /*//天地图
    let provider = new Cesium.WebMapTileServiceImageryProvider({
    //方案一
    url: 'http://t0.tianditu.com/img_w/wmts?service=wmts&request=GetTile&version=1.0.0&LAYER=img&tileMatrixSet=w&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}&style=default&format=tiles&tk=' + conf.sys.token,
    layer: "tdtImgBasicLayer",
    style: "default",
    tileMatrixSetID: "GoogleMapsCompatible", //tileMatrixSetID: 'w',
    show: true,
    credit: new Cesium.Credit('天地图全球影响'),
    maximumLevel: 18

    //方案二
    url: 'http://t0.tianditu.gov.cn/img_w/wmts?tk=' + conf.sys.token,
    layer:'img',
    style:'default',
    tileMatrixSetID:'w',
    format:'tiles',
    maximumLevel: 18
    });*/

    //监控底图选择面板是否弹出任务
    requires: [
        'Ext.data.Store',
        'Ext.util.HashMap',
        'Ext.util.TaskManager'
    ],

    //省界面 Rtree Store
    provinceStore: null,
    baseLayerMonitorTask: null,//监控任务用于解决面板压盖冲突
    isShowBaseLayerLabel: false,//是否显示底图标注图层

    /**
     * 初始化三维场景
     * @param gDivId 父容器div ID
     * @param isLoadTerrain 是否加载三维地形
     * @param isShowTimePhaseBar 是否显示动画和时间线组件
     */
    initGlobe: function (gDivId) {
        let viewer = new Cesium.Viewer(gDivId, {
            animation: true,  //动画控制
            shouldAnimate: true,
            timeline: true,    //时间线
            fullscreenButton: true, //全屏按钮
            infoBox: true,//要素信息框
            homeButton: true,
            baseLayerPicker: true,
            sceneModePicker: true,
            navigationHelpButton: false,
            geocoder: false,
            scene3DOnly: false
        });
        //版权信息
        viewer._cesiumWidget._creditContainer.style.display = "none";

        //cesium视图实例
        conf.globe.instance = viewer;

        //取消双击事件
        viewer.cesiumWidget.screenSpaceEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_DOUBLE_CLICK);
        //设置homebutton的位置
        Cesium.Camera.DEFAULT_VIEW_RECTANGLE =
            Cesium.Rectangle.fromDegrees(conf.globe.bound.W, conf.globe.bound.S, conf.globe.bound.E, conf.globe.bound.N);
        //设置初始位置
        this.resetView(viewer);

        //根据国情本土化转换
        chineseUtil.convert(viewer);

        //系统操作兼容性处理
        this.sysCompatibility(viewer);

        //默认不显示时间轴
        this.showTimePhaseBar(viewer, false);

        //变更场景默认底色
        viewer.scene.globe.baseColor = Cesium.Color.DARKGREY; //Cesium.Color.fromCssColorString(conf.common.COLOR);//;
        //viewer.scene.globe.depthTestAgainstTerrain = true;

        //启用要素捕获及简要信息跟随显示
        //this.layer.enabledLayerCapture(viewer);
    },
    getFootPrintWKT: function (fpc) {
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
        return fpcWkt;
    },
    //构建infobox内容
    getInfoBoxDesc: function (props) {
        let propsText = '<table class="cesium-infoBox-defaultTable">';
        for (let key in props) {
            if (key !== '过境区域') {
                propsText += '<tr>';
                propsText += '<th>' + key + '</th><td>' + props[key] + '</td>';
                propsText += '</tr>';
            } else {
                let regions = props['过境区域'];
                if (regions && regions.length > 0) {
                    let regionLen = regions.length;
                    for (let i = 0; i < regionLen; i++) {
                        let region = regions[i];
                        if (i === 0) {
                            propsText += '<tr>';
                            propsText += '<th rowspan="' + regionLen + '">' + key + '[' + regionLen + ']' + '</th><td>' + region['PROVINCE'] + '</td>';
                            propsText += '</tr>';
                        } else {
                            propsText += '<tr>';
                            propsText += '<td>' + region['PROVINCE'] + '</td>';
                            propsText += '</tr>';
                        }
                    }

                } else {
                    propsText += '<tr>';
                    propsText += '<th>' + key + '</th><td>' + props[key] + '</td>';
                    propsText += '</tr>';
                }
            }
        }
        propsText += '</table>';
        return propsText;
    },
    autoTraceEx: function (model, fpCircle, sate) {
        if (model && fpCircle) {
            let me = this;
            let sateName = sate['sateName'], longitude = sate['longitude'], latitude = sate['latitude'],
                altitude = sate['altitude'] * 1000, azimuth = sate['azimuth'],
                position = Cesium.Cartesian3.fromDegrees(longitude, latitude, altitude),
                heading = Cesium.Math.toRadians(azimuth), pitch = 0, roll = 0,
                hpr = new Cesium.HeadingPitchRoll(heading, pitch, roll),
                orientation = Cesium.Transforms.headingPitchRollQuaternion(position, hpr);
            let rangeCircle = sate['fcps'];
            let rcLen = rangeCircle.length;
            let footprint = [];
            for (let j = 0; j < rcLen; j++) {
                let rc = rangeCircle[j], rangelong = rc['lon'],
                    rangelat = rc['lat'];

                footprint.push(rangelong, rangelat);
            }

            //足迹圆设置
            fpCircle.polygon.hierarchy = Cesium.Cartesian3.fromDegreesArray(footprint);
            //fpCircle.position = position;

            //模型设置
            model.position = position;
            model.orientation = orientation;
            let props = {
                "名称": sateName,
                "高度(km)": sate['altitude'],
                "方位(°)": azimuth,
                "经度(°)": longitude,
                "纬度(°)": latitude,
                "范围(km)": sate['rangeSat'],
                "足迹圆半径(km)": sate['fcr'],
                "多普勒因子": sate['df']
            };
            //根据足迹圆获取过境信息
            let fpcWKT = this.getFootPrintWKT(rangeCircle);
            let fpcGeoJSON = Terraformer.WKT.parse(fpcWKT);
            if (this.provinceStore) {
                this.provinceStore.intersects(fpcGeoJSON, function (err, results) {
                    if (results && results.length > 0) {
                        let regions = [];
                        for (let i = 0; i < results.length; i++) {
                            regions.push(results[i]['properties'])
                        }
                        //只要相交则认为过境
                        props['过境区域'] = regions;
                        /*let regions = {
                            "type": "FeatureCollection",
                            "features": results
                        };
                        viewer.dataSources.add(Cesium.GeoJsonDataSource.load(regions, {
                            stroke: Cesium.Color.fromCssColorString('#FFFF00'),
                            fill: Cesium.Color.YELLOW.withAlpha(0.5),
                            strokeWidth: 3
                        })).then(function (dataSource) {
                            viewer.dataSources.add(dataSource);
                        }).otherwise(function (error) {
                            window.alert(error);
                        });*/

                        let propsBag = new Cesium.PropertyBag(props);
                        let propsText = me.getInfoBoxDesc(props);

                        //设定属性
                        model.properties = propsBag;
                        //设定infobox面板内容
                        model.description = propsText;
                    }
                });
            }

            let propsBag = new Cesium.PropertyBag(props);
            let propsText = this.getInfoBoxDesc(props);

            //设定属性
            model.properties = propsBag;
            //设定infobox面板内容
            model.description = propsText;
        }
    },
    /**
     * 根据卫星当前时刻位置绘制模型并返回模型实例
     * @param viewer
     * @param url
     * @param sate
     * @returns {Cesium.Entity}
     */
    createModelEx: function (viewer, url, sate) {
        let me = this;

        let sateName = sate['sateName'], modelId = sateName + "_satellite_id",
            longitude = sate['longitude'], latitude = sate['latitude'],
            altitude = sate['altitude'] * 1000, azimuth = sate['azimuth'],
            position = Cesium.Cartesian3.fromDegrees(longitude, latitude, altitude),
            heading = Cesium.Math.toRadians(azimuth), pitch = 0, roll = 0,
            hpr = new Cesium.HeadingPitchRoll(heading, pitch, roll),
            orientation = Cesium.Transforms.headingPitchRollQuaternion(position, hpr);
        let entity = viewer.entities.add({
            id: modelId,
            name: sateName,
            position: position,
            orientation: orientation,
            model: {
                uri: url,
                minimumPixelSize: 64,
                maximumScale: 2000
            }
        });
        let props = {
            "名称": sateName,
            "高度(km)": sate['altitude'],
            "方位(°)": azimuth,
            "经度(°)": longitude,
            "纬度(°)": latitude,
            "范围(km)": sate['rangeSat'],
            "足迹圆半径(km)": sate['fcr'],
            "多普勒因子": sate['df']
        };
        //根据足迹圆获取过境信息
        let rangeCircle = sate['fcps'];
        let fpcWKT = this.getFootPrintWKT(rangeCircle);
        let fpcGeoJSON = Terraformer.WKT.parse(fpcWKT);
        if (this.provinceStore) {
            this.provinceStore.intersects(fpcGeoJSON, function (err, results) {
                if (results && results.length > 0) {
                    let regions = [];
                    for (let i = 0; i < results.length; i++) {
                        regions.push(results[i]['properties'])
                    }
                    //只要相交则认为过境
                    props['过境区域'] = regions;
                    /*let regions = {
                        "type": "FeatureCollection",
                        "features": results
                    };
                    viewer.dataSources.add(Cesium.GeoJsonDataSource.load(regions, {
                        stroke: Cesium.Color.fromCssColorString('#FFFF00'),
                        fill: Cesium.Color.YELLOW.withAlpha(0.5),
                        strokeWidth: 3
                    })).then(function (dataSource) {
                        viewer.dataSources.add(dataSource);
                    }).otherwise(function (error) {
                        window.alert(error);
                    });*/

                    let propsBag = new Cesium.PropertyBag(props);
                    let propsText = me.getInfoBoxDesc(props);

                    //设定属性
                    entity.properties = propsBag;
                    //设定infobox面板内容
                    entity.description = propsText;
                }
            });
        }

        let propsBag = new Cesium.PropertyBag(props);
        let propsText = this.getInfoBoxDesc(props);

        //设定属性
        entity.properties = propsBag;
        //设定infobox面板内容
        entity.description = propsText;
        //追踪对象
        //viewer.trackedEntity = entity;
        return entity;
    },
    //创建足迹圆
    createRangeCircleEx: function (viewer, sate) {
        let rangeCircle = sate['fcps'], sateName = sate['sateName'],
            entityId = sateName + "_rangecircle_id";
        let rcLen = rangeCircle.length;
        let footprint = [];
        for (let j = 0; j < rcLen; j++) {
            let rc = rangeCircle[j], rangelong = rc['lon'],
                rangelat = rc['lat'];

            footprint.push(rangelong, rangelat);
        }

        let entity = viewer.entities.add({
            id: entityId,
            name: sateName,
            polygon: {
                hierarchy: Cesium.Cartesian3.fromDegreesArray(footprint),
                strokeWidth: 3,
                material: Cesium.Color.GREEN.withAlpha(0.5),
                outline: true,
                outlineColor: Cesium.Color.RED,
                clampToGround: true
            }
        });
        //定位实体
        viewer.flyTo(entity);
        return entity;
    },
    //星下点轨迹线
    createTrailEx: function (viewer, sate, satePoses) {
        let sateName = sate.get('text'),
            entityId = sateName + "_satetrail_id";
        let stLen = satePoses.length;
        let trailPots = [];
        for (let i = 0; i < stLen; i++) {
            let sp = satePoses[i], lon = sp['lon'],
                lat = sp['lat'];

            trailPots.push(lon, lat);
        }

        viewer.entities.add({
            id: entityId,
            name: sateName,
            polyline: {
                positions: Cesium.Cartesian3.fromDegreesArray(trailPots),
                material: Cesium.Color.ORANGE,
                width: 1,
                clampToGround: false
            }
        });
    },
    createModel: function (viewer, url, sate) {
        let sateName = sate['sateName'], modelId = sateName + "_satellite_id",
            longitude = sate['longitude'], latitude = sate['latitude'],
            altitude = sate['altitude'] * 1000, azimuth = sate['azimuth'],
            position = Cesium.Cartesian3.fromDegrees(longitude, latitude, altitude),
            heading = Cesium.Math.toRadians(azimuth), pitch = 0, roll = 0,
            hpr = new Cesium.HeadingPitchRoll(heading, pitch, roll),
            orientation = Cesium.Transforms.headingPitchRollQuaternion(position, hpr);

        /*
            方位:261.64305846848504度。
            海拔:-69.81020218211603度。
            纬度:-5.17893259903369度。
            经度:217.8293117221259度。
            日期:2019年7月23日星期二14:24:48 CST
            范围:12786.876867652023公里。
            射程:-1.8241275272743966米/秒。
            阶段:3.5898699673458623 / (256)
            高度:770.0777490575856公里
            θ:-1.8371156930907733 rad /秒
            是否椭圆: 是
            椭圆深度:0.24982649717491212弧度
        */
        let entity = viewer.entities.add({
            id: modelId,
            name: sateName,
            position: position,
            orientation: orientation,
            model: {
                uri: url,
                minimumPixelSize: 64,
                maximumScale: 2000
            }
        });
        let props = {
                "名称": sateName,
                "高度(m)": altitude,
                "方位(°)": azimuth,
                "经度(°)": longitude,
                "纬度(°)": latitude
            }, regions = sate['regions'], propsBag = new Cesium.PropertyBag(props),
            propsText = '<table class="cesium-infoBox-defaultTable">';
        if (regions && regions.length > 0) {
            let regionList = [];
            for (let i = 0; i < regions.length; i++) {
                regionList.push(regions[i]['name']);
            }
            props["信号覆盖区域"] = regionList.toString();
        } else {
            props["信号覆盖区域"] = "不经过中国";
        }
        for (let key in props) {
            propsText += '<tr>';
            propsText += '<th>' + key + '</th><td>' + props[key] + '</td>';
            propsText += '</tr>';
        }
        //设定属性
        entity.properties = propsBag;
        //设定infobox面板内容
        entity.description = propsText;
        //追踪对象
        //viewer.trackedEntity = entity;
    },
    createRangeCircle: function (viewer, sate) {
        let rangeCircle = sate['rangeCircle'], sateName = sate['sateName'],
            entityId = sateName + "_rangecircle_id";
        let rcLen = rangeCircle.length;
        let footprint = [];
        for (let j = 0; j < rcLen; j++) {
            let rc = rangeCircle[j], rangelong = rc['lon'],
                rangelat = rc['lat'];

            footprint.push(rangelong, rangelat);
        }
        let colors = [
            Cesium.Color.ORANGE.withAlpha(0.5),
            Cesium.Color.GREEN.withAlpha(0.5),
            Cesium.Color.YELLOW.withAlpha(0.5),
            Cesium.Color.BLUE.withAlpha(0.5),
            Cesium.Color.PURPLE.withAlpha(0.5),
            Cesium.Color.RED.withAlpha(0.5),
            Cesium.Color.ORANGERED.withAlpha(0.5),
            Cesium.Color.AZURE.withAlpha(0.5),
            Cesium.Color.BURLYWOOD.withAlpha(0.5),
            Cesium.Color.ALICEBLUE.withAlpha(0.5)
        ];
        let entity = viewer.entities.add({
            id: entityId,
            name: sateName,
            polygon: {
                hierarchy: Cesium.Cartesian3.fromDegreesArray(footprint),
                material: colors[Math.floor(Math.random() * 5)],
                outline: true,
                outlineColor: Cesium.Color.BLACK
            }
        });
        //定位实体
        viewer.flyTo(entity);
    },
    removeEntityFromViewer: function (viewer, entityId) {
        viewer.entities.removeById(entityId);
    },
    /**
     * 默认显示三维场景渲染性能参数，其中fps（屏幕每秒刷新次数，一般越高越好），ms（发出指令响应时间，一般越低越好）
     * @param viewer
     * @param isShow
     */
    showFramesPerSecond: function (viewer, isShow) {
        let scene = viewer.scene;
        scene.debugShowFramesPerSecond = isShow;
    },
    /**
     * 是否加载全球地形
     * @param viewer
     * @param isShow
     */
    showGlobeTerrain: function (viewer, isShow) {
        if (isShow) {
            viewer.terrainProvider = Cesium.createWorldTerrain({
                requestWaterMask: true,
                requestVertexNormals: true
            });
            viewer.scene.globe.showWaterEffect = !isShow;
        } else {
            if (viewer.terrainProvider && viewer.terrainProvider.ready) {
                viewer.terrainProvider = new Cesium.EllipsoidTerrainProvider({});
            }
        }
    },
    showSunLight: function (viewer, isShow) {
        //阳光照射区域高亮
        viewer.scene.globe.enableLighting = isShow;
    },
    showWaterMask: function (viewer, isShow) {
        if (viewer.terrainProvider && viewer.terrainProvider.ready) {
            viewer.scene.globe.showWaterEffect = isShow;
        }
    },
    showCloudFog: function (viewer, isShow) {
        viewer.scene.fog.enabled = isShow;
    },
    /**
     * 界面显示冲突解决
     * @param viewer
     */
    sysCompatibility: function (viewer) {
        //1、InfoBox----用于解决自定义面板与信息框架之间的压盖冲突
        viewer.selectedEntityChanged.addEventListener(function (entities) {
            if (entities) {
                //显示信息框，隐藏相关操作面板
                loUtil.resolveConflict(false);
            } else {
                //隐藏信息框，显示相关操作面板
                loUtil.resolveConflict(true);
            }
        });
        //2、BaseLayerPicker.
        let blpCommand = viewer.baseLayerPicker.viewModel.toggleDropDown;
        if (blpCommand.canExecute) {
            blpCommand.beforeExecute.addEventListener(function () {
                //隐藏当前开启的需要去掉冲突的面板
                loUtil.resolveConflict(false);
            });
            blpCommand.afterExecute.addEventListener(function () {
                //开启底图显隐监控任务
                if (globeUtil.baseLayerMonitorTask == null) {
                    globeUtil.baseLayerMonitorTask = {
                        run: function () {
                            console.log('开启冲突监控...');
                            if (!viewer.baseLayerPicker.viewModel.dropDownVisible && !viewer.infoBox.viewModel.showInfo) {
                                //显示之前已经隐藏的面板
                                loUtil.resolveConflict(true);

                                //销毁时间器
                                Ext.TaskManager.stop(globeUtil.baseLayerMonitorTask);
                                globeUtil.baseLayerMonitorTask = null;
                                console.log('结束冲突监控...');
                            }
                        },
                        interval: 1000 //单位毫秒
                    };
                    Ext.TaskManager.start(globeUtil.baseLayerMonitorTask);
                }
            });
        }
    },
    /**
     * 场景初始位置恢复
     * @param viewer
     */
    resetView: function (viewer) {
        viewer.camera.flyTo({
            //destination: Cesium.Cartesian3.fromDegrees(conf.globe.location.X, conf.globe.location.Y, conf.globe.location.H)
            destination: Cesium.Rectangle.fromDegrees(conf.globe.bound.W, conf.globe.bound.S, conf.globe.bound.E, conf.globe.bound.N)
        });
    },
    /**
     * 飞到指定位置
     * @param x 经度
     * @param y 纬度
     * @param h 相机高度
     */
    flyToPosition: function (viewer, x, y, h) {
        //定位区域
        viewer.camera.flyTo({
            destination: Cesium.Cartesian3.fromDegrees(x, y, h && h > 0 ? h : conf.globe.location.H)
        });
    },
    /**
     * 动画控制器，时间线状态恢复
     * @param viewer
     */
    resetTime: function (viewer) {
        let clock = viewer.clock, now = new Date();
        clock.multiplier = 1;
        let st = Cesium.JulianDate.fromDate(new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1));
        let et = Cesium.JulianDate.fromDate(new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1));

        //设置动画控制时间范围及当前时间--此处一定要注意先后顺序，clock（即animation）时间必须在timeline之前先设置，否则会出现异常
        clock.startTime = st;
        clock.stopTime = et;
        clock.currentTime = Cesium.JulianDate.fromDate(now);

        //设置时间线时间范围
        viewer.timeline.zoomTo(st, et);
    },
    /**
     * 控制场景动画和时间线显示和隐藏
     * @param viewer 三维场景实例
     * @param isShow true-显示，false-隐藏
     */
    showTimePhaseBar: function (viewer, isShow) {
        // .cesium-viewer-animationContainer, /* 左下角动画控件 */
        // .cesium-viewer-timelineContainer, /* 时间线 */
        let animationDom = Ext.query("div .cesium-viewer-animationContainer")[0],
            timelineDom = Ext.query("div .cesium-viewer-timelineContainer")[0];
        if (animationDom && timelineDom) {
            if (!isShow) {
                animationDom.style.display = 'none';
                timelineDom.style.display = 'none';
            } else {
                animationDom.style.display = 'block';
                timelineDom.style.display = 'block';
            }
            viewer.clock.shouldAnimate = isShow;
            viewer.forceResize();
        }
    },
    /**
     * 判断时间轴是否显示
     * @returns {boolean}
     */
    isTimePhaseShow: function () {
        let animationDom = Ext.query("div .cesium-viewer-animationContainer")[0],
            timelineDom = Ext.query("div .cesium-viewer-timelineContainer")[0];
        return !(animationDom.style.display === 'none' && timelineDom.style.display === 'none');
    },
    /**
     * 创建底图切换工具
     */
    createBaseLayerPicker4TDT: function (isShowLabel) {
        if (conf.globe.instance) {
            let tileMatrixSetID = 'c';
            let viewer = conf.globe.instance;

            //清除默认底图集合
            if (!isShowLabel) {
                viewer.imageryLayers.removeAll();
            }

            viewer.baseLayerPicker.viewModel.imageryProviderViewModels = [];
            viewer.baseLayerPicker.viewModel.terrainProviderViewModels = [];

            //影像底图切换
            let image_tdt = new Cesium.ProviderViewModel({
                name: "天地图影像",
                tooltip: "影像底图",
                //显示切换的图标
                iconUrl: "resources/images/acsweb/image.png",
                creationFunction: function () {
                    let tdt = new Cesium.WebMapTileServiceImageryProvider({
                        url: TDTURL_CONFIG.TDT_IMG_C,
                        credit: new Cesium.Credit('天地图全球影像服务'),
                        layer: "tdtImg4Acs_c",
                        style: "default",
                        format: "tiles",
                        tileMatrixSetID: tileMatrixSetID,
                        subdomains: ["t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7"],
                        tilingScheme: new Cesium.GeographicTilingScheme(),
                        tileMatrixLabels: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"],
                        maximumLevel: conf.globe.maxLevel
                    });

                    if (isShowLabel) {
                        //影像标注
                        addMapLabelProvider(TDTURL_CONFIG.TDT_CIA_C, 'tdtLabel4Acs_c', tileMatrixSetID);
                    }

                    return tdt;
                }
            });
            //矢量底图切换
            let vector_tdt = new Cesium.ProviderViewModel({
                name: "天地图矢量",
                tooltip: "矢量底图",
                iconUrl: "resources/images/acsweb/vector.png",
                creationFunction: function () {
                    let tdt = new Cesium.WebMapTileServiceImageryProvider({
                        url: TDTURL_CONFIG.TDT_VEC_C,
                        credit: new Cesium.Credit('天地图全球矢量服务'),
                        layer: "tdtVec4Acs_c",
                        style: "default",
                        format: "tiles",
                        tileMatrixSetID: tileMatrixSetID,
                        subdomains: ["t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7"],
                        tilingScheme: new Cesium.GeographicTilingScheme(),
                        tileMatrixLabels: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"],
                        maximumLevel: conf.globe.maxLevel
                    });
                    if (isShowLabel) {
                        addMapLabelProvider(TDTURL_CONFIG.TDT_CVA_C, 'tdtLabel4Acs_c', tileMatrixSetID);
                    }
                    return tdt;
                }
            });

            //地形底图切换
            let terrain_tdt = new Cesium.ProviderViewModel({
                name: "天地图地形",
                tooltip: "地形底图",
                iconUrl: "resources/images/acsweb/terrain.png",
                creationFunction: function () {
                    let tdt = new Cesium.WebMapTileServiceImageryProvider({
                        url: TDTURL_CONFIG.TDT_TER_C,
                        credit: new Cesium.Credit('天地图全球地形服务'),
                        layer: "tdtTer4Acs_c",
                        style: "default",
                        format: "tiles",
                        //format: "image/jpeg",
                        //tileMatrixSetID: "GoogleMapsCompatible",
                        tileMatrixSetID: tileMatrixSetID,
                        subdomains: ["t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7"],
                        tilingScheme: new Cesium.GeographicTilingScheme(),
                        tileMatrixLabels: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"],
                        maximumLevel: conf.globe.maxLevel
                    });
                    if (isShowLabel) {
                        addMapLabelProvider(TDTURL_CONFIG.TDT_CTA_C, 'tdtLabel4Acs_c', tileMatrixSetID);
                    }

                    return tdt;
                }
            });

            //标注加载
            let addMapLabelProvider = function (url, layerName, tmsId) {
                viewer.imageryLayers.addImageryProvider(new Cesium.WebMapTileServiceImageryProvider({
                    url: url,
                    layer: layerName,
                    credit: new Cesium.Credit('天地图全球标注服务'),
                    style: "default",
                    format: "tiles",
                    tileMatrixSetID: tmsId,
                    subdomains: ["t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7"],
                    tilingScheme: new Cesium.GeographicTilingScheme(),
                    tileMatrixLabels: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"],
                    maximumLevel: conf.globe.maxLevel
                }))
            };

            //更改地图切换的标题
            document.querySelector('.cesium-baseLayerPicker-sectionTitle').innerText = "场景底图";

            //设置默认地图源
            viewer.baseLayerPicker.viewModel.imageryProviderViewModels = [image_tdt, vector_tdt, terrain_tdt];
            viewer.baseLayerPicker.viewModel.selectedImagery = viewer.baseLayerPicker.viewModel.imageryProviderViewModels[0];
        }
    },
    /**
     * 是否显示底图标注
     * @param viewer
     * @param isShow
     */
    showBaseLayerLabel: function (viewer, isShow) {
        //todo 是否显示底图标注，该功能后续完善。
    },

    /**
     * 卫星星轨绘制工具
     */
    czml: {
        /**
         * czml数据源,包含卫星轨道及波束对象
         */
        hashMap: new Ext.util.HashMap(),
        addToHashMap: function (key, ds, sate, waveBeam) {
            let sateInfo = {
                'datasource': ds,
                'satellite': sate,
                'wavebeam': waveBeam
            };
            this.hashMap.add(key, sateInfo);
        },
        removeFromHashMap: function (key) {
            this.hashMap.removeAtKey(key);
        },
        get: function (key) {
            return this.hashMap.get(key);
        }
    },
    /**
     * 三维绘制工具
     */
    draw: {
        //经纬网格图层
        lonLatGridLayer: null,
        //(1)卫星轨道、波束绘制------------
        /**
         * 卫星轨道绘制
         * @param isShowBeam : 是否显示卫星波束
         */
        addOrbit: function (viewer, id, czml, isShowBeam) {
            viewer.dataSources.add(Cesium.CzmlDataSource.load(czml)).then(function (dataSource) {
                let clock = viewer.clock, sate = dataSource.entities.getById(id),
                    property = new Cesium.SampledPositionProperty(), totalLength = 0, totalStep = 0, waveBeamLength = 0,
                    pos = [];
                for (let ind = 0; ind < 292; ind++) {
                    let time = Cesium.JulianDate.addSeconds(clock.startTime, 300 * ind, new Cesium.JulianDate());
                    let position = sate.position.getValue(time);
                    if (position != null) {
                        let cartographic = viewer.scene.globe.ellipsoid.cartesianToCartographic(position);
                        let lat = Cesium.Math.toDegrees(cartographic.latitude),
                            lng = Cesium.Math.toDegrees(cartographic.longitude),
                            hei = cartographic.height / 1.5;
                        property.addSample(time, Cesium.Cartesian3.fromDegrees(lng, lat, hei));
                        pos.push(lng);
                        pos.push(lat);

                        totalLength += hei;
                        totalStep += 1;
                    }
                }

                //创建卫星波束相关参数
                if (totalLength > 0 && totalStep > 0) {
                    waveBeamLength = totalLength / totalStep;
                } else {
                    waveBeamLength = 600000;
                }

                let cylinderEntity = viewer.entities.add({
                    cylinder: {
                        HeightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
                        length: waveBeamLength,
                        topRadius: 0,
                        bottomRadius: waveBeamLength / 4,
                        material: Cesium.Color.RED.withAlpha(.4),
                        outline: !0,
                        numberOfVerticalLines: 0,
                        outlineColor: Cesium.Color.RED.withAlpha(.8)
                    }
                });
                cylinderEntity.position = property;
                cylinderEntity.position.setInterpolationOptions({ //设定位置的插值算法
                    interpolationDegree: 5,
                    interpolationAlgorithm: Cesium.LagrangePolynomialApproximation
                });
                cylinderEntity.show = isShowBeam;

                //临时存储到hashmap
                globeUtil.czml.addToHashMap(id, dataSource, sate, cylinderEntity);

                //缩放到卫星轨迹全幅范围
                //方案一
                viewer.camera.flyTo({destination: Cesium.Cartesian3.fromDegrees(viewer.camera.direction.x, viewer.camera.direction.y, waveBeamLength * 10)});
                //方案二
                /*let orbitPolygon = new Cesium.PolygonGeometry({
                    polygonHierarchy: new Cesium.PolygonHierarchy(
                        Cesium.Cartesian3.fromDegreesArray(pos)
                    )
                });
                let orbitGeometry = Cesium.PolygonGeometry.createGeometry(orbitPolygon);*/

                /* viewer.camera.flyToBoundingSphere(orbitGeometry.boundingSphere, {maximumHeight: waveBeamLength * 20});
                 viewer.camera.flyTo({destination: Cesium.Cartesian3.fromDegrees(orbitGeometry.boundingSphere.center.x, orbitGeometry.boundingSphere.center.y, waveBeamLength * 10)});*/

                //删除临时对象
                //delete orbitGeometry;
            });
        },
        /**
         * 删除卫星轨道
         * @param key
         */
        deleteOrbit: function (key, viewer) {
            let orbitInfo = globeUtil.czml.get(key);
            if (orbitInfo != null) {
                let ds = orbitInfo['datasource'], sate = orbitInfo['satellite'], waveBeam = orbitInfo['wavebeam'];
                if (ds && sate && waveBeam) {
                    //删除卫星
                    ds.entities.remove(sate);
                    //删除波束
                    viewer.entities.remove(waveBeam);
                    //删除数据源
                    viewer.dataSources.remove(ds, true);
                }

                //删除hashmap
                globeUtil.czml.removeFromHashMap(key);

                //判断是否为最后一条星轨
                if (globeUtil.czml.hashMap.getKeys().length === 0) {
                    //三维场景时间状态恢复
                    globeUtil.resetTime(conf.globe.instance);

                    //回复到初始位置
                    globeUtil.resetView(conf.globe.instance);

                    //隐藏三维场景时间轴
                    globeUtil.showTimePhaseBar(conf.globe.instance, false);
                }
            }
        },
        /*
        * 场景重置
        * */
        clear: function () {
            if (globeUtil.czml.hashMap) {
                //清除卫星轨迹
                let keys = globeUtil.czml.hashMap.getKeys();
                if (keys && keys.length > 0) {
                    for (let i = 0; i < keys.length; i++) {
                        let key = keys[i];
                        this.deleteOrbit(key, conf.globe.instance);
                    }
                }

                globeUtil.czml.hashMap.clear();

                //三维场景时间状态恢复
                globeUtil.resetTime(conf.globe.instance);

                //回复到初始位置
                //globeUtil.resetView(conf.globe.instance);

                //隐藏三维场景时间轴
                globeUtil.showTimePhaseBar(conf.globe.instance, false);
            }
        },
        /**
         * 显示或隐藏波束
         */
        showWaveBeam: function (isShow) {
            if (globeUtil.czml.hashMap) {
                globeUtil.czml.hashMap.each(function (key, orbitInfo, length) {
                    if (orbitInfo != null) {
                        let waveBeam = orbitInfo['wavebeam'];
                        if (waveBeam != null) {
                            waveBeam.show = isShow;
                        }
                    }
                });
            }
        },
        /**
         * 添加全球经纬网格，调用代码如下：
         * @description globeUtil.draw.addLonLatGrid(conf.globe.instance, 'GlobeLonLatGridLayer', 0.5, true);
         * @param viewer
         * @param name 格网图层名称
         * @param alpha 格网图层透明度
         * @param show 格网图层是否显示
         */
        addLonLatGrid: function (viewer, name, alpha, isShow) {
            if (this.lonLatGridLayer == null && isShow) {
                let gridProvider = new Cesium.GridImageryProvider({
                    color: Cesium.Color.fromCssColorString(conf.common.COLOR),
                    glowColor: Cesium.Color.fromCssColorString(conf.common.HIGH_COLOR),
                    glowWidth: 1
                    /*cells:16,
                    tileWidth:512,
                    tileHeight:512,
                    canvasSize:512*/
                });
                let layer = viewer.imageryLayers.addImageryProvider(gridProvider);
                layer.alpha = Cesium.defaultValue(alpha, 0.5);
                layer.show = Cesium.defaultValue(isShow, true);
                layer.name = name;
                this.lonLatGridLayer = layer;
            } else {
                viewer.imageryLayers.remove(this.lonLatGridLayer, true);
                this.lonLatGridLayer = null;
            }
        }
    },
    /**
     * json工具
     */
    json: {
        //json数据渲染相关内容存储
        hashMap: new Ext.util.HashMap(),
        //是否开启要素渲染自动追踪
        isAutoTracking: true,
        //要素定位及高亮绘制相关变量
        highlightDataKey: 'highlight-data-key',
        highlightDataSourceKey: 'highlight-data-source-key',

        init: function () {
            this.mapFeatureField();
        },
        /**
         * 将数据存储到hash表中
         * @param key 数组hash key
         * @param array 数组对象
         */
        addDataToHashMap: function (key, array) {
            if (this.get(key) == null) {
                this.hashMap.add(key, array);
            } else {
                this.hashMap.replace(key, this.get(key).concat(array));
            }
        },
        /**
         * 将数据源存储到hash表中
         * @param key 数据源集合hash key
         * @param dataSource 数据源集合
         */
        addDataSourceToHashMap: function (key, dataSource) {
            if (this.get(key) == null) {
                this.hashMap.add(key, [dataSource]);
            } else {
                this.get(key).push(dataSource);
            }
        },
        /**
         * 将请求集合存储到hash表中
         * @param key 请求集合key
         * @param requests 请求集合
         */
        addRequestToHashMap: function (key, requests) {
            if (this.get(key) == null) {
                this.hashMap.add(key, requests);
            } else {
                this.hashMap.replace(key, requests);
            }
        },
        /**
         * 将请求状态等信息存储到hash表中
         * @param key 状态key
         * @param state 状态对象
         */
        addStateToHashMap: function (key, state) {
            if (this.get(key) == null) {
                this.hashMap.add(key, state);
            } else {
                this.hashMap.replace(key, state);
            }
        },
        /**
         * 从哈希表中key对应的数组中删除一项
         * @param key 数组key
         * @param value 待删除的项
         */
        removeValueFromArray: function (key, value) {
            if (this.get(key) && this.get(key).length > 0) {
                let index = this.get(key).indexOf(value);
                if (index > -1) {
                    this.get(key).splice(index, 1);
                }
            }
        },
        removeFromHashMap: function (key) {
            this.hashMap.removeAtKey(key);
        },
        get: function (key) {
            return this.hashMap.get(key);
        },
        /**
         * 删除场景中已经加载的geojson数据源
         * @param viewer
         * @param mainKey key对应的项目为每类红线数据源集合对应的key的集合
         * @param value 空值则删除已经加载的数据源
         */
        removeDataSourceFromViewer: function (viewer, mainKey, value) {
            if (viewer) {
                if (mainKey) {
                    if (value) {
                        //根据特定value删除对应的数据源
                        //1、删除数据源
                        let dsCol = this.get(value);
                        if (dsCol && dsCol.length > 0) {
                            for (let i = 0; i < dsCol.length; i++) {
                                let isExist = viewer.dataSources.contains(dsCol[i]);
                                if (isExist) {
                                    dsCol[i].entities.removeAll();
                                    viewer.dataSources.remove(dsCol[i], true);
                                }
                            }
                        }
                        //1.1 删除value对应的hash项
                        this.removeFromHashMap(value);

                        //2、删除key对应数组中的value项
                        this.removeValueFromArray(mainKey, value);
                    } else {
                        //删除所有数据源
                        //1、删除数据源
                        let dsKeys = globeUtil.json.get(mainKey);
                        if (dsKeys && dsKeys.length > 0) {
                            for (let i = 0; i < dsKeys.length; i++) {
                                let dsCol = this.get(dsKeys[i]);
                                if (dsCol && dsCol.length > 0) {
                                    for (let j = 0; j < dsCol.length; j++) {
                                        let isExist = viewer.dataSources.contains(dsCol[j]);
                                        if (isExist) {
                                            dsCol[j].entities.removeAll();
                                            viewer.dataSources.remove(dsCol[j], true);
                                        }
                                    }
                                }
                                this.removeFromHashMap(dsKeys[i]);
                            }
                        }

                        //删除key对应的hash项目
                        this.removeFromHashMap(mainKey);
                    }
                }
            }
        },
        //预处理json数据转化为标准格式的geojson
        json2geojson: function (key, data, geomField) {
            let geoJson = {"type": "FeatureCollection", "features": []};
            if (data && data.length > 0) {
                for (let i = 0; i < data.length; i++) {
                    let geoItem = {};
                    let item = data[i];

                    //add type, only consider Feature, FeatureCollection is not consider.
                    geoItem["type"] = "Feature";

                    //add id
                    geoItem["id"] = key + '-' + guid();

                    //add properties
                    geoItem["properties"] = {};
                    for (let fkey in item) {
                        if (geomField !== fkey && fkey !== 'id') {
                            geoItem["properties"][this.hashMap.get(fkey)] = item[fkey];
                        }
                    }

                    //add geometry
                    geoItem["geometry"] = Ext.JSON.decode(decodeURIComponent(item[geomField]), true);

                    geoJson['features'].push(geoItem);
                }
            }
            return geoJson;
        },
        /**
         * 红线数据字段中英文映射
         */
        mapFeatureField: function () {
            this.hashMap.add('eCls', '类型');
            this.hashMap.add('eEcosystem', '生态系统');
            this.hashMap.add('eProvince', '省');
            this.hashMap.add('eCounty', '县（区）');
            this.hashMap.add('eX', '中心经度（°）');
            this.hashMap.add('eY', '中心纬度（°）');
            this.hashMap.add('eEcologyId', 'ID');
            this.hashMap.add('ePcode', '省编码');
            this.hashMap.add('eArea', '面积（㎡）');
            this.hashMap.add('eCode', '县（区）编码');

            //追加专题数据字段映射
            this.hashMap.add('area', '面积（㎡）');
            this.hashMap.add('fcls', 'Ⅰ级类');
            this.hashMap.add('scls', 'Ⅱ级类');
            this.hashMap.add('firstclass', 'Ⅰ级类');
            this.hashMap.add('secondclass', 'Ⅱ级类');
            this.hashMap.add('x', '中心经度（°）');
            this.hashMap.add('y', '中心纬度（°）');

            //生态红线要素详情字段映射
            this.hashMap.add('cls', '类型');
            this.hashMap.add('ecosystem', '生态系统');
            this.hashMap.add('province', '省');
            this.hashMap.add('county', '县（区）');
            this.hashMap.add('pcode', '省编码');
            this.hashMap.add('code', '县（区）编码');
        },
        /**
         * 在三维场景绘制要素
         * @param viewer 三维场景实例
         * @param arr 数据集合
         * @param isIncrement 当前数据是否为增量数据（之前没有缓存）
         */
        drawFeature: function (viewer, key, arr, dsKey, isIncrement, color) {
            let me = this;
            if (viewer) {
                let drawArr = [];
                if (!isIncrement) {
                    //数据已缓存
                    let cacheData = this.get(key);
                    if (cacheData && cacheData.length > 0) {
                        drawArr = cacheData;
                    }
                } else {
                    //数据暂未缓存，属增量数据
                    drawArr = arr;
                }

                if (drawArr && drawArr.length > 0) {
                    //预处理数据
                    let geoJson = this.json2geojson(key, drawArr, "eGeometry");
                    //数据加载
                    let promise = Cesium.GeoJsonDataSource.load(geoJson, {
                        clampToGround: true,
                        fill: Cesium.Color.fromBytes(color[0], color[1], color[2], color[3]),
                        stroke: Cesium.Color.fromBytes(color[0], color[1], color[2], color[3]),
                        strokeWidth: 3,
                        markerSymbol: '?'
                    }).then(function (dataSource) {
                        //数据源加载渲染
                        viewer.dataSources.add(dataSource);
                        //缓存数据源
                        me.addDataSourceToHashMap(dsKey, dataSource);

                        //以下代码用于给每个实体单独设置样式
                        /*let entities = dataSource.entities.values;
                        for (let i = 0; i < entities.length; i++) {
                            let entity = entities[i];
                            entity.polygon.material = Cesium.Color.fromBytes(color[0], color[1], color[2], color[3]);
                            entity.polygon.outline = true;
                            entity.polygon.outlineColor = Cesium.Color.fromBytes(color[0], color[1], color[2], color[3]);

                            //是否需要根据面积拉伸搞高程，酌情考虑。
                            entity.polygon.extrudedHeight = entity.properties['eArea'] / 1000;
                        }*/

                        //追踪数据
                        if (me.isAutoTracking) {
                            viewer.flyTo(dataSource);
                        }
                    }).otherwise(function (error) {
                        Ext.Msg.alert('提示', '系统错误，请联系管理员，错误内容：' + error);
                    });
                }
            }
        },
        drawBuildingModel: function (viewer, dsKey, maskTarget) {
            let me = this, loadingMask = null;
            loadingMask = ajax.fn.showMask(maskTarget, "数据量较大，请耐心等候...");
            let floorColors = {
                10: new Cesium.Color.fromCssColorString("#B3E5FC").withAlpha(1),
                15: new Cesium.Color.fromCssColorString("#81D4FA").withAlpha(1),
                20: new Cesium.Color.fromCssColorString("#4FC3F7").withAlpha(1),
                25: new Cesium.Color.fromCssColorString("#00B0FF").withAlpha(1),
                30: new Cesium.Color.fromCssColorString("#0091EA").withAlpha(1),
                35: new Cesium.Color.fromCssColorString("#039BE5").withAlpha(1),
                40: new Cesium.Color.fromCssColorString("#0288D1").withAlpha(1),
                300: new Cesium.Color.fromCssColorString("#0277BD").withAlpha(1)
            };

            //数据加载
            Cesium.GeoJsonDataSource.load('resources/data/buildings.json').then(function (dataSource) {
                //数据源加载渲染
                viewer.dataSources.add(dataSource);
                //以下代码用于给每个实体单独设置样式
                let entities = dataSource.entities.values;
                for (let i = 0; i < entities.length; i++) {
                    let entity = entities[i], properties = entity.properties,
                        floorNum = parseInt(properties['floor'] || 1), buildColor;
                    for (let fc in floorColors) {
                        if (floorNum < parseInt(fc)) {
                            buildColor = floorColors[fc];
                            break;
                        }
                    }
                    entity.polygon.material = buildColor;
                    entity.polygon.outline = false;
                    entity.polygon.extrudedHeight = 5 * floorNum;
                }

                //缓存数据源
                me.hashMap.add(dsKey, dataSource);

                //追踪数据
                viewer.flyTo(dataSource).then(function () {
                    ajax.fn.hideMask(loadingMask);
                });
            }).otherwise(function (error) {
                ajax.fn.hideMask(loadingMask);
                Ext.Msg.alert('提示', '系统错误，请联系管理员，错误内容：' + error);
            });
        },
        /**
         * 专题数据绘制 for array
         * @param viewer
         * @param key
         * @param dsKey
         * @param datas
         * @param color
         */
        drawSpecialFeature: function (viewer, key, dsKey, datas, color) {
            let me = this;
            if (datas && datas.length > 0) {
                //预处理数据
                let geoJson = this.json2geojson(key, datas, "geometry");

                //数据加载(点线面)
                let geoType = geoJson.features["0"].geometry.type;
                if (geoType.indexOf('Polygon') > -1) {
                    Cesium.GeoJsonDataSource.load(geoJson, {
                        clampToGround: true,
                        fill: Cesium.Color.fromBytes(color[0], color[1], color[2], color[3]),
                        stroke: Cesium.Color.fromBytes(color[0], color[1], color[2], color[3]),
                        strokeWidth: 5
                    }).then(function (dataSource) {
                        //数据源加载渲染
                        viewer.dataSources.add(dataSource);
                        viewer.dataSources.raiseToTop(dataSource);
                        //缓存数据源
                        me.hashMap.add(dsKey, dataSource);
                        //缓存数据
                        me.hashMap.add(key, datas);

                        //追踪数据
                        viewer.flyTo(dataSource);
                    }).otherwise(function (error) {
                        Ext.Msg.alert('提示', '系统错误，请联系管理员，错误内容：' + error);
                    });
                } else if (geoType.indexOf('LineString') > -1) {
                    Cesium.GeoJsonDataSource.load(geoJson, {
                        clampToGround: true,
                        fill: Cesium.Color.fromBytes(color[0], color[1], color[2], color[3]),
                        stroke: Cesium.Color.fromBytes(color[0], color[1], color[2], color[3]),
                        strokeWidth: 5
                    }).then(function (dataSource) {
                        //数据源加载渲染
                        viewer.dataSources.add(dataSource);
                        //缓存数据源
                        me.hashMap.add(dsKey, dataSource);
                        //缓存数据
                        me.hashMap.add(key, datas);

                        //追踪数据
                        viewer.flyTo(dataSource);

                    }).otherwise(function (error) {
                        Ext.Msg.alert('提示', '系统错误，请联系管理员，错误内容：' + error);
                    });
                } else if (geoType.indexOf('Point') > -1) {
                    Cesium.GeoJsonDataSource.load(geoJson).then(function (dataSource) {
                        //数据源加载渲染
                        viewer.dataSources.add(dataSource);
                        //缓存数据源
                        me.hashMap.add(dsKey, dataSource);
                        //缓存数据
                        me.hashMap.add(key, datas);

                        let entities = dataSource.entities.values;

                        for (let i = 0; i < entities.length; i++) {
                            let entity = entities[i];
                            entity.billboard = undefined;
                            entity.point = new Cesium.PointGraphics({
                                color: Cesium.Color.fromBytes(color[0], color[1], color[2], color[3]),
                                pixelSize: 10
                            });
                        }

                        //追踪数据
                        viewer.flyTo(dataSource);

                    }).otherwise(function (error) {
                        Ext.Msg.alert('提示', '系统错误，请联系管理员，错误内容：' + error);
                    });
                }
            }
        },
        /**
         * 加载geojson文件
         * @param viewer
         * @param dataSourceKey 数据源在三维场景数据源集合中的key
         * @param geoJson geojson文件路径
         */
        loadGeoJson: function (viewer, dataSourceKey, geoJson, isShow) {
            let me = this;
            let ds = this.get(dataSourceKey);
            if (ds && viewer.dataSources.contains(ds)) {
                ds.show = isShow;
            } else {
                viewer.dataSources.add(Cesium.GeoJsonDataSource.load(geoJson, {
                    stroke: Cesium.Color.ORANGERED,
                    fill: Cesium.Color.TRANSPARENT,
                    strokeWidth: 3,
                    markerSymbol: '?'
                })).then(function (dataSource) {
                    viewer.dataSources.add(dataSource);
                    me.hashMap.add(dataSourceKey, dataSource);
                });
            }
        },
        /**
         * 卸载geojson文件
         */
        unloadGeoJson: function (viewer, key, dsKey, removed) {
            let ds = this.hashMap.get(dsKey);
            if (ds) {
                ds.entities.removeAll();
                viewer.dataSources.remove(ds, true);
                this.hashMap.removeAtKey(dsKey);
                if (removed) {
                    this.hashMap.removeAtKey(key);
                }
            }
        }
    },
    /**
     * layer工具
     */
    layer: {
        splitSlider: null,//场景分割条
        operateHandler: null,
        leftLayerId: null,//地图对比左侧图层ID
        rightLayerId: null,//地图对比右侧图层ID
        //imageryIndex: 0,//开启场景分割时记录底图索引，关闭场景分割时恢复场景底图
        moveActive: false,//场景是否激活鼠标移动操作
        baseLayerStyle: {
            border: null,
            width: null,
            height: null
        },
        viewState: null,//当前视图状态
        COMPARE_STATE: 'COMPARE-STATE',//对比状态
        NORMAL_STATE: 'NORMAL-STATE',//常规状态

        //地图服务图层对象集合
        hashMap: new Ext.util.HashMap(),
        /**
         * 开启双屏对比
         */
        startCompare: function (serviceData) {
            if (this.viewState == null || this.viewState !== this.COMPARE_STATE) {
                this.viewState = this.COMPARE_STATE;

                let viewer = conf.globe.instance;
                //获取当前底图索引
                //this.imageryIndex = viewer.baseLayerPicker.viewModel.imageryProviderViewModels.indexOf(viewer.baseLayerPicker.viewModel.selectedImagery);

                //隐藏底图选择组件
                let tools = viewer.baseLayerPicker.container.childNodes;
                if (tools && tools.length > 0) {
                    this.baseLayerStyle.border = tools[1].style.border;
                    this.baseLayerStyle.width = tools[1].style.width;
                    this.baseLayerStyle.height = tools[1].style.height;
                    tools[1].style.width = '0px';
                    tools[1].style.height = '0px';
                    tools[1].style.border = '0px';
                }

                //开启对比
                this.createCompareLayer(viewer, serviceData);
                this.createSplitSlider(viewer);
                this.attachToSliderHandler();
            }
        },
        /**
         * 关闭双屏对比
         */
        closeCompare: function () {
            if (this.viewState != null && this.viewState !== this.NORMAL_STATE) {
                this.viewState = this.NORMAL_STATE;

                let viewer = conf.globe.instance;
                //清除对比图层并恢复底图选择组件
                if (this.leftLayerId && this.rightLayerId) {
                    let leftLayer = this.hashMap.get(this.leftLayerId);
                    let rightLayer = this.hashMap.get(this.rightLayerId);
                    viewer.imageryLayers.remove(leftLayer, true);
                    viewer.imageryLayers.remove(rightLayer, true);
                    this.hashMap.removeAtKey(this.leftLayerId);
                    this.hashMap.removeAtKey(this.rightLayerId);
                    this.leftLayerId = this.rightLayerId = null;
                }
                let tools = viewer.baseLayerPicker.container.childNodes;
                if (tools && tools.length > 0) {
                    tools[1].style.width = this.baseLayerStyle.width;
                    tools[1].style.height = this.baseLayerStyle.height;
                    tools[1].style.border = this.baseLayerStyle.border;
                    viewer.forceResize();
                }
                //--------------

                //关闭对比
                this.deleteSplitSlider();
                this.detachFromSliderHandler();
            }
        },
        /**
         * 创建分割条
         * @param viewer
         */
        createSplitSlider: function (viewer) {
            //1、创建滑动条
            let slider = document.getElementById('slider');
            let cesiumContainer = document.getElementById('globeContainerId');
            if (slider == null && cesiumContainer) {
                slider = document.createElement('div');
                slider.setAttribute('id', 'slider');
                slider.setAttribute('class', 'slider-cls');
                cesiumContainer.appendChild(slider);
            }
            this.splitSlider = slider;
            //2、设置场景分割位置
            viewer.scene.imagerySplitPosition = (slider.offsetLeft) / slider.parentElement.offsetWidth;
        },
        /**
         * 删除分割条
         */
        deleteSplitSlider: function () {
            let slider = document.getElementById('slider');
            let cesiumContainer = document.getElementById('globeContainerId');
            if (slider && cesiumContainer) {
                cesiumContainer.removeChild(slider);
            }

            this.splitSlider = null;
        },
        /**
         * 绑定分屏操作
         */
        attachToSliderHandler: function () {
            if (this.splitSlider) {
                this.operateHandler = new Cesium.ScreenSpaceEventHandler(this.splitSlider);
                let handler = this.operateHandler, me = this;
                this.moveActive = false;
                handler.setInputAction(function () {
                    me.moveActive = true;
                }, Cesium.ScreenSpaceEventType.LEFT_DOWN);
                handler.setInputAction(function () {
                    me.moveActive = true;
                }, Cesium.ScreenSpaceEventType.PINCH_START);

                handler.setInputAction(this.moveAction, Cesium.ScreenSpaceEventType.MOUSE_MOVE);
                handler.setInputAction(this.moveAction, Cesium.ScreenSpaceEventType.PINCH_MOVE);

                handler.setInputAction(function () {
                    me.moveActive = false;
                }, Cesium.ScreenSpaceEventType.LEFT_UP);
                handler.setInputAction(function () {
                    me.moveActive = false;
                }, Cesium.ScreenSpaceEventType.PINCH_END);
            }
        },
        /**
         * 卸载分屏操作
         */
        detachFromSliderHandler: function () {
            if (this.operateHandler) {
                let handler = this.operateHandler;
                handler.removeInputAction(function () {
                }, Cesium.ScreenSpaceEventType.LEFT_DOWN);
                handler.removeInputAction(function () {
                }, Cesium.ScreenSpaceEventType.PINCH_START);

                handler.removeInputAction(this.moveAction, Cesium.ScreenSpaceEventType.MOUSE_MOVE);
                handler.removeInputAction(this.moveAction, Cesium.ScreenSpaceEventType.PINCH_MOVE);

                handler.removeInputAction(function () {
                }, Cesium.ScreenSpaceEventType.LEFT_UP);
                handler.removeInputAction(function () {
                }, Cesium.ScreenSpaceEventType.PINCH_END);

                this.operateHandler = null;
            }
        },
        /**
         * 监控鼠标及分割条移动操作
         * @param movement
         */
        moveAction: function (movement) {
            if (!globeUtil.layer.moveActive) {
                return;
            }

            if (globeUtil.layer.splitSlider) {
                let viewer = conf.globe.instance, slider = globeUtil.layer.splitSlider;
                let relativeOffset = movement.endPosition.x;
                let splitPosition = (slider.offsetLeft + relativeOffset) / slider.parentElement.offsetWidth;
                slider.style.left = 100.0 * splitPosition + '%';
                viewer.scene.imagerySplitPosition = splitPosition;
            }
        },
        /**
         * 开启鼠标点击事件选择及信息弹框功能
         * @param viewer
         */
        enabledLayerCapture: function (viewer) {
            // HTML overlay for showing feature name on mouseover
            let nameOverlay = document.createElement('div');
            viewer.container.appendChild(nameOverlay);
            nameOverlay.className = 'backdrop';
            nameOverlay.style.display = 'none';
            nameOverlay.style.position = 'absolute';
            nameOverlay.style.bottom = '0';
            nameOverlay.style.left = '0';
            nameOverlay.style['pointer-events'] = 'none';
            nameOverlay.style.padding = '4px';
            nameOverlay.style.backgroundColor = 'black';
            nameOverlay.style.color = 'white';

            // An entity object which will hold info about the currently selected feature for infobox display
            //let selectedEntity = new Cesium.Entity();

            // Get default left click handler for when a feature is not picked on left click
            //let clickHandler = viewer.screenSpaceEventHandler.getInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);

            // Silhouette a feature blue on hover.
            viewer.screenSpaceEventHandler.setInputAction(function onMouseMove(movement) {
                // Pick a new feature
                let pickedFeature = viewer.scene.pick(movement.endPosition);
                if (!Cesium.defined(pickedFeature)) {
                    nameOverlay.style.display = 'none';
                    nameOverlay.textContent = '';
                    return;
                }

                if (pickedFeature && typeof (pickedFeature) != "undefined") {
                    let poid = pickedFeature.id;
                    if (poid && viewer.VtDataSources && viewer.VtDataSources.size > 0) {
                        //GeoJson矢量瓦片图层集合对应的hashmap key
                        let GeoJsonVectorTileCollection = 'GeoJson-Vector-Tile-Collection';
                        let vtCollection = viewer.VtDataSources.get(GeoJsonVectorTileCollection);
                        if (vtCollection && vtCollection.length > 0) {
                            let vtlen = vtCollection.length;
                            for (let i = 0; i < vtlen; i++) {
                                let provider = vtCollection[i];
                                let poParam = provider.findById(poid);
                                if (poParam && poParam["properties"]) {
                                    let props = poParam["properties"];
                                    // A feature was picked, so show it's overlay content
                                    nameOverlay.style.display = 'block';
                                    nameOverlay.style.bottom = viewer.canvas.clientHeight - movement.endPosition.y + 'px';
                                    nameOverlay.style.left = movement.endPosition.x + 'px';
                                    nameOverlay.textContent = props['NAME'];
                                    break;
                                }
                            }
                        }
                    }
                }
            }, Cesium.ScreenSpaceEventType.MOUSE_MOVE);
        },
        getFeatureInfo: function (viewer, fuid) {
            //清除已有高亮要素
            globeUtil.json.unloadGeoJson(viewer, globeUtil.json.highlightDataKey, globeUtil.json.highlightDataSourceKey, true);

            let params = {
                tzFuid: fuid
            };

            //执行成功回调
            function successCallBack(response, opts) {
                //查询结果转json对象
                let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                if (result && result['data'] && result['data'].length > 0) {
                    let featureInfo = result['data'];
                    if (featureInfo && featureInfo.length > 0) {
                        //create related entity and popup content
                        globeUtil.json.drawSpecialFeature(viewer, globeUtil.json.highlightDataKey, globeUtil.json.highlightDataSourceKey, featureInfo, '#AA00FF'.colorRgba());
                    }
                }
            }

            //执行失败回调
            function failureCallBack(response, opts) {
            }

            ajax.fn.execute(params, 'GET', conf.sys.serviceUrl + 'featureinfo/featureInfoHandler', successCallBack, failureCallBack);
        },
        /**
         * 创建分屏对比图层
         * @param viewer
         */
        createCompareLayer: function (viewer, serviceData) {
            if (serviceData) {
                let name4L = serviceData['left']['name'], cls4L = serviceData['left']['cls'],
                    time4L = serviceData['left']['ptime'], layerId4L = cls4L + '-' + name4L + '-' + time4L + '-4L',
                    url4L = conf.sys.mapRootUrl + cls4L + '/' + time4L + '/';
                let name4R = serviceData['right']['name'], cls4R = serviceData['right']['cls'],
                    time4R = serviceData['right']['ptime'], layerId4R = cls4R + '-' + name4R + '-' + time4R + '-4R',
                    url4R = conf.sys.mapRootUrl + cls4R + '/' + time4R + '/';
                let leftLayer = this.loadTileMapService(viewer, layerId4L, url4L, conf.sys.boundRectangle4TZ);
                let rightLayer = this.loadTileMapService(viewer, layerId4R, url4R, conf.sys.boundRectangle4TZ);
                leftLayer.splitDirection = Cesium.ImagerySplitDirection.LEFT;
                rightLayer.splitDirection = Cesium.ImagerySplitDirection.RIGHT;

                //临时存储分割屏左右图层Id
                this.leftLayerId = layerId4L;
                this.rightLayerId = layerId4R;

                //定位图层位置
                let lon = serviceData['left']['x'], lat = serviceData['left']['y'];
                globeUtil.flyToPosition(viewer, lon, lat, Math.ceil(viewer.camera.positionCartographic.height));
            }
        },
        /**
         * 在线或离线地图服务加载（适配谷歌web墨卡托球面投影瓦片）,通过ArcGIS Server切图
         * @param viewer
         * @param layerId
         * @param mapRootUrl
         * @param boundRectangle
         * @param tilingScheme
         * @param maxLevel
         * @returns {Cesium.ImageryLayer}
         */
        loadTileMapService: function (viewer, layerId, mapRootUrl, boundRectangle, tilingScheme, maxLevel) {
            //卸载服务
            this.unLoadTileMapService(viewer, layerId);
            //this.unLoadTileMapService(conf.globe.instance, chartUtil.timeline.layerId);
            // Geographic (EPSG:4326) project
            let tms = new Cesium.UrlTemplateImageryProvider({
                url: mapRootUrl + "{z}/{y}/{x}.png",
                // credit : '© 航天宏图',
                //注意☆☆☆：web墨卡托球面投影
                tilingScheme: tilingScheme || new Cesium.WebMercatorTilingScheme({
                    ellipsoid: Cesium.Ellipsoid.WGS84
                }),
                maximumLevel: maxLevel || 17,
                rectangle: boundRectangle || Cesium.Rectangle.MAX_VALUE
            });
            tms.requestImage = function (x, y, level) {
                if (!this.ready) {
                    return;
                }
                let url = this.url;
                url = url.replace('{z}', 'L' + numConvert(level, '00'));//level
                url = url.replace('{y}', 'R' + numConvert(y));
                url = url.replace('{x}', 'C' + numConvert(x));
                return Cesium.ImageryProvider.loadImage(this, url);
            };
            let layer = viewer.imageryLayers.addImageryProvider(tms);
            this.hashMap.add(layerId, layer);
            return layer;
        },
        /**
         * 卸载在线或离线瓦片服务，通过ArcGIS Server切图的离线瓦片数据
         * @param viewer
         * @param layerId
         */
        unLoadTileMapService: function (viewer, layerId) {
            if (this.hashMap.containsKey(layerId)) {
                viewer.imageryLayers.remove(this.hashMap.get(layerId), true);
                this.hashMap.removeAtKey(layerId);
            }
        },
        /**
         * 加载GeoServer发布的矢量瓦片服务
         * 调用地址示例：http://localhost:8088/geoserver/gwc/service/wmts?REQUEST=GetTile&SERVICE=WMTS&VERSION=1.0.0&LAYER=MyVectorSpace:TaiZhou&STYLE=&TILEMATRIX=EPSG:4326:14&TILEMATRIXSET=EPSG:4326&FORMAT=application/json;type=geojson&TILECOL=27297&TILEROW=5255
         * 调用方式：
         * //globeUtil.layer.loadVectorTileMapService(conf.globe.instance, 'http://localhost:8088/geoserver/gwc/service/wmts', 'tz-layer-grid-id', 'MyVectorSpace:tz-qybj', new Cesium.ColorGeometryInstanceAttribute(0, 1, 1, 1), conf.sys.boundRectangle4TZ, null, null, true, 0, 17);
         * @param viewer 三维场景视图实例
         * @param layerId 矢量图层id
         * @param name 矢量图层名，若图层设定为''或null，则默认采用图层名
         * @param mapRootUrl 矢量服务地址
         * @param color 要素渲染颜色
         * @param boundRectangle 图层加载范围
         * @param tilingScheme 切片方案
         * @param epsg 特定的椭球体、单位、地理坐标系或投影坐标系等信息
         * @param allowPicking 要素是否允许选取
         * @param minLevel 最小级别
         * @param maxLevel 最大级别
         * @returns {GeoJSONVectorTileProvider}
         */
        loadVectorTileMapService: function (viewer, mapRootUrl, layerId, name, color, boundRectangle, tilingScheme, epsg, allowPicking, minLevel, maxLevel) {
            const provider = new GeoJSONVectorTileProvider({
                Cesium: Cesium, //固定参数
                url: mapRootUrl,
                urlParams: {
                    REQUEST: 'GetTile',
                    SERVICE: 'WMTS',
                    VERSION: '1.0.0',
                    LAYER: name,
                    STYLE: '',
                    FORMAT: 'application/json;type=geojson',
                    TILEMATRIXSET: epsg || 'EPSG:4326',
                    TILEMATRIX: epsg || 'EPSG:4326:{z}',
                    TILECOL: '{x}',
                    TILEROW: '{y}'
                },
                color: color,
                radius: 24,
                allowPicking: allowPicking,
                upperLevelLimit: maxLevel || 19,
                lowerLevelLimit: minLevel || 0,
                tilingScheme: tilingScheme || new Cesium.WebMercatorTilingScheme({
                    ellipsoid: Cesium.Ellipsoid.WGS84
                }),
                rectangle: boundRectangle || Cesium.Rectangle.MAX_VALUE
            });

            // 添加至地图并缓存图层实例引用
            provider.addTo(viewer);
            this.hashMap.add(layerId, provider);
            return provider;
        },
        loadBuildingsMapService: function (viewer, mapRootUrl, layerId, name, color, boundRectangle, tilingScheme, epsg, allowPicking, minLevel, maxLevel) {
            const provider = new GeoJSONVectorTileBuildingProvider({
                Cesium: Cesium, //固定参数
                url: mapRootUrl,
                urlParams: {
                    REQUEST: 'GetTile',
                    SERVICE: 'WMTS',
                    VERSION: '1.0.0',
                    LAYER: name,
                    STYLE: '',
                    FORMAT: 'application/json;type=geojson',
                    TILEMATRIXSET: epsg || 'EPSG:4326',
                    TILEMATRIX: epsg || 'EPSG:4326:{z}',
                    TILECOL: '{x}',
                    TILEROW: '{y}'
                },
                color: color,
                radius: 24,
                allowPicking: allowPicking,
                upperLevelLimit: maxLevel || 19,
                lowerLevelLimit: minLevel || 0,
                tilingScheme: tilingScheme || new Cesium.WebMercatorTilingScheme({
                    ellipsoid: Cesium.Ellipsoid.WGS84
                }),
                rectangle: boundRectangle || Cesium.Rectangle.MAX_VALUE
            });

            viewer.camera.flyTo({
                destination: boundRectangle
            });

            // 添加至地图并缓存图层实例引用
            provider.addTo(viewer);
            this.hashMap.add(layerId, provider);
            return provider;
        },
        loadVectorTileMapServiceEx: function (viewer, mapRootUrl, layerId, name, color, boundRectangle, tilingScheme, epsg, minLevel, maxLevel) {
            const provider = new GeoJSONVectorTileProviderEx({
                Cesium: Cesium, //固定参数
                url: mapRootUrl,
                urlParams: {
                    REQUEST: 'GetTile',
                    SERVICE: 'WMTS',
                    VERSION: '1.0.0',
                    LAYER: name,
                    STYLE: '',
                    FORMAT: 'application/json;type=geojson',
                    TILEMATRIXSET: epsg || 'EPSG:4326',
                    TILEMATRIX: epsg || 'EPSG:4326:{z}',
                    TILECOL: '{x}',
                    TILEROW: '{y}'
                },
                color: color,
                radius: 10,
                upperLevelLimit: maxLevel || 19,
                lowerLevelLimit: minLevel || 0,
                rectangle: boundRectangle || Cesium.Rectangle.MAX_VALUE,
                tilingScheme: tilingScheme
            });

            // 添加至地图并缓存图层实例引用
            provider.addTo(viewer);
            //this.hashMap.add(layerId, provider);
            return provider;
        },
        /**
         * 卸载GeoServer发布的矢量瓦片服务
         * @param viewer
         * @param layerId
         */
        unLoadVectorTileMapService: function (viewer, layerId) {
            if (this.hashMap.containsKey(layerId)) {
                //移除矢量切片图层实例
                this.hashMap.get(layerId).remove();
                this.hashMap.removeAtKey(layerId);
            }
        },
        /**GeoServer发布的WMTS服务
         * 代用方式：
         * globeUtil.layer.loadWMTSMapService(conf.globe.instance, 'tz-wmts-layer-id', 'http://localhost:8088/geoserver/gwc/service/wmts', null, 'MyVectorSpace:json4geom', conf.sys.boundRectangle4TZ, null, 16, 'EPSG:4326');
         * @param viewer
         * @param layerId
         * @param mapRootUrl
         * @param boundRectangle
         * @param tilingScheme
         * @param maxLevel
         */
        loadWMTSMapService: function (viewer, layerId, mapRootUrl, style, layerName, boundRectangle, tilingScheme, maxLevel, epsg) {
            let tls = [];
            for (let i = 0; i <= maxLevel; i++) {
                tls.push(epsg + ':' + i);
            }

            let wmtsImageryProvider = new Cesium.WebMapTileServiceImageryProvider({
                url: mapRootUrl,
                layer: layerName,
                style: style || '',
                tilingScheme: tilingScheme || new Cesium.GeographicTilingScheme({
                    ellipsoid: Cesium.Ellipsoid.WGS84
                }),
                rectangle: boundRectangle || Cesium.Rectangle.MAX_VALUE,
                format: 'image/png',
                tileMatrixSetID: epsg,
                tileMatrixLabels: tls,
                maximumLevel: maxLevel
            });
            let layer = viewer.imageryLayers.addImageryProvider(wmtsImageryProvider);
            this.hashMap.add(layerId, layer);
            return layer;
        },
        /**
         * 卸载GeoServer发布的WMTS服务
         * @param viewer
         * @param layerId
         */
        unloadWMTSMapService: function (viewer, layerId) {
            if (this.hashMap.containsKey(layerId)) {
                viewer.imageryLayers.remove(this.hashMap.get(layerId), true);
                this.hashMap.removeAtKey(layerId);
            }
        }
    }
});

/**
 * 三维工具类
 * @type {acsweb.util.GlobeUtil}
 */
let globeUtil = new acsweb.util.GlobeUtil();
//字段映射初始化
//globeUtil.json.init();

//用于生成uuid
function S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
}

function guid() {
    return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4());
}

//进制转换函数
function numConvert(decimal, format) {
    let zero = '00000000';
    if (format) {
        zero = format;
        let tmp0 = zero.length - decimal.toString().length;
        return zero.substr(0, tmp0) + decimal;
    } else {
        let hex = decimal.toString(16);
        let tmp = zero.length - hex.toString().length;
        return zero.substr(0, tmp) + hex;
    }
}


