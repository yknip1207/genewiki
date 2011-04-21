Access page view, revision and volume information for an arbitrary list of wikipedia pages

Example invocation to get info on the articles listed in ./example_genes.txt, limited to 10, from oct 9 2009 to dec 30 2009 and write the data to the ./example/ directory. 

java -jar wp_metrics.jar -f ./example_genes.txt -o ./example_out/ -L 10 -t0 20090101 -t1 20091231 -rv -vl -pv

When its finished, you can view the tab-delimited .txt files in the viewer of your choice.  In addition, if you place the ./example/ directory in a web server, you can view the data through the index.html page.  (The data won't render if its not in a web server because of the way the JSON is read.)  You could also use the generated JSON data directly in your own web page.

Example invocation for gene wiki

java -jar wp_metrics.jar -o ./example_out/ -t Template:GNF_Protein_box -L 10 -t0 20101231 -t1 20110214 -rv -vl -pv

If running this with many pages, you need to increase java's memory allocation.  (You need to increase it to run with the whole gene wiki for example (10,000+ pages).
To do this, add something like -Xmx1000m to the command like this (1000m = 1000mb)

java -jar -Xmx1000m wp_metrics.jar -o ./example_out/ -t Template:GNF_Protein_box -L 10 -t0 20101231 -t1 20110214 -rv -vl -pv

enter java -jar wp_metrics.jar -h for more help 

-----------------------------
Note.  This distribution depends on code from here for accessing Wikipedia
http://code.google.com/p/gwtwiki/

This dependency is the main reason for the very large number and weight of files in the lib directory.  Some day maybe, perhaps, we could recode the small part of this library that we actually use and reduce the dependency footprint of this little program by 100 times..
 

