package controller;

/**
 * @author y-yusuke
 *
 */

import java.util.LinkedList;

import model.Clone;
import parser.CompareParser;
import parser.DecompileParser;
import parser.OriginParser;

public class App {
	public static void main(String[] args) {
		LinkedList<Clone> originClones = new LinkedList<Clone>();
		LinkedList<Clone> decompileClones = new LinkedList<Clone>();
		LinkedList<Clone> conversionClones = new LinkedList<Clone>();

		OriginParser oParser = new OriginParser();
		DecompileParser dParser= new DecompileParser();

		originClones = oParser.split();
		decompileClones = dParser.split();
		conversionClones = dParser.conversion(decompileClones);

		CompareParser cParser = new CompareParser();
		cParser.execute(originClones,decompileClones,conversionClones);
		cParser.divide(originClones,decompileClones,conversionClones);

		LinkedList<Clone> ocommon = new LinkedList<Clone>();
		LinkedList<Clone> dcommon = new LinkedList<Clone>();
		LinkedList<Clone> ccommon = new LinkedList<Clone>();
		LinkedList<Clone> dnewClones = new LinkedList<Clone>();
		LinkedList<Clone> cnewClones = new LinkedList<Clone>();
		LinkedList<Clone> doutClones = new LinkedList<Clone>();
		LinkedList<Clone> coutClones = new LinkedList<Clone>();
		LinkedList<Clone> rejectClones = new LinkedList<Clone>();
		ocommon = cParser.getOcommon();
		dcommon = cParser.getDcommon();
		ccommon = cParser.getCcommon();
		dnewClones = cParser.getDnewClones();
		cnewClones = cParser.getCnewClones();
		doutClones = cParser.getDoutClones();
		coutClones = cParser.getCoutClones();
		rejectClones = cParser.getRejectClones();

		int tmp = dcommon.get(0).getId();
		int count = 1;
		for(int i=1;i<dcommon.size();i++){
			if(tmp != dcommon.get(i).getId()){
				count ++;
				tmp = dcommon.get(i).getId();
			}
		}
		System.out.println(ocommon.size());
		System.out.println(dcommon.size());
		System.out.println(ccommon.size());
		System.out.println(count);
		tmp = dnewClones.get(0).getId();
		count = 1;
		for(int i=1;i<dnewClones.size();i++){
			if(tmp != dnewClones.get(i).getId()){
				count ++;
				tmp = dnewClones.get(i).getId();
			}
		}
		System.out.println(dnewClones.size());
		System.out.println(cnewClones.size());
		System.out.println(count);
		System.out.println(doutClones.size());
		System.out.println(coutClones.size());
		tmp = rejectClones.get(0).getId();
		count = 1;
		for(int i=1;i<rejectClones.size();i++){
			if(tmp != rejectClones.get(i).getId()){
				count ++;
				tmp = rejectClones.get(i).getId();
			}
		}
		System.out.println(rejectClones.size());
		System.out.println(count);
	}
}
