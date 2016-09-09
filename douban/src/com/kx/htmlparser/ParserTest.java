package com.kx.htmlparser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ParserTest {
	// 分析主目录
	public void menuParse(String baseURL, String content) throws Exception {
	}
	public void itemsParse(String baseURL, String content) throws Exception {
	}
	// 获取每个目录的下一页
	public void nextPageParse(String baseURL, String content) throws Exception {
		// unencode rel
	}
	// 获取每个item页面的专辑链接
	public void albumParse(String baseURL, String content) throws Exception {
	}
	// 获取每个专辑中的条目
	public void itemInAlbumParse(String baseURL, String content) throws Exception {
	}
	public void contentInItemOfAlbumParse(String baseURL, String content) throws Exception {
	}
	// 针对每一个具体的声音进行分析
	public void musicParse(String baseURL, String content) throws Exception {
	}
	public static void main(String argv[]) throws Exception {
		BufferedReader bw = null;
		try {
			bw = new BufferedReader(new InputStreamReader(new FileInputStream("C:/Users/孔新/Desktop/t.txt")));
		} catch (Exception e) {
		}
		String tmp = null;
		String content = "";
		while ((tmp = bw.readLine()) != null) {
			content += tmp;
		}
		new ParserTest().musicParse("http://www.ximalaya.com/dq/all/", content);
	}
}
