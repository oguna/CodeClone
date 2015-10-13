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
import analyzer.Counter;
import analyzer.Viewer;

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

		Counter count = new Counter();
		count.execute(cParser);

		Viewer view = new Viewer();
		view.newClone(cParser);
	}
}
