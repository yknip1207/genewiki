package tests;

import java.util.LinkedHashMap;
import java.util.List;

import org.gnf.pbb.controller.*;

public class ControllerTestHarness {

	public static void main(String[] args) {
		PBBController controller = new PBBController();
		
		final boolean OVERWRITE = true;
		final boolean DRY_RUN = false;
		
		for (String geneId : args) {
			LinkedHashMap<String, List<String>> modelDataMap = controller.importObjectData(geneId).getGeneDataAsMap();
			LinkedHashMap<String, List<String>> viewDataMap = controller.importDisplayData(geneId);
			LinkedHashMap<String, List<String>> updatedViewDataMap = controller.updateDisplayTerms(modelDataMap, viewDataMap, OVERWRITE);
			
			String updatedViewData = controller.outputDisplay(updatedViewDataMap);
			
			try {
				controller.wiki.update(updatedViewData, geneId, "Edited by PBB2!", DRY_RUN);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
