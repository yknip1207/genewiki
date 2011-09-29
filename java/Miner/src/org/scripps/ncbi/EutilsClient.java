package org.scripps.ncbi;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.*;

public class EutilsClient {

    public static void main(String[] args) throws Exception

    {
/**
 pubmed
protein
nucleotide
nuccore
nucgss
nucest
structure
genome
biosystems
books
cancerchromosomes
cdd
gap
dbvar
domains
epigenomics
gene
genomeprj
gensat
geo
gds
homologene
journals
mesh
ncbisearch
nlmcatalog
omia
omim
pepdome
pmc
popset
probe
proteinclusters
pcassay
pccompound
pcsubstance
seqannot
snp
sra
taxonomy
toolkit
toolkitall
unigene
unists
 */
    	
        // eInfo utility returns a list of available databases

        try

        {

            EUtilsServiceStub service = new EUtilsServiceStub();

           

            // call NCBI EInfo utility

            EUtilsServiceStub.EInfoRequest req = new EUtilsServiceStub.EInfoRequest();

            EUtilsServiceStub.EInfoResult res = service.run_eInfo(req);

            // results output

            for(int i=0; i<res.getDbList().getDbName().length; i++)

            {

                System.out.println(res.getDbList().getDbName()[i]);

            }

        }

        catch(Exception e) { System.out.println(e.toString()); }

    }

} 