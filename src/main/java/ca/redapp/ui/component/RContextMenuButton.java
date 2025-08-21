/***********************************************************************
 * REDapp - RContextMenuButton.java
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
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.Timer;

import ca.hss.general.Pair;
import ca.redapp.ui.Main;

/**
 * A button similar to {@link ca.redapp.ui.component.RButton} but with a drop-down menu
 * and an arrow to open the menu.
 * 
 * @author Travis Redpath
 *
 */
public class RContextMenuButton extends JButton implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	protected JLabel textField;
	protected JLabel textField2;
	private int internalWidth = 121;
	private static final int internalHeight = 41;
	private NineSlice imgLeftDisabled;
	private NineSlice imgRightDisabled;
	private NineSlice imgBase;
	private NineSlice imgLeftHover;
	private NineSlice imgRightHover;
	private NineSlice imgLeftClick;
	private NineSlice imgRightClick;
	private NineSlice imgLeftDisabledRightHover;
	private NineSlice imgLeftDisabledRightClick;
	private NineSlice imgRightDisabledLeftHover;
	private NineSlice imgRightDisabledLeftClick;
	private boolean mouseoverLeft = true;
	private ContextButtonClickListener listener = null;
	private String realText = null;
	private boolean leftDisabled = false;
	private boolean rightDisabled = false;
	private List<Pair<String, Object>> actions = new ArrayList<Pair<String, Object>>();
	private int preferredWidth = -1;

	/**
	 * Creates a new button with no text.
	 */
	public RContextMenuButton() {
		this("");
	}

	/**
	 * Creates a new button with the specified text.
	 * 
	 * @param text the text to display on the button
	 */
	public RContextMenuButton(String text) {
		super();
		imgBase = new NineSlice("/images/buttons/menu_arrow.amd");
		imgLeftDisabled = new NineSlice("/images/buttons/menu_arrow_disabled_left.amd");
		imgLeftDisabledRightHover = new NineSlice("/images/buttons/menu_arrow_disabled_left_hover.amd");
		imgLeftDisabledRightClick = new NineSlice("/images/buttons/menu_arrow_disabled_left_click.amd");
		imgRightDisabled = new NineSlice("/images/buttons/menu_arrow_drop_disabled.amd");
		imgRightDisabledLeftHover = new NineSlice("/images/buttons/menu_arrow_drop_disabled_hover.amd");
		imgRightDisabledLeftClick = new NineSlice("/images/buttons/menu_arrow_drop_disabled_click.amd");
		imgLeftHover = new NineSlice("/images/buttons/menu_arrow_left.amd");
		imgRightHover = new NineSlice("/images/buttons/menu_arrow_right.amd");
		imgLeftClick= new NineSlice("/images/buttons/menu_arrow_left_click.amd");
		imgRightClick = new NineSlice("/images/buttons/menu_arrow_right_click.amd");
		setIcon(imgBase);
		setRolloverIcon(imgLeftHover);
		setDisabledIcon(new NineSlice("/images/buttons/menu_arrow_disabled.amd"));
		setPressedIcon(imgLeftClick);
		super.setRolloverEnabled(true);
		super.setFocusPainted(false);
		super.setContentAreaFilled(false);
		super.setBorderPainted(false);
		setLayout(null);
		textField = new JLabel();
		textField2 = new JLabel();
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") >= 0) {
			textField.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
			textField2.setBorder(BorderFactory.createEmptyBorder(3, 6, 0, 0));
		}
		else {
			textField.setBorder(BorderFactory.createEmptyBorder(1, 5, 0, 0));
			textField2.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 0));
		}
		textField.setHorizontalAlignment(SwingConstants.LEFT);
		textField2.setHorizontalAlignment(SwingConstants.LEFT);
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
		textField2.setVisible(RButton.USE_SHADOW);
		add(textField);
		add(textField2);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/**
	 * Set the button enabled or disabled.
	 *
	 * @param enabled <code>true</code> for the button to be enabled.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		textField2.setVisible(enabled && RButton.USE_SHADOW);
	}

	/**
	 * Optimize the text for display across two lines.
	 * 
	 * @param text the text to optimize
	 */
	private void optimizeText(String text) {
		realText = text;
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
			user = preferredWidth - 58;
			maxlength = Math.max(maxlength, user);
		}
		//bypass a swing bug
		if (maxlength > 150) {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.indexOf("win") >= 0) {
				textField.setBorder(BorderFactory.createEmptyBorder(2, 15, 0, 0));
				textField2.setBorder(BorderFactory.createEmptyBorder(3, 16, 0, 0));
			}
			else {
				textField.setBorder(BorderFactory.createEmptyBorder(1, 15, 0, 0));
				textField2.setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0));
			}
			textField.setText("<html><body style='width:" + maxlength + "px'><div style=\"text-align: left;\">" + toptext + "<br/>" + bottomtext);
			textField2.setText("<html><body style='width:" + maxlength + "px'><div style=\"text-align: left;\">" + toptext + "<br/>" + bottomtext);
		}
		else {
			textField.setText("<html><body style='width:" + maxlength + "px'><div style=\"text-align: center;\">" + toptext + "<br/>" + bottomtext);
			textField2.setText("<html><body style='width:" + maxlength + "px'><div style=\"text-align: center;\">" + toptext + "<br/>" + bottomtext);
		}
		internalWidth = maxlength + 58;
		invalidate();
	}

	/**
	 * Set the main button enabled or disabled without effecting the drop-down button.
	 * 
	 * @param enabled <code>true</code> if the main button should be enabled.
	 */
	public void setLeftEnable(boolean enabled) {
		leftDisabled = !enabled;
		if (leftDisabled && rightDisabled) {
			setEnabled(false);
			leftDisabled = rightDisabled = false;
		}
		textField2.setVisible(enabled && RButton.USE_SHADOW);
		if (leftDisabled) {
			setIcon(imgLeftDisabled);
		}
		else {
			setIcon(imgBase);
		}
	}
	
	/**
	 * Disable the drop-down button without effecting the main button.
	 * 
	 * @param enabled <code>true</code> if the drop-down button should be enabled.
	 */
	public void setRightEnabled(boolean enabled) {
		rightDisabled = !enabled;
		if (leftDisabled && rightDisabled) {
			setEnabled(false);
			leftDisabled = rightDisabled = false;
		}
		if (rightDisabled)
			setIcon(imgRightDisabled);
		else
			setIcon(imgBase);
	}

	/**
	 * Add an item to the context menu. User data can be added to help identify items later.
	 * 
	 * @param title the text to be displayed in the menu item
	 * @param userData user data to help identify the menu item later
	 */
	public void addContextAction(String title, Object userData) {
		actions.add(new Pair<String, Object>(title, userData));
	}

	/**
	 * Add a collection of items to the context menu.
	 * 
	 * @param titles A list of title, userData pairs to add to the menu
	 */
	public void addContextActions(@SuppressWarnings("unchecked") Pair<String, Object> ... titles) {
		for (Pair<String, Object> s : titles)
			actions.add(new Pair<String, Object>(s.value1, s.value2));
	}

	/**
	 * Get the insets of the button (hard coded to none).
	 */
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

	/**
	 * Get the width and height of the button.
	 */
	@Override
	public Dimension getSize() {
		return new Dimension(internalWidth, internalHeight);
	}

	/**
	 * Set the preferred size of the button. Only the width is
	 * used as the height is a fixed value.
	 * 
	 * @param dim the new preferred dimensions of the button
	 */
	@Override
	public void setPreferredSize(Dimension dim) {
		preferredWidth = dim.width;
		optimizeText(realText);
	}

	/**
	 * Get the preferred size of the button.
	 * 
	 * @return the preferred size of the button
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(internalWidth, internalHeight);
	}

	/**
	 * Moves and resizes this component. The new location of the top-left corner is specified
	 * by <code>x</code> and <code>y</code>, and the new size is specified by <code>width</code>
	 * and <code>height</code>. The height is a fixed value and is not changeable.
	 * 
	 * @param x the new x-coordinate of this component
	 * @param y the new y-coordinate of this component
	 * @param width the new width of this component
	 * @param height ignored
	 */
	@Override
	public void setBounds(Rectangle rect) {
		preferredWidth = rect.width;
		optimizeText(realText);
		super.setBounds(rect.x, rect.y, internalWidth, internalHeight);
	}

	/**
	 * Moves and resizes this component to conform to the new bounding rectangle <code>r</code>.
	 * This component's new position is specified by <code>r.x</code> and <code>r.y</code>, and
	 * its new width is specified by <code>r.width</code>. The height is a fixed value and is
	 * not changeable.
	 * 
	 * @param rect the new bounding rectangle for this component
	 */
	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, internalWidth, internalHeight);
	}

	/**
	 * Sets the maximum size of this component to a constant value. The height
	 * is a fixed value and is not changeable.
	 */
	@Override
	public void setMaximumSize(Dimension dim) {
		Dimension dim2 = new Dimension();
		dim2.width = Math.max(dim.width, internalWidth);
		dim2.height = Math.max(dim.height, internalHeight);
		super.setMinimumSize(dim2);
	}

	/**
	 * Set the text displayed in the button.
	 * 
	 * @param text the new text to display in the button
	 */
	@Override
	public void setText(String text) {
		String t = Normalizer.normalize(text, Normalizer.Form.NFD);
		t = t.replaceAll("[^\\p{ASCII}]", "");
		optimizeText(t);
	}

	public String getRealText() {
		return realText;
	}

	//stop the user from setting some things that could hinder functionality.
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

	@Override
	public void mouseClicked(MouseEvent e) {
		if (listener != null && isEnabled()) {
			if (mouseoverLeft && !leftDisabled)
				listener.clicked();
			else if (!mouseoverLeft && actions.size() > 0 && !rightDisabled) {
				RPopupMenu menu = new RPopupMenu();
				for (int i = 0; i < actions.size(); i++) {
					JMenuItem item = new RMenuItem(actions.get(i).value1);
					item.putClientProperty("listIndex", (Integer)i);
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (listener != null) {
								JMenuItem item = (JMenuItem)e.getSource();
								int index = (Integer)item.getClientProperty("listIndex");
                                try {
                                    listener.contextActionClicked(actions.get(index).value1, actions.get(index).value2);
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
						}
					});
					menu.add(item);
				}
				menu.show(this);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) { }

	@Override
	public void mouseReleased(MouseEvent e) { }
	
	private void updateMousePosition(Point pt) {
		if (pt.x < (internalWidth - 33)) {
			if (leftDisabled) {
				setRolloverIcon(imgLeftDisabled);
				setPressedIcon(imgLeftDisabled);
			}
			else if (rightDisabled) {
				setRolloverIcon(imgRightDisabledLeftHover);
				setPressedIcon(imgRightDisabledLeftClick);
			}
			else {
				setRolloverIcon(imgLeftHover);
				setPressedIcon(imgLeftClick);
			}
			mouseoverLeft = true;
		}
		else {
			if (leftDisabled) {
				setRolloverIcon(imgLeftDisabledRightHover);
				setPressedIcon(imgLeftDisabledRightClick);
			}
			else if (rightDisabled) {
				setRolloverIcon(imgRightDisabled);
				setPressedIcon(imgRightDisabled);
			}
			else {
				setRolloverIcon(imgRightHover);
				setPressedIcon(imgRightClick);
			}
			mouseoverLeft = false;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		updateMousePosition(e.getPoint());
	}

	@Override
	public void mouseExited(MouseEvent e) { }

	@Override
	public void mouseDragged(MouseEvent e) { }

	@Override
	public void mouseMoved(MouseEvent e) {
		updateMousePosition(e.getPoint());
	}

	public void setContextButtonClickListener(ContextButtonClickListener listener) {
		this.listener = listener;
	}

	public static abstract class ContextButtonClickListener {
		public abstract void clicked();
		public abstract void contextActionClicked(String title, Object userData) throws IOException;
	}

	/**
	 * The context menu that will be displayed when the user pressed the drop-down button.
	 * 
	 * @author Travis Redpath
	 */
	protected class RPopupMenu extends JPopupMenu implements ActionListener {
		private static final long serialVersionUID = 1L;
		private static final int ITEM_HEIGHT = 25;
		private static final int SPEED = 7;
		private static final boolean DISABLE_ANIMATION = true;
		private int height = 0;
		private int count = 0;
		private Timer timer = new Timer(50, this);
		private boolean collapse = false;
		private int width = 110;

		public RPopupMenu() {
			setBorder(new LineBorder(Color.black, 1));
		}

		private void determineWidth() {
			int maxwidth = 0;
			for (int i = 0; i < getComponentCount(); i++) {
				Dimension size = ((RMenuItem)getComponent(i)).textSize();
				if (size.width > maxwidth)
					maxwidth = size.width;
			}
			maxwidth = Math.max(maxwidth, RContextMenuButton.this.internalWidth - 39);
			if ((maxwidth + 30) > width) {
				width = maxwidth + 30;
				for (int i = 0; i < getComponentCount(); i++) {
					((RMenuItem)getComponent(i)).updateWidth(width);
				}
			}
		}

		@Override
		public JMenuItem add(JMenuItem menuItem) {
			height += ITEM_HEIGHT;
			count++;
			JMenuItem item = super.add(menuItem);
			item.setPreferredSize(new Dimension(item.getPreferredSize().width, 0));
			return item;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(width, super.getPreferredSize().height);
		}

		@SuppressWarnings("unused")
		public void show(RContextMenuButton parent) {
			determineWidth();
			((RMenuItem)getComponent(getComponentCount() - 1)).removeBorder();
			if (Main.isMac() || width > 110 || DISABLE_ANIMATION)
				show(parent, 5, parent.getSize().height - 4);
			else {
				pack();
				super.show(parent, 5, parent.getSize().height - 4);
				timer.start();
			}
		}

		@Override
		public void show(Component parent, int x, int y) {
			determineWidth();
			for (int i = 0; i < getComponentCount(); i++) {
				Component c = getComponent(i);
				c.setPreferredSize(new Dimension(c.getPreferredSize().width, 25));
				c.invalidate();
			}
			pack();
			super.show(parent, x, y);
		}

		@SuppressWarnings("unused")
		@Override
		public void setVisible(boolean visible) {
			if (visible || Main.isMac() || width > 110 || DISABLE_ANIMATION) {
				timer.stop();
				super.setVisible(visible);
			}
			else {
				collapse = true;
				timer = new Timer(50, this);
				timer.start();
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Dimension size = getPreferredSize();
			if (collapse) {
				size.height = Math.max(size.height - (count * SPEED), 0);
				if (size.height == 0) {
					timer.stop();
					super.setVisible(false);
				}
			}
			else {
				size.height = Math.min(size.height + (count * SPEED), height);
				if (size.height == height) {
					timer.stop();
				}
			}
			for (int i = 0; i < getComponentCount(); i++) {
				Component c = getComponent(i);
				c.setPreferredSize(new Dimension(c.getPreferredSize().width, (int)(((double)size.height) / ((double)(count)))));
				c.invalidate();
			}
			pack();
		}
	}

	protected enum MouseState {
		Inside,
		Pressed,
		Outside
	}

	/**
	 * An item that is displayed in the context menu.
	 * 
	 * @author Travis Redpath
	 *
	 */
	protected static class RMenuItem extends JMenuItem implements MouseListener {
		private static final long serialVersionUID = 1L;
		private BufferedImage background = null;
		private boolean dirty = true;
		private MouseState state = MouseState.Outside;
		private boolean border = true;
		private int width = 110;
		private int y;
		private int fontSize;
		private Font f;
		private BufferedImage insideGradient;
		private BufferedImage pressedGradient;
		private BufferedImage outsideGradient;

		public RMenuItem(String text) {
			super(text);
			addMouseListener(this);
			if (Main.isLinux())
				fontSize = 13;
			else
				fontSize = 14;
			f = new Font("Arial", Font.BOLD, fontSize);
			insideGradient = getGradientCubesImage(width, 25, new Color(157, 3, 4), new Color(226, 27, 28));
			pressedGradient = getGradientCubesImage(width, 25, new Color(127, 3, 4), new Color(244, 40, 41));
			outsideGradient = getGradientCubesImage(width, 25, new Color(226, 28, 27), new Color(156, 3, 3));
		}

		protected void removeBorder() {
			border = false;
		}

		protected Dimension textSize() {
			FontMetrics metrics = getFontMetrics(f);
			Dimension retval = new Dimension();
			retval.width = metrics.stringWidth(this.getText());
			retval.height = metrics.getHeight();
			return retval;
		}

		protected void updateWidth(int width) {
			this.width = width;
			insideGradient = getGradientCubesImage(width, 25, new Color(157, 3, 4), new Color(226, 27, 28));
			pressedGradient = getGradientCubesImage(width, 25, new Color(127, 3, 4), new Color(244, 40, 41));
			outsideGradient = getGradientCubesImage(width, 25, new Color(226, 28, 27), new Color(156, 3, 3));
		}

		public BufferedImage getGradientCubesImage(int width, int height, Color topColor, Color bottomColor) {
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			   RenderingHints.VALUE_ANTIALIAS_ON);

			GradientPaint gradient = new GradientPaint(0, 0,
			   topColor, 0, height, bottomColor);
			graphics.setPaint(gradient);
			graphics.fillRect(0, 0, width, height);
			return image;
		}

		@Override
		protected final void paintComponent(Graphics g) {
			if (dirty) {
				unDirty(g);
			}
			Graphics2D graphics = (Graphics2D)g;
			graphics.drawImage(background, 0, 0, null);
			graphics.setFont(f);
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			graphics.setColor(Color.black);
			graphics.drawString(this.getText(), 16, y + 1);
			graphics.setColor(Color.white);
			graphics.drawString(this.getText(), 15, y);
			if (border) {
				graphics.setColor(Color.black);
				graphics.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			state = MouseState.Inside;
			dirty = true;
			invalidate();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			state = MouseState.Pressed;
			dirty = true;
			invalidate();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			state = MouseState.Inside;
			dirty = true;
			invalidate();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			state = MouseState.Inside;
			dirty = true;
			invalidate();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			state = MouseState.Outside;
			dirty = true;
			invalidate();
		}

		private void unDirty(Graphics graphics) {
			switch (state) {
			case Inside:
				background = insideGradient;
				break;
			case Pressed:
				background = pressedGradient;
				break;
			default:
				background = outsideGradient;
				break;
			}
			if (Main.isLinux())
				y = (int)(graphics.getFontMetrics().getLineMetrics(this.getText(), graphics).getHeight());
			else
				y = (int)(graphics.getFontMetrics().getLineMetrics(this.getText(), graphics).getHeight()) + 2;
			dirty = false;
		}

		@Override
		public void setSize(Dimension d) {
			//dirty = true;
			super.setSize(d);
		}

		@Override
		public void setPreferredSize(Dimension d) {
			//dirty = true;
			super.setPreferredSize(d);
		}
	}
}
