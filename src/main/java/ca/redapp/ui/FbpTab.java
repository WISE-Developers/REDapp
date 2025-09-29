/***********************************************************************
 * REDapp - FbpTab.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.EventHandler;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import ca.wise.fbp.FBPCalculations;
import ca.cwfgm.mapunits.MetricPrefix;
import ca.cwfgm.mapunits.MetricUnits;
import ca.hss.general.DecimalUtils;
import ca.hss.general.DecimalUtils.DataType;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import ca.redapp.ui.StatsTab.StatsTabListener;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RGroupBox;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RMapValueTextField;
import ca.redapp.ui.component.RTextArea;
import ca.redapp.ui.component.RTextField;
import ca.redapp.ui.component.SpringUtilities;
import ca.redapp.util.*;

public class FbpTab extends REDappTab implements DocumentListener, DisplayableMapTab, StatsTabListener {
	private static final long serialVersionUID = 1L;
	private FBPCalculations fbpCalculations = new FBPCalculations();
	private boolean initialized = false;
	private Main app;
	private ArrayList<FBPTabListener> listeners = new ArrayList<FBPTabListener>();
	private boolean initTransfer = false;
	private PassedValues passedValues = new PassedValues();

	public FbpTab(Main app) {
		this.app = app;
		initialize();
		initTabOrder();
		fuelTypeChanged(0);
//		reset();

		txtFbpCrownBaseHeight.getDocument().addDocumentListener(this);
		txtFbpGrassCuring.getDocument().addDocumentListener(this);
		txtFbpGrassFuelLoad.getDocument().addDocumentListener(this);
		txtFbpPercentConifer.getDocument().addDocumentListener(this);
		txtFbpPercentDeadFir.getDocument().addDocumentListener(this);

		txtFbpFFMC.getDocument().addDocumentListener(this);
		txtFbpDMC.getDocument().addDocumentListener(this);
		txtFbpDC.getDocument().addDocumentListener(this);
		chckbxFbpUseBui.addActionListener((e) -> {
			clearOutputValuesOnForm();
			if (chckbxFbpUseBui.isSelected()) {
				txtFbpDMC.setEnabled(false);
				txtFbpDC.setEnabled(false);
				txtFbpBUI.setEnabled(true);
			}
			else {
				txtFbpDMC.setEnabled(true);
				txtFbpDC.setEnabled(true);
				txtFbpBUI.setEnabled(false);
			}
		});
		txtFbpBUI.getDocument().addDocumentListener(this);
		txtFbpWindSpeed.getDocument().addDocumentListener(this);
		txtFbpWindDirection.getDocument().addDocumentListener(this);

		txtFbpElevation.getDocument().addDocumentListener(this);
		chckbxFbpSlope.addActionListener((e) -> clearOutputValuesOnForm());
		txtFbpSlope.getDocument().addDocumentListener(this);
		txtFbpAspect.getDocument().addDocumentListener(this);

		txtFbpElapsedTime.getDocument().addDocumentListener(this);
		rdBtnFbpPoint.addActionListener((e) -> clearOutputValuesOnForm());
		rdBtnFbpLine.addActionListener((e) -> clearOutputValuesOnForm());

		btnFbpExport.addActionListener((e) -> export());

        /*
		if (!Main.useMap())
			btnFbpExportMap.addActionListener((e) -> {
				if (!Main.useMap())
					app.mapTab.export(app.getLatitude(), app.getLongitude(), getDH(), getDF(), getDB(), getRAZ());
			});


		btnFbpDisplayOnMap.addActionListener((e) -> app.mapTab.drawFBP(this));
*/
		txtFbpDB.setShowConverted(Main.prefs.getBoolean(
				"fbp_db_showConverted", false));
		txtFbpDH.setShowConverted(Main.prefs.getBoolean(
				"fbp_dh_showConverted", false));
		txtFbpDF.setShowConverted(Main.prefs.getBoolean(
				"fbp_df_showConverted", false));

		txtFbpDH.addRMapValueTextFieldListener((f, s) -> {
			Main.prefs.putBoolean("fbp_dh_showConverted",
					txtFbpDH.isShowConverted());
		});
		txtFbpDF.addRMapValueTextFieldListener((f, s) -> {
			Main.prefs.putBoolean("fbp_df_showConverted",
					txtFbpDF.isShowConverted());
		});
		txtFbpDB.addRMapValueTextFieldListener((f, s) -> {
			Main.prefs.putBoolean("fbp_db_showConverted",
					txtFbpDB.isShowConverted());
		});

		comboFbpFuelType.addActionListener((e) -> {
			fuelTypeChanged(comboFbpFuelType.getSelectedIndex());
		});

		btnFbpTransferToStats.addActionListener((e) -> transferToStatsTab());
	}

	public void calculate() {
		if (getValuesFromForm()) {
			clearOutputValuesOnForm();
			JOptionPane.showMessageDialog(null,
					Main.resourceManager.getString("ui.label.range.invalid"), "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			fbpCalculations.FBPCalculateStatisticsCOM();
		} catch (CloneNotSupportedException e) {
			if (GLOBAL.DEBUG)
				e.printStackTrace();
			return;
		} catch (Exception e) {
			if (GLOBAL.DEBUG)
				e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					Main.resourceManager.getString("ui.label.fbp.error"),
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		setOutputValuesOnForm();
	}

	public void setOutputValuesOnForm() {
		txtFbpROSt.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.ros_t, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC))));
		txtFbpROSeq.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.ros_eq, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC))));
		txtFbpFROS.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.fros, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC))));
		txtFbpLB.setText(DecimalUtils.format(fbpCalculations.lb));
		txtFbpBROS.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.bros, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC))));
		txtFbpRSO.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.rso, UnitSystem.spreadRate(Main.unitSystem()), UnitSystem.spreadRate(UnitSystem.METRIC))));
		txtFbpArea.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.area, UnitSystem.area(Main.unitSystem()), UnitSystem.area(UnitSystem.METRIC))));
		txtFbpPerimeter.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.perimeter, UnitSystem.distanceMedium2(Main.unitSystem()), UnitSystem.distanceMedium2(UnitSystem.METRIC))));

		DecimalFormat noDecimals = new DecimalFormat("#");
		Double check = fbpCalculations.hfi;
		if (check > 10)
			txtFbpHFI.setText(noDecimals.format(Convert.convertUnit(check, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC))));
		else
			txtFbpHFI.setText(DecimalUtils.format(Convert.convertUnit(check, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC))));
		
		check = fbpCalculations.ffi;
		if (check > 10)
			txtFbpFFI.setText(noDecimals.format(Convert.convertUnit(check, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC))));
		else
			txtFbpFFI.setText(DecimalUtils.format(Convert.convertUnit(check, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC))));
		
		check = fbpCalculations.bfi;
		if (check > 10)
			txtFbpBFI.setText(noDecimals.format(Convert.convertUnit(check, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC))));
		else
			txtFbpBFI.setText(DecimalUtils.format(Convert.convertUnit(check, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC))));
		
		check = fbpCalculations.csi;
		if (check > 10)
			txtFbpCSI.setText(noDecimals.format(Convert.convertUnit(check, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC))));
		else
			txtFbpCSI.setText(DecimalUtils.format(Convert.convertUnit(check, UnitSystem.intensity(Main.unitSystem()), UnitSystem.intensity(UnitSystem.METRIC))));
		
		check = fbpCalculations.distanceHead;
		if (check > 10)
			txtFbpDH.setActualValue((double)Math.round(check), MetricPrefix.NONE, MetricUnits.length);
		else
			txtFbpDH.setActualValue(check, MetricPrefix.NONE, MetricUnits.length);
		
		check = fbpCalculations.distanceBack;
		if (check > 10)
			txtFbpDB.setActualValue((double)Math.round(check), MetricPrefix.NONE, MetricUnits.length);
		else
			txtFbpDB.setActualValue(check, MetricPrefix.NONE, MetricUnits.length);
		
		check = fbpCalculations.distanceFlank;
		if (check > 10)
			txtFbpDF.setActualValue((double)Math.round(check), MetricPrefix.NONE, MetricUnits.length);
		else
			txtFbpDF.setActualValue(check, MetricPrefix.NONE, MetricUnits.length);

		txtFbpCFB.setText(DecimalUtils.format(fbpCalculations.cfb));
		txtFbpCFB.setEnabled(fbpCalculations.cfbPossible);
		lblFbpCFB.setForeground(fbpCalculations.cfbPossible ? Color.BLACK : Color.GRAY);
		lblFbpCfbUnit.setForeground(fbpCalculations.cfbPossible ? Color.BLACK : Color.GRAY);
		
		txtFbpSFC.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.sfc, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC))));
		txtFbpTFC.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.tfc, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC))));
		txtFbpCFC.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.cfc, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC))));
		txtFbpISI.setText(DecimalUtils.format(fbpCalculations.isi, DecimalUtils.DataType.ISI));
		txtFbpFMC.setText(DecimalUtils.format(fbpCalculations.fmc, DecimalUtils.DataType.FFMC));
		txtFbpWSV.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.wsv, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC))));
		txtFbpRAZ.setText(DecimalUtils.format(fbpCalculations.raz));
		txtFbpFireDescription.setText(fbpCalculations.fireDescription);
		
		if (!fbpCalculations.useBui) {
			txtFbpBUI.getDocument().removeDocumentListener(this);
			txtFbpBUI.setText(DecimalUtils.format(fbpCalculations.bui, DecimalUtils.DataType.FORCE_1));
			txtFbpBUI.getDocument().addDocumentListener(this);
		}
		
		notifyCalculated(true);
	}

	private void clearOutputValuesOnForm() {
		txtFbpROSt.setText("");
		txtFbpROSeq.setText("");
		txtFbpFROS.setText("");
		txtFbpLB.setText("");
		txtFbpBROS.setText("");
		txtFbpRSO.setText("");
		txtFbpHFI.setText("");
		txtFbpFFI.setText("");
		txtFbpBFI.setText("");
		txtFbpArea.setText("");
		txtFbpPerimeter.setText("");
		txtFbpDH.clear();
		txtFbpDB.clear();
		txtFbpDF.clear();
		txtFbpCSI.setText("");
		txtFbpCFB.setText("");
		txtFbpSFC.setText("");
		txtFbpTFC.setText("");
		txtFbpCFC.setText("");
		txtFbpISI.setText("");
		txtFbpFMC.setText("");
		txtFbpWSV.setText("");
		txtFbpRAZ.setText("");
		txtFbpFireDescription.setText("");
		notifyCalculated(false);
	}

	@Override
	public void reset() {
		fbpCalculations = new FBPCalculations();

		comboFbpFuelType
				.setSelectedIndex(adjustIndexFuelTypeToComboBox(fbpCalculations.fuelType));
		txtFbpPercentConifer.setText(DecimalUtils.format(fbpCalculations.conifMixedWood));
		txtFbpPercentDeadFir.setText(DecimalUtils.format(fbpCalculations.deadBalsam));
		txtFbpGrassCuring.setText(DecimalUtils.format(fbpCalculations.grassCuring));
		txtFbpGrassFuelLoad.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.grassFuelLoad, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC))));
		txtFbpCrownBaseHeight.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.crownBase, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC))));
		rdBtnFbpPoint.setSelected(true);
		Calendar cal = Calendar.getInstance();
		spinnerFbpStartTime.setValue(cal.getTime());
		txtFbpElapsedTime.setText(DecimalUtils.format(fbpCalculations.elapsedTime, DecimalUtils.DataType.FORCE_0));
		chckbxFbpSlope.setSelected(fbpCalculations.useSlope);
		
		txtFbpFFMC.setText(DecimalUtils.format(fbpCalculations.ffmc, DecimalUtils.DataType.FFMC));
		
		txtFbpDMC.setText(DecimalUtils.format(fbpCalculations.dmc, DecimalUtils.DataType.DMC));

		txtFbpDC.setText(DecimalUtils.format(fbpCalculations.dc, DecimalUtils.DataType.DC));

		txtFbpBUI.setText(DecimalUtils.format(fbpCalculations.bui, DecimalUtils.DataType.FORCE_1));
	
		txtFbpWindSpeed.setText(DecimalUtils.format(Convert.convertUnit(fbpCalculations.windSpeed, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC)), DecimalUtils.DataType.WIND_SPEED));
		txtFbpWindDirection.setText(DecimalUtils.format(fbpCalculations.windDirection, DecimalUtils.DataType.WIND_DIR));

		chckbxFbpUseBui.setSelected(fbpCalculations.useBui);
		if (fbpCalculations.useBui) {
			txtFbpDMC.setEnabled(false);
			txtFbpDC.setEnabled(false);
			txtFbpBUI.setEnabled(true);
		}
		else {
			txtFbpDMC.setEnabled(true);
			txtFbpDC.setEnabled(true);
			txtFbpBUI.setEnabled(false);
		}
		
		txtFbpElevation.setText(DecimalUtils.format(Convert.convertUnit(Main.prefs.getDouble("fbp_elevation", fbpCalculations.slopeValue),
				UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)), DecimalUtils.DataType.FORCE_1));

		txtFbpSlope.setText(DecimalUtils.format(Main.prefs.getDouble("fbp_slope", 0.0), DecimalUtils.DataType.FORCE_1));
		
		txtFbpAspect.setText(DecimalUtils.format(Main.prefs.getDouble("fbp_aspect", 0.0), DecimalUtils.DataType.FORCE_1));

		chckbxFbpSlope.setSelected(Boolean.parseBoolean(Main.prefs.getString("fbp_useSlope", String.valueOf(fbpCalculations.useSlope))));
		chckbxFbpSlope.addActionListener((e) -> chkbxUseSlopeChanged());
		
		clearOutputValuesOnForm();
	}

	@Override
	public boolean supportsReset() {
		return true;
	}

	//PLACEHOLDER
	private boolean getValuesFromForm() {
		Double d;
		boolean error = false;
		d = app.getLatitude();
		if (d == null)
			error = true;
		else {
			fbpCalculations.latitude = d;
			if (d < -90.0 || d > 90.0) {
				error = true;
			}
		}
		d = app.getLongitude();
		if (d == null)
			error = true;
		else {
			fbpCalculations.longitude = d;
			if (d < -180.0 || d > 180.0) {
				error = true;
			}
		}
		d = getDoubleFromLineEdit(txtFbpElevation);
		if (d == null)
			error = true;
		else {
			fbpCalculations.elevation = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
			if (d < -5.0 || d > 7000.0) {
				error = true;
				Double min = Convert.convertUnit(-5.0, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
				Double max = Convert.convertUnit(7000.0, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
				String unit;
				if (Main.unitSystem() == UnitSystem.METRIC)
					unit = Main.resourceManager.getString("ui.label.units.m");
				else
					unit = Main.resourceManager.getString("ui.label.units.ft");
				lineEditHandleError(txtFbpElevation,
						Main.resourceManager.getString("ui.label.range.elev", DecimalUtils.format(min, DataType.FORCE_1), DecimalUtils.format(max, DataType.FORCE_1), unit));
			}
		}
		if (chckbxFbpSlope.isSelected()) {
			fbpCalculations.useSlope = true;
			d = getDoubleFromLineEdit(txtFbpSlope);
			if (d == null)
				error = true;
			else {
				fbpCalculations.slopeValue = d;
				if (d < 0.0 || d > 100.0) {
					error = true;
					lineEditHandleError(txtFbpSlope,
							Main.resourceManager.getString("ui.label.range.slope"));
				}
			}
			d = getDoubleFromLineEdit(txtFbpAspect);
			if (d == null)
				error = true;
			else {
				fbpCalculations.aspect = d;
				if (d < 0.0 || d > 360.0) {
					error = true;
					lineEditHandleError(txtFbpAspect,
							Main.resourceManager.getString("ui.label.range.aspect"));
				}
			}
		} else
			fbpCalculations.useSlope = false;
		d = getDoubleFromLineEdit(txtFbpFFMC);
		if (d == null)
			error = true;
		else {
			if(passedValues.ffmc != null) {
				fbpCalculations.ffmc = Double.parseDouble(passedValues.ffmc);
				if (d < 1.0 || d > 101.0) {
					error = true;
					lineEditHandleError(txtFbpFFMC,
							Main.resourceManager.getString("ui.label.range.ffmc"));
				}
			}
		}
		fbpCalculations.useBui = chckbxFbpUseBui.isSelected();
		if (fbpCalculations.useBui) {
			d = getDoubleFromLineEdit(txtFbpBUI);
			if (d == null)
				error = true;
			else {
				if(passedValues.bui != null) {
					fbpCalculations.bui = Double.parseDouble(passedValues.bui);
					if (d < 0.0 || d > 300.0) {
						error = true;
						lineEditHandleError(txtFbpBUI,
								Main.resourceManager.getString("ui.label.range.bui"));
					}
				}
			}
		} else {
			d = getDoubleFromLineEdit(txtFbpDMC);
			if (d == null)
				error = true;
			else {
				if(passedValues.dmc != null) {
					fbpCalculations.dmc = Double.parseDouble(passedValues.dmc);
					if (d < 0.0 || d > 500.0) {
						error = true;
						lineEditHandleError(txtFbpDMC,
								Main.resourceManager.getString("ui.label.range.dmc"));
					}
				}
			}
			d = getDoubleFromLineEdit(txtFbpDC);
			if (d == null)
				error = true;
			else {
				if(passedValues.dc != null) {
					fbpCalculations.dc = Double.parseDouble(passedValues.dc);
					if (d < 0.0 || d > 1500.0) {
						error = true;
						lineEditHandleError(txtFbpDC,
								Main.resourceManager.getString("ui.label.range.dc"));
					}
				}
			}
		}
		d = getDoubleFromLineEdit(txtFbpWindSpeed);
		if (d == null)
			error = true;
		else {
			fbpCalculations.windSpeed = Convert.convertUnit(d, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
			if (d < 0.0 || d > 100.0) {
				error = true;
				Double min = Convert.convertUnit(0.0, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
				Double max = Convert.convertUnit(100.0, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
				String unit;
				if (Main.unitSystem() == UnitSystem.METRIC)
					unit = Main.resourceManager.getString("ui.label.units.kph");
				else
					unit = Main.resourceManager.getString("ui.label.units.mph");
				lineEditHandleError(txtFbpWindSpeed,
						Main.resourceManager.getString("ui.label.range.ws", DecimalUtils.format(min, DataType.WIND_SPEED), DecimalUtils.format(max, DataType.WIND_SPEED), unit));
			}
		}
		d = getDoubleFromLineEdit(txtFbpWindDirection);
		if (d == null)
			error = true;
		else {
			fbpCalculations.windDirection = d;
			if (d < 0.0 || d > 360.0) {
				error = true;
				lineEditHandleError(txtFbpWindDirection,
						Main.resourceManager.getString("ui.label.range.wd"));
			}
		}
		if (txtFbpPercentConifer.isEnabled()) {
			d = getDoubleFromLineEdit(txtFbpPercentConifer);
			if (d == null)
				error = true;
			else {
				fbpCalculations.conifMixedWood = d;
				if (d < 0.0 || d > 100.0) {
					error = true;
					lineEditHandleError(txtFbpPercentConifer,
							Main.resourceManager.getString("ui.label.range.conifer"));
				}
			}
		}
		if (txtFbpPercentDeadFir.isEnabled()) {
			d = getDoubleFromLineEdit(txtFbpPercentDeadFir);
			if (d == null)
				error = true;
			else {
				fbpCalculations.deadBalsam = d;
				if (d < 0.0 || d > 100.0) {
					error = true;
					lineEditHandleError(txtFbpPercentDeadFir,
							Main.resourceManager.getString("ui.label.range.deadbalsam"));
				}
			}
		}
		if (txtFbpGrassCuring.isEnabled()) {
			d = getDoubleFromLineEdit(txtFbpGrassCuring);
			if (d == null)
				error = true;
			else {
				fbpCalculations.grassCuring = d;
				if (d < 0.0 || d > 100.0) {
					error = true;
					lineEditHandleError(txtFbpGrassCuring,
							Main.resourceManager.getString("ui.label.range.gcuring"));
				}
			}
		}
		if (txtFbpGrassFuelLoad.isEnabled()) {
			d = getDoubleFromLineEdit(txtFbpGrassFuelLoad);
			if (d == null)
				error = true;
			else {
				fbpCalculations.grassFuelLoad = Convert.convertUnit(d, UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(Main.unitSystem()));
				if (d < 0.0 || d > 5.0) {
					error = true;
					Double min = Convert.convertUnit(0.0, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC));
					Double max = Convert.convertUnit(5.0, UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC));
					String unit;
					if (Main.unitSystem() == UnitSystem.METRIC)
						unit = Main.resourceManager.getString("ui.label.units.kgm2");
					else
						unit = Main.resourceManager.getString("ui.label.units.tonsperacre");
					lineEditHandleError(txtFbpGrassFuelLoad,
							Main.resourceManager.getString("ui.label.range.gload", DecimalUtils.format(min, DataType.FORCE_1), DecimalUtils.format(max, DataType.FORCE_1), unit));
				}
			}
		}
		if (txtFbpCrownBaseHeight.isEnabled()) {
			d = getDoubleFromLineEdit(txtFbpCrownBaseHeight);
			if (d == null)
				error = true;
			else {
				fbpCalculations.crownBase = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
				if (d < 0.0 || d > 25.0) {
					error = true;
					Double min = Convert.convertUnit(0.0, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
					Double max = Convert.convertUnit(25.0, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
					String unit;
					if (Main.unitSystem() == UnitSystem.METRIC)
						unit = Main.resourceManager.getString("ui.label.units.m");
					else
						unit = Main.resourceManager.getString("ui.label.units.ft");
					lineEditHandleError(txtFbpCrownBaseHeight,
							Main.resourceManager.getString("ui.label.range.cbh", DecimalUtils.format(min, DataType.FORCE_1), DecimalUtils.format(max, DataType.FORCE_1), unit));
				}
			}
		}
		d = getDoubleFromLineEdit(txtFbpElapsedTime);
		if (d == null)
			error = true;
		else {
			fbpCalculations.elapsedTime = d;
			if (d < 0.1 || d > 1440.0) {
				error = true;
				lineEditHandleError(txtFbpElapsedTime,
						Main.resourceManager.getString("ui.label.range.duration"));
			}
		}
		fbpCalculations.acceleration = rdBtnFbpPoint.isSelected();
		fbpCalculations.useBuildup = true;

		Date dt = app.getDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		fbpCalculations.m_date.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		fbpCalculations.m_date.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		fbpCalculations.m_date.set(Calendar.DAY_OF_MONTH,
				cal.get(Calendar.DAY_OF_MONTH));

		int i = app.getSelectedTimeZone().toString().indexOf(":");
		fbpCalculations.m_date.setTimeZone(TimeZone
				.getTimeZone(app.getSelectedTimeZone().toString()
						.substring(0, i - 1)));

		Date tm = ((SpinnerDateModel) spinnerFbpStartTime.getModel())
				.getDate();
		cal.setTime(tm);
		fbpCalculations.m_date.set(Calendar.HOUR_OF_DAY,
				cal.get(Calendar.HOUR_OF_DAY));
		fbpCalculations.m_date.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
		fbpCalculations.m_date.set(Calendar.SECOND, cal.get(Calendar.SECOND));
		return error;
	}

	public void inputChanged() {
		txtFbpWSV.setText("");
		txtFbpRAZ.setText("");
		txtFbpISI.setText("");
		txtFbpFMC.setText("");
		txtFbpROSt.setText("");
		txtFbpROSeq.setText("");
		txtFbpHFI.setText("");
		txtFbpCFB.setText("");
		txtFbpSFC.setText("");
		txtFbpCFC.setText("");
		txtFbpTFC.setText("");
		txtFbpFireDescription.setText("");
		txtFbpRSO.setText("");
		txtFbpFROS.setText("");
		txtFbpBROS.setText("");
		txtFbpCSI.setText("");
		txtFbpFFI.setText("");
		txtFbpBFI.setText("");
		txtFbpDH.clear();
		txtFbpDF.clear();
		txtFbpDB.clear();
		txtFbpLB.setText("");
		txtFbpArea.setText("");
		txtFbpPerimeter.setText("");
	}

	void fuelTypeInfo() {
		int fuelType = adjustIndexComboBoxToFuelType(comboFbpFuelType
				.getSelectedIndex());
		FuelTypeInfo info = new FuelTypeInfo(app, fuelType);
		info.setVisible(true);
	}

	void fuelTypeChanged(int index) {
		// adjust combobox index to match FBP's fuelType index (as combobox has
		// some options omitted)
		// index = this.adjustIndexComboBoxToFuelType(index);
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
			txtFbpPercentConifer.setVisible(false);
			txtFbpPercentConifer.setEnabled(false);
			lblFbpPercentConifer.setVisible(false);
			lblFbpPercentConiferUnit.setVisible(false);
			txtFbpPercentDeadFir.setEnabled(false);
			txtFbpPercentDeadFir.setVisible(false);
			lblFbpPercentDeadFir.setVisible(false);
			lblFbpPercentDeadFirUnit.setVisible(false);
			txtFbpGrassCuring.setEnabled(false);
			txtFbpGrassCuring.setVisible(false);
			lblFbpGrassCuring.setVisible(false);
			lblFbpGrassCuringUnit.setVisible(false);
			txtFbpGrassFuelLoad.setEnabled(false);
			txtFbpGrassFuelLoad.setVisible(false);
			lblFbpGrassFuelLoad.setVisible(false);
			lblFbpGrassFuelLoadUnit.setVisible(false);
			txtFbpCrownBaseHeight.setEnabled(false);
			txtFbpCrownBaseHeight.setVisible(false);
			lblFbpCrownBaseHeight.setVisible(false);
			lblFbpCrownBaseHeightUnit.setVisible(false);
			btnFbpInformation.setEnabled(true);
			btnFbpInformation.setVisible(true);
			break;
		case 5:
			txtFbpPercentConifer.setEnabled(false);
			txtFbpPercentConifer.setVisible(false);
			lblFbpPercentConifer.setVisible(false);
			lblFbpPercentConiferUnit.setVisible(false);
			txtFbpPercentDeadFir.setEnabled(false);
			txtFbpPercentDeadFir.setVisible(false);
			lblFbpPercentDeadFir.setVisible(false);
			lblFbpPercentDeadFirUnit.setVisible(false);
			txtFbpGrassCuring.setEnabled(false);
			txtFbpGrassCuring.setVisible(false);
			lblFbpGrassCuring.setVisible(false);
			lblFbpGrassCuringUnit.setVisible(false);
			txtFbpGrassFuelLoad.setEnabled(false);
			txtFbpGrassFuelLoad.setVisible(false);
			lblFbpGrassFuelLoad.setVisible(false);
			lblFbpGrassFuelLoadUnit.setVisible(false);
			txtFbpCrownBaseHeight.setEnabled(true);
			txtFbpCrownBaseHeight.setVisible(true);
			lblFbpCrownBaseHeight.setVisible(true);
			lblFbpCrownBaseHeightUnit.setVisible(true);
			btnFbpInformation.setEnabled(true);
			btnFbpInformation.setVisible(true);
			break;
		case 9:
		case 10:
			txtFbpPercentConifer.setEnabled(true);
			txtFbpPercentConifer.setVisible(true);
			lblFbpPercentConifer.setVisible(true);
			lblFbpPercentConiferUnit.setVisible(true);
			txtFbpPercentDeadFir.setEnabled(false);
			txtFbpPercentDeadFir.setVisible(false);
			lblFbpPercentDeadFir.setVisible(false);
			lblFbpPercentDeadFirUnit.setVisible(false);
			txtFbpGrassCuring.setEnabled(false);
			txtFbpGrassCuring.setVisible(false);
			lblFbpGrassCuring.setVisible(false);
			lblFbpGrassCuringUnit.setVisible(false);
			txtFbpGrassFuelLoad.setEnabled(false);
			txtFbpGrassFuelLoad.setVisible(false);
			lblFbpGrassFuelLoad.setVisible(false);
			lblFbpGrassFuelLoadUnit.setVisible(false);
			txtFbpCrownBaseHeight.setEnabled(false);
			txtFbpCrownBaseHeight.setVisible(false);
			lblFbpCrownBaseHeight.setVisible(false);
			lblFbpCrownBaseHeightUnit.setVisible(false);
			btnFbpInformation.setEnabled(true);
			btnFbpInformation.setVisible(true);
			break;
		case 11:
		case 12:
			txtFbpPercentConifer.setEnabled(false);
			txtFbpPercentConifer.setVisible(false);
			lblFbpPercentConifer.setVisible(false);
			lblFbpPercentConiferUnit.setVisible(false);
			txtFbpPercentDeadFir.setEnabled(true);
			txtFbpPercentDeadFir.setVisible(true);
			lblFbpPercentDeadFir.setVisible(true);
			lblFbpPercentDeadFirUnit.setVisible(true);
			txtFbpGrassCuring.setEnabled(false);
			txtFbpGrassCuring.setVisible(false);
			lblFbpGrassCuring.setVisible(false);
			lblFbpGrassCuringUnit.setVisible(false);
			txtFbpGrassFuelLoad.setEnabled(false);
			txtFbpGrassFuelLoad.setVisible(false);
			lblFbpGrassFuelLoad.setVisible(false);
			lblFbpGrassFuelLoadUnit.setVisible(false);
			txtFbpCrownBaseHeight.setEnabled(false);
			txtFbpCrownBaseHeight.setVisible(false);
			lblFbpCrownBaseHeight.setVisible(false);
			lblFbpCrownBaseHeightUnit.setVisible(false);
			btnFbpInformation.setEnabled(true);
			btnFbpInformation.setVisible(true);
			break;
		case 13:
		case 14:
		//case 15: O1ab
			txtFbpPercentConifer.setEnabled(false);
			txtFbpPercentConifer.setVisible(false);
			lblFbpPercentConifer.setVisible(false);
			lblFbpPercentConiferUnit.setVisible(false);
			txtFbpPercentDeadFir.setEnabled(false);
			txtFbpPercentDeadFir.setVisible(false);
			lblFbpPercentDeadFir.setVisible(false);
			lblFbpPercentDeadFirUnit.setVisible(false);
			txtFbpGrassCuring.setEnabled(true);
			txtFbpGrassCuring.setVisible(true);
			lblFbpGrassCuring.setVisible(true);
			lblFbpGrassCuringUnit.setVisible(true);
			txtFbpGrassFuelLoad.setEnabled(true);
			txtFbpGrassFuelLoad.setVisible(true);
			lblFbpGrassFuelLoad.setVisible(true);
			lblFbpGrassFuelLoadUnit.setVisible(true);
			txtFbpCrownBaseHeight.setEnabled(false);
			txtFbpCrownBaseHeight.setVisible(false);
			lblFbpCrownBaseHeight.setVisible(false);
			lblFbpCrownBaseHeightUnit.setVisible(false);
			btnFbpInformation.setEnabled(true);
			btnFbpInformation.setVisible(true);
			break;
		}
		index = adjustIndexComboBoxToFuelType(index);
		fbpCalculations.fuelType = index;

		if (fbpCalculations.fuelType >= 16 && fbpCalculations.fuelType <= 21)
			fbpCalculations.cfbPossible = false;
		else
			fbpCalculations.cfbPossible = true;
		
		inputChanged();
	}

	private static BiMap<Integer, Integer> comboIndexToFuel = null;
	private static Map<Integer, Integer> fuelToComboIndex = null;
	private static synchronized void initializeFuelMaps() {
		if (comboIndexToFuel == null) {
			comboIndexToFuel = HashBiMap.create();
			comboIndexToFuel.put(0,0);
			comboIndexToFuel.put(1,1);
			comboIndexToFuel.put(2,2);
			comboIndexToFuel.put(3,3);
			comboIndexToFuel.put(4,4);
			comboIndexToFuel.put(5,5);
			comboIndexToFuel.put(6,6);
			comboIndexToFuel.put(7,7);
			comboIndexToFuel.put(8,8);
			comboIndexToFuel.put(9,10);
			comboIndexToFuel.put(10,11);
			comboIndexToFuel.put(11,13);
			comboIndexToFuel.put(12,14);
			comboIndexToFuel.put(13,16);
			comboIndexToFuel.put(14,17);
			//comboIndexToFuel.put(15,18); O1ab
			comboIndexToFuel.put(15,19);
			comboIndexToFuel.put(16,20);
			comboIndexToFuel.put(17,21);
			fuelToComboIndex = comboIndexToFuel.inverse();
		}
	}
	
	public static int adjustIndexFuelTypeToComboBox(int i) {
		initializeFuelMaps();
		return fuelToComboIndex.get(i);
	}

	public static int adjustIndexComboBoxToFuelType(int i) {
		initializeFuelMaps();
		return comboIndexToFuel.get(i);
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

	private void export() {
		OutputStream os = null;
		try {
			String file = null;
			Preferences exporter = null;
			String dir = Main.prefs.getString("FBP_START_DIR", System.getProperty("user.home"));
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
				Main.prefs.putString("FBP_START_DIR", fc.getParentDirectory());
				if (file.endsWith(".xml"))
					extension = "xml";
				else if (file.endsWith(".csv"))
					extension = "csv";
				else if (file.endsWith(".xls"))
					extension = "xls";
				else if (file.endsWith(".xlsx"))
					extension = "xlsx";
				else
					file = file + "." + extension;
				
				if (extension.equals("xml")) {
					exporter = new XMLExporter("fwi");
				} else if (extension.equals("csv")) {
					exporter = new CSVExporter();
				} else if (extension.equals("xlsx")) {
					exporter = new XLSExporter("FBP", true);
				} else if (extension.equals("xls")) {
					exporter = new XLSExporter("FBP", false);
				}
				
				if (exporter != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					exporter.put("Date", sdf.format(app.getDate()));
					exporter.put("TimeZone", app.getSelectedTimeZone().toString());
					exporter.put("Latitude", app.getLatitudeString());
					exporter.put("Longitude", app.getLongitudeString());
	
					exporter.put("FuelType", comboFbpFuelType.getSelectedItem()
							.toString());
					if (txtFbpPercentConifer.isVisible())
						exporter.put("ConiferMixedwood",
								txtFbpPercentConifer.getText());
					if (txtFbpCrownBaseHeight.isVisible()) {
						Double val = DecimalUtils.valueOf(txtFbpCrownBaseHeight.getText());
						if (val == null)
							val = 0.0;
						val = Convert.convertUnit(val, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
						exporter.put("CrownBase", DecimalUtils.format(val));
					}
					if (txtFbpPercentDeadFir.isVisible())
						exporter.put("DeadBalsam",
								txtFbpPercentDeadFir.getText());
					if (txtFbpGrassCuring.isVisible())
						exporter.put("GrassCuring", txtFbpGrassCuring.getText());
					if (txtFbpGrassFuelLoad.isVisible()) {
						Double val = DecimalUtils.valueOf(txtFbpGrassFuelLoad.getText());
						if (val == null)
							val = 0.0;
						val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(Main.unitSystem()));
						exporter.put("GrassFuelLoad", DecimalUtils.format(val));
					}
	
					exporter.put("FFMC", txtFbpFFMC.getText());
					boolean useBui = chckbxFbpUseBui.isSelected();
					if (!useBui) {
						exporter.put("DMC", txtFbpDMC.getText());
						exporter.put("DC", txtFbpDC.getText());
					}
					exporter.put("BUI", txtFbpBUI.getText());
					Double val = DecimalUtils.valueOf(txtFbpWindSpeed.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
					exporter.put("WindSpeed", DecimalUtils.format(val, DataType.WIND_SPEED));
					exporter.put("WindDirection", txtFbpWindDirection.getText());
	
					val = DecimalUtils.valueOf(txtFbpElevation.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
					exporter.put("Elevation", DecimalUtils.format(val));
					boolean useSlope = chckbxFbpSlope.isSelected();
					exporter.put("UseSlope", Boolean.toString(useSlope));
					if (useSlope)
						exporter.put("Slope", txtFbpSlope.getText());
					exporter.put("Aspect", txtFbpAspect.getText());
	
					if (rdBtnFbpPoint.isSelected())
						exporter.put("IgnitionType", "Point");
					else
						exporter.put("IgnitionType", "Line");
					SimpleDateFormat format = new SimpleDateFormat("hh:mm");
					exporter.put("StartTime", format.format((Date)spinnerFbpStartTime.getValue()));
					exporter.put("ElapsedTime", txtFbpElapsedTime.getText());
	
					val = DecimalUtils.valueOf(txtFbpWSV.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
					exporter.put("WSV", txtFbpWSV.getText());
					exporter.put("RAZ", txtFbpRAZ.getText());
					exporter.put("ISI", txtFbpISI.getText());
					exporter.put("FMC", txtFbpFMC.getText());
	
					val = DecimalUtils.valueOf(txtFbpROSt.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.spreadRate(UnitSystem.METRIC), UnitSystem.spreadRate(Main.unitSystem()));
					exporter.put("ROSt", DecimalUtils.format(val));
					val = DecimalUtils.valueOf(txtFbpROSeq.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.spreadRate(UnitSystem.METRIC), UnitSystem.spreadRate(Main.unitSystem()));
					exporter.put("ROSeq", DecimalUtils.format(val));
					val = DecimalUtils.valueOf(txtFbpHFI.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.intensity(UnitSystem.METRIC), UnitSystem.intensity(Main.unitSystem()));
					exporter.put("HFI", DecimalUtils.format(val));
					exporter.put("CFB", txtFbpCFB.getText());
					val = DecimalUtils.valueOf(txtFbpSFC.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(Main.unitSystem()));
					exporter.put("SFC", DecimalUtils.format(val));
					val = DecimalUtils.valueOf(txtFbpCFC.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(Main.unitSystem()));
					exporter.put("CFC", DecimalUtils.format(val));
					val = DecimalUtils.valueOf(txtFbpTFC.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(Main.unitSystem()));
					exporter.put("TFC", DecimalUtils.format(val));
					exporter.put("HeadFireDescription", fbpCalculations.headFireDescription);
					exporter.put("FlankFireDescription", fbpCalculations.flankFireDescription);
					exporter.put("BackFireDescription", fbpCalculations.backFireDescription);
	
					val = DecimalUtils.valueOf(txtFbpRSO.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(Main.unitSystem()));
					exporter.put("RSO", DecimalUtils.format(val));
					val = DecimalUtils.valueOf(txtFbpFROS.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(Main.unitSystem()));
					exporter.put("FROS", DecimalUtils.format(val));
					val = DecimalUtils.valueOf(txtFbpBROS.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(Main.unitSystem()));
					exporter.put("BROS", DecimalUtils.format(val));
					val = DecimalUtils.valueOf(txtFbpCSI.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.intensity(UnitSystem.METRIC), UnitSystem.intensity(Main.unitSystem()));
					exporter.put("CSI", DecimalUtils.format(val));
					val = DecimalUtils.valueOf(txtFbpFFI.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.intensity(UnitSystem.METRIC), UnitSystem.intensity(Main.unitSystem()));
					exporter.put("FFI", DecimalUtils.format(val));
					val = DecimalUtils.valueOf(txtFbpBFI.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.intensity(UnitSystem.METRIC), UnitSystem.intensity(Main.unitSystem()));
					exporter.put("BFI", DecimalUtils.format(val));
	
					exporter.put("DH",
							DecimalUtils.formatLocaleless(txtFbpDH.getActualValue(), DecimalUtils.DataType.FORCE_2));
					exporter.put("DF",
							DecimalUtils.formatLocaleless(txtFbpDF.getActualValue(), DecimalUtils.DataType.FORCE_2));
					exporter.put("DB",
							DecimalUtils.formatLocaleless(txtFbpDB.getActualValue(), DecimalUtils.DataType.FORCE_2));
	
					exporter.put("LB", txtFbpLB.getText());
					val = DecimalUtils.valueOf(txtFbpArea.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.area(UnitSystem.METRIC), UnitSystem.area(Main.unitSystem()));
					exporter.put("Area", DecimalUtils.format(val));
					val = DecimalUtils.valueOf(txtFbpPerimeter.getText());
					if (val == null)
						val = 0.0;
					val = Convert.convertUnit(val, UnitSystem.distanceMedium2(UnitSystem.METRIC), UnitSystem.distanceMedium2(Main.unitSystem()));
					exporter.put("Perimeter", DecimalUtils.format(val));
	
					os = new FileOutputStream(file);
					exporter.exportSubtree(os);
					os.flush();
					os.close();
				}
			}
		} catch (Exception e) {
			if (os != null) {
				try {
					os.close();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.label.fbp.export.error"),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@SuppressWarnings("deprecation")
	public void saveAllValues() {
		RPreferences prefs = Main.prefs;
		prefs.putString("fbp_fuelList", String
				.valueOf(adjustIndexComboBoxToFuelType(comboFbpFuelType
						.getSelectedIndex())));
		Double d;
		
		d = getDoubleFromLineEdit(txtFbpFFMC);
		if (d != null) {
			prefs.putString("fbp_ffmc", String.valueOf(d));
		}

		fbpCalculations.useBui = chckbxFbpUseBui.isSelected();
		if (fbpCalculations.useBui) {
			prefs.putBoolean("fbp_usebui", true);
			d = getDoubleFromLineEdit(txtFbpBUI);
			if (d != null) {
				prefs.putString("fbp_bui", String.valueOf(d));
			}
		}
		else
			prefs.putBoolean("fbp_usebui", false);

		fbpCalculations.useBuildup = true;
		prefs.putBoolean("fbp_useBuildup", fbpCalculations.useBuildup);

		d = getDoubleFromLineEdit(txtFbpDMC);
		if (d != null) {
			prefs.putString("fbp_dmc", String.valueOf(d));
		}
		d = getDoubleFromLineEdit(txtFbpDC);
		if (d != null) {
			prefs.putString("fbp_dc", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFbpWindSpeed);
		if (d != null) {
			d = Convert.convertUnit(d, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
			prefs.putString("fbp_windspeed", String.valueOf(d));
		}

		d = getDoubleFromLineEdit(txtFbpWindDirection);
		if (d != null) {
			prefs.putString("fbp_winddirection", String.valueOf(d));
		}

		if (txtFbpPercentConifer.isVisible()) {
			d = getDoubleFromLineEdit(txtFbpPercentConifer);
			if (d != null) {
				prefs.putString("fbp_coniferMixedwood", String.valueOf(d));
			}
		}
		if (txtFbpPercentDeadFir.isVisible()) {
			d = getDoubleFromLineEdit(txtFbpPercentDeadFir);
			if (d != null) {
				prefs.putString("fbp_deadBalsam", String.valueOf(d));
			}
		}
		if (txtFbpGrassCuring.isVisible()) {
			d = getDoubleFromLineEdit(txtFbpGrassCuring);
			if (d != null) {
				prefs.putString("fbp_grassCuring", String.valueOf(d));
			}
		}
		if (txtFbpGrassFuelLoad.isVisible()) {
			d = getDoubleFromLineEdit(txtFbpGrassFuelLoad);
			if (d != null) {
				d = Convert.convertUnit(d, UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(Main.unitSystem()));
				prefs.putString("fbp_grassFuelLoad", String.valueOf(d));
			}
		}
		if (txtFbpCrownBaseHeight.isVisible()) {
			d = getDoubleFromLineEdit(txtFbpCrownBaseHeight);
			if (d != null) {
				d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
				prefs.putString("fbp_crownBase", String.valueOf(d));
			}
		}
		d = getDoubleFromLineEdit(txtFbpElapsedTime);
		if (d != null) {
			prefs.putString("fbp_elapsedTime", String.valueOf(d));
		}

		prefs.putString("fbp_ignPoint",
				String.valueOf(rdBtnFbpPoint.isSelected()));

		Date tm = ((SpinnerDateModel) spinnerFbpStartTime.getModel())
				.getDate();
		prefs.putInt("fbp_hour", tm.getHours());
		prefs.putInt("fbp_min", tm.getMinutes());
	}

	@SuppressWarnings("deprecation")
	public void loadAllValues() {
		RPreferences prefs = Main.prefs;
		
		int ind = Integer.parseInt(prefs.getString("fbp_fuelList", "0"));
		//In the rare case they had O1ab selected before it was removed
		if(ind == 18) ind = 19;
		
		comboFbpFuelType.setSelectedIndex(adjustIndexFuelTypeToComboBox(ind));

		txtFbpFFMC.setText(DecimalUtils.format(prefs.getDouble("fbp_ffmc", 85.0), DecimalUtils.DataType.FFMC));

		chckbxFbpUseBui.setSelected(prefs.getBoolean("fbp_usebui", true));
		if (chckbxFbpUseBui.isSelected()) {
			txtFbpBUI.setText(DecimalUtils.format(prefs.getDouble("fbp_bui", 40.0), DecimalUtils.DataType.FORCE_1));
			txtFbpDMC.setEnabled(false);
			txtFbpDC.setEnabled(false);
			txtFbpBUI.setEnabled(true);
		}
		else {
			txtFbpDMC.setEnabled(true);
			txtFbpDC.setEnabled(true);
			txtFbpBUI.setEnabled(false);
		}

		txtFbpDMC.setText(DecimalUtils.format(prefs.getDouble("fbp_dmc", 25.0), DecimalUtils.DataType.DMC));
		txtFbpDC.setText(DecimalUtils.format(prefs.getDouble("fbp_dc", 200.0), DecimalUtils.DataType.DC));
		txtFbpWindSpeed.setText(DecimalUtils.format(Convert.convertUnit(prefs.getDouble("fbp_windspeed", 0.0), UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC)), DecimalUtils.DataType.WIND_SPEED));
		String dir = prefs.getString("fbp_winddirection", "0");
		Double d = null;
		try {
			d = Double.parseDouble(dir);
		}
		catch (NumberFormatException ex) {
			d = 0.0;
		}
		txtFbpWindDirection.setText(DecimalUtils.format(d, DecimalUtils.DataType.WIND_DIR));
		txtFbpPercentConifer.setText(DecimalUtils.format(prefs.getDouble("fbp_coniferMixedwood", 50.0), DecimalUtils.DataType.FORCE_1));
		txtFbpPercentDeadFir.setText(DecimalUtils.format(prefs.getDouble("fbp_deadBalsam", 50.0), DecimalUtils.DataType.FORCE_1));
		txtFbpGrassCuring.setText(DecimalUtils.format(prefs.getDouble("fbp_grassCuring", 60.0), DecimalUtils.DataType.FORCE_1));
		txtFbpGrassFuelLoad.setText(DecimalUtils.format(Convert.convertUnit(prefs.getDouble("fbp_grassFuelLoad", 0.35), UnitSystem.fuelConsumpiton(Main.unitSystem()), UnitSystem.fuelConsumpiton(UnitSystem.METRIC)), DecimalUtils.DataType.FORCE_2));
		txtFbpCrownBaseHeight.setText(DecimalUtils.format(Convert.convertUnit(prefs.getDouble("fbp_crownBase", 7.0), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)), DecimalUtils.DataType.FORCE_1));
		txtFbpElapsedTime.setText(DecimalUtils.format(prefs.getDouble("fbp_elapsedTime", 60.0), DecimalUtils.DataType.FORCE_1));
		rdBtnFbpPoint.setSelected(Boolean.parseBoolean(prefs.getString("fbp_ignPoint", "True")));
		rdBtnFbpLine.setSelected(!rdBtnFbpPoint.isSelected());
		
		txtFbpElevation.setText(DecimalUtils.format(Convert.convertUnit(Main.prefs.getDouble("fbp_elevation", fbpCalculations.slopeValue),
				UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)), DecimalUtils.DataType.FORCE_1));
		txtFbpSlope.setText(DecimalUtils.format(Main.prefs.getDouble("fbp_slope", 0.0), DecimalUtils.DataType.FORCE_1));
		txtFbpAspect.setText(DecimalUtils.format(Main.prefs.getDouble("fbp_aspect", 0.0), DecimalUtils.DataType.FORCE_1));

		Calendar cal = Calendar.getInstance();
		Date tm = new Date();
		tm.setHours(prefs.getInt("fbp_hour", cal.get(Calendar.HOUR_OF_DAY)));
		tm.setMinutes(prefs.getInt("fbp_min", cal.get(Calendar.MINUTE)));
		spinnerFbpStartTime.getModel().setValue(tm);

		getValuesFromForm();
	}

	public void transferToStatsTab() {
		if (!app.statsTab.canTransferTo()) {
			JOptionPane.showMessageDialog(app.frmRedapp, Main.resourceManager.getString("ui.label.fbp.tostats.error"));
			return;
		}
		int ind = comboFbpFuelType.getSelectedIndex();
		switch (ind) {
		case 5:
			Double cbh = Convert.convertUnit(LineEditHelper.getDoubleFromLineEdit(txtFbpCrownBaseHeight), UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
			app.statsTab.setFBPFuelValues(ind, cbh, null, null, null, null);
			break;
		case 9:
		case 10:
			Double pc = LineEditHelper.getDoubleFromLineEdit(txtFbpPercentConifer);
			app.statsTab.setFBPFuelValues(ind, null, pc, null, null, null);
			break;
		case 11:
		case 12:
			Double pdf = LineEditHelper.getDoubleFromLineEdit(txtFbpPercentDeadFir);
			app.statsTab.setFBPFuelValues(ind, null, null, pdf, null, null);
			break;
		case 13:
		case 14:
		//case 15: O1ab
			Double gc = LineEditHelper.getDoubleFromLineEdit(txtFbpGrassCuring);
			Double gfl = Convert.convertUnit(LineEditHelper.getDoubleFromLineEdit(txtFbpGrassFuelLoad), UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(Main.unitSystem()));
			app.statsTab.setFBPFuelValues(ind, null, null, null, gc, gfl);
			break;
		default:
			app.statsTab.setFBPFuelValues(ind, null, null, null, null, null);
			break;
		}
		Double elev = Convert.convertUnit(LineEditHelper.getDoubleFromLineEdit(txtFbpElevation), UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
		Boolean useSlope = chckbxFbpSlope.isSelected();
		Double slope = LineEditHelper.getDoubleFromLineEdit(txtFbpSlope);
		Double aspect = LineEditHelper.getDoubleFromLineEdit(txtFbpAspect);
		app.statsTab.setFBPTerrainValues(elev, useSlope, useSlope ? slope : null, aspect);
		app.setCurrentTab(app.statsTab);
	}

	/**
	 * Get the fbp calculator.
	 * @return
	 */
	public FBPCalculations getCalculator() {
		return fbpCalculations;
	}

	public void showConvertedValueDHChanged(RMapValueTextField edit,
			Boolean show) {
		Main.prefs.putBoolean("fbp_dh_showConverted", show.booleanValue());
		txtFbpDH.setShowConverted(show.booleanValue());
	}

	public void showConvertedValueDFChanged(RMapValueTextField edit,
			Boolean show) {
		Main.prefs.putBoolean("fbp_df_showConverted", show.booleanValue());
		txtFbpDF.setShowConverted(show.booleanValue());
	}

	public void showConvertedValueDBChanged(RMapValueTextField edit,
			Boolean show) {
		Main.prefs.putBoolean("fbp_db_showConverted", show.booleanValue());
		txtFbpDB.setShowConverted(show.booleanValue());
	}

	public void mapScalingChanged(int i, MetricPrefix units) {
		txtFbpDF.setScaleAndUnits(i, units);
		txtFbpDH.setScaleAndUnits(i, units);
		txtFbpDB.setScaleAndUnits(i, units);
	}

	//Redmine 712
	public void transferValues(String ffmc, String dmc, String dc, String bui, Boolean useBui, String ws, String wd, Date dt) {
		initTransfer = true;
		
		if (ffmc != null) {
			txtFbpFFMC.setText(DecimalUtils.format(Double.parseDouble(ffmc), DecimalUtils.DataType.FFMC));
			passedValues.ffmc = ffmc;
		} else {
			passedValues.ffmc = "";
		}
		
		if (dmc != null) {
			txtFbpDMC.setText(DecimalUtils.format(Double.parseDouble(dmc), DecimalUtils.DataType.DMC));
			passedValues.dmc = dmc;
		} else {
			passedValues.dmc = "";
		}
		
		if (dc != null) {
			txtFbpDC.setText(DecimalUtils.format(Double.parseDouble(dc), DecimalUtils.DataType.DC));
			passedValues.dc= dc;;
		} else {
			passedValues.dc = "";
		}
		
		if (bui != null) {
			txtFbpBUI.setText(DecimalUtils.format(Double.parseDouble(bui), DecimalUtils.DataType.FORCE_1));
			passedValues.bui = bui;
		} else {
			passedValues.bui = "";
		}
		
		if (useBui != null) {
			chckbxFbpUseBui.setSelected(useBui);
			if (useBui) {
				txtFbpDMC.setEnabled(false);
				txtFbpDC.setEnabled(false);
				txtFbpBUI.setEnabled(true);
			}
			else {
				txtFbpDMC.setEnabled(true);
				txtFbpDC.setEnabled(true);
				txtFbpBUI.setEnabled(false);
			}
		}
		
		if (ws != null)
			txtFbpWindSpeed.setText(DecimalUtils.format(Convert.convertUnit(ca.redapp.util.DoubleEx.valueOf(ws), UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC)), DataType.WIND_SPEED));
		if (wd != null)
			txtFbpWindDirection.setText(wd);
		if (dt != null)
			spinnerFbpStartTime.getModel().setValue(dt);
		
		initTransfer = false;
	}

	public double getDH() {
		return Convert.convertUnit(txtFbpDH.getActualValue(), UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
	}

	public double getDF() {
		return fbpCalculations.distanceFlank;
	}

	public double getDB() {
		return Convert.convertUnit(txtFbpDB.getActualValue(), UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
	}

	public double getRAZ() {
		return LineEditHelper.getDoubleFromLineEdit(txtFbpRAZ);
	}
	
	public double getArea() {
		return Convert.convertUnit(LineEditHelper.getDoubleFromLineEdit(txtFbpArea), UnitSystem.area(UnitSystem.METRIC), UnitSystem.area(Main.unitSystem()));
	}
	
	//Redmine 712
	public void txtFbpFFMCChanged() {
		if(!initTransfer) {
			String temp = txtFbpFFMC.getText();
			Double val = DecimalUtils.valueOf(temp);
			if(val != null) {
				passedValues.ffmc = temp;
			}
		}
	}
	
	public void txtFbpDMCChanged() {
		if(!initTransfer) {
			String temp = txtFbpDMC.getText();
			Double val = DecimalUtils.valueOf(temp);
			if(val != null) {
				passedValues.dmc = temp;
			}
		}
	}
	
	public void txtFbpDCChanged() {
		if(!initTransfer) {
			String temp = txtFbpDC.getText();
			Double val = DecimalUtils.valueOf(temp);
			if(val != null) {
				passedValues.dc = temp;
			}
		}
	}
	
	public void txtFbpBUIChanged() {
		if(!initTransfer) {
			String temp = txtFbpBUI.getText();
			Double val = DecimalUtils.valueOf(temp);
			if(val != null) {
				passedValues.bui = temp;
			}
		}
	}
	
	
	public void txtElevationChanged() {
		if (Boolean.parseBoolean(Main.prefs.getString("saveValues", "true"))) {
			String temp = txtFbpElevation.getText();
			Double val = DecimalUtils.valueOf(temp);
			if (val != null) {
				val = Convert.convertUnit(val, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
				Main.prefs.putDouble("fbp_elevation", val);
			}
		}
	}
	
	public void txtSlopeChanged() {
		if (Boolean.parseBoolean(Main.prefs.getString("saveValues", "true"))) {
			String temp = txtFbpSlope.getText();
			Double val= DecimalUtils.valueOf(temp);
			if (val != null) {
				Main.prefs.putDouble("fbp_slope", val);
			}
		}
	}
	
	public void txtAspectChanged() {
		if (Boolean.parseBoolean(Main.prefs.getString("saveValues", "true"))) {
			String temp = txtFbpAspect.getText();
			Double val= DecimalUtils.valueOf(temp);
			if (val != null) {
				Main.prefs.putDouble("fbp_aspect", val);
			}
		}
	}
	
	public void chkbxUseSlopeChanged() {
		if (Boolean.parseBoolean(Main.prefs.getString("saveValues", "true"))) {
			boolean b = chckbxFbpSlope.isSelected();
			Main.prefs.putString("fbp_useSlope", String.valueOf(b));
		}
	}

	// {{ UI Stuff

	private RTextField txtFbpFFMC;
	private RTextField txtFbpDMC;
	private RTextField txtFbpDC;
	private RTextField txtFbpBUI;
	private RTextField txtFbpWindSpeed;
	private RTextField txtFbpWindDirection;
	private RTextField txtFbpElevation;
	private RTextField txtFbpSlope;
	private RTextField txtFbpAspect;
	private RTextField txtFbpElapsedTime;
	private RTextField txtFbpWSV;
	private RTextField txtFbpRAZ;
	private RTextField txtFbpISI;
	private RTextField txtFbpFMC;
	private RTextField txtFbpROSt;
	private RTextField txtFbpROSeq;
	private RTextField txtFbpCFB;

	private RLabel lblFbpCFB;
	private RLabel lblFbpCfbUnit;
	
	private RTextField txtFbpHFI;
	private RTextField txtFbpSFC;
	private RTextField txtFbpCFC;
	private RTextField txtFbpTFC;
	private RTextField txtFbpRSO;
	private RTextField txtFbpFROS;
	private RTextField txtFbpBROS;
	private RTextField txtFbpCSI;
	private RTextField txtFbpFFI;
	private RTextField txtFbpBFI;
	private RTextField txtFbpLB;
	private RTextField txtFbpArea;
	private RTextField txtFbpPerimeter;
	private RTextField txtFbpGrassCuring;
	private RTextField txtFbpGrassFuelLoad;
	private RTextField txtFbpCrownBaseHeight;
	private RTextField txtFbpPercentConifer;
	private RTextField txtFbpPercentDeadFir;
	private RTextArea txtFbpFireDescription;
	private JButton btnFbpInformation;
	private RButton btnFbpExport;
	private RButton btnFbpTransferToStats;
	private RButton btnFbpDisplayOnMap;
	private RButton btnFbpExportMap;
	private RButton btnReset;
	private JComboBox<String> comboFbpFuelType;
	private JCheckBox chckbxFbpSlope;
	private JCheckBox chckbxFbpUseBui;
	private JRadioButton rdBtnFbpPoint;
	private JRadioButton rdBtnFbpLine;
	private JSpinner spinnerFbpStartTime;
	private RLabel lblFbpPercentConifer;
	private RLabel lblFbpPercentConiferUnit;
	private RLabel lblFbpPercentDeadFir;
	private RLabel lblFbpPercentDeadFirUnit;
	private RLabel lblFbpGrassCuring;
	private RLabel lblFbpGrassCuringUnit;
	private RLabel lblFbpGrassFuelLoad;
	private RLabel lblFbpGrassFuelLoadUnit;
	private RLabel lblFbpCrownBaseHeight;
	private RLabel lblFbpCrownBaseHeightUnit;
	private RLabel lblFbpWindSpeedUnit;
	private RLabel lblFbpElevationUnit;
	private RLabel lblFbpWsvUnit;
	private RLabel lblFbpROStUnit;
	private RLabel lblFbpROSeqUnit;
	private RLabel lblFbpSFCUnit;
	private RLabel lblFbpCFCUnit;
	private RLabel lblFbpTFCUnit;
	private RLabel lblFbpRsoUnit;
	private RLabel lblFbpFrosUnit;
	private RLabel lblFbpBrosUnit;
	private RLabel lblFbpCsiUnit;
	private RLabel lblFbpFfiUnit;
	private RLabel lblFbpBfiUnit;
	private RLabel lblFbpDHUnit;
	private RLabel lblFbpDFUnit;
	private RLabel lblFbpDBUnit;
	private RLabel lblFbpAreaUnit;
	private RLabel lblFbpPerimiterUnit;
	private RLabel lblFbpHFIUnits;
	private RMapValueTextField txtFbpDH;
	private RMapValueTextField txtFbpDF;
	private RMapValueTextField txtFbpDB;

	private static class RLabelSub extends RLabel {
		private static final long serialVersionUID = 1L;
		private int width = 40;

		public RLabelSub(String text, int width) {
			super(text);
			this.width = width;
		}

		public RLabelSub(String text) {
			super(text);
		}

		@Override
		public void setMinimumSize(Dimension minimumSize) {
			super.setMinimumSize(new Dimension(width, 20));
		}

		@Override
		public Dimension getMinimumSize() {
			return new Dimension(width, 20);
		}

		@Override
		public void setMaximumSize(Dimension maximumSize) {
			super.setMaximumSize(new Dimension(width, 20));
		}

		@Override
		public Dimension getMaximumSize() {
			return new Dimension(width, 20);
		}

		@Override
		public void setPreferredSize(Dimension preferredSize) {
			super.setPreferredSize(new Dimension(width, 20));
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(width, 20);
		}

		@Override
		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, width, 20);
		}
	}

	protected void initialize() {
		if (initialized)
			return;
		initialized = true;

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(3,10,3,10);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;

		GridBagConstraints gbcSecondary = new GridBagConstraints();
		gbcSecondary.insets = new Insets(3,10,3,10);


		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 971, 501);
		else
			setBounds(0, 0, 981, 506);

		if (Main.isWindows())
			setBackground(Color.white);

		RGroupBox panelFbpFuelType = new RGroupBox();
		panelFbpFuelType.setLayout(new GridBagLayout());
		panelFbpFuelType.setText(Main.resourceManager.getString("ui.label.fbp.fuel.title"));
		//panelFbpFuelType.setBounds(5, 10, 291, 145);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		this.add(panelFbpFuelType, gbc);

		comboFbpFuelType = new JComboBox<String>();
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
				//"O-1ab: " + Main.resourceManager.getString("ui.label.fuel.o1ab"),
				"S-1:  " + Main.resourceManager.getString("ui.label.fuel.s1"),
				"S-2:  " + Main.resourceManager.getString("ui.label.fuel.s2"),
				"S-3:  " + Main.resourceManager.getString("ui.label.fuel.s3") }));
		comboFbpFuelType.setBounds(10, 30, 271, 22);
		 gbcSecondary.gridx = 0; gbcSecondary.gridy				 =0;
		gbcSecondary.fill = GridBagConstraints.HORIZONTAL;
		panelFbpFuelType.add(comboFbpFuelType, gbcSecondary);

		RGroupBox groupFbpWeather = new RGroupBox();
		groupFbpWeather.setLayout(new BorderLayout());
		groupFbpWeather.setText(Main.resourceManager.getString("ui.label.fbp.weather.title"));
		groupFbpWeather.setBounds(5, 164, 291, 261);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 2;
		this.add(groupFbpWeather, gbc);

		JPanel panelFbpWeather = new JPanel();
		panelFbpWeather.setBackground(new Color(245, 245, 245));
		panelFbpWeather.setBounds(10, 20, 270, 180);
		panelFbpWeather.setLayout(new SpringLayout());
		groupFbpWeather.add(panelFbpWeather);

		RLabel LblFbpFFMC = new RLabel(Main.resourceManager.getString("ui.label.fire.ffmc"));
		LblFbpFFMC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.ffmc"));
		panelFbpWeather.add(LblFbpFFMC);

		txtFbpFFMC = new RTextField();
		txtFbpFFMC.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtFbpFFMC.getPreferredSize().height));
		panelFbpWeather.add(txtFbpFFMC);
		txtFbpFFMC.setColumns(10);

		panelFbpWeather.add(new JLabel(""));

		RLabel LblFbpDMC = new RLabel(Main.resourceManager.getString("ui.label.fire.dmc"));
		LblFbpDMC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.dmc"));
		panelFbpWeather.add(LblFbpDMC);

		txtFbpDMC = new RTextField();
		txtFbpDMC.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtFbpDMC.getPreferredSize().height));
		txtFbpDMC.setColumns(10);
		panelFbpWeather.add(txtFbpDMC);

		panelFbpWeather.add(new JLabel(""));

		RLabel LblFbpDC = new RLabel(Main.resourceManager.getString("ui.label.fire.dc"));
		LblFbpDC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.dc"));
		panelFbpWeather.add(LblFbpDC);

		txtFbpDC = new RTextField();
		txtFbpDC.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtFbpDC.getPreferredSize().height));
		txtFbpDC.setColumns(10);
		panelFbpWeather.add(txtFbpDC);

		panelFbpWeather.add(new JLabel(""));

		chckbxFbpUseBui = new JCheckBox(Main.resourceManager.getString("ui.label.fire.bui")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void setMinimumSize(Dimension minimumSize) {
				super.setMinimumSize(new Dimension(minimumSize.width, 20));
			}

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(super.getMinimumSize().width, 20);
			}

			@Override
			public void setMaximumSize(Dimension maximumSize) {
				super.setMaximumSize(new Dimension(maximumSize.width, 20));
			}

			@Override
			public Dimension getMaximumSize() {
				return new Dimension(super.getMaximumSize().width, 20);
			}

			@Override
			public void setPreferredSize(Dimension preferredSize) {
				super.setPreferredSize(new Dimension(preferredSize.width, 20));
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().width, 20);
			}

			@Override
			public void setBounds(int x, int y, int width, int height) {
				super.setBounds(x, y, width, 20);
			}
		};
		chckbxFbpUseBui.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.bui"));
		chckbxFbpUseBui.setHorizontalTextPosition(SwingConstants.RIGHT);
		if (Main.isLinux())
			chckbxFbpUseBui.setFont(chckbxFbpUseBui.getFont().deriveFont(12.0f));
		chckbxFbpUseBui.setBackground(new Color(245, 245, 245));
		panelFbpWeather.add(chckbxFbpUseBui);
		
		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout(0, 0));
		pnl.setBackground(new Color(245, 245, 245));
		pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtFbpDC.getMaximumSize().height));
		txtFbpBUI = new RTextField();
		txtFbpBUI.setPreferredSize(new Dimension(txtFbpDC.getPreferredSize().width, txtFbpDC.getMaximumSize().height));
		txtFbpBUI.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtFbpDC.getMaximumSize().height));
		txtFbpBUI.setColumns(10);
		pnl.add(txtFbpBUI, BorderLayout.NORTH);
		panelFbpWeather.add(pnl);
		
		//Redmine 718
		txtFbpFFMC.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class,
				this, "txtFbpFFMCChanged"));
		txtFbpDMC.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class,
				this, "txtFbpDMCChanged"));
		txtFbpDC.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class,
				this, "txtFbpDCChanged"));
		txtFbpBUI.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class,
				this, "txtFbpBUIChanged"));
		
		//slf4j
		
		panelFbpWeather.add(new JLabel(""));

		RLabel LblFbpWindSpeed = new RLabel(Main.resourceManager.getString("ui.label.weather.ws"));
		panelFbpWeather.add(LblFbpWindSpeed);

		txtFbpWindSpeed = new RTextField();
		txtFbpWindSpeed.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtFbpWindSpeed.getPreferredSize().height));
		txtFbpWindSpeed.setColumns(10);
		panelFbpWeather.add(txtFbpWindSpeed);
		
		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpWindSpeedUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblFbpWindSpeedUnit = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		panelFbpWeather.add(lblFbpWindSpeedUnit);

		RLabel lblWindDirection = new RLabel(Main.resourceManager.getString("ui.label.weather.wd"));
		panelFbpWeather.add(lblWindDirection);

		txtFbpWindDirection = new RTextField();
		txtFbpWindDirection.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtFbpWindDirection.getPreferredSize().height));
		txtFbpWindDirection.setColumns(10);
		panelFbpWeather.add(txtFbpWindDirection);

		RLabel LblFbpWindDirectionUnit = new RLabel(Main.resourceManager.getString("ui.label.units.degrees"));
		panelFbpWeather.add(LblFbpWindDirectionUnit);

		SpringUtilities.makeCompactGrid(panelFbpWeather, 6, 3, 6, 6, 6, 10);

		RGroupBox groupFbpTerrain = new RGroupBox();
		groupFbpTerrain.setLayout(new BorderLayout());
		groupFbpTerrain.setText(Main.resourceManager.getString("ui.label.fbp.terrain.title"));
		groupFbpTerrain.setBounds(305, 10, 206, 145);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		this.add(groupFbpTerrain, gbc);

		JPanel panelFbpTerrain = new JPanel();
		panelFbpTerrain.setBounds(10, 20, 186, 90);
		panelFbpTerrain.setBackground(new Color(245, 245, 245));
		panelFbpTerrain.setLayout(new SpringLayout());
		groupFbpTerrain.add(panelFbpTerrain);

		RLabel LblFbpTerrainElevation = new RLabel(Main.resourceManager.getString("ui.label.fbp.terrain.elev"));
		panelFbpTerrain.add(LblFbpTerrainElevation);

		txtFbpElevation = new RTextField();
		txtFbpElevation.setColumns(10);
		panelFbpTerrain.add(txtFbpElevation);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpElevationUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblFbpElevationUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
		lblFbpElevationUnit.setBounds(170, 20, 20, 20);
		panelFbpTerrain.add(lblFbpElevationUnit);

		chckbxFbpSlope = new JCheckBox(Main.resourceManager.getString("ui.label.fbp.terrain.slope")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void setMinimumSize(Dimension minimumSize) {
				super.setMinimumSize(new Dimension(minimumSize.width, 20));
			}

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(super.getMinimumSize().width, 20);
			}

			@Override
			public void setMaximumSize(Dimension maximumSize) {
				super.setMaximumSize(new Dimension(maximumSize.width, 20));
			}

			@Override
			public Dimension getMaximumSize() {
				return new Dimension(super.getMaximumSize().width, 20);
			}

			@Override
			public void setPreferredSize(Dimension preferredSize) {
				super.setPreferredSize(new Dimension(preferredSize.width, 20));
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().width, 20);
			}

			@Override
			public void setBounds(int x, int y, int width, int height) {
				super.setBounds(x, y, width, 20);
			}
		};
		if (Main.isLinux())
			chckbxFbpSlope.setFont(chckbxFbpSlope.getFont().deriveFont(12.0f));
		chckbxFbpSlope.setBackground(new Color(245, 245, 245));
		panelFbpTerrain.add(chckbxFbpSlope);

		txtFbpSlope = new RTextField();
		txtFbpSlope.setColumns(10);
		panelFbpTerrain.add(txtFbpSlope);

		RLabel LblFbpSlopeUnit = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		panelFbpTerrain.add(LblFbpSlopeUnit);

		RLabel LblFbpAspect = new RLabel(Main.resourceManager.getString("ui.label.fbp.terrain.aspect"));
		panelFbpTerrain.add(LblFbpAspect);

		txtFbpAspect = new RTextField();
		txtFbpAspect.setColumns(10);
		panelFbpTerrain.add(txtFbpAspect);

		RLabel LblFbpAspectUnit = new RLabel(Main.resourceManager.getString("ui.label.units.degrees"));
		panelFbpTerrain.add(LblFbpAspectUnit);

		SpringUtilities.makeCompactGrid(panelFbpTerrain, 3, 3, 5, 5, 5, 5, 10, 10);
		
		txtFbpElevation.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class,
				this, "txtElevationChanged"));
		txtFbpSlope.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class,
				this, "txtSlopeChanged"));
		txtFbpAspect.getDocument().addDocumentListener((DocumentListener)EventHandler.create(DocumentListener.class,
				this, "txtAspectChanged"));

		RGroupBox groupIgnition = new RGroupBox();
		groupIgnition.setLayout(new BorderLayout());
		groupIgnition.setText(Main.resourceManager.getString("ui.label.fbp.ignition.title"));
		groupIgnition.setBounds(520, 10, 206, 145);
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		this.add(groupIgnition, gbc);

		JPanel panelIgnition = new JPanel();
		panelIgnition.setBackground(new Color(245, 245, 245));
		panelIgnition.setBounds(10, 20, 186, 95);
		panelIgnition.setLayout(new SpringLayout());
		groupIgnition.add(panelIgnition);

		ButtonGroup btnGroupFbpIgnitionType = new ButtonGroup();

		rdBtnFbpPoint = new JRadioButton(Main.resourceManager.getString("ui.label.fbp.ignition.point")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void setMinimumSize(Dimension minimumSize) {
				super.setMinimumSize(new Dimension(minimumSize.width, 20));
			}

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(super.getMinimumSize().width, 20);
			}

			@Override
			public void setMaximumSize(Dimension maximumSize) {
				super.setMaximumSize(new Dimension(maximumSize.width, 20));
			}

			@Override
			public Dimension getMaximumSize() {
				return new Dimension(super.getMaximumSize().width, 20);
			}

			@Override
			public void setPreferredSize(Dimension preferredSize) {
				super.setPreferredSize(new Dimension(preferredSize.width, 20));
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().width, 20);
			}

			@Override
			public void setBounds(int x, int y, int width, int height) {
				super.setBounds(x, y, width, 20);
			}
		};
		if (Main.isLinux())
			rdBtnFbpPoint.setFont(rdBtnFbpPoint.getFont().deriveFont(12.0f));
		rdBtnFbpPoint.setBackground(new Color(245, 245, 245));
		btnGroupFbpIgnitionType.add(rdBtnFbpPoint);
		panelIgnition.add(rdBtnFbpPoint);

		rdBtnFbpLine = new JRadioButton(Main.resourceManager.getString("ui.label.fbp.ignition.line")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void setMinimumSize(Dimension minimumSize) {
				super.setMinimumSize(new Dimension(minimumSize.width, 20));
			}

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(super.getMinimumSize().width, 20);
			}

			@Override
			public void setMaximumSize(Dimension maximumSize) {
				super.setMaximumSize(new Dimension(maximumSize.width, 20));
			}

			@Override
			public Dimension getMaximumSize() {
				return new Dimension(super.getMaximumSize().width, 20);
			}

			@Override
			public void setPreferredSize(Dimension preferredSize) {
				super.setPreferredSize(new Dimension(preferredSize.width, 20));
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().width, 20);
			}

			@Override
			public void setBounds(int x, int y, int width, int height) {
				super.setBounds(x, y, width, 20);
			}
		};
		if (Main.isLinux())
			rdBtnFbpLine.setFont(rdBtnFbpLine.getFont().deriveFont(12.0f));
		rdBtnFbpLine.setBackground(new Color(245, 245, 245));
		btnGroupFbpIgnitionType.add(rdBtnFbpLine);
		panelIgnition.add(rdBtnFbpLine);

		RLabel lblTime = new RLabel(Main.resourceManager.getString("ui.label.fbp.ignition.start"));
		panelIgnition.add(lblTime);

		spinnerFbpStartTime = new JSpinner() {
			private static final long serialVersionUID = 1L;

			@Override
			public void setMinimumSize(Dimension minimumSize) {
				super.setMinimumSize(new Dimension(minimumSize.width, 22));
			}

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(super.getMinimumSize().width, 22);
			}

			@Override
			public void setMaximumSize(Dimension maximumSize) {
				super.setMaximumSize(new Dimension(maximumSize.width, 22));
			}

			@Override
			public Dimension getMaximumSize() {
				return new Dimension(super.getMaximumSize().width, 22);
			}

			@Override
			public void setPreferredSize(Dimension preferredSize) {
				super.setPreferredSize(new Dimension(preferredSize.width, 22));
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().width, 22);
			}

			@Override
			public void setBounds(int x, int y, int width, int height) {
				super.setBounds(x, y, width, 22);
			}
		};
		spinnerFbpStartTime.setModel(new SpinnerDateModel(new Date(
				1390975200000L), null, null, Calendar.MINUTE));
		spinnerFbpStartTime.setEditor(new JSpinner.DateEditor(
				spinnerFbpStartTime, "H:mm"));
		if (Main.isLinux()) {
			JComponent comp = spinnerFbpStartTime.getEditor();
			if (comp instanceof JSpinner.DefaultEditor) {
				JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)comp;
				editor.getTextField().setFont(editor.getTextField().getFont().deriveFont(11.0f));
			}
		}
		panelIgnition.add(spinnerFbpStartTime);

		RLabel lblElapsedTimet = new RLabel(Main.resourceManager.getString("ui.label.fbp.ignition.elapsed"));
		lblElapsedTimet.setToolTipText(Main.resourceManager.getString("ui.label.fbp.ignition.elapsed.desc"));
		panelIgnition.add(lblElapsedTimet);

		pnl = new JPanel();
		pnl.setBackground(new Color(245, 245, 245));
		pnl.setLayout(new SpringLayout());
		panelIgnition.add(pnl);

		txtFbpElapsedTime = new RTextField();
		lblElapsedTimet.setLabelFor(txtFbpElapsedTime);
		pnl.add(txtFbpElapsedTime);
		txtFbpElapsedTime.setColumns(10);

		RLabel lblMins = new RLabel(Main.resourceManager.getString("ui.label.units.min"));
		pnl.add(lblMins);

		SpringUtilities.makeCompactGrid(pnl, 1, 2, 0, 0, 5, 0);

		SpringUtilities.makeCompactGrid(panelIgnition, 3, 2, 5, 5, 5, 5, 10, 10);

		RGroupBox groupComponents = new RGroupBox();
		groupComponents.setLayout(new BorderLayout());
		if (Main.isLinux())
			groupComponents.setText(Main.resourceManager.getString("ui.label.fbp.system.titleshort"));
		else
			groupComponents.setText(Main.resourceManager.getString("ui.label.fbp.system.title"));
		groupComponents.setBounds(735, 10, 221, 145);

		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		this.add(groupComponents, gbc);

		JPanel panelComponents = new JPanel();
		panelComponents.setBackground(new Color(245, 245, 245));
		panelComponents.setBounds(10, 20, 201, 115);
		panelComponents.setLayout(new SpringLayout());
		groupComponents.add(panelComponents);

		RLabel LblFbpWSV = new RLabelSub(Main.resourceManager.getString("ui.label.fire.wsv"));
		LblFbpWSV.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.wsv"));
		panelComponents.add(LblFbpWSV);

		txtFbpWSV = new RTextField();
		txtFbpWSV.setEditable(false);
		txtFbpWSV.setColumns(10);
		panelComponents.add(txtFbpWSV);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpWsvUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblFbpWsvUnit = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		panelComponents.add(lblFbpWsvUnit);

		RLabel LblFbpRAZ = new RLabelSub(Main.resourceManager.getString("ui.label.fire.raz"));
		LblFbpRAZ.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.raz"));
		panelComponents.add(LblFbpRAZ);

		txtFbpRAZ = new RTextField();
		txtFbpRAZ.setEditable(false);
		txtFbpRAZ.setColumns(10);
		panelComponents.add(txtFbpRAZ);

		RLabel LblFbpRazUnit = new RLabel(Main.resourceManager.getString("ui.label.units.degrees"));
		panelComponents.add(LblFbpRazUnit);

		RLabel LblFbpISI = new RLabelSub(Main.resourceManager.getString("ui.label.fire.isi"));
		LblFbpISI.setToolTipText(Main.resourceManager.getString("ui.label.fbp.ignition.isi.desc"));
		panelComponents.add(LblFbpISI);

		txtFbpISI = new RTextField();
		txtFbpISI.setEditable(false);
		txtFbpISI.setColumns(10);
		panelComponents.add(txtFbpISI);

		JLabel lbl = new JLabel("");
		panelComponents.add(lbl);

		RLabel LblFbpFWI = new RLabelSub(Main.resourceManager.getString("ui.label.fire.fmc"));
		LblFbpFWI.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.fmc"));
		panelComponents.add(LblFbpFWI);

		txtFbpFMC = new RTextField();
		txtFbpFMC.setEditable(false);
		txtFbpFMC.setColumns(10);
		panelComponents.add(txtFbpFMC);

		RLabel LblFbpFmcUnit = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		panelComponents.add(LblFbpFmcUnit);

		SpringUtilities.makeCompactGrid(panelComponents, 4, 3, 5, 5, 5, 5, 10, 10);

		RGroupBox groupFbpPrimary = new RGroupBox();
		groupFbpPrimary.setLayout(new GridBagLayout());
		groupFbpPrimary.setText(Main.resourceManager.getString("ui.label.fbp.primary.title"));
		groupFbpPrimary.setBounds(305, 164, 651, 141);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		this.add(groupFbpPrimary, gbc);

		JPanel panelFbpPrimary1 = new JPanel();
		panelFbpPrimary1.setBackground(new Color(245, 245, 245));
		panelFbpPrimary1.setBounds(10, 20, 171, 115);
		panelFbpPrimary1.setLayout(new SpringLayout());

		GridBagConstraints gbcPrimaryOutputPanel = new GridBagConstraints();
		gbcPrimaryOutputPanel.insets = new Insets(3,10,3,10);

		gbcPrimaryOutputPanel.weighty = 1;
		gbcPrimaryOutputPanel.weightx = 1;
		gbcPrimaryOutputPanel.fill = GridBagConstraints.HORIZONTAL;

		gbcPrimaryOutputPanel.gridx = 0;
		gbcPrimaryOutputPanel.gridy = 0;
		gbcPrimaryOutputPanel.gridwidth = 1;
		gbcPrimaryOutputPanel.gridheight = 1;
		groupFbpPrimary.add(panelFbpPrimary1, gbcPrimaryOutputPanel);

		RLabel lblRos = new RLabelSub(Main.resourceManager.getString("ui.label.fire.rost"));
		lblRos.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.rost"));
		lblRos.setHorizontalAlignment(SwingConstants.RIGHT);
		panelFbpPrimary1.add(lblRos);

		txtFbpROSt = new RTextField();
		txtFbpROSt.setEditable(false);
		txtFbpROSt.setColumns(10);
		panelFbpPrimary1.add(txtFbpROSt);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpROStUnit = new RLabel(Main.resourceManager.getString("ui.label.units.mmin"));
		else
			lblFbpROStUnit = new RLabel(Main.resourceManager.getString("ui.label.units.chhr"));
		panelFbpPrimary1.add(lblFbpROStUnit);

		RLabel lblRoseq = new RLabelSub(Main.resourceManager.getString("ui.label.fire.roseq"));
		lblRoseq.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.roseq"));
		lblRoseq.setHorizontalAlignment(SwingConstants.RIGHT);
		panelFbpPrimary1.add(lblRoseq);

		txtFbpROSeq = new RTextField();
		txtFbpROSeq.setEditable(false);
		txtFbpROSeq.setColumns(10);
		panelFbpPrimary1.add(txtFbpROSeq);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpROSeqUnit = new RLabelSub(Main.resourceManager.getString("ui.label.units.mmin"));
		else
			lblFbpROSeqUnit = new RLabelSub(Main.resourceManager.getString("ui.label.units.chhr"));
		panelFbpPrimary1.add(lblFbpROSeqUnit);

		RLabel LblFbpHFI = new RLabel(Main.resourceManager.getString("ui.label.fire.hfi"));
		LblFbpHFI.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.hfi"));
		LblFbpHFI.setHorizontalAlignment(SwingConstants.RIGHT);
		panelFbpPrimary1.add(LblFbpHFI);

		txtFbpHFI = new RTextField();
		txtFbpHFI.setEditable(false);
		txtFbpHFI.setColumns(10);
		panelFbpPrimary1.add(txtFbpHFI);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpHFIUnits = new RLabel(Main.resourceManager.getString("ui.label.units.kwm"));
		else
			lblFbpHFIUnits = new RLabel(Main.resourceManager.getString("ui.label.units.btufts"));
		panelFbpPrimary1.add(lblFbpHFIUnits);

		lblFbpCFB = new RLabelSub(Main.resourceManager.getString("ui.label.fire.cfb"));
		lblFbpCFB.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.cfb"));
		lblFbpCFB.setHorizontalAlignment(SwingConstants.RIGHT);
		panelFbpPrimary1.add(lblFbpCFB);

		txtFbpCFB = new RTextField();
		txtFbpCFB.setEditable(false);
		txtFbpCFB.setColumns(10);
		panelFbpPrimary1.add(txtFbpCFB);

		lblFbpCfbUnit = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		panelFbpPrimary1.add(lblFbpCfbUnit);

		SpringUtilities.makeCompactGrid(panelFbpPrimary1, 4, 3, 5, 5, 5, 5, 10, 10);

		panelFbpPrimary1 = new JPanel();
		panelFbpPrimary1.setBackground(new Color(245, 245, 245));
		panelFbpPrimary1.setBounds(195, 20, 171, 85);
		panelFbpPrimary1.setLayout(new SpringLayout());
		gbcPrimaryOutputPanel.gridx = 1;
		gbcPrimaryOutputPanel.gridy = 0;
		gbcPrimaryOutputPanel.gridwidth = 1;
		gbcPrimaryOutputPanel.gridheight = 1;
		groupFbpPrimary.add(panelFbpPrimary1, gbcPrimaryOutputPanel);

		RLabel LblFbpSFC = new RLabelSub(Main.resourceManager.getString("ui.label.fire.sfc"));
		LblFbpSFC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.sfc"));
		LblFbpSFC.setHorizontalAlignment(SwingConstants.RIGHT);
		panelFbpPrimary1.add(LblFbpSFC);

		txtFbpSFC = new RTextField();
		txtFbpSFC.setEditable(false);
		txtFbpSFC.setColumns(10);
		panelFbpPrimary1.add(txtFbpSFC);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpSFCUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kgm2"));
		else
			lblFbpSFCUnit = new RLabel(Main.resourceManager.getString("ui.label.units.tonsperacre"));
		panelFbpPrimary1.add(lblFbpSFCUnit);

		RLabel LblFbpCFC = new RLabelSub(Main.resourceManager.getString("ui.label.fire.cfc"));
		LblFbpCFC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.cfc"));
		LblFbpCFC.setHorizontalAlignment(SwingConstants.RIGHT);
		panelFbpPrimary1.add(LblFbpCFC);

		txtFbpCFC = new RTextField();
		txtFbpCFC.setEditable(false);
		txtFbpCFC.setColumns(10);
		panelFbpPrimary1.add(txtFbpCFC);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpCFCUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kgm2"));
		else
			lblFbpCFCUnit = new RLabel(Main.resourceManager.getString("ui.label.units.tonsperacre"));
		panelFbpPrimary1.add(lblFbpCFCUnit);

		RLabel LblFbpTFC = new RLabelSub(Main.resourceManager.getString("ui.label.fire.tfc"));
		LblFbpTFC.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.tfc"));
		LblFbpTFC.setHorizontalAlignment(SwingConstants.RIGHT);
		panelFbpPrimary1.add(LblFbpTFC);

		txtFbpTFC = new RTextField();
		txtFbpTFC.setEditable(false);
		txtFbpTFC.setColumns(10);
		panelFbpPrimary1.add(txtFbpTFC);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpTFCUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kgm2"));
		else
			lblFbpTFCUnit = new RLabel(Main.resourceManager.getString("ui.label.units.tonsperacre"));
		panelFbpPrimary1.add(lblFbpTFCUnit);

		SpringUtilities.makeCompactGrid(panelFbpPrimary1, 3, 3, 5, 5, 5, 5, 10, 10);

		RLabel lblFireDescription = new RLabel(Main.resourceManager.getString("ui.label.fbp.primary.desc"));
		lblFireDescription.setBounds(410, 20, 231, 20);

		JPanel panelFireDescription = new JPanel(new BorderLayout());
		gbcPrimaryOutputPanel.gridx = 2;
		gbcPrimaryOutputPanel.gridy = 0;
		gbcPrimaryOutputPanel.gridwidth = 1;
		gbcPrimaryOutputPanel.gridheight = 1;
		gbcPrimaryOutputPanel.fill = GridBagConstraints.BOTH;
		groupFbpPrimary.add(panelFireDescription, gbcPrimaryOutputPanel);
		panelFireDescription.add(lblFireDescription, BorderLayout.NORTH);

		txtFbpFireDescription = new RTextArea();
		txtFbpFireDescription.setFont(txtFbpCFC.getFont());
		txtFbpFireDescription.setBackground(UIManager
				.getColor("TextField.disabledBackground"));
		txtFbpFireDescription.setEditable(false);
		txtFbpFireDescription.setBounds(410, 40, 231, 61);
		panelFireDescription.add(txtFbpFireDescription, BorderLayout.CENTER);
		txtFbpFireDescription.setColumns(10);

		RGroupBox groupFbpSecondary = new RGroupBox();
		groupFbpSecondary.setLayout(new GridBagLayout());
		groupFbpSecondary.setText(Main.resourceManager.getString("ui.label.fbp.secondary.title"));
		groupFbpSecondary.setBounds(305, 314, 651, 111);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		this.add(groupFbpSecondary, gbc);

        //These constraints will be used for the four overall columns in the group
		GridBagConstraints gbcSecondaryOutputs = new GridBagConstraints();
        gbcSecondaryOutputs.insets = new Insets(3,3,3,3);
		gbcSecondaryOutputs.weighty = 1;
		gbcSecondaryOutputs.weightx = 1;
		gbcSecondaryOutputs.fill = GridBagConstraints.HORIZONTAL;
		gbcSecondaryOutputs.gridx = 0;
		gbcSecondaryOutputs.gridy = 0;
		gbcSecondaryOutputs.gridwidth = 1;
		gbcSecondaryOutputs.gridheight = 1;

        //These constraints will be used for the three sub columns within each of those main columns (label, value, uom)
        GridBagConstraints gbcSubLabelCol = new GridBagConstraints();
        gbcSubLabelCol.insets = new Insets(5,0,5,2);
        gbcSubLabelCol.weighty = 1;
        gbcSubLabelCol.weightx = 0;
        //gbcSubLabelCol.anchor = GridBagConstraints.EAST;
        //gbcSubLabelCol.fill = GridBagConstraints.HORIZONTAL;
        gbcSubLabelCol.gridx = 0;
        gbcSubLabelCol.gridwidth = 1;
        gbcSubLabelCol.gridheight = 1;

        GridBagConstraints gbcSubValueCol = new GridBagConstraints();
        gbcSubValueCol.insets = new Insets(5,1,5,1);
        gbcSubValueCol.weighty = 1;
        gbcSubValueCol.weightx = 1.0;
        gbcSubValueCol.fill = GridBagConstraints.BOTH;
        gbcSubValueCol.gridx = 1;
        gbcSubValueCol.gridwidth = 1;
        gbcSubValueCol.gridheight = 1;

        GridBagConstraints gbcSubUomCol = new GridBagConstraints();
        gbcSubUomCol.insets = new Insets(5,1,5,1);
        gbcSubUomCol.weighty = 1;
        gbcSubUomCol.weightx = 0;
        gbcSubUomCol.anchor = GridBagConstraints.WEST;
        gbcSubUomCol.fill = GridBagConstraints.HORIZONTAL;
        //gbcSubLabelCol.fill = GridBagConstraints.LINE_START;
        gbcSubUomCol.gridx = 2;
        gbcSubUomCol.gridwidth = 1;
        gbcSubUomCol.gridheight = 1;

		JPanel panelFbpSecondary1 = new JPanel();
		panelFbpSecondary1.setBackground(new Color(245, 245, 245));
     	panelFbpSecondary1.setLayout(new GridBagLayout());
		//panelFbpSecondary1.setBounds(10, 20, 166, 85);
		groupFbpSecondary.add(panelFbpSecondary1, gbcSecondaryOutputs);

		RLabel LblFbpRSO = new RLabelSub(Main.resourceManager.getString("ui.label.fire.rso"));
		LblFbpRSO.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.rso"));
		//LblFbpRSO.setHorizontalAlignment(SwingConstants.RIGHT);
        gbcSubLabelCol.gridy = 0;
        LblFbpRSO.setHorizontalAlignment(SwingConstants.RIGHT);
		panelFbpSecondary1.add(LblFbpRSO, gbcSubLabelCol);

		txtFbpRSO = new RTextField();
		txtFbpRSO.setEditable(false);
		//txtFbpRSO.setColumns(10);
        gbcSubValueCol.gridy = 0;
		panelFbpSecondary1.add(txtFbpRSO, gbcSubValueCol);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpRsoUnit = new RLabel(Main.resourceManager.getString("ui.label.units.mmin"));
		else
			lblFbpRsoUnit = new RLabel(Main.resourceManager.getString("ui.label.units.chhr"));
        gbcSubUomCol.gridy = 0;
		panelFbpSecondary1.add(lblFbpRsoUnit, gbcSubUomCol);

		RLabel LblFbpFROS = new RLabelSub(Main.resourceManager.getString("ui.label.fire.fros"), Main.isFrench() ? 55 : 40);
		LblFbpFROS.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.fros"));
		//LblFbpFROS.setHorizontalAlignment(SwingConstants.RIGHT);
        gbcSubLabelCol.gridy = 1;
        LblFbpFROS.setHorizontalAlignment(SwingConstants.RIGHT);
		panelFbpSecondary1.add(LblFbpFROS, gbcSubLabelCol);

		txtFbpFROS = new RTextField();
		txtFbpFROS.setEditable(false);
		//txtFbpFROS.setColumns(10);
        gbcSubValueCol.gridy = 1;
		panelFbpSecondary1.add(txtFbpFROS, gbcSubValueCol);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpFrosUnit = new RLabelSub(Main.resourceManager.getString("ui.label.units.mmin"));
		else
			lblFbpFrosUnit = new RLabelSub(Main.resourceManager.getString("ui.label.units.chhr"));
        gbcSubUomCol.gridy = 1;
		panelFbpSecondary1.add(lblFbpFrosUnit, gbcSubUomCol);

		RLabel LblFbpBROS = new RLabelSub(Main.resourceManager.getString("ui.label.fire.bros"));
		LblFbpBROS.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.bros"));
		//LblFbpBROS.setHorizontalAlignment(SwingConstants.RIGHT);
        gbcSubLabelCol.gridy = 2;
        LblFbpBROS.setHorizontalAlignment(SwingConstants.RIGHT);
		panelFbpSecondary1.add(LblFbpBROS, gbcSubLabelCol);

		txtFbpBROS = new RTextField();
		txtFbpBROS.setEditable(false);
		//txtFbpBROS.setColumns(10);
        gbcSubValueCol.gridy = 2;
		panelFbpSecondary1.add(txtFbpBROS, gbcSubValueCol);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpBrosUnit = new RLabel(Main.resourceManager.getString("ui.label.units.mmin"));
		else
			lblFbpBrosUnit = new RLabel(Main.resourceManager.getString("ui.label.units.chhr"));
        gbcSubUomCol.gridy = 2;
		panelFbpSecondary1.add(lblFbpBrosUnit, gbcSubUomCol);

		//SpringUtilities.makeCompactGrid(panelFbpSecondary1, 3, 3, 5, 0, 5, 5, 10, 10);


	 JPanel	panelFbpSecondary2 = new JPanel();
        panelFbpSecondary2.setBackground(new Color(245, 245, 245));
        panelFbpSecondary2.setLayout(new GridBagLayout());
		//panelFbpSecondary1.setBounds(175, 20, 156, 85);
		gbcSecondaryOutputs.gridx = 1;
		gbcSecondaryOutputs.gridy = 0;
		groupFbpSecondary.add(panelFbpSecondary2, gbcSecondaryOutputs);

		RLabel LblFbpCSI = new RLabelSub(Main.resourceManager.getString("ui.label.fire.csi"));
		LblFbpCSI.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.csi"));
        gbcSubLabelCol.gridy = 0;
        LblFbpCSI.setHorizontalAlignment(SwingConstants.RIGHT);
        panelFbpSecondary2.add(LblFbpCSI, gbcSubLabelCol);

		txtFbpCSI = new RTextField();
		txtFbpCSI.setEditable(false);
        gbcSubValueCol.gridy = 0;
        panelFbpSecondary2.add(txtFbpCSI, gbcSubValueCol);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpCsiUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kwm"));
		else
			lblFbpCsiUnit = new RLabel(Main.resourceManager.getString("ui.label.units.btufts"));
        gbcSubUomCol.gridy = 0;
        panelFbpSecondary2.add(lblFbpCsiUnit, gbcSubUomCol);

		RLabel LblFbpFFI = new RLabelSub(Main.resourceManager.getString("ui.label.fire.ffi"));
		LblFbpFFI.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.ffi"));
        gbcSubLabelCol.gridy = 1;
        LblFbpFFI.setHorizontalAlignment(SwingConstants.RIGHT);
        panelFbpSecondary2.add(LblFbpFFI, gbcSubLabelCol);

		txtFbpFFI = new RTextField();
		txtFbpFFI.setEditable(false);
        gbcSubValueCol.gridy = 1;
        panelFbpSecondary2.add(txtFbpFFI, gbcSubValueCol);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpFfiUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kwm"));
		else
			lblFbpFfiUnit = new RLabel(Main.resourceManager.getString("ui.label.units.btufts"));
        gbcSubUomCol.gridy = 1;
        panelFbpSecondary2.add(lblFbpFfiUnit, gbcSubUomCol);

		RLabel LblFbpBFI = new RLabelSub(Main.resourceManager.getString("ui.label.fire.bfi"));
		LblFbpBFI.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.bfi"));
        gbcSubLabelCol.gridy = 2;
        LblFbpBFI.setHorizontalAlignment(SwingConstants.RIGHT);
        panelFbpSecondary2.add(LblFbpBFI, gbcSubLabelCol);

		txtFbpBFI = new RTextField();
		txtFbpBFI.setEditable(false);
        gbcSubValueCol.gridy = 2;
        panelFbpSecondary2.add(txtFbpBFI, gbcSubValueCol);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpBfiUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kwm"));
		else
			lblFbpBfiUnit = new RLabel(Main.resourceManager.getString("ui.label.units.btufts"));
        gbcSubUomCol.gridy = 2;
        panelFbpSecondary2.add(lblFbpBfiUnit, gbcSubUomCol);

		//SpringUtilities.makeCompactGrid(panelFbpSecondary2, 3, 3, 5, 0, 5, 5, 10, 10);

	JPanel	panelFbpSecondary3 = new JPanel();
        panelFbpSecondary3.setBackground(new Color(245, 245, 245));
        panelFbpSecondary3.setLayout(new GridBagLayout());
        //panelFbpSecondary3.setBounds(330, 20, 141, 85);
		gbcSecondaryOutputs.gridx = 2;
		gbcSecondaryOutputs.gridy = 0;
		groupFbpSecondary.add(panelFbpSecondary3, gbcSecondaryOutputs);

		RLabel LblFbpDH = new RLabelSub(Main.resourceManager.getString("ui.label.fire.dh"));
		LblFbpDH.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.dh"));
        gbcSubLabelCol.gridy = 0;
        LblFbpDH.setHorizontalAlignment(SwingConstants.RIGHT);
        panelFbpSecondary3.add(LblFbpDH, gbcSubLabelCol);

		txtFbpDH = new RMapValueTextField();
        gbcSubValueCol.gridy = 0;
        panelFbpSecondary3.add(txtFbpDH, gbcSubValueCol);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpDHUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblFbpDHUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
        gbcSubUomCol.gridy = 0;
        panelFbpSecondary3.add(lblFbpDHUnit, gbcSubUomCol);
		//txtFbpDH.attachUnitLabel(lblFbpDHUnit);

		RLabel LblFbpDF = new RLabelSub(Main.resourceManager.getString("ui.label.fire.df"), Main.isFrench() ? 55 : 40);
		LblFbpDF.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.df"));
        gbcSubLabelCol.gridy = 1;
        LblFbpDF.setHorizontalAlignment(SwingConstants.RIGHT);
        panelFbpSecondary3.add(LblFbpDF,gbcSubLabelCol);

		txtFbpDF = new RMapValueTextField();
        gbcSubValueCol.gridy = 1;
        panelFbpSecondary3.add(txtFbpDF, gbcSubValueCol);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpDFUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblFbpDFUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
        gbcSubUomCol.gridy = 1;
        panelFbpSecondary3.add(lblFbpDFUnit,gbcSubUomCol);
		//txtFbpDF.attachUnitLabel(lblFbpDFUnit);

		RLabel LblFbpDB = new RLabelSub(Main.resourceManager.getString("ui.label.fire.db"));
		LblFbpDB.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.db"));
        gbcSubLabelCol.gridy = 2;
        LblFbpDB.setHorizontalAlignment(SwingConstants.RIGHT);
        panelFbpSecondary3.add(LblFbpDB,gbcSubLabelCol);

		txtFbpDB = new RMapValueTextField();
        gbcSubValueCol.gridy = 2;
        panelFbpSecondary3.add(txtFbpDB, gbcSubValueCol);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpDBUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblFbpDBUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
        gbcSubUomCol.gridy = 2;
        panelFbpSecondary3.add(lblFbpDBUnit,gbcSubUomCol);
		//txtFbpDB.attachUnitLabel(lblFbpDBUnit);



	JPanel	panelFbpSecondary4 = new JPanel();
        panelFbpSecondary4.setBackground(new Color(245, 245, 245));
        panelFbpSecondary4.setLayout(new GridBagLayout());
        //panelFbpSecondary4.setBounds(470, 20, 171, 85);
		gbcSecondaryOutputs.gridx = 3;
		gbcSecondaryOutputs.gridy = 0;
		groupFbpSecondary.add(panelFbpSecondary4, gbcSecondaryOutputs);

		RLabel LblFbpLB = new RLabelSub(Main.resourceManager.getString("ui.label.fire.lb"));
		LblFbpLB.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.lb"));
		gbcSubLabelCol.gridy = 0;
        LblFbpLB.setHorizontalAlignment(SwingConstants.RIGHT);
        panelFbpSecondary4.add(LblFbpLB, gbcSubLabelCol);

		txtFbpLB = new RTextField();
		txtFbpLB.setEditable(false);
		gbcSubValueCol.gridy = 0;
        panelFbpSecondary4.add(txtFbpLB, gbcSubValueCol);

		lbl = new JLabel("");
        gbcSubUomCol.gridy = 0;
        panelFbpSecondary4.add(lbl, gbcSubUomCol);

		RLabel LblFbpArea = new RLabelSub(Main.resourceManager.getString("ui.label.fire.area"));
		LblFbpArea.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.area"));
        gbcSubLabelCol.gridy = 1;
        LblFbpArea.setHorizontalAlignment(SwingConstants.RIGHT);
        panelFbpSecondary4.add(LblFbpArea,gbcSubLabelCol);

		txtFbpArea = new RTextField();
		txtFbpArea.setEditable(false);
        gbcSubValueCol.gridy = 1;
        panelFbpSecondary4.add(txtFbpArea, gbcSubValueCol);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpAreaUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ha"));
		else
			lblFbpAreaUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ac"));
        gbcSubUomCol.gridy = 1;
        panelFbpSecondary4.add(lblFbpAreaUnit, gbcSubUomCol);

		RLabel LblFbpPerimiter = new RLabel(Main.resourceManager.getString("ui.label.fire.perim"));
		LblFbpPerimiter.setToolTipText(Main.resourceManager.getString("ui.label.fire.desc.perim"));
        gbcSubLabelCol.gridy = 2;
        LblFbpPerimiter.setHorizontalAlignment(SwingConstants.RIGHT);
        panelFbpSecondary4.add(LblFbpPerimiter,gbcSubLabelCol);

		txtFbpPerimeter = new RTextField();
		txtFbpPerimeter.setEditable(false);
        gbcSubValueCol.gridy = 2;
        panelFbpSecondary4.add(txtFbpPerimeter, gbcSubValueCol);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpPerimiterUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblFbpPerimiterUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ch"));
        gbcSubUomCol.gridy = 2;
        panelFbpSecondary4.add(lblFbpPerimiterUnit, gbcSubUomCol);



		JPanel panelFbpFuelTypeInputs = new JPanel();
		panelFbpFuelTypeInputs.setBackground(new Color(245, 245, 245));
		panelFbpFuelTypeInputs.setBounds(10, 60, 271, 78);
		 gbcSecondary.gridx = 0; gbcSecondary.gridy				 =1;
		gbcSecondary.fill = GridBagConstraints.HORIZONTAL;
		panelFbpFuelType.add(panelFbpFuelTypeInputs, gbcSecondary);
		GridBagLayout gbl_panelFbpFuelTypeInputs = new GridBagLayout();
		gbl_panelFbpFuelTypeInputs.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelFbpFuelTypeInputs.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelFbpFuelTypeInputs.columnWeights = new double[] { 0.0, 1.0,
				0.0, Double.MIN_VALUE };
		gbl_panelFbpFuelTypeInputs.rowWeights = new double[] { 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelFbpFuelTypeInputs.setLayout(gbl_panelFbpFuelTypeInputs);

		btnFbpInformation = new JButton(Main.resourceManager.getString("ui.label.fbp.fuel.info"));
		btnFbpInformation.addActionListener((e) -> {
			fuelTypeInfo();
		});
		GridBagConstraints gbc_BtnFbpInformation = new GridBagConstraints();
		gbc_BtnFbpInformation.anchor = GridBagConstraints.WEST;
		gbc_BtnFbpInformation.insets = new Insets(0, 0, 5, 5);
		gbc_BtnFbpInformation.gridx = 0;
		gbc_BtnFbpInformation.gridy = 0;
		panelFbpFuelTypeInputs.add(btnFbpInformation, gbc_BtnFbpInformation);

		lblFbpGrassCuring = new RLabel(Main.resourceManager.getString("ui.label.fire.gcuring"));
		GridBagConstraints gbc_LblFbpGrassCuring = new GridBagConstraints();
		gbc_LblFbpGrassCuring.anchor = GridBagConstraints.WEST;
		gbc_LblFbpGrassCuring.insets = new Insets(0, 0, 5, 5);
		gbc_LblFbpGrassCuring.gridx = 0;
		gbc_LblFbpGrassCuring.gridy = 1;
		panelFbpFuelTypeInputs.add(lblFbpGrassCuring, gbc_LblFbpGrassCuring);

		txtFbpGrassCuring = new RTextField();
		txtFbpGrassCuring.setMinimumSize(new Dimension(20, 20));
		GridBagConstraints gbc_txtFbpGrassCuring = new GridBagConstraints();
		gbc_txtFbpGrassCuring.insets = new Insets(0, 0, 5, 5);
		gbc_txtFbpGrassCuring.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFbpGrassCuring.gridx = 1;
		gbc_txtFbpGrassCuring.gridy = 1;
		panelFbpFuelTypeInputs.add(txtFbpGrassCuring, gbc_txtFbpGrassCuring);
		txtFbpGrassCuring.setColumns(10);

		lblFbpGrassCuringUnit = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		GridBagConstraints gbc_LblFbpGrassCuringUnit = new GridBagConstraints();
		gbc_LblFbpGrassCuringUnit.anchor = GridBagConstraints.WEST;
		gbc_LblFbpGrassCuringUnit.insets = new Insets(0, 0, 5, 0);
		gbc_LblFbpGrassCuringUnit.gridx = 2;
		gbc_LblFbpGrassCuringUnit.gridy = 1;
		panelFbpFuelTypeInputs.add(lblFbpGrassCuringUnit,
				gbc_LblFbpGrassCuringUnit);

		lblFbpGrassFuelLoad = new RLabel(Main.resourceManager.getString("ui.label.fire.gload"));
		GridBagConstraints gbc_LblFbpGrassFuelLoad = new GridBagConstraints();
		gbc_LblFbpGrassFuelLoad.anchor = GridBagConstraints.WEST;
		gbc_LblFbpGrassFuelLoad.insets = new Insets(0, 0, 5, 5);
		gbc_LblFbpGrassFuelLoad.gridx = 0;
		gbc_LblFbpGrassFuelLoad.gridy = 2;
		panelFbpFuelTypeInputs
				.add(lblFbpGrassFuelLoad, gbc_LblFbpGrassFuelLoad);

		txtFbpGrassFuelLoad = new RTextField();
		txtFbpGrassFuelLoad.setMinimumSize(new Dimension(20, 20));
		GridBagConstraints gbc_txtFbpGrassFuelLoad = new GridBagConstraints();
		gbc_txtFbpGrassFuelLoad.insets = new Insets(0, 0, 5, 5);
		gbc_txtFbpGrassFuelLoad.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFbpGrassFuelLoad.gridx = 1;
		gbc_txtFbpGrassFuelLoad.gridy = 2;
		panelFbpFuelTypeInputs
				.add(txtFbpGrassFuelLoad, gbc_txtFbpGrassFuelLoad);
		txtFbpGrassFuelLoad.setColumns(10);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpGrassFuelLoadUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kgm2"));
		else
			lblFbpGrassFuelLoadUnit = new RLabel(Main.resourceManager.getString("ui.label.units.tonsperacre"));
		GridBagConstraints gbc_LblFbpGrassFuelLoadUnit = new GridBagConstraints();
		gbc_LblFbpGrassFuelLoadUnit.anchor = GridBagConstraints.WEST;
		gbc_LblFbpGrassFuelLoadUnit.insets = new Insets(0, 0, 5, 0);
		gbc_LblFbpGrassFuelLoadUnit.gridx = 2;
		gbc_LblFbpGrassFuelLoadUnit.gridy = 2;
		panelFbpFuelTypeInputs.add(lblFbpGrassFuelLoadUnit,
				gbc_LblFbpGrassFuelLoadUnit);

		lblFbpCrownBaseHeight = new RLabel(Main.resourceManager.getString("ui.label.fire.cbh"));
		GridBagConstraints gbc_LblFbpCrownBaseHeight = new GridBagConstraints();
		gbc_LblFbpCrownBaseHeight.anchor = GridBagConstraints.WEST;
		gbc_LblFbpCrownBaseHeight.insets = new Insets(0, 0, 5, 5);
		gbc_LblFbpCrownBaseHeight.gridx = 0;
		gbc_LblFbpCrownBaseHeight.gridy = 3;
		panelFbpFuelTypeInputs.add(lblFbpCrownBaseHeight,
				gbc_LblFbpCrownBaseHeight);

		txtFbpCrownBaseHeight = new RTextField();
		GridBagConstraints gbc_txtFbpCrownBaseHeight = new GridBagConstraints();
		gbc_txtFbpCrownBaseHeight.insets = new Insets(0, 0, 5, 5);
		gbc_txtFbpCrownBaseHeight.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFbpCrownBaseHeight.gridx = 1;
		gbc_txtFbpCrownBaseHeight.gridy = 3;
		panelFbpFuelTypeInputs.add(txtFbpCrownBaseHeight,
				gbc_txtFbpCrownBaseHeight);
		txtFbpCrownBaseHeight.setColumns(10);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblFbpCrownBaseHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblFbpCrownBaseHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
		GridBagConstraints gbc_LblFbpCrownBaseHeightUnit = new GridBagConstraints();
		gbc_LblFbpCrownBaseHeightUnit.anchor = GridBagConstraints.WEST;
		gbc_LblFbpCrownBaseHeightUnit.insets = new Insets(0, 0, 5, 0);
		gbc_LblFbpCrownBaseHeightUnit.gridx = 2;
		gbc_LblFbpCrownBaseHeightUnit.gridy = 3;
		panelFbpFuelTypeInputs.add(lblFbpCrownBaseHeightUnit,
				gbc_LblFbpCrownBaseHeightUnit);

		lblFbpPercentConifer = new RLabel(Main.resourceManager.getString("ui.label.fire.pc"));
		GridBagConstraints gbc_LblFbpPercentConifer = new GridBagConstraints();
		gbc_LblFbpPercentConifer.anchor = GridBagConstraints.WEST;
		gbc_LblFbpPercentConifer.insets = new Insets(0, 0, 5, 5);
		gbc_LblFbpPercentConifer.gridx = 0;
		gbc_LblFbpPercentConifer.gridy = 4;
		panelFbpFuelTypeInputs.add(lblFbpPercentConifer,
				gbc_LblFbpPercentConifer);

		txtFbpPercentConifer = new RTextField();
		txtFbpPercentConifer.setText("");
		GridBagConstraints gbc_txtFbpPercentConifer = new GridBagConstraints();
		gbc_txtFbpPercentConifer.insets = new Insets(0, 0, 5, 5);
		gbc_txtFbpPercentConifer.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFbpPercentConifer.gridx = 1;
		gbc_txtFbpPercentConifer.gridy = 4;
		panelFbpFuelTypeInputs.add(txtFbpPercentConifer,
				gbc_txtFbpPercentConifer);
		txtFbpPercentConifer.setColumns(10);

		lblFbpPercentConiferUnit = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		GridBagConstraints gbc_LblFbpPercentConiferUnit = new GridBagConstraints();
		gbc_LblFbpPercentConiferUnit.anchor = GridBagConstraints.WEST;
		gbc_LblFbpPercentConiferUnit.insets = new Insets(0, 0, 5, 0);
		gbc_LblFbpPercentConiferUnit.gridx = 2;
		gbc_LblFbpPercentConiferUnit.gridy = 4;
		panelFbpFuelTypeInputs.add(lblFbpPercentConiferUnit,
				gbc_LblFbpPercentConiferUnit);

		lblFbpPercentDeadFir = new RLabel(Main.resourceManager.getString("ui.label.fire.pdf"));
		GridBagConstraints gbc_LblFbpPercentDeadFir = new GridBagConstraints();
		gbc_LblFbpPercentDeadFir.anchor = GridBagConstraints.WEST;
		gbc_LblFbpPercentDeadFir.insets = new Insets(0, 0, 0, 5);
		gbc_LblFbpPercentDeadFir.gridx = 0;
		gbc_LblFbpPercentDeadFir.gridy = 5;
		panelFbpFuelTypeInputs.add(lblFbpPercentDeadFir,
				gbc_LblFbpPercentDeadFir);

		txtFbpPercentDeadFir = new RTextField();
		GridBagConstraints gbc_txtFbpPercentDeadFir = new GridBagConstraints();
		gbc_txtFbpPercentDeadFir.insets = new Insets(0, 0, 0, 5);
		gbc_txtFbpPercentDeadFir.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFbpPercentDeadFir.gridx = 1;
		gbc_txtFbpPercentDeadFir.gridy = 5;
		panelFbpFuelTypeInputs.add(txtFbpPercentDeadFir,
				gbc_txtFbpPercentDeadFir);
		txtFbpPercentDeadFir.setColumns(10);

		lblFbpPercentDeadFirUnit = new RLabel(Main.resourceManager.getString("ui.label.units.percent"));
		GridBagConstraints gbc_LblFbpPercentDeadFirUnit = new GridBagConstraints();
		gbc_LblFbpPercentDeadFirUnit.anchor = GridBagConstraints.WEST;
		gbc_LblFbpPercentDeadFirUnit.gridx = 2;
		gbc_LblFbpPercentDeadFirUnit.gridy = 5;
		panelFbpFuelTypeInputs.add(lblFbpPercentDeadFirUnit,
				gbc_LblFbpPercentDeadFirUnit);

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

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridheight = 1;
		gbc.gridwidth = 4;

		this.add(panel1, gbc);

		RButton BtnFbpCalculate = new RButton(Main.resourceManager.getString("ui.label.fbp.calculate"), RButton.Decoration.Calc);
		BtnFbpCalculate.addActionListener((e) -> {
			calculate();
		});
		panel1.add(BtnFbpCalculate);

		if (!Main.useMap()) {
			btnFbpExportMap = new RButton(Main.resourceManager.getString("ui.label.fbp.mapdata"));
			panel1.add(btnFbpExportMap);
		} else {
            btnFbpDisplayOnMap = new RButton(Main.resourceManager.getString("ui.label.fbp.map"));
            panel1.add(btnFbpDisplayOnMap);
        }
		btnFbpTransferToStats = new RButton(Main.resourceManager.getString("ui.label.fbp.tostats"), RButton.Decoration.Arrow);
		panel1.add(btnFbpTransferToStats);

		btnFbpExport = new RButton(Main.resourceManager.getString("ui.label.fbp.export"));
		panel1.add(btnFbpExport);

		btnReset = new RButton(Main.resourceManager.getString("ui.label.footer.reset"));
		btnReset.addActionListener((e) -> {
			reset();
		});
		panel1.add(btnReset);

		boolean useMapUnits = Main.prefs.getBoolean("fbp_useMapScaling", false);
		if (useMapUnits) {
			txtFbpDH.setConvertedUnits(MetricPrefix.getAtPosition(Main.prefs.getInt(
					"fbp_mapUnits", MetricPrefix.centi.getPositionInList())));
			txtFbpDH.setScale(Main.prefs.getInt("fbp_mapScale", 1));
			txtFbpDB.setConvertedUnits(MetricPrefix.getAtPosition(Main.prefs.getInt(
					"fbp_mapUnits", MetricPrefix.centi.getPositionInList())));
			txtFbpDB.setScale(Main.prefs.getInt("fbp_mapScale", 1));
			txtFbpDF.setConvertedUnits(MetricPrefix.getAtPosition(Main.prefs.getInt(
					"fbp_mapUnits", MetricPrefix.centi.getPositionInList())));
			txtFbpDF.setScale(Main.prefs.getInt("fbp_mapScale", 1));
		}
		txtFbpDH.setShowConverted(Main.prefs.getBoolean("fbp_dh_showConverted",
				false));
		txtFbpDB.setShowConverted(Main.prefs.getBoolean("fbp_db_showConverted",
				false));
		txtFbpDF.setShowConverted(Main.prefs.getBoolean("fbp_df_showConverted",
				false));


	}

	private void initTabOrder() {
		tabOrder.clear();
		tabOrder.add(comboFbpFuelType);
		tabOrder.add(btnFbpInformation);
		tabOrder.add(txtFbpGrassCuring);
		tabOrder.add(txtFbpGrassFuelLoad);
		tabOrder.add(txtFbpPercentConifer);
		tabOrder.add(txtFbpPercentDeadFir);
		tabOrder.add(txtFbpFFMC);
		tabOrder.add(txtFbpDMC);
		tabOrder.add(txtFbpDC);
		tabOrder.add(chckbxFbpUseBui);
		tabOrder.add(txtFbpBUI);
		tabOrder.add(txtFbpWindSpeed);
		tabOrder.add(txtFbpWindDirection);
		tabOrder.add(txtFbpElevation);
		tabOrder.add(chckbxFbpSlope);
		tabOrder.add(txtFbpSlope);
		tabOrder.add(txtFbpAspect);
		tabOrder.add(rdBtnFbpPoint);
		tabOrder.add(rdBtnFbpLine);
		tabOrder.add(spinnerFbpStartTime);
		tabOrder.add(txtFbpElapsedTime);
	}

	// }}

	@Override
	public void setInternetConnected(boolean conn) { }

	@Override
	public void onTimeZoneChanged() {
		clearOutputValuesOnForm();
	}

	@Override
	public void onDateChanged() {
		clearOutputValuesOnForm();
	}

	@Override
	public void onLocationChanged() {
		clearOutputValuesOnForm();
	}
	
	@Override
	public void settingsUpdated() {
		if (Main.unitSystem() == UnitSystem.METRIC) {
			String kgm2 = Main.resourceManager.getString("ui.label.units.kgm2");
			String fuelload = lblFbpGrassFuelLoadUnit.getText();
			if (!fuelload.equals(kgm2)) {
				String temp = txtFbpGrassFuelLoad.getText();
				if (temp.length() > 0) {
					Double val = ca.redapp.util.DoubleEx.valueOf(temp);
					val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(UnitSystem.METRIC), UnitSystem.fuelConsumpiton(UnitSystem.IMPERIAL));
					txtFbpGrassFuelLoad.setText(DecimalUtils.format(val, DataType.FORCE_2));
				}
				temp = txtFbpCrownBaseHeight.getText();
				if (temp.length() > 0) {
					Double val = ca.redapp.util.DoubleEx.valueOf(temp);
					val = Convert.convertUnit(val, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(UnitSystem.IMPERIAL));
					txtFbpCrownBaseHeight.setText(DecimalUtils.format(val, DataType.FORCE_2));
				}
				temp = txtFbpWindSpeed.getText();
				if (temp.length() > 0) {
					Double val = ca.redapp.util.DoubleEx.valueOf(temp);
					val = Convert.convertUnit(val, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(UnitSystem.IMPERIAL));
					txtFbpWindSpeed.setText(DecimalUtils.format(val, DataType.WIND_SPEED));
				}
			}
			lblFbpGrassFuelLoadUnit.setText(kgm2);
			lblFbpCrownBaseHeightUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblFbpWindSpeedUnit.setText(Main.resourceManager.getString("ui.label.units.kiloperhour"));
			lblFbpElevationUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblFbpWsvUnit.setText(Main.resourceManager.getString("ui.label.units.kiloperhour"));
			lblFbpROStUnit.setText(Main.resourceManager.getString("ui.label.units.mmin"));
			lblFbpROSeqUnit.setText(Main.resourceManager.getString("ui.label.units.mmin"));
			lblFbpSFCUnit.setText(Main.resourceManager.getString("ui.label.units.kgm2"));
			lblFbpCFCUnit.setText(Main.resourceManager.getString("ui.label.units.kgm2"));
			lblFbpTFCUnit.setText(Main.resourceManager.getString("ui.label.units.kgm2"));
			lblFbpRsoUnit.setText(Main.resourceManager.getString("ui.label.units.mmin"));
			lblFbpFrosUnit.setText(Main.resourceManager.getString("ui.label.units.mmin"));
			lblFbpBrosUnit.setText(Main.resourceManager.getString("ui.label.units.mmin"));
			lblFbpCsiUnit.setText(Main.resourceManager.getString("ui.label.units.kwm"));
			lblFbpFfiUnit.setText(Main.resourceManager.getString("ui.label.units.kwm"));
			lblFbpBfiUnit.setText(Main.resourceManager.getString("ui.label.units.kwm"));
			lblFbpHFIUnits.setText(Main.resourceManager.getString("ui.label.units.kwm"));
			lblFbpDHUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblFbpDFUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblFbpDBUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblFbpAreaUnit.setText(Main.resourceManager.getString("ui.label.units.ha"));
			lblFbpPerimiterUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
		}
		else {
			String tonsperacre = Main.resourceManager.getString("ui.label.units.tonsperacre");
			String fuelload = lblFbpGrassFuelLoadUnit.getText();
			if (!fuelload.equals(tonsperacre)) {
				String temp = txtFbpGrassFuelLoad.getText();
				if (temp.length() > 0) {
					Double val = ca.redapp.util.DoubleEx.valueOf(temp);
					val = Convert.convertUnit(val, UnitSystem.fuelConsumpiton(UnitSystem.IMPERIAL), UnitSystem.fuelConsumpiton(UnitSystem.METRIC));
					txtFbpGrassFuelLoad.setText(DecimalUtils.format(val, DataType.FORCE_2));
				}
				temp = txtFbpCrownBaseHeight.getText();
				if (temp.length() > 0) {
					Double val = ca.redapp.util.DoubleEx.valueOf(temp);
					val = Convert.convertUnit(val, UnitSystem.distanceMedium(UnitSystem.IMPERIAL), UnitSystem.distanceMedium(UnitSystem.METRIC));
					txtFbpCrownBaseHeight.setText(DecimalUtils.format(val, DataType.FORCE_2));
				}
				temp = txtFbpWindSpeed.getText();
				if (temp.length() > 0) {
					Double val = ca.redapp.util.DoubleEx.valueOf(temp);
					val = Convert.convertUnit(val, UnitSystem.speed(UnitSystem.IMPERIAL), UnitSystem.speed(UnitSystem.METRIC));
					txtFbpWindSpeed.setText(DecimalUtils.format(val, DataType.WIND_SPEED));
				}
			}
			lblFbpGrassFuelLoadUnit.setText(Main.resourceManager.getString("ui.label.units.tonsperacre"));
			lblFbpCrownBaseHeightUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblFbpWindSpeedUnit.setText(Main.resourceManager.getString("ui.label.units.milesperhour"));
			lblFbpElevationUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblFbpWsvUnit.setText(Main.resourceManager.getString("ui.label.units.milesperhour"));
			lblFbpROStUnit.setText(Main.resourceManager.getString("ui.label.units.chhr"));
			lblFbpROSeqUnit.setText(Main.resourceManager.getString("ui.label.units.chhr"));
			lblFbpSFCUnit.setText(Main.resourceManager.getString("ui.label.units.tonsperacre"));
			lblFbpCFCUnit.setText(Main.resourceManager.getString("ui.label.units.tonsperacre"));
			lblFbpTFCUnit.setText(Main.resourceManager.getString("ui.label.units.tonsperacre"));
			lblFbpRsoUnit.setText(Main.resourceManager.getString("ui.label.units.chhr"));
			lblFbpFrosUnit.setText(Main.resourceManager.getString("ui.label.units.chhr"));
			lblFbpBrosUnit.setText(Main.resourceManager.getString("ui.label.units.chhr"));
			lblFbpCsiUnit.setText(Main.resourceManager.getString("ui.label.units.btufts"));
			lblFbpFfiUnit.setText(Main.resourceManager.getString("ui.label.units.btufts"));
			lblFbpBfiUnit.setText(Main.resourceManager.getString("ui.label.units.btufts"));
			lblFbpHFIUnits.setText(Main.resourceManager.getString("ui.label.units.btufts"));
			lblFbpDHUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblFbpDFUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblFbpDBUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblFbpAreaUnit.setText(Main.resourceManager.getString("ui.label.units.ac"));
			lblFbpPerimiterUnit.setText(Main.resourceManager.getString("ui.label.units.ch"));
		}
		double val = Main.prefs.getDouble("fbp_elevation", fbpCalculations.elevation);
		val = Convert.convertUnit(val, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
		txtFbpElevation.setText(DecimalUtils.format(val, DataType.FORCE_1));
		val = Main.prefs.getDouble("fbp_slope", fbpCalculations.slopeValue);
		txtFbpSlope.setText(DecimalUtils.format(val, DataType.FORCE_1));
		val = Main.prefs.getDouble("fbp_aspect", fbpCalculations.aspect);
		txtFbpAspect.setText(DecimalUtils.format(val, DataType.FORCE_1));
		boolean b = Boolean.parseBoolean(Main.prefs.getString("fbp_useSlope", String.valueOf(fbpCalculations.useSlope)));
		chckbxFbpSlope.setSelected(b);
		clearOutputValuesOnForm();
	}

	@Override
	public void onCurrentTabChanged() { }

	private void notifyCalculated(boolean calcd) {
		for (FBPTabListener listener : listeners) {
			listener.calculated(calcd);
		}
	}

	public void addListener(FBPTabListener listener) {
		listeners.add(listener);
	}

	public interface FBPTabListener {
		public void calculated(boolean isCalcd);
	}

	@Override
	public void canTransferUpdated(boolean canTransfer) {
		btnFbpTransferToStats.setEnabled(canTransfer);
	}
	
	private static class PassedValues {
		public String ffmc;
		public String dmc;
		public String dc;
		public String bui;
	}
}
