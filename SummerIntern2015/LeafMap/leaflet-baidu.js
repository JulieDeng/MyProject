function deg_rad(ang) {
    return ang * (Math.PI/180.0)
}
function merc_x(lon) {
    var r_major = 6378137.000;
    return r_major * deg_rad(lon);
}
function merc_y(lat) {
    if (lat > 89.5)
        lat = 89.5;
    if (lat < -89.5)
        lat = -89.5;
    var r_major = 6378137.000;
    var r_minor = 6356752.3142;
    var temp = r_minor / r_major;
    var es = 1.0 - (temp * temp);
    var eccent = Math.sqrt(es);
    var phi = deg_rad(lat);
    var sinphi = Math.sin(phi);
    var con = eccent * sinphi;
    var com = .5 * eccent;
    con = Math.pow((1.0-con)/(1.0+con), com);
    var ts = Math.tan(.5 * (Math.PI*0.5 - phi))/con;
    var y = 0 - r_major * Math.log(ts);
    return y;
}
function merc(x,y) {
    return [merc_x(x),merc_y(y)];
}

var Conv=({
    r_major:6378137.0,//Equatorial Radius, WGS84
    r_minor:6356752.314245179,//defined as constant
    f:298.257223563,//1/f=(a-b)/a , a=r_major, b=r_minor
    deg2rad:function(d)
    {
        var r=d*(Math.PI/180.0);
        return r;
    },
    rad2deg:function(r)
    {
        var d=r/(Math.PI/180.0);
        return d;
    },
    ll2m:function(lon,lat) //lat lon to mercator
    {
        //lat, lon in rad
        var x=this.r_major * this.deg2rad(lon);

        if (lat > 89.5) lat = 89.5;
        if (lat < -89.5) lat = -89.5;


        var temp = this.r_minor / this.r_major;
        var es = 1.0 - (temp * temp);
        var eccent = Math.sqrt(es);

        var phi = this.deg2rad(lat);

        var sinphi = Math.sin(phi);

        var con = eccent * sinphi;
        var com = .5 * eccent;
        var con2 = Math.pow((1.0-con)/(1.0+con), com);
        var ts = Math.tan(.5 * (Math.PI*0.5 - phi))/con2;
        var y = 0 - this.r_major * Math.log(ts);
        var ret={'x':x,'y':y};
        return ret;
    },
    m2ll:function(x,y) //mercator to lat lon
    {
        var lon=this.rad2deg((x/this.r_major));

        var temp = this.r_minor / this.r_major;
        var e = Math.sqrt(1.0 - (temp * temp));
        var lat=this.rad2deg(this.pj_phi2( Math.exp( 0-(y/this.r_major)), e));

        var ret={'lon':lon,'lat':lat};
        return ret;
    },
    pj_phi2:function(ts, e)
    {
        var N_ITER=15;
        var HALFPI=Math.PI/2;


        var TOL=0.0000000001;
        var eccnth, Phi, con, dphi;
        var i;
        var eccnth = .5 * e;
        Phi = HALFPI - 2. * Math.atan (ts);
        i = N_ITER;
        do
        {
            con = e * Math.sin (Phi);
            dphi = HALFPI - 2. * Math.atan (ts * Math.pow((1. - con) / (1. + con), eccnth)) - Phi;
            Phi += dphi;

        }
        while ( Math.abs(dphi)>TOL && --i);
        return Phi;
    }
});



var exports
if (typeof module === "object" && exports) {
    exports = module.exports
} else if (typeof window !== "undefined") {
    exports = window["eviltransform"] = {}
}

function outOfChina(lat, lng) {
    if ((lng < 72.004) || (lng > 137.8347)) {
        return true;
    }
    if ((lat < 0.8293) || (lat > 55.8271)) {
        return true;
    }
    return false;
}

function transformLat(x, y) {
    var ret = -100.0 + 2.0*x + 3.0*y + 0.2*y*y + 0.1*x*y + 0.2*Math.sqrt(Math.abs(x));
    ret += (20.0*Math.sin(6.0*x*Math.PI) + 20.0*Math.sin(2.0*x*Math.PI)) * 2.0 / 3.0;
    ret += (20.0*Math.sin(y*Math.PI) + 40.0*Math.sin(y/3.0*Math.PI)) * 2.0 / 3.0;
    ret += (160.0*Math.sin(y/12.0*Math.PI) + 320*Math.sin(y*Math.PI/30.0)) * 2.0 / 3.0;
    return ret;
}

function transformLon(x, y) {
    var ret = 300.0 + x + 2.0*y + 0.1*x*x + 0.1*x*y + 0.1*Math.sqrt(Math.abs(x));
    ret += (20.0*Math.sin(6.0*x*Math.PI) + 20.0*Math.sin(2.0*x*Math.PI)) * 2.0 / 3.0;
    ret += (20.0*Math.sin(x*Math.PI) + 40.0*Math.sin(x/3.0*Math.PI)) * 2.0 / 3.0;
    ret += (150.0*Math.sin(x/12.0*Math.PI) + 300.0*Math.sin(x/30.0*Math.PI)) * 2.0 / 3.0;
    return ret;
}

function delta(lat, lng) {
    var a = 6378245.0;
    var ee = 0.00669342162296594323;
    var dLat = transformLat(lng-105.0, lat-35.0);
    var dLng = transformLon(lng-105.0, lat-35.0);
    var radLat = lat / 180.0 * Math.PI;
    var magic = Math.sin(radLat);
    magic = 1 - ee*magic*magic;
    var sqrtMagic = Math.sqrt(magic);
    dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
    dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
    return {"lat": dLat, "lng": dLng};
}

function wgs2gcj(wgsLat, wgsLng) {
    if (outOfChina(wgsLat, wgsLng)) {
        return {"lat": wgsLat, "lng": wgsLng};
    }
    var d = delta(wgsLat, wgsLng);
    return {"lat": wgsLat + d.lat, "lng": wgsLng + d.lng};
}
exports.wgs2gcj = wgs2gcj;

function gcj2wgs(gcjLat, gcjLng) {
    if (outOfChina(gcjLat, gcjLng)) {
        return {"lat": gcjLat, "lng": gcjLng};
    }
    var d = delta(gcjLat, gcjLng);
    return {"lat": gcjLat - d.lat, "lng": gcjLng - d.lng};
}
exports.gcj2wgs = gcj2wgs;

function gcj2wgs_exact(gcjLat, gcjLng) {
    var initDelta = 0.01;
    var threshold = 0.000001;
    var dLat = initDelta, dLng = initDelta;
    var mLat = gcjLat-dLat, mLng = gcjLng-dLng;
    var pLat = gcjLat+dLat, pLng = gcjLng+dLng;
    var wgsLat, wgsLng;
    for (var i = 0; i < 30; i++) {
        wgsLat = (mLat+pLat)/2;
        wgsLng = (mLng+pLng)/2;
        var tmp = wgs2gcj(wgsLat, wgsLng)
        dLat = tmp.lat-gcjLat;
        dLng = tmp.lng-gcjLng;
        if ((Math.abs(dLat) < threshold) && (Math.abs(dLng) < threshold)) {
            return {"lat": wgsLat, "lng": wgsLng};
        }
        if (dLat > 0) {
            pLat = wgsLat;
        } else {
            mLat = wgsLat;
        }
        if (dLng > 0) {
            pLng = wgsLng;
        } else {
            mLng = wgsLng;
        }
    }
    return {"lat": wgsLat, "lng": wgsLng};
}
exports.gcj2wgs_exact = gcj2wgs_exact;

function distance(latA, lngA, latB, lngB) {
    var earthR = 6371000;
    var x = Math.cos(latA*Math.PI/180) * Math.cos(latB*Math.PI/180) * Math.cos((lngA-lngB)*Math.PI/180);
    var y = Math.sin(latA*Math.PI/180) * Math.sin(latB*Math.PI/180);
    var s = x + y;
    if (s > 1) {
        s = 1;
    }
    if (s < -1) {
        s = -1;
    }
    var alpha = Math.acos(s);
    var distance = alpha * earthR;
    return distance;
}
exports.distance = distance;

function gcj2bd(gcjLat, gcjLng) {
    if (outOfChina(gcjLat, gcjLng)) {
        return {"lat": gcjLat, "lng": gcjLng};
    }

    var x = gcjLng, y = gcjLat;
    var z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * Math.PI);
    var theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * Math.PI);
    var bdLng = z * Math.cos(theta) + 0.0065;
    var bgLat = z * Math.sin(theta) + 0.006;
    return {"lat": bdLat, "lng": bdLng};
}
exports.gcj2bd = gcj2bd;

function bd2gcj(bdLat, bdLng) {
    if (outOfChina(bdLat, bdLng)) {
        return {"lat": bgLat, "lng": bdLng};
    }

    var x = bdLng - 0.0065, y = bdLat - 0.006;
    var z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * Math.PI);
    var theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * Math.PI);
    var gcjLng = z * Math.cos(theta);
    var gcjLat = z * Math.sin(theta);
    return {"lat": gcjLat, "lng": gcjLng};
}
exports.bd2gcj = bd2gcj;

function wgs2bd(wgsLat, wgsLng) {
    var gcj = wgs2gcj(wgsLat, wgsLng)
    return gcj2bd(gcj.lat, gcj.lng)
}
exports.wgs2bd = wgs2bd;

function bd2wgs(bdLat, bdLng) {
    var gcj = bd2gcj(bdLat, bdLng)
    return gcj2wgs(gcj.lat, gcj.lng)
}
exports.bd2wgs = bd2wgs;


/**
 * Projection class for Baidu Spherical Mercator
 *
 * @class BaiduSphericalMercator
 */
L.Projection.BaiduSphericalMercator = {
    /**
     * Project latLng to point coordinate
     *
     * @method project
     * @param {Object} latLng coordinate for a point on earth
     * @return {Object} leafletPoint point coordinate of L.Point
     */
    project: function(latLng) {
        var projection = new BMap.MercatorProjection();
        var point = projection.lngLatToPoint(
            new BMap.Point(latLng.lng, latLng.lat)
        );
        console.log(latLng.lat, latLng.lng);
        //latLng = gcj2wgs_exact(latLng.lat, latLng.lng);
        var mercator=Conv.ll2m(latLng.lng, latLng.lat);
        console.log(merc);
        var leafletPoint = new L.Point(mercator.x, mercator.y);
        console.log(mercator);
        console.log(leafletPoint);
        return leafletPoint;
    },

    /**
     * unproject point coordinate to latLng
     *
     * @method unproject
     * @param {Object} bpoint baidu point coordinate
     * @return {Object} latitude and longitude
     */
    unproject: function (bpoint) {
        var projection= new BMap.MercatorProjection();
        var point = projection.pointToLngLat(
            new BMap.Pixel(bpoint.x, bpoint.y)
        );
        var latLng = new L.LatLng(point.lat, point.lng);
        return latLng;
    },

    /**
     * Don't know how it used currently.
     *
     * However, I guess this is the range of coordinate.
     * Range of pixel coordinate is gotten from
     * BMap.MercatorProjection.lngLatToPoint(180, -90) and (180, 90)
     * After getting max min value of pixel coordinate, use
     * pointToLngLat() get the max lat and Lng.
     */
    bounds: (function () {
        var MAX_X= 20037726.37;
        var MIN_Y= -11708041.66;
        var MAX_Y= 12474104.17;
        var bounds = L.bounds(
            [-MAX_X, MIN_Y], //180, -71.988531
            [MAX_X, MAX_Y]  //-180, 74.000022
        );
        return bounds;
    })()
};

/**
 * Transformation class for Baidu Transformation.
 * Basically, it contains the conversion of point coordinate and
 * pixel coordinate.
 *
 * @class BTransformation
 */
L.BTransformation = function () {
};

L.BTransformation.prototype = {
    MAXZOOM: 18,
    /**
     * Don't know how it used currently.
     */
    transform: function (point, zoom) {
        return this._transform(point.clone(), zoom);
    },

    /**
     * transform point coordinate to pixel coordinate
     *
     * @method _transform
     * @param {Object} point point coordinate
     * @param {Number} zoom zoom level of the map
     * @return {Object} point, pixel coordinate
     */
    _transform: function (point, zoom) {
        point.x = point.x >> (this.MAXZOOM - zoom);
        point.y = point.y >> (this.MAXZOOM - zoom);
        return point;
    },

    /**
     * transform pixel coordinate to point coordinate
     *
     * @method untransform
     * @param {Object} point pixel coordinate
     * @param {Number} zoom zoom level of the map
     * @return {Object} point, point coordinate
     */
    untransform: function (point, zoom) {
        point.x = point.x << (this.MAXZOOM - zoom);
        point.y = point.y << (this.MAXZOOM - zoom);
        return point;
    }
};

/**
 * Coordinate system for Baidu EPSG3857
 *
 * @class BEPSG3857
 */
L.CRS.BEPSG3857 = L.extend({}, L.CRS, {
    /**
     * transform latLng to pixel coordinate
     *
     * @method untransform
     * @param {Object} latlng latitude and longitude
     * @param {Number} zoom zoom level of the map
     * @return {Object} pixel coordinate calculated for latLng
     */
    latLngToPoint: function (latlng, zoom) { // (LatLng, Number) -> Point
        var projectedPoint = this.projection.project(latlng);
        return this.transformation._transform(projectedPoint, zoom);
    },

    /**
     * transform pixel coordinate to latLng
     *
     * @method untransform
     * @param {Object} point pixel coordinate
     * @param {Number} zoom zoom level of the map
     * @return {Object} latitude and longitude
     */
    pointToLatLng: function (point, zoom) { // (Point, Number[, Boolean]) -> LatLng
        var untransformedPoint = this.transformation.untransform(point, zoom);
        return this.projection.unproject(untransformedPoint);
    },

    code: 'EPSG:3857',
    projection: L.Projection.BaiduSphericalMercator,

    transformation: new L.BTransformation()
});

/**
 * Tile layer for Baidu Map
 *
 * @class Baidu
 */
L.Baidu = L.TileLayer.extend({
    options: {
        subdomains: ['online1', 'online2', 'online3'],
        //TODO: decode utf8 characters in attribution
        attribution: '© 2014 Baidu - GS(2012)6003;- Data © <a target="_blank" href="http://www.navinfo.com/">NavInfo</a> & <a target="_blank" href="http://www.cennavi.com.cn/">CenNavi</a> & <a target="_blank" href="http://www.365ditu.com/">DaoDaoTong</a>',
    },

    /**
     * initialize the map with key and tile URL
     *
     * @method initialize
     * @param {String} key access key of baidu map
     * @param {Object} options, option of the map
     */
    initialize: function (key, options) {
        L.Util.setOptions(this, options);
        this._key = key;
        this._url = 'http://{subdomain}.map.bdimg.com/tile/?qt=tile&x={x}&y={y}&z={z}&styles=pl&udt=20140711';
    },

    /**
     * Set the corresponding position of tiles in baidu map.
     * if point.y is less or equal than 256, i.e. 35=>291, -221=>547
     * if point.y is greater than 256, i.e. 291=>35, 547=>-221
     *
     * @method _getTilePos
     * @param {Object} tilePoint tile coordinate
     * @return {Object} point left and top property of <img>
     */
    _getTilePos: function (tilePoint) {
        var origin = this._map.getPixelOrigin();
        var tileSize = this._getTileSize();

        var point = tilePoint.multiplyBy(tileSize).subtract(origin);
        if (point.y <= 256) {
            point.y = (
                2 * Math.abs(
                    Math.floor(point.y / tileSize)
                ) + 1
            ) * tileSize + point.y;
        } else {
            point.y = point.y - (
                Math.floor(point.y / tileSize) * 2 - 1
            ) * tileSize;
        }
        return point;
    },

    /**
     * Override _update method in map. redefine bounds.
     * Pros: no blank row on the top or bottom
     * Cons: some times it might load a row that is not necessary.
     *
     * @method _updateBaidu
     */
     _update: function () {
        if (!this._map) { return; }
        var map = this._map,
            bounds = map.getPixelBounds(),
            zoom = map.getZoom(),
            tileSize = this._getTileSize();
        if (zoom > this.options.maxZoom || zoom < this.options.minZoom) {
            return;
        }

        boundsMax = bounds.max.divideBy(tileSize);
        boundsMax.x = Math.floor(boundsMax.x);
        boundsMax.y = Math.ceil(boundsMax.y);

        var tileBounds = L.bounds(
            bounds.min.divideBy(tileSize)._floor(),
            boundsMax
        );

        this._addTilesFromCenterOut(tileBounds);
        if (this.options.unloadInvisibleTiles || this.options.reuseTiles) {
            this._removeOtherTiles(tileBounds);
        }
    },

    /**
     * get a tile url of the map
     *
     * @method getTileUrl
     * @param {Object} coords, tile coordinate
     * @return {String} url of a tile
     */
    getTileUrl: function(coords) {
        return this._url.replace('{subdomain}', this._getSubdomain(coords))
            .replace('{x}', coords.x)
            .replace('{y}', coords.y)
            .replace('{z}', this._getZoomForUrl());
    }
});

L.map = function (id, options) {
    var map = new L.Map(id, options);

    /**
     * load new tiles when set zoom for baidu map
     * Works well: mouse scroll. zoom level <= 14 in double click
     * Works not well: zoom level > 14. Not Accurate at all.
     * TODO: figure out why not accurate. Potential: CRS differences.
     *
     * @method _setZoomAroundBaidu
     * @param {Object} latlng position of mouse clicked on the canvas
     * @param {Number} zoom zoom level
     * @param {Object} options options of the map
     * @return {Object} TODO: not sure for now. probably the map itself
     */
    var setZoomAroundBaidu = function (latlng, zoom, options) {
        var scale = this.getZoomScale(zoom);
        var viewHalf = this.getSize().divideBy(2);
        var containerPoint = latlng instanceof L.Point ? latlng : this.latLngToContainerPoint(latlng);
        var centerOffset = containerPoint.subtract(viewHalf).multiplyBy(1 - 1 / scale);
        var newCenter = this.containerPointToLatLng(viewHalf.add(centerOffset));
        var oldCenterLat = this.getCenter().lat;
        //add offset rather than minus it
        newCenter.lat = oldCenterLat - newCenter.lat + oldCenterLat;
        return this.setView(newCenter, zoom, {zoom: options});
    };

    /**
     * Override _getTopLeftPoint method. For Baidu Map, if dragging
     * down side of the map, y will increase rather than decrease.
     * vice versa.
     *
     * @method _getTopLeftPoint
     * @return {Object} point top left point
     */
    var _getTopLeftPointBaidu = function () {
        var pixel = this.getPixelOrigin();
        var pane = this._getMapPanePos();
        var point = new L.Point(pixel.x - pane.x, pixel.y + pane.y);
        return point;
    };

    //if option has baidu, use custom method
    if (options.baidu === true) {
        map._getTopLeftPoint = _getTopLeftPointBaidu;
        map.setZoomAround = setZoomAroundBaidu;
    }
    return map;
};

L.baiduLayer = function (key, options) {
    return new L.Baidu(key, options);
};
