/***********************************************************************
 * REDapp - RCheckComboBox.java
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
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * A combobox that has check boxes in it. 
 * 
 * @param <T> The type of object that will be stored in the check boxes.
 */
public class RCheckComboBox<T> extends JComboBox<Object> {
	private static final long serialVersionUID = 1L;
	private List<ItemCheckBox> cbs;
	private Map<T, Boolean> mapItemSelected;
	private List<CheckComboBoxSelectionChangedListener> changedListeners = new ArrayList<CheckComboBoxSelectionChangedListener>();
	private String title = "";

	public RCheckComboBox(final Set<T> items, String title) {
		this(items, false, title);
	}

	public RCheckComboBox(final Set<T> items, boolean selected, String title) {
		this.title = title;
		resetItems(items, selected);
	}

	public RCheckComboBox(final Set<T> items, final Set<T> selected, String title) {
		this.title = title;
		mapItemSelected = new LinkedHashMap<T, Boolean>();
		for (T item : items) {
			mapItemSelected.put(item, selected.contains(item));
		}

		reset();
	}

	public RCheckComboBox(Map<T, Boolean> MapItemSelected, String title) {
		this.title = title;
		this.mapItemSelected = MapItemSelected;
		reset();
	}

	public void addSelectionChangedListener(
			CheckComboBoxSelectionChangedListener l) {
		if (l == null) {
			return;
		}
		changedListeners.add(l);
	}

	public void removeSelectionChangedListener(
			CheckComboBoxSelectionChangedListener l) {
		changedListeners.remove(l);
	}

	public void resetItems(final Set<T> items, boolean selected) {
		mapItemSelected = new LinkedHashMap<T, Boolean>();
		for (T item : items) {
			mapItemSelected.put(item, selected);
		}

		reset();
	}

	public Set<T> getSelectedItems() {
		Set<T> ret = new TreeSet<T>(); // alphabetically
		for (Map.Entry<T, Boolean> entry : mapItemSelected.entrySet()) {
			T item = entry.getKey();
			Boolean selected = entry.getValue();

			if (selected) {
				ret.add(item);
			}
		}

		if (ret.isEmpty())
			return null;

		return ret;
	}

	public int getSelectedCount() {
		Set<T> set = getSelectedItems();
		if (set == null)
			return 0;
		return set.size();
	}

	public void addSelectedItems(Collection<T> items) {
		if (items == null)
			return;

		for (T item : items) {
			if (mapItemSelected.containsKey(item)) {
				mapItemSelected.put(item, true);
			}
		}

		reset();
		repaint();
	}

	public void addSelectedItems(T[] items) {
		if (items == null)
			return;

		for (T item : items) {
			if (mapItemSelected.containsKey(item)) {
				mapItemSelected.put(item, true);
			}
		}

		reset();
		repaint();
	}

	private void reset() {
		this.removeAllItems();

		initCBs();

		this.addItem("");
		for (JCheckBox cb : cbs) {
			this.addItem(cb);
		}

		setRenderer(new CheckBoxRenderer<Object>(cbs));
		addActionListener(this);
	}

	private void initCBs() {
		cbs = new Vector<ItemCheckBox>();

		ItemCheckBox cb;
		for (Map.Entry<T, Boolean> entry : mapItemSelected.entrySet()) {
			T item = entry.getKey();
			Boolean selected = entry.getValue();

			cb = new ItemCheckBox(item);
			cb.setSelected(selected);
			cbs.add(cb);
		}
	}

	private void checkBoxSelectionChanged(int index) {
		int n = cbs.size();
		if (index < 0 || index >= n)
			return;

		if (index < n) {
			ItemCheckBox cb = cbs.get(index);
			if (cb.getItem() == null) {
				return;
			}

			if (cb.isSelected()) {
				cb.setSelected(false);
				mapItemSelected.put(cb.getItem(), false);

			} else {
				cb.setSelected(true);
				mapItemSelected.put(cb.getItem(), true);
			}
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int sel = getSelectedIndex();

		if (sel == 0) {
			getUI().setPopupVisible(this, false);
		} else if (sel > 0) {
			checkBoxSelectionChanged(sel - 1);
			for (CheckComboBoxSelectionChangedListener l : changedListeners) {
				l.selectionChanged(sel - 1);
			}
		}

		this.setSelectedIndex(-1); // clear selection
	}

	@Override
	public void setPopupVisible(boolean flag) {
		// No code here prevents the popup from closing
	}

	// checkbox renderer for combobox
	class CheckBoxRenderer<R> implements ListCellRenderer<R> {
		private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		private javax.swing.JSeparator separator;
		private final List<ItemCheckBox> cbs;

		public CheckBoxRenderer(final List<ItemCheckBox> cbs) {
			// setOpaque(true);
			this.cbs = cbs;
			// this.objs = objs;
			separator = new javax.swing.JSeparator(
					javax.swing.JSeparator.HORIZONTAL);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends R> list, R value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (index > 0 && index <= cbs.size()) {
				ItemCheckBox cb = cbs.get(index - 1);
				if (cb.getItem() == null) {
					return separator;
				}

				cb.setBackground(isSelected ? Color.blue : Color.white);
				cb.setForeground(isSelected ? Color.white : Color.black);

				return cb;
			}

			String str;
			Set<T> items = getSelectedItems();
			ArrayList<String> strs = new ArrayList<String>();
			if (items == null) {
				str = title;
			} else {
				for (Object obj : items) {
					strs.add(obj.toString());
				}
				str = strs.toString();
			}
			return defaultRenderer.getListCellRendererComponent(list, str,
					index, isSelected, cellHasFocus);
		}
	}

	class ItemCheckBox extends JCheckBox {
		private static final long serialVersionUID = 1L;
		private final T item;

		public ItemCheckBox(final T item) {
			super(item.toString());
			this.item = item;
		}

		public T getItem() {
			return item;
		}
	}

}

interface CheckComboBoxSelectionChangedListener extends java.util.EventListener {
	public void selectionChanged(int idx);
}
