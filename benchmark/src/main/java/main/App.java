package main;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

import register.RepositoryRegister;
import register.RevisionRegister;
/**
 * @author y-yusuke
 *
 */
public class App {

	public final static String database_location = "F:/argouml.db";
	public final static String tmp_location = "F:/tmp/argouml";
	public static String repository_location;
	public static String repository_additional_location;
	public static int startRevision;
	public static int endRevision;
	public static final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	/* バージョン管理，言語の制限
	 * SVN
	 * Java
	 * trunkのみ
	 */
	/*
	 * クローンの制限
	 * メソッドのみ
	 * 6行以上
	 * 50トークン以上
	 */

	public static void main(String[] args) throws SVNException {
		long start = System.currentTimeMillis();
		RepositoryRegister registerRepository = new RepositoryRegister();
		SVNRepository repository = registerRepository.execute();
		RevisionRegister registerRevision = new RevisionRegister();
		registerRevision.execute();
		File tmpdir = new File(tmp_location);
		if(!tmpdir.exists()) tmpdir.mkdir();
		CandidateSqueezer candidateSqueezer = new CandidateSqueezer(repository);
		candidateSqueezer.execute();
		//delete(tmpdir);
		long end = System.currentTimeMillis();
		System.out.println((end - start)/1000  + "s");
	}

/*	private void delete(File tmp){
		if (!tmp.exists()) return;
		if (tmp.isFile()) tmp.delete();
		else if (tmp.isDirectory()) {
			File[] files = tmp.listFiles();
			for(File file : files) delete(file);
	        tmp.delete();
		}
	}*/
}
