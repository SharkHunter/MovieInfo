package net.pms.movieinfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.pms.dlna.RealFile;

public class WrapperFile extends RealFile {

	private String name;
	private String thumb;

	public WrapperFile(File file) {
		super(file);
		name=file.getName();
	}

	public WrapperFile(File file,String name) {
		super(file);
		this.name=name;
	}

	public void setThumb(String t) {
		thumb=t;
	}

	public String getName() {
		return name;
	}

	public InputStream getThumbnailInputStream() {
		try {
			return downloadAndSend(thumb,true);
		}
		catch (Exception e) {
			try {
				return super.getThumbnailInputStream();
			} catch (IOException e1) {
				return null;
			}
		}
	}
}
