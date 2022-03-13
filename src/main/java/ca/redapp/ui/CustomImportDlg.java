/***********************************************************************
 * REDapp - CustomImportDlg.java
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

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RLabel;

public class CustomImportDlg extends JDialog {
	private static final long serialVersionUID = 1L;
	private JTable tableView;
	private JTable tableView_2;
	private RButton btnImport;
	private RButton btnCancel;
	private JComboBox<String> cmbTmFormat;
	private JComboBox<String> cmdDtFormat;
	private RLabel lblTmFormat;
	private RLabel lblDtFormat;

	private ImportTableModel model;
	private DefaultTableModel typeModel;
	private File file;
	private ArrayList<Integer> ignoreList = new ArrayList<Integer>();
	private Map<IMPORT_TYPE, Integer> typeMap = new HashMap<IMPORT_TYPE, Integer>();
	private boolean hourly = true;
	private int numElements = -1;
	private boolean skipEmpty = true;
	private String delimiter = "";
	private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
	private final int NON_TIME_DATE_FORMAT_COUNT;
	private String temporaryFile = "";
	private int retval = JFileChooser.CANCEL_OPTION;

	private static String[] betterSplit(final String input, final String delim, boolean SkipEmpty) {
		if (!SkipEmpty)
			return input.split(delim);
		String[] list = input.split(delim, 0);
		ArrayList<String> goodList = new ArrayList<String>();
		for (int i = 0; i < list.length; i++) {
			if (list[i].length() > 0)
				goodList.add(list[i]);
		}
		String[] retval = new String[goodList.size()];
		goodList.toArray(retval);
		return retval;
	}

	public CustomImportDlg(Window owner, File fl, String delimiter, boolean SkipEmpty) {
		super(owner);
		setResizable(false);
		setModal(true);
		initialize();
		this.file = fl;
		this.delimiter = delimiter;
		this.skipEmpty = SkipEmpty;

		model = new ImportTableModel();
		typeModel = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<? extends Object> getColumnClass(int c) {
				return IMPORT_TYPE.class;
			}
		};

		tableView.setModel(model);
		tableView.getTableHeader().setDefaultRenderer(new CentreRenderer((DefaultTableCellRenderer)(new JTable()).getTableHeader().getDefaultRenderer()));
		tableView.setDefaultRenderer(String.class, new ImportTableModel.ImportTableModelRenderer());
		tableView_2.setModel(typeModel);
		tableView_2.setTableHeader(null);

		NON_TIME_DATE_FORMAT_COUNT = 4;
		int dateFormatIndex = prefs.getInt("DATE_FORMAT", 0);
		cmdDtFormat.setSelectedIndex(dateFormatIndex);
		cmdDtFormat.addActionListener((e) -> {	
			dateFormatChanged();
			timeFormatChanged();
		});

		if (dateFormatIndex >= NON_TIME_DATE_FORMAT_COUNT)
			cmbTmFormat.setEnabled(false);

		btnCancel.addActionListener((e) -> cancel());
		btnImport.addActionListener((e) -> finish());

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fl));
		} catch (FileNotFoundException e) {
		}
		if (br != null) {
			int count = 0;
			String line;
			int lineelements = 0;
			ignoreList.clear();
			try {
				int currentline = -1;
				while ((line = br.readLine()) != null) {
					currentline++;
					String[] split = betterSplit(line, delimiter, SkipEmpty);
					if (split.length == 0)
						continue;
					lineelements += split.length;
					int j;
					for (j = 0; j < split.length; j++) {
						try {
							Double.parseDouble(split[j]);
						}
						catch (NumberFormatException ex) {
							continue;
						}
						break;
					}
					if (j == split.length) {
						ignoreList.add(currentline);
						model.addColoredRow(currentline);
					}
					count++;
					if (count >= 50)
						break;
				}
			}
			catch (IOException e) { }
			finally {
				try {
					br.close();
				}
				catch (IOException e) { }
			}
			numElements = (int)(((double)lineelements) / ((double)count));
			try {
				ArrayList<ArrayList<Object>> listlist = new ArrayList<ArrayList<Object>>(numElements);
				for (int i = 0; i < numElements; i++) {
					listlist.add(new ArrayList<Object>());
					typeModel.addColumn("", new Object[] { IMPORT_TYPE.UNKNOWN });
				}
				for (int i = 0; i < numElements; i++) {
					tableView_2.getColumnModel().getColumn(i).setPreferredWidth(100);
					tableView_2.getColumnModel().getColumn(i).setMinWidth(100);
					tableView_2.getColumnModel().getColumn(i).setMaxWidth(100);
					JComboBox<IMPORT_TYPE> comboBox = new JComboBox<IMPORT_TYPE>();
					IMPORT_TYPE[] types = IMPORT_TYPE.values();
					for (int k = 0; k < types.length; k++) {
						comboBox.addItem(types[k]);
					}
					tableView_2.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(comboBox));
				}
				tableView_2.setRowHeight(31);
				tableView_2.setDefaultRenderer(IMPORT_TYPE.class, new TypeRenderer());
				count = 0;
				br = new BufferedReader(new FileReader(fl));
				while ((line = br.readLine()) != null) {
					String[] split = betterSplit(line, delimiter, SkipEmpty);
					if (split.length != numElements)
						continue;
					boolean hrly = false;
					for (int j = 0; j < split.length; j++) {
						listlist.get(j).add(split[j]);
						if (count == 0) {
							IMPORT_TYPE t = IMPORT_TYPE.guess(split[j], hrly);
							if (t == IMPORT_TYPE.TIME)
								hrly = true;
							typeModel.setValueAt(t, 0, j);
						}
						if (j == listlist.size() - 1)
							break;
					}
					count++;
					if (count >= 14)
						break;
				}
				if (count == 14) {
					for (int i = 0; i < listlist.size(); i++)
						listlist.get(i).add("---" + Main.resourceManager.getString("ui.label.custom.truncate") + "---");
				}
				for (int i = 0; i < listlist.size(); i++) {
					model.addColumn(i + 1, listlist.get(i).toArray());
				}
				for (int i = 0; i < listlist.size(); i++) {
					tableView.getColumnModel().getColumn(i).setPreferredWidth(100);
					tableView.getColumnModel().getColumn(i).setMinWidth(100);
					tableView.getColumnModel().getColumn(i).setMaxWidth(100);
				}
				if (numElements > 7)
					tableView.setRowHeight(30);
				else
					tableView.setRowHeight(31);
			}
			catch (IOException ex) { }
			finally {
				try {
					br.close();
				}
				catch(IOException e) { }
			}
		}
	}

    private static class TypeRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		TypeRenderer() {
			super();
			setHorizontalAlignment(RLabel.CENTER);
		}

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setBackground(new Color(0xb0e0e6));
            return c;
        }
    }

    private static class CentreRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		private DefaultTableCellRenderer renderer = null;

		public CentreRenderer(DefaultTableCellRenderer rend) {
			super();
			renderer = rend;
			setHorizontalAlignment(RLabel.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			if (renderer == null) {
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
			else {
				Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (c instanceof RLabel)
					((RLabel)c).setHorizontalAlignment(RLabel.CENTER);
				return c;
			}
		}
    }

    private static class ImportTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		private ArrayList<Integer> coloredRows = new ArrayList<Integer>();

		public void addColoredRow(int i) {
			coloredRows.add(i);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public Class<? extends Object> getColumnClass(int c) {
			return String.class;
		}

		static class ImportTableModelRenderer extends DefaultTableCellRenderer {
			private static final long serialVersionUID = 1L;
			private Color defaultColor = null;

	        @Override
	        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	            if (defaultColor == null)
	            	defaultColor = c.getBackground();
	            ImportTableModel m = (ImportTableModel)table.getModel();
	            if (m.coloredRows.contains(row)) {
	            	c.setBackground(Color.red);
	            	if (c instanceof RLabel)
	            		((RLabel)c).setToolTipText(Main.resourceManager.getString("ui.label.import.rowimporttooltip"));
	            }
	            else {
	            	c.setBackground(defaultColor);
	            	if (c instanceof RLabel)
	            		((RLabel)c).setToolTipText(null);
	            }
	            return c;
	        }
		}
	}

	public void dateFormatChanged() {
		int index = cmdDtFormat.getSelectedIndex();
		if (index >= NON_TIME_DATE_FORMAT_COUNT)
			cmbTmFormat.setEnabled(false);
		else
			cmbTmFormat.setEnabled(true);
		prefs.putInt("DATE_FORMAT", index);
	}

	public void timeFormatChanged() {
		int index = cmbTmFormat.getSelectedIndex();
		prefs.putInt("TIME_FORMAT", index);
	}

	public void cancel() {
		dispose();
	}

	private int buildHashMap() {
		DefaultTableModel model = (DefaultTableModel)tableView_2.getModel();
		typeMap.clear();
		for (int i = 0; i < model.getColumnCount(); i++) {
			IMPORT_TYPE t = (IMPORT_TYPE)model.getValueAt(0, i);
			if (typeMap.containsKey(t)) {
				typeMap.clear();
				return -1;
			}
			if (t != IMPORT_TYPE.UNKNOWN)
				typeMap.put(t, i);
		}
		if (typeMap.containsKey(IMPORT_TYPE.DATE))
			return 0;
		typeMap.clear();
		return -2;
	}

	private static class Time {
		public int hour;

		public Time(int h) {
			hour = h;
		}
	}

	private static class DateTime {
		public int hour;
		public Date date;

		public DateTime() {
			hour = 0;
			date = null;
		}

		public DateTime(Date d, Time t) {
			hour = t.hour;
			date = d;
		}

		public void setDate(Date d) {
			date = d;
		}

		public void setTime(Time t) {
			hour = t.hour;
		}

		@SuppressWarnings("deprecation")
		public String toString(boolean tm) {
			StringBuilder builder = new StringBuilder();
			builder.append(date.getDate());
			builder.append("/");
			builder.append(date.getMonth() + 1);
			builder.append("/");
			builder.append(date.getYear() + 1900);
			if (tm) {
				builder.append("  ");
				builder.append(hour);
			}
			return builder.toString();
		}
	}

	@SuppressWarnings("deprecation")
	private Object getValue(String[] line, IMPORT_TYPE type) {
		Integer index = typeMap.get(type);
		if (index == null || index < 0 || index > line.length)
			return null;
		if (type == IMPORT_TYPE.TIME) {
			if (cmbTmFormat.getSelectedIndex() == 0) {
				try {
					Integer i = Integer.parseInt(line[index]);
					Time t = new Time(i);
					return t;
				}
				catch (NumberFormatException ex) { }
			}
			else {
				SimpleDateFormat format = new SimpleDateFormat((String)cmbTmFormat.getSelectedItem());
				try {
					Date d = format.parse(line[index]);
					Time t = new Time(d.getHours());
					return t;
				}
				catch (ParseException e) { }
			}
		}
		else if (type == IMPORT_TYPE.DATE) {
			SimpleDateFormat format = new SimpleDateFormat((String)cmdDtFormat.getSelectedItem());
			try {
				Date d = format.parse(line[index]);
				if (cmdDtFormat.getSelectedIndex() >= NON_TIME_DATE_FORMAT_COUNT) {
					Time time = new Time(d.getHours());
					DateTime dt = new DateTime(d, time);
					return dt;
				}
				else {
					return d;
				}
			}
			catch (ParseException ex) { }
		}
		else {
			try {
				Double d = Double.parseDouble(line[index]);
				return d;
			}
			catch (NumberFormatException ex) { }
		}
		return null;
	}

	private void parseHourly() {
		BufferedReader br = null;
		BufferedWriter bw = null;
		String newline = System.getProperty("line.separator");
		try {
			br = new BufferedReader(new FileReader(file));
			File temp = File.createTempFile("wsdata", "temp");
			temporaryFile = temp.getAbsolutePath();
			temp.deleteOnExit();
			bw = new BufferedWriter(new FileWriter(temp));
			bw.write("hourly  hour");
			String line;
			int currentline = -1;
			boolean buildHeader = true;
			boolean tempAv = false;
			boolean rhAv = false;
			boolean preAv = false;
			boolean wsAv = false;
			boolean wdAv = false;
			boolean ffmcAv = false;
			boolean dmcAv = false;
			boolean dcAv = false;
			boolean buiAv = false;
			boolean isiAv = false;
			boolean fwiAv = false;
			while ((line = br.readLine()) != null) {
				currentline++;
				if (ignoreList.contains(currentline))
					continue;
				String[] split = betterSplit(line, delimiter, skipEmpty);
				if (split.length != numElements)
					continue;
				DateTime dt = new DateTime();
				double temperature = 0;
				double RH = 0;
				double precipitation = 0;
				double windSpeed = 0;
				double windDirection = 0;
				double ffmc = 0;
				double dmc = 0;
				double dc = 0;
				double bui = 0;
				double isi = 0;
				double fwi = 0;
				Object o = getValue(split, IMPORT_TYPE.DATE);
				if (o == null)
					continue;
				if (o.getClass() == DateTime.class)
					dt = (DateTime)o;
				else {
					dt.setDate((Date)o);
					o = getValue(split, IMPORT_TYPE.TIME);
					if (o == null)
						continue;
					dt.setTime((Time)o);
				}
				if ((o = getValue(split, IMPORT_TYPE.TEMP)) != null) {
					temperature = (Double)o;
					if (buildHeader) {
						bw.write("  temp");
						tempAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.RH)) != null) {
					RH = (Double)o;
					if (buildHeader) {
						bw.write("  rh");
						rhAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.PRECIP)) != null) {
					precipitation = (Double)o;
					if (buildHeader) {
						bw.write("  precip");
						preAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.WS)) != null) {
					windSpeed = (Double)o;
					if (buildHeader) {
						bw.write("  ws");
						wsAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.WD)) != null) {
					windDirection = (Double)o;
					if (buildHeader) {
						bw.write("  wd");
						wdAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.FFMC)) != null) {
					ffmc = (Double)o;
					if (buildHeader) {
						bw.write("  ffmc");
						ffmcAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.DMC)) != null) {
					dmc = (Double)o;
					if (buildHeader) {
						bw.write("  dmc");
						dmcAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.DC)) != null) {
					dc = (Double)o;
					if (buildHeader) {
						bw.write("  dc");
						dcAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.BUI)) != null) {
					bui = (Double)o;
					if (buildHeader) {
						bw.write("  bui");
						buiAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.ISI)) != null) {
					isi = (Double)o;
					if (buildHeader) {
						bw.write("  hisi");
						isiAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.FWI)) != null) {
					fwi = (Double)o;
					if (buildHeader) {
						bw.write("  hfwi");
						fwiAv = true;
					}
				}
				if (buildHeader) {
					bw.write(newline);
					buildHeader = false;
				}
				bw.write(dt.toString(true));
				if (tempAv) {
					bw.write("  ");
					bw.write(String.valueOf(temperature));
				}
				if (rhAv) {
					bw.write("  ");
					bw.write(String.valueOf(RH));
				}
				if (preAv) {
					bw.write("  ");
					bw.write(String.valueOf(precipitation));
				}
				if (wsAv) {
					bw.write("  ");
					bw.write(String.valueOf(windSpeed));
				}
				if (wdAv) {
					bw.write("  ");
					bw.write(String.valueOf(windDirection));
				}
				if (ffmcAv) {
					bw.write("  ");
					bw.write(String.valueOf(ffmc));
				}
				if (dmcAv) {
					bw.write("  ");
					bw.write(String.valueOf(dmc));
				}
				if (dcAv) {
					bw.write("  ");
					bw.write(String.valueOf(dc));
				}
				if (buiAv) {
					bw.write("  ");
					bw.write(String.valueOf(bui));
				}
				if (isiAv) {
					bw.write("  ");
					bw.write(String.valueOf(isi));
				}
				if (fwiAv) {
					bw.write("  ");
					bw.write(String.valueOf(fwi));
				}
				bw.write(newline);
			}
		}
		catch (FileNotFoundException e) {
		}
		catch (IOException e) {
		}
		finally {
			try {
				if (br != null)
					br.close();
			}
			catch(IOException e) { }
			try {
				if (bw != null)
					bw.close();
			}
			catch(IOException e) { }
		}
	}

	private void parseDaily() {
		BufferedReader br = null;
		BufferedWriter bw = null;
		String newline = System.getProperty("line.separator");
		try {
			br = new BufferedReader(new FileReader(file));
			File temp = File.createTempFile("wsdata", "temp");
			temporaryFile = temp.getAbsolutePath();
			temp.deleteOnExit();
			bw = new BufferedWriter(new FileWriter(temp));
			bw.write("daily");
			String line;
			int currentline = -1;
			boolean buildHeader = true;
			boolean minTempAv = false;
			boolean maxTempAv = false;
			boolean rhAv = false;
			boolean preAv = false;
			boolean minWsAv = false;
			boolean maxWsAv = false;
			boolean wdAv = false;
			while ((line = br.readLine()) != null) {
				currentline++;
				if (ignoreList.contains(currentline))
					continue;
				String[] split = betterSplit(line, delimiter, skipEmpty);
				if (split.length != numElements)
					continue;
				DateTime dt = new DateTime();
				double minTemp = 0;
				double maxTemp = 0;
				double RH = 0;
				double precipitation = 0;
				double minWindSpeed = 0;
				double maxWindSpeed = 0;
				double windDirection = 0;
				Object o = getValue(split, IMPORT_TYPE.DATE);
				if (o == null)
					continue;
				if (o.getClass() == DateTime.class)
					dt = (DateTime)o;
				else
					dt.setDate((Date)o);
				if ((o = getValue(split, IMPORT_TYPE.MIN_TEMP)) != null) {
					minTemp = (Double)o;
					if (buildHeader) {
						bw.write("  min_temp");
						minTempAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.MAX_TEMP)) != null) {
					maxTemp = (Double)o;
					if (buildHeader) {
						bw.write("  max_temp");
						maxTempAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.RH)) != null) {
					RH = (Double)o;
					if (buildHeader) {
						bw.write("  rh");
						rhAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.PRECIP)) != null) {
					precipitation = (Double)o;
					if (buildHeader) {
						bw.write("  precip");
						preAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.MIN_WS)) != null) {
					minWindSpeed = (Double)o;
					if (buildHeader) {
						bw.write("  min_ws");
						minWsAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.MAX_WS)) != null) {
					maxWindSpeed = (Double)o;
					if (buildHeader) {
						bw.write("  max_ws");
						maxWsAv = true;
					}
				}
				if ((o = getValue(split, IMPORT_TYPE.WD)) != null) {
					windDirection = (Double)o;
					if (buildHeader) {
						bw.write("  wd");
						wdAv = true;
					}
				}
				if (buildHeader) {
					bw.write(newline);
					buildHeader = false;
				}
				bw.write(dt.toString(false));
				if (minTempAv) {
					bw.write("  ");
					bw.write(String.valueOf(minTemp));
				}
				if (maxTempAv) {
					bw.write("  ");
					bw.write(String.valueOf(maxTemp));
				}
				if (rhAv) {
					bw.write("  ");
					bw.write(String.valueOf(RH));
				}
				if (preAv) {
					bw.write("  ");
					bw.write(String.valueOf(precipitation));
				}
				if (minWsAv) {
					bw.write("  ");
					bw.write(String.valueOf(minWindSpeed));
				}
				if (maxWsAv) {
					bw.write("  ");
					bw.write(String.valueOf(maxWindSpeed));
				}
				if (wdAv) {
					bw.write("  ");
					bw.write(String.valueOf(windDirection));
				}
				bw.write(newline);
			}
		}
		catch (FileNotFoundException e) {
		}
		catch (IOException e) {
		}
		finally {
			if (br != null) {
				try {
					br.close();
				}
				catch(IOException e) { }
			}
			if (bw != null) {
				try {
					bw.close();
				}
				catch(IOException e) { }
			}
		}
	}

	public void finish() {
		int res = buildHashMap();
		if (res != 0) {
			if (res == -1) {
				JOptionPane.showMessageDialog(this, Main.resourceManager.getString("ui.label.custom.import.error"), "Error", JOptionPane.ERROR_MESSAGE);
			}
			return;
		}
		if (typeMap.containsKey(IMPORT_TYPE.TIME) || cmdDtFormat.getSelectedIndex() >= NON_TIME_DATE_FORMAT_COUNT) {
			hourly = true;
			parseHourly();
		}
		else {
			hourly = false;
			parseDaily();
		}
		retval = JFileChooser.APPROVE_OPTION;
		dispose();
	}

	/**
	 * Get whether or not the imported file contains hourly or daily information.
	 * @return
	 */
	public boolean isHourly() {
		return hourly;
	}

	/**
	 * The file for the weather stream to import.
	 * @return
	 */
	public String importFile() {
		return temporaryFile;
	}

	private void initialize() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo_20.png")));
		setTitle(Main.resourceManager.getString("ui.dlg.title.custom"));
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 715, 607);
		else
			setBounds(0, 0, 725, 612);
		getContentPane().setLayout(null);

		tableView = new JTable();
		tableView.setRowSelectionAllowed(false);
		tableView.setCellSelectionEnabled(true);
		tableView.setBounds(0, 0, 709, 491);
		final JScrollPane scrollPane = new JScrollPane(tableView);
		scrollPane.setBounds(0, 31, 709, 491);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		tableView.setFillsViewportHeight(true);
		tableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableView.setPreferredScrollableViewportSize(tableView.getPreferredSize());
		getContentPane().add(scrollPane);

		tableView_2 = new JTable();
		tableView_2.setCellSelectionEnabled(true);
		tableView_2.setRowSelectionAllowed(false);
		tableView_2.setBounds(0, 0, 709, 31);
		final JScrollPane scrollPane_2 = new JScrollPane(tableView_2);
		scrollPane_2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scrollPane_2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane_2.setBounds(0, 0, 709, 31);
		tableView_2.setFillsViewportHeight(true);
		tableView_2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableView_2.setPreferredScrollableViewportSize(tableView_2.getPreferredSize());
		getContentPane().add(scrollPane_2);

		scrollPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				scrollPane_2.getHorizontalScrollBar().setValue(scrollPane.getHorizontalScrollBar().getValue());
			}
		});

		lblDtFormat = new RLabel(Main.resourceManager.getString("ui.label.custom.dateformat"));
		lblDtFormat.setBounds(10, 530, 171, 16);
		getContentPane().add(lblDtFormat);

		lblTmFormat = new RLabel(Main.resourceManager.getString("ui.label.custom.timeformat"));
		lblTmFormat.setBounds(190, 530, 171, 16);
		getContentPane().add(lblTmFormat);

		cmdDtFormat = new JComboBox<String>();
		cmdDtFormat.setModel(new DefaultComboBoxModel<String>(new String[] {"d/M/y", "M/d/y", "y/M/d", "y-M-d", "y-M-d H:m:s", "y/M/d H:m:s"}));
		cmdDtFormat.setBounds(10, 550, 171, 22);
		getContentPane().add(cmdDtFormat);

		cmbTmFormat = new JComboBox<String>();
		cmbTmFormat.setModel(new DefaultComboBoxModel<String>(new String[] {"H", "H:m", "H:m:s"}));
		cmbTmFormat.setBounds(190, 550, 171, 22);
		getContentPane().add(cmbTmFormat);

		btnImport = new RButton(Main.resourceManager.getString("ui.label.custom.import"));
		btnImport.setBounds(590, 530, 121, 41);
		getContentPane().add(btnImport);

		btnCancel = new RButton(Main.resourceManager.getString("ui.label.custom.cancel"));
		btnCancel.setBounds(460, 530, 121, 41);
		getContentPane().add(btnCancel);
	}

	public static enum IMPORT_TYPE {
		UNKNOWN(0),
		DATE(1),
		TIME(2),
		TEMP(3),
		RH(4),
		PRECIP(5),
		WS(6),
		WD(7),
		FFMC(8),
		DMC(9),
		DC(10),
		ISI(11),
		FWI(12),
		BUI(13),
		MIN_TEMP(14),
		MAX_TEMP(15),
		MIN_WS(16),
		MAX_WS(17);

		private int index;

		IMPORT_TYPE(int ind) {
			index = ind;
		}

		@Override
		public String toString() {
			switch (index) {
			case 1:
				return Main.resourceManager.getString("ui.label.custom.date");
			case 2:
				return Main.resourceManager.getString("ui.label.custom.time");
			case 3:
				return Main.resourceManager.getString("ui.label.weather.abbv.temp");
			case 4:
				return Main.resourceManager.getString("ui.label.weather.abbv.rh");
			case 5:
				return Main.resourceManager.getString("ui.label.weather.abbv.precip");
			case 6:
				return Main.resourceManager.getString("ui.label.weather.abbv.ws");
			case 7:
				return Main.resourceManager.getString("ui.label.weather.abbv.wd");
			case 8:
				return Main.resourceManager.getString("ui.label.fire.hffmc");
			case 9:
				return Main.resourceManager.getString("ui.label.fire.dmc");
			case 10:
				return Main.resourceManager.getString("ui.label.fire.dc");
			case 11:
				return Main.resourceManager.getString("ui.label.fire.hisi");
			case 12:
				return Main.resourceManager.getString("ui.label.fire.hfwi");
			case 13:
				return Main.resourceManager.getString("ui.label.fire.bui");
			case 14:
				return Main.resourceManager.getString("ui.label.weather.abbv.mintemp");
			case 15:
				return Main.resourceManager.getString("ui.label.weather.abbv.maxtemp");
			case 16:
				return Main.resourceManager.getString("ui.label.weather.abbv.minws");
			case 17:
				return Main.resourceManager.getString("ui.label.weather.abbv.maxws");
			default:
				return "";
			}
		}

		public int toInt() {
			return index;
		}

		public static IMPORT_TYPE fromInt(int i) {
			return IMPORT_TYPE.values()[i];
		}

		public static IMPORT_TYPE guess(String title, boolean hourly) {
			title = title.toLowerCase();
			if (title.compareTo("hourly") == 0 || title.compareTo("daily") == 0 || title.compareTo("date") == 0)
				return DATE;
			if (title.compareTo("hour") == 0 || title.compareTo("time") == 0 || title.compareTo("time(cst)") == 0)
				return TIME;
			if (hourly) {
				if (title.compareTo("temp") == 0 || title.compareTo("temperature") == 0 || title.compareTo("temp(celsius)") == 0)
					return TEMP;
			}
			else {
				if (title.compareTo("min_temp") == 0 || title.compareTo("min temp") == 0 || title.compareTo("min-temp") == 0 ||
						title.compareTo("minimum temperature") == 0)
					return MIN_TEMP;
				if (title.compareTo("max_temp") == 0 || title.compareTo("max temp") == 0 || title.compareTo("max-temp") == 0 ||
						title.compareTo("maximum temperature") == 0)
					return MAX_TEMP;
			}
			if (title.compareTo("rh") == 0 || title.compareTo("relative humidity") == 0 || title.compareTo("relative_humidity") == 0 ||
					title.compareTo("relative-humidity") == 0 || title.compareTo("min_rh") == 0)
				return RH;
			if (title.compareTo("precip") == 0 || title.compareTo("precipitation") == 0 || title.compareTo("rain") == 0 ||
					 title.compareTo("raintot") == 0 || title.compareTo("rn_1") == 0 || title.compareTo("rn24") == 0)
				return PRECIP;
			if (hourly) {
				if (title.compareTo("ws") == 0 || title.compareTo("wind speed") == 0 || title.compareTo("wind_speed") == 0 ||
						title.compareTo("wind-speed") == 0 || title.compareTo("speed(kph)") == 0 || title.compareTo("wspd") == 0)
					return WS;
			}
			else {
				if (title.compareTo("min_ws") == 0 || title.compareTo("min ws") == 0 || title.compareTo("min-ws") == 0 ||
						title.compareTo("minimum wind speed") == 0)
					return MIN_WS;
				if (title.compareTo("max_ws") == 0 || title.compareTo("max ws") == 0 || title.compareTo("max-ws") == 0 ||
						title.compareTo("maximum wind speed") == 0)
					return MAX_WS;
			}
			if (title.compareTo("wd") == 0 || title.compareTo("wind direction") == 0 || title.compareTo("wind_direction") == 0 ||
					title.compareTo("wind-direction") == 0 || title.compareTo("dir") == 0 || title.compareTo("dir(degrees)") == 0)
				return WD;
			if (title.compareTo("ffmc") == 0 || title.compareTo("ffmc(h)") == 0 || title.compareTo("hffmc") == 0)
				return FFMC;
			if (title.compareTo("dmc") == 0)
				return DMC;
			if (title.compareTo("dc") == 0)
				return DC;
			if (title.compareTo("bui") == 0)
				return BUI;
			if (title.compareTo("isi") == 0 || title.compareTo("isi(h)") == 0 || title.compareTo("hisi") == 0)
				return ISI;
			if (title.compareTo("fwi") == 0 || title.compareTo("fwi(h)") == 0 || title.compareTo("hfwi") == 0)
				return FWI;
			return UNKNOWN;
		}
	}

	public int getResult() {
		return retval;
	}
}
