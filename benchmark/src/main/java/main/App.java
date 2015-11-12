package main;

import mining.RegisterRepository;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
/**
 * @author y-yusuke
 *
 */
public class App {

	public final static String repository_location = "file:///F:/repository-objectweb";
	public final static String database_location = "F:\\objectweb.db";
	public final static int endRevision = 1507;

	public static void main(String[] args) throws SVNException {
		RegisterRepository registerRepository = new RegisterRepository();
		SVNRepository repository = registerRepository.execute(repository_location);
		Result result = new Result(repository,database_location,endRevision);
		result.execute();
	}
}
