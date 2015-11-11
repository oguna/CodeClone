package main;

import mining.RegisterRepository;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

import database.Result;

public class App {

	public final static int endRevision = 1507;
	public final static String repository_location = "file:///F:/repository-objectweb";

	public static void main(String[] args) throws SVNException {
		RegisterRepository registerRepository = new RegisterRepository();
		SVNRepository repository = registerRepository.execute(repository_location);
		Result result = new Result(repository,endRevision);
		result.execute();
	}
}
