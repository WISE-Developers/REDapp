/***********************************************************************
 * REDapp - FwiTab.java
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

import static ca.redapp.util.LineEditHelper.getDoubleFromLineEdit;
import static ca.redapp.util.LineEditHelper.lineEditHandleError;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpringLayout;
import javax.swing.SpringLayout.Constraints;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;

import ca.cwfgm.fwi.FWICalculations;
import ca.hss.general.DecimalUtils;
import ca.hss.general.OutVariable;
import ca.hss.general.DecimalUtils.DataType;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import ca.hss.times.TimeZoneInfo;
import ca.hss.times.WTime;
import ca.hss.times.WTimeManager;
import ca.hss.times.WTimeSpan;
import ca.hss.times.WorldLocation;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RComboBox;
import ca.redapp.ui.component.RGroupBox;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;
import ca.redapp.ui.component.SpringUtilities;
import ca.redapp.util.*;

public class FwiTab extends REDappTab implements DocumentListener {
	private static final long serialVersionUID = 1L;
	FWICalculations fwiCalculations = new FWICalculations();
	private boolean initialized = false;
	private Main app;
	private boolean calcLawson = false;
	
	public static OutVariable<WTime> rise = new OutVariable<>();
	public static OutVariable<WTime> set = new OutVariable<>();
	public static OutVariable<WTime> noon = new OutVariable<>();
	
	private double prvFfmc = -1;
	private boolean fwiCalc = false;

	public FwiTab(Main app) {
		this.app = app;
		initialize();
		initTabOrder();
		btnFwiTransferToFBP.setEnabled(false);
		toggleHourly();
		reset();

		txtFwiNoonTemp.getDocument().addDocumentListener(this);
		txtFwiNoonRelHumidity.getDocument().addDocumentListener(this);
		txtFwiNoonPrecip.getDocument().addDocumentListener(this);
		txtFwiNoonWindSpeed.getDocument().addDocumentListener(this);
		txtFwiHourlyTemp.getDocument().addDocumentListener(this);
		txtFwiHourlyRelHumidity.getDocument().addDocumentListener(this);
		txtFwiHourlyPrecip.getDocument().addDocumentListener(this);
		txtFwiHourlyWindSpeed.getDocument().addDocumentListener(this);
		
		txtFwiHourlyPrevHourFFMC.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class,
				this, "txtFwiHourlyPrevHourFFMCChanged"));
		
		txtFwiYstrdyFFMC.getDocument().addDocumentListener(this);
		txtFwiYstrdyDMC.getDocument().addDocumentListener(this);
		txtFwiYstrdyDC.getDocument().addDocumentListener(this);
		comboFwiHourlyMethod.addActionListener((e) -> comboFwiMethodChanged());
		chckbxFwiHourlyCalculate.addActionListener((e) -> clearOutputValuesOnForm());
		//hack to get the spinner to raise a change event when the values are
		//changed manually (without the up/down arrows).
		JComponent comp = spinnerFwiHourlyTime.getEditor();
	    JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
	    DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
	    formatter.setCommitsOnValidEdit(true);
		spinnerFwiHourlyTime.addChangeListener((e) -> hourlyTimeChanged());

		btnFwiTransferToFBP.addActionListener((e) -> transferToFBP());
		btnFwiExport.addActionListener((e) -> export());
	}
	
	public void txtFwiHourlyPrevHourFFMCChanged() {
		String temp = txtFwiHourlyPrevHourFFMC.getText();
		Double val = DecimalUtils.valueOf(temp);
		if(val != null) {
			if(!fwiCalc) {
				prvFfmc = -1;
			}
			else
				prvFfmc = fwiCalculations.prvhlyFFMC;
		}
		if(!fwiCalc)
			clearOutputValuesOnForm();
	}

	@Override
	public void reset() {
		fwiCalculations = new FWICalculations();

		spinnerFwiHourlyTime.setValue(Calendar.getInstance().getTime());
		txtFwiHourlyTemp.setText(DecimalUtils.format(fwiCalculations.hrlyTemp, DecimalUtils.DataType.TEMPERATURE));
		txtFwiHourlyRelHumidity.setText(DecimalUtils.format(fwiCalculations.hrlyRH, DecimalUtils.DataType.RH));
		txtFwiHourlyPrecip.setText(DecimalUtils.format(fwiCalculations.hrlyPrecip, DecimalUtils.DataType.PRECIP));
		txtFwiHourlyWindSpeed.setText(DecimalUtils.format(fwiCalculations.hrlyWindSpeed, DecimalUtils.DataType.WIND_SPEED));
		txtFwiDailyFFMC.setText(DecimalUtils.format(fwiCalculations.dlyFFMC, DecimalUtils.DataType.FFMC));
		txtFwiDailyDMC.setText(DecimalUtils.format(fwiCalculations.dlyDMC, DecimalUtils.DataType.DMC));
		txtFwiDailyBUI.setText(DecimalUtils.format(fwiCalculations.dlyBUI, DecimalUtils.DataType.BUI));
		txtFwiDailyDC.setText(DecimalUtils.format(fwiCalculations.dlyDC, DecimalUtils.DataType.DC));
		txtFwiDailyISI.setText(DecimalUtils.format(fwiCalculations.dlyISI, DecimalUtils.DataType.ISI));
		txtFwiDailyFWI.setText(DecimalUtils.format(fwiCalculations.dlyFWI, DecimalUtils.DataType.FWI));
		txtFwiDailyDSR.setText(DecimalUtils.format(fwiCalculations.dlyDSR));
		txtFwiYstrdyFFMC.setText(DecimalUtils.format(fwiCalculations.ystrdyFFMC, DecimalUtils.DataType.FFMC));
		txtFwiYstrdyDMC.setText(DecimalUtils.format(fwiCalculations.ystrdyDMC, DecimalUtils.DataType.DMC));
		txtFwiYstrdyDC.setText(DecimalUtils.format(fwiCalculations.ystrdyDC, DecimalUtils.DataType.DC));
		txtFwiNoonTemp.setText(DecimalUtils.format(fwiCalculations.noonTemp, DecimalUtils.DataType.TEMPERATURE));
		txtFwiNoonRelHumidity.setText(DecimalUtils.format(fwiCalculations.noonRH, DecimalUtils.DataType.RH));
		txtFwiNoonPrecip.setText(DecimalUtils.format(fwiCalculations.noonPrecip, DecimalUtils.DataType.PRECIP));
		txtFwiNoonWindSpeed.setText(DecimalUtils.format(fwiCalculations.noonWindSpeed, DecimalUtils.DataType.WIND_SPEED));
		comboFwiHourlyMethod.setSelectedIndex(0);
	}

	@Override
	public boolean supportsReset() {
		return true;
	}

	public void calculate() {
		if (getValuesFromForm()) {
			clearOutputValuesOnForm();

			JOptionPane.showMessageDialog(null,
					Main.resourceManager.getString("ui.label.range.invalid"),
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
			return;
		}

		fwiCalculations.FWICalculateDailyStatisticsCOM();
		setOutputValuesOnForm();
		
		btnFwiTransferToFBP.setEnabled(true);
	}
	
	private void comboFwiMethodChanged() {
		clearOutputValuesOnForm();
		int index = comboFwiHourlyMethod.getSelectedIndex();
		boolean checked = chckbxFwiPrevHourCalculate.isSelected();
		txtFwiHourlyPrevHourFFMC.setEditable(checked);
		txtFwiHourlyPrevHourFFMC.setEnabled(index == 1 && checked);
	}

	private void clearOutputValuesOnForm() {
		if(!calcLawson) {
			txtFwiDailyFFMC.setText("");
			txtFwiDailyDC.setText("");
			txtFwiDailyDMC.setText("");
			txtFwiDailyBUI.setText("");
			txtFwiDailyISI.setText("");
			txtFwiDailyFWI.setText("");
			txtFwiDailyDSR.setText("");
			txtFwiHourlyFFMC.setText("");
			txtFwiHourlyISI.setText("");
			txtFwiHourlyFWI.setText("");
			btnFwiTransferToFBP.setEnabled(false);
		}
	}
	
	public void clearHourlyOutputValuesOnForm() {
		txtFwiHourlyFFMC.setText("");
		txtFwiHourlyISI.setText("");
		txtFwiHourlyFWI.setText("");
	}

	private boolean getValuesFromForm() {
		boolean error = false;
		boolean useHourly = chckbxFwiHourlyCalculate.isSelected();
		fwiCalculations.calcHourly = useHourly;
		Double d;

		if (useHourly) {
			if (txtFwiHourlyRelHumidity.isEditable() && txtFwiHourlyRelHumidity.isEnabled()) {
				d = getDoubleFromLineEdit(txtFwiHourlyRelHumidity);
				if (d == null)
					error = true;
				else {
					fwiCalculations.hrlyRH = d;
					if (d < 0.0 || d > 100.0) {
						error = true;
						lineEditHandleError(txtFwiHourlyRelHumidity,
								Main.resourceManager.getString("ui.label.range.rh"));
					}
				}
			}
			//Task #544
			d = getDoubleFromLineEdit(txtFwiHourlyWindSpeed);
			if (Main.unitSystem() != UnitSystem.METRIC) 
				d = Convert.convertUnit(d, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
			d = DecimalUtils.formatNumber(d, DecimalUtils.DataType.FORCE_2);
			
			if (d == null)
				error = true;
			else {
				fwiCalculations.hrlyWindSpeed = Convert.convertUnit(d, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
				if (d < 0.0 || d > 200.0) {
					error = true;
					Double min = Convert.convertUnit(0.0, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
					Double max = Convert.convertUnit(100.0, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
					String unit;
					if (Main.unitSystem() == UnitSystem.METRIC)
						unit = Main.resourceManager.getString("ui.label.units.kph");
					else
						unit = Main.resourceManager.getString("ui.label.units.mph");
					lineEditHandleError(txtFwiHourlyWindSpeed,
							Main.resourceManager.getString("ui.label.range.ws", DecimalUtils.format(min, DataType.WIND_SPEED), DecimalUtils.format(max, DataType.WIND_SPEED), unit));
				}
			}

			if (fwiCalculations.useVanWagner) {
				//Task #544
				d = getDoubleFromLineEdit(txtFwiNoonTemp);
				if (Main.unitSystem() != UnitSystem.METRIC) 
					d = Convert.convertUnit(d, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
				d = DecimalUtils.formatNumber(d, DecimalUtils.DataType.FORCE_2);
				
				if (d == null)
					error = true;
				else {
					fwiCalculations.hrlyTemp = Convert.convertUnit(d, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
					if (d < -50.0 || d > 45.0) {
						error = true;
						Double min = Convert.convertUnit(-50.0, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
						Double max = Convert.convertUnit(45.0, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
						String unit;
						if (Main.unitSystem() == UnitSystem.METRIC)
							unit = Main.resourceManager.getString("ui.label.units.celsius");
						else
							unit = Main.resourceManager.getString("ui.label.units.fahrenheit");
						lineEditHandleError(txtFwiHourlyTemp,
								Main.resourceManager.getString("ui.label.range.temp", DecimalUtils.format(min, DataType.TEMPERATURE), DecimalUtils.format(max, DataType.TEMPERATURE), unit));
					}
				}
				
				//Task #544
				d = getDoubleFromLineEdit(txtFwiHourlyPrecip);
				if (Main.unitSystem() != UnitSystem.METRIC) 
					d = Convert.convertUnit(d, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
				d = DecimalUtils.formatNumber(d, DecimalUtils.DataType.FORCE_2);
				
				if (d == null)
					error = true;
				else {
					fwiCalculations.hrlyPrecip = Convert.convertUnit(d, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
					if (d < 0.0 || d > 300.0) {
						error = true;
						Double min = Convert.convertUnit(0.0, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
						Double max = Convert.convertUnit(300.0, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
						String unit;
						if (Main.unitSystem() == UnitSystem.METRIC)
							unit = Main.resourceManager.getString("ui.label.units.mm");
						else
							unit = Main.resourceManager.getString("ui.label.units.in");
						lineEditHandleError(txtFwiHourlyPrecip,
								Main.resourceManager.getString("ui.label.range.precip", DecimalUtils.format(min, DataType.PRECIP), DecimalUtils.format(max, DataType.PRECIP), unit));
					}
				}
				fwiCalculations.useLawsonPreviousHour = !chckbxFwiPrevHourCalculate.isSelected();
				if(chckbxFwiPrevHourCalculate.isSelected() && prvFfmc == -1) {
					d = getDoubleFromLineEdit(txtFwiHourlyPrevHourFFMC);
					if (d == null)
						error = true;
					else {
						fwiCalculations.prvhlyFFMC = d;
						if (d < 1.0 || d > 101.0) {
							error = true;
							lineEditHandleError(txtFwiHourlyPrevHourFFMC,
									Main.resourceManager.getString("ui.label.range.ffmc"));
						}
					}
				}
				else {
					fwiCalculations.prvhlyFFMC = prvFfmc;
				}
			}
		}

		d = app.getLatitude();
		if (d == null)
			error = true;
		else {
			fwiCalculations.setLatitude(d);
			if (d < -90.0 || d > 90.0) {
				error = true;
			}
		}

		d = app.getLongitude();
		if (d == null)
			error = true;
		else {
			fwiCalculations.setLongitude(d);
			if (d < -180.0 || d > 180.0) {
				error = true;
			}
		}

		d = getDoubleFromLineEdit(txtFwiYstrdyFFMC);
		if (d == null)
			error = true;
		else {
			fwiCalculations.ystrdyFFMC = d;
			if (d < 1.0 || d > 101.0) {
				error = true;
				lineEditHandleError(txtFwiYstrdyFFMC,
						Main.resourceManager.getString("ui.label.range.ffmc"));
			}
		}

		d = getDoubleFromLineEdit(txtFwiYstrdyDMC);
		if (d == null)
			error = true;
		else {
			fwiCalculations.ystrdyDMC = d;
			if (d < 0.0 || d > 500.0) {
				error = true;
				lineEditHandleError(txtFwiYstrdyDMC,
						Main.resourceManager.getString("ui.label.range.dmc"));
			}
		}

		d = getDoubleFromLineEdit(txtFwiYstrdyDC);
		if (d == null)
			error = true;
		else {
			fwiCalculations.ystrdyDC = d;
			if (d < 0.0 || d > 1500.0) {
				error = true;
				lineEditHandleError(txtFwiYstrdyDC,
						Main.resourceManager.getString("ui.label.range.dc"));
			}
		}
		
		//Task #544
		d = getDoubleFromLineEdit(txtFwiNoonTemp);
		if (Main.unitSystem() != UnitSystem.METRIC) 
			d = Convert.convertUnit(d, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
		d = DecimalUtils.formatNumber(d, DecimalUtils.DataType.FORCE_2);
		
		if (d == null)
			error = true;
		else {
			fwiCalculations.noonTemp = Convert.convertUnit(d, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
			if (d < -50.0 || d > 45.0) {
				error = true;
				Double min = Convert.convertUnit(-50.0, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
				Double max = Convert.convertUnit(45.0, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
				String unit;
				if (Main.unitSystem() == UnitSystem.METRIC)
					unit = Main.resourceManager.getString("ui.label.units.celsius");
				else
					unit = Main.resourceManager.getString("ui.label.units.fahrenheit");
				lineEditHandleError(txtFwiNoonTemp,
						Main.resourceManager.getString("ui.label.range.temp", DecimalUtils.format(min, DataType.TEMPERATURE), DecimalUtils.format(max, DataType.TEMPERATURE), unit));
			}
		}

		d = getDoubleFromLineEdit(txtFwiNoonRelHumidity);
		if (d == null)
			error = true;
		else {
			fwiCalculations.noonRH = d;
			if (d < 0.0 || d > 100.0) {
				error = true;
				lineEditHandleError(txtFwiNoonRelHumidity,
						Main.resourceManager.getString("ui.label.range.rh"));
			}
		}
		
		//Task #544
		d = getDoubleFromLineEdit(txtFwiNoonPrecip);
		if (Main.unitSystem() != UnitSystem.METRIC) 
			d = Convert.convertUnit(d, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
		d = DecimalUtils.formatNumber(d, DecimalUtils.DataType.FORCE_2);
		
		if (d == null)
			error = true;
		else {
			fwiCalculations.noonPrecip = Convert.convertUnit(d, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
			if (d < 0.0 || d > 300.0) {
				error = true;
				Double min = Convert.convertUnit(0.0, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
				Double max = Convert.convertUnit(300.0, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
				String unit;
				if (Main.unitSystem() == UnitSystem.METRIC)
					unit = Main.resourceManager.getString("ui.label.units.mm");
				else
					unit = Main.resourceManager.getString("ui.label.units.in");
				lineEditHandleError(txtFwiNoonPrecip,
						Main.resourceManager.getString("ui.label.range.precip", DecimalUtils.format(min, DataType.PRECIP), DecimalUtils.format(max, DataType.PRECIP), unit));
			}
		}

		//Task #544
		d = getDoubleFromLineEdit(txtFwiNoonWindSpeed);
		if (Main.unitSystem() != UnitSystem.METRIC) 
			d = Convert.convertUnit(d, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
		d = DecimalUtils.formatNumber(d, DecimalUtils.DataType.FORCE_2);
		
		if (d == null)
			error = true;
		else {
			fwiCalculations.noonWindSpeed = Convert.convertUnit(d, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
			if (d < 0.0 || d > 100.0) {
				error = true;
				Double min = Convert.convertUnit(0.0, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
				Double max = Convert.convertUnit(100.0, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
				String unit;
				if (Main.unitSystem() == UnitSystem.METRIC)
					unit = Main.resourceManager.getString("ui.label.units.kph");
				else
					unit = Main.resourceManager.getString("ui.label.units.mph");
				lineEditHandleError(txtFwiNoonWindSpeed,
						Main.resourceManager.getString("ui.label.range.ws", DecimalUtils.format(min, DataType.WIND_SPEED), DecimalUtils.format(max, DataType.WIND_SPEED), unit));
			}
		}

		Date dt = app.getDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		fwiCalculations.m_date.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		fwiCalculations.m_date.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		fwiCalculations.m_date.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));

		TimeZoneInfo info = app.getSelectedTimeZone();
		fwiCalculations.setTimezone(info.getTimezoneOffset());
		fwiCalculations.setDST(info.getDSTAmount());

		if (fwiCalculations.calcHourly) {
			Date tm = ((SpinnerDateModel)spinnerFwiHourlyTime.getModel()).getDate();
			cal.setTime(tm);
			fwiCalculations.m_date.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
			fwiCalculations.m_date.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
			fwiCalculations.m_date.set(Calendar.SECOND, cal.get(Calendar.SECOND));
		}

		return error;
	}

	private void setOutputValuesOnForm() {
		//hourly values should be calculated, use the Van Wagner calculations for hourly values,
		//and calculate the previous hour's FFMC using the Lawson calculation
		if(chckbxFwiHourlyCalculate.isSelected() && fwiCalculations.useVanWagner &&
				!chckbxFwiPrevHourCalculate.isSelected()) {
			fwiCalc = true;
			txtFwiHourlyPrevHourFFMC.setText(DecimalUtils.format(fwiCalculations.prvhlyFFMC, DecimalUtils.DataType.FFMC));
			fwiCalc = false;
		}
		
		txtFwiDailyFFMC.setText(DecimalUtils.format(fwiCalculations.dlyFFMC, DecimalUtils.DataType.FFMC));
		txtFwiDailyDC.setText(DecimalUtils.format(fwiCalculations.dlyDC, DecimalUtils.DataType.DC));
		txtFwiDailyDMC.setText(DecimalUtils.format(fwiCalculations.dlyDMC, DecimalUtils.DataType.DMC));
		txtFwiDailyBUI.setText(DecimalUtils.format(fwiCalculations.dlyBUI, DecimalUtils.DataType.BUI));
		txtFwiDailyISI.setText(DecimalUtils.format(fwiCalculations.dlyISI, DecimalUtils.DataType.ISI));
		txtFwiDailyFWI.setText(DecimalUtils.format(fwiCalculations.dlyFWI, DecimalUtils.DataType.FWI));
		txtFwiDailyDSR.setText(DecimalUtils.format(fwiCalculations.dlyDSR));
		if (fwiCalculations.calcHourly) {
			txtFwiHourlyFFMC.setText(DecimalUtils.format(fwiCalculations.hlyHFFMC, DecimalUtils.DataType.FFMC));
			txtFwiHourlyISI.setText(DecimalUtils.format(fwiCalculations.hlyHISI, DecimalUtils.DataType.ISI));
			txtFwiHourlyFWI.setText(DecimalUtils.format(fwiCalculations.hlyHFWI, DecimalUtils.DataType.FWI));
		}
		else {
			txtFwiHourlyFFMC.setText("");
			txtFwiHourlyISI.setText("");
			txtFwiHourlyFWI.setText("");
		}
	}

	public void toggleHourly() {
		boolean checked = chckbxFwiHourlyCalculate.isSelected();
		fwiCalculations.calcHourly = checked;
		@SuppressWarnings("deprecation")
		int hour = ((SpinnerDateModel)spinnerFwiHourlyTime.getModel()).getDate().getHours();
		TimeZoneInfo info = app.getSelectedTimeZone();
		boolean lawsonUseRH;
		if (info.getDSTAmount().getTotalSeconds() > 0)
			lawsonUseRH = hour < 13 && hour >= 7;
		else
			lawsonUseRH = hour < 12 && hour >= 6;
		
		LineEditHelper.setEnabled(spinnerFwiHourlyTime, checked);
		
		lblFwiHourlyFFMC.setForeground(checked ? Color.BLACK : Color.GRAY);
		txtFwiHourlyFFMC.setEnabled(checked);
		lblFwiHourlyISI.setForeground(checked ? Color.BLACK : Color.GRAY);
		txtFwiHourlyISI.setEnabled(checked);
		lblFwiHourlyFWI.setForeground(checked ? Color.BLACK : Color.GRAY);
		txtFwiHourlyFWI.setEnabled(checked);

		lblFwiHourlyTime.setForeground(checked ? Color.BLACK : Color.GRAY);
		lblFwiHourlyWindSpeed.setForeground(checked ? Color.BLACK : Color.GRAY);
		lblFwiHourlyWindSpeedUnit.setForeground(checked ? Color.BLACK : Color.GRAY);
		
		lblFwiHourlyTemp.setForeground((checked && fwiCalculations.useVanWagner) ? Color.BLACK : Color.GRAY);
		lblFwiHourlyTempUnit.setForeground((checked && fwiCalculations.useVanWagner) ? Color.BLACK : Color.GRAY);
		LineEditHelper.setEnabled(txtFwiHourlyTemp, (checked && fwiCalculations.useVanWagner));

		String strTime = spinnerFwiHourlyTime.getValue().toString();
		strTime = strTime.substring(strTime.indexOf(':') - 2, strTime.indexOf(':'));
		int selTime = Integer.parseInt(strTime);
		boolean lawsonMorning = !(fwiCalculations.useVanWagner) && (selTime >= 6) && (selTime < 12);
		
		lblFwiHourlyRelHumidity.setForeground((checked && (fwiCalculations.useVanWagner || lawsonMorning)) ? Color.BLACK : Color.GRAY);
		lblFwiHourlyRelHumidityUnit.setForeground((checked && (fwiCalculations.useVanWagner || lawsonMorning)) ? Color.BLACK : Color.GRAY);
		LineEditHelper.setEnabled(txtFwiHourlyRelHumidity, (checked && (fwiCalculations.useVanWagner || (!fwiCalculations.useVanWagner && lawsonUseRH) || lawsonMorning)));
		
		lblFwiHourlyPrecip.setForeground((checked && fwiCalculations.useVanWagner) ? Color.BLACK : Color.GRAY);
		lblFwiHourlyPrecipUnit.setForeground((checked && fwiCalculations.useVanWagner) ? Color.BLACK : Color.GRAY);
		LineEditHelper.setEnabled(txtFwiHourlyPrecip, (checked && fwiCalculations.useVanWagner));
		
		LineEditHelper.setEnabled(txtFwiHourlyWindSpeed, checked);
		
		chckbxFwiPrevHourCalculate.setForeground((checked && fwiCalculations.useVanWagner) ? Color.BLACK : Color.GRAY);
		chckbxFwiPrevHourCalculate.setEnabled(checked && fwiCalculations.useVanWagner);
		txtFwiHourlyPrevHourFFMC.setEnabled(checked && comboFwiHourlyMethod.getSelectedIndex() == 1);

		comboFwiHourlyMethod.setEnabled(checked);
	}

	/**
	 * Set the noon weather observations.
	 */
	public void transferNoonData(double noonTemp, double noonPrecip, double noonRH, double noonWS) {
		txtFwiNoonTemp.setText(DecimalUtils.format(noonTemp, DecimalUtils.DataType.TEMPERATURE));
		txtFwiNoonRelHumidity.setText(DecimalUtils.format(noonRH, DecimalUtils.DataType.RH));
		txtFwiNoonWindSpeed.setText(DecimalUtils.format(noonWS, DecimalUtils.DataType.WIND_SPEED));
		txtFwiNoonPrecip.setText(DecimalUtils.format(noonPrecip, DecimalUtils.DataType.PRECIP));
	}

	/**
	 * Set the hourly weather observations.
	 */
	@SuppressWarnings("deprecation")
	public void transferHourlyData(Double hourlyTemp, Double hourlyWS, Double hourlyRH, Double hourlyPrecip, Integer hour) {
		if (hourlyTemp != null)
			txtFwiHourlyTemp.setText(DecimalUtils.format(hourlyTemp, DecimalUtils.DataType.TEMPERATURE));
		if (hourlyWS != null)
			txtFwiHourlyWindSpeed.setText(DecimalUtils.format(hourlyWS, DecimalUtils.DataType.WIND_SPEED));
		if (hourlyRH != null)
			txtFwiHourlyRelHumidity.setText(DecimalUtils.format(hourlyRH, DecimalUtils.DataType.RH));
		if (hourlyPrecip != null)
			txtFwiHourlyPrecip.setText(DecimalUtils.format(hourlyPrecip, DecimalUtils.DataType.PRECIP));
		if (hour != null) {
			Date d = (Date)(spinnerFwiHourlyTime.getValue());
			d.setMinutes(0);
			d.setHours(hour);
			spinnerFwiHourlyTime.setValue(d);
		}
	}

	private void setTime(RTextField field, WTime time) {
		Calendar c = Calendar.getInstance();
		c.setTime(app.getDate());
		int cyear = c.get(Calendar.YEAR);
		int cmonth = c.get(Calendar.MONTH) + 1;
		int cday = c.get(Calendar.DAY_OF_MONTH);
		if (cyear != time.getYear(WTime.FORMAT_WITHDST | WTime.FORMAT_AS_LOCAL) ||
				cmonth != time.getMonth(WTime.FORMAT_WITHDST | WTime.FORMAT_AS_LOCAL) ||
				cday != time.getDay(WTime.FORMAT_WITHDST | WTime.FORMAT_AS_LOCAL))
			field.setText(time.toString(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST | WTime.FORMAT_DATE | WTime.FORMAT_TIME));
		else
			field.setText(time.toString(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST | WTime.FORMAT_TIME));
	}
	
	protected void calculateSunrise() {
		WorldLocation loc = new WorldLocation();
		Double d = app.getLatitude();
		if (d != null) {
			loc.setLatitude(ca.hss.math.General.DEGREE_TO_RADIAN(d.doubleValue()));
			d = app.getLongitude();
			if (d != null) {
				loc.setLongitude(ca.hss.math.General.DEGREE_TO_RADIAN(d.doubleValue()));
				TimeZoneInfo info = app.getSelectedTimeZone();
				if (info != null) {
					loc.setTimezoneOffset(info.getTimezoneOffset());
					loc.setStartDST(new WTimeSpan(0));
					if (info.getDSTAmount().getTotalSeconds() > 0) {
						loc.setEndDST(new WTimeSpan(366, 0, 0, 0));
					}
					else {
						loc.setEndDST(new WTimeSpan(0));
					}
					loc.setDSTAmount(info.getDSTAmount());
					Calendar c = Calendar.getInstance();
					c.setTime(app.getDate());
					WTime time = WTime.fromLocal(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
							c.get(Calendar.DAY_OF_MONTH), 12, 0, 0, new WTimeManager(loc));
					
					rise.value = new WTime(time);
					set.value = new WTime(time);
					noon.value = new WTime(time);
					
					loc.getSunRiseSetNoon(time, rise, set, noon);

					setTime(txtSunrise, rise.value);
					setTime(txtSunset, set.value);
					setTime(txtSolarNoon, noon.value);
				}
			}
		}
	}
	
	public void onLocationChanged() {
		clearOutputValuesOnForm();
		calculateSunrise();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		clearOutputValuesOnForm();
	}


	@Override
	public void insertUpdate(DocumentEvent e) {
		clearOutputValuesOnForm();
	}


	@Override
	public void removeUpdate(DocumentEvent e) {
		clearOutputValuesOnForm();
	}

	public void hourlyTimeChanged() {
		clearHourlyOutputValuesOnForm();
		toggleHourly();
	}

	@SuppressWarnings("deprecation")
	private void transferToFBP() {
		if (chckbxFwiHourlyCalculate.isSelected()) {
			Date tm = ((SpinnerDateModel)spinnerFwiHourlyTime.getModel()).getDate();
			
			/*
			app.fbpTab.transferValues(txtFwiHourlyFFMC.getText(), txtFwiDailyDMC.getText(), txtFwiDailyDC.getText(),
					txtFwiDailyBUI.getText(), true, txtFwiHourlyWindSpeed.getText(), null, tm);
			*/
			
			app.fbpTab.transferValues(String.valueOf(fwiCalculations.hlyHFFMC), 
									  String.valueOf(fwiCalculations.dlyDMC), 
									  String.valueOf(fwiCalculations.dlyDC),
									  String.valueOf(fwiCalculations.dlyBUI), 
									  true, 
									  String.valueOf(fwiCalculations.hrlyWindSpeed), 
									  null, 
									  tm);
		}
		else {
			Date dt = new Date();
			dt.setMinutes(0);
			TimeZoneInfo info = app.getSelectedTimeZone();
			if (info.getDSTAmount().getTotalSeconds() > 0)
				dt.setHours(13);
			else
				dt.setHours(12);
			
			/*
			app.fbpTab.transferValues(txtFwiDailyFFMC.getText().replace(',', '.'), txtFwiDailyDMC.getText().replace(',', '.'), txtFwiDailyDC.getText().replace(',', '.'),
					txtFwiDailyBUI.getText().replace(',', '.'), true, txtFwiNoonWindSpeed.getText().replace(',', '.'), null, dt);
			*/
			
			app.fbpTab.transferValues(String.valueOf(fwiCalculations.dlyFFMC), 
									  String.valueOf(fwiCalculations.dlyDMC), 
									  String.valueOf(fwiCalculations.dlyDC),
									  String.valueOf(fwiCalculations.dlyBUI), 
									  true, 
									  String.valueOf(fwiCalculations.noonWindSpeed), 
									  null, 
									  dt);
		}
		
		app.setCurrentTab(app.fbpTab);
	}
	
	public void export() {
		String file = null;
		String dir = Main.prefs.getString("FWI_START_DIR", System.getProperty("user.home"));
		RFileChooser fc = RFileChooser.fileSaver();
		fc.setCurrentDirectory(dir);
		String[] extensionFilters = new String[] {
				"*.csv",
				"*.xls",
				"*.xlsx",
				"*.xml",
		};
		String[] extensionFiltersNames = new String[] {
				Main.resourceManager.getString("ui.label.file.csv") + " (*.csv)",
				Main.resourceManager.getString("ui.label.file.xls") + " (*.xls)",
				Main.resourceManager.getString("ui.label.file.xlsx") + " (*.xlsx)",
				Main.resourceManager.getString("ui.label.file.xml") + " (*.xml)",
		};
		fc.setExtensionFilters(extensionFilters, extensionFiltersNames, 0);
		fc.setTitle(Main.resourceManager.getString("ui.label.fbp.export.title"));
		int retval = fc.showDialog(app.frmRedapp);

		if (retval == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile().getAbsolutePath();
			String extension = fc.getSelectedExtension()[0];
			Main.prefs.putString("FWI_START_DIR", fc.getParentDirectory());
			OutputStream os = null;

			try {
				Preferences exporter = null;
				if(!file.endsWith(".xml") && !file.endsWith(".csv") && !file.endsWith(".xlsx") && !file.endsWith(".xls"))
					file += "." + extension;
				if (extension.equals("xml")) {
					exporter = new XMLExporter("fwi");
				} else if (extension.equals("csv")) {
					exporter = new CSVExporter();
				} else if (extension.equals("xlsx")) {
					exporter = new XLSExporter("FWI", true);
				} else if (extension.equals(".xls")) {
					exporter = new XLSExporter("FWI", false);
				}
				
				if (exporter != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					exporter.put("Date", sdf.format(app.getDate()));
					exporter.put("TimeZone", app.getSelectedTimeZone().toString());
					exporter.put("Latitude", app.getLatitudeString());
					exporter.put("Longitude", app.getLongitudeString());
	
					Double val = DecimalUtils.valueOf(txtFwiNoonTemp.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
					exporter.put("Temperature", DecimalUtils.format(val, DataType.TEMPERATURE));
					exporter.put("RelativeHumidity", txtFwiNoonRelHumidity.getText());
					val = DecimalUtils.valueOf(txtFwiNoonPrecip.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
					exporter.put("Precipitation", DecimalUtils.format(val, DataType.PRECIP));
					val = DecimalUtils.valueOf(txtFwiNoonWindSpeed.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
					exporter.put("WindSpeed", DecimalUtils.format(val, DataType.WIND_SPEED));
	
					boolean useFWI = chckbxFwiHourlyCalculate.isSelected();
					exporter.put("PerformHourlyCalculations",
							Boolean.toString(useFWI));
					if (useFWI) {
						exporter.put("HourlyMethod", comboFwiHourlyMethod.getSelectedItem().toString());
						SimpleDateFormat format = new SimpleDateFormat("hh:mm");
						exporter.put("Time", format.format((Date)spinnerFwiHourlyTime.getValue()));
						if (comboFwiHourlyMethod.getSelectedIndex() == 1) {
							exporter.put("HourlyTemperature",
									txtFwiHourlyTemp.getText());
						}
						exporter.put("HourlyRelativeHumidity",
								txtFwiHourlyRelHumidity.getText());
						// if using Van Wagner
						if (comboFwiHourlyMethod.getSelectedIndex() == 1) {
							val = DecimalUtils.valueOf(txtFwiHourlyPrecip.getText());
							if (val == null)
								val = 0.0;
							val = Convert.convertUnit(val, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
							exporter.put("HourlyPrecipitation", DecimalUtils.format(val, DataType.PRECIP));
							val = DecimalUtils.valueOf(txtFwiHourlyWindSpeed.getText());
							if (val == null)
								val = 0.0;
							val = Convert.convertUnit(val, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
							exporter.put("HourlyWindSpeed", DecimalUtils.format(val, DataType.WIND_SPEED));
							exporter.put("PreviousHourFFMC",
									txtFwiHourlyPrevHourFFMC.getText());
						}
					}
					exporter.put("YesterdayFFMC", txtFwiYstrdyFFMC.getText());
					exporter.put("YesterdayDMC", txtFwiYstrdyDMC.getText());
					exporter.put("YesterdayDC", txtFwiYstrdyDC.getText());
	
					exporter.put("FFMC", txtFwiDailyFFMC.getText());
					exporter.put("DMC", txtFwiDailyDMC.getText());
					exporter.put("DC", txtFwiDailyDC.getText());
					exporter.put("ISI", txtFwiDailyISI.getText());
					exporter.put("BUI", txtFwiDailyBUI.getText());
					exporter.put("FWI", txtFwiDailyFWI.getText());
					exporter.put("DSR", txtFwiDailyDSR.getText());
	
					if (useFWI) {
						exporter.put("HFFMC", txtFwiHourlyFFMC.getText());
						exporter.put("HISI", txtFwiHourlyISI.getText());
						exporter.put("HFWI", txtFwiHourlyFWI.getText());
					}
	
					os = new FileOutputStream(file);
					exporter.exportSubtree(os);
					os.flush();
					os.close();
				}
			}
			catch (Exception e) {
				if (os != null) {
					try {
						os.close();
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				JOptionPane.showMessageDialog(null,
				    "Unable to export file",
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void saveAllValues() {
		RPreferences prefs = Main.prefs;
		// FWI Values
		Double d;
		prefs.putString("fwi_useFWI", String.valueOf(chckbxFwiHourlyCalculate.isSelected()));
		prefs.putString("fwi_prevCalc", String.valueOf(chckbxFwiPrevHourCalculate.isSelected()));
		boolean b = comboFwiHourlyMethod.getSelectedIndex() == 1;
		prefs.putString("fwi_useVanWagner", String.valueOf(b));

		d = getDoubleFromLineEdit(txtFwiHourlyTemp);
		if (d != null) {
			prefs.putString("fwi_hrlyObs_temp", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFwiHourlyRelHumidity);
		if (d != null) {
			prefs.putString("fwi_hrlyObs_rh", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFwiHourlyPrecip);
		if (d != null) {
			prefs.putString("fwi_hrlyObs_precip", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFwiHourlyWindSpeed);
		if (d != null) {
			prefs.putString("fwi_hrlyObs_windspeed", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFwiYstrdyFFMC);
		if (d != null) {
			prefs.putString("fwi_ystrdy_ffmc", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFwiYstrdyDMC);
		if (d != null) {
			prefs.putString("fwi_ystrdy_dmc", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFwiYstrdyDC);
		if (d != null) {
			prefs.putString("fwi_ystrdy_dc", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFwiNoonTemp);
		if (d != null) {
			prefs.putString("fwi_wxObs_temp", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFwiNoonRelHumidity);
		if (d != null) {
			prefs.putString("fwi_wxObs_rh", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFwiNoonPrecip);
		if (d != null) {
			prefs.putString("fwi_wxObs_precip", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFwiNoonWindSpeed);
		if (d != null) {
			prefs.putString("fwi_wxObs_windspeed", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFwiHourlyPrevHourFFMC);
		if (d != null) {
			prefs.putString("fwi_prv_hrly_ffmc", String.valueOf(d));
		}
	}

	public void loadAllValues() {
		RPreferences prefs = Main.prefs;
		// FWI Values
		chckbxFwiHourlyCalculate.setSelected(Boolean.parseBoolean(prefs.getString("fwi_useFWI", "False")));
		chckbxFwiPrevHourCalculate.setSelected(Boolean.parseBoolean(prefs.getString("fwi_prevCalc", "False")));
		comboFwiHourlyMethod.setSelectedIndex(Boolean.parseBoolean(prefs.getString("fwi_useVanWagner", "False")) ? 1 : 0);
		txtFwiHourlyTemp.setText(DecimalUtils.format(prefs.getDouble("fwi_hrlyObs_temp", 0.0), DecimalUtils.DataType.TEMPERATURE));
		txtFwiHourlyRelHumidity.setText(DecimalUtils.format(prefs.getDouble("fwi_hrlyObs_rh", 0.0), DecimalUtils.DataType.RH));
		txtFwiHourlyPrecip.setText(DecimalUtils.format(prefs.getDouble("fwi_hrlyObs_precip", 0.0), DecimalUtils.DataType.PRECIP));
		txtFwiHourlyWindSpeed.setText(DecimalUtils.format(prefs.getDouble("fwi_hrlyObs_windspeed", 0.0), DecimalUtils.DataType.WIND_SPEED));
		txtFwiHourlyPrevHourFFMC.setText(DecimalUtils.format(prefs.getDouble("fwi_prv_hrly_ffmc", 85.0), DecimalUtils.DataType.FFMC));
		txtFwiYstrdyFFMC.setText(DecimalUtils.format(prefs.getDouble("fwi_ystrdy_ffmc", 85.0), DecimalUtils.DataType.FFMC));
		txtFwiYstrdyDMC.setText(DecimalUtils.format(prefs.getDouble("fwi_ystrdy_dmc", 25.0), DecimalUtils.DataType.DMC));
		txtFwiYstrdyDC.setText(DecimalUtils.format(prefs.getDouble("fwi_ystrdy_dc", 200.0), DecimalUtils.DataType.DC));
		txtFwiNoonTemp.setText(DecimalUtils.format(prefs.getDouble("fwi_wxObs_temp", 0.0), DecimalUtils.DataType.TEMPERATURE));
		txtFwiNoonRelHumidity.setText(DecimalUtils.format(prefs.getDouble("fwi_wxObs_rh", 0.0), DecimalUtils.DataType.RH));
		txtFwiNoonPrecip.setText(DecimalUtils.format(prefs.getDouble("fwi_wxObs_precip", 0.0), DecimalUtils.DataType.PRECIP));
		txtFwiNoonWindSpeed.setText(DecimalUtils.format(prefs.getDouble("fwi_wxObs_windspeed", 0.0), DecimalUtils.DataType.WIND_SPEED));
		getValuesFromForm();
	}

	// {{ Ui Stuff

	private RTextField txtSunrise;
	private RTextField txtSolarNoon;
	private RTextField txtSunset;
	private RTextField txtFwiNoonTemp;
	private RTextField txtFwiNoonRelHumidity;
	private RTextField txtFwiNoonPrecip;
	private RTextField txtFwiNoonWindSpeed;
	private RTextField txtFwiHourlyTemp;
	private RTextField txtFwiHourlyRelHumidity;
	private RTextField txtFwiHourlyPrecip;
	private RTextField txtFwiHourlyWindSpeed;
	private RTextField txtFwiHourlyPrevHourFFMC;
	private RTextField txtFwiYstrdyDC;
	private RTextField txtFwiYstrdyDMC;
	private RTextField txtFwiYstrdyFFMC;
	private RTextField txtFwiDailyDC;
	private RTextField txtFwiDailyDMC;
	private RTextField txtFwiDailyFFMC;
	private RTextField txtFwiDailyISI;
	private RTextField txtFwiDailyBUI;
	private RTextField txtFwiDailyFWI;
	private RTextField txtFwiDailyDSR;
	private RTextField txtFwiHourlyFFMC;
	private RTextField txtFwiHourlyISI;
	private RTextField txtFwiHourlyFWI;
	private JCheckBox chckbxFwiHourlyCalculate;
	private JCheckBox chckbxFwiPrevHourCalculate;
	private RComboBox<String> comboFwiHourlyMethod;
	private RButton btnFwiExport;
	private RButton btnFwiTransferToFBP;
	private RButton btnReset;
	private JSpinner spinnerFwiHourlyTime;
	private RLabel lblFwiNoonTempUnit;
	private RLabel lblFwiNoonPrecipUnit;
	private RLabel lblFwiNoonWindSpeedUnit;
	private RLabel lblFwiHourlyTempUnit;
	private RLabel lblFwiHourlyPrecipUnit;
	private RLabel lblFwiHourlyWindSpeedUnit;
	private RGroupBox panelFwiNoon;

	private RLabel lblFwiHourlyTemp;
	private RLabel lblFwiHourlyRelHumidity;
	private RLabel lblFwiHourlyRelHumidityUnit;
	private RLabel lblFwiHourlyPrecip;

	private RLabel lblFwiHourlyTime;
	private RLabel lblFwiHourlyWindSpeed;
	private RLabel lblFwiHourlyFFMC;
	private RLabel lblFwiHourlyISI;
	private RLabel lblFwiHourlyFWI;


	protected void initialize() {
		if (initialized)
			return;
		initialized = true;

		setLayout(null);
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 971, 501);
		else
			setBounds(0, 0, 981, 506);

		if (Main.isWindows())
			setBackground(Color.white);

		//
		
		RGroupBox groupSunrise = new RGroupBox();
		groupSunrise.setText(Main.resourceManager.getString("ui.label.fwi.sunrise.title"));
		if (Main.isMac())
			groupSunrise.setBounds(445, 5, 511, 121);
		else
			groupSunrise.setBounds(450, 10, 511, 121);
		add(groupSunrise);
		
		JPanel panelSunrise = new JPanel();
		panelSunrise.setBackground(new Color(245, 245, 245));
		panelSunrise.setLayout(new SpringLayout());
		panelSunrise.setBounds(10, 20, 492, 85);
		groupSunrise.add(panelSunrise);

		RLabel lblSunrise = new RLabel(Main.resourceManager.getString("ui.label.fwi.sunrise.sunrise"));
		panelSunrise.add(lblSunrise);
		
		JPanel pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelSunrise.add(pnl);
		
		txtSunrise = new RTextField();
		txtSunrise.setEditable(false);
		txtSunrise.setColumns(10);
		txtSunrise.setMinimumSize(new Dimension(363, 20));
		txtSunrise.setMaximumSize(new Dimension(363, 20));
		txtSunrise.setPreferredSize(new Dimension(363, 20));
		txtSunrise.setBounds(0, 0, 352, 20);
		panelSunrise.add(txtSunrise);
		
		RLabel lblSolarNoon = new RLabel(Main.resourceManager.getString("ui.label.fwi.sunrise.noon"));
		panelSunrise.add(lblSolarNoon);
		
		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelSunrise.add(pnl);

		txtSolarNoon = new RTextField();
		txtSolarNoon.setEditable(false);
		txtSolarNoon.setColumns(10);
		txtSolarNoon.setMinimumSize(new Dimension(363, 20));
		txtSolarNoon.setMaximumSize(new Dimension(363, 20));
		txtSolarNoon.setPreferredSize(new Dimension(363, 20));
		txtSolarNoon.setBounds(0, 0, 352, 20);
		panelSunrise.add(txtSolarNoon);
		
		RLabel lblSunset = new RLabel(Main.resourceManager.getString("ui.label.fwi.sunrise.sunset"));
		panelSunrise.add(lblSunset);
		
		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelSunrise.add(pnl);

		txtSunset = new RTextField();
		txtSunset.setEditable(false);
		txtSunset.setColumns(10);
		txtSunset.setMinimumSize(new Dimension(363, 20));
		txtSunset.setMaximumSize(new Dimension(363, 20));
		txtSunset.setPreferredSize(new Dimension(363, 20));
		txtSunset.setBounds(0, 0, 352, 20);
		panelSunrise.add(txtSunset);
		
		SpringUtilities.makeCompactGrid(panelSunrise, 3, 3, 0, 5, 0, 5, 6, 10);
		
		//
		
		panelFwiNoon = new RGroupBox();
		panelFwiNoon.setText(Main.resourceManager.getString("ui.label.fwi.noon.title"));
		if (Main.isLinux())
			panelFwiNoon.setBounds(5, 5, 431, 85);
		else
			panelFwiNoon.setBounds(10, 10, 431, 85);
		panelFwiNoon.setLayout(new SpringLayout());
		add(panelFwiNoon);

		JPanel panelFwiNoonLeft = new JPanel();
		panelFwiNoonLeft.setLayout(new SpringLayout());
		panelFwiNoonLeft.setBackground(new Color(0, 0, 0, 0));
		panelFwiNoon.add(panelFwiNoonLeft);

		JPanel panelFwiNoonRight = new JPanel();
		panelFwiNoonRight.setLayout(new SpringLayout());
		panelFwiNoonRight.setBackground(new Color(0, 0, 0, 0));
		panelFwiNoon.add(panelFwiNoonRight);

		RLabel lblFwiNoonTemperature = new RLabel(Main.resourceManager.getString("ui.label.weather.temp"));
		panelFwiNoonLeft.add(lblFwiNoonTemperature);

		txtFwiNoonTemp = new RTextField();
		txtFwiNoonTemp.setFormat(DecimalFormat.getInstance(Main.resourceManager.loc));
		panelFwiNoonLeft.add(txtFwiNoonTemp);
		txtFwiNoonTemp.setMinimumSize(new Dimension(0, 20));
		txtFwiNoonTemp.setColumns(10);
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFwiNoonTempUnit = new RLabel(Main.resourceManager.getString("ui.label.units.celsius"));
		else
			lblFwiNoonTempUnit = new RLabel(Main.resourceManager.getString("ui.label.units.fahrenheit"));
		panelFwiNoonLeft.add(lblFwiNoonTempUnit);

		RLabel lblFwiNoonPrecip = new RLabel(Main.resourceManager.getString("ui.label.weather.precip"));
		panelFwiNoonRight.add(lblFwiNoonPrecip);

		txtFwiNoonPrecip = new RTextField();
		txtFwiNoonPrecip.setColumns(10);
		txtFwiNoonPrecip.setMinimumSize(new Dimension(0, 20));
		txtFwiNoonPrecip.setFormat(DecimalFormat.getInstance(Main.resourceManager.loc));
		panelFwiNoonRight.add(txtFwiNoonPrecip);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFwiNoonPrecipUnit = new RLabel(Main.resourceManager.getString("ui.label.units.mm"));
		else
			lblFwiNoonPrecipUnit = new RLabel(Main.resourceManager.getString("ui.label.units.in"));
		panelFwiNoonRight.add(lblFwiNoonPrecipUnit);

		RLabel lblFwiNoonRelHumidity = new RLabel(Main.resourceManager.getString("ui.label.weather.rh"));
		panelFwiNoonLeft.add(lblFwiNoonRelHumidity);

		txtFwiNoonRelHumidity = new RTextField();
		txtFwiNoonRelHumidity.setColumns(10);
		txtFwiNoonRelHumidity.setMinimumSize(new Dimension(0, 20));
		txtFwiNoonRelHumidity.setFormat(DecimalFormat.getInstance(Main.resourceManager.loc));
		panelFwiNoonLeft.add(txtFwiNoonRelHumidity);

		RLabel lblFwiNoonRelHumidityUnit = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		panelFwiNoonLeft.add(lblFwiNoonRelHumidityUnit);

		RLabel lblFwiNoonWindSpeed = new RLabel(Main.resourceManager.getString("ui.label.weather.ws"));
		panelFwiNoonRight.add(lblFwiNoonWindSpeed);

		txtFwiNoonWindSpeed = new RTextField();
		txtFwiNoonWindSpeed.setColumns(10);
		txtFwiNoonWindSpeed.setFormat(DecimalFormat.getInstance(Main.resourceManager.loc));
		txtFwiNoonWindSpeed.setPreferredSize(new Dimension(txtFwiNoonWindSpeed.getPreferredSize().width, 20));
		panelFwiNoonRight.add(txtFwiNoonWindSpeed);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFwiNoonWindSpeedUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblFwiNoonWindSpeedUnit = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		panelFwiNoonRight.add(lblFwiNoonWindSpeedUnit);

		SpringUtilities.makeCompactGrid(panelFwiNoonRight, 2, 3, 0, 0, 6, 10);
		SpringUtilities.makeCompactGrid(panelFwiNoonLeft, 2, 3, 0, 0, 6, 10);
		SpringUtilities.makeCompactGrid(panelFwiNoon, 1, 2, 6, 2, 6, 6, 6, 0);

		RGroupBox groupFwiHourlyObs = new RGroupBox();
		groupFwiHourlyObs.setText(Main.resourceManager.getString("ui.label.fwi.hourly.title"));
		if (Main.isLinux())
			groupFwiHourlyObs.setBounds(5, 99, 430, 242);
		else
			groupFwiHourlyObs.setBounds(10, 104, 430, 242);
		add(groupFwiHourlyObs);

		JPanel panelFwiHourlyObs = new JPanel();
		panelFwiHourlyObs.setBounds(10, 20, 410, 214);
		panelFwiHourlyObs.setBackground(new Color(245, 245, 245));
		panelFwiHourlyObs.setLayout(new SpringLayout());
		groupFwiHourlyObs.add(panelFwiHourlyObs);

		chckbxFwiHourlyCalculate = new JCheckBox(Main.resourceManager.getString("ui.label.fwi.hourly.hourly"));
		chckbxFwiHourlyCalculate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				toggleHourly();
			}
		});
		chckbxFwiHourlyCalculate.setBackground(new Color(245, 245, 245));
		if (Main.isLinux())
			chckbxFwiHourlyCalculate.setFont(chckbxFwiHourlyCalculate.getFont().deriveFont(12.0f));
		panelFwiHourlyObs.add(chckbxFwiHourlyCalculate);

		comboFwiHourlyMethod = new RComboBox<String>();
		comboFwiHourlyMethod.addActionListener((e) -> {
			int i = comboFwiHourlyMethod.getSelectedIndex();
			fwiCalculations.useVanWagner = i != 0;
			toggleHourly();
		});
		comboFwiHourlyMethod.setModel(new DefaultComboBoxModel<String>(new String[] {
				Main.resourceManager.getString("ui.label.fwicalc.hourly.lawson"), Main.resourceManager.getString("ui.label.fwicalc.hourly.wagner") }));
		panelFwiHourlyObs.add(comboFwiHourlyMethod);

		JLabel lbl = new JLabel();
		panelFwiHourlyObs.add(lbl);

		lblFwiHourlyTime = new RLabel(Main.resourceManager.getString("ui.label.fwi.hourly.time"));
		panelFwiHourlyObs.add(lblFwiHourlyTime);

		spinnerFwiHourlyTime = new JSpinner() {
			private static final long serialVersionUID = 1L;

			@Override
			public void setBounds(int x, int y, int width, int height) {
				super.setBounds(x, y, width, 22);
			}
		};
		spinnerFwiHourlyTime.setModel(new SpinnerDateModel(new Date(
				1390975200000L), null, null, Calendar.MINUTE));
		spinnerFwiHourlyTime.setEditor(new JSpinner.DateEditor(
				spinnerFwiHourlyTime, "H:mm"));
		spinnerFwiHourlyTime.setMinimumSize(new Dimension(0, 22));
		spinnerFwiHourlyTime.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		if (Main.isLinux()) {
			JComponent comp = spinnerFwiHourlyTime.getEditor();
			if (comp instanceof JSpinner.DefaultEditor) {
				JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)comp;
				editor.getTextField().setFont(editor.getTextField().getFont().deriveFont(11.0f));
			}
		}
		panelFwiHourlyObs.add(spinnerFwiHourlyTime);

		lbl = new JLabel();
		panelFwiHourlyObs.add(lbl);

		lblFwiHourlyTemp = new RLabel(Main.resourceManager.getString("ui.label.weather.temp"));
		panelFwiHourlyObs.add(lblFwiHourlyTemp);

		txtFwiHourlyTemp = new RTextField();
		txtFwiHourlyTemp.setColumns(10);
		txtFwiHourlyTemp.setFormat(DecimalFormat.getInstance(Main.resourceManager.loc));
		panelFwiHourlyObs.add(txtFwiHourlyTemp);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFwiHourlyTempUnit = new RLabel(Main.resourceManager.getString("ui.label.units.celsius"));
		else
			lblFwiHourlyTempUnit = new RLabel(Main.resourceManager.getString("ui.label.units.fahrenheit"));
		panelFwiHourlyObs.add(lblFwiHourlyTempUnit);

		lblFwiHourlyRelHumidity = new RLabel(Main.resourceManager.getString("ui.label.weather.rh"));
		panelFwiHourlyObs.add(lblFwiHourlyRelHumidity);

		txtFwiHourlyRelHumidity = new RTextField();
		txtFwiHourlyRelHumidity.setColumns(10);
		panelFwiHourlyObs.add(txtFwiHourlyRelHumidity);
		txtFwiHourlyRelHumidity.setFormat(DecimalFormat.getInstance(Main.resourceManager.loc));

		lblFwiHourlyRelHumidityUnit = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		panelFwiHourlyObs.add(lblFwiHourlyRelHumidityUnit);

		lblFwiHourlyPrecip = new RLabel(Main.resourceManager.getString("ui.label.weather.precip"));
		panelFwiHourlyObs.add(lblFwiHourlyPrecip);

		txtFwiHourlyPrecip = new RTextField();
		txtFwiHourlyPrecip.setColumns(10);
		txtFwiHourlyPrecip.setFormat(DecimalFormat.getInstance(Main.resourceManager.loc));
		panelFwiHourlyObs.add(txtFwiHourlyPrecip);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFwiHourlyPrecipUnit = new RLabel(Main.resourceManager.getString("ui.label.units.mm"));
		else
			lblFwiHourlyPrecipUnit = new RLabel(Main.resourceManager.getString("ui.label.units.in"));
		panelFwiHourlyObs.add(lblFwiHourlyPrecipUnit);

		lblFwiHourlyWindSpeed = new RLabel(Main.resourceManager.getString("ui.label.weather.ws"));
		panelFwiHourlyObs.add(lblFwiHourlyWindSpeed);

		txtFwiHourlyWindSpeed = new RTextField();
		txtFwiHourlyWindSpeed.setColumns(10);
		txtFwiHourlyWindSpeed.setFormat(DecimalFormat.getInstance(Main.resourceManager.loc));
		panelFwiHourlyObs.add(txtFwiHourlyWindSpeed);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFwiHourlyWindSpeedUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblFwiHourlyWindSpeedUnit = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		panelFwiHourlyObs.add(lblFwiHourlyWindSpeedUnit);

		chckbxFwiPrevHourCalculate = new JCheckBox(Main.resourceManager.getString("ui.label.fwi.hourly.ffmc"));
		chckbxFwiPrevHourCalculate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				togglePrevHourFfmc();
				clearOutputValuesOnForm();
			}
		});
		chckbxFwiPrevHourCalculate.setBackground(new Color(245, 245, 245));
		if (Main.isLinux())
			chckbxFwiPrevHourCalculate.setFont(chckbxFwiPrevHourCalculate.getFont().deriveFont(12.0f));
		panelFwiHourlyObs.add(chckbxFwiPrevHourCalculate);

		txtFwiHourlyPrevHourFFMC = new RTextField();
		txtFwiHourlyPrevHourFFMC.setColumns(10);
		txtFwiHourlyPrevHourFFMC.setFormat(DecimalFormat.getInstance(Main.resourceManager.loc));
		panelFwiHourlyObs.add(txtFwiHourlyPrevHourFFMC);

		lbl = new JLabel();
		panelFwiHourlyObs.add(lbl);

		SpringUtilities.makeCompactGrid(panelFwiHourlyObs, 7, 3, 0, 0, 5, 5, 6, 10);

        SpringLayout layout = (SpringLayout)panelFwiHourlyObs.getLayout();
        Constraints con = layout.getConstraints(lblFwiHourlyWindSpeedUnit);
        int wid = con.getWidth().getValue();
        con = layout.getConstraints(chckbxFwiHourlyCalculate);
        int wid2 = con.getWidth().getValue();

		RGroupBox groupFwiYstrdy = new RGroupBox();
		groupFwiYstrdy.setText(Main.resourceManager.getString("ui.label.fwi.yester.title"));
		if (Main.isLinux())
			groupFwiYstrdy.setBounds(5, 350, 430, 111);
		else
			groupFwiYstrdy.setBounds(10, 355, 430, 111);
		add(groupFwiYstrdy);

		JPanel panelFwiYstrdy = new JPanel();
		panelFwiYstrdy.setBounds(10, 20, 410, 85);
		panelFwiYstrdy.setBackground(new Color(245, 245, 245));
		panelFwiYstrdy.setLayout(new SpringLayout());
		groupFwiYstrdy.add(panelFwiYstrdy);

		RLabel lblFwiYstrdyFFMC = new RLabel(Main.resourceManager.getString("ui.label.fire.ffmc"));
		lblFwiYstrdyFFMC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.ffmc"));
		lblFwiYstrdyFFMC.setMinimumSize(new Dimension(wid2, 5));
		lblFwiYstrdyFFMC.setMaximumSize(new Dimension(wid2, 5));
		lblFwiYstrdyFFMC.setPreferredSize(new Dimension(wid2, 5));
		//lblFwiYstrdyFFMC.setBounds(10, 20, 81, 21);
		panelFwiYstrdy.add(lblFwiYstrdyFFMC);

		txtFwiYstrdyFFMC = new RTextField();
		txtFwiYstrdyFFMC.setColumns(10);
		txtFwiYstrdyFFMC.setFormat(DecimalFormat.getInstance(Main.resourceManager.loc));
		//txtFwiYstrdyFFMC.setBounds(260, 20, 101, 20);
		panelFwiYstrdy.add(txtFwiYstrdyFFMC);

		pnl.setBackground(new Color(245, 245, 245));
		pnl.setMinimumSize(new Dimension(wid, 5));
		pnl.setMaximumSize(new Dimension(wid, 5));
		pnl.setPreferredSize(new Dimension(wid, 5));
		pnl.setBounds(0, 0, wid, 5);
		panelFwiYstrdy.add(pnl);

		RLabel lblFwiYstrdyDMC = new RLabel(Main.resourceManager.getString("ui.label.fire.dmc"));
		lblFwiYstrdyDMC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.dmc"));
		//lblFwiYstrdyDMC.setBounds(10, 50, 91, 21);
		panelFwiYstrdy.add(lblFwiYstrdyDMC);

		txtFwiYstrdyDMC = new RTextField();
		txtFwiYstrdyDMC.setColumns(10);
		txtFwiYstrdyDMC.setFormat(DecimalFormat.getInstance(Main.resourceManager.loc));
		//txtFwiYstrdyDMC.setBounds(260, 50, 101, 20);
		panelFwiYstrdy.add(txtFwiYstrdyDMC);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		pnl.setMinimumSize(new Dimension(wid, 5));
		pnl.setMaximumSize(new Dimension(wid, 5));
		panelFwiYstrdy.add(pnl);

		RLabel lblFwiYstrdyDC = new RLabel(Main.resourceManager.getString("ui.label.fire.dc"));
		lblFwiYstrdyDC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.dc"));
		//lblFwiYstrdyDC.setBounds(10, 80, 91, 21);
		panelFwiYstrdy.add(lblFwiYstrdyDC);

		txtFwiYstrdyDC = new RTextField();
		txtFwiYstrdyDC.setColumns(10);
		txtFwiYstrdyDC.setFormat(DecimalFormat.getInstance(Main.resourceManager.loc));
		//txtFwiYstrdyDC.setBounds(260, 80, 101, 20);
		panelFwiYstrdy.add(txtFwiYstrdyDC);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		pnl.setMinimumSize(new Dimension(wid, 5));
		pnl.setMaximumSize(new Dimension(wid, 5));
		panelFwiYstrdy.add(pnl);
		
		SpringUtilities.makeCompactGrid(panelFwiYstrdy, 3, 3, 0, 0, 5, 5, 6, 10);

		RGroupBox groupFwiDaily = new RGroupBox();
		if (Main.isLinux()) {
			groupFwiDaily.setText(Main.resourceManager.getString("ui.label.fwi.calc.daily.titleshort"));
			groupFwiDaily.setBounds(445, 135, 241, 240);
		}
		else {
			groupFwiDaily.setText(Main.resourceManager.getString("ui.label.fwi.calc.daily.title"));
			groupFwiDaily.setBounds(450, 140, 241, 240);
		}
		add(groupFwiDaily);

		JPanel panelFwiDaily = new JPanel();
		panelFwiDaily.setBackground(new Color(245, 245, 245));
		panelFwiDaily.setBounds(10, 20, 220, 206);
		panelFwiDaily.setLayout(new SpringLayout());
		groupFwiDaily.add(panelFwiDaily);

		RLabel lblFwiDailyFFMC = new RLabel(Main.resourceManager.getString("ui.label.fire.ffmc"));
		lblFwiDailyFFMC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.ffmc"));
		//lblFwiDailyFFMC.setBounds(20, 20, 101, 21);
		panelFwiDaily.add(lblFwiDailyFFMC);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelFwiDaily.add(pnl);

		txtFwiDailyFFMC = new RTextField();
		txtFwiDailyFFMC.setEditable(false);
		txtFwiDailyFFMC.setColumns(10);
		txtFwiDailyFFMC.setMinimumSize(new Dimension(91, 20));
		txtFwiDailyFFMC.setMaximumSize(new Dimension(91, 20));
		txtFwiDailyFFMC.setPreferredSize(new Dimension(91, 20));
		txtFwiDailyFFMC.setBounds(0, 0, 91, 20);
		panelFwiDaily.add(txtFwiDailyFFMC);

		RLabel lblFwiDailyDMC = new RLabel(Main.resourceManager.getString("ui.label.fire.dmc"));
		lblFwiDailyDMC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.dmc"));
		//lblFwiDailyDMC.setBounds(20, 50, 101, 21);
		panelFwiDaily.add(lblFwiDailyDMC);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelFwiDaily.add(pnl);

		txtFwiDailyDMC = new RTextField();
		txtFwiDailyDMC.setEditable(false);
		txtFwiDailyDMC.setColumns(10);
		txtFwiDailyDMC.setMinimumSize(new Dimension(91, 20));
		txtFwiDailyDMC.setMaximumSize(new Dimension(91, 20));
		txtFwiDailyDMC.setPreferredSize(new Dimension(91, 20));
		txtFwiDailyDMC.setBounds(0, 0, 91, 20);
		panelFwiDaily.add(txtFwiDailyDMC);

		RLabel lblFwiDailyDC = new RLabel(Main.resourceManager.getString("ui.label.fire.dc"));
		lblFwiDailyDC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.dc"));
		//lblFwiDailyDC.setBounds(20, 80, 101, 21);
		panelFwiDaily.add(lblFwiDailyDC);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelFwiDaily.add(pnl);

		txtFwiDailyDC = new RTextField();
		txtFwiDailyDC.setEditable(false);
		txtFwiDailyDC.setColumns(10);
		txtFwiDailyDC.setMinimumSize(new Dimension(91, 20));
		txtFwiDailyDC.setMaximumSize(new Dimension(91, 20));
		txtFwiDailyDC.setPreferredSize(new Dimension(91, 20));
		txtFwiDailyDC.setBounds(0, 0, 91, 20);
		panelFwiDaily.add(txtFwiDailyDC);

		RLabel lblFwiDailyISI = new RLabel(Main.resourceManager.getString("ui.label.fire.isi"));
		lblFwiDailyISI.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.isi"));
		//lblFwiDailyISI.setBounds(20, 110, 101, 21);
		panelFwiDaily.add(lblFwiDailyISI);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelFwiDaily.add(pnl);

		txtFwiDailyISI = new RTextField();
		txtFwiDailyISI.setEditable(false);
		txtFwiDailyISI.setColumns(10);
		txtFwiDailyISI.setMinimumSize(new Dimension(91, 20));
		txtFwiDailyISI.setMaximumSize(new Dimension(91, 20));
		txtFwiDailyISI.setPreferredSize(new Dimension(91, 20));
		txtFwiDailyISI.setBounds(0, 0, 91, 20);
		panelFwiDaily.add(txtFwiDailyISI);

		RLabel lblFwiDailyBUI = new RLabel(Main.resourceManager.getString("ui.label.fire.bui"));
		lblFwiDailyBUI.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.bui"));
		//lblFwiDailyBUI.setBounds(20, 140, 101, 21);
		panelFwiDaily.add(lblFwiDailyBUI);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelFwiDaily.add(pnl);

		txtFwiDailyBUI = new RTextField();
		txtFwiDailyBUI.setEditable(false);
		txtFwiDailyBUI.setColumns(10);
		txtFwiDailyBUI.setMinimumSize(new Dimension(91, 20));
		txtFwiDailyBUI.setMaximumSize(new Dimension(91, 20));
		txtFwiDailyBUI.setPreferredSize(new Dimension(91, 20));
		txtFwiDailyBUI.setBounds(0, 0, 91, 20);
		panelFwiDaily.add(txtFwiDailyBUI);

		RLabel lblFwiDailyFWI = new RLabel(Main.resourceManager.getString("ui.label.fire.fwi"));
		lblFwiDailyFWI.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.fwi"));
		//lblFwiDailyFWI.setBounds(20, 170, 101, 21);
		panelFwiDaily.add(lblFwiDailyFWI);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelFwiDaily.add(pnl);

		txtFwiDailyFWI = new RTextField();
		txtFwiDailyFWI.setEditable(false);
		txtFwiDailyFWI.setColumns(10);
		txtFwiDailyFWI.setMinimumSize(new Dimension(91, 20));
		txtFwiDailyFWI.setMaximumSize(new Dimension(91, 20));
		txtFwiDailyFWI.setPreferredSize(new Dimension(91, 20));
		txtFwiDailyFWI.setBounds(0, 0, 91, 20);
		panelFwiDaily.add(txtFwiDailyFWI);

		RLabel lblFwiDailyDSR = new RLabel(Main.resourceManager.getString("ui.label.fire.dsr"));
		lblFwiDailyDSR.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.dsr"));
		//lblFwiDailyDSR.setBounds(20, 200, 101, 21);
		panelFwiDaily.add(lblFwiDailyDSR);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelFwiDaily.add(pnl);

		txtFwiDailyDSR = new RTextField();
		txtFwiDailyDSR.setEditable(false);
		txtFwiDailyDSR.setColumns(10);
		txtFwiDailyDSR.setMinimumSize(new Dimension(91, 20));
		txtFwiDailyDSR.setMaximumSize(new Dimension(91, 20));
		txtFwiDailyDSR.setPreferredSize(new Dimension(91, 20));
		txtFwiDailyDSR.setBounds(0, 0, 91, 20);
		panelFwiDaily.add(txtFwiDailyDSR);

		SpringUtilities.makeCompactGrid(panelFwiDaily, 7, 3, 0, 5, 0, 5, 6, 10);

		RGroupBox groupFwiHourly = new RGroupBox();
		if (Main.isLinux()) {
			groupFwiHourly.setText(Main.resourceManager.getString("ui.label.fwi.calc.hourly.titleshort"));
			groupFwiHourly.setBounds(695, 135, 261, 121);
		}
		else {
			groupFwiHourly.setText(Main.resourceManager.getString("ui.label.fwi.calc.hourly.title"));
			groupFwiHourly.setBounds(700, 140, 261, 121);
		}
		add(groupFwiHourly);

		JPanel panelFwiHourly = new JPanel();
		panelFwiHourly.setBackground(new Color(245, 245, 245));
		panelFwiHourly.setLayout(new SpringLayout());
		panelFwiHourly.setBounds(10, 20, 241, 85);
		groupFwiHourly.add(panelFwiHourly);

		lblFwiHourlyFFMC = new RLabel(Main.resourceManager.getString("ui.label.fire.hffmc"));
		lblFwiHourlyFFMC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.hffmc"));
		//lblFwiHourlyFFMC.setBounds(10, 20, 91, 21);
		panelFwiHourly.add(lblFwiHourlyFFMC);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelFwiHourly.add(pnl);

		txtFwiHourlyFFMC = new RTextField();
		txtFwiHourlyFFMC.setEditable(false);
		txtFwiHourlyFFMC.setColumns(10);
		txtFwiHourlyFFMC.setMinimumSize(new Dimension(91, 20));
		txtFwiHourlyFFMC.setMaximumSize(new Dimension(91, 20));
		txtFwiHourlyFFMC.setPreferredSize(new Dimension(91, 20));
		txtFwiHourlyFFMC.setBounds(0, 0, 91, 20);
		panelFwiHourly.add(txtFwiHourlyFFMC);

		lblFwiHourlyISI = new RLabel(Main.resourceManager.getString("ui.label.fire.hisi"));
		lblFwiHourlyISI.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.hisi"));
		//lblFwiHourlyISI.setBounds(10, 50, 91, 21);
		panelFwiHourly.add(lblFwiHourlyISI);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelFwiHourly.add(pnl);

		txtFwiHourlyISI = new RTextField();
		txtFwiHourlyISI.setEditable(false);
		txtFwiHourlyISI.setColumns(10);
		txtFwiHourlyISI.setMinimumSize(new Dimension(91, 20));
		txtFwiHourlyISI.setMaximumSize(new Dimension(91, 20));
		txtFwiHourlyISI.setPreferredSize(new Dimension(91, 20));
		txtFwiHourlyISI.setBounds(0, 0, 91, 20);
		panelFwiHourly.add(txtFwiHourlyISI);

		lblFwiHourlyFWI = new RLabel(Main.resourceManager.getString("ui.label.fire.hfwi"));
		lblFwiHourlyFWI.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.hfwi"));
		//lblFwiHourlyFWI.setBounds(10, 80, 91, 21);
		panelFwiHourly.add(lblFwiHourlyFWI);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		panelFwiHourly.add(pnl);

		txtFwiHourlyFWI = new RTextField();
		txtFwiHourlyFWI.setEditable(false);
		txtFwiHourlyFWI.setColumns(10);
		txtFwiHourlyFWI.setMinimumSize(new Dimension(91, 20));
		txtFwiHourlyFWI.setMaximumSize(new Dimension(91, 20));
		txtFwiHourlyFWI.setPreferredSize(new Dimension(91, 20));
		txtFwiHourlyFWI.setBounds(0, 0, 91, 20);
		panelFwiHourly.add(txtFwiHourlyFWI);

		SpringUtilities.makeCompactGrid(panelFwiHourly, 3, 3, 0, 5, 0, 5, 6, 10);

		JPanel panel1 = new JPanel();
		if (Main.isWindows())
			panel1.setBounds(10, 424, 951, 50);
		else
			panel1.setBounds(10, 419, 951, 50);
		FlowLayout layout2 = new FlowLayout(FlowLayout.RIGHT);
		layout2.setAlignOnBaseline(true);
		panel1.setLayout(layout2);
		if (Main.isWindows())
			panel1.setBackground(Color.white);
		add(panel1);
		
		RButton btnFwiCalculate = new RButton(Main.resourceManager.getString("ui.label.fwi.calculate"), RButton.Decoration.Calc);
		btnFwiCalculate.addActionListener((e) -> calculate());
		panel1.add(btnFwiCalculate);

		btnFwiTransferToFBP = new RButton(Main.resourceManager.getString("ui.label.fwi.tofbp"), RButton.Decoration.Arrow);
		panel1.add(btnFwiTransferToFBP);

		btnFwiExport = new RButton(Main.resourceManager.getString("ui.label.fwi.export"));
		panel1.add(btnFwiExport);

		btnReset = new RButton(Main.resourceManager.getString("ui.label.footer.reset"));
		btnReset.addActionListener((e) -> reset());
		panel1.add(btnReset);
	}
	
	private void togglePrevHourFfmc() {
		int index = comboFwiHourlyMethod.getSelectedIndex();
		boolean checked = chckbxFwiPrevHourCalculate.isSelected();
		txtFwiHourlyPrevHourFFMC.setEditable(checked);
		txtFwiHourlyPrevHourFFMC.setEnabled(index == 1);
	}

	private void initTabOrder() {
		tabOrder.clear();
		tabOrder.add(txtFwiNoonTemp);
		tabOrder.add(txtFwiNoonRelHumidity);
		tabOrder.add(txtFwiNoonPrecip);
		tabOrder.add(txtFwiNoonWindSpeed);
		tabOrder.add(chckbxFwiHourlyCalculate);
		tabOrder.add(comboFwiHourlyMethod);
		tabOrder.add(spinnerFwiHourlyTime);
		tabOrder.add(txtFwiHourlyTemp);
		tabOrder.add(txtFwiHourlyRelHumidity);
		tabOrder.add(txtFwiHourlyPrecip);
		tabOrder.add(txtFwiHourlyWindSpeed);
		tabOrder.add(txtFwiHourlyPrevHourFFMC);
		tabOrder.add(txtFwiYstrdyFFMC);
		tabOrder.add(txtFwiYstrdyDMC);
		tabOrder.add(txtFwiYstrdyDC);
	}

	// }}

	@Override
	public void setInternetConnected(boolean conn) { }

	@Override
	public void onTimeZoneChanged() {
		toggleHourly();
		clearOutputValuesOnForm();
		calculateSunrise();
	}
	
	@Override
	public void settingsUpdated() {
		if (Main.unitSystem() == UnitSystem.METRIC) {
			String celsius = Main.resourceManager.getString("ui.label.units.celsius");
			String temp = lblFwiNoonTempUnit.getText();
			if (!temp.equals(celsius)) {
				String text = txtFwiNoonTemp.getText();
				Double val = ca.redapp.util.DoubleEx.valueOf(text);
				val = Convert.convertUnit(val, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(UnitSystem.IMPERIAL));
				txtFwiNoonTemp.setText(DecimalUtils.format(val, DataType.TEMPERATURE));
				text = txtFwiNoonPrecip.getText();
				val = ca.redapp.util.DoubleEx.valueOf(text);
				val = Convert.convertUnit(val, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(UnitSystem.IMPERIAL));
				txtFwiNoonPrecip.setText(DecimalUtils.format(val, DataType.PRECIP));
				text = txtFwiNoonWindSpeed.getText();
				val = ca.redapp.util.DoubleEx.valueOf(text);
				val = Convert.convertUnit(val, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(UnitSystem.IMPERIAL));
				txtFwiNoonWindSpeed.setText(DecimalUtils.format(val, DataType.WIND_SPEED));
				text = txtFwiHourlyTemp.getText();
				val = ca.redapp.util.DoubleEx.valueOf(text);
				val = Convert.convertUnit(val, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(UnitSystem.IMPERIAL));
				txtFwiHourlyTemp.setText(DecimalUtils.format(val, DataType.TEMPERATURE));
				text = txtFwiHourlyPrecip.getText();
				val = ca.redapp.util.DoubleEx.valueOf(text);
				val = Convert.convertUnit(val, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(UnitSystem.IMPERIAL));
				txtFwiHourlyPrecip.setText(DecimalUtils.format(val, DataType.PRECIP));
				text = txtFwiHourlyWindSpeed.getText();
				val = ca.redapp.util.DoubleEx.valueOf(text);
				val = Convert.convertUnit(val, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(UnitSystem.IMPERIAL));
				txtFwiHourlyWindSpeed.setText(DecimalUtils.format(val, DataType.WIND_SPEED));
				
				
			}
			lblFwiNoonTempUnit.setText(celsius);
			lblFwiNoonPrecipUnit.setText(Main.resourceManager.getString("ui.label.units.mm"));
			lblFwiNoonWindSpeedUnit.setText(Main.resourceManager.getString("ui.label.units.kiloperhour"));
			lblFwiHourlyTempUnit.setText(Main.resourceManager.getString("ui.label.units.celsius"));
			lblFwiHourlyPrecipUnit.setText(Main.resourceManager.getString("ui.label.units.mm"));
			lblFwiHourlyWindSpeedUnit.setText(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		}
		else {
			String fahrenheit = Main.resourceManager.getString("ui.label.units.fahrenheit");
			String temp = lblFwiNoonTempUnit.getText();
			if (!temp.equals(fahrenheit)) {
				String text = txtFwiNoonTemp.getText();
				Double val = ca.redapp.util.DoubleEx.valueOf(text);
				val = Convert.convertUnit(val, UnitSystem.temperature(UnitSystem.IMPERIAL), UnitSystem.temperature(UnitSystem.METRIC));
				txtFwiNoonTemp.setText(DecimalUtils.format(val, DataType.TEMPERATURE));
				text = txtFwiNoonPrecip.getText();
				val = ca.redapp.util.DoubleEx.valueOf(text);
				val = Convert.convertUnit(val, UnitSystem.distanceSmall(UnitSystem.IMPERIAL), UnitSystem.distanceSmall(UnitSystem.METRIC));
				txtFwiNoonPrecip.setText(DecimalUtils.format(val, DataType.PRECIP));
				text = txtFwiNoonWindSpeed.getText();
				val = ca.redapp.util.DoubleEx.valueOf(text);
				val = Convert.convertUnit(val, UnitSystem.speed(UnitSystem.IMPERIAL), UnitSystem.speed(UnitSystem.METRIC));
				txtFwiNoonWindSpeed.setText(DecimalUtils.format(val, DataType.WIND_SPEED));
				text = txtFwiHourlyTemp.getText();
				val = ca.redapp.util.DoubleEx.valueOf(text);
				val = Convert.convertUnit(val, UnitSystem.temperature(UnitSystem.IMPERIAL), UnitSystem.temperature(UnitSystem.METRIC));
				txtFwiHourlyTemp.setText(DecimalUtils.format(val, DataType.TEMPERATURE));
				text = txtFwiHourlyPrecip.getText();
				val = ca.redapp.util.DoubleEx.valueOf(text);
				val = Convert.convertUnit(val, UnitSystem.distanceSmall(UnitSystem.IMPERIAL), UnitSystem.distanceSmall(UnitSystem.METRIC));
				txtFwiHourlyPrecip.setText(DecimalUtils.format(val, DataType.PRECIP));
				text = txtFwiHourlyWindSpeed.getText();
				val = ca.redapp.util.DoubleEx.valueOf(text);
				val = Convert.convertUnit(val, UnitSystem.speed(UnitSystem.IMPERIAL), UnitSystem.speed(UnitSystem.METRIC));
				txtFwiHourlyWindSpeed.setText(DecimalUtils.format(val, DataType.WIND_SPEED));
			}
			lblFwiNoonTempUnit.setText(fahrenheit);
			lblFwiNoonPrecipUnit.setText(Main.resourceManager.getString("ui.label.units.in"));
			lblFwiNoonWindSpeedUnit.setText(Main.resourceManager.getString("ui.label.units.milesperhour"));
			lblFwiHourlyTempUnit.setText(Main.resourceManager.getString("ui.label.units.fahrenheit"));
			lblFwiHourlyPrecipUnit.setText(Main.resourceManager.getString("ui.label.units.in"));
			lblFwiHourlyWindSpeedUnit.setText(Main.resourceManager.getString("ui.label.units.milesperhour"));
		}
		clearOutputValuesOnForm();
		panelFwiNoon.validate();
		panelFwiNoon.repaint();
	}

	@Override
	public void onDateChanged() {
		clearOutputValuesOnForm();
		calculateSunrise();
	}

	@Override
	public void onCurrentTabChanged() { }
}


