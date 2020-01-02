/**
 * Created by winnerlbm on 2018/9/4.
 */
Ext.define('acsweb.view.entitymanager.customizeentity.CustomizeEntityModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.customizeentity',

    stores: {
        /*
        A declaration of Ext.data.Store configurations that are first processed as binds to produce an effective
        store configuration. For example:

        users: {
            model: 'CustomizeEntity',
            autoLoad: true
        }
        */
    },

    data: {
        /* This object holds the arbitrary data that populates the ViewModel and is then available for binding. */
    },
    formulas: {
        apiDesc4C: {
            get: function (get) {
                let api = get('apiName4C');

                return api + '接口'
            }
        }
    }
});