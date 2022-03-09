/***********************************************************************
 * REDapp - BusyDialog.java
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

public class BusyDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private BusyPanel panel;
	private int posX = 0;
	private int posY = 0;

	public BusyDialog(Window owner) {
		super(owner);
		setResizable(false);
		setModal(true);
		initialize();
		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) { }
			@Override
			public void windowIconified(WindowEvent e) { }
			@Override
			public void windowDeiconified(WindowEvent e) { }
			@Override
			public void windowDeactivated(WindowEvent e) { }
			@Override
			public void windowActivated(WindowEvent e) { }
			@Override
			public void windowClosing(WindowEvent e) {}

			@Override
			public void windowClosed(WindowEvent e) {
				panel.stop();
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				posX = e.getX();
				posY = e.getY();
			}
		});
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				BusyDialog.this.setLocation(e.getXOnScreen() - posX, e.getYOnScreen() - posY);
			}
		});
		setDialogPosition(owner);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setUndecorated(true);
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
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo.png")));
		setTitle("Working");
		setBounds(0, 0, 116, 116);
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 116, 116);
		else
			setBounds(0, 0, 126, 126);
		getRootPane().setOpaque(false);
		if (Main.isLinux()) {
			getRootPane().setBorder(new LineBorder(new Color(173, 169, 165), 1, false));
		}
		else if (Main.isMac()) {
			getRootPane().setBorder(new LineBorder(new Color(144, 144, 144), 1, false));
		}
		else {
			getRootPane().setBorder(new CompoundBorder(new LineBorder(new Color(37, 44, 51), 1, true),
					new CompoundBorder(new LineBorder(new Color(246, 250, 254), 1, true),
					new CompoundBorder(new LineBorder(new Color(208, 229, 250), 1, true),
					new CompoundBorder(new LineBorder(new Color(207, 228, 250), 1, true),
					new CompoundBorder(new LineBorder(new Color(206, 227, 248), 1, false),
					new CompoundBorder(new LineBorder(new Color(204, 225, 247), 1, false),
					new CompoundBorder(new LineBorder(new Color(231, 241, 250), 1, false),
					new LineBorder(new Color(88, 102, 117), 1, false)))))))));
		}
		panel = new BusyPanel();
		add(panel);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible)
			panel.start();
		super.setVisible(visible);
	}

	private static class BusyPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private int counter = 0;
		private static final int numCircles = 8;
		private Timer timer;

		public BusyPanel() {
			super(true);
			setLayout(null);
			timer = new Timer();
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(100, 100);
		}

		@Override
		public Dimension getMinimumSize() {
			return new Dimension(100, 100);
		}

		public void start() {
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					counter++;
					repaint();
				}
			}, 25, 25);
		}

		public void stop() {
			timer.cancel();
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for (int i = 0; i < numCircles; i++) {
				if ((this.counter / 3) % numCircles == i)
					g.setColor(new Color(199, 73, 73));
				else if ((this.counter / 3) % numCircles == (i + 1 == numCircles ? 0 : i + 1))
					g.setColor(new Color(155, 99, 99));
				else if ((this.counter / 3) % numCircles == (i + 2 > numCircles ? 1 : (i + 2 == numCircles ? 0 : i + 2)))
					g.setColor(new Color(132, 113, 113));
				else
					g.setColor(new Color(127, 127, 127));
				g.fillOval((int)(getWidth() / 2 + 30 * Math.cos(2 * Math.PI * i / numCircles) - 10),
						(int)(getHeight() / 2 + 30 * Math.sin(2 * Math.PI * i / numCircles) - 10), 20, 20);
			}
		}
	}
}
