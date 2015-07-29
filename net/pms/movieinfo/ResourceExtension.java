package net.pms.movieinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.RealFile;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.movieinfo.plugins.Plugin;
import net.pms.configuration.PmsConfiguration;

public class ResourceExtension {

	private DLNAResource original;
	private String plugins;
	public int numberOfActors = 99;
	public int lineLength = 60;
	public int cellwrap = 0;
	public boolean showtags = true;
	public String imdbId = "";
	public String cover = "1";
	private PmsConfiguration configuration;
	public String thumbfolder = "";
	private static final Logger logger = LoggerFactory.getLogger(ResourceExtension.class);

	public ResourceExtension(DLNAResource original) {
		this(original,"");
	}

	public ResourceExtension(DLNAResource original,String imdb) {
		this.original = original;
		this.imdbId=imdb;
	}

	public void addChild(DLNAResource child) {
		if(MovieDB.movieDBParent(child))
			return;
		if (child.getFormat().isVideo() && child.isTranscodeFolderAvailable()) {
			VirtualFolder vf2 = null;
			for(DLNAResource r:original.getChildren()) {
				if (r instanceof MovieInfoVirtualFolder) {
					vf2 = (MovieInfoVirtualFolder) r;
					break;
				}
			}
			if (vf2 == null) {
				vf2 = new MovieInfoVirtualFolder(null);
				original.addChild(vf2);
			}
			if(MovieInfo.getDB() != null) {
				RealFile rf = (RealFile)child;
				Plugin plugin = MovieInfo.getDB().findInDB(rf.getFile().getAbsolutePath());
				if(plugin!=null) {
					FileMovieInfoVirtualFolder fmf = new FileMovieInfoVirtualFolder(child.getName(),null,this);
					fmf.setPlugin(plugin);
					vf2.addChild(fmf);
					return;
				}
			}
			VirtualFolder fileFolder2 = new MovieInfoVirtualFolder(child.getName(), null);
			getOptions();
			configuration = PMS.getConfiguration();
			thumbfolder = configuration.getAlternateThumbFolder();
			if (plugins != null) {
				String[] plgn = plugins.split(",");
				for(int i=0;i < plgn.length;i++)
					if (!plgn[i].equals(","))
//				fileFolder2.addChild(new FileMovieInfoVirtualFolder(plgn[i] +" INFO", null,numberOfActors,lineLength,imdbId));
				fileFolder2.addChild(new FileMovieInfoVirtualFolder(plgn[i] +" INFO", null,this));
				vf2.addChild(fileFolder2);
			}
		}
	}

	private void getOptions()
	{
		File miConf = new File("MOVIEINFO.conf");
		if (!miConf.exists())
			miConf = new File("plugins/MOVIEINFO.conf");
		if (miConf.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(miConf), "UTF-8"));
				String line = null;
				while ((line=br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0 && !line.startsWith("#") && line.indexOf("=") > -1) {
						if(line.startsWith("Plugins="))plugins = line.substring(line.indexOf("=")+1,line.length()).toUpperCase();
						if(line.startsWith("NumberOfActors="))numberOfActors = Integer.parseInt(line.substring(line.indexOf("=")+1,line.length()));
						if(line.startsWith("Linelength="))lineLength = Integer.parseInt(line.substring(line.indexOf("=")+1,line.length()));
						if(line.startsWith("cellwrap="))cellwrap = (int)(Double.parseDouble(line.substring(line.indexOf("=")+1,line.length()))*10);
						if(line.startsWith("showtags="))showtags = Boolean.parseBoolean(line.substring(line.indexOf("=")+1,line.length()));
						if(line.startsWith("Cover="))cover = line.substring(line.indexOf("=")+1,line.length());
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else
			logger.trace("MOVIEINFO.conf file not found!");
	}

	public DLNAResource getOriginal() {
		return original;
	}

}
