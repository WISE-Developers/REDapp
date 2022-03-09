/***********************************************************************
 * REDapp - CreateTempWSDialog.java
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

import javax.swing.JDialog;

import ca.hss.general.DecimalUtils;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RGroupBox;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;

public class CreateTempWSDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private RButton btnCancel;
	private RButton btnSave;
	
	private RTextField txtTempAlpha;
	private RTextField txtTempBeta;
	private RTextField txtTempGamma;
	private RTextField txtWSGamma;
	private RTextField txtWSBeta;
	private RTextField txtWSAlpha;
	
	private final Double DEFAULT_TEMP_ALPHA = -0.77;
	private final Double DEFAULT_TEMP_BETA = 2.80;
	private final Double DEFAULT_TEMP_GAMMA = -2.20;
	private final Double DEFAULT_WIND_ALPHA = 1.00;
	private final Double DEFAULT_WIND_BETA = 1.24;
	private final Double DEFAULT_WIND_GAMMA = -3.59;
	
	private long retval = -1;
	
	public CreateTempWSDialog(Window owner) {
		super(owner);
		setResizable(false);
		setModal(true);
		initialize();

		btnCancel.addActionListener((e) -> cancel());
		btnSave.addActionListener((e) -> save());
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

	public Double getTempAlpha() {
		return ca.redapp.util.DoubleEx.valueOf(txtTempAlpha.getText());
	}
	
	public Double getTempBeta() {
		return ca.redapp.util.DoubleEx.valueOf(txtTempBeta.getText());
	}
	
	public Double getTempGamma() {
		return ca.redapp.util.DoubleEx.valueOf(txtTempGamma.getText());
	}
	
	public Double getWSAlpha() {
		return ca.redapp.util.DoubleEx.valueOf(txtWSAlpha.getText());
	}
	
	public Double getWSBeta() {
		return ca.redapp.util.DoubleEx.valueOf(txtWSBeta.getText());
	}
	
	public Double getWSGamma() {
		return ca.redapp.util.DoubleEx.valueOf(txtWSGamma.getText());
	}
	
	public void cancel() {
		retval = ca.hss.general.ERROR.SEVERITY_WARNING;
		dispose();
	}

	public void save() {
		retval = 1;
		dispose();
	}
	
	public long getResult() {
		return retval;
	}

	private void initialize() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo_20.png")));
		setTitle(Main.resourceManager.getString("ui.dlg.title.create.tempws"));
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 255, 340);
		else
			setBounds(0, 0, 265, 345);
		getContentPane().setLayout(null);

		btnCancel = new RButton(Main.resourceManager.getString("ui.label.edit.cancel"));
		btnCancel.setBounds(10, 260, 121, 41);
		getContentPane().add(btnCancel);

		btnSave = new RButton(Main.resourceManager.getString("ui.label.edit.save"));
		btnSave.setBounds(165, 260, 121, 41);
		getContentPane().add(btnSave);

		RGroupBox panel_1 = new RGroupBox();
		panel_1.setText(Main.resourceManager.getString("ui.label.stats.duirnal.temp"));
		panel_1.setBounds(10, 20, 230, 110);
		getContentPane().add(panel_1);
		
		RLabel lblStatsTempAlpha = new RLabel("\u03B1");
		lblStatsTempAlpha.setBounds(10, 20, 101, 20);
		panel_1.add(lblStatsTempAlpha);

		RLabel lblStatsTempBeta = new RLabel("\u03B2");
		lblStatsTempBeta.setBounds(10, 50, 101, 20);
		panel_1.add(lblStatsTempBeta);

		RLabel lblStatsTempGamma = new RLabel("\u03B3");
		lblStatsTempGamma.setBounds(10, 80, 101, 20);
		panel_1.add(lblStatsTempGamma);
		
		txtTempAlpha = new RTextField();
		txtTempAlpha.setBounds(117, 20, 84, 20);
		txtTempAlpha.setText(DecimalUtils.format(DEFAULT_TEMP_ALPHA));
		panel_1.add(txtTempAlpha);

		txtTempBeta = new RTextField();
		txtTempBeta.setBounds(117, 50, 84, 20);
		txtTempBeta.setText(DecimalUtils.format(DEFAULT_TEMP_BETA));
		panel_1.add(txtTempBeta);

		txtTempGamma = new RTextField();
		txtTempGamma.setBounds(117, 80, 84, 20);
		txtTempGamma.setText(DecimalUtils.format(DEFAULT_TEMP_GAMMA));
		panel_1.add(txtTempGamma);
		
		RGroupBox panel_2 = new RGroupBox();
		panel_2.setText(Main.resourceManager.getString("ui.label.stats.duirnal.ws"));
		panel_2.setBounds(10, 140, 230, 110);
		getContentPane().add(panel_2);

		RLabel lblFbpPercentDeadFirUnit = new RLabel("\u03B1");
		lblFbpPercentDeadFirUnit.setBounds(10, 20, 101, 20);
		panel_2.add(lblFbpPercentDeadFirUnit);

		RLabel label_1 = new RLabel("\u03B2");
		label_1.setBounds(10, 50, 101, 20);
		panel_2.add(label_1);

		RLabel label = new RLabel("\u03B3");
		label.setBounds(10, 80, 101, 20);
		panel_2.add(label);
		
		txtWSAlpha = new RTextField();
		txtWSAlpha.setBounds(117, 20, 84, 20);
		txtWSAlpha.setText(DecimalUtils.format(DEFAULT_WIND_ALPHA));
		panel_2.add(txtWSAlpha);

		txtWSBeta = new RTextField();
		txtWSBeta.setBounds(117, 50, 84, 20);
		txtWSBeta.setText(DecimalUtils.format(DEFAULT_WIND_BETA));
		panel_2.add(txtWSBeta);

		txtWSGamma = new RTextField();
		txtWSGamma.setBounds(117, 80, 84, 20);
		txtWSGamma.setText(DecimalUtils.format(DEFAULT_WIND_GAMMA));
		panel_2.add(txtWSGamma);
	}
}