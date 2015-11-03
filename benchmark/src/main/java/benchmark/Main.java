package benchmark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		try {
			// JDBCドライバーの指定
			Class.forName("org.sqlite.JDBC");
			// データベースに接続する．なければ作成される
			Connection con = DriverManager
					.getConnection("jdbc:sqlite:../database.db");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// Connection の例外が発生した時
			e.printStackTrace();
		}
	}
}
