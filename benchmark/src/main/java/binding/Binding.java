package binding;

public class Binding {
	String invocationClass;
	String invocationMethod;
	String declarationClass;
	String declarationMethod;

	public Binding() {
	}

	public String getInvocationClass() {
		return invocationClass;
	}

	public String getInvocationMethod() {
		return invocationMethod;
	}

	public String getDeclarationClass() {
		return declarationClass;
	}

	public String getDeclarationMethod() {
		return declarationMethod;
	}

	public void setInvocationClass(String invocationClass) {
		this.invocationClass = invocationClass;
	}

	public void setInvocationMethod(String invocationMethod) {
		this.invocationMethod = invocationMethod;
	}

	public void setDeclarationClass(String declarationClass) {
		this.declarationClass = declarationClass;
	}

	public void setDeclarationMethod(String declarationMethod) {
		this.declarationMethod = declarationMethod;
	}
}
