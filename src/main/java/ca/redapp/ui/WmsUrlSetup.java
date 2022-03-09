/***********************************************************************
 * REDapp - WmsUrlSetup.java
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
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.data.imagery.LayerDetails;
import org.openstreetmap.josm.io.imagery.WMSImagery;

import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RComboBox;
import ca.redapp.ui.component.RGroupBox;
import ca.redapp.ui.component.RTextField;
import ca.redapp.ui.component.SpringUtilities;

public class WmsUrlSetup extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	
	private RTextField txtGetCapUrl;
	
	private JScrollPane scrollGenUrl;
	private JTextArea txtGenUrl;
	
	private RComboBox<String> imgFormCmb;
	private List<String> formatDetails = new ArrayList<String>();
	
	private JScrollPane layerScroll;
	private DefaultListModel<LayerList> layerModel;
	private JList<LayerList> layerList;
	private List<LayerList> layerDetails = new ArrayList<LayerList>();
	
	private byte retval;
	private String builtURL = "";
	
	UrlUpdateWorker worker = null;
	Thread workerThread = null;
	BusyDialog busyDialog = null;
	
	private boolean error = false;
	private WMSImagery wms = null;
	String url = "";
	
	private RButton btnSave;

	public WmsUrlSetup(JDialog parent) {
		super(parent);
		
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setTitle(Main.resourceManager.getString("ui.dlg.title.settings"));
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
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		RGroupBox panelCapUrl = new RGroupBox();
		panelCapUrl.setText(Main.resourceManager.getString("ui.label.wmsurl.getcapurl"));
		panelCapUrl.setLayout(new SpringLayout());
		contentPanel.add(panelCapUrl);

		txtGetCapUrl = new RTextField();
		txtGetCapUrl.setPreferredSize(new Dimension(320, 20));
		txtGetCapUrl.setHorizontalAlignment(JTextField.LEFT);
		panelCapUrl.add(txtGetCapUrl);
		
		RButton btnRefresh = new RButton(Main.resourceManager.getString("ui.label.wmsurl.refresh"));
		btnRefresh.addActionListener((e) -> refreshURL());
		panelCapUrl.add(btnRefresh);

		SpringUtilities.makeCompactGrid(panelCapUrl, 2, 1, 6, 6, 6, 6);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		RGroupBox panelAvailLayers = new RGroupBox();
		panelAvailLayers.setText(Main.resourceManager.getString("ui.label.wmsurl.availlayers"));
		panelAvailLayers.setLayout(new SpringLayout());
		contentPanel.add(panelAvailLayers);
		
		layerModel = new DefaultListModel<>();
		layerList = new JList<>();

		layerList.setModel(layerModel);
		
		layerList.setLayoutOrientation(JList.VERTICAL);
		layerList.setVisibleRowCount(10);
		
		layerList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				regenURL();
			}
		});
		
		layerScroll = new JScrollPane(layerList);
		layerScroll.setPreferredSize(new Dimension(320, 200));
		panelAvailLayers.add(layerScroll);
		
		panelAvailLayers.add(Box.createRigidArea(new Dimension(0, 5)));
		
		SpringUtilities.makeCompactGrid(panelAvailLayers, 2, 1, 6, 6, 6, 6);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		RGroupBox panelImgForm = new RGroupBox();
		panelImgForm.setText(Main.resourceManager.getString("ui.label.wmsurl.imgform"));
		panelImgForm.setLayout(new SpringLayout());
		contentPanel.add(panelImgForm);
		
		imgFormCmb = new RComboBox<String>();
		imgFormCmb.setWide(true);
		
		imgFormCmb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				regenURL();
			}
        });
		
		panelImgForm.add(imgFormCmb);
		
		panelImgForm.add(Box.createRigidArea(new Dimension(0, 5)));
		
		SpringUtilities.makeCompactGrid(panelImgForm, 2, 1, 6, 6, 6, 6);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		RGroupBox panelGenUrl = new RGroupBox();
		panelGenUrl.setLayout(new SpringLayout());
		panelGenUrl.setText(Main.resourceManager.getString("ui.label.wmsurl.genurl"));
		contentPanel.add(panelGenUrl);
		
		txtGenUrl = new JTextArea();
		txtGenUrl.setLineWrap(true);
		
		txtGenUrl.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				btnSave.setEnabled(!(txtGenUrl.getText().length() == 0));
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				btnSave.setEnabled(!(txtGenUrl.getText().length() == 0));
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				btnSave.setEnabled(!(txtGenUrl.getText().length() == 0));
			}
			
		});
		
		scrollGenUrl = new JScrollPane(txtGenUrl);
		scrollGenUrl.setPreferredSize(new Dimension(320, 100));
		panelGenUrl.add(scrollGenUrl);
		
		panelGenUrl.add(Box.createRigidArea(new Dimension(0, 5)));
		
		SpringUtilities.makeCompactGrid(panelGenUrl, 2, 1, 6, 6, 6, 6);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		JPanel panel1 = new JPanel();
		panel1.setLayout(new FlowLayout(FlowLayout.CENTER));
		contentPanel.add(panel1);
		
		RButton btnCancel = new RButton(Main.resourceManager.getString("ui.label.settings.close"), RButton.Decoration.Close);
		btnCancel.addActionListener((e) -> cancel());
		btnCancel.setBounds(250, 414, 121, 41);

		btnSave = new RButton(Main.resourceManager.getString("ui.label.settings.ok"));
		btnSave.setEnabled(false);
		btnSave.addActionListener((e) -> save());
		btnSave.setBounds(130, 414, 121, 41);

		JPanel panel2 = new JPanel();
		panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel1.add(panel2);
		panel2.add(btnSave);
		panel2.add(btnCancel);
		
		pack();
		setDialogPosition(parent);
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
		retval = 1;
		builtURL = txtGenUrl.getText();
		dispose();
	}
	
	private void cancel() {
		retval = 0;
		dispose();
	}
	
	public byte getResult() {
		return retval;
	}
	
	public String getURL() {
		return builtURL;
	}
	
	private void refreshURL() {
		String url = txtGetCapUrl.getText();
		
		if(url.length() != 0) {
			error = false;
			
			formatDetails = new ArrayList<String>();
			layerDetails = new ArrayList<LayerList>();
			layerModel.removeAllElements();
			
			worker = new UrlUpdateWorker();
			worker.url = url;
	
			workerThread = new Thread(worker);
			busyDialog = new BusyDialog(this);
			workerThread.start();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					busyDialog.setModal(true);
					busyDialog.setVisible(true);
					
					if (error) {
						JOptionPane.showMessageDialog(null,
								Main.resourceManager.getString("ui.label.wmsurl.refresherror"), "Error",
								JOptionPane.WARNING_MESSAGE);
					} else {
						String[] arr = new String[formatDetails.size()];
						imgFormCmb.setModel(new DefaultComboBoxModel<String>(formatDetails.toArray(arr)));
	
						for(int i = 0; i < layerDetails.size(); i++) {
							LayerList l = layerDetails.get(i);
							layerModel.addElement(l);
						}
					}
	
					contentPanel.repaint();
					contentPanel.revalidate();
				}
			});
		}
	}
	
	private void regenURL() {
		if(wms != null) {
			List<String> selLayers = new ArrayList<>();
			
			for(LayerList l : layerList.getSelectedValuesList()) {
				selLayers.add(l.name);
			}
			String selFormat = String.valueOf(imgFormCmb.getSelectedItem());
			
			url = wms.buildGetMapUrl(selLayers, null, selFormat, true);
			
			txtGenUrl.setText(url);
		}
	}
	
	protected class UrlUpdateWorker implements Runnable {
		public String url = "";
		
		@Override
		public void run() {
			try {
				wms = new WMSImagery(url);
				
				Collection<String> formats = wms.getFormats();
				//these will go in the image format combobox
				for (String format : formats) {
					formatDetails.add(format);
				}
				//the layers are stored as children of the items returned from getLayers
				List<LayerDetails> layers = wms.getLayers().stream().map(LayerDetails::getChildren).flatMap(List::stream).collect(Collectors.toList());
				//these are for the available layers list. Display title to the user but name will be needed later for the URL.
				for (LayerDetails layer : layers) {
					layerDetails.add(new LayerList(layer.getTitle(), layer.getName()));
				}
				
				layerDetails.sort(Comparator.comparing(LayerList::toString, String.CASE_INSENSITIVE_ORDER));
			} catch (Exception e) {
				e.printStackTrace();
				error = true;
			}
			
			worker = null;
			workerThread = null;
			busyDialog.setVisible(false);
			busyDialog = null;
		}
	}
	
	protected class LayerList {
		private String title = "";
		private String name = "";
		
		public LayerList(String title, String name) {
			this.title = title;
			this.name = name;
		}
		
		@Override
		public String toString() {
			return title;
		}
		
		public String getName() {
			return name;
		}
	}
}

