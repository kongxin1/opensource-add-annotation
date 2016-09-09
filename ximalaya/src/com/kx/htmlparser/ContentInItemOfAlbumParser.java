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

//对专辑里每个具体条目详细内容进行分析
public class ContentInItemOfAlbumParser implements Parser {
	private Logger logger = LoggerFactory.getLogger(MenuParser.class);
	private MysqlConnection mysql = new MysqlConnection();

	public List<ClawerMessage> parse(String baseURL, ClawerMessage msg) {
		Document doc = Jsoup.parse(msg.getStr(), baseURL);
		Elements eles = doc.select("div.detailContent_title");
		String href = null;
		String html = null;
		List<ClawerMessage> urls = new ArrayList<ClawerMessage>();
		List<String> args = new ArrayList<String>();
		// 获取每个条目的名字，就是每个声音集的名字
		for (Element ele : eles) {
			Elements es = ele.children();
			if (es.size() == 1) {
				Element e = es.get(0);
				html = e.html();
				args.add(html);
				logger.info("目前已经爬取到：声音集的名字=" + html + "，父页面是" + baseURL);
			}
		}
		if (html == null) {
			args.add("");
		}
		// 获取该条目的标签
		eles = doc.select("a.tagBtn2");
		String tag = null;
		StringBuilder tags = new StringBuilder("");
		for (Element ele : eles) {
			Elements es = ele.children();
			if (es.size() == 1) {
				Element e = es.get(0);
				tag = e.html();
				tags.append(tag).append(" ");
				logger.info("目前已经爬取到：声音集的名字=" + html + "，父页面是" + baseURL + "，其标签包括：" + tag);
			}
		}
		args.add(tags.toString());
		// 声音集的总播放次数
		eles = doc.select("div.detailContent_playcountDetail");
		String play = null;
		for (Element ele : eles) {
			Elements es = ele.children();
			if (es.size() == 1) {
				Element e = es.get(0);
				play = e.html();
				args.add(play);
				logger.info("目前已经爬取到：声音集的名字=" + html + "，父页面是" + baseURL + "，其播放总次数：" + play);
			}
		}
		if (play == null) {
			args.add("");
		}
		// 获取总的声音个数
		eles = doc.select("span.albumSoundcount");
		play = null;
		for (Element ele : eles) {
			play = ele.html();
			args.add(play);
			logger.info("目前已经爬取到：声音集的名字=" + html + "，父页面是" + baseURL + "，其声音集中共有声音：" + play);
		}
		if (play == null) {
			args.add("");
		}
		// 获取“专辑里的声音 (137)”里面的每一个条目
		int i = 0;
		eles = doc.select("a.title");
		for (Element ele : eles) {
			href = ele.attr("href");
			i++;
			URL url = null;
			try {
				url = new URL(new URL(baseURL), href);
				ClawerMessage cm = new ClawerMessage();
				cm.setOrder(msg.getOrder() + 1);
				cm.setUrl(url.toString());
				urls.add(cm);
				logger.info("目前已经爬取到：声音集的名字=" + html + "，父页面是" + baseURL + "，其声音集中第" + i + "个声音是：" + url);
			} catch (Exception ex) {
				logger.error("获取专辑页面异常，url=" + url, ex);
			}
		}
		mysql.insertContentInItemOfAlbumParser(args);
		return urls;
	}
}
