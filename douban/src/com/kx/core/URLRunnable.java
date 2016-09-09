package com.kx.core;

import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kx.domain.ClawerMessage;
import com.kx.download.Download;
import com.kx.mq.MessageBroker;

//�Ӷ����л�ȡ��url��Ȼ��������ض��󣬽������أ�������ҳ���䵽����
public class URLRunnable implements Runnable {
	private MessageBroker broker = null;
	private Download download = null;
	private static Logger logger = LoggerFactory.getLogger(URLRunnable.class);

	public void init() throws Exception {
		broker = new MessageBroker();
		broker.createConnection("tcp://localhost:61616", "content", "url");
		download = new Download();
	}
	public void run() {
		while (true) {
			ClawerMessage msg = null;
			try {
				msg = broker.get("url");
			} catch (Exception e) {
				logger.error("��URL����Ϣ�����л�ȡURLʧ��", e);
			}
			String content = null;
			if (msg != null) {
				try {
					String url = msg.getUrl();
					// url = "http://www.ximalaya.com/dq/kid-����������/";
					url = URLEncoder.encode(url, "utf-8");
					StringBuilder sb = new StringBuilder(url);
					// �����滻��/��
					while (true) {
						int i = sb.indexOf("%2F");
						if (i < 0)
							break;
						sb.replace(i, i + 3, "/");
					}
					// �滻������
					while (true) {
						int i = sb.indexOf("%3A");
						if (i < 0)
							break;
						sb.replace(i, i + 3, ":");
					}
					url = sb.toString();
					System.out.println(url);
					content = download.download(url, "");
					// BufferedWriter bw = null;
					// try {
					// bw = new BufferedWriter(new OutputStreamWriter(
					// new FileOutputStream("C:/Users/����/Desktop/t.txt")));
					// } catch (Exception e) {
					// }
					// bw.write(content);
					// bw.close();
				} catch (Exception e) {
					logger.error("��ҳ����ʧ�ܣ�url=" + msg.getUrl(), e);
				}
				if (content != null && !content.equals("")) {
					msg.setStr(content);
					try {
						broker.send("html", msg);
					} catch (Exception e) {
						logger.error("��ҳ����ʧ�ܣ�url=" + msg.getUrl(), e);
					}
				}
			}
		}
	}
	public static void main(String argv[]) throws Exception {
		URLRunnable t = new URLRunnable();
		t.init();
		t.run();
	}
}
