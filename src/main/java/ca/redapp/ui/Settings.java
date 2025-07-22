/***********************************************************************
 * REDapp - Settings.java
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

import static ca.redapp.util.LineEditHelper.getIntegerFromLineEdit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Window;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.Toolkit;

import ca.cwfgm.mapunits.MetricPrefix;
import ca.hss.general.WebDownloader;
import ca.hss.math.Convert.UnitSystem;
import ca.redapp.data.ZoomLevel;
import ca.redapp.map.MapType;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RGroupBox;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;
import ca.redapp.ui.component.SpringUtilities;
import ca.redapp.util.RPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Settings extends JDialog  {
	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	//private RTextField txtElevation;
	//private RTextField txtSlope;
	//private RTextField txtAspect;
	private RTextField txtScale;
	private RTextField txtSpotWXAPIKey;

	//private RTextField txtCurrent;
	//private RTextField txtForecastEnsemble;
	private RTextField txtWmsUrl;
	private RPreferences prefs;
	private JComboBox<String> comboTimezoneRegions;
	private Main app;
	private JComboBox<String> comboCoordinateUnits;
	private JComboBox<String> cmbZoomLevel;
	private JComboBox<String> comboLanguage;
	private JComboBox<String> comboUnits;
	//private JCheckBox chckbxSlope;
	private JCheckBox chckbxScale;
	private JCheckBox chckbxSaveValues;
	private boolean requiresRebootWarning = false;
	private int currentUnits;

	private RButton btnClose;
	
	private JComboBox<String> comboMapType;
	private RButton btnWmsUrl;

	/**
	 * Create the dialog.
	 */
	public Settings(Main app, RPreferences prefs) {
		super(app.frmRedapp);
		this.app = app;
		this.prefs = prefs;
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setTitle(Main.resourceManager.getString("ui.dlg.title.settings"));
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage(
				Main.class.getResource(Main.resourceManager
						.getImagePath("ui.icon.window.redapp"))));
		icons.add(Toolkit.getDefaultToolkit().getImage(
				Main.class.getResource(Main.resourceManager
						.getImagePath("ui.icon.window.redapp20"))));
		icons.add(Toolkit.getDefaultToolkit().getImage(
				Main.class.getResource(Main.resourceManager
						.getImagePath("ui.icon.window.redapp40"))));
		setIconImages(icons);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		RGroupBox panelGeneral = new RGroupBox();
		panelGeneral.setText(Main.resourceManager.getString("ui.label.settings.general.title"));
		panelGeneral.setLayout(new SpringLayout());
		contentPanel.add(panelGeneral);

		RLabel lblLanguage = new RLabel();
		lblLanguage.setText(Main.resourceManager.getString("ui.label.settings.general.lang"));
		panelGeneral.add(lblLanguage);

		comboLanguage = new JComboBox<String>();
		comboLanguage.setModel(new DefaultComboBoxModel<String>(new String[] { Main.resourceManager.getString("ui.label.settings.general.english") + " (EN)",
				Main.resourceManager.getString("ui.label.settings.general.french") + " (FR)"}));
		comboLanguage.setSelectedIndex(0);
		panelGeneral.add(comboLanguage);

		RLabel lblShowTimezoneFor = new RLabel(Main.resourceManager.getString("ui.label.settings.general.timezone"));
		panelGeneral.add(lblShowTimezoneFor);

		comboTimezoneRegions = new JComboBox<String>();
		comboTimezoneRegions.setModel(new DefaultComboBoxModel<String>(new String[] {
				Main.resourceManager.getString("ui.label.settings.general.timezone.all"),
				Main.resourceManager.getString("ui.label.settings.general.timezone.ca"),
				Main.resourceManager.getString("ui.label.settings.general.timezone.af"),
				Main.resourceManager.getString("ui.label.settings.general.timezone.as"),
				Main.resourceManager.getString("ui.label.settings.general.timezone.eu"),
				Main.resourceManager.getString("ui.label.settings.general.timezone.na"),
				Main.resourceManager.getString("ui.label.settings.general.timezone.oc"),
				Main.resourceManager.getString("ui.label.settings.general.timezone.sa") }));
		panelGeneral.add(comboTimezoneRegions);

		RLabel lblCoordinateUnits = new RLabel(Main.resourceManager.getString("ui.label.settings.general.coord"));
		panelGeneral.add(lblCoordinateUnits);

		comboCoordinateUnits = new JComboBox<String>();
		comboCoordinateUnits.setModel(new DefaultComboBoxModel<String>(new String[] {
				Main.resourceManager.getString("ui.label.settings.general.coord.dd"),
				Main.resourceManager.getString("ui.label.settings.general.coord.ddm"),
				Main.resourceManager.getString("ui.label.settings.general.coord.dms") }));
		panelGeneral.add(comboCoordinateUnits);

		RLabel lblUnits = new RLabel(Main.resourceManager.getString("ui.label.settings.general.units"));
		panelGeneral.add(lblUnits);
		
		comboUnits = new JComboBox<String>();
		comboUnits.setModel(new DefaultComboBoxModel<String>(new String[] {
				Main.resourceManager.getString("ui.label.settings.general.metric"),
				Main.resourceManager.getString("ui.label.settings.general.imperial")
		}));
		comboUnits.addActionListener((e) -> updateUnits());
		panelGeneral.add(comboUnits);

		RLabel lblScale = new RLabel(Main.resourceManager.getString("ui.label.settings.general.scale"));
		panelGeneral.add(lblScale);

		JPanel scaleOuterPanel = new JPanel();
		scaleOuterPanel.setLayout(new BorderLayout());
		scaleOuterPanel.setBackground(new Color(245, 245, 245));
		panelGeneral.add(scaleOuterPanel);

		chckbxScale = new JCheckBox("");
		if (Main.isLinux())
			chckbxScale.setFont(chckbxScale.getFont().deriveFont(12.0f));
		chckbxScale.setBackground(new Color(245, 245, 245));
		chckbxScale.addActionListener((e) -> txtScale.setEnabled(chckbxScale.isSelected()));
		scaleOuterPanel.add(chckbxScale, BorderLayout.WEST);

		JPanel scalePanel = new JPanel();
		scalePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		scalePanel.setBackground(new Color(245, 245, 245));
		scaleOuterPanel.add(scalePanel, BorderLayout.EAST);

		RLabel label = new RLabel("1:");
		label.setHorizontalAlignment(SwingConstants.TRAILING);
		scalePanel.add(label);

		txtScale = new RTextField();
		txtScale.setPreferredSize(new Dimension(47, 22));
		txtScale.setColumns(10);
		scalePanel.add(txtScale);

		RLabel lblSave = new RLabel(Main.resourceManager.getString("ui.label.settings.general.retain"));
		panelGeneral.add(lblSave);

		chckbxSaveValues = new JCheckBox("");
		if (Main.isLinux())
			chckbxSaveValues.setFont(chckbxSaveValues.getFont().deriveFont(12.0f));
		chckbxSaveValues.setBackground(new Color(245, 245, 245));
		panelGeneral.add(chckbxSaveValues);

		SpringUtilities.makeCompactGrid(panelGeneral, 6, 2, 6, 6, 6, 6);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		RGroupBox panelLink = new RGroupBox();
		panelLink.setText(Main.resourceManager.getString("ui.label.settings.links.title"));
		panelLink.setLayout(new SpringLayout());
		contentPanel.add(panelLink);

		RLabel lblSpotAPI = new RLabel(Main.resourceManager.getString("ui.label.settings.weather.spotwxapikey"));
		panelLink.add(lblSpotAPI);

		txtSpotWXAPIKey = new RTextField();
		txtSpotWXAPIKey.setColumns(10);
		txtSpotWXAPIKey.setHorizontalAlignment(JTextField.LEFT);
		panelLink.add(txtSpotWXAPIKey);

		panelLink.add(new JLabel());



/*
		RLabel lblForecastEnsemble = new RLabel(Main.resourceManager.getString("ui.label.settings.links.ensemble"));
		panelLink.add(lblForecastEnsemble);

		txtForecastEnsemble = new RTextField();
		txtForecastEnsemble.setColumns(10);
		txtForecastEnsemble.setHorizontalAlignment(JTextField.LEFT);
		panelLink.add(txtForecastEnsemble);
		*/
		SpringUtilities.makeCompactGrid(panelLink, 1, 2, 6, 6, 6, 6);
		
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		RGroupBox panelMap = new RGroupBox();
		panelMap.setText(Main.resourceManager.getString("ui.label.settings.general.mapgroup"));
		panelMap.setLayout(new SpringLayout());
		contentPanel.add(panelMap);

		RLabel lblMapScale = new RLabel();
		lblMapScale.setText(Main.resourceManager.getString("ui.label.settings.general.map"));
		panelMap.add(lblMapScale);

		cmbZoomLevel = new JComboBox<String>();
		lblMapScale.setLabelFor(cmbZoomLevel);
		panelMap.add(cmbZoomLevel);
		
		RLabel lblMapType = new RLabel(Main.resourceManager.getString("ui.label.settings.general.maptype"));
		panelMap.add(lblMapType);

		comboMapType = new JComboBox<String>();
		comboMapType.setModel(new DefaultComboBoxModel<String>(new String[] {
				Main.resourceManager.getString("ui.label.settings.general.maptype.osmoffline"),
				Main.resourceManager.getString("ui.label.settings.general.maptype.osmonline"),
				Main.resourceManager.getString("ui.label.settings.general.maptype.wms")}));
		
		//comboMapType.setEnabled(WebDownloader.hasInternetConnection());
		
		panelMap.add(comboMapType);
		
		RLabel lblWmsUrl = new RLabel(Main.resourceManager.getString("ui.label.settings.general.maptype.wmsurl"));
		panelMap.add(lblWmsUrl);

		txtWmsUrl = new RTextField();
		txtWmsUrl.setColumns(10);
		panelMap.add(txtWmsUrl);
		
		JPanel panelUrlBtn = new JPanel();
		panelUrlBtn.setLayout(new BorderLayout());
		panelUrlBtn.setBackground(new Color(245, 245, 245));
		
		panelMap.add(Box.createRigidArea(new Dimension(0, 10)));
		panelMap.add(panelUrlBtn);
		
		btnWmsUrl = new RButton(Main.resourceManager.getString("ui.label.settings.wmsurl"));
		btnWmsUrl.addActionListener((e) -> wmsUrlSetup());

		panelUrlBtn.add(btnWmsUrl, BorderLayout.EAST);

		SpringUtilities.makeCompactGrid(panelMap, 4, 2, 6, 6, 6, 6);

		btnClose = new RButton(Main.resourceManager.getString("ui.label.settings.close"), RButton.Decoration.Close);
		btnClose.addActionListener((e) -> closeButton());
		btnClose.setBounds(250, 414, 121, 41);

		RButton btnOK = new RButton(Main.resourceManager.getString("ui.label.settings.ok"));
		btnOK.addActionListener((e) -> okButton());
		btnOK.setBounds(130, 414, 121, 41);

		RButton btnRestoreDefaults = new RButton(Main.resourceManager.getString("ui.label.settings.restore"));
		btnRestoreDefaults.setBounds(10, 414, 121, 41);
		btnRestoreDefaults.addActionListener((e) -> restoreDefaultsButton());

		JPanel panel1 = new JPanel();
		panel1.setLayout(new FlowLayout(FlowLayout.CENTER));
		contentPanel.add(panel1);

		JPanel panel2 = new JPanel();
		panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel1.add(panel2);
		panel2.add(btnRestoreDefaults);
		panel2.add(btnOK);
		panel2.add(btnClose);

		pack();
		setDialogPosition(app.getForm());

		loadSettings();
	}
	
	private int mapToIndex(MapType m) {
		switch(m) {
			case OSM_ONLINE:
				return 1;
				
			case WMS:
				return 2;
			
			default:
				return 0;
		}
	}
	
	private MapType indexToMap(int m) {
		switch(m) {
			case 1:
				return MapType.OSM_ONLINE;
				
			case 2:
				return MapType.WMS;
			
			default:
				return MapType.OSM_OFFLINE;
		}
	}

	private void loadSettings() {
		currentUnits = prefs.getInt("general_units", UnitSystem.METRIC);
		comboUnits.setSelectedIndex(currentUnits - 1);
		
		boolean b = prefs.getBoolean("fbp_useMapScaling", false);
		chckbxScale.setSelected(b);
		txtScale.setText(prefs.getString("fbp_mapScale", "50000"));
		txtScale.setEnabled(b);
	

		ZoomLevel levels[] = ZoomLevel.values();
		DefaultComboBoxModel<String> levelModel = new DefaultComboBoxModel<String>();
		for (ZoomLevel level : levels) {
			levelModel.addElement(level.getScale());
		}
		cmbZoomLevel.setModel(levelModel);
		cmbZoomLevel.setSelectedIndex(prefs.getInt("map_zoomlevel", 10));

		comboMapType.setSelectedIndex(mapToIndex(MapType.fromInt(prefs.getInt("map_type", MapType.OSM_OFFLINE.toInt()))));
		//comboMapType.setSelectedIndex(WebDownloader.hasInternetConnection() ? mapToIndex(MapType.fromInt(prefs.getInt("map_type", MapType.OSM_OFFLINE.toInt()))) : MapType.OSM_OFFLINE.toInt() - 2);
		comboMapType.addActionListener((e) -> {
			requiresRebootWarning = true;
			MapType type = indexToMap(comboMapType.getSelectedIndex());
			txtWmsUrl.setEnabled(type == MapType.WMS);
			btnWmsUrl.setEnabled(type == MapType.WMS);
		});
		
		chckbxSaveValues.setSelected(Boolean.parseBoolean(prefs.getString(
				"saveValues", "true")));

		String units = prefs.getString("coordinateUnits", "Decimal Degrees");
		if (units.equals("Decimal Degrees"))
			comboCoordinateUnits.setSelectedIndex(0);
		else if (units.equals("Degrees Decimal Minutes"))
			comboCoordinateUnits.setSelectedIndex(1);
		else
			comboCoordinateUnits.setSelectedIndex(2);

		int index = prefs.getInt("regionindex", 0);
		if (index >= 0 && index < comboTimezoneRegions.getItemCount())
			comboTimezoneRegions.setSelectedIndex(index);
		else
			comboTimezoneRegions.setSelectedIndex(0);

		txtSpotWXAPIKey.setText(prefs.getString("SpotAPIKey", ""));
		txtSpotWXAPIKey.setCaretPosition(0);


		//txtForecastEnsemble.setText(prefs.getString("ensemble",				"http://dd.weatheroffice.ec.gc.ca/ensemble/naefs/xml/"));
		//txtForecastEnsemble.setCaretPosition(0);
		
		boolean wmsSel = MapType.fromInt(prefs.getInt("map_type", MapType.OSM_OFFLINE.toInt())) == MapType.WMS; 
		btnWmsUrl.setEnabled(wmsSel);
		txtWmsUrl.setEnabled(wmsSel);
		txtWmsUrl.setText(prefs.getString("wms_url", ""));
		txtWmsUrl.setCaretPosition(0);
		
		String defLanguage = Locale.getDefault().getISO3Language();
		int def = 0;
		if (defLanguage.indexOf("fr") >= 0)
			def = 1;
		int lang = prefs.getInt("language", def);
		comboLanguage.setSelectedIndex(lang);
		comboLanguage.addActionListener((e) -> {
			requiresRebootWarning = true;
		});
	}

	private void saveSettings() {
		// save default settings to java prefs
		prefs.putString("saveValues", (chckbxSaveValues.isSelected() ? "true"
				: "false"));
		prefs.putInt("regionindex", comboTimezoneRegions.getSelectedIndex());
		prefs.putInt("map_zoomlevel", cmbZoomLevel.getSelectedIndex());
		prefs.putInt("general_units", currentUnits);
		prefs.putInt("map_type", indexToMap(comboMapType.getSelectedIndex()).toInt());
		
		prefs.putString("wms_url", this.txtWmsUrl.getText());
		
		Double la = app.getLatitude();
		Double lo = app.getLongitude();
		double lat = la == null ? 0 : la;
		double lon = lo == null ? 0 : lo;
		int index = comboCoordinateUnits.getSelectedIndex();
		switch (index) {
		case 0:
			prefs.putString("coordinateUnits", "Decimal Degrees");
			break;
		case 1:
			prefs.putString("coordinateUnits", "Degrees Decimal Minutes");
			break;
		case 2:
			prefs.putString("coordinateUnits", "Degrees Minutes Seconds");
			break;
		}
		app.setLatitude(lat);
		app.setLongitude(lon);

		if (chckbxScale.isSelected()) {
			prefs.putBoolean("fbp_useMapScaling", true);
			Integer i = getIntegerFromLineEdit(txtScale);
			if (i != null) {
				prefs.putInt("fbp_mapScale", i.intValue());
				app.fbpTab.mapScalingChanged(i, MetricPrefix.centi);
			}
		} else {
			prefs.putBoolean("fbp_useMapScaling", false);
			app.fbpTab.mapScalingChanged(1, MetricPrefix.DISABLE);
		}


		prefs.putString("SpotAPIKey", this.txtSpotWXAPIKey.getText());
		//prefs.putString("ensemble", this.txtForecastEnsemble.getText());
		prefs.putInt("language", comboLanguage.getSelectedIndex());
	}

	public void updateUnits() {
		int units = comboUnits.getSelectedIndex() + 1;
		if (units != currentUnits) {
			currentUnits = units;
		}
	}

	public void restoreDefaultsButton() {
		comboTimezoneRegions.setSelectedIndex(0);
		chckbxSaveValues.setSelected(true);
		comboCoordinateUnits.setSelectedIndex(0);
		cmbZoomLevel.setSelectedIndex(10);
		comboUnits.setSelectedIndex(0);

		chckbxScale.setSelected(false);
		txtScale.setText("50000");
		txtSpotWXAPIKey.setText("");

		//txtForecastEnsemble.setText("http://dd.weatheroffice.ec.gc.ca/ensemble/naefs/xml/");

		saveSettings();
	}

	private void setDialogPosition(Window dlg) {
		if (dlg == null)
			return;
		int width = getWidth();
		int height = getHeight();
		int x = dlg.getX();
		int y = dlg.getY();
		int rwidth = dlg.getWidth();
		int rheight = dlg.getHeight();
		x = (int)(x + (rwidth / 2.0) - (width / 2.0));
		y = (int)(y + (rheight / 2.0) - (height / 2.0)) + 30;
		setLocation(x, y);
	}

	private void wmsUrlSetup() {
		WmsUrlSetup dlg = new WmsUrlSetup(this);
		dlg.setVisible(true);	
		byte result = dlg.getResult();
		
		if(result == 1) {
			String retURL = dlg.getURL();
			txtWmsUrl.setText(retURL);
			requiresRebootWarning = true;
		}
	}
	
	private void okButton() {
		saveSettings();
		app.settingsUpdated();
		if (requiresRebootWarning) {
			JOptionPane.showMessageDialog(Settings.this, Main.resourceManager.getString("ui.label.settings.general.langchange"));
		}
		this.dispose();
	}

	private void closeButton() {
		this.dispose();
	}
}
