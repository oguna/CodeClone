package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
		Statement statement_candidate = null;
		Statement statement_binding = null;

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

			//集約されたメソッド(候補)の情報を格納するテーブルを作成
			statement_candidate = connection.createStatement();
			ResultSet result_candidate = statement_candidate.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='CANDIDATE';");
			if(result_candidate.getInt(1) != 0) statement_candidate.executeUpdate("drop table CANDIDATE");
			statement_candidate.executeUpdate("create table CANDIDATE(ID integer , CODE_FRAGMENT_ID integer , REVISION integer , PROCESS string)");

			int current_revision_num = 1;
			while(current_revision_num<App.endRevision){
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
				while(result_delete.next()){
					codeFragment = codeFragmentDetector.execute(repository,
							result_delete.getString(5),Long.parseLong(result_delete.getString(8)),
							Integer.parseInt(result_delete.getString(6)),Integer.parseInt(result_delete.getString(7)),
							Integer.parseInt(result_delete.getString(1)));
					if(codeFragment != null) list_delete.add(codeFragment);
				}

				//追加されたメソッドを特定
				while(result_add.next()){
					codeFragment = codeFragmentDetector.execute(repository,
							result_add.getString(5),Long.parseLong(result_add.getString(8)),
							Integer.parseInt(result_add.getString(6)),Integer.parseInt(result_add.getString(7)),
							Integer.parseInt(result_add.getString(1)));
					if(codeFragment != null) list_add.add(codeFragment);
				}

				//修正前のメソッドを特定
				while(result_before_fix.next()){
					codeFragment = codeFragmentDetector.execute(repository,
							result_before_fix.getString(5),Long.parseLong(result_before_fix.getString(8)),
							Integer.parseInt(result_before_fix.getString(6)),Integer.parseInt(result_before_fix.getString(7)),
							Integer.parseInt(result_before_fix.getString(1)));
					if(codeFragment != null) list_before_fix.add(codeFragment);
				}

				//修正後のメソッドを特定
				while(result_after_fix.next()){
					codeFragment = codeFragmentDetector.execute(repository,
							result_after_fix.getString(5),Long.parseLong(result_after_fix.getString(8)),
							Integer.parseInt(result_after_fix.getString(6)),Integer.parseInt(result_after_fix.getString(7)),
							Integer.parseInt(result_after_fix.getString(1)));
					if(codeFragment != null) list_after_fix.add(codeFragment);
				}

				//修正前と修正後のコード片IDを取得
				while(result_link.next()){
					CodeFragmentLink codeFragmentLink = new CodeFragmentLink(
								Long.parseLong(result_link.getString(1)), Long.parseLong(result_link.getString(2)));
					list_link.add(codeFragmentLink);
				}
				System.out.println("revision " + current_revision_num + " mining finished.");

				//類似度を算出し，結果を出力
				System.out.println("revision " + current_revision_num + " calculate similarity start.");
				SimilarityCalculator calculateSimilarity = new SimilarityCalculator(list_delete, list_add, list_before_fix, list_after_fix, list_link);
				calculateSimilarity.execute(current_revision_num,statement_candidate);
				System.out.println("revision " + current_revision_num + " calculate similarity finished.");

				//メソッド呼び出し関係を考慮
				statement_binding = connection.createStatement();
				String sql_binding = query.binding(current_revision_num);
				ResultSet result_binding = statement_binding.executeQuery(sql_binding);
				if(result_binding.next()){
					//REVISION_IDENTIFIERの取得
					long after_revision = result_binding.getLong(7);
					long before_revision = 0;
					while(result_binding.next()){
						before_revision = result_binding.getLong(7);
						if(before_revision != after_revision) break;
					}
					if(before_revision > after_revision){
						long tmp = before_revision;
						before_revision = after_revision;
						after_revision = tmp;
					}
					//binding
					result_binding = statement_binding.executeQuery(sql_binding);
					System.out.println("revision " + current_revision_num + " binding start.");
					BindingDetector bindingDetector = new BindingDetector(repository, before_revision, after_revision, result_binding);
					bindingDetector.execute();
					System.out.println("revision " + current_revision_num + " binding end.");
				}
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
				if (statement_link != null) statement_link.close();
				if (statement_candidate != null) statement_candidate.close();
				if (statement_binding != null) statement_binding.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				System.err.println(e);
			}
		}
	}
}
