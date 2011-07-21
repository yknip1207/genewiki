package edu.scripps.testing;

import java.util.LinkedHashMap;

import org.gnf.pbb.util.ConfigParser;

public class ConfigParserTest {
	
	public static void main(String args[]) {
		LinkedHashMap<String, String> properties = ConfigParser.parseProperties("bot.properties");


		
		for (String key : properties.keySet()) {

/* 		Proof of concept- can convert strings to booleans on the fly */
//			String value = properties.get(key);
//			if (value.equals("true") || value.equals("false")) {
//				Boolean flag = Boolean.valueOf(value);
//				System.out.println(flag);
//			}
		
			System.out.println(properties.get(key));
		}
		
	}
	
}
