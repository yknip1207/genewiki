package org.genewiki.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.genewiki.StatusMonitor;
import org.genewiki.concurrent.Distributor;
import org.genewiki.util.Serialize;
import org.gnf.pbb.util.FileHandler;

import com.google.common.io.Files;

public class SyncControl {

//	private final ThreadFactory factory;
//	private final ExecutorService manager;
//	
//	
//	public SyncControl() {
//		factory = Executors.defaultThreadFactory();
//		manager = Executors.newFixedThreadPool(4, factory);
//	}
//	
//	public void execute(List<String> titles, int blocks) {
//		int sections = titles.size()/blocks;		//TODO Test me!
//		List<Thread> threads = new ArrayList<Thread>();
//		for (int i=0; i <= sections; i++) {
//			int start = i*blocks;
//			int end = ((i+1)*blocks) < titles.size() ?
//					(i+1)*blocks : titles.size();
//			List<String> sublist;
//			sublist = titles.subList(start, end);
////			threads.add(factory.newThread(new Sync(sublist, "THREAD "+i)));
////			if (i>5) {
////				System.out.println("Sleeping...");
////				try {Thread.sleep(20000);} catch (InterruptedException e) {};
////			}
//			System.out.println("Created new thread from "+(start)+" to "+end);
//		}
//		for (Thread thread : threads) {
//			manager.execute(thread);
//			System.out.println("******\n" +
//					"* SYNC CONTROLLER:Executing new thread...\n" +
//					"******\n");
//			try {Thread.sleep(5000);} catch (InterruptedException e) {};
//		}
//		manager.shutdown();
//		while (!manager.isTerminated()) {
//			try {
//				System.err.println("Sync Controller awaiting completion...");
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				// nobody really cares
//			}
//		}
//		System.err.println("Updates finished.");
//	}
//	
	
	
	public static void main(String[] args) throws IOException {
	
		Distributor<String> distributor = new Distributor<String>(4, new Sync());
		
		OptionParser parser = new OptionParser( "ar:of:" );
		OptionSet options = parser.parse(args);
		
		SyncControl control = new SyncControl();
		StatusMonitor monitor = StatusMonitor.instance;
		List<String> GWPages = new ArrayList<String>();
		if (options.has("a")) {
			GWPages = monitor.getAllGeneWikiPages();
			
		} else if (options.hasArgument("r")) {
			try {
				GWPages = monitor.getRecentChanges(
					Integer.parseInt((String) options.valueOf("r")));
			} catch (NumberFormatException e) {
				System.out.println("Option for -r must be an integer.");
			}
			
		} else if (options.has("o")){
			GWPages = monitor.getAllGeneWikiPages();
			System.out.println("Updating all outbound links...");
			List<String> linkedResources = monitor.getLinkedResources(GWPages);
			Serialize.out("linkedResources.list", new ArrayList<String>(linkedResources));
			distributor.execute(linkedResources, 1000);
			return;		// To prevent any following execution
			
		} else if (options.hasArgument("f")) {
			try {
				GWPages = (List<String>) Serialize.in((String) options.valueOf("f"));
				FileHandler fh = new FileHandler("export");
				Files.createParentDirs(fh.getRoot());
				fh.write("", "gimme.txt", 'o');
				for (String title : GWPages) {
					fh.write(title+"\n", "gimme.txt", 'a');
				}
				System.out.println("Done writing.");
			} catch (ClassCastException cce) {
				System.out.println("Could not deserialized specified file into List<String>.");
				System.exit(2);		//TODO maybe shouldn't use
			}
			
		} else {
			// print help
			System.out.println(
					"Sync: \n" +
					"-a			Update all Genewiki pages \n"+
					"-r [int]		Update pages changed within the specified minutes \n"+
					"-o			Update all outbound links from chosen pages. \n"+
					"			 (If specified alone, updates all outbound links.)" +
					"-f [file]		Update from serialized Java List<String> file.");
			return;
		}
		
		// Once GWPages has been set, execute the following block
		distributor.execute(GWPages, 1000);
		if (options.has("o")) { 		// i.e. -o was set in conjunction with another option
			distributor.execute(monitor.getLinkedResources(GWPages), 1000);
		}
	}
	
}
