package net.pms.movieinfo.plugins;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KINOPOISKPlugin implements Plugin
{
	private int fs=-1;
	private StringBuffer sb;
	private String newURL;
	private static final Logger LOGGER = LoggerFactory.getLogger(KINOPOISKPlugin.class);

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
		if(sb != null)
		fs = sb.indexOf("<title>");
		String title = null;
		if (fs > -1) {
			title = sb.substring(fs + 7, sb.indexOf("</title>", fs));
			//title = title.replace("CSFD.cz - ", "");

			if (title.contains("404 Not Found")) title = null;
			LOGGER.trace("{MovieInfo} {}: Parsed title: {}", getClass().getSimpleName(), title);
		}
		return title;
	}
	public String getPlot()
	{
		fs = sb.indexOf("style=\"padding:10px;padding-left:20px;\" class=\"news\">");
		String plot = null;
		if (fs > -1) {
			plot = sb.substring(fs + 53, sb.indexOf("</td>", fs + 53));
			plot = plot.trim();
		}
		return plot;
	}
	public String getDirector()
	{
		return null;
	}
	public String getGenre()
	{

		fs = sb.indexOf("жанр</td><td class=\"desc-data\">");
		String genre = null;
		if (fs > -1) {
			genre = sb.substring(fs + 31, sb.indexOf("</td>", fs + 31));
			genre = genre.replaceAll("<a href=.*?\"all\">", "").replace("</a>", "").replace(",", "").trim();
		}
		return genre;
	}
	public String getTagline()
	{
		fs = sb.indexOf("слоган</td><td class=\"desc-data\">&laquo;");
		String tagline = null;
		if (fs > -1) {
			tagline = sb.substring(fs + 40, sb.indexOf("&raquo;</td>", fs + 40));
			tagline = tagline.trim();
		}
		return tagline;
	}
	public String getRating()
	{
		fs = sb.indexOf("<div style=\"color:#f60;font:800 23px tahoma, verdana\">");
		if (fs > -1)
			fs = sb.indexOf("class=\"continue\">",fs);

		String rating = null;
		if (fs > -1) {
			rating = sb.substring(fs + 17, sb.indexOf(
					"</", fs+17));

		if (rating.trim().matches("[0-9]\\.[0-9]"))
		rating = (Double.parseDouble(rating.trim())) + "";

		rating = rating + "/10";
		}
		return rating;
	}
	public String getVideoThumbnail()
	{
		fs = sb.indexOf("<img src=\"/images/film/");
		String thumb = null;
		if (fs >-1){
		thumb = sb.substring(fs+10, sb.indexOf("\"",fs+10));
		thumb = "http://www.kinopoisk.ru" + thumb;
		}
		return thumb;
	}
	public ArrayList<CastStruct> getCast()
	{
		return null;
	}
	public String getTvShow() {return "сериал";}
	public String getCharSet() {return "windows-1251";}
	public String getGoogleSearchSite()
	{
		return "kinopoisk.ru/level/1/";
	}
	public String getVideoURL()
	{
		return "http://www.kinopoisk.ru/level/1/film/###MOVIEID###";
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
			int fs = temp.indexOf("http://www.kinopoisk.ru/level/1/film/");
			int end = temp.indexOf("\"", fs+37);
			newURL = null;
			if (fs > -1) {
				newURL = temp.substring(fs + 37, end);
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
