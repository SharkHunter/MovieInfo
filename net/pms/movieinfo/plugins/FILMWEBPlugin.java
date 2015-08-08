package net.pms.movieinfo.plugins;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FILMWEBPlugin implements Plugin
{
	private int fs;
	private StringBuffer sb;
	private String newURL;
	private static final Logger LOGGER = LoggerFactory.getLogger(FILMWEBPlugin.class);

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
			title = title.replace(" - FILMWEB.pl", "");
			if (title.contains("Nie znaleziono strony") || title.contains("film�w!")) {
				title = null;
			}
			LOGGER.trace("{MovieInfo} {}: Parsed title: {}", getClass().getSimpleName(), title);
		}
		return title;
	}

	/*public String getPlot()
	{
		fs = sb.indexOf("o-filmie-header");
		if (fs > -1)
		fs = sb.indexOf("<p>",fs);
		int end = sb.indexOf("</p>",fs);
		if(end > sb.indexOf("... <a", fs + 3) && sb.indexOf("... <a", fs + 3) > -1)
			end = sb.indexOf("... <a", fs + 3);
		String plot = null;
		if (fs > -1 && end > -1) {

			plot = sb.substring(fs + 3, end);
			plot = plot.trim();
			LOGGER.trace("{MovieInfo} {}: Parsed plot: {}", getClass().getSimpleName(), plot);
		}
		System.out.println(getSubSite("http://www.filmweb.pl/f295316/Underworld+Bunt+Lykan�w,2009/opisy"));
		return plot;
	}*/
	public String getPlot()
	{
		String plot = null;
		fs = sb.indexOf("- opisy - FILMWEB.pl\" href=\"");
		if (fs > -1) {
			plot = sb.substring(fs + 28, sb.indexOf("\"", fs + 28));
		}
		if (plot != null) {
			String sc = getSubSite(plot);
			fs = sc.indexOf("text-align:justify\">");
			int end = sc.indexOf("</p>", fs);
			if (fs > -1 && end > -1) {
				plot = sc.substring(fs + 20, end);
				plot = plot.trim();
				plot = plot.replaceAll("<a class=\'internal\'.*?>", "");
				plot = plot.replace("</a>", "");
				plot = plot.replace("<br/>", "");
				if(plot.startsWith("http"))plot=null;
				LOGGER.trace("{MovieInfo} {}: Parsed plot: {}", getClass().getSimpleName(), plot);
			}
		}
		return plot;
	}
	public String getDirector()
	{

		return null;
	}
	public String getGenre()
	{
		return null;
	}
	public String getTagline()
	{
		return null;
	}
	public String getRating()
	{
		fs = sb.indexOf("class=\"film-rating-fill\"");
		if (fs > -1)
		fs = sb.indexOf("class=\"value\">",fs+24);
		String rating = null;
		if (fs > -1) {
			rating = sb.substring(fs + 14, sb.indexOf("</", fs));
			rating += "/10";
			rating = rating.replace(",", ".");
		}
		return rating;
	}
	public String getVideoThumbnail()
	{
		String thumb = null;
		fs = sb.indexOf("class=\"film-poster");
		if (fs >- 1)
			fs = sb.indexOf("<img src=\"",fs);
		if (fs > -1)
			thumb = sb.substring(fs+10, sb.indexOf("\"", fs+10));
		return thumb;
	}
	public ArrayList<CastStruct> getCast()
	{
		return null;
	}
	public String getTvShow() {return "serial";}
	public String getCharSet() {return "UTF-8";}
	public String getGoogleSearchSite()
	{
		return "filmweb.pl/";
	}
	public String getVideoURL()
	{
		return "http://###MOVIEID###";
	}
	private String getSubSite(String URL){

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
		URL url = new URL(URL);
		URLConnection conn;
			conn = url.openConnection();
			if(url.getHost()=="www.filmweb.pl")conn.setRequestProperty("Host", "");
		InputStream is = conn.getInputStream();
		String cookie =conn.getHeaderField("Set-Cookie");
		if (cookie.contains("welcomeScreen=")) {
			URLConnection conn2 = url.openConnection();
			conn2.addRequestProperty("Accept-Encoding","gzip");
			conn2.setRequestProperty("Cookie", cookie);
			is = conn2.getInputStream();
	        InputStream decoder;
	        if (conn2.getHeaderField("Content-Encoding") != null)
	        if (conn2.getHeaderField("Content-Encoding").equals("gzip"))
	        {
	            decoder = new GZIPInputStream( is, 4096 );
	            is = decoder;
	        }

		}
		int n = -1;
		byte buffer [] = new byte [4096];
		while( (n=is.read(buffer))> -1) {
			bout.write(buffer, 0, n);
		}
		URL = null;
		URL = bout.toString(getCharSet());
		is.close();
		} catch (IOException e) {
			LOGGER.debug("{MovieInfo} {}: Exception during getSubSite: {}", getClass().getSimpleName(), e);
		}
		LOGGER.trace("{MovieInfo} {}: getSubSite returns: {}", getClass().getSimpleName(), URL);
		return URL;
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
			int fs = temp.indexOf("<h3 class=r><a href=\"http://");
			if (fs > -1) {
				newURL = temp.substring(fs+28,temp.indexOf("\"",fs+28));
				while(!newURL.matches(".*filmweb.pl/$") && !newURL.matches(".*filmweb.pl/f[0-9].*"))
				{
					fs = temp.indexOf("<h3 class=r><a href=\"http://",fs +28);
					if(fs > -1)	newURL = temp.substring(fs+28,temp.indexOf("\"",fs+28));
					else {newURL = null;break;}
				}

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
