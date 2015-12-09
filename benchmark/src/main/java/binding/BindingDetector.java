package binding;

import java.util.ArrayList;
import java.util.List;

import main.App;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

public class BindingDetector {
	public BindingDetector() {
	}

	public List<String> getFilePath(long revision) {
		try {
			SVNURL svnURL = SVNURL.parseURIEncoded(App.repository_location);
			ISVNAuthenticationManager authManager = null;
			List<String> files = new ArrayList<String>();
			SVNLogClient logClient = new SVNLogClient(authManager, null);
			logClient.doList(svnURL, SVNRevision.create(revision),
				SVNRevision.create(revision), false, true,
				new ISVNDirEntryHandler() {
					public void handleDirEntry(SVNDirEntry dirEntry){
						if (dirEntry.getKind() == SVNNodeKind.FILE){
							if(dirEntry.getURL().toString().endsWith(".java"))
								files.add(dirEntry.getURL().toString());
						}
					}
				}
			);
			return files;
		} catch (SVNException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void getBinding(List<String> files, SVNRepository repository, long revision) throws SVNException{
		List<CompilationUnit> compilationUnits = new ArrayList<CompilationUnit>();
		/*for(String filePath : files){
			SVNProperties fileProperties = new SVNProperties();
			OutputStream sourceCode = new ByteArrayOutputStream ();
			repository.getFile(filePath, revision, fileProperties, sourceCode);
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(sourceCode.toString().toCharArray());
			compilationUnits.add((CompilationUnit) parser.createAST(new NullProgressMonitor()));
		}
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		final Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		parser.setSource(compilationUnits);
		String[] keys = new String[] {};

		parser.createASTs(sources, null, keys, new Requestor(),
				new NullProgressMonitor());
*/
	}
}
