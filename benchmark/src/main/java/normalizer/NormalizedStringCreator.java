package normalizer;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
/**
 * @author y-yusuke
 *
 */
public class NormalizedStringCreator {

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

				//変更されたメソッドかどうかを判断
				int startLine;
				if(node.getJavadoc() != null) startLine = unit.getLineNumber(node.getJavadoc().getStartPosition());
				else startLine = unit.getLineNumber(node.getName().getStartPosition());
				int endLine = unit.getLineNumber(node.getStartPosition()+ node.getLength());
				if(start != startLine || end != endLine) return super.visit(node);

				Scanner scanner = new Scanner();
				scanner.setSource(node.toString().toCharArray());
				scanner.recordLineSeparator = true;

				while(true){
					int tokenType;
					try {
						tokenType = scanner.getNextToken();
						if (tokenType == TokenNameEOF) break;
						else if(tokenType == TokenNameNotAToken || tokenType == TokenNameWHITESPACE
								|| tokenType == TokenNameCOMMENT_LINE || tokenType == TokenNameCOMMENT_BLOCK
								||tokenType == TokenNameCOMMENT_JAVADOC) continue;
						else if(tokenType == TokenNameIdentifier || tokenType == TokenNameIntegerLiteral
								|| tokenType == TokenNameLongLiteral || tokenType == TokenNameFloatingPointLiteral
								|| tokenType == TokenNameDoubleLiteral || tokenType == TokenNameCharacterLiteral
								|| tokenType == TokenNameStringLiteral) tokens.add("$");
						else tokens.add(scanner.getCurrentTokenString());
					} catch (InvalidInputException e) {
						e.printStackTrace();
					}
				}
				return super.visit(node);
			}
		});
		return tokens;
	}
}
