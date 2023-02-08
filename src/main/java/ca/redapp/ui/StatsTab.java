/***********************************************************************
 * REDapp - StatsTab.java
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

import ca.redapp.data.StatsDataType;
import ca.redapp.data.StatsTableModel;
import ca.redapp.data.StatsTableModel.Column;
import ca.redapp.data.StatsTableModel.DisplayType;
import ca.redapp.data.StatsTableModel.StatsTableListener;
import ca.redapp.ui.CreateDailyDialog.CreateDailyDialogListener;
import ca.redapp.ui.CreateHourlyDialog.CreateHourlyDialogListener;
import ca.redapp.ui.CreateNoonDialog.CreateNoonDialogListener;
import ca.redapp.ui.EditDailyDialog.DailyWeatherData;
import ca.redapp.ui.EditNoonDialog.NoonWeatherData;
import ca.redapp.ui.Import.FileType;
import ca.redapp.ui.component.HeaderRenderer;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RCheckBoxList;
import ca.redapp.ui.component.RComboBox;
import ca.redapp.ui.component.RContextMenuButton;
import ca.redapp.ui.component.RGroupBox;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;
import ca.redapp.ui.component.RToggleButton;
import ca.redapp.ui.component.SpringUtilities;
import ca.redapp.ui.component.RContextMenuButton.ContextButtonClickListener;
import ca.redapp.util.LineEditHelper;
import ca.redapp.util.REDappLogger;
import ca.redapp.util.RFileChooser;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import ca.hss.general.TwoString;
import ca.hss.general.DecimalUtils.DataType;
import ca.cwfgm.export.DataExporter;
import ca.cwfgm.export.DataExporter.RowBuilder;
import ca.wise.fbp.FBPCalculations;
import ca.wise.fwi.Fwi;
import ca.wise.grid.GRID_ATTRIBUTE;
import ca.wise.grid.DFWIData;
import ca.wise.grid.IFWIData;
import ca.wise.grid.IWXData;
import ca.wise.weather.WEATHERSTREAM_IMPORT;
import ca.wise.weather.WEATHER_OPTION;
import ca.wise.weather.CWFGM_WeatherStream;
import ca.wise.weather.NoonWeatherCondition;
import ca.hss.general.DecimalUtils;
import ca.hss.general.OutVariable;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import ca.hss.times.WTime;
import ca.hss.times.WTimeSpan;
import ca.hss.times.WTimeManager;
import ca.hss.times.WorldLocation;

import javax.swing.SwingConstants;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.Point;
import javax.swing.ListSelectionModel;
import javax.xml.parsers.ParserConfigurationException;

import java.beans.EventHandler;
import javax.swing.JTree;

import static ca.hss.math.General.*;
import static ca.redapp.util.LineEditHelper.lineEditHandleError;

import java.awt.BorderLayout;
import javax.swing.ScrollPaneConstants;

public class StatsTab extends REDappTab implements StatsTableListener, DisplayableMapTab {
	// {{ variables

	private static final long serialVersionUID = 1L;
	private Main app;
	private boolean initialized = false;
	private StatsTableModel model;
	private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
	private CWFGM_WeatherStream ws = new CWFGM_WeatherStream();
	private NoonWeatherCondition nwc = new NoonWeatherCondition(ws.getTimeManager());
	private boolean m_CanTransfer = true;
	private boolean m_PauseCalculate = true;
	private ArrayList<StatsTabListener> listeners = new ArrayList<StatsTab.StatsTabListener>();
	private WTime editTime = null;
	private String[] fuelTypeCardNames = new String[] { "Empty", "First", "Second", "Third", "Fourth" };
	private FBPCalculations fbpCalculations = new FBPCalculations();
	private boolean checkboxUpdateNeeded = false;
	private boolean displayed = false;
	private boolean scanningForFFMC = false;
	private Import.FileType filetype = FileType.WEATHER_STREAM;
	private boolean importLock = false;
	/**
	 * Should rows with corrected weather values be highlighted.
	 */
	private boolean highlightCorrectedRows = false;

	//Default values for things stored in the preferences.
	private final Double DEFAULT_DAILY_DMC = 25.0;
	private final Double DEFAULT_DAILY_DC = 200.0;
	private final Double DEFAULT_DAILY_FFMC = 85.0;
	private final Double DEFAULT_HOURLY_FFMC = 85.0;
	private final boolean DEFAULT_FFMC_FIT = true;
	private final Double DEFAULT_ELEVATION = 500.0;
	private final Boolean DEFAULT_USE_SLOPE = true;
	private final Double DEFAULT_SLOPE = 0.0;
	private final Double DEFAULT_ASPECT = 0.0;
	private final Double DEFAULT_CROWN_BASE_HEIGHT = 7.0;
	private final Double DEFAULT_PERCENT_CONIFER = 0.0;
	private final Double DEFAULT_PERCENT_DEAD_FIR = 0.0;
	private final Double DEFAULT_GRASS_CURING = 0.0;
	private final Double DEFAULT_GRASS_FUEL_LOAD = 0.0;
	private final Integer DEFAULT_DISPLAY_TYPE = 0;

	//Keys for the preferences.
	public static final String TEMP_ALPHA_KEY = "TEMP_ALPHA";
	public static final String TEMP_BETA_KEY = "TEMP_BETA";
	public static final String TEMP_GAMMA_KEY = "TEMP_GAMMA";
	public static final String WIND_ALPHA_KEY = "WIND_ALPHA";
	public static final String WIND_BETA_KEY = "WIND_BETA";
	public static final String WIND_GAMMA_KEY = "WIND_GAMMA";
	public static final String CALC_METHOD_KEY = "CALCULATION_METHOD";
	public static final String DAILY_DMC_KEY = "DAILY_DMC";
	public static final String DAILY_DC_KEY = "DAILY_DC";
	public static final String DAILY_FFMC_KEY = "DAILY_FFMC";
	public static final String DAILY_PRECIPITATION_KEY = "DAILY_PRECIPITATION";
	public static final String HOURLY_FFMC_KEY = "HOURLY_FFMC";
	public static final String DAILY_FFMC_FIT = "DAILY_FIT";
	public static final String HOURLY_FFMC_START_KEY = "HOURLY_FFMC_START";
	public static final String GROUPBY_INDEX = "GROUPBY_INDEX";
	public static final String FBP_STATS_FOLDER = "fpbstats/";
	public static final String FUEL_TYPE_INDEX_KEY = FBP_STATS_FOLDER + "FUEL_TYPE_INDEX";
	public static final String BUILD_UP_EFFECT_KEY = FBP_STATS_FOLDER + "BUE_ON";
	public static final String ELEVATION_KEY = FBP_STATS_FOLDER + "ELEV";
	public static final String USE_SLOPE_KEY = FBP_STATS_FOLDER + "USE_SLOPE";
	public static final String SLOPE_KEY = FBP_STATS_FOLDER + "SLOPE";
	public static final String ASPECT_KEY = FBP_STATS_FOLDER + "ASPECT";
	public static final String CROWN_BASE_HEIGHT_KEY = FBP_STATS_FOLDER + "CROWN_BASE_HEIGHT";
	public static final String PERCENT_CONIFER_KEY = FBP_STATS_FOLDER + "PERCENT_CONIFER";
	public static final String PERCENT_DEAD_FIR_KEY = FBP_STATS_FOLDER + "PERCENT_DEAD_FIR";
	public static final String GRASS_CURING_KEY = FBP_STATS_FOLDER + "GRASS_CURING";
	public static final String GRASS_FUEL_LOAD_KEY = FBP_STATS_FOLDER + "GRASS_FUEL_LOAD";
	public static final String DISPLAY_TYPE_KEY = "DISPLAY_TYPE";
	
	private final Double DEFAULT_TEMP_ALPHA = -0.77;
	private final Double DEFAULT_TEMP_BETA = 2.80;
	private final Double DEFAULT_TEMP_GAMMA = -2.20;
	private final Double DEFAULT_WIND_ALPHA = 1.00;
	private final Double DEFAULT_WIND_BETA = 1.24;
	private final Double DEFAULT_WIND_GAMMA = -3.59;
	
	private String riseStr = "";
	private String noonStr = "";
	private String setStr = "";
	
	private boolean fitFFMCFlag = false;
	private double fitFFMCVal = -1.0;
	
	private final RLabel lblFbpSlopeUnits = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
	private static boolean firstImport = true;
	
	// }}

	// {{ Constructor

	public StatsTab(Main app) {
		this.app = app;
		if (Main.isWindows())
			setBackground(new Color(255, 255, 255));
		if (Main.isMac())
			tabBaseHeight = 265;
		else
			tabBaseHeight = 260;
		initialize();

		addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorRemoved(AncestorEvent event) { }
			@Override
			public void ancestorMoved(AncestorEvent event) { }

			@Override
			public void ancestorAdded(AncestorEvent event) {
				if (checkboxUpdateNeeded)
					model.importComplete();
				displayed = true;
			}
		});

		popup.add(editItem);
		popup2.add(editItem2);
		popup2.add(transferItem);
		if (Main.useMap()) {
			popup2.add(viewMapItem);
		}
		model = new StatsTableModel(tableStats, headerTable);
		tableStats.setModel(model);
		tableStats.setDefaultRenderer(String.class, model.new DefaultRenderer());
		tableStats.setDefaultRenderer(Double.class, model.new DefaultRenderer());
		tableStats.setDefaultRenderer(Float.class, model.new DefaultRenderer());
		tableStats.setDefaultRenderer(TwoString.class, model.new DefaultRenderer());
		comboFbpFuelType.setSelectedIndex(prefs.getInt(FUEL_TYPE_INDEX_KEY, 0));
		fuelTypeChanged();

		scrollPane.setVisible(true);
		scrollPane_2.setVisible(false);
		scrollPaneDay.setVisible(false);
		st_tview.setVisible(true);
		st_lview.setVisible(false);
		st_tviewday.setVisible(false);
		stats_groupBy.addActionListener((e) -> groupby_changed());

		model.attachListView(st_lview);
		model.setTableRoot(st_tview, st_tviewday, st_tviewnoon);
		model.addStatsTableListener(this);

		st_lview.setVisible(false);
		st_tview.setVisible(true);

		txtDailyFFMC.setText(DecimalUtils.format(prefs.getDouble(DAILY_FFMC_KEY, DEFAULT_DAILY_FFMC), DecimalUtils.DataType.FFMC));
		txtDailyDMC.setText(DecimalUtils.format(prefs.getDouble(DAILY_DMC_KEY, DEFAULT_DAILY_DMC), DecimalUtils.DataType.DMC));
		txtDailyDC.setText(DecimalUtils.format(prefs.getDouble(DAILY_DC_KEY, DEFAULT_DAILY_DC), DecimalUtils.DataType.DC));
		double val = prefs.getDouble(DAILY_PRECIPITATION_KEY, 0.0);
		val = Convert.convertUnit(val, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
		txtPrecipitation.setText(DecimalUtils.format(val, DecimalUtils.DataType.PRECIP));
		txtHourlyFFMC.setText(DecimalUtils.format(prefs.getDouble(HOURLY_FFMC_KEY, DEFAULT_HOURLY_FFMC), DecimalUtils.DataType.FFMC));
		chkDailyFit.setSelected(prefs.getBoolean(DAILY_FFMC_FIT, DEFAULT_FFMC_FIT));
		chkDailyFitChanged(false);
		comboHourlyMethod.setSelectedIndex(prefs.getInt(CALC_METHOD_KEY, 0));
		if (chkDailyFit.isSelected())
			prefs.putInt(HOURLY_FFMC_START_KEY, 17);
		else
			cmb_hourlyStart.setSelectedIndex(prefs.getInt(HOURLY_FFMC_START_KEY, 0));
		val = prefs.getDouble(ELEVATION_KEY, DEFAULT_ELEVATION);
		val = Convert.convertUnit(val, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
		txtFbpElevation.setText(DecimalUtils.format(val, DecimalUtils.DataType.FORCE_ATMOST_2));
		chckFbpSlope.setSelected(prefs.getBoolean(USE_SLOPE_KEY, DEFAULT_USE_SLOPE));
		txtFbpSlope.setText(DecimalUtils.format(prefs.getDouble(SLOPE_KEY, DEFAULT_SLOPE), DecimalUtils.DataType.FORCE_ATMOST_2));
		txtFbpAspect.setText(DecimalUtils.format(prefs.getDouble(ASPECT_KEY, DEFAULT_ASPECT), DecimalUtils.DataType.FORCE_ATMOST_2));
		val = prefs.getDouble(CROWN_BASE_HEIGHT_KEY, DEFAULT_CROWN_BASE_HEIGHT);
		val = Convert.convertUnit(val, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
		txtFbpCrownBaseHeight.setText(DecimalUtils.format(val, DecimalUtils.DataType.FORCE_ATMOST_2));
		txtFbpPercentConifer.setText(DecimalUtils.format(prefs.getDouble(PERCENT_CONIFER_KEY, DEFAULT_PERCENT_CONIFER), DecimalUtils.DataType.FORCE_ATMOST_2));
		txtFbpPercentDeadFir.setText(DecimalUtils.format(prefs.getDouble(PERCENT_DEAD_FIR_KEY, DEFAULT_PERCENT_DEAD_FIR), DecimalUtils.DataType.FORCE_ATMOST_2));
		txtFbpGrassCuring.setText(DecimalUtils.format(prefs.getDouble(GRASS_CURING_KEY, DEFAULT_GRASS_CURING), DecimalUtils.DataType.FORCE_ATMOST_2));
		val = prefs.getDouble(GRASS_FUEL_LOAD_KEY, DEFAULT_GRASS_FUEL_LOAD);
		val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC));
		txtFbpGrassFuelLoad.setText(DecimalUtils.format(val, DecimalUtils.DataType.FORCE_ATMOST_2));
		int type = prefs.getInt(DISPLAY_TYPE_KEY, DEFAULT_DISPLAY_TYPE);
		if (type == 0) {
			tglHourly.setSelected(true);
			model.setDisplayType(DisplayType.HOURLY);
		}
		else if (type == 1) {
			tglDaily.setSelected(true);
			model.setDisplayType(DisplayType.DAILY);
			scrollPane.setVisible(false);
			scrollPaneDay.setVisible(true);
			st_tview.setVisible(false);
			st_tviewday.setVisible(true);
		}
		else if (type == 2) {
			tglNoon.setSelected(true);
			model.setDisplayType(DisplayType.NOON);
			scrollPane.setVisible(false);
			scrollPaneNoon.setVisible(true);
			st_tview.setVisible(false);
			st_tviewnoon.setVisible(true);
		}
		hourlyMethodChanged();

		tableStats.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				JTable table = (JTable)me.getSource();
				Point p = me.getPoint();
				int row = table.rowAtPoint(p);
				WTime time = new WTime(model.getDisplayedTimes().get(row));
				if (me.getButton() == MouseEvent.BUTTON1) {
					if (me.getClickCount() == 2) {
						editRequest(time);
					}
				}
				//TODO menu popup
				else if (me.getButton() == MouseEvent.BUTTON3) {
					int col = table.columnAtPoint(p);
					table.setColumnSelectionInterval(col, col);
					table.setRowSelectionInterval(row, row);
					popup.show(table, p.x, p.y);
					editTime = new WTime(time);
				}
				headerTable.clearSelection();
			}
		});
		headerTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				JTable table = (JTable)me.getSource();
				Point p = me.getPoint();
				int row = table.rowAtPoint(p);
				WTime time = new WTime(model.getDisplayedTimes().get(row));
				if (me.getButton() == MouseEvent.BUTTON1) {
					if (me.getClickCount() == 2) {
						editRequest(time);
					}
					else if (me.getClickCount() == 1) {
						tableStats.setColumnSelectionInterval(0, tableStats.getColumnCount() - 1);
						tableStats.setRowSelectionInterval(row, row);
					}
				}
				//TODO menu popup
				else if (me.getButton() == MouseEvent.BUTTON3) {
					int col = table.columnAtPoint(p);
					table.setColumnSelectionInterval(col, col);
					table.setRowSelectionInterval(row, row);
					tableStats.setColumnSelectionInterval(0, tableStats.getColumnCount() - 1);
					tableStats.setRowSelectionInterval(row, row);
					if (filetype == FileType.WEATHER_STREAM)
						popup2.show(table, p.x, p.y);
					else
						popup.show(table, p.x, p.y);
					editTime = new WTime(time);
				}
			}
		});
		editItem.addActionListener((e) -> editRequest());
		editItem2.addActionListener((e) -> editRequest());
		transferItem.addActionListener((e) -> transfer_to_fbp());
		viewMapItem.addActionListener((e) -> display_on_map());
		
		txtDailyFFMC.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtDailyFFMCChanged"));
		txtDailyDMC.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtDailyDMCChanged"));
		txtDailyDC.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtDailyDCChanged"));
		txtPrecipitation.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtPrecipitationChanged"));
		txtHourlyFFMC.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtHourlyFFMCChanged"));
		chkDailyFit.addActionListener(e -> chkDailyFitChanged(true));
		btnImport.addActionListener((e) -> importFile());
		comboHourlyMethod.addActionListener((e) -> hourlyMethodChanged());
		cmb_hourlyStart.addActionListener((e) -> hourlyFFMCStartTimeChanged());
		comboFbpFuelType.addActionListener((e) -> fuelTypeChanged());
		txtFbpElevation.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtFBPElevationChanged"));
		txtFbpSlope.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtFBPSlopeChanged"));
		txtFbpAspect.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtFBPAspectChanged"));
		chckFbpSlope.addActionListener((e) -> chckFBPUseSlopeChanged());
		txtFbpCrownBaseHeight.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtFbpCrownBaseHeightChanged"));
		txtFbpPercentConifer.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtFbpPercentConiferChanged"));
		txtFbpPercentDeadFir.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtFbpPercentDeadFirChanged"));
		txtFbpGrassCuring.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtFbpGrassCuringChanged"));
		txtFbpGrassFuelLoad.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class, this, "txtFbpGrassFuelLoadChanged"));
		tglHourly.addActionListener((e) -> boxDisplayHourlyChanged());
		tglDaily.addActionListener((e) -> boxDisplayDailyChanged());
		tglNoon.addActionListener((e) -> boxDisplayNoonChanged());
		
		btnAdd.setContextButtonClickListener(new ContextButtonClickListener() {
			@Override
			public void clicked() {
				if (filetype == FileType.WEATHER_STREAM)
					inputHourly();
				else
					inputNoon();
			}

			@Override
			public void contextActionClicked(String title, Object value) {
				int index = (Integer)value;
				if (index == 0)
					inputHourly();
				else
					inputDaily();
			}
		});
		btnAdd.addContextAction(Main.resourceManager.getString("ui.label.stats.import.hourly"), 0);
		btnAdd.addContextAction(Main.resourceManager.getString("ui.label.stats.import.daily"), 1);

		btnEdit.setContextButtonClickListener(new ContextButtonClickListener() {
			@Override
			public void clicked() {
				editTime = model.getSelectedRow();
				editRequest();
			}

			@Override
			public void contextActionClicked(String title, Object value) {
				int index = (Integer)value;
				WTime nt = model.getSelectedRow();
				if (nt != null) {
					if (index == 0)
						editHourly(nt);
					else
						editDaily(nt);
				}
			}
		});
		btnEdit.addContextAction(Main.resourceManager.getString("ui.label.stats.import.hourly"), 0);
		btnEdit.addContextAction(Main.resourceManager.getString("ui.label.stats.import.daily"), 1);

		btnExport.setContextButtonClickListener(new ContextButtonClickListener() {
			@Override
			public void clicked() {
				export();
			}

			@Override
			public void contextActionClicked(String title, Object value) {
				int index = (Integer)value;
				if (index == 0) {
					exportHourlyWeather();
				}
				else if (index == 1) {
					exportDailyWeather();
				}
				else if (index == 2) {
					exportNoonStandardWeather();
				}
				else {
					export();
				}
			}
		});
		btnExport.addContextAction(Main.resourceManager.getString("ui.label.stats.export.hourly"), 0);
		btnExport.addContextAction(Main.resourceManager.getString("ui.label.stats.export.daily"), 1);
		btnExport.addContextAction(Main.resourceManager.getString("ui.label.stats.export.noon"), 2);
		btnExport.addContextAction(Main.resourceManager.getString("ui.label.stats.export.stats"), 3);

		//allow calculations to start
		m_PauseCalculate = false;
	}

	// }}

	// {{ text changing handlers

	public void txtTempAlphaChanged(double d) {
		prefs.putDouble(TEMP_ALPHA_KEY, d);
	}

	public void txtTempBetaChanged(double d) {
		prefs.putDouble(TEMP_BETA_KEY, d);
	}

	public void txtTempGammaChanged(double d) {
		prefs.putDouble(TEMP_GAMMA_KEY, d);
	}

	public void txtWSAlphaChanged(double d) {
		prefs.putDouble(WIND_ALPHA_KEY, d);
	}

	public void txtWSBetaChanged(double d) {
		prefs.putDouble(WIND_BETA_KEY, d);
	}

	public void txtWSGammaChanged(double d) {
		prefs.putDouble(WIND_GAMMA_KEY, d);
	}

	public void txtDailyFFMCChanged() {
		Double d;
		if ((d = LineEditHelper.getDoubleFromLineEdit(txtDailyFFMC)) != null) {
			prefs.putDouble(DAILY_FFMC_KEY, d);
			calculate();
		}
	}

	public void txtDailyDMCChanged() {
		Double d;
		if ((d = LineEditHelper.getDoubleFromLineEdit(txtDailyDMC)) != null) {
			prefs.putDouble(DAILY_DMC_KEY, d);
			calculate();
		}
	}

	public void txtDailyDCChanged() {
		Double d;
		if ((d = LineEditHelper.getDoubleFromLineEdit(txtDailyDC)) != null) {
			prefs.putDouble(DAILY_DC_KEY, d);
			calculate();
		}
	}

	public void txtPrecipitationChanged() {
		Double d;
		if ((d = LineEditHelper.getDoubleFromLineEdit(txtPrecipitation)) != null) {
			d = Convert.convertUnit(d, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
			prefs.putDouble(DAILY_PRECIPITATION_KEY, d);
			calculate();
		}
	}

	public void txtHourlyFFMCChanged() {
		Double d;
		if ((d = LineEditHelper.getDoubleFromLineEdit(txtHourlyFFMC)) != null) {
			prefs.putDouble(HOURLY_FFMC_KEY, d);
			
			if(!fitFFMCFlag)
				calculate();
			else
				fitFFMCFlag = true;
		}
	}
	
	/**
	 * The value of the fit to daily value checkbox has changed.
	 * @param shouldCalculate Should the calculate method be called.
	 */
	public void chkDailyFitChanged(boolean shouldCalculate) {
		prefs.putBoolean(DAILY_FFMC_FIT, chkDailyFit.isSelected());
		
		lblStatsHourlyFfmc.setForeground(chkDailyFit.isSelected() ? Color.GRAY : Color.BLACK);
		lblHourlyFfmcStart.setForeground(chkDailyFit.isSelected() ? Color.GRAY : Color.BLACK);
		txtHourlyFFMC.setEnabled(!chkDailyFit.isSelected());
		cmb_hourlyStart.setEnabled(!chkDailyFit.isSelected());
		if (chkDailyFit.isSelected()) {
			cmbHourlyStartSkipAction = true;
			cmb_hourlyStart.setSelectedIndex((app.getSelectedTimeZone().getDSTAmount().getTotalSeconds() > 0) ? 17 : 16);
			cmbHourlyStartSkipAction = false;
		}
		
		if (shouldCalculate && !importLock)
			calculate();
	}

	public void hourlyFFMCStartTimeChanged() {
		prefs.putInt(HOURLY_FFMC_START_KEY, cmb_hourlyStart.getSelectedIndex());
		if (!cmbHourlyStartSkipAction && !importLock) {
			calculate();
		}
	}

	public void txtFBPElevationChanged() {
		Double d;
		if ((d = LineEditHelper.getDoubleFromLineEdit(txtFbpElevation)) != null) {
			d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
			prefs.putDouble(ELEVATION_KEY, d);
			calculate();
		}
	}

	public void txtFBPSlopeChanged() {
		Double d = LineEditHelper.getDoubleFromLineEdit(txtFbpSlope);
		if (d != null && d <= 100 && d >= 0) {
			prefs.putDouble(SLOPE_KEY, d);
			calculate();
		} else {
			lineEditHandleError(txtFbpSlope, Main.resourceManager.getString("ui.label.range.slope"));
		}
	}

	public void txtFBPAspectChanged() {
		Double d = LineEditHelper.getDoubleFromLineEdit(txtFbpAspect);
		if (d != null && d <= 360 && d >= 0) {
			prefs.putDouble(ASPECT_KEY, d);
			calculate();
		} else {
			lineEditHandleError(txtFbpAspect, Main.resourceManager.getString("ui.label.range.aspect"));
		}
	}

	public void chckFBPUseSlopeChanged() {
		txtFbpSlope.setEnabled(chckFbpSlope.isSelected());
		lblFbpSlopeUnits.setForeground(chckFbpSlope.isSelected() ? Color.BLACK : Color.GRAY);
		prefs.putBoolean(USE_SLOPE_KEY, chckFbpSlope.isSelected());
		calculate();
	}
	
	public void boxDisplayHourlyChanged() {
		if (tglHourly.isSelected()) {
			prefs.putInt(DISPLAY_TYPE_KEY, 0);
			model.setDisplayType(DisplayType.HOURLY);
			btnAdd.setRightEnabled(true);
			btnEdit.setRightEnabled(true);
			btnExport.setRightEnabled(true);
			if (st_tviewday.isVisible() || st_tviewnoon.isVisible()) {
				scrollPane.setVisible(true);
				scrollPaneDay.setVisible(false);
				scrollPaneNoon.setVisible(false);
				st_tview.setVisible(true);
				st_tviewday.setVisible(false);
				st_tviewnoon.setVisible(false);
			}
		}
	}
	
	public void boxDisplayDailyChanged() {
		if (tglDaily.isSelected()) {
			prefs.putInt(DISPLAY_TYPE_KEY, 1);
			model.setDisplayType(DisplayType.DAILY);
			btnAdd.setRightEnabled(true);
			btnEdit.setRightEnabled(true);
			btnExport.setRightEnabled(true);
			if (st_tview.isVisible() || st_tviewnoon.isVisible()) {
				scrollPane.setVisible(false);
				scrollPaneNoon.setVisible(false);
				scrollPaneDay.setVisible(true);
				st_tview.setVisible(false);
				st_tviewnoon.setVisible(false);
				st_tviewday.setVisible(true);
			}
		}
	}
	
	public void boxDisplayNoonChanged() {
		//TODO recalculate
		if (tglNoon.isSelected()) {
			prefs.putInt(DISPLAY_TYPE_KEY, 2);
			model.setDisplayType(DisplayType.NOON);
			if (filetype == FileType.WEATHER_STREAM) {
				btnAdd.setRightEnabled(true);
				btnEdit.setRightEnabled(true);
				btnExport.setRightEnabled(true);
			}
			else {
				tglHourly.setEnabled(false);
				tglDaily.setEnabled(false);
				tglHourly.setSelected(false);
				tglDaily.setSelected(false);
				btnAdd.setRightEnabled(false);
				btnEdit.setRightEnabled(false);
				btnExport.setRightEnabled(false);
			}
			if (st_tview.isVisible() || st_tviewday.isVisible()) {
				scrollPane.setVisible(false);
				scrollPaneDay.setVisible(false);
				scrollPaneNoon.setVisible(true);
				st_tview.setVisible(false);
				st_tviewday.setVisible(false);
				st_tviewnoon.setVisible(true);
			}
		}
	}

	public void txtFbpCrownBaseHeightChanged() {
		Double d;
		if ((d = LineEditHelper.getDoubleFromLineEdit(txtFbpCrownBaseHeight)) != null) {
			d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
			prefs.putDouble(CROWN_BASE_HEIGHT_KEY, d);
			calculate();
		}
	}

	public void txtFbpPercentConiferChanged() {
		Double d;
		if ((d = LineEditHelper.getDoubleFromLineEdit(txtFbpPercentConifer)) != null) {
			prefs.putDouble(PERCENT_CONIFER_KEY, d);
			calculate();
		}
	}

	public void txtFbpPercentDeadFirChanged() {
		Double d;
		if ((d = LineEditHelper.getDoubleFromLineEdit(txtFbpPercentDeadFir)) != null) {
			prefs.putDouble(PERCENT_DEAD_FIR_KEY, d);
			calculate();
		}
	}

	public void txtFbpGrassCuringChanged() {
		Double d;
		if ((d = LineEditHelper.getDoubleFromLineEdit(txtFbpGrassCuring)) != null) {
			prefs.putDouble(GRASS_CURING_KEY, d);
			calculate();
		}
	}

	public void txtFbpGrassFuelLoadChanged() {
		Double d;
		if ((d = LineEditHelper.getDoubleFromLineEdit(txtFbpGrassFuelLoad)) != null) {
			d = Convert.convertUnit(d, UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(Main.unitSystem()));
			prefs.putDouble(GRASS_FUEL_LOAD_KEY, d);
			calculate();
		}
	}

	// }}
	// {{ combo box index changed handlers

	public void hourlyMethodChanged() {
		int index = comboHourlyMethod.getSelectedIndex();
		prefs.put(CALC_METHOD_KEY, String.valueOf(index));
		if (index == 0) {
			lblHourlyFfmcStart.setForeground(Color.GRAY);
			lblStatsHourlyFfmc.setForeground(Color.GRAY);
			lblStatsDailyFit.setForeground(Color.GRAY);
			lblStatsHourlyFfmc.setForeground(Color.GRAY);
			lblHourlyFfmcStart.setForeground(Color.GRAY);
			
			txtHourlyFFMC.setEnabled(false);
			cmb_hourlyStart.setEnabled(false);
			chkDailyFit.setEnabled(false);
		}
		else {
			lblHourlyFfmcStart.setForeground(Color.BLACK);
			lblStatsHourlyFfmc.setForeground(Color.BLACK);
			lblStatsDailyFit.setForeground(Color.BLACK);
			lblStatsHourlyFfmc.setForeground(chkDailyFit.isSelected() ? Color.GRAY : Color.BLACK);
			lblHourlyFfmcStart.setForeground(chkDailyFit.isSelected() ? Color.GRAY : Color.BLACK);
			
			txtHourlyFFMC.setEnabled(!chkDailyFit.isSelected());
			cmb_hourlyStart.setEnabled(!chkDailyFit.isSelected());
			chkDailyFit.setEnabled(true);
		}
		calculate();
	}

	public void groupby_changed() {
		int index = stats_groupBy.getSelectedIndex();
		prefs.putInt(GROUPBY_INDEX, index);
		if (index == 1) {
			scrollPane.setVisible(false);
			scrollPaneDay.setVisible(false);
			scrollPaneNoon.setVisible(false);
			scrollPane_2.setVisible(true);
			st_tview.setVisible(false);
			st_tviewday.setVisible(false);
			st_tviewnoon.setVisible(false);
			st_lview.setVisible(true);
		}
		else {
			if (tglHourly.isSelected()) {
				scrollPane.setVisible(true);
				st_tview.setVisible(true);
			}
			else if (tglNoon.isSelected()) {
				scrollPaneNoon.setVisible(true);
				st_tviewnoon.setVisible(true);
			}
			else if (tglDaily.isSelected()) {
				scrollPaneDay.setVisible(true);
				st_tviewday.setVisible(true);
			}
			scrollPane_2.setVisible(false);
			st_lview.setVisible(false);
		}
	}

	// }}

	// {{ Pause/unpause the auto-calculations

	/**
	 * Pause automatic calculations on value updates.
	 */
	public void pauseCalculations() {
		m_PauseCalculate = true;
	}

	/**
	 * Allow automatic calculations on value updates and calculate all values now.
	 */
	public void unpauseCalculations() {
		m_PauseCalculate = false;
		cmbHourlyStartSkipAction = true;
		calculate();
		cmbHourlyStartSkipAction = false;
	}

	// }}

	/**
	 * Is it possible to transfer new sets of weather data to the stats tab.
	 * Not really needed anymore as I've got transfers working now without needing to
	 * first import a file.
	 * @return
	 */
	public boolean canTransferTo() {
		return m_CanTransfer;
	}

	/**
	 * Is it possible to transfer a particular hours weather data to the stats tab.
	 * @param dt
	 * @return
	 */
	public boolean canTransferTo(Calendar dt) {
		if (!canTransferTo())
			return false;
		OutVariable<IWXData> data = new OutVariable<IWXData>();
		data.value = new IWXData();
		WTime nt = new WTime(dt.get(Calendar.YEAR), dt.get(Calendar.MONTH) + 1, dt.get(Calendar.DAY_OF_MONTH),
				dt.get(Calendar.HOUR_OF_DAY), 0, 0, new WTimeManager(new WorldLocation()));
		ws.getInstantaneousValues(nt, 0, data, null, null);
		if (data.value == null)
			return false;
		return true;
	}

	public void addStatsTabListener(StatsTabListener listener) {
		listeners.add(listener);
	}

	private void setDialogPosition(JDialog dlg) {
		int width = dlg.getWidth();
		int height = dlg.getHeight();
		int x = app.frmRedapp.getX();
		int y = app.frmRedapp.getY();
		int rwidth = app.frmRedapp.getWidth();
		int rheight = app.frmRedapp.getHeight();
		x = (int)(x + (rwidth / 2.0) - (width / 2.0));
		y = (int)(y + (rheight / 2.0) - (height / 2.0)) + 30;
		dlg.setLocation(x, y);
	}

	//Get the values from the form.
	private boolean wsGetValuesFromForm() {
		ws.setAttribute(WEATHER_OPTION.TEMP_ALPHA,
				Double.valueOf(prefs.getDouble(TEMP_ALPHA_KEY, DEFAULT_TEMP_ALPHA)));
		ws.setAttribute(WEATHER_OPTION.TEMP_BETA,
				Double.valueOf(prefs.getDouble(TEMP_BETA_KEY, DEFAULT_TEMP_BETA)));
		ws.setAttribute(WEATHER_OPTION.TEMP_GAMMA,
				Double.valueOf(prefs.getDouble(TEMP_GAMMA_KEY, DEFAULT_TEMP_GAMMA)));
		ws.setAttribute(WEATHER_OPTION.WIND_ALPHA,
				Double.valueOf(prefs.getDouble(WIND_ALPHA_KEY, DEFAULT_WIND_ALPHA)));
		ws.setAttribute(WEATHER_OPTION.WIND_BETA,
				Double.valueOf(prefs.getDouble(WIND_BETA_KEY, DEFAULT_WIND_BETA)));
		ws.setAttribute(WEATHER_OPTION.WIND_GAMMA,
				Double.valueOf(prefs.getDouble(WIND_GAMMA_KEY, DEFAULT_WIND_GAMMA)));

		Double ffmc = prefs.getDouble(DAILY_FFMC_KEY, DEFAULT_DAILY_FFMC);
		ws.setAttribute(WEATHER_OPTION.INITIAL_FFMC, ffmc);
		nwc.setAttribute(WEATHER_OPTION.INITIAL_FFMC, ffmc);
		
		if(chkDailyFit.isSelected() && fitFFMCVal != -1)
			fbpCalculations.ffmc = fitFFMCVal;
		else
			fbpCalculations.ffmc = ffmc;
		
		Double dmc = prefs.getDouble(DAILY_DMC_KEY, DEFAULT_DAILY_DMC);
		ws.setAttribute(WEATHER_OPTION.INITIAL_DMC, dmc);
		nwc.setAttribute(WEATHER_OPTION.INITIAL_DMC, dmc);
		fbpCalculations.dmc = dmc;
		Double dc = prefs.getDouble(DAILY_DC_KEY, DEFAULT_DAILY_DC);
		ws.setAttribute(WEATHER_OPTION.INITIAL_DC, dc);
		nwc.setAttribute(WEATHER_OPTION.INITIAL_DC, dc);
		fbpCalculations.dc = dc;
		Double precip = prefs.getDouble(DAILY_PRECIPITATION_KEY, 0.0);
		ws.setAttribute(WEATHER_OPTION.INITIAL_RAIN, precip);

		if (comboHourlyMethod.getSelectedIndex() == 0)
			ws.setAttribute(WEATHER_OPTION.FFMC_LAWSON, Boolean.valueOf(true));
		else {
			ws.setAttribute(WEATHER_OPTION.FFMC_VANWAGNER, Boolean.valueOf(true));
			ws.setAttribute(WEATHER_OPTION.INITIAL_HFFMCTIME, Long.valueOf((long)((prefs.getInt(HOURLY_FFMC_START_KEY, 0)) * 60 * 60)));
			ws.setAttribute(WEATHER_OPTION.INITIAL_HFFMC,
					Double.valueOf(Double.parseDouble((String)prefs.get(HOURLY_FFMC_KEY, DEFAULT_HOURLY_FFMC.toString()))));
		}

		//ws.setAttribute(CWFGM_WEATHER_OPTION.FWI_USE_SPECIFIED, Boolean.valueOf(true));

		Double lat = app.getLatitude();
		Double lon = app.getLongitude();
		if (lat != null) {
			fbpCalculations.latitude = lat;
			lat = DEGREE_TO_RADIAN(lat);
			ws.setAttribute(GRID_ATTRIBUTE.LATITUDE, lat);
			nwc.setAttribute(GRID_ATTRIBUTE.LATITUDE, lat);
		}
		if (lon != null) {
			fbpCalculations.longitude = lon;
			lon = DEGREE_TO_RADIAN(lon);
			ws.setAttribute(GRID_ATTRIBUTE.LONGITUDE, lon);
			nwc.setAttribute(GRID_ATTRIBUTE.LONGITUDE, lon);
		}
		
		fbpCalculations.fuelType = FbpTab.adjustIndexComboBoxToFuelType(prefs.getInt(FUEL_TYPE_INDEX_KEY, 0));
		fbpCalculations.elevation = Double.parseDouble(prefs.get(ELEVATION_KEY, DEFAULT_ELEVATION.toString()));

		if (prefs.getBoolean(USE_SLOPE_KEY, DEFAULT_USE_SLOPE))
			fbpCalculations.useSlope = true;
		else
			fbpCalculations.useSlope = false;
		fbpCalculations.slopeValue = Double.parseDouble(prefs.get(SLOPE_KEY, String.valueOf(DEFAULT_SLOPE)));
		fbpCalculations.aspect = Double.parseDouble(prefs.get(ASPECT_KEY, String.valueOf(DEFAULT_ASPECT)));

		fbpCalculations.useBui = true;

		int index = comboFbpFuelType.getSelectedIndex();
		switch (index) {
		case 5:
			fbpCalculations.crownBase = Double.parseDouble(prefs.get(CROWN_BASE_HEIGHT_KEY, DEFAULT_CROWN_BASE_HEIGHT.toString()));
			break;
		case 9:
		case 10:
			fbpCalculations.conifMixedWood = Double.parseDouble(prefs.get(PERCENT_CONIFER_KEY, DEFAULT_PERCENT_CONIFER.toString()));
			break;
		case 11:
		case 12:
			fbpCalculations.deadBalsam = Double.parseDouble(prefs.get(PERCENT_DEAD_FIR_KEY, DEFAULT_PERCENT_DEAD_FIR.toString()));
			break;
		case 13:
		case 14:
		//case 15: O1ab
			fbpCalculations.grassCuring = Double.parseDouble(prefs.get(GRASS_CURING_KEY, DEFAULT_GRASS_CURING.toString()));
			fbpCalculations.grassFuelLoad = Double.parseDouble(prefs.get(GRASS_FUEL_LOAD_KEY, DEFAULT_GRASS_FUEL_LOAD.toString()));
			break;
		}

		fbpCalculations.elapsedTime = 60.0;
		fbpCalculations.acceleration = false;
		fbpCalculations.useBuildup = true;

		return true;
	}

	private StatsTableModel.Column createIfNotExist(StatsDataType type) {
		if (!model.columnExists(type)) {
			Column c = model.new Column(type);
			c.setEditable(false);
			return c;
		}
		return model.getColumn(type);
	}
	
	private StatsTableModel.Column createIfNotExistForDay(StatsDataType type) {
		if (!model.columnExists(type)) {
			Column c = model.new Column(type, true, DisplayType.DAILY);
			c.setEditable(false);
			return c;
		}
		return model.getColumn(type);
	}
	
	private StatsTableModel.Column createIfNotExistForNoon(StatsDataType type) {
		if (!model.columnExists(type)) {
			Column c = model.new Column(type, true, DisplayType.NOON);
			c.setEditable(false);
			return c;
		}
		return model.getColumn(type);
	}

	private StatsTableModel.Column createIfNotExistInvisible(StatsDataType type) {
		if (!model.columnExists(type)) {
			Column c = model.new Column(type, false);
			c.setEditable(false);
			return c;
		}
		return model.getColumn(type);
	}
	
	public void calculate() {
		if (filetype == FileType.WEATHER_STREAM) {
			if (chkDailyFit.isSelected())
				scanningForFFMC = true;
			calculateHourly();
			scanningForFFMC = false;
			if(chkDailyFit.isSelected()) {
				fitFFMCFlag = true;
				txtHourlyFFMC.setText(DecimalUtils.format(fitFFMCVal, DecimalUtils.DataType.FFMC));
				calculateHourly();
				fitFFMCVal = -1;
			}
		}
		else if (filetype == FileType.NOON_WEATHER) {
			calculateNoon();
		}
		updatePrecipitationTooltip();
	}
	
	public void calculateNoon() {
		if (m_PauseCalculate)
			return;
		if (!wsGetValuesFromForm()) {
			return;
		}
		tglHourly.setEnabled(false);
		tglDaily.setEnabled(false);
		tglNoon.setEnabled(false);
		
		OutVariable<WTime> start = new OutVariable<>();
		start.value = new WTime(new WTimeManager(new WorldLocation()));
		OutVariable<WTimeSpan> duration = new OutVariable<WTimeSpan>();
		duration.value = new WTimeSpan();
		nwc.getValidTimeRange(start, duration);
		if (start.value == null || duration.value == null || start.value.getTotalSeconds() == 0) {
			tglNoon.setEnabled(true);
			return;
		}
		
		WorldLocation loc = new WorldLocation();
		loc.setTimezoneOffset(app.getSelectedTimeZone().getTimezoneOffset());
		loc.setLatitude(DEGREE_TO_RADIAN(app.getLatitude().doubleValue()));
		loc.setLongitude(DEGREE_TO_RADIAN(app.getLongitude().doubleValue()));
		WTimeManager appManager = new WTimeManager(loc);
		
		WTime ts = new WTime(start.value.getTime(0), appManager);
		WTime te = WTime.add(ts, duration.value);
		WTime t = new WTime(ts);
		
		WTime tsWs = new WTime(start.value);
		WTime tWs = new WTime(tsWs);
		
		int noonlst;
		if (tWs.getTimeManager().getWorldLocation().getEndDST().getTotalSeconds() > 0)
			noonlst = 13;
		else
			noonlst = 12;
		t.add(new WTimeSpan(0, noonlst, 0, 0));
		tWs.add(new WTimeSpan(0, noonlst, 0, 0));
		
		Column noontemp = createIfNotExistForNoon(StatsDataType.NOON_TEMPERATURE); noontemp.clear();
		Column noondew = createIfNotExistForNoon(StatsDataType.NOON_DEWPOINT); noondew.clear();
		Column noonrh = createIfNotExistForNoon(StatsDataType.NOON_RH); noonrh.clear();
		Column noonws = createIfNotExistForNoon(StatsDataType.NOON_WS); noonws.clear();
		Column noonwd = createIfNotExistForNoon(StatsDataType.NOON_WD); noonwd.clear();
		Column noonprecip = createIfNotExistForNoon(StatsDataType.NOON_PRECIP); noonprecip.clear();
		Column noonffmc = createIfNotExistForNoon(StatsDataType.NOON_FFMC); noonffmc.clear();
		Column noondmc = createIfNotExistForNoon(StatsDataType.NOON_DMC); noondmc.clear();
		Column noondc = createIfNotExistForNoon(StatsDataType.NOON_DC); noondc.clear();
		Column noonisi = createIfNotExistForNoon(StatsDataType.NOON_ISI); noonisi.clear();
		Column noonbui = createIfNotExistForNoon(StatsDataType.NOON_BUI); noonbui.clear();
		Column noonfwi = createIfNotExistForNoon(StatsDataType.NOON_FWI); noonfwi.clear();
        Column noondsr = createIfNotExistForNoon(StatsDataType.NOON_DSR); noondsr.clear();
		
		Column noonRise = createIfNotExistForNoon(StatsDataType.NOON_SUNRISE); noonRise.clear();
		Column noonNoon = createIfNotExistForNoon(StatsDataType.NOON_SOLAR_NOON); noonNoon.clear();
		Column noonSet = createIfNotExistForNoon(StatsDataType.NOON_SUNSET); noonSet.clear();
		
		Map<WTime, Object> noontemplist = new TreeMap<>();
		Map<WTime, Object> noonrhlist = new TreeMap<>();
		Map<WTime, Object> noonwslist = new TreeMap<>();
		Map<WTime, Object> noonwdlist = new TreeMap<>();
		Map<WTime, Object> noonpreciplist = new TreeMap<>();
		Map<WTime, Object> noondewlist = new TreeMap<>();
		Map<WTime, Object> noondclist = new TreeMap<>();
		Map<WTime, Object> noondmclist = new TreeMap<>();
		Map<WTime, Object> noonffmclist = new TreeMap<>();
		Map<WTime, Object> noonbuilist = new TreeMap<>();
		Map<WTime, Object> noonisilist = new TreeMap<>();
		Map<WTime, Object> noonfwilist = new TreeMap<>();
        Map<WTime, Object> noondsrlist = new TreeMap<>();
		Map<WTime, Object> noonRiseList = new TreeMap<>();
		Map<WTime, Object> noonNoonList = new TreeMap<>();
		Map<WTime, Object> noonSetList = new TreeMap<>();
		
		Map<WTime, Object> noonFBP_FMCList = new TreeMap<>();
		Map<WTime, Object> noonFBP_ISIList = new TreeMap<>();
		Map<WTime, Object> noonFBP_WSVList = new TreeMap<>();
		Map<WTime, Object> noonFBP_RAZList = new TreeMap<>();
		
		OutVariable<Double> noon_temp = new OutVariable<>();
		OutVariable<Double> noon_dew = new OutVariable<>();
		OutVariable<Double> noon_rh = new OutVariable<>();
		OutVariable<Double> noon_precip = new OutVariable<>();
		OutVariable<Double> noon_ws = new OutVariable<>();
		OutVariable<Double> noon_wd = new OutVariable<>();
		OutVariable<Double> noon_dc = new OutVariable<>();
		OutVariable<Double> noon_dmc = new OutVariable<>();
		OutVariable<Double> noon_ffmc = new OutVariable<>();
		OutVariable<Double> noon_bui = new OutVariable<>();
		OutVariable<Double> noon_isi = new OutVariable<>();
		OutVariable<Double> noon_fwi = new OutVariable<>();

		fbpCalculations.latitude = app.getLatitude();
		fbpCalculations.longitude = app.getLongitude();

		fbpCalculations.elevation = LineEditHelper.getDoubleFromLineEdit(txtFbpElevation);
		
		fbpCalculations.useSlope = chckFbpSlope.isSelected();
		fbpCalculations.slopeValue = LineEditHelper.getDoubleFromLineEdit(txtFbpSlope);
		fbpCalculations.aspect = LineEditHelper.getDoubleFromLineEdit(txtFbpAspect);
		
		fbpCalculations.fuelType = 	FbpTab.adjustIndexComboBoxToFuelType(comboFbpFuelType.getSelectedIndex());
		fbpCalculations.useBui = true;
		
		fbpCalculations.acceleration = true; //Use point ignition
		
		fbpCalculations.elapsedTime = 60;
		fbpCalculations.useBuildup = true;
		
		while (WTime.lessThan(t, te)) {
			try {
				nwc.getNoonWeatherValues(tWs, noon_temp, noon_dew, noon_rh, noon_ws, noon_wd, noon_precip);
				nwc.getNoonFWI(tWs, noon_dc, noon_dmc, noon_ffmc, noon_bui, noon_isi, noon_fwi);
			}
			catch (Exception ex) {
				nwc = new NoonWeatherCondition(ws.getTimeManager());
				JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.error.import"), "Error", JOptionPane.WARNING_MESSAGE);
				tglNoon.setEnabled(true);
				return;
			}
			
			calculateSunrise(tWs);	

			noonRiseList.put(t, riseStr);
			noonNoonList.put(t, noonStr);
			noonSetList.put(t, setStr);
			
			double val = noon_temp.value;
			val = Convert.convertUnit(val, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
			TwoString s = new TwoString(DecimalUtils.format(val, DataType.TEMPERATURE), DecimalUtils.format(noon_temp.value, DataType.FORCE_2));
			noontemplist.put(t, s);
			val = noon_dew.value;
			val = Convert.convertUnit(val, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
			s = new TwoString(DecimalUtils.format(val, DataType.TEMPERATURE), DecimalUtils.format(noon_temp.value, DataType.FORCE_2));
			noondewlist.put(t, s);
			noonrhlist.put(t, DecimalUtils.format(noon_rh.value * 100.0, DataType.RH));
			val = noon_ws.value;
			val = Convert.convertUnit(val, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
			s = new TwoString(DecimalUtils.format(val, DataType.WIND_SPEED), DecimalUtils.format(noon_ws.value, DataType.FORCE_2));
			noonwslist.put(t, s);
			noonwdlist.put(t, DecimalUtils.format(CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(noon_wd.value)), DecimalUtils.DataType.WIND_DIR));
			val = noon_precip.value;
			val = Convert.convertUnit(val, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
			s = new TwoString(DecimalUtils.format(val, DataType.PRECIP), DecimalUtils.format(noon_precip.value, DataType.FORCE_2));
			noonpreciplist.put(t, s);
			noondclist.put(t, DecimalUtils.format(noon_dc.value, DataType.DC));
			noondmclist.put(t, DecimalUtils.format(noon_dmc.value, DataType.DMC));
			noonffmclist.put(t, DecimalUtils.format(noon_ffmc.value, DataType.FFMC));
			noonbuilist.put(t, DecimalUtils.format(noon_bui.value, DataType.BUI));
			noonisilist.put(t, DecimalUtils.format(noon_isi.value, DataType.ISI));
			noonfwilist.put(t, DecimalUtils.format(noon_fwi.value, DataType.FWI));
            double temp_dsr = Fwi.dsr(noon_fwi.value);
            noondsrlist.put(t, DecimalUtils.format(temp_dsr));
			
			double dFBP_FMC = 0;
			double dFBP_ISI = 0;
			double dFBP_WSV = 0;
			double dFBP_RAZ = 0;
			
			try {
				setFBPCalc(noon_ffmc.value, noon_bui.value, noon_dmc.value, noon_dc.value, noon_ws.value, noon_wd.value, tWs);
				fbpCalculations.FBPCalculateStatisticsCOM();
				dFBP_FMC = fbpCalculations.fmc;
				dFBP_ISI = fbpCalculations.isi;
				dFBP_WSV = fbpCalculations.wsv;
				dFBP_RAZ = fbpCalculations.raz;
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			noonFBP_FMCList.put(t, DecimalUtils.format(dFBP_FMC, DataType.FFMC));
			noonFBP_ISIList.put(t, DecimalUtils.format(dFBP_ISI, DataType.ISI));
			noonFBP_WSVList.put(t, DecimalUtils.format(dFBP_WSV, DataType.WIND_SPEED));
			noonFBP_RAZList.put(t, DecimalUtils.format(dFBP_RAZ, DataType.WIND_DIR));
			
			t = WTime.add(t, WTimeSpan.Day);
			tWs = WTime.add(tWs, WTimeSpan.Day);
		}
		
		noonRise.putOrOverwrite(noonRiseList);
		noonNoon.putOrOverwrite(noonNoonList);
		noonSet.putOrOverwrite(noonSetList);
		
		noontemp.putOrOverwrite(noontemplist);
		noondew.putOrOverwrite(noondewlist);
		noonrh.putOrOverwrite(noonrhlist);
		noonws.putOrOverwrite(noonwslist);
		noonwd.putOrOverwrite(noonwdlist);
		noonprecip.putOrOverwrite(noonpreciplist);
		noondc.putOrOverwrite(noondclist);
		noondmc.putOrOverwrite(noondmclist);
		noonffmc.putOrOverwrite(noonffmclist);
		noonbui.putOrOverwrite(noonbuilist);
		noonisi.putOrOverwrite(noonisilist);
		noonfwi.putOrOverwrite(noonfwilist);
        noondsr.putOrOverwrite(noondsrlist);
		model.redraw();
		
		m_CanTransfer = false;
		notifyCanTransferUpdated(m_CanTransfer);
		tglNoon.setEnabled(true);
	}

	public void calculateHourly() {
		if (m_PauseCalculate)
			return;
		if (!wsGetValuesFromForm()) {
			return;
		}
		tglHourly.setEnabled(false);
		tglDaily.setEnabled(false);
		tglNoon.setEnabled(false);
		OutVariable<IWXData> wx = new OutVariable<>();
		OutVariable<IFWIData> ifwi = new OutVariable<>();
		OutVariable<DFWIData> dfwi = new OutVariable<>();
		OutVariable<IWXData> wxnoon = new OutVariable<>();
		wx.value = new IWXData();
		ifwi.value = new IFWIData();
		dfwi.value = new DFWIData();
		wxnoon.value = new IWXData();
		
		OutVariable<Double> min_temp = new OutVariable<>();
		OutVariable<Double> max_temp = new OutVariable<>();
		OutVariable<Double> day_rh = new OutVariable<>();
		OutVariable<Double> day_precip = new OutVariable<>();
		OutVariable<Double> min_ws = new OutVariable<>();
		OutVariable<Double> max_ws = new OutVariable<>();
		OutVariable<Double> day_wd = new OutVariable<>();
		min_temp.value = 0.0;
		max_temp.value = 0.0;
		day_rh.value = 0.0;
		day_precip.value = 0.0;
		min_ws.value = 0.0;
		max_ws.value = 0.0;
		day_wd.value = 0.0;
		boolean first = true;
		OutVariable<WTime> start = new OutVariable<>();
		start.value = new WTime(new WTimeManager(new WorldLocation()));
		OutVariable<WTimeSpan> duration = new OutVariable<WTimeSpan>();
		duration.value = new WTimeSpan();
		ws.getValidTimeRange(start, duration);
		
		if (start.value == null || duration.value == null || start.value.getTotalSeconds() == 0) {
			tglHourly.setEnabled(true);
			tglDaily.setEnabled(true);
			tglNoon.setEnabled(true);
			return;
		}
		
		WorldLocation loc = new WorldLocation();
		loc.setTimezoneOffset(app.getSelectedTimeZone().getTimezoneOffset());
		loc.setDSTAmount(app.getSelectedTimeZone().getDSTAmount());
		if (app.getSelectedTimeZone().getDSTAmount().getTotalSeconds() > 0)
			loc.setEndDST(new WTimeSpan(366, 0, 0, 0));
		loc.setLatitude(DEGREE_TO_RADIAN(app.getLatitude().doubleValue()));
		loc.setLongitude(DEGREE_TO_RADIAN(app.getLongitude().doubleValue()));
		WTimeManager appManager = new WTimeManager(loc);
		
		WTime ts = new WTime(start.value.getTime(0), appManager);
		WTime te = WTime.add(ts, duration.value);
		WTime t = new WTime(ts);
		
		WTime tsWs = new WTime(start.value);
		WTime tWs = new WTime(tsWs);
		
		StatsTableModel.Column temp = createIfNotExist(StatsDataType.IMPORT_TEMPERATURE); temp.clear();
		StatsTableModel.Column dew = createIfNotExist(StatsDataType.IMPORT_DEWPOINT); dew.clear();
		StatsTableModel.Column rh = createIfNotExist(StatsDataType.IMPORT_RH); rh.clear();
		StatsTableModel.Column wsp = createIfNotExist(StatsDataType.IMPORT_WS); wsp.clear();
		StatsTableModel.Column wd = createIfNotExist(StatsDataType.IMPORT_WD); wd.clear();
		StatsTableModel.Column precip = createIfNotExist(StatsDataType.IMPORT_PRECIP); precip.clear();
		StatsTableModel.Column dffmc = createIfNotExist(StatsDataType.FFMC); dffmc.clear();
		StatsTableModel.Column ddmc = createIfNotExist(StatsDataType.DMC); ddmc.clear();
		StatsTableModel.Column ddc = createIfNotExist(StatsDataType.DC); ddc.clear();
		StatsTableModel.Column disi = createIfNotExist(StatsDataType.ISI); disi.clear();
		StatsTableModel.Column dbui = createIfNotExist(StatsDataType.BUI); dbui.clear();
		StatsTableModel.Column dafwi = createIfNotExist(StatsDataType.FWI); dafwi.clear();
		StatsTableModel.Column dadsr = createIfNotExist(StatsDataType.DSR); dadsr.clear();
		StatsTableModel.Column ffmc = createIfNotExist(StatsDataType.HFFMC); ffmc.clear();
		StatsTableModel.Column isi = createIfNotExist(StatsDataType.HISI); isi.clear();
		StatsTableModel.Column fwi = createIfNotExist(StatsDataType.HFWI); fwi.clear();
		
		StatsTableModel.Column hourFBP_FMC = createIfNotExist(StatsDataType.H_FBP_FMC); hourFBP_FMC.clear();
		StatsTableModel.Column hourFBP_ISI = createIfNotExist(StatsDataType.H_FBP_ISI); hourFBP_ISI.clear();
		StatsTableModel.Column hourFBP_WSV = createIfNotExist(StatsDataType.H_FBP_WSV); hourFBP_WSV.clear();
		StatsTableModel.Column hourFBP_RAZ = createIfNotExist(StatsDataType.H_FBP_RAZ); hourFBP_RAZ.clear();
		
		Column ROSt = createIfNotExistInvisible(StatsDataType.ROSt); ROSt.clear();
		Column ROSeq = createIfNotExist(StatsDataType.ROSeq); ROSeq.clear();
		Column HFI = createIfNotExist(StatsDataType.HFI); HFI.clear();
		
		Column CFB = createIfNotExistInvisible(StatsDataType.CFB); CFB.clear();
		CFB.setCfbPossible(fbpCalculations.cfbPossible);
		
		Column SFC = createIfNotExistInvisible(StatsDataType.SFC); SFC.clear();
		Column CFC = createIfNotExistInvisible(StatsDataType.CFC); CFC.clear();
		Column TFC = createIfNotExistInvisible(StatsDataType.TFC); TFC.clear();
		Column RSO = createIfNotExistInvisible(StatsDataType.RSO); RSO.clear();
		Column FROS = createIfNotExistInvisible(StatsDataType.FROS); FROS.clear();
		Column BROS = createIfNotExistInvisible(StatsDataType.BROS); BROS.clear();
		Column CSI = createIfNotExistInvisible(StatsDataType.CSI); CSI.clear();
		Column FFI = createIfNotExistInvisible(StatsDataType.FFI); FFI.clear();
		Column BSI = createIfNotExistInvisible(StatsDataType.BFI); BSI.clear();
		Column DH = createIfNotExistInvisible(StatsDataType.DH); DH.clear();
		Column DF = createIfNotExistInvisible(StatsDataType.DF); DF.clear();
		Column DB = createIfNotExistInvisible(StatsDataType.DB); DB.clear();
		Column LB = createIfNotExistInvisible(StatsDataType.LB); LB.clear();
		Column AREA = createIfNotExistInvisible(StatsDataType.AREA); AREA.clear();
		Column PERIMITER = createIfNotExistInvisible(StatsDataType.PERIMITER); PERIMITER.clear();
		
		Column daymintemp = createIfNotExistForDay(StatsDataType.MIN_TEMP); daymintemp.clear();
		Column daymaxtemp = createIfNotExistForDay(StatsDataType.MAX_TEMP); daymaxtemp.clear();
		Column dayrh = createIfNotExistForDay(StatsDataType.DAY_RH); dayrh.clear();
		Column dayminws = createIfNotExistForDay(StatsDataType.MIN_WS); dayminws.clear();
		Column daymaxws = createIfNotExistForDay(StatsDataType.MAX_WS); daymaxws.clear();
		Column daywd = createIfNotExistForDay(StatsDataType.DAY_WD); daywd.clear();
		Column dayprecip = createIfNotExistForDay(StatsDataType.DAY_PRECIP); dayprecip.clear();
		Column dayffmc = createIfNotExistForDay(StatsDataType.DAY_FFMC); dayffmc.clear();
		Column daydmc = createIfNotExistForDay(StatsDataType.DAY_DMC); daydmc.clear();
		Column daydc = createIfNotExistForDay(StatsDataType.DAY_DC); daydc.clear();
		Column dayisi = createIfNotExistForDay(StatsDataType.DAY_ISI); dayisi.clear();
		Column daybui = createIfNotExistForDay(StatsDataType.DAY_BUI); daybui.clear();
		Column dayfwi = createIfNotExistForDay(StatsDataType.DAY_FWI); dayfwi.clear();
        Column daydsr = createIfNotExistForDay(StatsDataType.DAY_DSR); daydsr.clear();
		
		Column noontemp = createIfNotExistForNoon(StatsDataType.NOON_TEMPERATURE); noontemp.clear();
		Column noonrh = createIfNotExistForNoon(StatsDataType.NOON_RH); noonrh.clear();
		Column noonws = createIfNotExistForNoon(StatsDataType.NOON_WS); noonws.clear();
		Column noonwd = createIfNotExistForNoon(StatsDataType.NOON_WD); noonwd.clear();
		Column noondew = createIfNotExistForNoon(StatsDataType.NOON_DEWPOINT); noondew.clear();
		Column noonffmc = createIfNotExistForNoon(StatsDataType.NOON_FFMC); noonffmc.clear();
		Column noondmc = createIfNotExistForNoon(StatsDataType.NOON_DMC); noondmc.clear();
		Column noondc = createIfNotExistForNoon(StatsDataType.NOON_DC); noondc.clear();
		Column noonisi = createIfNotExistForNoon(StatsDataType.NOON_ISI); noonisi.clear();
		Column noonbui = createIfNotExistForNoon(StatsDataType.NOON_BUI); noonbui.clear();
		Column noonfwi = createIfNotExistForNoon(StatsDataType.NOON_FWI); noonfwi.clear();
        Column noondsr = createIfNotExistForNoon(StatsDataType.NOON_DSR); noondsr.clear();
		Column noonprecip = createIfNotExistForNoon(StatsDataType.NOON_PRECIP); noonprecip.clear();

		Column noonFBP_FMC = createIfNotExistForNoon(StatsDataType.D_FBP_FMC); noonFBP_FMC.clear();
		Column noonFBP_ISI = createIfNotExistForNoon(StatsDataType.D_FBP_ISI); noonFBP_ISI.clear();
		Column noonFBP_WSV = createIfNotExistForNoon(StatsDataType.D_FBP_WSV); noonFBP_WSV.clear();
		Column noonFBP_RAZ = createIfNotExistForNoon(StatsDataType.D_FBP_RAZ); noonFBP_RAZ.clear();

		Column noonROSt = createIfNotExistForNoon(StatsDataType.NOON_ROSt); noonROSt.clear();
		Column noonROSeq = createIfNotExistForNoon(StatsDataType.NOON_ROSeq); noonROSeq.clear();
		Column noonHFI = createIfNotExistForNoon(StatsDataType.NOON_HFI); noonHFI.clear();
		Column noonCFB = createIfNotExistForNoon(StatsDataType.NOON_CFB); noonCFB.clear();
		noonCFB.setCfbPossible(fbpCalculations.cfbPossible);
		Column noonSFC = createIfNotExistForNoon(StatsDataType.NOON_SFC); noonSFC.clear();
		Column noonCFC = createIfNotExistForNoon(StatsDataType.NOON_CFC); noonCFC.clear();
		Column noonTFC = createIfNotExistForNoon(StatsDataType.NOON_TFC); noonTFC.clear();

		Column noonRSO = createIfNotExistForNoon(StatsDataType.NOON_RSO); noonRSO.clear();
		Column noonFROS = createIfNotExistForNoon(StatsDataType.NOON_FROS); noonFROS.clear();
		Column noonBROS = createIfNotExistForNoon(StatsDataType.NOON_BROS); noonBROS.clear();
		Column noonCSI = createIfNotExistForNoon(StatsDataType.NOON_CSI); noonCSI.clear();
		Column noonFFI = createIfNotExistForNoon(StatsDataType.NOON_FFI); noonFFI.clear();
		Column noonBSI = createIfNotExistForNoon(StatsDataType.NOON_BFI); noonBSI.clear();
		Column noonDH = createIfNotExistForNoon(StatsDataType.NOON_DH); noonDH.clear();
		Column noonDF = createIfNotExistForNoon(StatsDataType.NOON_DF); noonDF.clear();
		Column noonDB = createIfNotExistForNoon(StatsDataType.NOON_DB); noonDB.clear();
		Column noonLB = createIfNotExistForNoon(StatsDataType.NOON_LB); noonLB.clear();
		Column noonAREA = createIfNotExistForNoon(StatsDataType.NOON_AREA); noonAREA.clear();
		Column noonPERIMITER = createIfNotExistForNoon(StatsDataType.NOON_PERIMITER); noonPERIMITER.clear();
		
		Column sunrise = createIfNotExistForDay(StatsDataType.SUNRISE); sunrise.clear();
		Column solarnoon = createIfNotExistForDay(StatsDataType.SOLAR_NOON); solarnoon.clear();
		Column sunset = createIfNotExistForDay(StatsDataType.SUNSET); sunset.clear();
		
		Column noonRise = createIfNotExistForNoon(StatsDataType.NOON_SUNRISE); noonRise.clear();
		Column noonNoon = createIfNotExistForNoon(StatsDataType.NOON_SOLAR_NOON); noonNoon.clear();
		Column noonSet = createIfNotExistForNoon(StatsDataType.NOON_SUNSET); noonSet.clear();
		
		StatsTableModel.Column hourRise = createIfNotExist(StatsDataType.H_SUNRISE); hourRise.clear();
		StatsTableModel.Column hourNoon = createIfNotExist(StatsDataType.H_SOLAR_NOON); hourNoon.clear();
		StatsTableModel.Column hourSet = createIfNotExist(StatsDataType.H_SUNSET); hourSet.clear();
		
		Map<WTime, Object> templist = new TreeMap<>();
		Map<WTime, Object> rhlist = new TreeMap<>();
		Map<WTime, Object> preciplist = new TreeMap<>();
		Map<WTime, Object> wsplist = new TreeMap<>();
		Map<WTime, Object> wdlist = new TreeMap<>();
		Map<WTime, Object> dewlist = new TreeMap<>();
		Map<WTime, Object> fwilist = new TreeMap<>();
		Map<WTime, Object> ffmclist = new TreeMap<>();
		Map<WTime, Object> isilist = new TreeMap<>();
		Map<WTime, Object> dffmclist = new TreeMap<>();
		Map<WTime, Object> ddmclist = new TreeMap<>();
		Map<WTime, Object> dbuilist = new TreeMap<>();
		Map<WTime, Object> ddclist = new TreeMap<>();
		Map<WTime, Object> disilist = new TreeMap<>();
		Map<WTime, Object> dafwilist = new TreeMap<>();
        Map<WTime, Object> dadsrlist = new TreeMap<>();
		Map<WTime, Object> ROStlist = new TreeMap<>();
		Map<WTime, Object> ROSeqlist = new TreeMap<>();
		Map<WTime, Object> HFIlist = new TreeMap<>();
		Map<WTime, Object> CFBlist = new TreeMap<>();
		Map<WTime, Object> SFClist = new TreeMap<>();
		Map<WTime, Object> CFClist = new TreeMap<>();
		Map<WTime, Object> TFClist = new TreeMap<>();
		Map<WTime, Object> RSOlist = new TreeMap<>();
		Map<WTime, Object> FROSlist = new TreeMap<>();
		Map<WTime, Object> BROSlist = new TreeMap<>();
		Map<WTime, Object> CSIlist = new TreeMap<>();
		Map<WTime, Object> FFIlist = new TreeMap<>();
		Map<WTime, Object> BSIlist = new TreeMap<>();
		Map<WTime, Object> DHlist = new TreeMap<>();
		Map<WTime, Object> DFlist = new TreeMap<>();
		Map<WTime, Object> DBlist = new TreeMap<>();
		Map<WTime, Object> LBlist = new TreeMap<>();
		Map<WTime, Object> AREAlist = new TreeMap<>();
		Map<WTime, Object> PERIMITERlist = new TreeMap<>();
		
		//maps for daily values
		Map<WTime, Object> daymintemplist = new TreeMap<>();
		Map<WTime, Object> daymaxtemplist = new TreeMap<>();
		Map<WTime, Object> dayrhlist = new TreeMap<>();
		Map<WTime, Object> daypreciplist = new TreeMap<>();
		Map<WTime, Object> dayminwslist = new TreeMap<>();
		Map<WTime, Object> daymaxwslist = new TreeMap<>();
		Map<WTime, Object> daywdlist = new TreeMap<>();
		Map<WTime, Object> dayffmclist = new TreeMap<>();
		Map<WTime, Object> daydmclist = new TreeMap<>();
		Map<WTime, Object> daydclist = new TreeMap<>();
		Map<WTime, Object> dayisilist = new TreeMap<>();
		Map<WTime, Object> daybuilist = new TreeMap<>();
		Map<WTime, Object> dayfwilist = new TreeMap<>();
        Map<WTime, Object> daydsrlist = new TreeMap<>();
		
		Map<WTime, Object> noontemplist = new TreeMap<>();
		Map<WTime, Object> noonrhlist = new TreeMap<>();
		Map<WTime, Object> noonwslist = new TreeMap<>();
		Map<WTime, Object> noonwdlist = new TreeMap<>();
		Map<WTime, Object> noonpreciplist = new TreeMap<>();
		Map<WTime, Object> noondewlist = new TreeMap<>();
		Map<WTime, Object> noondclist = new TreeMap<>();
		Map<WTime, Object> noondmclist = new TreeMap<>();
		Map<WTime, Object> noonffmclist = new TreeMap<>();
		Map<WTime, Object> noonbuilist = new TreeMap<>();
		Map<WTime, Object> noonisilist = new TreeMap<>();
		Map<WTime, Object> noonfwilist = new TreeMap<>();
        Map<WTime, Object> noondsrlist = new TreeMap<>();

        Map<WTime, Object> noonFBPFMCList = new TreeMap<>();
        Map<WTime, Object> noonFBPISIList = new TreeMap<>();
        Map<WTime, Object> noonFBPWSVList = new TreeMap<>();
        Map<WTime, Object> noonFBPRAZList = new TreeMap<>();

        Map<WTime, Object> noonROStlist = new TreeMap<>();
        Map<WTime, Object> noonROSeqlist = new TreeMap<>();
        Map<WTime, Object> noonHFIlist = new TreeMap<>();
        Map<WTime, Object> noonCFBlist = new TreeMap<>();
        Map<WTime, Object> noonSFClist = new TreeMap<>();
        Map<WTime, Object> noonCFClist = new TreeMap<>();
        Map<WTime, Object> noonTFClist = new TreeMap<>();

        Map<WTime, Object> noonRSOlist = new TreeMap<>();
        Map<WTime, Object> noonFROSlist = new TreeMap<>();
        Map<WTime, Object> noonBROSlist = new TreeMap<>();
        Map<WTime, Object> noonCSIlist = new TreeMap<>();
        Map<WTime, Object> noonFFIlist = new TreeMap<>();
        Map<WTime, Object> noonBSIlist = new TreeMap<>();
        Map<WTime, Object> noonDHlist = new TreeMap<>();
        Map<WTime, Object> noonDFlist = new TreeMap<>();
        Map<WTime, Object> noonDBlist = new TreeMap<>();
        Map<WTime, Object> noonLBlist = new TreeMap<>();
        Map<WTime, Object> noonAREAlist = new TreeMap<>();
        Map<WTime, Object> noonPERIMITERlist = new TreeMap<>();
		
		Map<WTime, Object> hourFBPFMCList = new TreeMap<>();
		Map<WTime, Object> hourFBPISIList = new TreeMap<>();
		Map<WTime, Object> hourFBPWSVList = new TreeMap<>();
		Map<WTime, Object> hourFBPRAZList = new TreeMap<>();
		
		Map<WTime, Object> sunriselist = new TreeMap<>();
		Map<WTime, Object> solarnoonlist = new TreeMap<>();
		Map<WTime, Object> sunsetlist = new TreeMap<>();
		
		Map<WTime, Object> noonRiseList = new TreeMap<>();
		Map<WTime, Object> noonNoonList = new TreeMap<>();
		Map<WTime, Object> noonSetList = new TreeMap<>();
		
		Map<WTime, Object> hourRiseList = new TreeMap<>();
		Map<WTime, Object> hourNoonList = new TreeMap<>();
		Map<WTime, Object> hourSetList = new TreeMap<>();

		fbpCalculations.m_date = Calendar.getInstance();
		fbpCalculations.m_date.set(Calendar.YEAR, (int)t.getYear(WTime.FORMAT_WITHDST | WTime.FORMAT_AS_LOCAL));
		fbpCalculations.m_date.set(Calendar.MONTH, (int)t.getMonth(WTime.FORMAT_WITHDST | WTime.FORMAT_AS_LOCAL) - 1);
		fbpCalculations.m_date.set(Calendar.DAY_OF_MONTH, (int)t.getDay(WTime.FORMAT_WITHDST | WTime.FORMAT_AS_LOCAL));
		fbpCalculations.m_date.set(Calendar.HOUR_OF_DAY, (int)t.getHour(WTime.FORMAT_WITHDST | WTime.FORMAT_AS_LOCAL));
		fbpCalculations.m_date.set(Calendar.MINUTE, (int)t.getMinute(WTime.FORMAT_WITHDST | WTime.FORMAT_AS_LOCAL));
		fbpCalculations.m_date.set(Calendar.SECOND, 0);
		
		int noonlst;
		if (tWs.getTimeManager().getWorldLocation().getEndDST().getTotalSeconds() > 0)
			noonlst = 13;
		else
			noonlst = 12;
		
		model.clearFlags();

		if (cmbHourlyStartSkipAction && !chkDailyFit.isSelected()) {
			int hffmcHour = (int)(t.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST));
			cmb_hourlyStart.setSelectedIndex(hffmcHour);
			if (comboHourlyMethod.getSelectedIndex() == 1)
				ws.setAttribute(WEATHER_OPTION.INITIAL_HFFMCTIME, Long.valueOf((long)(hffmcHour * 60 * 60)));
		}
		
		calculateSunrise(tWs);
		fitFFMCFlag = false;
		
		String startText = "";
		String endText = "";
		if (app.getSelectedTimeZone().getDSTAmount().getTotalSeconds() > 0)
			startText = "1301";
		else
			startText = "1201";
		endText = (t.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) == 0 ? "23" : t.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) - 1) + "";
		
		if(endText.length() == 1)
			endText = "0" + endText;
		
		lblStatsPrecipitation.setText("Precipitation (" + startText + " - " + endText + "00" + ")");
		double daySumPrecip = 0.0;
		
		while (WTime.lessThan(t, te)) {
			try {
				ws.getInstantaneousValues(tWs, 0, wx, ifwi, dfwi);
				daySumPrecip += wx.value.precipitation;
				if (first || tWs.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) == 0)
					ws.getDailyValues(tWs, min_temp, max_temp, min_ws, max_ws, day_rh, day_precip, day_wd);
			}
			catch (Exception ex) {
				System.out.println(ex.toString());
				temp.delete();
				rh.delete();
				precip.delete();
				wsp.delete();
				wd.delete();
				dew.delete();
				fwi.delete();
				ffmc.delete();
				isi.delete();
				dffmc.delete();
				ddmc.delete();
				dbui.delete();
				ddc.delete();
				disi.delete();
				dafwi.delete();
				dadsr.delete();
				ROSt.delete();
				ROSeq.delete();
				HFI.delete();
				CFB.delete();
				SFC.delete();
				CFC.delete();
				TFC.delete();
				RSO.delete();
				FROS.delete();
				BROS.delete();
				CSI.delete();
				FFI.delete();
				BSI.delete();
				DH.delete();
				DF.delete();
				DB.delete();
				LB.delete();
				AREA.delete();
				PERIMITER.delete();
				ws = new CWFGM_WeatherStream();
				JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.error.import"), "Error", JOptionPane.WARNING_MESSAGE);
				tglHourly.setEnabled(true);
				tglDaily.setEnabled(true);
				tglNoon.setEnabled(true);
				return;
			}
			
			if (tWs.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) == 0) {
				calculateSunrise(t);
			}

			WTime noon = new WTime(tWs);
			if (tWs.getTimeManager().getWorldLocation().getEndDST().getTotalSeconds() > 0) {
				noon.subtract(new WTimeSpan(0, 13, 0, 0));
				noon.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
				noon.add(new WTimeSpan(0, 13, 0, 0));
			}
			else {
				noon.subtract(new WTimeSpan(0, 12, 0, 0));
				noon.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
				noon.add(new WTimeSpan(0, 12, 0, 0));
			}
			wxnoon.value.temperature = Double.MAX_VALUE;
			ws.getInstantaneousValues(noon, 0, wxnoon, null, null);
			if (wxnoon.value == null || wxnoon.value.temperature == Double.MAX_VALUE) {
				wxnoon.value = new IWXData();
				noon = new WTime(tWs);
				noon.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
				WTime dayNeutral = new WTime(noon, WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST, (short)1);
				WTime dayLST = new WTime(dayNeutral, WTime.FORMAT_AS_LOCAL, (short)-1);
				noon = new WTime(dayLST);
				noon.add(new WTimeSpan(0, 12, 0, 0));
				ws.getInstantaneousValues(noon, 0, wxnoon, null, null);
			}
			
			//first hour of the day, excluding the first day
			if (!first && tWs.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) == 0) {
			    WTime dt = new WTime(t);
			    dt = WTime.subtract(dt, WTimeSpan.Hour);
			    dt.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);

                double val = daySumPrecip;
                val = Convert.convertUnit(val, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
                TwoString s = new TwoString(DecimalUtils.format(val, DataType.PRECIP), DecimalUtils.format(day_precip.value, DataType.FORCE_2));
                daypreciplist.put(dt, s);
                daySumPrecip = 0.0;
			}
			
			if ((first && tWs.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) > noonlst) ||
					tWs.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) == noonlst) {
				double val;
				
				WTime dt = new WTime(t);
				dt.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
				
				val = min_temp.value;
				val = Convert.convertUnit(val, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
				TwoString s = new TwoString(DecimalUtils.format(val, DataType.TEMPERATURE), DecimalUtils.format(min_temp.value, DataType.FORCE_2));
				daymintemplist.put(dt, s);
				val = max_temp.value;
				val = Convert.convertUnit(val, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
				s = new TwoString(DecimalUtils.format(val, DataType.TEMPERATURE), DecimalUtils.format(max_temp.value, DataType.FORCE_2));
				daymaxtemplist.put(dt, s);
				val = min_ws.value;
				val = Convert.convertUnit(val, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
				s = new TwoString(DecimalUtils.format(val, DataType.WIND_SPEED), DecimalUtils.format(min_ws.value, DataType.FORCE_2));
				dayminwslist.put(dt, s);
				val = max_ws.value;
				val = Convert.convertUnit(val, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
				s = new TwoString(DecimalUtils.format(val, DataType.WIND_SPEED), DecimalUtils.format(max_ws.value, DataType.FORCE_2));
				daymaxwslist.put(dt, s);
				dayrhlist.put(dt, DecimalUtils.format(day_rh.value * 100, DataType.RH));
                daywdlist.put(dt, DecimalUtils.format(CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(day_wd.value)), DecimalUtils.DataType.WIND_DIR));
				
				sunriselist.put(dt, riseStr);
				solarnoonlist.put(dt, noonStr);
				sunsetlist.put(dt, setStr);	
			}
			// hourly weather information			
			if (wx.value != null) {
				double val = wx.value.temperature;
				val = Convert.convertUnit(val, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
				TwoString htemp = new TwoString(DecimalUtils.format(val, DataType.TEMPERATURE), DecimalUtils.format(wx.value.temperature, DataType.FORCE_2));
				templist.put(t, htemp);
				rhlist.put(t, DecimalUtils.format(wx.value.rh * 100.0, DecimalUtils.DataType.RH));
				val = wx.value.precipitation;
				val = Convert.convertUnit(val, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
				TwoString hprecip = new TwoString(DecimalUtils.format(val, DecimalUtils.DataType.PRECIP), DecimalUtils.format(wx.value.precipitation, DecimalUtils.DataType.FORCE_2));
				preciplist.put(t, hprecip);
				val = wx.value.windSpeed;
				val = Convert.convertUnit(val, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
				TwoString hws = new TwoString(DecimalUtils.format(val, DecimalUtils.DataType.WIND_SPEED), DecimalUtils.format(wx.value.windSpeed, DecimalUtils.DataType.FORCE_2));
				wsplist.put(t, hws);
				wdlist.put(t, DecimalUtils.format(CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(wx.value.windDirection)), DecimalUtils.DataType.WIND_DIR));
				val = wx.value.dewPointTemperature;
				val = Convert.convertUnit(val, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
				TwoString hdew = new TwoString(DecimalUtils.format(val, DecimalUtils.DataType.TEMPERATURE), DecimalUtils.format(wx.value.dewPointTemperature, DecimalUtils.DataType.FORCE_2));
				dewlist.put(t, hdew);
				
				if ((wx.value.specifiedBits & ca.wise.grid.IWXData.SPECIFIED.INTERPOLATED) != 0)
					model.makeInterpolated(t);
				//if the user wants corrected data highlighted and this row had invalid data in it
				if (highlightCorrectedRows && (wx.value.specifiedBits & ca.wise.grid.IWXData.SPECIFIED.INVALID_DATA) != 0)
				    model.makeCorrected(t);
				if (tWs.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) == noonlst) {
					noonRiseList.put(t, riseStr);
					noonNoonList.put(t, noonStr);
					noonSetList.put(t, setStr);
					
					noontemplist.put(t, htemp);
					noonrhlist.put(t, DecimalUtils.format(wx.value.rh * 100.0, DecimalUtils.DataType.RH));
					noonwslist.put(t, hws);
					noonwdlist.put(t, DecimalUtils.format(CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(wx.value.windDirection)), DecimalUtils.DataType.WIND_DIR));
					noondewlist.put(t, hdew);
					val = day_precip.value;
					val = Convert.convertUnit(val, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
					TwoString nprecip = new TwoString(DecimalUtils.format(val, DataType.PRECIP), DecimalUtils.format(day_precip.value, DataType.FORCE_2));
					noonpreciplist.put(t, nprecip);

                    fbpCalculations.ffmc = dfwi.value.dFFMC;
                    fbpCalculations.dmc = dfwi.value.dDMC;
                    fbpCalculations.dc = dfwi.value.dDC;
                    fbpCalculations.bui = dfwi.value.dBUI;
                    fbpCalculations.windSpeed = wxnoon.value.windSpeed;
                    fbpCalculations.windDirection = CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(wx.value.windDirection));
                    fbpCalculations.m_date = tWs.toCalendar(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
                    
                    try {
                        fbpCalculations.FBPCalculateStatisticsCOM();

                        val = fbpCalculations.ros_t;
                        val = Convert.convertUnit(val, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC));
                        TwoString s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.ros_t));
                        noonROStlist.put(t, s);
                        val = fbpCalculations.ros_eq;
                        val = Convert.convertUnit(val, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.ros_eq));
                        noonROSeqlist.put(t, s);
                        val = fbpCalculations.hfi;
                        val = Convert.convertUnit(val, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.hfi));
                        noonHFIlist.put(t, s);
                        noonCFBlist.put(t, DecimalUtils.format(fbpCalculations.cfb));
                        val = fbpCalculations.sfc;
                        val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.sfc));
                        noonSFClist.put(t, s);
                        val = fbpCalculations.cfc;
                        val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.cfc));
                        noonCFClist.put(t, s);
                        val = fbpCalculations.tfc;
                        val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.tfc));
                        noonTFClist.put(t, s);
                        val = fbpCalculations.rso;
                        val = Convert.convertUnit(val, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.rso));
                        noonRSOlist.put(t, s);
                        val = fbpCalculations.fros;
                        val = Convert.convertUnit(val, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.fros));
                        noonFROSlist.put(t, s);
                        val = fbpCalculations.bros;
                        val = Convert.convertUnit(val, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.bros));
                        noonBROSlist.put(t, s);
                        val = fbpCalculations.csi;
                        val = Convert.convertUnit(val, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.csi));
                        noonCSIlist.put(t, s);
                        val = fbpCalculations.ffi;
                        val = Convert.convertUnit(val, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.ffi));
                        noonFFIlist.put(t, s);
                        val = fbpCalculations.bfi;
                        val = Convert.convertUnit(val, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.bfi));
                        noonBSIlist.put(t, s);
                        val = fbpCalculations.distanceHead;
                        val = Convert.convertUnit(val, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.distanceHead));
                        noonDHlist.put(t, s);
                        val = fbpCalculations.distanceFlank;
                        val = Convert.convertUnit(val, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.distanceFlank));
                        noonDFlist.put(t, s);
                        val = fbpCalculations.distanceBack;
                        val = Convert.convertUnit(val, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.distanceBack));
                        noonDBlist.put(t, s);
                        noonLBlist.put(t, DecimalUtils.format(fbpCalculations.lb));
                        val = fbpCalculations.area;
                        val = Convert.convertUnit(val, UnitSystem.area(Main.unitSystem()), UnitSystem.area(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.area));
                        noonAREAlist.put(t, s);
                        val = fbpCalculations.perimeter;
                        val = Convert.convertUnit(val, UnitSystem.distanceMedium2(Main.unitSystem()), UnitSystem.distanceMedium2(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.perimeter));
                        noonPERIMITERlist.put(t, s);
                        
                        val = fbpCalculations.raz;
                        noonFBPRAZList.put(t, DecimalUtils.format(val, DecimalUtils.DataType.WIND_DIR));
                        
                        val = fbpCalculations.wsv;
                        val = Convert.convertUnit(val, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
                        s = new TwoString(DecimalUtils.format(val, DecimalUtils.DataType.WIND_SPEED), DecimalUtils.format(fbpCalculations.wsv, DecimalUtils.DataType.FORCE_2));
                        noonFBPWSVList.put(t, s);

                        val = dfwi.value.dISI;
                        s = new TwoString(DecimalUtils.format(val, DecimalUtils.DataType.ISI), DecimalUtils.format(val, DecimalUtils.DataType.FORCE_2));
                        noonFBPISIList.put(t, s);

                        val = fbpCalculations.fmc;
                        s = new TwoString(DecimalUtils.format(val, DecimalUtils.DataType.ISI), DecimalUtils.format(val, DecimalUtils.DataType.FORCE_2));
                        noonFBPFMCList.put(t, s);
                    }
                    catch (CloneNotSupportedException e) { }
				}
			}
			
			// hourly FWI values
			if (ifwi.value != null) {
				fwilist.put(t,  DecimalUtils.format(ifwi.value.dFWI, DecimalUtils.DataType.FWI));
				ffmclist.put(t, DecimalUtils.format(ifwi.value.dFFMC, DecimalUtils.DataType.FFMC));
				isilist.put(t, DecimalUtils.format(ifwi.value.dISI, DecimalUtils.DataType.ISI));
			}
			// daily FWI values
			if (dfwi.value != null) {
				dffmclist.put(t, DecimalUtils.format(dfwi.value.dFFMC, DecimalUtils.DataType.FFMC));
				ddmclist.put(t, DecimalUtils.format(dfwi.value.dDMC, DecimalUtils.DataType.DMC));
				dbuilist.put(t, DecimalUtils.format(dfwi.value.dBUI, DecimalUtils.DataType.BUI));
				ddclist.put(t, DecimalUtils.format(dfwi.value.dDC, DecimalUtils.DataType.DC));
				try {
					double temp_isi = Fwi.isiFWI(dfwi.value.dFFMC, wxnoon.value.windSpeed, 60 * 60);
					disilist.put(t, DecimalUtils.format(temp_isi, DecimalUtils.DataType.ISI));
					double temp_fwi = Fwi.fwi(temp_isi, dfwi.value.dBUI);
					dafwilist.put(t, DecimalUtils.format(temp_fwi, DecimalUtils.DataType.FWI));
			        double temp_dsr = Fwi.dsr(temp_fwi);
			        dadsrlist.put(tWs,  DecimalUtils.format(temp_dsr));
					if ((first && tWs.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) > noonlst) ||
							tWs.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) == noonlst) {
						WTime dt = new WTime(t);
						dt.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
						dayffmclist.put(dt, DecimalUtils.format(dfwi.value.dFFMC, DecimalUtils.DataType.FFMC));
						daydmclist.put(dt, DecimalUtils.format(dfwi.value.dDMC, DecimalUtils.DataType.DMC));
						daydclist.put(dt, DecimalUtils.format(dfwi.value.dDC, DecimalUtils.DataType.DC));
						dayisilist.put(dt, DecimalUtils.format(temp_isi, DecimalUtils.DataType.ISI));
						daybuilist.put(dt, DecimalUtils.format(dfwi.value.dBUI, DecimalUtils.DataType.BUI));
						dayfwilist.put(dt, DecimalUtils.format(temp_fwi, DecimalUtils.DataType.FWI));
                        daydsrlist.put(dt, DecimalUtils.format(temp_dsr));
					}
					if (tWs.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) == noonlst) {
						noondclist.put(t, DecimalUtils.format(dfwi.value.dDC, DecimalUtils.DataType.DC));
						noondmclist.put(t, DecimalUtils.format(dfwi.value.dDMC, DecimalUtils.DataType.DMC));
						noonffmclist.put(t, DecimalUtils.format(dfwi.value.dFFMC, DecimalUtils.DataType.FFMC));
						noonbuilist.put(t, DecimalUtils.format(dfwi.value.dBUI, DecimalUtils.DataType.BUI));
						noonisilist.put(t, DecimalUtils.format(temp_isi, DecimalUtils.DataType.ISI));
						noonfwilist.put(t, DecimalUtils.format(temp_fwi, DecimalUtils.DataType.FWI));
                        noondsrlist.put(t, DecimalUtils.format(temp_dsr));
					}
				}
				catch (Exception ex) { }
			}
			// FBP values
			{
				//hourly
				{
					fbpCalculations.ffmc = ifwi.value.dFFMC;
					fbpCalculations.dmc = dfwi.value.dDMC;
					fbpCalculations.dc = dfwi.value.dDC;
					fbpCalculations.bui = dfwi.value.dBUI;
					fbpCalculations.windSpeed = wx.value.windSpeed;
					fbpCalculations.windDirection = CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(wx.value.windDirection));
					fbpCalculations.m_date = tWs.toCalendar(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
					try {
						fbpCalculations.FBPCalculateStatisticsCOM();
						double val = fbpCalculations.ros_t;
						val = Convert.convertUnit(val, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC));
						TwoString s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.ros_t));
						ROStlist.put(t, s);
						val = fbpCalculations.ros_eq;
						val = Convert.convertUnit(val, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.ros_eq));
						ROSeqlist.put(t, s);
						val = fbpCalculations.hfi;
						val = Convert.convertUnit(val, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.hfi));
						HFIlist.put(t, s);
						CFBlist.put(t, DecimalUtils.format(fbpCalculations.cfb));
						val = fbpCalculations.sfc;
						val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.sfc));
						SFClist.put(t, s);
						val = fbpCalculations.cfc;
						val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.cfc));
						CFClist.put(t, s);
						val = fbpCalculations.tfc;
						val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.tfc));
						TFClist.put(t, s);
						val = fbpCalculations.rso;
						val = Convert.convertUnit(val, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.rso));
						RSOlist.put(t, s);
						val = fbpCalculations.fros;
						val = Convert.convertUnit(val, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.fros));
						FROSlist.put(t, s);
						val = fbpCalculations.bros;
						val = Convert.convertUnit(val, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.bros));
						BROSlist.put(t, s);
						val = fbpCalculations.csi;
						val = Convert.convertUnit(val, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.csi));
						CSIlist.put(t, s);
						val = fbpCalculations.ffi;
						val = Convert.convertUnit(val, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.ffi));
						FFIlist.put(t, s);
						val = fbpCalculations.bfi;
						val = Convert.convertUnit(val, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.bfi));
						BSIlist.put(t, s);
						val = fbpCalculations.distanceHead;
						val = Convert.convertUnit(val, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.distanceHead));
						DHlist.put(t, s);
						val = fbpCalculations.distanceFlank;
						val = Convert.convertUnit(val, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.distanceFlank));
						DFlist.put(t, s);
						val = fbpCalculations.distanceBack;
						val = Convert.convertUnit(val, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.distanceBack));
						DBlist.put(t, s);
						LBlist.put(t, DecimalUtils.format(fbpCalculations.lb));
						val = fbpCalculations.area;
						val = Convert.convertUnit(val, UnitSystem.area(Main.unitSystem()), UnitSystem.area(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.area));
						AREAlist.put(t, s);
						val = fbpCalculations.perimeter;
						val = Convert.convertUnit(val, UnitSystem.distanceMedium2(Main.unitSystem()), UnitSystem.distanceMedium2(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val), DecimalUtils.format(fbpCalculations.perimeter));
						PERIMITERlist.put(t, s);
						
						val = fbpCalculations.raz;
						hourFBPRAZList.put(t, DecimalUtils.format(val, DecimalUtils.DataType.WIND_DIR));
						
						val = fbpCalculations.wsv;
						val = Convert.convertUnit(val, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
						s = new TwoString(DecimalUtils.format(val, DecimalUtils.DataType.WIND_SPEED), DecimalUtils.format(fbpCalculations.wsv, DecimalUtils.DataType.FORCE_2));
						hourFBPWSVList.put(t, s);
					}
					catch (CloneNotSupportedException e) { }
				}
				//daily
				{
					fbpCalculations.ffmc = dfwi.value.dFFMC;
					fbpCalculations.dmc = dfwi.value.dDMC;
					fbpCalculations.dc = dfwi.value.dDC;
					fbpCalculations.bui = dfwi.value.dBUI;
					fbpCalculations.windSpeed = wxnoon.value.windSpeed;
					fbpCalculations.windDirection = CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(wx.value.windDirection));
					fbpCalculations.m_date = tWs.toCalendar(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
					try {
						fbpCalculations.FBPCalculateStatisticsCOM();

						double val = dfwi.value.dISI;
						TwoString s = new TwoString(DecimalUtils.format(val, DecimalUtils.DataType.ISI), DecimalUtils.format(val, DecimalUtils.DataType.FORCE_2));
						hourFBPISIList.put(t, s);

						val = fbpCalculations.fmc;
						s = new TwoString(DecimalUtils.format(val, DecimalUtils.DataType.ISI), DecimalUtils.format(val, DecimalUtils.DataType.FORCE_2));
						hourFBPFMCList.put(t, s);
					}
					catch (CloneNotSupportedException e) { }
				}
			}
			hourRiseList.put(t, riseStr);
			hourNoonList.put(t, noonStr);
			hourSetList.put(t, setStr);
			
			if(first) {
				fitFFMCVal = dfwi.value.dFFMC;
			}
			
			first = false;
			
			if(chkDailyFit.isSelected() && !fitFFMCFlag) {
				if(t.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) >= noonlst) {
					fitFFMCFlag = true;
					fitFFMCVal = dfwi.value.dFFMC;
					if (scanningForFFMC)
						break;//need to recalculate anyways
				}
			}
			
			t = WTime.add(t, WTimeSpan.Hour);
			tWs = WTime.add(tWs, WTimeSpan.Hour);
		}
		
        //add any remaining precipitation data, unless the loop ended on the first hour of a day (1 after the last increment)
        if (!first && tWs.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) != 1) {
            WTime dt = new WTime(t);
            dt = WTime.subtract(dt, WTimeSpan.Hour);
            dt.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);

            double val = daySumPrecip;
            val = Convert.convertUnit(val, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
            TwoString s = new TwoString(DecimalUtils.format(val, DataType.PRECIP), DecimalUtils.format(day_precip.value, DataType.FORCE_2));
            daypreciplist.put(dt, s);
            daySumPrecip = 0.0;
        }
        
		daymintemp.putOrOverwrite(daymintemplist);
		daymaxtemp.putOrOverwrite(daymaxtemplist);
		dayminws.putOrOverwrite(dayminwslist);
		daymaxws.putOrOverwrite(daymaxwslist);
		dayrh.putOrOverwrite(dayrhlist);
		dayprecip.putOrOverwrite(daypreciplist);
		daywd.putOrOverwrite(daywdlist);
		dayffmc.putOrOverwrite(dayffmclist);
		daydmc.putOrOverwrite(daydmclist);
		daydc.putOrOverwrite(daydclist);
		dayisi.putOrOverwrite(dayisilist);
		daybui.putOrOverwrite(daybuilist);
		dayfwi.putOrOverwrite(dayfwilist);
        daydsr.putOrOverwrite(daydsrlist);
        
		
		noontemp.putOrOverwrite(noontemplist);
		noonrh.putOrOverwrite(noonrhlist);
		noonws.putOrOverwrite(noonwslist);
		noonwd.putOrOverwrite(noonwdlist);
		noonprecip.putOrOverwrite(noonpreciplist);
		noondew.putOrOverwrite(noondewlist);
		noondc.putOrOverwrite(noondclist);
		noondmc.putOrOverwrite(noondmclist);
		noonffmc.putOrOverwrite(noonffmclist);
		noonbui.putOrOverwrite(noonbuilist);
		noonisi.putOrOverwrite(noonisilist);
		noonfwi.putOrOverwrite(noonfwilist);
        noondsr.putOrOverwrite(noondsrlist);
        
        noonFBP_FMC.putOrOverwrite(noonFBPFMCList);
        noonFBP_ISI.putOrOverwrite(noonFBPISIList);
        noonFBP_WSV.putOrOverwrite(noonFBPWSVList);
        noonFBP_RAZ.putOrOverwrite(noonFBPRAZList);

        noonROSt.putOrOverwrite(noonROStlist);
        noonROSeq.putOrOverwrite(noonROSeqlist);
        noonHFI.putOrOverwrite(noonHFIlist);
        noonCFB.putOrOverwrite(noonCFBlist);
        noonSFC.putOrOverwrite(noonSFClist);
        noonCFC.putOrOverwrite(noonCFClist);
        noonTFC.putOrOverwrite(noonTFClist);

        noonRSO.putOrOverwrite(noonRSOlist);
        noonFROS.putOrOverwrite(noonFROSlist);
        noonBROS.putOrOverwrite(noonBROSlist);
        noonCSI.putOrOverwrite(noonCSIlist);
        noonFFI.putOrOverwrite(noonFFIlist);
        noonBSI.putOrOverwrite(noonBSIlist);
        noonDH.putOrOverwrite(noonDHlist);
        noonDF.putOrOverwrite(noonDFlist);
        noonDB.putOrOverwrite(noonDBlist);
        noonLB.putOrOverwrite(noonLBlist);
        noonAREA.putOrOverwrite(noonAREAlist);
        noonPERIMITER.putOrOverwrite(noonPERIMITERlist);
        
		
		temp.putOrOverwrite(templist);
		rh.putOrOverwrite(rhlist);
		precip.putOrOverwrite(preciplist);
		wsp.putOrOverwrite(wsplist);
		wd.putOrOverwrite(wdlist);
		dew.putOrOverwrite(dewlist);
		fwi.putOrOverwrite(fwilist);
		ffmc.putOrOverwrite(ffmclist);
		isi.putOrOverwrite(isilist);
		dffmc.putOrOverwrite(dffmclist);
		ddmc.putOrOverwrite(ddmclist);
		dbui.putOrOverwrite(dbuilist);
		ddc.putOrOverwrite(ddclist);
		disi.putOrOverwrite(disilist);
		dafwi.putOrOverwrite(dafwilist);
		dadsr.putOrOverwrite(dadsrlist);
		ROSt.putOrOverwrite(ROStlist);
		ROSeq.putOrOverwrite(ROSeqlist);
		HFI.putOrOverwrite(HFIlist);
		CFB.putOrOverwrite(CFBlist);
		SFC.putOrOverwrite(SFClist);
		CFC.putOrOverwrite(CFClist);
		TFC.putOrOverwrite(TFClist);
		RSO.putOrOverwrite(RSOlist);
		FROS.putOrOverwrite(FROSlist);
		BROS.putOrOverwrite(BROSlist);
		CSI.putOrOverwrite(CSIlist);
		FFI.putOrOverwrite(FFIlist);
		BSI.putOrOverwrite(BSIlist);
		DH.putOrOverwrite(DHlist);
		DF.putOrOverwrite(DFlist);
		DB.putOrOverwrite(DBlist);
		LB.putOrOverwrite(LBlist);
		AREA.putOrOverwrite(AREAlist);
		PERIMITER.putOrOverwrite(PERIMITERlist);
		
		hourFBP_FMC.putOrOverwrite(hourFBPFMCList);
		hourFBP_ISI.putOrOverwrite(hourFBPISIList);
		hourFBP_WSV.putOrOverwrite(hourFBPWSVList);
		hourFBP_RAZ.putOrOverwrite(hourFBPRAZList);
		
		sunrise.putOrOverwrite(sunriselist);
		solarnoon.putOrOverwrite(solarnoonlist);
		sunset.putOrOverwrite(sunsetlist);
		
		noonRise.putOrOverwrite(noonRiseList);
		noonNoon.putOrOverwrite(noonNoonList);
		noonSet.putOrOverwrite(noonSetList);
		
		hourRise.putOrOverwrite(hourRiseList);
		hourNoon.putOrOverwrite(hourNoonList);
		hourSet.putOrOverwrite(hourSetList);
		
		model.redraw();
		m_CanTransfer = true;
		notifyCanTransferUpdated(m_CanTransfer);
		st_tview.invalidate();
		tglHourly.setEnabled(true);
		tglDaily.setEnabled(true);
		tglNoon.setEnabled(true);
	}

	protected void calculateSunrise(WTime date) {
		WorldLocation loc = new WorldLocation();
		Double d = app.getLatitude();
		OutVariable<WTime> rise = new OutVariable<>();
		OutVariable<WTime> set = new OutVariable<>();
		OutVariable<WTime> noon = new OutVariable<>();
		
		if (d != null) {
			loc.setLatitude(DEGREE_TO_RADIAN(d.doubleValue()));
			d = app.getLongitude();
			if (d != null) {
				loc.setLongitude(DEGREE_TO_RADIAN(d.doubleValue()));
				
				WorldLocation info = ws.getTimeManager().getWorldLocation();

				if (info != null) {
					loc.setTimezoneOffset(info.getTimezoneOffset());
					loc.setStartDST(new WTimeSpan(0));
					loc.setEndDST(info.getEndDST());
					if (loc.getEndDST().getTotalSeconds() > 0)
						loc.setDSTAmount(info.getDSTAmount());
					Calendar c = Calendar.getInstance();
					c.setTime(app.getDate());

					rise.value = new WTime(date);
					set.value = new WTime(date);
					noon.value = new WTime(date);
					
					loc.getSunRiseSetNoon(date, rise, set, noon);
				}
			}
		}
		
		riseStr = rise.value.toString(WTime.FORMAT_WITHDST | WTime.FORMAT_AS_LOCAL | WTime.FORMAT_TIME);
		noonStr = noon.value.toString(WTime.FORMAT_WITHDST | WTime.FORMAT_AS_LOCAL | WTime.FORMAT_TIME);
		setStr = set.value.toString(WTime.FORMAT_WITHDST | WTime.FORMAT_AS_LOCAL | WTime.FORMAT_TIME);
	}
	
	protected void setFBPCalc(double ffmc, double bui, double dmc, double dc, double ws, double wd, WTime t) {		
		fbpCalculations.ffmc = ffmc;

		fbpCalculations.bui = bui;
		fbpCalculations.dmc = dmc;
		
		fbpCalculations.dc = dc;
		
		fbpCalculations.windSpeed = ws;
		fbpCalculations.windDirection = CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(wd));

		long format = WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST;
		
		fbpCalculations.m_date.set(Calendar.YEAR, (int) t.getYear(format));
		fbpCalculations.m_date.set(Calendar.MONTH, (int) t.getMonth(format));
		fbpCalculations.m_date.set(Calendar.DAY_OF_MONTH, (int) t.getDay(format));
		
		fbpCalculations.m_date.set(Calendar.HOUR_OF_DAY, (int) t.getHour(format));
		fbpCalculations.m_date.set(Calendar.MINUTE, (int) t.getMinute(format));
		fbpCalculations.m_date.set(Calendar.SECOND, (int) t.getSecond(format));
	}

	/**
	 * Update the weather information for the given time.
	 * @param dt The date and time that the weather data was recorded at.
	 * @param temp The temperature (�C).
	 * @param rh The relative humidity (%).
	 * @param precip The precipitation (mm).
	 * @param wndsp The wind speed (km/h).
	 * @param wd The wind direction (degrees).
	 * @throws IllegalArgumentException
	 */
	public void updateHourlyWeather(Calendar dt, double temp, double rh, double precip, double wndsp, double wd) throws IllegalArgumentException {
		if (!canTransferTo())
			return;
		WTime st = new WTime((Long)ws.getAttribute(WEATHER_OPTION.START_TIME), ws.getTimeManager());
		long tm = st.getTime(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
		if (tm == 0) {
			//the timezone may not have been set yet
			setTimezoneFromGlobal();
			//update the time with the possibly new timezone
			st = new WTime((Long)ws.getAttribute(WEATHER_OPTION.START_TIME), ws.getTimeManager());
		}
		OutVariable<IWXData> data = new OutVariable<IWXData>();
		data.value = new IWXData();
		WTime nt = new WTime(dt.get(Calendar.YEAR) + 1900, dt.get(Calendar.MONTH) + 1, dt.get(Calendar.DAY_OF_MONTH),
				dt.get(Calendar.HOUR_OF_DAY), 0, 0, new WTimeManager(new WorldLocation()));
		ws.getInstantaneousValues(nt, 0, data, null, null);
		if (data.value == null)
			throw new IllegalArgumentException("The date/time must exist before it can be updated.");
		data.value.temperature = temp;
		data.value.rh = rh;
		data.value.precipitation = precip;
		data.value.windSpeed = wndsp;
		data.value.windDirection = wd;
		ws.setInstantaneousValues(nt, data.value);
		calculate();
	}

	/**
	 * Set the weather data for an entire day.
	 * @param startTime The day to set the weather for
	 * @param data The weather data for all 24 hours of a day in metric units.
	 * @throws IllegalArgumentException Thrown if weather data was not specified for all 24 hours.
	 */
	public void addDayHourlyWeather(Calendar startTime, IWXData[] data) throws IllegalArgumentException {
		if (!canTransferTo())
			return;
		if (data.length != 24)
			throw new IllegalArgumentException("The data array must contain information for all hours of a day");
		WTime time = WTime.fromLocal(startTime.get(Calendar.YEAR), startTime.get(Calendar.MONTH) + 1, startTime.get(Calendar.DAY_OF_MONTH),
				0, 0, 0, ws.getTimeManager());
		WTime st = new WTime((Long)ws.getAttribute(WEATHER_OPTION.START_TIME), ws.getTimeManager());
		long tm = st.getTime(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
		if (tm == 0) {
			//the timezone may not have been set yet
			setTimezoneFromGlobal();
			//update the time with the possibly new timezone
			time = WTime.fromLocal(startTime.get(Calendar.YEAR), startTime.get(Calendar.MONTH) + 1, startTime.get(Calendar.DAY_OF_MONTH),
					0, 0, 0, ws.getTimeManager());
			//set the start time of the weather stream
			ws.setAttribute(WEATHER_OPTION.START_TIME, Long.valueOf(time.getTime(0)));
		}
		if (!ws.makeHourlyObservations(time)) {
			JOptionPane.showMessageDialog(app.frmRedapp, Main.resourceManager.getString("ui.label.stats.error.noadd"));
		}
		else {
			for (int i = 0; i < 24; i++) {
				WTimeSpan hour = new WTimeSpan(0, i, 0, 0);
				WTime hourToEdit = WTime.add(time, hour);
				ws.setInstantaneousValues(hourToEdit, data[i]);
			}
			calculate();
			if (tm == 0) {
				if (app.getCurrentTab() == this)
					model.importComplete();
				else
					checkboxUpdateNeeded = true;
			}
		}
	}

	/**
	 * Set the weather data for an entire day.
	 * @param startTime The day to set the weather for
	 * @param data The weather data for all 24 hours of a day in metric units.
	 * @throws IllegalArgumentException Thrown if weather data was not specified for all 24 hours.
	 */
	public void addDayWeather(Calendar dt, double min_temp, double max_temp, double min_ws, double max_ws, double rh, double precip, double wd) {
		WTime time = WTime.fromLocal(dt.get(Calendar.YEAR), dt.get(Calendar.MONTH) + 1, dt.get(Calendar.DAY_OF_MONTH),
				0, 0, 0, ws.getTimeManager());
		WTime st = new WTime((Long)ws.getAttribute(WEATHER_OPTION.START_TIME), ws.getTimeManager());
		if (st.getTime(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST) == 0) {//Redmine 809 && !timezoneOverride) {
			ws.setAttribute(WEATHER_OPTION.START_TIME, Long.valueOf(time.getTime(0)));
		}
		ws.makeDailyObservations(time);
		ws.setDailyValues(time, min_temp, max_temp, min_ws, max_ws, rh, precip, wd);
		calculate();
	}

	/**
	 *
	 * @param fuelIndex The selected index in the fuel type combobox.
	 * @param cbh Crown base height (m).
	 * @param pc Percent conifer.
	 * @param pdf Percent dead fir.
	 * @param gc Grass curing.
	 * @param gfl Grass fuel load (km/m^2).
	 */
	public void setFBPFuelValues(Integer fuelIndex, Double cbh, Double pc, Double pdf, Double gc, Double gfl) {
		if (fuelIndex != null) {
			comboFbpFuelType.setSelectedIndex(fuelIndex);
		}
		if (cbh != null) {
			txtFbpCrownBaseHeight.setText(DecimalUtils.format(Convert.convertUnit(cbh, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC))));
		}
		if (pc != null) {
			txtFbpPercentConifer.setText(DecimalUtils.format(pc));
		}
		if (pdf != null) {
			txtFbpPercentDeadFir.setText(DecimalUtils.format(pdf));
		}
		if (gc != null) {
			txtFbpGrassCuring.setText(DecimalUtils.format(gc));
		}
		if (gfl != null) {
			txtFbpGrassFuelLoad.setText(DecimalUtils.format(Convert.convertUnit(gfl, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC))));
		}
	}

	/**
	 *
	 * @param elev Elevation (m).
	 * @param useSlope Use the slope.
	 * @param slp Slope.
	 * @param asp Aspect.
	 */
	public void setFBPTerrainValues(Double elev, Boolean useSlope, Double slp, Double asp) {
		if (elev != null) {
			txtFbpElevation.setText(DecimalUtils.format(Convert.convertUnit(elev, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC))));
		}
		if (useSlope != null) {
			chckFbpSlope.setSelected(useSlope);
		}
		if (slp != null) {
			txtFbpSlope.setText(DecimalUtils.format(slp));
		}
		if (asp != null) {
			txtFbpAspect.setText(DecimalUtils.format(asp));
		}
	}
	
	/**
	 * Import a weather stream file.
	 * @param filename
	 * @param type
	 */
	public void importFile(String filename, Import.FileType type) {
	    highlightCorrectedRows = false;
	    
		if (type == Import.FileType.WEATHER_STREAM) {
			long err;
			try {
				err = ws.importFile(filename, (int)(WEATHERSTREAM_IMPORT.PURGE | WEATHERSTREAM_IMPORT.INVALID_FIX));
			}
			catch (IOException ex) {
				JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.error.import2"), "Error", JOptionPane.ERROR_MESSAGE);
				err = ca.hss.general.ERROR.INVALID_DATA;
			}
			
			CreateTempWSDialog dlg = null;
			if(ws.getWeatherMode() == 1) {
				if (((err & ca.hss.general.ERROR.SEVERITY_WARNING) == 0) && (err != -1)) {
					dlg = new CreateTempWSDialog(app.frmRedapp);
					setDialogPosition(dlg);
					dlg.setVisible(true);	
					err = dlg.getResult();
				}
			}
			
			if (((err & ca.hss.general.ERROR.SEVERITY_WARNING) == 0) && (err != -1)) {
				if (dlg != null) {
					txtTempAlphaChanged(dlg.getTempAlpha());
					txtTempBetaChanged(dlg.getTempBeta());
					txtTempGammaChanged(dlg.getTempGamma());
					txtWSAlphaChanged(dlg.getWSAlpha());
					txtWSBetaChanged(dlg.getWSBeta());
					txtWSGammaChanged(dlg.getWSGamma());
				}
				
				tglHourly.setEnabled(true);
				tglDaily.setEnabled(true);
				if (tglHourly.isSelected())
					boxDisplayHourlyChanged();
				else if (tglDaily.isSelected())
					boxDisplayDailyChanged();
				else
					boxDisplayNoonChanged();
				if (err == ca.hss.general.ERROR.INTERPOLATE || err == ca.hss.general.ERROR.INTERPOLATE_BEFORE_INVALID_DATA) {
					JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.warning.interpolation"), "Warning", JOptionPane.WARNING_MESSAGE);
                    //there was imported data out of valid ranges that was corrected
                    if (ws.hasAnyCorrected()) {
                        if (JOptionPane.showConfirmDialog(null, Main.resourceManager.getString("ui.label.stats.warning.corrected"), "Warning",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                            highlightCorrectedRows = true;
                        }
                    }
				}
				if (err == ca.hss.general.ERROR.INVALID_DATA) {
				    //there was imported data out of valid ranges that was corrected
				    if (ws.hasAnyCorrected()) {
				        if (JOptionPane.showConfirmDialog(null, Main.resourceManager.getString("ui.label.stats.warning.corrected"), "Warning",
				                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
				            highlightCorrectedRows = true;
				        }
				    }
				    else if (err == ca.hss.general.ERROR.INTERPOLATE_BEFORE_INVALID_DATA) {
				        JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.warning.partial"), "Warning", JOptionPane.WARNING_MESSAGE);
				    }
				}
				if (!model.columnExists(StatsDataType.IMPORT_TEMPERATURE)) {
					model.new Column(StatsDataType.IMPORT_TEMPERATURE);
				}
				if (!model.columnExists(StatsDataType.IMPORT_DEWPOINT)) {
					model.new Column(StatsDataType.IMPORT_DEWPOINT);
				}
				if (!model.columnExists(StatsDataType.IMPORT_RH)) {
					model.new Column(StatsDataType.IMPORT_RH);
				}
				if (!model.columnExists(StatsDataType.IMPORT_WS)) {
					model.new Column(StatsDataType.IMPORT_WS);
				}
				if (!model.columnExists(StatsDataType.IMPORT_WD)) {
					model.new Column(StatsDataType.IMPORT_WD);
				}
				if (!model.columnExists(StatsDataType.IMPORT_PRECIP)) {
					model.new Column(StatsDataType.IMPORT_PRECIP);
				}
				if (!model.columnExists(StatsDataType.FFMC)) {
					model.new Column(StatsDataType.FFMC);
				}
				if (!model.columnExists(StatsDataType.DMC)) {
					model.new Column(StatsDataType.DMC);
				}
				if (!model.columnExists(StatsDataType.DC)) {
					model.new Column(StatsDataType.DC);
				}
				if (!model.columnExists(StatsDataType.ISI)) {
					model.new Column(StatsDataType.ISI);
				}
				if (!model.columnExists(StatsDataType.BUI)) {
					model.new Column(StatsDataType.BUI);
				}
				if (!model.columnExists(StatsDataType.FWI)) {
					model.new Column(StatsDataType.FWI);
				}
				if (!model.columnExists(StatsDataType.HFFMC)) {
					model.new Column(StatsDataType.HFFMC);
				}
				if (!model.columnExists(StatsDataType.HISI)) {
					model.new Column(StatsDataType.HISI);
				}
				if (!model.columnExists(StatsDataType.HFWI)) {
					model.new Column(StatsDataType.HFWI);
				}
				if (!model.columnExists(StatsDataType.H_SUNRISE)) {
					model.new Column(StatsDataType.H_SUNRISE);
				}
				if (!model.columnExists(StatsDataType.H_SOLAR_NOON)) {
					model.new Column(StatsDataType.H_SOLAR_NOON);
				}
				if (!model.columnExists(StatsDataType.H_SUNSET)) {
					model.new Column(StatsDataType.H_SUNSET);
				}
				if (!model.columnExists(StatsDataType.NOON_TEMPERATURE)) {
					model.new Column(StatsDataType.NOON_TEMPERATURE, true, DisplayType.NOON);
				}
				if (!model.columnExists(StatsDataType.NOON_DEWPOINT)) {
					model.new Column(StatsDataType.NOON_DEWPOINT, true, DisplayType.NOON);
				}
				if (!model.columnExists(StatsDataType.NOON_RH)) {
					model.new Column(StatsDataType.NOON_RH, true, DisplayType.NOON);
				}
				if (!model.columnExists(StatsDataType.NOON_WS)) {
					model.new Column(StatsDataType.NOON_WS, true, DisplayType.NOON);
				}
				if (!model.columnExists(StatsDataType.NOON_WD)) {
					model.new Column(StatsDataType.NOON_WD, true, DisplayType.NOON);
				}
				if (!model.columnExists(StatsDataType.NOON_PRECIP)) {
					model.new Column(StatsDataType.NOON_PRECIP, true, DisplayType.NOON);
				}

				cmbHourlyStartSkipAction = true;
				calculate();
				cmbHourlyStartSkipAction = false;
				
				if (app.getCurrentTab() == this) {
					firstImport = false;
					model.importComplete();
				}else
					checkboxUpdateNeeded = true;
			}
			else {
				JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.error.import2"), "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
		else if (type == FileType.NOON_WEATHER) {
			long err;
			try {
				err = nwc.importFile(filename, (int)WEATHERSTREAM_IMPORT.PURGE);
			}
			catch (IOException ex) {
				JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.error.import2"), "Error", JOptionPane.ERROR_MESSAGE);
				err = ca.hss.general.ERROR.INVALID_DATA;
			}
			if (err == ca.hss.general.ERROR.S_OK) {
				tglNoon.setSelected(true);
				boxDisplayNoonChanged();
				if (!model.columnExists(StatsDataType.NOON_TEMPERATURE)) {
					model.new Column(StatsDataType.NOON_TEMPERATURE, true, DisplayType.NOON);
				}
				if (!model.columnExists(StatsDataType.NOON_DEWPOINT)) {
					model.new Column(StatsDataType.NOON_DEWPOINT, true, DisplayType.NOON);
				}
				if (!model.columnExists(StatsDataType.NOON_RH)) {
					model.new Column(StatsDataType.NOON_RH, true, DisplayType.NOON);
				}
				if (!model.columnExists(StatsDataType.NOON_WS)) {
					model.new Column(StatsDataType.NOON_WS, true, DisplayType.NOON);
				}
				if (!model.columnExists(StatsDataType.NOON_WD)) {
					model.new Column(StatsDataType.NOON_WD, true, DisplayType.NOON);
				}
				if (!model.columnExists(StatsDataType.NOON_PRECIP)) {
					model.new Column(StatsDataType.NOON_PRECIP, true, DisplayType.NOON);
				}
				
				calculate();
				if (app.getCurrentTab() == this)
					model.importComplete();
				else
					checkboxUpdateNeeded = true;
			}
			else {
				JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.error.import2"), "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	public void inputNoon() {
		WTime end = nwc.getEndTime();
		end.add(WTimeSpan.Day);
		final CreateNoonDialog dlg = new CreateNoonDialog(app.frmRedapp, end);
		dlg.setCreateNoonDialogListener(new CreateNoonDialogListener() {
			@Override
			public void cancelled(CreateNoonDialog dlg) { }
			
			@Override
			public void accepted(CreateNoonDialog dlg) {
				WTime t = dlg.getTime();
				OutVariable<Double> val = new OutVariable<>();
				if (!dlg.getTemperature(val))
					return;
				double temp = val.value;
				if (!dlg.getRH(val))
					return;
				double rh = val.value;
				if (!dlg.getPrecipitation(val))
					return;
				double precip = val.value;
				if (!dlg.getWindSpeed(val))
					return;
				double ws = val.value;
				if (!dlg.getWindDirection(val))
					return;
				double wd = val.value;
				nwc.setNoonWeatherValues(t, temp, rh, ws, wd, precip);
				dlg.dispose();
				calculate();
			}
		});
		dlg.setVisible(true);
	}

	/**
	 * Input daily weather information manually.
	 */
	@SuppressWarnings("deprecation")
	public void inputDaily() {
		OutVariable<WTime> start = new OutVariable<>();
		start.value = new WTime(ws.getTimeManager());
		OutVariable<WTimeSpan> duration = new OutVariable<>();
		duration.value = new WTimeSpan();
		ws.getValidTimeRange(start, duration);
		Date newDate;
		boolean fixDate;
		if (start.value.getTotalMicroSeconds() > 0) {
			WTime end = new WTime(start.value);
			end = end.add(duration.value);
			long format = WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST;
			end.purgeToDay(format);
			newDate = new Date((int)end.getYear(format) - 1900, (int)end.getMonth(format) - 1, (int)end.getDay(format), 0, 0, 0);
			fixDate = true;
		}
		else {
			newDate = app.getDate();
			fixDate = false;
		}
		final CreateDailyDialog dlg = new CreateDailyDialog(app.frmRedapp, newDate, fixDate);
		dlg.setCreateDailyDialogListener(new CreateDailyDialogListener() {
			@Override
			public void cancelled() { }

			@Override
			public void accepted() {
				Calendar c = Calendar.getInstance();
				Date dt = dlg.getDate();
				c.set(Calendar.YEAR, dt.getYear() + 1900);
				c.set(Calendar.MONTH, dt.getMonth());
				c.set(Calendar.DAY_OF_MONTH, dt.getDate());
				double rh = dlg.getRelativeHumidity() / 100.0;
				double wd = DEGREE_TO_RADIAN(COMPASS_TO_CARTESIAN_DEGREE(dlg.getWindDirection()));
				WTime st = new WTime((Long)ws.getAttribute(WEATHER_OPTION.START_TIME), ws.getTimeManager());
				long time = st.getTime(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
				
				txtTempAlphaChanged(dlg.getTempAlpha());
				txtTempBetaChanged(dlg.getTempBeta());
				txtTempGammaChanged(dlg.getTempGamma());
				txtWSAlphaChanged(dlg.getWSAlpha());
				txtWSBetaChanged(dlg.getWSBeta());
				txtWSGammaChanged(dlg.getWSGamma());
				
				addDayWeather(c, dlg.getMinTemp(), dlg.getMaxTemp(), dlg.getMinWindSpeed(), dlg.getMaxWindSpeed(), rh, dlg.getPrecipitation(), wd);
				if (time == 0) {
					if (app.getCurrentTab() == StatsTab.this)
						model.importComplete();
					else
						checkboxUpdateNeeded = true;
				}
			}
		});
		dlg.setVisible(true);
	}

	/**
	 * Input hourly weather information manually.
	 */
	@SuppressWarnings("deprecation")
	public void inputHourly() {
		if (filetype != FileType.WEATHER_STREAM)
			return;
		OutVariable<WTime> start = new OutVariable<>();
		start.value = new WTime(ws.getTimeManager());
		OutVariable<WTimeSpan> duration = new OutVariable<>();
		duration.value = new WTimeSpan();
		ws.getValidTimeRange(start, duration);
		Date newDate;
		boolean fixDate;
		IWXData[] data = new IWXData[24];
		for (int i = 0; i < 24; i++)
			data[i] = new IWXData();
		if (start.value.getTotalMicroSeconds() > 0) {
			WTime end = new WTime(start.value);
			end = end.add(duration.value);
			long format = WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST;
			end.purgeToDay(format);
			newDate = new Date((int)end.getYear(format) - 1900, (int)end.getMonth(format) - 1, (int)end.getDay(format), 0, 0, 0);
			fixDate = true;
			OutVariable<IWXData> odata = new OutVariable<>();
			odata.value = new IWXData();
			for (int i = 0; i < 24; i++) {
				ws.getInstantaneousValues(WTime.add(end, new WTimeSpan(0, i, 0, 0)), 0, odata, null, null);
				data[i] = new IWXData(odata.value);
			}
		}
		else {
			newDate = app.getDate();
			fixDate = false;
		}
		final CreateHourlyDialog dlg = new CreateHourlyDialog(app.frmRedapp, newDate, fixDate, data);
		dlg.setCreateHourlyDialogListener(new CreateHourlyDialogListener() {
			@Override
			public void cancelled() { }

			@Override
			public void accepted() {
				Calendar c = Calendar.getInstance();
				Date dt = dlg.getDate();
				c.set(Calendar.YEAR, dt.getYear() + 1900);
				c.set(Calendar.MONTH, dt.getMonth());
				c.set(Calendar.DAY_OF_MONTH, dt.getDate());
				IWXData[] data = dlg.getData();
				for (int i = 0; i < 24; i++) {
					data[i].rh = data[i].rh / 100.0;
					data[i].windDirection = DEGREE_TO_RADIAN(COMPASS_TO_CARTESIAN_DEGREE(data[i].windDirection));
				}
				addDayHourlyWeather(c, data);
			}
		});
		dlg.setVisible(true);
	}

	/**
	 * Import a file.
	 */
	public void importFile() {
		wsGetValuesFromForm();
		String dir = prefs.get("START_DIR", System.getProperty("user.home"));
		String str = null;
		RFileChooser chooser = RFileChooser.filePicker();
		chooser.setCurrentDirectory(dir);
		chooser.setTitle(Main.resourceManager.getString("ui.label.stats.import.title"));
		int retval = chooser.showDialog(this);
		
		if (retval == JFileChooser.APPROVE_OPTION) {
			str = chooser.getSelectedFile().getAbsolutePath();
			prefs.put("START_DIR", chooser.getParentDirectory());
			
			Import imdlg = null;
			try {
				imdlg = new Import(app, str);
			}
			catch (FileNotFoundException ex) {
				REDappLogger.error("Error importing file", ex);
			}
			if (imdlg != null) {
				double dmc, dc, ffmc, hffmc;
				dmc = LineEditHelper.getDoubleFromLineEdit(txtDailyDMC);
				dc = LineEditHelper.getDoubleFromLineEdit(txtDailyDC);
				ffmc = LineEditHelper.getDoubleFromLineEdit(txtDailyFFMC);
				hffmc = LineEditHelper.getDoubleFromLineEdit(txtHourlyFFMC);
				imdlg.setStartupCodeDefaults(dc, dmc, ffmc, hffmc,  
											 comboHourlyMethod.getSelectedIndex(), cmb_hourlyStart.getSelectedIndex(), chkDailyFit.isSelected());
				setDialogPosition(imdlg);
				imdlg.setVisible(true);
				if (imdlg.getResult() == JFileChooser.APPROVE_OPTION) {
					if (firstImport)
						setTimezoneFromGlobal();
					txtDailyDC.setText(DecimalUtils.format(imdlg.getDC(), DecimalUtils.DataType.DC));
					txtDailyDMC.setText(DecimalUtils.format(imdlg.getDMC(), DecimalUtils.DataType.DMC));
					txtDailyFFMC.setText(DecimalUtils.format(imdlg.getFFMC(), DecimalUtils.DataType.FFMC));
					txtHourlyFFMC.setText(DecimalUtils.format(imdlg.getHFFMC(), DecimalUtils.DataType.FFMC));
					comboHourlyMethod.setSelectedIndex(imdlg.getCalculationMethod());
					
					if(comboHourlyMethod.getSelectedIndex() == 1) {
						importLock = true;
						chkDailyFit.setSelected(imdlg.getDailyFit());
						if (!chkDailyFit.isSelected())
							cmb_hourlyStart.setSelectedIndex(imdlg.getHourlyStart());
						importLock = false;
					}

					if (imdlg.getFileType() == FileType.UNKNOWN_FILE) {
						CustomImportDlg cidlg = new CustomImportDlg(app.frmRedapp, new File(str), imdlg.delimiter(), imdlg.ignoreEmpty());
						setDialogPosition(cidlg);
						cidlg.setVisible(true);
						if (cidlg.getResult() == JFileChooser.APPROVE_OPTION) {
							filetype = FileType.WEATHER_STREAM;
							importFile(cidlg.importFile(), FileType.WEATHER_STREAM);
						}
					}
					else {
						filetype = imdlg.getFileType();
						importFile(str, imdlg.getFileType());
					}
				}
			}
		}
	}

	// {{ Export

	public void exportHourlyWeather() {
		OutVariable<String> filename = new OutVariable<String>();
		DataExporter exporter = getExportFilename(filename);
		if (exporter != null) {
			try {
				exportHourlyWeather(exporter);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.error.export"), "Error", JOptionPane.ERROR_MESSAGE);
			}
			finally {
				if (exporter != null) {
					try {
						exporter.close();
					}
					catch (IOException ex) { }
				}
			}
		}
	}

	public void exportDailyWeather() {
		OutVariable<String> filename = new OutVariable<String>();
		DataExporter exporter = getExportFilename(filename);
		if (exporter != null) {
			try {
				exportDailyWeather(exporter);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.error.export"), "Error", JOptionPane.ERROR_MESSAGE);
			}
			finally {
				if (exporter != null) {
					try {
						exporter.close();
					}
					catch (IOException ex) { }
				}
			}
		}
	}

	public void exportNoonStandardWeather() {
		OutVariable<String> filename = new OutVariable<String>();
		DataExporter exporter = getExportFilename(filename);
		if (exporter != null) {
			try {
				if (filetype == FileType.WEATHER_STREAM)
					exportNoonStandardWeather(exporter);
				else if (filetype == FileType.NOON_WEATHER)
					exportNoonWeather(exporter);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.error.export"), "Error", JOptionPane.ERROR_MESSAGE);
			}
			finally {
				if (exporter != null) {
					try {
						exporter.close();
					}
					catch (IOException ex) { }
				}
			}
		}
	}

	/**
	 * Export the data in the table to a file.
	 */
	public void export() {
		if (filetype == FileType.WEATHER_STREAM) {
			OutVariable<String> filename = new OutVariable<String>();
			DataExporter exporter = getExportFilename(filename);
			if (exporter != null) {
				try {
					exportHourly(exporter);
				}
				catch (IOException ex) {
					JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.error.export"), "Error", JOptionPane.ERROR_MESSAGE);
				}
				finally {
					if (exporter != null) {
						try {
							exporter.close();
						}
						catch (IOException ex) { }
					}
				}
			}
		}
		else if (filetype == FileType.NOON_WEATHER) {
			exportNoonStandardWeather();
		}
	}

	protected DataExporter getExportFilename(OutVariable<String> filename) {
		RFileChooser chooser = RFileChooser.fileSaver();
		String dir = prefs.get("START_DIR_EXPORT", System.getProperty("user.home"));
		chooser.setCurrentDirectory(dir);
		String[] extensionFilters;
		String[] extensionFiltersNames;
		String str = null;
		DataExporter exporter = null;
		extensionFilters = new String[] {
				"*.csv",
				 "*.xls",
				"*.xlsx",
				"*.xml" };
		extensionFiltersNames = new String[] {
				Main.resourceManager.getString("ui.label.file.csv") + " (*.csv)",
				Main.resourceManager.getString("ui.label.file.xls") + " (*.xls)",
				Main.resourceManager.getString("ui.label.file.xlsx") + " (*.xlsx)",
				Main.resourceManager.getString("ui.label.file.xml") + " (*.xml)" };
		chooser.setExtensionFilters(extensionFilters, extensionFiltersNames, 0);
		chooser.setTitle("Export File");
		int retval = chooser.showDialog(this);
		if (retval == JFileChooser.APPROVE_OPTION) {
			str = chooser.getSelectedFile().getAbsolutePath();
			prefs.put("START_DIR_EXPORT", chooser.getParentDirectory());
			String[] exts = chooser.getSelectedExtension();
			String ext = null;
			for (int i = 0; i < exts.length; i++) {
				if (str.endsWith(exts[i])) {
					ext = exts[i];
				}
			}
			if (ext == null) {
				ext = exts[0];
				str = str + "." + ext;
			}
			File fl = new File(str);
			try {
				if (ext.equalsIgnoreCase("csv"))
					exporter = new ca.cwfgm.export.CSVExporter(fl);
				else if (ext.equalsIgnoreCase("xml"))
					exporter = new ca.cwfgm.export.XMLExporter(fl);
				else if (ext.equalsIgnoreCase("xls") || ext.equalsIgnoreCase("xlsx"))
					exporter = new ca.cwfgm.export.XLSExporter(fl);
				else {
					exporter = new ca.cwfgm.export.CSVExporter(fl);
					((ca.cwfgm.export.CSVExporter)exporter).setDelimiter("\t");
				}
			}
			catch (ParserConfigurationException e) {
				JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.error.export"), "Error", JOptionPane.ERROR_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.stats.error.export"), "Error", JOptionPane.ERROR_MESSAGE);
			}
			return exporter;
		}
		return null;
	}

	/**
	 * Remove characters not supported by the given file type and fix the dew points name.
	 * @param text
	 * @param exporter
	 * @return
	 */
	private String sanatizeForExport(String text, DataExporter exporter) {
		if (text.compareToIgnoreCase(Main.resourceManager.getString("ui.label.weather.dew")) == 0)
			text = "DewPoint";
		else if (text.compareToIgnoreCase("<html><body>" + Main.resourceManager.getString("ui.label.fire.roseq")) == 0)
			text = "ROS_eq";
		else if (text.compareToIgnoreCase("<html><body>" + Main.resourceManager.getString("ui.label.fire.rost")) == 0)
			text = "ROS_t";
		else if (text.compareToIgnoreCase("<html><body>" + Main.resourceManager.getString("ui.label.fire.isifbp")) == 0)
			text = "ISI_FBP";
		else if (text.compareToIgnoreCase(Main.resourceManager.getString("ui.label.fwi.sunrise.noon")) == 0)
			text = "Solar_Noon";
		else {
			String[] inv = exporter.invalidChars();
			for (String in : inv) {
				text = text.replace(in, "_");
			}
		}
		return text;
	}

	/**
	 * Export the hourly weather information.
	 * @param exporter
	 * @throws IOException
	 */
	private void exportHourly(DataExporter exporter) throws IOException {
		DisplayType type = model.getDisplayType();
		model.setDisplayType(DisplayType.HOURLY);
		RowBuilder builder = new RowBuilder();
		
		if (exporter instanceof ca.cwfgm.export.XMLExporter) {
			builder.addData("Parameter");
			builder.addData("Value");
			exporter.setElementName("metadata");
			exporter.writeHeader(builder.toHeader());

			builder.addData("Latitude");
			builder.addData(app.getLatitude());
			
			exporter.writeRow(builder.toRow());

			builder.addData("Longitude");
			builder.addData(app.getLongitude());
			
			exporter.writeRow(builder.toRow());
		}
		
		int selFuel = comboFbpFuelType.getSelectedIndex();
		
		List<String> headers = model.getVisibleHeaders();
		builder.addData("Date");
		builder.addData("Hour");
		for (int i = 0; i < headers.size(); i++) {
			String data = headers.get(i);
			data = sanatizeForExport(data, exporter);
			builder.addData(data);
		}
		
		builder.addData("Fueltype");
		builder.addData("CBH");
		builder.addData("PC");
		builder.addData("PDF");
		builder.addData("Curing");
		builder.addData("GFL");
		
		exporter.setElementName("hour");
		exporter.writeHeader(builder.toHeader());
		
		List<WTime> times = model.getDisplayedTimes();
		long hour = 0;
		long minute = 0;
		String timeT = null;
		
		for (WTime time : times) {
			hour = time.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
			minute = time.getMinute(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
			timeT = hour + (minute == 0 ? "" : ":" + String.format("%02d", minute));
			
			builder.addData(time.toString(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST | WTime.FORMAT_STRING_YYYY_MM_DD | WTime.FORMAT_DATE));
			builder.addData(timeT);
			List<Object> data = model.getVisibleColumns(time);
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i) instanceof TwoString) {
					data.set(i, ((TwoString)data.get(i)).getSecondary());
				}
			}
			builder.addAllData(model.getVisibleColumns(time).toArray());
			
			String fuelType = comboFbpFuelType.getSelectedItem().toString();
			builder.addData(fuelType.substring(0, fuelType.indexOf(":")));
			
			builder.addData(selFuel == 5 ? fbpCalculations.crownBase : "");
			builder.addData((selFuel == 9 || selFuel == 10) ? fbpCalculations.conifMixedWood : "");
			builder.addData((selFuel == 11 || selFuel == 12) ? fbpCalculations.deadBalsam : "");
			builder.addData((selFuel == 13 || selFuel == 14) ? fbpCalculations.grassCuring : "");
			builder.addData((selFuel == 13 || selFuel == 14) ? fbpCalculations.grassFuelLoad : "");
			
			exporter.setElementName("hour");
			exporter.writeRow(builder.toRow());
		}
		model.setDisplayType(type);
	}

	private void exportHourlyWeather(DataExporter exporter) throws IOException {
		RowBuilder builder = new RowBuilder();
		builder.addData("Date");
		builder.addData("Hour");
		builder.addData("Temp");
		builder.addData("DewPoint");
		builder.addData("RH");
		builder.addData("WS");
		builder.addData("WD");
		builder.addData("Precip");
		builder.addData("FFMC");
		builder.addData("DMC");
		builder.addData("DC");
		builder.addData("ISI");
		builder.addData("BUI");
		builder.addData("FWI");
		builder.addData("HFFMC");
		builder.addData("HISI");
		builder.addData("HFWI");
		exporter.writeHeader(builder.toHeader());
		WTime time = model.getStartTime();
		WTime endTime = model.getEndTime();
		WTime timeWS = new WTime(time.getTime(0), ws.getTimeManager());
		OutVariable<IWXData> wx = new OutVariable<IWXData>();
		OutVariable<IFWIData> ifwi = new OutVariable<IFWIData>();
		OutVariable<DFWIData> dfwi = new OutVariable<DFWIData>();
		OutVariable<IWXData> wxnoon = new OutVariable<IWXData>();
		wx.value = new IWXData();
		ifwi.value = new IFWIData();
		dfwi.value = new DFWIData();
		wxnoon.value = new IWXData();
		while (WTime.lessThanEqualTo(time, endTime)) {
			double temp_isi = 0;
			double temp_fwi = 0;
			WTime noon = new WTime(timeWS);
			if (timeWS.getTimeManager().getWorldLocation().getEndDST().getTotalSeconds() > 0) {
				noon.subtract(new WTimeSpan(0, 13, 0, 0));
				noon.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
				noon.add(new WTimeSpan(0, 13, 0, 0));
			}
			else {
				noon.subtract(new WTimeSpan(0, 12, 0, 0));
				noon.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
				noon.add(new WTimeSpan(0, 12, 0, 0));
			}
			ws.getInstantaneousValues(timeWS, 0, wx, ifwi, dfwi);
			try {
				ws.getInstantaneousValues(noon, 0, wxnoon, null, null);
				if (wxnoon.value == null) {
					wxnoon.value = new IWXData();
					noon = new WTime(time);
					noon.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
					ws.getInstantaneousValues(noon, 0, wxnoon, null, null);
				}
				temp_isi = Fwi.isiFWI(dfwi.value.dFFMC, wxnoon.value.windSpeed, 60 * 60);
				temp_fwi = Fwi.fwi(temp_isi, dfwi.value.dBUI);
			}
			catch (Exception ex) {
				REDappLogger.error("Error getting instantaneous values", ex);
			}
			
			builder.addData(time.toString(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST | WTime.FORMAT_STRING_YYYY_MM_DD | WTime.FORMAT_DATE));
			
			builder.addData(time.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST));
			builder.addData(DecimalUtils.format(wx.value.temperature, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(wx.value.dewPointTemperature, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(wx.value.rh * 100.0, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(wx.value.windSpeed, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(wx.value.windDirection)), DataType.WIND_DIR));
			builder.addData(DecimalUtils.format(wx.value.precipitation, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(dfwi.value.dFFMC, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(dfwi.value.dDMC, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(dfwi.value.dDC, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(temp_isi, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(dfwi.value.dBUI, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(temp_fwi, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(ifwi.value.dFFMC, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(ifwi.value.dISI, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(ifwi.value.dFWI, DataType.FORCE_2));
			exporter.writeRow(builder.toRow());
			time.add(WTimeSpan.Hour);
			timeWS.add(WTimeSpan.Hour);
		}
	}

	private void exportDailyWeather(DataExporter exporter) throws IOException {
		RowBuilder builder = new RowBuilder();
		builder.addData("Date");
		builder.addData("Min_Temp");
		builder.addData("Max_Temp");
		builder.addData("Min_RH");
		builder.addData("Min_WS");
		builder.addData("Max_WS");
		builder.addData("WD");
		builder.addData("Precip");
		builder.addData("FFMC");
		builder.addData("DMC");
		builder.addData("DC");
		builder.addData("ISI");
		builder.addData("BUI");
		builder.addData("FWI");
		exporter.writeHeader(builder.toHeader());
		WTime time = model.getStartTime();
		WTime endTime = model.getEndTime();
		WTime timeWS = new WTime(time.getTime(0), ws.getTimeManager());
		OutVariable<Double> min_temp = new OutVariable<Double>();
		OutVariable<Double> max_temp = new OutVariable<Double>();
		OutVariable<Double> min_ws = new OutVariable<Double>();
		OutVariable<Double> max_ws = new OutVariable<Double>();
		OutVariable<Double> rh = new OutVariable<Double>();
		OutVariable<Double> precip = new OutVariable<Double>();
		OutVariable<Double> wd = new OutVariable<Double>();
		OutVariable<IWXData> wx = new OutVariable<IWXData>();
		OutVariable<IFWIData> ifwi = new OutVariable<IFWIData>();
		OutVariable<DFWIData> dfwi = new OutVariable<DFWIData>();
		OutVariable<IWXData> wxnoon = new OutVariable<IWXData>();
		wx.value = new IWXData();
		ifwi.value = new IFWIData();
		dfwi.value = new DFWIData();
		wxnoon.value = new IWXData();
		while (WTime.lessThanEqualTo(time, endTime)) {
			double temp_isi = 0;
			double temp_fwi = 0;
			WTime temp = new WTime(timeWS);
			ws.getDailyValues(timeWS, min_temp, max_temp, min_ws, max_ws, rh, precip, wd);
			WTime noon = new WTime(timeWS);
			WTime rainEndTime = new WTime(timeWS);
			WTime rainStartTime;
			
			if (timeWS.getTimeManager().getWorldLocation().getEndDST().getTotalSeconds() > 0) {
				noon.subtract(new WTimeSpan(0, 13, 0, 0));
				noon.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
				noon.add(new WTimeSpan(0, 13, 0, 0));
				temp.add(new WTimeSpan(0, 13, 0, 0));
				//rainEndTime.Add(new WTimeSpan(0, 14, 0, 0));
			}
			else {
				noon.subtract(new WTimeSpan(0, 12, 0, 0));
				noon.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
				noon.add(new WTimeSpan(0, 12, 0, 0));
				temp.add(new WTimeSpan(0, 12, 0, 0));
				//rainEndTime.Add(new WTimeSpan(0, 13, 0, 0));
			}
			
			rainStartTime = new WTime(noon);
			rainStartTime.add(new WTimeSpan(0, 1, 0, 0));
			ws.getInstantaneousValues(temp, 0, wx, ifwi, dfwi);
			try {
				ws.getInstantaneousValues(noon, 0, wxnoon, null, null);
				if (wxnoon.value == null) {
					wxnoon.value = new IWXData();
					noon = new WTime(timeWS);
					noon.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
					ws.getInstantaneousValues(noon, 0, wxnoon, null, null);
					rainStartTime = new WTime(noon);
				}
				temp_isi = Fwi.isiFBP(dfwi.value.dFFMC, wxnoon.value.windSpeed, 60 * 60);
				temp_fwi = Fwi.fwi(temp_isi, dfwi.value.dBUI);
			}
			catch (Exception ex) {
				REDappLogger.error("Error getting instantaneous values", ex);
			}
			double rain = 0;
			
			rainStartTime.subtract(new WTimeSpan(0, 24, 0, 0));
			
			temp = new WTime(rainStartTime);
			
			rainEndTime = new WTime(rainStartTime);
			rainEndTime.add(new WTimeSpan(0, 24, 0, 0));
			
			while (WTime.lessThan(temp, rainEndTime)) {
				OutVariable<IWXData> data = new OutVariable<IWXData>();
				data.value = new IWXData();
				ws.getInstantaneousValues(temp, 0, data, null, null);
				if (data.value != null)
					rain += data.value.precipitation;
				temp.add(WTimeSpan.Hour);
			}
			builder.addData(time.toString(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST | WTime.FORMAT_STRING_YYYY_MM_DD | WTime.FORMAT_DATE));
			builder.addData(DecimalUtils.format(min_temp.value, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(max_temp.value, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(rh.value * 100.0, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(min_ws.value, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(max_ws.value, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(wd.value)), DataType.WIND_DIR));
			builder.addData(DecimalUtils.format(rain, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(dfwi.value.dFFMC, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(dfwi.value.dDMC, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(dfwi.value.dDC, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(temp_isi, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(dfwi.value.dBUI, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(temp_fwi, DataType.FORCE_2));
			exporter.writeRow(builder.toRow());
			time.add(WTimeSpan.Day);
			timeWS.add(WTimeSpan.Day);
		}
	}

	private void exportNoonStandardWeather(DataExporter exporter) throws IOException {
		RowBuilder builder = new RowBuilder();
		builder.addData("Date");
		builder.addData("Temp");
		builder.addData("DewPoint");
		builder.addData("RH");
		builder.addData("WS");
		builder.addData("WD");
		builder.addData("Precip");
		builder.addData("FFMC");
		builder.addData("DMC");
		builder.addData("DC");
		builder.addData("ISI");
		builder.addData("BUI");
		builder.addData("FWI");
		exporter.writeHeader(builder.toHeader());
		WTime time = model.getStartTime();
		WTime endTime = model.getEndTime();
		WTime timeWS = new WTime(time.getTime(0), ws.getTimeManager());
		OutVariable<IWXData> wx = new OutVariable<IWXData>();
		OutVariable<IFWIData> ifwi = new OutVariable<IFWIData>();
		OutVariable<DFWIData> dfwi = new OutVariable<DFWIData>();
		OutVariable<IWXData> wxnoon = new OutVariable<IWXData>();
		wx.value = new IWXData();
		ifwi.value = new IFWIData();
		dfwi.value = new DFWIData();
		wxnoon.value = new IWXData();
		while (WTime.lessThanEqualTo(time, endTime)) {
			double temp_isi = 0;
			double temp_fwi = 0;
			WTime temp = new WTime(timeWS);

			WTime noon = new WTime(timeWS);
			WTime rainEndTime = new WTime(timeWS);
			WTime rainStartTime;
			
			if (timeWS.getTimeManager().getWorldLocation().getEndDST().getTotalSeconds() > 0) {
				noon.subtract(new WTimeSpan(0, 12, 0, 0));
				noon.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
				noon.add(new WTimeSpan(0, 13, 0, 0));
				temp.add(new WTimeSpan(0, 13, 0, 0));
				//rainEndTime.Add(new WTimeSpan(0, 14, 0, 0));
			}
			else {
				noon.subtract(new WTimeSpan(0, 12, 0, 0));
				noon.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
				noon.add(new WTimeSpan(0, 12, 0, 0));
				temp.add(new WTimeSpan(0, 12, 0, 0));
				//rainEndTime.Add(new WTimeSpan(0, 13, 0, 0));
			}
			rainStartTime = new WTime(noon);
			rainStartTime.add(new WTimeSpan(0, 1, 0, 0));
			ws.getInstantaneousValues(temp, 0, wx, ifwi, dfwi);
			try {
				ws.getInstantaneousValues(noon, 0, wxnoon, null, null);
				if (wxnoon.value == null) {
					wxnoon.value = new IWXData();
					noon = new WTime(timeWS);
					noon.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
					ws.getInstantaneousValues(noon, 0, wxnoon, null, null);
					rainStartTime = new WTime(noon);
				}
				temp_isi = Fwi.isiFBP(dfwi.value.dFFMC, wxnoon.value.windSpeed, 60 * 60);
				temp_fwi = Fwi.fwi(temp_isi, dfwi.value.dBUI);
			}
			catch (Exception ex) {
				REDappLogger.error("Error getting instantaneous values", ex);
			}
			double rain = 0;

			rainStartTime.subtract(new WTimeSpan(0, 24, 0, 0));
			
			temp = new WTime(rainStartTime);
			
			rainEndTime = new WTime(rainStartTime);
			rainEndTime.add(new WTimeSpan(0, 24, 0, 0));
			
			while (WTime.lessThan(temp, rainEndTime)) {
				OutVariable<IWXData> data = new OutVariable<IWXData>();
				data.value = new IWXData();
				ws.getInstantaneousValues(temp, 0, data, null, null);
				if (data.value != null)
					rain += data.value.precipitation;
				temp.add(WTimeSpan.Hour);
			}
			builder.addData(time.toString(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST | WTime.FORMAT_STRING_YYYY_MM_DD | WTime.FORMAT_DATE));
			builder.addData(DecimalUtils.format(wx.value.temperature, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(wx.value.dewPointTemperature, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(wx.value.rh * 100.0, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(wx.value.windSpeed, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(wx.value.windDirection)), DataType.WIND_DIR));
			builder.addData(DecimalUtils.format(rain, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(dfwi.value.dFFMC, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(dfwi.value.dDMC, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(dfwi.value.dDC, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(temp_isi, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(dfwi.value.dBUI, DataType.FORCE_2));
			builder.addData(DecimalUtils.format(temp_fwi, DataType.FORCE_2));
			exporter.writeRow(builder.toRow());
			time.add(WTimeSpan.Day);
			timeWS.add(WTimeSpan.Day);
		}
	}

	private void exportNoonWeather(DataExporter exporter) throws IOException {
		RowBuilder builder = new RowBuilder();
		builder.addData("Date");
		builder.addData("Temp");
		builder.addData("Min_RH");
		builder.addData("Precip");
		builder.addData("WS");
		builder.addData("WD");
		builder.addData("FFMC");
		builder.addData("DMC");
		builder.addData("DC");
		builder.addData("ISI");
		builder.addData("BUI");
		builder.addData("FWI");
		exporter.writeHeader(builder.toHeader());
		WTime time = model.getStartTime();
		WTime endTime = model.getEndTime();
		WTime timeWS = new WTime(time.getTime(0), ws.getTimeManager());
		OutVariable<Double> noon_temp = new OutVariable<>();
		OutVariable<Double> noon_dew = new OutVariable<>();
		OutVariable<Double> noon_rh = new OutVariable<>();
		OutVariable<Double> noon_precip = new OutVariable<>();
		OutVariable<Double> noon_ws = new OutVariable<>();
		OutVariable<Double> noon_wd = new OutVariable<>();
		OutVariable<Double> noon_ffmc = new OutVariable<>();
		OutVariable<Double> noon_dmc = new OutVariable<>();
		OutVariable<Double> noon_dc = new OutVariable<>();
		OutVariable<Double> noon_isi = new OutVariable<>();
		OutVariable<Double> noon_bui = new OutVariable<>();
		OutVariable<Double> noon_fwi = new OutVariable<>();
		while (WTime.lessThanEqualTo(time, endTime)) {
			nwc.getNoonWeatherValues(timeWS, noon_temp, noon_dew, noon_rh, noon_ws, noon_wd, noon_precip);
			nwc.getNoonFWI(timeWS, noon_dc, noon_dmc, noon_ffmc, noon_bui, noon_isi, noon_fwi);
			builder.addData(time.toString(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST | WTime.FORMAT_STRING_YYYY_MM_DD | WTime.FORMAT_DATE));
			builder.addData(DecimalUtils.format(noon_temp.value, DataType.TEMPERATURE));
			builder.addData(DecimalUtils.format(noon_rh.value * 100.0, DataType.RH));
			builder.addData(DecimalUtils.format(noon_precip.value, DataType.PRECIP));
			builder.addData(DecimalUtils.format(noon_ws.value, DataType.WIND_SPEED));
			builder.addData(DecimalUtils.format(CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(noon_wd.value)), DataType.WIND_DIR));
			builder.addData(DecimalUtils.format(noon_ffmc.value, DataType.FFMC));
			builder.addData(DecimalUtils.format(noon_dmc.value, DataType.DMC));
			builder.addData(DecimalUtils.format(noon_dc.value, DataType.DC));
			builder.addData(DecimalUtils.format(noon_isi.value, DataType.ISI));
			builder.addData(DecimalUtils.format(noon_bui.value, DataType.BUI));
			builder.addData(DecimalUtils.format(noon_fwi.value, DataType.FWI));
			exporter.writeRow(builder.toRow());
			time.add(WTimeSpan.Day);
			timeWS.add(WTimeSpan.Day);
		}
	}

	// }}

	// {{ Edit

	private void editDaily(WTime time) {
		EditDailyDialog dlg = new EditDailyDialog(app.frmRedapp, time);
		setDialogPosition(dlg);
		WTime nt = new WTime(time);
		WTime startTime = new WTime((Long)ws.getAttribute(WEATHER_OPTION.START_TIME), ws.getTimeManager());
		WTime endTime = new WTime((Long)ws.getAttribute(WEATHER_OPTION.END_TIME), ws.getTimeManager());
		List<DailyWeatherData> data = new ArrayList<EditDailyDialog.DailyWeatherData>();
		while (WTime.lessThan(startTime, endTime)) {
			OutVariable<Double> min_temp = new OutVariable<Double>();
			OutVariable<Double> max_temp = new OutVariable<Double>();
			OutVariable<Double> min_ws = new OutVariable<Double>();
			OutVariable<Double> max_ws = new OutVariable<Double>();
			OutVariable<Double> rh = new OutVariable<Double>();
			OutVariable<Double> precip = new OutVariable<Double>();
			OutVariable<Double> wd = new OutVariable<Double>();
			ws.getDailyValues(startTime, min_temp, max_temp, min_ws, max_ws, rh, precip, wd);
			wd.value = CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(wd.value));
			DailyWeatherData d = new DailyWeatherData();
			d.time = new WTime(startTime);
			d.minTemp = min_temp.value;
			d.maxTemp = max_temp.value;
			d.rh = rh.value * 100;
			d.precip = precip.value;
			d.minWS = min_ws.value;
			d.maxWS = max_ws.value;
			d.wd = wd.value;
			data.add(d);

			startTime.add(new WTimeSpan(1, 0, 0, 0));
		}

		nt.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
		dlg.setValues(data, nt);
		dlg.setVisible(true);
		if (dlg.getResult() == JFileChooser.APPROVE_OPTION) {
			if (dlg.getEditHourly()) {
				editHourly(dlg.getCurrentTime());
			}
			else {
				data = dlg.getValues();
				int modcount = 0;
				for (int i = 0; i < data.size(); i++) {
					DailyWeatherData d = data.get(i);
					if (d.modified) {
						modcount++;
						double wd = DEGREE_TO_RADIAN(COMPASS_TO_CARTESIAN_DEGREE(d.wd));
						ws.makeDailyObservations(d.time);
						ws.setDailyValues(d.time, d.minTemp, d.maxTemp, d.minWS, d.maxWS, d.rh / 100.0, d.precip, wd);
					}
				}
				if (modcount > 0) {
					calculate();
				}
			}
		}
	}

	private void editHourly(WTime time) {
		WTime tm = new WTime(time);
		tm.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
		IWXData[] data = new IWXData[24];
		for (int i = 0; i < 24; i++) {
			OutVariable<IWXData> wx = new OutVariable<IWXData>();
			wx.value = new IWXData();
			ws.getInstantaneousValues(tm, 0, wx, null, null);
			data[i] = wx.value;
			data[i].windDirection = CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(data[i].windDirection));
			data[i].rh = data[i].rh * 100.0;
			tm.add(WTimeSpan.Hour);
		}
		int firstHour = ws.firstHourOfDay(time);
		int lastHour = ws.lastHourOfDay(time);
		EditHourlyDialog dlg = new EditHourlyDialog(app.frmRedapp, time, data, firstHour, lastHour);
		setDialogPosition(dlg);
		dlg.setHour((int)time.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST));
		dlg.setVisible(true);
		if (dlg.getResult() == JFileChooser.APPROVE_OPTION) {
			if (dlg.hasChanged()) {
				tm = new WTime(time);
				tm.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
				ws.makeHourlyObservations(tm);
				tm.add(WTimeSpan.multiply(WTimeSpan.Hour, firstHour));
				for (int i = firstHour; i <= lastHour; i++) {
					data[i].windDirection = DEGREE_TO_RADIAN(COMPASS_TO_CARTESIAN_DEGREE(data[i].windDirection));
					data[i].rh = data[i].rh / 100.0;
					ws.setInstantaneousValues(tm, data[i]);
					tm.add(WTimeSpan.Hour);
				}
				calculate();
			}
		}
	}
	
	//TODO edit
	private void editNoon(WTime time) {
		EditNoonDialog dlg = new EditNoonDialog(app.frmRedapp, time);
		WTime nt = new WTime(time);
		WTime startTime = new WTime((Long)nwc.getAttribute(WEATHER_OPTION.START_TIME), nwc.getTimeManager());
		WTime endTime = new WTime((Long)nwc.getAttribute(WEATHER_OPTION.END_TIME), nwc.getTimeManager());
		List<NoonWeatherData> data = new ArrayList<>();
		while (WTime.lessThan(startTime, endTime)) {
			OutVariable<Double> temp = new OutVariable<Double>();
			OutVariable<Double> dew = new OutVariable<Double>();
			OutVariable<Double> ws = new OutVariable<Double>();
			OutVariable<Double> rh = new OutVariable<Double>();
			OutVariable<Double> precip = new OutVariable<Double>();
			OutVariable<Double> wd = new OutVariable<Double>();
			nwc.getNoonWeatherValues(startTime, temp, dew, rh, ws, wd, precip);
			wd.value = CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(wd.value));
			NoonWeatherData d = new NoonWeatherData();
			d.time = new WTime(startTime);
			d.temp = temp.value;
			d.rh = rh.value * 100;
			d.precip = precip.value;
			d.ws = ws.value;
			d.wd = wd.value;
			data.add(d);

			startTime.add(new WTimeSpan(1, 0, 0, 0));
		}
		
		nt.purgeToDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
		dlg.setValues(data, nt);
		dlg.setVisible(true);
		if (dlg.getResult() == JFileChooser.APPROVE_OPTION) {
			data = dlg.getValues();
			int modcount = 0;
			for (int i = 0; i < data.size(); i++) {
				NoonWeatherData d = data.get(i);
				if (d.modified) {
					modcount++;
					double wd = DEGREE_TO_RADIAN(COMPASS_TO_CARTESIAN_DEGREE(d.wd));
					nwc.setNoonWeatherValues(d.time, d.temp, d.rh / 100.0, d.ws, wd, d.precip);
				}
			}
			if (modcount > 0) {
				calculate();
			}
		}
	}

	// }}

	@Override
	public void reset() {
		firstImport = true;
		
		//Redmine 809
		//timezoneOverride = false;
		
		model.clear();
		ws = new CWFGM_WeatherStream();
		nwc = new NoonWeatherCondition(ws.getTimeManager());
		btnExport.setEnabled(false);
		btnEdit.setEnabled(false);
		tglHourly.setEnabled(true);
		tglDaily.setEnabled(true);
		tglNoon.setEnabled(true);
		btnAdd.setRightEnabled(true);
		btnEdit.setRightEnabled(true);
		btnExport.setRightEnabled(true);
		lblStatsPrecipitation.setText("Precipitation");
	}

	@Override
	public boolean supportsReset() {
		return true;
	}

	public void fuelTypeChanged() {
		int index = comboFbpFuelType.getSelectedIndex();
		prefs.putInt(FUEL_TYPE_INDEX_KEY, index);
		CardLayout layout = (CardLayout)fuelTypeCards.getLayout();
		switch (index) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 6:
		case 7:
		case 8:
		case 15:
		case 16:
		case 17:
			layout.show(fuelTypeCards, fuelTypeCardNames[0]);
			fuelTypeCards.setSize(fuelTypeCards.getSize().width, 0);
			fbpGroup.setSize(fbpGroup.getSize().width, fbpBaseHeight);
			tabFBPInternal.setPreferredSize(new Dimension(tabFBPInternal.getPreferredSize().width, tabBaseHeight));
			terrainGroup.setLocation(terrainGroup.getLocation().x, terrainBaseY);
			break;
		case 5:
			layout.show(fuelTypeCards, fuelTypeCardNames[1]);
			fuelTypeCards.setSize(fuelTypeCards.getSize().width, 25);
			fbpGroup.setSize(fbpGroup.getSize().width, fbpBaseHeight + 30);
			tabFBPInternal.setPreferredSize(new Dimension(tabFBPInternal.getPreferredSize().width, tabBaseHeight + 30));
			terrainGroup.setLocation(terrainGroup.getLocation().x, terrainBaseY + 30);
			break;
		case 9:
		case 10:
			layout.show(fuelTypeCards, fuelTypeCardNames[2]);
			fuelTypeCards.setSize(fuelTypeCards.getSize().width, 25);
			fbpGroup.setSize(fbpGroup.getSize().width, fbpBaseHeight + 30);
			tabFBPInternal.setPreferredSize(new Dimension(tabFBPInternal.getPreferredSize().width, tabBaseHeight + 30));
			terrainGroup.setLocation(terrainGroup.getLocation().x, terrainBaseY + 30);
			txtFbpPercentConifer.setText("50");
			break;
		case 11:
		case 12:
			layout.show(fuelTypeCards, fuelTypeCardNames[3]);
			fuelTypeCards.setSize(fuelTypeCards.getSize().width, 25);
			fbpGroup.setSize(fbpGroup.getSize().width, fbpBaseHeight + 30);
			tabFBPInternal.setPreferredSize(new Dimension(tabFBPInternal.getPreferredSize().width, tabBaseHeight + 30));
			terrainGroup.setLocation(terrainGroup.getLocation().x, terrainBaseY + 30);
			txtFbpPercentDeadFir.setText("50");
			break;
		case 13:
		case 14:
		//case 15: O1ab
			layout.show(fuelTypeCards, fuelTypeCardNames[4]);
			fuelTypeCards.setSize(fuelTypeCards.getSize().width, 55);
			fbpGroup.setSize(fbpGroup.getSize().width, fbpBaseHeight + 60);
			tabFBPInternal.setPreferredSize(new Dimension(tabFBPInternal.getPreferredSize().width, tabBaseHeight + 60));
			terrainGroup.setLocation(terrainGroup.getLocation().x, terrainBaseY + 60);
			txtFbpGrassCuring.setText("60");
			txtFbpGrassFuelLoad.setText("0.35");
			break;
		}
		index = FbpTab.adjustIndexComboBoxToFuelType(index);
		fbpCalculations.fuelType = index;
		
		if (fbpCalculations.fuelType >= 17 && fbpCalculations.fuelType <= 21)
			fbpCalculations.cfbPossible = false;
		else
			fbpCalculations.cfbPossible = true;
		
		calculate();
	}

	void fuelTypeInfo() {
		int fuelType = FbpTab.adjustIndexComboBoxToFuelType(comboFbpFuelType
				.getSelectedIndex());
		FuelTypeInfo info = new FuelTypeInfo(app, fuelType);
		info.setVisible(true);
	}

	// {{ Ui stuff

	private RTextField txtDailyFFMC;
	private RTextField txtDailyDMC;
	private RTextField txtDailyDC;
	private RTextField txtHourlyFFMC;
	private JCheckBox chkDailyFit;
	private RTextField txtFbpCrownBaseHeight;
	private RTextField txtFbpPercentConifer;
	private RTextField txtFbpPercentDeadFir;
	private RTextField txtFbpGrassCuring;
	private RTextField txtFbpGrassFuelLoad;
	private RTextField txtFbpElevation;
	private RTextField txtFbpSlope;
	private RTextField txtFbpAspect;
	private RTextField txtPrecipitation;
	private RLabel lblStatsPrecipitation;
	private JTree st_tview;
	private JTree st_tviewday;
	private JTree st_tviewnoon;
	private RButton btnImport;
	private RButton btnReset;
	private RContextMenuButton btnExport;
	private RContextMenuButton btnAdd;
	private RContextMenuButton btnEdit;
	private JButton btnFbpInformation;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane_2;
	private JScrollPane scrollPaneDay;
	private JScrollPane scrollPaneNoon;
	private JPopupMenu popup = new JPopupMenu();
	private JPopupMenu popup2 = new JPopupMenu();
	private JMenuItem editItem = new JMenuItem("Edit");
	private JMenuItem editItem2 = new JMenuItem("Edit");
	private JMenuItem transferItem = new JMenuItem(Main.resourceManager.getString("ui.label.stats.tofbp"));
	private JMenuItem viewMapItem = new JMenuItem(Main.resourceManager.getString("ui.label.stats.map"));
	private RCheckBoxList st_lview;
	private JComboBox<String> comboHourlyMethod;
	private JComboBox<String> stats_groupBy;
	private JComboBox<String> cmb_hourlyStart;
	private RComboBox<String> comboFbpFuelType;
	//private RComboBox<TimeZoneInfo> timezoneList;
	private JCheckBox chckFbpSlope;
	private JTable tableStats;
	private JTable headerTable;
	private JPanel fuelTypeCards;
	private JPanel tabFBPInternal;
	private JTabbedPane tabsStats;
	private RGroupBox fbpGroup;
	private RGroupBox terrainGroup;
	private RLabel lblFbpCrownBaseHeightUnit;
	private RLabel lblFbpGrassFuelLoadUnits;
	private RLabel lblFbpElevationUnits;
	private RToggleButton tglDaily;
	private RToggleButton tglHourly;
	private RToggleButton tglNoon;

	private static final int terrainBaseY = 89;
	private static final int fbpBaseHeight = 82;
	private final int tabBaseHeight;
	private boolean cmbHourlyStartSkipAction = false;
	private RLabel lblHourlyFfmcStart;
	private RLabel lblStatsHourlyFfmc;
	private RLabel lblStatsDailyFit;

	protected void initialize() {
		if (initialized)
			return;
		initialized = true;

		setLayout(null);
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 971, 501);
		else
			setBounds(0, 0, 981, 506);

		tabsStats = new JTabbedPane(JTabbedPane.BOTTOM);
		tabsStats.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		if (Main.isMac())
			tabsStats.setBounds(2, 0, 237, 418);
		else if (Main.isLinux())
			tabsStats.setBounds(6, 0, 229, 418);
		else
			tabsStats.setBounds(10, 0, 221, 428);
		add(tabsStats);

		JPanel tabColumns = new JPanel();
		if (Main.isWindows())
			tabColumns.setBackground(new Color(255, 255, 255));
		tabsStats.addTab(Main.resourceManager.getString("ui.label.stats.columns.title"), null, tabColumns, null);
		tabColumns.setLayout(null);

		st_lview = new RCheckBoxList();
		st_lview.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane_2 = new JScrollPane(st_lview);
		scrollPane_2.setOpaque(true);
		if (Main.isLinux())
			scrollPane_2.setBounds(5, 90, 205, 265);
		else if (Main.isMac())
			scrollPane_2.setBounds(5, 90, 205, 267);
		else
			scrollPane_2.setBounds(5, 90, 205, 307);
		if (Main.isWindows())
			scrollPane_2.setBackground(Color.white);
		st_lview.setBounds(0, 0, 205, 226);
		tabColumns.add(scrollPane_2);
		
		RLabel lblDisplayHourly = new RLabel(Main.resourceManager.getString("ui.label.stats.disp"));
		lblDisplayHourly.setBounds(7, 0, 150, 20);
		tabColumns.add(lblDisplayHourly);
		
		//TODO working UI
		RToggleButton.RToggleButtonGroup tglGroup = new RToggleButton.RToggleButtonGroup();
		
		tglHourly = new RToggleButton(Main.resourceManager.getString("ui.label.stats.disp.hour"));
		tglHourly.setLeft(true);
		tglHourly.setBounds(4, 20, 69, 20);
		tabColumns.add(tglHourly);
		tglGroup.add(tglHourly);
		
		tglDaily = new RToggleButton(Main.resourceManager.getString("ui.label.stats.disp.day"));
		tglDaily.setBounds(73, 20, 70, 20);
		tabColumns.add(tglDaily);
		tglGroup.add(tglDaily);
		
		tglNoon = new RToggleButton(Main.resourceManager.getString("ui.label.stats.disp.noon"));
		tglNoon.setRight(true);
		tglNoon.setBounds(143, 20, 69, 20);
		tabColumns.add(tglNoon);
		tglGroup.add(tglNoon);

		RLabel lblGroupBy = new RLabel(Main.resourceManager.getString("ui.label.stats.columns.groupby"));
		lblGroupBy.setBounds(7, 40, 201, 20);
		tabColumns.add(lblGroupBy);

		stats_groupBy = new JComboBox<String>();
		stats_groupBy.setModel(new DefaultComboBoxModel<String>(new String[] {Main.resourceManager.getString("ui.label.stats.columns.groupby.type"),
				Main.resourceManager.getString("ui.label.stats.columns.groupby.order") }));
		stats_groupBy.setBounds(4, 60, 208, 22);
		tabColumns.add(stats_groupBy);

		st_tview = new JTree();
		scrollPane = new JScrollPane(st_tview);
		if (Main.isLinux())
			scrollPane.setBounds(5, 90, 205, 265);
		else if (Main.isMac())
			scrollPane.setBounds(5, 90, 205, 267);
		else
			scrollPane.setBounds(5, 90, 205, 307);
		scrollPane.setOpaque(false);
		st_tview.setBounds(0, 0, 205, 226);
		tabColumns.add(scrollPane);
		
		st_tviewday = new JTree();
		scrollPaneDay = new JScrollPane(st_tviewday);
		if (Main.isLinux())
			scrollPaneDay.setBounds(5, 90, 205, 265);
		else if (Main.isMac())
			scrollPaneDay.setBounds(5, 90, 205, 267);
		else
			scrollPaneDay.setBounds(5, 90, 205, 307);
		scrollPaneDay.setOpaque(false);
		st_tviewday.setBounds(0, 0, 205, 226);
		tabColumns.add(scrollPaneDay);
		
		st_tviewnoon = new JTree();
		scrollPaneNoon = new JScrollPane(st_tviewnoon);
		if (Main.isLinux())
			scrollPaneNoon.setBounds(5, 90, 205, 265);
		else if (Main.isMac())
			scrollPaneNoon.setBounds(5, 90, 205, 267);
		else
			scrollPaneNoon.setBounds(5, 90, 205, 307);
		scrollPaneNoon.setOpaque(false);
		st_tviewnoon.setBounds(0, 0, 205, 226);
		tabColumns.add(scrollPaneNoon);

		JPanel tabStatsCodes = new JPanel();
		if (Main.isWindows())
			tabStatsCodes.setBackground(Color.white);
		tabsStats.addTab(Main.resourceManager.getString("ui.label.fire.fwi"), null, tabStatsCodes, null);
		tabStatsCodes.setLayout(null);

		RGroupBox dailygroup = new RGroupBox();
		dailygroup.setText(Main.resourceManager.getString("ui.label.stats.codes.yest.title"));
		dailygroup.setBounds(2, 2, 211, 140);
		tabStatsCodes.add(dailygroup);

		RLabel lblStatsDailyFFMC = new RLabel(Main.resourceManager.getString("ui.label.stats.codes.yest.ffmc"));
		lblStatsDailyFFMC.setBounds(10, 20, 111, 21);
		dailygroup.add(lblStatsDailyFFMC);

		txtDailyFFMC = new RTextField(DecimalUtils.getFormat(DecimalUtils.DataType.FFMC));
		txtDailyFFMC.setBounds(142, 20, 59, 20);
		txtDailyFFMC.setColumns(10);
		dailygroup.add(txtDailyFFMC);

		RLabel lblStatsDailyDmc = new RLabel(Main.resourceManager.getString("ui.label.stats.codes.yest.dmc"));
		lblStatsDailyDmc.setBounds(10, 50, 111, 21);
		dailygroup.add(lblStatsDailyDmc);

		txtDailyDMC = new RTextField(DecimalUtils.getFormat(DecimalUtils.DataType.DMC));
		txtDailyDMC.setColumns(10);
		txtDailyDMC.setBounds(142, 50, 59, 20);
		dailygroup.add(txtDailyDMC);

		RLabel lblStatsDailyDc = new RLabel(Main.resourceManager.getString("ui.label.stats.codes.yest.dc"));
		lblStatsDailyDc.setBounds(10, 80, 111, 21);
		dailygroup.add(lblStatsDailyDc);

		txtDailyDC = new RTextField(DecimalUtils.getFormat(DecimalUtils.DataType.DC));
		txtDailyDC.setColumns(10);
		txtDailyDC.setBounds(142, 80, 59, 20);
		dailygroup.add(txtDailyDC);

		lblStatsPrecipitation = new RLabel(Main.resourceManager.getString("ui.label.stats.codes.yest.precip"));
		String startText = "", endText = "";
		if (app.getSelectedTimeZone().getDSTAmount().getTotalSeconds() > 0)
			startText = "1301";
		else
			startText = "1201";
		endText = "2300";
		lblStatsPrecipitation.setBounds(10, 110, 141, 21);
		lblStatsPrecipitation.setToolTipText(Main.resourceManager.getString("ui.label.stats.codes.yest.precip.desc", startText, endText));
		dailygroup.add(lblStatsPrecipitation);

		txtPrecipitation = new RTextField(DecimalUtils.getFormat(DecimalUtils.DataType.PRECIP));
		txtPrecipitation.setColumns(10);
		txtPrecipitation.setBounds(142, 110, 59, 20);
		dailygroup.add(txtPrecipitation);

		RGroupBox hourlyCodes = new RGroupBox();
		hourlyCodes.setText(Main.resourceManager.getString("ui.label.stats.codes.method"));
		hourlyCodes.setBounds(2, 150, 211, 144);
		tabStatsCodes.add(hourlyCodes);
		
		comboHourlyMethod = new JComboBox<String>();
		comboHourlyMethod.setModel(new DefaultComboBoxModel<String>(new String[] {
				Main.resourceManager.getString("ui.label.fwicalc.hourly.lawson"), Main.resourceManager.getString("ui.label.fwicalc.hourly.wagner") }));
		comboHourlyMethod.setBounds(10, 20, 190, 22);
		hourlyCodes.add(comboHourlyMethod);
		
		String fitLabel = "";
		
		if (app.getSelectedTimeZone().getDSTAmount().getTotalSeconds() > 0)
			fitLabel = Main.resourceManager.getString("ui.label.stats.codes.today.fit", "17");
		else
			fitLabel = Main.resourceManager.getString("ui.label.stats.codes.today.fit", "16");

		lblStatsDailyFit = new RLabel(fitLabel);
		if (Main.isMac()) {
			Font font = lblStatsDailyFit.getFont();
			font = font.deriveFont(font.getSize() - 1.0f);
			lblStatsDailyFit.setFont(font);
		}
		lblStatsDailyFit.setBounds(10, 50, 165, 20);
		hourlyCodes.add(lblStatsDailyFit);
		
		chkDailyFit = new JCheckBox();
		if (Main.isLinux())
			chkDailyFit.setFont(chkDailyFit.getFont().deriveFont(12.0f));
		else if (Main.isWindows())
			chkDailyFit.setBackground(new Color(245, 245, 245));
		if (Main.isMac())
			chkDailyFit.setBounds(175, 50, 30, 20);
		else
			chkDailyFit.setBounds(185, 50, 20, 20);
		chkDailyFit.setHorizontalAlignment(SwingConstants.RIGHT);
		chkDailyFit.setAlignmentX(RIGHT_ALIGNMENT);
		hourlyCodes.add(chkDailyFit);
		
		lblStatsHourlyFfmc = new RLabel(Main.resourceManager.getString("ui.label.stats.codes.today.ffmc"));
		lblStatsHourlyFfmc.setBounds(10, 80, 111, 20);
		hourlyCodes.add(lblStatsHourlyFfmc);

		txtHourlyFFMC = new RTextField(DecimalUtils.getFormat(DecimalUtils.DataType.FFMC));
		txtHourlyFFMC.setColumns(10);
		txtHourlyFFMC.setBounds(127, 80, 74, 20);
		hourlyCodes.add(txtHourlyFFMC);
		
		lblHourlyFfmcStart = new RLabel(Main.resourceManager.getString("ui.label.stats.codes.today.start"));
		lblHourlyFfmcStart.setBounds(10, 110, 111, 20);
		hourlyCodes.add(lblHourlyFfmcStart);
		
		cmb_hourlyStart = new JComboBox<String>();
		cmb_hourlyStart.setModel(new DefaultComboBoxModel<String>(new String[] {
				"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"}));
		cmb_hourlyStart.setSelectedIndex(1);
		cmb_hourlyStart.setBounds(127, 110, 74, 22);
		hourlyCodes.add(cmb_hourlyStart);
		
		lblStatsHourlyFfmc.setForeground(chkDailyFit.isSelected() ? Color.GRAY : Color.BLACK);
		lblHourlyFfmcStart.setForeground(chkDailyFit.isSelected() ? Color.GRAY : Color.BLACK);
		txtHourlyFFMC.setEnabled(!chkDailyFit.isSelected());
		cmb_hourlyStart.setEnabled(!chkDailyFit.isSelected());

		JPanel tabFBP = new JPanel();
		if (Main.isWindows())
			tabFBP.setBackground(new Color(255, 255, 255));
		tabsStats.addTab(Main.resourceManager.getString("ui.label.stats.fbp.title"), null, tabFBP, null);
		tabFBP.setLayout(new BorderLayout(0, 0));

		tabFBPInternal = new JPanel();
		if (Main.isWindows())
			tabFBPInternal.setBackground(new Color(255, 255, 255));
		if (Main.isMac())
			tabFBPInternal.setPreferredSize(new Dimension(tabsStats.getWidth() - 5, tabBaseHeight));
		else
			tabFBPInternal.setPreferredSize(new Dimension(tabsStats.getWidth() - 5, tabBaseHeight));
		tabFBPInternal.setLayout(null);

		fbpGroup = new RGroupBox();
		fbpGroup.setText(Main.resourceManager.getString("ui.label.fbp.fuel.title"));
		fbpGroup.setBounds(2, 2, 211, 80);
		tabFBPInternal.add(fbpGroup);

		comboFbpFuelType = new RComboBox<String>();
		comboFbpFuelType.setModel(new DefaultComboBoxModel<String>(new String[] {
				"C-1:  " + Main.resourceManager.getString("ui.label.fuel.c1"),
				"C-2:  " + Main.resourceManager.getString("ui.label.fuel.c2"),
				"C-3:  " + Main.resourceManager.getString("ui.label.fuel.c3"),
				"C-4:  " + Main.resourceManager.getString("ui.label.fuel.c4"),
				"C-5:  " + Main.resourceManager.getString("ui.label.fuel.c5"),
				"C-6:  " + Main.resourceManager.getString("ui.label.fuel.c6"),
				"C-7:  " + Main.resourceManager.getString("ui.label.fuel.c7"),
				"D-1:  " + Main.resourceManager.getString("ui.label.fuel.d1"),
				"D-2:  " + Main.resourceManager.getString("ui.label.fuel.d2"),
				"M-1:  " + Main.resourceManager.getString("ui.label.fuel.m1"),
				"M-2:  " + Main.resourceManager.getString("ui.label.fuel.m2"),
				"M-3:  " + Main.resourceManager.getString("ui.label.fuel.m3"),
				"M-4:  " + Main.resourceManager.getString("ui.label.fuel.m4"),
				"O-1a:  " + Main.resourceManager.getString("ui.label.fuel.o1a"),
				"O-1b:  " + Main.resourceManager.getString("ui.label.fuel.o1b"),
				//"O-1ab:	" + Main.resourceManager.getString("ui.label.fuel.o1ab"),
				"S-1:  " + Main.resourceManager.getString("ui.label.fuel.s1"),
				"S-2:  " + Main.resourceManager.getString("ui.label.fuel.s2"),
				"S-3:  " + Main.resourceManager.getString("ui.label.fuel.s3") }));
		comboFbpFuelType.setWide(true);
		comboFbpFuelType.setBounds(10, 20, 191, 22);
		fbpGroup.add(comboFbpFuelType);

		btnFbpInformation = new JButton(Main.resourceManager.getString("ui.label.fbp.fuel.info"));
		btnFbpInformation.setBounds(10, 50, 191, 22);
		btnFbpInformation.addActionListener((e) -> fuelTypeInfo());
		fbpGroup.add(btnFbpInformation);

		fuelTypeCards = new JPanel();
		fuelTypeCards.setBackground(new Color(245, 245, 245));
		fuelTypeCards.setBounds(10, 80, 191, 55);
		fuelTypeCards.setLayout(new CardLayout(0, 0));
		fbpGroup.add(fuelTypeCards);

		JPanel panel_3 = new JPanel();
		panel_3.setBackground(new Color(245, 245, 245));
		fuelTypeCards.add(panel_3, fuelTypeCardNames[0]);

		JPanel panel_4 = new JPanel();
		panel_4.setBackground(new Color(245, 245, 245));
		panel_4.setLayout(new SpringLayout());
		fuelTypeCards.add(panel_4, fuelTypeCardNames[1]);

		RLabel lblFbpCrownBaseHeight = new RLabel(Main.resourceManager.getString("ui.label.fire.cbh.short"));
		if (Main.isFrench())
			lblFbpCrownBaseHeight.setToolTipText(Main.resourceManager.getString("ui.label.fire.cbh"));
		if (Main.isLinux() || Main.isMac())
			lblFbpCrownBaseHeight.setFont(new Font("Tahoma", Font.PLAIN, 11));
		//lblFbpCrownBaseHeight.setSize(96, 20);
		panel_4.add(lblFbpCrownBaseHeight);

		txtFbpCrownBaseHeight = new RTextField();
		//txtFbpCrownBaseHeight.setSize(70, 20);
		//txtFbpCrownBaseHeight.setLocation(101, 0);
		panel_4.add(txtFbpCrownBaseHeight);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpCrownBaseHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblFbpCrownBaseHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
		//lblFbpCrownBaseHeightUnit.setSize(15, 20);
		//lblFbpCrownBaseHeightUnit.setLocation(176, 0);
		panel_4.add(lblFbpCrownBaseHeightUnit);

		SpringUtilities.makeCompactGrid(panel_4, 1, 3, 0, 0, 0, 0, 2, 0);

		JPanel panel_5 = new JPanel();
		panel_5.setBackground(new Color(245, 245, 245));
		panel_5.setLayout(null);
		fuelTypeCards.add(panel_5, fuelTypeCardNames[2]);

		RLabel label_2 = new RLabel(Main.resourceManager.getString("ui.label.fire.pc"));
		label_2.setSize(96, 20);
		panel_5.add(label_2);

		txtFbpPercentConifer = new RTextField(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		txtFbpPercentConifer.setSize(70, 20);
		txtFbpPercentConifer.setLocation(101, 0);
		panel_5.add(txtFbpPercentConifer);

		final RLabel lblFbpPercentConifer = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		lblFbpPercentConifer.setSize(15, 20);
		lblFbpPercentConifer.setLocation(176, 0);
		panel_5.add(lblFbpPercentConifer);

		JPanel panel_6 = new JPanel();
		panel_6.setBackground(new Color(245, 245, 245));
		panel_6.setLayout(null);
		fuelTypeCards.add(panel_6, fuelTypeCardNames[3]);

		RLabel lblFbpPercentDeadFir = new RLabel(Main.resourceManager.getString("ui.label.fire.pdf"));
		lblFbpPercentDeadFir.setSize(106, 20);
		panel_6.add(lblFbpPercentDeadFir);

		txtFbpPercentDeadFir = new RTextField(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		txtFbpPercentDeadFir.setSize(60, 20);
		txtFbpPercentDeadFir.setLocation(111, 0);
		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumIntegerDigits(3);
		txtFbpPercentDeadFir.setFormat(format);
		panel_6.add(txtFbpPercentDeadFir);

		final RLabel lblFbpPercentDeadFirUnits = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		lblFbpPercentDeadFirUnits.setSize(15, 20);
		lblFbpPercentDeadFirUnits.setLocation(176, 0);
		panel_6.add(lblFbpPercentDeadFirUnits);

		JPanel panel_7 = new JPanel();
		panel_7.setBackground(new Color(245, 245, 245));
		panel_7.setLayout(new SpringLayout());
		fuelTypeCards.add(panel_7, fuelTypeCardNames[4]);

		RLabel lblFbpGrassCuring = new RLabel(Main.resourceManager.getString("ui.label.fire.gcuring.short"));
		if (Main.resourceManager.loc.getISO3Language().contains("fr"))
			lblFbpGrassCuring.setToolTipText(Main.resourceManager.getString("ui.label.fire.gcuring"));
		//lblFbpGrassCuring.setSize(96, 20);
		panel_7.add(lblFbpGrassCuring);

		txtFbpGrassCuring = new RTextField(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		//txtFbpGrassCuring.setSize(40, 20);
		//txtFbpGrassCuring.setLocation(106, 0);
		NumberFormat format2 = NumberFormat.getInstance();
		format2.setMaximumIntegerDigits(3);
		txtFbpGrassCuring.setFormat(format2);
		panel_7.add(txtFbpGrassCuring);

		RLabel lblFbpGrassCuringUnits = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		//lblFbpGrassCuringUnits.setSize(35, 20);
		//lblFbpGrassCuringUnits.setLocation(151, 0);
		panel_7.add(lblFbpGrassCuringUnits);

		RLabel lblFbpFuelLoad = new RLabel(Main.resourceManager.getString("ui.label.fire.gload.short"));
		if (Main.resourceManager.loc.getISO3Language().contains("fr"))
			lblFbpFuelLoad.setToolTipText(Main.resourceManager.getString("ui.label.fire.gload"));
		if (Main.isMac()) {
			Font f = lblFbpFuelLoad.getFont();
			lblFbpFuelLoad.setFont(f.deriveFont(f.getSize() - 1));
		}
		//lblFbpFuelLoad.setSize(101, 20);
		//lblFbpFuelLoad.setLocation(0, 30);
		panel_7.add(lblFbpFuelLoad);

		txtFbpGrassFuelLoad = new RTextField(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		//txtFbpGrassFuelLoad.setSize(40, 20);
		//txtFbpGrassFuelLoad.setLocation(106, 30);
		NumberFormat format3 = NumberFormat.getInstance();
		format3.setMaximumIntegerDigits(3);
		txtFbpGrassFuelLoad.setFormat(format3);
		panel_7.add(txtFbpGrassFuelLoad);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpGrassFuelLoadUnits = new RLabel(Main.resourceManager.getString("ui.label.units.kgm2"));
		else
			lblFbpGrassFuelLoadUnits = new RLabel(Main.resourceManager.getString("ui.label.units.tonsperacre"));
		//lblFbpGrassFuelLoadUnits.setSize(50, 20);
		//lblFbpGrassFuelLoadUnits.setLocation(151, 30);
		panel_7.add(lblFbpGrassFuelLoadUnits);

		SpringUtilities.makeCompactGrid(panel_7, 2, 3, 0, 0, 0, 0, 2, 10);

		terrainGroup = new RGroupBox();
		terrainGroup.setText(Main.resourceManager.getString("ui.label.fbp.terrain.title"));
		terrainGroup.setBounds(2, 62, 211, 110);
		tabFBPInternal.add(terrainGroup);

		RLabel lblFbpElevation = new RLabel(Main.resourceManager.getString("ui.label.fbp.terrain.elev"));
		lblFbpElevation.setSize(96, 20);
		lblFbpElevation.setLocation(10, 20);
		terrainGroup.add(lblFbpElevation);

		txtFbpElevation = new RTextField(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		txtFbpElevation.setColumns(10);
		txtFbpElevation.setBounds(111, 20, 70, 20);
		terrainGroup.add(txtFbpElevation);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpElevationUnits = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblFbpElevationUnits = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
		lblFbpElevationUnits.setSize(15, 20);
		lblFbpElevationUnits.setLocation(186, 20);
		terrainGroup.add(lblFbpElevationUnits);

		chckFbpSlope = new JCheckBox(Main.resourceManager.getString("ui.label.fbp.terrain.slope"));
		if (Main.isLinux())
			chckFbpSlope.setFont(chckFbpSlope.getFont().deriveFont(12.0f));
		if (Main.isWindows())
			chckFbpSlope.setBackground(new Color(245, 245, 245));
		chckFbpSlope.setSize(96, 20);
		chckFbpSlope.setLocation(10, 50);
		terrainGroup.add(chckFbpSlope);

		txtFbpSlope = new RTextField(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		txtFbpSlope.setColumns(10);
		txtFbpSlope.setBounds(111, 50, 70, 20);
		terrainGroup.add(txtFbpSlope);

		lblFbpSlopeUnits.setSize(15, 20);
		lblFbpSlopeUnits.setLocation(186, 50);
		terrainGroup.add(lblFbpSlopeUnits);

		RLabel lblFbpAspect = new RLabel(Main.resourceManager.getString("ui.label.fbp.terrain.aspect"));
		lblFbpAspect.setSize(96, 20);
		lblFbpAspect.setLocation(10, 80);
		terrainGroup.add(lblFbpAspect);

		txtFbpAspect = new RTextField(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		txtFbpAspect.setColumns(10);
		txtFbpAspect.setBounds(111, 80, 70, 20);
		terrainGroup.add(txtFbpAspect);

		final RLabel lblFbpAspectUnits = new RLabel(Main.resourceManager.getString("ui.label.units.degrees"));
		lblFbpAspectUnits.setSize(15, 20);
		lblFbpAspectUnits.setLocation(186, 80);
		terrainGroup.add(lblFbpAspectUnits);

		final JScrollPane scrollPane_1 = new JScrollPane(tabFBPInternal);
		scrollPane_1.setBorder(null);
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		if (Main.isLinux())
			scrollPane_1.setBounds(0, 0, tabsStats.getWidth() - 5, 338);
		else
			scrollPane_1.setBounds(0, 0, tabsStats.getWidth() - 5, 380);
		if (Main.isMac())
			scrollPane_1.getVerticalScrollBar().setVisible(true);
		tabFBP.add(scrollPane_1);

		tableStats = new JTable();
		tableStats.setCellSelectionEnabled(true);
		tableStats.getTableHeader().setDefaultRenderer(new HeaderRenderer());
		((DefaultTableCellRenderer)tableStats.getDefaultRenderer(String.class)).setHorizontalAlignment(SwingConstants.RIGHT);
		tableStats.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableStats.setShowGrid(true);
		tableStats.setGridColor(Color.black);
		Font f = new Font("Arial", Font.BOLD, 12);
		tableStats.getTableHeader().setFont(f);
		tableStats.setPreferredScrollableViewportSize(tableStats.getPreferredSize());
		tableStats.getTableHeader().setReorderingAllowed(false);

		headerTable = new JTable();
		headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		headerTable.setCellSelectionEnabled(true);
		headerTable.setPreferredScrollableViewportSize(new Dimension(135, 0));

		JTableHeader corner = headerTable.getTableHeader();
		corner.setReorderingAllowed(false);
		corner.setResizingAllowed(false);
		((DefaultTableCellRenderer)corner.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		corner.setFont(f);

		JScrollPane spane = new JScrollPane(tableStats);
		spane.setBorder(new LineBorder(new Color(130, 135, 144)));
		if (Main.isWindows())
			spane.setBounds(241, 0, 722, 428);
		else
			spane.setBounds(241, 0, 722, 418);
		spane.setRowHeaderView(headerTable);
		spane.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner);
		add(spane);

		JPanel panel1 = new JPanel();
		if (Main.isWindows())
			panel1.setBounds(10, 424, 951, 50);
		else
			panel1.setBounds(10, 419, 951, 50);
		FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
		layout.setAlignOnBaseline(true);
		panel1.setLayout(layout);
		if (Main.isWindows())
			panel1.setBackground(Color.white);
		add(panel1);

		btnImport = new RButton(Main.resourceManager.getString("ui.label.stats.import.button"));
		panel1.add(btnImport);

		btnAdd = new RContextMenuButton(Main.resourceManager.getString("ui.label.stats.add.button"));
		panel1.add(btnAdd);

		btnEdit = new RContextMenuButton(Main.resourceManager.getString("ui.label.stats.edit.button"));
		btnEdit.setEnabled(false);
		panel1.add(btnEdit);

		btnExport = new RContextMenuButton(Main.resourceManager.getString("ui.label.stats.export.button"));
		btnExport.setEnabled(false);
		panel1.add(btnExport);

		btnReset = new RButton(Main.resourceManager.getString("ui.label.footer.reset"));
		btnReset.addActionListener((e) -> reset());
		panel1.add(btnReset);
	}

	// }}

	@Override
	public void numberOfColumnsChanged(int count) {
		if (count == 0) {
			btnExport.setEnabled(false);
			btnEdit.setEnabled(false);
		}
		else {
			btnExport.setEnabled(true);
			btnEdit.setEnabled(true);
		}
	}

	@Override
	public void rowChanged(WTime time, Column c) {
	}

	public void editRequest() {
		editRequest(editTime);
		editTime = null;
	}
	
	double _dh;
	double _df;
	double _db;
	double _raz;
	double _area;
	public void display_on_map() {
		Column c = model.getColumn(StatsDataType.DH);
		Double val;
		try {
			val = Double.parseDouble(c.rowAt(editTime).toString());
		}
		catch (NumberFormatException ex) {
			val = null;
		}
		if (val != null) {
			_dh = val;
		}
		
		c = model.getColumn(StatsDataType.DF);
		try {
			val = Double.parseDouble(c.rowAt(editTime).toString());
		}
		catch (NumberFormatException ex) {
			val = null;
		}
		if (val != null) {
			_df = val;
		}
		
		c = model.getColumn(StatsDataType.DB);
		try {
			val = Double.parseDouble(c.rowAt(editTime).toString());
		}
		catch (NumberFormatException ex) {
			val = null;
		}
		if (val != null) {
			_db = val;
		}
		
		double wsv = 0;
		c = model.getColumn(StatsDataType.IMPORT_WS);
		try {
			val = Double.parseDouble(c.rowAt(editTime).toString());
		}
		catch (NumberFormatException ex) {
			val = null;
		}
		if (val != null) {
			wsv = val;
		}
		
		c = model.getColumn(StatsDataType.IMPORT_WD);
		try {
			val = Double.parseDouble(c.rowAt(editTime).toString());
		}
		catch (NumberFormatException ex) {
			val = null;
		}
		if (val != null) {
			if (wsv == 0.0) {
				_raz = 0.0;
			}
			else {
				_raz = NORMALIZE_ANGLE_DEGREE(val + 180);
				if (_raz == 0.0) {
					_raz = 360.0;
				}
			}
		}
		c = model.getColumn(StatsDataType.AREA);
		try {
			val = Double.parseDouble(c.rowAt(editTime).toString());
		}
		catch (NumberFormatException ex) {
			val = null;
		}
		if (val != null) {
			_area = val;
		}
		app.mapTab.drawFBP(this);
	}

	public double getDH() {
		return _dh;
	}

	public double getDF() {
		return _df;
	}

	public double getDB() {
		return _db;
	}

	public double getRAZ() {
		return _raz;
	}
	
	public double getArea() {
		return _area;
	}
	
	public void setHourCmb(int i) {
		if (!chkDailyFit.isSelected())
			cmb_hourlyStart.setSelectedIndex(i);
	}

	@SuppressWarnings("deprecation")
	public void transfer_to_fbp() {
		String ffmc = null;
		String dmc = null;
		String dc = null;
		String bui = null;
		String ws = null;
		String wd = null;
		Date dt = null;

		Column c = model.getColumn(StatsDataType.HFFMC);
		Double val;
		if (c != null) {
			try {
				val = Double.parseDouble(c.rowAt(editTime).toString());
			}
			catch (NumberFormatException ex) {
				val = null;
			}
			if (val != null) {
				ffmc = val.toString();
			}
		}
		c = model.getColumn(StatsDataType.DMC);
		if (c != null) {
			try {
				val = Double.parseDouble(c.rowAt(editTime).toString());
			}
			catch (NumberFormatException ex) {
				val = null;
			}
			if (val != null) {
				dmc = val.toString();
			}
		}
		c = model.getColumn(StatsDataType.DC);
		if (c != null) {
			try {
				val = Double.parseDouble(c.rowAt(editTime).toString());
			}
			catch (NumberFormatException ex) {
				val = null;
			}
			if (val != null) {
				dc = val.toString();
			}
		}
		c = model.getColumn(StatsDataType.BUI);
		if (c != null) {
			try {
				val = Double.parseDouble(c.rowAt(editTime).toString());
			}
			catch (NumberFormatException ex) {
				val = null;
			}
			if (val != null) {
				bui = val.toString();
			}
		}
		c = model.getColumn(StatsDataType.IMPORT_WS);
		if (c != null) {
			try {
				val = Double.parseDouble(c.rowAt(editTime).toString());
			}
			catch (NumberFormatException ex) {
				val = null;
			}
			if (val != null) {
				ws = val.toString();
			}
		}
		c = model.getColumn(StatsDataType.IMPORT_WD);
		if (c != null) {
			try {
				val = Double.parseDouble(c.rowAt(editTime).toString());
			}
			catch (NumberFormatException ex) {
				val = null;
			}
			if (val != null) {
				wd = val.toString();
			}
		}
		dt = new Date(3000, 1, 5, (int)editTime.getHour(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST), 0);

		app.fbpTab.transferValues(ffmc, dmc, dc, bui, bui == null ? null : true, ws, wd, dt);
		app.setCurrentTab(app.fbpTab);
		editTime = null;
	}

	public void editRequest(WTime time) {
		if (time == null)
			return;
		WTime nt = new WTime(time);
		if (filetype == FileType.NOON_WEATHER) {
			editNoon(time);
		}
		else {
			int res = ws.isDailyObservations(nt);
			if (res == 0 || tglDaily.isSelected()) {
				editDaily(time);
			}
			else if (res == 1) {
				editHourly(time);
			}
		}
	}

	public static interface StatsTabListener {
		public abstract void canTransferUpdated(boolean canTransfer);
	}

	private void notifyCanTransferUpdated(boolean canTransfer) {
		for (StatsTabListener listener : listeners) {
			listener.canTransferUpdated(canTransfer);
		}
	}

	@Override
	public void setInternetConnected(boolean conn) { }

	@Override
	public void onLocationChanged() {
		if (app.getLatitude() != null && app.getLongitude() != null)
			calculate();
	}
	
	private void updatePrecipitationTooltip() {
		OutVariable<WTime> start = new OutVariable<>();
		OutVariable<WTimeSpan> duration = new OutVariable<>();
		ws.getValidTimeRange(start, duration);
		long startHour = start.value.getHour(WTime.FORMAT_WITHDST | WTime.FORMAT_AS_LOCAL);
		if (start.value.getTotalSeconds() > 0 && startHour != 0) {
			startHour--;
			String startText, endText;
			//TODO use weather stream timezone
			if (app.getSelectedTimeZone().getDSTAmount().getTotalSeconds() > 0)
				startText = "1301 ";
			else
				startText = "1201 ";
			startText += Main.resourceManager.getString("ui.label.stats.codes.today.yesterday");
			endText = ("00" + String.valueOf(startHour));
			endText = endText.substring(endText.length() - 2, endText.length());
			endText += "00 ";
			endText += Main.resourceManager.getString("ui.label.stats.codes.today.today");
			lblStatsPrecipitation.setToolTipText(Main.resourceManager.getString("ui.label.stats.codes.yest.precip.desc", startText, endText));
		}
		else {
			if (app.getSelectedTimeZone().getDSTAmount().getTotalSeconds() > 0)
				lblStatsPrecipitation.setToolTipText(Main.resourceManager.getString("ui.label.stats.codes.yest.precip.desc1"));
			else
				lblStatsPrecipitation.setToolTipText(Main.resourceManager.getString("ui.label.stats.codes.yest.precip.desc2"));
		}
	}

	@Override
	public void onTimeZoneChanged() {
		updatePrecipitationTooltip();
		setTimezoneFromGlobal();

		if (app.getSelectedTimeZone().getDSTAmount().getTotalSeconds() > 0)
			lblStatsDailyFit.setText(Main.resourceManager.getString("ui.label.stats.codes.today.fit", "17"));
		else
			lblStatsDailyFit.setText(Main.resourceManager.getString("ui.label.stats.codes.today.fit", "16"));
		
		if (chkDailyFit.isSelected())
			cmb_hourlyStart.setSelectedIndex((app.getSelectedTimeZone().getDSTAmount().getTotalSeconds() > 0) ? 17 : 16);
		
		calculate();
	}
	
	private void setTimezoneFromGlobal() {
		long totalOffset = 0;
		long dstOffset = 0;
		
		dstOffset = app.getSelectedTimeZone().getDSTAmount().getTotalSeconds();
		totalOffset = app.getSelectedTimeZone().getTimezoneOffset().getTotalSeconds();
		
		long dstEnd;
		if (dstOffset > 0) {
			dstEnd = new WTimeSpan(366, 0, 0, 0).getTotalSeconds();
		}
		else {
			dstEnd = 0;
		}
		
		ws.setAttribute(GRID_ATTRIBUTE.DST_END, dstEnd);
		nwc.setAttribute(GRID_ATTRIBUTE.DST_END, dstEnd);
		ws.setAttribute(GRID_ATTRIBUTE.TIMEZONE, totalOffset);
		nwc.setAttribute(GRID_ATTRIBUTE.TIMEZONE, totalOffset);
	}

	@Override
	public void onDateChanged() {
		calculate();
	}

	@Override
	public void onCurrentTabChanged() {
		if (app.getCurrentTab() == this && checkboxUpdateNeeded && displayed) {
			model.importComplete();
		}
	}

	@Override
	public Component getLastComponent(Container aContainer) {
		return null;
	}

	@Override
	public Component getFirstComponent(Container aContainer) {
		return null;
	}

	@Override
	public Component getDefaultComponent(Container aContainer) {
		return null;
	}

	@Override
	public Component getComponentBefore(Container aContainer, Component aComponent) {
		if (aComponent == comboHourlyMethod) {
			if (cmb_hourlyStart.isEnabled())
				return cmb_hourlyStart;
			return txtPrecipitation.componentForTabs();
		}
		if (aComponent == cmb_hourlyStart)
			return txtHourlyFFMC.componentForTabs();
		if (txtHourlyFFMC.equalsForTabs(aComponent))
			return txtPrecipitation.componentForTabs();
		if (txtPrecipitation.equalsForTabs(aComponent))
			return txtDailyDC.componentForTabs();
		if (txtDailyDC.equalsForTabs(aComponent))
			return txtDailyDMC.componentForTabs();
		if (txtDailyDMC.equalsForTabs(aComponent))
			return txtDailyFFMC.componentForTabs();
		if (txtDailyFFMC.equalsForTabs(aComponent))
			return comboHourlyMethod;

		if (txtFbpAspect.equalsForTabs(aComponent))
			return txtFbpSlope.componentForTabs();
		if (txtFbpSlope.equalsForTabs(aComponent))
			return chckFbpSlope;
		if (aComponent == chckFbpSlope)
			return txtFbpElevation.componentForTabs();
		if (txtFbpElevation.equalsForTabs(aComponent)) {
			switch (comboFbpFuelType.getSelectedIndex()) {
			case 5:
				return txtFbpCrownBaseHeight.componentForTabs();
			case 9:
			case 10:
				return txtFbpPercentConifer.componentForTabs();
			case 11:
			case 12:
				return txtFbpPercentDeadFir.componentForTabs();
			case 13:
			case 14:
			//case 15: O1ab
				return txtFbpGrassFuelLoad.componentForTabs();
			default:
				return btnFbpInformation;
			}
		}
		if (txtFbpCrownBaseHeight.equalsForTabs(aComponent))
			return btnFbpInformation;
		if (txtFbpPercentConifer.equalsForTabs(aComponent))
			return btnFbpInformation;
		if (txtFbpPercentDeadFir.equalsForTabs(aComponent))
			return btnFbpInformation;
		if (txtFbpGrassFuelLoad.equalsForTabs(aComponent))
			return txtFbpGrassCuring.componentForTabs();
		if (txtFbpGrassCuring.equalsForTabs(aComponent))
			return btnFbpInformation;
		if (aComponent == btnFbpInformation)
			return comboFbpFuelType;
		if (aComponent == comboFbpFuelType)
			return txtFbpAspect.componentForTabs();

		return null;
	}
	
	@Override
	public void settingsUpdated() {
		pauseCalculations();
		if (Main.unitSystem() == UnitSystem.METRIC) {
			String m = Main.resourceManager.getString("ui.label.units.m");
			String cbh = lblFbpCrownBaseHeightUnit.getText();
			if (!cbh.equals(m)) {
				String temp = txtFbpCrownBaseHeight.getText();
				Double val = ca.redapp.util.DoubleEx.valueOf(temp);
				if (val != null) {
					val = Convert.convertUnit(val, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(UnitSystem.IMPERIAL));
					txtFbpCrownBaseHeight.setText(DecimalUtils.format(val, DataType.FORCE_ATMOST_2));
				}
				temp = txtFbpGrassFuelLoad.getText();
				val = ca.redapp.util.DoubleEx.valueOf(temp);
				if (val != null) {
					val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(UnitSystem.IMPERIAL));
					txtFbpGrassFuelLoad.setText(DecimalUtils.format(val, DataType.FORCE_2));
				}
				temp = txtFbpElevation.getText();
				val = ca.redapp.util.DoubleEx.valueOf(temp);
				if (val != null) {
					val = Convert.convertUnit(val, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(UnitSystem.IMPERIAL));
					txtFbpElevation.setText(DecimalUtils.format(val, DataType.FORCE_ATMOST_2));
				}
			}
			lblFbpCrownBaseHeightUnit.setText(m);
			lblFbpGrassFuelLoadUnits.setText(Main.resourceManager.getString("ui.label.units.kgm2"));
			lblFbpElevationUnits.setText(Main.resourceManager.getString("ui.label.units.m"));
		}
		else {
			String ft = Main.resourceManager.getString("ui.label.units.ft");
			String cbh = lblFbpCrownBaseHeightUnit.getText();
			if (!cbh.equals(ft)) {
				String temp = txtFbpCrownBaseHeight.getText();
				Double val = ca.redapp.util.DoubleEx.valueOf(temp);
				if (val != null) {
					val = Convert.convertUnit(val, UnitSystem.distanceMedium(UnitSystem.IMPERIAL), UnitSystem.distanceMedium(UnitSystem.METRIC));
					txtFbpCrownBaseHeight.setText(DecimalUtils.format(val, DataType.FORCE_ATMOST_2));
				}
				temp = txtFbpGrassFuelLoad.getText();
				val = ca.redapp.util.DoubleEx.valueOf(temp);
				if (val != null) {
					val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(UnitSystem.IMPERIAL), UnitSystem.fuelConsumpiton(UnitSystem.METRIC));
					txtFbpGrassFuelLoad.setText(DecimalUtils.format(val, DataType.FORCE_2));
				}
				temp = txtFbpElevation.getText();
				val = ca.redapp.util.DoubleEx.valueOf(temp);
				if (val != null) {
					val = Convert.convertUnit(val, UnitSystem.distanceMedium(UnitSystem.IMPERIAL), UnitSystem.distanceMedium(UnitSystem.METRIC));
					txtFbpElevation.setText(DecimalUtils.format(val, DataType.FORCE_ATMOST_2));
				}
			}
			lblFbpCrownBaseHeightUnit.setText(ft);
			lblFbpGrassFuelLoadUnits.setText(Main.resourceManager.getString("ui.label.units.tonsperacre"));
			lblFbpElevationUnits.setText(Main.resourceManager.getString("ui.label.units.ft"));
		}

		unpauseCalculations();
	}

	@Override
	public Component getComponentAfter(Container aContainer, Component aComponent) {
		if (txtDailyFFMC.equalsForTabs(aComponent))
			return txtDailyDMC.componentForTabs();
		if (txtDailyDMC.equalsForTabs(aComponent))
			return txtDailyDC.componentForTabs();
		if (txtDailyDC.equalsForTabs(aComponent))
			return txtPrecipitation.componentForTabs();
		if (txtPrecipitation.equalsForTabs(aComponent)) {
			if (txtHourlyFFMC.isEnabled())
				return txtHourlyFFMC.componentForTabs();
			return comboHourlyMethod;
		}
		if (txtHourlyFFMC.equalsForTabs(aComponent))
			return cmb_hourlyStart;
		if (aComponent == cmb_hourlyStart)
			return comboHourlyMethod;
		if (aComponent == comboHourlyMethod)
			return txtDailyFFMC.componentForTabs();

		if (aComponent == comboFbpFuelType)
			return btnFbpInformation;
		if (aComponent == btnFbpInformation) {
			switch (comboFbpFuelType.getSelectedIndex()) {
			case 5:
				return txtFbpCrownBaseHeight.componentForTabs();
			case 9:
			case 10:
				return txtFbpPercentConifer.componentForTabs();
			case 11:
			case 12:
				return txtFbpPercentDeadFir.componentForTabs();
			case 13:
			case 14:
			//case 15: O1ab
				return txtFbpGrassCuring.componentForTabs();
			default:
				return txtFbpElevation.componentForTabs();
			}
		}
		if (txtFbpCrownBaseHeight.equalsForTabs(aComponent))
			return txtFbpElevation.componentForTabs();
		if (txtFbpPercentConifer.equalsForTabs(aComponent))
			return txtFbpElevation.componentForTabs();
		if (txtFbpPercentDeadFir.equalsForTabs(aComponent))
			return txtFbpElevation.componentForTabs();
		if (txtFbpGrassCuring.equalsForTabs(aComponent))
			return txtFbpGrassFuelLoad.componentForTabs();
		if (txtFbpGrassFuelLoad.equalsForTabs(aComponent))
			return txtFbpElevation.componentForTabs();
		if (txtFbpElevation.equalsForTabs(aComponent))
			return chckFbpSlope;
		if (aComponent == chckFbpSlope)
			return txtFbpSlope.componentForTabs();
		if (txtFbpSlope.equalsForTabs(aComponent))
			return txtFbpAspect.componentForTabs();
		if (txtFbpAspect.equalsForTabs(aComponent))
			return comboFbpFuelType;

		return null;
	}
}
