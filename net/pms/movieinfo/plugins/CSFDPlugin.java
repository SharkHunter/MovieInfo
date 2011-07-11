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
		if(sb != null)
		fs = sb.indexOf("<title>");
		String title = null;
		if (fs > -1) {
			title = sb.substring(fs + 7, sb.indexOf("</title>", fs));

			title = title.replace("CSFD.cz - ", "");
		if (title.contains("404 Not Found"))title=null;
		}
		return title;
	}
	public String getPlot()
	{
		fs = sb.indexOf("<div style='float:left;width:425px;padding-top:10px;font-weight:normal'>");
		String plot = null;
		if (fs > -1) {
			plot = sb.substring(fs + 72, sb.indexOf("<b>", fs + 72));
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
		int end = 0;
	    int end2 =-1; 
	    	fs = sb.indexOf("style='color:#000000;font-weight:bold;font-size:12px'>");
	    	if (fs > -1)
			fs = sb.indexOf("<br>   <b> ",fs);
	    	if (fs > -1)
	    	{
			fs = sb.indexOf("b> ", fs) + 3;
			end = sb.indexOf("&nbsp;", fs);
	        end2 = sb.indexOf("<BR>", fs);
	    	}
			String genre = null;
			if (end > end2) return null;
			if (fs > -1) {
				genre = sb.substring(fs, end);
			}
			return genre;
	}
	public String getTagline()
	{
		return null;
	}
	public String getRating()
	{
		fs = sb.indexOf("<td style='padding:10px;text-align:center;font-weight:bold;font-size:36px;color:white;background-color:#");
		
		String rating = null;
		if (fs > -1) {
			rating = sb.substring(fs + 112, sb.indexOf(
					"</", fs+112));
		
		if (rating.trim().matches("[0-9]{0,1}[0-9]\\%"))
		rating = (Double.parseDouble(rating.trim().replace("%", ""))/10) + "";
		
		rating = rating + "/10";
		}
		return rating;
	}
	public String getVideoThumbnail()
	{
		fs = sb.indexOf("<table background=");
		String thumb = null;
		if (fs >-1){
		thumb = sb.substring(fs+19, sb.indexOf("\"",fs+19));	
		}
		return thumb;
	}
	public ArrayList<String> getCast()
	{
		return castlist;
	}
	public String getTvShow() {return "tv seri�l";}
	public String getCharSet() {return "UTF-8";}
	public String getGoogleSearchSite()
	{
		return "csfd.cz/";
	}
	public String getVideoURL()
	{
		return "http://www.csfd.cz/###MOVIEID###/?text=1";
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
			int fs = temp.indexOf("http://www.csfd.cz/film/");
			int end = temp.indexOf("\"", fs+19);
			newURL = null;
			if (fs > -1) {
				newURL = temp.substring(fs + 19, end);
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
