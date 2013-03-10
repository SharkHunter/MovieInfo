package net.pms.movieinfo;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.h2.engine.Constants;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;

import com.sun.jna.Platform;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.RealFile;
import net.pms.dlna.virtual.VirtualFolder;

public class MovieDB extends VirtualFolder implements Runnable {
	private Thread scanner;
	private int dbCount;
	private String scanPath;
	
	private static String[] KeyWords = { "Genre", "Title", "Director", 
										 "Rating", "AgeRating"  };
	
	public MovieDB() {
		super("MovieDB", null);
		setupTables();
	}
	
	public static JdbcConnectionPool getDBConnection() {
		/* This is take from DLNAMediaDb*/
		String url;
		String dbName;
		String dir = "database";
		dbName = "movieDB";
		File fileDir = new File(dir);
		if (Platform.isWindows()) {
			String profileDir = PMS.getConfiguration().getProfileDirectory();
			url = String.format("jdbc:h2:%s\\%s/%s", profileDir, dir, dbName);
			fileDir = new File(profileDir, dir);
		} else {
			url = Constants.START_URL + dir + "/" + dbName;
		}
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL(url);
		ds.setUser("sa");
		ds.setPassword("");
		return JdbcConnectionPool.create(ds);
	}
	
	private void executeUpdate(Connection conn, String sql) throws SQLException {
		if (conn != null) {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		}
	}
	
	private static void close(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			PMS.info("close error "+e);
		}
	}
	
	private void setupTables() {
		JdbcConnectionPool cp=getDBConnection();
		Connection conn =null;
		ResultSet rs = null;
		Statement stmt = null;
		try {
			conn = cp.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT count(*) FROM FILES");
			if (rs.next()) {
				dbCount = rs.getInt(1);
			}
			rs.close();
			stmt.close();
			if(dbCount != -1) { 
				/* tables exist, just return */
				close(conn);
				return;
			}
		} catch (Exception e) {
			PMS.info("setup tabe error "+e);
		} finally {
			close(conn);
		}
		try {
			conn = cp.getConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE FILES (");
			sb.append(" ID                INT AUTO_INCREMENT");
			sb.append(", FILENAME          VARCHAR2(1024)  NOT NULL");
			sb.append(", IMDB          VARCHAR2(1024)       NOT NULL");
			sb.append(", OSHASH          VARCHAR2(1024)       NOT NULL");
			sb.append(", THUMB         VARCHAR2(1024)");
			for(String word : KeyWords) {
				sb.append(", "+word+"                VARCHAR2(1024)");
			}
			sb.append(", primary key (ID))");
			executeUpdate(conn, sb.toString());
			sb=new StringBuilder();
			sb.append("CREATE TABLE CAST (");
			sb.append(" ID                INT AUTO_INCREMENT");
			sb.append(", CAST  VARCHAR2(1024)  NOT NULL");
			sb.append(", MOVIE INT");
			sb.append(", THUMB VARCHAR2(1024)");
			sb.append(", primary key (ID))");
			executeUpdate(conn, sb.toString());
		} catch (SQLException se) {
			PMS.info("create mi tb error "+se);
		} finally {
			close(conn);
		}
	}
	
	public void discoverChildren() {
		for(String word : KeyWords) {
			String sql="SELECT "+word+" from FILES";
			if(word.equalsIgnoreCase("rating"))
				sql=sql+" order by rating desc";
			MovieDBFolder m =new MovieDBFolder(word,sql);
			addChild(m);
		}
		// we add the cast separatly...
		MovieDBFolder m = new MovieDBFolder("Cast","SELECT CAST,THUMB from CAST");
		m.cast();
		addChild(m);
	}
	
	private static String ucFirst(String str) {
		char first=str.charAt(0);
		return String.valueOf(first).toUpperCase()+str.substring(1);
	}
	
	private static String fixStr(String s) {
		if(StringUtils.isEmpty(s))
			return "";
		return ucFirst(s.trim());
	}
	
	private static String fixRating(String r) {
		int pos=r.indexOf("/");
		if(pos!=-1)
			r=r.substring(0,pos).trim();
		return fixStr(r);
	}
	
	public static void add(DLNAResource res,String imdb,String genres,
			   String title,String rating,
			   String director,String agerating,
			   ArrayList<String> cast,String thumb,
			   String hash) {
		if(res==null)
			return;
		String file=res.getSystemName();
		add(file,imdb,genres,title,rating,director,agerating,cast,
			thumb,hash);
	}
	
	
	public static void add(String file,String imdb,String genres,
						   String title,String rating,
						   String director,String agerating,
						   ArrayList<String> cast,String thumb,
						   String hash) {
		if(!MovieInfo.movieDB())
			return;
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		ResultSet rs = null;
		JdbcConnectionPool cp=getDBConnection();
		int id=0;
		try {
			conn = cp.getConnection();
			ps = conn.prepareStatement("INSERT INTO FILES(FILENAME, IMDB, OSHASH,GENRE, TITLE, DIRECTOR, RATING, AGERATING, THUMB) VALUES (?,?, ?, ?, ?, ?, ?, ?,?)",
									   Statement.RETURN_GENERATED_KEYS);
			ps1 = conn.prepareStatement("INSERT INTO CAST(CAST, MOVIE, THUMB) VALUES (?,?,?)");
			String[] tmp=genres.split(",");
			for(int i=0;i<tmp.length;i++) {
				ps.setString(1, file);
				ps.setString(2, imdb);
				ps.setString(3, fixStr(hash));
				ps.setString(4, fixStr(tmp[i]));
				ps.setString(5, fixStr(title));
				ps.setString(6, fixStr(director));
				ps.setString(7, fixRating(rating));
				ps.setString(8, fixStr(agerating));
				ps.setString(9, thumb);
				ps.executeUpdate();
				rs=ps.getGeneratedKeys();
				rs.next();
				if(id==0)
					id=rs.getInt(1);
			}
			// Build Cast array
			for(int j=0;j<cast.size();j++) {		
				String t=cast.remove(0);
				String name=cast.remove(0);
				cast.remove(0);
				j+=3;
				ps1.setString(1, name);
				ps1.setInt(2, id);
				ps1.setString(3, t);
				ps1.executeUpdate();	
			}
		} catch (Exception e) {
			PMS.info("insert into mdb "+e);
		} finally {
			try {
				if(ps!=null)
					ps.close();
				if(ps1!=null)
					ps1.close();
				if(rs!=null)
					rs.close();
			} catch (SQLException e) {
				PMS.info("error insert");
			}
			close(conn);
		}
	}
	
	public synchronized void stopScanLibrary() {
		if (scanner != null && scanner.isAlive()) {
			
		}
	}
	
	public synchronized boolean isScanLibraryRunning() {
		return scanner != null && scanner.isAlive();
	}
	
	public synchronized void scanLibrary(String path) {
		PMS.info("scan lib");
		if(!MovieInfo.movieDB())
			return;
		scanPath=path;
		PMS.getConfiguration().setCustomProperty("movieinfo.scan_path", scanPath);
		try {
			PMS.getConfiguration().save();
		} catch (ConfigurationException e) {
		}
		if (scanner == null) {
			scanner = new Thread(this, "Library Scanner");
			scanner.start();
		} else if (scanner.isAlive()) {
		} else {
			scanner = new Thread(this, "Library Scanner");
			scanner.start();
		}
	}
	
	private boolean alreadyScanned(File f) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean r=false;
		JdbcConnectionPool cp=getDBConnection();
		try {
		conn = cp.getConnection();
		ps = conn.prepareStatement("SELECT FILENAME from FILES WHERE FILENAME = ?");
		ps.setString(1, f.getAbsolutePath());
		rs=ps.executeQuery();
		if(rs.next())
			r= true;
		} catch (Exception e) {
			PMS.info("error in already scan "+e);
		} finally {
			try {
				if(ps!=null)
					ps.close();
				if(rs!=null)
					rs.close();
			} catch (SQLException e) {
			}
		}
		close(conn);	
		return r;
	}
	
	private void scanDir(File dir) {
		File[] files=dir.listFiles();
		for(File f : files) {
			try {
				if(alreadyScanned(f)) {
					continue;
				}
				String hash=OpenSubs.getHash(f);
				if(StringUtils.isEmpty(hash))
					continue;
				String imdb=OpenSubs.fetchImdbId(hash);
				if(StringUtils.isEmpty(imdb)) {
					PMS.info("couldn't fetch imdbid "+f);
					continue;
				}
				if(!imdb.startsWith("tt"))
					imdb="tt"+imdb;
				FileMovieInfoVirtualFolder fmf =new FileMovieInfoVirtualFolder("IMDB INFO",null,
													0,0,imdb,new RealFile(f));
				fmf.setHash(hash);
				fmf.gather();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void run() {
		File[] dirs;
		if(scanner==null) // weird
			return;
		if(StringUtils.isEmpty(scanPath))
			dirs=PMS.get().getFoldersConf();
		else {
			String[] foldersArray = scanPath.split(",");
			dirs=new File[foldersArray.length];
			for(int i=0;i<foldersArray.length;i++) {
				dirs[i]=new File(foldersArray[i]);
			}
		}
		for(File f : dirs) {
			if(!f.exists())
				continue;
			scanDir(f);
		}
		scanner=null;
	}
}
