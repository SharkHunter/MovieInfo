package net.pms.movieinfo;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import net.pms.dlna.DLNAMediaDatabase;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.RealFile;
import net.pms.external.AdditionalFolderAtRoot;
import net.pms.external.AdditionalResourceFolderListener;
import net.pms.newgui.LooksFrame;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import net.pms.Messages;
import net.pms.PMS;


import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class MovieInfo implements AdditionalFolderAtRoot,
								  AdditionalResourceFolderListener {
	private String Plugins="IMDB,FILMWEB,CSFD,MOVIEMETER,ALLOCINE,MYMOVIES,OFDB,FILMDELTA,SENSACINE,ONFILM,KINOPOISK";
	private String NumberOfActors="99";
	private String DisplayInfo="title,rating,tagline,genre,plot,cast,director";
	private String Linelength="60";
	private String Filter="filter";
	private MovieInfoCfg cfg;
	
	public void addAdditionalFolder(DLNAResource currentResource, DLNAResource child) {
		if(child instanceof RealFile) {
			RealFile rf=(RealFile)child;
			String imdb=extractImdb(rf);
			/*if(currentResource instanceof MovieInfoVirtualFolder)
			return;*/
			ResourceExtension ext = new ResourceExtension(currentResource,imdb);
			ext.addChild(child);
		}
	}
	
	public static String extractImdb(DLNAResource res) {
		if(res instanceof RealFile) 
			return extractImdb((RealFile)res);
		else
			return null;
	}
	
	public static String extractImdb(RealFile file) {
		String fName=file.getFile().getAbsolutePath();
		Pattern re=Pattern.compile("_imdb([^_]+)_");
		Matcher m=re.matcher(fName);
		String ret="";
		while(m.find()) {
			ret=m.group(1);
			if(!ret.startsWith("tt"))
				ret="tt"+ret;
		}
		return ret;
	}
	
	@Override
	public void shutdown() {
		
	}
	
	@Override
	public String name() {
		return "Movie Info plugin";
	}
	
	private JTextField PluginsField;
	private JTextField NumberOfActorsField;
	private JCheckBox CoverField;
	private JTextField DisplayInfoField;
	private JTextField LinelengthField;
	private JTextField FilterField;
	private JTextField ScanPath;

	@Override
	public JComponent config() {
		FormLayout layout = new FormLayout("left:pref, 2dlu, p,2dlu, p,2dlu, p, 2dlu, p, 2dlu, p,2dlu, p,200dlu, pref:grow", //$NON-NLS-1$
				"p, 5dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p,0:grow"); //$NON-NLS-1$
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(Borders.EMPTY_BORDER);
		builder.setOpaque(false);

		CellConstraints cc = new CellConstraints();
		
		JComponent cmp = builder.addSeparator("MOVIE INFO CONFIG",  cc.xy(1, 1));
	      cmp = (JComponent) cmp.getComponent(0);
	      cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

	    PluginsField = new JTextField();
		if (Plugins != null)
			PluginsField.setText(Plugins);
		PluginsField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				PMS.getConfiguration().setCustomProperty("cmd",
						PluginsField.getText());
			}

		});
		builder.addLabel("Plugins:", cc.xy(1, 3));
		builder.add(PluginsField, cc.xyw(3,3,12));

		NumberOfActorsField = new JTextField();
		if (NumberOfActors != null)
			NumberOfActorsField.setText(NumberOfActors);
		NumberOfActorsField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				PMS.getConfiguration().setCustomProperty(
						"cmd", NumberOfActorsField.getText());
			}

		});
		builder.addLabel("NumberOfActors:", cc.xy(1, 5));
		builder.add(NumberOfActorsField, cc.xy(3, 5));
		
		CoverField = new JCheckBox();
		CoverField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				PMS.getConfiguration().setCustomProperty(
						"cmd", CoverField.getText());
			}
			
		});
		builder.addLabel("Download Cover To Folder:", cc.xy(5, 5));
		builder.add(CoverField, cc.xy(7, 5));
		
		DisplayInfoField = new JTextField();
		if (DisplayInfo != null)
			DisplayInfoField.setText(DisplayInfo);
		DisplayInfoField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				PMS.getConfiguration().setCustomProperty("cmd",
						DisplayInfoField.getText());
			}
			
		});
		builder.addLabel("DisplayInfo:", cc.xy(13, 5));
		builder.add(DisplayInfoField, cc.xyw(14, 5,2));
		
		LinelengthField = new JTextField();
		if (Linelength != null)
			LinelengthField.setText(Linelength);
		LinelengthField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				PMS.getConfiguration().setCustomProperty(
						"cmd", LinelengthField.getText());
			}
			
		});
		builder.addLabel("Linelength:", cc.xy(9, 5));
		builder.add(LinelengthField, cc.xy(11, 5));
		
		FilterField = new JTextField();
		if (Filter != null)
			FilterField.setText(Filter);
		FilterField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				PMS.getConfiguration().setCustomProperty(
						"cmd", FilterField.getText());
			}
			
		});
		ScanPath=new JTextField();
		builder.addLabel("Filter:", cc.xy(1, 7));
		builder.add(FilterField, cc.xyw(3, 7,12));
		builder.addLabel("Scan path:", cc.xy(1, 9));
		builder.add(ScanPath, cc.xyw(3, 9,12));
		ScanPath.setText((String)PMS.getConfiguration().getCustomProperty("movieinfo.scan_path"));
		JButton scan =new JButton("Scan files");
		scan.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mdb!=null) {
					if (!mdb.isScanLibraryRunning()) {
						int option = JOptionPane.showConfirmDialog(
								(Component) PMS.get().getFrame(),
								Messages.getString("FoldTab.3") + Messages.getString("FoldTab.4"),
								Messages.getString("Dialog.Question"),
								JOptionPane.YES_NO_OPTION);
						if (option == JOptionPane.YES_OPTION) {
							mdb.scanLibrary(ScanPath.getText());
						}
					} else {
						int option = JOptionPane.showConfirmDialog(
								(Component) PMS.get().getFrame(),
								Messages.getString("FoldTab.10"),
								Messages.getString("Dialog.Question"),
								JOptionPane.YES_NO_OPTION);
						if (option == JOptionPane.YES_OPTION) {
							mdb.stopScanLibrary();
						}
					}
				}
			}
		});
		builder.add(scan,cc.xy(1, 11));

		return builder.getPanel();
	}
	
	private MovieDB mdb;
	private static MovieInfo inst;
	
	public MovieInfo() {
		inst = this;
		PMS.info("Starting MovieInfo");
		mdb=null;
		cfg=new MovieInfoCfg();
		if(movieDB()){
			mdb=new MovieDB();
		}
	}
	
	public static MovieInfoCfg cfg() {
		return inst.cfg;
	}
	
	public static boolean movieDB() {
		String s=(String)PMS.getConfiguration().getCustomProperty("movieinfo.movieDB");
		if(StringUtils.isNotEmpty(s))
			return s.equalsIgnoreCase("true");
		return false;
	}

	@Override
	public DLNAResource getChild() {
		return mdb;
	}
	
	public static MovieDB getDB() {
		return inst.mdb;
	}

}
