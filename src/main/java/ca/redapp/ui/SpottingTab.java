/***********************************************************************
 * REDapp - SpottingTab.java
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
import static ca.redapp.util.LineEditHelper.getIntegerFromLineEdit;
import static ca.redapp.util.LineEditHelper.lineEditHandleError;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.albini.app.AlbiniCalculator;
import ca.albini.app.AlbiniCalculator.FireType;
import ca.albini.app.AlbiniCalculator.TerrainType;
import ca.albini.Sem.SpotAlgorithm.SpotSource;
import ca.albini.Sem.SpotAlgorithm.SpotSpecies;
import ca.hss.general.DecimalUtils;
import ca.hss.general.DecimalUtils.DataType;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RComboBox;
import ca.redapp.ui.component.RGroupBox;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;
import ca.redapp.ui.component.SpringUtilities;
import ca.redapp.util.LineEditHelper;
import ca.redapp.util.RPreferences;

public class SpottingTab extends REDappTab implements ActionListener, DocumentListener {
	private static final long serialVersionUID = 1L;
	private boolean initialized = false;

	private AlbiniCalculator calculator;

	public SpottingTab() {
		initialize();
		initTabOrder();
	}

	private void populateSpecies() {
		comboSpottingSpecies.removeAllItems();
		SpotSpecies species[] = SpotSpecies.values();
		for (SpotSpecies s : species) {
			comboSpottingSpecies.addItem(Main.resourceManager.getString(s.getResourceId()));
		}
		comboSpottingSpecies.setWide(true);
	}

	private void populateSources() {
		comboSpottingSpotSource.removeAllItems();
		SpotSource sources[] = SpotSource.values();
		for (SpotSource s : sources) {
			comboSpottingSpotSource.addItem(Main.resourceManager.getString(s.getResourceId()));
		}
	}

	private void burningPileClicked() {
	    CardLayout cl = (CardLayout)(cardsSpottingFireType.getLayout());
	    txtSpottingFireTypeFlameHeight.setEnabled(true);
	    txtSpottingFireTypeFlameLength.setEnabled(false);
	    comboSpottingSpecies.setEnabled(false);
	    txtSpottingDBH.setEnabled(false);
	    txtSpottingTreeHeight.setEnabled(false);
	    txtSpottingNumberTreeTorching.setEnabled(false);
	    cl.show(cardsSpottingFireType, "Burning Pile");
		calculator.setFireType(FireType.BurningPile);
	}

	private void surfaceFireClicked() {
	    CardLayout cl = (CardLayout)(cardsSpottingFireType.getLayout());
	    txtSpottingFireTypeFlameHeight.setEnabled(false);
	    txtSpottingFireTypeFlameLength.setEnabled(true);
	    comboSpottingSpecies.setEnabled(false);
	    txtSpottingDBH.setEnabled(false);
	    txtSpottingTreeHeight.setEnabled(false);
	    txtSpottingNumberTreeTorching.setEnabled(false);
	    cl.show(cardsSpottingFireType, "Surface Fire");
		calculator.setFireType(FireType.SurfaceFire);
	}

	private void torchingTreesClicked() {
	    CardLayout cl = (CardLayout)(cardsSpottingFireType.getLayout());
	    txtSpottingFireTypeFlameHeight.setEnabled(false);
	    txtSpottingFireTypeFlameLength.setEnabled(false);
	    comboSpottingSpecies.setEnabled(true);
	    txtSpottingDBH.setEnabled(true);
	    txtSpottingTreeHeight.setEnabled(true);
	    txtSpottingNumberTreeTorching.setEnabled(true);
	    cl.show(cardsSpottingFireType, "Torching Trees");
		calculator.setFireType(FireType.TorchingTrees);
	}

	private void toggleTerrainType(){
		boolean flag = rdbtnSpottingMountainousTerrain.isSelected();
		if(flag)
			calculator.setTerrainType(TerrainType.MountainousTerrain);
		else
			calculator.setTerrainType(TerrainType.FlatTerrain);

		LineEditHelper.setEnabled(lblSpottingSpotSource, comboSpottingSpotSource, null, flag);
		LineEditHelper.setEnabled(lblSpottingR2VDistance, txtSpottingR2VDistance, lblSpottingR2VDistanceUnit, flag);
		LineEditHelper.setEnabled(lblSpottingR2VElevationChange, txtSpottingR2VElevationChange, lblSpottingR2VElevationChangeUnit, flag);
	}

	private void calculate() {
		if (!getValuesOnForm()) {
			calculator.calculate();
			setValuesOnForm();
		}
		else {
			JOptionPane.showMessageDialog(null,
				    Main.resourceManager.getString("ui.label.spotting.error"),
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	private void setValuesOnForm() {
		double d = Convert.convertUnit(calculator.getOutputFlameHeight(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
		txtSpottingFlameHeight.setText(DecimalUtils.format(d));
		d = Convert.convertUnit(calculator.getOutputCriticalCoverHeight(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
		txtSpottingCriticalCoverHeight.setText(DecimalUtils.format(d));
		d = Convert.convertUnit(calculator.getOutputFireBrandHeight(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
		txtSpottingFirebrandHeight.setText(DecimalUtils.format(d));
		d = Convert.convertUnit(calculator.getOutputSpotfireDistance(), UnitSystem.distanceLarge(Main.unitSystem()), UnitSystem.distanceLarge(UnitSystem.METRIC));
		txtSpottingSpotfireDistance.setText(DecimalUtils.format(d));
		if (calculator.getFireType() == FireType.TorchingTrees) {
			txtSpottingFlameDuration.setText(DecimalUtils.format(calculator.getOutputFlameDuration()));
			lblSpottingFireTypeFlameHeight.setText(Main.resourceManager.getString("ui.label.spotting.fire.fheight"));
		}
		else {
			if (calculator.getFireType() == FireType.SurfaceFire) {
				lblSpottingFireTypeFlameHeight.setText(Main.resourceManager.getString("ui.label.spotting.fire.flength"));
			}
			else {
				lblSpottingFireTypeFlameHeight.setText(Main.resourceManager.getString("ui.label.spotting.fire.fheight"));
			}
			txtSpottingFlameDuration.setText("");
		}
	}

	private boolean getValuesOnForm() {
		Double d;
		boolean error = false;

		d = getDoubleFromLineEdit(txtSpottingWindSpeed);
		if (d == null)
			error = true;
		else {
			d = Convert.convertUnit(d, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
			if (cmbWindHeight.getSelectedIndex() == 0)
				calculator.set10MWindSpeed(d);
			else
				calculator.setWindSpeed(d);
			if (d < calculator.minWindSpeed() || d > calculator.maxWindSpeed()) {
				error = true;
				StringBuilder builder = new StringBuilder();
				builder.append(Main.resourceManager.getString("ui.label.spotting.error.ws"));
				builder.append(" (");
				builder.append(Convert.convertUnit(calculator.minWindSpeed(), UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC)));
				builder.append(" to ");
				builder.append(Convert.convertUnit(calculator.maxWindSpeed(), UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC)));
				builder.append(" ");
				if (Main.unitSystem() == UnitSystem.METRIC)
					builder.append(Main.resourceManager.getString("ui.label.units.kiloperhour"));
				else
					builder.append(Main.resourceManager.getString("ui.label.units.milesperhour"));
				builder.append(").");
				lineEditHandleError(txtSpottingWindSpeed, builder.toString());
			}
		}

		d = getDoubleFromLineEdit(txtSpottingDownwindCoverHeight);
		if (d == null)
			error = true;
		else {
			d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
			calculator.setDownwindCoverHeight(d);
			if (d < calculator.minCoverHeight() || d > calculator.maxCoverHeight()) {
				error = true;
				StringBuilder builder = new StringBuilder();
				builder.append(Main.resourceManager.getString("ui.label.spotting.error.dwch"));
				builder.append(" (");
				builder.append(Convert.convertUnit(calculator.minCoverHeight(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)));
				builder.append(" to ");
				builder.append(Convert.convertUnit(calculator.maxCoverHeight(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)));
				builder.append(" ");
				if (Main.unitSystem() == UnitSystem.METRIC)
					builder.append(Main.resourceManager.getString("ui.label.units.m"));
				else
					builder.append(Main.resourceManager.getString("ui.label.units.ft"));
				builder.append(").");
				lineEditHandleError(txtSpottingDownwindCoverHeight, builder.toString());
			}
		}

		if (rdbtnSpottingBurningPile.isSelected()) {
			d = getDoubleFromLineEdit(txtSpottingFireTypeFlameHeight);
			if (d == null)
				error = true;
			else {
				d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
				calculator.setFlameHeight(d);
				if (d < calculator.minFlameHeight() || d > calculator.maxFlameHeight()) {
					error = true;
					StringBuilder builder = new StringBuilder();
					builder.append(Main.resourceManager.getString("ui.label.spotting.error.fheight"));
					builder.append(" (");
					builder.append(Convert.convertUnit(calculator.minFlameHeight(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)));
					builder.append(" to ");
					builder.append(Convert.convertUnit(calculator.maxFlameHeight(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)));
					builder.append(" ");
					if (Main.unitSystem() == UnitSystem.METRIC)
						builder.append(Main.resourceManager.getString("ui.label.units.m"));
					else
						builder.append(Main.resourceManager.getString("ui.label.units.ft"));
					builder.append(")");
					lineEditHandleError(txtSpottingFireTypeFlameHeight, builder.toString());
				}
			}
		}
		else if (rdbtnSpottingSurfaceFire.isSelected()) {
			d = getDoubleFromLineEdit(txtSpottingFireTypeFlameLength);
			if (d == null)
				error = true;
			else {
				d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
				calculator.setFlameLength(d);
				if (d < calculator.minFlameLength() || d > calculator.maxFlameLength()) {
					error = true;
					StringBuilder builder = new StringBuilder();
					builder.append(Main.resourceManager.getString("ui.label.spotting.error.flength"));
					builder.append(" (");
					builder.append(Convert.convertUnit(calculator.minFlameLength(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)));
					builder.append(" to ");
					builder.append(Convert.convertUnit(calculator.maxFlameLength(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)));
					builder.append(" ");
					if (Main.unitSystem() == UnitSystem.METRIC)
						builder.append(Main.resourceManager.getString("ui.label.units.m"));
					else
						builder.append(Main.resourceManager.getString("ui.label.units.ft"));
					builder.append(")");
					lineEditHandleError(txtSpottingFireTypeFlameLength, builder.toString());
				}
			}
		}
		else if (rdbtnSpottingTorchingTrees.isSelected()) {
			int species = comboSpottingSpecies.getSelectedIndex();
			calculator.setSpotSpecies(SpotSpecies.fromInt(species));

			d = getDoubleFromLineEdit(txtSpottingDBH);
			if (d == null)
				error = true;
			else {
				d = Convert.convertUnit(d, UnitSystem.distanceSmall2(UnitSystem.METRIC), UnitSystem.distanceSmall2(Main.unitSystem()));
				calculator.setDBH(d);
				if (d < calculator.minDBH() || d > calculator.maxDBH()) {
					error = true;
					StringBuilder builder = new StringBuilder();
					builder.append(Main.resourceManager.getString("ui.label.spotting.error.dbh"));
					builder.append(" (");
					builder.append(Convert.convertUnit(calculator.minDBH(), UnitSystem.distanceSmall2(Main.unitSystem()), UnitSystem.distanceSmall2(UnitSystem.METRIC)));
					builder.append(" to ");
					builder.append(Convert.convertUnit(calculator.maxDBH(), UnitSystem.distanceSmall2(Main.unitSystem()), UnitSystem.distanceSmall2(UnitSystem.METRIC)));
					builder.append(" ");
					if (Main.unitSystem() == UnitSystem.METRIC)
						builder.append(Main.resourceManager.getString("ui.label.units.cm"));
					else
						builder.append(Main.resourceManager.getString("ui.label.units.in"));
					builder.append(")");
					lineEditHandleError(txtSpottingDBH, builder.toString());
				}
			}

			d = getDoubleFromLineEdit(txtSpottingTreeHeight);
			if (d == null)
				error = true;
			else {
				d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
				calculator.setTreeHeight(d);
				if (d < calculator.minTreeHeight() || d > calculator.maxTreeHeight()) {
					error = true;
					StringBuilder builder = new StringBuilder();
					builder.append(Main.resourceManager.getString("ui.label.spotting.error.theight"));
					builder.append(" (");
					builder.append(Convert.convertUnit(calculator.minTreeHeight(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)));
					builder.append(" to ");
					builder.append(Convert.convertUnit(calculator.maxTreeHeight(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)));
					builder.append(" ");
					if (Main.unitSystem() == UnitSystem.METRIC)
						builder.append(Main.resourceManager.getString("ui.label.units.m"));
					else
						builder.append(Main.resourceManager.getString("ui.label.units.ft"));
					builder.append(")");
					lineEditHandleError(txtSpottingTreeHeight, builder.toString());
				}
			}

			Integer i = getIntegerFromLineEdit(txtSpottingNumberTreeTorching);
			if (d == null)
				error = true;
			else {
				calculator.setNumberOfTorchingTrees(i);
				if (d < calculator.minNumberOfTorchingTrees() || d > calculator.maxNumberOfTorchingTrees()) {
					error = true;
					lineEditHandleError(txtSpottingNumberTreeTorching,
							Main.resourceManager.getString("ui.label.spotting.error.count") + " (" + calculator.minNumberOfTorchingTrees() + " to " + calculator.maxNumberOfTorchingTrees() + ").");
				}
			}
		}

		if (rdbtnSpottingMountainousTerrain.isSelected()) {
			int source = comboSpottingSpotSource.getSelectedIndex();
			calculator.setSpotSource(SpotSource.fromInt(source));

			d = getDoubleFromLineEdit(txtSpottingR2VDistance);
			if (d == null)
				error = true;
			else {
				d = Convert.convertUnit(d, UnitSystem.distanceLarge(UnitSystem.METRIC), UnitSystem.distanceLarge(Main.unitSystem()));
				calculator.setRidgetopToValleyDistance(d);
				if (d < calculator.minRidgetopToValleyDistance() || d > calculator.maxRidgetopToValleyDistance()) {
					error = true;
					StringBuilder builder = new StringBuilder();
					builder.append(Main.resourceManager.getString("ui.label.spotting.error.r2vdist"));
					builder.append(" (");
					builder.append(Convert.convertUnit(calculator.minRidgetopToValleyDistance(), UnitSystem.distanceLarge(Main.unitSystem()), UnitSystem.distanceLarge(UnitSystem.METRIC)));
					builder.append(" to ");
					builder.append(Convert.convertUnit(calculator.maxRidgetopToValleyDistance(), UnitSystem.distanceLarge(Main.unitSystem()), UnitSystem.distanceLarge(UnitSystem.METRIC)));
					builder.append(" ");
					if (Main.unitSystem() == UnitSystem.METRIC)
						builder.append(Main.resourceManager.getString("ui.label.units.km"));
					else
						builder.append(Main.resourceManager.getString("ui.label.units.mi"));
					builder.append(")");
					lineEditHandleError(txtSpottingR2VDistance, builder.toString());
				}
			}

			d = getDoubleFromLineEdit(txtSpottingR2VElevationChange);
			if (d == null)
				error = true;
			else {
				d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()));
				calculator.setRidgetopToValleyElevationChange(d);
				if (d < calculator.minRidgetopToValleyElevationChange() || d > calculator.maxRidgetopToValleyElevationChange()) {
					error = true;
					StringBuilder builder = new StringBuilder();
					builder.append(Main.resourceManager.getString("ui.label.spotting.error.r2velev"));
					builder.append(" (");
					builder.append(Convert.convertUnit(calculator.minRidgetopToValleyDistance(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)));
					builder.append(" to ");
					builder.append(Convert.convertUnit(calculator.maxRidgetopToValleyDistance(), UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC)));
					builder.append(" ");
					if (Main.unitSystem() == UnitSystem.METRIC)
						builder.append(Main.resourceManager.getString("ui.label.units.m"));
					else
						builder.append(Main.resourceManager.getString("ui.label.units.ft"));
					builder.append(")");
					lineEditHandleError(txtSpottingR2VElevationChange, builder.toString());
				}
			}
		}

		return error;
	}

	public void loadAllValues(RPreferences prefs) {
		double d = prefs.getDouble("sc_windSpeed", calculator.getWindSpeed());
		d = Convert.convertUnit(d, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
		txtSpottingWindSpeed.setText(DecimalUtils.format(d, DecimalUtils.DataType.WIND_SPEED));
		d = prefs.getDouble("sc_downwindCoverHeight", calculator.getDownwindCoverHeight());
		d = Convert.convertUnit(d, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
		txtSpottingDownwindCoverHeight.setText(DecimalUtils.format(d, DecimalUtils.DataType.FORCE_ATMOST_2));
		d = prefs.getDouble("sc_flameHeight", calculator.getFlameHeight());
		d = Convert.convertUnit(d, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
		txtSpottingFireTypeFlameHeight.setText(DecimalUtils.format(d, DecimalUtils.DataType.FORCE_ATMOST_2));
		d = prefs.getDouble("sc_flameLength", calculator.getFlameLength());
		d = Convert.convertUnit(d, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
		txtSpottingFireTypeFlameLength.setText(DecimalUtils.format(d, DecimalUtils.DataType.FORCE_ATMOST_2));
		d = prefs.getDouble("sc_dbh", calculator.getDBH());
		d = Convert.convertUnit(d, UnitSystem.distanceSmall2(Main.unitSystem()), UnitSystem.distanceSmall2(UnitSystem.METRIC));
		txtSpottingDBH.setText(DecimalUtils.format(d, DecimalUtils.DataType.FORCE_ATMOST_2));
		d = prefs.getDouble("sc_height", calculator.getTreeHeight());
		d = Convert.convertUnit(d, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
		txtSpottingTreeHeight.setText(DecimalUtils.format(d, DecimalUtils.DataType.FORCE_ATMOST_2));
		txtSpottingNumberTreeTorching.setText(DecimalUtils.format(prefs.getDouble("sc_numberTreeTorching", calculator.getNumberOfTorchingTrees()), DecimalUtils.DataType.FORCE_ATMOST_2));
		d = prefs.getDouble("sc_r2vDstance", calculator.getRidgetopToValleyDistance());
		d = Convert.convertUnit(d, UnitSystem.distanceLarge(Main.unitSystem()), UnitSystem.distanceLarge(UnitSystem.METRIC));
		txtSpottingR2VDistance.setText(DecimalUtils.format(d, DecimalUtils.DataType.FORCE_ATMOST_2));
		d = prefs.getDouble("sc_r2vElevationChange", calculator.getRidgetopToValleyElevationChange());
		d = Convert.convertUnit(d, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
		txtSpottingR2VElevationChange.setText(DecimalUtils.format(d, DecimalUtils.DataType.FORCE_ATMOST_2));
		String fireType = prefs.getString("sc_fireType", FireType.BurningPile.name());
		if (fireType.equalsIgnoreCase(FireType.BurningPile.name())) {
			rdbtnSpottingBurningPile.setSelected(true);
			burningPileClicked();
		}
		else if (fireType.equalsIgnoreCase(FireType.SurfaceFire.name())) {
			rdbtnSpottingSurfaceFire.setSelected(true);
			surfaceFireClicked();
		}
		else {
			rdbtnSpottingTorchingTrees.setSelected(true);
			torchingTreesClicked();
		}
		String terrainType = prefs.getString("sc_terrainType", TerrainType.FlatTerrain.name());
		if (terrainType.equalsIgnoreCase(TerrainType.FlatTerrain.name())) {
			rdbtnSpottingFlatTerrain.setSelected(true);
			toggleTerrainType();
		}
		else {
			rdbtnSpottingMountainousTerrain.setSelected(true);
			toggleTerrainType();
		}
		comboSpottingSpecies.setSelectedIndex(Integer.parseInt(prefs.getString("sc_species", "0")));
		comboSpottingSpotSource.setSelectedIndex(Integer.parseInt(prefs.getString("sc_spotSource", "0")));
		cmbWindHeight.setSelectedIndex(prefs.getBoolean("sc_10mwind", Main.unitSystem() == UnitSystem.METRIC) ? 0 : 1);
	}

	public void saveAllValues(RPreferences prefs) {
		Double d;

		d = getDoubleFromLineEdit(txtSpottingWindSpeed);
		if (d != null)
			prefs.putString("sc_windSpeed", String.valueOf(Convert.convertUnit(d, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()))));

		d = getDoubleFromLineEdit(txtSpottingDownwindCoverHeight);
		if (d != null)
			prefs.putString("sc_downwindCoverHeight", String.valueOf(Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()))));

		d = getDoubleFromLineEdit(txtSpottingFireTypeFlameHeight);
		if (d != null)
			prefs.putString("sc_flameHeight", String.valueOf(Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()))));

		d = getDoubleFromLineEdit(txtSpottingFireTypeFlameLength);
		if (d != null)
			prefs.putString("sc_flameLength", String.valueOf(Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()))));

		prefs.putString("sc_species", String.valueOf(comboSpottingSpecies.getSelectedIndex()));

		d = getDoubleFromLineEdit(txtSpottingDBH);
		if (d != null)
			prefs.putString("sc_dbh", String.valueOf(Convert.convertUnit(d, UnitSystem.distanceSmall2(UnitSystem.METRIC), UnitSystem.distanceSmall2(Main.unitSystem()))));

		d = getDoubleFromLineEdit(txtSpottingTreeHeight);
		if (d != null)
			prefs.putString("sc_height", String.valueOf(Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()))));

		Integer i = getIntegerFromLineEdit(txtSpottingNumberTreeTorching);
		if (d != null)
			prefs.putString("sc_numberTreeTorching", String.valueOf(i));

		prefs.putString("sc_spotSource", String.valueOf(comboSpottingSpotSource.getSelectedIndex()));

		d = getDoubleFromLineEdit(txtSpottingR2VDistance);
		if (d != null)
			prefs.putString("sc_r2vDstance", String.valueOf(Convert.convertUnit(i, UnitSystem.distanceLarge(UnitSystem.METRIC), UnitSystem.distanceLarge(Main.unitSystem()))));

		d = getDoubleFromLineEdit(txtSpottingR2VElevationChange);
		if (d != null)
			prefs.putString("sc_r2vElevationChange", String.valueOf(Convert.convertUnit(i, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(Main.unitSystem()))));

		prefs.putString("sc_fireType", calculator.getFireType().name());
		prefs.putString("sc_terrainType", calculator.getTerrainType().name());
		prefs.putBoolean("sc_10mwind", cmbWindHeight.getSelectedIndex() == 0 ? true : false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		clearOutputValuesOnForm();
		Object obj = e.getSource();
		if(obj instanceof JRadioButton){
			JRadioButton src = (JRadioButton) obj;
			if(src == rdbtnSpottingBurningPile)
				burningPileClicked();
			else if(src == rdbtnSpottingSurfaceFire)
				surfaceFireClicked();
			else if(src == rdbtnSpottingTorchingTrees)
				torchingTreesClicked();
			else
				toggleTerrainType();
		}
		else if(obj instanceof JButton)
			if(obj == btnSpottingCalculate)
				calculate();
	}

	private void clearOutputValuesOnForm() {
		txtSpottingFlameHeight.setText("");
		txtSpottingCriticalCoverHeight.setText("");
		txtSpottingFirebrandHeight.setText("");
		txtSpottingSpotfireDistance.setText("");
		txtSpottingFlameDuration.setText("");
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

	/**
	 * Set the text in the wind speed textbox.
	 * @param speed
	 */
	public void setWindSpeed(String speed) {
		txtSpottingWindSpeed.setText(speed);
	}

	/**
	 * Set the terrain type to mountainous.
	 */
	public void makeMountainousTerrain() {
		rdbtnSpottingMountainousTerrain.doClick();
	}

	/**
	 * Set the terrain type to flat.
	 */
	public void makeFlatTerrain() {
		rdbtnSpottingFlatTerrain.doClick();
	}

	/**
	 * Set the fire type to burning pile.
	 */
	public void makeBurningPileFire() {
		rdbtnSpottingBurningPile.doClick();
	}

	/**
	 * Set the fire type to surface fire.
	 */
	public void makeSurfaceFire() {
		rdbtnSpottingSurfaceFire.doClick();
	}

	/**
	 * Set the fire type to torching trees.
	 */
	public void makeTorchingTreesFire() {
		rdbtnSpottingTorchingTrees.doClick();
	}

	// {{ UI Stuff

	private RTextField txtSpottingDownwindCoverHeight;
	private RTextField txtSpottingWindSpeed;
	private RTextField txtSpottingR2VDistance;
	private RTextField txtSpottingR2VElevationChange;
	private RTextField txtSpottingFireTypeFlameHeight;
	private RTextField txtSpottingFireTypeFlameLength;
	private RTextField txtSpottingDBH;
	private RTextField txtSpottingTreeHeight;
	private RTextField txtSpottingNumberTreeTorching;
	private RTextField txtSpottingCriticalCoverHeight;
	private RTextField txtSpottingFlameHeight;
	private RTextField txtSpottingFirebrandHeight;
	private RTextField txtSpottingSpotfireDistance;
	private RTextField txtSpottingFlameDuration;
	private JPanel cardSpottingSurfaceFire;
	private JPanel cardsSpottingFireType;
	private JRadioButton rdbtnSpottingMountainousTerrain;
	private JRadioButton rdbtnSpottingFlatTerrain;
	private JRadioButton rdbtnSpottingBurningPile;
	private JRadioButton rdbtnSpottingSurfaceFire;
	private JRadioButton rdbtnSpottingTorchingTrees;
	private JComboBox<String> comboSpottingSpotSource;
	private RComboBox<String> comboSpottingSpecies;
	private JComboBox<String> cmbWindHeight;
	private RLabel lblSpottingSpotSource;
	private RLabel lblSpottingR2VDistance;
	private RLabel lblSpottingR2VElevationChange;
	private RLabel lblSpottingFireTypeFlameHeight;
	private RLabel lblSpottingR2VDistanceUnit;
	private RLabel lblSpottingR2VElevationChangeUnit;
	private RLabel lblSpottingWindSpeedUnit;
	private RLabel lblSpottingDownwindCoverHeightUnit;
	private RLabel lblSpottingFireTypeFlameHeightUnit;
	private RLabel lblSpottingFireTypeFlameLengthUnit;
	private RLabel lblSpottingDbhUnit;
	private RLabel lblSpottingTreeHeightUnit;
	private RLabel lblSpottingFlameHeightUnit;
	private RLabel lblSpottingCriticalCoverHeightUnit;
	private RLabel lblSpottingFirebrandHeightUnit;
	private RLabel lblSpottingSpotfireDistanceUnit;
	private RButton btnSpottingCalculate;
	private RButton btnReset;

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

		RGroupBox groupSpottingTerrain = new RGroupBox();
		groupSpottingTerrain.setText(Main.resourceManager.getString("ui.label.spotting.terrain.title"));
		groupSpottingTerrain.setBounds(5, 130, 351, 171);
		add(groupSpottingTerrain);

		JPanel panelSpottingTerrain = new JPanel();
		panelSpottingTerrain.setBackground(new Color(245, 245, 245));
		panelSpottingTerrain.setLayout(new SpringLayout());
		panelSpottingTerrain.setBounds(10, 15, 331, 151);
		groupSpottingTerrain.add(panelSpottingTerrain);

		ButtonGroup btnGroupSpottingTerrainType = new ButtonGroup();

		rdbtnSpottingFlatTerrain = new JRadioButton(Main.resourceManager.getString("ui.label.spotting.terrain.flat")) {
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
			rdbtnSpottingFlatTerrain.setFont(rdbtnSpottingFlatTerrain.getFont().deriveFont(12.0f));
		rdbtnSpottingFlatTerrain.setBackground(new Color(245, 245, 245));
		btnGroupSpottingTerrainType.add(rdbtnSpottingFlatTerrain);
		panelSpottingTerrain.add(rdbtnSpottingFlatTerrain);

		JLabel lbl = new JLabel("");
		panelSpottingTerrain.add(lbl);

		lbl = new JLabel("");
		panelSpottingTerrain.add(lbl);

		rdbtnSpottingMountainousTerrain = new JRadioButton(Main.resourceManager.getString("ui.label.spotting.terrain.mountain")) {
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
			rdbtnSpottingMountainousTerrain.setFont(rdbtnSpottingMountainousTerrain.getFont().deriveFont(12.0f));
		rdbtnSpottingMountainousTerrain.setBackground(new Color(245, 245, 245));
		btnGroupSpottingTerrainType.add(rdbtnSpottingMountainousTerrain);
		panelSpottingTerrain.add(rdbtnSpottingMountainousTerrain);

		lbl = new JLabel("");
		panelSpottingTerrain.add(lbl);

		lbl = new JLabel("");
		panelSpottingTerrain.add(lbl);

		lblSpottingSpotSource = new RLabel(Main.resourceManager.getString("ui.label.spotting.terrain.source"));
		panelSpottingTerrain.add(lblSpottingSpotSource);

		comboSpottingSpotSource = new RComboBox<String>();
		panelSpottingTerrain.add(comboSpottingSpotSource);

		lbl = new JLabel("");
		panelSpottingTerrain.add(lbl);

		lblSpottingR2VDistance = new RLabel(Main.resourceManager.getString("ui.label.spotting.terrain.r2vdist"));
		panelSpottingTerrain.add(lblSpottingR2VDistance);

		txtSpottingR2VDistance = new RTextField();
		panelSpottingTerrain.add(txtSpottingR2VDistance);
		txtSpottingR2VDistance.setColumns(10);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblSpottingR2VDistanceUnit = new RLabel(Main.resourceManager.getString("ui.label.units.km"));
		else
			lblSpottingR2VDistanceUnit = new RLabel(Main.resourceManager.getString("ui.label.units.mi"));
		panelSpottingTerrain.add(lblSpottingR2VDistanceUnit);

		lblSpottingR2VElevationChange = new RLabel(Main.resourceManager.getString("ui.label.spotting.terrain.r2velev"));
		panelSpottingTerrain.add(lblSpottingR2VElevationChange);

		txtSpottingR2VElevationChange = new RTextField();
		txtSpottingR2VElevationChange.setColumns(10);
		panelSpottingTerrain.add(txtSpottingR2VElevationChange);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblSpottingR2VElevationChangeUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblSpottingR2VElevationChangeUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
		panelSpottingTerrain.add(lblSpottingR2VElevationChangeUnit);

		SpringUtilities.makeCompactGrid(panelSpottingTerrain, 5, 3, 5, 5, 5, 5, 10, 10);

		RGroupBox groupSpottingGeneral = new RGroupBox();
		groupSpottingGeneral.setText(Main.resourceManager.getString("ui.label.spotting.general.title"));
		groupSpottingGeneral.setBounds(5, 10, 351, 110);
		add(groupSpottingGeneral);

		JPanel panelSpottingGeneral = new JPanel();
		panelSpottingGeneral.setBackground(new Color(245, 245, 245));
		panelSpottingGeneral.setLayout(new SpringLayout());
		panelSpottingGeneral.setBounds(10, 15, 331, 90);
		groupSpottingGeneral.add(panelSpottingGeneral);

		RLabel lblSpottingWindSpeed = new RLabel(Main.resourceManager.getString("ui.label.spotting.general.ws"));
		panelSpottingGeneral.add(lblSpottingWindSpeed);

		txtSpottingWindSpeed = new RTextField();
		txtSpottingWindSpeed.setColumns(10);
		panelSpottingGeneral.add(txtSpottingWindSpeed);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblSpottingWindSpeedUnit = new RLabel(Main.resourceManager.getString("ui.label.units.kiloperhour"));
		else
			lblSpottingWindSpeedUnit = new RLabel(Main.resourceManager.getString("ui.label.units.milesperhour"));
		panelSpottingGeneral.add(lblSpottingWindSpeedUnit);

		RLabel lblmWindSpeed = new RLabel();
		lblmWindSpeed.setText(Main.resourceManager.getString("ui.label.spotting.general.wsh"));
		panelSpottingGeneral.add(lblmWindSpeed);

		cmbWindHeight = new RComboBox<String>();
		cmbWindHeight.setModel(new DefaultComboBoxModel<String>(new String[] {"10m", "20ft"}));
		//cmbWindHeight.setBounds(200, 49, 81, 22);
		cmbWindHeight.addActionListener((e) -> clearOutputValuesOnForm());
		panelSpottingGeneral.add(cmbWindHeight);

		lbl = new JLabel("");
		panelSpottingGeneral.add(lbl);

		RLabel lblSpottingDownwindCoverHeight = new RLabel(Main.resourceManager.getString("ui.label.spotting.general.dwch"));
		panelSpottingGeneral.add(lblSpottingDownwindCoverHeight);

		txtSpottingDownwindCoverHeight = new RTextField();
		panelSpottingGeneral.add(txtSpottingDownwindCoverHeight);
		txtSpottingDownwindCoverHeight.setColumns(10);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblSpottingDownwindCoverHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblSpottingDownwindCoverHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
		panelSpottingGeneral.add(lblSpottingDownwindCoverHeightUnit);

		SpringUtilities.makeCompactGrid(panelSpottingGeneral, 3, 3, 5, 5, 5, 5, 10, 10);

		RGroupBox panelSpottingFireType = new RGroupBox();
		panelSpottingFireType.setText(Main.resourceManager.getString("ui.label.spotting.fire.title"));
		panelSpottingFireType.setBounds(365, 10, 301, 291);
		add(panelSpottingFireType);

		ButtonGroup btnGroupSpottingFireType = new ButtonGroup();

		rdbtnSpottingBurningPile = new JRadioButton(Main.resourceManager.getString("ui.label.spotting.fire.pile"));
		if (Main.isLinux())
			rdbtnSpottingBurningPile.setFont(rdbtnSpottingBurningPile.getFont().deriveFont(12.0f));
		rdbtnSpottingBurningPile.setBackground(new Color(245, 245, 245));
		btnGroupSpottingFireType.add(rdbtnSpottingBurningPile);
		rdbtnSpottingBurningPile.setBounds(10, 30, 281, 20);
		panelSpottingFireType.add(rdbtnSpottingBurningPile);

		rdbtnSpottingSurfaceFire = new JRadioButton(Main.resourceManager.getString("ui.label.spotting.fire.surface"));
		if (Main.isLinux())
			rdbtnSpottingSurfaceFire.setFont(rdbtnSpottingSurfaceFire.getFont().deriveFont(12.0f));
		rdbtnSpottingSurfaceFire.setBackground(new Color(245, 245, 245));
		btnGroupSpottingFireType.add(rdbtnSpottingSurfaceFire);
		rdbtnSpottingSurfaceFire.setBounds(10, 60, 281, 20);
		panelSpottingFireType.add(rdbtnSpottingSurfaceFire);

		rdbtnSpottingTorchingTrees = new JRadioButton(Main.resourceManager.getString("ui.label.spotting.fire.trees"));
		if (Main.isLinux())
			rdbtnSpottingTorchingTrees.setFont(rdbtnSpottingTorchingTrees.getFont().deriveFont(12.0f));
		rdbtnSpottingTorchingTrees.setBackground(new Color(245, 245, 245));
		btnGroupSpottingFireType.add(rdbtnSpottingTorchingTrees);
		rdbtnSpottingTorchingTrees.setBounds(10, 90, 281, 20);
		panelSpottingFireType.add(rdbtnSpottingTorchingTrees);

		cardsSpottingFireType = new JPanel();
		cardsSpottingFireType.setBackground(new Color(245, 245, 245));
		cardsSpottingFireType.setBounds(7, 110, 291, 141);
		panelSpottingFireType.add(cardsSpottingFireType);
		cardsSpottingFireType.setLayout(new CardLayout(0, 0));

		JPanel cardSpottingBurningPile = new JPanel();
		cardSpottingBurningPile.setBackground(new Color(245, 245, 245));
		cardsSpottingFireType.add(cardSpottingBurningPile, "Burning Pile");
		cardSpottingBurningPile.setLayout(null);

		lblSpottingFireTypeFlameHeight = new RLabel(Main.resourceManager.getString("ui.label.spotting.fire.fheight"));
		lblSpottingFireTypeFlameHeight.setBounds(0, 20, 141, 20);
		cardSpottingBurningPile.add(lblSpottingFireTypeFlameHeight);

		txtSpottingFireTypeFlameHeight = new RTextField();
		txtSpottingFireTypeFlameHeight.setBounds(160, 20, 91, 20);
		cardSpottingBurningPile.add(txtSpottingFireTypeFlameHeight);
		txtSpottingFireTypeFlameHeight.setColumns(10);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblSpottingFireTypeFlameHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblSpottingFireTypeFlameHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
		lblSpottingFireTypeFlameHeightUnit.setBounds(260, 20, 31, 20);
		cardSpottingBurningPile.add(lblSpottingFireTypeFlameHeightUnit);

		cardSpottingSurfaceFire = new JPanel();
		cardSpottingSurfaceFire.setBackground(new Color(245, 245, 245));
		cardsSpottingFireType.add(cardSpottingSurfaceFire, "Surface Fire");
		cardSpottingSurfaceFire.setLayout(null);

		RLabel lblSpottingFlameLength = new RLabel(Main.resourceManager.getString("ui.label.spotting.fire.flength"));
		lblSpottingFlameLength.setBounds(0, 20, 151, 20);
		cardSpottingSurfaceFire.add(lblSpottingFlameLength);

		txtSpottingFireTypeFlameLength = new RTextField();
		txtSpottingFireTypeFlameLength.setBounds(160, 20, 91, 20);
		cardSpottingSurfaceFire.add(txtSpottingFireTypeFlameLength);
		txtSpottingFireTypeFlameLength.setColumns(10);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblSpottingFireTypeFlameLengthUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblSpottingFireTypeFlameLengthUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
		lblSpottingFireTypeFlameLengthUnit.setBounds(260, 20, 30, 20);
		cardSpottingSurfaceFire.add(lblSpottingFireTypeFlameLengthUnit);

		JPanel cardSpottingTorchingTrees = new JPanel();
		cardSpottingTorchingTrees.setBackground(new Color(245, 245, 245));
		cardsSpottingFireType.add(cardSpottingTorchingTrees, "Torching Trees");
		cardSpottingTorchingTrees.setLayout(null);

		RLabel lblSpottingSpecies = new RLabel(Main.resourceManager.getString("ui.label.spotting.fire.spec"));
		lblSpottingSpecies.setBounds(0, 20, 141, 20);
		cardSpottingTorchingTrees.add(lblSpottingSpecies);

		comboSpottingSpecies = new RComboBox<String>();
		comboSpottingSpecies.setBounds(160, 19, 121, 22);
		cardSpottingTorchingTrees.add(comboSpottingSpecies);

		RLabel lblSpottingDBH = new RLabel(Main.resourceManager.getString("ui.label.spotting.fire.dbh"));
		lblSpottingDBH.setBounds(0, 50, 141, 20);
		cardSpottingTorchingTrees.add(lblSpottingDBH);

		RLabel lblSpottingTreeHeight = new RLabel(Main.resourceManager.getString("ui.label.spotting.fire.theight"));
		lblSpottingTreeHeight.setBounds(0, 80, 141, 20);
		cardSpottingTorchingTrees.add(lblSpottingTreeHeight);

		RLabel lblSpottingNumberTreeTorching = new RLabel(Main.resourceManager.getString("ui.label.spotting.fire.count"));
		lblSpottingNumberTreeTorching.setBounds(0, 110, 141, 20);
		cardSpottingTorchingTrees.add(lblSpottingNumberTreeTorching);

		txtSpottingDBH = new RTextField();
		txtSpottingDBH.setBounds(160, 50, 91, 20);
		cardSpottingTorchingTrees.add(txtSpottingDBH);
		txtSpottingDBH.setColumns(10);

		txtSpottingTreeHeight = new RTextField();
		txtSpottingTreeHeight.setColumns(10);
		txtSpottingTreeHeight.setBounds(160, 80, 91, 20);
		cardSpottingTorchingTrees.add(txtSpottingTreeHeight);

		txtSpottingNumberTreeTorching = new RTextField();
		txtSpottingNumberTreeTorching.setColumns(10);
		txtSpottingNumberTreeTorching.setBounds(160, 110, 91, 20);
		cardSpottingTorchingTrees.add(txtSpottingNumberTreeTorching);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblSpottingDbhUnit = new RLabel(Main.resourceManager.getString("ui.label.units.cm"));
		else
			lblSpottingDbhUnit = new RLabel(Main.resourceManager.getString("ui.label.units.in"));
		lblSpottingDbhUnit.setBounds(260, 50, 30, 20);
		cardSpottingTorchingTrees.add(lblSpottingDbhUnit);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblSpottingTreeHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblSpottingTreeHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
		lblSpottingTreeHeightUnit.setBounds(260, 80, 30, 20);
		cardSpottingTorchingTrees.add(lblSpottingTreeHeightUnit);

		RGroupBox groupSpottingOutputs = new RGroupBox();
		groupSpottingOutputs.setText(Main.resourceManager.getString("ui.label.spotting.output.title"));
		groupSpottingOutputs.setBounds(675, 10, 281, 171);
		add(groupSpottingOutputs);

		JPanel panelSpottingOutputs = new JPanel();
		panelSpottingOutputs.setBackground(new Color(245, 245, 245));
		panelSpottingOutputs.setLayout(new SpringLayout());
		panelSpottingOutputs.setBounds(5, 15, 271, 145);
		groupSpottingOutputs.add(panelSpottingOutputs);

		RLabel lblSpottingFlameHeight = new RLabel(Main.resourceManager.getString("ui.label.spotting.output.fheight"));
		//lblSpottingFlameHeight.setBounds(10, 20, 131, 20);
		panelSpottingOutputs.add(lblSpottingFlameHeight);

		txtSpottingFlameHeight = new RTextField();
		txtSpottingFlameHeight.setEditable(false);
		txtSpottingFlameHeight.setColumns(10);
		//txtSpottingFlameHeight.setBounds(150, 20, 61, 20);
		panelSpottingOutputs.add(txtSpottingFlameHeight);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblSpottingFlameHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblSpottingFlameHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
		//lblSpottingFlameHeightUnit.setBounds(220, 20, 30, 20);
		panelSpottingOutputs.add(lblSpottingFlameHeightUnit);

		RLabel lblSpottingCriticalFireCover = new RLabel(Main.resourceManager.getString("ui.label.spotting.output.cheight"));
		//lblSpottingCriticalFireCover.setBounds(10, 50, 131, 20);
		panelSpottingOutputs.add(lblSpottingCriticalFireCover);

		txtSpottingCriticalCoverHeight = new RTextField();
		txtSpottingCriticalCoverHeight.setEditable(false);
		txtSpottingCriticalCoverHeight.setColumns(10);
		//txtSpottingCriticalCoverHeight.setBounds(150, 50, 61, 20);
		panelSpottingOutputs.add(txtSpottingCriticalCoverHeight);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblSpottingCriticalCoverHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblSpottingCriticalCoverHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
		//lblSpottingCriticalCoverHeightUnit.setBounds(220, 50, 30, 20);
		panelSpottingOutputs.add(lblSpottingCriticalCoverHeightUnit);

		RLabel lblSpottingFirebrandHeight = new RLabel(Main.resourceManager.getString("ui.label.spotting.output.firebrand"));
		//lblSpottingFirebrandHeight.setBounds(10, 80, 131, 20);
		panelSpottingOutputs.add(lblSpottingFirebrandHeight);

		txtSpottingFirebrandHeight = new RTextField();
		txtSpottingFirebrandHeight.setEditable(false);
		txtSpottingFirebrandHeight.setColumns(10);
		//txtSpottingFirebrandHeight.setBounds(150, 80, 61, 20);
		panelSpottingOutputs.add(txtSpottingFirebrandHeight);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblSpottingFirebrandHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.m"));
		else
			lblSpottingFirebrandHeightUnit = new RLabel(Main.resourceManager.getString("ui.label.units.ft"));
		//lblSpottingFirebrandHeightUnit.setBounds(220, 80, 30, 20);
		panelSpottingOutputs.add(lblSpottingFirebrandHeightUnit);

		RLabel lblSpottingSpotfireDistance = new RLabel(Main.resourceManager.getString("ui.label.spotting.output.spotfire"));
		//lblSpottingSpotfireDistance.setBounds(10, 110, 131, 20);
		panelSpottingOutputs.add(lblSpottingSpotfireDistance);

		txtSpottingSpotfireDistance = new RTextField();
		txtSpottingSpotfireDistance.setEditable(false);
		txtSpottingSpotfireDistance.setColumns(10);
		//txtSpottingSpotfireDistance.setBounds(150, 110, 61, 20);
		panelSpottingOutputs.add(txtSpottingSpotfireDistance);

		if (Main.unitSystem() == UnitSystem.METRIC)
			lblSpottingSpotfireDistanceUnit = new RLabel(Main.resourceManager.getString("ui.label.units.km"));
		else
			lblSpottingSpotfireDistanceUnit = new RLabel(Main.resourceManager.getString("ui.label.units.mi"));
		//lblSpottingSpotfireDistanceUnit.setBounds(220, 110, 30, 20);
		panelSpottingOutputs.add(lblSpottingSpotfireDistanceUnit);

		RLabel lblSpottingFlameDuration = new RLabel(Main.resourceManager.getString("ui.label.spotting.output.duration"));
		//lblSpottingFlameDuration.setBounds(10, 140, 131, 20);
		panelSpottingOutputs.add(lblSpottingFlameDuration);

		txtSpottingFlameDuration = new RTextField();
		txtSpottingFlameDuration.setEditable(false);
		txtSpottingFlameDuration.setColumns(10);
		//txtSpottingFlameDuration.setBounds(150, 140, 61, 20);
		panelSpottingOutputs.add(txtSpottingFlameDuration);

		lbl = new JLabel("");
		panelSpottingOutputs.add(lbl);

		SpringUtilities.makeCompactGrid(panelSpottingOutputs, 5, 3, 5, 5, 5, 5, 10, 10);

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

		btnSpottingCalculate = new RButton(Main.resourceManager.getString("ui.label.spotting.calculate"), RButton.Decoration.Calc);
		panel1.add(btnSpottingCalculate);

		btnReset = new RButton(Main.resourceManager.getString("ui.label.footer.reset"));
		btnReset.addActionListener((e) -> reset());
		panel1.add(btnReset);

		txtSpottingWindSpeed.getDocument().addDocumentListener(this);
		txtSpottingDownwindCoverHeight.getDocument().addDocumentListener(this);

		rdbtnSpottingFlatTerrain.addActionListener(this);
		rdbtnSpottingMountainousTerrain.addActionListener(this);
		comboSpottingSpotSource.addActionListener(this);
		txtSpottingR2VDistance.getDocument().addDocumentListener(this);
		txtSpottingR2VElevationChange.getDocument().addDocumentListener(this);

		rdbtnSpottingBurningPile.addActionListener(this);
		rdbtnSpottingSurfaceFire.addActionListener(this);
		rdbtnSpottingTorchingTrees.addActionListener(this);

		txtSpottingFireTypeFlameHeight.getDocument().addDocumentListener(this);
		txtSpottingFireTypeFlameLength.getDocument().addDocumentListener(this);
		comboSpottingSpecies.addActionListener(this);
		txtSpottingDBH.getDocument().addDocumentListener(this);
		txtSpottingTreeHeight.getDocument().addDocumentListener(this);
		txtSpottingNumberTreeTorching.getDocument().addDocumentListener(this);

		btnSpottingCalculate.addActionListener(this);

		populateSpecies();
		populateSources();

		calculator = new AlbiniCalculator();

		rdbtnSpottingBurningPile.setSelected(true);
		burningPileClicked();
		rdbtnSpottingFlatTerrain.setSelected(true);
		toggleTerrainType();
	}

	private void initTabOrder() {
		tabOrder.clear();
		tabOrder.add(txtSpottingWindSpeed);
		tabOrder.add(cmbWindHeight);
		tabOrder.add(txtSpottingDownwindCoverHeight);
		tabOrder.add(rdbtnSpottingFlatTerrain);
		tabOrder.add(rdbtnSpottingMountainousTerrain);
		tabOrder.add(comboSpottingSpotSource);
		tabOrder.add(txtSpottingR2VDistance);
		tabOrder.add(txtSpottingR2VElevationChange);
		tabOrder.add(rdbtnSpottingBurningPile);
		tabOrder.add(rdbtnSpottingSurfaceFire);
		tabOrder.add(rdbtnSpottingTorchingTrees);
		tabOrder.add(txtSpottingFireTypeFlameHeight);
		tabOrder.add(txtSpottingFireTypeFlameLength);
		tabOrder.add(comboSpottingSpecies);
		tabOrder.add(txtSpottingDBH);
		tabOrder.add(txtSpottingTreeHeight);
		tabOrder.add(txtSpottingNumberTreeTorching);
	}

	// }}

	@Override
	public void setInternetConnected(boolean conn) { }

	@Override
	public void reset() {
		txtSpottingWindSpeed.setText("");
		cmbWindHeight.setSelectedIndex(0);
		txtSpottingDownwindCoverHeight.setText("");
		rdbtnSpottingFlatTerrain.setSelected(true);
		comboSpottingSpotSource.setSelectedIndex(0);
		txtSpottingR2VDistance.setText("");
		txtSpottingR2VElevationChange.setText("");
		rdbtnSpottingBurningPile.setSelected(true);
		txtSpottingFireTypeFlameHeight.setText("");
		txtSpottingFireTypeFlameLength.setText("");
		comboSpottingSpecies.setSelectedIndex(0);
		txtSpottingDBH.setText("");
		txtSpottingTreeHeight.setText("");
		txtSpottingNumberTreeTorching.setText("");
		txtSpottingFlameHeight.setText("");
		txtSpottingCriticalCoverHeight.setText("");
		txtSpottingFirebrandHeight.setText("");
		txtSpottingSpotfireDistance.setText("");
		txtSpottingFlameDuration.setText("");
	}
	
	@Override
	public void settingsUpdated() {
		if (Main.unitSystem() == UnitSystem.METRIC) {
			String km = Main.resourceManager.getString("ui.label.units.km");
			String r2vd = lblSpottingR2VDistanceUnit.getText();
			if (!r2vd.equals(km)) {
				String temp = txtSpottingR2VDistance.getText();
				Double d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceLarge(UnitSystem.METRIC), UnitSystem.distanceLarge(UnitSystem.IMPERIAL));
					txtSpottingR2VDistance.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
				temp = txtSpottingR2VElevationChange.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(UnitSystem.IMPERIAL));
					txtSpottingR2VElevationChange.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
				temp = txtSpottingWindSpeed.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(UnitSystem.IMPERIAL));
					txtSpottingWindSpeed.setText(DecimalUtils.format(d, DataType.WIND_SPEED));
				}
				temp = txtSpottingDownwindCoverHeight.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(UnitSystem.IMPERIAL));
					txtSpottingDownwindCoverHeight.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
				temp = txtSpottingFireTypeFlameHeight.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(UnitSystem.IMPERIAL));
					txtSpottingFireTypeFlameHeight.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
				temp = txtSpottingFireTypeFlameLength.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(UnitSystem.IMPERIAL));
					txtSpottingFireTypeFlameLength.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
				temp = txtSpottingDBH.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceSmall2(UnitSystem.METRIC), UnitSystem.distanceSmall2(UnitSystem.IMPERIAL));
					txtSpottingDBH.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
				temp = txtSpottingTreeHeight.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d,  UnitSystem.distanceMedium(UnitSystem.METRIC), UnitSystem.distanceMedium(UnitSystem.IMPERIAL));
					txtSpottingTreeHeight.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
			}
			lblSpottingR2VDistanceUnit.setText(km);
			lblSpottingR2VElevationChangeUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblSpottingWindSpeedUnit.setText(Main.resourceManager.getString("ui.label.units.kiloperhour"));
			lblSpottingDownwindCoverHeightUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblSpottingFireTypeFlameHeightUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblSpottingFireTypeFlameLengthUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblSpottingDbhUnit.setText(Main.resourceManager.getString("ui.label.units.cm"));
			lblSpottingTreeHeightUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblSpottingFlameHeightUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblSpottingCriticalCoverHeightUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblSpottingFirebrandHeightUnit.setText(Main.resourceManager.getString("ui.label.units.m"));
			lblSpottingSpotfireDistanceUnit.setText(Main.resourceManager.getString("ui.label.units.km"));
		}
		else {
			String mi = Main.resourceManager.getString("ui.label.units.mi");
			String r2vd = lblSpottingR2VDistanceUnit.getText();
			if (!r2vd.equals(mi)) {
				String temp = txtSpottingR2VDistance.getText();
				Double d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceLarge(UnitSystem.IMPERIAL), UnitSystem.distanceLarge(UnitSystem.METRIC));
					txtSpottingR2VDistance.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
				temp = txtSpottingR2VElevationChange.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.IMPERIAL), UnitSystem.distanceMedium(UnitSystem.METRIC));
					txtSpottingR2VElevationChange.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
				temp = txtSpottingWindSpeed.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.speed(UnitSystem.IMPERIAL), UnitSystem.speed(UnitSystem.METRIC));
					txtSpottingWindSpeed.setText(DecimalUtils.format(d, DataType.WIND_SPEED));
				}
				temp = txtSpottingDownwindCoverHeight.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.IMPERIAL), UnitSystem.distanceMedium(UnitSystem.METRIC));
					txtSpottingDownwindCoverHeight.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
				temp = txtSpottingFireTypeFlameHeight.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.IMPERIAL), UnitSystem.distanceMedium(UnitSystem.METRIC));
					txtSpottingFireTypeFlameHeight.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
				temp = txtSpottingFireTypeFlameLength.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceMedium(UnitSystem.IMPERIAL), UnitSystem.distanceMedium(UnitSystem.METRIC));
					txtSpottingFireTypeFlameLength.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
				temp = txtSpottingDBH.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d, UnitSystem.distanceSmall2(UnitSystem.IMPERIAL), UnitSystem.distanceSmall2(UnitSystem.METRIC));
					txtSpottingDBH.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
				temp = txtSpottingTreeHeight.getText();
				d = DecimalUtils.valueOf(temp);
				if (d != null) {
					d = Convert.convertUnit(d,  UnitSystem.distanceMedium(UnitSystem.IMPERIAL), UnitSystem.distanceMedium(UnitSystem.METRIC));
					txtSpottingTreeHeight.setText(DecimalUtils.format(d, DataType.FORCE_ATMOST_2));
				}
			}
			lblSpottingR2VDistanceUnit.setText(mi);
			lblSpottingR2VElevationChangeUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblSpottingWindSpeedUnit.setText(Main.resourceManager.getString("ui.label.units.milesperhour"));
			lblSpottingDownwindCoverHeightUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblSpottingFireTypeFlameHeightUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblSpottingFireTypeFlameLengthUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblSpottingDbhUnit.setText(Main.resourceManager.getString("ui.label.units.in"));
			lblSpottingTreeHeightUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblSpottingFlameHeightUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblSpottingCriticalCoverHeightUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblSpottingFirebrandHeightUnit.setText(Main.resourceManager.getString("ui.label.units.ft"));
			lblSpottingSpotfireDistanceUnit.setText(Main.resourceManager.getString("ui.label.units.mi"));
		}
		clearOutputValuesOnForm();
	}

	@Override
	public boolean supportsReset() {
		return true;
	}

	@Override
	public void onLocationChanged() { }

	@Override
	public void onTimeZoneChanged() { }

	@Override
	public void onDateChanged() { }

	@Override
	public void onCurrentTabChanged() { }
}
