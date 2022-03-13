/***********************************************************************
 * REDapp - HeaderRenderer.java
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
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import ca.hss.times.WTime;
import ca.redapp.data.StatsTableModel;
import ca.redapp.data.StatsTableModel.StatsHeaderTableModel;
import ca.redapp.ui.Main;

/**
 * Render a tables header. Explicit handling of WTime values to convert them to valid, human-readable strings.
 */
public class HeaderRenderer implements TableCellRenderer {
	DefaultTableCellRenderer headerRenderer;
	DefaultTableCellRenderer cellRenderer;
	Font font = null;
	private long dateformat;

	public HeaderRenderer() {
		JTable t = new JTable();
		headerRenderer = (DefaultTableCellRenderer)t.getTableHeader().getDefaultRenderer();
		cellRenderer = (DefaultTableCellRenderer)t.getDefaultRenderer(Object.class);
		headerRenderer.setHorizontalAlignment(JLabel.CENTER);
		dateformat = WTime.FORMAT_AS_LOCAL | WTime.FORMAT_STRING_MM_DD_YYYY |
				WTime.FORMAT_TIME | WTime.FORMAT_DATE | WTime.FORMAT_ABBREV | WTime.FORMAT_EXCLUDE_SECONDS | WTime.FORMAT_WITHDST;
	}
	
	public void setDateFormat(long format) {
		dateformat = format;
	}

	public void setFont(Font f) {
		font = f;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		if (value.getClass() == WTime.class) {
			String str = ((WTime)value).toString(dateformat);
			Component com;
			boolean selected = table.getSelectionModel().isSelectedIndex(row);
			if (selected) {
				com = cellRenderer.getTableCellRendererComponent(table, str, selected, hasFocus, row, column);
				((JLabel)com).setBorder(new EmptyBorder(0, 5, 0, 0));
				if (font != null)
					com.setFont(font.deriveFont(Font.BOLD));
				else
					com.setFont(com.getFont().deriveFont(Font.BOLD));
			}
			else {
				com = headerRenderer.getTableCellRendererComponent(table, str, selected, hasFocus, row, column);
				if (font != null)
					com.setFont(font.deriveFont(Font.PLAIN));
				else
					com.setFont(com.getFont().deriveFont(Font.PLAIN));
			}
			if (com instanceof JLabel) {
				((JLabel)com).setHorizontalAlignment(SwingConstants.LEFT);
				boolean usegray = false;
                boolean usered = false;
				if (table.getModel() instanceof StatsHeaderTableModel) {
					usegray = ((StatsHeaderTableModel)table.getModel()).parent.isInterpolated((WTime)value);
					usered = ((StatsHeaderTableModel)table.getModel()).parent.isCorrected((WTime)value);
				}
				else if (table.getModel() instanceof StatsTableModel) {
					usegray = ((StatsTableModel)table.getModel()).isInterpolated((WTime)value);
					usered = ((StatsTableModel)table.getModel()).isCorrected((WTime)value);
				}
				if (usegray) {
					((JLabel)com).setForeground(Color.gray);
					((JLabel)com).setToolTipText(Main.resourceManager.getString("ui.label.stats.warning.interpolated"));
				}
				else if (usered) {
				    ((JLabel)com).setForeground(Color.red);
                    ((JLabel)com).setToolTipText(Main.resourceManager.getString("ui.label.stats.warning.invalid"));
				}
				else {
					((JLabel) com).setToolTipText("");
				}
			}
			return com;
		}
		else
			return headerRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}
