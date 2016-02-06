package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

import similarity.SimilarityCalculator;
import binding.BindingDetector;
import database.Query;
import fragmentdetector.CodeFragment;
import fragmentdetector.CodeFragmentDetector;
import fragmentdetector.CodeFragmentLink;

/**
 * @author y-yusuke
 *
 */
public class CandidateSqueezer {

	SVNRepository repository;
	public static int candidateCount = 0;

	public CandidateSqueezer(SVNRepository repository){
		this.repository = repository;
	}

	public void execute() throws SVNException {
		Connection connection = null;
		Statement statement_delete = null;
		Statement statement_add = null;
		Statement statement_before_fix = null;
		Statement statement_after_fix = null;
		Statement statement_link = null;
		Statement statement_before_revision = null;
		Statement statement_after_revision = null;
		Statement statement_candidate = null;
		Statement statement_oracle = null;

		List<CodeFragment> list_delete = new ArrayList<CodeFragment>();
		List<CodeFragment> list_add = new ArrayList<CodeFragment>();
		List<CodeFragment> list_before_fix = new ArrayList<CodeFragment>();
		List<CodeFragment> list_after_fix = new ArrayList<CodeFragment>();
		List<CodeFragmentLink> list_link = new ArrayList<CodeFragmentLink>();

		try {
			// JDBCドライバーの指定
			Class.forName("org.sqlite.JDBC");
			// データベースに接続
			connection = DriverManager.getConnection("jdbc:sqlite:" + App.database_location);
			statement_delete = connection.createStatement();
			statement_add = connection.createStatement();
			statement_before_fix = connection.createStatement();
			statement_after_fix = connection.createStatement();
			statement_link = connection.createStatement();
			statement_before_revision = connection.createStatement();
			statement_after_revision = connection.createStatement();
			statement_candidate = connection.createStatement();
			statement_oracle = connection.createStatement();

			//集約されたメソッド(候補)の情報を格納するテーブルを作成
			ResultSet result_candidate = statement_candidate.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='CANDIDATE';");
			if(result_candidate.getInt(1) != 0) statement_candidate.executeUpdate("drop table CANDIDATE");
			statement_candidate.executeUpdate("create table CANDIDATE(ID integer , CODE_FRAGMENT_ID integer , REVISION integer , PROCESS string)");

			//集約されたメソッドの情報を格納するテーブルを作成
			ResultSet result_oracle = statement_oracle.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='ORACLE';");
			if(result_oracle.getInt(1) != 0) statement_oracle.executeUpdate("drop table ORACLE");
			statement_oracle.executeUpdate("create table ORACLE(ID integer , CODE_FRAGMENT_ID integer , REVISION integer , PROCESS string, "
													+ "REPOSITORY_ROOT_URL string, REPOSITORY_ADDITIONAL_URL string, FILE_PATH string, REVISION_IDENTIFIER integer, "
													+ "START_LINE integer, END_LINE integer)");

			for(int current_revision_num = App.startRevision ; current_revision_num < App.endRevision ; current_revision_num++){
				System.out.println(App.df.format(new Date(System.currentTimeMillis())).toString() +" revision " + current_revision_num + " mining start.");
				//削除・追加されたメソッド，修正前・修正後のメソッドを特定
				Query query = new Query();
				String sql_delete = query.delete(current_revision_num);
				String sql_add = query.add(current_revision_num);
				String sql_before_fix = query.before_fix(current_revision_num);
				String sql_after_fix = query.after_fix(current_revision_num);
				String sql_link = query.link(current_revision_num);
				list_delete = getList(statement_delete, sql_delete);
				list_add = getList(statement_add, sql_add);
				list_before_fix = getList(statement_before_fix, sql_before_fix);
				list_after_fix = getList(statement_after_fix, sql_after_fix);
				ResultSet result_link = statement_link.executeQuery(sql_link);

				//修正前と修正後のコード片IDを取得
				while(result_link.next()){
					CodeFragmentLink codeFragmentLink = new CodeFragmentLink(
								Long.parseLong(result_link.getString(1)), Long.parseLong(result_link.getString(2)));
					list_link.add(codeFragmentLink);
				}
				System.out.println(App.df.format(new Date(System.currentTimeMillis())).toString() +" revision " + current_revision_num + " mining finished.");

				if(list_delete.size() == 0 && list_before_fix.size() == 0) continue;
				else if(list_add.size() == 0 && list_after_fix.size() == 0) continue;

				//類似度を算出し，結果を出力
				System.out.println(App.df.format(new Date(System.currentTimeMillis())).toString() +" revision " + current_revision_num + " calculate similarity start.");
				SimilarityCalculator calculateSimilarity = new SimilarityCalculator(list_delete, list_add, list_before_fix, list_after_fix, list_link);
				calculateSimilarity.execute(current_revision_num, statement_candidate);
				System.out.println(App.df.format(new Date(System.currentTimeMillis())).toString() +" revision " + current_revision_num + " calculate similarity finished.");

				//メソッド呼び出し関係を調査
				String sql_binding = query.binding(current_revision_num);
				ResultSet result_binding_before = statement_before_revision.executeQuery(sql_binding);
				if(result_binding_before.next()){
					System.out.println(App.df.format(new Date(System.currentTimeMillis())).toString() +" revision " + current_revision_num + " binding start.");
					//REVISION_IDENTIFIERの取得
					long revisionIdentifier[] = getRevisionIdentifier(result_binding_before);
					//集約されたメソッドを特定
					result_binding_before = statement_before_revision.executeQuery(sql_binding);
					ResultSet result_binding_after = statement_after_revision.executeQuery(sql_binding);
					BindingDetector bindingDetector = new BindingDetector(repository, revisionIdentifier[0], revisionIdentifier[1], result_binding_before, result_binding_after, statement_oracle);
					bindingDetector.execute();
					System.out.println(App.df.format(new Date(System.currentTimeMillis())).toString() +" revision " + current_revision_num + " binding end.");
				}
				//後処理
				list_delete.clear();
				list_add.clear();
				list_before_fix.clear();
				list_after_fix.clear();
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
				if (statement_link != null) statement_link.close();
				if (statement_before_revision != null) statement_before_revision.close();
				if (statement_after_revision != null) statement_after_revision.close();
				if (statement_candidate != null) statement_candidate.close();
				if (statement_oracle != null) statement_oracle.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				System.err.println(e);
			}
		}
	}

	/**
	 * 削除・追加されたメソッド，修正前・修正後のメソッドを特定
	 * @param statement
	 * @param sql
	 * @return
	 */
	private List<CodeFragment> getList(Statement statement, String sql){
		List<CodeFragment> list = new ArrayList<CodeFragment>();
		CodeFragmentDetector codeFragmentDetector = new CodeFragmentDetector();
		CodeFragment codeFragment = new CodeFragment();
		try {
			ResultSet result = statement.executeQuery(sql);
			while(result.next()){
				codeFragment = codeFragmentDetector.execute(repository,
						result.getString(5),Long.parseLong(result.getString(8)),
						Integer.parseInt(result.getString(6)),Integer.parseInt(result.getString(7)),
						Integer.parseInt(result.getString(1)));
				if(codeFragment != null) list.add(codeFragment);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 対象の前後のリビジョン番号を取得
	 * @param result_binding_before
	 * @return
	 */
	private long[] getRevisionIdentifier(ResultSet result_binding_before){
		long after_revision = 0, before_revision = 0;
		long revisionIdentifier[] = new long[2];
		try {
			after_revision = result_binding_before.getLong(7);
			while(result_binding_before.next()){
				before_revision = result_binding_before.getLong(7);
				if(before_revision != after_revision) break;
			}
			if(before_revision > after_revision){
				long tmp = before_revision;
				before_revision = after_revision;
				after_revision = tmp;
			}
			revisionIdentifier[0] = before_revision;
			revisionIdentifier[1] = after_revision;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return revisionIdentifier;
	}
}
