package main;

import java.io.File;

import mining.RegisterRepository;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
/**
 * @author y-yusuke
 *
 */
public class App {

	public final static String repository_location = "file:///F:/repository-objectweb";
	public final static String database_location = "F:/objectweb.db";
	public final static int endRevision = 1507;

	public static void main(String[] args) throws SVNException {
		long start = System.currentTimeMillis();
		RegisterRepository registerRepository = new RegisterRepository();
		SVNRepository repository = registerRepository.execute();
		File tmpdir = new File("F:/tmp");
		tmpdir.mkdir();
		CandidateSqueezer candidateSqueezer = new CandidateSqueezer(repository);
		candidateSqueezer.execute();
		//delete(tmpdir);
		long end = System.currentTimeMillis();
		System.out.println((end - start)/1000  + "s");
	}

	private void delete(File tmpdir){
		File[] files = tmpdir.listFiles();
		for(File file : files) file.delete();
        tmpdir.delete();
	}
}
