/***********************************************************************
 * REDapp - EditDailyDialog.java
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
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.Font;

import static ca.redapp.util.LineEditHelper.lineEditHandleError;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collections;
import java.util.List;

import ca.hss.general.DecimalUtils;
import ca.hss.general.DecimalUtils.DataType;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import ca.hss.times.WTime;
import ca.hss.times.WTimeSpan;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;
import ca.redapp.util.LineEditHelper;

public class EditDailyDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private RLabel lblTextlabel;
	private RButton btnCancel;
	private RButton btnEditHourly;
	private RButton btnSave;
	private JButton btnNext;
	private JButton btnBack;
	private RTextField txtMinTemp;
	private RTextField txtMaxTemp;
	private RTextField txtRH;
	private RTextField txtPrecip;
	private RTextField txtMinWS;
	private RTextField txtMaxWS;
	private RTextField txtWD;
	private int retval = JFileChooser.APPROVE_OPTION;
	private boolean editHourly = false;
	private List<DailyWeatherData> mdata;
	private WTime currentTime = null;

	public EditDailyDialog(Window owner, WTime time) {
		super(owner);
		initialize();
		btnCancel.addActionListener((e) -> {
			retval = JFileChooser.CANCEL_OPTION;
			dispose();
		});
		btnEditHourly.addActionListener((e) -> {
			editHourly = true;
			dispose();
		});
		btnSave.addActionListener((e) -> {
			if (validatedInputs()) {
				updateData();
				dispose();
			}
		});
		btnNext.addActionListener((e) -> {
			if (currentTime != null) {
				setTime(WTime.add(currentTime, new WTimeSpan(1, 0, 0, 0)));
			}
		});
		btnBack.addActionListener((e) -> {
			if (currentTime != null) {
				setTime(WTime.subtract(currentTime, new WTimeSpan(1, 0, 0, 0)));
			}
		});
		setDialogPosition(owner);
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

	public boolean getEditHourly() {
		return editHourly;
	}

	public int getResult() {
		return retval;
	}

	public void setValues(List<DailyWeatherData> data, WTime startTime) {
		mdata = data;
		setTime(startTime);
	}

	public List<DailyWeatherData> getValues() {
		return Collections.unmodifiableList(mdata);
	}

	public WTime getCurrentTime() {
		return currentTime;
	}

	private void setTime(WTime time) {
		updateData();
		for (int i = 0; i < mdata.size(); i++) {
			if (mdata.get(i).time.equals(time)) {
				setValues(mdata.get(i));
				currentTime = mdata.get(i).time;
				btnBack.setEnabled(true);
				btnNext.setEnabled(true);
				if (i == 0) {
					btnBack.setEnabled(false);
				}
				if (i == (mdata.size() - 1)) {
					btnNext.setEnabled(false);
				}
				break;
			}
		}
	}

	private void setValues(DailyWeatherData data) {
		String text;
		switch ((int)data.time.getMonth(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST)) {
		case 1:
			text = Main.resourceManager.getString("ui.label.month.jan");
			break;
		case 2:
			text = Main.resourceManager.getString("ui.label.month.feb");
			break;
		case 3:
			text = Main.resourceManager.getString("ui.label.month.mar");
			break;
		case 4:
			text = Main.resourceManager.getString("ui.label.month.apr");
			break;
		case 5:
			text = Main.resourceManager.getString("ui.label.month.may");
			break;
		case 6:
			text = Main.resourceManager.getString("ui.label.month.jun");
			break;
		case 7:
			text = Main.resourceManager.getString("ui.label.month.jul");
			break;
		case 8:
			text = Main.resourceManager.getString("ui.label.month.aug");
			break;
		case 9:
			text = Main.resourceManager.getString("ui.label.month.sep");
			break;
		case 10:
			text = Main.resourceManager.getString("ui.label.month.oct");
			break;
		case 11:
			text = Main.resourceManager.getString("ui.label.month.nov");
			break;
		default:
			text = Main.resourceManager.getString("ui.label.month.dec");
			break;
		}
		text += " " + data.time.getDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
		text += " " + data.time.getYear(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
		lblTextlabel.setText(text);
		double val = Convert.convertUnit(data.minTemp, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
		txtMinTemp.setText(DecimalUtils.format(val, DecimalUtils.DataType.TEMPERATURE));
		val = Convert.convertUnit(data.maxTemp, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
		txtMaxTemp.setText(DecimalUtils.format(val, DecimalUtils.DataType.TEMPERATURE));
		txtRH.setText(DecimalUtils.format(data.rh, DecimalUtils.DataType.RH));
		val = Convert.convertUnit(data.precip, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
		txtPrecip.setText(DecimalUtils.format(val, DecimalUtils.DataType.PRECIP));
		val = Convert.convertUnit(data.minWS, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
		txtMinWS.setText(DecimalUtils.format(val, DecimalUtils.DataType.WIND_SPEED));
		val = Convert.convertUnit(data.maxWS, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
		txtMaxWS.setText(DecimalUtils.format(val, DecimalUtils.DataType.WIND_SPEED));
		txtWD.setText(DecimalUtils.format(data.wd, DecimalUtils.DataType.WIND_DIR));
	}

	private void updateData() {
		if (currentTime != null) {
			int index = -1;
			for (int i = 0; i < mdata.size(); i++) {
				if (mdata.get(i).time.equals(currentTime)) {
					index = i;
					break;
				}
			}
			if (index >= 0) {
				Double tempmin, tempmax, rh, precip, wsmin, wsmax, wd;
				tempmin = LineEditHelper.getDoubleFromLineEdit(txtMinTemp);
				if (tempmin == -0.0)
					tempmin = 0.0;
				tempmin = Convert.convertUnit(tempmin, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
				tempmin = DecimalUtils.formatNumber(tempmin, DataType.TEMPERATURE);
				tempmax = LineEditHelper.getDoubleFromLineEdit(txtMaxTemp);
				tempmax = Convert.convertUnit(tempmax, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
				tempmax = DecimalUtils.formatNumber(tempmax, DataType.TEMPERATURE);
				rh = LineEditHelper.getDoubleFromLineEdit(txtRH);
				precip = LineEditHelper.getDoubleFromLineEdit(txtPrecip);
				precip = Convert.convertUnit(precip, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
				precip = DecimalUtils.formatNumber(precip, DataType.PRECIP);
				wsmin = LineEditHelper.getDoubleFromLineEdit(txtMinWS);
				wsmin = Convert.convertUnit(wsmin, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
				wsmin = DecimalUtils.formatNumber(wsmin, DataType.WIND_SPEED);
				wsmax = LineEditHelper.getDoubleFromLineEdit(txtMaxWS);
				wsmax = Convert.convertUnit(wsmax, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
				wsmax = DecimalUtils.formatNumber(wsmax, DataType.WIND_SPEED);
				wd = LineEditHelper.getDoubleFromLineEdit(txtWD);
				Double currtempmin, currtempmax, currrh, currprecip, currwsmin, currwsmax, currwd;
				currtempmin = DecimalUtils.formatNumber(mdata.get(index).minTemp, DecimalUtils.DataType.TEMPERATURE);
				currtempmax = DecimalUtils.formatNumber(mdata.get(index).maxTemp, DecimalUtils.DataType.TEMPERATURE);
				currrh = DecimalUtils.formatNumber(mdata.get(index).rh, DecimalUtils.DataType.RH);
				currprecip = DecimalUtils.formatNumber(mdata.get(index).precip, DecimalUtils.DataType.PRECIP);
				currwsmin = DecimalUtils.formatNumber(mdata.get(index).minWS, DecimalUtils.DataType.WIND_SPEED);
				currwsmax = DecimalUtils.formatNumber(mdata.get(index).maxWS, DecimalUtils.DataType.WIND_SPEED);
				currwd = DecimalUtils.formatNumber(mdata.get(index).wd, DecimalUtils.DataType.WIND_DIR);
				if (tempmin.compareTo(currtempmin) != 0 ||
						tempmax.compareTo(currtempmax) != 0 ||
						rh.compareTo(currrh) != 0 ||
						precip.compareTo(currprecip) != 0 ||
						wsmin.compareTo(currwsmin) != 0 ||
						wsmax.compareTo(currwsmax) != 0 ||
						wd.compareTo(currwd) != 0) {
					mdata.get(index).modified = true;
					mdata.get(index).minTemp = tempmin;
					mdata.get(index).maxTemp = tempmax;
					mdata.get(index).rh = rh;
					mdata.get(index).precip = precip;
					mdata.get(index).minWS = wsmin;
					mdata.get(index).maxWS = wsmax;
					mdata.get(index).wd = wd;
				}
			}
		}
	}

	private void initialize() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo_20.png")));
		setTitle(Main.resourceManager.getString("ui.dlg.title.edit.daily"));
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 424, 336);
		else
			setBounds(0, 0, 434, 341);
		getContentPane().setLayout(null);

		btnNext = new JButton("");
		btnNext.setBounds(370, 3, 30, 30);
		btnNext.setIcon(new ImageIcon(Main.class
				.getResource("/images/icons/forward_hour.png")));
		btnNext.setRolloverIcon(new ImageIcon(Main.class
				.getResource("/images/icons/forward_hour_hover.png")));
		btnNext.setDisabledIcon(new ImageIcon(Main.class
				.getResource("/images/icons/forward_hour_disabled.png")));
		btnNext.setRolloverEnabled(true);
		btnNext.setFocusPainted(false);
		btnNext.setContentAreaFilled(false);
		btnNext.setBorderPainted(false);
		getContentPane().add(btnNext);

		btnBack = new JButton("");
		btnBack.setEnabled(false);
		btnBack.setBounds(10, 3, 30, 30);
		btnBack.setIcon(new ImageIcon(Main.class
				.getResource("/images/icons/back_hour.png")));
		btnBack.setRolloverIcon(new ImageIcon(Main.class
				.getResource("/images/icons/back_hour_hover.png")));
		btnBack.setDisabledIcon(new ImageIcon(Main.class
				.getResource("/images/icons/back_hour_disabled.png")));
		btnBack.setRolloverEnabled(true);
		btnBack.setFocusPainted(false);
		btnBack.setContentAreaFilled(false);
		btnBack.setBorderPainted(false);
		getContentPane().add(btnBack);

		JPanel panel1 = new JPanel(new BorderLayout(0, 0));
		panel1.setBounds(0, 250, 424, 45);
		getContentPane().add(panel1);

		btnCancel = new RButton(Main.resourceManager.getString("ui.label.edit.cancel"));
		btnCancel.setBounds(14, 250, 121, 41);
		panel1.add(btnCancel, BorderLayout.WEST);

		btnEditHourly = new RButton(Main.resourceManager.getString("ui.label.edit.edithourly"));
		btnEditHourly.setBounds(144, 250, 121, 41);
		panel1.add(btnEditHourly, BorderLayout.CENTER);

		btnSave = new RButton(Main.resourceManager.getString("ui.label.edit.save"));
		btnSave.setBounds(274, 250, 121, 41);
		panel1.add(btnSave, BorderLayout.EAST);

		lblTextlabel = new RLabel("TextLabel");
		lblTextlabel.setForeground(new Color(168, 69, 69));
		lblTextlabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblTextlabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblTextlabel.setBounds(0, 10, 411, 20);
		getContentPane().add(lblTextlabel);

		RLabel lblMinimumTemperature = new RLabel(Main.resourceManager.getString("ui.label.weather.mintemp"));
		lblMinimumTemperature.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMinimumTemperature.setBounds(20, 40, 161, 16);
		getContentPane().add(lblMinimumTemperature);

		RLabel lblMaximumTemperature = new RLabel(Main.resourceManager.getString("ui.label.weather.maxtemp"));
		lblMaximumTemperature.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMaximumTemperature.setBounds(20, 70, 161, 16);
		getContentPane().add(lblMaximumTemperature);

		RLabel lblRelativeHumidity = new RLabel(Main.resourceManager.getString("ui.label.weather.minrh"));
		lblRelativeHumidity.setHorizontalAlignment(SwingConstants.TRAILING);
		lblRelativeHumidity.setBounds(20, 100, 161, 16);
		getContentPane().add(lblRelativeHumidity);

		RLabel lblPrecipitation = new RLabel(Main.resourceManager.getString("ui.label.weather.precip"));
		lblPrecipitation.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPrecipitation.setBounds(20, 130, 161, 16);
		getContentPane().add(lblPrecipitation);

		RLabel lblMinimumWindSpeed = new RLabel(Main.resourceManager.getString("ui.label.weather.minws"));
		lblMinimumWindSpeed.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMinimumWindSpeed.setBounds(20, 160, 161, 16);
		getContentPane().add(lblMinimumWindSpeed);

		RLabel lblMaximumWindSpeed = new RLabel(Main.resourceManager.getString("ui.label.weather.maxws"));
		lblMaximumWindSpeed.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMaximumWindSpeed.setBounds(20, 190, 161, 16);
		getContentPane().add(lblMaximumWindSpeed);

		RLabel lblAverageWindDirection = new RLabel(Main.resourceManager.getString("ui.label.edit.wd"));
		lblAverageWindDirection.setHorizontalAlignment(SwingConstants.TRAILING);
		lblAverageWindDirection.setBounds(20, 220, 161, 16);
		getContentPane().add(lblAverageWindDirection);

		txtMinTemp = new RTextField();
		txtMinTemp.setBounds(190, 38, 113, 20);
		txtMinTemp.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtMinTemp);

		txtMaxTemp = new RTextField();
		txtMaxTemp.setBounds(190, 68, 113, 20);
		txtMaxTemp.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtMaxTemp);

		txtRH = new RTextField();
		txtRH.setBounds(190, 98, 113, 20);
		txtRH.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtRH);

		txtPrecip = new RTextField();
		txtPrecip.setBounds(190, 128, 113, 20);
		txtPrecip.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtPrecip);

		txtMinWS = new RTextField();
		txtMinWS.setBounds(190, 158, 113, 20);
		txtMinWS.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtMinWS);

		txtMaxWS = new RTextField();
		txtMaxWS.setBounds(190, 188, 113, 20);
		txtMaxWS.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtMaxWS);

		txtWD = new RTextField();
		txtWD.setBounds(190, 218, 113, 20);
		txtWD.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtWD);

		RLabel lblMinTempUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblMinTempUnits = new RLabel(Main.resourceManager.getString("ui.label.units.celsius"));
		else
			lblMinTempUnits = new RLabel(Main.resourceManager.getString("ui.label.units.fahrenheit"));
		lblMinTempUnits.setBounds(310, 38, 20, 20);
		getContentPane().add(lblMinTempUnits);

		RLabel lblMaxTempUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblMaxTempUnits = new RLabel(Main.resourceManager.getString("ui.label.units.celsius"));
		else
			lblMaxTempUnits = new RLabel(Main.resourceManager.getString("ui.label.units.fahrenheit"));
		lblMaxTempUnits.setBounds(310, 68, 20, 20);
		getContentPane().add(lblMaxTempUnits);

		RLabel lblNewLabel_2 = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		lblNewLabel_2.setBounds(310, 98, 20, 21);
		getContentPane().add(lblNewLabel_2);

		RLabel lblPrecipUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblPrecipUnits = new RLabel(Main.resourceManager.getString("ui.label.units.mm"));
		else
			lblPrecipUnits = new RLabel(Main.resourceManager.getString("ui.label.units.in"));
		lblPrecipUnits.setBounds(310, 128, 31, 20);
		getContentPane().add(lblPrecipUnits);

		RLabel lblMinWSUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblMinWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblMinWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		lblMinWSUnits.setBounds(310, 158, 41, 21);
		getContentPane().add(lblMinWSUnits);

		RLabel lblMaxWSUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblMaxWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblMaxWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		lblMaxWSUnits.setBounds(310, 188, 41, 21);
		getContentPane().add(lblMaxWSUnits);

		RLabel lblNewLabel_6 = new RLabel(Main.resourceManager.getString("ui.label.units.degrees"));
		lblNewLabel_6.setBounds(310, 218, 31, 20);
		getContentPane().add(lblNewLabel_6);
	}

	public static class DailyWeatherData {
		public WTime time;
		public double minTemp;
		public double maxTemp;
		public double rh;
		public double precip;
		public double minWS;
		public double maxWS;
		public double wd;

		public boolean modified = false;
	}
	
	private boolean validatedInputs() {
		boolean validated = true;
		double d = 0;
		double d1 = 0;
		
		d = ca.redapp.util.DoubleEx.valueOf(txtMinTemp.getText());
		if (Main.unitSystem() != UnitSystem.METRIC) 
			d = Convert.convertUnit(d, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
		d = DecimalUtils.formatNumber(d, DecimalUtils.DataType.FORCE_2);
		if (d < -50.0 || d > 45.0) {
			validated  = false;
			Double min = Convert.convertUnit(-50.0, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
			Double max = Convert.convertUnit(45.0, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
			String unit;
			if (Main.unitSystem() == UnitSystem.METRIC)
				unit = Main.resourceManager.getString("ui.label.units.celsius");
			else
				unit = Main.resourceManager.getString("ui.label.units.fahrenheit");
			
			lineEditHandleError(txtMinTemp, Main.resourceManager.getString("ui.label.range.temp", DecimalUtils.format(min, DataType.TEMPERATURE), DecimalUtils.format(max, DataType.TEMPERATURE), unit));
		}
		
		d1 = ca.redapp.util.DoubleEx.valueOf(txtMaxTemp.getText());
		if (Main.unitSystem() != UnitSystem.METRIC) 
			d1 = Convert.convertUnit(d, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
		d1 = DecimalUtils.formatNumber(d1, DecimalUtils.DataType.FORCE_2);
		if (d1 < -50.0 || d1 > 45.0) {
			validated  = false;
			Double min = Convert.convertUnit(-50.0, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
			Double max = Convert.convertUnit(45.0, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
			String unit;
			if (Main.unitSystem() == UnitSystem.METRIC)
				unit = Main.resourceManager.getString("ui.label.units.celsius");
			else
				unit = Main.resourceManager.getString("ui.label.units.fahrenheit");
			
			lineEditHandleError(txtMaxTemp, Main.resourceManager.getString("ui.label.range.temp", DecimalUtils.format(min, DataType.TEMPERATURE), DecimalUtils.format(max, DataType.TEMPERATURE), unit));
		} else if (d1 < d) {
			validated = false;
			lineEditHandleError(txtMaxTemp, Main.resourceManager.getString("ui.label.range.minmaxtemp"));
		}
		
		d = ca.redapp.util.DoubleEx.valueOf(txtRH.getText());
		if (d < 0.0 || d > 100.0) {
			validated = false;
			lineEditHandleError(txtRH, Main.resourceManager.getString("ui.label.range.rh"));
		}
		
		d = ca.redapp.util.DoubleEx.valueOf(txtPrecip.getText());
		if (Main.unitSystem() != UnitSystem.METRIC) 
			d = Convert.convertUnit(d, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
		d = DecimalUtils.formatNumber(d, DecimalUtils.DataType.FORCE_2);
		if (d < 0.0 || d > 300.0) {
			validated = false;
			Double min = Convert.convertUnit(0.0, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
			Double max = Convert.convertUnit(300.0, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
			String unit;
			if (Main.unitSystem() == UnitSystem.METRIC)
				unit = Main.resourceManager.getString("ui.label.units.mm");
			else
				unit = Main.resourceManager.getString("ui.label.units.in");
			lineEditHandleError(txtPrecip, Main.resourceManager.getString("ui.label.range.precip", DecimalUtils.format(min, DataType.PRECIP), DecimalUtils.format(max, DataType.PRECIP), unit));
		}
		
		d = ca.redapp.util.DoubleEx.valueOf(txtMinWS.getText());
		if (Main.unitSystem() != UnitSystem.METRIC) 
			d = Convert.convertUnit(d, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
		d = DecimalUtils.formatNumber(d, DecimalUtils.DataType.FORCE_2);
		if (d < 0.0 || d > 200.0) {
			validated = false;
			Double min = Convert.convertUnit(0.0, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
			Double max = Convert.convertUnit(100.0, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
			String unit;
			if (Main.unitSystem() == UnitSystem.METRIC)
				unit = Main.resourceManager.getString("ui.label.units.kph");
			else
				unit = Main.resourceManager.getString("ui.label.units.mph");
			lineEditHandleError(txtMinWS, Main.resourceManager.getString("ui.label.range.ws", DecimalUtils.format(min, DataType.WIND_SPEED), DecimalUtils.format(max, DataType.WIND_SPEED), unit));
		}
		
		d1 = ca.redapp.util.DoubleEx.valueOf(txtMaxWS.getText());
		if (Main.unitSystem() != UnitSystem.METRIC) 
			d = Convert.convertUnit(d1, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
		d1 = DecimalUtils.formatNumber(d1, DecimalUtils.DataType.FORCE_2);
		if (d1 < 0.0 || d1 > 200.0) {
			validated = false;
			Double min = Convert.convertUnit(0.0, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
			Double max = Convert.convertUnit(100.0, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
			String unit;
			if (Main.unitSystem() == UnitSystem.METRIC)
				unit = Main.resourceManager.getString("ui.label.units.kph");
			else
				unit = Main.resourceManager.getString("ui.label.units.mph");
			lineEditHandleError(txtMaxWS, Main.resourceManager.getString("ui.label.range.ws", DecimalUtils.format(min, DataType.WIND_SPEED), DecimalUtils.format(max, DataType.WIND_SPEED), unit));
		} else if (d1 < d) {
			validated = false;
			lineEditHandleError(txtMaxWS, Main.resourceManager.getString("ui.label.range.minmaxws"));
		}
		
		d = ca.redapp.util.DoubleEx.valueOf(txtWD.getText());
		if (d < 0.0 || d > 360.0) {
			validated = false;
			lineEditHandleError(txtWD, Main.resourceManager.getString("ui.label.range.wd"));
		}
		
		return validated;
	}
}
