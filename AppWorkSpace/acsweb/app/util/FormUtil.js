/**
 * Created by winnerlbm on 2019/6/18.
 */
Ext.define('acsweb.util.FormUtil', {
    //是否启用表单只读
    enableFormReadOnly: function (form, isReadOnly) {
        let fts = form.items.items;
        Ext.each(fts, function (ft) {
            if (ft.xtype == 'textfield' || ft.xtype == 'combobox') {
                ft.setReadOnly(isReadOnly);
            }
        });
    },
    //获取表单数据
    getFormData: function (form) {
        let formData = form.getForm().getValues();
        return formData;
    }
});

let formUtil = new acsweb.util.FormUtil();