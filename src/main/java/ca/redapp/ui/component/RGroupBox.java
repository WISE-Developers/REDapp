/***********************************************************************
 * REDapp - RGroupBox.java
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

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import ca.redapp.ui.component.RGroupBox;

import java.awt.Font;
import java.awt.Image;
import java.awt.Color;

/**
 * A group box for grouping similar items in a window.
 * 
 * @author Travis Redpath
 *
 */
public class RGroupBox extends JPanel {
	private static final long serialVersionUID = 1L;
	private Color lineColour = null;
	private Color textColour = null;
	private String text = null;
	@SuppressWarnings("unused")
	private Image image = null;

	public RGroupBox() {
		super();
		setLayout(null);
		reset();
		Color back = UIManager.getColor("RGroupBox.backcolor");
		if (back != null) {
			this.setBackground(back);
		}
	}
	
	/**
	 * Set the colour of the group boxes border.
	 * 
	 * @param color the border colour.
	 */
	public void setBorderColour(Color color) {
		this.lineColour = color;
		reset();
	}
	
	/**
	 * Set the background colour of the group box.
	 * 
	 * @param colour the background colour
	 */
	public void setBackgroundColour(Color colour) {
		this.setBackground(colour);
	}
	
	/**
	 * Set the text colour of the the title of the group box.
	 * 
	 * @param colour the group box titles text colour
	 */
	public void setTextColour(Color colour) {
		textColour = colour;
		reset();
	}
	
	/**
	 * Set the title to display in the group box.
	 * 
	 * @param text the new title text
	 */
	public void setText(String text) {
		this.text = text;
		reset();
	}
	
	public void setImage(Image img) {
		image = img;
		invalidate();
		repaint();
	}
	
	private void reset() {
		Color line = null;
		boolean hasLineColour = true;
		int fontsize = Font.BOLD;
		if (lineColour == null) {
			line = UIManager.getColor("RGroupBox.bordercolor");
			if (line == null) {
				line = new Color(213, 223, 229);
				hasLineColour = false;
			}
		}
		else
			line = lineColour;
		Color textc = null;
		if (textColour == null) {
			textc = UIManager.getColor("RGroupBox.textcolor");
			if (textc == null) {
				textc = Color.BLACK;
				fontsize = Font.PLAIN;
			}
		}
		else
			textc = textColour;
		String txt;
		if (text == null)
			txt = "GroupBox";
		else
			txt = text;
		if (hasLineColour) {
			Border lborder = new LineBorder(line, 1, false);
			super.setBorder(new TitledBorder(lborder, txt, TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", fontsize, 12), textc)/* {
				private static final long serialVersionUID = 1L;
				
				@Override
				public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
					super.paintBorder(c, g, x, y, width, height);
					
					if (image != null) {
						FontMetrics metrics = g.getFontMetrics();
						int fwid = metrics.stringWidth(text);
						int fhet = metrics.getHeight();
						g.drawImage(image, x + 10 + fwid, y, fhet, fhet, null);
					}
				}
			}*/);
		}
		else
			super.setBorder(new TitledBorder(null, txt, TitledBorder.LEADING, TitledBorder.TOP, null, textc));
	}
	
	@Override
	public void setBorder(Border border) {
		reset();
	}
}
