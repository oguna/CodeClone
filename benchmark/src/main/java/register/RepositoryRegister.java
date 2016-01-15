package register;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import main.App;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;

public class RepositoryRegister {
	public RepositoryRegister() {
	}

	public SVNRepository execute() throws SVNException {
		Connection connection = null;
		Statement statement = null;
		// JDBCドライバーの指定
		try {
			Class.forName("org.sqlite.JDBC");
			// データベースに接続
			connection = DriverManager.getConnection("jdbc:sqlite:" + App.database_location);
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery("select REPOSITORY_ROOT_URL,REPOSITORY_ADDITIONAL_URL from REPOSITORY");
			result.next();
			App.repository_location = result.getString(1);
			if(result.getString(2) == null) App.repository_additional_location = "";
			else App.repository_additional_location = result.getString(2).replaceFirst("/", "");
			FSRepositoryFactory.setup();
			SVNURL svnURL = SVNURL.parseURIEncoded(App.repository_location);
			return FSRepositoryFactory.create(svnURL);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
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
