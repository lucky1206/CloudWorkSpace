/**
 * Created by winnerlbm on 2018/8/1.
 */
Ext.define('acsweb.util.SearchTextUtil', {
    extend: 'Ext.form.field.Text',

    alias: 'widget.searchtextutil',
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