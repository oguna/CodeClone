package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import mining.CodeFragment;
import mining.CodeFragmentDetector;
import mining.CodeFragmentLink;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

import similarity.CalculateSimilarity;
import database.Query;
/**
 * @author y-yusuke
 *
 */
public class Result {

	SVNRepository repository;
	String database_location;
	int revision_num;

	public Result(SVNRepository repository, String database_location, int revision_num){
		this.repository = repository;
		this.database_location = database_location;
		this.revision_num = revision_num;
	}

	public void execute() throws SVNException{
		Connection connection = null;
		Statement statement_delete = null;
		Statement statement_add = null;
		Statement statement_before_fix = null;
		Statement statement_after_fix = null;
		Statement statement_link = null;

		List<CodeFragment> list_delete = new ArrayList<CodeFragment>();
		List<CodeFragment> list_add = new ArrayList<CodeFragment>();
		List<CodeFragment> list_before_fix = new ArrayList<CodeFragment>();
		List<CodeFragment> list_after_fix = new ArrayList<CodeFragment>();
		List<CodeFragmentLink> list_link = new ArrayList<CodeFragmentLink>();

		try {
			// JDBCドライバーの指定
			Class.forName("org.sqlite.JDBC");
			// データベースに接続
			connection = DriverManager.getConnection("jdbc:sqlite:" + database_location);
			statement_delete = connection.createStatement();
			statement_add = connection.createStatement();
			statement_before_fix = connection.createStatement();
			statement_after_fix = connection.createStatement();
			statement_link = connection.createStatement();

			int current_revision_num = 1;
			while(current_revision_num<revision_num){
				Query query = new Query();
				String sql_delete = query.delete(current_revision_num);
				String sql_add = query.add(current_revision_num);
				String sql_before_fix = query.before_fix(current_revision_num);
				String sql_after_fix = query.after_fix(current_revision_num);
				String sql_link = query.link(current_revision_num);

				ResultSet result_delete = statement_delete.executeQuery(sql_delete);
				ResultSet result_add = statement_add.executeQuery(sql_add);
				ResultSet result_before_fix = statement_before_fix.executeQuery(sql_before_fix);
				ResultSet result_after_fix = statement_after_fix.executeQuery(sql_after_fix);
				ResultSet result_link = statement_link.executeQuery(sql_link);

				CodeFragmentDetector codeFragmentDetector = new CodeFragmentDetector();
				CodeFragment codeFragment = new CodeFragment();

				System.out.println("revision " + current_revision_num + " mining start.");
				//削除されたメソッドを特定
				if(result_delete != null){
					while(result_delete.next()){
						codeFragment = codeFragmentDetector.execute(repository,
								result_delete.getString(5),Long.parseLong(result_delete.getString(8)),
								Integer.parseInt(result_delete.getString(6)),Integer.parseInt(result_delete.getString(7)),
								Integer.parseInt(result_delete.getString(1)));
						list_delete.add(codeFragment);
					}
				}

				//追加されたメソッドを特定
				if(result_add != null){
					while(result_add.next()){
						codeFragment = codeFragmentDetector.execute(repository,
								result_add.getString(5),Long.parseLong(result_add.getString(8)),
								Integer.parseInt(result_add.getString(6)),Integer.parseInt(result_add.getString(7)),
								Integer.parseInt(result_add.getString(1)));
						list_add.add(codeFragment);
					}
				}

				//修正前のメソッドを特定
				if(result_before_fix != null){
					while(result_before_fix.next()){
						codeFragment = codeFragmentDetector.execute(repository,
								result_before_fix.getString(5),Long.parseLong(result_before_fix.getString(8)),
								Integer.parseInt(result_before_fix.getString(6)),Integer.parseInt(result_before_fix.getString(7)),
								Integer.parseInt(result_before_fix.getString(1)));
						list_before_fix.add(codeFragment);
					}
				}

				//修正後のメソッドを特定
				if(result_after_fix != null){
					while(result_after_fix.next()){
						codeFragment = codeFragmentDetector.execute(repository,
								result_after_fix.getString(5),Long.parseLong(result_after_fix.getString(8)),
								Integer.parseInt(result_after_fix.getString(6)),Integer.parseInt(result_after_fix.getString(7)),
								Integer.parseInt(result_after_fix.getString(1)));
						list_after_fix.add(codeFragment);
					}
				}

				//修正前と修正後のコード片IDを取得
				if(result_link != null){
					while(result_link.next()){
						CodeFragmentLink codeFragmentLink = new CodeFragmentLink(
									Long.parseLong(result_link.getString(1)), Long.parseLong(result_link.getString(2)));
						list_link.add(codeFragmentLink);
					}
				}
				System.out.println("revision " + current_revision_num + " mining finished.");

				//類似度を算出し，結果を出力
				System.out.println("revision " + current_revision_num + " calculate similarity start.");
				CalculateSimilarity calculateSimilarity = new CalculateSimilarity(list_delete, list_add, list_before_fix, list_after_fix, list_link);
				calculateSimilarity.execute(current_revision_num);
				System.out.println("revision " + current_revision_num + " calculate similarity finished.");

				//後処理
				list_delete.clear();
				list_add.clear();
				list_before_fix.clear();
				list_after_fix.clear();
				current_revision_num++;
			}
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		} catch (SQLException e) {
			System.err.println(e);
		}finally {
			try {
				if (statement_delete != null) statement_delete.close();
				if (statement_add != null) statement_add.close();
				if (statement_before_fix != null) statement_before_fix.close();
				if (statement_after_fix != null) statement_after_fix.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				System.err.println(e);
			}
		}
	}
}
