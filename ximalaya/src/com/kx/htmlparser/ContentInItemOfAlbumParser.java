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

//��ר����ÿ��������Ŀ��ϸ���ݽ��з���
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
		// ��ȡÿ����Ŀ�����֣�����ÿ��������������
		for (Element ele : eles) {
			Elements es = ele.children();
			if (es.size() == 1) {
				Element e = es.get(0);
				html = e.html();
				args.add(html);
				logger.info("Ŀǰ�Ѿ���ȡ����������������=" + html + "����ҳ����" + baseURL);
			}
		}
		if (html == null) {
			args.add("");
		}
		// ��ȡ����Ŀ�ı�ǩ
		eles = doc.select("a.tagBtn2");
		String tag = null;
		StringBuilder tags = new StringBuilder("");
		for (Element ele : eles) {
			Elements es = ele.children();
			if (es.size() == 1) {
				Element e = es.get(0);
				tag = e.html();
				tags.append(tag).append(" ");
				logger.info("Ŀǰ�Ѿ���ȡ����������������=" + html + "����ҳ����" + baseURL + "�����ǩ������" + tag);
			}
		}
		args.add(tags.toString());
		// ���������ܲ��Ŵ���
		eles = doc.select("div.detailContent_playcountDetail");
		String play = null;
		for (Element ele : eles) {
			Elements es = ele.children();
			if (es.size() == 1) {
				Element e = es.get(0);
				play = e.html();
				args.add(play);
				logger.info("Ŀǰ�Ѿ���ȡ����������������=" + html + "����ҳ����" + baseURL + "���䲥���ܴ�����" + play);
			}
		}
		if (play == null) {
			args.add("");
		}
		// ��ȡ�ܵ���������
		eles = doc.select("span.albumSoundcount");
		play = null;
		for (Element ele : eles) {
			play = ele.html();
			args.add(play);
			logger.info("Ŀǰ�Ѿ���ȡ����������������=" + html + "����ҳ����" + baseURL + "�����������й���������" + play);
		}
		if (play == null) {
			args.add("");
		}
		// ��ȡ��ר��������� (137)�������ÿһ����Ŀ
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
				logger.info("Ŀǰ�Ѿ���ȡ����������������=" + html + "����ҳ����" + baseURL + "�����������е�" + i + "�������ǣ�" + url);
			} catch (Exception ex) {
				logger.error("��ȡר��ҳ���쳣��url=" + url, ex);
			}
		}
		mysql.insertContentInItemOfAlbumParser(args);
		return urls;
	}
}
