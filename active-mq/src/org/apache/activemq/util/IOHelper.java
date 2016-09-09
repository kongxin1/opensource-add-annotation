/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Collection of File and Folder utility methods.
 */
public final class IOHelper {
	protected static final int MAX_DIR_NAME_LENGTH;// 200
	protected static final int MAX_FILE_NAME_LENGTH;// 64
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	static {
		// 通过System.getProperty方法获得下面的系统值
		MAX_DIR_NAME_LENGTH = Integer.getInteger("MaximumDirNameLength", 200);
		MAX_FILE_NAME_LENGTH = Integer.getInteger("MaximumFileNameLength", 64);
	}

	private IOHelper() {
	}
	/**
	 * 返回默认的数据目录，其前缀已经在系统中进行设置
	 * @Title: getDefaultDataDirectory
	 * @Description: TODO
	 * @return
	 * @return: String
	 */
	public static String getDefaultDataDirectory() {
		return getDefaultDirectoryPrefix() + "activemq-data";
	}
	/**
	 * 返回默认的存储目录
	 * @Title: getDefaultStoreDirectory
	 * @Description: TODO
	 * @return
	 * @return: String
	 */
	public static String getDefaultStoreDirectory() {
		return getDefaultDirectoryPrefix() + "amqstore";
	}
	/**
	 * Allows a system property to be used to overload the default data directory which can be
	 * useful for forcing the test cases to use a target/ prefix
	 */
	/**
	 * 获取设置的目录前缀的系统值
	 * @Title: getDefaultDirectoryPrefix
	 * @Description: TODO
	 * @return
	 * @return: String
	 */
	public static String getDefaultDirectoryPrefix() {
		try {
			return System.getProperty("org.apache.activemq.default.directory.prefix", "");
		} catch (Exception e) {
			return "";
		}
	}
	/**
	 * Converts any string into a string that is safe to use as a file name. The result will only
	 * include ascii characters and numbers, and the "-","_", and "." characters.
	 * @param name
	 * @return safe name of the directory
	 */
	/**
	 * 得到安全的目录名字，下面的方法将name中的特殊字符转化为了十六进制字符
	 * @Title: toFileSystemDirectorySafeName
	 * @Description: TODO
	 * @param name
	 * @return
	 * @return: String
	 */
	public static String toFileSystemDirectorySafeName(String name) {
		return toFileSystemSafeName(name, true, MAX_DIR_NAME_LENGTH);
	}
	/**
	 * 得到安全的文件名，下面的方法将name中的特殊字符转化为了十六进制字符
	 * @Title: toFileSystemSafeName
	 * @Description: TODO
	 * @param name
	 * @return
	 * @return: String
	 */
	public static String toFileSystemSafeName(String name) {
		return toFileSystemSafeName(name, false, MAX_FILE_NAME_LENGTH);
	}
	/**
	 * Converts any string into a string that is safe to use as a file name. The result will only
	 * include ascii characters and numbers, and the "-","_", and "." characters.
	 * @param name
	 * @param dirSeparators
	 * @param maxFileLength
	 * @return file system safe name
	 */
	/**
	 * 将name入参中的除了字符数字，_,-,.,{@link #clone()},/,\\之外的字符转化为十六进制的字符，保证文件名在系统可以安全使用
	 * @Title: toFileSystemSafeName
	 * @Description: TODO
	 * @param name
	 * @param dirSeparators
	 * @param maxFileLength
	 * @return
	 * @return: String
	 */
	public static String toFileSystemSafeName(String name, boolean dirSeparators, int maxFileLength) {
		int size = name.length();
		StringBuffer rc = new StringBuffer(size * 2);
		for (int i = 0; i < size; i++) {
			char c = name.charAt(i);
			boolean valid = c >= 'a' && c <= 'z';
			valid = valid || (c >= 'A' && c <= 'Z');
			valid = valid || (c >= '0' && c <= '9');
			valid = valid || (c == '_') || (c == '-') || (c == '.') || (c == '#')
					|| (dirSeparators && ((c == '/') || (c == '\\')));
			if (valid) {
				rc.append(c);
			} else {
				// Encode the character using hex notation
				rc.append('#');
				rc.append(HexSupport.toHexFromInt(c, true));
			}
		}
		String result = rc.toString();
		if (result.length() > maxFileLength) {
			result = result.substring(result.length() - maxFileLength, result.length());
		}
		return result;
	}
	/**
	 * 删除top目录下的所有文件和目录，包括top也要删除，返回true表示完全删除
	 * @Title: delete
	 * @Description: TODO
	 * @param top
	 * @return
	 * @return: boolean
	 */
	public static boolean delete(File top) {
		boolean result = true;
		Stack<File> files = new Stack<File>();
		// Add file to the stack to be processed...
		files.push(top);
		// Process all files until none remain...
		while (!files.isEmpty()) {
			File file = files.pop();
			if (file.isDirectory()) {
				File list[] = file.listFiles();
				if (list == null || list.length == 0) {
					// The current directory contains no entries...
					// delete directory and continue...
					result &= file.delete();
				} else {
					// Add back the directory since it is not empty....
					// and when we process it again it will be empty and can be
					// deleted safely...
					files.push(file);
					for (File dirFile : list) {
						if (dirFile.isDirectory()) {
							// Place the directory on the stack...
							files.push(dirFile);
						} else {
							// This is a simple file, delete it...
							result &= dirFile.delete();
						}
					}
				}
			} else {
				// This is a simple file, delete it...
				result &= file.delete();
			}
		}
		return result;
	}
	/**
	 * 删除fileToDelete目录下的所有文件和目录，fileToDelete文件也要删除，如果完全删除返回true
	 * @Title: deleteFile
	 * @Description: TODO
	 * @param fileToDelete
	 * @return
	 * @return: boolean
	 */
	public static boolean deleteFile(File fileToDelete) {
		if (fileToDelete == null || !fileToDelete.exists()) {
			return true;
		}
		boolean result = deleteChildren(fileToDelete);
		result &= fileToDelete.delete();
		return result;
	}
	/**
	 * 删除parent目录下的所有文件和目录
	 * @Title: deleteChildren
	 * @Description: TODO
	 * @param parent
	 * @return
	 * @return: boolean
	 */
	public static boolean deleteChildren(File parent) {
		if (parent == null || !parent.exists()) {
			return false;
		}
		boolean result = true;
		if (parent.isDirectory()) {
			File[] files = parent.listFiles();
			if (files == null) {
				result = false;
			} else {
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (file.getName().equals(".") || file.getName().equals("..")) {
						continue;
					}
					if (file.isDirectory()) {
						result &= deleteFile(file);
					} else {
						result &= file.delete();
					}
				}
			}
		}
		return result;
	}
	/**
	 * 将文件移动到targetDirectory目录下
	 * @Title: moveFile
	 * @Description: TODO
	 * @param src
	 * @param targetDirectory
	 * @throws IOException
	 * @return: void
	 */
	public static void moveFile(File src, File targetDirectory) throws IOException {
		if (!src.renameTo(new File(targetDirectory, src.getName()))) {
			// If rename fails we must do a true deep copy instead.
			Path sourcePath = src.toPath();
			Path targetDirPath = targetDirectory.toPath();
			try {
				Files.move(sourcePath, targetDirPath.resolve(sourcePath.getFileName()),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				throw new IOException("Failed to move " + src + " to " + targetDirectory + " - " + ex.getMessage());
			}
		}
	}
	/**
	 * 将文件和目录移动到targetDirectory目录下
	 * @Title: moveFiles
	 * @Description: TODO
	 * @param srcDirectory
	 * @param targetDirectory
	 * @param filter
	 * @throws IOException
	 * @return: void
	 */
	public static void moveFiles(File srcDirectory, File targetDirectory, FilenameFilter filter) throws IOException {
		if (!srcDirectory.isDirectory()) {
			throw new IOException("source is not a directory");
		}
		if (targetDirectory.exists() && !targetDirectory.isDirectory()) {
			throw new IOException("target exists and is not a directory");
		} else {
			mkdirs(targetDirectory);
		}
		List<File> filesToMove = new ArrayList<File>();
		getFiles(srcDirectory, filesToMove, filter);
		for (File file : filesToMove) {
			if (!file.isDirectory()) {
				moveFile(file, targetDirectory);
			}
		}
	}
	/**
	 * 进行无文件名过滤器的文件拷贝
	 * @Title: copyFile
	 * @Description: TODO
	 * @param src
	 * @param dest
	 * @throws IOException
	 * @return: void
	 */
	public static void copyFile(File src, File dest) throws IOException {
		copyFile(src, dest, null);
	}
	/**
	 * 将src下的文件目录拷贝到dest目录下
	 * @Title: copyFile
	 * @Description: TODO
	 * @param src
	 * @param dest
	 * @param filter
	 * @throws IOException
	 * @return: void
	 */
	public static void copyFile(File src, File dest, FilenameFilter filter) throws IOException {
		if (src.getCanonicalPath().equals(dest.getCanonicalPath()) == false) {
			if (src.isDirectory()) {
				mkdirs(dest);
				List<File> list = getFiles(src, filter);
				for (File f : list) {
					if (f.isFile()) {
						File target = new File(getCopyParent(src, dest, f), f.getName());
						copySingleFile(f, target);
					}
				}
			} else if (dest.isDirectory()) {
				mkdirs(dest);
				File target = new File(dest, src.getName());
				copySingleFile(src, target);
			} else {
				copySingleFile(src, dest);
			}
		}
	}
	/**
	 * 得到src的父目录
	 * @Title: getCopyParent
	 * @Description: TODO
	 * @param from
	 * @param to
	 * @param src
	 * @return
	 * @return: File
	 */
	static File getCopyParent(File from, File to, File src) {
		File result = null;
		File parent = src.getParentFile();
		String fromPath = from.getAbsolutePath();
		if (parent.getAbsolutePath().equals(fromPath)) {
			// one level down
			result = to;
		} else {
			String parentPath = parent.getAbsolutePath();
			String path = parentPath.substring(fromPath.length());
			result = new File(to.getAbsolutePath() + File.separator + path);
		}
		return result;
	}
	/**
	 * 凡是符合文件名过滤器的，就将dir目录下的所有文件和目录以及dir存入list中
	 * @Title: getFiles
	 * @Description: TODO
	 * @param dir
	 * @param filter
	 * @return
	 * @return: List<File>
	 */
	static List<File> getFiles(File dir, FilenameFilter filter) {
		List<File> result = new ArrayList<File>();
		getFiles(dir, result, filter);
		return result;
	}
	/**
	 * 凡是符合文件名过滤器的，就将dir目录下的所有文件和目录以及dir存入list中，
	 * @Title: getFiles
	 * @Description: TODO
	 * @param dir
	 * @param list
	 * @param filter
	 * @return: void
	 */
	static void getFiles(File dir, List<File> list, FilenameFilter filter) {
		if (!list.contains(dir)) {
			list.add(dir);
			String[] fileNames = dir.list(filter);
			for (int i = 0; i < fileNames.length; i++) {
				File f = new File(dir, fileNames[i]);
				if (f.isFile()) {
					list.add(f);
				} else {
					getFiles(dir, list, filter);
				}
			}
		}
	}
	/**
	 * 将源文件数据写入目标文件中
	 * @Title: copySingleFile
	 * @Description: TODO
	 * @param src
	 * @param dest
	 * @throws IOException
	 * @return: void
	 */
	public static void copySingleFile(File src, File dest) throws IOException {
		FileInputStream fileSrc = new FileInputStream(src);
		FileOutputStream fileDest = new FileOutputStream(dest);
		copyInputStream(fileSrc, fileDest);
	}
	/**
	 * 从输入流中读入文件数据并写入输出流中
	 * @Title: copyInputStream
	 * @Description: TODO
	 * @param in
	 * @param out
	 * @throws IOException
	 * @return: void
	 */
	public static void copyInputStream(InputStream in, OutputStream out) throws IOException {
		try {
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int len = in.read(buffer);
			while (len >= 0) {
				out.write(buffer, 0, len);
				len = in.read(buffer);
			}
		} finally {
			in.close();
			out.close();
		}
	}
	public static int getMaxDirNameLength() {
		return MAX_DIR_NAME_LENGTH;
	}
	public static int getMaxFileNameLength() {
		return MAX_FILE_NAME_LENGTH;
	}
	/**
	 * 创建入参要求的目录
	 * @Title: mkdirs
	 * @Description: TODO
	 * @param dir
	 * @throws IOException
	 * @return: void
	 */
	public static void mkdirs(File dir) throws IOException {
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				throw new IOException("Failed to create directory '" + dir
						+ "', regular file already existed with that name");
			}
		} else {
			if (!dir.mkdirs()) {
				throw new IOException("Failed to create directory '" + dir + "'");
			}
		}
	}
}
