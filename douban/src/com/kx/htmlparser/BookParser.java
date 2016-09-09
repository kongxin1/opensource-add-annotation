package com.kx.htmlparser;

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

//针对每一个具体的声音进行分析
public class BookParser implements Parser {
	private Logger logger = LoggerFactory.getLogger(MenuParser.class);
	private MysqlConnection mysql = new MysqlConnection();

	public List<ClawerMessage> parse(String baseURL, ClawerMessage msg) {
		Document doc = Jsoup.parse(msg.getStr(), baseURL);
		Elements eles = doc.select("span[property=v:itemreviewed]");
		String bookName = null;
		List<String> args = new ArrayList<String>();
		// 获取书籍的名字
		for (Element ele : eles) {
			bookName = ele.html();
			args.add(bookName);
			logger.info("目前已经爬取到：书籍的名字=" + bookName + "，父页面是" + baseURL);
		}
		// 获取该条目的标签
		eles = doc.select("a.tagBtn2");
		String tag = null;
		StringBuilder tags = new StringBuilder();
		for (Element ele : eles) {
			Elements es = ele.children();
			if (es.size() == 1) {
				Element e = es.get(0);
				tag = e.html();
				tags.append(tag);
				logger.info("目前已经爬取到：声音的名字=" + html + "，父页面是" + baseURL + "，其标签包括：" + tag);
			}
		}
		args.add(tags.toString());
		// 获取总时长
		eles = doc.select("span.sound_duration");
		for (Element ele : eles) {
			String time = ele.html();
			if (!"00:00".equals(time)) {
				args.add(time);
				logger.info("目前已经爬取到：声音的名字=" + html + "，父页面是" + baseURL + "，其声音时长：" + time);
			}
		}
		// 播放总次数
		eles = doc.select("div.soundContent_playcount");
		for (Element ele : eles) {
			String play = ele.html();
			args.add(play);
			logger.info("目前已经爬取到：声音的名字=" + html + "，父页面是" + baseURL + "，其播放次数：" + play);
		}
		mysql.insertMusicParser(args);
		return new ArrayList<ClawerMessage>();
	}
}
