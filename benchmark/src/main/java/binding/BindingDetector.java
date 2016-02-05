package binding;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.*;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.App;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
/**
 * @author y-yusuke
 *
 */
public class BindingDetector {
	SVNRepository repository;
	long before_revision;
	long after_revision;
	ResultSet result_binding_before;
	ResultSet result_binding_after;
	Statement statement_oracle;
	List<Candidate> beforeCandidates = new ArrayList<Candidate>();
	List<Candidate> deleteCandidates = new ArrayList<Candidate>();
	List<Candidate> afterCandidates = new ArrayList<Candidate>();
	List<Candidate> addCandidates = new ArrayList<Candidate>();

	public BindingDetector(SVNRepository repository, long before_revision, long after_revision,
			ResultSet result_binding_before, ResultSet result_binding_after, Statement statement_oracle) {
		this.repository = repository;
		this.before_revision = before_revision;
		this.after_revision = after_revision;
		this.result_binding_before = result_binding_before;
		this.result_binding_after = result_binding_after;
		this.statement_oracle = statement_oracle;
	}

	public BindingDetector() {
	}

	public void execute() {
		getCandidates(before_revision, result_binding_before);
		getCandidates(after_revision, result_binding_after);
		List<Binding> beforeBindings = getBindings(before_revision);
		List<Binding> afterBindings = getBindings(after_revision);

		List<Candidate> oracles = new ArrayList<Candidate>();
		List<Candidate> tmpOracles = new ArrayList<Candidate>();
		List<Candidate> oracles1 = integratePattern1(beforeBindings, afterBindings);
		List<Candidate> oracles2 = integratePattern2(beforeBindings, afterBindings);

		for(Candidate oracle : oracles1) tmpOracles.add(oracle);
		for(Candidate oracle : oracles2) tmpOracles.add(oracle);

		oracles = garbageRemove(tmpOracles);
		dbCreate(oracles);
	}
	/**
	 * 集約パターン1の呼び出し関係
	 * @param beforeBindings
	 * @param afterBindings
	 * @return
	 */
	private List<Candidate> integratePattern1(List<Binding> beforeBindings, List<Binding> afterBindings) {
		List<Candidate> oracles = new ArrayList<Candidate>();
		for(Candidate addCandidate : addCandidates){
			List<Candidate> tmpOracles = new ArrayList<Candidate>();
			//addメソッドの呼び出し箇所を把握
			List<Binding> addInvokers = new ArrayList<Binding>();
			for(Binding afterBinding : afterBindings){
				if(afterBinding.getTargetClass().equals(addCandidate.getTargetClass())
					&& afterBinding.getTargetMethod().equals(addCandidate.getTargetMethod()))
					addInvokers.add(afterBinding);
			}
			//deleteメソッドの呼び出し箇所を把握
			for(Candidate deleteCandidate : deleteCandidates){
				if(deleteCandidate.getId() == addCandidate.getId()){
					List<Binding> deleteInvokers = new ArrayList<Binding>();
					for(Binding beforeBinding : beforeBindings){
						if(beforeBinding.getTargetClass().equals(deleteCandidate.getTargetClass())
							&& beforeBinding.getTargetMethod().equals(deleteCandidate.getTargetMethod()))
							deleteInvokers.add(beforeBinding);
					}
					boolean flag = false;
					for(Binding deleteInvoker : deleteInvokers){
						for(Binding addInvoker : addInvokers){
							if(deleteInvoker.getInvokeClass().equals(addInvoker.getInvokeClass())
								&& deleteInvoker.getInvokeMethod().equals(addInvoker.getInvokeMethod())){
									tmpOracles.add(deleteCandidate);
									flag = true;
									break;
							}
						}
						if(flag) break;
					}
				}
			}
			if(tmpOracles.size() > 1){
				tmpOracles.add(addCandidate);
				for(Candidate tmpOracle : tmpOracles) oracles.add(tmpOracle);
			}
			tmpOracles.clear();
		}
		return oracles;
	}

	/**
	 * 集約パターン2の呼び出し関係
	 * @param beforeBindings
	 * @param afterBindings
	 * @return
	 */
	private List<Candidate> integratePattern2(List<Binding> beforeBindings, List<Binding> afterBindings) {
		List<Candidate> oracles = new ArrayList<Candidate>();
		for(Candidate afterCandidate : afterCandidates){
			List<Candidate> tmpOracles = new ArrayList<Candidate>();
			boolean flag;

			//after_fixメソッドの呼び出し箇所を把握
			List<Binding> afterInvokers = new ArrayList<Binding>();
			for(Binding afterBinding : afterBindings){
				if(afterBinding.getTargetClass().equals(afterCandidate.getTargetClass())
					&& afterBinding.getTargetMethod().equals(afterCandidate.getTargetMethod()))
					afterInvokers.add(afterBinding);
			}

			//before_fixメソッドの呼び出し箇所を把握
			List<Binding> beforeInvokers = new ArrayList<Binding>();
			for(Candidate beforeCandidate : beforeCandidates){
				if(beforeCandidate.getId() == afterCandidate.getId()){
					for(Binding beforeBinding : beforeBindings){
						if(beforeBinding.getTargetClass().equals(beforeCandidate.getTargetClass())
							&& beforeBinding.getTargetMethod().equals(beforeCandidate.getTargetMethod()))
							beforeInvokers.add(beforeBinding);
					}
					tmpOracles.add(beforeCandidate);
					break;
				}
			}

			//after_fixメソッドとbefore_fixメソッドで共通する呼び出し箇所を削除
			flag = false;
			for(Binding beforeInvoker : beforeInvokers){
				for(int i = afterInvokers.size() - 1 ; i >= 0 ; i --){
					Binding afterInvoker = afterInvokers.get(i);
					if(beforeInvoker.getInvokeClass().equals(afterInvoker.getInvokeClass())
							&& beforeInvoker.getInvokeMethod().equals(afterInvoker.getInvokeMethod())){
						afterInvokers.remove(i);
						flag = true;
						break;
					}
				}
			}

			//if(!flag) continue;

			//deleteメソッドの呼び出し箇所を把握
			for(Candidate deleteCandidate : deleteCandidates){
				if(deleteCandidate.getId() == afterCandidate.getId()){
					List<Binding> deleteInvokers = new ArrayList<Binding>();
					for(Binding beforeBinding : beforeBindings){
						if(beforeBinding.getTargetClass().equals(deleteCandidate.getTargetClass())
							&& beforeBinding.getTargetMethod().equals(deleteCandidate.getTargetMethod()))
							deleteInvokers.add(beforeBinding);
					}
					flag = false;
					if(afterInvokers.size() != 0){
						for(Binding deleteInvoker : deleteInvokers){
							for(Binding afterInvoker : afterInvokers){
								if(deleteInvoker.getInvokeClass().equals(afterInvoker.getInvokeClass())
									&& deleteInvoker.getInvokeMethod().equals(afterInvoker.getInvokeMethod())){
										tmpOracles.add(deleteCandidate);
										flag = true;
										break;
								}
							}
							if(flag) break;
						}
					}else if(deleteInvokers.size() == 0) tmpOracles.add(deleteCandidate);
				}
			}
			if(tmpOracles.size() > 1){
				tmpOracles.add(afterCandidate);
				for(Candidate tmpOracle : tmpOracles) oracles.add(tmpOracle);
			}
			tmpOracles.clear();
		}
		return oracles;
	}

	/**
	 * 削除されたメソッドのゴミを除去
	 * @param tmpOracles
	 * @return
	 */
	private List<Candidate> garbageRemove(List<Candidate> tmpOracles) {
		List<Candidate> deleteOracles = new ArrayList<Candidate>();
		List<Candidate> afterOracles = new ArrayList<Candidate>();
		List<Candidate> addOracles = new ArrayList<Candidate>();

		for(Candidate tmpOracle : tmpOracles) {
			if(tmpOracle.getProcess().equals("delete")) deleteOracles.add(tmpOracle);
			else if(tmpOracle.getProcess().equals("after_fix")) afterOracles.add(tmpOracle);
			else if(tmpOracle.getProcess().equals("add")) addOracles.add(tmpOracle);
		}

		//重複したdeleteメソッドの除去
		List<Integer> removeIndex = new ArrayList<Integer>();
		for(int i = 0 ; i < deleteOracles.size() - 1 ; i++){
			boolean flag = false;
			for(int remove : removeIndex) if(i == remove) flag = true;
			if(flag) continue;
			Candidate deleteOracle = deleteOracles.get(i);
			long codeFragmentId = deleteOracle.getCodeFragmentId();
			List<Candidate> duplicates = new ArrayList<Candidate>();
			for(int j = i + 1 ; j < deleteOracles.size() ; j++){
				flag = false;
				for(int remove : removeIndex) if(j == remove) flag = true;
				if(flag) continue;
				if(codeFragmentId == deleteOracles.get(j).getCodeFragmentId()){
					duplicates.add(deleteOracles.get(j));
					removeIndex.add(j);
				}
			}
			if(duplicates.size() == 0) continue;

			//類似度が最も高いdeleteメソッドの検出
			duplicates.add(deleteOracle);
			double max = 0;
			double similariy = 0;
			long id = -1;
			for(Candidate duplicate : duplicates){
				for(Candidate afterOracle : afterOracles){
					if(duplicate.getId() == afterOracle.getId()){
						similariy = caluclateSimilarity(duplicate, afterOracle);
						break;
					}
				}
				if(similariy == 0){
					for(Candidate addOracle : addOracles){
						if(duplicate.getId() == addOracle.getId()){
							similariy = caluclateSimilarity(duplicate, addOracle);
							break;
						}
					}
				}
				if(max < similariy){
					max = similariy;
					id = duplicate.getId();
				}
			}

			//ゴミdeleteメソッドを削除
			for(int j = tmpOracles.size() -1  ; j >= 0 ; j--){
				if(codeFragmentId == tmpOracles.get(j).getCodeFragmentId()
						&& id != tmpOracles.get(j).getId()) tmpOracles.remove(j);
			}
		}
		//ゴミafter_fix，before_fixメソッドを削除
		for(int i = 0 ; i < afterOracles.size() ; i++){
			long id = afterOracles.get(i).getId();
			int count = 0;
			for(Candidate tmpOracle : tmpOracles){
				if(id == tmpOracle.getId()) count++;
			}
			if(count == 2){
				for(int j = tmpOracles.size() - 1 ; j >= 0 ; j--){
					if(id == tmpOracles.get(j).getId() && tmpOracles.get(j).getProcess().equals("before_fix"))
						tmpOracles.remove(j);
					else if(id == tmpOracles.get(j).getId() && tmpOracles.get(j).getProcess().equals("after_fix"))
						tmpOracles.remove(j);
				}
			}
		}
		//ゴミadd，deleteメソッドを削除
		for(int i = 0 ; i < addOracles.size() ; i++){
			long id = addOracles.get(i).getId();
			int count = 0;
			for(Candidate tmpOracle : tmpOracles){
				if(id == tmpOracle.getId()) count++;
			}
			if(count <= 2){
				for(int j = tmpOracles.size() - 1  ; j >= 0 ; j--){
					if(id == tmpOracles.get(j).getId() && tmpOracles.get(j).getProcess().equals("delete"))
						tmpOracles.remove(j);
					else	if(id == tmpOracles.get(j).getId() && tmpOracles.get(j).getProcess().equals("add"))
						tmpOracles.remove(j);
				}
			}
		}
		return tmpOracles;
	}

	/**
	 * メソッドの類似度算出
	 * 正規化なし
	 * @param duplicate
	 * @param afterOracle
	 * @return
	 */
	private double caluclateSimilarity(Candidate duplicate, Candidate afterOracle) {
		String sourceCode1 = getSourceCode(duplicate);
		List<String> tokens1 = ngramTokenCreate(duplicate, sourceCode1);
		String sourceCode2 = getSourceCode(afterOracle);
		List<String> tokens2 = ngramTokenCreate(duplicate, sourceCode2);
		List<Integer> count = new ArrayList<Integer>();
		for (int i = 0; i < tokens1.size(); i++) {
			for (int j = 0; j < tokens2.size(); j++) {
				if (tokens1.get(i).equals(tokens2.get(j))) {
					if (count.contains(j))
						continue;
					else {
						count.add(j);
						break;
					}
				}
			}
		}
		return (double) count.size() / (double) tokens1.size();
	}

	/**
	 * 対象メソッドのソースコード取得
	 * @param candidate
	 * @return
	 */
	private String getSourceCode(Candidate candidate) {
		SVNProperties fileProperties = new SVNProperties();
		OutputStream content = new ByteArrayOutputStream ();
		try {
			String filePath;
			if(!App.repository_additional_location.equals("")) filePath = App.repository_additional_location + "/" + candidate.getFilePath();
			else filePath = candidate.getFilePath();
			repository.getFile(filePath, candidate.getRevisionIdentifier(), fileProperties, content);
		} catch (SVNException e) {
			e.printStackTrace();
		}

		final ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toString().toCharArray());
		final CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());

		unit.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodDeclaration node) {
				if (node.isConstructor()) return super.visit(node);
				//候補のメソッドかどうかを判断
				int startLine;
				if(node.getJavadoc() != null) startLine = unit.getLineNumber(node.getJavadoc().getStartPosition());
				else startLine = unit.getLineNumber(node.getName().getStartPosition());
				int endLine = unit.getLineNumber(node.getStartPosition()+ node.getLength());
				if(candidate.getStart() != startLine || candidate.getEnd() != endLine) return super.visit(node);
				candidate.setSourceCode(node.toString());
				return super.visit(node);
			}
		});
		return candidate.getSourceCode();
	}

	/**
	 * 対象メソッドをn-gram単位に切り分け
	 * @param oracle
	 * @return
	 */
	private List<String> ngramTokenCreate(Candidate candidate, String source){
		List<String> tokens = new ArrayList<String>();

		Scanner scanner = new Scanner();
		scanner.setSource(source.toCharArray());
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
				else if(tokenType == TokenNameIdentifier) {
					tokens.add(scanner.getCurrentTokenString());
				} else if (tokenType == TokenNameIntegerLiteral
						|| tokenType == TokenNameLongLiteral || tokenType == TokenNameFloatingPointLiteral
						|| tokenType == TokenNameDoubleLiteral || tokenType == TokenNameCharacterLiteral
						|| tokenType == TokenNameStringLiteral) {
					tokens.add(scanner.getCurrentTokenString());
				}else {
					tokens.add(scanner.getCurrentTokenString());
				}
			} catch (InvalidInputException e) {
				e.printStackTrace();
			}
		}
		List<String> ngramTokens = new ArrayList<String>();
		for (int i = 0; i < tokens.size() - 2; i++) {
			String tmp = tokens.get(i) + tokens.get(i + 1)
					+ tokens.get(i + 2);
			ngramTokens.add(tmp);
		}
		return ngramTokens;
	}

	/**
	 * 集約されたメソッドをデータベースに登録
	 * @param oracles
	 */
	private void dbCreate(List<Candidate> oracles){
		try {
			for(Candidate oracle : oracles)
				statement_oracle.execute(insertValues(oracle));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * データベース登録クエリの生成
	 * @return insert
	 */
	private String insertValues(Candidate oracle) {
		String insert = "insert into ORACLE values ("
				+ oracle.getId() + "," + oracle.getCodeFragmentId() + ","
				+ oracle.getRevision() + "," + "\"" + oracle.getProcess()+ "\","
				+ "\"" + oracle.getRepositoryRootURL()+ "\","
				+ "\"" + App.repository_additional_location+ "\"," + "\"" + oracle.getFilePath()+ "\","
				+ oracle.getRevisionIdentifier() + "," + oracle.getStart() + "," + oracle.getEnd() +")";
		return insert;
	}

	/**
	 * 集約されたメソッドの候補をデータベースから取得
	 * @param revision
	 * @return
	 * @throws SQLException
	 */
	private void getCandidates(long revision, ResultSet result_binding) {
		try {
			while(result_binding.next()){
				if(result_binding.getLong(7) == revision){
					Candidate candidate = getPackageMethod(result_binding.getString(6),
							result_binding.getLong(7), result_binding.getLong(8), result_binding.getLong(9));
					candidate.setId(result_binding.getInt(1));
					candidate.setCodeFragmentId(result_binding.getLong(2));
					candidate.setRevision(result_binding.getLong(3));
					candidate.setProcess(result_binding.getString(4));
					candidate.setRepositoryRootURL(result_binding.getString(5));
					candidate.setFilePath(result_binding.getString(6));
					candidate.setRevisionIdentifier(result_binding.getLong(7));
					candidate.setStart(result_binding.getLong(8));
					candidate.setEnd(result_binding.getLong(9));
					if(candidate.getProcess().equals("before_fix")) beforeCandidates.add(candidate);
					else if(candidate.getProcess().equals("delete")) deleteCandidates.add(candidate);
					else if(candidate.getProcess().equals("after_fix")) afterCandidates.add(candidate);
					else if(candidate.getProcess().equals("add")) addCandidates.add(candidate);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 集約されたメソッドの候補が存在するパッケージ名，クラス名，メソッド名を取得
	 * @param filePath
	 * @param revision
	 * @param start
	 * @param end
	 * @return
	 */
	private Candidate getPackageMethod(String filePath, long revision, long start, long end){
		Candidate candidate = new Candidate();
		try {
			SVNProperties fileProperties = new SVNProperties();
			OutputStream content = new ByteArrayOutputStream ();
			if(!App.repository_additional_location.equals("")) filePath = App.repository_additional_location + "/" +filePath;
			repository.getFile(filePath, revision, fileProperties, content);
			final ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(content.toString().toCharArray());
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
					if(start != startLine || end != endLine) return super.visit(node);

					candidate.setTargetClass(unit.getPackage().getName().toString() + "." + nodeClass.getName().toString());
					String targetMethod = node.getName().toString();
					List<Object> parameters = node.parameters();
					if(parameters != null){
						targetMethod = targetMethod + "(";
						for(Object parameter : parameters){
							String[] type = parameter.toString().split(" ", 0);
							targetMethod = targetMethod + type[0] + " ";
						}
						targetMethod = targetMethod + ")";
					}
					candidate.setTargetMethod(targetMethod);
					return super.visit(node);
				}
			});
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return candidate;
	}

	/**
	 * revisionのbindingを取得
	 * 呼び出す側のパッケージ名，クラス名，メソッド名
	 * 呼び出される側のパッケージ名，クラス名，メソッド名
	 * @param revision
	 * @return
	 */
	public List<Binding> getBindings(long revision) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		final Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		String[] keys = new String[] {};

		List<String> filePaths = checkOutFiles(revision);
		String[] sources = filePaths.toArray(new String[0]);
		String[] classpathEntries = {};
		String[] sourcepathEntries = {};
		parser.setEnvironment(classpathEntries, sourcepathEntries, null, true);

		List<Binding> binds = new ArrayList<Binding>();
		parser.createASTs(sources, null, keys, new Requestor(binds),new NullProgressMonitor());
		return binds;
	}

	/**
	 * revisionのソースコードをチェックアウト
	 * @param revision
	 * @return
	 */
	private List<String> checkOutFiles(long revision) {
		List<String> sources = new ArrayList<String>();
		List<String> filePaths = getFilePath(revision);
		SVNProperties fileProperties = new SVNProperties();
		File checkOutDir = new File(App.tmp_location + "/" + revision);
		if(checkOutDir.exists()){
			for(String filePath : filePaths){
				filePath = filePath.replace(App.repository_location,"");
				sources.add(App.tmp_location + "/" + revision + filePath);
			}
		} else {
			checkOutDir.mkdir();
			for(String filePath : filePaths){
				filePath = filePath.replace(App.repository_location,"");
				try {
					OutputStream content = new ByteArrayOutputStream ();
					repository.getFile(filePath, revision, fileProperties, content);
					File file = new File(App.tmp_location + "/" + revision + filePath);
				    File dir = new File(file.getParent());
				    if(!dir.exists()) dir.mkdirs();
				    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
					pw.write(content.toString());
					pw.close();
				} catch (SVNException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			    sources.add(App.tmp_location + "/" + revision + filePath);
			}
		}
		return sources;
	}

	/**
	 * revisionに存在するjavaファイルのリストを取得
	 * @param revision
	 * @return
	 */
	private List<String> getFilePath(long revision) {
		try {
			SVNURL svnURL = SVNURL.parseURIEncoded(App.repository_location + "/" + App.repository_additional_location);
			ISVNAuthenticationManager authManager = null;
			List<String> filePaths = new ArrayList<String>();
			SVNLogClient logClient = new SVNLogClient(authManager, null);
			try{
				logClient.doList(svnURL, SVNRevision.create(revision),
					SVNRevision.create(revision), false, true,
					new ISVNDirEntryHandler() {
						public void handleDirEntry(SVNDirEntry dirEntry){
							if (dirEntry.getKind() == SVNNodeKind.FILE){
								//javaのみ
								if(dirEntry.getURL().toString().endsWith(".java"))
									filePaths.add(dirEntry.getURL().toString());
							}
						}
					}
				);
			} catch (NullPointerException e) {}
			return filePaths;
		} catch (SVNException e) {
			e.printStackTrace();
			return null;
		}
	}
}
