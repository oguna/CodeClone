package similarity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import main.CandidateSqueezer;
import mining.CodeFragment;
import mining.CodeFragmentLink;

/**
 * @author y-yusuke
 *
 */
public class CalculateSimilarity {

	private final static int n = 3;
	private final static double threshold = 0.7;
	List<CodeFragment> list_delete;
	List<CodeFragment> list_add;
	List<CodeFragment> list_before_fix;
	List<CodeFragment> list_after_fix;
	List<CodeFragmentLink> list_link;

	public CalculateSimilarity(List<CodeFragment> list_delete,
			List<CodeFragment> list_add, List<CodeFragment> list_before_fix,
			List<CodeFragment> list_after_fix, List<CodeFragmentLink> list_link) {
		this.list_delete = list_delete;
		this.list_add = list_add;
		this.list_before_fix = list_before_fix;
		this.list_after_fix = list_after_fix;
		this.list_link = list_link;
	}

	public void execute(int current_revision_num, Statement statement) {

		// 削除されたメソッドをN-gram化
		for (CodeFragment codeFragment : list_delete) {
			List<String> ngramContent = createNgram(
					codeFragment.getNormalizeContent(), n);
			codeFragment.setNgramContent(ngramContent);
		}

		// 修正前のメソッドをN-gram化
		for (CodeFragment codeFragment : list_before_fix) {
			List<String> ngramContent = createNgram(
					codeFragment.getNormalizeContent(), n);
			codeFragment.setNgramContent(ngramContent);
		}

		// 集約されたメソッドを特定 {削除(複数) => 追加}
		for (CodeFragment add_codeFragment : list_add) {
			// 追加されたメソッドをN-gram化
			List<String> add_ngramContent = createNgram(
					add_codeFragment.getNormalizeContent(), n);
			List<Integer> delete_count = new ArrayList<Integer>();

			// 削除されたメソッドと類似度を比較
			;
			for (int i = 0; i < list_delete.size(); i++) {
				CodeFragment delete_codeFragment = list_delete.get(i);
				List<String> delete_ngramContent = delete_codeFragment
						.getNgramContent();
				boolean similar = similarityCompare(add_ngramContent,
						delete_ngramContent);
				if (similar)
					delete_count.add(i);
			}

			// 複数存在すれば，集約と決定
			if (delete_count.size() > 1) {
				dbCreate(statement, delete_count, add_codeFragment,
						current_revision_num);
				file_writer(delete_count, add_codeFragment,
						current_revision_num);
				CandidateSqueezer.candidateCount++;
			}
			delete_count.clear();
		}

		// 集約されたメソッドを特定 {削除(複数)+修正前 => 修正後}
		for (CodeFragment after_fix_codeFragment : list_after_fix) {
			// 修正後のメソッドをN-gram化
			List<String> after_fix_ngramContent = createNgram(
					after_fix_codeFragment.getNormalizeContent(), n);
			int before_fix_count = 0;
			List<Integer> delete_count = new ArrayList<Integer>();

			// 修正前のメソッドと類似度を比較
			CodeFragment before_fix_codeFragment = searchBeforeCode(after_fix_codeFragment);
			if (before_fix_codeFragment == null)
				continue;
			boolean similar = similarityCompare(after_fix_ngramContent,
					before_fix_codeFragment.getNgramContent());
			if (similar)
				before_fix_count++;

			// 削除されたメソッドと類似度を比較
			for (int i = 0; i < list_delete.size(); i++) {
				CodeFragment delete_codeFragment = list_delete.get(i);
				List<String> delete_ngramContent = delete_codeFragment
						.getNgramContent();
				similar = similarityCompare(after_fix_ngramContent,
						delete_ngramContent);
				if (similar)
					delete_count.add(i);
			}

			// 複数存在すれば，集約と決定
			if (delete_count.size() > 0 && before_fix_count == 1) {
				dbCreate2(statement, after_fix_codeFragment,
						before_fix_codeFragment, delete_count,
						current_revision_num);
				file_writer2(after_fix_codeFragment, before_fix_codeFragment,
						delete_count, current_revision_num);
				CandidateSqueezer.candidateCount++;
			}
			delete_count.clear();
		}
	}

	/**
	 * 修正前と修正後の対応付け
	 *
	 * @param after_fix_codeFragment
	 * @return before_fix_codeFragment
	 */
	private CodeFragment searchBeforeCode(CodeFragment after_fix_codeFragment) {
		long before_element_id = -1;
		for (CodeFragmentLink link : list_link) {
			if (link.getAfter_element_id() == after_fix_codeFragment.getId()) {
				before_element_id = link.getBefore_element_id();
				break;
			}
		}
		for (CodeFragment before_fix_codeFragment : list_before_fix) {
			if (before_fix_codeFragment.getId() == before_element_id)
				return before_fix_codeFragment;
		}
		return null;
	}

	/**
	 * N-gram作成
	 *
	 * @param sourceCode
	 * @param n
	 * @return N-gram
	 */
	private List<String> createNgram(List<String> sourceCode, int n) {
		List<String> ngramContent = new ArrayList<String>();
		String tmp;
		for (int i = 0; i < sourceCode.size() - 2; i++) {
			tmp = sourceCode.get(i) + sourceCode.get(i + 1)
					+ sourceCode.get(i + 2);
			ngramContent.add(tmp);
		}
		return ngramContent;
	}

	/**
	 * 2つのコード片の類似度を比較
	 *
	 * @param list
	 * @param list2
	 * @return true or false
	 */
	private boolean similarityCompare(List<String> list, List<String> list2) {
		List<Integer> count = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < list2.size(); j++) {
				if (list.get(i).equals(list2.get(j))) {
					if (count.contains(j))
						continue;
					else {
						count.add(j);
						break;
					}
				}
			}
		}

		if (((double) count.size() / (double) list.size()) >= threshold
				&& ((double) count.size() / (double) list2.size()) >= threshold)
			return true;
		else return false;
	}

	/**
	 * 集約されたメソッドの候補をデータベースに登録
	 * {削除(複数) => 追加}
	 *
	 * @param statement
	 * @param delete_count
	 * @param add_codeFragment
	 * @param current_revision_num
	 */
	private void dbCreate(Statement statement, List<Integer> delete_count,
			CodeFragment add_codeFragment, int current_revision_num) {
		try {
			statement.execute(insertValues(add_codeFragment,
					current_revision_num, "add"));
			for (int i = 0; i < delete_count.size(); i++)
				statement.execute(insertValues(
						list_delete.get(delete_count.get(i)),
						current_revision_num, "delete"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 集約されたメソッドの候補をデータベースに登録
	 *{削除(複数)+修正前 => 修正後}
	 *
	 * @param statement
	 * @param after_fix_codeFragment
	 * @param before_fix_codeFragment
	 * @param delete_count
	 * @param current_revision_num
	 */
	private void dbCreate2(Statement statement,
			CodeFragment after_fix_codeFragment,
			CodeFragment before_fix_codeFragment, List<Integer> delete_count,
			int current_revision_num) {
		try {
			statement.execute(insertValues(after_fix_codeFragment,
					current_revision_num, "after_fix"));
			statement.execute(insertValues(before_fix_codeFragment,
					current_revision_num, "before_fix"));
			for (int i = 0; i < delete_count.size(); i++)
				statement.execute(insertValues(
						list_delete.get(delete_count.get(i)),
						current_revision_num, "delete"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * データベース登録クエリの生成
	 *
	 * @param codeFragment
	 * @param current_revision_num
	 * @param process
	 * @return insert
	 */
	private String insertValues(CodeFragment codeFragment,
			int current_revision_num, String process) {
		String insert = "insert into CANDIDATE values ("
				+ CandidateSqueezer.candidateCount + "," + codeFragment.getId()
				+ "," + current_revision_num + "," + "\"" + process + "\")";
		return insert;
	}

	/*
	 *
	 * いったんファイル生成
	 */
	private void file_writer(List<Integer> delete_count,
			CodeFragment add_codeFragment, int current_revision_num) {
		try {
			File file = new File("F:/tmp/" + current_revision_num + "_"
					+ add_codeFragment.getId() + ".txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
					file)));
			pw.println("add_sourcecode");
			pw.println(add_codeFragment.getId());
			pw.println(add_codeFragment.getContent());
			pw.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			pw.println("delete_sourcecode");
			for (int i = 0; i < delete_count.size(); i++) {
				pw.println(list_delete.get(delete_count.get(i)).getId());
				pw.println(list_delete.get(delete_count.get(i)).getContent());
				pw.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void file_writer2(CodeFragment after_fix_codeFragment,
			CodeFragment before_fix_codeFragment, List<Integer> delete_count,
			int current_revision_num) {
		try {
			File file = new File("F:/tmp/" + current_revision_num + "_"
					+ after_fix_codeFragment.getId() + ".txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
					file)));
			pw.println("after_sourcecode");
			pw.println(after_fix_codeFragment.getId());
			pw.println(after_fix_codeFragment.getContent());
			pw.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			pw.println("before_fix_sourcecode");
			pw.println(before_fix_codeFragment.getId());
			pw.println(before_fix_codeFragment.getContent());
			pw.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			pw.println("delete_sourcecode");
			for (int i = 0; i < delete_count.size(); i++) {
				pw.println(list_delete.get(delete_count.get(i)).getId());
				pw.println(list_delete.get(delete_count.get(i)).getContent());
				pw.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
