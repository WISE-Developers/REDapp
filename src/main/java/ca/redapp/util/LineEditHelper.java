/***********************************************************************
 * REDapp - LineEditHelper.java
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

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import ca.redapp.ui.Main;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;

/**
 * Helper for getting floating point value from text fields. It can also set error
 * messages on text fields.
 * 
 */
public class LineEditHelper {
	private static NumberFormat df = DecimalFormat.getInstance(Main.resourceManager.loc);

	private LineEditHelper() { }

	public static void setReadOnly(JTextField edit) {
		edit.setEditable(false);
		edit.setBackground(new Color(0xf5f5f5));
	}

	public static Double getDegreesFromLineEdit(JTextField edit){
		boolean error = false;
		Double retval = null;
		String d = edit.getText();
		if (d.length() == 0) {
			lineEditHandleError(edit);
		}
		try {
			retval = ConvertUtils.getDecimalDegrees(d);
		} catch (Exception e) {
			error = true;
			lineEditHandleError(edit);
		}
		if (!error)
			lineEditClearError(edit);
		return retval;
	}

	public static Double getDegreesFromLineEdit(RTextField edit){
		boolean error = false;
		Double retval = null;
		String d = edit.getText();
		if (d.length() == 0) {
			lineEditHandleError(edit);
		} //TxtLatitude
		try {
			retval = ConvertUtils.getDecimalDegrees(d);
		} catch (Exception e) {
			error = true;
			lineEditHandleError(edit);
		}
		if (!error)
			lineEditClearError(edit);
		return retval;
	}

	public static Double getDoubleFromLineEdit(JTextComponent edit) {
		boolean error = false;
		Double retval = null;
		String d = edit.getText();
		if (d.length() == 0) {
			lineEditHandleError(edit);
		}
		try {
			retval = df.parse(d).doubleValue();
		} catch (Exception e) {
			error = true;
			lineEditHandleError(edit);
		}
		if (!error)
			lineEditClearError(edit);
		return retval;
	}

	public static Double getDoubleFromLineEditNoError(RTextField edit) {
		Double retval = null;
		String d = edit.getText();
		try {
			retval = df.parse(d).doubleValue();
		} catch (Exception e) {
		}
		return retval;
	}

	public static Double getDoubleFromLineEdit(RTextField edit) {
		boolean error = false;
		Double retval = null;
		String d = edit.getText();
		if (d.length() == 0) {
			lineEditHandleError(edit);
		}
		try {
			retval = df.parse(d).doubleValue();
		} catch (Exception e) {
			error = true;
			lineEditHandleError(edit);
		}
		if (!error)
			lineEditClearError(edit);
		return retval;
	}

	public static Integer getIntegerFromLineEdit(JTextField edit) {
		boolean error = false;
		Integer retval = null;
		String d = edit.getText();
		if (d.length() == 0) {
			lineEditHandleError(edit);
		}
		try {
			retval = Integer.valueOf(d);
		} catch (Exception e) {
			error = true;
			lineEditHandleError(edit);
		}
		if (!error)
			lineEditClearError(edit);
		return retval;
	}

	public static Integer getIntegerFromLineEdit(RTextField edit) {
		boolean error = false;
		Integer retval = null;
		String d = edit.getText();
		if (d.length() == 0) {
			lineEditHandleError(edit);
		}
		try {
			retval = Integer.valueOf(d);
		} catch (Exception e) {
			error = true;
			lineEditHandleError(edit);
		}
		if (!error)
			lineEditClearError(edit);
		return retval;
	}

	public static void lineEditHandleError(JComponent edit) {
		/*QPalette pal = edit.palette();
		pal.setColor(QPalette.ColorRole.Base, QColor.red);
		edit.setPalette(pal);*/
		edit.setBackground(Color.RED);
	}

	public static void lineEditClearError(JComponent edit) {
		/*QPalette pal = edit.palette();
		pal.setColor(QPalette.ColorRole.Base, QColor.white);
		edit.setPalette(pal);*/
		edit.setBackground(null);//TODO
		edit.setToolTipText(null);
	}

	public static void lineEditHandleError(JComponent edit, String error) {
		/*QPalette pal = edit.palette();
		pal.setColor(QPalette.ColorRole.Base, QColor.red);
		edit.setPalette(pal);*/
		edit.setBackground(Color.RED);
		edit.setToolTipText(error);
	}

	public static void setEnabled(JLabel label, JComponent edit, JLabel unit,
			boolean enabled) {
		label.setEnabled(enabled);
		edit.setEnabled(enabled);
		if (unit != null)
			unit.setEnabled(enabled);
	}

	public static void setEnabled(RLabel label, JComponent edit, RLabel unit,
			boolean enabled) {
		label.setEnabled(enabled);
		edit.setEnabled(enabled);
		if (unit != null)
			unit.setEnabled(enabled);
	}

	public static void setEnabled(JComponent edit, boolean enabled) {
		edit.setEnabled(enabled);
	}
}
