package org.genewiki.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

public class FileHandler {
	private final static Charset utf8 = Charset.forName("UTF-8");
	private final File root;
	
	public FileHandler() {
		String rootLoc = "";
		try {
			// Returns the actual path to the class in the jarfile
			rootLoc = FileHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] exploded = rootLoc.split("/");
		StringBuffer newfile = new StringBuffer();
		// Pieces together the file up until the jar to give the jar's directory
		for (String folder:exploded) {
			if (!folder.endsWith(".jar"))
				newfile.append(folder+"/");
		}
		root = new File(newfile.toString());
	}
	
	public FileHandler(String root) {
		this.root = new File(root);
	}
	
	public File getRoot() {
		return this.root;
	}
	
	public String getRootLoc() {
		try {
			return this.root.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<String> readIn(String filename) throws FileNotFoundException {
		return (List<String>) Serialize.in(filename);
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
}
