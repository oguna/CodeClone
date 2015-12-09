package register;

import main.App;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;

public class RepositoryRegister {
	public RepositoryRegister() {
	}

	public SVNRepository execute() throws SVNException {
		FSRepositoryFactory.setup();
		SVNURL svnURL = SVNURL.parseURIEncoded(App.repository_location);
		return FSRepositoryFactory.create(svnURL);
	}
}
