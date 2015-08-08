package net.pms.movieinfo.plugins;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ONFILMPlugin implements Plugin
{
	private int fs=-1;
	private StringBuffer sb;
	private String newURL;
	private static final Logger LOGGER = LoggerFactory.getLogger(ONFILMPlugin.class);

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
			title = title.replace("OnFilm - FilmInfo - ", "");
			LOGGER.trace("{MovieInfo} {}: Parsed title: {}", getClass().getSimpleName(), title);
		}
		return title;
	}
	public String getPlot()
	{
		fs = sb.indexOf("<span id=\"ctl00_BodyContentPlaceHolder_ItemFilmControl_BagsidetekstLabel\" class=\"OnFilmTextNormal\">");
		String plot = null;
		if (fs > -1) {
			plot = sb.substring(fs + 99, sb.indexOf("</span>", fs + 99));
			plot = plot.replace("<br/>","");
			plot = plot.trim();
			LOGGER.trace("{MovieInfo} {}: Parsed plot: {}", getClass().getSimpleName(), plot);
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
		fs = sb.indexOf("ctl00_BodyContentPlaceHolder_ItemFilmControl_FilmBurgernesVurderingControl_Header");
		double count = 0;
		DecimalFormat d = new DecimalFormat("#.##");
		String rating = null;
		if (fs > -1) {
			for(int i = 1; i <= 6;i++){

				fs = sb.indexOf("ctl00_BodyContentPlaceHolder_ItemFilmControl_FilmBurgernesVurderingControl_Header",fs+ 89);
				count += Double.parseDouble(sb.substring(fs + 89, sb.indexOf("%", fs+89)))*i;
			}
			count = count/60;
			count = Double.parseDouble(d.format(count));
		if(count>0) rating = count + "/10";
		}
		return rating;
	}
	public String getVideoThumbnail()
	{
		fs = sb.indexOf("id=\"ctl00_BodyContentPlaceHolder_ItemFilmControl_FilmInfoCover_CoverLink\"><img src=\"");
		String thumb = null;
		if (fs >-1){
		thumb = sb.substring(fs+84, sb.indexOf("\"",fs+84));
		}
		return thumb;
	}
	public ArrayList<CastStruct> getCast()
	{
		return null;
	}
	public String getTvShow() {return "tv-serie";}
	public String getCharSet() {return "UTF-8";}
	public String getGoogleSearchSite()
	{
		return "onfilm.dk/Film";
	}
	public String getVideoURL()
	{
		return "http://www.onfilm.dk/Film###MOVIEID###";
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
			int fs = temp.indexOf("onfilm.dk/Film/");
			int end = temp.indexOf("\"", fs+14);
			newURL = null;
			if (fs > -1) {
				newURL = temp.substring(fs + 14, end);
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
