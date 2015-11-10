package mining;

import java.util.LinkedList;

public class CodeFragment {
	String content;
	LinkedList<String> normalizeContent = new LinkedList<String>();
	LinkedList<String> ngramContent = new LinkedList<String>();
	public LinkedList<String> getNgramContent() {
		return ngramContent;
	}

	public void setNgramContent(LinkedList<String> ngramContent) {
		this.ngramContent = ngramContent;
	}

	long id;

	public CodeFragment(){
	}

	public CodeFragment(String content, LinkedList<String> normalizeContent, long id){
		this.content = content;
		this.normalizeContent = normalizeContent;
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public LinkedList<String> getNormalizeContent() {
		return normalizeContent;
	}

	public long getId() {
		return id;
	}
}
