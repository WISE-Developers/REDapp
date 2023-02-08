/***********************************************************************
 * REDapp - EditHourlyDialog.java
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

import static ca.redapp.util.LineEditHelper.lineEditHandleError;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.wise.grid.IWXData;
import ca.hss.general.DecimalUtils;
import ca.hss.general.DecimalUtils.DataType;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import ca.hss.times.WTime;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;
import ca.redapp.util.LineEditHelper;

public class EditHourlyDialog extends JDialog {
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
	private int hour = 0;
	private WTime time = null;
	private IWXData[] hours = null;
	private boolean hasChanged = false;
	private int retval = JFileChooser.CANCEL_OPTION;
	private AtomicInteger changingHours = new AtomicInteger(0);
	private int firstHour;
	private int lastHour;

	/**
	 * Create a new EditHourlyDialog instance.
	 * 
	 * @param owner The dialogs owner view.
	 * @param time The time to edit.
	 * @param hours The current weather data.
	 * @param firsthour The first hour of the day that is editable.
	 * @param lasthour The last hour of the day that is editable.
	 */
	public EditHourlyDialog(Window owner, WTime time, IWXData[] hours, int firsthour, int lasthour) {
		super(owner);
		initialize();
		this.hours = hours;
		this.time = new WTime(time);
		this.firstHour = firsthour;
		this.lastHour = lasthour;
		btnCancel.addActionListener((e) -> cancel());
		btnSave.addActionListener((e) -> save());
		btnNext.addActionListener((e) -> next());
		btnBack.addActionListener((e) -> back());

		txtTemp.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				temperatureChanged(txtTemp.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				temperatureChanged(txtTemp.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				temperatureChanged(txtTemp.getText());
			}
		});
		txtRH.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				rhChanged(txtRH.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				rhChanged(txtRH.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				rhChanged(txtRH.getText());
			}
		});
		txtPrecip.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				precipChanged(txtPrecip.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				precipChanged(txtPrecip.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				precipChanged(txtPrecip.getText());
			}
		});
		txtWS.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				wsChanged(txtWS.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				wsChanged(txtWS.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				wsChanged(txtWS.getText());
			}
		});
		txtWD.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				wdChanged(txtWD.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				wdChanged(txtWD.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				wdChanged(txtWD.getText());
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

	private void temperatureChanged(String newText) {
		if (changingHours.intValue() == 0) {
			if (newText.length() > 0) {
				Double d = LineEditHelper.getDoubleFromLineEdit(txtTemp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
					if (d.doubleValue() != hours[hour].temperature) {
						hours[hour].temperature = d;
						hasChanged = true;
					}
				}
			}
		}
	}

	private void rhChanged(String newText) {
		if (changingHours.intValue() == 0) {
			if (newText.length() > 0) {
				Double d = LineEditHelper.getDoubleFromLineEdit(txtRH);
				if (d != null && d.doubleValue() != hours[hour].rh) {
					hours[hour].rh = d;
					hasChanged = true;
				}
			}
		}
	}

	private void precipChanged(String newText) {
		if (changingHours.intValue() == 0) {
			if (newText.length() > 0) {
				Double d = LineEditHelper.getDoubleFromLineEdit(txtPrecip);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
					if (d.doubleValue() != hours[hour].precipitation) {
						hours[hour].precipitation = d;
						hasChanged = true;
					}
				}
			}
		}
	}

	private void wsChanged(String newText) {
		if (changingHours.intValue() == 0) {
			if (newText.length() > 0) {
				Double d = LineEditHelper.getDoubleFromLineEdit(txtWS);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
					if (d.doubleValue() != hours[hour].windSpeed) {
						hours[hour].windSpeed = d;
						hasChanged = true;
					}
				}
			}
		}
	}

	private void wdChanged(String newText) {
		if (changingHours.intValue() == 0) {
			if (newText.length() > 0) {
				Double d = LineEditHelper.getDoubleFromLineEdit(txtWD);
				if (d != null && d.doubleValue() != hours[hour].windDirection) {
					hours[hour].windDirection = d;
					hasChanged = true;
				}
			}
		}
	}

	public void setHour(int hour) {
		this.hour = hour;
		if (hour < lastHour)
			btnNext.setEnabled(true);
		else
			btnNext.setEnabled(false);
		if (hour > firstHour)
			btnBack.setEnabled(true);
		else
			btnBack.setEnabled(false);
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
		text += " " + hour + ":00";
		lblTextLabel.setText(text);
		changingHours.incrementAndGet();
		double val = hours[hour].temperature;
		val = Convert.convertUnit(val, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
		txtTemp.setText(DecimalUtils.format(val, DecimalUtils.DataType.TEMPERATURE));
		txtRH.setText(DecimalUtils.format(hours[hour].rh, DecimalUtils.DataType.RH));
		val = hours[hour].precipitation;
		val = Convert.convertUnit(val, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
		txtPrecip.setText(DecimalUtils.format(val, DecimalUtils.DataType.PRECIP));
		val = hours[hour].windSpeed;
		val = Convert.convertUnit(val, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
		txtWS.setText(DecimalUtils.format(val, DecimalUtils.DataType.WIND_SPEED));
		txtWD.setText(DecimalUtils.format(hours[hour].windDirection, DecimalUtils.DataType.WIND_DIR));
		changingHours.decrementAndGet();
	}

	public void cancel() {
		dispose();
	}

	public void save() {
		if (validatedInputs()) {
			retval = JFileChooser.APPROVE_OPTION;
			dispose();
		}
	}

	public void next() {
		setHour(hour + 1);
	}

	public void back() {
		setHour(hour - 1);
	}

	public boolean hasChanged() {
		return hasChanged;
	}

	public int getResult() {
		return retval;
	}

	private void initialize() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo_20.png")));
		setTitle(Main.resourceManager.getString("ui.dlg.title.edit.hourly"));
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 371, 282);
		else
			setBounds(0, 0, 381, 287);
		getContentPane().setLayout(null);

		JPanel panel1 = new JPanel();
		panel1.setBounds(0, 200, 371, 45);
		getContentPane().add(panel1);

		btnCancel = new RButton(Main.resourceManager.getString("ui.label.edit.cancel"));
		btnCancel.setBounds(50, 200, 121, 41);
		panel1.add(btnCancel, BorderLayout.NORTH);

		btnSave = new RButton(Main.resourceManager.getString("ui.label.edit.save"));
		btnSave.setBounds(180, 200, 121, 41);
		panel1.add(btnSave, BorderLayout.NORTH);

		RLabel lblTemperature = new RLabel(Main.resourceManager.getString("ui.label.weather.temp"));
		lblTemperature.setHorizontalAlignment(SwingConstants.TRAILING);
		lblTemperature.setBounds(30, 38, 131, 20);
		getContentPane().add(lblTemperature);

		RLabel lblRelativeHumidity = new RLabel(Main.resourceManager.getString("ui.label.weather.rh"));
		lblRelativeHumidity.setHorizontalAlignment(SwingConstants.TRAILING);
		lblRelativeHumidity.setBounds(30, 70, 131, 20);
		getContentPane().add(lblRelativeHumidity);

		RLabel lblPrecipitation = new RLabel(Main.resourceManager.getString("ui.label.weather.precip"));
		lblPrecipitation.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPrecipitation.setBounds(30, 100, 131, 20);
		getContentPane().add(lblPrecipitation);

		RLabel lblWindSpeed = new RLabel(Main.resourceManager.getString("ui.label.weather.ws"));
		lblWindSpeed.setHorizontalAlignment(SwingConstants.TRAILING);
		lblWindSpeed.setBounds(30, 128, 131, 20);
		getContentPane().add(lblWindSpeed);

		RLabel lblWindDirection = new RLabel(Main.resourceManager.getString("ui.label.weather.wd"));
		lblWindDirection.setHorizontalAlignment(SwingConstants.TRAILING);
		lblWindDirection.setBounds(30, 160, 131, 20);
		getContentPane().add(lblWindDirection);

		txtTemp = new RTextField();
		txtTemp.setBounds(170, 38, 113, 20);
		txtTemp.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.TEMPERATURE));
		getContentPane().add(txtTemp);

		txtRH = new RTextField();
		txtRH.setBounds(170, 70, 113, 20);
		txtRH.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.RH));
		getContentPane().add(txtRH);

		txtPrecip = new RTextField();
		txtPrecip.setBounds(170, 100, 113, 20);
		txtPrecip.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.PRECIP));
		getContentPane().add(txtPrecip);

		txtWS = new RTextField();
		txtWS.setBounds(170, 128, 113, 20);
		txtWS.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.WIND_SPEED));
		getContentPane().add(txtWS);

		txtWD = new RTextField();
		txtWD.setBounds(170, 160, 113, 20);
		txtWD.setFormat(DecimalUtils.getFormat(DecimalUtils.DataType.WIND_DIR));
		getContentPane().add(txtWD);
		
		RLabel lblTempUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblTempUnits = new RLabel(Main.resourceManager.getString("ui.label.units.celsius"));
		else
			lblTempUnits = new RLabel(Main.resourceManager.getString("ui.label.units.fahrenheit"));
		lblTempUnits.setBounds(290, 38, 20, 20);
		getContentPane().add(lblTempUnits);

		RLabel lblNewLabel_1 = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		lblNewLabel_1.setBounds(290, 70, 20, 21);
		getContentPane().add(lblNewLabel_1);

		RLabel lblPrecipUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblPrecipUnits = new RLabel(Main.resourceManager.getString("ui.label.units.mm"));
		else
			lblPrecipUnits = new RLabel(Main.resourceManager.getString("ui.label.units.in"));
		lblPrecipUnits.setBounds(290, 100, 31, 20);
		getContentPane().add(lblPrecipUnits);

		RLabel lblWSUnits;
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblWSUnits = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		lblWSUnits.setBounds(290, 128, 41, 21);
		getContentPane().add(lblWSUnits);

		RLabel lblNewLabel_4 = new RLabel(Main.resourceManager.getString("ui.label.units.degrees"));
		lblNewLabel_4.setBounds(290, 160, 31, 20);
		getContentPane().add(lblNewLabel_4);

		lblTextLabel = new RLabel("TextLabel");
		lblTextLabel.setBounds(0, 10, 351, 20);
		lblTextLabel.setForeground(new Color(168, 69, 69));
		lblTextLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblTextLabel);

		btnNext = new JButton("");
		btnNext.setBounds(317, 3, 30, 30);
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
	}
	
	private boolean validatedInputs() {
		boolean validated = true;
		double d = 0;
		
		d = ca.redapp.util.DoubleEx.valueOf(txtTemp.getText());
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
			
			lineEditHandleError(txtTemp, Main.resourceManager.getString("ui.label.range.temp", DecimalUtils.format(min, DataType.TEMPERATURE), DecimalUtils.format(max, DataType.TEMPERATURE), unit));
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
		
		d = ca.redapp.util.DoubleEx.valueOf(txtWS.getText());
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
			lineEditHandleError(txtWS, Main.resourceManager.getString("ui.label.range.ws", DecimalUtils.format(min, DataType.WIND_SPEED), DecimalUtils.format(max, DataType.WIND_SPEED), unit));
		}
		
		d = ca.redapp.util.DoubleEx.valueOf(txtWD.getText());
		if (d < 0.0 || d > 360.0) {
			validated = false;
			lineEditHandleError(txtWD, Main.resourceManager.getString("ui.label.range.wd"));
		}
		
		return validated;
	}
}
