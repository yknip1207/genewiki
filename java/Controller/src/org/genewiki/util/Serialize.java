package org.genewiki.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Provides quick and dirty methods to handle serializing data in
 * and out of a file. No typechecking is done.
 * @author eclarke
 *
 */
public class Serialize {
	
	/**
	 * Writes a serialized object out to the specified filename. 
	 * @param filename
	 * @param object
	 */
	public static void out(String filename, Serializable object) {
		try{
			FileOutputStream fOut = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fOut);
			out.writeObject(object);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads a generic object from a file. It is the client's responsibility
	 * to handle casting it to the appropriate object. An UncheckedCast warning
	 * is almost always raised from this method. A ClassCastException will
	 * be thrown if the object being cast to doesn't match the serialized
	 * object.
	 * @param filename
	 * @return generic object from serialized data
	 * @throws FileNotFoundException
	 */
	public static Object in(String filename) throws FileNotFoundException {
		try {
			FileInputStream fIn = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fIn);
			Object result = in.readObject();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
