/**
 *
 */
package com.acs.controller;

import com.acs.common.AcsConstants;
import com.acs.util.download.FileDownload;
import com.acs.util.download.FileZip;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;

/**
 * @author LBM
 * @time 2018年1月11日
 * @project codecreator
 * @type VersionController
 * @desc 工程版本管理及下载功能类
 */
@Api(value = "version", description = "版本管理及资源下载", tags = "版本管理及资源下载")
@Controller
@Scope("session") // 将bean 的范围设置成session，表示当前bean是会话级别且线程安全的变量。
@RequestMapping(value = "/version", method = {RequestMethod.GET, RequestMethod.POST})
public class VersionController {
    // Session范围全局变量,工程配置类
    private FileZip fz;

    // @PostConstruct当bean加载完之后，就会执行init方法，并且将pps实例化；
    // @PostConstruct
    @ModelAttribute
    public void init() {
        fz = new FileZip();

        //System.out.println("VersionController控制器调用开启");
    }

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected HttpSession session;

    // 控制器每次调用时都会调用通过ModelAttribute注解的方法,注意与init区分,如果在方法体中直接声明，Spring也会自动给他们赋值。
    @ModelAttribute
    public void setReqAndRes(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.session = request.getSession();
        // System.out.println("当前控制器被请求了...");
    }

    @RequestMapping(value = "/download")
    @ResponseBody
    @ApiOperation(value = "工程文件一键下载", httpMethod = "GET", notes = "根据工程名称一键下载")
    public void oneKey4Download(
            @ApiParam(required = true, value = "工程名称", name = "projectName") @RequestParam(name = "projectName") String projectName)
            throws Exception {
        String sfp = Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1);
        String disk = (String) sfp.subSequence(0, 2);// 获取盘符
        // 工程存放目录(该目录还需要存放工程配置及类配置文件)
        String rootPath = disk + "\\" + AcsConstants.PROJECTWS;
        String tfp = rootPath + "\\" + projectName;

        File tfpf = new File(tfp);
        if (tfpf.exists()) {
            // 压缩文件存放目录
            String zipWsPath = rootPath + "\\" + "zips\\";

            File zipTargetDir = new File(zipWsPath);
            if (!zipTargetDir.exists()) {
                zipTargetDir.mkdir();
            }

            if (response != null) {
                /* 生成的全部代码压缩成zip文件 */
                fz.zip(tfp, zipWsPath + projectName + ".zip");
                // 下载生成的压缩文件
                FileDownload.fileDownload(response, zipWsPath + projectName + ".zip", projectName + ".zip");
            }
        }
    }

    @RequestMapping(value = "/source")
    @ResponseBody
    @ApiOperation(value = "工程源文件一键下载", httpMethod = "GET", notes = "根据工程名称一键下载源代码")
    public void oneKey4DownloadSource(
            @ApiParam(required = true, value = "工程名称", name = "projectName") @RequestParam(name = "projectName") String projectName)
            throws Exception {
        String sfp = Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1);
        String disk = (String) sfp.subSequence(0, 2);// 获取盘符
        // 工程存放目录(该目录还需要存放工程配置及类配置文件)
        String rootPath = disk + "\\" + AcsConstants.PROJECTWS;
        String tfp = rootPath + "\\" + projectName;

        File tfpf = new File(tfp);
        if (tfpf.exists()) {
            // 压缩文件存放目录
            String zipWsPath = rootPath + "\\" + "zips\\";

            File zipTargetDir = new File(zipWsPath);
            if (!zipTargetDir.exists()) {
                zipTargetDir.mkdir();
            }

            if (response != null) {
                String sourceName = "Sources.zip";

                /* 生成的全部代码文件压缩成zip文件 */
                fz.zip(tfp + "\\src\\main\\java", zipWsPath + projectName + sourceName);

                // 下载生成的源代码压缩文件
                FileDownload.fileDownload(response, zipWsPath + projectName + sourceName, projectName + sourceName);
            }
        }
    }

    @RequestMapping(value = "/mapping")
    @ResponseBody
    @ApiOperation(value = "工程MyBatis映射文件一键下载", httpMethod = "GET", notes = "根据工程名称一键下载MyBatis映射文件")
    public void oneKey4DownloadMapping(
            @ApiParam(required = true, value = "工程名称", name = "projectName") @RequestParam(name = "projectName") String projectName)
            throws Exception {
        String sfp = Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1);
        String disk = (String) sfp.subSequence(0, 2);// 获取盘符
        // 工程存放目录(该目录还需要存放工程配置及类配置文件)
        String rootPath = disk + "\\" + AcsConstants.PROJECTWS;
        String tfp = rootPath + "\\" + projectName;

        File tfpf = new File(tfp);
        if (tfpf.exists()) {
            // 压缩文件存放目录
            String zipWsPath = rootPath + "\\" + "zips\\";

            File zipTargetDir = new File(zipWsPath);
            if (!zipTargetDir.exists()) {
                zipTargetDir.mkdir();
            }

            if (response != null) {
                String mappingName = "Mappings.zip";

                /* 生成的全部映射文件压缩成zip文件 */
                fz.zip(tfp + "\\src\\main\\resources\\mapping", zipWsPath + projectName + mappingName);

                // 下载生成的映射压缩文件
                FileDownload.fileDownload(response, zipWsPath + projectName + mappingName, projectName + mappingName);
            }
        }
    }

    @RequestMapping(value = "/webproject")
    @ResponseBody
    @ApiOperation(value = "Web工程文件一键下载", httpMethod = "GET", notes = "根据工程名称一键下载Web工程文件")
    public void oneKey4DownloadWebProject(@ApiParam(required = true, value = "工程名称", name = "projectName") @RequestParam(name = "projectName") String projectName) throws Exception {
        String sfp = Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1);
        String disk = (String) sfp.subSequence(0, 2);// 获取盘符
        // 工程存放目录(该目录还需要存放工程配置及类配置文件)
        String rootPath = disk + "\\" + AcsConstants.PROJECTWS + "\\web";
        String tfp = rootPath + "\\" + projectName;

        File tfpf = new File(tfp);
        if (tfpf.exists()) {
            // 压缩文件存放目录
            String zipWsPath = rootPath + "\\" + "zips\\";

            File zipTargetDir = new File(zipWsPath);
            if (!zipTargetDir.exists()) {
                zipTargetDir.mkdir();
            }

            if (response != null) {
                /* 生成的全部代码压缩成zip文件 */
                fz.zip(tfp, zipWsPath + projectName + ".zip");
                // 下载生成的压缩文件
                FileDownload.fileDownload(response, zipWsPath + projectName + ".zip", projectName + ".zip");
            }
        }
    }

    @RequestMapping(value = "/getfilecontent")
    @ResponseBody
    @ApiOperation(value = "预览文件内容", httpMethod = "GET", notes = "根据文件路径预览文件内容")
    public JSONObject getFileContent(@ApiParam(required = true, value = "服务器上文件路径", name = "path") @RequestParam(name = "path") String path) throws IOException {
        File file = new File(path);//定义一个file对象，用来初始化FileReader
        FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
        BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
        StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
        String s;
        while ((s = bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
            sb.append(s + "\n");//将读取的字符串添加换行符后累加存放在缓存中
        }
        bReader.close();
        String str = sb.toString();
        JSONObject jo = new JSONObject();
        if (!str.equals("") && str.trim().length() > 0) {
            jo.put("success", true);
            jo.put("data", str);
        } else {
            jo.put("success", false);
            jo.put("data", "");
        }
        return jo;
    }
}
