/**
 * Created by LBM on 2018/10/08.
 */
const webEnvironment = false;//内网为true,外网为false
Ext.define('acsweb.AppConfig', {
    /*serviceUrl: webEnvironment ? 'http://10.1.4.22:8098/acsserver/' : 'http://weixin.piesat.cn/acsserver/',
    // GeoServer站点地址
    mapServerUrl: webEnvironment ? 'http://10.1.4.22:8088/geoserver/web/' : 'http://weixin.piesat.cn/geoserver/web/',
    //GeoServer服务地址根路径
    geoMapRootUrl: webEnvironment ? 'http://10.1.4.22:8088/geoserver/gwc/service/wmts' : 'http://weixin.piesat.cn/geoserver/gwc/service/wmts',*/
    serviceUrl: webEnvironment ? 'http://localhost:8080/acsserver/' : 'http://114.116.231.97:8080/acsserver/',
    //GeoServer站点地址
    mapServerUrl: webEnvironment ? 'http://114.116.231.97:7080/geoserver/web/' : 'http://114.116.231.97:7080/geoserver/web/',
    //GeoServer服务地址根路径
    geoMapRootUrl: webEnvironment ? 'http://114.116.231.97:7080/geoserver/gwc/service/wmts' : 'http://114.116.231.97:7080/geoserver/gwc/service/wmts',
    //行政区划边界矢量服务
    regionLayerName: 'MyVectorSpace:CHINA_province_region2000',
    //全国瓦片加载范围
    chinaBoundRectangle: Cesium.Rectangle.fromDegrees(73.49896200049596, 3.8338430000544577, 135.08738700011008, 53.56160200003234),
    //WGS84大地坐标系
    tilingScheme: new Cesium.WebMercatorTilingScheme({
        ellipsoid: Cesium.Ellipsoid.WGS84
    }),
    traceTimeGap: 1000,//单位毫秒
    tdtToken: '2f3eb03445a8c299976d55341be55f96',
    title: "ACS",
    /*说明：
    * 菜单只允许一项pressed属性为true
    * */
    menus: [
        {
            text: '工程管理',
            iconCls: 'cloud icon-project app-menu-cls',
            pressed: true,
            action: 'projectmanager'
        },
        {
            text: '接口管理',
            iconCls: 'cloud icon-interface app-menu-cls',
            pressed: false,
            action: 'entitymanager'
        },
        {
            text: '代码管理',
            iconCls: 'cloud icon-schema app-menu-cls',
            pressed: false,
            action: 'versionmanager'
        }
        /*,{
            text: '数据入库',
            iconCls: 'cloud icon-import app-menu-cls',
            pressed: false,
            action: 'dataprocess'
        },
        {
            text: '数据发布',
            iconCls: 'cloud icon-map app-menu-cls',
            pressed: false,
            action: 'datapublish'
        },
        {
            text: '服务注册',
            iconCls: 'cloud icon-cubedata app-menu-cls',
            pressed: false,
            action: 'mapregedit'
        },
        {
            text: '轨迹预测',
            iconCls: 'cloud icon-sate app-menu-cls',
            pressed: false,
            action: 'satellitex'
        }*/
    ],
    //数据库类型列表
    dbTypeList: [
        {name: 'Oracle数据库', type: 'Oracle', value: 'oracle.jdbc.driver.OracleDriver'},
        {name: 'MySQL数据库', type: 'MySQL', value: 'com.mysql.jdbc.Driver'},
        {name: 'PostgreSQL数据库', type: 'PostgreSQL', value: 'org.postgresql.Driver'}
    ],
    //数据库配置
    dbConfig: null,
    //工程配置
    projectConfig: null,
    //库表信息
    dbTables: null,
    //工程目录结构信息
    projectCatalogData: null,
    //实体代码文件列表
    projectCodeData: null,
    //操作符类型
    operatorTypes: [
        {name: '==', value: '=='},
        {name: '=', value: '='},
        {name: '!=', value: '!='},
        {name: '<>', value: '<>'},
        {name: '>', value: '>'},
        {name: '<', value: '<'},
        {name: '>=', value: '>='},
        {name: '<=', value: '<='},
        {name: '!<', value: '!<'},
        {name: '!>', value: '!>'},
        {name: 'in', value: 'in'},
        {name: 'not in', value: 'not in'},
        {name: 'like', value: 'like'}
    ],
    //地图相关
    map: {
        //公用地图组
        layerGroup: null,
        //初始位置
        location: {
            X: 116.33,
            Y: 39.83
        },
        //缩放级别
        zoom: 8
    },
    //场景相关
    globe: {
        //实例
        instance: null,
        minLevel: 0,
        maxLevel: 17,
        //latitude: 32.1855372812, longitude: 120.151768967,
        //初始位置
        location: {
            X: 116.33,
            Y: 39.93,
            H: 150000
        },
        //初始范围:Rectangle(west, south, east, north)
        bound: {
            W: 73.49896200049596,
            S: 3.8338430000544577,
            E: 135.08738700011008,
            N: 53.56160200003234
        },
        //父容器ID
        parentContainerId: 'globeParentContainerId',
        //容器ID
        containerId: 'globeContainerId'
    }
});

let conf = new acsweb.AppConfig();

/**
 * Created by winnerlbm on 2019/4/9.
 */
//天地图URL配置
let TDTURL_CONFIG = {
    TDT_IMG_C: "http://114.116.231.97:7080/proxy/{s}/img_c/wmts?service=wmts&request=GetTile&version=1.0.0" +
        "&LAYER=img&tileMatrixSet=c&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}" +
        "&style=default&format=tiles&tk=" + conf.tdtToken  //在线天地图影像服务地址(经纬度)
    , TDT_VEC_C: "http://114.116.231.97:7080/proxy/{s}/vec_c/wmts?service=wmts&request=GetTile&version=1.0.0" +
        "&LAYER=vec&tileMatrixSet=c&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}" +
        "&style=default&format=tiles&tk=" + conf.tdtToken   //在线天地图矢量地图服务(经纬度)
    , TDT_TER_C: "http://114.116.231.97:7080/proxy/{s}/ter_c/wmts?service=wmts&request=GetTile&version=1.0.0" +
        "&LAYER=ter&tileMatrixSet=c&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}" +
        "&style=default&format=tiles&tk=" + conf.tdtToken   //在线天地图地形地图服务(经纬度)
    , TDT_CIA_C: "http://114.116.231.97:7080/proxy/{s}/cia_c/wmts?service=wmts&request=GetTile&version=1.0.0" +
        "&LAYER=cia&tileMatrixSet=c&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}" +
        "&style=default&format=tiles&tk=" + conf.tdtToken      //在线天地图影像中文标记服务(经纬度)
    , TDT_CVA_C: "http://114.116.231.97:7080/proxy/{s}/cva_c/wmts?service=wmts&request=GetTile&version=1.0.0" +
        "&LAYER=cva&tileMatrixSet=c&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}" +
        "&style=default&format=tiles&tk=" + conf.tdtToken      //在线天地图矢量中文标记服务(经纬度)
    , TDT_CTA_C: "http://114.116.231.97:7080/proxy/{s}/cta_c/wmts?service=wmts&request=GetTile&version=1.0.0" +
        "&LAYER=cta&tileMatrixSet=c&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}" +
        "&style=default&format=tiles&tk=" + conf.tdtToken      //在线天地图地形中文标记服务(经纬度)
};
