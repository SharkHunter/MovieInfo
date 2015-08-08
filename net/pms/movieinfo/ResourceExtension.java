package net.pms.movieinfo;

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
	public String imdbId = "";
	private PmsConfiguration configuration;
	public String thumbfolder = "";
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceExtension.class);

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
			configuration = PMS.getConfiguration();
			thumbfolder = configuration.getAlternateThumbFolder();
			if (MovieInfo.configuration().getPlugins() != null) {
				String[] plgn = MovieInfo.configuration().getPlugins().split(",");
				for(int i=0;i < plgn.length;i++)
					if (!plgn[i].equals(","))
//				fileFolder2.addChild(new FileMovieInfoVirtualFolder(plgn[i] +" INFO", null,numberOfActors,lineLength,imdbId));
				fileFolder2.addChild(new FileMovieInfoVirtualFolder(plgn[i] +" INFO", null,this));
				vf2.addChild(fileFolder2);
			}
		}
	}

	public DLNAResource getOriginal() {
		return original;
	}

}
