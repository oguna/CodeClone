package checker;

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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
				File methodInfoFile = new File(CheckerMain.output_location + CheckerMain.repository_name + "/methodInfo/" + oracle.getRevision() + "_" + oracle.getId() + "/" + result.getLong(2) + "_" + oracle.getProcess() + ".txt");
			    File methodInfoDir = new File(methodInfoFile.getParent());
			    if(!methodInfoDir.exists()) methodInfoDir.mkdirs();
				PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(methodInfoFile)));
				if(oracle.getProcess().equals("before_fix") || oracle.getProcess().equals("delete")){
					if(id != result.getLong(1) && id != -1) pw.write("\n");
					else if(id == result.getLong(1)) pw.write("\t");
					javaFileOutput(oracle, content.toString());
					datasetsOutput(oracle, pw);
				}
				oracle = getMethodInfo(oracle, content.toString());
				methodInfoOutput(oracle, pw2);
				pw2.close();
				id = result.getLong(1);
			}
			pw.write("\n");
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
	private void datasetsOutput(Oracle oracle, PrintWriter pw){
		pw.write(CheckerMain.repository_name + "/" + oracle.getRevision() + "/" + App.repository_additional_location + "/" + oracle.getFilePath());
		pw.write("\t");
		pw.write(String.valueOf(oracle.getStart()));
		pw.write("\t");
		pw.write(String.valueOf(oracle.getEnd()));
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
