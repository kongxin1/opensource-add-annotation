import java.io.File;

public class T1 {
	public static void main(String argv[]) throws Exception {
		File dir = new File("C:/Users/¿×ÐÂ/Desktop/t.txt");
		while (dir != null && !dir.isDirectory()) {
			dir = dir.getParentFile();
			System.out.println(dir + "\t" + dir.isDirectory());
		}
		System.out.println(dir);
	}
}
