/* Class by Jaqb
 *
 */
package net.pms.movieinfo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.WebVideoStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.movieinfo.plugins.CastStruct;
import net.pms.movieinfo.plugins.Plugin;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileMovieInfoVirtualFolder extends VirtualFolder {

	private final boolean MOVIEWEBSITE = false;
	private String title = null;
	private String rating;
	private String agerating;
	private String plot;
	private String thumb;
	private String tagline;
	private String pluginUrl;
	private String genre;
	private String director;
	private boolean resolved;
	private boolean isDVD = false;
	private ArrayList<CastStruct> castlist = new ArrayList<CastStruct>();
	private int numberOfActors;
	private int plotLineLength;
	private String className = null;
	private String pluginTv;
	private String pluginCharset;
	private String nfoId;
	private String trailer;
	private DLNAResource origRes;
	private String hash;
	private Plugin plugin;
	private String thumbfolder = "";
	protected MovieInfoConfiguration config = MovieInfo.configuration();
	private static final Logger LOGGER = LoggerFactory.getLogger(FileMovieInfoVirtualFolder.class);

	public FileMovieInfoVirtualFolder(String name, String thumbnailIcon, boolean copy) {
		super(name, thumbnailIcon);
	}

	@Override
	public void resolve() {
		if (!resolved) {
			if (plugin != null) {
				transferData(plugin);
			} else {
				gather();
			}
			display();
		resolved = true;
		}
	}

	public void setPlugin(Plugin p) {
		plugin = p;
	}

	public void gather() {
		final boolean GOOGLE = true;
		if(nfoId == null)
			nfoId=MovieInfo.extractImdb(origRes);

		if (!resolved && getChildren().size() == 0) {
			String name = getName();
			if (getParent() != null) {
				if (getParent().getParent().getParent().getName().contains("[DVD ISO]"))
					isDVD = true;
				name = getParent().getName();
			}
			name = name.replaceAll("\\..{2,4}$", "");



			if (className != null)
				className = className.replace(" INFO", "");
			String classNameIMDB = "net.pms.movieinfo.plugins.IMDBPlugin";
			String classNamePlugin = "net.pms.movieinfo.plugins." +className+ "Plugin";
			Plugin imdb = null;
			Plugin plugin = null;

			try {
				imdb = (Plugin)(Class.forName(classNameIMDB).newInstance());
				plugin = (Plugin)(Class.forName(classNamePlugin).newInstance());

			} catch (InstantiationException e) {
				LOGGER.debug("{MovieInfo} Excepton during gathering: {}", e);
			} catch (IllegalAccessException e) {
				LOGGER.debug("{MovieInfo} Excepton during gathering: {}", e);
			} catch (ClassNotFoundException e) {
				LOGGER.debug("{MovieInfo} Excepton during gathering: {}", e);
			}
			pluginTv = plugin.getTvShow();
			pluginUrl = plugin.getGoogleSearchSite();
			pluginCharset = plugin.getCharSet();

			String nfo = nfoId;
			if(nfo.length() == 0)
				nfo = imdbIDFromNfo(name);
			if (nfo != null) {
				if (nfo.matches("tt[0-9]{7}")) {
					if (!className.equals("IMDB")) {
						imdb.importFile(getWebsite(nfo, imdb.getVideoURL(),	MOVIEWEBSITE, null));
						if ((nfo = plugin.lookForMovieID(getWebsite(imdb.getTitle(), null,GOOGLE, plugin.getGoogleSearchSite()))) != null)
							plugin.importFile(getWebsite(nfo, plugin.getVideoURL(), MOVIEWEBSITE, null));
					} else
						plugin.importFile(getWebsite(nfo, plugin.getVideoURL(),	MOVIEWEBSITE, null));
				} else {
					plugin.importFile(getWebsite(nfo, plugin.getVideoURL(),MOVIEWEBSITE, null));
				}
			} else {
				String moviename = "";
				if (!isDVD)
					moviename = getMovienameFromFilename(getParent().getName());
				else
					moviename = getMovienameFromFilename(getParent().getParent().getParent().getDisplayName().replace("[DVD ISO] ", ""));
				if (!moviename.equals(""))
					nfo = plugin.lookForMovieID(getWebsite(moviename, null, GOOGLE, plugin.getGoogleSearchSite()));
				if (nfo != null) {
					plugin.importFile(getWebsite(nfo, plugin.getVideoURL(), MOVIEWEBSITE, null));
				} else {
					if (!isDVD)
						moviename = getMovienameFromFilename(getMovienameFromFilename(getParent().getParent().getParent().getName()));
					else
						moviename = getMovienameFromFilename(getParent().getParent().getParent().getParent().getName());

					if (!moviename.equals(""))
						nfo = plugin.lookForMovieID(getWebsite(moviename, null, GOOGLE, plugin.getGoogleSearchSite()));
					if (nfo != null) {
						plugin.importFile(getWebsite(nfo, plugin.getVideoURL(), MOVIEWEBSITE, null));
					}
				}
			}
			if (nfo != null) {
				transferData(plugin);
				MovieDB.add(origRes, nfoId,
						genre, title, rating,
						director,agerating,castlist,
						thumb,hash,plot,tagline);
			}
		}
	}

	private void transferData(Plugin plugin) {
		title = plugin.getTitle();
		thumb = plugin.getVideoThumbnail();
		trailer = plugin.getTrailerURL();
		rating = plugin.getRating();
		genre = plugin.getGenre();
		plot = plugin.getPlot();
		director = plugin.getDirector();
		castlist = plugin.getCast();
		tagline = plugin.getTagline();
		agerating = plugin.getAgeRating();
	}

	public void setHash(String h) {
		hash=h;
	}

	private void display() {
		for (String displayEntry : MovieInfo.configuration().getDisplayInfo()) {
			switch (displayEntry.toLowerCase()) {
				case "title":
					displayTitle();
					break;
				case "tagline":
					displayTagline();
					break;
				case "rating":
					displayRating();
					break;
				case "genre":
					displayGenre();
					break;
				case "director":
					displayDirector();
					break;
				case "cast":
					displayCast();
					break;
				case "plot":
					displayPlot();
					break;
				case "agerating":
					displayAgeRating();
					break;
			}
		}
		nullDisplayVariables();
	}

	private void nullDisplayVariables() {
		title = null;
		trailer = null;
		thumbfolder = "";
		rating = null;
		genre = null;
		tagline = null;
		agerating = null;
		director = null;
		plot = null;
		castlist = null;
	}

	private void displayTitle() {
		if (title != null) {
			title = clean(title);
			MovieInfoVirtualFolder folder;
			if (config.getCellWrap() == 0) {
				folder = new MovieInfoVirtualFolder((config.getShowTags() ? "Title: " : "") + title, thumb);
			} else {
				folder = new MovieInfoVirtualFolder(wrapKerned(title.toUpperCase(), 1.2), thumb);
			}
			if (thumb != null)
				folder.addChild(new MovieInfoVirtualData(title, thumb.replaceAll("S[X,Y][0-9]{2,3}_S[X,Y][0-9]{2,3}_", "SX300_SY300_")));
			if (!isDVD && className.equals(MovieInfo.configuration().getTopPlugin())) {
				File f = new File((StringUtils.isNotBlank(thumbfolder) ? thumbfolder : getParent().getParent().getParent().getSystemName()) + File.separator + getParent().getName() + ".cover.jpg");
				if (MovieInfo.configuration().getDownloadCover()) {
					saveCover(f);
				}
			}
			if (className.equals("IMDB")) {
				if(trailer != null)
					addChild(new WebVideoStream(title+" - Trailer",trailer,thumb));
			}
			addChild(folder);
		}
	}

	public void saveCover(File f) {
		saveCover(f,thumb);
	}

	public void saveCover(File f,String tUrl) {
		if (!f.exists() && f.isAbsolute()) {
			URL url = null;
			if(tUrl != null && tUrl.matches("S[X,Y][0-9]{2,3}_S[X,Y][0-9]{2,3}_"))
				tUrl = tUrl.replaceAll("S[X,Y][0-9]{2,3}_S[X,Y][0-9]{2,3}_","SX300_SY300_");
			if(tUrl != null) {
				try {
					url = new URL(tUrl);
					ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					URLConnection conn;
					conn = url.openConnection();
					InputStream in = conn.getInputStream();
					FileOutputStream fOUT = null;
					if (f != null) {
						fOUT = new FileOutputStream(f);
						byte buf[] = new byte[4096];
						int n = -1;
						while ((n = in.read(buf)) > -1) {
							bytes.write(buf, 0, n);
							if (fOUT != null)
								fOUT.write(buf, 0, n);
						}
						in.close();
						if (fOUT != null)
							fOUT.close();
					}
				} catch (IOException e) {
				}
			}
		}
	}

	private void displayRating() {
		if (rating != null && rating.matches("[0-9][\\.,\\,][0-9]*?/10.*?")) {
			rating = clean(rating);
			int star = Math.round(Float.parseFloat(rating.substring(0, rating.indexOf("/"))));
			String stars = "";
			for (int i = 0; i < star; i++)
				stars = stars + (char) 9733;
			for (int i = 0; i < 10 - star; i++)
				stars = stars + (char) 9734;
			addInfo("Rating: ", stars + " " + rating);
		}
	}

	private void displayGenre() {
		if (genre != null && genre != " ") {
			genre = clean(genre);
			addInfo("Genre: ", genre);
		}
	}

	private void displayTagline() {
		if (tagline != null) {
			tagline = clean(tagline);
			addInfo("Tagline: ", tagline);
		}
	}

	private void displayAgeRating() {
		if (agerating != null) {
			agerating = clean(agerating);
			addInfo("Age Rating: ", agerating);
		}
	}

	private void displayDirector() {
		if (director != null) {
			director = clean(director);
			director = director.replaceAll("\\.\\s*$", "");
			addInfo("Director: ", director);
		}
	}

	private void displayPlot() {
		String plotLine = "", label = config.getShowTags() ? "Plot: " : "";
		int start = 0;
		if (plot != null) {
			plot = clean(plot);
			LOGGER.trace("{MovieInfo} Cleaned plot: {}", plot);
			if (config.getCellWrap() != 0) {
				addInfo("Plot: ", plot);
			} else {
				for (int i = 0; i <= plot.length(); i += plotLineLength ) {

					if (i + plotLineLength <= plot.length() && plot.contains(" ")) {
						plotLine = plot.substring(start, plot.lastIndexOf(" ",	i + plotLineLength));
						start = plot.lastIndexOf(" ", i + plotLineLength);
					} else if (i + plotLineLength > plot.length()) {
						plotLine = plot.substring(start, plot.length());
					}
					if (plotLine.length() > 1) {
						this.addChild(new MovieInfoVirtualFolder(label + plotLine, null));
						LOGGER.trace("{MovieInfo} Added plot line: {}{}", label, plotLine);
					} else {
						LOGGER.trace("{MovieInfo} Did not add empty plot line");
					}
					label = config.getShowTags() ? "____ " : "";
				}
			}
		}
	}

	private void displayCast() {
		if (castlist != null && !castlist.isEmpty()) {
			String temp = config.getShowTags() ? "Cast: " : "";
			int a = 0;
			while (!castlist.isEmpty() && a++ < numberOfActors) {
				CastStruct castEntry = castlist.remove(0);
				String label = (config.getCellWrap() != 0 ? "" : temp);
				if (castEntry.Actor != null) {
					label += clean(castEntry.Actor);
				}
				if (castEntry.Character != null) {
					label += " as " + castEntry.Character;
				}
				MovieInfoVirtualFolder folder = new MovieInfoVirtualFolder((config.getCellWrap() != 0 ? wrap(label) : label), castEntry.Picture);
				if (castEntry.Actor != null && castEntry.Picture != null) {
					folder.addChild(new MovieInfoVirtualData(castEntry.Actor,castEntry.Picture));
				}
				addChild(folder);
				temp = config.getShowTags() ? "____   " : "";
			}
		}
	}

	private void addInfo(String tag, String s) {
		if(StringUtils.isEmpty(s)) {
			return;
		}
		if (config.getShowTags()) {
			s = tag + s;
		}
		if (config.getCellWrap() > 0) {
			s = wrap(s);
			addChild(new MovieInfoVirtualFolder(s, null));
			int blanks = ((s.replaceAll("[^\n]", "").length() * 10 + 5) / config.getCellWrap());
//			int blanks = (int)(((double)s.replaceAll("[^\n]", "").length() + 0.5) / cellwrap);
//			int blanks = (s.replaceAll("[^\n]", "").length() + cellwrap - 1) / cellwrap - 1;
			for (int i = 0; i < blanks; i++)
				addChild(new MovieInfoVirtualFolder("", null));
		} else
			addChild(new MovieInfoVirtualFolder(s, null));
	}

	private String wrapKerned(String s, double factor) {
		int len = (int)(plotLineLength / factor);
		return s.length() > len ? WordUtils.wrap(s, len, null, true) : s;
	}

	private String wrap(String s) {
		return s.length() > plotLineLength ? WordUtils.wrap(s, plotLineLength, null, true) : s;
	}

	private String imdbIDFromNfo(String movieName) {
		String newURL = null;
		try {
			BufferedReader in = null;
			FileReader fr = null;
			File f = null;

			if (!isDVD)
			f = new File((getParent().getParent().getParent().getSystemName() + "\\" + getParent().getName()).replaceAll("\\..{2,4}$", ".nfo"));
			else
				f = new File((getParent().getParent().getParent().getParent().getSystemName() + "\\" + getParent().getParent().getParent().getName()).replace("[DVD ISO] ", "").replaceAll("\\{.*\\}","").replaceAll("\\..{2,4}$", "") + ".nfo");
			if (f.exists()) {
				fr = new FileReader(f);
				in = new BufferedReader(fr);
				newURL = lookForImdbID(in);
			}
/*			if (newURL == null && !isDVD) {
				movieName = movieName.replaceAll(" ", "+");
				LOGGER.trace("{MovieInfo} Movie name for nfo search: {}", movieName);
				URL url = new URL("http://www.newzleech.com/?group=&minage=&age=&min=min&max=max&q=" + movieName + "&m=search&adv=1");
				URLConnection yc = url.openConnection();
				in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
				String inputLine;
				StringWriter content = new StringWriter();
				while ((inputLine = in.readLine()) != null)
					content.write(inputLine);
				in.close();
				String temp = content.toString();
				content.close();
				int fs = temp.indexOf("nfo.php");
				newURL = null;
				if (fs > -1) {
					newURL = temp.substring(fs, temp.indexOf(">", fs) - 1);
					url = new URL("http://www.newzleech.com/" + newURL);
					yc = url.openConnection();
					in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
					newURL = lookForImdbID(in);
					LOGGER.trace("{MovieInfo} Url = {}", url);
				}
			} */
		} catch (Exception e) {
		}
		return newURL;
	}

	private String lookForImdbID(BufferedReader in) {
		String newURL = null;
		try {
			String inputLine, temp;
			StringWriter content = new StringWriter();

			while ((inputLine = in.readLine()) != null) {
				content.write(inputLine);
				content.write(" ");
			}
			in.close();
			content.close();
			temp = content.toString();
			int fs = temp.indexOf(pluginUrl);
			if (fs == -1 || pluginUrl.contains("imdb.com")) {
				fs = temp.indexOf("title/tt");
				if (fs > -1)
				newURL = temp.substring(fs + 6, fs + 15);
			} else {
				if (fs > -1) {
					newURL = temp.substring(fs + pluginUrl.length(),temp.indexOf(" ", fs + pluginUrl.length()));
				}
				fs = temp.indexOf(pluginUrl);
			}
		} catch (IOException e) {
		}
		return newURL;
	}

	/*private String[] getYoutubeTrailer(BufferedReader in, boolean youtube) {
		String[] yt = new String[3];
		try {
			String inputLine, temp;
			StringWriter content = new StringWriter();
			while ((inputLine = in.readLine()) != null) {
				content.write(inputLine);
				content.write(" ");
			}
			in.close();
			content.close();
			temp = content.toString();
			int fs = temp.indexOf("watch?");
			newURL = null;
			if (fs > -1) {
				yt[0] = "http://www.youtube.com/" + temp.substring(fs,temp.indexOf("\"", fs));
				if(youtube) {
					fs = temp.indexOf("title=",fs);
					yt[1] = temp.substring(fs + 7,temp.indexOf("\"", fs + 7));
				} else {
					fs = temp.indexOf("\">",fs);
					yt[1] = temp.substring(fs + 2,temp.indexOf("</a", fs + 2)).replaceAll("<.*?>", "").replace("YouTube - ", "");
				}
			}
			yt[2] = temp.indexOf("isHDAvailable = true") +"";
		} catch (IOException e) {
		}
		return yt;
	}*/

	private BufferedReader getWebsite(String mname, String url, final boolean google, String searchURL) {
		BufferedReader in = null;
		URL u = null;
		String host = null;
		String file = null;
		try {
			if (!google) {
				if (url.contains("###MOVIEID###")) {
					if (url.contains("youtube.com"))
						mname = mname.replace(" ", "+");
					url = url.replace("###MOVIEID###", mname);
				}
				u = new URL(url);
				//u = new URL("http://www.imdb.com/title/" + key + "/");
				LOGGER.debug("{MovieInfo} Resolved link for gathering data: " + u);
			} else {
				mname = mname.replace(" ", "+");
				u = new URL(("http://www.google.com/search?hl=" + MovieInfo.configuration().getIFLanguage() + "&q=" + mname + "+site%3A" + searchURL));
				LOGGER.debug("{MovieInfo} SEARCH link sent to GOOGLE: " + u);
			}
			host = u.getHost();
			file = u.toString().replace("http://" + u.getHost(), "");
		} catch (MalformedURLException e) {
			LOGGER.debug("{MovieInfo} Malformed Exception in getWebSite: {}", e);
			return null;
		}

		URLConnection conn;
		InputStream is = null;
		try {
			conn = u.openConnection();
			conn.setRequestProperty("GET",file + " HTTP/1.0");
			conn.addRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.0; en-GB; rv:1.9.0.10) Gecko/2009042316 Firefox/3.0.8");
			conn.addRequestProperty("Connection","close");
			conn.addRequestProperty("Accept","text/*");
			conn.addRequestProperty("Accept-Encoding","gzip");
			if (!host.contains("www.filmweb.pl"))
				conn.setRequestProperty("Host",host);
			else
				conn.setRequestProperty("Host", "");
			is = conn.getInputStream();
			InputStream decoder;
			if (conn.getHeaderField("Content-Encoding") != null) {
				if (conn.getHeaderField("Content-Encoding").equals("gzip")) {
					decoder = new GZIPInputStream(is, 4096);
					is = decoder;
				}
			}
		} catch (IOException e) {
			Pattern p = Pattern.compile("Server returned HTTP response code: (\\d+)");
			Matcher m = p.matcher(e.getMessage());
			if (m.find()) {
				// HTTP response code
				switch (m.group(1)) {
					case "503":
						// Google gives 503 and presents a captcha if too many queries come too fast
						if (google) {
							LOGGER.warn("{MovieInfo} Google refused query with HTTP response 503. Check if google.com is operational.");
						} else {
							LOGGER.debug("{MovieInfo} The web server refused query with 503 \"Service Unavailable\"");
						}
						break;
					default:
						LOGGER.debug("{MovieInfo} The webserver unexpectedly replied with HTTP resonse code {}", m.group(1));;
				}
			} else {
				LOGGER.debug("{MovieInfo} IOException in getWebSite: {}", e);
			}
			return null;
		}
/*			String hName;
			cookie=null;
			for (int j=1; (hName = conn.getHeaderFieldKey(j))!=null; j++) {
				String cStr=conn.getHeaderField(j);
				if (!hName.equalsIgnoreCase("Set-Cookie"))
					continue;
				String[] fields=cStr.split(";\\s*");
				String cookie1=fields[0];
				int pos;
				if((pos=cookie1.indexOf(";"))!=-1)
					cookie1 = cookie1.substring(0, pos);
				cookie=(cookie==null?"":cookie+"; ")+cookie1;
			}
	        if(cookie!=null&&cookie.length()!=0)
	        	cookie="Cookie: "+cookie+"!";
*/
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			int n = -1;
/*			if(cookie!=null&&cookie.length()!=0)
				bout.write(cookie.getBytes(), 0, cookie.length());
*/
			byte buffer [] = new byte [4096]; //8192
			while( (n=is.read(buffer))> -1) {
				bout.write(buffer, 0, n);
			}
			in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bout.toByteArray()), pluginCharset));
		} catch (UnsupportedEncodingException e) {
			LOGGER.debug("{MovieInfo} BufferedReader caught UnsupportedEncodingException: {}", e);
			return null;
		} catch (IOException e) {
			LOGGER.debug("{MovieInfo} BufferedReader caught IOException: {}", e);
			return null;
		}
		return in;
	}

	protected String getMovienameFromFilename(String displayname) {
		String h = displayname;
//		h = h.split("\\.")[0]; // If not commented, all parts of moviename after first dot are removed!
		h = h.replaceAll("DISC[0-9]{1,2}", "");
		h = h.replaceAll("D[0-9]{1,2}", "");
		h = h.replaceAll("\\..{2,4}$", "");
		h = h.replaceAll("1080[pP]", "");
		h = h.replaceAll("720[pP]", "");
		h = h.replaceAll("VIDEO_TS", "");
		Pattern a;
		String[] pat = { ".[Ss][0-9]{1,2}[Ee][0-9]{2}", // Tv show with episode s01e01
				"[^aA0-zZ9][0-9]{3}[^aA0-zZ9]" }; // TV show with episode 313
		String f = null;
		for (int i = 0; i < pat.length; i++) {
			a = Pattern.compile(pat[i]);
			Matcher ma = a.matcher(h);

			if (ma.find()) {
				f = h.substring(0, ma.start());
				f = f + " " + pluginTv;
				h = f;
			}
		}
		if (MovieInfo.configuration().getRemoveYear())
			h = h.replaceAll("[^0-9][\\(,\\[]{0,1}[0,1,2][0,9][0-9]{2}[\\),\\]]{0,1}","");	// Remove year from searches
		h = " " + h + " ";
		h = h.replaceAll("[^\\p{N}\\p{L}]", " ");	// Don't remove all chars of any language so diacritic will NOT be also removed!
//		h = h.replaceAll("[^aA0-zZ9]", " ");		// It doesn't remove ASCII characters only so special chars like diacritic are removed!
//		h = h.replaceAll("\\p{Punct}", " ");		// Dots are always removed so this line is obsolete
		h = h.replaceAll("\\s{2,}"," ");
		h = h.toUpperCase();
		String[] check = MovieInfo.configuration().getFilters();

		for (int i = 0; i < check.length; i++) {
			if (h.matches("(?i)^.* " + check[i] + " .*$")) {
				h = h.replaceAll("(?i) " + check[i] + " "," ");
			}
		}
		h = h.trim();
		if(f != null)
			if(f.contains("сериал"))
				h = h + " %D1%81%D0%B5%D1%80%D0%B8%D0%B0%D0%BB";
		return h;
	}

	public static String plainText(String html) {
		return html
			// Preserve line breaks
			.replaceAll("<(br|p|BR|P) */?>", "\n")
			// Remove remaining tags
			.replaceAll("<[^>]+>", "")
			// Collapse non-newline whitespace
			.replaceAll("[ \t\f\r]+", " ")
			.trim();
	}

	public static String clean(String s) {
		if (s == null) {
			return null;
		}
		s = s.replace("&amp;", "&");
		Pattern t = Pattern.compile("&#x{0,1}.*?;");
		Matcher ma = t.matcher(s);
		String tmp;
		while (ma.find()) {
			tmp = ma.group();
			if (tmp.contains("x"))
				s = s.replaceAll(tmp, (char) Integer.valueOf(tmp.replaceAll("[^0-9a-fA-F]", ""), 16).intValue() + "");
			else
				s = s.replaceAll(tmp, (char) Integer.valueOf(tmp.replaceAll("[aA-zZ\\W]", "")).intValue() + "");
		}

		s = s.replace("â€™", "'");
		s = s.replace("&quot;", "\"");
		s = s.replace("&lt;", "<");
		s = s.replace("&gt;", ">");
		s = s.replace("&apos;", "'");
		s = s.replace("|", "");
		s = s.replace("&nbsp;", " ");
		s = s.replace("&oacute;","ó");
		s = s.replace("&Oacute;","Ó");
		s = s.replace("&ndash;","-");

		Iterator<String> it = MovieInfo.configuration().getCleanList().iterator();
		while (it.hasNext())
			s = s.replace(it.next(),it.next());
		s = s.replaceAll("\\s+", " ").trim();
		return s;
	}

	@Override
	public void discoverChildren() {
		super.discoverChildren();
	}

	public FileMovieInfoVirtualFolder(String name, String thumbnailIcon, int actors, int plotLineLenght) {
		this(name, thumbnailIcon, actors, plotLineLenght, "");
	}

	public FileMovieInfoVirtualFolder(String name, String thumbnailIcon, int actors, int plotLineLenght, String nfo) {
		super(name, thumbnailIcon);
		numberOfActors = config.getMaxNumberOfActors();
		if (actors > 0 && actors < numberOfActors) {
			numberOfActors = actors;
		}
		plotLineLength = config.getPlotLineLength();
		if (plotLineLenght > 0) {
			plotLineLength = plotLineLenght;
		}
		className = name;
		nfoId = nfo;
	}

	public FileMovieInfoVirtualFolder(String name, String thumbnailIcon, int actors, int plotLineLenght, String nfo, DLNAResource resource) {
		this(name, thumbnailIcon, actors, plotLineLenght, nfo);
		origRes = resource;
	}

	public FileMovieInfoVirtualFolder(String name, String thumbnailIcon, ResourceExtension resExt) {
		this(name, thumbnailIcon, 0, 0, resExt.imdbId);
		className = name;
		thumbfolder = resExt.thumbfolder;
		origRes = resExt.getOriginal();
	}
}
