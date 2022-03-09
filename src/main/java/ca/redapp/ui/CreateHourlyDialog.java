/***********************************************************************
 * REDapp - CreateHourlyDialog.java
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

package ca.redapp.ui;

import static ca.hss.math.General.CARTESIAN_TO_COMPASS_DEGREE;
import static ca.hss.math.General.RADIAN_TO_DEGREE;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;

import ca.cwfgm.grid.IWXData;
import ca.redapp.data.DayModel;
import ca.redapp.data.ExcelAdapter;
import ca.redapp.ui.component.HeaderRenderer;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RLabel;

public class CreateHourlyDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private DayModel model;
	private boolean dateFixed;

	public CreateHourlyDialog(Window owner, Date date, boolean DateFixed, IWXData[] existing) {
		super(owner);
		this.dateFixed = DateFixed;
		initialize();
		spinnerDate.setValue(date);
		setDialogPosition(owner);
		int i = 0;
		for (IWXData d : existing) {
			model.setValueAt(d.temperature, i, 1);
			model.setValueAt(d.rh * 100, i, 2);
			model.setValueAt(d.windSpeed, i, 3);
			model.setValueAt(CARTESIAN_TO_COMPASS_DEGREE(RADIAN_TO_DEGREE(d.windDirection)), i, 4);
			model.setValueAt(d.precipitation, i, 5);
			i++;
		}
	}

	private void setDialogPosition(Window dlg) {
		if (dlg == null)
			return;
		int width = getWidth();
		int height = getHeight();
		int x = dlg.getX();
		int y = dlg.getY();
		int rwidth = dlg.getWidth();
		int rheight = dlg.getHeight();
		x = (int)(x + (rwidth / 2.0) - (width / 2.0));
		y = (int)(y + (rheight / 2.0) - (height / 2.0)) + 30;
		setLocation(x, y);
	}

	public IWXData[] getData() {
		return model.hours;
	}

	public Date getDate() {
		return (Date)spinnerDate.getValue();
	}

	private JSpinner spinnerDate;
	private JTable table;
	private RButton btnSave;
	private RButton btnCancel;

	private void initialize() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo_20.png")));
		setTitle(Main.resourceManager.getString("ui.dlg.title.create.hourly"));
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 600, 523);
		else
			setBounds(0, 0, 610, 528);
		getContentPane().setLayout(null);

		RLabel lblDate = new RLabel(Main.resourceManager.getString("ui.label.create.date"));
		lblDate.setHorizontalAlignment(SwingConstants.TRAILING);
		lblDate.setBounds(98, 10, 161, 16);
		getContentPane().add(lblDate);

		spinnerDate = new JSpinner();
		spinnerDate.setModel(new SpinnerDateModel(new Date(1389938400000L),
				null, null, Calendar.DAY_OF_YEAR));
		spinnerDate.setEditor(new JSpinner.DateEditor(spinnerDate,
				"MMMM d, yyyy"));
		spinnerDate.setBounds(269, 8, 140, 20);
		spinnerDate.setEnabled(!dateFixed);
		if (Main.isLinux()) {
			JComponent comp = spinnerDate.getEditor();
			if (comp instanceof JSpinner.DefaultEditor) {
				JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)comp;
				editor.getTextField().setFont(editor.getTextField().getFont().deriveFont(11.0f));
			}
		}
		getContentPane().add(spinnerDate);

		table = new JTable() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean editCellAt(int row, int column, java.util.EventObject e) {
				boolean result = super.editCellAt(row, column, e);
				final Component editor = getEditorComponent();
				if (editor == null || !(editor instanceof JTextComponent))
					return result;
				if (e instanceof KeyEvent)
					((JTextComponent)editor).selectAll();
				return result;
			}
		};
		model = new DayModel();
		table.setModel(model);
		table.setCellSelectionEnabled(true);
		table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
		((DefaultTableCellRenderer)table.getDefaultRenderer(String.class)).setHorizontalAlignment(SwingConstants.RIGHT);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setShowGrid(true);
		table.setGridColor(Color.black);
		Font f = new Font("Arial", Font.BOLD, 12);
		table.getTableHeader().setFont(f);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.getTableHeader().setReorderingAllowed(false);

		new ExcelAdapter(table);

		JScrollPane pane = new JScrollPane(table);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		pane.setBounds(3, 35, 588, 411);
		getContentPane().add(pane);

		JPanel panel1 = new JPanel();
		panel1.setBounds(0, 445, 590, 43);
		panel1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		panel1.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(panel1);

		btnSave = new RButton(Main.resourceManager.getString("ui.label.edit.save"));
		btnSave.setBounds(470, 450, 500, 41);
		btnSave.addActionListener((e) -> {
			setVisible(false);
			if (listener != null)
				listener.accepted();
		});
		panel1.add(btnSave);

		btnCancel = new RButton(Main.resourceManager.getString("ui.label.edit.cancel"));
		btnCancel.setBounds(340, 450, 121, 41);
		btnCancel.addActionListener((e) -> {
			dispose();
			if (listener != null)
				listener.cancelled();
		});
		panel1.add(btnCancel);
	}

	private CreateHourlyDialogListener listener = null;

	public void setCreateHourlyDialogListener(CreateHourlyDialogListener listener) {
		this.listener = listener;
	}

	public static abstract class CreateHourlyDialogListener {
		public abstract void accepted();
		public abstract void cancelled();
	}
}
