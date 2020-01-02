/**
 * Created by winnerlbm on 2019/7/15.
 */
Ext.define('acsweb.view.preservice.PreServiceController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.preservice',

    lastSrid: null,
    _map: null,

    /**
     * Called when the view is created
     */
    init: function () {

    },
    afterrenderHandler: function () {
        //this._map = this.initMap('svrContainerId');
    },
    //cesium窗口自适应
    resizeHandler: function (con, width, height, oldWidth, oldHeight, eOpts) {
        let svrContainerDom = Ext.getDom('svrContainerId');
        if (svrContainerDom) {
            svrContainerDom.style.width = width + 'px';
            svrContainerDom.style.height = height + 'px';
        }

        if (this._map) {
            this._map.invalidateSize();
        }
    },
    //----------------地图服务相关-----------------
    initMap: function (mapDiv, epsg) {
        if (epsg.indexOf('3857') > 0) {
            //EPSG:3857
            let normalm = L.tileLayer.chinaProvider('TianDiTu.Normal.Map', {
                    maxZoom: 18,
                    minZoom: 1
                }),
                normala = L.tileLayer.chinaProvider('TianDiTu.Normal.Annotion', {
                    maxZoom: 18,
                    minZoom: 1
                }),
                imgm = L.tileLayer.chinaProvider('TianDiTu.Satellite.Map', {
                    maxZoom: 18,
                    minZoom: 1
                }),
                imga = L.tileLayer.chinaProvider('TianDiTu.Satellite.Annotion', {
                    maxZoom: 18,
                    minZoom: 1
                });

            let baseNormal = L.layerGroup([normalm, normala]),
                baseImage = L.layerGroup([imgm, imga]);

            let baseLayers = {
                "地图": baseNormal,
                "影像": baseImage,
            };

            let map = L.map(mapDiv, {
                crs: L.CRS.EPSG3857,
                center: [conf.map.location.Y, conf.map.location.X],
                zoom: conf.map.zoom,
                layers: [baseNormal],
                zoomControl: false
            });

            L.control.layers(baseLayers).addTo(map);
            L.control.zoom({
                zoomInTitle: '放大',
                zoomOutTitle: '缩小'
            }).addTo(map);

            map.on('baselayerchange ', function () {
                //图层置顶
                if (conf.map.layerGroup) {
                    conf.map.layerGroup.bringToFront();
                }
            });

            map.invalidateSize();
            return map;
        } else if (epsg.indexOf('4326') > 0) {
            //EPSG:4326
            let normalm = L.tileLayer('http://{s}.tianditu.gov.cn/vec_c/wmts?layer=vec&style=default&tilematrixset=c&Service=WMTS&Request=GetTile&Version=1.0.0&Format=tiles&TileMatrix={z}&TileCol={x}&TileRow={y}&tk=' + conf.tdtToken, {
                    subdomains: ['t0', 't1', 't2', 't3', 't4', 't5', 't6', 't7'],
                    maxZoom: 18,
                    minZoom: 1,
                    zoomOffset: 1,
                    tileSize: 256
                }),
                normala = L.tileLayer('http://{s}.tianditu.gov.cn/cva_c/wmts?layer=cva&style=default&tilematrixset=c&Service=WMTS&Request=GetTile&Version=1.0.0&Format=tiles&TileMatrix={z}&TileCol={x}&TileRow={y}&tk=' + conf.tdtToken, {
                    subdomains: ['t0', 't1', 't2', 't3', 't4', 't5', 't6', 't7'],
                    maxZoom: 18,
                    minZoom: 1,
                    zoomOffset: 1,
                    tileSize: 256
                }),
                imgm = L.tileLayer('http://{s}.tianditu.gov.cn/img_c/wmts?layer=img&style=default&tilematrixset=c&Service=WMTS&Request=GetTile&Version=1.0.0&Format=tiles&TileMatrix={z}&TileCol={x}&TileRow={y}&tk=' + conf.tdtToken, {
                    subdomains: ['t0', 't1', 't2', 't3', 't4', 't5', 't6', 't7'],
                    maxZoom: 18,
                    minZoom: 1,
                    zoomOffset: 1,
                    tileSize: 256
                }),
                imga = L.tileLayer('http://{s}.tianditu.gov.cn/cia_c/wmts?layer=cia&style=default&tilematrixset=c&Service=WMTS&Request=GetTile&Version=1.0.0&Format=tiles&TileMatrix={z}&TileCol={x}&TileRow={y}&tk=' + conf.tdtToken, {
                    subdomains: ['t0', 't1', 't2', 't3', 't4', 't5', 't6', 't7'],
                    maxZoom: 18,
                    minZoom: 1,
                    zoomOffset: 1,
                    tileSize: 256
                });

            let baseNormal = L.layerGroup([normalm, normala]),
                baseImage = L.layerGroup([imgm, imga]);

            let baseLayers = {
                "地图": baseNormal,
                "影像": baseImage,
            };

            let map = L.map('svrContainerId', {
                crs: L.CRS.EPSG4326,
                center: [conf.map.location.Y, conf.map.location.X],
                zoom: conf.map.zoom,
                layers: [baseNormal],
                zoomControl: false
            });

            L.control.layers(baseLayers).addTo(map);
            L.control.zoom({
                zoomInTitle: '放大',
                zoomOutTitle: '缩小'
            }).addTo(map);

            map.on('baselayerchange ', function () {
                //图层置顶
                if (conf.map.layerGroup) {
                    conf.map.layerGroup.bringToFront();
                }
            });

            map.invalidateSize();
            return map;
        }
    },
    loadVectorLayer: function (svrName, svrUrl, svrType, svrBounds, svrSrid) {
        if (this.lastSrid == null || this._map == null) {
            this._map = this.initMap('svrContainerId', svrSrid);
        } else if (this.lastSrid) {
            if (this.lastSrid !== svrSrid) {
                if (this._map) {
                    let svrContainerDom = Ext.getDom('svrContainerId');
                    if (svrContainerDom) {
                        svrContainerDom.innerHTML = '';
                    }
                    this._map.remove();
                    this._map = null;
                }
                this._map = this.initMap('svrContainerId', svrSrid);
            }
        }
        this.lastSrid = svrSrid;

        if (this._map) {
            if (conf.map.layerGroup) {
                conf.map.layerGroup.clearLayers();
                conf.map.layerGroup = null;
            }

            conf.map.layerGroup = L.featureGroup([]).addTo(this._map);
            let geoVectorTileOptions = null;
            let geoStyle = {};
            geoStyle[svrName] = {
                color: '#ff00ff',
                fillOpacity: 0.5,
                stroke: true,
                opacity: 0.5,
                radius: 4,
                weight: 1,
                fillColor: '#0000ff',
                fill: true
            };

            if (svrType === 'pbf') {
                geoVectorTileOptions = {
                    rendererFactory: L.canvas.tile,
                    attribution: '© GeoServer',
                    vectorTileLayerStyles: geoStyle,
                    tms: true // 如果是 TMS 方式，则必须开启
                };
            } else if (svrType === 'wmts') {
                geoVectorTileOptions = {
                    rendererFactory: L.canvas.tile,
                    attribution: '© GeoServer',
                    vectorTileLayerStyles: geoStyle
                };
            }
            let vectorLayer = L.vectorGrid.protobuf(svrUrl, geoVectorTileOptions);

            if (svrBounds) {
                this._map.fitBounds(svrBounds);
            }
            conf.map.layerGroup.addLayer(vectorLayer);

            //图层置顶
            conf.map.layerGroup.bringToFront();
        }
    }
});