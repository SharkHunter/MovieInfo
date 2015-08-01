package net.pms.movieinfo.plugins;
import java.io.BufferedReader;
import java.util.ArrayList;

public abstract interface Plugin {

	public void importFile(BufferedReader in);
	public String getTitle();;
	public String getPlot();
	public String getDirector();
	public String getGenre();
	public String getTagline();
	public String getRating();
	public String getAgeRating();
	public String getVideoThumbnail();
	public ArrayList<CastStruct> getCast();
	public String getGoogleSearchSite();
	public String lookForMovieID(BufferedReader in);
	public String getVideoURL();
	public String getTvShow();
	public String getCharSet();
	public String getTrailerURL();
}
