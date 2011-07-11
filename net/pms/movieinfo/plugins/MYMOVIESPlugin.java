package net.pms.movieinfo.plugins;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

public class MYMOVIESPlugin implements Plugin
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
			title = title.replace("| MYmovies", "");
		}
		return title;
	}
	public String getPlot()
	{
		fs = sb.indexOf("<p style=\"text-align:justify;\">");
		String plot = null;
		if (fs > -1) {
			plot = sb.substring(fs + 31,sb.indexOf("<div",fs+31));
			plot = plot.replaceAll("<a href.*?>","");
			plot = plot.replace("</a>","");
			plot = plot.replace("<br/>","");
			plot = plot.replace("</p>","");
			plot = plot.replace("<em>","");
			plot = plot.replace("</em>","");
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
		return null;
	}
	public String getTagline()
	{
		return null;
	}
	public String getRating()
	{
		fs = sb.indexOf("letter-spacing:1px; margin:0px 11px 7px 11px\">");
		String rating = null;
		if (fs > -1) {
			rating = sb.substring(fs + 46, sb.indexOf("<", fs+46));
			rating = rating.replace(",", ".");
			if (rating.matches("[0-9]\\.[0-9][0-9]{0,1}"))
				rating = Double.parseDouble(rating.trim())*2 + "";
			rating += "/10";
		}
		return rating;
	}
	public String getVideoThumbnail()
	{
		String thumb = null;
		fs = sb.indexOf("div id=\"container\" style=\"overflow:");
		if (fs >- 1)
			fs = sb.indexOf("<img src=\"",fs);
		if (fs > -1) 
			thumb = sb.substring(fs+10, sb.indexOf("\"", fs+10));
		return thumb;
	}
	public ArrayList<String> getCast()
	{
		return castlist;
	}
	public String getTvShow() {return "serie tv";}
	public String getCharSet() {return "8859_1";}
	public String getGoogleSearchSite(){return "mymovies.it/dizionario/recensione.asp";}
	public String getVideoURL(){return "http://www.mymovies.it/###MOVIEID###";}
	public String lookForMovieID(BufferedReader in) {
		try {
			String inputLine, temp;
			StringWriter content = new StringWriter();

			while ((inputLine = in.readLine()) != null)
				content.write(inputLine);

			in.close();
			content.close();
			temp = content.toString();
			int fs = temp.indexOf("http://www.mymovies.it/");
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
//		System.out.println(this.getClass().getSimpleName() + "lookForMovieID Returns " + newURL);
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
