/**
 * Created by winnerlbm on 2019/6/24.
 */
Ext.define('acsweb.view.dataprocess.DataProcessController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.dataprocess',

    requires: [
        'Ext.data.Store',
        'Ext.util.TaskManager'
    ],
    uploadMonitorTask: null,
    dbConfig: null,

    /**
     * Called when the view is created
     */
    init: function () {

    },
    //------开启上传文件状态监控--------
    monitorUploadState: function () {
        let me = this;
        if (this.uploadMonitorTask == null) {
            this.uploadMonitorTask = {
                run: function () {
                    if (uploadiFrame.window.getUploadState()) {
                        console.log('文件上传完成');
                        uploadiFrame.window.changeUploadState();

                        //获取文件上传记录列表
                        me.getFileInfoList(-1);
                    }
                },
                interval: 1000 //单位毫秒
            };
            Ext.TaskManager.start(this.uploadMonitorTask);
        }
    },
    //-------开始上传文件解析入库-------
    /**
     * 1、获取文件上传记录列表并分析数据内容
     * @param state 文件处理状态，-1：待处理入库或入库处理异常，需要重新处理入库；1：已处理入库,2:忽略状态，查询所有记录。
     */
    getFileInfoList: function (state) {
        sqliFrame.window.writeFileContent(null, 'sql');
        if (this.dbConfig) {
            let params = {
                dbAddress: this.dbConfig['dbAddress'],
                dbDesc: this.dbConfig['dbDesc'],
                dbName: this.dbConfig['dbName'],
                dbPassword: this.dbConfig['dbPassword'],
                dbPort: this.dbConfig['dbPort'],
                dbType: this.dbConfig['dbType'],
                dbUser: this.dbConfig['dbUser'],
                state: state
            }, fiGrid = this.lookupReference('fileInfoGridRef'), mask = ajax.fn.showMask(fiGrid, '数据分析中...'), me = this;
            fiGrid.setStore(null);

            //执行成功回调
            function successCallBack(response, opts) {
                //查询结果转json对象
                let result = Ext.JSON.decode(decodeURIComponent((response.responseText)), true);
                if (result && result['data'] && result['data'].length > 0) {
                    let fisStore = new Ext.create('Ext.data.Store', {
                        data: result['data']
                    });
                    fiGrid.setStore(fisStore);
                }

                ajax.fn.hideMask(mask);

                console.log('文件分析完成');
            }

            //执行失败回调
            function failureCallBack(response, opts) {
                ajax.fn.hideMask(mask);
            }

            ajax.fn.execute(params, 'GET', conf.serviceUrl + 'dataprocess/filelist', successCallBack, failureCallBack);
        }
    },

    //------模块渲染
    afterrenderHandler: function () {
        //数据库配置
        this.onLoadDataBaseConfigList();
    },
    rowclickHandler: function (gp, record, element, rowIndex, e, eOpts) {
        let sql = record.get('ctsql');
        sqliFrame.window.writeFileContent(sql, 'sql');
    },
    allDataBaseInfoList: function () {
        //数据库配置
        this.onLoadDataBaseConfigList();
    },
    // 根据字段组装建表SQL文本
    generateSqlText: function (fileName, tabelName, sqlJson) {
        if (sqlJson && sqlJson.length > 0) {
            let srartWrap = "CREATE TABLE IF NOT EXISTS " + tabelName + " (\n";
            let endWrap = ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='" + fileName + "'";
            let middleContent = "";
            let fieldNamds = [];
            for (let i = 0; i < sqlJson.length; i++) {
                let sj = sqlJson[i];
                middleContent += '\t' + sj['sql'] + ',\n';
                fieldNamds.push(sj['name'])
            }
            if (fieldNamds.indexOf('AREA') === -1) {
                middleContent += "\tAREA double DEFAULT NULL COMMENT '面积（㎡）',\n";
            }
            if (fieldNamds.indexOf('X') === -1) {
                middleContent += "\tX double DEFAULT NULL COMMENT '中心经度(°)',\n";
            }
            if (fieldNamds.indexOf('Y') === -1) {
                middleContent += "\tY double DEFAULT NULL COMMENT '中心纬度(°)',\n";
            }
            if (fieldNamds.indexOf('FUID') === -1) {
                middleContent += "\tFUID varchar(100) DEFAULT NULL COMMENT '要素唯一标识',\n";
            }
            middleContent += "\tGEOJSON mediumtext COMMENT 'GeoJSON字符串',\n";
            middleContent += "\tGEOM geometry DEFAULT NULL COMMENT '点/线/面WKT几何对象'\n";
            return srartWrap + middleContent + endWrap;
        }
    },

    //------数据库配置-------------------------------------------
    onLoadDataBaseConfigList: function () {
        let me = this;
        let dbcList = me.lookupReference('databaseConfigListRef');
        let params = {};

        //执行成功回调
        function successCallBack(response, opts) {
            //查询结果转json对象
            let result = Ext.JSON.decode(decodeURIComponent(response.responseText), true);
            let dbs = result['dbs'];
            if (dbs && dbs.length > 0) {
                //此处先过滤数据类型，目前仅支持MySQL数据库
                let dbs4MySQL = [];
                for (let i = 0; i < dbs.length; i++) {
                    if (dbs[i]['dbType'] === 'com.mysql.jdbc.Driver') {
                        dbs4MySQL.push(dbs[i]);
                    }
                }

                if (dbs4MySQL.length > 0) {
                    let dbsStore = new Ext.create('Ext.data.Store', {
                        data: dbs4MySQL
                    });
                    dbcList.setStore(dbsStore);
                }
            }
        }

        //执行失败回调
        function failureCallBack(response, opts) {
        }

        ajax.fn.execute(params, 'POST', conf.serviceUrl + 'coding/getdatabase', successCallBack, failureCallBack);
    },

    //------选择关联数据库----------------------------------------
    selectDBciHandler: function (combo, record, eOpts) {
        if (record) {
            sqliFrame.window.writeFileContent(null, 'sql');
            //1、获取数据库配置信息并填充配置信息
            let dbc = record.getData();
            this.dbConfig = dbc;
            if (this.dbConfig) {
                uploadiFrame.window.setDataBase(this.dbConfig, conf.serviceUrl + 'dataprocess/fileupload');
                this.monitorUploadState();

                let params = {
                    dbAddress: this.dbConfig['dbAddress'],
                    dbDesc: this.dbConfig['dbDesc'],
                    dbName: this.dbConfig['dbName'],
                    dbPassword: this.dbConfig['dbPassword'],
                    dbPort: this.dbConfig['dbPort'],
                    dbType: this.dbConfig['dbType'],
                    dbUser: this.dbConfig['dbUser']
                }, me = this;

                //执行成功回调
                function successCallBack(response, opts) {
                    //获取已上传文件清单
                    me.getFileInfoList(2);
                }

                //执行失败回调
                function failureCallBack(response, opts) {
                    alert("连接失败");
                }

                ajax.fn.execute(params, 'POST', conf.serviceUrl + 'coding/testdb', successCallBack, failureCallBack);
            }
        }
    },
    //全部上传文件记录，含已入库和未入库
    allFileInfoList: function () {
        this.getFileInfoList(2);
    },
    //未入库上传文件清单
    unImportFileInfoList: function () {
        this.getFileInfoList(-1);
    },
    //数据入库
    data2dbHandler: function () {
        this.executeDataImport();
    },
    executeDataImport: function () {
        if (this.dbConfig) {
            let params = {
                    dbAddress: this.dbConfig['dbAddress'],
                    dbDesc: this.dbConfig['dbDesc'],
                    dbName: this.dbConfig['dbName'],
                    dbPassword: this.dbConfig['dbPassword'],
                    dbPort: this.dbConfig['dbPort'],
                    dbType: this.dbConfig['dbType'],
                    dbUser: this.dbConfig['dbUser'],
                    state: -1
                }, fiGrid = this.lookupReference('fileInfoGridRef'),
                mask = ajax.fn.showMask(fiGrid, '正在入库，请耐心等待...'),
                me = this;

            //执行成功回调
            function successCallBack(response, opts) {
                me.getFileInfoList(2);

                ajax.fn.hideMask(mask);

                console.log('所有数据入库完成');
            }

            //执行失败回调
            function failureCallBack(response, opts) {
                ajax.fn.hideMask(mask);
            }

            ajax.fn.execute(params, 'GET', conf.serviceUrl + 'dataprocess/shp2db', successCallBack, failureCallBack);
        }
    }
});