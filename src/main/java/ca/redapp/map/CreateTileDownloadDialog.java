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

import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JDialog;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import ca.redapp.ui.Import;
import ca.redapp.ui.Main;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RLabel;

public class CreateTileDownloadDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private RButton btnCancel;
	
	private long retval;
	
	private CompletableFuture<Boolean> completableFuture;
	
	private AtomicBoolean cancel;
	
	private double minLat, minLon, maxLat, maxLong;
	private int minZoom, maxZoom;
	private String location;
	
	public CreateTileDownloadDialog(Window owner, double minLat, double minLon, double maxLat, double maxLong, int minZoom, int maxZoom, String location) {
		super(owner);
		setResizable(false);
		setModal(true);
		
		this.minLat = minLat;
		this.minLon = minLon;
		this.maxLat = maxLat;
		this.maxLong = maxLong;
		
		this.minZoom = minZoom;
		this.maxZoom = maxZoom;
		
		this.location = location;
		
		initialize();

		btnCancel.addActionListener((e) -> cancel());

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
	
	public void completed(boolean complete) {

		retval = (complete) ? 1 : ca.hss.general.ERROR.SEVERITY_WARNING;
		
		dispose();
	}
	
	public void cancel() {
		retval = ca.hss.general.ERROR.SEVERITY_WARNING;

		cancel.set(true);
		
		dispose();
	}
	
	public long getResult() {
		return retval;
	}

	private void initialize() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/redapplogo_20.png")));
		setTitle(Main.resourceManager.getString("ui.dlg.title.create.tileinput"));
		setBounds(0, 0, 255, 145);
		getContentPane().setLayout(null);

		RLabel lblDownloading = new RLabel(Main.resourceManager.getString("ui.label.tile.download"));
		lblDownloading.setBounds(10, 10, 230, 40);
		getContentPane().add(lblDownloading);
		
		btnCancel = new RButton(Main.resourceManager.getString("ui.label.edit.cancel"));
		btnCancel.setBounds(67, 70, 121, 41);
		getContentPane().add(btnCancel);

		cancel = new AtomicBoolean();
		cancel.set(false);
		completableFuture = TileCache.cacheBoundingBox(minLat, minLon, maxLat, maxLong, minZoom, maxZoom, location, cancel);
		
		completableFuture.thenAccept(complete -> completed(complete));
	}
}