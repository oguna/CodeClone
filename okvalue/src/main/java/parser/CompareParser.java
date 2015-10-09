package parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import model.Clone;
import model.Compare;

/**
 * @author y-yusuke
 *
 */

public class CompareParser {

	LinkedList<Clone> ocommon = new LinkedList<Clone>();
	LinkedList<Clone> dcommon = new LinkedList<Clone>();
	LinkedList<Clone> ccommon = new LinkedList<Clone>();
	LinkedList<Clone> onotcommon = new LinkedList<Clone>();
	LinkedList<Clone> dnotcommon = new LinkedList<Clone>();
	LinkedList<Clone> cnotcommon = new LinkedList<Clone>();
	LinkedList<Clone> dnewClones = new LinkedList<Clone>();
	LinkedList<Clone> cnewClones = new LinkedList<Clone>();
	LinkedList<Clone> doutClones = new LinkedList<Clone>();
	LinkedList<Clone> coutClones = new LinkedList<Clone>();
	LinkedList<Clone> rejectClones = new LinkedList<Clone>();

	public CompareParser() {
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
		double min;
		int  tmp;
		LinkedList<Compare> compareList = new LinkedList<Compare>();
		Compare compare = new Compare();

		try {
			File writefile_common = new File(
					"C:/cygwin64/home/y-yusuke/simian/bin/Result/result(common).txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(writefile_common)));
			File writefile_notcommon = new File(
					"C:/cygwin64/home/y-yusuke/simian/bin/Result/result(notcommon).txt");
			PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(writefile_notcommon)));

			for (int i = 0; i < conversionClones.size(); i++) {
				dclone = decompileClones.get(i);
				cclone = conversionClones.get(i);

				for (int j = 0; j < originClones.size(); j++) {
					oclone = originClones.get(j);

					if (cclone.getLocation().equals(oclone.getLocation())) {
						//共通クローンの判定

						//重複行数のカウント
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

						//ok値の算出
						ccontained = overlaplines / clines;
						rcontained = overlaplines / rlines;
						if(rcontained < ccontained){
							max = ccontained;
							min = rcontained;
						}
						else{
							max = rcontained;
							min = ccontained;
						}

						//共通クローン候補をリストに格納
						if(max >= 0.7){
							//リスト格納
							compare.setIndex(j);
							compare.setMin(min);
							compare.setMax(max);
							compareList.add(compare);
							compare = new Compare();
						}
					}
				}

				if(compareList.size() > 0){
					//共通クローンを出力
					min = compareList.get(0).getMin();
					tmp = compareList.get(0).getIndex();
					for(int p = 1 ; p < compareList.size() ; p++){
						if(min < compareList.get(p).getMin())
							tmp = compareList.get(p).getIndex();
					}
					oclone = originClones.get(tmp);

					pw.println("-------------origin-------------");
					pw.println(oclone.getId());
					pw.println("file:///" + oclone.getLocation());
					pw.println(oclone.getFilename());
					pw.println(oclone.getStart());
					pw.println(oclone.getEnd());
					pw.println("-----------decompile-----------");
					pw.println(dclone.getId());
					pw.println("file:///" + dclone.getLocation());
					pw.println(dclone.getFilename());
					pw.println(dclone.getStart());
					pw.println(dclone.getEnd());
					pw.println("-----------conversion-----------");
					pw.println(cclone.getId());
					pw.println("file:///" + cclone.getLocation());
					pw.println(cclone.getFilename());
					pw.println(cclone.getStart());
					pw.println(cclone.getEnd());
					pw.println("********************************************************************");
					pw.println("********************************************************************");
					ocommon.add(oclone);
					dcommon.add(dclone);
					ccommon.add(cclone);
				}else{
					//非共通クローンを出力
					pw2.println("-----------decompile-----------");
					pw2.println(dclone.getId());
					pw2.println("file:///" + dclone.getLocation());
					pw2.println(dclone.getFilename());
					pw2.println(dclone.getStart());
					pw2.println(dclone.getEnd());
					pw2.println("-----------conversion-----------");
					pw2.println(cclone.getId());
					pw2.println("file:///" + cclone.getLocation());
					pw2.println(cclone.getFilename());
					pw2.println(cclone.getStart());
					pw2.println(cclone.getEnd());
					pw2.println("********************************************************************");
					pw2.println("********************************************************************");
					dnotcommon.add(dclone);
					cnotcommon.add(cclone);
				}
				compareList.clear();
			}
			pw.close();
			pw2.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void divide(LinkedList<Clone> originClones,LinkedList<Clone> decompileClones,LinkedList<Clone> conversionClones) {
		Clone oclone = new Clone();
		Clone dclone = new Clone();
		Clone cclone = new Clone();
		boolean flag = false;

		try {
			File writefile_newclone = new File(
					"C:/cygwin64/home/y-yusuke/simian/bin/Result/result(newclone).txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(writefile_newclone)));
			File writefile_outcast = new File(
					"C:/cygwin64/home/y-yusuke/simian/bin/Result/result(outcast).txt");
			PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(writefile_outcast)));
			File writefile_reject = new File(
					"C:/cygwin64/home/y-yusuke/simian/bin/Result/result(reject).txt");
			PrintWriter pw3 = new PrintWriter(new BufferedWriter(new FileWriter(writefile_reject)));

			//newクローンの仕分け
			for(int i = 0 ; i < dnotcommon.size() ; i++){
				for(int j = 0 ; j < dcommon.size() ; j++){
					if(dnotcommon.get(i).getId() == dcommon.get(j).getId()){
						flag = true;
						break;
					}
				}
				dclone = dnotcommon.get(i);
				cclone = cnotcommon.get(i);
				if(flag == false){
					pw.println("-----------decompile-----------");
					pw.println(dclone.getId());
					pw.println("file:///" + dclone.getLocation());
					pw.println(dclone.getFilename());
					pw.println(dclone.getStart());
					pw.println(dclone.getEnd());
					pw.println("-----------conversion-----------");
					pw.println(cclone.getId());
					pw.println("file:///" + cclone.getLocation());
					pw.println(cclone.getFilename());
					pw.println(cclone.getStart());
					pw.println(cclone.getEnd());
					pw.println("********************************************************************");
					pw.println("********************************************************************");
					dnewClones.add(dclone);
					cnewClones.add(cclone);
				}else{
					pw2.println("-----------decompile-----------");
					pw2.println(dclone.getId());
					pw2.println("file:///" + dclone.getLocation());
					pw2.println(dclone.getFilename());
					pw2.println(dclone.getStart());
					pw2.println(dclone.getEnd());
					pw2.println("-----------conversion-----------");
					pw2.println(cclone.getId());
					pw2.println("file:///" + cclone.getLocation());
					pw2.println(cclone.getFilename());
					pw2.println(cclone.getStart());
					pw2.println(cclone.getEnd());
					pw2.println("********************************************************************");
					pw2.println("********************************************************************");
					doutClones.add(dclone);
					coutClones.add(cclone);
					flag = false;
				}
			}

			//棄却クローンの仕分け
			for(int i = 0 ; i < originClones.size() ; i++){
				for(int j = 0 ; j < ocommon.size() ; j++){
					if(originClones.get(i).getId() == ocommon.get(j).getId() &&
						originClones.get(i).getFilename().equals(ocommon.get(j).getFilename()) &&
						originClones.get(i).getLocation().equals(ocommon.get(j).getLocation()) &&
						originClones.get(i).getStart() == ocommon.get(j).getStart() &&
						originClones.get(i).getEnd() == ocommon.get(j).getEnd()){
						flag = true;
						break;
					}
				}
				if(flag == false){
					oclone = originClones.get(i);
					pw3.println("-------------origin-------------");
					pw3.println(oclone.getId());
					pw3.println("file:///" + oclone.getLocation());
					pw3.println(oclone.getFilename());
					pw3.println(oclone.getStart());
					pw3.println(oclone.getEnd());
					pw3.println("********************************************************************");
					pw3.println("********************************************************************");
					rejectClones.add(oclone);
				}
				flag = false;
			}
			pw.close();
			pw2.close();
			pw3.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public LinkedList<Clone> getOcommon() {
		return ocommon;
	}

	public LinkedList<Clone> getDcommon() {
		return dcommon;
	}

	public LinkedList<Clone> getCcommon() {
		return ccommon;
	}

	public LinkedList<Clone> getDnewClones() {
		return dnewClones;
	}

	public LinkedList<Clone> getCnewClones() {
		return cnewClones;
	}

	public LinkedList<Clone> getDoutClones() {
		return doutClones;
	}

	public LinkedList<Clone> getCoutClones() {
		return coutClones;
	}

	public LinkedList<Clone> getRejectClones() {
		return rejectClones;
	}

}
