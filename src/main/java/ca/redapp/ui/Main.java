/***********************************************************************
 * REDapp - Main.java
 * Copyright (C) 2015-2019 The REDapp Development Team
 * Homepage: http://redapp.org
 * 
 * REDapp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * REDapp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REDapp. If not see <http://www.gnu.org/licenses/>. 
 **********************************************************************/

package ca.redapp.ui;

import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ca.hss.general.DecimalUtils;
import ca.hss.general.WebDownloader;
import ca.hss.math.Convert.UnitSystem;
import ca.hss.text.TranslationCallback;
import ca.hss.times.WTimeSpan;
import ca.hss.times.TimeZoneInfo;
import ca.hss.times.WorldLocation;
import ca.hss.times.WorldLocation.TimeZoneGroup;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RGroupBox;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;
import ca.redapp.util.ConvertUtils;
import ca.redapp.util.Geolocate;
import ca.redapp.util.LineEditHelper;
import ca.redapp.util.RPreferences;
import ca.redapp.util.ResourceManager;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static ca.hss.math.General.DEGREE_TO_RADIAN;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class Main implements FocusListener, DocumentListener {
	public static ResourceManager resourceManager;
	JFrame frmRedapp;
	static RPreferences prefs;
	static boolean shouldUseMqtt;

	private RTextField txtLongitude;
	private RTextField txtLatitude;
	private JComboBox<TimeZoneInfo> comboTimeZone;
	private RButton btnLocate;
	private JSpinner spinnerDate;
	private JTabbedPane tabPane;

	private final List<REDappTab> tabs = new ArrayList<REDappTab>();
	public FwiTab fwiTab;
	public StatsTab statsTab;
	public MapTab mapTab;
	public FbpTab fbpTab;
	public SpottingTab spottingTab;
	public REDappTab mqttTab;
	private Geolocate locator;
	private NetworkConnectionThread m_StartupThread;
	
	//Redmine 713
	//private BusyDialog m_StartupBusyDialog = null;
	
	boolean hasNetworkConnection = true;
	private static List<String> vmArgs = null;

	static {
		prefs = new RPreferences(Preferences.userRoot().node("ca.hss.app.redapp.ui.Main"));
		int currentVersion = prefs.getInt("current_version", 0);
		if (currentVersion < 1) {
			prefs.remove("current");
			prefs.putInt("current_version", 1);
		}
		String defLanguage = Locale.getDefault().getISO3Language();
		int def = 0;
		if (defLanguage.toLowerCase().contains("fr"))
			def = 1;
		int lang = prefs.getInt("language", def);
		if (lang == 0)
			resourceManager = new ResourceManager(Locale.ENGLISH);
		else
			resourceManager = new ResourceManager(Locale.CANADA_FRENCH);
		DecimalUtils.setLocale(resourceManager.loc);
		TranslationCallback.instance = resourceManager;
		shouldUseMqtt = prefs.getBoolean("mqtt_active_2", false);
		if (shouldUseMqtt) {
			Class<?> cls = null;
			try {
				cls = Class.forName("org.eclipse.paho.client.mqttv3.MqttCallback");
			}
			catch (ClassNotFoundException ignored) { }
			shouldUseMqtt = cls != null;
		}
	}

	/**
	 * Create the application.
	 * @param VmArgs The arguments passed to java.
	 */
	public Main(List<String> VmArgs) {
		Main.vmArgs = VmArgs;
		initialize();

		if (Launcher.mac.isMac()) {
			Image img = Toolkit.getDefaultToolkit().getImage(
					Main.class.getResource(resourceManager
							.getImagePath("ui.icon.window.redapp")));
			Launcher.mac.setDockIconImage(img);
		}

		frmRedapp.addWindowListener(new WindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				//saveAllValues was failing, and consequently the program could not close.  Added a try catch to allow the program to close in that circumstance. --Dylan 2025-01-13
				try {
					saveAllValues();
				} catch (Exception ignored){
					;
				}
				tabs.forEach(REDappTab::onClosing);
				if (Launcher.mac.isMac())
					System.exit(0);
			}

			@Override
			public void windowClosed(WindowEvent e) { }
			@Override
			public void windowActivated(WindowEvent e) { }
			@Override
			public void windowOpened(WindowEvent e) { }
			@Override
			public void windowIconified(WindowEvent e) { }
			@Override
			public void windowDeiconified(WindowEvent e) { }
			@Override
			public void windowDeactivated(WindowEvent e) { }
		});

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if (evt.getNewValue() instanceof JTextField) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JTextField textField = (JTextField)evt.getNewValue();
							textField.selectAll();
						}
					});
				}
				else if (evt.getNewValue() instanceof RTextField) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							RTextField textField = (RTextField)evt.getNewValue();
							textField.selectAll();
						}
					});
				}
			}
		});

		for (String arg : Main.vmArgs) {
			if (arg.toLowerCase().contains("-dhttp.proxyhost")) {
				//Why would they set this from the command line,
				//are they trying to break stuff?
				int index = tabPane.indexOfComponent(mapTab);
				tabPane.setEnabledAt(index, false);
				tabPane.setToolTipTextAt(index, Main.resourceManager.getString("ui.label.map.proxydisable"));
				if (Launcher.debugJavaFX)
					System.out.println("Disabling map tab due to unsupported command line proxy specification");
			}
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		float dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		
		if (dpi > 96f && isWindows()){
			setUIFont(new javax.swing.plaf.FontUIResource("Label.font", Font.PLAIN, 11));
		}

		frmRedapp = new JFrame();
		frmRedapp.setResizable(false);
		frmRedapp.setTitle(Main.resourceManager.getString("ui.app.title"));
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage(
				Main.class.getResource(resourceManager
						.getImagePath("ui.icon.window.redapp"))));
		icons.add(Toolkit.getDefaultToolkit().getImage(
				Main.class.getResource(resourceManager
						.getImagePath("ui.icon.window.redapp20"))));
		icons.add(Toolkit.getDefaultToolkit().getImage(
				Main.class.getResource(resourceManager
						.getImagePath("ui.icon.window.redapp40"))));
		frmRedapp.setIconImages(icons);
		if (isLinux())
			frmRedapp.setBounds(0, 0, 993, 690);
		else if (Launcher.javaVersion.major < 9)
			frmRedapp.setBounds(0, 0, 993, 674);
		else
			frmRedapp.setBounds(0, 0, 1003, 679);
		frmRedapp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmRedapp.getContentPane().setLayout(null);

		frmRedapp.setFocusTraversalPolicy(new FocusTraversalPolicy() {
			@Override
			public Component getLastComponent(Container aContainer) {
				Component c = getCurrentTab();
				if (c instanceof REDappTab)
					return ((REDappTab)c).getLastComponent(aContainer);
				return null;
			}

			@Override
			public Component getFirstComponent(Container aContainer) {
				Component c = getCurrentTab();
				if (c instanceof REDappTab)
					return ((REDappTab)c).getFirstComponent(aContainer);
				return null;
			}

			@Override
			public Component getDefaultComponent(Container aContainer) {
				Component c = getCurrentTab();
				if (c instanceof REDappTab)
					return ((REDappTab)c).getDefaultComponent(aContainer);
				return null;
			}

			@Override
			public Component getComponentBefore(Container aContainer, Component aComponent) {
				if (txtLongitude.equalsForTabs(aComponent))
					return txtLatitude.componentForTabs();
				if (txtLatitude.equalsForTabs(aComponent))
					return comboTimeZone;
				if (aComponent == comboTimeZone)
					return ((JSpinner.DefaultEditor)spinnerDate.getEditor()).getTextField();
				if (aComponent == ((JSpinner.DefaultEditor)spinnerDate.getEditor()).getTextField())
					return txtLongitude.componentForTabs();
				Component c = getCurrentTab();
				if (c instanceof REDappTab)
					return ((REDappTab)c).getComponentBefore(aContainer, aComponent);
				return null;
			}

			@Override
			public Component getComponentAfter(Container aContainer, Component aComponent) {
				if (aComponent == ((JSpinner.DefaultEditor)spinnerDate.getEditor()).getTextField())
					return comboTimeZone;
				if (aComponent == comboTimeZone)
					return txtLatitude.componentForTabs();
				if (txtLatitude.equalsForTabs(aComponent))
					return txtLongitude.componentForTabs();
				if (txtLongitude.equalsForTabs(aComponent))
					return ((JSpinner.DefaultEditor)spinnerDate.getEditor()).getTextField();
				Component c = getCurrentTab();
				if (c instanceof REDappTab)
					return ((REDappTab)c).getComponentAfter(aContainer, aComponent);
				return null;
			}
		});

		Point p = frmRedapp.getLocation();
		int sx = prefs.getInt("mainwin_x", p.x);
		int sy = prefs.getInt("mainwin_y", p.y);
		Rectangle2D result = new Rectangle2D.Double();
		GraphicsEnvironment localGE = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		for (GraphicsDevice gd : localGE.getScreenDevices()) {
			for (GraphicsConfiguration config : gd.getConfigurations()) {
				Rectangle2D.union(result, config.getBounds(), result);
			}
		}
		if (result.contains(sx, sy))
			frmRedapp.setBounds(sx, sy, frmRedapp.getWidth(),
					frmRedapp.getHeight());

		/* Redmine 713
		m_StartupBusyDialog = new BusyDialog(frmRedapp);
		m_StartupBusyDialog.setModal(false);
		m_StartupBusyDialog.setVisible(true);
		*/

		RGroupBox panelDate = new RGroupBox();
		panelDate.setBorderColour(new Color(168, 69, 69));
		panelDate.setTextColour(new Color(168, 69, 69));
		panelDate.setBackgroundColour(new Color(245, 245, 245));
		panelDate.setText(resourceManager.getString("ui.label.header.datetime"));
		panelDate.setBounds(8, 10, 473, 81);
		frmRedapp.getContentPane().add(panelDate);
		
		RLabel lblDate = new RLabel(resourceManager.getString("ui.label.header.date"));
		lblDate.setBounds(10, 20, 121, 21);
		panelDate.add(lblDate);

		spinnerDate = new JSpinner();
		spinnerDate.setLocale(resourceManager.loc);
		spinnerDate.setModel(new SpinnerDateModel(new Date(1389938400000L),
				null, null, Calendar.DAY_OF_YEAR));
		spinnerDate.setEditor(new JSpinner.DateEditor(spinnerDate, "MMMM d, yyyy"));
		spinnerDate.setBounds(150, 19, 301, 22);
		spinnerDate.addChangeListener((ChangeListener)EventHandler.create(ChangeListener.class, this, "dateChanged"));
		JComponent comp = spinnerDate.getEditor();
		if (comp instanceof JSpinner.DefaultEditor) {
			JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)comp;
			editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
			if (isLinux())
				editor.getTextField().setFont(editor.getTextField().getFont().deriveFont(11.0f));
		}
		panelDate.add(spinnerDate);

		RLabel lblTimeZone = new RLabel(resourceManager.getString("ui.label.header.timezone"));
		lblTimeZone.setBounds(10, 50, 121, 21);
		panelDate.add(lblTimeZone);

		comboTimeZone = new JComboBox<TimeZoneInfo>();
		comboTimeZone.setBounds(150, 50, 301, 22);
		comboTimeZone.addActionListener((e) -> timeZoneChanged());
		populateTimezoneComboBox();
		panelDate.add(comboTimeZone);

		RGroupBox panelLocation = new RGroupBox();
		panelLocation.setBorderColour(new Color(168, 69, 69));
		panelLocation.setTextColour(new Color(168, 69, 69));
		panelLocation.setBackgroundColour(new Color(245, 245, 245));
		panelLocation.setText(resourceManager.getString("ui.label.header.ignition"));
		panelLocation.setBounds(490, 10, 491, 81);
		frmRedapp.getContentPane().add(panelLocation);

		RLabel lblLatitude = new RLabel(
				resourceManager.getString("ui.label.header.latitude"));
		lblLatitude.setBounds(10, 20, 91, 21);
		panelLocation.add(lblLatitude);

		RLabel lblLongitude = new RLabel(
				resourceManager.getString("ui.label.header.longitude"));
		lblLongitude.setBounds(10, 50, 91, 21);
		panelLocation.add(lblLongitude);

		txtLongitude = new RTextField();
		txtLongitude.setBounds(110, 49, 111, 22);
		panelLocation.add(txtLongitude);
		txtLongitude.setColumns(10);
		txtLongitude.addFocusListener(this);
		txtLongitude.getDocument().addDocumentListener(this);

		txtLatitude = new RTextField();
		txtLatitude.setBounds(110, 19, 111, 22);
		panelLocation.add(txtLatitude);
		txtLatitude.setColumns(10);
		txtLatitude.addFocusListener(this);
		txtLatitude.getDocument().addDocumentListener(this);

		btnLocate = new RButton(resourceManager.getString("ui.label.header.locate"));
		Dimension d = btnLocate.getSize();
		btnLocate.setBounds(471 - d.width, 20, 121, 41);
		btnLocate.addActionListener((e) -> locate());
		panelLocation.add(btnLocate);

		tabPane = new JTabbedPane(JTabbedPane.TOP);
		if (isMac())
			tabPane.setBounds(2, 94, 987, 513);
		else
			tabPane.setBounds(10, 100, 971, 501); 
		tabPane.addChangeListener((ChangeListener)EventHandler.create(ChangeListener.class, this, "tabChanged"));
		frmRedapp.getContentPane().add(tabPane);
		
        org.openstreetmap.josm.data.Preferences wmsPrefs = org.openstreetmap.josm.data.Preferences.main();
        org.openstreetmap.josm.spi.preferences.Config.setPreferencesInstance(wmsPrefs);
        org.openstreetmap.josm.spi.preferences.Config.setBaseDirectoriesProvider(org.openstreetmap.josm.data.preferences.JosmBaseDirectories.getInstance());
        org.openstreetmap.josm.spi.preferences.Config.setUrlsProvider(org.openstreetmap.josm.data.preferences.JosmUrls.getInstance());
		
		initFwi(tabPane);

		SwingUtilities.invokeLater(() -> {
			initFbp(tabPane);
			initStats(tabPane);
			initMap(tabPane);
			initSpotting(tabPane);

			initMqtt(tabPane);

			statsTab.addStatsTabListener(fbpTab);
			if (Boolean.parseBoolean(prefs.getString("saveValues", "true"))) {
				fbpTab.loadAllValues();
				fwiTab.loadAllValues();
				spottingTab.loadAllValues(prefs);
			}
	
			JPanel btnPanel = new JPanel();
			btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			btnPanel.setBounds(300, 595, 687, 51);
			btnPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			frmRedapp.getContentPane().add(btnPanel);
	
			RButton btnBugsAndSupport = new RButton(resourceManager.getString("ui.label.footer.bugs"));
			btnBugsAndSupport.setBounds(860, 600, 121, 41);
			btnBugsAndSupport.addActionListener((e) -> bugsAndSupportButton());
			btnPanel.add(btnBugsAndSupport);
	
			RButton btnSettings = new RButton(resourceManager.getString("ui.label.footer.settings"));
			btnSettings.addActionListener((e) -> showSettings());
			btnSettings.setBounds(618, 600, 121, 41);
			btnPanel.add(btnSettings);
	
			RButton btnAssumptions = new RButton(resourceManager.getString("ui.label.footer.assumptions"));
			btnAssumptions.addActionListener((e) -> Assumptions.showAssumptionsDialog(Main.this, prefs));
			btnAssumptions.setBounds(497, 600, 121, 41);
			btnPanel.add(btnAssumptions);
	
			RButton btnAbout = new RButton(resourceManager.getString("ui.label.footer.about"));
			btnAbout.setBounds(860, 600, 121, 41);
			btnAbout.addActionListener((e) -> openAboutDialog());
			btnPanel.add(btnAbout);
	
			if (isMac()) {
				int index = tabPane.indexOfComponent(mapTab);
				tabPane.setEnabledAt(index, false);
				tabPane.setToolTipTextAt(index, resourceManager.getString("ui.label.map.nomap"));
				if (Launcher.debugJavaFX) {
					int value = 0;
					value = value | 2;
					if (isMac())
						value = value | 4;
					System.out.println("Disabling the map tab due to unsupported platform " + value);
				}
			}
	
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					initGeneral();
				}
			});
		});
	}

	/**
	 * Get the primary window.
	 * @return
	 */
	public Window getForm() {
		return frmRedapp;
	}

	/**
	 * Get the latitude.
	 * @return
	 */
	public Double getLatitude() {
		return LineEditHelper.getDegreesFromLineEdit(txtLatitude);
	}

	/**
	 * Set the latitude.
	 * @param deg
	 */
	public void setLatitude(double deg) {
		txtLatitude.setText(ConvertUtils.formatDegrees(deg));
	}

	/**
	 * Get the latitude as a string.
	 * @return
	 */
	public String getLatitudeString() {
		return txtLatitude.getText();
	}

	/**
	 * Get the longitude.
	 * @return
	 */
	public Double getLongitude() {
		return LineEditHelper.getDegreesFromLineEdit(txtLongitude);
	}

	/**
	 * Set the longitude.
	 * @param deg
	 */
	public void setLongitude(double deg) {
		txtLongitude.setText(ConvertUtils.formatDegrees(deg));
	}

	/**
	 * Get the longitude as a string.
	 * @return
	 */
	public String getLongitudeString() {
		return txtLongitude.getText();
	}

	/**
	 * Get the currently selected timezone.
	 */
	public TimeZoneInfo getSelectedTimeZone() {
		return (TimeZoneInfo)comboTimeZone.getSelectedItem();
	}
	
	public int curSelTimeZone() {
		return comboTimeZone.getSelectedIndex();
	}

	/**
	 * Get the currently set date.
	 */
	@SuppressWarnings("deprecation")
	public Date getDate() {
		Date dt = ((SpinnerDateModel)spinnerDate.getModel()).getDate();
		dt.setHours(0);
		dt.setMinutes(0);
		dt.setSeconds(0);
		return dt;
	}

	private void initGeneral() {
		//Redmine 713
		/*
		indexTab = tabPane.indexOfComponent(mapTab);
		tabPane.setEnabledAt(indexTab, false);
		tabPane.setToolTipTextAt(indexTab, resourceManager.getString("ui.label.header.internet.testing"));
		*/
		
		hasNetworkConnection = false;
		btnLocate.setVisible(false);
		tabPane.setSelectedComponent(fwiTab);
		
		int indexTab = tabPane.indexOfComponent(fwiTab);
		tabPane.setEnabledAt(indexTab, false);
		tabPane.setToolTipTextAt(indexTab, resourceManager.getString("ui.label.header.internet.testing"));

		fbpTab.setInternetConnected(false);
		
		this.spinnerDate.setValue(new Date());

		m_StartupThread = new NetworkConnectionThread(this);
		new Thread(m_StartupThread).start();

		if (!prefs.getBoolean("HideAssumptionsOnStartup", false)) {
			Assumptions.showAssumptionsDialog(this, prefs);
		}

		Double d = null;
		String l = prefs.getString("app_latitude", null);
		if (l != null) {
			try {
				d = Double.parseDouble(l);
			}
			catch (NumberFormatException ex) {
				d = null;
			}
		}
		if (d == null) {
			d = fbpTab.getCalculator().latitude;
		}
		txtLatitude.setText(ConvertUtils.formatDegrees(d));

		d = null;
		l = prefs.getString("app_longitude", null);
		if (l != null) {
			try {
				d = Double.parseDouble(l);
			}
			catch (NumberFormatException ex) {
				d = null;
			}
		}
		if (d == null) {
			d = fbpTab.getCalculator().longitude;
		}
		txtLongitude.setText(ConvertUtils.formatDegrees(d));
	}



	private void initFwi(JTabbedPane tabs) {
		fwiTab = new FwiTab(this);
		if (fbpTab != null) {
			int index = tabs.indexOfComponent(fbpTab);
			tabs.insertTab(Main.resourceManager.getString("ui.dlg.title.fwi"), null, fwiTab, null, index);
		}
		else
			tabs.addTab(Main.resourceManager.getString("ui.dlg.title.fwi"), null, fwiTab, null);
		this.tabs.add(fwiTab);
	}

	private void initFbp(JTabbedPane tabs) {
		fbpTab = new FbpTab(this);
		if (fwiTab != null) {
			int index = tabs.indexOfComponent(fwiTab);
			tabs.insertTab(Main.resourceManager.getString("ui.dlg.title.fbp"), null, fbpTab, null, index + 1);
		}
		else if (mapTab != null) {
			int index = tabs.indexOfComponent(mapTab);
			tabs.insertTab(Main.resourceManager.getString("ui.dlg.title.fbp"), null, fbpTab, null, index);
		}
		else
			tabs.addTab(Main.resourceManager.getString("ui.dlg.title.fbp"), null, fbpTab, null);
		this.tabs.add(fbpTab);
	}

	private void initMap(JTabbedPane tabs) {
		mapTab = new MapTab(this);
		if (fbpTab != null) {
			int index = tabs.indexOfComponent(fbpTab);
			tabs.insertTab(Main.resourceManager.getString("ui.dlg.title.map"), null, mapTab, null, index + 1);
		}
		else if (spottingTab != null) {
			int index = tabs.indexOfComponent(spottingTab);
			tabs.insertTab(Main.resourceManager.getString("ui.dlg.title.map"), null, mapTab, null, index);
		}
		else
			tabs.addTab(Main.resourceManager.getString("ui.dlg.title.map"), null, mapTab, null);
		this.tabs.add(mapTab);
	}

	private void initSpotting(JTabbedPane tabs) {
		spottingTab = new SpottingTab();
		if (mapTab != null) {
			int index = tabs.indexOfComponent(mapTab);
			tabs.insertTab(Main.resourceManager.getString("ui.dlg.title.spotting"), null, spottingTab, null, index + 1);
		}
		else if (statsTab != null) {
			int index = tabs.indexOfComponent(statsTab);
			tabs.insertTab(Main.resourceManager.getString("ui.dlg.title.spotting"), null, spottingTab, null, index);
		}
		else
			tabs.addTab(Main.resourceManager.getString("ui.dlg.title.spotting"), null, spottingTab, null);
		this.tabs.add(spottingTab);
	}

	private void initStats(JTabbedPane tabs) {
		statsTab = new StatsTab(this);
		if (spottingTab != null) {
			int index = tabs.indexOfComponent(spottingTab);
			tabs.insertTab(Main.resourceManager.getString("ui.dlg.title.stats"), null, statsTab, null, index + 1);
		}
		else
			tabs.addTab(Main.resourceManager.getString("ui.dlg.title.stats"), null, statsTab, null);
		this.tabs.add(statsTab);
	}
	
	private void initMqtt(JTabbedPane tabs) {
		if (shouldUseMqtt && mqttTab == null) {
			try {
				Class<?> cls = Class.forName("ca.redapp.ui.MqttTab");
				Constructor<?> ctor = cls.getConstructor(this.getClass());
				mqttTab = (REDappTab)ctor.newInstance(new Object[] { this });
			}
			catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
			if (mqttTab != null) {
				tabs.addTab(Main.resourceManager.getString("ui.dlg.title.mqtt"), null, mqttTab, null);
				this.tabs.add(mqttTab);
			}
		}
	}
	
	void showMqtt(boolean show) {
		if (show) {
			shouldUseMqtt = true;
			if (mqttTab == null)
				initMqtt(tabPane);
			else
				tabPane.addTab(Main.resourceManager.getString("ui.dlg.title.mqtt"), null, mqttTab, null);
			prefs.putBoolean("mqtt_active_2", true);
		}
		else {
			if (mqttTab != null) {
				int index = tabPane.indexOfTab(Main.resourceManager.getString("ui.dlg.title.mqtt"));
				if (index >= 0)
					tabPane.removeTabAt(index);
				prefs.putBoolean("mqtt_active_2", false);
				shouldUseMqtt = false;
			}
		}
	}

	private void populateTimezoneComboBox() {
		TimeZoneGroup g = TimeZoneGroup.fromId(prefs.getInt("regionindex", 0));
		TimeZoneInfo[] info = ca.hss.times.WorldLocation.getTimezones(g);

		comboTimeZone.setModel(new DefaultComboBoxModel<TimeZoneInfo>(info));
		initializeTimezoneComboBox(info);
	}

	public void initializeTimezoneComboBox(TimeZoneInfo[] timeZoneInfoList) {
		boolean set = false;
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("zzz");
		String tz = sdf.format(date);
		for (int i = 0; i < timeZoneInfoList.length; i++) {
			if (tz.equalsIgnoreCase(timeZoneInfoList[i].getCode())) {
				comboTimeZone.setSelectedIndex(i);
				set = true;
				break;
			}
		}

		if (!set) {
			Calendar c = Calendar.getInstance();
			int offset = (c.get(Calendar.ZONE_OFFSET) + c
					.get(Calendar.DST_OFFSET)) / (1000);
			WTimeSpan span = new WTimeSpan(offset);
			for (int i = 0; i < timeZoneInfoList.length; i++) {
				if (WTimeSpan.equal(timeZoneInfoList[i].getTimezoneOffset(), span)) {
					comboTimeZone.setSelectedIndex(i);
					set = true;
					break;
				}
			}
		}

		if (!set) {
			WorldLocation worldLocation = new WorldLocation();
			if (fwiTab == null) {
				worldLocation.setLatitude(DEGREE_TO_RADIAN(54.000012));
				worldLocation.setLongitude(DEGREE_TO_RADIAN(-115.000021));
			}
			else {
				worldLocation.setLatitude(fwiTab.fwiCalculations.getLatitude());
				worldLocation.setLongitude(fwiTab.fwiCalculations.getLongitude());
			}
			TimeZoneInfo timeZoneInfo = worldLocation.guessTimeZone((short) 0);

			for (int i = 0; i < timeZoneInfoList.length; i++) {
				if (timeZoneInfoList[i].compareTo(timeZoneInfo) == 0) {
					comboTimeZone.setSelectedIndex(i);
					set = true;
					break;
				}
			}
		}

		if (!set)
			comboTimeZone.setSelectedIndex(0);
	}

	public void reset() {
		Component c = getCurrentTab();
		if (c instanceof REDappTab) {
			((REDappTab)c).reset();
			if (c instanceof FbpTab)
				((FbpTab)c).saveAllValues();
			else if (c instanceof FwiTab)
				((FwiTab)c).saveAllValues();
		}
	}

	private void saveAllValues() {
		Point p = frmRedapp.getLocation();
		prefs.putInt("mainwin_x", p.x);
		prefs.putInt("mainwin_y", p.y);

		// FBP Values
		try {
			prefs.putDouble("app_latitude", LineEditHelper.getDegreesFromLineEdit(this.txtLatitude));
		} catch (NullPointerException e) {}
		
		try {
			prefs.putDouble("app_longitude", LineEditHelper.getDegreesFromLineEdit(this.txtLongitude));
		} catch (NullPointerException e) {}

		fwiTab.saveAllValues();
		fbpTab.saveAllValues();
		spottingTab.saveAllValues(prefs);
	}

	public void internetDetected() {
		synchronized (this) {
			mapTab.internetFound();
			

			if (WebDownloader.hasInternetConnection()) {
				locator = new Geolocate(this);
				
				int indexTab = tabPane.indexOfComponent(mapTab);
				tabPane.setEnabledAt(indexTab, true);
				tabPane.setToolTipTextAt(indexTab, "");
				
				hasNetworkConnection = true;
				btnLocate.setVisible(true);
				
				indexTab = tabPane.indexOfComponent(fwiTab);
				tabPane.setEnabledAt(indexTab, true);
				tabPane.setToolTipTextAt(indexTab, "");

				fbpTab.setInternetConnected(true);
			} else {
				/* Redmine 713
				hasNetworkConnection = false;
				btnLocate.setVisible(false);
				tabPane.setSelectedComponent(fwiTab);
				*/
				
				int index = tabPane.indexOfComponent(fwiTab);
				tabPane.setToolTipTextAt(index, resourceManager.getString("ui.label.header.internet.error.brief"));

				fbpTab.setInternetConnected(false);

				//m_StartupBusyDialog.setVisible(false);
				
				JOptionPane
						.showMessageDialog(
								null,
								Main.resourceManager.getString("ui.label.header.internet.error"),
								"Warning", JOptionPane.WARNING_MESSAGE);
			}
			
			/* Redmine 713
			m_StartupBusyDialog.setVisible(false);
			m_StartupBusyDialog = null;
			*/
			

			m_StartupThread = null;
		}
	}

	private void locate() {
		if (locator == null)
			return;
		String ip = null;
		String ipgetters[] = new String[] { "http://checkip.amazonaws.com/", "http://www.icanhazip.com/",
				"http://ipinfo.io/ip", "http://ipecho.net/plain",
				"http://ifconfig.me/ip" };
		try {
			//URL getip = new URL("http://api.exip.org/?call=ip");
			int i = 0;
			do {
				URL getip = new URL(ipgetters[i]);
				try (BufferedReader in = new BufferedReader(new InputStreamReader(
						getip.openStream()))) {
					ip = in.readLine();
				}
			} while (ip == null && i < ipgetters.length);
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		if (ip != null)
			locator.locate(ip);
		else
			JOptionPane.showMessageDialog(null,
					"Unable to obtain your IP address.", "Error",
					JOptionPane.WARNING_MESSAGE);
	}

	public void locationFound(Double lat, Double lng) {
		txtLatitude.setText(ConvertUtils.formatDegrees(lat));
		txtLongitude.setText(ConvertUtils.formatDegrees(lng));
	}

	public void openAboutDialog() {
		AboutDialog about = new AboutDialog(this);
		about.setVisible(true);
	}

	public void bugsAndSupportButton() {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(
						new URI("https://github.com/WISE-Developers/REDapp/issues/new/choose"));
			} catch (Exception e) {
				JOptionPane
						.showMessageDialog(
								null,
								"Unable to open a browser with https://github.com/WISE-Developers/REDapp/issues/new/choose",
								"Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("nix") || os.contains("nux")) {

				JOptionPane
						.showMessageDialog(
								null,
								"Unable to open the web browser. Please ensure libgnome2.0 is installed.",
								"Error", JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	public void showSettings() {
		Settings settings = new Settings(this, prefs);
		settings.setVisible(true);
	}

	public void settingsUpdated() {
		populateTimezoneComboBox();

		fwiTab.settingsUpdated();
		fbpTab.settingsUpdated();
		spottingTab.settingsUpdated();
		mapTab.settingsUpdated();
		statsTab.settingsUpdated();
		if (mqttTab != null)
			mqttTab.settingsUpdated();
	}

	public void formatLatLong() {
		txtLatitude.setText(txtLatitude.getText().replace('d', '\u00B0')
				.replace('m', '\'').replace('s', '"'));
		txtLongitude.setText(txtLongitude.getText().replace('d', '\u00B0')
				.replace('m', '\'').replace('s', '"'));
	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		formatLatLong();
	}

	private void latitudeChanged() {
		Double d = LineEditHelper.getDegreesFromLineEdit(txtLatitude);
		if (d != null) {
			if (d < -90.0 || d > 90.0) {
				LineEditHelper.lineEditHandleError(txtLatitude, Main.resourceManager.getString("ui.label.header.latitude.error"));
			}
			else {
				prefs.putString("app_latitude", d.toString());
			}
			for (int i = 0; i < tabs.size(); i++)
				tabs.get(i).onLocationChanged();
		}
	}

	private void longitudeChanged() {
		Double d = LineEditHelper.getDegreesFromLineEdit(txtLongitude);
		if (d != null) {
			if (d < -180.0 || d > 180.0) {
				LineEditHelper.lineEditHandleError(txtLongitude, Main.resourceManager.getString("ui.label.header.longitude.error"));
			}
			else {
				prefs.putString("app_longitude", d.toString());
			}
			for (int i = 0; i < tabs.size(); i++)
				tabs.get(i).onLocationChanged();
		}
	}

	public void timeZoneChanged() {
		for (int i = 0; i < tabs.size(); i++)
			tabs.get(i).onTimeZoneChanged();
	}

	public void dateChanged() {
		for (int i = 0; i < tabs.size(); i++)
			tabs.get(i).onDateChanged();
	}

	public void tabChanged() {
		for (int i = 0; i < tabs.size(); i++)
			tabs.get(i).onCurrentTabChanged();
		getCurrentTab();
	}

	public void setCurrentTab(JComponent comp) {
		tabPane.setSelectedComponent(comp);
	}

	public Component getCurrentTab() {
		return tabPane.getSelectedComponent();
	}

	public static boolean isMac() {
		return Launcher.mac.isMac();
	}

	public static volatile Boolean french = null;
	public static boolean isFrench() {
		if (french == null) {
			french = resourceManager.loc.getISO3Language().contains("fr");
		}
		return french;
	}

	private static volatile Boolean linux = null;
	public static boolean isLinux() {
		if (linux == null) {
			String os = System.getProperty("os.name").toLowerCase();
			linux = os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0;
		}
		return linux;
	}

	private static volatile Boolean windows = null;
	public static boolean isWindows() {
		if (windows == null) {
			String os = System.getProperty("os.name").toLowerCase();
			windows = os.indexOf("win") >= 0;
		}
		return windows;
	}

	private static volatile Boolean map = null;
	public static boolean useMap() {
		if (map == null) {
			if (!WebDownloader.hasInternetConnection())
				map = false;
			else
				map = true;
		}
		return map;
	}
	
	public static int unitSystem() {
		return prefs.getInt("general_units", UnitSystem.METRIC);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		if (e.getDocument() == txtLatitude.getDocument()) {
			latitudeChanged();
		}
		else if (e.getDocument() == txtLongitude.getDocument()) {
			longitudeChanged();
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		if (e.getDocument() == txtLatitude.getDocument()) {
			latitudeChanged();
		}
		else if (e.getDocument() == txtLongitude.getDocument()) {
			longitudeChanged();
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if (e.getDocument() == txtLatitude.getDocument()) {
			latitudeChanged();
		}
		else if (e.getDocument() == txtLongitude.getDocument()) {
			longitudeChanged();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void setUIFont(javax.swing.plaf.FontUIResource f) {
	    java.util.Enumeration keys = UIManager.getDefaults().keys();
	    while (keys.hasMoreElements()) {
	    	Object key = keys.nextElement();
	    	Object value = UIManager.get (key);
	    	if (value instanceof javax.swing.plaf.FontUIResource)
	    		UIManager.put(key, f);
	    }
	} 
}
