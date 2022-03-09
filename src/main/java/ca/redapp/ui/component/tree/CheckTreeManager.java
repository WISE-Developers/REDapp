/***********************************************************************
 * REDapp - CheckTreeManager.java
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

package ca.redapp.ui.component.tree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/**
 * A manager for a tree view of {@link ca.hss.app.redapp.ui.component.TriStateCheckBox}.
 * 
 */
public class CheckTreeManager extends MouseAdapter implements TreeSelectionListener {
	private CheckTreeSelectionModel selectionModel;
	private JTree tree;
	int hotspot = new JCheckBox().getPreferredSize().width - 3;
	private ArrayList<CheckStateListener> listeners = new ArrayList<CheckStateListener>();
	
	public CheckTreeManager(JTree tree) {
		this.tree = tree;
		selectionModel = new CheckTreeSelectionModel(tree.getModel());
		if (!(tree.getCellRenderer() instanceof CheckTreeCellRenderer))
			tree.setCellRenderer(new CheckTreeCellRenderer(tree.getCellRenderer(), selectionModel));
		tree.addMouseListener(this);
		selectionModel.addTreeSelectionListener(this);
	}
	
	public void enable() {
		CheckTreeCellRenderer renderer = (CheckTreeCellRenderer)tree.getCellRenderer();
		renderer.setSelectionModel(selectionModel);
	}
	
	public void setState(TreePath path, Tribool state) {
		selectionModel.removeTreeSelectionListener(this);

		try {
			if (state == Tribool.TRUE) {
				selectionModel.addSelectionPath(path);
			}
			else if (state == Tribool.FALSE) {
				selectionModel.removeSelectionPath(path);
			}
			notifyCheckStateListeners(path, state);
		}
		finally {
			selectionModel.addTreeSelectionListener(this);
			tree.treeDidChange();
		}
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		TreePath path = tree.getPathForLocation(me.getX(), me.getY());
		if (path == null)
			return;
		if (me.getX() > tree.getPathBounds(path).x + hotspot)
			return;
		
		boolean selected = selectionModel.isPathSelected(path, true);
		selectionModel.removeTreeSelectionListener(this);
		
		try {
			if (selected) {
				selectionModel.removeSelectionPath(path);
				notifyCheckStateListeners(path, Tribool.FALSE);
			}
			else {
				selectionModel.addSelectionPath(path);
				notifyCheckStateListeners(path, Tribool.TRUE);
			}
		}
		finally {
			selectionModel.addTreeSelectionListener(this);
			tree.treeDidChange();
		}
	}
	
	public CheckTreeSelectionModel getSelectionModel() {
		return selectionModel;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		tree.treeDidChange();
	}
	
	public void addCheckStateListener(CheckStateListener listener) {
		listeners.add(listener);
	}
	
	private void notifyCheckStateListeners(TreePath path, Tribool checked) {
		for (CheckStateListener listener : listeners) {
			listener.checkChanged(path, checked);
		}
	}
	
	public static abstract class CheckStateListener {
		public abstract void checkChanged(TreePath path, Tribool checked);
	}
}
