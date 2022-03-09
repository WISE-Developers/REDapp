/***********************************************************************
 * REDapp - DayModel.java
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

package ca.redapp.data;

import javax.swing.table.AbstractTableModel;

import ca.cwfgm.grid.IWXData;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import ca.redapp.ui.Main;

/**
 * A table model to store a days worth of weather data.
 * 
 * @author Travis Redpath
 *
 */
public class DayModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	public IWXData[] hours = null;

	public DayModel() {
		hours = new IWXData[24];
		for (int i = 0; i < 24; i++) {
			hours[i] = new IWXData();
			hours[i].precipitation = 0;
			hours[i].rh = 0;
			hours[i].temperature = 0;
			hours[i].windDirection = 0;
			hours[i].windSpeed = 0;
		}
	}

	@Override
	public int getRowCount() {
		return 24;
	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 1:
			return Convert.convertUnit(hours[rowIndex].temperature, UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC));
		case 2:
			return hours[rowIndex].rh;
		case 3:
			return Convert.convertUnit(hours[rowIndex].windSpeed, UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC));
		case 4:
			return hours[rowIndex].windDirection;
		case 5:
			return Convert.convertUnit(hours[rowIndex].precipitation, UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC));
		default:
			return String.valueOf(rowIndex) + ":00";
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		try {
			Double d = Double.parseDouble(value.toString());
			switch (col) {
			case 1:
				hours[row].temperature = Convert.convertUnit(d, UnitSystem.temperature(UnitSystem.METRIC), UnitSystem.temperature(Main.unitSystem()));
				break;
			case 2:
				hours[row].rh = d;
				break;
			case 3:
				hours[row].windSpeed = Convert.convertUnit(d, UnitSystem.speed(UnitSystem.METRIC), UnitSystem.speed(Main.unitSystem()));
				break;
			case 4:
				hours[row].windDirection = d;
				break;
			case 5:
				hours[row].precipitation = Convert.convertUnit(d, UnitSystem.distanceSmall(UnitSystem.METRIC), UnitSystem.distanceSmall(Main.unitSystem()));
				break;
			}
		}
		catch (NumberFormatException ex) {
		}
		fireTableCellUpdated(row, col);
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case 1:
			return Main.resourceManager.getString("ui.label.weather.abbv.temp");
		case 2:
			return Main.resourceManager.getString("ui.label.weather.abbv.rh");
		case 3:
			return Main.resourceManager.getString("ui.label.weather.abbv.ws");
		case 4:
			return Main.resourceManager.getString("ui.label.weather.abbv.wd");
		case 5:
			return Main.resourceManager.getString("ui.label.weather.abbv.precip");
		default:
			return Main.resourceManager.getString("ui.label.custom.time");
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return col != 0;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if (col == 0)
			return String.class;
		return Double.class;
	}
}
