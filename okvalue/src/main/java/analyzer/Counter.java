package analyzer;

import java.util.LinkedList;

import model.Clone;
import parser.CompareParser;

public class Counter {
	public void execute(CompareParser cParser) {
		LinkedList<Clone> ocommon = new LinkedList<Clone>();
		LinkedList<Clone> dcommon = new LinkedList<Clone>();
		LinkedList<Clone> dnewClones = new LinkedList<Clone>();
		LinkedList<Clone> rejectClones = new LinkedList<Clone>();
		ocommon = cParser.getOcommon();
		dcommon = cParser.getDcommon();
		dnewClones = cParser.getDnewClones();
		rejectClones = cParser.getRejectClones();

		int tmp = ocommon.get(0).getId();
		int count = 1;
		for(int i=1;i<ocommon.size();i++){
			if(tmp != ocommon.get(i).getId()){
				count ++;
				tmp = ocommon.get(i).getId();
			}
		}
		System.out.println("origincommonclone : " + count);

		tmp = dcommon.get(0).getId();
		count = 1;
		for(int i=1;i<dcommon.size();i++){
			if(tmp != dcommon.get(i).getId()){
				count ++;
				tmp = dcommon.get(i).getId();
			}
		}
		System.out.println("commonclone : " + count);

		tmp = dnewClones.get(0).getId();
		count = 1;
		for(int i=1;i<dnewClones.size();i++){
			if(tmp != dnewClones.get(i).getId()){
				count ++;
				tmp = dnewClones.get(i).getId();
			}
		}
		System.out.println("newclone : " + count);

		tmp = rejectClones.get(0).getId();
		count = 1;
		for(int i=1;i<rejectClones.size();i++){
			if(tmp != rejectClones.get(i).getId()){
				count ++;
				tmp = rejectClones.get(i).getId();
			}
		}
		System.out.println("rejectclone : " +count);
	}
}
