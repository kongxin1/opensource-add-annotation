package com.kx.download;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

//报文头未处理
public class Download {
	public String download(String url, String cookie) throws IOException {
		// URL u = new URL(url);
		// u.get
		HttpClient client = new HttpClient();
		GetMethod postMethod = new GetMethod(url);
		// 设置参数
		client.getHttpConnectionManager().getParams().setConnectionTimeout(50000);// 设置连接时间
		// 设置报文头
		// List<Header> headers = new ArrayList<Header>();
		// postMethod.addRequestHeader(headerName, headerValue);
		// postMethod
		// .addRequestHeader(new Header("User-Agent",
		// "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.110 Safari/537.36"));
		// postMethod.addRequestHeader(new Header("Host", "www.ximalaya.com"));
		// postMethod.addRequestHeader(new Header("Accept-Language", "zh-CN,zh;q=0.8"));
		// postMethod.addRequestHeader(new Header("Accept",
		// "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
		// postMethod.addRequestHeader(new Header("Accept-Encoding", "gzip, deflate, sdch"));
		// postMethod.addRequestHeader(new Header("Cache-Control", "max-age=0"));
		// postMethod.addRequestHeader(new Header("Connection", "keep-alive"));
		// postMethod
		// .addRequestHeader(new Header(
		// "Cookie",
		// "Hm_lvt_4a7d8ec50cfd6af753c4f8aee3425070=1465460518; Hm_lpvt_4a7d8ec50cfd6af753c4f8aee3425070=1465478822; _ga=GA1.2.793778404.1465460518; _gat=1"));
		// client.getHostConfiguration().getParams().setParameter("http.default-headers", headers);
		try {
			int status = client.executeMethod(postMethod);
			System.out.println(status);
			String response = null;
			if (status == HttpStatus.SC_OK) {
				InputStream inputStream = postMethod.getResponseBodyAsStream();
				BufferedInputStream bis = new BufferedInputStream(inputStream);
				int start = 0;
				int length = 0;
				int end = 10240 * 15;
				int step = end;
				byte[] content = new byte[end];// 初始15K
				while ((length = bis.read(content, start, end - start)) != -1) {
					start = start + length;
					if (start >= end - 100) {
						byte[] tmp = new byte[end + step];
						System.arraycopy(content, 0, tmp, 0, start);
						end = end + step;
						content = tmp;
					}
				}
				try {
					response = new String(content, 0, start, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} else {
				response = "fail";
			}
			return response;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			postMethod.releaseConnection();
		}
	}
	public static void main(String argv[]) throws Exception {
		// %E6%96%B0%E5%A6%88%E5%90%AC%E5%90%AC%E7%9C%8B/
		String content = new Download().download("https://book.douban.com/tag/?icn=index-nav", null);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:/Users/孔新/Desktop/t.txt")));
		} catch (Exception e) {
		}
		bw.write(content);
		bw.close();
	}
}
