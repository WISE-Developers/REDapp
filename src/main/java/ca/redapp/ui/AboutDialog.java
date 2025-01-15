/***********************************************************************
 * REDapp - AboutDialog.java
 * Copyright (C) 2015-2019 The REDapp Development Team
 * Homepage: http://redapp.org
 * REDapp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
  * REDapp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
 * along with REDapp. If not see <http://www.gnu.org/licenses/>. 
 **********************************************************************/

package ca.redapp.ui;

import java.awt.ComponentOrientation;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import ca.redapp.ui.component.RButton;
import ca.redapp.util.BuildConfig;
import ca.redapp.util.MavenProjectVersionGetter;
import javax.swing.event.HyperlinkListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;

public class AboutDialog extends JDialog implements MouseListener {
	private static final long serialVersionUID = 1L;
	private Main app;
	private Long startClick = null;

	public AboutDialog(Main app) {
		super(app.frmRedapp);
		this.app = app;
		initialize();
		setDialogPosition(app.getForm());
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

	private void initialize() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle(Main.resourceManager.getString("ui.dlg.title.about"));
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
		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		if (Launcher.javaVersion.major < 9)
			setBounds(100, 100, 450, 400);
		else
			setBounds(100, 100, 460, 405);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		getContentPane().add(panel, BorderLayout.SOUTH);

		JPanel panel_right = new JPanel();
		panel_right.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		panel_right.setLayout(new BorderLayout());
		panel.add(panel_right, BorderLayout.EAST);

		RButton ok = new RButton(Main.resourceManager.getString("ui.label.settings.ok"));
		ok.setBounds(0, 0, 121, 41);
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.setVisible(false);
			}
		});
		panel_right.add(ok, BorderLayout.PAGE_END);

		JPanel panel_left = new JPanel();
		panel_left.setLayout(new BorderLayout());
		panel.add(panel_left, BorderLayout.WEST);

		RButton cred = new RButton(Main.resourceManager.getString("ui.label.about.license"));
		cred.setBounds(0, 0, 121, 41);
		cred.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LicenseDialog ld = new LicenseDialog(app);
				ld.setVisible(true);
			}
		});
		panel_left.add(cred, BorderLayout.PAGE_END);

		JPanel panel_1 = new JPanel(new BorderLayout());
		getContentPane().add(panel_1, BorderLayout.CENTER);
		String version = MavenProjectVersionGetter.getCurrentProjectVersion();//BuildConfig.version.getMinor() + "." + BuildConfig.version.getPatch() + BuildConfig.version.getSuffix().replace('-', '.');



        ZonedDateTime datetime = ZonedDateTime.parse(BuildConfig.buildTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String displayDate = datetime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy", Main.resourceManager.loc));
        String currentYear = String.valueOf(datetime.getYear());
		//String displayDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy", Main.resourceManager.loc));
		//String currentYear = String.valueOf(LocalDateTime.now().getYear());

		if (Main.isLinux()) {
			JTextArea editor = new JTextArea();
			editor.setEditable(false);
			editor.setText(Main.resourceManager.getString("ui.label.about.text", version, displayDate, currentYear));
			editor.setLineWrap(true);
			editor.setWrapStyleWord(true);
			editor.setPreferredSize(new Dimension(0, 400));
			
			JScrollPane scroll = new JScrollPane(editor);
			panel_1.add(scroll, BorderLayout.CENTER);
			editor.setCaretPosition(0);
		}
		else {
			JEditorPane editor = new JEditorPane();
			editor.setEditable(false);
			editor.setContentType("text/html");
			editor.setText(Main.resourceManager.getString("ui.label.about.html", version, displayDate, currentYear));
			editor.setPreferredSize(new Dimension(0, 400));
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
			panel_1.add(scroll, BorderLayout.CENTER);
			editor.setCaretPosition(0);
		}
		
		JPanel panel_2 = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				int w = getWidth();
				int h = getHeight();
				Color color1 = new Color(203, 143, 143, 255);
				Color color2 = new Color(168, 69, 69, 255);
				GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
				g2d.setPaint(gp);
				g2d.fillRect(0, 0, w, h);
			}
		};
		panel_1.add(panel_2, BorderLayout.WEST);
		panel_2.setLayout(new BorderLayout(0, 0));
		panel_2.addMouseListener(this);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(AboutDialog.class.getResource("/images/icons/redapplogo_40.png")));
		panel_2.add(lblNewLabel, BorderLayout.NORTH);
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		startClick = null;
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		startClick = null;
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		startClick = null;
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		startClick = System.currentTimeMillis();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (startClick != null) {
			if ((System.currentTimeMillis() - startClick) > 1000) {
				Class<?> cls = null;
				try {
					cls = Class.forName("org.eclipse.paho.client.mqttv3.MqttCallback");
				}
				catch (ClassNotFoundException e1) { }
				if (cls != null) {
					JPopupMenu menu = new JPopupMenu();
					JMenuItem toggle;
					if (Main.shouldUseMqtt)
						toggle = new JMenuItem(Main.resourceManager.getString("ui.label.mqtt.disable"));
					else
						toggle = new JMenuItem(Main.resourceManager.getString("ui.label.mqtt.enable"));
					toggle.addActionListener((e) -> {
						app.showMqtt(!Main.shouldUseMqtt);
					});
					menu.add(toggle);
					menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
				}
			}
		}
		startClick = null;
	}
}
