package com.kx.htmlparser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ParserTest {
	// ������Ŀ¼
	public void menuParse(String baseURL, String content) throws Exception {
	}
	public void itemsParse(String baseURL, String content) throws Exception {
	}
	// ��ȡÿ��Ŀ¼����һҳ
	public void nextPageParse(String baseURL, String content) throws Exception {
		// unencode rel
	}
	// ��ȡÿ��itemҳ���ר������
	public void albumParse(String baseURL, String content) throws Exception {
	}
	// ��ȡÿ��ר���е���Ŀ
	public void itemInAlbumParse(String baseURL, String content) throws Exception {
	}
	public void contentInItemOfAlbumParse(String baseURL, String content) throws Exception {
	}
	// ���ÿһ��������������з���
	public void musicParse(String baseURL, String content) throws Exception {
	}
	public static void main(String argv[]) throws Exception {
		BufferedReader bw = null;
		try {
			bw = new BufferedReader(new InputStreamReader(new FileInputStream("C:/Users/����/Desktop/t.txt")));
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
