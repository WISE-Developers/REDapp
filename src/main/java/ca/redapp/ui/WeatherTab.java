/***********************************************************************
 * REDapp - WeatherTab.java
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

import static ca.hss.math.General.COMPASS_TO_CARTESIAN_DEGREE;
import static ca.hss.math.General.DEGREE_TO_RADIAN;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import ca.hss.text.StringExtensions;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import ca.wise.grid.IWXData;
import ca.hss.general.DecimalUtils;
import ca.hss.general.OutVariable;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import ca.hss.times.TimeZoneInfo;
import ca.redapp.data.ForecastModel;
import ca.redapp.ui.StatsTab.StatsTabListener;
import ca.redapp.ui.component.HeaderRenderer;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RComboBox;
import ca.redapp.ui.component.RGroupBox;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;
import ca.redapp.ui.component.SpringUtilities;
import ca.redapp.util.LineEditHelper;
import ca.redapp.util.RFileChooser;
import ca.weather.acheron.Calculator;
import ca.weather.acheron.Day;
import ca.weather.acheron.Hour;
import ca.weather.acheron.LocationWeather;
import ca.weather.acheron.Calculator.LocationSmall;
import ca.weather.current.CurrentWeather;
import ca.weather.current.Cities.*;
import ca.weather.forecast.InvalidXMLException;
import ca.weather.forecast.Model;
import ca.weather.forecast.Province;
import ca.weather.forecast.Time;

public class WeatherTab extends REDappTab implements ChangeListener, StatsTabListener {
	private static final long serialVersionUID = 1L;
	private Main app;
	public List<LocationSmall> locationData;
	protected List<String> canadianCities;
	protected TabUpdateWorker worker = null;
	protected Thread workerThread = null;
	protected BusyDialog busyDialog = null;
	protected Semaphore tabUpdateSem = new Semaphore(1);
	protected Model model = Model.GEM_DETER;
	private boolean initialized = false;
	private ForecastModel forecastModel;
	private DefaultTableModel forecastHeaderModel;
	
	private int memberList = -1;

	public static final String NOCITY = "NOCITY";
	String city = NOCITY;

	Calculator calculator;
	private CurrentWeather currentWeather;

	public WeatherTab(Main app) {
		this.app = app;
		initialize();
		initTabOrder();
		canadianCities = new ArrayList<String>();
		calculator = new Calculator();
		calculator.setPercentile(50);
		btnWeatherCurrentTransferToFWI.setEnabled(false);
		btnTransferToStats.setEnabled(false);
		btnWeatherHourlyTransferToFWI.setEnabled(false);
		btnWeatherExport.setEnabled(false);

		btnWeatherCurrentTransferToFWI.addActionListener((e) -> transferCurrentWxToFWI());
		btnWeatherHourlyTransferToFWI.addActionListener((e) -> transferForecastWxToFWI());
		btnWeatherExport.addActionListener((e) -> exportWeatherData());

		rdbtnWeatherGEMD.addActionListener((e) -> {
			model = Model.GEM_DETER;
			memberList = -1;
			comboWeatherCustom.setEnabled(rdbtnWeatherGEM.isSelected() || rdbtnWeatherNCEP.isSelected());
			populateWeatherCustom();
			tabUpdate();
		});
		rdbtnWeatherGEM.addActionListener((e) -> {
			model = Model.CUSTOM;
			
			comboWeatherCustom.setEnabled(rdbtnWeatherGEM.isSelected() || rdbtnWeatherNCEP.isSelected());
			populateWeatherCustom();
			tabUpdate();
		});
		rdbtnWeatherNCEP.addActionListener((e) -> {
			model = Model.CUSTOM;
			
			comboWeatherCustom.setEnabled(rdbtnWeatherGEM.isSelected() || rdbtnWeatherNCEP.isSelected());
			populateWeatherCustom();
			tabUpdate();
		});
		
		rdbtnWeather00Z.addActionListener((e) -> {
			calculator.setTime(Time.MIDNIGHT);
			comboWeatherCustom.setEnabled(rdbtnWeatherGEM.isSelected() || rdbtnWeatherNCEP.isSelected());
			populateWeatherCustom();
			tabUpdate();
		});
		rdbtnWeather12Z.addActionListener((e) -> {
			calculator.setTime(Time.NOON);
			comboWeatherCustom.setEnabled(rdbtnWeatherGEM.isSelected() || rdbtnWeatherNCEP.isSelected());
			populateWeatherCustom();
			tabUpdate();
		});

		comboWeatherCustom.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				try {
					String sel = comboWeatherCustom.getSelectedItem().toString();
	
					memberList = Integer.parseInt(sel);
					tabUpdate();
				} catch (Exception ex) {}
			}
		});
	}
	
	private void populateWeatherCustom() {
		if(comboWeatherCustom.isEnabled()) {
			comboWeatherCustom.removeAllItems();
			
			if(rdbtnWeatherGEM.isSelected()) {
				for (int i = 1; i <= 21; i++)
					comboWeatherCustom.addItem(i);
			}
			else if(rdbtnWeatherNCEP.isSelected()) {
				for (int i = 23; i <= 43; i++)
					comboWeatherCustom.addItem(i);
			}
		}
	}

	public void weatherProvinceSelected(int i) {
		List<String> list = getListOfCitiesForecastWx(comboWeatherCurrentCity
				.getSelectedIndex());
		comboWeatherCurrentCity.removeAllItems();
		String[] arr = new String[list.size()];
		comboWeatherCurrentCity.setModel(new DefaultComboBoxModel<String>(list.toArray(arr)));
	}

	void filterForCanadianCities() {
		// discard US and Mexico data for now
		if (locationData == null)
			return;
		Iterator<LocationSmall> it = locationData.iterator();
		while (it.hasNext()) {
			LocationSmall place = it.next();
			int charMarker = place.locationName.length();
			char[] placeChar = place.locationName.toCharArray();
			if (placeChar[--charMarker] == 'A'
					|| placeChar[--charMarker] == 'a') {
				if (placeChar[--charMarker] == 'C'
						|| placeChar[--charMarker] == 'c') {
					
					canadianCities.add(StringExtensions.capitalizeFully(place.locationName.substring(0,--charMarker)));
				}
			}
		}
	}

	private List<String> getCitiesFromProvince(String prov) {
		ArrayList<String> list = new ArrayList<String>();
		Iterator<String> it = canadianCities.iterator();
		while (it.hasNext()) {
			String place = it.next();
			String provinceID = place.substring(place.length() - 2,
					place.length());
			if (provinceID.compareToIgnoreCase(prov) == 0)
				list.add(place.substring(0, place.length() - 3));
		}
		return list;
	}

	List<String> getListOfCitiesForecastWx(int i) {
		if (i < 0)
			return new ArrayList<String>();
		List<String> list;
		switch (Province.values()[i]) {
		case ALBERTA:
			list = getCitiesFromProvince("AB");
			break;
		case ONTARIO:
			list = getCitiesFromProvince("ON");
			break;
		case BRITISH_COLUMBIA:
			list = getCitiesFromProvince("BC");
			break;
		case MANITOBA:
			list = getCitiesFromProvince("MB");
			break;
		case NEW_BRUNSWICK:
			list = getCitiesFromProvince("NB");
			break;
		case NEWFOUNDLAND_AND_LABRADOR:
			list = getCitiesFromProvince("NL");
			break;
		case NORTHWEST_TERRITORIES:
			list = getCitiesFromProvince("NT");
			break;
		case NOVA_SCOTIA:
			list = getCitiesFromProvince("NS");
			break;
		case NUNAVUT:
			list = getCitiesFromProvince("NU");
			break;
		case PRINCE_EDWARD_ISLAND:
			list = getCitiesFromProvince("PE");
			break;
		case QUEBEC:
			list = getCitiesFromProvince("QC");
			break;
		case SASKATCHEWAN:
			list = getCitiesFromProvince("SK");
			break;
		case YUKON:
			list = getCitiesFromProvince("YT");
			break;
		default:
			list = new ArrayList<String>();
			break;
		}
		return list;
	}

	List<String> getListOfCitiesObservedWx(int i) {
		if (i < 0)
			return new ArrayList<String>();
		List<String> list;
		switch (Province.values()[i]) {
		case ALBERTA:
			list = AlbertaCities.valuesAsStrings();
			break;
		case ONTARIO:
			list = OntarioCities.valuesAsStrings();
			break;
		case BRITISH_COLUMBIA:
			list = BritishColumbiaCities.valuesAsStrings();
			break;
		case MANITOBA:
			list = ManitobaCities.valuesAsStrings();
			break;
		case NEW_BRUNSWICK:
			list = NewBrunswickCities.valuesAsStrings();
			break;
		case NEWFOUNDLAND_AND_LABRADOR:
			list = NewfoundLandLabradorCities.valuesAsStrings();
			break;
		case NORTHWEST_TERRITORIES:
			list = NorthwestTerritoriesCities.valuesAsStrings();
			break;
		case NOVA_SCOTIA:
			list = NovaScotiaCities.valuesAsStrings();
			break;
		case NUNAVUT:
			list = NunavutCities.valuesAsStrings();
			break;
		case PRINCE_EDWARD_ISLAND:
			list = PrinceEdwardIslandCities.valuesAsStrings();
			break;
		case QUEBEC:
			list = QuebecCities.valuesAsStrings();
			break;
		case SASKATCHEWAN:
			list = SaskatchewanCities.valuesAsStrings();
			break;
		default:
			list = YukonTerritoryCities.valuesAsStrings();
			break;
		}
		return list;
	}

	@SuppressWarnings("deprecation")
	private Date getDate() {
		if (chkbxOverrideDate.isSelected()) {
			Date d = ((SpinnerDateModel)spinnerDate.getModel()).getDate();
			d.setHours(0);
			d.setMinutes(0);
			d.setSeconds(0);
			return d;
		}
		return app.getDate();
	}

	public void citySelectedForecast() {
		if (comboWeatherForecastCity.getSelectedIndex() == -1)
			return;
		city = comboWeatherForecastCity.getSelectedItem().toString().toUpperCase();
		if (city.isEmpty()) {
			forecastModel.clearData();
			btnWeatherCurrentTransferToFWI.setEnabled(false);
			btnTransferToStats.setEnabled(false);
			btnWeatherExport.setEnabled(false);
			return;
		}
		Pattern cityPattern = Pattern.compile(city);
		for (LocationSmall loc : locationData)
			if (cityPattern.matcher(loc.locationName).find())
				city = loc.locationName;
		calculator.setLocation(city);
		tabUpdate();

		if (app.statsTab.canTransferTo()) {
			btnTransferToStats.setEnabled(true);
			Date dt = getDate();
			Calendar cal = new GregorianCalendar();
			cal.setTime(dt);
		}
		btnWeatherExport.setEnabled(true);
		btnWeatherHourlyTransferToFWI.setEnabled(true);
	}

	/**
	 * if anything in the tab is updated this method will re-read the values,
	 * update all the fields and save it to the directory
	 */
	@SuppressWarnings("deprecation")
	private void tabUpdate() {
		if (!tabUpdateSem.tryAcquire())
			return;
		dt = getDate();
		dt.setHours(0);
		dt.setMinutes(0);
		dt.setSeconds(0);
		inf = app.getSelectedTimeZone();

		if (city.equalsIgnoreCase(NOCITY)) {
			tabUpdateSem.release();
			return; // don't bother updating until a city has been selected
		}
		if (model == Model.CUSTOM && memberList == -1) {
			tabUpdateSem.release();
			return;
		}
		worker = new TabUpdateWorker();
		worker.date = getDate();
		worker.info = app.getSelectedTimeZone();
		if (model == Model.CUSTOM) {
			worker.members = new ArrayList<Integer>();
			
			if(memberList != -1)
				worker.members.add(memberList);
		}
		workerThread = new Thread(worker);
		busyDialog = new BusyDialog(app.frmRedapp);
		workerThread.start();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				busyDialog.setModal(true);
				busyDialog.setVisible(true);
			}
		});
	}

	protected int minHour = 0;
	protected int maxHour = 23;
	protected int minDay = 1;

	protected class TabUpdateWorker implements Runnable {
		public Day day = null;
		public boolean error = false;
		public Date date;
		public TimeZoneInfo info;
		public boolean ignorePrecipitation = false;
		public List<Integer> members;

		@Override
		public void run() {
			if (city.equalsIgnoreCase(NOCITY))
				return; // don't bother updating until a city has been selected
			calculator.setModel(model);
			if (model == Model.CUSTOM) {
				calculator.clearMembers();
				for (int i : members)
					calculator.addMember(i);
			}
			calculator.setBasePath(Main.prefs.getString("ensemble", ""));
			calculator.setTimezone(info);
			calculator.setIgnorePrecipitation(ignorePrecipitation);
			Calendar cal = calculator.getDate();
			cal.setTime(date);
			// cal.set(date.getYear(), date.getMonth() - 1, date.getDay());
			if (!calculator.calculate()) {
				// System.out.println("Bad input values in Calculate.calculate");
				error = true;
				tabUpdateFinished();
				return;
			}
			else if (calculator.isDataError()) {
	            JOptionPane.showMessageDialog(null,
	                    Main.resourceManager.getString("ui.label.weather.error.invalid"), "Warning",
	                    JOptionPane.WARNING_MESSAGE);
			}
			minHour = (int)(6 + info.getTimezoneOffset().getTotalHours() + info.getDSTAmount().getTotalHours());
			if (minHour < 0)
				minHour = 0;
			maxHour = (int)(23 - info.getTimezoneOffset().getTotalHours() - info.getDSTAmount().getTotalHours());
			if (maxHour > 23)
				maxHour = 23;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					tabUpdateFinished();
				}
			});
		}
	}

	private void tabUpdateFinished() {
		if (worker.error) {
			JOptionPane.showMessageDialog(null,
					Main.resourceManager.getString("ui.label.weather.error.fetch"), "Error",
					JOptionPane.WARNING_MESSAGE);
			btnTransferToStats.setEnabled(false);
			btnWeatherExport.setEnabled(false);
			btnWeatherHourlyTransferToFWI.setEnabled(false);
		}
		else {
			forecastFieldUpdate();
			btnWeatherExport.setEnabled(true);
			btnWeatherHourlyTransferToFWI.setEnabled(true);
			if (app.statsTab.canTransferTo())
				btnTransferToStats.setEnabled(true);
		}
		comboWeatherForecastDay.setValue(1);
		LocationWeather loc = calculator.getLocationsWeatherData().iterator().next();
		((SpinnerNumberModel)comboWeatherForecastDay.getModel()).setMaximum(loc.getDayCount());
		worker = null;
		workerThread = null;
		busyDialog.setVisible(false);
		busyDialog = null;
		tabUpdateSem.release();
	}

	// this updates the fields without going back to the server to get the
	// weather. Used when changing
	// the percentile, forecast day, and hourly time
	public void forecastFieldUpdate() {
		if (city.equalsIgnoreCase(NOCITY))
			return; // don't bother updating until a city has been selected
		calculator.setModel(model);
		calculator.setTimezone(app.getSelectedTimeZone());
		LocationWeather loc = calculator.getLocationsWeatherData().iterator()
				.next();

		// get the hourly observations
		Iterator<Hour> hourIterator = loc.getHourData().iterator();

		forecastModel.clearData();
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate());
		int index = (Integer)comboWeatherForecastDay.getValue() - 1;
		cal.add(Calendar.DATE, index);
		cal.set(Calendar.MINUTE, 0);
		Hour hour;
		int start = 0;
		List<Hour> hours = new ArrayList<Hour>();
		if (hourIterator.hasNext()) {
			hour = hourIterator.next();
			boolean found = false;
			int offset = (int)(app.getSelectedTimeZone().getTimezoneOffset().getHours() + app.getSelectedTimeZone().getDSTAmount().getHours());
			while (true) {
				Calendar cal2 = (Calendar)hour.getCalendarDate().clone();
				if (cal2.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
						cal2.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)) {
					int h = hour.getHour() - offset;
					while (h < 0)
						h += 24;
					while (h > 23)
						h -= 24;
					if (h == 0 || h == 6 || h == 12 || h == 18) {
						found = true;
						hours.add(hour);
					}
				}
				else if (found)
					break;
				hour = hourIterator.next();
			}

			if (hours.size() < 4) {
				start = 4 - hours.size();
				for (int i = 0; i < 4 - hours.size(); i++) {
					forecastHeaderModel.setValueAt("", i, 0);
				}
			}
			SimpleDateFormat formatter;
			offset = 0;
			formatter = new SimpleDateFormat("MMM dd yyyy HH:mm");
			for (int i = start, j = 0; i < 4; i++, j++) {
				hour = hours.get(j);
				cal = (Calendar)hour.getCalendarDate().clone();
				cal.add(Calendar.HOUR_OF_DAY, -offset);
				forecastHeaderModel.setValueAt(formatter.format(cal.getTime()), i, 0);
				double temp = hour.getTemperature();
				double rh = hour.getRelativeHumidity();
				double precip = hour.getPrecipitation();
				double ws = hour.getWindSpeed();
				double wd = hour.getWindDirection();
				forecastModel.setDataAt(i, temp, rh, precip, ws, wd);
			}
		}
		forecastHeaderModel.fireTableDataChanged();
	}

	@SuppressWarnings("deprecation")
	public void citySelectedObserved() {
		if (comboWeatherCurrentCity.getSelectedIndex() == -1)
			return;
		Cities[] cities = CitiesHelper
				.getCities(Province.values()[comboWeatherCurrentProvince
						.getSelectedIndex()]);
		String dropBoxCity = comboWeatherCurrentCity.getSelectedItem()
				.toString();
		if (dropBoxCity.isEmpty()) {
			txtWeatherCurrentTemp.setText("");
			txtWeatherCurrentTime.setText("");
			txtWeatherCurrentTime.setToolTipText("");
			txtWeatherCurrentRelHumidity.setText("");
			txtWeatherCurrentWindSpeed.setText("");
			txtWeatherCurrentWindDirection.setText("");
			btnWeatherCurrentTransferToFWI.setEnabled(false);
			return;
		}
		Cities chosenCity = null;
		for (Cities city : cities) {
			if (city.getName().equalsIgnoreCase(dropBoxCity)) {
				chosenCity = city;
				break;
			}
		}
		if (chosenCity == null)
			throw new RuntimeException(
					"Internal error in WeatherDataTab.citySelectedObservedWx");

		try {
			currentWeather = new CurrentWeather(chosenCity, Main.prefs.getString("current", ""));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			String notReported = Main.resourceManager.getString("ui.label.weather.error.notreported");
			Double temp = currentWeather.getTemperature();
			if (temp == null)
				txtWeatherCurrentTemp.setText(notReported);
			else {
				double t = Convert.convertUnit(temp, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
				txtWeatherCurrentTemp.setText(DecimalUtils.format(t, DecimalUtils.DataType.TEMPERATURE));
			}
			String wxTime = null;
			Date wxDateTime = currentWeather.getObservedDateTime();
			if (wxDateTime == null) {
				wxTime = currentWeather.getObserved() + " LT";
			}
			else {
				Calendar c = Calendar.getInstance();
				c.setTime(wxDateTime);
				int offset = wxDateTime.getTimezoneOffset();
				offset += app.getSelectedTimeZone().getTimezoneOffset().getTotalMinutes() + app.getSelectedTimeZone().getDSTAmount().getTotalMinutes();
				c.add(Calendar.MINUTE, offset);
				SimpleDateFormat sdf = new SimpleDateFormat("h:mm, d MMM yyyy");
				wxDateTime = c.getTime();
				wxTime = sdf.format(wxDateTime);
			}
			txtWeatherCurrentTime.setUserData("DateTime", wxDateTime);
			if (wxTime == null)
				wxTime = notReported;
			txtWeatherCurrentTime.setText(wxTime);
			txtWeatherCurrentTime.setToolTipText(wxTime);
			Double humidity = currentWeather.getHumidity();
			if (humidity == null)
				txtWeatherCurrentRelHumidity.setText(notReported);
			else
				txtWeatherCurrentRelHumidity.setText(DecimalUtils.format(humidity, DecimalUtils.DataType.RH));
			Double speed = currentWeather.getWindSpeed();
			if (speed == null)
				txtWeatherCurrentWindSpeed.setText(notReported);
			else {
				double s = Convert.convertUnit(speed, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
				txtWeatherCurrentWindSpeed.setText(DecimalUtils.format(s, DecimalUtils.DataType.WIND_SPEED));
			}
			Double wdir = currentWeather.getWindDirectionAngle();
			if (wdir == null)
				txtWeatherCurrentWindDirection.setText(notReported);
			else
				txtWeatherCurrentWindDirection.setText(DecimalUtils.format(wdir, DecimalUtils.DataType.WIND_DIR));
			//panelWeatherCurrent.setImage(currentWeather.getConditionImage());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvalidXMLException e) {
			e.printStackTrace();
		}
		btnWeatherCurrentTransferToFWI.setEnabled(true);
	}

	public void canTransferUpdated(boolean b) {
		if (b) {
			if (btnWeatherExport.isEnabled()) {
				btnTransferToStats.setEnabled(true);
			}
			else {
				btnTransferToStats.setEnabled(false);
			}
		}
		else {
			btnTransferToStats.setEnabled(false);
		}
	}

	public void transferForecastWxToFBP() {
		int row = forecastTable.getSelectedRow();
		if (row < 0)
			return;
		OutVariable<Double> temp = new OutVariable<Double>();
		OutVariable<Double> rh = new OutVariable<Double>();
		OutVariable<Double> precip = new OutVariable<Double>();
		OutVariable<Double> ws = new OutVariable<Double>();
		OutVariable<Double> wd = new OutVariable<Double>();
		forecastModel.getDataAt(row, temp, rh, precip, ws, wd);
		String ws2 = "", wd2 = "";
		if (ws.value != null)
			ws2 = ws.toString();
		if (wd.value != null)
			wd2 = wd.toString();
		app.fbpTab.transferValues(null, null, null, null, null, ws2, wd2, null);
		app.setCurrentTab(app.fbpTab);
	}

	public void transferCurrentWxToFWI() {
		Double hourlyTemp = LineEditHelper.getDoubleFromLineEditNoError(txtWeatherCurrentTemp);
		Double hourlyRH = LineEditHelper.getDoubleFromLineEditNoError(txtWeatherCurrentRelHumidity);
		Double hourlyWS = LineEditHelper.getDoubleFromLineEditNoError(txtWeatherCurrentWindSpeed);
		Integer hour;
		String date = txtWeatherCurrentTime.getText();
		String[] list = date.split(":");
		if (list == null || list.length < 1)
			hour = null;
		else {
			try {
				hour = Integer.parseInt(list[0]);
			} catch (Exception e){
				String tmpHour = list[0];
				tmpHour = tmpHour.substring(tmpHour.lastIndexOf(" ") + 1);
				hour = Integer.parseInt(tmpHour);
			}
		}
		app.fwiTab.transferHourlyData(hourlyTemp, hourlyWS, hourlyRH, null, hour);
		app.setCurrentTab(app.fwiTab);
	}

	public void transferForecastWxToFWI() {
		transferForecastWxToFWINoon();
		transferForecastWxToFWIHour();
		app.setCurrentTab(app.fwiTab);
	}

	private void transferForecastWxToFWINoon() {
		int r = forecastModel.getRowCount();
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy HH:mm");
		if (r < 1)
			return;
		String date = "";
		for (int i = 0; i < r; i++) {
			String temp = (String)forecastHeaderModel.getValueAt(i, 0);
			if (temp.length() > date.length())
				date = temp;
		}
		if (date.length() < 1)
			return;
		int month = 1, day = 1, year = 2015;
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(formatter.parse(date));
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DAY_OF_MONTH);
			year = c.get(Calendar.YEAR);
		} catch (ParseException e) {
		}
		LocationWeather loc = calculator.getLocationsWeatherData().iterator().next();
		Iterator<Hour> hourIterator = loc.getHourData().iterator();
		Hour hour = hourIterator.next();
		List<Double> precips = new ArrayList<Double>();
		do {
			Calendar c = (Calendar)hour.getCalendarDate().clone();
			precips.add(hour.getPrecipitation());
			if (c.get(Calendar.YEAR) == year && c.get(Calendar.DAY_OF_MONTH) == day && c.get(Calendar.MONTH) == month && c.get(Calendar.HOUR_OF_DAY) == 12) {
				double precip = 0;
				for (int i = precips.size() - 1, j = 0; i >= 0 && j < 24; i--, j++) {
					precip += precips.get(i);
				}
				app.fwiTab.transferNoonData(hour.getTemperature(), precip, hour.getRelativeHumidity(), hour.getWindSpeed());
				break;
			}
			hour = hourIterator.next();
		} while (hourIterator.hasNext());
	}

	private void transferForecastWxToFWIHour() {
		int row = forecastTable.getSelectedRow();
		if (row < 0)
			return;
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy HH:mm");
		OutVariable<Double> temp = new OutVariable<Double>();
		OutVariable<Double> rh = new OutVariable<Double>();
		OutVariable<Double> precip = new OutVariable<Double>();
		OutVariable<Double> ws = new OutVariable<Double>();
		OutVariable<Double> wd = new OutVariable<Double>();
		forecastModel.getDataAt(row, temp, rh, precip, ws, wd);
		int hour = row * 6;
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(formatter.parse((String)forecastHeaderModel.getValueAt(row, 0)));
			hour = c.get(Calendar.HOUR_OF_DAY);
		} catch (ParseException e) {
		}
		while (hour < 0)
			hour += 24;
		while (hour > 23)
			hour -= 24;
		app.fwiTab.transferHourlyData(temp.value, ws.value, rh.value, precip.value, hour);
	}

	public void transferEnsembleToStats() {
		app.statsTab.pauseCalculations();
		LocationWeather loc = calculator.getLocationsWeatherData().iterator().next();
		Iterator<Hour> hourIterator = loc.getHourData().iterator();
		Hour hour = hourIterator.next();
		Calendar c2 = (Calendar)hour.getCalendarDate().clone();
		c2.setTimeZone(TimeZone.getTimeZone("UTC"));
		IWXData[] hours = new IWXData[24];
		int currentHour = 0;
		int starthour = hour.getCalendarDate().get(Calendar.HOUR_OF_DAY);
		while (starthour != currentHour) {
			hours[currentHour] = new IWXData();
			hours[currentHour].temperature = 0.0;
			hours[currentHour].rh = 0.0;
			hours[currentHour].precipitation = 0.0;
			hours[currentHour].windSpeed = 0.0;
			hours[currentHour].windDirection = DEGREE_TO_RADIAN(COMPASS_TO_CARTESIAN_DEGREE(0.0));
			hours[currentHour].dewPointTemperature = -300.0;
			currentHour++;
		}
		int count = 0;
		do {
			Calendar c = (Calendar)hour.getCalendarDate().clone();
			hours[currentHour] = new IWXData();
			hours[currentHour].temperature = hour.getTemperature();
			hours[currentHour].rh = hour.getRelativeHumidity() / 100.0;
			hours[currentHour].precipitation = hour.getPrecipitation();
			hours[currentHour].windSpeed = hour.getWindSpeed();
			hours[currentHour].windDirection = DEGREE_TO_RADIAN(COMPASS_TO_CARTESIAN_DEGREE(hour.getWindDirection()));
			hours[currentHour].dewPointTemperature = -300.0;
			if (hour.isError())
			    hours[currentHour].specifiedBits |= IWXData.SPECIFIED.INVALID_DATA;
			currentHour++;
			count++;
			if (currentHour == 24) {
				currentHour = 0;
				if (count == 24) {
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					try {
						app.statsTab.addDayHourlyWeather(c, hours);
					} catch (IllegalArgumentException e) { }
				}
				count = 0;
			}
			hour = hourIterator.next();
		} while (hourIterator.hasNext());
		app.setCurrentTab(app.statsTab);
		app.statsTab.unpauseCalculations();
	}

	public void transferForecastWxToStats() {
		int index = forecastTable.getSelectedRow();
		if (index < 0)
			return;
		Calendar c = new GregorianCalendar();
		c.setTime(getDate());
		c.add(Calendar.DATE, (Integer)comboWeatherForecastDay.getValue() - 1);
		OutVariable<Double> temp = new OutVariable<Double>();
		OutVariable<Double> rh = new OutVariable<Double>();
		OutVariable<Double> precip = new OutVariable<Double>();
		OutVariable<Double> ws = new OutVariable<Double>();
		OutVariable<Double> wd = new OutVariable<Double>();
		forecastModel.getDataAt(0, temp, rh, precip, ws, wd);
		c.add(Calendar.HOUR_OF_DAY, index * 6);
		try {
			app.statsTab.updateHourlyWeather(c, temp.value, rh.value, precip.value, ws.value, wd.value);
		} catch (IllegalArgumentException e) {
		}
		app.setCurrentTab(app.statsTab);
	}

	public void exportWeatherData() {
		RFileChooser fc = RFileChooser.directoryPicker();
		String dir = Main.prefs.getString("EXPORT_WEATHER_DIR", System.getProperty("user.home"));
		fc.setCurrentDirectory(dir);
		fc.setTitle(Main.resourceManager.getString("ui.label.weather.ensemble.export.title"));
		int retVal = fc.showDialog(app.frmRedapp);
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			Main.prefs.putString("EXPORT_WEATHER_DIR", file.getAbsolutePath());
			
			Model mdl = Model.CUSTOM;
			if(rdbtnWeatherGEM.isSelected()) mdl = Model.GEM;
			else if(rdbtnWeatherGEMD.isSelected()) mdl = Model.GEM_DETER;
			else if(rdbtnWeatherNCEP.isSelected()) mdl = Model.NCEP;
			
			calculator.saveHourlyWeather(file.getAbsolutePath(), mdl);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (app.getCurrentTab() == WeatherTab.this)
			tabUpdate();
	}

	private void copyForecastData() {
		int[] cols = forecastTable.getSelectedColumns();
		int[] rows = forecastTable.getSelectedRows();
		if (cols.length == 0 || rows.length == 0)
			return;
		String tocopy = "";
		for (int i = 0; i < rows.length; i++) {
			if (cols.length == 5) {
				tocopy += forecastHeaderTable.getValueAt(rows[i], 0);
				tocopy += "\t";
			}
			for (int j = 0; j < cols.length; j++) {
				String val = forecastTable.getValueAt(rows[i], cols[j]).toString();
				tocopy += val;
				if (j < cols.length - 1) {
					tocopy += "\t";
				}
			}
			if (i < rows.length - 1) {
				tocopy += "\n";
			}
		}
		Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection sel = new StringSelection(tocopy);
		board.setContents(sel, sel);
	}

	// {{ UI Code

	private RTextField txtWeatherCurrentTime;
	private RTextField txtWeatherCurrentTemp;
	private RTextField txtWeatherCurrentRelHumidity;
	private RTextField txtWeatherCurrentWindSpeed;
	private RTextField txtWeatherCurrentWindDirection;
	private RButton btnWeatherExport;
	private RButton btnWeatherCurrentTransferToFWI;
	private RButton btnWeatherHourlyTransferToFWI;
	private RButton btnTransferToStats;
	private RButton btnReset;
	private RComboBox<String> comboWeatherForecastCity;
	private RComboBox<Province> comboWeatherForecastProvince;
	private RComboBox<Province> comboWeatherCurrentProvince;
	private RComboBox<String> comboWeatherCurrentCity;
	private JSpinner comboWeatherForecastDay;
	
	//Redmine 810
	//private JSpinner spinnerPercentile;
	
	private JSpinner spinnerDate;
	private RComboBox<Integer> comboWeatherCustom;
	
	//Redmine 810
	//private JRadioButton rdbtnWeatherCustom;
	
	private JRadioButton rdbtnWeatherNCEP;
	private JRadioButton rdbtnWeatherGEM;
	private JRadioButton rdbtnWeatherGEMD;
	private JRadioButton rdbtnWeather00Z;
	private JRadioButton rdbtnWeather12Z;
	private JCheckBox chkbxOverrideDate;
	private JTable forecastTable;
	private JTable forecastHeaderTable;
	private RLabel lblWeatherCurrentTempUnit;
	private RLabel lblWeatherCurrentWindSpeedUnit;
	private RGroupBox panelWeatherCurrent;

	protected void initialize() {
		if (initialized)
			return;
		initialized = true;

		setLayout(null);
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 1060, 448);
		else
			setBounds(0, 0, 1070, 453);

		if (Main.isWindows())
			setBackground(Color.white);

		panelWeatherCurrent = new RGroupBox();
		panelWeatherCurrent.setText(Main.resourceManager.getString("ui.label.weather.current.title"));
		if (Main.isMac())
			panelWeatherCurrent.setBounds(5, 10, 283, 327);
		else
			panelWeatherCurrent.setBounds(5, 10, 283, 330);
		add(panelWeatherCurrent);

		JPanel panel = new JPanel();
		panel.setLayout(new SpringLayout());
		if (Main.isMac())
			panel.setBounds(10, 20, 253, 200);
		else
			panel.setBounds(10, 20, 263, 200);
		panelWeatherCurrent.add(panel);
		panel.setBackground(new Color(245, 245, 245));

		RLabel lblWeatherCurrentProvince = new RLabel(Main.resourceManager.getString("ui.label.weather.current.province"));
		panel.add(lblWeatherCurrentProvince);

		comboWeatherCurrentProvince = new RComboBox<Province>();
		panel.add(comboWeatherCurrentProvince);
		comboWeatherCurrentProvince.setModel(new DefaultComboBoxModel<Province>(Province.values()));
		comboWeatherCurrentProvince.setWide(true);
		comboWeatherCurrentProvince.setSelectedIndex(-1);
		comboWeatherCurrentProvince.addActionListener((e) -> {
			List<String> list = getListOfCitiesObservedWx(comboWeatherCurrentProvince
					.getSelectedIndex());
			comboWeatherCurrentCity.removeAllItems();
			String[] arr = new String[list.size()];
			comboWeatherCurrentCity.setModel(new DefaultComboBoxModel<String>(list.toArray(arr)));
			comboWeatherCurrentCity.setWide(true);
			comboWeatherCurrentCity.setSelectedIndex(-1);
			panelWeatherCurrent.setImage(null);
		});

		RLabel lblWeatherCurrentCity = new RLabel(Main.resourceManager.getString("ui.label.weather.current.city"));
		panel.add(lblWeatherCurrentCity);

		comboWeatherCurrentCity = new RComboBox<String>();
		comboWeatherCurrentCity.addActionListener((e) -> citySelectedObserved());
		panel.add(comboWeatherCurrentCity);

		RLabel lblWeatherCurrentTime = new RLabel(Main.resourceManager.getString("ui.label.weather.current.obstime"));
		panel.add(lblWeatherCurrentTime);

		txtWeatherCurrentTime = new RTextField();
		txtWeatherCurrentTime.setEditable(false);
		panel.add(txtWeatherCurrentTime);
		txtWeatherCurrentTime.setColumns(10);

		RLabel lblWeatherCurrentTemp = new RLabel(Main.resourceManager.getString("ui.label.weather.temp"));
		panel.add(lblWeatherCurrentTemp);

		JPanel panel2 = new JPanel();
		panel2.setLayout(new SpringLayout());
		panel2.setBackground(new Color(245, 245, 245));
		panel.add(panel2);

		txtWeatherCurrentTemp = new RTextField();
		txtWeatherCurrentTemp.setEditable(false);
		panel2.add(txtWeatherCurrentTemp);
		txtWeatherCurrentTemp.setColumns(10);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblWeatherCurrentTempUnit = new RLabel(Main.resourceManager.getString("ui.label.units.celsius"));
		else
			lblWeatherCurrentTempUnit = new RLabel(Main.resourceManager.getString("ui.label.units.fahrenheit"));
		lblWeatherCurrentTempUnit.setPreferredSize(new Dimension(40, 20));
		panel2.add(lblWeatherCurrentTempUnit);

		SpringUtilities.makeCompactGrid(panel2, 1, 2, 0, 0, 5, 0);

		RLabel lblWeatherCurrentRelHumidity = new RLabel(Main.resourceManager.getString("ui.label.weather.rh"));
		panel.add(lblWeatherCurrentRelHumidity);

		panel2 = new JPanel();
		panel2.setLayout(new SpringLayout());
		panel2.setBackground(new Color(245, 245, 245));
		panel.add(panel2);

		txtWeatherCurrentRelHumidity = new RTextField();
		txtWeatherCurrentRelHumidity.setEditable(false);
		panel2.add(txtWeatherCurrentRelHumidity);
		txtWeatherCurrentRelHumidity.setColumns(10);

		RLabel lblWeatherCurrentRelHumidityUnit = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		lblWeatherCurrentRelHumidityUnit.setPreferredSize(new Dimension(40, 20));
		panel2.add(lblWeatherCurrentRelHumidityUnit);

		SpringUtilities.makeCompactGrid(panel2, 1, 2, 0, 0, 5, 0);

		RLabel lblWeatherCurrentWindSpeed = new RLabel(Main.resourceManager.getString("ui.label.weather.ws"));
		panel.add(lblWeatherCurrentWindSpeed);

		panel2 = new JPanel();
		panel2.setLayout(new SpringLayout());
		panel2.setBackground(new Color(245, 245, 245));
		panel.add(panel2);

		txtWeatherCurrentWindSpeed = new RTextField();
		txtWeatherCurrentWindSpeed.setEditable(false);
		panel2.add(txtWeatherCurrentWindSpeed);
		txtWeatherCurrentWindSpeed.setColumns(10);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblWeatherCurrentWindSpeedUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblWeatherCurrentWindSpeedUnit = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		lblWeatherCurrentWindSpeedUnit.setPreferredSize(new Dimension(40, 20));
		panel2.add(lblWeatherCurrentWindSpeedUnit);

		SpringUtilities.makeCompactGrid(panel2, 1, 2, 0, 0, 5, 0);

		RLabel lblWeatherCurrentWindDirection = new RLabel(Main.resourceManager.getString("ui.label.weather.wd"));
		panel.add(lblWeatherCurrentWindDirection);

		panel2 = new JPanel();
		panel2.setLayout(new SpringLayout());
		panel2.setBackground(new Color(245, 245, 245));
		panel.add(panel2);

		txtWeatherCurrentWindDirection = new RTextField();
		txtWeatherCurrentWindDirection.setEditable(false);
		panel2.add(txtWeatherCurrentWindDirection);
		txtWeatherCurrentWindDirection.setColumns(10);

		RLabel lblWeatherCurrentWindDirectionUnit = new RLabel(Main.resourceManager.getString("ui.label.units.degrees"));
		lblWeatherCurrentWindDirectionUnit.setPreferredSize(new Dimension(40, 20));
		panel2.add(lblWeatherCurrentWindDirectionUnit);

		SpringUtilities.makeCompactGrid(panel2, 1, 2, 0, 0, 5, 0);
		
		/*
		if (Launcher.javaVersion.major < 9)
			SpringUtilities.makeCompactGrid(panel, 7, 2, 0, 0, 20, 10);
		else*/
			SpringUtilities.makeCompactGrid(panel, 7, 2, 0, 0, 20, 9);

		btnWeatherCurrentTransferToFWI = new RButton(Main.resourceManager.getString("ui.label.weather.current.tofwi"), RButton.Decoration.Arrow);
		Dimension d = btnWeatherCurrentTransferToFWI.getSize();
		btnWeatherCurrentTransferToFWI.setBounds(140 - (int)(((double)d.width)/ 2.0), 260, 121, 41);
		panelWeatherCurrent.add(btnWeatherCurrentTransferToFWI);

		RGroupBox panelWeatherEnsemble = new RGroupBox();
		panelWeatherEnsemble.setText(Main.resourceManager.getString("ui.label.weather.ensemble.title"));
		if (Main.isMac())
			panelWeatherEnsemble.setBounds(295, 10, 661, 327);
		else
			panelWeatherEnsemble.setBounds(295, 10, 661, 330);
		add(panelWeatherEnsemble);

		chkbxOverrideDate = new JCheckBox();
		chkbxOverrideDate.setBounds(358, 20, 191, 22);
		chkbxOverrideDate.setText(Main.resourceManager.getString("ui.label.weather.ensemble.overridedate"));
		chkbxOverrideDate.setBackground(new Color(245, 245, 245));
		if (Main.isLinux())
			chkbxOverrideDate.setFont(chkbxOverrideDate.getFont().deriveFont(12.0f));
		chkbxOverrideDate.addActionListener((e) -> {
			Main.prefs.putBoolean("weather_override_date", chkbxOverrideDate.isSelected());
			spinnerDate.setEnabled(chkbxOverrideDate.isSelected());
			if (dt != null && getDate().toString().compareTo(dt.toString()) != 0) {
				tabUpdate();
			}
			if (!chkbxOverrideDate.isSelected()) {
				spinnerDate.setValue(app.getDate());
			}
		});
		panelWeatherEnsemble.add(chkbxOverrideDate);

		spinnerDate = new JSpinner();
		spinnerDate.setLocale(Main.resourceManager.loc);
		spinnerDate.setModel(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_YEAR));
		spinnerDate.setEditor(new JSpinner.DateEditor(spinnerDate, "MMMM d, yyyy"));
		spinnerDate.setBounds(358, 48, 191, 22);
		spinnerDate.addChangeListener((x) -> dateChanged() );
		if (Main.isLinux()) {
			JComponent comp = spinnerDate.getEditor();
			if (comp instanceof JSpinner.DefaultEditor) {
				JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)comp;
				editor.getTextField().setFont(editor.getTextField().getFont().deriveFont(11.0f));
			}
		}
		panelWeatherEnsemble.add(spinnerDate);

//		//TODO working on a better date picker.
//		{
//			org.jdatepicker.impl.JDatePickerImpl dpicker = (org.jdatepicker.impl.JDatePickerImpl)new org.jdatepicker.JDateComponentFactory(null, null, Main.resourceManager.loc).createJDatePicker();
//			dpicker.setBounds(358, 148, 200, 22);
//			panelWeatherEnsemble.add(dpicker);
//		}

		boolean checked = Main.prefs.getBoolean("weather_override_date", false);
		chkbxOverrideDate.setSelected(checked);
		spinnerDate.setEnabled(checked);

		RLabel lblWeatherForecastProvince = new RLabel(Main.resourceManager.getString("ui.label.weather.forecast.province"));
		lblWeatherForecastProvince.setBounds(20, 21, 91, 20);
		lblWeatherForecastProvince.setHorizontalAlignment(SwingConstants.RIGHT);
		panelWeatherEnsemble.add(lblWeatherForecastProvince);

		RLabel lblWeatherForecastCity = new RLabel(Main.resourceManager.getString("ui.label.weather.forecast.city"));
		lblWeatherForecastCity.setBounds(20, 49, 91, 20);
		lblWeatherForecastCity.setHorizontalAlignment(SwingConstants.RIGHT);
		panelWeatherEnsemble.add(lblWeatherForecastCity);

		comboWeatherForecastProvince = new RComboBox<Province>();
		comboWeatherForecastProvince.setBounds(121, 20, 191, 22);
		panelWeatherEnsemble.add(comboWeatherForecastProvince);
		comboWeatherForecastProvince.setModel(new DefaultComboBoxModel<Province>(Province.values()));
		comboWeatherForecastProvince.setSelectedIndex(-1);

		comboWeatherForecastCity = new RComboBox<String>();
		comboWeatherForecastCity.addActionListener((e) -> citySelectedForecast());
		comboWeatherForecastCity.setBounds(121, 48, 191, 22);
		panelWeatherEnsemble.add(comboWeatherForecastCity);

		comboWeatherForecastProvince.addActionListener((e) -> {
			List<String> list = getListOfCitiesForecastWx(comboWeatherForecastProvince
								.getSelectedIndex());
			comboWeatherForecastCity.removeAllItems();
			String[] arr = new String[list.size()];
			comboWeatherForecastCity.setModel(new DefaultComboBoxModel<String>(list.toArray(arr)));
			comboWeatherForecastCity.setSelectedIndex(-1);
		});

		RLabel lblWeatherPercentile = new RLabel(Main.resourceManager.getString("ui.label.weather.ensemble.percentile"));
		lblWeatherPercentile.setBounds(357, 76, 125, 20);
		panelWeatherEnsemble.add(lblWeatherPercentile);

		/*Redmine 810
		spinnerPercentile = new JSpinner(new SpinnerNumberModel(50, 1, 99, 1));
		spinnerPercentile.setBounds(350, 95, 71, 23);
		spinnerPercentile.setEnabled(false);
		JComponent comp = spinnerPercentile.getEditor();

		JFormattedTextField field = (JFormattedTextField)comp.getComponent(0);
		if (Main.isLinux()) {
			if (comp instanceof JSpinner.DefaultEditor) {
				JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)comp;
				editor.getTextField().setFont(editor.getTextField().getFont().deriveFont(11.0f));
			}
		}
		
		DefaultFormatter formatter = (DefaultFormatter)field.getFormatter();
		formatter.setCommitsOnValidEdit(true);
		spinnerPercentile.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				percentileTimer.cancel();
				percentileTimer = new Timer();
				percentileTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						synchronized (percentileTimer) {
							WeatherTab.this.calculator.setPercentile((Integer)spinnerPercentile.getValue());
							tabUpdate();
						}
					}
				}, 1000);
			}
		});
		panelWeatherEnsemble.add(spinnerPercentile);
		*/

		JPanel panel1 = new JPanel();
		panel1.setLayout(new FlowLayout(FlowLayout.RIGHT));
		panel1.setBounds(0, 160, 661, 50);
		panel1.setBackground(new Color(0, 0, 0, 0));
		panelWeatherEnsemble.add(panel1);

		RLabel lblWeatherModel = new RLabel(Main.resourceManager.getString("ui.label.weather.ensemble.model"));
		lblWeatherModel.setBounds(20, 76, 221, 20);
		panelWeatherEnsemble.add(lblWeatherModel);

		ButtonGroup btnGroupWeatherModel = new ButtonGroup();

		rdbtnWeatherGEMD = new JRadioButton(Main.resourceManager.getString("ui.label.weather.ensemble.gemd"));
		if (Main.isLinux())
			rdbtnWeatherGEMD.setFont(rdbtnWeatherGEMD.getFont().deriveFont(12.0f));
		rdbtnWeatherGEMD.setBackground(new Color(245, 245, 245));
		rdbtnWeatherGEMD.setSelected(true);
		btnGroupWeatherModel.add(rdbtnWeatherGEMD);
		rdbtnWeatherGEMD.setBounds(20, 96, 150, 20);
		panelWeatherEnsemble.add(rdbtnWeatherGEMD);

		rdbtnWeatherGEM = new JRadioButton(Main.resourceManager.getString("ui.label.weather.ensemble.gem"));
		if (Main.isLinux())
			rdbtnWeatherGEM.setFont(rdbtnWeatherGEM.getFont().deriveFont(12.0f));
		rdbtnWeatherGEM.setBackground(new Color(245, 245, 245));
		rdbtnWeatherGEM.setSelected(true);
		btnGroupWeatherModel.add(rdbtnWeatherGEM);
		rdbtnWeatherGEM.setBounds(20, 116, 137, 20);
		panelWeatherEnsemble.add(rdbtnWeatherGEM);

		rdbtnWeatherNCEP = new JRadioButton(Main.resourceManager.getString("ui.label.weather.ensemble.ncep"));
		if (Main.isLinux())
			rdbtnWeatherNCEP.setFont(rdbtnWeatherNCEP.getFont().deriveFont(12.0f));
		rdbtnWeatherNCEP.setBackground(new Color(245, 245, 245));
		btnGroupWeatherModel.add(rdbtnWeatherNCEP);
		rdbtnWeatherNCEP.setBounds(20, 136, 137, 21);
		panelWeatherEnsemble.add(rdbtnWeatherNCEP);

		/*Redmine 810
		rdbtnWeatherCustom = new JRadioButton(Main.resourceManager.getString("ui.label.weather.ensemble.custom"));
		if (Main.isLinux())
			rdbtnWeatherCustom.setFont(rdbtnWeatherCustom.getFont().deriveFont(12.0f));
		rdbtnWeatherCustom.setBackground(new Color(245, 245, 245));
		btnGroupWeatherModel.add(rdbtnWeatherCustom);
		rdbtnWeatherCustom.setBounds(20, 156, 137, 21);
		panelWeatherEnsemble.add(rdbtnWeatherCustom);
		*/

		comboWeatherCustom = new RComboBox<Integer>();
		for (int i = 1; i <= 43; i++)
			comboWeatherCustom.addItem(i);

		if (Launcher.mac.isMac())
			comboWeatherCustom.setBounds(357, 96, 121, 22);
		else
			comboWeatherCustom.setBounds(357, 96, 101, 22);

		comboWeatherCustom.setEnabled(false);
		panelWeatherEnsemble.add(comboWeatherCustom);

		ButtonGroup btnGroupWeatherHour = new ButtonGroup();

		RLabel lblForecastTime = new RLabel(Main.resourceManager.getString("ui.label.weather.ensemble.forecasttime"));
		lblForecastTime.setBounds(190, 76, 250, 20);
		panelWeatherEnsemble.add(lblForecastTime);

		rdbtnWeather00Z = new JRadioButton(Main.resourceManager.getString("ui.label.weather.ensemble.zero"));
		if (Main.isLinux())
			rdbtnWeather00Z.setFont(rdbtnWeather00Z.getFont().deriveFont(12.0f));
		rdbtnWeather00Z.setBackground(new Color(245, 245, 245));
		rdbtnWeather00Z.setSelected(true);
		btnGroupWeatherHour.add(rdbtnWeather00Z);
		rdbtnWeather00Z.setBounds(190, 96, 71, 21);
		panelWeatherEnsemble.add(rdbtnWeather00Z);

		rdbtnWeather12Z = new JRadioButton(Main.resourceManager.getString("ui.label.weather.ensemble.noon"));
		if (Main.isLinux())
			rdbtnWeather12Z.setFont(rdbtnWeather12Z.getFont().deriveFont(12.0f));
		rdbtnWeather12Z.setBackground(new Color(245, 245, 245));
		btnGroupWeatherHour.add(rdbtnWeather12Z);
		rdbtnWeather12Z.setBounds(190, 116, 71, 21);
		panelWeatherEnsemble.add(rdbtnWeather12Z);

		comboWeatherForecastDay = new JSpinner(new SpinnerNumberModel(1, 1, 16, 1));
		comboWeatherForecastDay.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				forecastFieldUpdate();
			}
		});
		comboWeatherForecastDay.setBounds(486, 95, 71, 23);
		if (Main.isLinux()) {
			JComponent comp2 = comboWeatherForecastDay.getEditor();
			if (comp2 instanceof JSpinner.DefaultEditor) {
				JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)comp2;
				editor.getTextField().setFont(editor.getTextField().getFont().deriveFont(11.0f));
			}
		}
		panelWeatherEnsemble.add(comboWeatherForecastDay);

		RLabel lblForecastDay = new RLabel(Main.resourceManager.getString("ui.label.weather.ensemble.day"));
		lblForecastDay.setBounds(486, 76, 160, 20);
		panelWeatherEnsemble.add(lblForecastDay);

		forecastModel = new ForecastModel();
		forecastTable = new JTable(forecastModel);
		forecastTable.getTableHeader().setDefaultRenderer(new HeaderRenderer());
		forecastTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		forecastTable.setShowGrid(true);
		forecastTable.setCellSelectionEnabled(true);
		forecastTable.setGridColor(Color.black);
		Font f = new Font("Arial", Font.BOLD, 12);
		forecastTable.getTableHeader().setFont(f);
		forecastTable.setPreferredScrollableViewportSize(forecastTable.getPreferredSize());
		forecastTable.getTableHeader().setReorderingAllowed(false);
		if (Main.isLinux())
			forecastTable.setRowHeight(24);
		else if (Main.isMac())
			forecastTable.setRowHeight(25);
		else
			forecastTable.setRowHeight(23);

		KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
		forecastTable.registerKeyboardAction((e) -> copyForecastData(), "Copy", copy, JComponent.WHEN_FOCUSED);
		forecastTable.addMouseListener(new MouseAdapter() {
			boolean contains(int[] values, int value) {
				for (int i = 0; i < values.length; i++) {
					if (values[i] == value)
						return true;
				}
				return false;
			}

			@Override
			public void mousePressed(MouseEvent me) {
				if (SwingUtilities.isLeftMouseButton(me))
					forecastHeaderTable.clearSelection();
				else if (SwingUtilities.isRightMouseButton(me) && btnWeatherHourlyTransferToFWI.isEnabled() && forecastTable.getSelectedRows().length > 0) {
					int overrow = forecastTable.rowAtPoint(me.getPoint());
					int overcol = forecastTable.columnAtPoint(me.getPoint());
					if (contains(forecastTable.getSelectedRows(), overrow) && contains(forecastTable.getSelectedColumns(), overcol)) {
						JPopupMenu menu = new JPopupMenu();
						JMenuItem item = new JMenuItem(Main.resourceManager.getString("ui.label.editor.copy"));
						item.addActionListener((e) -> copyForecastData());
						menu.add(item);
						menu.show(forecastTable, me.getX(), me.getY());
					}
				}
			}
		});

		forecastHeaderModel = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		forecastHeaderModel.addColumn(Main.resourceManager.getString("ui.label.stats.datetimeheader"), new Object[] { "0:00", "6:00", "12:00", "18:00" });
		forecastHeaderTable = new JTable(forecastHeaderModel);
		forecastHeaderTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		forecastHeaderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		forecastHeaderTable.setCellSelectionEnabled(true);
		forecastHeaderTable.setPreferredScrollableViewportSize(new Dimension(135, 0));
		forecastHeaderTable.getColumnModel().getColumn(0).setPreferredWidth(135);
		if (Main.isLinux())
			forecastHeaderTable.setRowHeight(24);
		else if (Main.isMac())
			forecastHeaderTable.setRowHeight(25);
		else
			forecastHeaderTable.setRowHeight(23);
		TableCellRenderer rend = new TableCellRenderer() {
			DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer)(new JTable()).getTableHeader().getDefaultRenderer();
			DefaultTableCellRenderer cellRenderer = (DefaultTableCellRenderer)(new JTable()).getDefaultRenderer(Object.class);
			Font font = new Font("Arial", Font.PLAIN, 12);

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				Component com;
				String str = (String)value;
				boolean selected = table.getSelectionModel().isSelectedIndex(row);
				if (selected) {
					com = cellRenderer.getTableCellRendererComponent(table, str, selected, hasFocus, row, column);
					((JLabel)com).setBorder(new EmptyBorder(0, 5, 0, 0));
					if (font != null)
						com.setFont(font.deriveFont(Font.BOLD));
					else
						com.setFont(com.getFont().deriveFont(Font.BOLD));
				}
				else {
					headerRenderer.setHorizontalAlignment(JLabel.CENTER);
					com = headerRenderer.getTableCellRendererComponent(table, str, selected, hasFocus, row, column);
					if (font != null)
						com.setFont(font.deriveFont(Font.PLAIN));
					else
						com.setFont(com.getFont().deriveFont(Font.PLAIN));
				}
				if (com instanceof JLabel)
					((JLabel)com).setHorizontalAlignment(SwingConstants.LEFT);
				return com;
			}
		};
		forecastHeaderTable.getColumnModel().getColumn(0).setCellRenderer(rend);
		forecastHeaderTable.addMouseListener(new MouseAdapter() {
			boolean contains(int[] values, int value) {
				for (int i = 0; i < values.length; i++) {
					if (values[i] == value)
						return true;
				}
				return false;
			}

			@Override
			public void mousePressed(MouseEvent me) {
				if (SwingUtilities.isRightMouseButton(me) && btnWeatherHourlyTransferToFWI.isEnabled() && forecastTable.getSelectedRows().length > 0) {
					int overrow = forecastTable.rowAtPoint(me.getPoint());
					if (contains(forecastTable.getSelectedRows(), overrow)) {
						JPopupMenu menu = new JPopupMenu();
						JMenuItem item = new JMenuItem(Main.resourceManager.getString("ui.label.editor.copy"));
						item.addActionListener((e) -> copyForecastData());
						menu.add(item);
						menu.show(forecastHeaderTable, me.getX(), me.getY());
					}
				}
				else if (SwingUtilities.isLeftMouseButton(me)) {
					JTable table = (JTable)me.getSource();
					Point p = me.getPoint();
					int row = table.rowAtPoint(p);
					forecastTable.setColumnSelectionInterval(0, forecastTable.getColumnCount() - 1);
					forecastTable.setRowSelectionInterval(row, row);
				}
			}
		});

		JTableHeader corner = forecastHeaderTable.getTableHeader();
		corner.setReorderingAllowed(false);
		corner.setResizingAllowed(false);
		((DefaultTableCellRenderer)corner.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		corner.setFont(f);

		JScrollPane pane = new JScrollPane(forecastTable);
		if (Main.isMac())
			pane.setBounds(3, 207, 655, 117);
		else
			pane.setBounds(3, 207, 655, 120);
		pane.setRowHeaderView(forecastHeaderTable);
		pane.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		//pane.setBorder(BorderFactory.createLineBorder(new Color(0xa8, 0x45, 0x45), 1));
		pane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.black));
		panelWeatherEnsemble.add(pane);

		panel1 = new JPanel();
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

		btnWeatherHourlyTransferToFWI = new RButton(Main.resourceManager.getString("ui.label.weather.hourly.tofwi"), RButton.Decoration.Arrow);
		panel1.add(btnWeatherHourlyTransferToFWI);

		btnTransferToStats = new RButton(Main.resourceManager.getString("ui.label.weather.ensemble.tostats"), RButton.Decoration.Arrow);
		btnTransferToStats.addActionListener((e) -> transferEnsembleToStats());
		panel1.add(btnTransferToStats);

		btnWeatherExport = new RButton(Main.resourceManager.getString("ui.label.weather.ensemble.export"));
		panel1.add(btnWeatherExport);

		btnReset = new RButton(Main.resourceManager.getString("ui.label.footer.reset"));
		btnReset.addActionListener((e) -> reset());
		panel1.add(btnReset);
	}

	private void initTabOrder() {
		tabOrder.clear();
		tabOrder.add(comboWeatherCurrentProvince);
		tabOrder.add(comboWeatherCurrentCity);
		tabOrder.add(comboWeatherForecastProvince);
		tabOrder.add(comboWeatherForecastCity);
		tabOrder.add(rdbtnWeatherGEMD);
		tabOrder.add(rdbtnWeatherGEM);
		tabOrder.add(rdbtnWeatherNCEP);
		
		//Redmine 810
		//tabOrder.add(rdbtnWeatherCustom);
		
		tabOrder.add(comboWeatherCustom);
		tabOrder.add(rdbtnWeather00Z);
		tabOrder.add(rdbtnWeather12Z);
		tabOrder.add(comboWeatherForecastDay);
		
		//Redmine 810
		//tabOrder.add(spinnerPercentile);
	}

	// }}

	@Override
	public void setInternetConnected(boolean conn) { }

	@Override
	public void reset() {
		comboWeatherCurrentCity.setSelectedIndex(-1);
		comboWeatherCurrentProvince.setSelectedIndex(-1);
		txtWeatherCurrentTime.setText("");
		txtWeatherCurrentTime.setToolTipText("");
		txtWeatherCurrentTemp.setText("");
		txtWeatherCurrentRelHumidity.setText("");
		txtWeatherCurrentWindSpeed.setText("");
		txtWeatherCurrentWindDirection.setText("");
		comboWeatherForecastCity.setSelectedIndex(-1);
		comboWeatherForecastProvince.setSelectedIndex(-1);
		comboWeatherCustom.setSelectedIndex(0);
		comboWeatherForecastDay.setValue(1);
		rdbtnWeatherGEMD.setSelected(true);
		model = Model.GEM_DETER;
		rdbtnWeather00Z.setSelected(true);
		calculator.setTime(Time.MIDNIGHT);
		
		//Redmine 810
		//spinnerPercentile.setValue(50);
		
		calculator.setPercentile(50);
		forecastModel.clearData();
		btnTransferToStats.setEnabled(false);
		btnWeatherCurrentTransferToFWI.setEnabled(false);
		btnWeatherExport.setEnabled(false);
		btnWeatherHourlyTransferToFWI.setEnabled(false);
		
		//Redmine 810
		//spinnerPercentile.setEnabled(false);
		
		city = NOCITY;
	}

	@Override
	public boolean supportsReset() {
		return true;
	}

	public void onLocationChanged() {}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onTimeZoneChanged() {
		if (app.getCurrentTab() == WeatherTab.this)
			tabUpdate();
		Date wxDateTime = (Date)txtWeatherCurrentTime.getUserData("DateTime", null);
		if (wxDateTime != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(wxDateTime);
			int offset = wxDateTime.getTimezoneOffset();
			offset += app.getSelectedTimeZone().getTimezoneOffset().getTotalMinutes() + app.getSelectedTimeZone().getDSTAmount().getTotalMinutes();
			c.add(Calendar.MINUTE, offset);
			SimpleDateFormat sdf = new SimpleDateFormat("h:mm, d MMM yyyy");
			wxDateTime = c.getTime();
			txtWeatherCurrentTime.setText(sdf.format(wxDateTime));
			txtWeatherCurrentTime.setToolTipText(sdf.format(wxDateTime));
		}
	}
	
	@Override
	public void settingsUpdated() {
		if (Main.unitSystem() == UnitSystem.METRIC) {
			lblWeatherCurrentTempUnit.setText(Main.resourceManager.getString("ui.label.units.celsius"));
			lblWeatherCurrentWindSpeedUnit.setText(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		}
		else {
			lblWeatherCurrentTempUnit.setText(Main.resourceManager.getString("ui.label.units.fahrenheit"));
			lblWeatherCurrentWindSpeedUnit.setText(Main.resourceManager.getString("ui.label.units.milesperhour"));
		}
		citySelectedObserved();
	}

	@Override
	public void onDateChanged() {
		if (app.getCurrentTab() == WeatherTab.this && (dt != null && app.getDate().toString().compareTo(dt.toString()) != 0) && !chkbxOverrideDate.isSelected())
			tabUpdate();
	}

	public void dateChanged() {
		if (dt != null && getDate().toString().compareTo(dt.toString()) == 0)
			return;
		tabUpdate();
	}

	private Date dt = null;
	private TimeZoneInfo inf = null;

	@Override
	public void onCurrentTabChanged() {
		if (app.getCurrentTab() == WeatherTab.this) {
			if (chkbxOverrideDate.isSelected()) {
				return;
			}
			else if ((dt != null && app.getDate().compareTo(dt) == 0) && (inf != null && app.getSelectedTimeZone().compareTo(inf) == 0)) {
				return;
			}
			tabUpdate();
		}
	}
}

