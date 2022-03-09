/***********************************************************************
 * REDapp - CSVExporter.java
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

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public class CSVExporter extends Preferences {
	LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
	public String delimiter = ",";

	@Override
	public String absolutePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addNodeChangeListener(NodeChangeListener ncl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
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
	public void exportNode(OutputStream os) throws IOException,
			BackingStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void exportSubtree(OutputStream os) throws IOException,
			BackingStoreException {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os));
		Set<Entry<String, String>> values = map.entrySet();
		int i = 0;
		for (Entry<String, String> entry : values) {
			w.write(entry.getKey());
			if (i < (values.size() - 1)) {
				w.write(delimiter);
			}
			i++;
		}
		w.newLine();
		i = 0;
		
		for (Entry<String, String> entry : values) {
			boolean escape = false;
			String val = entry.getValue();
			if (val.contains("\r") || val.contains("\n") || val.contains("\"") || val.contains(",")) {
				escape = true;
				val = val.replace("\"", "\\\"");
				w.write("\"");
			}
			w.write(val);
			if (escape)
				w.write("\"");
			if (i < (values.size() - 1)) {
				w.write(delimiter);
			}
			i++;
		}
		w.close();
	}

	@Override
	public void flush() throws BackingStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public String get(String key, String def) {
		String value = def;
		if (map.containsKey(key)) {
			value = map.get(key);
		}
		return value;
	}

	@Override
	public boolean getBoolean(String key, boolean def) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getByteArray(String key, byte[] def) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getDouble(String key, double def) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getFloat(String key, float def) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInt(String key, int def) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLong(String key, long def) {
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
	public Preferences node(String pathName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean nodeExists(String pathName) throws BackingStoreException {
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
		map.put(key, value);
	}

	@Override
	public void putBoolean(String key, boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putByteArray(String key, byte[] value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putDouble(String key, double value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putFloat(String key, float value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putInt(String key, int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putLong(String key, long value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(String key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeNode() throws BackingStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeNodeChangeListener(NodeChangeListener ncl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
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
