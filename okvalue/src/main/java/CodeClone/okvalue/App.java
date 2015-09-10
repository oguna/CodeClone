package CodeClone.okvalue;

/**
 * @author y-yusuke
 *
 */

import java.util.LinkedList;

import model.Clone;
import controller.CompareController;
import controller.DecompileController;
import controller.OriginController;

public class App {
	public static void main(String[] args) {
		LinkedList<Clone> originClones = new LinkedList<Clone>();
		LinkedList<Clone> decompileClones = new LinkedList<Clone>();
		LinkedList<Clone> conversionClones = new LinkedList<Clone>();

		OriginController oControl = new OriginController();
		DecompileController dControl = new DecompileController();

		originClones = oControl.split();
		decompileClones = dControl.split();
		conversionClones = dControl.conversion(decompileClones);

		CompareController cControl = new CompareController();
		cControl.execute(originClones,decompileClones,conversionClones);
	}
}
