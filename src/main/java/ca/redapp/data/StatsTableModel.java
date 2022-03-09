/***********************************************************************
 * REDapp - StatsTableModel.java
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

package ca.redapp.data;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.table.TableCellRenderer;

import ca.hss.times.WTime;
import ca.hss.times.WTimeSpan;
import ca.redapp.data.StatsDataType.DataTypeGroup;
import ca.redapp.ui.Launcher;
import ca.redapp.ui.Launcher.JavaVersion;
import ca.redapp.ui.Main;
import ca.redapp.ui.component.HeaderRenderer;
import ca.redapp.ui.component.RCheckBoxList;
import ca.redapp.ui.component.tree.CheckTreeManager;
import ca.redapp.ui.component.tree.Tribool;
import ca.redapp.ui.component.tree.CheckTreeManager.CheckStateListener;

/**
 * The model for displaying information in the statistics table.
 * 
 * @author Travis Redpath
 *
 */
public class StatsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	protected List<Column> m_HourData;
	protected List<Column> m_DayData;
	protected List<Column> m_NoonData;
	protected List<WTime> m_displayedDates;
	private Map<Integer, Integer> visibleColumns = new HashMap<Integer, Integer>();
	protected boolean m_dirty = true;
	protected DefaultListModel<JCheckBox> m_ListModel;
	protected DefaultListModel<JCheckBox> m_DayListModel;
	protected DefaultListModel<JCheckBox> m_NoonListModel;
	protected DefaultTreeModel m_TreeModel;
	protected DefaultTreeModel m_TreeModelDay;
	protected DefaultTreeModel m_TreeModelNoon;
	protected DefaultTableModel m_HeaderModel;
	private CheckTreeManager m_TreeManager;
	private CheckTreeManager m_DayTreeManager;
	private CheckTreeManager m_NoonTreeManager;
	private JTable m_view;
	private JTable m_header;
	private JTree m_TreeView;
	private JTree m_DayTreeView;
	private JTree m_NoonTreeView;
	private RCheckBoxList m_ListView;
	private Preferences m_settings;
	private ArrayList<StatsTableListener> listeners = new ArrayList<StatsTableModel.StatsTableListener>();
	private AtomicInteger rearanging = new AtomicInteger(0);
	private AtomicInteger modifyingTree = new AtomicInteger(0);
	private Map<Long, RowFlags> interpolatedValues = new HashMap<>();
	private HeaderRenderer headerRenderer;
	private DisplayType displaytype = DisplayType.HOURLY;

	/**
	 * Create a new table model with a specified table and row header table.
	 * 
	 * @param view the table that will display the data
	 * @param header the table that will display the row headers
	 */
	
	public StatsTableModel(JTable view, JTable header) {
		m_settings = Preferences.userRoot().node(this.getClass().getName());
		m_HourData = new ArrayList<>();
		m_DayData = new ArrayList<>();
		m_NoonData = new ArrayList<>();
		m_displayedDates = new ArrayList<>();
		m_ListModel = new DefaultListModel<>();
		m_DayListModel = new DefaultListModel<>();
		m_NoonListModel = new DefaultListModel<>();
		m_view = view;
		m_header = header;
		m_HeaderModel = new StatsHeaderTableModel(this);
		m_HeaderModel.addColumn(Main.resourceManager.getString("ui.label.stats.datetimeheader"));
		m_header.setModel(m_HeaderModel);
		headerRenderer = new HeaderRenderer();
		headerRenderer.setFont(new Font("Arial", Font.PLAIN, 12));
		m_header.getColumnModel().getColumn(0).setCellRenderer(headerRenderer);
	}
	
	public static class StatsHeaderTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		public final StatsTableModel parent;
		
		public StatsHeaderTableModel(StatsTableModel model) {
			parent = model;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
	
	public void setDisplayType(DisplayType type) {
		if (type == displaytype)
			return;
		displaytype = type;
		if (displaytype == DisplayType.HOURLY) {
			headerRenderer.setDateFormat(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_STRING_MM_DD_YYYY |
				WTime.FORMAT_TIME | WTime.FORMAT_DATE | WTime.FORMAT_ABBREV | WTime.FORMAT_EXCLUDE_SECONDS | WTime.FORMAT_WITHDST);
			m_TreeManager.enable();
			m_ListView.setModel(m_ListModel);
		}
		else if (displaytype == DisplayType.NOON) {
			headerRenderer.setDateFormat(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_STRING_MM_DD_YYYY |
					WTime.FORMAT_TIME | WTime.FORMAT_DATE | WTime.FORMAT_ABBREV | WTime.FORMAT_EXCLUDE_SECONDS | WTime.FORMAT_WITHDST);
			m_NoonTreeManager.enable();
			m_ListView.setModel(m_NoonListModel);
		}
		else {
			headerRenderer.setDateFormat(WTime.FORMAT_AS_LOCAL | WTime.FORMAT_STRING_MM_DD_YYYY |
				WTime.FORMAT_DATE | WTime.FORMAT_ABBREV | WTime.FORMAT_WITHDST);
			m_DayTreeManager.enable();
			m_ListView.setModel(m_DayListModel);
		}
		makeDirty();
	}
	
	public DisplayType getDisplayType() { return displaytype; }
	
	public class DefaultRenderer implements TableCellRenderer {
		DefaultTableCellRenderer cellRenderer;

		public DefaultRenderer() {
			JTable t = new JTable();
			cellRenderer = (DefaultTableCellRenderer)t.getDefaultRenderer(Object.class);
			cellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component com = cellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			boolean usegray = false;
			boolean usered = false;
			if (table.getModel() instanceof StatsHeaderTableModel) {
				if (((StatsHeaderTableModel)table.getModel()).parent.displaytype == DisplayType.HOURLY) {
					usegray = ((StatsHeaderTableModel)table.getModel()).parent.isInterpolated(StatsTableModel.this.getStartTime().add(new WTimeSpan(0, row, 0, 0))) || !((StatsHeaderTableModel)table.getModel()).parent.getColumn(column).cfbPossible();
                    usered = ((StatsHeaderTableModel)table.getModel()).parent.isCorrected(StatsTableModel.this.getStartTime().add(new WTimeSpan(0, row, 0, 0)));
				}
			}
			else if (table.getModel() instanceof StatsTableModel) {
				if (((StatsTableModel)table.getModel()).displaytype == DisplayType.HOURLY) {
					usegray = ((StatsTableModel)table.getModel()).isInterpolated(StatsTableModel.this.getStartTime().add(new WTimeSpan(0, row, 0, 0))) || !((StatsTableModel)table.getModel()).getColumn(column).cfbPossible();
				}
			}

			if (usegray)
				((JLabel)com).setForeground(Color.gray);
			else if (usered)
			    ((JLabel)com).setForeground(Color.red);
			else
				((JLabel)com).setForeground(Color.black);
			return com;
		}
	}

	/**
	 * Add a listener to custom events in the statistics table.
	 * 
	 * @param listener
	 */
	public void addStatsTableListener(StatsTableListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a listener to custom events in the statistics table.
	 * 
	 * @param listener
	 */
	public void removeStatsTableListener(StatsTableListener listener) {
		listeners.remove(listener);
	}

	private void notifyColumnCountChanged(int count) {
		for (StatsTableListener listener : listeners) {
			listener.numberOfColumnsChanged(count);
		}
	}
	
	public void clearFlags() {
		interpolatedValues.clear();
	}
	
	public boolean isInterpolated(WTime time) {
		if (interpolatedValues == null || displaytype != DisplayType.HOURLY)
			return false;
		Boolean b = interpolatedValues.containsKey(time.getTotalMicroSeconds());
		if (b) {
		    return interpolatedValues.get(time.getTotalMicroSeconds()).isInterpolated;
		}
		return false;
	}
    
    public boolean isCorrected(WTime time) {
        if (interpolatedValues == null || displaytype != DisplayType.HOURLY)
            return false;
        Boolean b = interpolatedValues.containsKey(time.getTotalMicroSeconds());
        if (b) {
            return interpolatedValues.get(time.getTotalMicroSeconds()).isCorrected;
        }
        return false;
    }
	
	public void makeInterpolated(WTime time) {
		interpolatedValues.computeIfAbsent(time.getTotalMicroSeconds(), x -> new RowFlags())
		    .isInterpolated = true;
	}
    
    public void makeCorrected(WTime time) {
        interpolatedValues.computeIfAbsent(time.getTotalMicroSeconds(), x -> new RowFlags())
            .isCorrected = true;
    }

	private JCheckBox createCheckBox(String text, StatsDataType type) {
		JCheckBox box = new JCheckBox();
		box.setText(text);
		box.setSelected(true);
		box.putClientProperty("DATATYPE", type);
		return box;
	}

	/**
	 * Get the time displayed in the currently selected row.
	 * 
	 * @return
	 */
	public WTime getSelectedRow() {
		int row = m_header.getSelectedRow();
		if (row < 0) {
			row = m_view.getSelectedRow();
			if (row < 0) {
				row = 0;
			}
		}
		Object o = m_header.getValueAt(row, 0);
		if (o instanceof WTime)
			return (WTime)o;
		return null;
	}

	/**
	 * Get a list of all of the header names.
	 * 
	 * @return A list of all of the header names.
	 */
	public List<String> headers() {
		List<String> retval = new ArrayList<String>();
		for (Column item : m_HourData) {
			retval.add(item.toString());
		}
		return retval;
	}

	/**
	 * Does a column of the given type exist in the table.
	 * 
	 * @param type
	 * @return
	 */
	public boolean columnExists(StatsDataType type) {
		for (Column item : m_HourData) {
			if (item.type == type)
				return true;
		}
		for (Column item : m_DayData) {
			if (item.type == type) {
				return true;
			}
		}
		for (Column item : m_NoonData) {
			if (item.type == type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Refresh the table.
	 */
	protected void makeDirty() {
		m_dirty = true;
		reset();
	}

	protected void reset() {
		fireTableDataChanged();
		fireTableStructureChanged();
		m_header.getColumnModel().getColumn(0).setPreferredWidth(135);
		m_header.setRowHeight(23);
		m_view.setRowHeight(23);
	}

	/**
	 * Get a list of all date/times currently displayed in the table.
	 * 
	 * @return
	 */
	public List<WTime> getDisplayedTimes() {
		if (m_dirty)
			clean();
		return Collections.unmodifiableList(m_displayedDates);
	}

	/**
	 * Get the first time in the table.
	 * 
	 * @return
	 */
	public WTime getStartTime() {
		if (m_dirty)
			clean();
		return new WTime(m_displayedDates.get(0));
	}

	/**
	 * Get the last time in the table.
	 * 
	 * @return
	 */
	public WTime getEndTime() {
		if (m_dirty)
			clean();
		return new WTime(m_displayedDates.get(m_displayedDates.size() - 1));
	}

	/**
	 * Get a column from its data type.
	 * 
	 * @param type
	 * @return The column or null if the column doesn't exist.
	 */
	public Column getColumn(StatsDataType type) {
		for (Column c : m_HourData) {
			if (c.type == type) {
				return c;
			}
		}
		for (Column c : m_DayData) {
			if (c.type == type) {
				return c;
			}
		}
		for (Column c : m_NoonData) {
			if (c.type == type) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Get the column at a specific index.
	 * 
	 * @param index The index of the column.
	 * @return The index of the column or null if it doesn't exist.
	 */
	public Column columnAt(int index) {
		return getColumn(index);
	}

	/**
	 * Get the number of visible headers.
	 * 
	 * @return
	 */
	public int visibleHeaderCount() {
		if (m_dirty)
			clean();
		return visibleColumns.size();
	}

	/**
	 * Adds a new column to the table.
	 * 
	 * @param newCol The column to add.
	 */
	private int addColumn(Column newCol) {
		rearanging.getAndIncrement();
		int numColumns = m_HourData.size();
		m_HourData.add(newCol);
		JCheckBox box = createCheckBox(newCol.title, newCol.type);
		box.setSelected(newCol.visible);
		m_ListModel.addElement(box);
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModel.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			DataTypeGroup group = (DataTypeGroup)node.getUserObject();
			if (group == newCol.type.dataTypeGroup()) {
				DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(newCol.type, false);
				node.add(newnode);
				m_TreeModel.nodesWereInserted(node, new int[] { node.getChildCount() - 1 });
				break;
			}
		}
		notifyColumnCountChanged(m_HourData.size() - 1);
		rearanging.getAndDecrement();
		return numColumns;
	}
	
	/**
	 * Adds a new column to the tables day data.
	 * 
	 * @param newCol The column to add.
	 * @return The number of columns.
	 */
	private int addDayColumn(Column newCol) {
		rearanging.getAndIncrement();
		int numColumns = m_DayData.size();
		m_DayData.add(newCol);
		JCheckBox box = createCheckBox(newCol.title, newCol.type);
		box.setSelected(newCol.visible);
		m_DayListModel.addElement(box);
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModelDay.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			DataTypeGroup group = (DataTypeGroup)node.getUserObject();
			if (group == newCol.type.dataTypeGroup()) {
				DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(newCol.type, false);
				node.add(newnode);
				m_TreeModelDay.nodesWereInserted(node, new int[] { node.getChildCount() - 1});
				break;
			}
		}
		notifyColumnCountChanged(m_DayData.size() - 1);
		rearanging.getAndDecrement();
		return numColumns;
	}
	
	/**
	 * Adds a new column to the tables noon data.
	 * 
	 * @param newCol The column to add.
	 * @return The number of columns.
	 */
	private int addNoonColumn(Column newCol) {
		rearanging.getAndIncrement();
		int numColumns = m_NoonData.size();
		m_NoonData.add(newCol);
		JCheckBox box = createCheckBox(newCol.title, newCol.type);
		box.setSelected(newCol.visible);
		m_NoonListModel.addElement(box);
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModelNoon.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			DataTypeGroup group = (DataTypeGroup)node.getUserObject();
			if (group == newCol.type.dataTypeGroup()) {
				DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(newCol.type, false);
				node.add(newnode);
				m_TreeModelNoon.nodesWereInserted(node, new int[] { node.getChildCount() - 1});
				break;
			}
		}
		notifyColumnCountChanged(m_NoonData.size() - 1);
		rearanging.getAndDecrement();
		return numColumns;
	}

	/**
	 * Adds a new column to the table.
	 * 
	 * @param newCol The column to add.
	 */
	private int insertColumn(int index, Column newCol) {
		int numColumns = m_HourData.size();
		int i = index;
		if (i > numColumns)
			i = numColumns;
		m_HourData.add(i, newCol);
		m_dirty = true;
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModel.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			DataTypeGroup group = (DataTypeGroup)node.getUserObject();
			if (group == newCol.type.dataTypeGroup()) {
				DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(newCol.type, false);
				node.add(newnode);
				m_TreeModel.nodesWereInserted(node, new int[] { node.getChildCount() - 1 });
				break;
			}
		}
		notifyColumnCountChanged(m_HourData.size() - 1);
		return i;
	}

	/**
	 * Adds a new column to the tables day data.
	 * 
	 * @param newCol The column to add.
	 */
	private int insertDayColumn(int index, Column newCol) {
		int numColumns = m_DayData.size();
		int i = index;
		if (i > numColumns)
			i = numColumns;
		m_DayData.add(i, newCol);
		m_dirty = true;
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModelDay.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			DataTypeGroup group = (DataTypeGroup)node.getUserObject();
			if (group == newCol.type.dataTypeGroup()) {
				DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(newCol.type, false);
				node.add(newnode);
				m_TreeModelDay.nodesWereInserted(node, new int[] { node.getChildCount() - 1 });
				break;
			}
		}
		notifyColumnCountChanged(m_DayData.size() - 1);
		return i;
	}

	/**
	 * Adds a new column to the tables noon data.
	 * 
	 * @param newCol The column to add.
	 */
	private int insertNoonColumn(int index, Column newCol) {
		int numColumns = m_NoonData.size();
		int i = index;
		if (i > numColumns)
			i = numColumns;
		m_NoonData.add(i, newCol);
		m_dirty = true;
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModelNoon.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			DataTypeGroup group = (DataTypeGroup)node.getUserObject();
			if (group == newCol.type.dataTypeGroup()) {
				DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(newCol.type, false);
				node.add(newnode);
				m_TreeModelNoon.nodesWereInserted(node, new int[] { node.getChildCount() - 1 });
				break;
			}
		}
		notifyColumnCountChanged(m_NoonData.size() - 1);
		return i;
	}

	/**
	 * Remove a column from the table.
	 * 
	 * @param index
	 */
	private void removeColumn(int index) {
		Column removed = m_HourData.remove(index);
		m_ListModel.removeElementAt(index);
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModel.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			DataTypeGroup group = (DataTypeGroup)node.getUserObject();
			if (group == removed.type.dataTypeGroup()) {
				for (int k = 0; k < node.getChildCount(); k++) {
					DefaultMutableTreeNode leaf = (DefaultMutableTreeNode)node.getChildAt(k);
					StatsDataType type = (StatsDataType)leaf.getUserObject();
					if (type == removed.type) {
						m_TreeManager.getSelectionModel().removeSelectionPath(new TreePath(leaf.getPath()));
						node.remove(leaf);
						m_TreeModel.nodesWereRemoved(node, new int[] { k }, new Object[] { leaf });
						break;
					}
				}
				break;
			}
		}
		makeDirty();
		notifyColumnCountChanged(m_HourData.size() - 1);
	}

	/**
	 * Remove a column from the daily table.
	 * 
	 * @param index
	 */
	private void removeDayColumn(int index) {
		Column removed = m_DayData.remove(index);
		m_DayListModel.removeElementAt(index);
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModelDay.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			DataTypeGroup group = (DataTypeGroup)node.getUserObject();
			if (group == removed.type.dataTypeGroup()) {
				for (int k = 0; k < node.getChildCount(); k++) {
					DefaultMutableTreeNode leaf = (DefaultMutableTreeNode)node.getChildAt(k);
					StatsDataType type = (StatsDataType)leaf.getUserObject();
					if (type == removed.type) {
						m_DayTreeManager.getSelectionModel().removeSelectionPath(new TreePath(leaf.getPath()));
						node.remove(leaf);
						m_TreeModelDay.nodesWereRemoved(node, new int[] { k }, new Object[] { leaf });
						break;
					}
				}
				break;
			}
		}
		makeDirty();
		notifyColumnCountChanged(m_DayData.size() - 1);
	}

	/**
	 * Remove a column from the noon table.
	 * 
	 * @param index
	 */
	private void removeNoonColumn(int index) {
		Column removed = m_NoonData.remove(index);
		m_NoonListModel.removeElementAt(index);
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModelNoon.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			DataTypeGroup group = (DataTypeGroup)node.getUserObject();
			if (group == removed.type.dataTypeGroup()) {
				for (int k = 0; k < node.getChildCount(); k++) {
					DefaultMutableTreeNode leaf = (DefaultMutableTreeNode)node.getChildAt(k);
					StatsDataType type = (StatsDataType)leaf.getUserObject();
					if (type == removed.type) {
						m_NoonTreeManager.getSelectionModel().removeSelectionPath(new TreePath(leaf.getPath()));
						node.remove(leaf);
						m_TreeModelNoon.nodesWereRemoved(node, new int[] { k }, new Object[] { leaf });
						break;
					}
				}
				break;
			}
		}
		makeDirty();
		notifyColumnCountChanged(m_NoonData.size() - 1);
	}
	
	private static void clearTreeModel(DefaultTreeModel model) {
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)model.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			int indices[] = new int[node.getChildCount()];
			Object children[] = new Object[node.getChildCount()];
			for (int k = 0; k < node.getChildCount(); k++) {
				indices[k] = k;
				children[k] = node.getChildAt(k);
			}
			node.removeAllChildren();
			model.nodesWereRemoved(node, indices, children);
		}
	}

	/**
	 * Remove all columns.
	 */
	public void clear() {
		m_HourData.clear();
		m_ListModel.clear();
		m_DayListModel.clear();
		m_NoonListModel.clear();
		m_DayData.clear();
		m_NoonData.clear();
		clearTreeModel(m_TreeModel);
		clearTreeModel(m_TreeModelDay);
		clearTreeModel(m_TreeModelNoon);
		makeDirty();
		notifyColumnCountChanged(m_HourData.size() - 1);
	}

	/**
	 * If a column for the type already exists the value at the given time will be replaced or added if it doesn't
	 * exist. If the column doesn't exist it will be added.
	 * 
	 * @param time
	 * @param value
	 * @param type
	 * @throws IllegalArgumentException
	 */
	public void addOrSetTypeValue(WTime time, Object value, StatsDataType type) throws IllegalArgumentException {
		if (type == StatsDataType.UNKNOWN)
			throw new IllegalArgumentException("The data type cannot be UNKNOWN");
		Column c = findColumn(type);
		if (c == null) {
			c = new Column(type);
		}
		c.putOrOverwrite(time, value);
		notifyColumnCountChanged(m_HourData.size() - 1);
	}

	/**
	 * If a column for the type already exists the value at the given time will be replaced or added if it doesn't
	 * exist. If the column doesn't exist it will be added.
	 * 
	 * @param time
	 * @param value
	 * @param type
	 * @throws IllegalArgumentException
	 */
	public void addOrSetTypeValueDay(WTime time, Object value, StatsDataType type) throws IllegalArgumentException {
		if (type == StatsDataType.UNKNOWN)
			throw new IllegalArgumentException("The data type cannot be UNKNOWN");
		Column c = findDayColumn(type);
		if (c == null) {
			c = new Column(type);
		}
		c.putOrOverwrite(time, value);
		notifyColumnCountChanged(m_DayData.size() - 1);
	}

	/**
	 * If a column for the type already exists the value at the given time will be replaced or added if it doesn't
	 * exist. If the column doesn't exist it will be added.
	 * 
	 * @param time
	 * @param value
	 * @param type
	 * @throws IllegalArgumentException
	 */
	public void addOrSetTypeValueNoon(WTime time, Object value, StatsDataType type) throws IllegalArgumentException {
		if (type == StatsDataType.UNKNOWN)
			throw new IllegalArgumentException("The data type cannot be UNKNOWN");
		Column c = findNoonColumn(type);
		if (c == null) {
			c = new Column(type);
		}
		c.putOrOverwrite(time, value);
		notifyColumnCountChanged(m_NoonData.size() - 1);
	}

	/**
	 * Call this after doing an import to reset the order of the columns.
	 */
	public void importComplete() {
		arrangeColumns();
		arrangeDayColumns();
		arrangeNoonColumns();
	}
	
	private void arrangeColumns() {
		rearanging.getAndIncrement();
		int prefVersion = m_settings.getInt("VERSION", -1);
		String o = null;
		if (prefVersion < 3) {
			o = "IMPORTTEMP,IMPORTDEWPOINT,IMPORTRH,IMPORWS,IMPORTWD,IMPORTPRECIP,FFMC,DMC,DC,ISI,BUI,FWI,DSR,HFFMC,HISI,HFWI,ROST,ROSEQ,HFI,CFB,SFC,CFC,TFC,RSO,FROS,BROS,CSI,FFI,BFI,DH,DF,DB,LB,FAREA,FPERIM,H_FBP_FMC,H_FBP_ISI,H_FBP_WSV,H_FBP_RAZ,H_SUNRISE,H_SOLAR_NOON,H_SUNSET";
			m_settings.put("ORDER", o);
			m_settings.putInt("VERSION", 3);
		}
		else if (prefVersion < 4) {
		    o = m_settings.get("ORDER", "IMPORTTEMP,IMPORTDEWPOINT,IMPORTRH,IMPORWS,IMPORTWD,IMPORTPRECIP,FFMC,DMC,DC,ISI,BUI,FWI,DSR,HFFMC,HISI,HFWI,ROST,ROSEQ,HFI,CFB,SFC,CFC,TFC,RSO,FROS,BROS,CSI,FFI,BFI,DH,DF,DB,LB,FAREA,FPERIM,H_FBP_FMC,H_FBP_ISI,H_FBP_WSV,H_FBP_RAZ,H_SUNRISE,H_SOLAR_NOON,H_SUNSET");
		    String[] split = o.split(",");
		    List<String> lsplit = Arrays.stream(split)
		            .filter(x -> !x.equals("DSR"))
		            .collect(Collectors.toList());
		    for (int i = 0; i < lsplit.size(); i++) {
		        if (lsplit.get(i).equals("FWI")) {
		            lsplit.add(i + 1, "DSR");
		        }
		    }
		    o = String.join(",", lsplit);
		    m_settings.putInt("VERSION", 4);
		    m_settings.put("ORDER", o);
		}
		else
			o = m_settings.get("ORDER", null);
		
		if (o != null) {
			String[] list = o.split(",");
			for (int i = 0; i < list.length; i++) {
				StatsDataType type = StatsDataType.fromSettingsString(list[i]);
				if (type == StatsDataType.UNKNOWN)
					continue;
				int index = -1;
				for (int j = 0; j < m_ListModel.size(); j++) {
					JCheckBox item = m_ListModel.get(j);
					StatsDataType t = (StatsDataType)item.getClientProperty("DATATYPE");
					if (t == type) {
						index = j;
						break;
					}
				}
				if (index < 0)
					continue;
				if (index != i) {
					JCheckBox cb = m_ListModel.remove(index);
					if (i >= m_ListModel.getSize())
						m_ListModel.addElement(cb);
					else
						m_ListModel.add(i, cb);
				}
			}
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < m_ListModel.size(); i++) {
				builder.append((m_ListModel.get(i)).getClientProperty("DATATYPE").toString());
				builder.append(",");
			}

			rearanging.getAndDecrement();
			setOrderFromList();
		}
		else {
			rearanging.getAndDecrement();
		}

		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModel.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			for (int k = 0; k < node.getChildCount(); k++) {
				DefaultMutableTreeNode leaf = (DefaultMutableTreeNode)node.getChildAt(k);
				StatsDataType type = (StatsDataType)leaf.getUserObject();
				Column c = getColumn(type);
				if (c != null && !c.visible) {
					m_TreeManager.getSelectionModel().removeSelectionPath(new TreePath(leaf.getPath()));
				}
			}
		}
	}
	
	private void arrangeDayColumns() {
		rearanging.getAndIncrement();
		String o = m_settings.get("ORDER_DAY", null);
		if (o != null) {
			String[] list = o.split(",");
			for (int i = 0; i < list.length; i++) {
				StatsDataType type = StatsDataType.fromSettingsString(list[i]);
				if (type == StatsDataType.UNKNOWN)
					continue;
				int index = -1;
				for (int j = 0; j < m_DayListModel.size(); j++) {
					JCheckBox item = m_DayListModel.get(j);
					StatsDataType t = (StatsDataType)item.getClientProperty("DATATYPE");
					if (t == type) {
						index = j;
						break;
					}
				}
				if (index < 0)
					continue;
				if (index != i) {
					JCheckBox cb = m_DayListModel.remove(index);
					m_DayListModel.insertElementAt(cb, i);
				}
			}
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < m_DayListModel.size(); i++) {
				builder.append((m_DayListModel.get(i)).getClientProperty("DATATYPE").toString());
				builder.append(",");
			}

			rearanging.getAndDecrement();
			setDayOrderFromList();
		}
		else {
			rearanging.getAndDecrement();
		}

		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModelDay.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			for (int k = 0; k < node.getChildCount(); k++) {
				DefaultMutableTreeNode leaf = (DefaultMutableTreeNode)node.getChildAt(k);
				StatsDataType type = (StatsDataType)leaf.getUserObject();
				Column c = getColumn(type);
				if (c != null && !c.visible) {
					m_DayTreeManager.getSelectionModel().removeSelectionPath(new TreePath(leaf.getPath()));
				}
			}
		}
	}
	
	private void arrangeNoonColumns() {
		rearanging.getAndIncrement();
		String o = m_settings.get("ORDER_NOON", null);
		if (o != null) {
			String[] list = o.split(",");
			for (int i = 0; i < list.length; i++) {
				StatsDataType type = StatsDataType.fromSettingsString(list[i]);
				if (type == StatsDataType.UNKNOWN)
					continue;
				int index = -1;
				for (int j = 0; j < m_NoonListModel.size(); j++) {
					JCheckBox item = m_NoonListModel.get(j);
					StatsDataType t = (StatsDataType)item.getClientProperty("DATATYPE");
					if (t == type) {
						index = j;
						break;
					}
				}
				if (index < 0)
					continue;
				if (index != i) {
					JCheckBox cb = m_NoonListModel.remove(index);
					m_NoonListModel.insertElementAt(cb, i);
				}
			}
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < m_NoonListModel.size(); i++) {
				builder.append((m_NoonListModel.get(i)).getClientProperty("DATATYPE").toString());
				builder.append(",");
			}

			rearanging.getAndDecrement();
			setDayOrderFromList();
		}
		else {
			rearanging.getAndDecrement();
		}

		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModelNoon.getRoot();
		for (int j = 0; j < rootNode.getChildCount(); j++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
			for (int k = 0; k < node.getChildCount(); k++) {
				DefaultMutableTreeNode leaf = (DefaultMutableTreeNode)node.getChildAt(k);
				StatsDataType type = (StatsDataType)leaf.getUserObject();
				Column c = getColumn(type);
				if (c != null && !c.visible) {
					m_NoonTreeManager.getSelectionModel().removeSelectionPath(new TreePath(leaf.getPath()));
				}
			}
		}
	}

	/**
	 * Removes a column and returns its value.
	 * 
	 * @param type The type of column to remove.
	 * @return If the column exists it will be removed from the view and returned. Otherwise null is returned.
	 */
	private Column takeColumn(StatsDataType type) {
		for (int i = 0; i < m_HourData.size(); i++) {
			if (m_HourData.get(i).type == type) {
				m_ListModel.removeElementAt(i);
				Column c = m_HourData.remove(i);
				c.oldColumn = i;
				return c;
			}
		}
		return null;
	}

	/**
	 * Removes a column and returns its value.
	 * 
	 * @param type The type of column to remove.
	 * @return If the column exists it will be removed from the view and returned. Otherwise null is returned.
	 */
	private Column takeDayColumn(StatsDataType type) {
		for (int i = 0; i < m_DayData.size(); i++) {
			if (m_DayData.get(i).type == type) {
				m_DayListModel.removeElementAt(i);
				Column c = m_DayData.remove(i);
				c.oldColumn = i;
				return c;
			}
		}
		return null;
	}

	/**
	 * Removes a column and returns its value.
	 * 
	 * @param type The type of column to remove.
	 * @return If the column exists it will be removed from the view and returned. Otherwise null is returned.
	 */
	private Column takeNoonColumn(StatsDataType type) {
		for (int i = 0; i < m_NoonData.size(); i++) {
			if (m_NoonData.get(i).type == type) {
				m_NoonListModel.removeElementAt(i);
				Column c = m_NoonData.remove(i);
				c.oldColumn = i;
				return c;
			}
		}
		return null;
	}

	/**
	 * If the column exists it will be returned, otherwise null is returned.
	 * 
	 * @param type
	 * @return
	 */
	private Column findColumn(StatsDataType type) {
		for (int i = 0; i < m_HourData.size(); i++) {
			if (m_HourData.get(i).type == type) {
				return m_HourData.get(i);
			}
		}
		return null;
	}

	/**
	 * If the column exists it will be returned, otherwise null is returned.
	 * 
	 * @param type
	 * @return
	 */
	private Column findDayColumn(StatsDataType type) {
		for (int i = 0; i < m_DayData.size(); i++) {
			if (m_DayData.get(i).type == type) {
				return m_DayData.get(i);
			}
		}
		return null;
	}

	/**
	 * If the column exists it will be returned, otherwise null is returned.
	 * 
	 * @param type
	 * @return
	 */
	private Column findNoonColumn(StatsDataType type) {
		for (int i = 0; i < m_NoonData.size(); i++) {
			if (m_NoonData.get(i).type == type) {
				return m_NoonData.get(i);
			}
		}
		return null;
	}

	private int getColumnIndex(Column c) {
		for (int i = 0; i < m_HourData.size(); i++) {
			if (m_HourData.get(i).id == c.id) {
				return i;
			}
		}
		return -1;
	}

	private int getDayColumnIndex(Column c) {
		for (int i = 0; i < m_DayData.size(); i++) {
			if (m_DayData.get(i).id == c.id) {
				return i;
			}
		}
		return -1;
	}

	private int getNoonColumnIndex(Column c) {
		for (int i = 0; i < m_NoonData.size(); i++) {
			if (m_NoonData.get(i).id == c.id) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get a list of all of the visible header names.
	 * 
	 * @return A list of all of the visible header names.
	 */
	public List<String> getVisibleHeaders() {
		List<String> retval = new ArrayList<String>();
		for (Column item : m_HourData) {
			if (item.isVisible())
				retval.add(item.toString());
		}
		return retval;
	}

	/**
	 * Get a list of all of the visible header names.
	 * 
	 * @return A list of all of the visible header names.
	 */
	public List<String> getVisibleDayHeaders() {
		List<String> retval = new ArrayList<String>();
		for (Column item : m_DayData) {
			if (item.isVisible())
				retval.add(item.toString());
		}
		return retval;
	}

	/**
	 * Get a list of all of the visible header names.
	 * 
	 * @return A list of all of the visible header names.
	 */
	public List<String> getVisibleNoonHeaders() {
		List<String> retval = new ArrayList<String>();
		for (Column item : m_NoonData) {
			if (item.isVisible())
				retval.add(item.toString());
		}
		return retval;
	}

	public List<Object> getVisibleColumns(WTime time) {
		List<Object> list = new ArrayList<Object>();
		for (Column item : m_HourData) {
			if (item.isVisible()) {
				Object val = item.rowAt(time);
				if (val != null)
					list.add(val);
			}
		}
		return list;
	}

	public List<Object> getVisibleDayColumns(WTime time) {
		List<Object> list = new ArrayList<Object>();
		for (Column item : m_DayData) {
			if (item.isVisible()) {
				Object val = item.rowAt(time);
				if (val != null)
					list.add(val);
			}
		}
		return list;
	}

	public List<Object> getVisibleNoonColumns(WTime time) {
		List<Object> list = new ArrayList<Object>();
		for (Column item : m_NoonData) {
			if (item.isVisible()) {
				Object val = item.rowAt(time);
				if (val != null)
					list.add(val);
			}
		}
		return list;
	}

	/**
	 * Redraw the table after the titles have been changed.
	 */
	protected void updateTitles() {
		reset();
	}

	/**
	 * Attach a list view to this table for modifying the visible columns and the order of the columns.
	 * 
	 * @param view The list view to attach to this model.
	 */
	public void attachListView(RCheckBoxList view) {
		m_ListView = view;
		m_ListView.setModel(m_ListModel);
		if (Launcher.javaVersion.major > JavaVersion.VERSION_8.major && !Launcher.activationAdded) {
			m_ListView.setDragEnabled(false);
		}
		else {
			m_ListView.setDragEnabled(true);
			m_ListView.setDropMode(DropMode.INSERT);
			m_ListView.setTransferHandler(new ListItemTransferHandler());
		}
		m_ListView.addCheckedChangedListener((i, c) -> itemCheckedChanged(i, c));
		m_ListModel.addListDataListener((ListDataListener)EventHandler.create(ListDataListener.class, this, "setOrderFromList"));
		m_DayListModel.addListDataListener((ListDataListener)EventHandler.create(ListDataListener.class, this, "setDayOrderFromList"));
		m_NoonListModel.addListDataListener((ListDataListener)EventHandler.create(ListDataListener.class, this, "setNoonOrderFromList"));
	}

	private void itemCheckedChanged(int index, boolean checked) {
		Column edited;
		if (displaytype == DisplayType.HOURLY)
			edited = m_HourData.get(index);
		else if (displaytype == DisplayType.NOON)
			edited = m_NoonData.get(index);
		else
			edited = m_DayData.get(index);
		edited.visible = checked;
		if (modifyingTree.compareAndSet(0, 0)) {
			if (displaytype == DisplayType.HOURLY) {
				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModel.getRoot();
				for (int j = 0; j < rootNode.getChildCount(); j++) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
					DataTypeGroup group = (DataTypeGroup)node.getUserObject();
					if (group == edited.type.dataTypeGroup()) {
						for (int k = 0; k < node.getChildCount(); k++) {
							DefaultMutableTreeNode leaf = (DefaultMutableTreeNode)node.getChildAt(k);
							StatsDataType type = (StatsDataType)leaf.getUserObject();
							if (type == edited.type) {
								if (checked)
									m_TreeManager.getSelectionModel().addSelectionPath(new TreePath(leaf.getPath()));
								else
									m_TreeManager.getSelectionModel().removeSelectionPath(new TreePath(leaf.getPath()));
								break;
							}
						}
						break;
					}
				}
			}
			else if (displaytype == DisplayType.NOON) {
				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModelNoon.getRoot();
				for (int j = 0; j < rootNode.getChildCount(); j++) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
					DataTypeGroup group = (DataTypeGroup)node.getUserObject();
					if (group == edited.type.dataTypeGroup()) {
						for (int k = 0; k < node.getChildCount(); k++) {
							DefaultMutableTreeNode leaf = (DefaultMutableTreeNode)node.getChildAt(k);
							StatsDataType type = (StatsDataType)leaf.getUserObject();
							if (type == edited.type) {
								if (checked)
									m_NoonTreeManager.getSelectionModel().addSelectionPath(new TreePath(leaf.getPath()));
								else
									m_NoonTreeManager.getSelectionModel().removeSelectionPath(new TreePath(leaf.getPath()));
								break;
							}
						}
						break;
					}
				}
			}
			else {
				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)m_TreeModelDay.getRoot();
				for (int j = 0; j < rootNode.getChildCount(); j++) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(j);
					DataTypeGroup group = (DataTypeGroup)node.getUserObject();
					if (group == edited.type.dataTypeGroup()) {
						for (int k = 0; k < node.getChildCount(); k++) {
							DefaultMutableTreeNode leaf = (DefaultMutableTreeNode)node.getChildAt(k);
							StatsDataType type = (StatsDataType)leaf.getUserObject();
							if (type == edited.type) {
								if (checked)
									m_DayTreeManager.getSelectionModel().addSelectionPath(new TreePath(leaf.getPath()));
								else
									m_DayTreeManager.getSelectionModel().removeSelectionPath(new TreePath(leaf.getPath()));
								break;
							}
						}
						break;
					}
				}
			}
		}
		makeDirty();
	}

	/**
	 * Set the tree view that allows the user to show/hide data types.
	 * 
	 * @param item
	 */
	public void setTableRoot(JTree item, JTree dayitem, JTree noonitem) {
		m_TreeView = item;
		m_DayTreeView = dayitem;
		m_NoonTreeView = noonitem;
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("ROOT", true);
		DefaultMutableTreeNode dayRootNode = new DefaultMutableTreeNode("ROOT", true);
		DefaultMutableTreeNode noonRootNode = new DefaultMutableTreeNode("ROOT", true);
		m_TreeModel = new DefaultTreeModel(rootNode);
		m_TreeModelDay = new DefaultTreeModel(dayRootNode);
		m_TreeModelNoon = new DefaultTreeModel(noonRootNode);
		m_TreeView.setModel(m_TreeModel);
		m_DayTreeView.setModel(m_TreeModelDay);
		m_NoonTreeView.setModel(m_TreeModelNoon);
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setClosedIcon(null);
		renderer.setOpenIcon(null);
		m_TreeView.setCellRenderer(renderer);
		m_DayTreeView.setCellRenderer(renderer);
		m_NoonTreeView.setCellRenderer(renderer);
		m_TreeManager = new CheckTreeManager(m_TreeView);
		m_TreeManager.getSelectionModel().addSelectionPath(new TreePath(rootNode.getPath()));
		m_TreeManager.addCheckStateListener(new CheckStateListener() {
			@Override
			public void checkChanged(TreePath path, Tribool checked) {
				if (displaytype == DisplayType.HOURLY) {
					Object o = path.getLastPathComponent();
					if (o.getClass() == DefaultMutableTreeNode.class) {
						Object data = ((DefaultMutableTreeNode)o).getUserObject();
						if (data.getClass() == StatsDataType.class) {
							StatsDataType type = (StatsDataType)data;
							for (int i = 0; i < m_ListModel.getSize(); i++) {
								JCheckBox box = (JCheckBox)m_ListModel.get(i);
								StatsDataType t = (StatsDataType)box.getClientProperty("DATATYPE");
								if (t == type) {
									box.setSelected(checked == Tribool.TRUE);
									Column edited = m_HourData.get(i);
									edited.visible = checked == Tribool.TRUE;
									makeDirty();
									break;
								}
							}
						}
						else if (data.getClass() == DataTypeGroup.class) {
							DataTypeGroup group = (DataTypeGroup)data;
							int numChanged = 0;
							for (int i = 0; i < m_ListModel.getSize(); i++) {
								JCheckBox box = (JCheckBox)m_ListModel.get(i);
								StatsDataType t = (StatsDataType)box.getClientProperty("DATATYPE");
								if (t.dataTypeGroup() == group) {
									box.setSelected(checked == Tribool.TRUE);
									Column edited = m_HourData.get(i);
									edited.visible = checked == Tribool.TRUE;
									numChanged++;
								}
							}
							if (numChanged > 0)
								makeDirty();
						}
					}
				}
			}
		});
		m_DayTreeManager = new CheckTreeManager(m_DayTreeView);
		m_DayTreeManager.getSelectionModel().addSelectionPath(new TreePath(dayRootNode.getPath()));
		m_DayTreeManager.addCheckStateListener(new CheckStateListener() {
			@Override
			public void checkChanged(TreePath path, Tribool checked) {
				if (displaytype == DisplayType.DAILY) {
					Object o = path.getLastPathComponent();
					if (o.getClass() == DefaultMutableTreeNode.class) {
						Object data = ((DefaultMutableTreeNode)o).getUserObject();
						if (data.getClass() == StatsDataType.class) {
							StatsDataType type = (StatsDataType)data;
							for (int i = 0; i < m_DayListModel.getSize(); i++) {
								JCheckBox box = (JCheckBox)m_DayListModel.get(i);
								StatsDataType t = (StatsDataType)box.getClientProperty("DATATYPE");
								if (t == type) {
									box.setSelected(checked == Tribool.TRUE);
									Column edited = m_DayData.get(i);
									edited.visible = checked == Tribool.TRUE;
									makeDirty();
									break;
								}
							}
						}
						else if (data.getClass() == DataTypeGroup.class) {
							DataTypeGroup group = (DataTypeGroup)data;
							int numChanged = 0;
							for (int i = 0; i < m_DayListModel.getSize(); i++) {
								JCheckBox box = (JCheckBox)m_DayListModel.get(i);
								StatsDataType t = (StatsDataType)box.getClientProperty("DATATYPE");
								if (t.dataTypeGroup() == group) {
									box.setSelected(checked == Tribool.TRUE);
									Column edited = m_DayData.get(i);
									edited.visible = checked == Tribool.TRUE;
									numChanged++;
								}
							}
							if (numChanged > 0)
								makeDirty();
						}
					}
				}
			}
		});
		m_NoonTreeManager = new CheckTreeManager(m_NoonTreeView);
		m_NoonTreeManager.getSelectionModel().addSelectionPath(new TreePath(noonRootNode.getPath()));
		m_NoonTreeManager.addCheckStateListener(new CheckStateListener() {
			@Override
			public void checkChanged(TreePath path, Tribool checked) {
				if (displaytype == DisplayType.NOON) {
					Object o = path.getLastPathComponent();
					if (o.getClass() == DefaultMutableTreeNode.class) {
						Object data = ((DefaultMutableTreeNode)o).getUserObject();
						if (data.getClass() == StatsDataType.class) {
							StatsDataType type = (StatsDataType)data;
							for (int i = 0; i < m_NoonListModel.getSize(); i++) {
								JCheckBox box = (JCheckBox)m_NoonListModel.get(i);
								StatsDataType t = (StatsDataType)box.getClientProperty("DATATYPE");
								if (t == type) {
									box.setSelected(checked == Tribool.TRUE);
									Column edited = m_NoonData.get(i);
									edited.visible = checked == Tribool.TRUE;
									makeDirty();
									break;
								}
							}
						}
						else if (data.getClass() == DataTypeGroup.class) {
							DataTypeGroup group = (DataTypeGroup)data;
							int numChanged = 0;
							for (int i = 0; i < m_NoonListModel.getSize(); i++) {
								JCheckBox box = (JCheckBox)m_NoonListModel.get(i);
								StatsDataType t = (StatsDataType)box.getClientProperty("DATATYPE");
								if (t.dataTypeGroup() == group) {
									box.setSelected(checked == Tribool.TRUE);
									Column edited = m_NoonData.get(i);
									edited.visible = checked == Tribool.TRUE;
									numChanged++;
								}
							}
							if (numChanged > 0)
								makeDirty();
						}
					}
				}
			}
		});
		for (DataTypeGroup group : DataTypeGroup.values()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(group, true);
			if (group.isDaily())
				dayRootNode.add(node);
			else if (group.isNoon())
				noonRootNode.add(node);
			else
				rootNode.add(node);
		}
		m_TreeView.expandRow(0);
		m_TreeView.setRootVisible(false);
		m_TreeView.setShowsRootHandles(true);
		m_DayTreeView.expandRow(0);
		m_DayTreeView.setRootVisible(false);
		m_DayTreeView.setShowsRootHandles(true);
		m_NoonTreeView.expandRow(0);
		m_NoonTreeView.setRootVisible(false);
		m_NoonTreeView.setShowsRootHandles(true);
	}

	/**
	 * Reset the order of the list from the last saved order.
	 */
	public void setOrderFromList() {
		if (rearanging.intValue() != 0)
			return;
		ArrayList<Column> newList = new ArrayList<Column>();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < m_ListModel.getSize(); i++) {
			JComponent c = (JComponent)m_ListModel.get(i);
			Object t = c.getClientProperty("DATATYPE");
			if (t != null && t.getClass() == StatsDataType.class) {
				StatsDataType type = (StatsDataType)t;
				newList.add(findColumn(type));
				builder.append(type.settingsString());
				builder.append(",");
			}
		}
		m_HourData = newList;
		makeDirty();
		String str = builder.toString();
		if (str.endsWith(","))
			str = str.substring(0, str.length() - 1);
		m_settings.put("ORDER", str);
	}

	/**
	 * Reset the order of the list from the last saved order.
	 */
	public void setDayOrderFromList() {
		if (rearanging.intValue() != 0)
			return;
		ArrayList<Column> newList = new ArrayList<Column>();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < m_DayListModel.getSize(); i++) {
			JComponent c = (JComponent)m_DayListModel.get(i);
			Object t = c.getClientProperty("DATATYPE");
			if (t != null && t.getClass() == StatsDataType.class) {
				StatsDataType type = (StatsDataType)t;
				newList.add(findDayColumn(type));
				builder.append(type.settingsString());
				builder.append(",");
			}
		}
		m_DayData = newList;
		makeDirty();
		String str = builder.toString();
		if (str.endsWith(","))
			str = str.substring(0, str.length() - 1);
		m_settings.put("ORDER_DAY", str);
	}

	/**
	 * Reset the order of the list from the last saved order.
	 */
	public void setNoonOrderFromList() {
		if (rearanging.intValue() != 0)
			return;
		ArrayList<Column> newList = new ArrayList<Column>();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < m_NoonListModel.getSize(); i++) {
			JComponent c = (JComponent)m_NoonListModel.get(i);
			Object t = c.getClientProperty("DATATYPE");
			if (t != null && t.getClass() == StatsDataType.class) {
				StatsDataType type = (StatsDataType)t;
				newList.add(findNoonColumn(type));
				builder.append(type.settingsString());
				builder.append(",");
			}
		}
		m_NoonData = newList;
		makeDirty();
		String str = builder.toString();
		if (str.endsWith(","))
			str = str.substring(0, str.length() - 1);
		m_settings.put("ORDER_NOON", str);
	}

    /**
     * Notifies all listeners that all cell values in the table's
     * rows may have changed. The number of rows may also have changed
     * and the <code>JTable</code> should redraw the
     * table from scratch. The structure of the table (as in the order of the
     * columns) is assumed to be the same.
     *
     * @see TableModelEvent
     * @see EventListenerList
     * @see javax.swing.JTable#tableChanged(TableModelEvent)
     */
    public void fireTableDataChanged() {
    	super.fireTableDataChanged();
    	m_HeaderModel.fireTableDataChanged();
    }

	/**
	 * Get the number of rows displayed in the table.
	 * 
	 * @return the number of rows displayed in the table
	 */
	@Override
	public int getRowCount() {
		if (m_dirty)
			clean();
		return m_displayedDates.size();
	}

	/**
	 * Get the number of columns displayed in the table.
	 * 
	 * @return the number of columns displayed in the table
	 */
	@Override
	public int getColumnCount() {
		if (m_dirty)
			clean();
		return visibleColumns.size();
	}

	/**
	 * Get the value at the given row and column indices.
	 * 
	 * @param rowIndex the row index of the requested data
	 * @param columnIndex the column index of the requested data
	 * @return the data in the requested row and column
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		WTime tm = timeVal(rowIndex);
		return getColumn(columnIndex).rowAt(tm);
	}

	/**
	 * Set the value at the given row and column indices.
	 * 
	 * @param rowIndex the row index to set the data at
	 * @param columnIndex the column index to set the data at
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		throw new RuntimeException("Not implemented");
	}

	/**
	 * Get the title of the specified column.
	 * 
	 * @return the title of the specified column
	 */
	@Override
	public String getColumnName(int col) {
		return getColumn(col).title;
	}

	/**
	 * Get the class of the data displayed in the specified column
	 * 
	 * @param the column index to get the title of
	 * @return the title of the specified column
	 */
	@Override
	public Class<?> getColumnClass(int col) {
		Object o = getValueAt(0, col);
		if (o == null)
			return Double.class;
		return o.getClass();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	protected WTime timeVal(int index) {
		return m_displayedDates.get(index);
	}

	private void clean() {
		WTime min = null;
		WTime max = null;
		m_dirty = false;
		buildColumnMap();
		List<Column> list;
		if (displaytype == DisplayType.HOURLY)
			list = m_HourData;
		else if (displaytype == DisplayType.NOON)
			list = m_NoonData;
		else
			list = m_DayData;
		for (Column c : list) {
			if (c.isVisible()) {
				WTime temp = c.firstDateTime();
				if (temp == null)
					continue;
				WTime first = new WTime(temp);
				temp = c.lastDateTime();
				if (temp == null)
					continue;
				WTime last = new WTime(temp);
				if (first != null && last != null) {
					if (min == null) {
						min = first;
						max = last;
					}
					else {
						if (WTime.lessThan(first, min))
							min = first;
						if (WTime.greaterThan(last, max))
							max = last;
					}
				}
			}
		}
		m_displayedDates.clear();
		m_HeaderModel.setRowCount(0);
		if (min != null && max != null) {
			int rowCount;
			long secs = WTime.subtract(max, min).getTotalSeconds();
			if (secs == 0)
				rowCount = 0;
			else
				rowCount = (int)(((double)secs) / 3600.0);
			rowCount++;
			for (int i = 0; i < rowCount; i++) {
				boolean hasData = false;
				for (int j = 0; j < list.size(); j++) {
					if (list.get(j).isVisible()) {
						if (list.get(j).rowAt(min) != null) {
							hasData = true;
							break;
						}
					}
				}
				if (hasData) {
					m_displayedDates.add(new WTime(min));
					m_HeaderModel.addRow(new Object[] { new WTime(min) });
				}
				min.add(new WTimeSpan(0, 1, 0, 0));
			}
		}
		m_HeaderModel.fireTableDataChanged();
	}

	private void buildColumnMap() {
		visibleColumns.clear();
		int counter = 0;
		List<Column> list;
		if (displaytype == DisplayType.HOURLY)
			list = m_HourData;
		else if (displaytype == DisplayType.NOON)
			list = m_NoonData;
		else
			list = m_DayData;
		for (int i = 0; i < list.size(); i++) {
			Column c = list.get(i);
			if (c.isVisible()) {
				visibleColumns.put(counter, i);
				counter++;
			}
		}
	}

	private Column getColumn(int index) {
		if (m_dirty)
			clean();
		if (displaytype == DisplayType.HOURLY)
			return m_HourData.get(visibleColumns.get(index));
		else if (displaytype == DisplayType.NOON)
			return m_NoonData.get(visibleColumns.get(index));
		else
			return m_DayData.get(visibleColumns.get(index));
	}

	public void redraw() {
		makeDirty();
	}

	/**
	 * A column of data to be displayed in the table.
	 */
	public class Column implements Comparable<Column> {
		private StatsDataType type = StatsDataType.UNKNOWN;
		private boolean editable = true;
		private String title;
		private TreeMap<WTime, Object> rows;
		private TreeMap<WTime, Object> userData;
		private Object udata;
		private int oldColumn = -1;
		private boolean visible = true;
		private UUID id = null;
		private DisplayType dispType;
		private boolean cfbPossible = true;
		
		/**
		 * If the column type already exists this column will replace it, otherwise a new column will be
		 * created and automatically added to the table.
		 * @param title The title of the column. This value must be unique to the table.
		 */
		public Column(StatsDataType type) {
			this(type, true);
		}
		
		public Column(StatsDataType type, boolean visible) {
			this(type, visible, DisplayType.HOURLY);
		}

		/**
		 * If the column type already exists this column will replace it, otherwise a new column will be
		 * created and automatically added to the table.
		 * @param title The title of the column. This value must be unique to the table.
		 * @param visible Whether or not the node should be visible.
		 */
		public Column(StatsDataType type, boolean visible, DisplayType dispType) {
			Column old;
			if (dispType == DisplayType.DAILY)
				old = takeDayColumn(type);
			else if (dispType == DisplayType.NOON)
				old = takeNoonColumn(type);
			else
				old = takeColumn(type);
			this.title = Main.resourceManager.getString(type.getResourceId());
			if (this.title.indexOf("<sub>") >= 0)
				this.title = "<html><body>" + this.title;
			this.type = type;
			rows = new TreeMap<WTime, Object>(new WTime.WTimeComparator());
			userData = new TreeMap<WTime, Object>(new WTime.WTimeComparator());
			udata = null;
			this.visible = visible;
			this.dispType = dispType;
			if (old == null) {
				id = UUID.randomUUID();
				if (dispType == DisplayType.DAILY)
					addDayColumn(this);
				else if (dispType == DisplayType.NOON)
					addNoonColumn(this);
				else
					addColumn(this);
			}
			else {
				id = old.id;
				if (dispType == DisplayType.DAILY)
					insertDayColumn(old.oldColumn, this);
				else if (dispType == DisplayType.NOON)
					insertNoonColumn(old.oldColumn, this);
				else
					insertColumn(old.oldColumn, this);
			}
		}

		/**
		 * The index of the column in the table.
		 * @return
		 */
		public int column() {
			if (dispType == DisplayType.DAILY)
				return getDayColumnIndex(this);
			else if (dispType == DisplayType.NOON)
				return getNoonColumnIndex(this);
			return getColumnIndex(this);
		}

		/**
		 * Enable or disable the ability for the user to edit this columns data.
		 * @param edit
		 */
		public void setEditable(boolean edit) {
			editable = edit;
		}

		/**
		 * Query whether or not this columns data is editable.
		 * @return
		 */
		public boolean getEditable() {
			return editable;
		}

		/**
		 * Set the columns user data.
		 * @param value
		 */
		public void setUserData(Object value) {
			udata = value;
		}

		/**
		 * Get the type of this column.
		 * @return
		 */
		public StatsDataType getType() {
			return type;
		}
		
		public DisplayType displayType() {
			return dispType;
		}

		/**
		 * Get the columns user data.
		 * @return
		 */
		public Object getUserData() {
			return udata;
		}

		/**
		 * Get the row for a given date/time.
		 * @param time
		 * @return
		 */
		public Object rowAt(WTime time) {
			return rows.get(time);
		}

		/**
		 * Get the number of rows in this column.
		 * @return
		 */
		public int rowCount() {
			return rows.size();
		}

		/**
		 * Get the lowest date/time value in this column.
		 * @return
		 */
		public WTime firstDateTime() {
			if (rows.size() == 0)
				return null;
			return rows.firstKey();
		}

		/**
		 * Get the highest date/time value in this column.
		 * @return
		 */
		public WTime lastDateTime() {
			if (rows.size() == 0)
				return null;
			return rows.lastKey();
		}

		/**
		 * Set the user data for the row at the specified date and time.
		 * @param dt
		 * @param value
		 * @throws RowDoesNotExistException
		 */
		public void setUserData(WTime dt, Object value) throws RowDoesNotExistException {
			if (rows.get(dt) == null)
				throw new RowDoesNotExistException(dt.toString());
			userData.put(dt, value);
		}

		/**
		 * Get the user data for the row at the specified date and time.
		 * @param dt
		 * @return
		 * @throws RowDoesNotExistException
		 */
		public Object getUserData(WTime dt) throws RowDoesNotExistException {
			Object o = userData.get(dt);
			if (o == null)
				throw new RowDoesNotExistException(dt.toString());
			return o;
		}

		/**
		 * Remove the row at the specified date and time.
		 * @param time
		 * @return
		 * @throws RowDoesNotExistException
		 */
		public Object remove(WTime time) throws RowDoesNotExistException {
			Object o = rows.get(time);
			if (o == null)
				throw new RowDoesNotExistException(time.toString());
			rows.remove(time);
			userData.remove(time);
			reset();
			return o;
		}

		/**
		 * Append a single row to the column.
		 * @param time The rows corresponding time.
		 * @param value
		 * @throws RowExistsException
		 */
		public void put(WTime time, Object value) throws RowExistsException {
			if (rows.get(time) != null)
				throw new RowExistsException(time.toString());
			WTime t2 = new WTime(time);
			t2.purgeToMinute(WTime.FORMAT_AS_LOCAL);
			rows.put(t2, value);
			makeDirty();
		}

		/**
		 * Append a single row to the column or overwrite the current value if the time already exists.
		 * If the row already exists any user data associated with it will also be removed.
		 * @param time The rows corresponding time.
		 * @param value
		 */
		public void putOrOverwrite(WTime time, Object value) {
			WTime t2 = new WTime(time);
			t2.purgeToHour(WTime.FORMAT_AS_LOCAL);
			if (rows.get(t2) != null)
				userData.remove(t2);
			rows.put(t2, value);
			makeDirty();
		}

		/**
		 * Add a set of data all at once. When this method is used the table must be manually
		 * reset in order for the data to be shown.
		 * @param list
		 */
		public void putOrOverwrite(Map<WTime, Object> list) {
			Map<WTime, Object> list2 = new TreeMap<WTime, Object>();
			Set<WTime> keys = list.keySet();
			Iterator<WTime> iter = keys.iterator();
			while (iter.hasNext()) {
				WTime dt = new WTime(iter.next());
				dt.purgeToMinute(WTime.FORMAT_AS_LOCAL);
				list2.put(dt, list.get(dt));
				userData.remove(dt);
			}
			rows.putAll(list2);
		}

		/**
		 * Remove all data from the column.
		 */
		public void clear() {
			rows.clear();
			userData.clear();
			makeDirty();
		}

		/**
		 * Is the column visible.
		 */
		public boolean isVisible() {
			return visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
			makeDirty();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj instanceof Column) {
				Column header = (Column)obj;
				if (type == header.type)
					return true;
				return false;
			}
			return super.equals(obj);
		}
		
		@Override
		public int hashCode() {
			assert false : "hashCode not designed";
			return 42;
		}

		/**
		 * Remove the column from the table.
		 */
		public void delete() {
			if (dispType == DisplayType.DAILY)
				StatsTableModel.this.removeDayColumn(column());
			else if (dispType == DisplayType.NOON)
				StatsTableModel.this.removeNoonColumn(column());
			else
				StatsTableModel.this.removeColumn(column());
		}

		@Override
		public int compareTo(Column o) {
			return title.compareTo(o.title);
		}

		@Override
		public String toString() {
			return title;
		}
		
		public void setCfbPossible(boolean cfb) {
			cfbPossible = cfb;
		}
		
		public boolean cfbPossible() {
			return cfbPossible;
		}
	}

	/**
	 * An interface for listening to different events that the
	 * stats table model will trigger.
	 *
	 * @author "Travis Redpath"
	 */
	public static interface StatsTableListener {
		/**
		 * The number of columns in the table has changed.
		 * @param count The new number of columns.
		 */
		public abstract void numberOfColumnsChanged(int count);

		/**
		 * A row has been changed.
		 * @param time The time corresponding to the row.
		 * @param c The column within the row that had its data changed.
		 */
		public abstract void rowChanged(WTime time, Column c);
	}

	public static class RowExistsException extends Exception {
		private static final long serialVersionUID = 1L;
		private String rowName;
		public RowExistsException(String row) {
			rowName = row;
		}

		@Override
		public String toString() {
			return "Row " + rowName + " already exists";
		}
	}

	public static class RowDoesNotExistException extends Exception {
		private static final long serialVersionUID = 1L;
		private String rowName;
		public RowDoesNotExistException(String row) {
			rowName = row;
		}

		@Override
		public String toString() {
			return "Row " + rowName + " doesn't exists";
		}
	}
	
	public static enum DisplayType {
		HOURLY,
		DAILY,
		NOON;
	}
	
	private static class RowFlags {
	    /**
	     * The data was generated by the interpolator, not directly imported.
	     */
	    public boolean isInterpolated = false;
	    
	    /**
	     * Data in the row was invalid and has been corrected by the importer.
	     */
	    public boolean isCorrected = false;
	}
}
