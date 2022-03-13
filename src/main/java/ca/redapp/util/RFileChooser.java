/***********************************************************************
 * REDapp - RFileChooser.java
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

package ca.redapp.util;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A wrapper around a couple of different open/save dialogs. The best dialog for
 * the version of Java that is currently running is used.
 * 
 * @author Travis Redpath
 *
 */
public class RFileChooser {
	private JFileChooser _InternalJava = null;
	private DialogMode mode;

	private RFileChooser() {
	}

	/**
	 * Set the starting directory.
	 * 
	 * @param dir
	 */
	public void setCurrentDirectory(String dir) {
		_InternalJava.setCurrentDirectory(new File(dir));
	}

	/**
	 * Set the dialog title.
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		//Unused from migration away from SWT
	}

	/**
	 * Set the file extension filters.
	 */
	public void setExtensionFilters(String[] extensionFilters, String[] extensionFiltersNames, int selectedExtensionFilterIndex) {
		int length = Math.min(extensionFilters.length, extensionFiltersNames.length);
		_InternalJava.setAcceptAllFileFilterUsed(false);
		for (int i = 0; i < length; i++) {
			String extensions = extensionFilters[i];
			String[] extensionList = extensions.split(";");
			if (extensionList.length == 1) {
				if (extensions.startsWith("*."))
					extensions = extensions.substring(2);
				_InternalJava.addChoosableFileFilter(new FileNameExtensionFilter(extensionFiltersNames[i], extensions));
			}
			else if (extensionList.length > 1) {
				for (int j = 0; j < extensionList.length; j++) {
					if (extensionList[j].startsWith("*."))
						extensionList[j] = extensionList[j].substring(2);
				}
				_InternalJava.addChoosableFileFilter(new FileNameExtensionFilter(extensionFiltersNames[i], extensionList));
			}
		}
	}

	/**
	 * Get the selected file extension (if there is one).
	 * 
	 * @return
	 */
	public String[] getSelectedExtension() {
		FileNameExtensionFilter filter = (FileNameExtensionFilter)_InternalJava.getFileFilter();
		return filter.getExtensions();
	}

	/**
	 * Show the dialog as modal and synchronous.
	 * @param parent
	 * @return
	 */
	public int showDialog(Component parent) {
		int retval = JFileChooser.CANCEL_OPTION;
		if (mode == DialogMode.Save)
			retval = _InternalJava.showSaveDialog(parent);
		else
			retval = _InternalJava.showOpenDialog(parent);
		return retval;
	}

	/**
	 * Get the selected directory or file.
	 * 
	 * @return
	 */
	public File getSelectedFile() {
		File fl;
		fl = _InternalJava.getSelectedFile();
		return fl;
	}

	/**
	 * Get the selected directory or file.
	 * 
	 * <strong>Note:</strong> this method is the same as {@link ca.redapp.util.RFileChooser#getSelectedFile()}.
	 * 
	 * @return
	 */
	public File getSelectedDirectory() {
		return getSelectedFile();
	}

	/**
	 * Get the parent directory of the selected file.
	 * 
	 * @return
	 */
	public String getParentDirectory() {
		return _InternalJava.getCurrentDirectory().getAbsolutePath();
	}

	/**
	 * Create a new file selector dialog.
	 * 
	 * @return
	 */
	public static RFileChooser filePicker() {
		RFileChooser chooser = new RFileChooser();
		chooser.mode = DialogMode.Open;
		chooser._InternalJava = new JFileChooser();
		return chooser;
	}

	/**
	 * Create a new directory selector dialog.
	 * 
	 * @return
	 */
	public static RFileChooser directoryPicker() {
		RFileChooser chooser = new RFileChooser();
		chooser.mode = DialogMode.Open;
		chooser._InternalJava = new JFileChooser();
		chooser._InternalJava.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser._InternalJava.setApproveButtonText("Choose");
		return chooser;
	}

	/**
	 * Create a new file save dialog.
	 * 
	 * @return
	 */
	public static RFileChooser fileSaver() {
		RFileChooser chooser = new RFileChooser();
		chooser.mode = DialogMode.Save;
		chooser._InternalJava = new JFileChooser();
		return chooser;
	}

	private enum DialogMode {
		Open,
		Save
	}
}
