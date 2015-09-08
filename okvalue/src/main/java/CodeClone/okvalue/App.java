package CodeClone.okvalue;

import java.util.LinkedList;

import model.Clone;
import controller.CompareController;
import controller.DecompileController;

public class App {
	public static void main(String[] args) {
		LinkedList<Clone> Clones = new LinkedList<Clone>();

		DecompileController dControl = new DecompileController();
		dControl.split();
		Clones = dControl.conversion();

		CompareController cControl = new CompareController();
		cControl.split();
	}
}
