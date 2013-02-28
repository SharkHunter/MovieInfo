package net.pms.movieinfo;

import java.io.InputStream;

import net.pms.dlna.virtual.VirtualFolder;

public class MovieInfoVirtualFolder extends VirtualFolder {
	
	public static String MOVIE_FOLDER = "#--MOVIE INFO--#";
	
	public MovieInfoVirtualFolder(String thumbnailIcon) {
		super(MOVIE_FOLDER, thumbnailIcon);
	}

	public MovieInfoVirtualFolder(String name, String thumbnailIcon) {
		super(name, thumbnailIcon);
	}
	
	public InputStream getThumbnailInputStream()  {
    	if (thumbnailIcon != null) {
    		try {
    			if (thumbnailIcon.startsWith("http://"))
    				return downloadAndSend(thumbnailIcon,true);	
    			return getResourceInputStream(thumbnailIcon);
    		}
    		catch (Exception e) {
    		}
    	}
		return super.getThumbnailInputStream();
    }
	
}
