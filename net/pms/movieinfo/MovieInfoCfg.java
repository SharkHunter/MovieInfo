package net.pms.movieinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MovieInfoCfg {
	private ArrayList<String> cleanlist = new ArrayList<String>();
	private String[] disp;
	private String cover;

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
				String info = "";
				cover = "";
				if (!cleanlist.isEmpty())
					cleanlist.clear();
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0 && !line.startsWith("#") && line.indexOf("=") > -1) {
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
