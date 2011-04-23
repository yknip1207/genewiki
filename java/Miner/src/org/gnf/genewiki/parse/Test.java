package org.gnf.genewiki.parse;

	import java.util.regex.Pattern;
import java.util.regex.Matcher;

	public class Test {
	    public static void main(String[] args){
	        String input = "is [[a]] [[protein]] which in [[drosaphila]] is great";
	        String reg = "\\[\\[.*?\\]\\]";
	    	//  (r'\{\{SWL\|target=(.*?)\|type=(.*?)\}\}
	         Pattern pattern = 
	          Pattern.compile(reg);
	            Matcher matcher = 
	            pattern.matcher(input);

	            boolean found = false;
	            while (matcher.find()) {
	   /*             System.out.println(matcher.groupCount()+" " +
	                		"I found the text \""+matcher.group()+"" +
	                		"\" starting at " +
	                   "index "+matcher.start()+" and ending at index "+matcher.end());
	    */
//	            	String b = ParseUtils.wikiToPlain(before(input, matcher.start(), 15));
//	            	String a = ParseUtils.wikiToPlain(after(input, matcher.end(), 15));
//	            	String link = trimWikiLink(matcher.group());
//	                System.out.println(b+" : "+link+" : "+a);
//	                
//	                found = true;
	            }
	            if(!found){
	                System.out.println("No match found.%n");
	            }
	        }
	    
	    
	    
	    	public static String before(String input, int point, int range){
	    		if(range > point){
	    			range = point;
	    		}
	    		return input.substring(point - range, point);
	    	}
	    	
	    	public static String after(String input, int point, int range){
	    		if(point+range > input.length()){
	    			range = input.length() - point;
	    		}
	    		return input.substring(point, point + range);
	    	}
	    	
	    	public static String trimWikiLink(String input){
	    		return (input.substring(2,input.length()-2));
	    	}
	    }
