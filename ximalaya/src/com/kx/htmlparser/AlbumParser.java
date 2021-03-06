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

//获取每个item页面的专辑链接
public class AlbumParser implements Parser {
	private Logger logger = LoggerFactory.getLogger(AlbumParser.class);
	private MysqlConnection mysql = new MysqlConnection();

	public List<ClawerMessage> parse(String baseURL, ClawerMessage msg) {
		Document doc = Jsoup.parse(msg.getStr(), baseURL);
		Elements eles = doc.select("li.nav_item");
		String href = null;
		String html = null;
		List<ClawerMessage> urls = new ArrayList<ClawerMessage>();
		for (Element ele : eles) {
			Elements es = ele.children();
			if (es.size() == 1) {
				Element e = es.get(0);
				href = e.attr("href");
				html = e.html();
				if ("专辑".equals(html)) {
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
						logger.info("已经爬取到：专辑=" + url + "，父页面是" + baseURL);
					} catch (Exception ex) {
						logger.error("获取专辑页面异常，url=" + url, ex);
					}
				}
			}
		}
		return urls;
	}
}
