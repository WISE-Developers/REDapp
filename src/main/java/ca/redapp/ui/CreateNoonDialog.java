/***********************************************************************
 * REDapp - CreateNoonDialog.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ca.hss.general.DecimalUtils;
import ca.hss.general.OutVariable;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import ca.hss.times.WTime;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;
import ca.redapp.util.LineEditHelper;

public class CreateNoonDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private RButton btnCancel;
	private RButton btnSave;
	private RLabel lblTextlabel;
	private RTextField txtTemp;
	private RTextField txtRH;
	private RTextField txtPrecip;
	private RTextField txtWS;
	private RTextField txtWD;
	private WTime time;

	public CreateNoonDialog(Window owner, WTime time) {
		super(owner);
		this.time = time;
		initialize();
		btnCancel.addActionListener((e) -> {
			dispose();
			if (listener != null)
				listener.cancelled(CreateNoonDialog.this);
		});
		btnSave.addActionListener((e) -> {
			if (listener != null)
				listener.accepted(CreateNoonDialog.this);
		});
		String text;
		switch ((int)time.getMonth(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST)) {
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
		text += " " + time.getDay(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
		text += " " + time.getYear(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_WITHDST);
		lblTextlabel.setText(text);
		setDialogPosition(owner);
	}
	
	/**
	 * Get the temperature in celsius.
	 * @return
	 */
	public boolean getTemperature(OutVariable<Double> val) {
		Double v = LineEditHelper.getDoubleFromLineEdit(txtTemp);
		if (v == null)
			return false;
		val.value = Convert.convertUnit(v, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
		return true;
	}
	
	public boolean getRH(OutVariable<Double> val) {
		Double v = LineEditHelper.getDoubleFromLineEdit(txtRH);
		if (v == null)
			return false;
		val.value = v / 100.0;
		return true;
	}
	
	public boolean getPrecipitation(OutVariable<Double> val) {
		Double v = LineEditHelper.getDoubleFromLineEdit(txtPrecip);
		if (v == null)
			return false;
		val.value = Convert.convertUnit(v, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
		return true;
	}
	
	public boolean getWindSpeed(OutVariable<Double> val) {
		Double v = LineEditHelper.getDoubleFromLineEdit(txtWS);
		if (v == null)
			return false;
		val.value = Convert.convertUnit(v, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
		return true;
	}
	
	public boolean getWindDirection(OutVariable<Double> val) {
		Double v = LineEditHelper.getDoubleFromLineEdit(txtWD);
		if (v == null)
			return false;
		val.value = DEGREE_TO_RADIAN(COMPASS_TO_CARTESIAN_DEGREE(v));
		return true;
	}
	
	public WTime getTime() {
		return time;
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

	private void initialize() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo_20.png")));
		setTitle(Main.resourceManager.getString("ui.dlg.title.create.noon"));
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 349, 276);
		else
			setBounds(0, 0, 359, 281);
		getContentPane().setLayout(null);

		lblTextlabel = new RLabel("TextLabel");
		lblTextlabel.setForeground(new Color(168, 69, 69));
		lblTextlabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblTextlabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblTextlabel.setBounds(0, 10, 349, 20);
		getContentPane().add(lblTextlabel);

		JPanel panel = new JPanel(new BorderLayout(0, 0));
		panel.setBounds(10, 200, 322, 41);
		getContentPane().add(panel);

		btnCancel = new RButton(Main.resourceManager.getString("ui.label.edit.cancel"));
		btnCancel.setBounds(10, 260, 121, 41);
		panel.add(btnCancel, BorderLayout.WEST);

		btnSave = new RButton(Main.resourceManager.getString("ui.label.edit.save"));
		btnSave.setBounds(212, 260, 121, 41);
		panel.add(btnSave, BorderLayout.EAST);

		RLabel lblTemperature = new RLabel(Main.resourceManager.getString("ui.label.weather.temp"));
		lblTemperature.setHorizontalAlignment(SwingConstants.TRAILING);
		lblTemperature.setBounds(10, 50, 131, 16);
		getContentPane().add(lblTemperature);

		txtTemp = new RTextField();
		txtTemp.setBounds(151, 47, 113, 20);
		txtTemp.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtTemp);

		RLabel lblTempUnit;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblTempUnit = new RLabel(Main.resourceManager.getString("ui.label.units.celsius"));
		else
			lblTempUnit = new RLabel(Main.resourceManager.getString("ui.label.units.fahrenheit"));
		lblTempUnit.setBounds(274, 46, 20, 20);
		getContentPane().add(lblTempUnit);

		RLabel lblRelativeHumidity = new RLabel(Main.resourceManager.getString("ui.label.weather.rh"));
		lblRelativeHumidity.setHorizontalAlignment(SwingConstants.TRAILING);
		lblRelativeHumidity.setBounds(10, 80, 131, 16);
		getContentPane().add(lblRelativeHumidity);

		txtRH = new RTextField();
		txtRH.setBounds(151, 78, 113, 20);
		txtRH.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtRH);

		RLabel lblNewLabel_2 = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		lblNewLabel_2.setBounds(274, 78, 20, 21);
		getContentPane().add(lblNewLabel_2);

		RLabel lblPrecipitation = new RLabel(Main.resourceManager.getString("ui.label.weather.precip"));
		lblPrecipitation.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPrecipitation.setBounds(10, 110, 131, 16);
		getContentPane().add(lblPrecipitation);

		txtPrecip = new RTextField();
		txtPrecip.setBounds(151, 108, 113, 20);
		txtPrecip.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtPrecip);

		RLabel lblPrecipUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblPrecipUnits = new RLabel(Main.resourceManager.getString("ui.label.units.mm"));
		else
			lblPrecipUnits = new RLabel(Main.resourceManager.getString("ui.label.units.in"));
		lblPrecipUnits.setBounds(274, 108, 31, 20);
		getContentPane().add(lblPrecipUnits);

		RLabel lblMaximumWindSpeed = new RLabel(Main.resourceManager.getString("ui.label.weather.ws"));
		lblMaximumWindSpeed.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMaximumWindSpeed.setBounds(10, 140, 131, 16);
		getContentPane().add(lblMaximumWindSpeed);

		txtWS = new RTextField();
		txtWS.setBounds(151, 138, 113, 20);
		txtWS.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtWS);

		RLabel lblWSUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		lblWSUnits.setBounds(274, 138, 41, 21);
		getContentPane().add(lblWSUnits);

		RLabel lblAverageWindDirection = new RLabel(Main.resourceManager.getString("ui.label.weather.wd"));
		lblAverageWindDirection.setHorizontalAlignment(SwingConstants.TRAILING);
		lblAverageWindDirection.setBounds(10, 170, 131, 16);
		getContentPane().add(lblAverageWindDirection);

		txtWD = new RTextField();
		txtWD.setBounds(151, 168, 113, 20);
		txtWD.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.FORCE_ATMOST_2));
		getContentPane().add(txtWD);

		RLabel lblNewLabel_6 = new RLabel(Main.resourceManager.getString("ui.label.units.degrees"));
		lblNewLabel_6.setBounds(274, 168, 31, 20);
		getContentPane().add(lblNewLabel_6);
	}

	private CreateNoonDialogListener listener = null;

	public void setCreateNoonDialogListener(CreateNoonDialogListener listener) {
		this.listener = listener;
	}

	public static abstract class CreateNoonDialogListener {
		public abstract void accepted(CreateNoonDialog dlg);
		public abstract void cancelled(CreateNoonDialog dlg);
	}
}
