package tests;

import java.util.LinkedHashMap;
import java.util.List;

import org.gnf.pbb.*;
import org.gnf.pbb.controller.*;

public class ControllerTestHarness {

	public static void main(String[] args) {
		PBBController controller = new PBBController();
		GeneObject gene = controller.importObjectData("1107");
		LinkedHashMap<String, List<String>> infobox = controller.importDisplayData("Template:PBB/410");
		System.out.println(gene.geneOntologies.getOntologiesByCategory("Molecular Function"));
		System.out.println(infobox.get("Name"));
		System.out.println(gene.geneOntologies.getAllOntologies().size());
	}
	
}
