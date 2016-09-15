import java.sql.Connection;
import java.sql.Statement;

import org.apache.commons.dbcp.BasicDataSource;

public class Main {
	public static void main(String argv[]) throws Exception {
		BasicDataSource bds = new BasicDataSource();
		bds.setTimeBetweenEvictionRunsMillis(10);
		bds.setDriverClassName("com.mysql.jdbc.Driver");
		// bds.setMinEvictableIdleTimeMillis(1000);
		bds.setMinIdle(10);
		bds.setTestWhileIdle(true);
		bds.setUsername("root");
		bds.setPassword("root");
		bds.setUrl("jdbc:mysql://localhost:3306/test");
		Connection conn = bds.getConnection();
		Statement state = conn.createStatement();
		state.executeQuery("select 1");
		Thread.sleep(1000000000);
	}
}
