package checker;

import java.io.File;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

import register.RepositoryRegister;

public class CheckerMain {

	public final static String repository_name = "ant";
	public final static String datasets_location = "F:/Datasets++/";
	public final static String output_location = "F:/Oracle++/";

	public static void main(String[] args) throws SVNException {
		RepositoryRegister registerRepository = new RepositoryRegister();
		SVNRepository repository = registerRepository.execute();
		File datasetsDir = new File(datasets_location);
		if(!datasetsDir.exists()) datasetsDir.mkdir();
		File outputDir = new File(output_location + repository_name);
		if(!outputDir.exists()) outputDir.mkdir();
		Output output = new Output(repository);
		output.execute();
	}
}
