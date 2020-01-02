/**
 * 
 */
package com.acs.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LBM
 * @time 2018年2月11日
 * @project jzac
 * @type JSONParserUtil
 * @desc JSON文件处理类
 */
public class JSONParserUtil {
	/**
	 * 读取文本内容
	 * 
	 * @param fileName
	 * @return
	 */
	private String readToString(String fileName) {
		String encoding = "UTF-8";
		File file = new File(fileName);
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(filecontent);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return new String(filecontent, encoding);
		} catch (UnsupportedEncodingException e) {
			System.err.println("操作系统不支持：" + encoding);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 去掉文本中所有\s分类字符
	 * 
	 * @param str
	 * @return
	 */
	private String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	/**
	 * 更新Web Module文件
	 * 
	 * @param path
	 * @param content
	 * @throws IOException
	 */
	public void updateWebModule(String path, JSONObject content) throws IOException {
		//FileWriter out;
		try {
			String oldStr = null, jsonStr = null, jsonWs = null;
			oldStr = this.readToString(path);
			jsonStr = this.replaceBlank(oldStr);
			JSONObject jsonData = new JSONObject(jsonStr);
			JSONArray children = jsonData.getJSONArray("children");
			int moduleLen = children.length();
			String moduleId = content.getString("id");
			int moduleIndex = -1;

			if (moduleLen > 0) {
				for (int i = 0; i < moduleLen; i++) {
					JSONObject module = (JSONObject) children.get(i);
					String tempId = module.getString("id");
					if (moduleId.equals(tempId)) {
						// 删除已经存在模块配置
						children.remove(i);
						moduleIndex = i;
						break;
					}
				}
			}
			if (moduleIndex != -1) {
				children.put(moduleIndex, content);
			} else {
				children.put(content);
			}
			jsonWs = jsonData.toString();
			//out = new FileWriter(path);
			File f = new File(path);
			FileOutputStream fos = new FileOutputStream(f);
			OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter jsonBw = new BufferedWriter(out);// 输出内容
			jsonBw.write(jsonWs);
			jsonBw.flush();
			jsonBw.close();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 更新Web Config文件
	 * @throws IOException
	 */
	public void updateWebConfig(String path, JSONObject content) throws IOException {
		//FileWriter out;
		try {
			String oldStr = null, jsonStr = null, jsonWs = null;
			oldStr = this.readToString(path);
			jsonStr = this.replaceBlank(oldStr);
			JSONObject jsonData = new JSONObject(jsonStr);
			// 更新内容
			for (Iterator<String> iterator = content.keys(); iterator.hasNext();) {
				String key = (String) iterator.next();
				jsonData.put(key, content.get(key));
			}

			jsonWs = jsonData.toString();
			//out = new FileWriter(path);
			File f = new File(path);
			FileOutputStream fos = new FileOutputStream(f);
			OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter jsonBw = new BufferedWriter(out);// 输出内容
			jsonBw.write(jsonWs);
			jsonBw.flush();
			jsonBw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		}
	}

	/*@Test
	public void testJson() throws IOException {
		JSONParserUtil jpu = new JSONParserUtil();
		JSONObject content = new JSONObject();
		content.put("text", "module1");
		content.put("id", "module4Id");
		content.put("url", "xmodule1");
		content.put("leaf", true);
		jpu.updateWebModule("D:\\SystemModule.json", content);
		
		jpu.updateWebConfig("D:\\SystemConfig.json", content);
	}*/
}
