package binding;

/**
 * @author y-yusuke
 *
 */
public class Candidate {
	int id;
	String process;
	String declarationClass;
	String declarationMethod;

	public Candidate(){
	}

	public int getId() {
		return id;
	}
	public String getProcess() {
		return process;
	}
	public String getDeclarationClass() {
		return declarationClass;
	}
	public String getDeclarationMethod() {
		return declarationMethod;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setProcess(String process) {
		this.process = process;
	}
	public void setDeclarationClass(String declarationClass) {
		this.declarationClass = declarationClass;
	}
	public void setDeclarationMethod(String declarationMethod) {
		this.declarationMethod = declarationMethod;
	}
}
