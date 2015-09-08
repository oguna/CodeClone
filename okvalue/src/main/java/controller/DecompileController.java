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

public class DecompileController {

	Clone clone = new Clone();
	LinkedList<Clone> decompileClones = new LinkedList<Clone>();

	public DecompileController() {

	}

	public void split() {

		int id = 1;
		String location;
		String filename;
		int start;
		int end;
		String linestr;
		String[] clonestr;
		String[] javafilelocation;

		try {
			File readfile = new File(
					"C:/cygwin/home/y-yusuke/simian/bin/Result/result(decompile).txt");
			BufferedReader br = new BufferedReader(new FileReader(readfile));

			File writefile = new File(
					"C:/cygwin/home/y-yusuke/simian/bin/Result/result(decompile_parse).txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
					writefile)));

			for (int i = 0; i < 4; i++)
				br.readLine();

			linestr = br.readLine();
			while (linestr.startsWith("Found")) {
				linestr = br.readLine();
				if (linestr.startsWith("Processed"))
					break;
				while (true) {
					clonestr = linestr.split(":", 0);
					location = (clonestr[0] + ":" + clonestr[1]).trim()
							.replace("\\", "/");
					javafilelocation = location.split("/", 0);
					filename = javafilelocation[javafilelocation.length - 1];
					start = Integer.parseInt(clonestr[2]);
					end = Integer.parseInt(clonestr[4]);
					clone.setId(id);
					clone.setLocation(location);
					clone.setFilename(filename);
					clone.setStart(start);
					clone.setEnd(end);
					decompileClones.add(clone);
					clone = new Clone();
					pw.println(id);
					pw.println(location);
					pw.println(filename);
					pw.println(start);
					pw.println(end);
					linestr = br.readLine();
					if (linestr.startsWith("Found"))
						break;
				}
				id++;
			}
			br.close();
			pw.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public LinkedList<Clone> conversion() {
		LinkedList<Clone> conversionClones = new LinkedList<Clone>();
		LinkedList<Integer> range = new LinkedList<Integer>();
		int id = 1;
		String location;
		String filename;
		int start;
		int end;
		int lineNumber;
		String linestr;
		String[] comment;
		clone = new Clone();

		try {
			File writefile = new File(
					"C:/cygwin/home/y-yusuke/simian/bin/Result/result(decompile_conversion).txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
					writefile)));
			while ((clone = decompileClones.poll()) != null) {
				id = clone.getId();
				location = clone.getLocation();
				filename = clone.getFilename();
				start = clone.getStart();
				end = clone.getEnd();

				try {
					File readfile = new File(location);
					BufferedReader br = new BufferedReader(new FileReader(
							readfile));

					for (int i = 1; i < start; i++)
						linestr = br.readLine();
					for (int i = 0; i < end - start + 1; i++) {
						linestr = br.readLine();
						if (linestr.startsWith("/*")) {
							comment = linestr.split("\\*", 0);
							lineNumber = Integer.parseInt(comment[1].trim());
							range.add(lineNumber);
						}
					}
					location = location.replace("src2", "src/main");
					if (range.isEmpty()) {
						start = 0;
						end = 0;
					} else {
						start = Collections.min(range);
						end = Collections.max(range);
					}
					clone.setId(id);
					clone.setLocation(location);
					clone.setFilename(filename);
					clone.setStart(start);
					clone.setEnd(end);
					conversionClones.add(clone);
					pw.println(id);
					pw.println(location);
					pw.println(filename);
					pw.println(start);
					pw.println(end);
					range.clear();
					clone = new Clone();
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
