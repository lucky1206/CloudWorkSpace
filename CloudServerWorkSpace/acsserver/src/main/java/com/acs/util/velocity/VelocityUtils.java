package com.acs.util.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author LBM
 * @time 2017年12月1日
 * @project codecreator
 * @type VelocityUtils
 * @desc VM模版引擎工具
 */
public class VelocityUtils {
	private String _templatePath;

	public VelocityUtils(String templatePath) {
		this._templatePath = templatePath;
	}

	@SuppressWarnings("unused")
	private Log log = LogFactory.getLog(VelocityUtils.class);

	private VelocityEngine velocityEngine(String templatePath) throws VelocityException, IOException {
		// 写绝对路径
		VelocityEngine ve = new VelocityEngine();
		// velocity配置参考
		/*
		 * <property name="velocityPropertiesMap"> <props> <prop
		 * key="input.encoding">UTF-8</prop> <prop key="output.encoding">UTF-8</prop>
		 * <prop key="directive.set.null.allowed">true</prop> <prop
		 * key="velocimacro.library.autoreload">false</prop> //可不配置, 默认即为false <prop
		 * key="velocimacro.context.localscope">true</prop> <prop
		 * key="file.resource.loader.cache">true</prop> //打开cache开关 <prop
		 * key="file.resource.loader.modificationCheckInterval">60</prop>
		 * //load的间隔时间：其实若无动态修改的需求, 此处可改为-1，即只在启动时load一次, 此后不再load <prop
		 * key="resource.manager.defaultcache.size">0</prop>
		 * //resource.manager.defaultcache.size=0表示不限制cache大小 <prop
		 * key="velocimacro.library">macro/cps_web_combo.vm,macro/cps_web_global.vm,
		 * macro/cps_gs.vm</prop> </props> </property>
		 */

		ve.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, templatePath);
		//version 2.0 config
		ve.setProperty("input.encoding", "UTF-8");
		ve.setProperty("output.encoding", "UTF-8");
		ve.setProperty("directive.set.null.allowed", true);
		ve.setProperty("velocimacro.library.autoreload", false);//可不配置, 默认即为false
		ve.setProperty("velocimacro.context.localscope", true);
		ve.setProperty("file.resource.loader.cache", true);
		ve.setProperty("resource.manager.defaultcache.size", 0);//resource.manager.defaultcache.size=0表示不限制cache大小
		ve.setProperty("directive.foreach.counter.name", "velocityCount");// 其中velocityCount可以用其他词语代替，如‘c’
		ve.setProperty("directive.foreach.counter.initial.value", 1);// 设置计数器从1开始
		/**
		 * velocity
		 * 引擎启动之后，其尝试将日志文件写入tomcat所在目录文件中去，所以你可以强制将日志写入tomcat标准日志中去，按照以下所配置的属性
		 * tomcat服务器启动之后，只要在之后操作中启动velocity引擎，就会看到其相关日志输出，本有velocity.properties配置文件
		 */
		ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
		ve.setProperty("runtime.log.logsystem.log4j.category", "velocity");
		ve.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
		
		/*在服务根目录下输出velocity.log日志文件
		 * ve.setProperty("runtime.log",
		 * Thread.currentThread().getContextClassLoader().getResource("/").getPath().
		 * substring(1) + "velocity.log");
		 */

		/*在classes目录下输出velocity.log日志文件
		 * String classesPath =
		 * Thread.currentThread().getContextClassLoader().getResource("/").getPath().
		 * substring(1); String logPath = classesPath.substring(0,
		 * classesPath.indexOf("WEB-INF")); ve.setProperty("runtime.log", logPath +
		 * "velocity.log");
		 */

		// 打印velocity日志文件路径
		// System.out.println(logPath);
		/*
		 * ve.setProperty("runtime.log",
		 * StringUtils.class.getClassLoader().getResource("/").getPath().substring(1) +
		 * "velocity.log");
		 */

		try {
			// 初始化velocity模版引擎
			ve.init();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ve;
	}

	/**
	 * 模版解译
	 * 
	 * @param map
	 * @param templateName
	 * @return 合并之后的文本内容
	 */
	public String mergeTemplate(Map<String, Object> map, String templateName) {
		VelocityContext context = new VelocityContext(map);

		try {
			Template template = velocityEngine(this._templatePath).getTemplate(templateName);
			StringWriter sw = new StringWriter();

			if (template != null)
				template.merge(context, sw);
			sw.flush();
			sw.close();
			return sw.toString();
		} catch (ResourceNotFoundException e) {
			e.printStackTrace();
		} catch (ParseErrorException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
