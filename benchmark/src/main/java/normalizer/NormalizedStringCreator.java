package normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
/**
 * @author y-yusuke
 *
 */
public class NormalizedStringCreator{

	final List<String> reservedWord = new ArrayList<String>();
	String sourceCode;

	public NormalizedStringCreator (String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public List<String> execute(int start,int end) {
		List<String> tokens = new ArrayList<String>();
		final ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(sourceCode.toCharArray());
		final CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());

		unit.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodDeclaration node) {
				if (node.isConstructor()) return super.visit(node);

				init();
				StringBuffer sb = new StringBuffer();

				//変更されたメソッドかどうかを判断
				int startLine;
				if(node.getJavadoc() != null) startLine = unit.getLineNumber(node.getJavadoc().getStartPosition());
				else startLine = unit.getLineNumber(node.getName().getStartPosition());
				int endLine = unit.getLineNumber(node.getStartPosition()+ node.getLength());
				if(start != startLine || end != endLine) return super.visit(node);

				//Return Type
				sb.append(node.getReturnType2().toString());
				sb.append(" ");

				//Method Name
				sb.append(node.getName().toString());

				//Parameters
				sb.append("(");
				sb.append(StringUtils.join(node.parameters(), ", "));
				sb.append(")");

				//throws
				sb.append(" throws ");
				sb.append(StringUtils.join(node.thrownExceptionTypes(), ", "));

				//Block
				Block block = node.getBody();
				sb.append(block);

				String code = sb.toString();
				String[] tmpStatements = code.split("\n");
				List<String> statements = new ArrayList<String>();
				for (String state : tmpStatements) {
					state = state.trim();
					if (state.length() > 0) statements.add(state);
				}

				//識別子を正規化
				for(String str : statements){
					StringTokenizer st = new StringTokenizer(str," !#$%&'()-=^~|[{]}+:*,<.>/?", true);
					while (st.hasMoreTokens()) {
						String tmp = st.nextToken();
						if (isSymbol(tmp))
							tokens.add(tmp);
						else if (reservedWord.contains(tmp))
							tokens.add(tmp);
						else if (isWhiteSpace(tmp)){}
						else tokens.add("$");
					}
				}
				return super.visit(node);
			}
		});
		return tokens;
	}

	private boolean isSymbol(String tmp) {
		if (tmp.equals("!") || tmp.endsWith("#")
			|| tmp.equals("$") || tmp.equals("%") || tmp.equals("&")
			|| tmp.equals("(") || tmp.equals(")") || tmp.equals("-")
			|| tmp.equals("=") || tmp.equals("^") || tmp.equals("~")
			|| tmp.equals("|") || tmp.equals("[") || tmp.equals("]")
			|| tmp.equals("{") || tmp.equals("}") || tmp.equals("+")
			|| tmp.equals(";") || tmp.equals("*") || tmp.equals(":")
			|| tmp.equals(",") || tmp.equals("<") || tmp.equals(".")
			|| tmp.equals(">") || tmp.equals("/") || tmp.equals("?")) {
			return true;
		}
		else return false;
	}

	private static boolean isWhiteSpace(String tmp) {
		if (tmp.equals(" ") || tmp.equals("\n") || tmp.equals("\t")) return true;
		else return false;
	}

	private synchronized void init() {
		reservedWord.add("assert");reservedWord.add("boolean");
		reservedWord.add("break");reservedWord.add("byte");
		reservedWord.add("case");reservedWord.add("catch");
		reservedWord.add("char");reservedWord.add("class");
		reservedWord.add("continue");reservedWord.add("default");
		reservedWord.add("do");reservedWord.add("double");
		reservedWord.add("else");reservedWord.add("enum");
		reservedWord.add("extends");reservedWord.add("finally");
		reservedWord.add("float");reservedWord.add("for");
		reservedWord.add("goto");reservedWord.add("if");
		reservedWord.add("implements");reservedWord.add("instanceof");
		reservedWord.add("int");reservedWord.add("interface");
		reservedWord.add("long");reservedWord.add("new");
		reservedWord.add("return");reservedWord.add("short");
		reservedWord.add("super");reservedWord.add("switch");
		reservedWord.add("this");reservedWord.add("throw");
		reservedWord.add("throws");reservedWord.add("try");
		reservedWord.add("void");reservedWord.add("while");
		reservedWord.add("true");reservedWord.add("false");
		reservedWord.add("null");
	}
}
