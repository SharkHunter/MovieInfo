package net.pms.movieinfo;

import net.pms.dlna.virtual.VirtualFolder;

public class MovieInfoVirtualFolder extends VirtualFolder {
	
	public static String MOVIE_FOLDER = "#--MOVIE INFO--#";
	
	public MovieInfoVirtualFolder(String thumbnailIcon) {
		super(MOVIE_FOLDER, thumbnailIcon);
	}

	public MovieInfoVirtualFolder(String name, String thumbnailIcon) {
		super(name, thumbnailIcon);
	}
	
}
