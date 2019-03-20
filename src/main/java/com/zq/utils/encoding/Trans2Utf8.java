package com.zq.utils.encoding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Trans2Utf8 {
	public static void transferFile(String dirPath) throws Exception {
		File dirFile = new File(dirPath);
		// 获取此目录下的所有文件名与目录名
		String[] fileList = dirFile.list();
		for (int i = 0; i < fileList.length; i++) {
			String string = fileList[i];
			File file = new File(dirFile.getPath(), string);
			String name = file.getName();
			// 如果是一个目录，搜索深度depth++，输出目录名后，进行递归
			if (file.isDirectory()) {
				transferFile(file.getCanonicalPath());
			} else {
				if (name.contains(".java") || name.contains(".properties") || name.contains(".xml")) {
					readAndWrite(file);
					System.out.println(name + " has converted to utf8 ");
				}
			}
		}
	}

	private static void readAndWrite(File file) throws Exception {
		String content = readFileByEncode(file.getPath(), "GBK");
		writeByBufferedReader(file.getPath(), new String(content.getBytes("UTF-8"), "UTF-8"));
	}

	public static void main(String[] args) throws Exception {
		// 程序入口，制定src的path
		String path = "C:\\Users\\zhide\\Desktop\\message";
		transferFile(path);
	}

	public static void writeByBufferedReader(String path, String content) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			File file = new File(path);
			file.delete();
			if (!file.exists()) {
				file.createNewFile();
			}
			fw = new FileWriter(file, false);
			bw = new BufferedWriter(fw);
			bw.write(content);
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String readFileByEncode(String path, String chatSet) throws Exception {
		InputStream input = new FileInputStream(path);
		InputStreamReader in = new InputStreamReader(input, chatSet);
		BufferedReader reader = new BufferedReader(in);
		StringBuffer sb = new StringBuffer();
		String line = reader.readLine();
		while (line != null) {
			sb.append(line);
			sb.append("\r\n");
			line = reader.readLine();
		}
		return sb.toString();
	}
}