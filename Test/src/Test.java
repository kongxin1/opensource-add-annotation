import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

public class Test {
	// public static void main(String argv[]) {
	// for (int i = 0; i < argv.length; i++) {
	// System.out.println(argv[i]);
	// // System.out.println(System.getProperty("java.io.tmpdir"));
	// }
	private ParserFactory factory;

	public Test() {
		factory = getParserFactory();
	}
	public List<String> parseMethodDefs(String file) throws IOException {
		JCCompilationUnit unit = parse(file);
		MethodScanner scanner = new MethodScanner();
		return scanner.visitCompilationUnit(unit, new ArrayList<String>());
	}
	JCCompilationUnit parse(String file) throws IOException {
		Parser parser = factory.newParser(readFile(file), true, false, true);
		return parser.parseCompilationUnit();
	}
	private ParserFactory getParserFactory() {
		Context context = new Context();
		JavacFileManager.preRegister(context);
		ParserFactory factory = ParserFactory.instance(context);
		return factory;
	}
	CharSequence readFile(String file) throws IOException {
		FileInputStream fin = new FileInputStream(file);
		FileChannel ch = fin.getChannel();
		ByteBuffer buffer = ch.map(MapMode.READ_ONLY, 0, ch.size());
		return Charset.defaultCharset().decode(buffer);
	}

	// 扫描方法时，把方法名加入到一个list中
	static class MethodScanner extends TreeScanner<List<String>, List<String>> {
		// @Override
		// public List<String> visitMethod(MethodTree node, List<String> p) {
		// p.add(node.getName().toString());
		// return p;
		// }
		@Override
		public List<String> visitClass(ClassTree node, List<String> p) {
			p.add(node.getSimpleName().toString());
			List<? extends Tree> ns = node.getMembers();
			for (Tree t : ns) {
				if (t.getKind().toString().equals("CLASS")) {
					ClassTree c = (ClassTree) t;
					p.add(c.getSimpleName().toString());
				}
			}
			return p;
		}
	}

	public static void main(String[] args) throws IOException {
		Test parser = new Test();
		for (String method : parser.parseMethodDefs("C:/Users/孔新/workspace/Test/src/Test.java")) {
			System.out.println(method);
		}
		try (InputStream in = null) {
		}
	}

	public class T {
	}
}
