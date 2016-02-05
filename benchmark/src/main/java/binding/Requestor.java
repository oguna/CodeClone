package binding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * @author y-yusuke
 *
 */
public class Requestor extends FileASTRequestor {
	List<Binding> binds = new ArrayList<Binding>();

	public Requestor(List<Binding> binds) {
		this.binds = binds;
	}

	@Override
	public void acceptAST(String path, CompilationUnit unit) {
		unit.accept(new ASTVisitor (){
			private TypeDeclaration nodeClass = null;
			private MethodDeclaration nodeMethod = null;
			List<Object> parameters = null;

			@Override
			public boolean visit(TypeDeclaration node){
				this.nodeClass = node;
				return super.visit(node);
			}
			@Override
			public boolean visit(MethodDeclaration node){
				this.nodeMethod = node;
				this.parameters = node.parameters();
				return super.visit(node);
			}
			@Override
			public boolean visit(MethodInvocation node) {
				IMethodBinding b = node.resolveMethodBinding();
				if(b != null){
					Binding bind = new Binding();
					if(unit.getPackage() != null){
						if(nodeClass != null) bind.setInvokeClass(unit.getPackage().getName().toString() + "." + nodeClass.getName().toString());
						else bind.setInvokeClass("");
						if(nodeMethod != null) {
							String invokeMethod = nodeMethod.getName().toString();
							if(parameters != null){
								invokeMethod = invokeMethod + "(";
								for(Object parameter : parameters){
									String[] type = parameter.toString().split(" ", 0);
									invokeMethod = invokeMethod + type[0] + " ";
								}
								invokeMethod = invokeMethod + ")";
							}
							bind.setInvokeMethod(invokeMethod);
						}
						else bind.setInvokeMethod("");

						bind.setTargetClass(b.getDeclaringClass().getQualifiedName());
						String targetMethod = b.getName();
						ITypeBinding[] targetParameters = b.getParameterTypes();
						if (targetParameters != null) {
							targetMethod = targetMethod + "(";
							  for (int i = 0; i < targetParameters.length; i++)
								  targetMethod = targetMethod + targetParameters[i].getName() + " ";
							  targetMethod = targetMethod + ")";
						}
						bind.setTargetMethod(targetMethod);
						binds.add(bind);
					}
				}
				return super.visit(node);
			}
		});
	}
}
