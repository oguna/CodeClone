package binding;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.App;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
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
		List<Binding> beforeBinding = getBindings(before_revision);
		List<Binding> afterBinding = getBindings(after_revision);

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
							if(dirEntry.getURL().toString().endsWith(".java"))
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
