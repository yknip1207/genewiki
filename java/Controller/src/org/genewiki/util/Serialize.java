package org.genewiki.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Serialize {
	
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
