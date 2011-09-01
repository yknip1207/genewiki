package edu.scripps.testing;

import org.genewiki.pbb.Configs;
import org.genewiki.pbb.exceptions.ConfigException;

public class ConfigsTest {
	
	public static int failCount = 0;

	public static void main(String[] args) {
		Configs cfg = Configs.GET;
		/* -- Test 1: Initialization -- */
		try {
			cfg.flag("useCache");
			failCount++;
			print("Test 1 failed: initialization failed safety check.");
		} catch (ConfigException e) {
			print("Test 1 passed: initialization check.");
		}
		
		/* -- Test 2: Property file loading -- */
		cfg.setFromFile("bot.properties");
		try {
			if (cfg.str("name").equals("ProteinBoxBot")) {
				print("Test 2.1 passed: String HashMap okay...");
			} else { 
				failCount++; 
				print("Test 2.1 failed: String HashMap not returning correct values.");
			}
			if (!cfg.flag("useCache")) {
				print("Test 2.2 passed: Flag HashMap okay...");
			} else {
				failCount++;
				print("Test 2.2 failed: Boolean flag HashMap not returning correct values.");
			}
		} catch (Exception e) {
			failCount++;
			print("Test 2 failed due to some exception. Sad panda.");
			e.printStackTrace();
		}
		
		if (failCount == 0) {
			print("Congrats! All tests passed!");
		} else {
			print("Failure: not all tests passed.");
		}
		
	}

	public static void print(String str) {
		System.out.println(str);
	}
}
