package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import model.Clone;

public class OriginParser {
	private int id;
	private String location;
	private String filename;
	private int start;
	private int end;
	Clone oclone = new Clone();
	LinkedList<Clone> originClones = new LinkedList<Clone>();

	public OriginParser() {
	}

	public LinkedList<Clone> split() {
		id = 1;
		String linestr;
		String[] cloneInfo;
		String[] javaFile;

		try {
			File readfile = new File(
					"C:/cygwin64/home/y-yusuke/simian/bin/Result/origin/result.txt");
			BufferedReader br = new BufferedReader(new FileReader(readfile));

			File writefile = new File(
					"C:/cygwin64/home/y-yusuke/simian/bin/Result/origin/result(parse).txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(writefile)));

			for (int i = 0; i < 4; i++) br.readLine();

			linestr = br.readLine();
			while (linestr.startsWith("Found")) {
				linestr = br.readLine();
				if (linestr.startsWith("Processed")) break;
				while (true) {
					//解析
					cloneInfo = linestr.split(":", 0);
					location = (cloneInfo[0] + ":" + cloneInfo[1]).trim().replace("\\", "/");
					javaFile = location.split("/", 0);
					filename = javaFile[javaFile.length - 1];
					start = Integer.parseInt(cloneInfo[2]);
					end = Integer.parseInt(cloneInfo[4]);

					//リスト格納
					oclone.setId(id);
					oclone.setLocation(location);
					oclone.setFilename(filename);
					oclone.setStart(start);
					oclone.setEnd(end);
					originClones.add(oclone);
					oclone = new Clone();

					//ファイル書き込み
					pw.println(id);
					pw.println("file:///" + location);
					pw.println(filename);
					pw.println(start);
					pw.println(end);

					linestr = br.readLine();
					if (linestr.startsWith("Found")) break;
				}
				id++;
				pw.println("********************************************************************");
				pw.println("********************************************************************");
			}
			br.close();
			pw.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
		return originClones;
	}
}
