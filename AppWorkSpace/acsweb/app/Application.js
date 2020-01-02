/**
 * The main application class. An instance of this class is created by app.js when it
 * calls Ext.application(). This is the ideal place to handle application launch and
 * initialization details.
 */
Ext.define('acsweb.Application', {
    extend: 'Ext.app.Application',

    name: 'acsweb',
    controllers: ["AppController"],
    stores: [
        // TODO: add global / shared stores here
    ],

    launch: function () {
        // TODO - Launch the application
        document.title = conf.title;
        let load = Ext.get('loading');
        if (load) {
            load.remove();//清除启动mask
        }
    },

    onAppUpdate: function () {
        //应用更新之后自动重载
        window.location.reload();
    }
});

/**
 * Created by LBM on 2017/12/18.
 */
/**custom Vtype for vtype:'IPAddress'*/
Ext.define('Override.form.field.VTypes', {
    override: 'Ext.form.field.VTypes',

    IPAddress: function (value) {
        return this.IPAddressRe.test(value);
    },
    IPAddressRe: /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/,
    IPAddressText: '请输入正确格式的IP地址!',
    IPAddressMask: /[\d\.]/i
});

