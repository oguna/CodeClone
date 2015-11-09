package main;

import mining.RegisterRepository;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

import database.Result;

public class App {

	public static void main(String[] args) throws SVNException {
		RegisterRepository registerRepository = new RegisterRepository();
		SVNRepository repository = registerRepository.execute("file:///F:/repository-objectweb");
		Result result = new Result(repository,5);
		result.execute();
	}
}
