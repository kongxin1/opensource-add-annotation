package com.kx.htmlparser;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kx.db.MysqlConnection;
import com.kx.domain.ClawerMessage;

//分析主目录
public class MenuParser implements Parser {
	private Logger logger = LoggerFactory.getLogger(MenuParser.class);
	private MysqlConnection mysql = new MysqlConnection();

	public List<ClawerMessage> parse(String baseURL, ClawerMessage msg) throws Exception {
		Document doc = Jsoup.parse(msg.getStr(), baseURL);
		Elements eles = doc.select("a.tagBtn ");
		String href = null;
		String tid = null;
		List<ClawerMessage> urls = new ArrayList<ClawerMessage>();
		for (Element ele : eles) {
			href = ele.attr("href");
			tid = ele.attr("tid");
			URL url = null;
			try {
				url = new URL(new URL(baseURL), href);
				if (!mysql.isExist(url.toString())) {
					ClawerMessage cm = new ClawerMessage();
					cm.setOrder(msg.getOrder() + 1);
					cm.setUrl(url.toString());
					urls.add(cm);
					List<String> args = new ArrayList<String>();
					args.add(url.toString());
					args.add(baseURL);
					mysql.insertURL(args);
				}
				logger.info("已经爬取到：" + tid + "=" + url + "，父页面是" + baseURL);
			} catch (Exception ex) {
				logger.error("获取专辑页面异常，url=" + url, ex);
			}
		}
		return urls;
	}
}
