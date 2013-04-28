package net.pms.movieinfo.plugins;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

public class CSFDPlugin implements Plugin
{
	private int fs=-1;
	private StringBuffer sb;
	private String newURL;
	private ArrayList<String> castlist = new ArrayList<String>();

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
		if(sb != null) {
			fs = sb.indexOf("<title>");
			if (fs > -1) {
				title = sb.substring(fs + 7, sb.indexOf("</title>", fs));
				title = title.replace(" | ČSFD.cz", "");
			}
			if (title != null)
				if (title.contains("404 Not Found")) title=null;
		}
		return title;
	}
	public String getPlot()
	{
		fs = sb.indexOf("alt=\"Odrážka\"");
		String plot = null;
		if (fs > -1) {
			plot = sb.substring(sb.indexOf(">",fs)+1, sb.indexOf("<span class=\"source", fs + 13));
			
			plot = plot.trim();
		}
		return plot;
	}
	public String getDirector()
	{
		fs = sb.indexOf("<h4>Režie:</h4>");
		String dir = null;
		if (fs > -1) {
			fs = sb.indexOf(">", fs + 46) + 1;
			int enditem = fs;
			int end = sb.indexOf("</a>	", fs);
			dir = "";
			while (enditem != end) {
				enditem = sb.indexOf("</a>", fs);
				dir += sb.substring(fs, enditem);
				if (enditem != end) {
					fs = sb.indexOf(">", enditem + 4) + 1;
					dir += ", ";
				}
			}
			dir = dir.trim();
   		}
		return dir;
	}
	public String getGenre()
	{
		fs = sb.indexOf("<p class=\"genre\">");
		String genre = null;
		if (fs > -1) {
			genre = sb.substring(fs + 17, sb.indexOf("</p>", fs + 17));
   			genre = genre.trim();
		}
		return genre;
	}
	public String getTagline()
	{
		fs = sb.indexOf("<p class=\"origin\">");
		String tagline = null;
		if (fs > -1) {
			tagline = sb.substring(fs + 18, sb.indexOf("</p>", fs + 18));
			tagline = tagline.trim();
		}
		return tagline;
	}
	public String getRating()
	{
		fs = sb.indexOf("<h2 class=\"average\">");
		String rating = null;
		if (fs > -1) {
			rating = sb.substring(fs + 20, sb.indexOf("</h2>", fs + 20));
			if (rating.trim().matches("[0-9]{0,1}[0-9]\\%"))
				rating = (Double.parseDouble(rating.trim().replace("%", ""))/10) + "";
			rating = rating + "/10";
		}
		return rating;
	}
	public String getVideoThumbnail()
	{
		fs = sb.indexOf("<div id=\"poster\" class=\"image\">");
		fs = sb.indexOf("<img src=\"", fs);
		String thumb = null;
		if (fs >-1){
			thumb = sb.substring(fs + 10, sb.indexOf("\" alt=\"poster\"",fs + 10));
			if (thumb.contains("poster-free.png")) {
				thumb = null;
			}
		}
		return thumb;
	}
	public ArrayList<String> getCast()
	{

		fs = sb.indexOf("<h4>Hrají:</h4>");
		if (fs > -1) {
			fs = sb.indexOf("/\">", fs) + 3;
			int enditem = fs;
			int end = sb.indexOf("</a>	", fs);
			while (enditem != end) {
				enditem = sb.indexOf("</a>", fs);
				castlist.add("");
				castlist.add(sb.substring(fs, enditem));
				castlist.add("");
				if (enditem != end) {
					fs = sb.indexOf(">", enditem + 4) + 1;
				}
			}
		}
		return castlist;
	}
	public String getTvShow() {return "TV seriál";}
	public String getCharSet() {return "UTF-8";}
	public String getGoogleSearchSite()
	{
		return "csfd.cz/film/";
	}
	public String getVideoURL()
	{
		return "http://www.csfd.cz/film/###MOVIEID###";
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
			int fs = temp.indexOf("www.csfd.cz/film/");
			int end = temp.indexOf("/", fs + 17);
			newURL = null;
			if (fs > -1) {
				newURL = temp.substring(fs + 17, end + 1);
			}
			if (newURL != null)
			{
				fs = newURL.indexOf("?");
				if (fs > -1)
					newURL = newURL.substring(0,fs);
			}			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//System.out.println("lookForImdbID Exception: " + e);
			// e.printStackTrace();
		}
		//System.out.println(this.getClass().getName() + "lookForMovieID Returns " + newURL);
		return newURL; //To use as ###MOVIEID### in getVideoURL()
	}
	@Override
	public String getAgeRating() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getTrailerURL() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
