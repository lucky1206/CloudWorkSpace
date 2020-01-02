/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
/* Vincenty Direct Solution of Geodesics on the Ellipsoid (c) Chris Veness 2005-2012              */
/*                                                                                                */
/* from: Vincenty direct formula - T Vincenty, "Direct and Inverse Solutions of Geodesics on the  */
/*       Ellipsoid with application of nested equations", Survey Review, vol XXII no 176, 1975    */
/*       http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf                                             */

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

function toRad(Value) {
    /** Converts numeric degrees to radians */
    return Value * Math.PI / 180;
}

function toDeg(Value) {
    /** Converts radians to numeric degrees */
    return Value * 180 / Math.PI;
}

function distVincenty(lat1, lon1, lat2, lon2, callback) {
    let a = 6378137,
        b = 6356752.314245,
        f = 1 / 298.257223563;  // WGS-84 ellipsoid params

    let L = toRad((lon2 - lon1));
    let U1 = Math.atan((1 - f) * Math.tan(toRad(lat1)));
    let U2 = Math.atan((1 - f) * Math.tan(toRad(lat2)));
    let sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
    let sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

    let lambda = L, lambdaP, iterLimit = 100;
    let cosSqAlpha = 0;
    let sinSigma = 0;
    let cos2SigmaM = 0;
    let cosSigma = 0;
    let sinLambda = 0;
    do {
        sinLambda = Math.sin(lambda), cosLambda = Math.cos(lambda);
        sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda) +
            (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
        if (sinSigma == 0) {
            let result = {distance: 0, initialBearing: 0, finalBearing: 0};
            if (callback !== undefined && callback instanceof Function) {
                if (callback.length === 3) {
                    callback(result.distance, result.initialBearing, result.finalBearing);
                } else {
                    callback(result.distance);
                }
            }
            return result;
        }
        // co-incident points
        cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
        let sigma = Math.atan2(sinSigma, cosSigma);
        let sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
        cosSqAlpha = 1 - sinAlpha * sinAlpha;
        cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
        if (isNaN(cos2SigmaM)) cos2SigmaM = 0;  // equatorial line: cosSqAlpha=0 (§6)
        let C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
        lambdaP = lambda;
        lambda = L + (1 - C) * f * sinAlpha *
            (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
    } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

    if (iterLimit == 0) return NaN;  // formula failed to converge

    let uSq = cosSqAlpha * (a * a - b * b) / (b * b);
    let A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
    let B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
    let deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) -
        B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
    let s = b * A * (sigma - deltaSigma);

    s = Number(s.toFixed(3)); // round to 1mm precision

    // note: to return initial/final bearings in addition to distance, use something like:
    let fwdAz = Math.atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
    let revAz = Math.atan2(cosU1 * sinLambda, -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
    let result = {distance: s, initialBearing: toDeg(fwdAz), finalBearing: toDeg(revAz)};

    if (callback !== undefined && callback instanceof Function) {
        if (callback.length === 3) {
            callback(result.distance, result.initialBearing, result.finalBearing);
        } else {
            callback(result.distance);
        }
    }

    return result;
}


/**
 * Calculates destination point given start point lat/long, bearing & distance,
 * using Vincenty inverse formula for ellipsoids
 *
 * @param   {Number} lat1, lon1: first point in decimal degrees
 * @param   {Number} brng: initial bearing in decimal degrees
 * @param   {Number} dist: distance along bearing in metres
 * @returns (LatLon} destination point
 */
function destVincenty(lat1, lon1, brng, dist, callback) {
    let a = 6378137, b = 6356752.3142, f = 1 / 298.257223563;  // WGS-84 ellipsiod
    let s = dist;
    let alpha1 = toRad(brng);
    let sinAlpha1 = Math.sin(alpha1);
    let cosAlpha1 = Math.cos(alpha1);

    let tanU1 = (1 - f) * Math.tan(toRad(lat1));
    let cosU1 = 1 / Math.sqrt((1 + tanU1 * tanU1)), sinU1 = tanU1 * cosU1;
    let sigma1 = Math.atan2(tanU1, cosAlpha1);
    let sinAlpha = cosU1 * sinAlpha1;
    let cosSqAlpha = 1 - sinAlpha * sinAlpha;
    let uSq = cosSqAlpha * (a * a - b * b) / (b * b);
    let A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
    let B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));

    let sigma = s / (b * A), sigmaP = 2 * Math.PI;
    let cos2SigmaM = 0;
    let sinSigma = 0;
    let cosSigma = 0;
    while (Math.abs(sigma - sigmaP) > 1e-12) {
        cos2SigmaM = Math.cos(2 * sigma1 + sigma);
        sinSigma = Math.sin(sigma);
        cosSigma = Math.cos(sigma);
        let deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) -
            B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
        sigmaP = sigma;
        sigma = s / (b * A) + deltaSigma;
    }

    let tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1;
    let lat2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1,
        (1 - f) * Math.sqrt(sinAlpha * sinAlpha + tmp * tmp));
    let lambda = Math.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
    let C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
    let L = lambda - (1 - C) * f * sinAlpha *
        (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
    let lon2 = (toRad(lon1) + L + 3 * Math.PI) % (2 * Math.PI) - Math.PI;  // normalise to -180...+180

    let revAz = Math.atan2(sinAlpha, -tmp);  // final bearing, if required

    let result = {lat: toDeg(lat2), lon: toDeg(lon2), finalBearing: toDeg(revAz)};

    if (callback !== undefined && callback instanceof Function) {
        if (callback.length === 3) {
            callback(result.lat, result.lon, result.finalBearing);
        } else {
            callback(result);
        }
    }

    return result;
}
