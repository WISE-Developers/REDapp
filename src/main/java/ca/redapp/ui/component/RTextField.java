/***********************************************************************
 * REDapp - RTextField.java
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

import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.Document;
import javax.swing.text.NumberFormatter;

/**
 * A text field with a square, gray border when the mouse isn't over it and a square, red border when the mouse
 * is hovering over it. It also contains a menu for default text actions (copy, cut, paste, etc).
 * 
 * @author Travis Redpath
 *
 */
public class RTextField extends RBaseTextField<JFormattedTextField> {
	private static final long serialVersionUID = 1L;
	private ArrayList<RTextFieldListener> listeners = new ArrayList<RTextField.RTextFieldListener>();
	private Map<String, Object> userData = null;

	public RTextField() {
		super(new JFormattedTextField());
	}

	public RTextField(Format format) {
		super(new JFormattedTextField());
		setFormat(format);
	}

	public RTextField(int columns) {
		super(new JFormattedTextField());
	}

	public RTextField(String text) {
		super(new JFormattedTextField());
	}

	public RTextField(String text, int columns) {
		super(new JFormattedTextField());
	}

	public RTextField(Document doc, String text, int columns) {
		super(new JFormattedTextField());
	}
	
	@Override
	protected int getTextLength() {
		return textField.getText().length();
	}

	/**
	 * Set a format to display numbers or dates with.
	 * 
	 * @param format
	 */
	public void setFormat(Format format) {
		AbstractFormatter formatter = null;
		if (format.getClass() == DecimalFormat.class || format.getClass() == NumberFormat.class || format.getClass() == ChoiceFormat.class)
			formatter = new NumberFormatter((NumberFormat)format);
		else if (format.getClass() == DateFormat.class || format.getClass() == SimpleDateFormat.class)
			formatter = new DateFormatter((DateFormat)format);
		if (formatter != null) {
			DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
			textField.setFormatterFactory(factory);
		}
	}

	public void setUserData(String name, Object data) {
		if (userData == null)
			userData = new HashMap<String, Object>();
		userData.put(name, data);
	}

	public Object getUserData(String name, Object def) {
		if (userData == null || !userData.containsKey(name))
			return def;
		return userData.get(name);
	}

	void addRTextFieldListener(RTextFieldListener listener) {
		listeners.add(listener);
	}

	void removeRTextFieldListener(RTextFieldListener listener) {
		listeners.remove(listener);
	}

	static interface RTextFieldListener {
		public abstract void redrawn();
	}
} //java.version
