/***********************************************************************
 * REDapp - ExportDialog.java
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

import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import ca.redapp.ui.component.RLabel;

public class ExportDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private ExportType type = ExportType.HourlyPrometheus;
	private int retval = JFileChooser.CANCEL_OPTION;
	private JButton btnExportHourly;
	private JButton btnExportDaily;
	private JButton btnCancel;
	private JRadioButton rdBtnTextFile;
	private JRadioButton rdBtnCsvFile;
	private JRadioButton rdBtnExcelFile;
	private JRadioButton rdBtnXmlFile;

	public ExportDialog(Window owner) {
		super(owner);
		setResizable(false);
		setModal(true);
		initialize();

		btnExportHourly.addActionListener((e) -> {
			retval = JFileChooser.APPROVE_OPTION;
			dispose();
		});
		btnExportDaily.addActionListener((e) -> {
			retval = JFileChooser.APPROVE_OPTION;
			type = ExportType.hourToDay(type);
			dispose();
		});
		btnCancel.addActionListener((e) -> {
			dispose();
		});
		rdBtnTextFile.addActionListener((e) -> {
			type = ExportType.HourlyPrometheus;
		});
		rdBtnCsvFile.addActionListener((e) -> {
			type = ExportType.HourlyCSV;
		});
		rdBtnExcelFile.addActionListener((e) -> {
			type = ExportType.HourlyXLS;
		});
		rdBtnXmlFile.addActionListener((e) -> {
			type = ExportType.HourlyXML;
		});
	}

	public int getResult() {
		return retval;
	}

	public ExportType getExportType() {
		return type;
	}

	private void initialize() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo_20.png")));
		setTitle(Main.resourceManager.getString("ui.dlg.title.export"));
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 481, 200);
		else
			setBounds(0, 0, 491, 205);
		getContentPane().setLayout(null);

		btnExportHourly = new JButton(Main.resourceManager.getString("ui.label.export.hourly"));
		btnExportHourly.setBounds(345, 140, 120, 23);
		getContentPane().add(btnExportHourly);

		btnExportDaily = new JButton(Main.resourceManager.getString("ui.label.export.daily"));
		btnExportDaily.setBounds(215, 140, 120, 23);
		getContentPane().add(btnExportDaily);

		btnCancel = new JButton(Main.resourceManager.getString("ui.label.export.cancel"));
		btnCancel.setBounds(130, 140, 75, 23);
		getContentPane().add(btnCancel);

		JPanel groupBox = new JPanel();
		groupBox.setBorder(new TitledBorder(null, Main.resourceManager.getString("ui.label.export.export.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		groupBox.setBounds(10, 10, 461, 121);
		groupBox.setLayout(null);
		getContentPane().add(groupBox);

		rdBtnTextFile = new JRadioButton(Main.resourceManager.getString("ui.label.file.txt"));
		rdBtnTextFile.setSelected(true);
		rdBtnTextFile.setBounds(10, 20, 141, 18);
		groupBox.add(rdBtnTextFile);

		rdBtnCsvFile = new JRadioButton(Main.resourceManager.getString("ui.label.file.csv"));
		rdBtnCsvFile.setBounds(10, 40, 141, 18);
		groupBox.add(rdBtnCsvFile);

		rdBtnExcelFile = new JRadioButton(Main.resourceManager.getString("ui.label.file.xlsx"));
		rdBtnExcelFile.setBounds(10, 59, 141, 18);
		groupBox.add(rdBtnExcelFile);

		rdBtnXmlFile = new JRadioButton(Main.resourceManager.getString("ui.label.file.xml"));
		rdBtnXmlFile.setBounds(10, 80, 141, 18);
		groupBox.add(rdBtnXmlFile);

		ButtonGroup group = new ButtonGroup();
		group.add(rdBtnTextFile);
		group.add(rdBtnCsvFile);
		group.add(rdBtnExcelFile);
		group.add(rdBtnXmlFile);

		RLabel lblASpace = new RLabel("- " + Main.resourceManager.getString("ui.label.export.export.text"));
		lblASpace.setBounds(160, 21, 291, 16);
		groupBox.add(lblASpace);

		RLabel lblAComma = new RLabel("- " + Main.resourceManager.getString("ui.label.export.export.csv"));
		lblAComma.setBounds(160, 41, 291, 16);
		groupBox.add(lblAComma);

		RLabel lblAnExcel = new RLabel("- " + Main.resourceManager.getString("ui.label.export.export.xls"));
		lblAnExcel.setBounds(160, 60, 291, 16);
		groupBox.add(lblAnExcel);

		RLabel lblAnXml = new RLabel("- " + Main.resourceManager.getString("ui.label.export.export.xml"));
		lblAnXml.setBounds(160, 81, 291, 16);
		groupBox.add(lblAnXml);
	}

	public static enum ExportType {
		HourlyPrometheus,
		HourlyCSV,
		HourlyXLS,
		HourlyXML,
		DailyPrometheus,
		DailyCSV,
		DailyXLS,
		DailyXML;

		public static ExportType hourToDay(ExportType type) {
			switch (type) {
			case HourlyCSV:
				return DailyCSV;
			case HourlyXLS:
				return DailyXLS;
			case HourlyXML:
				return DailyXML;
			default:
				return DailyPrometheus;
			}
		}

		public boolean isHourly() {
			switch (this) {
			case HourlyCSV:
			case HourlyPrometheus:
			case HourlyXLS:
			case HourlyXML:
				return true;
			default:
				return false;
			}
		}
	}
}
