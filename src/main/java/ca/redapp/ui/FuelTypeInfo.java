/***********************************************************************
 * REDapp - FuelTypeInfo.java
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

import javax.imageio.ImageIO;
import javax.swing.JDialog;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import javax.swing.JScrollPane;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import ca.redapp.ui.component.RLabel;
import ca.redapp.util.REDappLogger;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FuelTypeInfo extends JDialog {
	private static final long serialVersionUID = 1L;
	RLabel lblImage;
	int fuelType;
	int currentImageIndex;
	final String[][] imagePaths = {{// C1: Spruce-Lichen Woodland
			"/images/fueltype/C1a.png",
			"/images/fueltype/C1b.png"
		},{ // C2: Boreal Spruce
			"/images/fueltype/C2a.png",
			"/images/fueltype/C2b.png"
		},{// C3: Mature Jack or Lodgepole Pine
			"/images/fueltype/C3a.png",
			"/images/fueltype/C3b.png"
		},{ // C4: Immature Jack or Lodgepole Pine
			"/images/fueltype/C4a.png",
			"/images/fueltype/C4b.png"
		},{ // C5: Red and White Pine
			"/images/fueltype/C5a.png",
			"/images/fueltype/C5b.png"
		},{ // C6: Conifer Plantation
			"/images/fueltype/C6a.png",
			"/images/fueltype/C6b.png"
		},{// C7: Ponderosa Pine / Douglas Fir
			"/images/fueltype/C7a.png",
			"/images/fueltype/C7b.png"
		},{// D1: Leafless Aspen
			"/images/fueltype/D1a.png",
			"/images/fueltype/D1b.png"
		},{ // D2: Green Aspen (w/ BUI Thresholding)
			"/images/fueltype/D2a.png",
		},{ // D-1/D-2:  Aspen
		},{ // M1: Boreal Mixedwood-Leafless
			"/images/fueltype/M1a.png",
			"/images/fueltype/M1b.png"
		},{ // M2: Boreal Mixedwood-Green
			"/images/fueltype/M2a.png",
			"/images/fueltype/M2b.png"
		},{ // M-1/M-2:  Boreal Mixedwood
		},{// M3: Dead Balsam Fir Mixedwood-Leafless
			"/images/fueltype/M3a.png",
			"/images/fueltype/M3b.png"
		},{ // M4: Dead Balsam Fir Mixedwood-Green
			"/images/fueltype/M4a.png",
			"/images/fueltype/M4b.png"
		},{ // M-3/M-4:  Dead Balsam Fir / Mixedwood
		},{// O1a: Matted Grass
			"/images/fueltype/O1a_a.png",
			"/images/fueltype/O1a_b.png"
		},{ // O1b: Standing Grass
			"/images/fueltype/O1b_a.png",
			"/images/fueltype/O1b_b.png"
		},{// O1ab: Matted/Standing Grass
			"/images/fueltype/O1ab_a_a.png",
			"/images/fueltype/O1ab_a_b.png",
			"/images/fueltype/O1ab_b_a.png",
			"/images/fueltype/O1ab_b_b.png"
		},{// S1: Jack or Lodgepole Pine Slash
			"/images/fueltype/S1a.png",
			"/images/fueltype/S1b.png"
		},{ // S2: White Spruce/Balsam Slash
			"/images/fueltype/S2a.png",
			"/images/fueltype/S2b.png"
		},{ // S3: Coastal Cedar/Hemlock/Douglas-Fir Slash
			"/images/fueltype/S3a.png",
			"/images/fueltype/S3b.png"
		}
	};
	private JButton btnPrevious;
	private JButton btnNext;

	private String getDescription(int FuelType) {
		switch (FuelType) {
		case 0: // C1: Spruce-Lichen Woodland
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.c1");

		case 1: // C2: Boreal Spruce
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.c2");

		case 2: // C3: Mature Jack or Lodgepole Pine
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.c3");

		case 3: // C4: Immature Jack or Lodgepole Pine
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.c4");

		case 4: // C5: Red and White Pine
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.c5");

		case 5: // C6: Conifer Plantation
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.c6");

		case 6: // C7: Ponderosa Pine / Douglas Fir
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.c7");

		case 7: // D1: Leafless Aspen
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.d1");

		case 8: // D2: Populus tremuloides Michx
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.d2");

		case 10: // M1: Boreal Mixedwood-Leafless
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.m1");

		case 11: // M2: Boreal Mixedwood-Green
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.m2");

		case 13: // M3: Dead Balsam Fir Mixedwood-Leafless
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.m3");

		case 14: // M4: Dead Balsam Fir Mixedwood-Green
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.m4");

		case 16: // O1a: Matted Grass
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.o1a");
			
		case 17: // O1b: Standing Grass
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.o1b");

		/*case 18: //O1ab: Matted/Standing Grass
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.o1ab");*/

		case 19: // S1: Jack or Lodgepole Pine Slash
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.s1");

		case 20: // S2: White Spruce/Balsam Slash
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.s2");

		case 21: // S3: Coastal Cedar/Hemlock/Douglas-Fir Slash
			return Main.resourceManager.getString("ui.label.fuelinfo.fuel.s3");
		}
		return "";
	}

	private String getName(int FuelType) {
		switch (FuelType) {
		case 0:
			return "C1: " + Main.resourceManager.getString("ui.label.fuel.c1");

		case 1:
			return "C2: " + Main.resourceManager.getString("ui.label.fuel.c2");

		case 2:
			return "C3: " + Main.resourceManager.getString("ui.label.fuel.c3");

		case 3:
			return "C4: " + Main.resourceManager.getString("ui.label.fuel.c4");

		case 4:
			return "C5: " + Main.resourceManager.getString("ui.label.fuel.c5");

		case 5:
			return "C6: " + Main.resourceManager.getString("ui.label.fuel.c6");

		case 6:
			return "C7: " + Main.resourceManager.getString("ui.label.fuel.c7");

		case 7:
			return "D1: " + Main.resourceManager.getString("ui.label.fuel.d1");

		case 8:
			return "D2: " + Main.resourceManager.getString("ui.label.fuel.d2");

		case 10:
			return "M1: " + Main.resourceManager.getString("ui.label.fuel.m1");

		case 11:
			return "M2: " + Main.resourceManager.getString("ui.label.fuel.m2");

		case 13:
			return "M3: " + Main.resourceManager.getString("ui.label.fuel.m3");

		case 14:
			return "M4: " + Main.resourceManager.getString("ui.label.fuel.m4");

		case 16:
			return "O1a: " + Main.resourceManager.getString("ui.label.fuel.o1a");

		case 17:
			return "O1b: " + Main.resourceManager.getString("ui.label.fuel.o1b");
			
		/*case 18:
			return "O1ab: " + Main.resourceManager.getString("ui.label.fuel.o1ab");*/

		case 19:
			return "S1: " + Main.resourceManager.getString("ui.label.fuel.s1");

		case 20:
			return "S2: " + Main.resourceManager.getString("ui.label.fuel.s2");

		case 21:
			return "S3: " + Main.resourceManager.getString("ui.label.fuel.s3");
		}
		return "";
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
	public FuelTypeInfo(Main app, int FuelType) {
		super(app.frmRedapp);
		this.fuelType = FuelType;
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setTitle("FBP Fuel Type Information -> " + getName(FuelType));
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
		if (Launcher.javaVersion.major < 9)
			setBounds(100, 100, 527, 467);
		else
			setBounds(100, 100, 537, 472);
		getContentPane().setLayout(null);

		btnNext = new JButton(Main.resourceManager.getString("ui.label.fuelinfo.next"));
		btnNext.addActionListener((e) -> {
			updateImage(currentImageIndex+1);
		});
		if (Main.isMac())
			btnNext.setBounds(375, 310, 120, 24);
		else
			btnNext.setBounds(395, 310, 100, 24);
		getContentPane().add(btnNext);

		btnPrevious = new JButton(Main.resourceManager.getString("ui.label.fuelinfo.previous"));
		btnPrevious.addActionListener((e) -> {
			updateImage(currentImageIndex-1);
		});
		if (Main.isMac())
			btnPrevious.setBounds(250, 310, 120, 24);
		else
			btnPrevious.setBounds(290, 310, 100, 24);
		getContentPane().add(btnPrevious);

		JScrollPane scrollDescription = new JScrollPane();
		scrollDescription.setBounds(10, 340, 501, 91);
		getContentPane().add(scrollDescription);

		JTextArea txtDescription = new JTextArea();
		if (Main.isWindows())
			txtDescription.setFont(new Font("Arial", 0, 12));
		txtDescription.setText(this.getDescription(FuelType));
		txtDescription.setWrapStyleWord(true);
		txtDescription.setLineWrap(true);
		scrollDescription.setViewportView(txtDescription);
		txtDescription.setCaretPosition(0);
		txtDescription.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(final MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					final JTextComponent component = (JTextComponent)e.getComponent();
					final JPopupMenu menu = new JPopupMenu();
					JMenuItem item;
					item = new JMenuItem(new DefaultEditorKit.CopyAction());
					item.setText(Main.resourceManager.getString("ui.label.editor.copy"));
					item.setEnabled(component.getSelectionStart() != component.getSelectionEnd());
					menu.add(item);
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		lblImage = new RLabel("");
		lblImage.setBounds(10, 10, 501, 281);
		getContentPane().add(lblImage);
		updateImage(0);
		setDialogPosition(app.frmRedapp);
		lblImage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(final MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					final JPopupMenu menu = new JPopupMenu();
					JMenuItem item;
					item = new JMenuItem();
					item.setText(Main.resourceManager.getString("ui.label.editor.copy"));
					item.addActionListener((f) -> {
						new CopyImageToClipBoard(imagePaths, fuelType, currentImageIndex);
					});
					menu.add(item);
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	public static BufferedImage resize(BufferedImage image, int width, int height) {
	    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
	    Graphics2D g2d = (Graphics2D) bi.createGraphics();
	    g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
	    g2d.drawImage(image, 0, 0, width, height, null);
	    g2d.dispose();
	    return bi;
	}

	private void updateImage(int imageIndex){
		currentImageIndex = imageIndex;
		try {
			BufferedImage img = ImageIO.read(Main.class.getResource(imagePaths[fuelType][imageIndex]));
			img = resize(img, lblImage.getWidth(), lblImage.getHeight());
			ImageIcon ic = new ImageIcon(img);
			lblImage.setIcon(ic);
			btnPrevious.setEnabled(currentImageIndex > 0);
			btnNext.setEnabled(currentImageIndex+1 < imagePaths[fuelType].length);
		} catch (IOException e) {
		}
	}

	private static class CopyImageToClipBoard implements ClipboardOwner {
		public CopyImageToClipBoard(String[][] paths, int type, int current) {
			try {
				BufferedImage img = ImageIO.read(Main.class.getResource(paths[type][current]));
				TransferableImage trans = new TransferableImage(img);
				Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
				c.setContents(trans, this);
			}
			catch (IOException e) {
				REDappLogger.error("Error setting clipboard contents.", e);
			}
		}

		public void lostOwnership(Clipboard clip, Transferable trans) { }
	}

	private static class TransferableImage implements Transferable {
		Image i;

		public TransferableImage(Image i) {
			this.i = i;
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (flavor.equals(DataFlavor.imageFlavor) && i != null) {
				return i;
			}
			throw new UnsupportedFlavorException(flavor);
		}

		public DataFlavor[] getTransferDataFlavors() {
			DataFlavor[] flavors = new DataFlavor[1];
			flavors[0] = DataFlavor.imageFlavor;
			return flavors;
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			DataFlavor[] flavors = getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				if (flavor.equals(flavors[i]))
					return true;
			}
			return false;
		}
	}
}
