package controller;

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

public class CompareController {
	Clone clone = new Clone();
	LinkedList<Clone> originClones = new LinkedList<Clone>();

	public CompareController() {
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
					"C:/cygwin/home/y-yusuke/simian/bin/Result/result.txt");
			BufferedReader br = new BufferedReader(new FileReader(readfile));

			File writefile = new File(
					"C:/cygwin/home/y-yusuke/simian/bin/Result/result(parse).txt");
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
					originClones.add(clone);
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

	public void execute(LinkedList<Clone> decompileClones) {
		Clone dclone = new Clone();
		Clone oclone = new Clone();
		int flag = 0;
		try {
			File writefile_common = new File(
					"C:/cygwin/home/y-yusuke/simian/bin/Result/result(common).txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
					writefile_common)));
			File writefile_notcommon = new File(
					"C:/cygwin/home/y-yusuke/simian/bin/Result/result(notcommon).txt");
			PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(
					writefile_notcommon)));

			for (int i = 0; i < decompileClones.size(); i++) {
				dclone = decompileClones.get(i);
				for (int j = 0; j < originClones.size(); j++) {
					oclone = originClones.get(j);
					if (dclone.getLocation().equals(oclone.getLocation())) {
						if(dclone.getStart() >= oclone.getStart() && dclone.getStart() <= oclone.getEnd()){
							pw.println(dclone.getId());
							pw.println(dclone.getLocation());
							pw.println(dclone.getFilename());
							pw.println(dclone.getStart());
							pw.println(dclone.getEnd());
							pw.println(" ");
							pw.println(oclone.getId());
							pw.println(oclone.getLocation());
							pw.println(oclone.getFilename());
							pw.println(oclone.getStart());
							pw.println(oclone.getEnd());
							pw.println("----------------------------------------------");
							flag = 1;
							break;
						}else if(dclone.getEnd() >= oclone.getStart() && dclone.getEnd() <= oclone.getEnd()){
							pw.println(dclone.getId());
							pw.println(dclone.getLocation());
							pw.println(dclone.getFilename());
							pw.println(dclone.getStart());
							pw.println(dclone.getEnd());
							pw.println(" ");
							pw.println(oclone.getId());
							pw.println(oclone.getLocation());
							pw.println(oclone.getFilename());
							pw.println(oclone.getStart());
							pw.println(oclone.getEnd());
							pw.println("----------------------------------------------");
							break;
						}else if(dclone.getStart() <= oclone.getStart() && dclone.getEnd() >= oclone.getEnd()){
							pw.println(dclone.getId());
							pw.println(dclone.getLocation());
							pw.println(dclone.getFilename());
							pw.println(dclone.getStart());
							pw.println(dclone.getEnd());
							pw.println(" ");
							pw.println(oclone.getId());
							pw.println(oclone.getLocation());
							pw.println(oclone.getFilename());
							pw.println(oclone.getStart());
							pw.println(oclone.getEnd());
							pw.println("----------------------------------------------");
							flag = 1;
							break;
						}else if(dclone.getStart() >= oclone.getStart() && dclone.getEnd() <= oclone.getEnd()){
							pw.println(dclone.getId());
							pw.println(dclone.getLocation());
							pw.println(dclone.getFilename());
							pw.println(dclone.getStart());
							pw.println(dclone.getEnd());
							pw.println(" ");
							pw.println(oclone.getId());
							pw.println(oclone.getLocation());
							pw.println(oclone.getFilename());
							pw.println(oclone.getStart());
							pw.println(oclone.getEnd());
							pw.println("----------------------------------------------");
							flag = 1;
							break;
						}
					}
				}
				if(flag == 0){
					pw2.println(dclone.getId());
					pw2.println(dclone.getLocation());
					pw2.println(dclone.getFilename());
					pw2.println(dclone.getStart());
					pw2.println(dclone.getEnd());
					pw2.println(" ");
				}else flag = 0;
			}
			pw.close();
			pw2.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
