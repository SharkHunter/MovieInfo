package net.pms.movieinfo;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.RealFile;
import net.pms.external.AdditionalFolderAtRoot;
import net.pms.external.AdditionalResourceFolderListener;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private MovieInfoConfiguration configuration;
	private static final Logger LOGGER = LoggerFactory.getLogger(MovieInfo.class);

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

	private JTextField pluginsField;
	private JSpinner maxNumberOfActorsField;
	private JCheckBox downloadCoverField;
	private JTextField displayInfoField;
	private JSpinner plotLineLengthField;
	private JTextField filterField;
	private JTextField ScanPath;

	@Override
	public JComponent config() {
		FormLayout layout = new FormLayout("left:pref, 2dlu, p,2dlu, p,2dlu, p, 2dlu, p, 2dlu, p,2dlu, p,200dlu, pref:grow", //$NON-NLS-1$
				"p, 5dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p,0:grow"); //$NON-NLS-1$
		PanelBuilder builder = new PanelBuilder(layout);
		builder.border(Borders.EMPTY);
		builder.opaque(false);

		CellConstraints cc = new CellConstraints();

		JComponent cmp = builder.addSeparator("MOVIE INFO CONFIG",  cc.xy(1, 1));
	      cmp = (JComponent) cmp.getComponent(0);
	      cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

	    pluginsField = new JTextField();
	    pluginsField.setEnabled(false); // Until MovieInfoConfiguration.save() is implemented
		if (configuration.getPlugins() != null)
			pluginsField.setText(configuration.getPlugins());
		pluginsField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				configuration.setPlugins(pluginsField.getText());
			}
		});
		builder.addLabel("Plugins to use (in prioritized order):", cc.xy(1, 3));
		builder.add(pluginsField, cc.xyw(3,3,12));

		maxNumberOfActorsField = new JSpinner(new SpinnerNumberModel());
	    maxNumberOfActorsField.setEnabled(false); // Until MovieInfoConfiguration.save() is implemented
		maxNumberOfActorsField.setValue(configuration.getMaxNumberOfActors());
		maxNumberOfActorsField.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setMaxNumberOfActors((Integer) maxNumberOfActorsField.getValue());
			}
		});
		builder.addLabel("Maximum number of actors to display:", cc.xy(1, 5));
		builder.add(maxNumberOfActorsField, cc.xy(3, 5));

		downloadCoverField = new JCheckBox();
	    downloadCoverField.setEnabled(false); // Until MovieInfoConfiguration.save() is implemented
		downloadCoverField.setSelected(configuration.getDownloadCover());
		downloadCoverField.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setDownloadCover(downloadCoverField.isSelected());
			}
		});
		builder.addLabel("Download cover to movie folder:", cc.xy(5, 5));
		builder.add(downloadCoverField, cc.xy(7, 5));

		displayInfoField = new JTextField();
	    displayInfoField.setEnabled(false); // Until MovieInfoConfiguration.save() is implemented
		if (configuration.getDisplayInfo() != null) {
			displayInfoField.setText(configuration.getDisplayInfoAsString());
		}
		displayInfoField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO: Needs verification logics
				configuration.setDisplayInfoFromString(displayInfoField.getText());
			}
		});
		builder.addLabel("DisplayInfo:", cc.xy(13, 5));
		builder.add(displayInfoField, cc.xyw(14, 5,2));

		plotLineLengthField = new JSpinner(new SpinnerNumberModel());
	    plotLineLengthField.setEnabled(false); // Until MovieInfoConfiguration.save() is implemented
		plotLineLengthField.setValue(configuration.getPlotLineLength());
		plotLineLengthField.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				configuration.setPlotLineLength((Integer) plotLineLengthField.getValue());
			}
		});
		builder.addLabel("Plot line length:", cc.xy(9, 5));
		builder.add(plotLineLengthField, cc.xy(11, 5));

		filterField = new JTextField();
	    filterField.setEnabled(false); // Until MovieInfoConfiguration.save() is implemented
		if (configuration.getFilters() != null) {
			filterField.setText(configuration.getFiltersAsString());
		}
		filterField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO: Needs verification logics
				configuration.setFiltersFromString(filterField.getText());
			}
		});
		ScanPath=new JTextField();
		builder.addLabel("Filter:", cc.xy(1, 7));
		builder.add(filterField, cc.xyw(3, 7,12));
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
		LOGGER.info("{MovieInfo} Starting MovieInfo plugin");
		mdb=null;
		configuration=new MovieInfoConfiguration();
		if(movieDB()){
			mdb=new MovieDB();
		}
	}

	public static MovieInfoConfiguration configuration() {
		return inst.configuration;
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
