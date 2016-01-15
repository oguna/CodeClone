package binding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IMethodBinding;
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

			@Override
			public boolean visit(TypeDeclaration node){
				this.nodeClass = node;
				return super.visit(node);
			}
			@Override
			public boolean visit(MethodDeclaration node){
				this.nodeMethod = node;
				return super.visit(node);
			}
			@Override
			public boolean visit(MethodInvocation node) {
				IMethodBinding b = node.resolveMethodBinding();
				if(b != null){
					Binding bind = new Binding();
					if(unit.getPackage() != null){
						if(nodeClass != null) bind.setInvokeClass(unit.getPackage().getName().toString() + "." + nodeClass.getName().toString());
						else bind.setInvokeClass(null);
						if(nodeMethod != null) bind.setInvokeMethod(nodeMethod.getName().toString());
						else bind.setInvokeMethod(null);
						bind.setTargetClass(b.getDeclaringClass().getQualifiedName());
						bind.setTargetMethod(b.getName());
						binds.add(bind);
					}
				}
				return super.visit(node);
			}
		});
	}
}
