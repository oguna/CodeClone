package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Result {
	public Result(){
	}

	public void execute(){
		Connection connection = null;
		Statement statement = null;
		Statement statement2 = null;
		Statement statement3 = null;
		Statement statement4 = null;

		try {
			// JDBCドライバーの指定
			Class.forName("org.sqlite.JDBC");
			// データベースに接続
			connection = DriverManager.getConnection("jdbc:sqlite:F:\\objectweb.db");
			statement = connection.createStatement();
			statement2 = connection.createStatement();
			statement3 = connection.createStatement();
			statement4 = connection.createStatement();

			int i = 1;
			while(i<5){
				Query query = new Query();
				String sql_delete = query.delete(i);
				String sql_add = query.add(i);
				String sql_before_fix = query.before_fix(i);
				String sql_after_fix = query.after_fix(i);

				ResultSet result_delete = statement.executeQuery(sql_delete);
				ResultSet result_add = statement2.executeQuery(sql_add);
				ResultSet result_before_fix = statement3.executeQuery(sql_before_fix);
				ResultSet result_after_fix = statement4.executeQuery(sql_after_fix);

				i++;
			}
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		} catch (SQLException e) {
			// Connection の例外が発生した時
			System.err.println(e);
		}finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				System.err.println(e);
			}
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				System.err.println(e);
			}
		}
	}
}
