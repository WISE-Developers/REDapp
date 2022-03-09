/***********************************************************************
 * REDapp - CreateTileInputDialog.java
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

package ca.redapp.map;

import static ca.redapp.util.LineEditHelper.getDoubleFromLineEdit;
import static ca.redapp.util.LineEditHelper.lineEditHandleError;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import java.util.Locale;
import java.util.prefs.Preferences;

import ca.redapp.ui.Import;
import ca.redapp.ui.Main;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RGroupBox;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;

public class CreateTileInputDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private RButton btnCancel;
	private RButton btnEnter;
	
	private RTextField txtMinLat;
	private RTextField txtMinLong;
	private RTextField txtMinZoom;
	
	private RTextField txtMaxLat;
	private RTextField txtMaxLong;
	private RTextField txtMaxZoom;
	
	private long retval;
	private double lon = 0;
	private double lat = 0;
	
	private Preferences prefs = Preferences.userRoot().node("ca.hss.app.redapp.ui.Main");
	
	public CreateTileInputDialog(Window owner, double lon, double lat) {
		super(owner);
		
		this.lon = lon;
		this.lat = lat;
		setResizable(false);
		setModal(true);
		initialize();

		btnCancel.addActionListener((e) -> cancel());
		btnEnter.addActionListener((e) -> enter());
		
		setDialogPosition(owner);
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

	public Double getMinLat() {
		return ca.redapp.util.DoubleEx.valueOf(txtMinLat.getText());
	}
	
	public Double getMinLong() {
		return ca.redapp.util.DoubleEx.valueOf(txtMinLong.getText());
	}
	
	public Double getMaxLat() {
		return ca.redapp.util.DoubleEx.valueOf(txtMaxLat.getText());
	}
	
	public Double getMaxLong() {
		return ca.redapp.util.DoubleEx.valueOf(txtMaxLong.getText());
	}
	
	public int getMinZoom() {
		return Integer.parseInt(txtMinZoom.getText());
	}
	
	public int getMaxZoom() {
		return Integer.parseInt(txtMaxZoom.getText());
	}
	
	public void cancel() {
		retval = ca.hss.general.ERROR.SEVERITY_WARNING;
		dispose();
	}

	public void enter() {
		if(validForm()) {
			retval = 1;
			dispose();
		} else {
			JOptionPane.showMessageDialog(null,
					Main.resourceManager.getString("ui.label.range.invalid"),
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public long getResult() {
		return retval;
	}
	
	public boolean validForm() {
		boolean valid = true;
		
		double value1 = Double.MAX_VALUE;
		double value2 = Double.MAX_VALUE;
		try { value1 = getDoubleFromLineEdit(txtMinLat); } catch (NullPointerException e) {}
		try { value2 = getDoubleFromLineEdit(txtMaxLat); } catch (NullPointerException e) {}
		if ((value1 > 90 || value1 < -90)) {
			valid = false;
			lineEditHandleError(txtMinLat, Main.resourceManager.getString("ui.label.range.lat"));
			
		} if (value2 > 90 || value2 < -90) { 
			valid = false;
			lineEditHandleError(txtMaxLat, Main.resourceManager.getString("ui.label.range.lat"));
		
		} if (value2 < value1) {
			valid = false;
			lineEditHandleError(txtMaxLat, Main.resourceManager.getString("ui.label.range.greaterLat"));
		}
		
		value1 = Double.MAX_VALUE;
		value2 = Double.MAX_VALUE;
		try { value1 = getDoubleFromLineEdit(txtMinLong); } catch (NullPointerException e) {}
		try { value2 = getDoubleFromLineEdit(txtMaxLong); } catch (NullPointerException e) {}
		if (value1 > 180 || value1 < -180) {
			valid = false;
			lineEditHandleError(txtMinLong, Main.resourceManager.getString("ui.label.range.lon"));
			
		} if (value2 > 180 || value2 < -180) { 
			valid = false;
			lineEditHandleError(txtMaxLong, Main.resourceManager.getString("ui.label.range.lon"));
		
		} if (value2 < value1) {
			valid = false;
			lineEditHandleError(txtMaxLong, Main.resourceManager.getString("ui.label.range.greaterLon"));
		}
		
		value1 = Double.MAX_VALUE;
		value2 = Double.MAX_VALUE;
		try { value1 = getDoubleFromLineEdit(txtMinZoom); } catch (NullPointerException e) {}
		try { value2 = getDoubleFromLineEdit(txtMaxZoom); } catch (NullPointerException e) {}
		if (value1 > 19 || value1 < 0) {
			valid = false;
			lineEditHandleError(txtMinZoom, Main.resourceManager.getString("ui.label.range.zoom"));
			
		} if (value2 > 19 || value2 < 0) { 
			valid = false;
			lineEditHandleError(txtMaxZoom, Main.resourceManager.getString("ui.label.range.zoom"));
		
		}if (value2 < value1) {
			valid = false;
			lineEditHandleError(txtMaxZoom, Main.resourceManager.getString("ui.label.range.greaterZoom"));
		}
		
		return valid;
	}

	private void initialize() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo_20.png")));
		setTitle(Main.resourceManager.getString("ui.dlg.title.create.tileinput"));
		setBounds(0, 0, 255, 275);
		getContentPane().setLayout(null);
		
		String defLanguage = Locale.getDefault().getISO3Language();
		int def = 0;
		if (defLanguage.indexOf("fr") >= 0)
			def = 1;
		
		int lang = prefs.getInt("language", def);

		RLabel lblWarning = new RLabel(Main.resourceManager.getString("ui.label.tile.warning"));

		lblWarning.setBounds(10, 10, 230, 40);
		
		getContentPane().add(lblWarning);
		
		btnCancel = new RButton(Main.resourceManager.getString("ui.label.edit.cancel"));
		btnCancel.setBounds(10, 200, 121, 41);
		
		getContentPane().add(btnCancel);

		btnEnter = new RButton(Main.resourceManager.getString("ui.label.edit.enter"));

		if (lang == 0)
			btnEnter.setBounds(160, 200, 121, 41);
		else
			btnEnter.setBounds(120, 200, 121, 41);
			
		getContentPane().add(btnEnter);
		
		RGroupBox panel_1 = new RGroupBox();
		panel_1.setText(Main.resourceManager.getString("ui.label.tile.coords"));
		panel_1.setBounds(10, 57, 230, 80);
		getContentPane().add(panel_1);
		
		RLabel lblMinLat = new RLabel(Main.resourceManager.getString("ui.label.tile.minlat"));
		lblMinLat.setBounds(10, 20, 101, 20);
		panel_1.add(lblMinLat);

		RLabel lblMinLong = new RLabel(Main.resourceManager.getString("ui.label.tile.minlong"));
		lblMinLong.setBounds(10, 50, 101, 20);
		panel_1.add(lblMinLong);
		
		txtMinLat = new RTextField();
		txtMinLat.setBounds(60, 20, 40, 20);
		txtMinLat.setText(lat == Double.MAX_VALUE ? "" : "" + (lat - 0.1));
		panel_1.add(txtMinLat);

		txtMinLong = new RTextField();
		txtMinLong.setBounds(60, 50, 40, 20);
		txtMinLong.setText(lon == Double.MAX_VALUE ? "" : "" + (lon - 0.2));
		panel_1.add(txtMinLong);
		
		RLabel lblMaxLat = new RLabel(Main.resourceManager.getString("ui.label.tile.maxlat"));
		lblMaxLat.setBounds(130, 20, 101, 20);
		panel_1.add(lblMaxLat);

		RLabel lblMaxLong = new RLabel(Main.resourceManager.getString("ui.label.tile.maxlong"));
		lblMaxLong.setBounds(130, 50, 101, 20);
		panel_1.add(lblMaxLong);
		
		txtMaxLat = new RTextField();
		txtMaxLat.setBounds(180, 20, 40, 20);
		txtMaxLat.setText(lat == Double.MAX_VALUE ? "" : "" + (lat + 0.1));
		panel_1.add(txtMaxLat);

		txtMaxLong = new RTextField();
		txtMaxLong.setBounds(180, 50, 40, 20);
		txtMaxLong.setText(lon == Double.MAX_VALUE ? "" : "" + (lon + 0.2));
		panel_1.add(txtMaxLong);
		
		
		RGroupBox panel_2 = new RGroupBox();
		panel_2.setText(Main.resourceManager.getString("ui.label.tile.zoom"));
		panel_2.setBounds(10, 147, 230, 50);
		getContentPane().add(panel_2);

		RLabel lblMinZoom = new RLabel(Main.resourceManager.getString("ui.label.tile.minzoom"));
		lblMinZoom.setBounds(10, 20, 101, 20);
		panel_2.add(lblMinZoom);
		
		txtMinZoom = new RTextField();
		txtMinZoom.setBounds(60, 20, 40, 20);
		txtMinZoom.setText("" + 0);
		panel_2.add(txtMinZoom);
		
		RLabel lblMaxZoom = new RLabel(Main.resourceManager.getString("ui.label.tile.maxzoom"));
		lblMaxZoom.setBounds(130, 20, 101, 20);
		panel_2.add(lblMaxZoom);
		
		txtMaxZoom = new RTextField();
		txtMaxZoom.setBounds(180, 20, 40, 20);
		txtMaxZoom.setText("" + 8);
		panel_2.add(txtMaxZoom);
	}
}