package controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import model.Clone;

/**
 * @author y-yusuke
 *
 */

public class CompareController {

	public CompareController() {
	}

	public void execute(LinkedList<Clone> originClones,LinkedList<Clone> decompileClones,LinkedList<Clone> conversionClones) {
		Clone oclone = new Clone();
		Clone dclone = new Clone();
		Clone cclone = new Clone();
		int flag = 0;
		try {
			File writefile_common = new File(
					"C:/cygwin/home/y-yusuke/simian/bin/Result/result(common).txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(writefile_common)));
			File writefile_notcommon = new File(
					"C:/cygwin/home/y-yusuke/simian/bin/Result/result(notcommon).txt");
			PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(writefile_notcommon)));

			for (int i = 0; i < conversionClones.size(); i++) {
				dclone = decompileClones.get(i);
				cclone = conversionClones.get(i);

				for (int j = 0; j < originClones.size(); j++) {
					oclone = originClones.get(j);

					if (cclone.getLocation().equals(oclone.getLocation())) {
						//共通クローンの判定
						if(cclone.getStart() >= oclone.getStart() && cclone.getStart() <= oclone.getEnd()) flag = 1;
						else if(cclone.getEnd() >= oclone.getStart() && cclone.getEnd() <= oclone.getEnd()) flag =1;
						else if(cclone.getStart() <= oclone.getStart() && cclone.getEnd() >= oclone.getEnd()) flag =1;
						else if(cclone.getStart() >= oclone.getStart() && cclone.getEnd() <= oclone.getEnd()) flag = 1;

						//共通クローンを出力
						if(flag == 1){
							pw.println("-------------origin-------------");
							pw.println(oclone.getId());
							pw.println(oclone.getLocation());
							pw.println(oclone.getFilename());
							pw.println(oclone.getStart());
							pw.println(oclone.getEnd());
							pw.println("-----------decompile-----------");
							pw.println(dclone.getId());
							pw.println(dclone.getLocation());
							pw.println(dclone.getFilename());
							pw.println(dclone.getStart());
							pw.println(dclone.getEnd());
							pw.println("-----------conversion-----------");
							pw.println(cclone.getId());
							pw.println(cclone.getLocation());
							pw.println(cclone.getFilename());
							pw.println(cclone.getStart());
							pw.println(cclone.getEnd());
							pw.println("********************************************************************");
							pw.println("********************************************************************");
							break;
						}
					}
				}
				//非共通クローンを出力
				if(flag == 0){
					pw2.println("-----------decompile-----------");
					pw2.println(dclone.getId());
					pw2.println(dclone.getLocation());
					pw2.println(dclone.getFilename());
					pw2.println(dclone.getStart());
					pw2.println(dclone.getEnd());
					pw2.println("-----------conversion-----------");
					pw2.println(cclone.getId());
					pw2.println(cclone.getLocation());
					pw2.println(cclone.getFilename());
					pw2.println(cclone.getStart());
					pw2.println(cclone.getEnd());
					pw2.println("********************************************************************");
					pw2.println("********************************************************************");
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
