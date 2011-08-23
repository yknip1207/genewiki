package org.genewiki.pbb.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.genewiki.pbb.exceptions.ExceptionHandler;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;


public class FileHandler {
	
	private final static Logger log = Logger.getLogger(FileHandler.class.getName());
	private final static Charset utf8 = Charset.forName("UTF-8");
	private ExceptionHandler botState;
	private File root;

	public FileHandler(String root) {
		
		/* -- Test write capabilities to specified location -- */
		File testFile = new File(root, "testfile.dat");
		try {
			Files.createParentDirs(testFile);
			this.root = new File(root);
		} catch (IOException e) { 	// Seems unlikely as long as it's used locally
			log.severe("Could not create directory structure specified.");
			botState.fatal(e);
		}
	}
	
	/**
	 * @return the root path that this FileHandler is using
	 */
	public File getRoot() {
		return this.root;
	}
	
	
	
	/**
	 * Reads a file or path to file (with the FileHandler's root as the parent tree) as
	 * a string and returns it.
	 * @param filename
	 * @return contents of file as string
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String read(String filename) throws FileNotFoundException, IOException {
		File target = new File(root, filename);
		StringBuilder output = new StringBuilder();
		Files.copy(target, utf8, output);
		return output.toString();
	}	
	
	/**
	 * Writes the string to a file. The writeOption can either be 'a' (append) or 'o' (overwrite)
	 * in cases where the file already exists. If the file doesn't exist, it will be created.
	 * @param content
	 * @param filename
	 * @param writeOption
	 * @throws IOException
	 */
	public void write(String content, String filename, char writeOption) throws IOException {
		Preconditions.checkArgument((writeOption == 'a' || writeOption == 'o'), "Invalid write option.");
		File target = new File(root, filename);
		Files.createParentDirs(target);
		if (writeOption == 'a') {
			Files.append(content, target, utf8);
		} else {
			Files.write(content, target, utf8);
		}
	}
	
	/**
	 * Dump a web resource into the specified file (like wget on bash?)
	 * @param url
	 * @param filename
	 */
	public void wget(String url, String filename) {
		
		try {
			InputSupplier<InputStream> instream= Resources.newInputStreamSupplier(new URL(url));
			Files.copy(instream, new File(root, filename));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			botState.recoverable(e);
		} catch (IOException e1) {
			e1.printStackTrace();
			botState.fatal(e1);
		}
		
	}
}
