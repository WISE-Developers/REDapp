/***********************************************************************
 * REDapp - RTextArea.java
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

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.border.Border;

/**
 * A text area with a red border when the mouse hovers over it.
 * 
 * @author Travis
 *
 */
public class RTextArea extends JTextArea implements MouseListener {
	private static final long serialVersionUID = 1L;
	private Border normal;
	private Border hover;

	public RTextArea() {
		this("");
	}

	public RTextArea(String text) {
		super(text);
		normal = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(153, 153, 153), 1), BorderFactory.createEmptyBorder(3, 6, 3, 6));
		hover = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(168, 69, 69), 1), BorderFactory.createEmptyBorder(3, 6, 3, 6));
		setBorder(normal);
		addMouseListener(this);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		setBorder(hover);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setBorder(normal);
	}

	@Override
	public void mouseClicked(MouseEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) { }

	@Override
	public void mouseReleased(MouseEvent e) { }
}
