package net.pms.movieinfo.plugins;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSFDPlugin implements Plugin
{
	private int fs=-1;
	private StringBuffer sb;
	private String newURL;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSFDPlugin.class);

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
			LOGGER.debug("{MovieInfo} {}: Exception during importFile: {}", getClass().getSimpleName(), e);
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
				if (title.contains("404 Not Found")) title = null;
				LOGGER.trace("{MovieInfo} {}: Parsed title: {}", getClass().getSimpleName(), title);
			}
		}
		return title;
	}
	public String getPlot()
	{
		fs = sb.indexOf("<h3>Obsah");
		String plot = null;
		if (fs > -1) {
			plot = sb.substring(sb.indexOf("</span>",fs)+7, sb.indexOf("<span class=\"source", fs));
			plot = plot.trim();
			LOGGER.trace("{MovieInfo} {}: Parsed plot: {}", getClass().getSimpleName(), plot);
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
		if (fs >-1) {
			thumb = sb.substring(fs + 10, sb.indexOf("\" alt=\"poster\"",fs + 10));
			if (thumb.startsWith("//")) {
				thumb = thumb.replaceFirst("//", "http://");
			}
			if (thumb.contains("poster-free.png")) {
				thumb = null;
			}
		}
		return thumb;
	}
	public ArrayList<CastStruct> getCast()
	{
		ArrayList<CastStruct> castlist = new ArrayList<CastStruct>();

		fs = sb.indexOf("<h4>Hrají:</h4>");
		if (fs > -1) {
			fs = sb.indexOf("/\">", fs) + 3;
			int enditem = fs;
			int end = sb.indexOf("</a>	", fs);
			while (enditem != end) {
				CastStruct castEntry = new CastStruct();
				enditem = sb.indexOf("</a>", fs);
				castEntry.Actor = sb.substring(fs, enditem);
				castlist.add(castEntry);
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
			LOGGER.debug("{MovieInfo} {}: Exception during lookForMovieID: {}", getClass().getSimpleName(), e);
		}
		LOGGER.trace("{MovieInfo} {}: lookForMoveiID returns: {}", getClass().getSimpleName(), newURL);
		return newURL; //To use as ###MOVIEID### in getVideoURL()
	}
	@Override
	public String getAgeRating() {
		return null;
	}
	@Override
	public String getTrailerURL() {
		return null;
	}

}
