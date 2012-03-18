/* Class by Jaqb
 *
 */
package net.pms.movieinfo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import net.pms.dlna.virtual.VirtualFolder;
import net.pms.movieinfo.plugins.Plugin;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileMovieInfoVirtualFolder extends VirtualFolder {

	private final boolean GOOGLE = true;
	private final boolean MOVIEWEBSITE = false;
	private String newURL;
	private String title = null;
	private String rating;
	private String agerating;
	private String plot;
	private String thumb;
	private String tagline;
	private String pluginUrl;
	private String priority;
	private String genre;
	private String dir;
	private boolean resolved;
	private boolean isDVD = false;
	private ArrayList<String> castlist = new ArrayList<String>();
	private ArrayList<String> cleanlist = new ArrayList<String>();
	private int numberOfActors = 99;
	private String className = null;
	private int lineLength = 60;
	private String filter;
	private String pluginTv;
	private String pluginChar;
	private String[] disp;
	private String cover;
	private String nfoId;
	private String trailer;
	private String cookie;

	private int cellwrap = 0;
	private boolean showtags = true;
	private String ilanguage = "en";
	private boolean removeyear = false;
	private String thumbfolder = "";
	private static final Logger logger = LoggerFactory.getLogger(FileMovieInfoVirtualFolder.class);

	public FileMovieInfoVirtualFolder(String name, String thumbnailIcon, boolean copy) {
		super(name, thumbnailIcon);
	}
	
	@Override
	public void resolve() {
		super.resolve();
		
		if (!resolved && getChildren().size() == 0) {
			getConfig();
			MovieInfoVirtualFolder fld = null;
			if (getParent().getParent().getParent().getName().contains("[DVD ISO]"))
				isDVD = true;
			String name = getParent().getName();
			name = name.replaceAll("\\..{2,4}$", "");
			
					
			if (className != null)
			className = className.replace(" INFO", "");
//			System.out.println(className);
			String classNameIMDB = "net.pms.movieinfo.plugins.IMDBPlugin";
			String classNamePlugin = "net.pms.movieinfo.plugins." +className+ "Plugin";
			//System.out.println(classNamePlugin);
			Plugin imdb = null;
			Plugin plugin = null;

			try {
				imdb = (Plugin)(Class.forName(classNameIMDB).newInstance());
				plugin = (Plugin)(Class.forName(classNamePlugin).newInstance());

			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pluginTv = plugin.getTvShow();
			pluginUrl = plugin.getGoogleSearchSite();
			pluginChar = plugin.getCharSet();
			
			String nfo = nfoId;
			if(nfo.length() == 0)
				nfo = imdbIDFromNfo(name);
			if (nfo != null) {
//				System.out.println(plugin.getClass().getSimpleName() + " nfo file found");
//				System.out.println(plugin.getClass().getSimpleName() + " nfo =" +nfo);
				if (nfo.matches("tt[0-9]{7}")) {
//					System.out.println(plugin.getClass().getSimpleName() + " nfo file matches imdb");
					
					if (!className.equals("IMDB")) {
						imdb.importFile(getWebsite(nfo, imdb.getVideoURL(),	MOVIEWEBSITE, null));
//						System.out.println(className + " nfo file matches imdb");
						if ((nfo = plugin.lookForMovieID(getWebsite(imdb.getTitle(), null,GOOGLE, plugin.getGoogleSearchSite()))) != null)
							plugin.importFile(getWebsite(nfo, plugin.getVideoURL(), MOVIEWEBSITE, null));
					} else
						plugin.importFile(getWebsite(nfo, plugin.getVideoURL(),	MOVIEWEBSITE, null));						
				} else {
//					System.out.println(plugin.getClass().getSimpleName() + " nfo file dont matches imdb");
					plugin.importFile(getWebsite(nfo, plugin.getVideoURL(),MOVIEWEBSITE, null));
				}
			} else {
//				System.out.println(plugin.getClass().getSimpleName() + " nfo file not found, search filename");
				String moviename = "";
				if (!isDVD)
					moviename = getMovienameFromFilename(getParent().getName());
				else
					moviename = getMovienameFromFilename(getParent().getParent().getParent().getDisplayName().replace("[DVD ISO] ", ""));
				//System.out.println("Moviename = " + moviename);
//				System.out.println(plugin.getClass().getSimpleName() + " Moviename = " + moviename);
				if (!moviename.equals(""))
					nfo = plugin.lookForMovieID(getWebsite(moviename, null, GOOGLE, plugin.getGoogleSearchSite()));
				if (nfo != null) {
//					System.out.println(plugin.getClass().getSimpleName() + " nfo =" +nfo);
					plugin.importFile(getWebsite(nfo, plugin.getVideoURL(), MOVIEWEBSITE, null));
					} else {
//					System.out.println(plugin.getClass().getSimpleName() + " movie from moviename not found, search directory name");
					if (!isDVD)
						moviename = getMovienameFromFilename(getMovienameFromFilename(getParent().getParent().getParent().getName()));
					else 
						moviename = getMovienameFromFilename(getParent().getParent().getParent().getParent().getName());
					
//					System.out.println(plugin.getClass().getSimpleName() + " Directory name = " + moviename);
					//System.out.println("Foldername = " + moviename);
					if (!moviename.equals(""))
						nfo = plugin.lookForMovieID(getWebsite(moviename, null, GOOGLE, plugin.getGoogleSearchSite()));
					if (nfo != null) {
						plugin.importFile(getWebsite(nfo, plugin.getVideoURL(), MOVIEWEBSITE, null));
//						System.out.println(plugin.getClass().getSimpleName() + " nfo =" +nfo);
					}
				}
			}
			if (nfo != null) {
				for (int i = 0; i < disp.length; i++) {
					if(disp[i].contains("title")) {
						title = plugin.getTitle();
						thumb = plugin.getVideoThumbnail();
						trailer = plugin.getTrailerURL();
						displayTitle(fld);
					}
					if(disp[i].equals("rating")) {
						rating = plugin.getRating();
						displayRating(fld);
					}
					if(disp[i].contains("genre")) {
						genre = plugin.getGenre();
						displayGenre(fld);
					}
					if(disp[i].contains("plot")) {
						plot = plugin.getPlot();
						displayPlot(fld);
					}
					if(disp[i].contains("director")) {
						dir = plugin.getDirector();
						displayDirector(fld);
					}
					if(disp[i].contains("cast")) {
						castlist = plugin.getCast();
						displayCast(fld);
					}
					if(disp[i].contains("tagline")) {
						tagline = plugin.getTagline();
						displayTagline(fld);
					}
					if(disp[i].equals("agerating")) {
						agerating = plugin.getAgeRating();
						displayAgeRating(fld);
					}
				}

			}
//			  System.out.println("Title: " + title);
//			  System.out.println("plot: " + plot);
//			  System.out.println("dir: " + dir);
//			  System.out.println("genre: " + genre);
//			  System.out.println("tagline: " + tagline);
//			  System.out.println("rating: " + rating);
//			  System.out.println("agerating: " + agerating);
//			  System.out.println("votes: " + votes);
//			  System.out.println("thumb: " + thumb);

		}
		resolved = true;
	}
	
	private void displayTitle(MovieInfoVirtualFolder fld) {
		if (title != null) {
			title = clean(title);
			if(cellwrap == 0) {
				fld = new MovieInfoVirtualFolder((showtags ? "Title: " : "") + title, thumb);
//				System.out.println("Title: " + title);
			} else {
				fld = new MovieInfoVirtualFolder(wrapKerned(title.toUpperCase(), 1.2), thumb);
//				System.out.println(wrapKerned(title.toUpperCase(), 1.2));
			}
			if (thumb != null)
			fld.addChild(new MovieInfoVirtualData(title, thumb.replaceAll("S[X,Y][0-9]{2,3}_S[X,Y][0-9]{2,3}_", "SX300_SY300_")));
			if(cover.equals("1")) {
			File f = null;
			if (!isDVD && className.equals(priority))
			try {
						f = new File((StringUtils.isNotBlank(thumbfolder) ? thumbfolder : getParent().getParent().getParent().getSystemName()) + File.separator + getParent().getName() + ".cover.jpg");
//						System.out.println("cover="+ f.getAbsolutePath());
						if (!f.exists() && f.isAbsolute()) {
							
							URL url = null;
							if(thumb != null && thumb.matches("S[X,Y][0-9]{2,3}_S[X,Y][0-9]{2,3}_"))
								thumb = thumb.replaceAll("S[X,Y][0-9]{2,3}_S[X,Y][0-9]{2,3}_","SX300_SY300_");
							if(thumb != null) {
								url = new URL(thumb);
//								System.out.println("thumb="+thumb);
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
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			if (className.equals("IMDB")) {
				String[] yt = getYoutubeTrailer(getWebsite(title.replace(" & ", " ") + " trailer", "http://www.youtube.com/results?search_type=videos&search_query=###MOVIEID###&high_definition=1", MOVIEWEBSITE, null), true);
				if (!"None".equalsIgnoreCase(yt[1])) {
					fld.addChild(new MovieInfoTrailer("Youtube: " + yt[1], thumb, yt[0]));
				}
				yt = getYoutubeTrailer(getWebsite(title.replace(" & ", " ") + " trailer hd", null, GOOGLE, "youtube.com"), false);
				if (!"None".equalsIgnoreCase(yt[1])) {
					fld.addChild(new MovieInfoTrailer("Google: " + yt[1], thumb, yt[0]));
				}
			}
			addChild(fld);
		} 	
	}

	private void displayRating(MovieInfoVirtualFolder fld) {
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

	private void displayGenre(MovieInfoVirtualFolder fld) {
		if (genre != null && genre != " ") {
			genre = clean(genre);
			addInfo("Genre: ", genre);
		}
	}

	private void displayTagline(MovieInfoVirtualFolder fld) {
		if (tagline != null) {
			tagline = clean(tagline);
			addInfo("Tagline: ", tagline);
		}
	}

	private void displayAgeRating(MovieInfoVirtualFolder fld) {
		if (agerating != null) {
			agerating = clean(agerating);
			addInfo("Age Rating: ", agerating);
		}
	}

	private void displayDirector(MovieInfoVirtualFolder fld) {
		if (dir != null) {
			dir = clean(dir);
			dir = dir.replaceAll("\\.\\s*$", "");
			addInfo("Director: ", dir);
		}
	}

	private void displayPlot(MovieInfoVirtualFolder fld) {
		String plot1 = "", temp = showtags ? "Plot: " : "";
		int start = 0;
		if (plot != null) {
			plot = clean(plot);
//			System.out.println("Plot clean = " + plot);
			if(cellwrap != 0) {
				addInfo("Plot: ", plot);
			} else {
				for (int i = 0; i <= plot.length(); i = i + lineLength ) {

					if (i + lineLength <= plot.length() && plot.contains(" ")) {
						plot1 = plot.substring(start, plot.lastIndexOf(" ",	i + lineLength));
						start = plot.lastIndexOf(" ", i + lineLength);
					} else if (i + lineLength > plot.length()) {
						plot1 = plot.substring(start, plot.length());
					}
					if (plot1.length() > 1)
						this.addChild(new MovieInfoVirtualFolder(temp + plot1, null));
//					System.out.println(temp + plot1);
					temp = showtags ? "____ " : "";
				}
			}
		}
	}

	private void displayCast(MovieInfoVirtualFolder fld) {
		if (!castlist.isEmpty() && castlist != null) {
			String temp = showtags ? "Cast: " : "";
			String plot1 =null;
			String act = "";
			int a = 0;
			while (!castlist.isEmpty() && a++ < numberOfActors) {
				if(castlist.get(0) != null)
				plot1 = castlist.remove(0).replaceAll("S[X,Y][0-9]{2}_S[X,Y][0-9]{2}_", "SX300_SY300_").replace("http://i.media-imdb.com/images/tn15/addtiny.gif","http://i.media-imdb.com/images/nophoto.jpg");
				act = clean(castlist.remove(0));
				String label = (cellwrap != 0 ? "" : temp) + act + (castlist.get(0) == "" ? "" : " as ") + clean(castlist.remove(0));
				fld = (new MovieInfoVirtualFolder((cellwrap != 0 ? wrap(label) : label), null));
				fld.addChild(new MovieInfoVirtualData(act,plot1));
				addChild(fld);
//				System.out.println(act);
				temp = showtags ? "____   " : "";
			}
		}
	}

	private void addInfo(String tag, String s) {
		if (showtags)
			s = tag + s;
		if (cellwrap > 0) {
			s = wrap(s);
			addChild(new MovieInfoVirtualFolder(s, null));
			int blanks = ((s.replaceAll("[^\n]", "").length() * 10 + 5) / cellwrap);
//			int blanks = (int)(((double)s.replaceAll("[^\n]", "").length() + 0.5) / cellwrap);
//			int blanks = (s.replaceAll("[^\n]", "").length() + cellwrap - 1) / cellwrap - 1;
			for (int i = 0; i < blanks; i++)
				addChild(new MovieInfoVirtualFolder("", null));
		} else
			addChild(new MovieInfoVirtualFolder(s, null));
//		System.out.println(s);
	}

	private String wrapKerned(String s, double factor) {
		int len = (int)(lineLength / factor);
		return s.length() > len ? WordUtils.wrap(s, len, null, true) : s;
	}	

	private String wrap(String s) {
		return s.length() > lineLength ? WordUtils.wrap(s, lineLength, null, true) : s;
	}	

	private String imdbIDFromNfo(String movieName) {

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
//				System.out.println("movie name for nfo search" + movieName);
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
//					System.out.println("url = " + url);
				}
			} */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// System.out.println("imdbIDFromNfo Exception: " + e);
		}
//		System.out.println("imdbIDFromNfo Returns " + newURL);
		return newURL;
	}

	private String lookForImdbID(BufferedReader in) {
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
			newURL = null;
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
			// TODO Auto-generated catch block
//			 System.out.println("lookForImdbID Exception: " + e);
//			 e.printStackTrace();
		}
//		System.out.println("lookForImdbID Returns " + newURL);
		return newURL;
	}

	private String[] getYoutubeTrailer(BufferedReader in, boolean youtube) {
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
			// TODO Auto-generated catch block
//			 System.out.println("lookForImdbID Exception: " + e);
//			 e.printStackTrace();
		}
//		System.out.println("lookForImdbID Returns " + newURL);
		return yt;
	}

	private BufferedReader getWebsite(String mname, String url, boolean google, String searchURL) {
		BufferedReader in = null;
		try {
			URL u;
			if (!google) {
				if (url.contains("###MOVIEID###")) {
					if (url.contains("youtube.com"))
						mname = mname.replace(" ", "+");
					url = url.replace("###MOVIEID###", mname);
				}
				u = new URL(url);
				//u = new URL("http://www.imdb.com/title/" + key + "/");
				logger.debug("MovieInfo - Resolved link for gathering data: " + u);
			} else {
				mname = mname.replace(" ", "+");
				u = new URL(("http://www.google.com/search?hl=" + ilanguage + "&q=" + mname + "+site%3A" + searchURL));
				logger.debug("MovieInfo - SEARCH link sent to GOOGLE: " + u);
			}
			String host = u.getHost();
			String file = u.toString().replace("http://" + u.getHost(), "");

//			System.out.println(u.toString());
//			System.out.println(u.getPath());
//			System.out.println(u.getHost());
//			System.out.println("file= " + file);
//			System.out.println("host= " + host);
			
			URLConnection conn;
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
			InputStream is = conn.getInputStream();
	        InputStream decoder;
	        if (conn.getHeaderField("Content-Encoding") != null)
	        	if (conn.getHeaderField("Content-Encoding").equals("gzip")) {
	        		decoder = new GZIPInputStream(is, 4096);
	        		is = decoder;
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
	        ByteArrayOutputStream bout = new ByteArrayOutputStream();
			int n = -1;
/*			if(cookie!=null&&cookie.length()!=0)
				bout.write(cookie.getBytes(), 0, cookie.length());
*/
			byte buffer [] = new byte [4096]; //8192
			while( (n=is.read(buffer))> -1) {
				bout.write(buffer, 0, n);
			}
			in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bout.toByteArray()), pluginChar));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//System.out.println("getWebsite Exception: " + e);
			// e.printStackTrace();
		}

		//System.out.println("getWebsite Returns " + in.toString() + "Google =" + google);
		return in;
	}

	private void getConfig() {
		File miConf = new File("MOVIEINFO.conf"); 
		if (!miConf.exists())
			 miConf = new File("plugins/MOVIEINFO.conf");
		if (miConf.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(miConf), "UTF-8")); 
				String line = null;
				filter = "";
				String info = "";
				cover = "";
				if (!cleanlist.isEmpty())
					cleanlist.clear();			
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0 && !line.startsWith("#") && line.indexOf("=") > -1) { 
						if(line.startsWith("Filter="))
							filter += line.substring(line.indexOf("=") + 2,line.lastIndexOf("\"")).toUpperCase();
						if(line.startsWith("Plugins="))
							if(line.indexOf(",") > -1)
								priority = line.substring(line.indexOf("=") + 1,line.indexOf(",")).toUpperCase();
							else
								priority = line.substring(line.indexOf("=") + 1,line.length()).toUpperCase();
						if(line.startsWith("CleanDisplay=")) {
							cleanlist.add(line.substring(22,line.indexOf("\"",22)));
							cleanlist.add(line.substring(line.indexOf(".with(") + 7,line.lastIndexOf("\"")));
						}
						if(line.startsWith("DisplayInfo=")) {
							info =(line.substring(12));
						}
						if(line.startsWith("Cover=")) {
							cover =(line.substring(6));
						}
						if(line.startsWith("ILanguage=")) {
							ilanguage = line.substring(line.indexOf("=")+1,line.length()).toLowerCase();
						}
						if(line.startsWith("RemoveYear=")) {
							removeyear = Boolean.parseBoolean(line.substring(line.indexOf("=")+1,line.length()));
						}
					}
				}
				disp = info.split(",");
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
		if (removeyear)
			h = h.replaceAll("[^0-9][\\(,\\[]{0,1}[0,1,2][0,9][0-9]{2}[\\),\\]]{0,1}","");	// Remove year from searches
		h = " " + h + " ";
		h = h.replaceAll("[^\\p{N}\\p{L}]", " ");	// Don't remove all chars of any language so diacritic will NOT be also removed!
//		h = h.replaceAll("[^aA0-zZ9]", " ");		// It doesn't remove ASCII characters only so special chars like diacritic are removed!
//		h = h.replaceAll("\\p{Punct}", " ");		// Dots are always removed so this line is obsolete
		h = h.replaceAll("\\s{2,}"," ");
		h = h.toUpperCase();
		String[] check = filter.split("\".*?,.*?\"");

		for (int i = 0; i < check.length; i++) {
			if (h.matches("^.*(?i) " + check[i] + " .*$"))
				h = h.replaceAll(" " + check[i] + " "," "); 
		}
		h = h.trim();
		if(f != null)
			if(f.contains("сериал"))
				h = h + " %D1%81%D0%B5%D1%80%D0%B8%D0%B0%D0%BB";
//		System.out.println("getMovienameFromFilename Returns " + h);
		return h;
	}

	public String clean(String s) {
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

		Iterator<String> it = cleanlist.iterator();
		while (it.hasNext())
			s = s.replace(it.next(),it.next());
		s = s.replaceAll("\\s+", " ").trim();
		return s;
	}

	@Override
	public void discoverChildren() {
		super.discoverChildren();
	}

	public FileMovieInfoVirtualFolder(String name, String thumbnailIcon,int act, int line) {
		this(name,thumbnailIcon,act,line,"");
	}

	public FileMovieInfoVirtualFolder(String name, String thumbnailIcon,int act, int line,String nfo) {
		super(name, thumbnailIcon);
		if(act > 0)
			numberOfActors = act;
		if(line > 0)
			lineLength = line;
		className = name;
		nfoId = nfo;
	}

	public FileMovieInfoVirtualFolder(String name, String thumbnailIcon, ResourceExtension r) {
		super(name, thumbnailIcon);
		numberOfActors = r.numberOfActors;
		lineLength = r.lineLength;
		cellwrap = r.cellwrap;
		showtags = r.showtags;
		cover = r.cover;
		className = name;
		nfoId = r.imdbId;
		thumbfolder = r.thumbfolder;
	}

}
