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
		Elements eles = doc.select("a.tag-title-wrapper");
		String tid = null;
		List<ClawerMessage> urls = new ArrayList<ClawerMessage>();
		List<String> names = new ArrayList<String>();
		for (Element ele : eles) {
			// href = ele.attr("href");
			// tid = ele.attr("tid");
			String name = ele.attr("name");
			// logger.info("主页中name：" + name + "=" + url + "，父页面是" + baseURL);
			names.add(name);
		}
		eles = doc.select("table.tagCol");
		int i = 0;
		for (Element ele : eles) {
			URL url = null;
			Elements as = ele.select("a");
			i++;
			String name = names.get(i);
			for (Element a : as) {
				String href = a.attr("href");
				String index = a.html();
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
					logger.info("已经获取到：parent=" + name + "，index=" + index);
				} catch (Exception ex) {
					logger.error("处理主页出错，href=" + href + "，parent=" + baseURL);
				}
			}
		}
		return urls;
	}
}
