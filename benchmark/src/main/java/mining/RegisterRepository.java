package mining;

import main.App;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;

public class RegisterRepository {
	public RegisterRepository() {
	}

	public SVNRepository execute() throws SVNException {
		FSRepositoryFactory.setup();
		SVNURL svn_url = SVNURL.parseURIEncoded(App.repository_location);
		return FSRepositoryFactory.create(svn_url);
	}
}
