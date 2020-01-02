/**
 * Created by winnerlbm on 2018/7/30.
 */
Ext.define('acsweb.util.SearchFieldUtil', {
    extend: 'Ext.form.field.ComboBox',

    alias: 'widget.searchfieldutil',
    triggers: {
        clear: {
            weight: 0,
            cls: Ext.baseCSSPrefix + 'form-clear-trigger',
            hidden: true,
            scope: 'this'
        },
        search: {
            weight: 1,
            cls: Ext.baseCSSPrefix + 'form-search-trigger',
            scope: 'this'
        }
    }
});