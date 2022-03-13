/***********************************************************************
 * REDapp - RPasswordField.java
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

import javax.swing.JPasswordField;
import javax.swing.text.Document;

public class RPasswordField extends RBaseTextField<JPasswordField> {
	private static final long serialVersionUID = 1L;

	public RPasswordField() {
		super(new JPasswordField());
	}

	public RPasswordField(int columns) {
		super(new JPasswordField());
	}

	public RPasswordField(String text) {
		super(new JPasswordField());
	}

	public RPasswordField(String text, int columns) {
		super(new JPasswordField());
	}

	public RPasswordField(Document doc, String text, int columns) {
		super(new JPasswordField());
	}
	
	@Override
	protected int getTextLength() {
		return textField.getPassword().length;
	}
}
