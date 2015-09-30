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
		double clines;
		double rlines;
		double overlaplines;
		double ccontained;
		double rcontained;
		double max;
		boolean flag = false;
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
						clines = oclone.getEnd() - oclone.getStart() +1;
						rlines = cclone.getEnd() - cclone.getStart() +1;
						overlaplines = 0;
						for(int p = oclone.getStart() ; p <= oclone.getEnd() ; p++){
							for(int q = cclone.getStart() ; q <= cclone.getEnd() ; q++){
								if(p == q) {
									overlaplines++;
									break;
								}
							}
						}
						ccontained = overlaplines / clines;
						rcontained = overlaplines / rlines;
						if(rcontained < ccontained) max = ccontained;
						else max = rcontained;

						//共通クローンを出力
						if(max >= 0.7){
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
							flag = true;
							break;
						}
					}
				}

				//非共通クローンを出力
				if(flag == false){
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
				}flag = false;
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
