/***********************************************************************
 * REDapp - LicenseDialog.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.event.HyperlinkEvent.EventType;

import ca.hss.general.LinkWorker;
import ca.redapp.ui.component.RButton;
import ca.redapp.util.RFileChooser;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class LicenseDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private Main parent;
	private JEditorPane editor;
	private RButton save;
	private static final LicenseData[] licenses = new LicenseData[] {
			new LicenseData("ui.app.title", "ui.label.about.gpl", "/gpl-3.0", "http://redapp.org"),
			new LicenseData("ui.license.compress", "ui.license.apache", "/apache-2.0", "https://commons.apache.org/proper/commons-compress/"),
			new LicenseData("ui.license.math", "ui.license.apache", "/apache-2.0", "https://commons.apache.org/proper/commons-math/"),
			new LicenseData("ui.license.poi", "ui.license.apache", "/apache-2.0", "https://poi.apache.org/"),
			new LicenseData("ui.license.hss_java", "ui.license.apache", "/apache-2.0", "http://heartlandsoftware.ca"),
			new LicenseData("ui.license.times", "ui.license.apache", "/apache-2.0", "http://heartlandsoftware.ca"),
			new LicenseData("ui.license.josm", "ui.label.about.gpl", "/gpl-3.0", "https://josm.openstreetmap.de"),
			new LicenseData("ui.license.osm", "ui.licence.odbl", "/odbl-1.0", "https://www.openstreetmap.org"),
			new LicenseData("ui.license.appdir", "ui.license.apache", "/apache-2.0", "https://github.com/harawata/appdirs"),
			new LicenseData("ui.license.jackson", "ui.license.apache", "/apache-2.0", "https://github.com/FasterXML/jackson"),
			new LicenseData("ui.license.paho", "ui.license.eclipse", "/epl-1.0", "https://eclipse.org/paho/")
		};

	public LicenseDialog(Main app) {
		parent = app;
		initialize();
		setDialogPosition(app.frmRedapp);
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

	private void save() {
		String file = null;
		RFileChooser fc = RFileChooser.fileSaver();
		String[] extensionFilters = new String[] {
				"*.txt",
		};
		String[] extensionFiltersNames = new String[] {
				Main.resourceManager.getString("ui.label.file.txt") + " (*.txt)",
		};
		fc.setExtensionFilters(extensionFilters, extensionFiltersNames, 0);
		fc.setTitle(Main.resourceManager.getString("ui.label.fbp.export.title"));
		int retval = fc.showDialog(parent.frmRedapp);

		if (retval == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile().getAbsolutePath();
			if (!file.endsWith(".txt"))
				file += ".txt";
			try (FileOutputStream out = new FileOutputStream(file, false)) {
				out.write(editor.getText().getBytes());
				out.close();
			}
			catch (IOException e) {
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initialize() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle(Main.resourceManager.getString("ui.label.about.license"));
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage(
				Main.class.getResource(Main.resourceManager
						.getImagePath("ui.icon.window.redapp"))));
		icons.add(Toolkit.getDefaultToolkit().getImage(
				Main.class.getResource(Main.resourceManager
						.getImagePath("ui.icon.window.redapp20"))));
		icons.add(Toolkit.getDefaultToolkit().getImage(
				Main.class.getResource(Main.resourceManager
						.getImagePath("ui.icon.window.redapp40"))));
		setIconImages(icons);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		if (Launcher.javaVersion.major < 9)
			setBounds(100, 100, 450, 400);
		else
			setBounds(100, 100, 460, 405);
		getContentPane().setLayout(new BorderLayout());
		
		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_2.setLayout(new BorderLayout());
		panel_2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel_1.add(panel_2, BorderLayout.NORTH);
		
		JPanel panel_4 = new JPanel();
		panel_2.add(panel_4, BorderLayout.SOUTH);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		final JLabel lblTitle = new JLabel("Title");
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD));
		panel_4.add(lblTitle, BorderLayout.CENTER);
		
		final JLabel lblWebsite = new JLabel("http://website.ca");
		lblWebsite.setHorizontalAlignment(SwingConstants.CENTER);
		lblWebsite.setForeground(new Color(0, 0, 0x99));
		Font f = lblWebsite.getFont();
		Map attributes = f.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		lblWebsite.setFont(f.deriveFont(attributes));
		lblWebsite.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblWebsite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				try {
					URI uri = new URI(lblWebsite.getText());
					(new LinkWorker(uri)).execute();
				}
				catch (URISyntaxException ex) {
					ex.printStackTrace();
				}
			}
		});
		panel_4.add(lblWebsite, BorderLayout.SOUTH);
		
		final JList<LicenseData> creditList = new JList<LicenseData>();
		creditList.setVisibleRowCount(3);
		creditList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		DefaultListModel<LicenseData> model = new DefaultListModel<>();
		for (int i = 0; i < licenses.length; i++) {
			model.addElement(licenses[i]);
		}
		creditList.setModel(model);
		creditList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				LicenseData data = licenses[creditList.getSelectedIndex()];
				lblTitle.setText(Main.resourceManager.getString(data.title_id));
				lblWebsite.setText(data.website);
				String loc = data.license_location;
				if (Main.resourceManager.loc.getLanguage().contains("fr"))
					loc += ".fr.txt";
				else
					loc += ".en.txt";
				try {
					editor.setPage(parent.getClass().getResource(loc));
					save.setEnabled(true);
				} catch (IOException e2) {
				}
			}
		});
		creditList.setBorder(BorderFactory.createEmptyBorder());
		
		JScrollPane scroller = new JScrollPane(creditList);
		scroller.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(130, 135, 144)),
				BorderFactory.createEmptyBorder(2, 2, 2, 2)));
		panel_2.add(scroller, BorderLayout.CENTER);
		
		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3, BorderLayout.CENTER);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		getContentPane().add(panel, BorderLayout.SOUTH);

		JPanel panel_right = new JPanel();
		panel_right.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		panel_right.setLayout(new FlowLayout(FlowLayout.RIGHT));
		panel.add(panel_right, BorderLayout.EAST);

		RButton ok = new RButton(Main.resourceManager.getString("ui.label.settings.ok"));
		ok.setBounds(0, 0, 121, 41);
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LicenseDialog.this.setVisible(false);
			}
		});
		panel_right.add(ok);

		JPanel panel_left = new JPanel();
		panel_right.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(panel_left, BorderLayout.WEST);

		save = new RButton(Main.resourceManager.getString("ui.label.edit.save"));
		save.setBounds(0, 0, 121, 41);
		save.setEnabled(false);
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LicenseDialog.this.save();
			}
		});
		panel_left.add(save);

		editor = new JEditorPane();
		editor.setEditable(false);
		editor.setContentType("text/html");
		editor.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException|URISyntaxException e1) {
						JOptionPane.showMessageDialog(null, "Unable to open link.");
					}
				}
			}
		});

		JScrollPane scroll = new JScrollPane(editor);
		panel_3.add(scroll, BorderLayout.CENTER);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(new Dimension(400, 300));
		editor.setCaretPosition(0);

		creditList.setSelectedIndex(0);
		
		pack();
	}

	private static class LicenseData {
		public String name_id;
		public String title_id;
		public String license_location;
		public String website;

		public LicenseData(String nameId, String titleId, String licenseLocation, String website) {
			name_id = nameId;
			title_id = titleId;
			license_location = licenseLocation;
			this.website = website;
		}
		
		@Override
		public String toString() {
			return Main.resourceManager.getString(name_id);
		}
	}
}
