/***********************************************************************
 * REDapp - ForecastModel.java
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

import ca.hss.general.DecimalUtils;
import ca.hss.general.OutVariable;
import ca.hss.general.DecimalUtils.DataType;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import ca.redapp.ui.Main;

/**
 * A table model for displaying forecast data. Forecast data
 * from Environment Canada is four forecasts per day 6 hours apart.
 * 
 * @author Travis Redpath
 *
 */
public class ForecastModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private Double temperature[] = new Double[4];
	private Double rh[] = new Double[4];
	private Double precip[] = new Double[4];
	private Double ws[] = new Double[4];
	private Double wd[] = new Double[4];

	public ForecastModel() {
		clearData();
	}

	public void clearData() {
		for (int i = 0; i < 4; i++) {
			temperature[i] = null;
			rh[i] = null;
			precip[i] = null;
			ws[i] = null;
			wd[i] = null;
		}
		fireTableDataChanged();
	}

	public void setDataAt(int index, Double temp, Double rh, Double precip, Double ws, Double wd) {
		if (index >= 0 && index < 5) {
			temperature[index] = temp;
			this.rh[index] = rh;
			this.precip[index] = precip;
			this.ws[index] = ws;
			this.wd[index] = wd;
			fireTableDataChanged();
		}
	}

	public void getDataAt(int index, OutVariable<Double> temp, OutVariable<Double> rh, OutVariable<Double> precip, OutVariable<Double> ws, OutVariable<Double> wd) {
		if (index >= 0 && index < 5) {
			temp.value = temperature[index];
			rh.value = this.rh[index];
			precip.value = this.precip[index];
			ws.value = this.ws[index];
			wd.value = this.wd[index];
		}
	}

	@Override
	public int getRowCount() {
		return 4;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public Object getValueAt(int RowIndex, int ColumnIndex) {
		switch (ColumnIndex) {
		case 0:
			if (temperature[RowIndex] == null)
				return "";
			return DecimalUtils.format(Convert.convertUnit(temperature[RowIndex], UnitSystem.temperature(Main.unitSystem()), UnitSystem.temperature(UnitSystem.METRIC)), DataType.TEMPERATURE);
		case 1:
			if (rh[RowIndex] == null)
				return "";
			return DecimalUtils.format(rh[RowIndex], DataType.RH);
		case 2:
			if (precip[RowIndex] == null)
				return "";
			return DecimalUtils.format(Convert.convertUnit(precip[RowIndex], UnitSystem.distanceSmall(Main.unitSystem()), UnitSystem.distanceSmall(UnitSystem.METRIC)), DataType.PRECIP);
		case 3:
			if (ws[RowIndex] == null)
				return "";
			return DecimalUtils.format(Convert.convertUnit(ws[RowIndex], UnitSystem.speed(Main.unitSystem()), UnitSystem.speed(UnitSystem.METRIC)), DataType.WIND_SPEED);
		case 4:
			if (wd[RowIndex] == null)
				return "";
			return DecimalUtils.format(wd[RowIndex], DataType.WIND_DIR);
		}
		return "";
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case 0:
			return Main.resourceManager.getString("ui.label.weather.abbv.temp");
		case 1:
			return Main.resourceManager.getString("ui.label.weather.abbv.rh");
		case 2:
			return Main.resourceManager.getString("ui.label.weather.abbv.precip");
		case 3:
			return Main.resourceManager.getString("ui.label.weather.abbv.ws");
		case 4:
			return Main.resourceManager.getString("ui.label.weather.abbv.wd");
		}
		return "";
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return String.class;
	}
}
