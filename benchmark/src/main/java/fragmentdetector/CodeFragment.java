package fragmentdetector;

import java.util.ArrayList;
import java.util.List;
/**
 * @author y-yusuke
 *
 */
public class CodeFragment {
	String source;
	String content;
	List<String> normalizeContent = new ArrayList<String>();
	List<String> ngramContent = new ArrayList<String>();
	long id;

	public CodeFragment(){
	}
	public CodeFragment(String content, List<String> normalizeContent, long id){
		this.content = content;
		this.normalizeContent = normalizeContent;
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public List<String> getNormalizeContent() {
		return normalizeContent;
	}
	public long getId() {
		return id;
	}
	public List<String> getNgramContent() {
		return ngramContent;
	}
	public void setNgramContent(List<String> ngramContent) {
		this.ngramContent = ngramContent;
	}
}
