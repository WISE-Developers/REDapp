/***********************************************************************
 * REDapp - CreateDailyDialog.java
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

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;

import java.util.Calendar;
import java.util.Date;

import ca.hss.general.DecimalUtils;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import ca.hss.times.WTime;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RGroupBox;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;
import ca.redapp.util.LineEditHelper;

public class CreateDailyDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private RButton btnCancel;
	private RButton btnSave;
	private RTextField txtMinTemp;
	private RTextField txtMaxTemp;
	private RTextField txtRH;
	private RTextField txtPrecip;
	private RTextField txtMinWS;
	private RTextField txtMaxWS;
	private RTextField txtWD;

	private RTextField txtTempAlpha;
	private RTextField txtTempBeta;
	private RTextField txtTempGamma;
	private RTextField txtWSGamma;
	private RTextField txtWSBeta;
	private RTextField txtWSAlpha;
	
	private final Double DEFAULT_TEMP_ALPHA = -0.77;
	private final Double DEFAULT_TEMP_BETA = 2.80;
	private final Double DEFAULT_TEMP_GAMMA = -2.20;
	private final Double DEFAULT_WIND_ALPHA = 1.00;
	private final Double DEFAULT_WIND_BETA = 1.24;
	private final Double DEFAULT_WIND_GAMMA = -3.59;
	
	private JSpinner spinnerDate;
	private boolean dateFixed;

	@SuppressWarnings("deprecation")
	public CreateDailyDialog(Window owner, WTime time, boolean fixDate) {
		this(owner, new Date((int)time.getYear(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST),
				(int)time.getMonth(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST),
				(int)time.getDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST)), fixDate);
	}

	/**
	 * @wbp.parser.constructor
	 */
	public CreateDailyDialog(Window owner, Date time, boolean fixDate) {
		super(owner);
		this.dateFixed = fixDate;
		initialize();
		btnCancel.addActionListener((e) -> {
			dispose();
			if (listener != null)
				listener.cancelled();
		});
		btnSave.addActionListener((e) -> {
			setVisible(false);
			if (listener != null)
				listener.accepted();
		});
		spinnerDate.setValue(time);
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

	/**
	 * Assumes all values are in metric units.
	 * @param minTemp
	 * @param maxTemp
	 * @param rh
	 * @param precip
	 * @param minWS
	 * @param maxWS
	 * @param avgWD
	 */
	public void setValues(double minTemp, double maxTemp, double rh, double precip, double minWS, double maxWS, double avgWD) {
		double val = Convert.convertUnit(minTemp, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
		txtMinTemp.setText(DecimalUtils.format(val, DecimalUtils.DataType.TEMPERATURE));
		val = Convert.convertUnit(maxTemp, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
		txtMaxTemp.setText(DecimalUtils.format(val, DecimalUtils.DataType.TEMPERATURE));
		txtRH.setText(DecimalUtils.format(rh, DecimalUtils.DataType.RH));
		val = Convert.convertUnit(precip, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
		txtPrecip.setText(DecimalUtils.format(val, DecimalUtils.DataType.PRECIP));
		val = Convert.convertUnit(minWS, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
		txtMinWS.setText(DecimalUtils.format(val, DecimalUtils.DataType.WIND_SPEED));
		val = Convert.convertUnit(maxWS, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
		txtMaxWS.setText(DecimalUtils.format(val, DecimalUtils.DataType.WIND_SPEED));
		txtWD.setText(DecimalUtils.format(avgWD, DecimalUtils.DataType.WIND_DIR));
	}

	public Double getMinTemp() {
		return Convert.convertUnit(LineEditHelper.getDoubleFromLineEdit(txtMinTemp), UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
	}

	public Double getMaxTemp() {
		return Convert.convertUnit(LineEditHelper.getDoubleFromLineEdit(txtMaxTemp), UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
	}

	public Double getRelativeHumidity() {
		return LineEditHelper.getDoubleFromLineEdit(txtRH);
	}

	public Double getPrecipitation() {
		return Convert.convertUnit(LineEditHelper.getDoubleFromLineEdit(txtPrecip), UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
	}

	public Double getMinWindSpeed() {
		return Convert.convertUnit(LineEditHelper.getDoubleFromLineEdit(txtMinWS), UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
	}

	public Double getMaxWindSpeed() {
		return Convert.convertUnit(LineEditHelper.getDoubleFromLineEdit(txtMaxWS), UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
	}

	public Double getWindDirection() {
		return LineEditHelper.getDoubleFromLineEdit(txtWD);
	}

	public Double getTempAlpha() {
		return ca.redapp.util.DoubleEx.valueOf(txtTempAlpha.getText());
	}
	
	public Double getTempBeta() {
		return ca.redapp.util.DoubleEx.valueOf(txtTempBeta.getText());
	}
	
	public Double getTempGamma() {
		return ca.redapp.util.DoubleEx.valueOf(txtTempGamma.getText());
	}
	
	public Double getWSAlpha() {
		return ca.redapp.util.DoubleEx.valueOf(txtWSAlpha.getText());
	}
	
	public Double getWSBeta() {
		return ca.redapp.util.DoubleEx.valueOf(txtWSBeta.getText());
	}
	
	public Double getWSGamma() {
		return ca.redapp.util.DoubleEx.valueOf(txtWSGamma.getText());
	}

	public Date getDate() {
		return (Date)spinnerDate.getValue();
	}

	private void initialize() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo_20.png")));
		setTitle(Main.resourceManager.getString("ui.dlg.title.create.daily"));
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 349, 580);
		else
			setBounds(0, 0, 359, 585);
		getContentPane().setLayout(null);

		JPanel panel = new JPanel(new BorderLayout(0, 0));
		panel.setBounds(10, 505, 322, 41);
		getContentPane().add(panel);

		btnCancel = new RButton(Main.resourceManager.getString("ui.label.edit.cancel"));
		btnCancel.setBounds(10, 505, 121, 41);
		panel.add(btnCancel, BorderLayout.WEST);

		btnSave = new RButton(Main.resourceManager.getString("ui.label.edit.save"));
		btnSave.setBounds(212, 505, 121, 41);
		panel.add(btnSave, BorderLayout.EAST);

		RLabel lblDate = new RLabel(Main.resourceManager.getString("ui.label.create.date"));
		lblDate.setHorizontalAlignment(SwingConstants.TRAILING);
		lblDate.setBounds(10, 20, 161, 16);
		getContentPane().add(lblDate);

		spinnerDate = new JSpinner();
		spinnerDate.setLocale(Main.resourceManager.loc);
		spinnerDate.setModel(new SpinnerDateModel(new Date(1389938400000L),
				null, null, Calendar.DAY_OF_YEAR));
		spinnerDate.setEditor(new JSpinner.DateEditor(spinnerDate,
				"MMMM d, yyyy"));
		spinnerDate.setBounds(181, 18, 113, 20);
		spinnerDate.setEnabled(!dateFixed);
		if (Main.isLinux()) {
			JComponent comp = spinnerDate.getEditor();
			if (comp instanceof JSpinner.DefaultEditor) {
				JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)comp;
				editor.getTextField().setFont(editor.getTextField().getFont().deriveFont(11.0f));
			}
		}
		getContentPane().add(spinnerDate);

		RLabel lblMinimumTemperature = new RLabel(Main.resourceManager.getString("ui.label.weather.mintemp"));
		lblMinimumTemperature.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMinimumTemperature.setBounds(10, 50, 161, 16);
		getContentPane().add(lblMinimumTemperature);

		RLabel lblMaximumTemperature = new RLabel(Main.resourceManager.getString("ui.label.weather.maxtemp"));
		lblMaximumTemperature.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMaximumTemperature.setBounds(10, 80, 161, 16);
		getContentPane().add(lblMaximumTemperature);

		RLabel lblRelativeHumidity = new RLabel(Main.resourceManager.getString("ui.label.weather.minrh"));
		lblRelativeHumidity.setHorizontalAlignment(SwingConstants.TRAILING);
		lblRelativeHumidity.setBounds(10, 110, 161, 16);
		getContentPane().add(lblRelativeHumidity);

		RLabel lblPrecipitation = new RLabel(Main.resourceManager.getString("ui.label.weather.precip"));
		lblPrecipitation.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPrecipitation.setBounds(10, 140, 161, 16);
		getContentPane().add(lblPrecipitation);

		RLabel lblMinimumWindSpeed = new RLabel(Main.resourceManager.getString("ui.label.weather.minws"));
		lblMinimumWindSpeed.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMinimumWindSpeed.setBounds(10, 170, 161, 16);
		getContentPane().add(lblMinimumWindSpeed);

		RLabel lblMaximumWindSpeed = new RLabel(Main.resourceManager.getString("ui.label.weather.maxws"));
		lblMaximumWindSpeed.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMaximumWindSpeed.setBounds(10, 200, 161, 16);
		getContentPane().add(lblMaximumWindSpeed);

		RLabel lblAverageWindDirection = new RLabel(Main.resourceManager.getString("ui.label.weather.wd"));
		lblAverageWindDirection.setHorizontalAlignment(SwingConstants.TRAILING);
		lblAverageWindDirection.setBounds(10, 230, 161, 16);
		getContentPane().add(lblAverageWindDirection);
		
		RGroupBox panel_1 = new RGroupBox();
		panel_1.setText(Main.resourceManager.getString("ui.label.stats.duirnal.temp"));
		panel_1.setBounds(65, 260, 230, 110);
		getContentPane().add(panel_1);
		
		RLabel lblStatsTempAlpha = new RLabel("\u03B1");
		lblStatsTempAlpha.setBounds(10, 20, 101, 20);
		panel_1.add(lblStatsTempAlpha);

		RLabel lblStatsTempBeta = new RLabel("\u03B2");
		lblStatsTempBeta.setBounds(10, 50, 101, 20);
		panel_1.add(lblStatsTempBeta);

		RLabel lblStatsTempGamma = new RLabel("\u03B3");
		lblStatsTempGamma.setBounds(10, 80, 101, 20);
		panel_1.add(lblStatsTempGamma);
		
		txtTempAlpha = new RTextField();
		txtTempAlpha.setBounds(117, 20, 84, 20);
		txtTempAlpha.setText(DecimalUtils.format(DEFAULT_TEMP_ALPHA));
		panel_1.add(txtTempAlpha);

		txtTempBeta = new RTextField();
		txtTempBeta.setBounds(117, 50, 84, 20);
		txtTempBeta.setText(DecimalUtils.format(DEFAULT_TEMP_BETA));
		panel_1.add(txtTempBeta);

		txtTempGamma = new RTextField();
		txtTempGamma.setBounds(117, 80, 84, 20);
		txtTempGamma.setText(DecimalUtils.format(DEFAULT_TEMP_GAMMA));
		panel_1.add(txtTempGamma);
		
		RGroupBox panel_2 = new RGroupBox();
		panel_2.setText(Main.resourceManager.getString("ui.label.stats.duirnal.ws"));
		panel_2.setBounds(65, 380, 230, 110);
		getContentPane().add(panel_2);

		RLabel lblFbpPercentDeadFirUnit = new RLabel("\u03B1");
		lblFbpPercentDeadFirUnit.setBounds(10, 20, 101, 20);
		panel_2.add(lblFbpPercentDeadFirUnit);

		RLabel label_1 = new RLabel("\u03B2");
		label_1.setBounds(10, 50, 101, 20);
		panel_2.add(label_1);

		RLabel label = new RLabel("\u03B3");
		label.setBounds(10, 80, 101, 20);
		panel_2.add(label);
		
		txtWSAlpha = new RTextField();
		txtWSAlpha.setBounds(117, 20, 84, 20);
		txtWSAlpha.setText(DecimalUtils.format(DEFAULT_WIND_ALPHA));
		panel_2.add(txtWSAlpha);

		txtWSBeta = new RTextField();
		txtWSBeta.setBounds(117, 50, 84, 20);
		txtWSBeta.setText(DecimalUtils.format(DEFAULT_WIND_BETA));
		panel_2.add(txtWSBeta);

		txtWSGamma = new RTextField();
		txtWSGamma.setBounds(117, 80, 84, 20);
		txtWSGamma.setText(DecimalUtils.format(DEFAULT_WIND_GAMMA));
		panel_2.add(txtWSGamma);
		/**/

		txtMinTemp = new RTextField();
		txtMinTemp.setBounds(181, 47, 113, 20);
		txtMinTemp.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtMinTemp);

		txtMaxTemp = new RTextField();
		txtMaxTemp.setBounds(181, 77, 113, 20);
		txtMaxTemp.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtMaxTemp);

		txtRH = new RTextField();
		txtRH.setBounds(181, 107, 113, 20);
		txtRH.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtRH);

		txtPrecip = new RTextField();
		txtPrecip.setBounds(181, 137, 113, 20);
		txtPrecip.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtPrecip);

		txtMinWS = new RTextField();
		txtMinWS.setBounds(181, 167, 113, 20);
		txtMinWS.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtMinWS);

		txtMaxWS = new RTextField();
		txtMaxWS.setBounds(181, 197, 113, 20);
		txtMaxWS.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtMaxWS);

		txtWD = new RTextField();
		txtWD.setBounds(181, 227, 113, 20);
		txtWD.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtWD);

		RLabel lblMinTempUnit;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblMinTempUnit = new RLabel(Main.resourceManager.getString("ui.label.units.celsius"));
		else
			lblMinTempUnit = new RLabel(Main.resourceManager.getString("ui.label.units.fahrenheit"));
		lblMinTempUnit.setBounds(304, 46, 20, 20);
		getContentPane().add(lblMinTempUnit);

		RLabel lblMaxTempUnit;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblMaxTempUnit = new RLabel(Main.resourceManager.getString("ui.label.units.celsius"));
		else
			lblMaxTempUnit = new RLabel(Main.resourceManager.getString("ui.label.units.fahrenheit"));
		lblMaxTempUnit.setBounds(304, 76, 20, 20);
		getContentPane().add(lblMaxTempUnit);

		RLabel lblNewLabel_2 = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		lblNewLabel_2.setBounds(304, 106, 20, 21);
		getContentPane().add(lblNewLabel_2);

		RLabel lblPrecipUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblPrecipUnits = new RLabel(Main.resourceManager.getString("ui.label.units.mm"));
		else
			lblPrecipUnits = new RLabel(Main.resourceManager.getString("ui.label.units.in"));
		lblPrecipUnits.setBounds(304, 136, 31, 20);
		getContentPane().add(lblPrecipUnits);

		RLabel lblMinWSUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblMinWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblMinWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		lblMinWSUnits.setBounds(304, 166, 41, 21);
		getContentPane().add(lblMinWSUnits);

		RLabel lblMaxWSUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblMaxWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblMaxWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		lblMaxWSUnits.setBounds(304, 196, 41, 21);
		getContentPane().add(lblMaxWSUnits);

		RLabel lblNewLabel_6 = new RLabel(Main.resourceManager.getString("ui.label.units.degrees"));
		lblNewLabel_6.setBounds(304, 226, 31, 20);
		getContentPane().add(lblNewLabel_6);
	}

	private CreateDailyDialogListener listener = null;

	public void setCreateDailyDialogListener(CreateDailyDialogListener listener) {
		this.listener = listener;
	}

	public static abstract class CreateDailyDialogListener {
		public abstract void accepted();
		public abstract void cancelled();
	}
}
