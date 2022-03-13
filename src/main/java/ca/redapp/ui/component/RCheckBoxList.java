/***********************************************************************
 * REDapp - RCheckBoxList.java
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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * A list that can display checkboxes.
 * 
 * @param <T> The type of data
 */
public class RCheckBoxList extends JList<JCheckBox> {
	private static final long serialVersionUID = 1L;
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	private ArrayList<CheckedChangedListener> listeners = new ArrayList<RCheckBoxList.CheckedChangedListener>();
	
	public RCheckBoxList() {
		setCellRenderer(new RCheckBoxListCellRenderer());
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				
				if (index != -1) {
					if (e.getPoint().x < 16) {
						JCheckBox checkbox = (JCheckBox)getModel().getElementAt(index);
						checkbox.setSelected(!checkbox.isSelected());
						repaint();
						notifyCheckedChanged(index, checkbox.isSelected());
					}
				}
				else
					clearSelection();
			}
		});
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	public void addCheckedChangedListener(CheckedChangedListener listener) {
		listeners.add(listener);
	}
	
	private void notifyCheckedChanged(int index, boolean checked) {
		for (CheckedChangedListener listener : listeners) {
			listener.checkedChanged(index, checked);
		}
	}
	
	@Override
	public int locationToIndex(Point location) {
		int index = super.locationToIndex(location);
		if (index != -1 && !getCellBounds(index, index).contains(location))
			return -1;
		else return index;
	}
	
	protected class RCheckBoxListCellRenderer implements ListCellRenderer<JCheckBox> {
		@Override
		public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value, int index,
				boolean isSelected, boolean cellHasFocus) {
			JCheckBox checkbox = (JCheckBox)value;
			checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
			checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
			checkbox.setEnabled(isEnabled());
			checkbox.setFont(getFont());
			checkbox.setFocusPainted(false);
			checkbox.setBorderPainted(true);
			checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
			return checkbox;
		}
	}
	
	public static interface CheckedChangedListener {
		public abstract void checkedChanged(int index, boolean checked);
	}
}
