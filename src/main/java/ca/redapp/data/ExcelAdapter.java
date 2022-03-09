/***********************************************************************
 * REDapp - ExcelAdapter.java
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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

/**
 * Allows pasting data into a table from Excel.
 * 
 * @author Travis Redpath
 *
 */
public class ExcelAdapter implements ActionListener {
	private String rowString, value;
	private Clipboard system;
	private JTable table;

	public ExcelAdapter(JTable MyTable) {
		table = MyTable;
		KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
		table.registerKeyboardAction(this, "Paste", paste, JComponent.WHEN_FOCUSED);
		system = Toolkit.getDefaultToolkit().getSystemClipboard();
	}

	public JTable getTable() { return table; }

	public void actionPerformed(ActionEvent e) {
		int StartRow = (table.getSelectedRows())[0];
		int StartCol = (table.getSelectedColumns())[0];
		if (StartCol == 0) {
			JOptionPane.showMessageDialog(null, "Cannot paste into hours column");
			return;
		}
		try {
			String trstring = (String)(system.getContents(this).getTransferData(DataFlavor.stringFlavor));
			StringTokenizer st1 = new StringTokenizer(trstring, "\n");
			for (int i = 0; st1.hasMoreTokens(); i++) {
				rowString = st1.nextToken();
				StringTokenizer st2 = new StringTokenizer(rowString, "\t");
				for (int j = 0; st2.hasMoreTokens(); j++) {
					value = (String)st2.nextToken();
					if (StartRow + i < table.getRowCount() && StartCol + j < table.getColumnCount())
						table.setValueAt(value, StartRow + i, StartCol + j);
				}
			}
		}
		catch (Exception ex) { }
	}
}
