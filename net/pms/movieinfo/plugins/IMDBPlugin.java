package net.pms.movieinfo.plugins;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.pms.PMS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMDBPlugin implements Plugin
{
	private int fs;
	private StringBuffer sb;
	private ArrayList<String> castlist = new ArrayList<String>();
	private String newURL;
	private static final Logger logger = LoggerFactory.getLogger(IMDBPlugin.class);

	public void importFile(BufferedReader in)
	{
		try {
			BufferedReader br = in;
			sb = new StringBuffer();
			String eachLine = null;
			if(br != null)
			eachLine = br.readLine();

			while (eachLine != null) {
				sb.append(eachLine);
				eachLine = br.readLine();
			}
		} catch (IOException e) {
		}
	}
	public String getTitle() 
	{
		String title = null;
		if (sb!=null){
		fs = sb.indexOf("<title>");
		
		if (fs > -1) {
			title = sb.substring(fs + 7, sb.indexOf("</title>", fs));
		}
		if (title != null)
			if (title.contains("Page not found"))
				title=null;
			else
				title=title.replace("- IMDb", "").trim();
		}
		return title;
	}
	public String getPlot()
	{
		fs = sb.indexOf("Storyline</h2>");
		fs=sb.indexOf("<p>",fs+14);
		String plot = null;
		if (fs > -1) {
			plot = sb.substring(fs + 3, sb.indexOf("<", fs + 3));
		}
		return plot;
	}
	public String getDirector()
	{
		Pattern re=Pattern.compile("\"description\" content=\"Directed by ([^\\.]+)\\.");
		Matcher m=re.matcher(sb.toString());
		if(m.find())
			return m.group(1);
		return null;
	}
	public String getGenre()
	{
		Pattern re = Pattern.compile("itemprop=\"genre\">([^>]+)</span></a>");
		Matcher m=re.matcher(sb.toString());
		String genre ="";
		String sep="";
		while(m.find()) {
			genre = genre + sep + m.group(1);
			sep = ", ";
		}
		return genre;
	}
	public String getTagline()
	{
		fs = sb.indexOf("Taglines:</h4>");
		String tagline = null;
		if (fs > -1) {
			tagline = sb.substring(fs + 14, sb.indexOf("<", fs + 14));
		}
		return tagline;
	}
	@Override
	public String getAgeRating() {
		fs = sb.indexOf("itemprop=\"contentRating\">");
		String agerating = null;
		if (fs > -1) {
			agerating = sb.substring(fs + 25, sb.indexOf("<", fs + 25));
		}
		return agerating;
	}
	public String getRating()
	{
		fs = sb.indexOf("itemprop=\"ratingValue\">");
		String rating = null;
		if (fs > -1) {
			rating = sb.substring(fs+23,sb.indexOf("<", fs+23)) + "/10";
		}
		return rating;
	}
	public String getVideoThumbnail()
	{
		fs = sb.indexOf("id=\"img_primary\"");
		String thumb = null;
		if (fs > -1) {
			fs=sb.indexOf("src=\"",fs+16);
			thumb = sb.substring(fs+5, sb.indexOf("\"", fs+5));
		}
		return thumb;
	}
	public ArrayList<String> getCast()
	{
		Pattern re=Pattern.compile("class=\"cast_list\"(.*?)</table>",
				Pattern.MULTILINE|Pattern.DOTALL);
		Matcher m=re.matcher(sb.toString());
		if(!m.find()) {
			return castlist;
		}
		Pattern re1=Pattern.compile("title=\"([^\"]+)\".*?loadlate=\"([^\"]+)\".*?/character/[^>]+>([^<]+)</a>",
				Pattern.MULTILINE|Pattern.DOTALL);
		Matcher m1=re1.matcher(m.group(1));
		while(m1.find()) {
			String p=m1.group(2);
			p=p.replaceAll("SX32","SX214").replaceAll("32,44_", "214,314_");
			p=p.replaceAll("SY44","SY314");
			castlist.add(p);
			castlist.add(m1.group(1));
			castlist.add(m1.group(3));
		}
		return castlist;
	}
	
	public String getTvShow() {return "tv series";}
	public String getCharSet() {return "UTF-8";}
	public String getGoogleSearchSite()
	{
		return "imdb.com/title";
	}
	public String getVideoURL()
	{
		return "http://www.imdb.com/title/###MOVIEID###/";
	}
	public String lookForMovieID(BufferedReader in) {
		try {
			String inputLine, temp;
			StringWriter content = new StringWriter();

			while ((inputLine = in.readLine()) != null)
				content.write(inputLine);

			in.close();
			content.close();
			temp = content.toString();
			int fs = temp.indexOf("title/tt");
			newURL = null;
			if (fs > -1) {
				newURL = temp.substring(fs + 6, fs + 15);
			}

		} catch (IOException e) {
		}
		return newURL; //To use as ###MOVIEID### in getVideoURL()
	}
	
	public String getTrailerURL() {
		Pattern re=Pattern.compile("href=\"(/video[^\\?]+)\\?[^\"]+\"");
		Matcher m=re.matcher(sb.toString());
		String tmp="";
		if(m.find()) {
			String vid = m.group(1);
			if(!vid.endsWith("/"))
				vid = vid + "/";
			tmp="http://www.imdb.com" + vid + "player?stop=0";
		}
		try {
			URL u=new URL(tmp);
			URLConnection c=u.openConnection();
			c.addRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.0; en-GB; rv:1.9.0.10) Gecko/2009042316 Firefox/3.0.8");
			BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
			String line;
			StringBuilder page=new StringBuilder();
			while((line=in.readLine())!=null) {
				page.append(line);
			}
			in.close();
			String rtmp=findTrailerData("file",page.toString());
			String id=findTrailerData("id",page.toString());
			return rtmp+" playpath="+id+" swfVfy=1 swfUrl=http://www.imdb.com/images/js/app/video/mediaplayer.swf";
		}
		catch (Exception e) {
			logger.debug("error "+e);
			return null;
		}
	}
	
	private String findTrailerData(String field,String page) {
		Pattern re=Pattern.compile("addVariable\\(\""+field+"\"[^\"]+\"([^\"]+)\"\\);");
		Matcher m=re.matcher(page);
		if(m.find())
			return unescape(m.group(1));
		return null;
	}
	
	private String unescape(String str) {
		try {
			logger.debug("unesc "+str);
			return URLDecoder.decode(str,"UTF-8");
		} catch (Exception e) {
		}
		return str;
	}
}
