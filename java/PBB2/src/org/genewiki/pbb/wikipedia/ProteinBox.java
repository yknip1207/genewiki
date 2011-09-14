package org.genewiki.pbb.wikipedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.genewiki.pbb.Configs;
import org.genewiki.pbb.db.DatabaseManager;
import org.genewiki.pbb.exceptions.ConfigException;
import org.genewiki.pbb.images.PdbImage;

import com.google.common.base.Preconditions;

/**
 * ProteinBox represents all possible fields in the GNF_Protein_box wikipedia template.
 * It uses the Builder construction pattern to allow optional fields. See ProteinBox.Builder
 * for construction.
 * @author erikclarke
 *
 */
public class ProteinBox {
	
	
	
	// Keys that may only correspond to one value
	private static final List<String> SINGLE_VALUES = Arrays.asList(new String[]{
			"Name", 
			"image", 
			"image_source", 
			"HGNCid", 
			"MGIid",
			"Symbol",
			"OMIM", 
			"ECnumber", 
			"Homologene", 
			"GeneAtlas_image1",
			"GeneAtlas_image2", 
			"GeneAtlas_image3", 
			"Protein_domain_image",
			"Hs_EntrezGene",
			"Hs_Ensembl",
			"Hs_RefseqmRNA",
			"Hs_RefseqProtein",
			"Hs_GenLoc_db",
			"Hs_GenLoc_chr",
			"Hs_GenLoc_start",
			"Hs_GenLoc_end",
			"Hs_Uniprot",
			"Mm_EntrezGene",
			"Mm_Ensembl",
			"Mm_RefseqmRNA",
			"Mm_RefseqProtein",
			"Mm_GenLoc_db",
			"Mm_GenLoc_chr",
			"Mm_GenLoc_start",
			"Mm_GenLoc_end",
			"Mm_Uniprot"});
	
	// Keys that correspond to a list of values
	private static final List<String> MULTIPLE_VALUES = Arrays.asList(new String[]{
			"PDB", 
			"AltSymbols", 
			"Function", 
			"Component", 
			"Process"});
	
	// All fields, for convenience (in order)
	public static final List<String> ALL_VALUES = Arrays.asList(new String[] {
			"Name", 
			"image", 
			"image_source",
			"PDB",
			"HGNCid", 
			"MGIid",
			"Symbol",
			"AltSymbols",
			"OMIM", 
			"ECnumber", 
			"Homologene", 
			"GeneAtlas_image1",
			"GeneAtlas_image2", 
			"GeneAtlas_image3", 
			"Protein_domain_image",
			"Function",
			"Component",
			"Process",
			"Hs_EntrezGene",
			"Hs_Ensembl",
			"Hs_RefseqmRNA",
			"Hs_RefseqProtein",
			"Hs_GenLoc_db",
			"Hs_GenLoc_chr",
			"Hs_GenLoc_start",
			"Hs_GenLoc_end",
			"Hs_Uniprot",
			"Mm_EntrezGene",
			"Mm_Ensembl",
			"Mm_RefseqmRNA",
			"Mm_RefseqProtein",
			"Mm_GenLoc_db",
			"Mm_GenLoc_chr",
			"Mm_GenLoc_start",
			"Mm_GenLoc_end",
			"Mm_Uniprot"});
	
	private LinkedHashMap<String, String> singleValueFields;
	private LinkedHashMap<String, List<String>> multipleValueFields;
	
	private String prependText;
	private String appendText;
	private String summary;
	private List<String> fields_changed;
	
	private String id;		// The Entrez id for this gene
	
	
	/**
	 * The ProteinBox.Builder allows for the creation of a ProteinBox with optional
	 * fields by stringing together or stacking Builder objects for each field.
	 * <br \>
	 * <code>
	 * ProteinBox myBox = new ProteinBox.Builder("name", "symbol").add("key","value)...
	 * 	.add("lastKey","lastValue").build();
	 * </code>
	 * <br \>
	 * See Bloch, Joshua. Effective Java, 2nd Ed. Item 2.
	 * @author erikclarke
	 *
	 */
	public static class Builder {
		private final LinkedHashMap<String, String> singleValFields;
		private final LinkedHashMap<String, List<String>> multipleValFields;
		private boolean used = false;
		
		/**
		 * Creates a new ProteinBox.Builder with the required Name and Hs_EntrezGene fields, which can then be
		 * extended through this.add(key, value), or used to generate a ProteinBox through this.build().
		 * @param name
		 * @param entrezId
		 */
		public Builder(String name, String entrezId) {
			singleValFields = new LinkedHashMap<String, String>();
			multipleValFields = new LinkedHashMap<String, List<String>>();
			
			// Initializing all known fields to default empty values
			for (String key : SINGLE_VALUES) {
				singleValFields.put(key, "");
				//System.out.println(singleValFields.get(key));
			}
			for (String key : MULTIPLE_VALUES) {
				multipleValFields.put(key, new ArrayList<String>(0));
			}
			
			// initializing the two required fields
			if (!(name == null) || !(entrezId == null)) {
				singleValFields.put("Name", name);
				singleValFields.put("Hs_EntrezGene", entrezId);
			} else if (Configs.GET.flag("canCreate")){
				throw new IllegalArgumentException("Name and/or EntrezId fields cannot be null.");
			}
		}
		
		/**
		 * Assigns a value to a key in the ProteinBox.Builder
		 * @param key to assign value (must exist in ProteinBox)
		 * @param value a single value
		 * @return this builder object (call Builder.build() to create new ProteinBox)
		 */
		public Builder add(String key, String value) {
			if (SINGLE_VALUES.contains(key)) {
				singleValFields.put(key, value);
			} else {
				throw new IllegalArgumentException(String.format("'%s' is not a valid ProteinBox fieldname.", key));
			}
			return this;
		}
		
		/**
		 * Assigns a value to a key in the ProteinBox.Builder
		 * @param key to assign values (must exist in ProteinBox)
		 * @param value a list of values
		 * @return this builder object (call Builder.build() to create new ProteinBox)
		 */
		public Builder add(String key, List<String> value) {
			if (MULTIPLE_VALUES.contains(key)) {
				multipleValFields.put(key, value);
			} else {
				throw new IllegalArgumentException(String.format("'%s' is not a valid ProteinBox fieldname.", key));
			}
			return this;
		}
		
		/**
		 * Searches for an image for the given PDB id (and HUGO symbol) in Wikimedia Commons and links to it.
		 * If this fails, a new image is generated and uploaded, and the ProteinBox is linked
		 * to the new image instead.
		 * @param pdbId
		 * @return this Builder object (call Builder.build() to create a new ProteinBox)
		 */
		public Builder addImage(String pdbId) {
			String pdb;
			String sym;
			String prevImg;
			String img;
			String imgSrc;
			try {
				pdb = Preconditions.checkNotNull(this.multipleValFields.get("PDB").get(0));
				sym = Preconditions.checkNotNull(this.singleValFields.get("Symbol"));
				prevImg = Preconditions.checkNotNull(this.singleValFields.get("image"));
			} catch (NullPointerException e) {
				return this; 	// Something we needed was null; abort
			}
			
			// If we have a pdb value and no previous image:
			if (!pdb.equals("") && prevImg.equals("")) {
//				System.out.print("Searching for existing image on Commons... ");
				img = ImageFinder.getImage(sym, pdb.toLowerCase());
				if (img != null) {
//					System.out.println("image found and linked.");
					imgSrc = "Rendering of "+sym+" from [[Protein Data Bank | PDB]] {{PDB2|"+pdb+"}}";
				} else {
//					System.out.println("no image found, attempting render and upload.");
					try {
						PdbImage pdbImg = new PdbImage(pdb, sym);
						pdbImg.uploadPdbImg();
						img = pdbImg.getImage();
						imgSrc = pdbImg.getCaption();
					} catch (IOException ie) {
						ie.printStackTrace();
						System.err.println("Could not create image- ensure that PyMOL is configured correctly.");
						return this;
					} catch (ConfigException ce) {
						ce.printStackTrace();
						System.err.println("Could not upload image due to bad URL or credentials in bot.properties file.");
						return this;
					}
				}
				
				singleValFields.put("image", img);
				singleValFields.put("image_source", imgSrc);
			} else {
				if (pdb.equals("")) {
//					System.out.println("No PDB values found to search for or generate image from.");
				} else {
//					System.out.println("Previous image already set.");
				}
			}
			return this;
		}
		
		public ProteinBox build() {
			if (!used) {
				used = true;
				return new ProteinBox(this);
			} else {
				throw new RuntimeException("This builder has already been used and cannot be used again.");
			}
		}
	}
	
	/** 
	 * Constructor is inaccessible; use ProteinBox.Builder.build()
	 * @param builder
	 */
	private ProteinBox(Builder builder) {
		singleValueFields = builder.singleValFields;
		multipleValueFields = builder.multipleValFields;
		prependText = "";
		appendText = "";
		fields_changed = new ArrayList<String>(0);
		id = builder.singleValFields.get("Hs_EntrezGene");
	}
	
	/* ---- Public Methods ---- */
	
	/**
	 * Returns a single value for a key (key must exist in list of single-value keys)
	 * @param key
	 * @return single string
	 */
	public String getSingle(String key) {
		if(SINGLE_VALUES.contains(key)) {
			return singleValueFields.get(key);
		} else {
			throw new IllegalArgumentException(String.format("'%s' is not a valid ProteinBox fieldname.", key));
		}
	}
	
	/**
	 * Returns a list of strings for a key (key must exist in list of multiple-value keys)
	 * @param key
	 * @return list of strings
	 */
	public List<String> getList(String key) {
		if(MULTIPLE_VALUES.contains(key)) {
			return multipleValueFields.get(key);
		} else {
			throw new IllegalArgumentException(String.format("'%s' is not a valid ProteinBox fieldname.", key));
		}
	}
	
	public void prepend(String prependText) {
		this.prependText = prependText;
	}
	
	public void append(String appendText) {
		this.appendText = appendText;
	}
	
	public void setEditSummary(String summary) {
		this.summary = summary;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public String getChangedFields() {
		String str = this.fields_changed.toString();
		return str;
	}
	
	public String getId() {
		return this.id;
	}
	
	/**
	 * We override the updated ProteinBox's id with the original even if the source
	 * has a different one so that the same template gets updated.	
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Returns a copy of this ProteinBox updated with data from another ProteinBox. 
	 * If data is unique in this instance (i.e. the source is missing the information 
	 * and this instance has it, it is kept. Otherwise, any differing data is set to 
	 * the source's version.
	 * @param source
	 * @return copy of this ProteinBox with updated fields
	 */
	public ProteinBox updateWith(ProteinBox source) {
		int updated = 0;
		String name = source.getSingle("Name");	
		String entrez = source.getSingle("Hs_EntrezGene");
		ProteinBox.Builder builder = new ProteinBox.Builder(name, entrez);
		
		for (String key : SINGLE_VALUES) {
			String thisValue = this.getSingle(key);
			String sourceValue = source.getSingle(key);
			if (sourceValue == null && thisValue == null) {
				continue;
			} else if (sourceValue == null) {
				// Nothing to update.
//				System.out.println("WARNING: mygene.info potentially missing information in field "+ key);
//				try {
//					db.addMissingFromSource(entrez, key, thisValue);
//				} catch (SQLException e) {
//					botState.minor(e);
//				}
				builder.add(key, thisValue);
				
			} else if (thisValue == null) {
				// Definitely update.
//				System.out.printf("Field %s in original is empty.", key);
			} else if (thisValue.equalsIgnoreCase(sourceValue)) {
				// No need to update.
				builder.add(key, thisValue);
			} else if (sourceValue.equals("") || sourceValue.equals("0") || sourceValue == null) {
				// No need to update.
				builder.add(key, thisValue);
			} else {
				updated++;
				builder.add(key, sourceValue);
				DatabaseManager.addChange(entrez, key, thisValue, sourceValue);
				//System.out.printf("Gene %s \t Field %s \t Old %s \t New %s \n", entrez, key, thisValue, sourceValue);
				this.fields_changed.add(key);
			}
		}
		
		for (String key : MULTIPLE_VALUES) {
			List<String> thisList = this.getList(key);
			List<String> sourceList = source.getList(key);
			Collections.sort(thisList);
			Collections.sort(sourceList);
			if (sourceList.isEmpty()) {
				// Don't update if it's empty.
				builder.add(key, thisList);
			} else if (thisList.equals(sourceList)) {
				// Don't update if they're the same lists.
				builder.add(key, thisList);
			} else {

				// We always overwrite the fields with lists
				// due to their changing nature (ontologies
				// could be corrected, PDB ids could be removed,
				// etc)
				updated++;
				builder.add(key, sourceList);
				
				DatabaseManager.addChange(entrez, key, thisList.toString(), sourceList.toString());
				this.fields_changed.add(key);
				//System.out.printf("Gene %s \t Field %s \t Old %s \t New %s \n", entrez, key, thisList.toString(), sourceList.toString());
				
			}
		}
		
		// Add images if possible.
		// This is done last to get the most up-to-date PDB values.
		try {
			String pdb = Preconditions.checkNotNull(builder.multipleValFields.get("PDB").get(0));
			builder.addImage(pdb); // This method searches for the image in commons and renders/uploads a new one if missing
			String image = builder.singleValFields.get("image");
			String image_source = builder.singleValFields.get("image_source");
			if (!image.equals("")) {
				DatabaseManager.addChange(entrez, "image", "", image);
				DatabaseManager.addChange(entrez, "image_source", "", image_source);
				updated += 2;
			} else {
//				System.out.println("No image found or created. Moving on...");
			}
		} catch (IndexOutOfBoundsException e) {
			// No image for us this time.
		}

		
		ProteinBox pb = builder.build();
		pb.setEditSummary(String.format("%d/%d fields updated.\n", updated, ALL_VALUES.size()));
		pb.prepend(this.prependText);
		pb.append(this.appendText);
		
		pb.setId(this.getId()); // We always need to have the ids match so that the correct template gets updated.
		
		return pb;
	}

	/**
	 * Returns the properly formatted wikitext representation of the ProteinBox.
	 */
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		String nl = System.getProperty("line.separator");
		
		out.append(prependText);
		out.append("{{GNF_Protein_box"+nl);
		for (String key : ALL_VALUES) {
			String value = "";
			if (SINGLE_VALUES.contains(key)) {
				value = getSingle(key);
				if (key.equals("Name")) 
					{ value = Character.toUpperCase(value.charAt(0)) + value.substring(1); }
				out.append(String.format(" | %s = %s", key, value) + nl);
			} else {
				List<String> list = getList(key);
				if (key.equals("AltSymbols")) {
					value = StringUtils.join(list, "; ");
					out.append(String.format(" | %s =; %s", key, value) + nl);
				} else {
					out.append(String.format(" | %s = ", key));
					for (String str : list) {
						if (!str.equals("")) {
							out.append("{{");
							if (key.equals("PDB")) { 
								out.append("PDB2|"+str+"}}, ");
							} else { 
								out.append(str+"}} ");
							}
						}
					}
					if (out.charAt(out.length()-2) == ',')
						out.deleteCharAt(out.length()-2); // Removes the trailing comma from a list of template links
					out.append(nl);
				}
			}
		}
		out.append("}}");
		out.append(appendText);
		return out.toString();
	}
	
	public void reset() {
		singleValueFields = new LinkedHashMap<String, String>();
		multipleValueFields = new LinkedHashMap<String, List<String>>();
		prependText = "";
		appendText = "";
		
	}
	
	
//	public static void main(String[] args) {
//
//		ProteinBox.Builder buildMe = new ProteinBox.Builder("LOL", "LOL1").add("OMIM", "1234");
//		buildMe.add("PDB", Arrays.asList(new String[]{"123", "452", "f4g"}));
//		ProteinBox pb = buildMe.build();
//		pb.prepend("Something before");
//		pb.append("Something after");
//		pb.getChangedFields();
//		//buildMe = new ProteinBox.Builder("LOL", "LOL1");
//		buildMe.add("OMIM", "blahblah");
//		buildMe.add("PDB", Arrays.asList(new String[]{"123", "jupiter"}));
//		ProteinBox src = buildMe.build();
//		pb = pb.updateWith(src);
//		System.out.println(pb.toString());
//		
//	}
}