/***********************************************************************
 * REDapp - XMLExporter.java
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
import java.text.*;
import java.util.Date;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

public class XMLExporter extends Preferences {

	Document doc;
	Element root;

	public XMLExporter(String title) throws ParserConfigurationException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		doc = docBuilder.newDocument();
		root = doc.createElement(title);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		root.setAttribute("dateTime", dateFormat.format(date));
	}

	@Override
	public void put(String key, String value) {
		Element elem = doc.createElement(key);
		elem.appendChild(doc.createTextNode(value));
		root.appendChild(elem);
	}

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
		try {
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(root);
			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new IOException();
		}

	}

	@Override
	public void flush() throws BackingStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public String get(String key, String def) {
		String value = def;
		NodeList list = root.getElementsByTagName(key);
		if(list.getLength() > 0 && list.item(0).hasChildNodes()){
			value = list.item(0).getFirstChild().getNodeValue();
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

