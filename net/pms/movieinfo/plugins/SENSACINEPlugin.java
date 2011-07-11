package net.pms.movieinfo.plugins;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

public class SENSACINEPlugin implements Plugin
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
			title = sb.substring(fs + 7, sb.indexOf("</title>", fs)-1);
			title = title.replace(" - Sensacine.co", "");
			}
		return title;
	}
	public String getPlot()
	{
		fs = sb.indexOf("<h2 class=\"SpBlocTitle\" >Sinopsis</h2>");
		if (fs > -1)
			fs = sb.indexOf("<div align=\"justify\"><h4>",fs);
		String plot = null;
		if (fs > -1) {
			plot = sb.substring(fs + 25,sb.indexOf("</",fs+25));
			plot = plot.trim();
//			System.out.println(this.getClass().getSimpleName() + " " + plot);
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
		String rating = null;

		return rating;
	}
	public String getVideoThumbnail()
	{
		String thumb = null;
		fs = sb.indexOf("<td valign=\"top\" width=\"120\"><img src=\"");
		if (fs > -1) 
			thumb = sb.substring(fs+39, sb.indexOf("\"", fs+39));
		return thumb;
	}
	public ArrayList<String> getCast()
	{
		return castlist;
	}
	public String getTvShow() {return "";}
	public String getCharSet() {return "8859_1";}
	public String getGoogleSearchSite()
	{
		return "sensacine.com/film";
	}
	public String getVideoURL()
	{
		return "http://www.sensacine.com/film###MOVIEID###";
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
			int fs = temp.indexOf("http://www.sensacine.com/film/fichefilm_gen_cfilm");
			int end = temp.indexOf("\"", fs+29);
			newURL = null;
			if (fs > -1) {
				newURL = temp.substring(fs + 29, end);
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
