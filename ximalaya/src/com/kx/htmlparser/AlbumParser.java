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

//��ȡÿ��itemҳ���ר������
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
				if ("ר��".equals(html)) {
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
						logger.info("�Ѿ���ȡ����ר��=" + url + "����ҳ����" + baseURL);
					} catch (Exception ex) {
						logger.error("��ȡר��ҳ���쳣��url=" + url, ex);
					}
				}
			}
		}
		return urls;
	}
}
