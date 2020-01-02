/**
 * SQL语句解析工具类
 */
package com.acs.util;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectTableReference;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitorAdapter;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGFunctionTableSource;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGASTVisitorAdapter;
import com.alibaba.druid.sql.visitor.ParameterizedOutputVisitorUtils;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LBM
 * @time 2017年12月29日
 * @project codecreator
 * @type SQLPraseUtil
 * @desc SQL解译工具
 */
public class SQLParseUtil {
    public JSONObject sqlParser(String sql, String dbType) {
        // 不参数化但是格式化
        String noFormatedSql = this.preProcessSql(sql);
        // 参数化语句
        // ParameterizedOutputVisitorUtils pov = new ParameterizedOutputVisitorUtils();
        String formatedSql = ParameterizedOutputVisitorUtils.parameterize(sql, dbType).toLowerCase();
        // 参数化并格式化
        formatedSql = this.preProcessSql(formatedSql);

        // 1、分解组合并去掉case语句
        // 参数语句
        JSONArray fja = new JSONArray();
        explainSql4Case(formatedSql, 0, fja);
        // 原始语句
        JSONArray oja = new JSONArray();
        explainSql4Case(noFormatedSql, 0, oja);
        String formatedSeq = "";// 存储去掉case参数化之后的语句
        if (fja != null && fja.size() > 0) {
            int flen = fja.size();
            for (int i = 0; i < flen; i++) {
                JSONObject fseq = (JSONObject) fja.get(i);
                JSONObject oseq = (JSONObject) oja.get(i);
                if (flen == 1) {
                    formatedSeq += fseq.getString("lastSeq");
                    formatedSeq += oseq.getString("caseSeq");
                    formatedSeq += fseq.getString("restSeq");
                } else {
                    if (i < flen - 1) {
                        formatedSeq += fseq.getString("lastSeq");
                        formatedSeq += oseq.getString("caseSeq");
                    } else {
                        formatedSeq += fseq.getString("lastSeq");
                        formatedSeq += oseq.getString("caseSeq");
                        formatedSeq += fseq.getString("restSeq");
                    }
                }
            }
        }

        // 2、分解并组合参数化like子句
        String formatedSeqLikeInput = "";
        if ("".equals(formatedSeq)) {
            formatedSeqLikeInput = formatedSql;
        } else {
            formatedSeqLikeInput = formatedSeq;
        }
        JSONArray likeja = new JSONArray();
        explainSql4Like(formatedSeqLikeInput, 0, likeja);
        String formatedSeq1 = "";
        if (likeja != null && likeja.size() > 0) {
            int flen = likeja.size();
            for (int i = 0; i < flen; i++) {
                JSONObject lseq = (JSONObject) likeja.get(i);
                if (flen == 1) {
                    formatedSeq1 += lseq.getString("lastSeq");
                    formatedSeq1 += "?";
                    formatedSeq1 += lseq.getString("restSeq");
                } else {
                    if (i < flen - 1) {
                        formatedSeq1 += lseq.getString("lastSeq");
                        formatedSeq1 += "?";
                    } else {
                        formatedSeq1 += lseq.getString("lastSeq");
                        formatedSeq1 += "?";
                        formatedSeq1 += lseq.getString("restSeq");
                    }
                }
            }
        }

        // 3、分解并组合参数化in子句
        String formatedSeqInInput = "";
        if ("".equals(formatedSeq1)) {
            formatedSeqInInput = formatedSeqLikeInput;
        } else {
            formatedSeqInInput = formatedSeq1;
        }
        // 格式化
        JSONArray fija = new JSONArray();
        explainSql4In(formatedSeqInInput, 0, fija);
        //@todo 在进行SQL解译时需要包含对in子句的解析时，请注释该段代码。--2018年7月11日
        String formatedSeq2 = "";// 存储去掉in参数化之后的语句
        if (fija != null && fija.size() > 0) {
            int filen = fija.size();
            for (int i = 0; i < filen; i++) {
                JSONObject fiseq = (JSONObject) fija.get(i);
                if (filen == 1) {
                    formatedSeq2 += fiseq.getString("lastSeq");
                    formatedSeq2 += fiseq.getString("inSeq");
                    formatedSeq2 += fiseq.getString("restSeq");
                } else {
                    if (i < filen - 1) {
                        formatedSeq2 += fiseq.getString("lastSeq");
                        formatedSeq2 += fiseq.getString("inSeq");
                    } else {
                        formatedSeq2 += fiseq.getString("lastSeq");
                        formatedSeq2 += fiseq.getString("inSeq");
                        formatedSeq2 += fiseq.getString("restSeq");
                    }
                }
            }
        }

        //@todo 在进行SQL解译时需要排除对in子句的解析时，请取消该段代码注释。--2018年7月11日
        // 原始化
        /*JSONArray oija = new JSONArray();
        explainSql4In(noFormatedSql, 0, oija);
        String formatedSeq2 = "";// 存储去掉in参数化之后的语句
        if (fija != null && fija.size() > 0) {
            int filen = fija.size();
            for (int i = 0; i < filen; i++) {
                JSONObject fiseq = (JSONObject) fija.get(i);
                JSONObject oiseq = (JSONObject) oija.get(i);
                if (filen == 1) {
                    formatedSeq2 += fiseq.getString("lastSeq");
                    formatedSeq2 += oiseq.getString("inSeq");
                    formatedSeq2 += fiseq.getString("restSeq");
                } else {
                    if (i < filen - 1) {
                        formatedSeq2 += fiseq.getString("lastSeq");
                        formatedSeq2 += oiseq.getString("inSeq");
                    } else {
                        formatedSeq2 += fiseq.getString("lastSeq");
                        formatedSeq2 += oiseq.getString("inSeq");
                        formatedSeq2 += fiseq.getString("restSeq");
                    }
                }
            }
        }*/

        // 4、解译sql获取表格，字段信息
        String formatedSeqParamInput = "";
        if ("".equals(formatedSeq2)) {
            formatedSeqParamInput = formatedSeqInInput;
        } else {
            formatedSeqParamInput = formatedSeq2;
        }

        return explainSql4Param(noFormatedSql, formatedSeqParamInput, dbType);
    }

    /**
     * 判断集合中是否存在相同名称的字段
     *
     * @param name 字段名称
     * @param ja   字段集合
     * @return 返回字段出现的次数
     */
    public int judgeFieldExist(String name, JSONArray ja) {
        int count = 0;
        if (ja != null && ja.size() > 0 && !name.trim().equalsIgnoreCase("") && name != null) {
            int jaLen = ja.size();
            for (int i = 0; i < jaLen; i++) {
                JSONObject jo = ja.getJSONObject(i);
                String joName = jo.getString("javaname").toLowerCase();
                if (joName.indexOf(name.toLowerCase()) > -1) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * SQL解码思路： 1、case子句保留原样，不进行参数化,注意这里的case子句需要满足格式要求end之后有必须关键字 as；
     * 2、like子句合并通配符并参数化，测试时需要将通配符与查询字符同时传入； 3、in/not in子句进行参数化,注意这里的in子句必须满足格式，如
     * in(10,20); 4、参数提取并模型化。
     */
    // 1、预处理SQL语句
    private String preProcessSql(String sql) {
        // 1、全部转小写
        String lowSql = sql.toLowerCase().trim().replaceAll("\\s+", " ");// 去掉重复的空格及其他符号
        String[] lowSeqs = lowSql.split(",");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < lowSeqs.length; i++) {
            if (i != lowSeqs.length - 1)
                sb.append(lowSeqs[i].trim() + ",");
            else
                sb.append(lowSeqs[i].trim());
        }
        lowSql = sb.toString().trim();
        if (lowSql.indexOf(";", 0) > 0) {
            // 如果末尾含有分号去掉
            int index = lowSql.indexOf(";", 0);
            lowSql = lowSql.substring(0, index);
        }
        return lowSql;
    }

    // 2、 合并之后去掉所有的case when子句，也就是说case语句不参与格式化。
    private void explainSql4Case(String sql, int caseIndex, JSONArray seqArray) {
        // 1、提取case语句
        int fci = sql.indexOf("case", caseIndex);
        int fasi = sql.indexOf(" as ", fci + 4);
        if (fci > -1 && fasi > -1) {
            // 获取" as "之后与case语句相关部分内容,此处要求end之后必须关键字 as
            int easi = fasi + 4;// " as "结束位置
            int endIndex = easi;// case 语句的末尾字符索引
            int len = sql.length();
            // 判断when子句个数
            String seqChar = "";
            for (int index = endIndex; index < len; index++) {
                seqChar = sql.substring(index, index + 1);
                if (!"".equals(seqChar.trim())) {
                    if (",".equals(seqChar.trim())) {
                        endIndex = index + 1;
                        break;
                    }
                } else {
                    endIndex = index + 1;
                    break;
                }
            }

            String alias = sql.substring(easi, endIndex - 1);// case字段别名
            String lastSeq = sql.substring(caseIndex, fci);
            String caseSeq = sql.substring(fci, endIndex);
            String restSeq = sql.substring(endIndex);
            JSONObject seqObj = new JSONObject();
            seqObj.put("alias", alias);
            seqObj.put("lastSeq", lastSeq);
            seqObj.put("caseSeq", caseSeq);
            seqObj.put("restSeq", restSeq);
            seqArray.add(seqObj);
            if (restSeq.indexOf("case") > -1) {
                explainSql4Case(sql, endIndex, seqArray);
            }
        }
    }

    // 3、 提取重构like子句
    private void explainSql4Like(String sql, int caseIndex, JSONArray seqArray) {
        // 1、提取like子句
        int fli = sql.indexOf("like", caseIndex);
        if (fli > -1) {
            fli = fli + 5;
            int eli = sql.indexOf(" ", fli);
            if (fli > -1 && eli > -1) {
                JSONObject seqObj = new JSONObject();
                String lastSeq = sql.substring(caseIndex, fli);
                String likeSeq = sql.substring(fli, eli);
                String restSeq = sql.substring(eli);
                seqObj.put("lastSeq", lastSeq);
                seqObj.put("likeSeq", likeSeq);
                seqObj.put("restSeq", restSeq);
                seqArray.add(seqObj);
                if (restSeq.indexOf("like") > -1) {
                    explainSql4Like(sql, eli, seqArray);
                }
            }
        }
    }

    // 4、提取重构in/not in子句
    private void explainSql4In(String sql, int caseIndex, JSONArray seqArray) {
        // 1、提取in子句
        int fii = sql.indexOf(" in ", caseIndex);
        if (fii > -1) {
            fii = fii + 3;
            int eii = sql.indexOf(") ", fii) + 1;
            /*如果eii=0，则说明in子句是最后一个条件，右括号之后已经没有语句，如：select d.* from district d where d.name in (?)，
            语句末尾不再有空格，因此需要重新查询语句结束位置索引。*/
            if (eii == 0) {
                eii = sql.indexOf(")", fii) + 1;
            }
            if (fii > -1 && eii > -1) {
                JSONObject seqObj = new JSONObject();
                String lastSeq = sql.substring(caseIndex, fii);
                String inSeq = sql.substring(fii, eii);
                String restSeq = sql.substring(eii);
                seqObj.put("lastSeq", lastSeq);
                seqObj.put("inSeq", inSeq);
                seqObj.put("restSeq", restSeq);
                seqArray.add(seqObj);
                if (restSeq.indexOf(" in ") > -1) {
                    explainSql4In(sql, eii, seqArray);
                }
            }
        }
    }

    // 5、Response & Resquest参数模型提取，建议不管是单表还是多表，均采用表别名编写SQL语句
    private JSONObject explainSql4Param(String sql, String formatedSql, String dbType) {
        JSONObject sqlObj = new JSONObject();// sql解译对象

        // SQL解译信息
        JSONArray tableJa = new JSONArray();
        JSONArray requestJa = new JSONArray();
        JSONArray responseJa = new JSONArray();

        // 提取表别名和字段信息
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
        ExportMySqlTableAliasVisitor mysqlVisitor = null;
        ExportOracleTableAliasVisitor oracleVisitor = null;
        ExportPostgreSQLTableAliasVisitor postgreSQLVisitor = null;
        if (dbType.equalsIgnoreCase(JdbcUtils.MYSQL)) {
            mysqlVisitor = new ExportMySqlTableAliasVisitor();
            for (SQLStatement stmt : stmtList) {
                stmt.accept(mysqlVisitor);

                // 获取查询响应字段
                if (stmt instanceof SQLSelectStatement) {
                    SQLSelectStatement sstmt = (SQLSelectStatement) stmt;
                    SQLSelect sqlselect = sstmt.getSelect();
                    // 判断当前语句是否为union关键字连接的语句,若一条sql语句中至少存在一个union关键字
                    if (sqlselect.getQuery().getClass().equals(SQLUnionQuery.class)) {
                        SQLUnionQuery query = (SQLUnionQuery) sqlselect.getQuery();
                        this.explainSql4Union(query, responseJa);
                    } else {
                        SQLSelectQueryBlock query = (SQLSelectQueryBlock) sqlselect.getQuery();
                        List<SQLSelectItem> ssi = query.getSelectList();
                        for (SQLSelectItem si : ssi) {
                            JSONObject column = new JSONObject();
                            String exp = si.getExpr().toString();
                            String alias = si.getAlias();

                            if (alias != null) {
                                column.put("isAlias", true);// 字段别名，不需要再次解析
                                column.put("name", alias);
                            } else {
                                column.put("isAlias", false);// 不是字段别名，需要再次解析为表对应的字段
                                column.put("name", exp);
                            }
                            responseJa.add(column);
                        }
                    }
                }
            }

            // 获取别名
            Map<String, SQLTableSource> tmap = mysqlVisitor.getAliasMap();
            if (tmap != null && tmap.size() > 0) {
                for (Map.Entry<String, SQLTableSource> entry : tmap.entrySet()) {
                    JSONObject table = new JSONObject();
                    table.put("alias", entry.getKey());
                    table.put("name", entry.getValue().toString());
                    tableJa.add(table);
                }
            }

        } else if (dbType.equalsIgnoreCase(JdbcUtils.ORACLE)) {
            oracleVisitor = new ExportOracleTableAliasVisitor();
            for (SQLStatement stmt : stmtList) {
                stmt.accept(oracleVisitor);

                // 获取查询响应字段
                if (stmt instanceof SQLSelectStatement) {
                    SQLSelectStatement sstmt = (SQLSelectStatement) stmt;
                    SQLSelect sqlselect = sstmt.getSelect();
                    // 判断当前语句是否为union关键字连接的语句,若一条sql语句中至少存在一个union关键字
                    if (sqlselect.getQuery().getClass().equals(SQLUnionQuery.class)) {
                        SQLUnionQuery query = (SQLUnionQuery) sqlselect.getQuery();
                        this.explainSql4Union(query, responseJa);
                    } else {
                        SQLSelectQueryBlock query = (SQLSelectQueryBlock) sqlselect.getQuery();
                        List<SQLSelectItem> ssi = query.getSelectList();
                        for (SQLSelectItem si : ssi) {
                            JSONObject column = new JSONObject();
                            String exp = si.getExpr().toString();
                            String alias = si.getAlias();

                            if (alias != null) {
                                column.put("isAlias", true);// 字段别名，不需要再次解析
                                column.put("name", alias);
                            } else {
                                column.put("isAlias", false);// 不是字段别名，需要再次解析为表对应的字段
                                column.put("name", exp);
                            }
                            responseJa.add(column);
                        }
                    }
                }
            }

            // 获取别名
            Map<String, SQLTableSource> tmap = oracleVisitor.getAliasMap();
            if (tmap != null && tmap.size() > 0) {
                for (Map.Entry<String, SQLTableSource> entry : tmap.entrySet()) {
                    JSONObject table = new JSONObject();
                    table.put("alias", entry.getKey());
                    String name = entry.getValue().toString();
                    if (name.indexOf(" ") > -1) {
                        String[] tns = name.split(" ");
                        if (tns != null && tns.length > 0) {
                            name = tns[0];
                        }
                    }
                    table.put("name", name);
                    tableJa.add(table);
                }
            }
        } else if (dbType.equalsIgnoreCase(JdbcUtils.POSTGRESQL)) {
            postgreSQLVisitor = new ExportPostgreSQLTableAliasVisitor();
            for (SQLStatement stmt : stmtList) {
                stmt.accept(postgreSQLVisitor);

                // 获取查询响应字段
                if (stmt instanceof SQLSelectStatement) {
                    SQLSelectStatement sstmt = (SQLSelectStatement) stmt;
                    SQLSelect sqlselect = sstmt.getSelect();
                    // 判断当前语句是否为union关键字连接的语句,若一条sql语句中至少存在一个union关键字
                    if (sqlselect.getQuery().getClass().equals(SQLUnionQuery.class)) {
                        SQLUnionQuery query = (SQLUnionQuery) sqlselect.getQuery();
                        this.explainSql4Union(query, responseJa);
                    } else {
                        SQLSelectQueryBlock query = (SQLSelectQueryBlock) sqlselect.getQuery();
                        List<SQLSelectItem> ssi = query.getSelectList();
                        for (SQLSelectItem si : ssi) {
                            JSONObject column = new JSONObject();
                            String exp = si.getExpr().toString();
                            String alias = si.getAlias();

                            if (alias != null) {
                                column.put("isAlias", true);// 字段别名，不需要再次解析
                                column.put("name", alias);
                            } else {
                                column.put("isAlias", false);// 不是字段别名，需要再次解析为表对应的字段
                                column.put("name", exp);
                            }
                            responseJa.add(column);
                        }
                    }
                }
            }

            // 获取别名
            Map<String, SQLTableSource> tmap = postgreSQLVisitor.getAliasMap();
            if (tmap != null && tmap.size() > 0) {
                for (Map.Entry<String, SQLTableSource> entry : tmap.entrySet()) {
                    JSONObject table = new JSONObject();
                    table.put("alias", entry.getKey());
                    String name = entry.getValue().toString();
                    if (name.indexOf(" ") > -1) {
                        String[] tns = name.split(" ");
                        if (tns != null && tns.length > 0) {
                            name = tns[0];
                        }
                    }
                    table.put("name", name);
                    tableJa.add(table);
                }
            }
        }

        // 因insert语句不能准确获取插入表的名称，需要特定处理
        if (formatedSql.indexOf("insert ") > -1 && tableJa.size() == 0) {
            String specialStr = "insert into ";// 需要查找的字符串
            int sii = formatedSql.indexOf(specialStr) + specialStr.length();
            int eii = -1;
            String rightChar = "";
            // 查找sii 右侧第一个空格
            for (int index = sii; index < formatedSql.length(); index++) {
                rightChar = formatedSql.substring(index, index + 1);
                if (rightChar.equals(" ")) {
                    eii = index;
                    break;
                }
            }

            // 获取真实表名
            if (sii > -1 && eii > -1 && eii > sii) {
                String tn = formatedSql.substring(sii, eii);
                JSONObject table = new JSONObject();
                table.put("alias", null);
                table.put("name", tn);
                tableJa.add(table);
            }
        }

        if (formatedSql.indexOf("insert ") < 0) {
            // 获取请求字段(select、update、delete)
            String[] paramBodys = {" between ? and ?", " like ?", " == ?", " = ?", " != ?", " <> ?", " > ?", " < ?",
                    " >= ?", " <= ?", " !< ?", " !> ?", " = to_date(?", " in (?)"};
            for (String paramBody : paramBodys) {
                explainSql4ParamName(0, formatedSql, paramBody, requestJa);
            }
        } else {
            // 获取请求字段(insert)
            String[] paramBodys = {" values "};
            for (String paramBody : paramBodys) {
                explainSql4InsertParamName(formatedSql, paramBody, requestJa);
            }
        }

        // 存储解析之后的sql信息
        sqlObj.put("tableJa", tableJa);
        sqlObj.put("requestJa", requestJa);
        sqlObj.put("responseJa", responseJa);
        sqlObj.put("formatedSql", formatedSql);

        return sqlObj;
    }

    // 5.1 解译SQL UNION关键字
    private void explainSql4Union(SQLUnionQuery sqlUnionQuery, JSONArray responseJa) {
        if (sqlUnionQuery.getClass().equals(SQLUnionQuery.class)) {
            // 解析左侧的select语句
            SQLSelectQueryBlock query = (SQLSelectQueryBlock) sqlUnionQuery.getLeft();
            List<SQLSelectItem> ssi = query.getSelectList();
            for (SQLSelectItem si : ssi) {
                JSONObject column = new JSONObject();
                String exp = si.getExpr().toString();
                String alias = si.getAlias();

                if (alias != null) {
                    column.put("isAlias", true);// 字段别名，不需要再次解析
                    column.put("name", alias);
                } else {
                    column.put("isAlias", false);// 不是字段别名，需要再次解析为表对应的字段
                    column.put("name", exp);
                }

                // 判断字段是否已经添加到响应数组中
                if (responseJa != null) {
                    if (!responseJa.contains(column)) {
                        responseJa.add(column);
                    }
                }
            }

            // 解析右侧的sql语句,可能为union语句也可能为select语句
            if (sqlUnionQuery.getRight().getClass().equals(SQLUnionQuery.class)) {
                explainSql4Union((SQLUnionQuery) sqlUnionQuery.getRight(), responseJa);
            } else {
                // 解析右侧的select语句
                query = (SQLSelectQueryBlock) sqlUnionQuery.getRight();
                ssi = query.getSelectList();
                for (SQLSelectItem si : ssi) {
                    JSONObject column = new JSONObject();
                    String exp = si.getExpr().toString();
                    String alias = si.getAlias();

                    if (alias != null) {
                        column.put("isAlias", true);// 字段别名，不需要再次解析
                        column.put("name", alias);
                    } else {
                        column.put("isAlias", false);// 不是字段别名，需要再次解析为表对应的字段
                        column.put("name", exp);
                    }

                    // 判断字段是否已经添加到响应数组中
                    if (responseJa != null) {
                        if (!responseJa.contains(column)) {
                            responseJa.add(column);
                        }
                    }
                }
            }
        }
    }

    // 6、根据参数化最终语句获取请求字段
    // 6.1
    private void explainSql4InsertParamName(String formatedSql, String paramBody, JSONArray requestJa) {
        // eg：insert into student(name,class_id,sex,student_id) values (?,?,?,?)
        int fpbi = formatedSql.indexOf(paramBody);// 请求体索引
        // 分别截取请求体左侧和右侧的参数和通配符列表
        if (fpbi > -1) {
            // 查找左侧第一个"("
            String leftSeqChar = "";
            int leftSeqIndex = -1;
            for (int index = fpbi; index >= 1; index--) {
                leftSeqChar = formatedSql.substring(index - 1, index);
                if (leftSeqChar.trim().equals("(")) {
                    leftSeqIndex = index;
                    break;
                }
            }

            String leftParamNames = "";
            if (leftSeqIndex > -1) {
                // 截取请求体名称部分
                leftParamNames = formatedSql.substring(leftSeqIndex, fpbi - 1);
                // System.out.println("leftParamNames:" + leftParamNames);
            }

            // 查找右侧第一个")"
            String rightSeqChar = "";
            int rightSeqIndex = -1;
            for (int index = fpbi + paramBody.length(); index < formatedSql.length(); index++) {
                rightSeqChar = formatedSql.substring(index, index + 1);
                // System.out.println(rightSeqChar);
                if (rightSeqChar.trim().equals(")")) {
                    rightSeqIndex = index;
                    break;
                }
            }

            String rightSymbols = "";
            if (rightSeqIndex > -1) {
                // 截取请求体名称部分
                rightSymbols = formatedSql.substring(fpbi + paramBody.length() + 1, rightSeqIndex);
                // System.out.println("rightSymbols:" + rightSymbols);
            }

            // 请求体名称及请求体符号均不为空时
            if (!leftParamNames.equals("") && !rightSymbols.equals("")) {
                String[] lpns = leftParamNames.split(",");
                String[] rss = rightSymbols.split(",");

                int lpnsLen = lpns.length;
                int rssLen = rss.length;
                if (lpnsLen > 0 && rssLen > 0 && lpnsLen == rssLen) {
                    for (int i = 0; i < rssLen; i++) {
                        JSONObject paramObj = new JSONObject();
                        String param = lpns[i];
                        paramObj.put("isAlias", false);// 不是字段别名，需要再次解析为表对应的字段
                        paramObj.put("name", param);// 参数名称
                        paramObj.put("index", (fpbi + paramBody.length() + 1) + 2 * i);// 请求体索引
                        paramObj.put("body", "?");// 请求体
                        paramObj.put("type", "normal");
                        requestJa.add(paramObj);
                    }
                }
            }

            // System.out.println("requestJa:" + requestJa.toJSONString());
        }
    }

    // 6.2
    private String getParam(int startIndex, String formatedSql, String paramBody) {
        int fpbi = formatedSql.indexOf(paramBody, startIndex);// 请求体索引
        if (fpbi > -1) {
            // 查找paramBody左侧出现的第一个空格位置
            String leftSql = formatedSql.substring(0, fpbi);

            // 如果左侧出现第一个空格
            String seqChar = "";
            int seqIndex = -1;
            for (int index = fpbi; index >= 1; index--) {
                seqChar = leftSql.substring(index - 1, index);
                if (seqChar.trim().equals("")) {
                    seqIndex = index;
                    break;
                }
            }

            // 如果左侧出现第一个逗号
            String seqCharS = ",";
            int seqIndexS = -1;
            for (int index = fpbi; index >= 1; index--) {
                seqCharS = leftSql.substring(index - 1, index);
                if (seqCharS.trim().equals(",")) {
                    seqIndexS = index;
                    break;
                }
            }

            // 若两者都大于-1取大值
            if (seqIndex > -1 && seqIndexS > -1) {
                seqIndex = Math.max(seqIndex, seqIndexS);
            } else if (seqIndex == -1 && seqIndexS > -1) {
                seqIndex = seqIndexS;
            }

            //截取参数
            String param = formatedSql.substring(seqIndex, fpbi);

            return param;
        }
        return null;
    }

    /**
     * @param startIndex  : 解析起始位置
     * @param formatedSql : 待解析的sql语句
     * @param paramBody   : sql解析体
     * @param requestJa   : 请求参数列表
     */
    private void explainSql4ParamName(int startIndex, String formatedSql, String paramBody, JSONArray requestJa) {
        int fpbi = formatedSql.indexOf(paramBody, startIndex);// 请求体索引
        if (fpbi > -1) {
            String param = getParam(startIndex, formatedSql, paramBody);

            //@todo 这里需要考虑组合关键字的处理，如not in，后续逐渐补充对特殊关键字的处理
            String paramBody4Special = null;
            if (param.equalsIgnoreCase("not")) {
                //需要特殊处理的查询体
                paramBody4Special = " not" + paramBody;
                fpbi = formatedSql.indexOf(paramBody4Special, startIndex);// 重新计算请求体索引
                param = getParam(startIndex, formatedSql, paramBody4Special);
            }

            JSONObject paramObj = new JSONObject();
            paramObj.put("name", param);// 参数名称
            paramObj.put("index", fpbi);// 请求体索引
            paramObj.put("body", paramBody);// 请求体
            if (paramBody.equals(" between ? and ?")) {
                paramObj.put("type", "between");
            } else if (paramBody.equals(" like ?")) {
                paramObj.put("type", "like");
            } else if (paramBody.equals(" in (?)")) {
                paramObj.put("type", "in");
            } else {
                paramObj.put("type", "normal");
            }
            requestJa.add(paramObj);
            if (paramBody4Special == null) {
                String restSql = formatedSql.substring(fpbi + paramBody.length());
                if (restSql.indexOf(paramBody) > -1) {
                    explainSql4ParamName(fpbi + paramBody.length(), formatedSql, paramBody, requestJa);
                }
            } else {
                String restSql = formatedSql.substring(fpbi + paramBody4Special.length());
                if (restSql.indexOf(paramBody) > -1) {
                    explainSql4ParamName(fpbi + paramBody4Special.length(), formatedSql, paramBody, requestJa);
                }
            }
        }
    }
}

/**
 * @author LBM
 * @time 2017年12月29日
 * @project codecreator
 * @type ExportTableAliasVisitor
 * @desc 针对MySQL数据库自定义Visitor，用于获取SQL语句中别名对应的表名
 */
class ExportMySqlTableAliasVisitor extends MySqlASTVisitorAdapter {
    private Map<String, SQLTableSource> aliasMap = new HashMap<String, SQLTableSource>();

    @Override
    public boolean visit(SQLExprTableSource x) {
        String alias = x.getAlias();
        aliasMap.put(alias, x);
        return true;
    }

    public Map<String, SQLTableSource> getAliasMap() {
        return aliasMap;
    }
}

/**
 * @author LBM
 * @time 2017年12月29日
 * @project codecreator
 * @type ExportOracleTableAliasVisitor
 * @desc 针对Oracle数据库自定义Visitor，用于获取SQL语句中别名对应的表名
 */
class ExportOracleTableAliasVisitor extends OracleASTVisitorAdapter {
    private Map<String, SQLTableSource> aliasMap = new HashMap<String, SQLTableSource>();

    @Override
    public boolean visit(OracleSelectTableReference x) {
        String alias = x.getAlias();
        aliasMap.put(alias, x);
        return true;
    }

    public Map<String, SQLTableSource> getAliasMap() {
        return aliasMap;
    }
}

/**
 * @author LBM
 * @time 2017年12月29日
 * @project codecreator
 * @type ExportOracleTableAliasVisitor
 * @desc 针对Oracle数据库自定义Visitor，用于获取SQL语句中别名对应的表名
 */
class ExportPostgreSQLTableAliasVisitor extends PGASTVisitorAdapter {
    private Map<String, SQLTableSource> aliasMap = new HashMap<String, SQLTableSource>();

    @Override
    public boolean visit(SQLExprTableSource x) {
        String alias = x.getAlias();
        aliasMap.put(alias, x);
        return true;
    }

    public Map<String, SQLTableSource> getAliasMap() {
        return aliasMap;
    }
}
