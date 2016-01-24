package checker;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.*;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import main.App;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.io.SVNRepository;

import binding.Binding;
import binding.BindingDetector;

public class Output {
	SVNRepository repository;

	public Output(SVNRepository repository) {
		this.repository = repository;
	}

	public void execute(){
		Connection connection = null;
		Statement statement = null;
		try {
			// データベースに接続
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + App.database_location);
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery("select * from ORACLE");
			List<Oracle> oracles = new ArrayList<Oracle>();
			File datasetsFile = new File(CheckerMain.datasets_location + CheckerMain.repository_name + ".txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(datasetsFile)));

			long id = -1;
			while(result.next()){
				Oracle oracle = new Oracle(result.getLong(1), result.getLong(8), result.getString(4), result.getString(7), result.getLong(9), result.getLong(10));
				SVNProperties fileProperties = new SVNProperties();
				OutputStream content = new ByteArrayOutputStream ();

				String filePath;
				if(!App.repository_additional_location.equals("")) filePath = App.repository_additional_location + "/" + oracle.getFilePath();
				else filePath = oracle.getFilePath();
				repository.getFile(filePath, oracle.getRevision(), fileProperties, content);
				oracle = getMethodInfo(oracle, content.toString());

				if(oracle.getProcess().equals("before_fix") || oracle.getProcess().equals("delete")){
					if(id != result.getLong(1) && id != -1){
						datasetsOutput(oracles, pw);
						oracles.clear();
					}
					oracles.add(oracle);
					javaFileOutput(oracle, content.toString());
				}

				File methodInfoFile = new File(CheckerMain.output_location + CheckerMain.repository_name + "/methodInfo/" + oracle.getId() + "_" + oracle.getRevision() + "/" + result.getLong(2) + "_" + oracle.getProcess() + ".txt");
			    File methodInfoDir = new File(methodInfoFile.getParent());
			    if(!methodInfoDir.exists()) methodInfoDir.mkdirs();
				PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(methodInfoFile)));
				methodInfoOutput(oracle, pw2);
				pw2.close();
				id = result.getLong(1);
			}
			datasetsOutput(oracles, pw);
			pw.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SVNException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 該当メソッドを含むjavaファイル出力
	 * @param oracle
	 * @param content
	 */
	private void javaFileOutput(Oracle oracle, String content){
		try {
			File file = new File(CheckerMain.output_location + CheckerMain.repository_name + "/" + oracle.getRevision() + "/" + App.repository_additional_location + "/" + oracle.getFilePath());
		    File dir = new File(file.getParent());
		    if(!dir.exists()) dir.mkdirs();
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.write(content);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 該当メソッドの情報をデータセット化
	 * @param oracle
	 * @param pw
	 */
	private void datasetsOutput(List<Oracle> oracles, PrintWriter pw){
		for(int i = 0 ; i < oracles.size() ; i++){
			Oracle oracle1 = oracles.get(i);
			for(int j = i + 1 ; j < oracles.size() ; j++){
				Oracle oracle2 = oracles.get(j);
				int cloneType = identifyCloneType(oracle1, oracle2);
				pw.write(CheckerMain.repository_name + "/" + oracle1.getRevision() + "/" + App.repository_additional_location + "/" + oracle1.getFilePath());
				pw.write("\t");
				pw.write(String.valueOf(oracle1.getStart()));
				pw.write("\t");
				pw.write(String.valueOf(oracle1.getEnd()));
				pw.write("\t");
				pw.write(CheckerMain.repository_name + "/" + oracle2.getRevision() + "/" + App.repository_additional_location + "/" + oracle2.getFilePath());
				pw.write("\t");
				pw.write(String.valueOf(oracle2.getStart()));
				pw.write("\t");
				pw.write(String.valueOf(oracle2.getEnd()));
				pw.write("\t");
				pw.write(String.valueOf(cloneType));
				pw.write("\n");
			}
		}
	}

	/**
	 * クローンTypeの識別
	 * Type1のコーディングスタイルにおいて，「{，}，;」は無視している
	 * @param oracle1
	 * @param oracle2
	 * @return
	 */
	private int identifyCloneType(Oracle oracle1, Oracle oracle2) {
		oracle1 = nomalizedTokenCreate(oracle1);
		oracle2 = nomalizedTokenCreate(oracle2);
		boolean match;
		match = compareTokens(oracle1.getTokens(),  oracle2.getTokens());
		if(match) return 1;
		else {
			match = compareTokens(oracle1.getNormalizedTokens(),  oracle2.getNormalizedTokens());
			if(match) return 2;
			else return 3;
		}
	}

	/**
	 * 正規化したトークンが一致しているかどうか比較
	 * @param tokens1
	 * @param tokens2
	 * @return
	 */
	private boolean compareTokens(List<String> tokens1, List<String> tokens2){
		if(tokens1.size() == tokens2.size()){
			for(int i = 0 ; i < tokens1.size() ; i++){
				if(!tokens1.get(i).equals(tokens2.get(i))){
					return false;
				}
			}
			return true;
		} else return false;
	}

	/**
	 * 対象メソッドの正規化
	 * @param oracle
	 * @return
	 */
	private Oracle nomalizedTokenCreate(Oracle oracle){
		List<String> tokens = new ArrayList<String>();
		List<String> normalizedTokens = new ArrayList<String>();

		Scanner scanner = new Scanner();
		scanner.setSource(oracle.getSourcecode().toCharArray());
		scanner.recordLineSeparator = true;

		while(true){
			int tokenType;
			try {
				tokenType = scanner.getNextToken();
				if (tokenType == TokenNameEOF) break;
				else if(tokenType == TokenNameNotAToken || tokenType == TokenNameWHITESPACE
						|| tokenType == TokenNameCOMMENT_LINE || tokenType == TokenNameCOMMENT_BLOCK
						||tokenType == TokenNameCOMMENT_JAVADOC
						|| tokenType == TokenNameLBRACE || tokenType == TokenNameRBRACE
						|| tokenType == TokenNameSEMICOLON) continue;
				else if(tokenType == TokenNameIdentifier || tokenType == TokenNameIntegerLiteral
						|| tokenType == TokenNameLongLiteral || tokenType == TokenNameFloatingPointLiteral
						|| tokenType == TokenNameDoubleLiteral || tokenType == TokenNameCharacterLiteral
						|| tokenType == TokenNameStringLiteral){
					tokens.add(scanner.getCurrentTokenString());
					normalizedTokens.add("$");
				} else{
					tokens.add(scanner.getCurrentTokenString());
					normalizedTokens.add(scanner.getCurrentTokenString());
				}
			} catch (InvalidInputException e) {
				e.printStackTrace();
			}
		}

		oracle.setTokens(tokens);
		oracle.setNormalizedTokens(normalizedTokens);
		return oracle;
	}

	/**
	 * 該当メソッドの情報を出力
	 * @param oracle
	 * @param pw
	 */
	private void methodInfoOutput(Oracle oracle, PrintWriter pw) {
		pw.write("MethodLocation\n");
		pw.write(oracle.getFilePath() + "\t" + oracle.getStart() + "\t" + oracle.getEnd() + "\n\n");
		pw.write("invokeLocation\n");
		for(Binding bind : oracle.getBinds()){
			pw.write(bind.getInvokeClass() + "\t" + bind.getInvokeMethod() + "\n");
		}
		pw.write("\n");
		pw.write("SourceCode\n");
		pw.write("+++++++++++++\n");
		pw.write(oracle.getSourcecode());
		pw.write("+++++++++++++\n");
	}

	/**
	 * 該当メソッドの情報(呼び出し場所，ソースコード)を取得
	 * @param oracle
	 * @param content
	 * @return
	 */
	private Oracle getMethodInfo(Oracle oracle, String content){
		final ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray());
		final CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());

		unit.accept(new ASTVisitor() {
			private TypeDeclaration nodeClass;
			@Override
			public boolean visit(TypeDeclaration node){
				this.nodeClass = node;
				return super.visit(node);
			}
			@Override
			public boolean visit(MethodDeclaration node) {
				if (node.isConstructor()) return super.visit(node);
				//候補のメソッドかどうかを判断
				int startLine;
				if(node.getJavadoc() != null) startLine = unit.getLineNumber(node.getJavadoc().getStartPosition());
				else startLine = unit.getLineNumber(node.getName().getStartPosition());
				int endLine = unit.getLineNumber(node.getStartPosition()+ node.getLength());
				if(oracle.getStart() != startLine || oracle.getEnd() != endLine) return super.visit(node);
				oracle.setSourcecode(node.toString());
				oracle.setTargetClass(unit.getPackage().getName().toString() + "." + nodeClass.getName().toString());
				oracle.setTargetMethod(node.getName().toString());
				return super.visit(node);
			}
		});

		BindingDetector binding = new BindingDetector();
		List<Binding> allbinds = binding.getBindings(oracle.getRevision());
		List<Binding> targetbinds = new ArrayList<Binding>();
		for(Binding bind : allbinds){
			if(bind.getTargetClass().equals(oracle.getTargetClass())
					&& bind.getTargetMethod().equals(oracle.getTargetMethod()))
				targetbinds.add(bind);
		}
		oracle.setBinds(targetbinds);
		return oracle;
	}
}
