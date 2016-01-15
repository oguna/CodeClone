package binding;

/**
 * @author y-yusuke
 *
 */
public class Binding {
	String invokeClass;
	String invokeMethod;
	String targetClass;
	String targetMethod;

	public Binding(){
	}
	public String getInvokeClass() {
		return invokeClass;
	}
	public String getInvokeMethod() {
		return invokeMethod;
	}
	public String getTargetClass() {
		return targetClass;
	}
	public String getTargetMethod() {
		return targetMethod;
	}
	public void setInvokeClass(String invokeClass) {
		this.invokeClass = invokeClass;
	}
	public void setInvokeMethod(String invokeMethod) {
		this.invokeMethod = invokeMethod;
	}
	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}
	public void setTargetMethod(String targetMethod) {
		this.targetMethod = targetMethod;
	}
}
