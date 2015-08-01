package net.pms.movieinfo;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;


public class MovieDBFolder extends VirtualFolder {

	private String sql;
	private boolean stop;
	private String val;
	private boolean cast;
	private static final Logger LOGGER = LoggerFactory.getLogger(MovieDBFolder.class);

	public MovieDBFolder(String name, String sql) {
		this(name,sql,false,"",null);
	}

	public MovieDBFolder(String name, String sql,
						 boolean stop,String val,String thumb) {
		super(name, thumb);
		this.sql=sql;
		this.stop=stop;
		this.val=val;
		cast=false;
	}

	public void cast() {
		cast=true;
	}

	public void atzLevel(int l) {
	}

	public void discoverChildren() {
		Connection conn =null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		JdbcConnectionPool cp=MovieDB.getDBConnection();
		try {
			conn = cp.getConnection();
			ps=conn.prepareStatement(sql);
			if(StringUtils.isNotEmpty(val))
				ps.setString(1, val);
			rs = ps.executeQuery();
			HashMap<String,DLNAResource> map=new HashMap<String,DLNAResource>();
			TreeMap<String,ArrayList<DLNAResource>> letters=new TreeMap<String,ArrayList<DLNAResource>>();
			while (rs.next()) {
				String nxtName;
				String thumb=null;
				String title="";
				if(stop) {
					if(cast) {
						int id=rs.getInt("MOVIE");
						ps=conn.prepareStatement("SELECT FILENAME,THUMB,TITLE FROM FILES WHERE ID = ?");
						ps.setInt(1, id);
						ResultSet rs1=ps.executeQuery();
						rs1.next();
						nxtName=rs1.getString("FILENAME");
						thumb=rs1.getString("THUMB");
						title=rs1.getString("TITLE");
						rs1.close();
					}
					else {
						nxtName=rs.getString("FILENAME");
						thumb=rs.getString("THUMB");
						title=rs.getString("TITLE");
					}
					if(StringUtils.isEmpty(nxtName))
						continue;
					if(map.containsKey(nxtName))
						continue;
					if(StringUtils.isEmpty(title))
						title=nxtName;
					WrapperFile f=new WrapperFile(new File(nxtName),title);
					f.setThumb(thumb);
					map.put(nxtName, f);
					ArrayList<DLNAResource> l=letters.get(""+nxtName.charAt(0));
					if(l==null)
						l=new ArrayList<DLNAResource>();
					l.add(f);
					letters.put(""+nxtName.charAt(0),l);
				}
				else {
					nxtName= rs.getString(name);
					if(cast)
						thumb=rs.getString("THUMB");
					if(StringUtils.isEmpty(nxtName))
						continue;
					if(map.containsKey(nxtName))
						continue;
					String nxtsql;
					if(cast)
						nxtsql="SELECT MOVIE FROM CAST WHERE CAST = ?";
					else
						nxtsql="SELECT FILENAME,THUMB,TITLE FROM FILES WHERE "+name+" = ?";
					MovieDBFolder f=new MovieDBFolder(nxtName,nxtsql,true,nxtName,
							thumb);
					f.cast=cast;
					map.put(nxtName,f);
					ArrayList<DLNAResource> l=letters.get(""+nxtName.charAt(0));
					if(l==null)
						l=new ArrayList<DLNAResource>();
					l.add(f);
					letters.put(""+nxtName.charAt(0),l);
				}
			}
			if(map.size() < 20) {
				// simple case
				for(String s : map.keySet()) {
					addChild(map.get(s));
				}
			}
			else {
				for(String c : letters.keySet()) {
					final ArrayList<DLNAResource> l=letters.get(c);
					final VirtualFolder vf=new VirtualFolder(c,"") {
						public void discoverChildren() {
							for(DLNAResource r : l) {
								addChild(r);
							}
						}
					};
					addChild(vf);
				}
			}
		} catch (Exception e) {
			LOGGER.debug("{MovieInfo} SQL exception: {}", e);
		} finally {
			try {
				if(rs!=null)
					rs.close();
				if(ps!=null)
					ps.close();
				if(conn!=null)
					conn.close();
			} catch (SQLException e) {
				LOGGER.error("{MovieInfo} Close error: {}", e);
			}
		}
	}

	public boolean isRefreshNeeded() {
		return true;
	}

	public InputStream getThumbnailInputStream() {
		try {
			return downloadAndSend(thumbnailIcon,true);
		}
		catch (Exception e) {
			return super.getThumbnailInputStream();
		}
	}

}
