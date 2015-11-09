package mining;

public class CodeFragment {
	String content;
	long id;

	public CodeFragment(){
	}

	public CodeFragment(String content, long id){
		this.content = content;
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public long getId() {
		return id;
	}
}
