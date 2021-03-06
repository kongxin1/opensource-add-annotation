import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class RemoveNote {
	public static void main(String argv[]) throws Exception {
		File parent = new File("C:/Users/孔新/workspace/active-mq/src/org/apache/activemq/store/kahadb/data");
		remove(parent);
	}
	public static void remove(File parent) throws Exception {
		File[] files = parent.listFiles();
		for (File f : files) {
			if (f.isDirectory() == false) {
				System.out.println("处理文件:" + f.getName());
				if (f.getName().endsWith(".java")) {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
					String line = null;
					StringBuilder sb = new StringBuilder();
					while ((line = br.readLine()) != null) {
						if (line.contains("/*") && line.contains("*/")) {
							// int startStar = line.indexOf("/*");
							int endStar = line.indexOf("*/") + 2;
							line = line.substring(endStar);
						}
						if (line.contains("* Qualified Name")) {
							line = "";
						}
						if (line.contains("* JD-Core Version")) {
							line = "";
						}
						sb.append(line).append("\n");
					}
					br.close();
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
					bw.write(sb.toString());
					bw.flush();
					bw.close();
				}
			} else {
				remove(f);
			}
		}
	}
}
