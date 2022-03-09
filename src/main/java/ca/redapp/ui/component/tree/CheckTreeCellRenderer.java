/***********************************************************************
 * REDapp - CheckTreeCellRenderer.java
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

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import ca.redapp.ui.component.TristateCheckBox;

/**
 * A renderer for drawing {@link ca.hss.app.redapp.ui.component.TriStateCheckBox} in a tree view.
 * 
 */
public class CheckTreeCellRenderer extends JPanel implements TreeCellRenderer {
	private static final long serialVersionUID = 1L;
	private CheckTreeSelectionModel selectionModel;
	private TreeCellRenderer delegate;
	private TristateCheckBox checkBox = new TristateCheckBox();
	
	public CheckTreeCellRenderer(TreeCellRenderer delegate, CheckTreeSelectionModel SelectionModel) {
		this.delegate = delegate;
		this.selectionModel = SelectionModel;
		setLayout(new BorderLayout());
		setOpaque(false);
		checkBox.setOpaque(false);
	}
	
	public void setSelectionModel(CheckTreeSelectionModel SelectionModel) {
		this.selectionModel = SelectionModel;
		invalidate();
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		Component renderer = delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		
		TreePath path = tree.getPathForRow(row);
		if (path != null) {
			if (selectionModel.isPathSelected(path, true))
				checkBox.setState(TristateCheckBox.SELECTED);
			else
				checkBox.setState(selectionModel.isPartiallySelected(path) ? TristateCheckBox.DONT_CARE : TristateCheckBox.NOT_SELECTED);
		}
		removeAll();
		add(checkBox, BorderLayout.WEST);
		add(renderer, BorderLayout.CENTER);
		return this;
	}
}
