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

import net.pms.PMS;
import net.pms.dlna.MapFile;
import net.pms.dlna.RealFile;
import net.pms.dlna.virtual.VirtualFolder;


public class MovieDBFolder extends VirtualFolder {

	private String sql;
	private boolean stop;
	private String val;
	private boolean cast;
	private int atz;
	private boolean sort;
	
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
		atz=0;
		sort=false;
	}
	
	public void cast() {
		cast=true;
	}
	
	public void atzLevel(int l) {
		atz=l;
	}
	
	public void setSort() {
		sort=true;
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
			HashMap<String,String> map=new HashMap<String,String>();
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
					map.put(nxtName, "");
					WrapperFile f=new WrapperFile(new File(nxtName),title);
					f.setThumb(thumb);
					addChild(f);
				}
				else {
					nxtName= rs.getString(name);
					if(cast)
						thumb=rs.getString("THUMB");
					if(StringUtils.isEmpty(nxtName))
						continue;
					if(map.containsKey(nxtName))
						continue;
					map.put(nxtName, "");
					String nxtsql;
					if(cast)
						nxtsql="SELECT MOVIE FROM CAST WHERE CAST = ?";
					else
						nxtsql="SELECT FILENAME,THUMB,TITLE FROM FILES WHERE "+name+" = ?";
					MovieDBFolder f=new MovieDBFolder(nxtName,nxtsql,true,nxtName,
													  thumb);
					f.cast=cast;
					addChild(f);
				}
			}
		} catch (Exception e) {
			PMS.info("sql execption "+e);
		} finally {
			try {
				if(rs!=null)
					rs.close();
				if(ps!=null)
					ps.close();
				if(conn!=null)
					conn.close();
			} catch (SQLException e) {
				PMS.info("close error "+e);
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
