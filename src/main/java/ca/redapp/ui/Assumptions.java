/***********************************************************************
 * REDapp - Assumptions.java
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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import ca.redapp.ui.component.RButton;
import ca.redapp.util.RPreferences;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Assumptions extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private static Assumptions assump = null;
	private RPreferences prefs;
	private JCheckBox chckbxDontShowThis;

	public static void showAssumptionsDialog(Main app, RPreferences prefs) {
		if (assump == null)
			assump = new Assumptions(prefs);
		if (assump.isVisible()) {
			assump.dispose();
			assump = new Assumptions(prefs);
			assump.setDialogPosition(app.getForm());
			assump.setVisible(true);
		}
		else
		{
			assump.setDialogPosition(app.getForm());
			assump.setVisible(true);
		}
	}

	public static void hideAssumptionsDialog() {
		if (assump != null)
			assump.setVisible(false);
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

	/**
	 * Create the dialog.
	 */
	private Assumptions(RPreferences prefs) {
		this.prefs = prefs;
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setTitle(Main.resourceManager.getString("ui.dlg.title.assumptions"));
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
		if (Launcher.javaVersion.major < 9)
			setBounds(100, 100, 450, 300);
		else
			setBounds(100, 100, 460, 305);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		JPanel interactPanel = new JPanel();
		interactPanel.setPreferredSize(new Dimension(414, 45));
		interactPanel.setLayout(new BorderLayout());

		chckbxDontShowThis = new JCheckBox(Main.resourceManager.getString("ui.label.assumpt.hide"));
		if (Main.isLinux())
			chckbxDontShowThis.setFont(chckbxDontShowThis.getFont().deriveFont(12.0f));
		chckbxDontShowThis.addActionListener(this);
		chckbxDontShowThis.setBounds(10, 5, 277, 41);
		chckbxDontShowThis.setSelected(prefs.getBoolean(
				"HideAssumptionsOnStartup", false));
		interactPanel.add(chckbxDontShowThis, BorderLayout.WEST);

		RButton btnClose = new RButton(Main.resourceManager.getString("ui.label.assumpt.close"), RButton.Decoration.Close);
		btnClose.addActionListener((e) -> close());
		btnClose.setBounds(293, 5, 121, 41);
		interactPanel.add(btnClose, BorderLayout.EAST);

		/*
		JEditorPane browser = new JEditorPane();
		browser.setEditable(false);
		String location = MapTab.getHTMLLocation().replace('\\', '/');
		if (Main.resourceManager.loc.getISO3Language().contains("fr"))
			location += "/assumptions2_fr.htm";
		else
			location += "/assumptions2.htm";
		browser.setContentType("text/html");
		try {
			browser.setPage(location);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		*/
		JTextArea editor = new JTextArea();
		editor.setEditable(false);
		editor.setText(Main.resourceManager.getString("ui.label.assumptions.text"));
		editor.setLineWrap(true);
		editor.setWrapStyleWord(true);

		//editor.setPreferredSize(new Dimension(0, 400));



		JScrollPane pane = new JScrollPane(editor);

		pane.setBounds(10, 11, 414, 191);
		pane.setPreferredSize(new Dimension(414, 191));
		contentPanel.add(pane);
		editor.setCaretPosition(0);

		addWindowListener(new WindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				assump = null;
			}

			@Override
			public void windowClosed(WindowEvent e) { }
			@Override
			public void windowActivated(WindowEvent e) { }
			@Override
			public void windowOpened(WindowEvent e) { }
			@Override
			public void windowIconified(WindowEvent e) { }
			@Override
			public void windowDeiconified(WindowEvent e) { }
			@Override
			public void windowDeactivated(WindowEvent e) { }
		});

		contentPanel.add(interactPanel);
		
		pack();
	}

	private void close() {
		assump = null;
		this.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		prefs.putBoolean("HideAssumptionsOnStartup",
				chckbxDontShowThis.isSelected());
	}
}
