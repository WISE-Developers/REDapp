/***********************************************************************
 * REDapp - RToggleButton.java
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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.JLabel;

/**
 * A two state button. There is more contrast between the selected
 * and unselected states with this toggle button than with
 * {@link javax.swing.JToggleButton}.
 * 
 * @author Travis
 *
 */
public class RToggleButton extends AbstractButton {
	private static final long serialVersionUID = 1L;
	private String label;
	private Font font = new JLabel().getFont();
	private Color topColor = new Color(174, 174, 174);
	private Color bottomColor = new Color(250, 250, 250);
	private Color disabledColor = new Color(210, 210, 210);
	private Color topColorSelected = new Color(156, 3, 3);
	private Color bottomColorSelected = new Color(224, 27, 28);
	private boolean isLeft = false;
	private boolean isRight = false;
	private boolean isMouseOver = false;
	private boolean stuck = false;
	
	public RToggleButton(String label) {
		this.label = label;
		setModel(new DefaultButtonModel());
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (isEnabled() && !stuck) {
					ButtonGroup group = ((DefaultButtonModel)getModel()).getGroup();
					if (group != null) {
						if (group instanceof RToggleButtonGroup)
							((RToggleButtonGroup)group).setSelected(RToggleButton.this, !isSelected());
						else
							group.setSelected(getModel(), !isSelected());
					}
					else
						setSelected(!isSelected());
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				if (isEnabled()) {
					isMouseOver = true;
					invalidate();
					repaint();
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				if (isEnabled()) {
					isMouseOver = false;
					invalidate();
					repaint();
				}
			}
		});
	}
	
	@Override
	public Font getFont() {
		return font;
	}
	
	public void setLeft(boolean left) {
		isLeft = left;
		invalidate();
	}
	
	public boolean isLeft() { return isLeft; }
	
	public void setRight(boolean right) {
		isRight = right;
		invalidate();
	}
	
	public boolean isRight() { return isRight; }
	
	public void setStuck(boolean stuck) {
		this.stuck = stuck;
		invalidate();
	}
	
	public boolean isStuck() { return stuck; }
	
	@Override
	protected void paintComponent(Graphics g) {
		double width = getWidth();
		double height = getHeight();
		Rectangle2D size = getFontMetrics(getFont()).getStringBounds(label, getGraphics());
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setFont(getFont());
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (isSelected() || stuck) {
			if (isMouseOver && !stuck)
				g2.setPaint(new GradientPaint(0, 0, bottomColorSelected, 0, (int)height, topColorSelected));
			else
				g2.setPaint(new GradientPaint(0, 0, topColorSelected, 0, (int)height, bottomColorSelected));
		}
		else {
			if (!isEnabled())
				g2.setPaint(disabledColor);
			else if (isMouseOver)
				g2.setPaint(new GradientPaint(0, 0, topColor, 0, (int)height, bottomColor));
			else
				g2.setPaint(new GradientPaint(0, 0, bottomColor, 0, (int)height, topColor));
		}
		
		if (isLeft || isRight) {
			g2.fillRoundRect(0, 0, (int)width, (int)height, 8, 8);
		
			if (isSelected() || stuck)
				g2.setPaint(topColorSelected);
			else
				g2.setPaint(topColor);
			
			g2.drawRoundRect(0, 0, (int)width - 1, (int)height - 1, 8, 8);
			
			if (isSelected() || stuck) {
				if (isMouseOver && !stuck)
					g2.setPaint(new GradientPaint(0, 0, bottomColorSelected, 0, (int)height, topColorSelected));
				else
					g2.setPaint(new GradientPaint(0, 0, topColorSelected, 0, (int)height, bottomColorSelected));
			}
			else {
				if (!isEnabled())
					g2.setPaint(disabledColor);
				else if (isMouseOver)
					g2.setPaint(new GradientPaint(0, 0, topColor, 0, (int)height, bottomColor));
				else
					g2.setPaint(new GradientPaint(0, 0, bottomColor, 0, (int)height, topColor));
			}
		}
		
		int x = 0;
		int widthSub = 0;
		if (isLeft) {
			x = 10;
			widthSub = 10;
		}
		if (isRight) {
			widthSub += 10;
		}
		g2.fillRect(x, 0, (int)width - widthSub, (int)height);
		
		if (isSelected() || stuck)
			g2.setPaint(topColorSelected);
		else
			g2.setPaint(topColor);
		if (!isLeft && !isRight)
			g2.drawRect(0, 0, (int)width - 1, (int)height - 1);
		else {
			if (isRight && isLeft) {
				g2.drawLine(10, 0, (int)width - 10, 0);
				g2.drawLine((int)width - 10, (int)height - 1, 10, (int)height - 1);
			}
			if (!isRight) {
				g2.drawLine(10, 0, (int)width - 1, 0);
				g2.drawLine((int)width - 1, 0, (int)width - 1, (int)height - 1);
				g2.drawLine((int)width - 1, (int)height - 1, 10, (int)height - 1);
			}
			if (!isLeft) {
				g2.drawLine((int)width - 10, 0, 0, 0);
				g2.drawLine(0, 0, 0, (int)height - 1);
				g2.drawLine(0, (int)height - 1, (int)width - 10, (int)height - 1);
			}
		}
		
		if (isSelected() || stuck) {
			g2.setColor(new Color(255, 255, 255));
		}
		else {
			if (isEnabled())
				g2.setColor(Color.black);
			else
				g2.setColor(new Color(120, 120, 120));
		}
		g2.drawString(label, (int)(width/2 - size.getWidth()/2), (int)(height/2 + 5));
	}
	
	public static class RToggleButtonGroup extends ButtonGroup {
		private static final long serialVersionUID = 1L;
		private List<AbstractButton> items = new ArrayList<>();
		
		@Override
		public void add(AbstractButton button) {
			items.add(button);
			button.getModel().setGroup(this);
		}

		@Override
		public void remove(AbstractButton button) {
			items.remove(button);
			button.getModel().setGroup(null);
		}
		
		public void setSelected(AbstractButton button, boolean selected) {
			int index = items.indexOf(button);
			if (index >= 0) {
				AbstractButton sel = items.remove(index);
				ListIterator<AbstractButton> iter = items.listIterator();
				while (iter.hasNext()) {
					AbstractButton b = iter.next();
					boolean s = b.isSelected();
					if (s) {
						b.setSelected(false);
						ActionListener[] listeners = b.getActionListeners();
						for (ActionListener l : listeners) {
							ActionEvent e = new ActionEvent(b,
                                      ActionEvent.ACTION_PERFORMED,
                                      b.getText());
							l.actionPerformed(e);
						}
					}
				}
				sel.setSelected(true);
				ActionEvent e = new ActionEvent(sel,
                        ActionEvent.ACTION_PERFORMED,
                        sel.getText());
				ActionListener[] listeners = sel.getActionListeners();
				for (ActionListener l : listeners) {
					l.actionPerformed(e);
				}
				items.add(sel);
			}
		}
	}
}
