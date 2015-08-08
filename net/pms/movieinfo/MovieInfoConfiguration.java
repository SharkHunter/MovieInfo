package net.pms.movieinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovieInfoConfiguration {
	private volatile String plugins = "IMDB";
	private volatile int maxNumberOfActors = 20;
	private volatile boolean downloadCover = false;
	private volatile String[] displayInfo = {
		"title", "tagline", "rating", "genre", "director", "cast", "plot", "agerating"
		};
	private volatile int plotLineLength = 60;
	private volatile int cellWrap = 0;
	private volatile boolean showTags = true;
	private volatile String ifLanguage = "en";
	private volatile boolean removeYear = false;
	private volatile String[] filters = {};
	private volatile ArrayList<String> cleanList = new ArrayList<String>();
	private static final Logger LOGGER = LoggerFactory.getLogger(MovieInfoConfiguration.class);

	public MovieInfoConfiguration() {
		readConfiguration();
	}

	public void save() {
		//TODO: Must be implemented if changes are to be saved to file
	}

	private String[] filterStringToStringArray(String value) {
		ArrayList<String> filterList = new ArrayList<String>();
		Pattern filterPattern = Pattern.compile("[\\s\\n]*\"([^\"]+)\"[\\s\\n]*,?");
		Matcher filterMatcher = filterPattern.matcher(value);
		while (filterMatcher.find()) {
			filterList.add(filterMatcher.group(1));
		}
		String[] filters = new String[filterList.size()];
		filters = filterList.toArray(filters);
		return filters;
	}

	private void readConfiguration() {
		File miConf = new File("MOVIEINFO.conf");
		if (!miConf.exists()) {
			miConf = new File("plugins/MOVIEINFO.conf");
		}
		if (miConf.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(miConf), "UTF-8"));
				String line = null;
				cleanList.clear();
				Pattern pattern = Pattern.compile("^\\s*(\\w+)\\s*=\\s*(.*?)\\s*$");
				while ((line = br.readLine()) != null) {
					Matcher match = pattern.matcher(line);
					if (match.find()) {
						String key = match.group(1).toLowerCase();
						String value = match.group(2);
						if (key.equals("plugins")) {
							plugins = value.toUpperCase();
						} else if(key.equals("numberofactors")) {
							try {
								maxNumberOfActors = Integer.parseInt(value);
							} catch (NumberFormatException e) {
								LOGGER.warn("{MovieInfo} Invalid maximum number of actors \"{}\"", value);
							}
						} else if (key.equals("cover")) {
							value = value.toLowerCase();
							downloadCover = value.equals("1") || value.equals("true") || value.equals("yes");
						} else if (key.equals("displayinfo")) {
							displayInfo = value.toLowerCase().split("\\s*,\\s*");
						} else if (key.equals("linelength")) {
							try {
								plotLineLength = Integer.parseInt(value);
							} catch (NumberFormatException e) {
								LOGGER.warn("{MovieInfo} Invalid plot length \"{}\"", value);
							}
						} else if (key.equals("cellwrap")) {
							try {
								cellWrap = (int)(Double.parseDouble(value)*10);
							} catch (Exception e) {
								LOGGER.warn("{MovieInfo} Invalid cellwrap value \"{}\"", value);
							}
						} else if (key.equals("showtags")) {
							value = value.toLowerCase();
							showTags = value.equals("1") || value.equals("true") || value.equals("yes");
						} else if (key.equals("ilanguage")) {
							ifLanguage = value.toLowerCase();
						} else if (key.equals("removeyear")) {
							value = value.toLowerCase();
							removeYear = value.equals("1") || value.equals("true") || value.equals("yes");
						} else if (key.equals("filter")) {
							filters = filterStringToStringArray(value);
						} else if (key.equals("cleandisplay")) {
							Pattern cleanPattern = Pattern.compile("\\s*replace\\s*\\(\"([^\"]*)\"\\)\\.with\\s*\\(\"([^\"]*)\"\\)");
							Matcher cleanMatcher = cleanPattern.matcher(value);
							if (cleanMatcher.find()) {
								cleanList.add(cleanMatcher.group(1));
								cleanList.add(cleanMatcher.group(2));
							}
						}
					}
				}
				br.close();
			} catch (Exception e) {
				LOGGER.info("{MovieInfo} Problems reading MOVIEINFO.conf, using defaults");
				LOGGER.debug("{MovieInfo} Caught Exception while reading MovieInfo configuration: {}", e);
			}
		} else {
			LOGGER.info("{MovieInfo} File MOVIEINFO.conf not found, using defaults");
		}
	}

	public String getPlugins() {
		return plugins;
	}

	public void setPlugins(String value) {
		plugins = value;
		save();
	}

	public String getTopPlugin() {
		if (plugins.indexOf(",") > 0) {
			return plugins.substring(0, plugins.indexOf(",")).trim();
		} else {
			return plugins.trim();
		}
	}

	public boolean getDownloadCover() {
		return downloadCover;
	}

	public void setDownloadCover(boolean value) {
		downloadCover = value;
		save();
	}

	public String[] getDisplayInfo() {
		return displayInfo;
	}

	public String getDisplayInfoAsString() {
		String joinedString = "";
		int delimeters = (displayInfo.length - 1);
		for (String displayEntry : displayInfo) {
			joinedString += (delimeters-- > 0) ? displayEntry + ", " : displayEntry;
		}
		return joinedString;
	}

	public void setDisplayInfo(String[] values) {
		displayInfo = values;
		save();
	}

	public void setDisplayInfoFromString(String value) {
		displayInfo = value.trim().toLowerCase().split("\\s*,\\s*");
		save();
	}

	public int getMaxNumberOfActors() {
		return maxNumberOfActors;
	}

	public void setMaxNumberOfActors(int value) {
		maxNumberOfActors = value;
		save();
	}

	public int getPlotLineLength() {
		return plotLineLength;
	}

	public void setPlotLineLength(int value) {
		plotLineLength = value;
		save();
	}

	public int getCellWrap() {
		return cellWrap;
	}

	public void setCellWrap(int value) {
		cellWrap = value;
		save();
	}

	public boolean getShowTags() {
		return showTags;
	}

	public void setShowTags(boolean value) {
		showTags = value;
		save();
	}

	public String getIFLanguage() {
		return ifLanguage;
	}

	public void setIFLanguage(String value) {
		ifLanguage = value;
		save();
	}

	public boolean getRemoveYear () {
		return removeYear;
	}

	public void setRemoveYear(boolean value) {
		removeYear = value;
		save();
	}

	public String[] getFilters() {
		return filters;
	}

	public String getFiltersAsString() {
		String joinedString = "";
		int delimeters = (filters.length - 1);
		for (String filter : filters) {
			joinedString += (delimeters-- > 0) ? "\"" + filter + "\", " : "\"" + filter + "\"";
		}
		return joinedString;
	}

	public void setFilters(String[] values) {
		filters = values;
		save();
	}

	public void setFiltersFromString(String value) {
		filters = filterStringToStringArray(value);
		save();
	}

	public ArrayList<String> getCleanList() {
		return cleanList;
	}
}
