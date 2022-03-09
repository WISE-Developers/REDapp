/***********************************************************************
 * REDapp - RLabel.java
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

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * A helper class for JLabel that enforces font sizes on Linux and helps with displaying subscript using HTML.
 * 
 * @author Travis Redpath
 *
 */
public class RLabel extends JLabel {
	private static final long serialVersionUID = 1L;

	public RLabel() {
		this("", SwingConstants.LEFT);
	}

	public RLabel(String text) {
		this(text, SwingConstants.LEFT);
	}

	public RLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		String os = System.getProperty("os.name").toLowerCase();

		if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0)
			setFont(getFont().deriveFont(12.0f));
		if (text.contains("<sub>") && !text.toLowerCase().startsWith("<html>")) {
			text = "<html>" + text + "</html>";
			setText(text);
		}
	}
}
