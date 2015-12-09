package binding;


public class Binding {
	String invocationFilePath;
	String invocationClassName;
	String invocationMethodName;
	String declarationFilePath;
	String declarationClassName;
	String declarationMethodName;

	public Binding(String invocationFilePath, String invocationClassName, String invocationMethodName,
			String declarationFilePath, String declarationClassName, String declarationMethodName) {
		this.invocationFilePath = invocationFilePath;
		this.invocationClassName = invocationClassName;
		this.invocationMethodName = invocationMethodName;
		this.declarationFilePath = declarationFilePath;
		this.declarationClassName = declarationClassName;
		this.declarationMethodName = declarationMethodName;
	}

	public String getInvocationFilePath() {
		return invocationFilePath;
	}

	public String getInvocationClassName() {
		return invocationClassName;
	}

	public String getInvocationMethodName() {
		return invocationMethodName;
	}

	public String getDeclarationFilePath() {
		return declarationFilePath;
	}

	public String getDeclarationClassName() {
		return declarationClassName;
	}

	public String getDeclarationMethodName() {
		return declarationMethodName;
	}
}
