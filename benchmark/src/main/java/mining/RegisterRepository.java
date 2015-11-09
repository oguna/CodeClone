package mining;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;

public class RegisterRepository {
	public RegisterRepository() {
	}

	public SVNRepository execute(String url) throws SVNException {
		FSRepositoryFactory.setup();
		SVNURL svn_url = SVNURL.parseURIEncoded(url);
		return FSRepositoryFactory.create(svn_url);
	}
}
