/***********************************************************************
 * REDapp - XLSExporter.java
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class XLSExporter extends Preferences {

	Workbook workbook;
	Sheet sheet;
	Row header, data;
	int col;

	public XLSExporter(String title, boolean useXLSX) {
		if(useXLSX)
			workbook = new XSSFWorkbook();
		else
			workbook = new HSSFWorkbook();
		sheet = workbook.createSheet(title);
		header = sheet.createRow(0);
		data = sheet.createRow(1);
	}

	@Override
	public String absolutePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addNodeChangeListener(NodeChangeListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPreferenceChangeListener(PreferenceChangeListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] childrenNames() throws BackingStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() throws BackingStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void exportNode(OutputStream arg0) throws IOException,
			BackingStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void exportSubtree(OutputStream os) throws IOException,
			BackingStoreException {
		workbook.write(os);
	}

	@Override
	public void flush() throws BackingStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public String get(String key, String defaultValue) {
		for (int i = 0; i < header.getLastCellNum(); i++) {
			Cell cell = header.getCell(i);
			if (cell.getStringCellValue().equals(key))
				return data.getCell(i).getStringCellValue();
		}
		return defaultValue;
	}

	@Override
	public boolean getBoolean(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getByteArray(String arg0, byte[] arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getDouble(String arg0, double arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getFloat(String arg0, float arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInt(String arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLong(String arg0, long arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isUserNode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] keys() throws BackingStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Preferences node(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean nodeExists(String arg0) throws BackingStoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Preferences parent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(String key, String value) {
		Cell cell = header.createCell(col);
		cell.setCellValue(key);
		cell = data.createCell(col++);
		cell.setCellValue(value);
	}

	@Override
	public void putBoolean(String arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putByteArray(String arg0, byte[] arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putDouble(String arg0, double arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putFloat(String arg0, float arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putInt(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putLong(String arg0, long arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeNode() throws BackingStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeNodeChangeListener(NodeChangeListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePreferenceChangeListener(PreferenceChangeListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sync() throws BackingStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "";
	}

}
