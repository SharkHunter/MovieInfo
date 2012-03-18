package net.pms.movieinfo.plugins;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		if (title.contains("Page not found"))title=null;
		}
		return title;
	}
	public String getPlot()
	{
		//fs = sb.indexOf("Plot:</h5>");
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
		String dir = null;
		int end = -1;
		fs = sb.indexOf("\"description\" content=\"");
		if (fs > -1)
		end = sb.indexOf("With", fs+23);
		if (end > -1) {
			dir = sb.substring(fs+23, end);
			fs = dir.lastIndexOf(" by ");				
			if (fs > -1) {
				dir = dir.substring(fs + 4, dir.length());
			}else dir = null;
		}
		return dir;
	}
	public String getGenre()
	{
		int end = 0;
		fs = sb.indexOf("itemprop=\"genre\"");
		fs = sb.indexOf(">", fs) + 1;
	/*	end = sb.indexOf("<a class=", fs) + 2;
		if (end > sb.indexOf("</div", fs))*/
		end = sb.indexOf("</div>", fs);
		String genre = null;
		if (fs > -1 && end > -1) {
			genre = "";
			while (fs < end) {
				genre = genre + sb.substring(fs, sb.indexOf("</a>", fs));
				fs = sb.indexOf(" >", fs) + 2;
				if (fs < end)
					genre = genre + ", ";
			}
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
		fs = sb.indexOf("class=\"cast_list\"");
		int end = sb.indexOf("</table", fs);
		int end1 = 0;
		
		if (fs > -1)
			fs = sb.indexOf("<img", fs) + 4;
			
		if (fs > -1) {
			while (fs < end && fs != 5) {
				// name
				fs=sb.indexOf("alt=\"",fs);
				String n1=sb.substring(fs+5, sb.indexOf("\"", fs+5));
				// image
				fs=sb.indexOf("src=\"",fs);
				castlist.add(sb.substring(fs+5, sb.indexOf("\"", fs+5)));
				castlist.add(n1);
				// character
				fs = sb.indexOf("class=\"character\"", fs + 5);
				fs = sb.indexOf("<div>", fs + 17);
				// work backwards from first close tag because div contents could be either plain text or <a>
				int end2 = sb.indexOf("</", fs + 5);
				end1 = sb.lastIndexOf(">", end2);
				if(end1>0&&end2>0)
					castlist.add(sb.substring(end1+1,end2));
				fs=sb.indexOf("<img",fs)+4;
			}

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
			// TODO Auto-generated catch block
			//System.out.println("lookForImdbID Exception: " + e);
			// e.printStackTrace();
		}
		//System.out.println("lookForImdbID Returns " + newURL);
		return newURL; //To use as ###MOVIEID### in getVideoURL()
	}
	
	public String getTrailerURL() {
		int start=sb.indexOf("a href=\"/video/imdb/");
		if(start<0)
			return null;
		int end=sb.indexOf("\"",start+20);
		String tmp="http://www.imdb.com"+sb.substring(start+8,end)+"html5";
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
			int id=page.indexOf("mp4_h264");
			if(id<0)
				return null;
			id=page.indexOf("\'",id)+1;
			end=page.indexOf("\'",id);
			return page.substring(id,end);
		}
		catch (Exception e) {
			logger.debug("error "+e);
			return null;
		}
	}
}
