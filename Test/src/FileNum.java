import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FileNum {
	public static int num = 0;
	public static int kx = 0;

	public static void main(String argv[]) throws Exception {
		File parent = new File("C:/Users/¿×ÐÂ/workspace/active-mq");
		count(parent);
		System.out.println(num);
		System.out.println(kx);
	}
	public static void count(File parent) throws Exception {
		File[] files = parent.listFiles();
		for (File f : files) {
			if (f.isDirectory() == false) {
				if (f.getName().endsWith(".java")) {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
					String line = null;
					StringBuilder sb = new StringBuilder();
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
					if (sb.toString().contains("¿×ÐÂ")) {
						// System.out.println(f);
						kx++;
					}
					br.close();
					num++;
				}
			} else {
				count(f);
			}
		}
	}
}
