package net.pms.movieinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MovieInfoCfg {
	private ArrayList<String> cleanlist = new ArrayList<String>();
	private String filter;
	private String[] disp;
	private String cover;
	private String priority;
	private String ilanguage = "en";
	private boolean removeyear = false;
	private String dbPlug;

	public MovieInfoCfg() {
		readCfg();
	}

	private void readCfg() {
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
						if(line.startsWith("Cover="))
							cover = line.substring(line.indexOf("=")+1,line.length());
						if(line.startsWith("DBPlugin="))
							cover = line.substring(line.indexOf("=")+1,line.length());

					}
				}
				disp = info.split(",");
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getCover() {
		return cover;
	}

	public String[] getDisplay() {
		return disp;
	}
}
