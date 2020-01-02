/**
 * 提供 GeoJSON 矢量切片渲染服务
 *
 * @param {Object} options 选项
 * @param {Object} Cesium Cesium 对象
 * @param {String} options.url 瓦片服务 URL
 * @param {Object} [options.urlParams] 瓦片服务参数
 * <ul>
 *   <li>{x} 瓦片 X 坐标</li>
 *   <li>{y} 瓦片 Y 坐标 </li>
 *   <li>{z} 瓦片等级</li>
 * </ul>
 * @param {Rectangle} options.Rectangle 瓦片数据范围
 * @param {TilingScheme} [options.tilingScheme=Cesium.GeoGraphicTilingScheme]
 * @param {Number} [options.minimumLevel=1] 瓦片数据的最低等级
 * @param {Number} [options.lowerLevelLimit=1] 在特定视角下允许的最低层级(低于此等级不渲染)
 * @param {Number} [options.maximumLevel=19] 瓦片数据的最高等级
 * @param {Number} [options.upperLevelLimit] 在特定视角下允许的最高层级(高于此等级将向下合并)
 *
 * @example
 * // 创建 provider
 * const provider = new GeoJSONVectorTileBuildingProvider({
 *   url: 'http://localhost:8080/geoserver/gwc/service/wmts',
 *   urlParams: {
 *      REQUEST: 'GetTile',
 *      SERVICE: 'WMTS',
 *      VERSION: '1.0.0',
 *      LAYER: 'layername',
 *      STYLE: '',
 *      FORMAT: 'application/json',
 *      TILEMATRIXSET: 'EPSG:4326',
 *      TILEMATRIX: 'EPSG:4326:{z}',
 *      TILECOL: '{x}',
 *      TILEROW: '{y}'
 *   },
 *   upperLevelLimit: 15,
 *   lowerLevelLimit: 10,
 *   rectangle: Cesium.Rectangle.fromDegrees(70, 4, 120, 20)
 * })
 *
 * // 添加至地图
 * provider.addTo(viewer)
 *
 * // 移除 provider
 * provider.remove()
 *
 * // 过滤 id 属性
 * provider.filterBy('id', ['xxxxxxxx'])
 *
 * // 移除属性过滤
 * provider.removeFilter()
 *
 * @changelog
 * [feat: 允许设置可选的最高层级以优化瓦片加载]
 * [feat: 缓存一上视角位置的瓦片数据]
 * [chore: 不渲染距离过远的瓦片]
 * [feat: 允许过滤数据]
 * [feat: 模型物高度适应地形]
 * [chore: 坐标转换(WGS84 -> GCJ02)]
 * [feat: 添加 MultiPolygon 支持]
 * [initial support]
 *
 * @author LBM 2019-05-23
 */
function GeoJSONVectorTileBuildingProvider(options) {
    //数据缓存
    this._hashMap = new HashMap();
    // Cesium.Viewer 实例
    this._viewer = null;

    // Cesium 对象
    this._Cesium = options.Cesium;

    // 矢量切片服务接口
    this._url = options.url;

    // 矢量切片服务接口请求参数
    this._urlParams = options.urlParams;

    //layer id
    this._layerId = options.layerId || this._urlParams.LAYER;

    // 矢量切片服务 URL 模板
    this._urlTemplate = this._url + '?' + this._serialize(this._urlParams);

    // 矢量切片服务数据范围
    this._rectangle = options.rectangle;

    //矢量（点/线/面通用）绘制颜色，默认为红色
    /*color: Cesium.ColorGeometryInstanceAttribute.fromColor(Cesium.Color.fromRandom({
        alpha: 0.5
    }))*/
    this._color = options.color || new this._Cesium.ColorGeometryInstanceAttribute(1, 0, 0, 1);

    //若绘制线段，默认线宽为1
    this._lineWidth = options.lineWidth || 1;

    //若绘制点/圆，默认点半径为16px
    this._radius = options.radius || 16;

    //要素是否可以被点选
    this._allowPicking = options.allowPicking || false;

    // 最大视图等级
    this._maximumLevel = options.maximumLevel || 19;

    // 最小视图等级
    this._minimumLevel = options.minimumLevel || 1;

    // Cesium.TilingScheme 实例
    this._tilingScheme = options.tilingScheme || new this._Cesium.GeographicTilingScheme();

    // 瓦片范围，以 level 索引
    this._tileRangeByLevel = {};

    // 摄像机 moveEnd 事件时间戳
    this._moveEndTimestamp = null;

    // provider 是否已销毁
    this._isRemoved = false;

    // 摄像机是否正在移动
    this._isCameraMoving = false;

    // 将被过滤的属性和值
    this._propertyToBeFiltered = null;
    this._valuesToBeFiltered = null;

    // 瓦片 primitive，以 tile key 索引
    this._primitiveByTile = {};

    // 最新视角下的瓦片标识列表
    this._tileKeys = [];

    // 瓦片层级限制
    this._lowerLevelLimit = options.lowerLevelLimit || 1;
    this._upperLevelLimit = options.upperLevelLimit || null;

    this._cameraMoveEndHandler = this._cameraMoveEndHandler.bind(this);
    this._cameraMoveStartHandler = this._cameraMoveStartHandler.bind(this);

    // 计算瓦片范围
    const west = this._radianToDegree(this._rectangle.west);
    const south = this._radianToDegree(this._rectangle.south);
    const east = this._radianToDegree(this._rectangle.east);
    const north = this._radianToDegree(this._rectangle.north);

    for (let z = this._minimumLevel; z <= this._maximumLevel; z++) {
        const bottomLeftTile = this._lonLatToTileInWGS84([west, south], z);
        const topRightTile = this._lonLatToTileInWGS84([east, north], z);
        const minRow = Math.min(bottomLeftTile[0], topRightTile[0]);
        const maxRow = Math.max(bottomLeftTile[0], topRightTile[0]);
        const minCol = Math.min(bottomLeftTile[1], topRightTile[1]);
        const maxCol = Math.max(bottomLeftTile[1], topRightTile[1]);

        this._tileRangeByLevel[z] = {minCol, maxCol, minRow, maxRow}
    }

    this._tileRangeByLevel[0] = {minCol: 0, maxCol: 0, minRow: 0, maxRow: 0};

    this._floorColors = {
        10: this._Cesium.ColorGeometryInstanceAttribute.fromColor(new Cesium.Color.fromCssColorString("#B3E5FC").withAlpha(1)),
        15: this._Cesium.ColorGeometryInstanceAttribute.fromColor(new Cesium.Color.fromCssColorString("#81D4FA").withAlpha(1)),
        20: this._Cesium.ColorGeometryInstanceAttribute.fromColor(new Cesium.Color.fromCssColorString("#4FC3F7").withAlpha(1)),
        25: this._Cesium.ColorGeometryInstanceAttribute.fromColor(new Cesium.Color.fromCssColorString("#00B0FF").withAlpha(1)),
        30: this._Cesium.ColorGeometryInstanceAttribute.fromColor(new Cesium.Color.fromCssColorString("#0091EA").withAlpha(1)),
        35: this._Cesium.ColorGeometryInstanceAttribute.fromColor(new Cesium.Color.fromCssColorString("#039BE5").withAlpha(1)),
        40: this._Cesium.ColorGeometryInstanceAttribute.fromColor(new Cesium.Color.fromCssColorString("#0288D1").withAlpha(1)),
        300: this._Cesium.ColorGeometryInstanceAttribute.fromColor(new Cesium.Color.fromCssColorString("#0277BD").withAlpha(1))
    };
}

GeoJSONVectorTileBuildingProvider.prototype = {
    constructor: GeoJSONVectorTileBuildingProvider,

    /**
     * 序列化参数对象
     *
     * @param {Object} params 参数对象
     * @returns {String} 序列化字符串
     */
    _serialize: function (params) {
        const queryArray = [];
        const encode = window.encodeURIComponent;

        for (let key in params) {
            const value = params[key];

            if (typeof value === 'object') {
                queryArray.push(encode(key) + '=' + encode(JSON.stringify(value)));
            } else {
                queryArray.push(encode(key) + '=' + encode(params[key]));
            }
        }

        return queryArray.join('&');
    },

    /**
     * 模拟 fetch 接口
     *
     * @param {String} url 请求 URL
     * @param {Object} [options] 选项
     * @returns {Promise} 处理请求结果的 Promise
     *
     */
    _fetch: function (url, options = {}) {
        const promise = new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();

            xhr.open(options.method || 'GET', url);

            if (options.headers) {
                for (let key in options.headers) {
                    xhr.setRequestHeader(key, options.headers[key]);
                }
            }

            xhr.onload = () => {
                if (xhr.readyState === 4 && xhr.status === 200) {
                    try {
                        const json = JSON.parse(xhr.responseText);

                        resolve(json)
                    } catch (e) {
                        reject(new Error('INVALID RESPONSE'));
                    }
                }
            };

            xhr.onerror = (e) => {
                reject(e);
            };

            xhr.onloadend = (e) => {
                if (e.target.status !== 200) {
                    reject(new Error(`[${e.target.status}]request failed: ${url}`));
                }
            };

            xhr.send(options.body || null);
        });

        return promise;
    },

    /**
     * 通过经纬度获取瓦片 X、Y 值 (WGS84)
     *
     * @param {Number[]} lonLat 经纬度
     * @param {Number} level 地图缩放等级
     * @returns {Number[]} 瓦片的 X、Y 值
     */
    _lonLatToTileInWGS84: function (lonLat, level) {
        const [lon, lat] = lonLat;
        const tileX = Math.floor(Math.pow(2, level) * (90 - lat) / 180);
        const tileY = Math.floor(Math.pow(2, level) * (180 + lon) / 180);

        return [tileX, tileY];
    },

    /**
     * 弧度转角度
     *
     * @param {Number} radian 弧度值
     * @returns {Number} 角度值
     */
    _radianToDegree: function (radian) {
        return radian / Math.PI * 180;
    },

    /**
     * 判断目标瓦片是否在数据范围内
     *
     * @param {QuadtreeTile} tile 瓦片对象
     * @returns {Boolean} 瓦片是否在数据范围内
     */
    _isTileInRange: function (tile) {
        try {
            const {minCol, maxCol, minRow, maxRow} = this._tileRangeByLevel[tile.level];
            return tile.x >= minCol && tile.x <= maxCol && tile.y >= minRow && tile.y <= maxRow;
        } catch (e) {
            return false;
        }
    },

    /**
     * 摄像机 moveStart 事件处理程序
     */
    _cameraMoveStartHandler: function () {
        this._isCameraMoving = true;
    },

    /**
     * 摄像机 moveEnd 事件处理程序
     */
    _cameraMoveEndHandler: function () {
        const timestamp = Date.now();
        let tilesToRender = this._viewer.scene.globe._surface._tilesToRender;
        const levels = tilesToRender.map(tile => tile.level);
        const minLevel = Math.min(...levels);
        const maxLevel = Math.max(...levels);

        if (maxLevel < this._lowerLevelLimit) {
            tilesToRender = [];
        }

        if (maxLevel - minLevel > 2) {
            tilesToRender = tilesToRender.filter((tile) => {
                return tile.level >= maxLevel - 2;
            })
        }

        tilesToRender.sort((val1, val2) => -(val1.level - val2.level));
        tilesToRender = tilesToRender.filter(tile => this._isTileInRange(tile));

        if (this._upperLevelLimit) {
            tilesToRender = this._refineTiles(tilesToRender, this._upperLevelLimit);
        }

        this._isCameraMoving = false;
        this._moveEndTimestamp = timestamp;
        this._tileKeys = [];

        tilesToRender.forEach((tile) => {
            //用于存放单张矢量瓦片关联的要素的ID
            const tileIds = [];

            const tileKey = 'z' + tile.level + 'x' + tile.x + 'y' + tile.y;

            this._tileKeys.push(tileKey);

            if (this._primitiveByTile[tileKey]) {
                return;
            }

            const tileUrl = this._urlTemplate
                .replace(window.encodeURIComponent('{x}'), tile.x)
                .replace(window.encodeURIComponent('{y}'), tile.y)
                .replace(window.encodeURIComponent('{z}'), tile.level);

            this._fetch(tileUrl).then((json) => {
                if (this._isRemoved) throw new Error('DISCARD');
                if (this._isCameraMoving) throw new Error('DISCARD');
                if (timestamp !== this._moveEndTimestamp) throw new Error('DISCARD');

                const geomInstances = [];
                //用于存储要素几何类型
                let geometryType;
                json.features.forEach((feature) => {
                    if (this._propertyToBeFiltered) {
                        if (Array.isArray(this._valuesToBeFiltered)) {
                            const value = feature.properties[this._propertyToBeFiltered];

                            if (this._valuesToBeFiltered.includes(value)) return;
                        }
                    }

                    //判断要素类型
                    geometryType = feature.geometry.type;
                    //获取要素ID
                    const fid = feature.id;
                    //缓存单个要素属性
                    if (this._allowPicking) {
                        this._hashMap.set(fid, {
                            type: geometryType,
                            properties: feature.properties
                        });
                        tileIds.push(fid);
                    }

                    let floorNum = feature.properties['FLOOR'];
                    let buildColor = this._color;
                    for (let fc in this._floorColors) {
                        if (floorNum < parseInt(fc)) {
                            buildColor = this._floorColors[fc];
                            break;
                        }
                    }

                    if (geometryType.indexOf('Polygon') > -1) {
                        //面
                        const coordinates = [];

                        feature.geometry.coordinates.forEach((coords, index) => {
                            coordinates.push([]);
                            coords.forEach((coordinate) => {
                                if (coordinate.length > 2) {
                                    coordinate.forEach((coord) => {
                                        if (coord.length >= 2) {
                                            coordinates[index].push(...coord);
                                        }
                                    })
                                } else {
                                    coordinates[index].push(...coordinate);
                                }
                            });

                            if (coordinates[index] && coordinates[index].length > 0) {
                                const polygonHierarchy = new this._Cesium.PolygonHierarchy(this._Cesium.Cartesian3.fromDegreesArray(coordinates[index]));
                                const center = this._Cesium.BoundingSphere.fromPoints(polygonHierarchy.positions).center
                                const height = this._viewer.scene.globe.getHeight(this._Cesium.Cartographic.fromCartesian(center)) || 0;

                                const geometry = new this._Cesium.PolygonGeometry({
                                    polygonHierarchy: polygonHierarchy,
                                    height: height,
                                    vertexFormat: this._Cesium.PerInstanceColorAppearance.VERTEX_FORMAT,
                                    extrudedHeight: 5 * floorNum
                                    //extrudedHeight: feature.properties.height + height
                                });

                                const geomInstance = new this._Cesium.GeometryInstance({
                                    geometry: geometry,
                                    id: fid,
                                    attributes: {
                                        color: buildColor
                                    }
                                });

                                geomInstances.push(geomInstance);
                            }
                        })
                    } else if (geometryType.indexOf('LineString') === 0) {
                        //LineString
                        const coordinates = [];

                        feature.geometry.coordinates.forEach((coords) => {
                            coordinates.push(...coords);
                        });

                        if (coordinates && coordinates.length > 0) {
                            const geometry = new this._Cesium.PolylineGeometry({
                                positions: this._Cesium.Cartesian3.fromDegreesArray(coordinates),
                                width: this._lineWidth,
                                vertexFormat: this._Cesium.PolylineColorAppearance.VERTEX_FORMAT
                            });

                            const geomInstance = new this._Cesium.GeometryInstance({
                                geometry: geometry,
                                id: fid,
                                attributes: {
                                    color: this._color
                                }
                            });
                            geomInstances.push(geomInstance);
                        }
                    } else if (geometryType.indexOf('LineString') > 0) {
                        //MultiLineString
                        const coordinates = [];
                        feature.geometry.coordinates.forEach((coords, index) => {
                            coordinates.push([]);
                            coords.forEach((coordinate) => {
                                coordinates[index].push(...coordinate);
                            });

                            if (coordinates[index] && coordinates[index].length > 0) {
                                const geometry = new this._Cesium.PolylineGeometry({
                                    positions: this._Cesium.Cartesian3.fromDegreesArray(coordinates[index]),
                                    width: this._lineWidth,
                                    vertexFormat: this._Cesium.PolylineColorAppearance.VERTEX_FORMAT
                                });

                                const geomInstance = new this._Cesium.GeometryInstance({
                                    geometry: geometry,
                                    id: fid,
                                    attributes: {
                                        color: this._color
                                    }
                                });

                                geomInstances.push(geomInstance);
                            }
                        })
                    } else if (geometryType.indexOf('Point') > -1) {
                        //点
                        const coordinates = feature.geometry.coordinates;
                        if (coordinates && coordinates.length > 0) {
                            const geometry = new this._Cesium.CircleGeometry({
                                center: this._Cesium.Cartesian3.fromDegrees(coordinates[0], coordinates[1]),
                                radius: this._radius,
                                vertexFormat: this._Cesium.PerInstanceColorAppearance.VERTEX_FORMAT
                            });

                            const geomInstance = new this._Cesium.GeometryInstance({
                                geometry: geometry,
                                id: fid,
                                attributes: {
                                    color: this._color
                                }
                            });

                            geomInstances.push(geomInstance);
                        }
                    }
                });

                //渲染到场景
                if (geomInstances && geomInstances.length > 0) {
                    if (geometryType) {
                        if (geometryType.indexOf('Polygon') > -1 || geometryType.indexOf('Point') > -1) {
                            const primitive = new this._Cesium.Primitive({
                                allowPicking: this._allowPicking,
                                appearance: new this._Cesium.PerInstanceColorAppearance({
                                    translucent: false
                                }),
                                geometryInstances: geomInstances
                            });

                            this._viewer.scene.primitives.add(primitive);
                            //要素置底
                            this._viewer.scene.primitives.lowerToBottom(primitive);

                            this._primitiveByTile[tileKey] = primitive;
                            if (this._allowPicking) {
                                //缓存tilekey and ids
                                this._hashMap.set(tileKey, tileIds);
                            }
                        } else if (geometryType.indexOf('LineString') > -1) {
                            const primitive = new this._Cesium.Primitive({
                                allowPicking: this._allowPicking,
                                appearance: new this._Cesium.PolylineColorAppearance({
                                    translucent: false
                                }),
                                geometryInstances: geomInstances
                            });

                            this._viewer.scene.primitives.add(primitive);
                            //要素置底
                            this._viewer.scene.primitives.lowerToBottom(primitive);

                            this._primitiveByTile[tileKey] = primitive;
                            if (this._allowPicking) {
                                //缓存tilekey and ids
                                this._hashMap.set(tileKey, tileIds);
                            }
                        }
                    }
                }
            }).catch((ex) => {
                if (ex.message !== 'DISCARD') {
                }
            })
        });

        this._removeObsoletePrimitives(this._primitiveByTile, this._tileKeys);
    },

    /**
     * 通过拆分、合并优化瓦片下载与渲染
     *
     * @param {Object[]} tiles 待处理的瓦片列表
     * @param {Number} targetLevel 目标优化层级
     * @returns {Object[]} 优化后的瓦片列表
     */
    _refineTiles: function (tiles, targetLevel) {
        const newTiles = [];
        const tileKeys = [];

        tiles.forEach((tile) => {
            const tileKey = 'Z' + tile.level + 'X' + tile.x + 'Y' + tile.y;

            if (tile.level < targetLevel - 1) {
                if (!tileKeys.includes(tileKey)) {
                    newTiles.push(tile);
                    tileKeys.push(tileKey)
                }
            } else if (tile.level === targetLevel - 1) {
                for (let i = 0; i < 2; i++) {
                    for (let j = 0; j < 2; j++) {
                        const tileKey = 'Z' + (tile.level + 1) + 'X' + (tile.x * 2 + i) + 'Y' + (tile.y * 2 + j);

                        if (!tileKeys.includes(tileKey)) {
                            newTiles.push({
                                x: tile.x * 2 + i,
                                y: tile.y * 2 + j,
                                level: tile.level + 1
                            });
                            tileKeys.push(tileKey)
                        }
                    }
                }
            } else {
                const levelDiff = tile.level - targetLevel;

                if (levelDiff === 0) {
                    if (!tileKeys.includes(tileKey)) {
                        newTiles.push(tile);
                        tileKeys.push(tileKey)
                    }
                } else {
                    const factor = levelDiff * 2;
                    const targetX = Math.floor(tile.x / factor);
                    const targetY = Math.floor(tile.y / factor);
                    const tileKey = 'Z' + targetLevel + 'X' + targetX + 'Y' + targetY;

                    if (!tileKeys.includes(tileKey)) {
                        newTiles.push({x: targetX, y: targetY, level: targetLevel});
                        tileKeys.push(tileKey)
                    }
                }
            }
        });

        return newTiles
    },

    /**
     * 移除过期的瓦片及关联的要素
     *
     * @param {Object} primitiveByTile 瓦片 primitive，以 tileKey 索引
     * @param {String[]} tileKeys 当前有效的瓦片 keys
     */
    _removeObsoletePrimitives: function (primitiveByTile, tileKeys) {
        for (let tileKey in primitiveByTile) {
            if (!tileKeys.includes(tileKey)) {
                const removed = this._viewer.scene.primitives.remove(primitiveByTile[tileKey]);

                if (removed) {
                    delete primitiveByTile[tileKey];

                    //清除关联的要素信息
                    if (this._hashMap.has(tileKey)) {
                        const tileIds = this._hashMap.get(tileKey);
                        if (tileIds && tileIds.length > 0) {
                            for (let i = 0; i < tileIds.length; i++) {
                                this._hashMap.remove(tileIds[i]);
                            }
                        }
                        this._hashMap.remove(tileKey);
                    }
                }
            }
        }
    },

    findById: function (id) {
        if (this._hashMap.has(id)) {
            return this._hashMap.get(id);
        }
    },

    /**
     * 将 provider 添加到指定 viewer
     *
     * @param {Cesium.Viewer} viewer 目标 viewer
     */
    addTo: function (viewer) {
        let GeoJsonVectorTileCollection = 'GeoJson-Vector-Tile-Collection';
        this._viewer = viewer;
        this._isRemoved = false;
        this._viewer.camera.moveEnd.addEventListener(this._cameraMoveEndHandler);
        this._viewer.camera.moveStart.addEventListener(this._cameraMoveStartHandler);
        this._cameraMoveEndHandler()

        //追加矢量瓦片数据源
        if (viewer['VtDataSources'] == null) {
            viewer['VtDataSources'] = new HashMap();
            viewer['VtDataSources'].set(GeoJsonVectorTileCollection, []);
        }
        if (viewer.VtDataSources && viewer.VtDataSources.has(GeoJsonVectorTileCollection)) {
            let geojsonDsCol = viewer.VtDataSources.get(GeoJsonVectorTileCollection);
            if (geojsonDsCol.length === 0) {
                geojsonDsCol.push(this);
            } else if (geojsonDsCol.length > 0 && geojsonDsCol.indexOf(this) < 0) {
                geojsonDsCol.push(this);
            }
        }

        console.log('Added，当前瓦片图层总数：' + viewer.VtDataSources.get(GeoJsonVectorTileCollection).length);
    },

    /**
     * 移除 provider
     */
    remove: function () {
        let GeoJsonVectorTileCollection = 'GeoJson-Vector-Tile-Collection';
        if (this._viewer) {
            //删除对应的数据源
            if (this._viewer.VtDataSources && this._viewer.VtDataSources.get(GeoJsonVectorTileCollection).length > 0) {
                let geojsonCol = this._viewer.VtDataSources.get(GeoJsonVectorTileCollection),
                    vtIndex = geojsonCol.indexOf(this);
                if (vtIndex > -1) {
                    if (vtIndex > -1) {
                        delete geojsonCol.splice(vtIndex, 1);

                        console.log('Deleted，当前瓦片图层总数：' + this._viewer.VtDataSources.get(GeoJsonVectorTileCollection).length);
                    }
                }
            }

            for (let tileKey in this._primitiveByTile) {
                this._viewer.scene.primitives.remove(this._primitiveByTile[tileKey]);

                //清除关联的要素信息
                if (this._hashMap.has(tileKey)) {
                    const tileIds = this._hashMap.get(tileKey);
                    if (tileIds && tileIds.length > 0) {
                        for (let i = 0; i < tileIds.length; i++) {
                            this._hashMap.remove(tileIds[i]);
                        }
                    }
                    this._hashMap.remove(tileKey);
                }
            }

            this._primitiveByTile = {};
            this._viewer.camera.moveEnd.removeEventListener(this._cameraMoveEndHandler);
            this._viewer.camera.moveStart.removeEventListener(this._cameraMoveStartHandler);
            this._viewer = null;
            this._isRemoved = true;
        }
    },

    /**
     * provider 是否已移除
     */
    isRemoved: function () {
        return this._isRemoved
    },

    /**
     * 添加属性过滤
     *
     * @param {string} property 要过滤的属性名称
     * @param {any[]} values 要过滤的属性值
     * @param {Cesium.Rectangle} [rectangle] 要过滤的数据范围
     */
    filterBy: function (property, values, rectangle) {
        this._propertyToBeFiltered = property;
        this._valuesToBeFiltered = values;
    },

    /**
     * 移除属性过滤
     */
    removeFilter: function () {
        this._propertyToBeFiltered = null;
        this._valuesToBeFiltered = null;
    }
};
