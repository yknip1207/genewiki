package org.gnf.genewiki.ncbi;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.DocSumType;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.ItemType;


public class PubMed {

	public static void main(String[] args) {
		String d = getPubmedDate("17");
		System.out.println(d);
	}


	public static Map<String, String> getPubmedArticleTypes(String idlist){
		if(idlist==null||idlist.length()<4){
			return null;
		}
		Map<String, String> journal = new HashMap<String, String>();
		String info = ""; 

		EUtilsServiceStub service = null;
		try {
			service = new EUtilsServiceStub();
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// call NCBI ESummary utility
		EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
		req.setDb("pubmed");
		req.setId(idlist);
		EUtilsServiceStub.ESummaryResult res;
		try {
			res = service.run_eSummary(req);

			// results output
			if(res==null){
				return(journal);
			}
			DocSumType[] docs = res.getDocSum();
			if(docs!=null){
				for(int i=0; i<docs.length; i++)
				{
					//System.out.println("ID: "+res.getDocSum()[i].getId());
					String pmid = res.getDocSum()[i].getId();
					for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
					{
						//uncomment to see all the available fields
					//	System.out.println("    " + res.getDocSum()[i].getItem()[k].getName()+": " + res.getDocSum()[i].getItem()[k].getItemContent());
//						if(res.getDocSum()[i].getItem()[k].getName().equals("PubDate")){
//							String date = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"
//							dates.put(pmid, date);
//						}
						
						if(res.getDocSum()[i].getItem()[k].getName().equals("PubTypeList")){
							if(res.getDocSum()[i].getItem()[k]!=null){
								ItemType[] items = res.getDocSum()[i].getItem()[k].getItem();
								if(items!=null){
									for(ItemType item : items){
										journal.put(pmid, item.getItemContent());
									}
								}
							}
						}
					}
					info+="\t";
				}
			}
		} catch (RemoteException e) {
			System.out.println(idlist);
			e.printStackTrace();
		}		


		return journal;
	}
	
	public static Map<String, String> getPubmedJournals(String idlist){
		if(idlist==null||idlist.length()<4){
			return null;
		}
		Map<String, String> journal = new HashMap<String, String>();
		String info = ""; 

		EUtilsServiceStub service = null;
		try {
			service = new EUtilsServiceStub();
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// call NCBI ESummary utility
		EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
		req.setDb("pubmed");
		req.setId(idlist);
		EUtilsServiceStub.ESummaryResult res;
		try {
			res = service.run_eSummary(req);

			// results output
			if(res==null){
				return(journal);
			}
			DocSumType[] docs = res.getDocSum();
			if(docs!=null){
				for(int i=0; i<docs.length; i++)
				{
					//System.out.println("ID: "+res.getDocSum()[i].getId());
					String pmid = res.getDocSum()[i].getId();
					for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
					{
						//uncomment to see all the available fields
						//System.out.println("    " + res.getDocSum()[i].getItem()[k].getName()+": " + res.getDocSum()[i].getItem()[k].getItemContent());
						if(res.getDocSum()[i].getItem()[k].getName().equals("FullJournalName")){
							List<String> auths = new ArrayList<String>();
							if(res.getDocSum()[i].getItem()[k]!=null){
								journal.put(pmid, res.getDocSum()[i].getItem()[k].getItemContent());
							}
						}
					}
					info+="\t";
				}
			}
		} catch (RemoteException e) {
			System.out.println(idlist);
			e.printStackTrace();
		}		


		return journal;
	}

	public static Map<String, List<String>> getPubmedAuthors(String idlist){
		if(idlist==null||idlist.length()<4){
			return null;
		}
		Map<String, List<String>> authors = new HashMap<String, List<String>>();
		String info = ""; 

		EUtilsServiceStub service = null;
		try {
			service = new EUtilsServiceStub();
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// call NCBI ESummary utility
		EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
		req.setDb("pubmed");
		req.setId(idlist);
		EUtilsServiceStub.ESummaryResult res;
		try {
			res = service.run_eSummary(req);

			// results output
			if(res==null){
				return(authors);
			}
			if(res.getDocSum()==null){
				return authors;
			}
			for(int i=0; i<res.getDocSum().length; i++)
			{
				//System.out.println("ID: "+res.getDocSum()[i].getId());
				String pmid = res.getDocSum()[i].getId();
				for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
				{
					//uncomment to see all the available fields
					//System.out.println("    " + res.getDocSum()[i].getItem()[k].getName()+": " + res.getDocSum()[i].getItem()[k].getItemContent());
					if(res.getDocSum()[i].getItem()[k].getName().equals("AuthorList")){
						List<String> auths = new ArrayList<String>();
						if(res.getDocSum()[i].getItem()[k]!=null){
							ItemType[] items = res.getDocSum()[i].getItem()[k].getItem();
							if(items!=null){
								for(int a = 0; a<items.length; a++){
									auths.add(items[a].getItemContent());
								}
								//							for(ItemType it : res.getDocSum()[i].getItem()[k].getItem()){
								//								auths.add(it.getItemContent());
								//							}
								authors.put(pmid, auths);
							}
						}
						//String date = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"
						//authors.put(pmid, date);
					}
				}
				info+="\t";
			}
		} catch (RemoteException e) {
			System.out.println(idlist);
			e.printStackTrace();
		}		


		return authors;
	}

	/**
	 * return a map linking pubmed id to date published	
	 * @param idlist
	 * @return
	 */
	public static Map<String, String> getPubmedDates(String idlist){
		if(idlist==null||idlist.length()<4){
			return null;
		}
		Map<String, String> dates = new HashMap<String, String>();
		String info = ""; 

		EUtilsServiceStub service = null;
		try {
			service = new EUtilsServiceStub();
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// call NCBI ESummary utility
		EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
		req.setDb("pubmed");
		req.setId(idlist);
		EUtilsServiceStub.ESummaryResult res;
		try {
			res = service.run_eSummary(req);

			// results output
			for(int i=0; i<res.getDocSum().length; i++)
			{
				//System.out.println("ID: "+res.getDocSum()[i].getId());
				String pmid = res.getDocSum()[i].getId();
				for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
				{
					//uncomment to see all the available fields
					//System.out.println("    " + res.getDocSum()[i].getItem()[k].getName()+": " + res.getDocSum()[i].getItem()[k].getItemContent());
					if(res.getDocSum()[i].getItem()[k].getName().equals("PubDate")){
						String date = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"
						dates.put(pmid, date);
					}
				}
				info+="\t";
			}
		} catch (RemoteException e) {
			System.out.println(idlist);
			e.printStackTrace();
		}		


		return dates;
	}

	public static String getPubmedDate(String id){
		String info = ""; 
		try
		{
			EUtilsServiceStub service = new EUtilsServiceStub();
			// call NCBI ESummary utility
			EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
			req.setDb("pubmed");
			req.setId(id);
			EUtilsServiceStub.ESummaryResult res = service.run_eSummary(req);
			// results output
			for(int i=0; i<res.getDocSum().length; i++)
			{
				//       System.out.println("ID: "+res.getDocSum()[i].getId());
				for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
				{
					//uncomment to see all the available fields
					//System.out.println("    " + res.getDocSum()[i].getItem()[k].getName()+": " + res.getDocSum()[i].getItem()[k].getItemContent());
					if(res.getDocSum()[i].getItem()[k].getName().equals("PubDate")){
						info += res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"
					}
				}
				info+="\t";
			}
		}

		catch(Exception e) { System.out.println(e.toString()); }
		return info;
	}

	/**
	         try
        {
            EFetchSequenceServiceStub service = new EFetchSequenceServiceStub();
            // call NCBI EFetch utility
            EFetchSequenceServiceStub.EFetchRequest req = new EFetchSequenceServiceStub.EFetchRequest();
            req.setDb("nuccore");
            req.setId(fetchIds);
            EFetchSequenceServiceStub.EFetchResult res = service.run_eFetch(req);
            // results output
            for (int i = 0; i < res.getGBSet().getGBSetSequence().length; i++)
            {
                EFetchSequenceServiceStub.GBSeq_type0 obj = res.getGBSet().getGBSetSequence()[i].getGBSeq();
                System.out.println("Organism: " + obj.getGBSeq_organism());
                System.out.println("Locus: " + obj.getGBSeq_locus());
                System.out.println("Definition: " + obj.getGBSeq_definition());
                System.out.println("------------------------------------------");
            }
        }
        catch (Exception e) { System.out.println(e.toString()); }
	 */
}
