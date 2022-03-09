/***********************************************************************
 * REDapp - RCombobBox.java
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;

import javax.swing.JComboBox;

/**
 * A component that combines a button or editable field and a drop-down list. The user can select a value from
 * the drop-down list, which appears at the user's request.
 * 
 * Unlike a {@link javax.swing.JComboBox} this combo box can have a drop-down that is wider than the button
 * that triggers the drop-down to open.
 * 
 * @author Travis Redpath
 *
 * @param <T> the type of elements of this combo box.
 */
public class RComboBox<T> extends JComboBox<T> {
	private static final long serialVersionUID = 1L;
	private boolean layingOut = false;
	private int widestLength = 0;
	private boolean wide = false;

	/**
	 * Creates an RComboBox with a default data model. The default data model is an empty list of objects.
	 * Use <code>addItem</code> to add items. By default the first item in the data model become selected.
	 */
	public RComboBox() {
		super();
		super.setMinimumSize(new Dimension(0, 22));
		super.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
	}

	/**
	 * Is the drop-down set to show up wider than the button (if necessary).
	 * 
	 * @return if the drop-down can be wider than the button.
	 */
	public boolean isWide() { return wide; }

	/**
	 * Set whether to allow the drop-down list to be wider than the button if it needs to be. If
	 * the widest item in the list requires more width than the button would allow, the drop-down
	 * list will be wider.
	 * 
	 * @param wide whether the drop-down should be allowed to be wider than the button.
	 */
	public void setWide(boolean wide) { this.wide = wide; widestLength = getWidestItemWidth(); }

	private int getWidestItemWidth() {
		int numOfItems = this.getItemCount();
		Font font = this.getFont();
		FontMetrics metrics = this.getFontMetrics(font);
		int widest = 0;
		for (int i = 0; i < numOfItems; i++) {
			Object item = this.getItemAt(i);
			int lineWidth = metrics.stringWidth(item.toString());
			widest = Math.max(widest, lineWidth);
		}
		return widest + 15;
	}

	/**
	 * Get the width of the widest component.
	 * @return the width of the widest component.
	 */
	@Override
	public Dimension getSize() {
		Dimension dim = super.getSize();
		if (!layingOut && isWide())
			dim.width = Math.max(widestLength, dim.width);
		return dim;
	}

	/**
	 * Causes this container to lay out its components. Most programs should not call this
	 * method directly, but should invoke the <code>validate</code> method instead.
	 */
	@Override
	public void doLayout() {
		try {
			layingOut = true;
			super.doLayout();
		} finally {
			layingOut = false;
		}
	}

	/**
	 * Sets the minimum size of this component to a constant value. The height
	 * is fixed at 22 pixels and is not changeable.
	 */
	@Override
	public void setMinimumSize(Dimension miniumumSize) {
		super.setMinimumSize(new Dimension(miniumumSize.width, 22));
	}

	/**
	 * Sets the maximum size of this component to a constant value. The height
	 * is fixed at 22 pixels and is not changeable.
	 */
	@Override
	public void setMaximumSize(Dimension maxiumumSize) {
		super.setMaximumSize(new Dimension(maxiumumSize.width, 22));
	}

	/**
	 * Sets the preferred size of this component to a constant value. The height
	 * is fixed at 22 pixels and is not changeable.
	 */
	@Override
	public void setPreferredSize(Dimension preferredSize) {
		super.setPreferredSize(new Dimension(preferredSize.width, 22));
	}

	/**
	 * Moves and resizes this component. The new location of the top-left corner is specified
	 * by <code>x</code> and <code>y</code>, and the new size is specified by <code>width</code>
	 * and <code>height</code>. The height is fixed at 22 pixels and is not changeable.
	 * 
	 * @param x the new x-coordinate of this component
	 * @param y the new y-coordinate of this component
	 * @param width the new width of this component
	 * @param height ignored, the height is fixed at 22 pixels
	 */
	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, 22);
	}

	/**
	 * Moves and resizes this component to conform to the new bounding rectangle <code>r</code>.
	 * This component's new position is specified by <code>r.x</code> and <code>r.y</code>, and
	 * its new width is specified by <code>r.width</code>. The height is fixed at 22 pixels and is
	 * not changeable.
	 * 
	 * @param rect the new bounding rectangle for this component
	 */
	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect.x, rect.y, rect.width, 22);
	}
}
