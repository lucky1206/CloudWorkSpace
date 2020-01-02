/**
 * GUID工具类
 * Created by winnerlbm on 2019/7/11.
 */
Ext.define('acsweb.util.GUIDUtil', {
    //用于生成uuid
    _S4: function () {
        return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
    },

    guid: function (len) {
        len = len > 0 ? len : 4;
        let guidValue = '';
        for (let i = 0; i < len; i++) {
            guidValue += this._S4();
        }
        return guidValue;
    }
});

let guidUtil = new acsweb.util.GUIDUtil();