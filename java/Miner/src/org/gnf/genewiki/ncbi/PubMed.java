package org.gnf.genewiki.ncbi;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.axis2.AxisFault;
import org.gnf.util.BioInfoUtil;
import org.gnf.util.MapFun;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AbstractTextType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AuthorListType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AuthorType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.InvestigatorListType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.InvestigatorType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.MedlineCitationType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.MeshHeadingListType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.MeshHeadingType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleSet;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleSetChoiceE;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleSet_type0;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.DocSumType;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.ItemType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub; 
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AbstractType; 
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
/*
PubDate: 1986 Apr
EPubDate: null
Source: Proc Natl Acad Sci U S A
AuthorList: null
LastAuthor: Putnam FW
Title: Amino acid sequence of human plasma alpha 1B-glycoprotein: homology to the immunoglobulin supergene family.
Volume: 83
Issue: 8
Pages: 2363-7
LangList: null
NlmUniqueID: 7505876
ISSN: 0027-8424
ESSN: 1091-6490
PubTypeList: null
RecordStatus: PubMed - indexed for MEDLINE
PubStatus: ppublish
ArticleIds: null
History: null
References: null
HasAbstract: 1
PmcRefCount: 11
FullJournalName: Proceedings of the National Academy of Sciences of the United States of America
ELocationID: null
SO: 1986 Apr;83(8):2363-7
 */
public class PubMed {

	public static void main(String[] args) {
//		//		Map<String, Set<String>> pmid_meshes = getCachedPmidMeshMap("/Users/bgood/data/bioinfo/pmidmeshcache.txt");
//		//		System.out.println(pmid_meshes.get("3458201"));
//
//		Map<String, List<String>> gene2pub = BioInfoUtil.getHumanGene2pub("/Users/bgood/data/bioinfo/gene2pubmed");
//
//		Set<String> pmids = new HashSet<String>(MapFun.flipMapStringListStrings(gene2pub).keySet());
//		System.out.println(pmids.size()+" ");
//		PubMed pm = new PubMed();
//		pm.buildPubmedSummaryCache(pmids, "/Users/bgood/data/bioinfo/pubmed_summary_cache.txt");
//		Set<String> ids = new HashSet<String>();
//		ids.add("21928318");
//		PubMed pm = new PubMed();
//		try {
//			pm.getAuthorDetails(ids);
//		} catch (AxisFault e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	/**
	 * @param pubcache
	 * @return
	 */

	public static Map<String, String> getCachedPmidAbstractMap(String pubcache){
		Map<String, String> pmid_texts = new HashMap<String, String>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(pubcache));
			String line = f.readLine();
			while(line!=null){
				String[] row = line.split("\t");
				if(row.length==2){
					pmid_texts.put(row[0], row[1]);
				}else if (row.length > 2){
					pmid_texts.put(row[0], row[1]+"\t"+row[2]);
				}
				line = f.readLine();
			}
			f.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pmid_texts;
	}

	public static Map<String, Set<String>> getCachedPmidMeshMap(String pubmeshcache){
		Map<String, Set<String>> pmid_meshs = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(pubmeshcache));
			String line = f.readLine();
			while(line!=null){
				String[] row = line.split("\t");
				String pmid = row[0];
				String[] meshes = row[1].split("::");
				Set<String> mshset = new HashSet<String>();
				for(String m : meshes){
					mshset.add(m);
				}
				pmid_meshs.put(pmid, mshset);
				line = f.readLine();
			}
			f.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pmid_meshs;
	}

	public static Map<String, Set<String>> getMeshTerms(Set<String> idlist) throws AxisFault{
		if(idlist==null){
			return null;
		}
		Map<String, Set<String>> pmid_meshs = new HashMap<String, Set<String>>();
		// call NCBI E utility
		EFetchPubmedServiceStub service = new EFetchPubmedServiceStub();
		//Call NCBI EFetch Utilities 
		EFetchPubmedServiceStub.EFetchRequest req = new EFetchPubmedServiceStub.EFetchRequest();
		int c = 0;
		String ids = "";
		for(String id : idlist){
			ids+=id+",";
		}
		ids = ids.substring(0,ids.length()-1);
		c++;
		req.setId(ids);

		EFetchPubmedServiceStub.EFetchResult res;

		try {
			res = service.run_eFetch(req);
			// results output
			if(res==null){
				for(String id : idlist){
					pmid_meshs.put(id, null);
				}
				return pmid_meshs;
			}

			PubmedArticleSet_type0 articles = res.getPubmedArticleSet();
			String id = null;
			if(articles==null||articles.getPubmedArticleSetChoice()==null){
				for(String id1 : idlist){
					pmid_meshs.put(id1, null);
				}
				return pmid_meshs;
			}
			for(PubmedArticleSetChoiceE article : articles.getPubmedArticleSetChoice()){
				Set<String> meshes = new HashSet<String>();
				try{
					MedlineCitationType cite = article.getPubmedArticle().getMedlineCitation();
					id = cite.getPMID().getString();
					MeshHeadingListType mhlt = cite.getMeshHeadingList();
					if(id!=null&&mhlt!=null){
						for(MeshHeadingType mht : mhlt.getMeshHeading()){
							String term = mht.getDescriptorName().getString();
							meshes.add(term);
						}
						pmid_meshs.put(id, meshes);
					}
				}catch(NullPointerException e){
					System.out.println("No MeSH found for pmid "+id);
				}
			}
		} catch (RemoteException e) {
			System.out.println("error on "+ids);
			e.printStackTrace();
		} 		
		if(c%10==0){
			System.out.println("on "+c+" of "+idlist.size());
		}
		return pmid_meshs;
	}

	public Map<String, PubmedSummary> getCachedPubmedsums(String cachefile){
		Map<String, PubmedSummary> pmid_sums = new HashMap<String, PubmedSummary>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(cachefile));
			String line = f.readLine();
			while(line!=null){
				String[] row = line.split("\t");
				if(row!=null&&row.length>1){
					PubmedSummary sum = new PubmedSummary(row);
					pmid_sums.put(sum.Pmid, sum);
				}
				line = f.readLine();
			}
			f.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pmid_sums;
	}

	public Map<String, Set<String>> readCachedPmid2Authors(String cachefile){
		Map<String, Set<String>> pmid_authors = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(cachefile));
			String line = f.readLine();
			while(line!=null){
				String[] row = line.split("\t");
				if(row!=null&&row.length>1){
					String pmid = row[0];
					String[] authors = row[1].split("::");
					Set<String> authorset = new HashSet<String>();
					for(String a : authors){
						authorset.add(a);
					}
					pmid_authors.put(pmid, authorset);
				}
				line = f.readLine();
			}
			f.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pmid_authors;
	}
	
	public void buildCacheLinkingPmids2Authors(Set<String> pmids, String cachefile){
		Map<String, Set<String>> old_pmid_authors = new HashMap<String, Set<String>>();
		File cache = new File(cachefile);
		try {
			if(!cache.createNewFile()){
				old_pmid_authors = readCachedPmid2Authors(cachefile);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Total pmids done = "+old_pmid_authors.keySet().size());
		pmids.removeAll(old_pmid_authors.keySet());
		System.out.println("Total pmids to get = "+pmids.size());
		int max_batch = 500;
		int c = 0; int n = 0;
		String plist = "";
		if(pmids!=null&&pmids.size()>0){
			for(String pmid : pmids){
				plist+=pmid+",";
				c++;
				n++;
				if(c==max_batch||c==pmids.size()){
					System.out.println("Getting author names - at "+n);
					plist = plist.substring(0, plist.length()-1);
					Map<String, Set<String>> tmp_authors = getPubmedAuthorsFromNCBI(plist);
					try {
						FileWriter f = new FileWriter(cachefile, true);
						for(String pmid_ : tmp_authors.keySet()){
							f.write(pmid_+"\t");
							Set<String> authors = tmp_authors.get(pmid_);
							if(authors!=null&&authors.size()>0){
								String as = "";
								for(String a : authors){
									as+=a+"::";
								}
								as = as.substring(0,as.length()-2);
								f.write(as+"\n");
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					plist = "";
					c=0;
				}
			}
		}				
	}
	
	public void buildPubmedSummaryCache(Set<String> pmids, String cachefile){
		Map<String, PubmedSummary> old_pmid_sums = new HashMap<String, PubmedSummary>();
		File cache = new File(cachefile);
		try {
			if(!cache.createNewFile()){
				old_pmid_sums = getCachedPubmedsums(cachefile);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Total pmids done = "+old_pmid_sums.keySet().size());
		pmids.removeAll(old_pmid_sums.keySet());
		System.out.println("Total pmids to get = "+pmids.size());
		int max_batch = 100;
		int c = 0; int n = 0;
		String plist = "";
		if(pmids!=null&&pmids.size()>0){
			for(String pmid : pmids){
				plist+=pmid+",";
				c++;
				n++;
				if(c==max_batch||c==pmids.size()){
					System.out.println("Getting pubmed sums - at "+n);
					plist = plist.substring(0, plist.length()-1);
					Map<String, PubmedSummary> tmp_sums = getPubmedSummaries(plist);
					try {
						FileWriter f = new FileWriter(cachefile, true);
						for(PubmedSummary psum : tmp_sums.values()){
							f.write(psum.toString()+"\n");
							System.out.println(psum.toString());
						}
						f.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					plist = "";
					c=0;
				}
			}
		}				
	}

	public class AuthorDetails{
		String name;
		String email;
		String institution;
	}

// not working
//	public Map<String, AuthorDetails> getAuthorDetails(Set<String> idlist) throws AxisFault{
//		if(idlist==null){
//			return null;
//		}
//		Map<String, AuthorDetails> author_details = new HashMap<String, AuthorDetails>();
//		// call NCBI E utility
//		EFetchPubmedServiceStub service = new EFetchPubmedServiceStub();
//		//Call NCBI EFetch Utilities 
//		EFetchPubmedServiceStub.EFetchRequest req = new EFetchPubmedServiceStub.EFetchRequest();
//		int c = 0;
//		for(String id : idlist){
//			c++;
//			String output = "";
//			req.setId(id);
//
//			EFetchPubmedServiceStub.EFetchResult res;
//			try {
//				res = service.run_eFetch(req);
//				// results output
//				if(res==null){
//					continue;
//				}
//				String affiliation = res.getPubmedArticleSet().getPubmedArticleSetChoice()[0].getPubmedArticle().getMedlineCitation().getArticle().getAffiliation();
//				System.out.println(affiliation);
//				
//			} catch (RemoteException e) {
//				System.out.println("error on "+id);
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}		
//			if(c%10==0){
//				System.out.println("on "+c+" of "+idlist.size());
//			}
//		}
//		return author_details;
//	}
	
	public static Map<String, String> getPubmedTitlesAndAbstracts(Set<String> idlist, String pubcache, boolean cache) throws AxisFault{
		if(idlist==null){
			return null;
		}
		Map<String, String> pmid_text = new HashMap<String, String>();
		if(cache){
			pmid_text = getCachedPmidAbstractMap(pubcache);
		}
		System.out.println("Cache contains: "+pmid_text.keySet().size());
		idlist.removeAll(pmid_text.keySet());
		System.out.println("Retrieving new: "+idlist.size());
		// call NCBI E utility
		EFetchPubmedServiceStub service = new EFetchPubmedServiceStub();
		//Call NCBI EFetch Utilities 
		EFetchPubmedServiceStub.EFetchRequest req = new EFetchPubmedServiceStub.EFetchRequest();
		int c = 0;
		for(String id : idlist){
			c++;
			String output = "";
			req.setId(id);

			EFetchPubmedServiceStub.EFetchResult res;
			try {
				res = service.run_eFetch(req);
				// results output
				if(res==null){
					pmid_text.put(id, "no data");
					continue;
				}
				String title = res.getPubmedArticleSet().getPubmedArticleSetChoice()[0].getPubmedArticle().getMedlineCitation().getArticle().getArticleTitle().getString();
				AbstractType abstractTypeObj = res.getPubmedArticleSet().getPubmedArticleSetChoice()[0].getPubmedArticle().getMedlineCitation().getArticle().getAbstract();
				String abstractText = "";
				if (abstractTypeObj != null) { 
					AbstractTextType[] abtexts = abstractTypeObj.getAbstractText();
					if(abtexts!=null){
						for(AbstractTextType ab : abtexts){
							abstractText+=ab.getString();
						}
					}
				}

				//				String title = res.getPubmedArticleSet().getPubmedArticle()[0].getMedlineCitation().getArticle().getArticleTitle();
				//				AbstractType abstractTypeObj = res.getPubmedArticleSet().getPubmedArticle()[0].getMedlineCitation().getArticle().getAbstract();
				//				String abstractText = "";
				//				if (abstractTypeObj != null) { 
				//					abstractText = abstractTypeObj.getAbstractText();
				//				}
				output+=title+"\t"+abstractText;
				pmid_text.put(id, output);
				if(cache){
					FileWriter f = new FileWriter(pubcache, true);
					f.write(id+"\t"+output+"\n");
					f.close();
				}
			} catch (RemoteException e) {
				System.out.println("error on "+id);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			if(c%10==0){
				System.out.println("on "+c+" of "+idlist.size());
			}
		}
		return pmid_text;
	}



	public static Map<String, String> getPubmedArticleTypes(String idlist){
		if(idlist==null){
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
		if(idlist==null){
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

	public static Map<String, Set<String>> getPubmedAuthorsFromNCBI(String idlist){
		if(idlist==null){
			return null;
		}
		Map<String, Set<String>> authors = new HashMap<String, Set<String>>();
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
						Set<String> auths = new HashSet<String>();
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
			System.out.println("died sending about "+idlist.length()/9+" pmids");
			e.printStackTrace();
		}		


		return authors;
	}

	public static Map<String, String> getPubmedDates(Set<String> idlist){
		String ids = "";
		for(String s : idlist){
			ids+=s+",";
		}
		ids = ids.substring(0, ids.length()-1);
		return getPubmedDates(ids);
	}

	
	public String convertSeasonToMonth(String season){
		String month = season;
		
		if(month.contains("Autumn")){
			month = month.replace("Autumn", "Sep");
		} else if(month.contains("Fall")){
			month = month.replace("Fall", "Sep");
		} else if(month.contains("Winter")){
			month = month.replace("Winter","Dec");
		} else if(month.contains("Spring")){
			month = month.replace("Spring","Mar");
		} else if(month.contains("Summer")){
			month = month.replace("Summer","Jun");
		}
		
		return month;
	}

	public class PubmedSummary{
		String Pmid;
		String PubDate; //: 2001 Apr
		String EPubDate;
		String Source;// Mol Pharmacol
		String AuthorList;// null
		String LastAuthor;// Zhou QY
		String Title;// Identification of two prokineticin cDNAs: recombinant proteins potently contract gastrointestinal smooth muscle.
		String Volume;// 59
		String Issue;// 4
		String Pages;// 692-8
		String LangList;// null
		String NlmUniqueID;// 0035623
		String ISSN;// 0026-895X
		String ESSN;// 1521-0111
		String PubTypeList;// null
		String RecordStatus;// PubMed - indexed for MEDLINE
		String PubStatus;// ppublish
		String ArticleIds;// null
		String History;// null
		String References;// null
		String HasAbstract;// 1
		String PmcRefCount;// 39
		String FullJournalName;// Molecular pharmacology
		String ELocationID;// null
		String SO;// 2001 Apr;59(4):692-8
		public PubmedSummary(String pmid, String pubDate, String ePubDate, String source,
				String authorList, String lastAuthor, String title,
				String volume, String issue, String pages, String langList,
				String nlmUniqueID, String iSSN, String eSSN,
				String pubTypeList, String recordStatus, String pubStatus,
				String articleIds, String history, String references,
				String hasAbstract, String pmcRefCount, String fullJournalName,
				String eLocationID, String sO) {
			super();
			Pmid = pmid;
			PubDate = pubDate;
			EPubDate = ePubDate;
			Source = source;
			AuthorList = authorList;
			LastAuthor = lastAuthor;
			Title = title;
			Volume = volume;
			Issue = issue;
			Pages = pages;
			LangList = langList;
			NlmUniqueID = nlmUniqueID;
			ISSN = iSSN;
			ESSN = eSSN;
			PubTypeList = pubTypeList;
			RecordStatus = recordStatus;
			PubStatus = pubStatus;
			ArticleIds = articleIds;
			History = history;
			References = references;
			HasAbstract = hasAbstract;
			PmcRefCount = pmcRefCount;
			FullJournalName = fullJournalName;
			ELocationID = eLocationID;
			SO = sO;
		}
		public PubmedSummary(String[] row) {
			super();
			if(row.length>23){
				Pmid = row[0];
				PubDate = row[1];
				EPubDate = row[2];
				Source = row[3];
				AuthorList = row[4];
				LastAuthor = row[5];
				Title = row[6];
				Volume = row[7];
				Issue = row[8];
				Pages = row[9];
				LangList = row[10];
				NlmUniqueID = row[11];
				ISSN = row[12];
				ESSN = row[13];
				PubTypeList = row[14];
				RecordStatus = row[15];
				PubStatus = row[16];
				ArticleIds = row[17];
				History = row[18];
				References = row[19];
				HasAbstract = row[20];
				PmcRefCount = row[21];
				FullJournalName = row[22];
				ELocationID = row[23];
				SO = row[24];
			}
		}

		public String toString(){
			String psum = 
				Pmid+"\t"+
				PubDate+"\t"+
				EPubDate+"\t"+
				Source+"\t"+
				AuthorList+"\t"+
				LastAuthor+"\t"+
				Title+"\t"+
				Volume+"\t"+
				Issue+"\t"+
				Pages+"\t"+
				LangList+"\t"+
				NlmUniqueID+"\t"+
				ISSN+"\t"+
				ESSN+"\t"+
				PubTypeList+"\t"+
				RecordStatus+"\t"+
				PubStatus+"\t"+
				ArticleIds+"\t"+
				History+"\t"+
				References+"\t"+
				HasAbstract+"\t"+
				PmcRefCount+"\t"+
				FullJournalName+"\t"+
				ELocationID+"\t"+
				SO;

			return psum;
		}
		public String getPmid() {
			return Pmid;
		}
		public void setPmid(String pmid) {
			Pmid = pmid;
		}
		public String getPubDate() {
			return PubDate;
		}
		public void setPubDate(String pubDate) {
			PubDate = pubDate;
		}
		public String getEPubDate() {
			return EPubDate;
		}
		public void setEPubDate(String ePubDate) {
			EPubDate = ePubDate;
		}
		public String getSource() {
			return Source;
		}
		public void setSource(String source) {
			Source = source;
		}
		public String getAuthorList() {
			return AuthorList;
		}
		public void setAuthorList(String authorList) {
			AuthorList = authorList;
		}
		public String getLastAuthor() {
			return LastAuthor;
		}
		public void setLastAuthor(String lastAuthor) {
			LastAuthor = lastAuthor;
		}
		public String getTitle() {
			return Title;
		}
		public void setTitle(String title) {
			Title = title;
		}
		public String getVolume() {
			return Volume;
		}
		public void setVolume(String volume) {
			Volume = volume;
		}
		public String getIssue() {
			return Issue;
		}
		public void setIssue(String issue) {
			Issue = issue;
		}
		public String getPages() {
			return Pages;
		}
		public void setPages(String pages) {
			Pages = pages;
		}
		public String getLangList() {
			return LangList;
		}
		public void setLangList(String langList) {
			LangList = langList;
		}
		public String getNlmUniqueID() {
			return NlmUniqueID;
		}
		public void setNlmUniqueID(String nlmUniqueID) {
			NlmUniqueID = nlmUniqueID;
		}
		public String getISSN() {
			return ISSN;
		}
		public void setISSN(String iSSN) {
			ISSN = iSSN;
		}
		public String getESSN() {
			return ESSN;
		}
		public void setESSN(String eSSN) {
			ESSN = eSSN;
		}
		public String getPubTypeList() {
			return PubTypeList;
		}
		public void setPubTypeList(String pubTypeList) {
			PubTypeList = pubTypeList;
		}
		public String getRecordStatus() {
			return RecordStatus;
		}
		public void setRecordStatus(String recordStatus) {
			RecordStatus = recordStatus;
		}
		public String getPubStatus() {
			return PubStatus;
		}
		public void setPubStatus(String pubStatus) {
			PubStatus = pubStatus;
		}
		public String getArticleIds() {
			return ArticleIds;
		}
		public void setArticleIds(String articleIds) {
			ArticleIds = articleIds;
		}
		public String getHistory() {
			return History;
		}
		public void setHistory(String history) {
			History = history;
		}
		public String getReferences() {
			return References;
		}
		public void setReferences(String references) {
			References = references;
		}
		public String getHasAbstract() {
			return HasAbstract;
		}
		public void setHasAbstract(String hasAbstract) {
			HasAbstract = hasAbstract;
		}
		public String getPmcRefCount() {
			return PmcRefCount;
		}
		public void setPmcRefCount(String pmcRefCount) {
			PmcRefCount = pmcRefCount;
		}
		public String getFullJournalName() {
			return FullJournalName;
		}
		public void setFullJournalName(String fullJournalName) {
			FullJournalName = fullJournalName;
		}
		public String getELocationID() {
			return ELocationID;
		}
		public void setELocationID(String eLocationID) {
			ELocationID = eLocationID;
		}
		public String getSO() {
			return SO;
		}
		public void setSO(String sO) {
			SO = sO;
		}

	}


	/**
	 * return a map linking pubmed id to complete pubmed summary
	 * @param idlist
	 * @return
	 */
	public Map<String, PubmedSummary> getPubmedSummaries(String idlist){
		if(idlist==null){
			return null;
		}
		Map<String, PubmedSummary> sums = new HashMap<String, PubmedSummary>();
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
				String PubDate = ""; //: 2001 Apr
				String EPubDate = "";
				String Source = "";// Mol Pharmacol
				String AuthorList = "";// null
				String LastAuthor = "";// Zhou QY
				String Title = "";// Identification of two prokineticin cDNAs: recombinant proteins potently contract gastrointestinal smooth muscle.
				String Volume = "";// 59
				String Issue = "";// 4
				String Pages = "";// 692-8
				String LangList = "";// null
				String NlmUniqueID = "";// 0035623
				String ISSN = "";// 0026-895X
				String ESSN = "";// 1521-0111
				String PubTypeList = "";// null
				String RecordStatus = "";// PubMed - indexed for MEDLINE
				String PubStatus = "";// ppublish
				String ArticleIds = "";// null
				String History = "";// null
				String References = "";// null
				String HasAbstract = "";// 1
				String PmcRefCount = "";// 39
				String FullJournalName = "";// Molecular pharmacology
				String ELocationID = "";// null
				String SO = "";// 2001 Apr;59(4):692-8
				for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
				{
					if(res.getDocSum()[i].getItem()[k].getName().equals("PubDate")){
						PubDate = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("EPubDate")){
						EPubDate = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("Source")){
						Source = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("AuthorList")){
						AuthorList = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("LastAuthor")){
						LastAuthor = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("Title")){
						Title = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("Volume")){
						Volume = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("Issue")){
						Issue = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("Pages")){
						Pages = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("LangList")){
						LangList = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("NlmUniqueID")){
						NlmUniqueID = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("ISSN")){
						ISSN = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("ESSN")){
						ESSN = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("PubTypeList")){
						PubTypeList = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("RecordStatus")){
						RecordStatus = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("PubStatus")){
						PubStatus = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("ArticleIds")){
						ArticleIds = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("History")){
						History = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("HasAbstract")){
						HasAbstract = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("PmcRefCount")){
						PmcRefCount = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("FullJournalName")){
						FullJournalName = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("ELocationID")){
						ELocationID = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}
					else if(res.getDocSum()[i].getItem()[k].getName().equals("SO")){
						SO = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"						
					}

				}
				PubmedSummary psum = new PubmedSummary(pmid, PubDate, EPubDate, Source, AuthorList, LastAuthor, Title, Volume, Issue, Pages, LangList, NlmUniqueID, ISSN, ESSN, PubTypeList, RecordStatus, PubStatus, ArticleIds, History, References, HasAbstract, PmcRefCount, FullJournalName, ELocationID, SO);
				sums.put(pmid, psum);
				info+="\t";
			}
		} catch (RemoteException e) {
			System.out.println(idlist);
			e.printStackTrace();
		}		


		return sums;
	}

	/**
	 * return a map linking pubmed id to date published	
	 * @param idlist
	 * @return
	 */
	public static Map<String, String> getPubmedDates(String idlist){
		if(idlist==null){
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
					System.out.println("    " + res.getDocSum()[i].getItem()[k].getName()+": " + res.getDocSum()[i].getItem()[k].getItemContent());
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

	public static Map<String, Set<String>> getMeshTerms(String pmid) throws AxisFault {
		Set<String> pmids = new HashSet<String>();
		pmids.add(pmid);
		return getMeshTerms(pmids);
	}

	public static Map<String, Set<String>> getPubmedAuthorsNCBI(Set<String> pmids) {
		String plist = "";
		int max_batch = 500;
		int c = 0;
		Map<String, Set<String>> pmid_authors = new HashMap<String, Set<String>>();
		if(pmids!=null&&pmids.size()>0){
			for(String pmid : pmids){
				plist+=pmid+",";
				c++;
				if(c==max_batch||c==pmids.size()){
					System.out.println("Getting author names for, batch size = "+c);
					plist = plist.substring(0, plist.length()-1);
					Map<String, Set<String>> tmp_authors = getPubmedAuthorsFromNCBI(plist);
					pmid_authors = MapFun.mergeStringSetMaps(pmid_authors, tmp_authors);
					plist = "";
					c =0;
				}
			}
		}

		return pmid_authors;
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
	/*
				 String db = "", searchTerm = "", retMax = "", relDate = "", minDate = "", maxDate = "", dateType = ""; String[] idListArray = null; String id = "", title = "", abstractText = ""; int numLiteratureId = 0;
		try {
			EUtilsServiceStub service1 = new EUtilsServiceStub();
			// call NCBI ESearch utility // NOTE: search term should be URL encoded 
			EUtilsServiceStub.ESearchRequest req1 = new EUtilsServiceStub.ESearchRequest();
			//Set parameters 
			req1.setDb(db); req1.setTerm(searchTerm); 
			req1.setDatetype(retMax); req1.setReldate(relDate); req1.setMindate(minDate); req1.setMaxdate(maxDate); req1.setDatetype(dateType);
			//Run ESearch 
			EUtilsServiceStub.ESearchResult res1 = service1.run_eSearch(req1);
			// results output
			idListArray = res1.getIdList().getId();
			if (idListArray != null) {
				EFetchPubmedServiceStub service2 = new EFetchPubmedServiceStub();
				//Call NCBI EFetch Utilities 
				EFetchPubmedServiceStub.EFetchRequest req2 = new EFetchPubmedServiceStub.EFetchRequest();
				//Get the number of returned Pubmed IDs 
				numLiteratureId = idListArray.length;
				System.out.println("NUmber of returned literatures = " + numLiteratureId);
				for (int i = 0; i < numLiteratureId; i++) {
					//Allow the thread to sleep for 3 seconds before //fetch literature. This is an attempt not to overloading //NCBI server. 
					try {
						Thread.sleep(3000); 
					} catch (InterruptedException e) { }
					id = idListArray[i]; System.out.println("Pubmed ID: " + id);
					//Set Pubmed ID 
					req2.setId(id);
					//Run EFetch 
					EFetchPubmedServiceStub.EFetchResult res2 = service2.run_eFetch(req2);
					//Print out result 
					int numPubmedArticleSet = res2.getPubmedArticleSet().getPubmedArticle().length; 
					System.out.println("Number of pubmed article sets = " + numPubmedArticleSet);
					//String pubmedId=res.getPubmedArticleSet().getPubmedArticle()[0].getMedlineCitation().getPMID();
					title = res2.getPubmedArticleSet().getPubmedArticle()[0].getMedlineCitation().getArticle().getArticleTitle();
					AbstractType abstractTypeObj = res2.getPubmedArticleSet().getPubmedArticle()[0].getMedlineCitation().getArticle().getAbstract();


					if (abstractTypeObj != null) { abstractText = abstractTypeObj.getAbstractText();
					} 
					System.out.println(title); System.out.println(abstractText); System.out.println();
				}
			}
		}catch (Exception e){}
	 */

}
