package com.acs.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * @author LBM
 * @time 2017年11月29日 下午1:44:28 项目名称：codecreator 类名称：FileUtil 类描述：文件处理工具类
 */
public class FileUtil {
	/**
	 * 复制一个目录及其子目录、文件到另外一个目录
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
			}
			String files[] = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// 递归复制
				copyFolder(srcFile, destFile);
			}
		} else {
			FileInputStream in = new FileInputStream(src);
			FileOutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[500];

			int length = 0;
			//int sumLen = 0;

			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
				//sumLen += length;
			}

			in.close();
			out.flush();
			out.close();
			//System.out.println(src.getAbsolutePath() + "文件长度：" + sumLen);
		}
	}

	/**
	 * 删除空目录
	 * 
	 * @param dir
	 *            将要删除的目录路径
	 */
	public boolean doDeleteEmptyDir(String dir) {
		return (new File(dir)).delete();
	}

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 * 
	 * @param dir
	 *            将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
	public boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	/**
	 * 覆盖相同文件内容
	 * 
	 * @param filePath
	 *            文件所在目录
	 * @param fileName
	 *            文件名称
	 * @param fileType
	 *            文件类型
	 * @param fileContent
	 *            文件内容
	 * @throws IOException
	 */
	public void writeFile(String filePath, String fileName, String fileType, String fileContent) throws IOException {
		File f = new File(filePath + fileName + fileType);
		FileOutputStream fos = new FileOutputStream(f);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(fileContent);
		bw.flush();
		bw.close();
		fos.close();
	}

	/**
	 * @param dirPath
	 *            当前传入的根目录
	 * @param dirTree
	 *            用于存储目录文件结构的JSON对象
	 */
	public void scanDirectory(String dirPath, JSONObject dirTree) {
		// 建立当前目录中文件的File对象
		File file = new File(dirPath);
		if (file.isDirectory()) {
			String name = file.getName();// 名称
			String path = file.getPath();// 路径
			dirTree.put("text", name);
			dirTree.put("path", path);
			dirTree.put("type", "");
			dirTree.put("leaf", false);
			dirTree.put("children", new JSONArray());
			// 取得代表目录中所有文件的File对象数组
			File[] fileList = file.listFiles();
			int fLen = fileList.length;
			// 判断是否为空目录
			if (fLen > 0) {
				// 遍历该目录下的所有文件
				for (int i = 0; i < fLen; i++) {
					File subFile = fileList[i];
					String subName = subFile.getName();// 名称
					String subPath = subFile.getPath();// 路径
					JSONObject fileObj = new JSONObject();
					fileObj.put("text", subName);
					fileObj.put("path", subPath);
					if (subFile.isDirectory()) {
						fileObj.put("type", "");
						fileObj.put("children", new JSONArray());
						fileObj.put("leaf", false);
						scanDirectory(subPath, fileObj);
					} else {
						String type = subName.substring(subName.lastIndexOf(".") + 1, subName.length());
						fileObj.put("type", type);
						fileObj.put("leaf", true);
					}
					dirTree.getJSONArray("children").add(fileObj);
				}
			}
		}
	}

	/*仅用于测试*/
	public static void main(String[] args) {
		FileUtil fu = new FileUtil();
		JSONObject dirTree = new JSONObject();
		String dirPath = "E:\\acpws\\jmgc";
		fu.scanDirectory(dirPath, dirTree);
		System.out.println(dirTree.toJSONString());
	}

	/**
	 * 创建目录
	 *
	 * @param destDirName
	 *            目标目录名
	 * @return 目录创建成功返回true，否则返回false
	 */
	public static boolean createDir(String destDirName) {
		File dir = new File(destDirName);
		if (dir.exists()) {
			return false;
		}
		if (!destDirName.endsWith(File.separator)) {
			destDirName = destDirName + File.separator;
		}
		// 创建单个目录
		if (dir.mkdirs()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 删除文件
	 *
	 * @param filePathAndName String 文件路径及名称 如c:/fqf.txt
	 * @return boolean
	 */
	public static void delFile(String filePathAndName) {
		try {
			String filePath = filePathAndName;
			filePath = filePath.toString();
			File myDelFile = new File(filePath);
			myDelFile.delete();

		} catch (Exception e) {
			System.out.println("删除文件操作出错");
			e.printStackTrace();

		}

	}

	/**
	 * 读取到字节数组0
	 * 
	 * @param filePath
	 *            //路径
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public static byte[] getContent(String filePath) throws IOException {
		File file = new File(filePath);
		long fileSize = file.length();
		if (fileSize > Integer.MAX_VALUE) {
			System.out.println("file too big...");
			return null;
		}
		FileInputStream fi = new FileInputStream(file);
		byte[] buffer = new byte[(int) fileSize];
		int offset = 0;
		int numRead = 0;
		while (offset < buffer.length && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
			offset += numRead;
		}
		// 确保所有数据均被读取
		if (offset != buffer.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}
		fi.close();
		return buffer;
	}

	/**
	 * 读取到字节数组1
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray(String filePath) throws IOException {

		File f = new File(filePath);
		if (!f.exists()) {
			throw new FileNotFoundException(filePath);
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(f));
			int buf_size = 1024;
			byte[] buffer = new byte[buf_size];
			int len = 0;
			while (-1 != (len = in.read(buffer, 0, buf_size))) {
				bos.write(buffer, 0, len);
			}
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			bos.close();
		}
	}

	/**
	 * 读取到字节数组2
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray2(String filePath) throws IOException {

		File f = new File(filePath);
		if (!f.exists()) {
			throw new FileNotFoundException(filePath);
		}

		FileChannel channel = null;
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(f);
			channel = fs.getChannel();
			ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
			while ((channel.read(byteBuffer)) > 0) {
				// do nothing
				// System.out.println("reading");
			}
			return byteBuffer.array();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Mapped File way MappedByteBuffer 可以在处理大文件时，提升性能
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray3(String filePath) throws IOException {

		FileChannel fc = null;
		RandomAccessFile rf = null;
		try {
			rf = new RandomAccessFile(filePath, "r");
			fc = rf.getChannel();
			MappedByteBuffer byteBuffer = fc.map(MapMode.READ_ONLY, 0, fc.size()).load();
			// System.out.println(byteBuffer.isLoaded());
			byte[] result = new byte[(int) fc.size()];
			if (byteBuffer.remaining() > 0) {
				// System.out.println("remain");
				byteBuffer.get(result, 0, byteBuffer.remaining());
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				rf.close();
				fc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
