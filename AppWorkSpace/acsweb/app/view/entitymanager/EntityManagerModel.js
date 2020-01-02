/**
 * Created by LBM on 2017/12/18.
 */
Ext.define('acsweb.view.entitymanager.EntityManagerModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.entitymanager',

    stores: {
        /*
        A declaration of Ext.data.Store configurations that are first processed as binds to produce an effective
        store configuration. For example:

        users: {
            model: 'EntityManager',
            autoLoad: true
        }
        */
    },

    data: {
        /* This object holds the arbitrary data that populates the ViewModel and is then available for binding. */
    },
    formulas: {
        apiDesc: {
            get: function (get) {
                let api = get('apiName');

                return api + '接口'
            }
        }
    }
});