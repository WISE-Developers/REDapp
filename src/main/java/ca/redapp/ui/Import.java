/***********************************************************************
 * REDapp - Import.java
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

import javax.swing.JDialog;
import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.border.TitledBorder;

import java.awt.CardLayout;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;

import ca.hss.general.DecimalUtils;
import ca.redapp.ui.component.RLabel;
import ca.redapp.util.LineEditHelper;
import ca.redapp.util.REDappLogger;

import javax.swing.JLabel;


public class Import extends JDialog {
	private static final long serialVersionUID = 1L;
	private JTextField txtDailyFFMC;
	private JTextField txtDailyDC;
	private JTextField txtDailyDMC;
	private JTextField txtHourlyFFMC;
	private JRadioButton weatherRadio;
	private JRadioButton delimRadio;
	private JRadioButton noonRadio;
	private JButton btnFinish;
	private JButton btnCancel;
	private JButton btnBack;
	private JButton btnNext;
	private JPanel cards;
	private JCheckBox delSpace;
	private JCheckBox delOther;
	private JCheckBox delComma;
	private JCheckBox delSemi;
	private JCheckBox delTab;
	private JCheckBox delConsec;
	private JCheckBox chkDailyFit;
	private RLabel lblHourlyValue;
	private RLabel lblHourlyFFMC;
	private RLabel lblPreviewOfFile;
	private JTextArea txtPreview;
	private JComboBox<String> comboHourlyMethod;
	private JComboBox<String> comboHourlyValue;
	private List<String> previewText = new ArrayList<String>();
	private int retval = JFileChooser.CANCEL_OPTION;
	private int currentCard = 0;
	private String[] cardNames = new String[] { "name_130462131124250", "name_1048425034670543", "name_130456465465665", "name_130439671482997" };
	private double defaultDc, defaultDmc, defaultFfmc, defaultHFfmc;
	private JTextField delOtherValue;
	private boolean delimiterValid = true;
	private String filename;
	private Main app;
	
	/*Redmine 809
	private JComboBox<TimeZoneInfo> comboTimeZone;
	private JCheckBox chckbxOverrideTimeZone;
	private RLabel lblTimeZone;
	*/



	/**
	 * Create the dialog.
	 * @throws FileNotFoundException
	 */
	public Import(Main owner, String filename) throws FileNotFoundException {
		super(owner.frmRedapp);
		app = owner;
		this.filename = filename;
		setResizable(false);
		setModal(true);
		initialize();

		btnFinish.addActionListener((e) -> done());
		btnCancel.addActionListener((e) -> cancel());
		btnNext.addActionListener((e) -> next());
		btnBack.addActionListener((e) -> back());

		weatherRadio.addActionListener((e) -> wsRadioToggled());
		delimRadio.addActionListener((e) -> defaultRadioToggled());
		noonRadio.addActionListener((e) -> noonRadioToggled());

		delOther.addActionListener((e) -> delimiterChanged());
		delComma.addActionListener((e) -> delimiterChanged());
		delSemi.addActionListener((e) -> delimiterChanged());
		delSpace.addActionListener((e) -> delimiterChanged());
		delTab.addActionListener((e) -> delimiterChanged());
		delOtherValue.addActionListener((e) -> delimiterChanged());

		setUnknownFile();
		if (Main.isWindows()) {
			lblPreviewOfFile.setText(Main.resourceManager.getString("ui.label.import.preview.preview") + " " + filename.replace("/", "\\"));
			lblPreviewOfFile.setToolTipText(filename.replace("/", "\\"));
		}
		else {
			lblPreviewOfFile.setText(Main.resourceManager.getString("ui.label.import.preview.preview") + " " + filename);
			lblPreviewOfFile.setToolTipText(filename);
		}

		File fl = new File(filename);
		BufferedReader br = new BufferedReader(new FileReader(fl));
		try {
			int lines = 0;
			int maxSize = -1;
			boolean first = true;
			String line;
			while ((line = br.readLine()) != null && lines < 50) {
				if (first) {
					first = false;

					if (line.toLowerCase(Locale.ENGLISH).startsWith("hourly") || line.toLowerCase(Locale.ENGLISH).startsWith("daily") || line.toLowerCase(Locale.ENGLISH).startsWith("date"))
						setWeatherStream();
				}
				if (line.length() > maxSize)
					maxSize = line.length();
				txtPreview.append(line);
				txtPreview.append("\r\n");
				previewText.add(line);
				lines++;
			}
			if (lines >= 50) {
				String ending = "";
				for (int i = 0; i < ((double)(maxSize - 3)) / 2.0; i++)
					ending += "-";
				ending += "[MORE]";
				for (int i = 0; i < ((double)(maxSize - 3)) / 2.0; i++)
					ending += "-";
				txtPreview.append(ending);
			}
			txtPreview.moveCaretPosition(0);
			if (getFileType() == FileType.UNKNOWN_FILE && filename.contains("noon")) {
				setNoonWeather();
			}
		}
		catch (IOException ex) {
			REDappLogger.error("Error reading file", ex);
		}
		finally {
			try {
				br.close();
			}
			catch (IOException ex) {
				REDappLogger.error("Error closing stream", ex);
			}
		}
	}

	public void cancel() {
		dispose();
	}

	public void done() {
		if (validateCodes()) {
			retval = JFileChooser.APPROVE_OPTION;
			dispose();
		}
		else {
			currentCard = 2;
			((CardLayout)cards.getLayout()).show(cards, cardNames[currentCard]);
			pageChanged(currentCard);
		}
	}

	public void delimiterChanged() {
		if (delComma.isSelected() || (delOther.isSelected() && !delOtherValue.getText().isEmpty()) ||
				delSemi.isSelected() || delSpace.isSelected() || delTab.isSelected()) {
			delimiterValid = true;
			btnFinish.setEnabled(true);
		}
		else {
			delimiterValid = false;
			btnFinish.setEnabled(false);
		}
		delOtherValue.setEnabled(delOther.isSelected());
	}

	public void wsRadioToggled() {
		if (weatherRadio.isSelected()) {
			btnFinish.setEnabled(true);
		}
	}

	public void defaultRadioToggled() {
		if (delimRadio.isSelected()) {
			btnFinish.setEnabled(false);
		}
	}
	
	public void noonRadioToggled() {
		if (noonRadio.isSelected()) {
			btnFinish.setEnabled(true);
		}
	}

	public void next() {
		CardLayout layout = (CardLayout)cards.getLayout();
		if ((weatherRadio.isSelected() || noonRadio.isSelected()) && currentCard == 0)
			currentCard = 3;
		else
			currentCard++;
		
		//Redmine 809
		if (currentCard == 2)
			currentCard++;
		
		layout.show(cards, cardNames[currentCard]);
		pageChanged(currentCard);
	}

	private boolean validateCodes() {
		boolean error = false;
		Double dFFMC = LineEditHelper.getDoubleFromLineEdit(txtDailyFFMC);
		if (dFFMC == null)
			error = true;
		else {
			if (dFFMC < 1.0 || dFFMC > 101.0) {
				error = true;
				LineEditHelper.lineEditHandleError(txtDailyFFMC,
						Main.resourceManager.getString("ui.label.range.ffmc"));
			}
		}
		Double dDMC = LineEditHelper.getDoubleFromLineEdit(txtDailyDMC);
		if (dDMC == null)
			error = true;
		else {
			if (dDMC < 0.0 || dDMC > 500.0) {
				error = true;
				LineEditHelper.lineEditHandleError(txtDailyDMC,
						Main.resourceManager.getString("ui.label.range.dmc"));
			}
		}
		Double dDC = LineEditHelper.getDoubleFromLineEdit(txtDailyDC);
		if (dDC == null)
			error = true;
		else {
			if (dDC < 0.0 || dDC > 1500.0) {
				error = true;
				LineEditHelper.lineEditHandleError(txtDailyDC,
						Main.resourceManager.getString("ui.label.range.dc"));
			}
		}
		Double hFFMC = LineEditHelper.getDoubleFromLineEdit(txtHourlyFFMC);
		if (hFFMC == null)
			error = true;
		else {
			if (hFFMC < 1.0 || hFFMC > 101.0) {
				error = true;
				LineEditHelper.lineEditHandleError(txtHourlyFFMC,
						Main.resourceManager.getString("ui.label.range.ffmc"));
			}
		}
		return !error;
	}

	public void back() {
		CardLayout layout = (CardLayout)cards.getLayout();
		if ((weatherRadio.isSelected() || noonRadio.isSelected()) && currentCard == 3)
			currentCard = 0;
		else
			currentCard--;
		
		//Redmine 809
		if (currentCard == 2)
			currentCard--;
		
		layout.show(cards, cardNames[currentCard]);
		pageChanged(currentCard);
	}

	private void pageChanged(Integer index) {
		if(index > 0)
			btnBack.setEnabled(true);
		else
			btnBack.setEnabled(false);
		if (weatherRadio.isSelected() || noonRadio.isSelected()) {
			if (index == 0 || index == 2)
				btnNext.setEnabled(true);
			else
				btnNext.setEnabled(false);
		}
		else {
			if (index < cardNames.length - 1)
				btnNext.setEnabled(true);
			else
				btnNext.setEnabled(false);
			if (index != 0 && delimiterValid)
				btnFinish.setEnabled(true);
			else
				btnFinish.setEnabled(false);
		}
	}

	private void setWeatherStream() {
		weatherRadio.setSelected(true);
		delimRadio.setSelected(false);
		noonRadio.setSelected(false);
	}

	private void setUnknownFile() {
		weatherRadio.setSelected(false);
		delimRadio.setSelected(true);
		noonRadio.setSelected(false);
		if (filename != null && !filename.isEmpty() && filename.toLowerCase().endsWith(".csv")) {
			delComma.setSelected(true);
		}
	}
	
	private void setNoonWeather() {
		weatherRadio.setSelected(false);
		delimRadio.setSelected(false);
		noonRadio.setSelected(true);
	}

	public void setStartupCodeDefaults(double dc, double dmc, double ffmc, double hffmc, int method, int hour, boolean fit) {
		defaultDc = dc;
		txtDailyDC.setText(DecimalUtils.format(dc, DecimalUtils.DataType.DC));
		defaultDmc = dmc;
		txtDailyDMC.setText(DecimalUtils.format(dmc, DecimalUtils.DataType.DMC));
		
		if(ffmc < 0)
			ffmc = 85;
		
		defaultFfmc = ffmc;
		txtDailyFFMC.setText(DecimalUtils.format(ffmc, DecimalUtils.DataType.FFMC));
		
		if(hffmc < 0) {
			hffmc = 85;
			comboHourlyValue.setSelectedIndex((app.getSelectedTimeZone().getDSTAmount().getTotalSeconds() > 0) ? 17 : 16);
		}
		else
			comboHourlyValue.setSelectedIndex(hour);
		
		defaultHFfmc = hffmc;
		txtHourlyFFMC.setText(DecimalUtils.format(hffmc, DecimalUtils.DataType.FFMC));

		comboHourlyMethod.setSelectedIndex(method);
		chkDailyFit.setSelected(fit);
		chkDailyFitChanged();
	}

	public double getDC() {
		Double dc = LineEditHelper.getDoubleFromLineEdit(txtDailyDC);
		if (dc == null)
			return defaultDc;
		return dc.doubleValue();
	}

	public double getDMC() {
		Double dmc = LineEditHelper.getDoubleFromLineEdit(txtDailyDMC);
		if (dmc == null)
			return defaultDmc;
		return dmc.doubleValue();
	}

	public double getFFMC() {
		Double ffmc = LineEditHelper.getDoubleFromLineEdit(txtDailyFFMC);
		if (ffmc == null)
			return defaultFfmc;
		return ffmc.doubleValue();
	}

	public double getHFFMC() {
		Double hffmc = LineEditHelper.getDoubleFromLineEdit(txtHourlyFFMC);
		if (hffmc == null)
			return defaultHFfmc;
		return hffmc.doubleValue();
	}

	public int getCalculationMethod() {
		return comboHourlyMethod.getSelectedIndex();
	}
	
	public int getHourlyStart() {
		return comboHourlyValue.getSelectedIndex();
	}
	
	public boolean getDailyFit() {
		return chkDailyFit.isSelected();
	}

	private void initialize() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo_20.png")));
		setTitle("Import");
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 531, 372);
		else
			setBounds(0, 0, 541, 377);
		getContentPane().setLayout(null);

		btnFinish = new JButton(Main.resourceManager.getString("ui.label.import.btn.finish"));
		btnFinish.setBounds(430, 310, 85, 23);
		getContentPane().add(btnFinish);

		btnNext = new JButton(Main.resourceManager.getString("ui.label.import.btn.next") + " >");
		btnNext.setBounds(340, 310, 85, 23);
		getContentPane().add(btnNext);

		btnBack = new JButton("< " + Main.resourceManager.getString("ui.label.import.btn.back"));
		btnBack.setEnabled(false);
		btnBack.setBounds(250, 310, 85, 23);
		getContentPane().add(btnBack);

		btnCancel = new JButton(Main.resourceManager.getString("ui.label.import.btn.cancel"));
		btnCancel.setBounds(160, 310, 85, 23);
		getContentPane().add(btnCancel);

		JPanel panelPreview = new JPanel();
		panelPreview.setLayout(null);
		panelPreview.setBorder(new TitledBorder(null, Main.resourceManager.getString("ui.label.import.preview.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelPreview.setBounds(10, 150, 501, 151);
		getContentPane().add(panelPreview);

		lblPreviewOfFile = new RLabel(Main.resourceManager.getString("ui.label.import.preview.preview"));
		lblPreviewOfFile.setBounds(10, 20, 481, 16);
		panelPreview.add(lblPreviewOfFile);

		txtPreview = new JTextArea();
		txtPreview.setBounds(10, 40, 481, 101);

		JScrollPane sp = new JScrollPane(txtPreview);
		sp.setBounds(10, 40, 481, 101);
		panelPreview.add(sp);

		cards = new JPanel();
		cards.setBounds(10, 10, 501, 141);
		getContentPane().add(cards);
		cards.setLayout(new CardLayout(0, 0));

		JPanel cardOriginalDataType = new JPanel();
		cards.add(cardOriginalDataType, cardNames[0]);
		cardOriginalDataType.setLayout(null);

		JPanel group1 = new JPanel();
		group1.setSize(501, 121);
		group1.setBorder(new TitledBorder(null, Main.resourceManager.getString("ui.label.import.original.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		group1.setLayout(null);
		cardOriginalDataType.add(group1);

		ButtonGroup bgroup1 = new ButtonGroup();

		delimRadio = new JRadioButton(Main.resourceManager.getString("ui.label.import.original.delimit"));
		delimRadio.setBounds(10, 40, 158, 18);
		group1.add(delimRadio);
		bgroup1.add(delimRadio);

		weatherRadio = new JRadioButton(Main.resourceManager.getString("ui.label.import.original.stream"));
		weatherRadio.setSelected(true);
		weatherRadio.setBounds(10, 20, 158, 18);
		group1.add(weatherRadio);
		bgroup1.add(weatherRadio);
		
		noonRadio = new JRadioButton("Daily Weather");
		noonRadio.setBounds(10, 60, 158, 18);
		group1.add(noonRadio);
		bgroup1.add(noonRadio);;

		RLabel lblNewLabel = new RLabel("- " + Main.resourceManager.getString("ui.label.import.original.stream.desc"));
		lblNewLabel.setBounds(177, 20, 320, 16);
		group1.add(lblNewLabel);

		RLabel lblNewLabel_1 = new RLabel("- " + Main.resourceManager.getString("ui.label.import.original.delimit.desc"));
		lblNewLabel_1.setBounds(177, 40, 320, 16);
		group1.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("- Daily weather information");
		lblNewLabel_2.setBounds(177, 60, 320, 16);
		group1.add(lblNewLabel_2);

		JPanel cardDelimiter = new JPanel();
		cardDelimiter.setLayout(null);
		cards.add(cardDelimiter, cardNames[1]);

		JPanel delPanel = new JPanel();
		delPanel.setLocation(0, 0);
		delPanel.setSize(165, 131);
		delPanel.setBorder(new TitledBorder(null, Main.resourceManager.getString("ui.label.import.delimiters.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		delPanel.setLayout(null);
		cardDelimiter.add(delPanel);

		delTab = new JCheckBox(Main.resourceManager.getString("ui.label.import.delimiters.tab"));
		delTab.setSelected(true);
		delTab.setBounds(10, 20, 112, 18);
		delPanel.add(delTab);

		delSemi = new JCheckBox(Main.resourceManager.getString("ui.label.import.delimiters.semicolon"));
		delSemi.setBounds(10, 40, 112, 18);
		delPanel.add(delSemi);

		delComma = new JCheckBox(Main.resourceManager.getString("ui.label.import.delimiters.comma"));
		delComma.setBounds(10, 60, 112, 18);
		delPanel.add(delComma);

		delSpace = new JCheckBox(Main.resourceManager.getString("ui.label.import.delimiters.space"));
		delSpace.setBounds(10, 80, 112, 18);
		delPanel.add(delSpace);

		delOtherValue = new JTextField();
		delOtherValue.setEnabled(false);
		delOtherValue.setBounds(128, 98, 31, 22);
		delPanel.add(delOtherValue);
		delOtherValue.setColumns(10);

		delOther = new JCheckBox(Main.resourceManager.getString("ui.label.import.delimiters.other") + ":");
		delOther.setBounds(10, 100, 112, 18);
		delPanel.add(delOther);

		delConsec = new JCheckBox(Main.resourceManager.getString("ui.label.import.delimiters.consecutive"));
		delConsec.setBounds(171, 40, 299, 18);
		cardDelimiter.add(delConsec);

		/* Redmine 809
		JPanel cardTimeZone = new JPanel();
		cards.add(cardTimeZone, cardNames[2]);
		cardTimeZone.setLayout(null);

		JPanel group2 = new JPanel();
		group2.setSize(501, 121);
		group2.setBorder(new TitledBorder(null, Main.resourceManager.getString("ui.label.import.timezone"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		group2.setLayout(null);
		cardTimeZone.add(group2);
		
		chckbxOverrideTimeZone = new JCheckBox();
		chckbxOverrideTimeZone.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (chckbxOverrideTimeZone.isEnabled()) {
					comboTimeZone.setEnabled(chckbxOverrideTimeZone.isSelected());
					lblTimeZone.setForeground(chckbxOverrideTimeZone.isSelected() ? Color.BLACK : Color.GRAY);
				} 
			}
		});
		
		chckbxOverrideTimeZone.setBackground(new Color(245, 245, 245));
		if (Main.isLinux())
			chckbxOverrideTimeZone.setFont(chckbxOverrideTimeZone.getFont().deriveFont(12.0f));
		chckbxOverrideTimeZone.setBounds(20, 20, 20, 20);
		chckbxOverrideTimeZone.setSelected(false);
		chckbxOverrideTimeZone.setEnabled(firstImported);
		group2.add(chckbxOverrideTimeZone);
		
		lblTimeZone = new RLabel(Main.resourceManager.getString("ui.label.import.overridedefault"));
		lblTimeZone.setBounds(45, 20, 101, 20);
		lblTimeZone.setForeground(Color.GRAY);
		group2.add(lblTimeZone);

		comboTimeZone = new JComboBox<TimeZoneInfo>();
		comboTimeZone.setBounds(120, 20, 301, 22);
		//ComboTimeZone.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "timeZoneChanged"));
		populateTimezoneComboBox();
		comboTimeZone.setEnabled(false);
		group2.add(comboTimeZone);
		*/
		
		JPanel cardStartingCodes = new JPanel();
		cards.add(cardStartingCodes, cardNames[3]);
		cardStartingCodes.setLayout(null);
		
		JPanel group3 = new JPanel();
		group3.setSize(250, 141);
		group3.setBorder(new TitledBorder(null, Main.resourceManager.getString("ui.label.import.codes.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		group3.setLayout(null);
		cardStartingCodes.add(group3);
		
		JPanel group4 = new JPanel();
		group4.setSize(250, 141);
		group4.setLocation(250, 0);
		group4.setBorder(new TitledBorder(null, Main.resourceManager.getString("ui.label.import.codes.method"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		group4.setLayout(null);
		cardStartingCodes.add(group4);

		RLabel lblDailyFfmc = new RLabel(Main.resourceManager.getString("ui.label.import.codes.ffmc"));
		lblDailyFfmc.setBounds(20, 20, 101, 21);
		group3.add(lblDailyFfmc);

		txtDailyFFMC = new JTextField();
		txtDailyFFMC.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDailyFFMC.setBounds(130, 19, 81, 22);
		group3.add(txtDailyFFMC);
		txtDailyFFMC.setColumns(10);

		RLabel lblDailyDMC = new RLabel(Main.resourceManager.getString("ui.label.import.codes.dmc"));
		lblDailyDMC.setBounds(20, 50, 101, 21);
		group3.add(lblDailyDMC);

		txtDailyDMC = new JTextField();
		txtDailyDMC.setColumns(10);
		txtDailyDMC.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDailyDMC.setBounds(130, 49, 81, 22);
		group3.add(txtDailyDMC);

		RLabel lblDailyDC = new RLabel(Main.resourceManager.getString("ui.label.import.codes.dc"));
		lblDailyDC.setBounds(20, 80, 101, 21);
		group3.add(lblDailyDC);

		txtDailyDC = new JTextField();
		txtDailyDC.setColumns(10);
		txtDailyDC.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDailyDC.setBounds(130, 79, 81, 22);
		group3.add(txtDailyDC);
		
		chkDailyFit = new JCheckBox();
		chkDailyFit.setSize(96, 20);
		chkDailyFit.setLocation(10, 50);
		chkDailyFit.setBounds(203, 50, 20, 20);
		chkDailyFit.addActionListener((e) -> chkDailyFitChanged());
		group4.add(chkDailyFit);

		lblHourlyFFMC = new RLabel(Main.resourceManager.getString("ui.label.import.codes.hffmc"));
		lblHourlyFFMC.setBounds(20, 80, 101, 16);
		lblHourlyFFMC.setForeground(chkDailyFit.isSelected() ? Color.GRAY: Color.BLACK);
		group4.add(lblHourlyFFMC);
		
		txtHourlyFFMC = new JTextField();
		txtHourlyFFMC.setColumns(10);
		txtHourlyFFMC.setHorizontalAlignment(SwingConstants.RIGHT);
		txtHourlyFFMC.setBounds(140, 79, 81, 22);
		txtHourlyFFMC.setEnabled(!chkDailyFit.isSelected());
		group4.add(txtHourlyFFMC);

		comboHourlyMethod = new JComboBox<String>();
		comboHourlyMethod.setModel(new DefaultComboBoxModel<String>(new String[] { Main.resourceManager.getString("ui.label.fwicalc.hourly.lawson"),
				Main.resourceManager.getString("ui.label.fwicalc.hourly.wagner")}));
		comboHourlyMethod.setBounds(20, 19, 201, 22);
		comboHourlyMethod.addActionListener((e) -> hourlyMethodChanged());
		group4.add(comboHourlyMethod);
		
		String fitLabel = "";
		
		if (app.getSelectedTimeZone().getDSTAmount().getTotalSeconds() > 0)
			fitLabel = Main.resourceManager.getString("ui.label.stats.codes.today.fit", "17");
		else
			fitLabel = Main.resourceManager.getString("ui.label.stats.codes.today.fit", "16");
		
		RLabel lblStatsDailyFit = new RLabel(fitLabel);
		lblStatsDailyFit.setBounds(20, 50, 151, 20);
		group4.add(lblStatsDailyFit);
		
		lblHourlyValue = new RLabel(Main.resourceManager.getString("ui.label.stats.codes.today.start"));
		lblHourlyValue.setBounds(20, 110, 101, 16);
		lblHourlyValue.setForeground(chkDailyFit.isSelected() ? Color.GRAY: Color.BLACK);
		group4.add(lblHourlyValue);
		
		comboHourlyValue = new JComboBox<String>();
		comboHourlyValue.setModel(new DefaultComboBoxModel<String>(new String[] {
				"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"}));
		comboHourlyValue.setSelectedIndex(1);
		comboHourlyValue.setBounds(140, 109, 81, 22);
		comboHourlyValue.setEnabled(!chkDailyFit.isSelected());
		group4.add(comboHourlyValue);

		setFocusTraversalPolicy(new FocusTraversalPolicy() {
			@Override
			public Component getLastComponent(Container aContainer) {
				return btnFinish;
			}

			@Override
			public Component getFirstComponent(Container aContainer) {
				return txtDailyFFMC;
			}

			@Override
			public Component getDefaultComponent(Container aContainer) {
				return txtDailyFFMC;
			}

			@Override
			public Component getComponentBefore(Container aContainer,
					Component aComponent) {
				if (aComponent == btnFinish)
					return comboHourlyMethod;
				if (aComponent == comboHourlyMethod)
					return txtHourlyFFMC;
				if (aComponent == txtDailyDMC)
					return txtDailyFFMC;
				if (aComponent == txtDailyDC)
					return txtDailyDMC;
				if (aComponent == txtHourlyFFMC)
					return txtDailyDC;
				return null;
			}

			@Override
			public Component getComponentAfter(Container aContainer,
					Component aComponent) {
				if (aComponent == txtDailyFFMC)
					return txtDailyDMC;
				if (aComponent == txtDailyDMC)
					return txtDailyDC;
				if (aComponent == txtDailyDC)
					return txtHourlyFFMC;
				if (aComponent == txtHourlyFFMC)
					return comboHourlyMethod;
				if (aComponent == comboHourlyMethod)
					return btnFinish;
				return null;
			}
		});
	}

	private void chkDailyFitChanged() {
		lblHourlyFFMC.setForeground(chkDailyFit.isSelected() ? Color.GRAY: Color.BLACK);
		txtHourlyFFMC.setEnabled(!chkDailyFit.isSelected());
		lblHourlyValue.setForeground(chkDailyFit.isSelected() ? Color.GRAY: Color.BLACK);
		comboHourlyValue.setEnabled(!chkDailyFit.isSelected());
		
		if (chkDailyFit.isSelected()) {
			comboHourlyValue.setSelectedIndex((app.getSelectedTimeZone().getDSTAmount().getTotalSeconds() > 0) ? 17 : 16);
		}
	}
	
	private void hourlyMethodChanged() {
		lblHourlyFFMC.setForeground(chkDailyFit.isSelected() ? Color.GRAY: Color.BLACK);
		txtHourlyFFMC.setEnabled(!chkDailyFit.isSelected());
		lblHourlyValue.setForeground(chkDailyFit.isSelected() ? Color.GRAY: Color.BLACK);
		comboHourlyValue.setEnabled(!chkDailyFit.isSelected());
		chkDailyFit.setEnabled(comboHourlyMethod.getSelectedIndex() == 1);
	}
	
	/**
	 * The delimiter to use when importing generic data files.
	 * @return
	 */
	public String delimiter() {
		String del = "";
		if (delTab.isSelected())
			del += "\\t";
		if (delSemi.isSelected()) {
			if (del.length() > 0)
				del += "|";
			del += ";";
		}
		if (delComma.isSelected()) {
			if (del.length() > 0)
				del += "|";
			del += ",";
		}
		if (delSpace.isSelected()) {
			if (del.length() > 0)
				del += "|";
			del += " ";
		}
		if (delOther.isSelected() && !delOtherValue.getText().isEmpty()) {
			if (del.length() > 0)
				del += "|";
			del += delOtherValue.getText();
		}
		return del;
	}

	/**
	 * Should empty values be ignored.
	 * @return
	 */
	public boolean ignoreEmpty() {
		return delConsec.isSelected();
	}

	public int getResult() {
		return retval;
	}

	public FileType getFileType() {
		if (weatherRadio.isSelected())
			return FileType.WEATHER_STREAM;
		else if (noonRadio.isSelected())
			return FileType.NOON_WEATHER;
		else
			return FileType.UNKNOWN_FILE;
	}

	public enum FileType {
		WEATHER_STREAM(1),
		UNKNOWN_FILE(2),
		NOON_WEATHER(3);
		
		private int intVal;

		private FileType(int val) { intVal = val; }

		public static FileType fromInt(int val) {
			switch (val) {
			case 1:
				return WEATHER_STREAM;
			case 3:
				return NOON_WEATHER;
			default:
				return UNKNOWN_FILE;
			}
		}

		public int toInt() { return intVal; }

		@Override
		public String toString() {
			switch (intVal) {
			case 1:
				return "Weather Stream";
			case 3:
				return "Daily Weather";
			default:
				return "Unknown File";
			}
		}
	}
	
	/*Redmine 809
	private void populateTimezoneComboBox() {
		TimeZoneGroup g = TimeZoneGroup.fromId(Main.prefs.getInt("regionindex", 0));
		TimeZoneInfo[] info = ca.hss.times.WorldLocation.getTimezones(g);

		comboTimeZone.setModel(new DefaultComboBoxModel<TimeZoneInfo>(info));
		initializeTimezoneComboBox(info);
	}
	
	public void initializeTimezoneComboBox(TimeZoneInfo[] timeZoneInfoList) {
		comboTimeZone.setSelectedItem(app.getSelectedTimeZone());
	}
	
	public boolean useSelectedTimeZone() {
		return chckbxOverrideTimeZone.isSelected();
	}
	
	public TimeZoneInfo getSelectedTimeZone() {
		return (TimeZoneInfo)comboTimeZone.getSelectedItem();
	}
	*/
}
