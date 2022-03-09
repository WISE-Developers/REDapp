/***********************************************************************
 * REDapp - RButton.java
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.IOException;
import java.text.Normalizer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * A button with a red, rounded-rectangle background with text and an optional image.
 * 
 * The button uses the COUTURE-Bold font and always displays letters as capitals and
 * without diacritics.
 * 
 * @author Travis Redpath
 */
public class RButton extends JButton {
	private static final long serialVersionUID = 1L;
	protected JLabel textField;
	protected JLabel textField2;
	protected JLabel imageBox;
	private int internalWidth = 40;
	private int preferredWidth = -1;
	private final int internalHeight = 41;
	private final Decoration decor;
	private String currentText;
	private ImageIcon decorImage;
	public static final boolean USE_SHADOW = false;

	static {
		try {
			Font font = Font.createFont(Font.TRUETYPE_FONT, RButton.class.getResourceAsStream("/data/COUTURE-Bold.ttf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(font);
		}
		catch (FontFormatException|IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Construct a new button with no text and no image.
	 */
	public RButton() {
		this("");
	}

	/**
	 * Construct a new button with a given text and no image.
	 * @param text The text to display in the button.
	 */
	public RButton(String text) {
		this(text, Decoration.None);
	}

	/**
	 * Construct a new button with a given text and overlay image.
	 * @param text The text to display in the button.
	 * @param dec The type of image to display.
	 */
	public RButton(String text, Decoration dec) {
		super();
		decor = dec;
		switch (dec) {
		case Calc:
			decorImage = new ImageIcon(getClass().getResource("/images/buttons/calc.png"));
			break;
		case Close:
			decorImage = new ImageIcon(getClass().getResource("/images/buttons/cls.png"));
			break;
		case Arrow:
			decorImage = new ImageIcon(getClass().getResource("/images/buttons/arr.png"));
			break;
		default:
			break;
		}
		setIcon(new NineSlice("/images/buttons/enabled.amd"));
		setRolloverIcon(new NineSlice("/images/buttons/hover.amd"));
		setDisabledIcon(new NineSlice("/images/buttons/disabled.amd"));
		setPressedIcon(new NineSlice("/images/buttons/click.amd"));
		super.setRolloverEnabled(true);
		super.setFocusPainted(false);
		super.setContentAreaFilled(false);
		super.setBorderPainted(false);
		setLayout(null);
		textField = new JLabel();
		textField2 = new JLabel();
		String os = System.getProperty("os.name").toLowerCase();
		int leftbaseoffset;
		if (decor == Decoration.None) {
			textField.setHorizontalAlignment(SwingConstants.CENTER);
			textField2.setHorizontalAlignment(SwingConstants.CENTER);
			leftbaseoffset = 0;
		}
		else {
			textField.setHorizontalAlignment(SwingConstants.LEFT);
			textField2.setHorizontalAlignment(SwingConstants.LEFT);
			leftbaseoffset = 5;
			imageBox = new JLabel(decorImage);
			add(imageBox);
		}
		if (os.indexOf("win") >= 0) {
			textField.setBorder(BorderFactory.createEmptyBorder(2, 0 + leftbaseoffset, 0, 0));
			textField2.setBorder(BorderFactory.createEmptyBorder(3, 1 + leftbaseoffset, 0, 0));
		}
		else {
			textField.setBorder(BorderFactory.createEmptyBorder(1, 0 + leftbaseoffset, 0, 0));
			textField2.setBorder(BorderFactory.createEmptyBorder(4, 1 + leftbaseoffset, 0, 0));
		}
		Font f = new Font("COUTURE Bold", Font.BOLD, 12);
		if (f.canDisplay('A')) {
			textField.setFont(f);
			textField2.setFont(f);
		}
		textField.setForeground(Color.white);
		textField2.setForeground(Color.black);
		setText(text);
		textField.setBounds(0, 0, internalWidth, internalHeight);
		textField2.setBounds(0, 0, internalWidth, internalHeight);
		textField2.setVisible(USE_SHADOW);
		add(textField);
		add(textField2);
	}

	/**
	 * Find the best layout of the text across one or two lines.
	 * 
	 * @param text The new text that needs optimized for display.
	 */
	private void optimizeText(String text) {
		currentText = text;
		String words[] = text.toUpperCase().split(" ");
		FontMetrics metrics = textField.getFontMetrics(textField.getFont());
		int spacelength = metrics.stringWidth(" ");
		int wordlength[] = new int[words.length];
		for (int i = 0; i < words.length; i++) {
			wordlength[i] = metrics.stringWidth(words[i]);
		}
		int newsplit = (int)(((double)words.length) / 2.0);
		int diff = 0;
		int newdiff = Integer.MAX_VALUE;
		int split;
		int j;
		do {
			split = newsplit;
			diff = newdiff;
			int toplength = 0;
			int bottomlength = 0;
			if (diff != Integer.MAX_VALUE) {
				if (diff < 0)
					newsplit++;
				else if (diff > 0)
					newsplit--;
			}
			for (j = 0; j < newsplit; j++) {
				toplength += wordlength[j];
			}
			toplength += spacelength * (j - 1);
			for (; j < words.length; j++) {
				bottomlength += wordlength[j];
			}
			bottomlength += spacelength * (j - 1 - split);
			newdiff = toplength - bottomlength;
		} while (Math.abs(diff) > Math.abs(newdiff));
		String toptext = "";
		String bottomtext = "";
		int toplength = 0;
		int bottomlength = 0;
		for (j = 0; j < split; j++) {
			toptext += words[j];
			if (j != (split - 1))
				toptext += " ";
			toplength += wordlength[j];
		}
		toplength += spacelength * (j - 1);
		for (; j < words.length; j++) {
			bottomtext += words[j];
			if (j != (words.length - 1))
				bottomtext += " ";
			bottomlength += wordlength[j];
		}
		bottomlength += spacelength * (j - 1 - split);
		int maxlength = Math.max(toplength, bottomlength);
		if (preferredWidth > 0) {
			int user;
			if (decor == Decoration.None)
				user = preferredWidth - 40;
			else
				user = preferredWidth - 58;
			maxlength = Math.max(maxlength, user);
			if (decor == Decoration.None && maxlength == user)
				maxlength += 10;
		}
		textField.setText("<html><body style='width:" + maxlength + "px'><div style=\"text-align: center;\">" + toptext + "<br/>" + bottomtext);
		textField2.setText("<html><body style='width:" + maxlength + "px'><div style=\"text-align: center;\">" + toptext + "<br/>" + bottomtext);
		if (decor == Decoration.None) {
			if (preferredWidth > 0)
				internalWidth = maxlength + 30;
			else
				internalWidth = maxlength + 40;
		}
		else
			internalWidth = maxlength + 58;
		if (decor != Decoration.None) {
			int width = decorImage.getIconWidth();
			int height = decorImage.getIconHeight();
			int x = internalWidth - 11 - width;
			int y = (int)((((double)internalHeight) / 2.0) - (((double)height) / 2.0));
			imageBox.setBounds(x, y, width, height);
		}
		invalidate();
	}

	@Override
	public Insets getInsets() {
		return new Insets(0, 0, 0, 0);
	}
	
	@Override
	public Component.BaselineResizeBehavior getBaselineResizeBehavior() {
		return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
	}
	
	@Override
	public int getBaseline(int width, int height) {
		return 0;
	}

	@Override
	public Dimension getSize() {
		return new Dimension(internalWidth, internalHeight);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		textField2.setVisible(enabled && USE_SHADOW);
	}

	@Override
	public void setPreferredSize(Dimension dim) {
		preferredWidth = dim.width;
		optimizeText(currentText);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(internalWidth, internalHeight);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, internalWidth, internalHeight);
	}

	@Override
	public void setBounds(Rectangle rect) {
		preferredWidth = rect.width;
		optimizeText(currentText);
		super.setBounds(rect.x, rect.y, internalWidth, internalHeight);
	}

	@Override
	public void setMaximumSize(Dimension dim) {
		Dimension dim2 = new Dimension();
		dim2.width = Math.max(dim.width, internalWidth);
		dim2.height = Math.max(dim.height, internalHeight);
		super.setMinimumSize(dim2);
	}

	@Override
	public void setText(String text) {
		//remove diacritics
		String t = Normalizer.normalize(text, Normalizer.Form.NFD);
		//remove unsupported symbols
		t = t.replaceAll("[^\\p{ASCII}]", "");
		optimizeText(t);
	}

	@Override
	public void setRolloverEnabled(boolean val) { }
	@Override
	public void setFocusPainted(boolean val) { }
	@Override
	public void setContentAreaFilled(boolean val) { }
	@Override
	public void setBorderPainted(boolean val) { }
	@Override
	public void setSize(int width, int height) { }
	@Override
	public void setSize(Dimension dim) { }
	@Override
	public void setMinimumSize(Dimension dim) { }

	public static enum Decoration {
		Arrow,
		Close,
		Calc,
		None
	}
}
