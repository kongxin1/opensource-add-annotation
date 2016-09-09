package com.kx.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlConnection {
	private Logger logger = LoggerFactory.getLogger(MysqlConnection.class);
	private static BasicDataSource ds = null;;
	public Connection conn = null;
	static {
		ds = new BasicDataSource();
		ds.setUrl("jdbc:mysql://localhost/clawer");
		ds.setPassword("root");
		ds.setUsername("root");
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setMaxActive(500);
		ds.setInitialSize(50);
	}

	public MysqlConnection() {
		// try {
		// conn = ds.getConnection();// 获取连接
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}
	public void insertMusicParser(List<String> args) {
		try {
			Connection conn = ds.getConnection();
			String sql = "insert into MusicParser values(?,?,?,?) ;";
			PreparedStatement ps = conn.prepareStatement(sql);
			for (int i = 1; i <= args.size(); i++) {
				ps.setString(i, args.get(i - 1));
			}
			ps.executeUpdate();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("数据库未插入" + args, e);
		}
	}
	public void insertContentInItemOfAlbumParser(List<String> args) {
		try {
			Connection conn = ds.getConnection();
			String sql = "insert into ContentInItemOfAlbumParser values(?,?,?,?) ;";
			PreparedStatement ps = conn.prepareStatement(sql);
			for (int i = 1; i <= args.size(); i++) {
				ps.setString(i, args.get(i - 1));
			}
			ps.executeUpdate();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("数据库未插入" + args, e);
		}
	}
	public boolean isExist(String url) {
		try {
			Connection conn = ds.getConnection();
			String sql = "select * from url where url=?;";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, url);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				conn.close();
				return true;
			}
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("数据库查找" + url + "失败", e);
		}
		return false;
	}
	public void insertURL(List<String> args) {
		try {
			Connection conn = ds.getConnection();
			String sql = "insert into url(url,parent) values(?,?) ;";
			PreparedStatement ps = conn.prepareStatement(sql);
			for (int i = 1; i <= args.size(); i++) {
				ps.setString(i, args.get(i - 1));
			}
			ps.executeUpdate();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("数据库未插入" + args, e);
		}
	}
}
