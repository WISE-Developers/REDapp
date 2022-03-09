/***********************************************************************
 * REDapp - RMapValueTextField.java
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

package ca.redapp.ui.component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import ca.cwfgm.mapunits.MetricPrefix;
import ca.cwfgm.mapunits.MetricUnitValue;
import ca.cwfgm.mapunits.MetricUnits;
import ca.hss.general.DecimalUtils;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import ca.redapp.ui.Main;

/**
 * A text field that can display a string in either the display units or in a scaled
 * unit system for drawing on a map.
 * 
 * @author Travis
 *
 */
public class RMapValueTextField extends JPanel {
	private static final long serialVersionUID = 1L;
	private RTextField textField;
	private JButton button;
	private JLabel label;
	private JPopupMenu popup;
	private JCheckBoxMenuItem actualAction;
	private JCheckBoxMenuItem convertedAction;
	private MetricUnitValue actual = null;
	private MetricUnitValue converted = null;
	private boolean actualSet = false;
	private boolean convertedSet = false;
	private boolean showConverted = false;
	private double scale = 1.0;
	private MetricPrefix tempUnits = null;
	private boolean showingList = false;
	private ArrayList<RMapValueTextFieldListener> listeners = new ArrayList<RMapValueTextField.RMapValueTextFieldListener>();

	public RMapValueTextField() {
		textField = new RTextField();
		textField.setEditable(false);
		setLayout(null);
		textField.setBounds(0, 0, getWidth(), getHeight());
		button = new JButton();
		button.setIcon(new ImageIcon(RClearTextField.class.getResource("/images/icons/ExpandArrow.png")));
		button.setBounds(0, 0, 16, getHeight());
		button.setVisible(false);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clicked();
			}
		});
		popup = new JPopupMenu();
		actualAction = new JCheckBoxMenuItem("", true);
		actualAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showConverted = false;
				showingList = false;
				convertedAction.setSelected(false);
				refreshValue();
			}
		});
		convertedAction = new JCheckBoxMenuItem("", false);
		convertedAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showConverted = true;
				showingList = false;
				actualAction.setSelected(false);
				refreshValue();
			}
		});
		ButtonGroup bgroup = new ButtonGroup();
		bgroup.add(actualAction);
		bgroup.add(convertedAction);
		popup.add(actualAction);
		popup.add(convertedAction);
		popup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				showingList = false;
			}
		});
		addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				resizeEvent();
			}

			@Override
			public void componentShown(ComponentEvent e) { }
			@Override
			public void componentMoved(ComponentEvent e) { }
			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
		add(button);
		add(textField);
		setMinimumSize(new Dimension(40, 20));
		setPreferredSize(new Dimension(100, 20));
		setOpaque(false);
	}

	public void addRMapValueTextFieldListener(RMapValueTextFieldListener listener) {
		listeners.add(listener);
	}

	public void setActualValue(double value, MetricPrefix prefix, MetricUnits units) {
		actual = new MetricUnitValue(prefix, units, value);
		actualSet = true;
		String unit;
		double val = actual.getValue();
		if (Main.unitSystem() == UnitSystem.METRIC) {
			unit = actual.getUnitString();
		}
		else {
			val = Convert.convertUnit(val, UnitSystem.distanceMedium2(UnitSystem.IMPERIAL), UnitSystem.distanceMedium2(UnitSystem.METRIC));
			unit = Main.resourceManager.getString("ui.label.units.ch");
		}
		actualAction.setText(DecimalUtils.format(val, DecimalUtils.DataType.FORCE_ATMOST_2) + " " + unit);
		if (tempUnits != null)
			setConvertedUnits(tempUnits);
		else if (converted != null)
			setConvertedUnits(converted.getPrefix());
		refreshValue();
	}

	public void setConvertedUnits(MetricPrefix units) {
		if (units == MetricPrefix.DISABLE) {
			convertedSet = false;
			converted = null;
			showConverted = false;
			tempUnits = null;
			setShowButton(false);
			return;
		}
		if (actual == null) {
			tempUnits = units;
			return;
		}
		if (units == actual.getPrefix() && (scale == 1 || scale <= 0)) {
			convertedSet = true;
			converted = null;
			showConverted = false;
			setShowButton(false);
		}
		else {
			tempUnits = null;
			convertedSet = true;
			converted = MetricPrefix.convertTo(actual, units);
			converted.setValue(converted.getValue() / scale);
			String brack = " (";
			brack += Main.resourceManager.getString("ui.label.mapbox");
			brack += ")";
			convertedAction.setText(DecimalUtils.format(converted.getValue(), DecimalUtils.DataType.FORCE_ATMOST_2) + " " + converted.getUnitString() + brack);
			setShowButton(true);
		}
		refreshValue();
	}

	public void setScale(int scale) {
		this.scale = scale;
		if (actual == null || converted == null)
			return;
		converted = MetricPrefix.convertTo(actual, converted.getPrefix());
		converted.setValue(converted.getValue() / scale);
		refreshValue();
	}

	public void attachUnitLabel(JLabel label) {
		this.label = label;
		refreshValue();
	}

	public void setScaleAndUnits(Integer scale, MetricPrefix units) {
		this.scale = scale;
		setConvertedUnits(units);
	}

	public double getActualValue() {
		return actual.getValue();
	}

	public double getConvertedValue() {
		return converted.getValue();
	}

	public MetricPrefix getActualUnits() {
		return actual.getPrefix();
	}

	public MetricPrefix getConvertedUnits() {
		return converted.getPrefix();
	}

	public int getScale() {
		return (int)scale;
	}

	public JLabel getAttachedLabel() {
		return label;
	}

	public void setShowConverted(boolean show) {
		if (show != showConverted) {
			if (show) {
				convertedAction.setSelected(true);
				actualAction.setSelected(false);
			}
			else {
				convertedAction.setSelected(false);
				actualAction.setSelected(true);
			}
			showConverted = show;
			refreshValue();
		}
	}

	public boolean isShowConverted() {
		return showConverted;
	}

	public void clear() {
		if (actual == null && converted == null)
			return;
		if (converted != null)
			tempUnits = converted.getPrefix();
		else
			tempUnits = null;
		actual = null;
		converted = null;
		actualSet = false;
		convertedSet = false;
		setShowButton(false);
		refreshValue();
	}

	private void refreshValue() {
		if (showConverted && convertedSet) {
			if (label == null)
				textField.setText(DecimalUtils.format(converted.getValue(), DecimalUtils.DataType.FORCE_ATMOST_2) + "*");
			else {
				textField.setText(DecimalUtils.format(converted.getValue(), DecimalUtils.DataType.FORCE_ATMOST_2));
				label.setText(converted.getUnitString() + "*");
				label.setToolTipText(Main.resourceManager.getString("ui.label.mapbox"));
			}
			notifyShowConvertedValueChanged(true);
		}
		else if (actualSet) {
			if (label == null)
				textField.setText(DecimalUtils.format(actual.getValue(), DecimalUtils.DataType.FORCE_ATMOST_2));
			else {
				String units;
				double val = actual.getValue();
				if (Main.unitSystem() == UnitSystem.METRIC) {
					units = actual.getUnitString();
				}
				else {
					val = Convert.convertUnit(val, UnitSystem.distanceMedium2(UnitSystem.IMPERIAL), UnitSystem.distanceMedium2(UnitSystem.METRIC));
					units = Main.resourceManager.getString("ui.label.units.ch");
				}
				textField.setText(DecimalUtils.format(val, DecimalUtils.DataType.FORCE_ATMOST_2));
				label.setText(units);
				label.setToolTipText(null);
			}
			notifyShowConvertedValueChanged(false);
		}
		else
			textField.setText("");
	}

	private void setShowButton(boolean show) {
		if (show) {
			button.setVisible(true);
			textField.setBounds(16, 0, getWidth() - 16, getHeight());
		}
		else {
			button.setVisible(false);
			textField.setBounds(0, 0, getWidth(), getHeight());
		}
	}

	protected void clicked() {
		if (showingList) {
			showingList = false;
			popup.setVisible(false);
		}
		else {
			showingList = true;
			popup.show(this, 0, getHeight());
		}
	}

	private void resizeEvent() {
		button.setBounds(0, 0, 16, getHeight());
		if (button.isVisible())
			textField.setBounds(16, 0, getWidth() - 16, getHeight());
		else
			textField.setBounds(0, 0, getWidth(), getHeight());
	}

	public static interface RMapValueTextFieldListener {
		public abstract void showConvertedValueChanged(RMapValueTextField field, Boolean shown);
	}

	private void notifyShowConvertedValueChanged(boolean shown) {
		for (RMapValueTextFieldListener listener : listeners) {
			listener.showConvertedValueChanged(this, shown);
		}
	}
}
