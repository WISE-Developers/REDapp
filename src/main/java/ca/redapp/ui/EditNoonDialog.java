/***********************************************************************
 * REDapp - EditNoonDialog.java
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
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

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

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.SwingConstants;

public class EditNoonDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private RLabel lblTextLabel;
	private RButton btnCancel;
	private RButton btnSave;
	private JButton btnNext;
	private JButton btnBack;
	private RTextField txtTemp;
	private RTextField txtRH;
	private RTextField txtPrecip;
	private RTextField txtWS;
	private RTextField txtWD;
	private int retval = JFileChooser.CANCEL_OPTION;
	private List<NoonWeatherData> mData;
	private WTime currentTime = null;

	public EditNoonDialog(Window owner, WTime time) {
		super(owner);
		initialize();
		setDialogPosition(owner);
		btnCancel.addActionListener((e) -> {
			retval = JFileChooser.CANCEL_OPTION;
			dispose();
		});
		btnSave.addActionListener((e) -> {
			updateData();
			retval = JFileChooser.APPROVE_OPTION;
			dispose();
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

	public int getResult() {
		return retval;
	}

	public void setValues(List<NoonWeatherData> data, WTime startTime) {
		mData = data;
		setTime(startTime);
	}

	public List<NoonWeatherData> getValues() {
		return Collections.unmodifiableList(mData);
	}

	public WTime getCurrentTime() {
		return currentTime;
	}

	private void setTime(WTime time) {
		updateData();
		for (int i = 0; i < mData.size(); i++) {
			if (mData.get(i).time.equals(time)) {
				setValues(mData.get(i));
				currentTime = mData.get(i).time;
				btnBack.setEnabled(true);
				btnNext.setEnabled(true);
				if (i == 0) {
					btnBack.setEnabled(false);
				}
				if (i == (mData.size() - 1)) {
					btnNext.setEnabled(false);
				}
				break;
			}
		}
	}

	private void setValues(NoonWeatherData data) {
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
		lblTextLabel.setText(text);
		double val = Convert.convertUnit(data.temp, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
		txtTemp.setText(DecimalUtils.format(val, DecimalUtils.DataType.TEMPERATURE));
		txtRH.setText(DecimalUtils.format(data.rh, DecimalUtils.DataType.RH));
		val = Convert.convertUnit(data.precip, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
		txtPrecip.setText(DecimalUtils.format(val, DecimalUtils.DataType.PRECIP));
		val = Convert.convertUnit(data.ws, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
		txtWS.setText(DecimalUtils.format(val, DecimalUtils.DataType.WIND_SPEED));
		txtWD.setText(DecimalUtils.format(data.wd, DecimalUtils.DataType.WIND_DIR));
	}

	private void updateData() {
		if (currentTime != null) {
			int index = -1;
			for (int i = 0; i < mData.size(); i++) {
				if (mData.get(i).time.equals(currentTime)) {
					index = i;
					break;
				}
			}
			if (index >= 0) {
				Double temp, rh, precip, ws, wd;
				temp = LineEditHelper.getDoubleFromLineEdit(txtTemp);
				if (temp == -0.0)
					temp = 0.0;
				temp= Convert.convertUnit(temp, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
				temp = DecimalUtils.formatNumber(temp, DataType.TEMPERATURE);
				rh = LineEditHelper.getDoubleFromLineEdit(txtRH);
				precip = LineEditHelper.getDoubleFromLineEdit(txtPrecip);
				precip = Convert.convertUnit(precip, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
				precip = DecimalUtils.formatNumber(precip, DataType.PRECIP);
				ws = LineEditHelper.getDoubleFromLineEdit(txtWS);
				ws = Convert.convertUnit(ws, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
				ws = DecimalUtils.formatNumber(ws, DataType.WIND_SPEED);
				wd = LineEditHelper.getDoubleFromLineEdit(txtWD);
				Double currtemp, currrh, currprecip, currws, currwd;
				currtemp = DecimalUtils.formatNumber(mData.get(index).temp, DecimalUtils.DataType.TEMPERATURE);
				currrh = DecimalUtils.formatNumber(mData.get(index).rh, DecimalUtils.DataType.RH);
				currprecip = DecimalUtils.formatNumber(mData.get(index).precip, DecimalUtils.DataType.PRECIP);
				currws = DecimalUtils.formatNumber(mData.get(index).ws, DecimalUtils.DataType.WIND_SPEED);
				currwd = DecimalUtils.formatNumber(mData.get(index).wd, DecimalUtils.DataType.WIND_DIR);
				if (temp.compareTo(currtemp) != 0 ||
						rh.compareTo(currrh) != 0 ||
						precip.compareTo(currprecip) != 0 ||
						ws.compareTo(currws) != 0 ||
						wd.compareTo(currwd) != 0) {
					mData.get(index).modified = true;
					mData.get(index).temp = temp;
					mData.get(index).rh = rh;
					mData.get(index).precip = precip;
					mData.get(index).ws = ws;
					mData.get(index).wd = wd;
				}
			}
		}
	}

	private void initialize() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo_20.png")));
		setTitle(Main.resourceManager.getString("ui.dlg.title.edit.noon"));
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 424, 276);
		else
			setBounds(0, 0, 434, 281);
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

		JPanel panel = new JPanel(new BorderLayout(0, 0));
		panel.setBounds(10, 200, 400, 41);
		getContentPane().add(panel);

		btnCancel = new RButton(Main.resourceManager.getString("ui.label.edit.cancel"));
		btnCancel.setBounds(10, 260, 121, 41);
		panel.add(btnCancel, BorderLayout.WEST);

		btnSave = new RButton(Main.resourceManager.getString("ui.label.edit.save"));
		btnSave.setBounds(212, 260, 121, 41);
		panel.add(btnSave, BorderLayout.EAST);

		lblTextLabel = new RLabel("TextLabel");
		lblTextLabel.setForeground(new Color(168, 69, 69));
		lblTextLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblTextLabel.setBounds(0, 10, 411, 20);
		getContentPane().add(lblTextLabel);

		RLabel lblTemperature = new RLabel(Main.resourceManager.getString("ui.label.weather.temp"));
		lblTemperature.setHorizontalAlignment(SwingConstants.TRAILING);
		lblTemperature.setBounds(20, 40, 161, 16);
		getContentPane().add(lblTemperature);

		txtTemp = new RTextField();
		txtTemp.setBounds(190, 38, 113, 20);
		txtTemp.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtTemp);

		RLabel lblTempUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblTempUnits = new RLabel(Main.resourceManager.getString("ui.label.units.celsius"));
		else
			lblTempUnits = new RLabel(Main.resourceManager.getString("ui.label.units.fahrenheit"));
		lblTempUnits.setBounds(310, 38, 20, 20);
		getContentPane().add(lblTempUnits);

		RLabel lblRelativeHumidity = new RLabel(Main.resourceManager.getString("ui.label.weather.rh"));
		lblRelativeHumidity.setHorizontalAlignment(SwingConstants.TRAILING);
		lblRelativeHumidity.setBounds(20, 70, 161, 16);
		getContentPane().add(lblRelativeHumidity);

		txtRH = new RTextField();
		txtRH.setBounds(190, 68, 113, 20);
		txtRH.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtRH);

		RLabel lblNewLabel_2 = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		lblNewLabel_2.setBounds(310, 68, 20, 21);
		getContentPane().add(lblNewLabel_2);

		RLabel lblPrecipitation = new RLabel(Main.resourceManager.getString("ui.label.weather.precip"));
		lblPrecipitation.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPrecipitation.setBounds(20, 100, 161, 16);
		getContentPane().add(lblPrecipitation);

		txtPrecip = new RTextField();
		txtPrecip.setBounds(190, 98, 113, 20);
		txtPrecip.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtPrecip);

		RLabel lblPrecipUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblPrecipUnits = new RLabel(Main.resourceManager.getString("ui.label.units.mm"));
		else
			lblPrecipUnits = new RLabel(Main.resourceManager.getString("ui.label.units.in"));
		lblPrecipUnits.setBounds(310, 98, 31, 20);
		getContentPane().add(lblPrecipUnits);

		RLabel lblWindSpeed = new RLabel(Main.resourceManager.getString("ui.label.weather.ws"));
		lblWindSpeed.setHorizontalAlignment(SwingConstants.TRAILING);
		lblWindSpeed.setBounds(20, 130, 161, 16);
		getContentPane().add(lblWindSpeed);

		txtWS = new RTextField();
		txtWS.setBounds(190, 128, 113, 20);
		txtWS.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtWS);

		RLabel lblWSUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		lblWSUnits.setBounds(310, 128, 41, 21);
		getContentPane().add(lblWSUnits);

		RLabel lblWindDirection = new RLabel(Main.resourceManager.getString("ui.label.weather.wd"));
		lblWindDirection.setHorizontalAlignment(SwingConstants.TRAILING);
		lblWindDirection.setBounds(20, 160, 161, 16);
		getContentPane().add(lblWindDirection);

		txtWD = new RTextField();
		txtWD.setBounds(190, 158, 113, 20);
		txtWD.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtWD);

		RLabel lblNewLabel_6 = new RLabel(Main.resourceManager.getString("ui.label.units.degrees"));
		lblNewLabel_6.setBounds(310, 158, 31, 20);
		getContentPane().add(lblNewLabel_6);
	}

	public static class NoonWeatherData {
		public WTime time;
		public double temp;
		public double rh;
		public double precip;
		public double ws;
		public double wd;

		public boolean modified = false;
	}
}
