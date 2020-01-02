/**
 * Created by winnerlbm on 2019/8/5.
 */
Ext.define('acsweb.util.SatelliteUtil', {
    //参数定义
    rad2deg: 180 / Math.PI,
    /* WGS84地球半径，单位：KM */
    EARTH_RADIUS_KM: 6378.137,

    /**
     * 计算足迹圆半径
     * @param altitude 卫星高度
     * @returns {number}
     */
    getRangeCircleRadiusKm: function (altitude) {
        return 0.5 * (12756.33 * Math.acos(this.EARTH_RADIUS_KM
            / (this.EARTH_RADIUS_KM + altitude)));
    },
    /**
     * WGS84，根据定位，距离，方位角计算点
     * @param longitude 经度  单位：°
     * @param latitude 纬度 单位： °
     * @param raduisKm 距离 单位：KM
     * @param incrementDegrees
     * @returns {Array}
     */
    calcRangeCircle: function (longitude, latitude, raduisKm, incrementDegrees) {
        let fpcPots = [];
        for (let azi = 0; azi < 360; azi += incrementDegrees) {
            //footprintPoint数据结构：footprintPoint.lat, footprintPoint.lon, footprintPoint.finalBearing
            let footprintPoint = destVincenty(latitude, longitude, azi, raduisKm * 1000);
            fpcPots.push(footprintPoint);
        }
        return fpcPots;
    },
    /**
     * 计算未来24小时卫星星下点轨迹,timeGap:单位秒
     * @param sate
     * @param groundStation
     * @param calcDate
     * @param timeGap
     * @returns {Array}
     */
    calcSatePos4Time: function (sate, groundStation, calcDate, timeGap) {
        let satePoses = [];
        let cDate;
        if (calcDate) {
            cDate = calcDate;
        } else {
            cDate = new Date();
        }
        //1、参数定义
        let tleLine1 = sate.get('tfr'), tleLine2 = sate.get('tsr');

        //2、初始化卫星记录
        let sateRec = satellite.twoline2satrec(tleLine1, tleLine2);
        //获取毫秒数
        let msec = cDate.getTime();
        //间隔10秒
        for (let t = msec; t < msec + 24 * 3600 * 1000; t += timeGap * 1000) {
            let now = new Date(t);
            let posAndVelocity = satellite.propagate(sateRec, now);
            let positionEci = posAndVelocity.position,
                velocityEci = posAndVelocity.velocity;

            let gmst = satellite.gstime(now);
            let observerGd = {
                longitude: satellite.degreesToRadians(groundStation['lon']),
                latitude: satellite.degreesToRadians(groundStation['lat']),
                height: groundStation['h']
            };

            let positionEcf = satellite.eciToEcf(positionEci, gmst),
                velocityEcf = satellite.eciToEcf(velocityEci, gmst),
                observerEcf = satellite.geodeticToEcf(observerGd),
                positionGd = satellite.eciToGeodetic(positionEci, gmst),
                lookAngles = satellite.ecfToLookAngles(observerGd, positionEcf),
                dopplerFactor = satellite.dopplerFactor(observerEcf, positionEcf, velocityEcf);

            // 视角可以访问方位、高程、范围属性.
            let azimuth = lookAngles.azimuth,
                elevation = lookAngles.elevation,
                rangeSat = lookAngles.rangeSat;

            // 地理坐标通过`longitude`, `latitude`, `height`属性访问，此时度量单位为弧度.
            let longitude = positionGd.longitude,
                latitude = positionGd.latitude,
                height = positionGd.height;
            //  将弧度转换为地理经纬度数，以下即为卫星当前星下点坐标.
            let longitudeStr = satellite.degreesLong(longitude),
                latitudeStr = satellite.degreesLat(latitude),
                azimuthStr = azimuth * this.rad2deg;
            //计算足迹圆半径
            let footCircleRadiusKm = this.getRangeCircleRadiusKm(height);
            satePoses.push({
                lon: longitudeStr,
                lat: latitudeStr,
                al: height,
                radius: footCircleRadiusKm,
                azi: azimuthStr,
                el: elevation,
                range: rangeSat,
                df: dopplerFactor
            });
        }
        return satePoses;
    },
    /**
     * 根据tle计算卫星
     * @param satellite 卫星参数
     * @param groundStation 地面基站， 如：let groundStation = {
                lon: 0,
                lat: 0,
                h: 0
            };
     * @param calcDate 计算日期，若为空，则采用当前时间计算
     */
    calc4SatPos: function (sate, groundStation, calcDate) {
        let cDate;
        if (calcDate) {
            cDate = calcDate;
        } else {
            cDate = new Date();
        }
        //1、参数定义
        let tleLine1 = sate.get('tfr'), tleLine2 = sate.get('tsr'),
            sateName = sate.get('text'), sateId = sate.get('tid');

        //2、初始化卫星记录
        let sateRec = satellite.twoline2satrec(tleLine1, tleLine2);

        //3\根据时间计算卫星位置及速度
        let posAndVelocity = satellite.propagate(sateRec, cDate);
        let positionEci = posAndVelocity.position,
            velocityEci = posAndVelocity.velocity;

        //4、设置地面基站
        let observerGd = {
            longitude: satellite.degreesToRadians(groundStation['lon']),
            latitude: satellite.degreesToRadians(groundStation['lat']),
            height: groundStation['h']
        };

        //5、相关参数计算
        let gmst = satellite.gstime(cDate);
        let positionEcf = satellite.eciToEcf(positionEci, gmst),
            velocityEcf = satellite.eciToEcf(velocityEci, gmst),
            observerEcf = satellite.geodeticToEcf(observerGd),
            positionGd = satellite.eciToGeodetic(positionEci, gmst),
            lookAngles = satellite.ecfToLookAngles(observerGd, positionEcf),
            dopplerFactor = satellite.dopplerFactor(observerEcf, positionEcf, velocityEcf);

        // ECI and ECF 通过`x`, `y`, `z` 属性访问.
        /*let satelliteX = positionEci.x,
            satelliteY = positionEci.y,
            satelliteZ = positionEci.z;*/

        // 视角可以访问方位、高程、范围属性.
        let azimuth = lookAngles.azimuth,
            elevation = lookAngles.elevation,
            rangeSat = lookAngles.rangeSat;

        // 地理坐标通过`longitude`, `latitude`, `height`属性访问，此时度量单位为弧度.
        let longitude = positionGd.longitude,
            latitude = positionGd.latitude,
            height = positionGd.height;
        //  将弧度转换为地理经纬度数，以下即为卫星当前星下点坐标.
        let longitudeStr = satellite.degreesLong(longitude),
            latitudeStr = satellite.degreesLat(latitude),
            azimuthStr = azimuth * this.rad2deg;

        //6、卫星足迹圆半径计算,单位：KM，卫星足迹圆点集计算
        let footCircleRadiusKm = this.getRangeCircleRadiusKm(height);
        let footCirclePots = this.calcRangeCircle(longitudeStr, latitudeStr, footCircleRadiusKm, 2);

        //7、返回结果
        return {
            sateName: sateName,
            tid: sateId,
            longitude: longitudeStr,
            latitude: latitudeStr,
            altitude: height,
            azimuth: azimuthStr,
            elevation: elevation,
            rangeSat: rangeSat,
            df: dopplerFactor,
            fcr: footCircleRadiusKm,
            fcps: footCirclePots
        };
    }
});

/**
 * 卫星位置预测相关功能工具类
 * @type {acsweb.util.SatelliteUtil}
 */
let sateUtil = new acsweb.util.SatelliteUtil();