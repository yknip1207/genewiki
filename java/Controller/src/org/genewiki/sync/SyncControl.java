package org.genewiki.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.genewiki.StatusMonitor;

public class SyncControl {

	private final ThreadFactory factory;
	private final ExecutorService manager;
	
	
	public SyncControl() {
		factory = Executors.defaultThreadFactory();
		manager = Executors.newFixedThreadPool(3, factory);
	}
	
	public void execute(List<String> titles, int blocks) {
		int sections = titles.size()/blocks;		//TODO Test me!
		List<Thread> threads = new ArrayList<Thread>();
		for (int i=0; i <= sections; i++) {
			int start = i*blocks;
			int end = ((i+1)*blocks) < titles.size() ?
					(i+1)*blocks : titles.size();
			List<String> sublist;
			sublist = titles.subList(start, end);
			threads.add(factory.newThread(new Sync(sublist)));
			System.out.println("Created new thread from "+(start)+" to "+end);
		}
		for (Thread thread : threads) {
			manager.execute(thread);
			System.out.println("Executing new thread...");
			try {Thread.sleep(20000);} catch (InterruptedException e) {};
		}
		manager.shutdown();
		while (!manager.isTerminated()) {
			try {
				System.err.println("Sync Controller awaiting completion...");
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// nobody really cares
			}
		}
		System.err.println("Updates finished.");
	}
	
	public static void main(String[] args) throws IOException {
		SyncControl control = new SyncControl();
		StatusMonitor monitor = StatusMonitor.instance;
		List<String> GWPages = monitor.getAllGeneWikiPages();
		control.execute(GWPages, 1000);
		control.execute(monitor.getOutboundLinks(GWPages), 1000);
	}
	
}
