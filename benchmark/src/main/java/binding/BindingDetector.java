package binding;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.App;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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

public class BindingDetector {
	SVNRepository repository;
	long before_revision;
	long after_revision;
	ResultSet result_binding;

	public BindingDetector(SVNRepository repository, long before_revision, long after_revision, ResultSet result_binding) {
		this.repository = repository;
		this.before_revision = before_revision;
		this.after_revision = after_revision;
		this.result_binding = result_binding;
	}

	public void execute() {
		List<Candidate> beforeCandidates = getCandidates(before_revision);
		List<Candidate> afterCandidates = getCandidates(after_revision);
		List<Binding> beforeBindings = getBindings(before_revision);
		List<Binding> afterBindings = getBindings(after_revision);

		for(Candidate afterCandidate : afterCandidates){
			int id = afterCandidate.getId();
			//after_fixとaddメソッドの呼び出し箇所を把握
			List<Binding> afterInvocations = new ArrayList<Binding>();
			for(Binding afterBinding : afterBindings){
				if(afterBinding.getDeclarationClass().equals(afterCandidate.getDeclarationClass())
					&& afterBinding.getDeclarationMethod().equals(afterCandidate.getDeclarationMethod())){
					afterInvocations.add(afterBinding);
				}
			}
			//before_fixとdeleteメソッドの呼び出し箇所を把握
			for(Candidate beforeCandidate : beforeCandidates){
				if(beforeCandidate.getId() == id){
					for(Binding afterInvocation : afterInvocations){

					}
				}
			}
		}



		beforeCandidates = null;
		afterCandidates = null;
		beforeBindings = null;
		afterBindings = null;
	}

	private List<Candidate> getCandidates(long revision){
		List<Candidate> candidates = new ArrayList<Candidate>();
		try {
			while(result_binding.next()){
				//trunkのみを対象
				if(result_binding.getString(6).startsWith("trunk") && result_binding.getLong(7) == revision){
					Candidate candidate = getPackageMethod(result_binding.getString(6),
							result_binding.getLong(7), result_binding.getLong(8), result_binding.getLong(9));
					candidate.setId(result_binding.getInt(1));
					candidate.setProcess(result_binding.getString(4));
					candidates.add(candidate);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return candidates;
	}

	private Candidate getPackageMethod(String filePath, long revision, long start, long end){
		Candidate candidate = new Candidate();
		try {
			SVNProperties fileProperties = new SVNProperties();
			OutputStream content = new ByteArrayOutputStream ();
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
					//変更されたメソッドかどうかを判断
					int startLine;
					if(node.getJavadoc() != null) startLine = unit.getLineNumber(node.getJavadoc().getStartPosition());
					else startLine = unit.getLineNumber(node.getName().getStartPosition());
					int endLine = unit.getLineNumber(node.getStartPosition()+ node.getLength());
					if(start != startLine || end != endLine) return super.visit(node);

					candidate.setDeclarationClass(unit.getPackage().getName().toString() + "." + nodeClass.getName().toString());
					candidate.setDeclarationMethod(node.getName().toString());
					return super.visit(node);
				}
			});
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return candidate;
	}

	private List<Binding> getBindings(long revision) {
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

	private List<String> checkOutFiles(long revision) {
		List<String> sources = new ArrayList<String>();
		List<String> filePaths = getFilePath(revision);
		SVNProperties fileProperties = new SVNProperties();
		OutputStream content = new ByteArrayOutputStream ();
		File checkOutDir = new File(App.tmp_location + "/" + revision);
		if(checkOutDir.exists()){
			for(String filePath : filePaths)
				sources.add(App.tmp_location + "/" + revision + filePath);
		}else {
			checkOutDir.mkdir();
			for(String filePath : filePaths){
				filePath = filePath.replace(App.repository_location,"");
				try {
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

	private List<String> getFilePath(long revision) {
		try {
			SVNURL svnURL = SVNURL.parseURIEncoded(App.repository_location);
			ISVNAuthenticationManager authManager = null;
			List<String> filePaths = new ArrayList<String>();
			SVNLogClient logClient = new SVNLogClient(authManager, null);
			logClient.doList(svnURL, SVNRevision.create(revision),
				SVNRevision.create(revision), false, true,
				new ISVNDirEntryHandler() {
					public void handleDirEntry(SVNDirEntry dirEntry){
						if (dirEntry.getKind() == SVNNodeKind.FILE){
							//trunk & .javaのみ
							if(dirEntry.getURL().toString().startsWith(App.repository_location + "/trunk") && dirEntry.getURL().toString().endsWith(".java"))
								filePaths.add(dirEntry.getURL().toString());
						}
					}
				}
			);
			return filePaths;
		} catch (SVNException e) {
			e.printStackTrace();
			return null;
		}
	}
}
