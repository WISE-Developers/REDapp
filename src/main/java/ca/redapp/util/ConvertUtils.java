/***********************************************************************
 * REDapp - ConvertUtils.java
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

package ca.redapp.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.prefs.Preferences;

import ca.redapp.ui.Main;

/**
 * A class for converting between different ways of displaying the latitude and longitude.
 *
 */
public class ConvertUtils {
	private ConvertUtils() {
	}

	private static Preferences prefs = Preferences.userRoot().node("ca.hss.app.redapp.ui.Main");

	public static double getDecimalDegrees(String input) throws Exception {
		String[] strs = input.split("\u00B0");
		String units = prefs.get("coordinateUnits", "Decimal Degrees");
		if (units.equals("Decimal Degrees")) {
			NumberFormat df = DecimalFormat.getInstance(Main.resourceManager.loc);
			return df.parse(strs[0]).doubleValue();
		} else if (strs.length == 2) {
			double value = Double.parseDouble(strs[0]);
			boolean isNegative = false;
			if (value < 0) {
				isNegative = true;
				value = -value;
			}
			strs = strs[1].split("'");
			if (units.equals("Degrees Decimal Minutes")) {
				double minutes = Double.parseDouble(strs[0]);
				if (minutes > 60 || minutes < 0)
					throw new Exception("Minutes outside of valid range");
				value += minutes / 60.0;
			} else {
				if (strs.length == 2) {
					double minutes = Double.parseDouble(strs[0]);
					double seconds = Double.parseDouble(strs[1].split("\"")[0]);
					if (minutes > 60 || minutes < 0)
						throw new Exception("Minutes outside of valid range");
					if (seconds > 60 || seconds < 0)
						throw new Exception("Seconds outside of valid range");
					value += minutes / 60.0;
					value += seconds / 60.0 / 60.0;
				} else
					throw new Exception("Invalid format");
			}
			if (isNegative)
				return -value;
			else
				return value;
		} else
			throw new Exception("Invalid format");
	}

	public static String formatDegrees(double decimalDegrees) {
		String units = prefs.get("coordinateUnits", "Decimal Degrees");
		if (units.equals("Decimal Degrees")) {
			DecimalFormat df = (DecimalFormat)NumberFormat.getNumberInstance(Main.resourceManager.loc);
			df.applyPattern("##.########");
			return df.format(decimalDegrees) + "\u00B0";
		} else {
			int degrees = (int) (decimalDegrees);
			String value = degrees + "\u00B0";
			double remainder = Math.abs(decimalDegrees % 1.0);
			if (units.equals("Degrees Decimal Minutes")) {
				DecimalFormat df = (DecimalFormat)NumberFormat.getNumberInstance(Main.resourceManager.loc);
				df.applyPattern("##.#####");
				return value + df.format(remainder * 60.0) + "'";
			} else {
				DecimalFormat df = (DecimalFormat)NumberFormat.getNumberInstance(Main.resourceManager.loc);
				df.applyPattern("##.####");
				int minutes = (int) (remainder * 60.0);
				value += minutes + "'";
				double seconds = (remainder * 60 * 60) % 60;
				return value + df.format(seconds) + "\"";
			}
		}
	}
}
