package org.genewiki.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * A Distributor handles "easily parallelizable" tasks, such 
 * as processing lists, by splitting the task list up into chunks
 * and spawning worker threads for each block. It's up to the 
 * client to ensure things are thread-safe.
 * @author eclarke
 *
 * @param <K> The type of task (i.e. if processing a list of titles,
 * K would be of type String.)
 */
public class Distributor<K> {

	private final ThreadFactory factory;
	private final ExecutorService manager;
	private final RunnableFactory generator;
	
	/**
	 * Creates a new Distributor object with the capability to 
	 * launch the specified number of threads, using the provided
	 * Runnable generator.
	 * @param threads number of threads that can be simultaneously active
	 * @param generator provides a pre-made Runnable. 
	 */
	public Distributor(int threads, RunnableFactory generator) {
		factory = Executors.defaultThreadFactory();
		manager = Executors.newFixedThreadPool(threads);
		this.generator = generator;
	}
	
	/**
	 * Divides the specified tasks into the block size specified,
	 * then spawns worker threads that execute each chunk of tasks.
	 * The maximum active workers is determined during construction
	 * of the Distributor object.
	 * @param tasks
	 * @param blocks
	 */
	public void execute(List<K> tasks, int blocks) {
		
		int max = tasks.size()/blocks;
		List<Thread> threads = new ArrayList<Thread>();
		HashMap<Integer, Future<?>> results = new HashMap<Integer, Future<?>>();
		for (int i=0; i <= max; i++) {
			int start = i*blocks;
			int end = (i+1)*blocks < tasks.size()
					? (i+1)*blocks
					: tasks.size();
			List<K> sublist;
			sublist = tasks.subList(start, end);
			threads.add(factory.newThread(generator.newRunnable(sublist, i)));
			println("Created new thread for tasks "+start+" to "+end);
		}
		
		int i = 0;
		for (Thread thread : threads) {
			results.put(i, manager.submit(thread));
			println("Adding new thread to execution queue.");
			i++;
		}
		
		
		while (!manager.isShutdown()) {
			boolean isDone = true; // assert that we're done
			for (Integer key : results.keySet()) {
				if (results.get(key).isDone()) {
					try { 
						results.get(key).get();
						// If we get here, this thread finished fine, don't correct assertion
					} catch (ExecutionException e) {
						isDone = false; // Thread failed, correct assertion
						errln("Resubmitting failed thread #"+key+"...");
						results.put(key, manager.submit(threads.get(key)));
					} catch (InterruptedException e) {}
				} else {
					isDone = false;	// We're not done, correct assertion
					continue;
				}
				// Has the assertion been challenged?
				if (isDone) { // nope, we're done
					manager.shutdown();
				} else { // yes- continue operation
					println("Working...");
				}
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e){
				errln("Attempting orderly shutdown...");
				manager.shutdownNow();
			}
		}
		
		println("Tasks completed.");
	}
	
	/* ---- PRIVATE METHODS ---- */
	private void println(String str) {
		System.out.println(str);
	}
	
	private void errln(String str) {
		System.err.println(str);
	}
}
