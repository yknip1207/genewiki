package org.gnf.genewiki.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import info.bliki.wiki.filter.PlainTextConverter;
//import info.bliki.wiki.model.WikiModel;

public class ParseUtils {

	
	public static String paragraph(String wikitext, int spot){
		//		String reg = "<p>.*?</p>";
		String reg = "}|\n.*?\n";
		Pattern pattern = 
			Pattern.compile(reg);
		Matcher matcher = pattern.matcher(wikitext);

		while (matcher.find()) {
			if(matcher.start()< spot && matcher.end()>spot){
				return matcher.group();
			}
		}
		
		return wikitext;
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

//	public static String wikiToPlain(String input){
//		WikiModel wikiModel = new WikiModel("http://en.wikipedia.com/wiki/${image}", "http://en.wikipedia.com/wiki/${title}");
//        String plainStr = wikiModel.render(new PlainTextConverter(), input);
//        return plainStr;
//	}
//
//	public static String wikiToHtml(String input){
//		WikiModel wikiModel = new WikiModel("http://en.wikipedia.com/wiki/${image}", "http://en.wikipedia.com/wiki/${title}");
//        String plainStr = wikiModel.render(input);
//        return plainStr;
//	}
	
}
