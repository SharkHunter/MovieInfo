package net.pms.movieinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;

public class ResourceExtension {
	
	private DLNAResource original;
	private String plugins;
	private int numberOfActors;
	private int lineLength;
	private String imdbId;
	
	public ResourceExtension(DLNAResource original) {
		this(original,"");
	}
	
	public ResourceExtension(DLNAResource original,String imdb) {
		this.original = original;
		this.imdbId=imdb;
	}
	
	public void addChild(DLNAResource child) {
		if (child.getExt().isVideo() && (!child.isNotranscodefolder())) {
			VirtualFolder vf2 = null;
			for(DLNAResource r:original.getChildren()) {
				if (r instanceof MovieInfoVirtualFolder) {
					vf2 = (MovieInfoVirtualFolder) r;
					break;
				}
			}
			if (vf2 == null) {
				vf2 = new MovieInfoVirtualFolder(null);
				original.getChildren().add(vf2);
				vf2.setParent( original );
			}
			VirtualFolder fileFolder2 = new MovieInfoVirtualFolder(child.getName(), null);
			getOptions();
			if (plugins != null) {
				String[] plgn = plugins.split(",");
				for(int i=0;i < plgn.length;i++)
					if (!plgn[i].equals(","))
				fileFolder2.addChild(new FileMovieInfoVirtualFolder(plgn[i] +" INFO", null,numberOfActors,lineLength,imdbId));
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
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else
			PMS.minimal("MOVIEINFO.conf file not found!");
	}

}
