package main;

import java.io.File;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

import register.RepositoryRegister;
import binding.BindingDetector;
/**
 * @author y-yusuke
 *
 */
public class App {

	public final static String repository_location = "file:///F:/repository-objectweb";
	public final static String database_location = "F:/objectwebtmp.db";
	public final static String tmp_location = "F:/tmp";
	public final static int endRevision = 1507;

	public static void main(String[] args) throws SVNException {
		long start = System.currentTimeMillis();
		RepositoryRegister registerRepository = new RepositoryRegister();
		SVNRepository repository = registerRepository.execute();
		File tmpdir = new File(tmp_location);
		tmpdir.mkdir();
		//CandidateSqueezer candidateSqueezer = new CandidateSqueezer(repository);
		//candidateSqueezer.execute();
		BindingDetector a = new BindingDetector();
		a.checkOutFiles(repository, 9);
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
