package net.pms.movieinfo;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.pms.movieinfo.plugins.Plugin;

public class MovieDBPlugin implements Plugin {

	private String title;
	private String rating;
	private String agerating;
	private String plot;
	private String thumb;
	private String tagline;
	private String genre;
	private String dir;
	private ArrayList<String> castlist;
	private int id;
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(MovieDBPlugin.class);


	public MovieDBPlugin(ResultSet rs) {
		try {
			title = rs.getString("TITLE");
			rating = rs.getString("RATING");
			agerating = rs.getString("AGERATING");
			thumb = rs.getString("THUMB");
			dir = rs.getString("DIRECTOR");
			id = rs.getInt("ID");
			tagline = rs.getString("TAGLINE");
			plot = rs.getString("PLOT");
			castlist = new ArrayList<String>();
		} catch (Exception e) {
		}
	}

	public void addCast(ResultSet rs) {
		try {
			castlist.add(rs.getString("THUMB"));
			castlist.add(rs.getString("CAST"));
			castlist.add(rs.getString("CHAR"));
		} catch (Exception e) {
		}
	}

	public void addGenre(String g) {
		if(genre!=null)
			genre = genre + ", " + g;
		else
			genre = g;
	}

	public int getID() {
		return id;
	}

	@Override
	public String getAgeRating() {
		return agerating;
	}

	@Override
	public ArrayList<String> getCast() {
		return castlist;
	}

	@Override
	public String getCharSet() {
		return null;
	}

	@Override
	public String getDirector() {
		return dir;
	}

	@Override
	public String getGenre() {
		return genre;
	}

	@Override
	public String getGoogleSearchSite() {
		return null;
	}

	@Override
	public String getPlot() {
		return plot;
	}

	@Override
	public String getRating() {
		return rating;
	}

	@Override
	public String getTagline() {
		return tagline;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getTrailerURL() {
		return null;
	}

	@Override
	public String getTvShow() {
		return null;
	}

	@Override
	public String getVideoThumbnail() {
		return thumb;
	}

	@Override
	public String getVideoURL() {
		return null;
	}

	@Override
	public void importFile(BufferedReader in) {
	}

	@Override
	public String lookForMovieID(BufferedReader in) {
		return null;
	}

}
