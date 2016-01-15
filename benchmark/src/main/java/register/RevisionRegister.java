package register;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import main.App;

public class RevisionRegister {
	public RevisionRegister() {
	}

	public void execute(){
		Connection connection = null;
		Statement statement = null;
		// JDBCドライバーの指定
		try {
			Class.forName("org.sqlite.JDBC");
			// データベースに接続
			connection = DriverManager.getConnection("jdbc:sqlite:" + App.database_location);
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery("select REVISION_ID from REVISION");
			result.next();
			int start = result.getInt(1);
			int end = result.getInt(1);
			while(result.next()){
				if(start > result.getInt(1)) start = result.getInt(1);
				if(end < result.getInt(1)) end = result.getInt(1);
			}
			App.startRevision = start;
			App.endRevision = end;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) statement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				System.err.println(e);
			}
		}
	}
}
