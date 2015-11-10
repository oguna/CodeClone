package similarity;

import java.util.LinkedList;

import mining.CodeFragment;

public class CalculateSimilarity {

	public final static int n = 3;
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

	public void execute(){
		for(int i = 0; i < list_delete.size() ; i++){
			CodeFragment codeFragment = new CodeFragment();
			codeFragment = list_delete.get(i);
			LinkedList<String> ngramContent = new LinkedList<String>();
			ngramContent = createNgram(codeFragment.getNormalizeContent(),n);
			codeFragment.setNgramContent(ngramContent);
		}
		for(int i = 0; i < list_before_fix.size() ; i++){
			CodeFragment codeFragment = new CodeFragment();
			codeFragment = list_before_fix.get(i);
			LinkedList<String> ngramContent = new LinkedList<String>();
			ngramContent = createNgram(codeFragment.getNormalizeContent(),n);
			codeFragment.setNgramContent(ngramContent);
		}
		for(int i = 0 ; i < list_add.size() ; i++){

		}
	}

	private LinkedList<String> createNgram(LinkedList<String> sourceCode,int n){
		LinkedList<String> ngramContent = new LinkedList<String>();
		String tmp;
		for(int i = 0 ; i < sourceCode.size() - 1 ; i++){
			tmp = sourceCode.get(i) + sourceCode.get(i+1) + sourceCode.get(i+2);
			ngramContent.add(tmp);
		}
		return ngramContent;
	}
}
