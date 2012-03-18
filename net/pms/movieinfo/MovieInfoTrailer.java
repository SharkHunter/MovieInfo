package net.pms.movieinfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.Range;
import net.pms.dlna.WebVideoStream;
import net.pms.encoders.Player;

public class MovieInfoTrailer extends WebVideoStream {

	public MovieInfoTrailer(String name, String thumbnailIcon, String url) {
		super(name,url,thumbnailIcon);
	}
	public Player getPlayer() {
		player = new MEncoderVideoYoutube(PMS.getConfiguration());
		return player;
	}
	@Override
	public InputStream getInputStream(Range range, RendererConfiguration mediarenderer) throws IOException {
//		public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediaRenderer) throws IOException {
		if (this.getUrl().toLowerCase().indexOf("youtube") > -1 && getUrl().toLowerCase().indexOf("?") > -1) {
			try {
				InputStream is = downloadAndSend(getUrl(), false);
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				int n = -1;
				byte buffer [] = new byte [4096];
				while( (n=is.read(buffer))> -1) {
					bout.write(buffer, 0, n);
				}
				is.close();
				String content = new String(bout.toByteArray());
				int fs = content.indexOf("swfArgs");
				int hd = content.indexOf("isHDAvailable = true");
				String newURL = "http://www.youtube.com/get_video%3F";
				if (fs > -1) {
					String seq = content.substring(fs+18, content.indexOf("}", fs));
					seq = seq.trim();
					StringTokenizer st = new StringTokenizer(seq, ",");
					while (st.hasMoreTokens()) {
						String elt = st.nextToken();
						if (elt.startsWith(" \"video_id\""))
						{	
							newURL += "&video_id%3D";
							newURL += elt.substring(14, elt.length()-1);
							if (hd>-1)newURL += "&fmt=22";
							else newURL += "&fmt=18";
						}
						else if (elt.startsWith(" \"l\""))
						{
							newURL += "&l=";
							newURL += elt.substring(6, elt.length());
							
						}
						else if (elt.startsWith(" \"sk\""))
						{
							newURL += "&sk=";
							newURL += elt.substring(8, elt.length()-1);
							
						}
						else if (elt.startsWith(" \"t\""))
						{
							newURL += "&t=";
							newURL += elt.substring(7, elt.length()-1);
						}
						newURL = newURL.replace("=", "%3D");
					}
					setUrl(newURL);
				}
			} catch (IOException e) {
				PMS.error(null, e);
			}
		}
	//	return super.getInputStream(low, high, timeseek, mediaRenderer);
		return super.getInputStream(range,mediarenderer);
	}
	
	public boolean isTranscodeFolderAvailable() {
		return false;
	}

}
