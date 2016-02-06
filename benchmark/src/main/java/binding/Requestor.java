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
									for(int i = 0 ; i < type.length ; i++) {
										//修飾子を除去
										if(type[i].equals("final") || type[i].equals("static")
												|| type[i].equals("transient") || type[i].equals("volatile")) continue;
										else {
											//型名のみを取得
											if(type[i].indexOf(".") != -1){
												String[] type2 = type[i].split("\\.", 0);
												//型パラメータの除去
												String[] typeParameter = type2[type2.length - 1].split("<");
												invokeMethod = invokeMethod + typeParameter[0] + " ";
											}else {
												//型パラメータの除去
												String[] typeParameter = type[i].split("<");
												invokeMethod = invokeMethod + typeParameter[0] + " ";
											}
											break;
										}
									}
								}
								invokeMethod = invokeMethod + ")";
							}
							bind.setInvokeMethod(invokeMethod);
						}
						else bind.setInvokeMethod("");

						//型パラメータの除去
						String[] classTypePrameter = b.getDeclaringClass().getQualifiedName().split("<");
						bind.setTargetClass(classTypePrameter[0]);
						String targetMethod = b.getName();
						ITypeBinding[] targetParameters = b.getParameterTypes();
						if (targetParameters != null) {
							targetMethod = targetMethod + "(";
							  for (int i = 0; i < targetParameters.length; i++) {
								//型パラメータの除去
								  String[] typeParameter  = targetParameters[i].getName().split("<");
								  targetMethod = targetMethod + typeParameter[0] + " ";
							  }
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
