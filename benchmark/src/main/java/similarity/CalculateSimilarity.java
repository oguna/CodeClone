package similarity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import mining.CodeFragment;

public class CalculateSimilarity {

	public final static int n = 3;
	public final static double threshold = 0.7;
	LinkedList<CodeFragment> list_delete = new LinkedList<CodeFragment>();
	LinkedList<CodeFragment> list_add = new LinkedList<CodeFragment>();
	LinkedList<CodeFragment> list_before_fix = new LinkedList<CodeFragment>();
	LinkedList<CodeFragment> list_after_fix = new LinkedList<CodeFragment>();

	public CalculateSimilarity(LinkedList<CodeFragment> list_delete,
											LinkedList<CodeFragment> list_add,
											LinkedList<CodeFragment> list_before_fix,
											LinkedList<CodeFragment> list_after_fix){
		this.list_delete = list_delete;
		this.list_add = list_add;
		this.list_before_fix = list_before_fix;
		this.list_after_fix = list_after_fix;
	}

	public void execute(int current_revision_num){

		//削除されたメソッドをN-gram化
		for (CodeFragment codeFragment : list_delete) {
			LinkedList<String> ngramContent = createNgram(codeFragment.getNormalizeContent(), n);
			codeFragment.setNgramContent(ngramContent);
		}

		//修正前のメソッドをN-gram化
		for (CodeFragment codeFragment : list_before_fix) {
			LinkedList<String> ngramContent = createNgram(codeFragment.getNormalizeContent(), n);
			codeFragment.setNgramContent(ngramContent);
		}

		//集約されたメソッドを特定 (削除×* => 追加)
		for (CodeFragment add_codeFragment : list_add) {
			//追加されたメソッドをN-gram化
			LinkedList<String> add_ngramContent = createNgram(add_codeFragment.getNormalizeContent(),n);
			LinkedList<Integer> delete_count = new LinkedList<Integer>();

			//削除されたメソッドと類似度を比較
			;
			for (int i = 0 ; i < list_delete.size() ; i++) {
				CodeFragment delete_codeFragment = list_delete.get(i);
				LinkedList<String> delete_ngramContent = delete_codeFragment.getNgramContent();
				 boolean similar = compare(add_ngramContent,delete_ngramContent);
				if(similar) delete_count.add(i);
			}

			//複数存在すれば，集約と決定
			if(delete_count.size() > 1) file_writer(delete_count,add_codeFragment,current_revision_num);
			delete_count.clear();
		}

		//集約されたメソッドを特定 (削除+修正前 => 修正後)
		for (CodeFragment after_fix_codeFragment : list_after_fix) {
			//修正後のメソッドをN-gram化
			LinkedList<String> after_fix_ngramContent = createNgram(after_fix_codeFragment.getNormalizeContent(),n);
			LinkedList<Integer> delete_count = new LinkedList<Integer>();
			LinkedList<Integer> before_fix_count = new LinkedList<Integer>();

			//削除されたメソッドと類似度を比較
			for (int i = 0 ; i < list_delete.size() ; i++) {
				CodeFragment delete_codeFragment = list_delete.get(i);
				LinkedList<String> delete_ngramContent = delete_codeFragment.getNgramContent();
				 boolean similar = compare(after_fix_ngramContent,delete_ngramContent);
				if(similar) delete_count.add(i);
			}

			//修正前のメソッドと類似度を比較
			for(int i=0 ; i < list_before_fix.size() ; i++){
				CodeFragment before_fix_codeFragment = list_before_fix.get(i);
				LinkedList<String> before_fix_ngramContent = before_fix_codeFragment.getNgramContent();
				 boolean similar = compare(after_fix_ngramContent,before_fix_ngramContent);
				if(similar) before_fix_count.add(i);
			}

			//複数存在すれば，集約と決定
			if(delete_count.size()>0 && before_fix_count.size()>0){
				file_writer2(delete_count,before_fix_count,after_fix_codeFragment,current_revision_num);
			}
			delete_count.clear();
			before_fix_count.clear();
		}
	}

	//いったんファイル生成
	private void file_writer(LinkedList<Integer> delete_count, CodeFragment add_codeFragment,
			int current_revision_num) {
		try{
			File file = new File("F:/tmp/" + current_revision_num + "_" +add_codeFragment.getId() + ".txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.println("add_sourcecode");
			pw.println(add_codeFragment.getId());
			pw.println(add_codeFragment.getContent());
			pw.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			pw.println("delete_sourcecode");
			for(int i = 0 ; i < delete_count.size() ; i++){
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

	private void file_writer2(LinkedList<Integer> delete_count,LinkedList<Integer> before_fix_count,
			CodeFragment after_codeFragment, int current_revision_num) {
		try{
			File file = new File("F:/tmp/" + current_revision_num + "_" +after_codeFragment.getId() + ".txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.println("after_sourcecode");
			pw.println(after_codeFragment.getId());
			pw.println(after_codeFragment.getContent());
			pw.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			pw.println("delete_sourcecode");
			for(int i = 0 ; i < delete_count.size() ; i++){
				pw.println(list_delete.get(delete_count.get(i)).getId());
				pw.println(list_delete.get(delete_count.get(i)).getContent());
				pw.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			}
			pw.println("before_fix_sourcecode");
			for(int i = 0 ; i < before_fix_count.size() ; i++){
				pw.println(list_before_fix.get(before_fix_count.get(i)).getId());
				pw.println(list_before_fix.get(before_fix_count.get(i)).getContent());
				pw.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * N-gram作成
	 * @param sourceCode
	 * @param n
	 * @return
	 */
	private LinkedList<String> createNgram(LinkedList<String> sourceCode,int n){
		LinkedList<String> ngramContent = new LinkedList<String>();
		String tmp;
		for(int i = 0 ; i < sourceCode.size() - 2 ; i++){
			tmp = sourceCode.get(i) + sourceCode.get(i+1) + sourceCode.get(i+2);
			ngramContent.add(tmp);
		}
		return ngramContent;
	}

	/**
	 * 2つのコード片の類似度を比較
	 * @param list
	 * @param list2
	 * @return
	 */
	private boolean compare(LinkedList<String> list,LinkedList<String> list2){
		if(list.size() < 50 || list2.size() < 50) return false;

		LinkedList<Integer> count = new LinkedList<Integer>();
		for(int i = 0 ; i < list.size() ; i++){
			for(int j = 0 ; j < list2.size() ; j++){
				if(list.get(i).equals(list2.get(j))){
					if(count.contains(j)) continue;
					else {
						count.add(j);
						break;
					}
				}
			}
		}
		if(((double)count.size() / (double)list.size()) >= threshold
				&& ((double)count.size() / (double)list2.size()) >= threshold) return true;
		else return false;
	}
}
