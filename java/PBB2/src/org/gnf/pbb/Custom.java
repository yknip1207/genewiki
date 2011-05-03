package org.gnf.pbb;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains implementation-specific code, and is called at certain points in the infobox parser. These methods
 * can and should be customized to provide any specific edits that may be required in addition to the general infobox
 * parser methods.
 * @author eclarke
 *
 */
public class Custom {

	public static List<String> valueParse(List<String> _list) {
		List<String> list = new ArrayList<String>();
		for (String value : _list) {
			value = value.replaceAll("PDB2\\|", "");
			list.add(value);
		}
		return list;
	}
	
}
