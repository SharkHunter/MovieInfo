package net.pms.movieinfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import net.pms.PMS;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovieInfoVirtualData extends VirtualFolder {

	private boolean done = false;
	private static final Logger logger = LoggerFactory.getLogger(MovieInfoVirtualData.class);

	public MovieInfoVirtualData(String thumbnailIcon) {
		super(MovieInfoVirtualFolder.MOVIE_FOLDER, thumbnailIcon);
	}

	public MovieInfoVirtualData(String name, String thumbnailIcon) {
		super(name, thumbnailIcon);
	}
	public boolean isFolder(){
		return false;
	}
	public String getThumbnailURL(){	
		return thumbnailIcon;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public boolean isTranscodeFolderAvailable() {
		return false;
	}

	@Override
	public long length() {
		return -1; //DLNAMediaInfo.TRANS_SIZE;
	}

	@Override
	public boolean isTranscodeFolderAvailable() {
		return false;
	}

	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return getName();
	}
	private String getFfmpegPath() {
		String value = PMS.getConfiguration().getFfmpegPath();
		if (value == null) {
			logger.trace("No ffmpeg - unable to thumbnail");
			throw new RuntimeException("No ffmpeg - unable to thumbnail");
		} else {
			return value;
		}
	}
	private void createVideo() throws IOException {
		String args [] = new String[10];
		args[0] = getFfmpegPath();
		args[1] = "-f";
		args[2] = "image2";
		args[3] = "-i";
		args[4] = PMS.getConfiguration().getTempFolder().getAbsolutePath()+ "\\%02d.jpg";
		args[5] = "-r";
		args[6] = "24";
		args[7] = "-s";
		args[8] = "600x800";
		args[9] = PMS.getConfiguration().getTempFolder().getAbsolutePath()+ "\\image.mpg";

		OutputParams params = new OutputParams(PMS.getConfiguration());
		params.workDir = PMS.getConfiguration().getTempFolder();
		params.maxBufferSize = 1;
		params.noexitcheck = true; // not serious if anything happens during the thumbnailer
		final ProcessWrapperImpl pw = new ProcessWrapperImpl(args, params);
		// FAILSAFE
		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
				pw.stopProcess();
			}
		};
		Thread failsafe = new Thread(r);
		failsafe.start();
		pw.run();
	}
	
	public InputStream getInputStream() throws IOException {
//		System.out.println("getInputStream");
		File f = null;
		if(!done ){		
		thumbnailIcon = thumbnailIcon.replaceAll("SX300_SY300_","SX1920_SY1080_");
		URL url = new URL(thumbnailIcon);
			f = new File(PMS.getConfiguration().getTempFolder().getAbsolutePath()+ "\\01.jpg");
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			URLConnection conn = url.openConnection();
			InputStream in = conn.getInputStream();
			FileOutputStream fOUT = null;
			if ( f != null) {
				fOUT = new FileOutputStream(f);
			}
			byte buf [] = new byte [4096];
			int n = -1;
			while ((n=in.read(buf)) > -1) {
				bytes.write(buf, 0, n);
				if (fOUT != null)
					fOUT.write(buf, 0, n);
			}
			in.close();
			if (fOUT != null)
				fOUT.close();
			String tmp ="";
			for (int i = 2;i <= 24;i++){
				if(i < 10)tmp = "0" + i ;
				else tmp = i + "";
			  copy(f.getPath(),PMS.getConfiguration().getTempFolder().getAbsolutePath() +"\\"+ tmp + ".jpg");
				
			}

		f = new File(PMS.getConfiguration().getTempFolder().getAbsolutePath()+ "\\image.mpg");
		if(f.exists())f.delete();
		createVideo();
		done = true;
		}
		f = new File(PMS.getConfiguration().getTempFolder().getAbsolutePath()+ "\\image.mpg");
		InputStream is = null;
		try {
			if(f.exists())
			is = new FileInputStream(new File(f.getPath()));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//InputStream is = ClassLoader.getSystemResourceAsStream(fileName);
		return is;
	}
	static final int BUFF_SIZE = 100000;
	static final byte[] buffer = new byte[BUFF_SIZE];

	public static void copy(String from, String to) throws IOException{
	   InputStream in = null;
	   OutputStream out = null; 
	   try {
	      in = new FileInputStream(from);
	      out = new FileOutputStream(to);
	      while (true) {
	         synchronized (buffer) {
	            int amountRead = in.read(buffer);
	            if (amountRead == -1) {
	               break;
	            }
	            out.write(buffer, 0, amountRead); 
	         }
	      } 
	   } finally {
	      if (in != null) {
	         in.close();
	      }
	      if (out != null) {
	         out.close();
	      }
	   }
	}



}
