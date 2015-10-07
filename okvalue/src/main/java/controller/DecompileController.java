package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;

import model.Clone;

/**
 * @author y-yusuke
 *
 */

public class DecompileController {

	private int id;
	private String location;
	private String filename;
	private int start;
	private int end;

	public DecompileController() {

	}

	public LinkedList<Clone> split() {
		id = 1;
		String linestr;
		String[] cloneInfo;
		String[] javaFile;
		Clone dclone = new Clone();
		LinkedList<Clone> decompileClones = new LinkedList<Clone>();

		try {
			File readfile = new File(
					"C:/cygwin64/home/y-yusuke/simian/bin/Result/decompile/result(decompile).txt");
			BufferedReader br = new BufferedReader(new FileReader(readfile));

			File writefile = new File(
					"C:/cygwin64/home/y-yusuke/simian/bin/Result/decompile/result(decompile_parse).txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(writefile)));

			for (int i = 0; i < 4; i++)
				br.readLine();

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
					dclone.setId(id);
					dclone.setLocation(location);
					dclone.setFilename(filename);
					dclone.setStart(start);
					dclone.setEnd(end);
					decompileClones.add(dclone);
					dclone = new Clone();

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
		return decompileClones;
	}

	public LinkedList<Clone> conversion(LinkedList<Clone> decompileClones) {
		id = 1;
		int lineNumber;
		String linestr;
		String[] comment;
		Clone dclone = new Clone();
		Clone cclone = new Clone();
		LinkedList<Clone> conversionClones = new LinkedList<Clone>();
		LinkedList<Integer> range = new LinkedList<Integer>();

		try {
			File writefile = new File(
					"C:/cygwin64/home/y-yusuke/simian/bin/Result/decompile/result(decompile_conversion).txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(writefile)));

			for (int i = 0; i < decompileClones.size(); i++) {
				dclone = decompileClones.get(i);
				id = dclone.getId();
				location = dclone.getLocation();
				filename = dclone.getFilename();
				start = dclone.getStart();
				end = dclone.getEnd();

				try {
					File readfile = new File(location);
					BufferedReader br = new BufferedReader(new FileReader(readfile));

					location = location.replace("src2", "src/main");

					//コードクローン範囲の対応付け
					for (int j = 1; j < start; j++)
						linestr = br.readLine();
					for (int j = 0; j < end - start + 1; j++) {
						linestr = br.readLine();
						if (linestr.startsWith("/*")) {
							comment = linestr.split("\\*", 0);
							lineNumber = Integer.parseInt(comment[1].trim());
							range.add(lineNumber);
						}
					}

					if (range.isEmpty()) {
						start = 0;
						end = 0;
					} else {
						start = Collections.min(range);
						end = Collections.max(range);
					}

					//リスト格納
					cclone.setId(id);
					cclone.setLocation(location);
					cclone.setFilename(filename);
					cclone.setStart(start);
					cclone.setEnd(end);
					conversionClones.add(cclone);
					cclone = new Clone();

					//ファイル書き込み
					pw.println(id);
					pw.println("file:///" + location);
					pw.println(filename);
					pw.println(start);
					pw.println(end);
					pw.println("********************************************************************");
					pw.println("********************************************************************");

					range.clear();
					br.close();
				} catch (FileNotFoundException e) {
					System.out.println(e);
				} catch (IOException e) {
					System.out.println(e);
				}
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
		return conversionClones;
	}
}
