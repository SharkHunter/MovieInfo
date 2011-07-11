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

public class OFDBPlugin implements Plugin
{
	private int fs;
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
		if(sb != null)
		fs = sb.indexOf("<title>");
		String title = null;
		if (fs > -1) {
			title = sb.substring(fs + 7, sb.indexOf("</title>", fs));
			title = title.replace("OFDb - ", "");
			}
		return title;
	}
//	public String getPlot()
//	{
//		fs = sb.indexOf("<b>Inhalt:</b>");
//		String plot = null;
//		if (fs > -1) {
//			plot = sb.substring(fs + 14,sb.indexOf("<",fs+14));
//			plot = plot.trim();
//			System.out.println(this.getClass().getSimpleName() + " " + plot);
//		}
//		return plot;
//	}
	public String getPlot()
	{
		String plot = null;
		fs = sb.indexOf("<a href=\"plot/");
		if (fs > -1) plot = "http://www.ofdb.de/" + sb.substring(fs + 9, sb.indexOf("\"",fs+9));
		if (plot != null)
		{
			String sc = getSubSite(plot);
		fs = sc.indexOf("</b><br><br>");
		if (fs > -1) {
			plot = sc.substring(fs + 12,sc.indexOf("</",fs+12));
			plot = plot.replace("\n", "");
			plot = plot.trim();
//			System.out.println(this.getClass().getSimpleName() + " " + plot);
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
		fs = sb.indexOf("<br>Note:");
		String rating = null;
		if (fs > -1) {
			rating = sb.substring(fs + 9, sb.indexOf("&", fs+9));
		}
		rating = rating.trim()+"/10";
		return rating;
	}
	public String getVideoThumbnail()
	{
		String thumb = null;
		fs = sb.indexOf("<img src=\"http://img.ofdb.de/film/");
		if (fs > -1) 
			thumb = sb.substring(fs+10, sb.indexOf("\"", fs+10));
		return thumb;
	}
	public ArrayList<String> getCast()
	{
		return castlist;
	}
	public String getTvShow() {return "";}
	public String getCharSet() {return "UTF-8";}
	public String getGoogleSearchSite()
	{
		return "ofdb.de/film";
	}
	public String getVideoURL()
	{
		return "http://www.ofdb.de/film###MOVIEID###";
	}
	private String getSubSite(String URL){

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
		URL url = new URL(URL);
		URLConnection conn;
			conn = url.openConnection();
			conn.addRequestProperty("Accept-Encoding","gzip");
		InputStream is = conn.getInputStream();
		InputStream decoder;
        if (conn.getHeaderField("Content-Encoding") != null)
	        if (conn.getHeaderField("Content-Encoding").equals("gzip"))
	        {
	            decoder = new GZIPInputStream( is, 4096 );
	            is = decoder;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("getSubSite: " + URL);
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
			int fs = temp.indexOf("http://www.ofdb.de/film");
			int end = temp.indexOf("\"", fs+23);
			newURL = null;
			if (fs > -1) {
				newURL = temp.substring(fs + 23, end);
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
