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

//���ÿһ��������������з���
public class BookParser implements Parser {
	private Logger logger = LoggerFactory.getLogger(MenuParser.class);
	private MysqlConnection mysql = new MysqlConnection();

	public List<ClawerMessage> parse(String baseURL, ClawerMessage msg) {
		Document doc = Jsoup.parse(msg.getStr(), baseURL);
		Elements eles = doc.select("span[property=v:itemreviewed]");
		String bookName = null;
		List<String> args = new ArrayList<String>();
		// ��ȡ�鼮������
		for (Element ele : eles) {
			bookName = ele.html();
			args.add(bookName);
			logger.info("Ŀǰ�Ѿ���ȡ�����鼮������=" + bookName + "����ҳ����" + baseURL);
		}
		// ��ȡ����Ŀ�ı�ǩ
		eles = doc.select("a.tagBtn2");
		String tag = null;
		StringBuilder tags = new StringBuilder();
		for (Element ele : eles) {
			Elements es = ele.children();
			if (es.size() == 1) {
				Element e = es.get(0);
				tag = e.html();
				tags.append(tag);
				logger.info("Ŀǰ�Ѿ���ȡ��������������=" + html + "����ҳ����" + baseURL + "�����ǩ������" + tag);
			}
		}
		args.add(tags.toString());
		// ��ȡ��ʱ��
		eles = doc.select("span.sound_duration");
		for (Element ele : eles) {
			String time = ele.html();
			if (!"00:00".equals(time)) {
				args.add(time);
				logger.info("Ŀǰ�Ѿ���ȡ��������������=" + html + "����ҳ����" + baseURL + "��������ʱ����" + time);
			}
		}
		// �����ܴ���
		eles = doc.select("div.soundContent_playcount");
		for (Element ele : eles) {
			String play = ele.html();
			args.add(play);
			logger.info("Ŀǰ�Ѿ���ȡ��������������=" + html + "����ҳ����" + baseURL + "���䲥�Ŵ�����" + play);
		}
		mysql.insertMusicParser(args);
		return new ArrayList<ClawerMessage>();
	}
}
