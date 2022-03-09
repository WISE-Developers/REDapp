/***********************************************************************
 * REDapp - Geolocate.java
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

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import ca.redapp.ui.BusyDialog;
import ca.redapp.ui.Main;

/**
 * Geolocate the user using their IP address and a list of known address locations.
 * 
 * @author Travis Redpath
 *
 */
public class Geolocate {
	private Map<Long, LatLong> map = Collections
			.synchronizedMap(new HashMap<Long, LatLong>());
	private Preferences prefs = Preferences.userRoot();
	private Geolocate_internal _internal;
	protected Thread _InternalThread = null;
	protected BusyDialog busyDialog = null;
	protected Main app = null;

	public Geolocate(Main app) {
		this.app = app;
	}

	private static long bytesToLong(byte[] address) {
		long ipnum = 0;
		for (int i = 0; i < 4; ++i) {
			long y = address[i];
			if (y < 0) {
				y += 256;
			}
			ipnum += y << ((3 - i) * 8);
		}
		return ipnum;
	}

	/**
	 * Asynchronous call to find the location based on the IP address. Opens a modal
	 * busy dialog until the asynchronous task completes.
	 * 
	 * @param ip The machines IP address.
	 */
	public void locate(String ip) {
		_internal = new Geolocate_internal();
		_InternalThread = new Thread(_internal);
		_internal.ip = ip;
		busyDialog = new BusyDialog(app.getForm());
		_InternalThread.start();
		busyDialog.setModal(true);
		busyDialog.setVisible(true);
	}

	private void _internalFinished() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				busyDialog.setVisible(false);
				if (!_internal.found)
					JOptionPane.showMessageDialog(null,
							"Unable to find your current location.", "Error",
							JOptionPane.ERROR_MESSAGE);
				else
					app.locationFound(_internal.loc.latitude, _internal.loc.longitude);
				_internal = null;
				_InternalThread = null;
				busyDialog = null;
			}
		});
	}

	protected class Geolocate_internal implements Runnable {
		String ip;
		public boolean found = false;
		public LatLong loc = null;

		@Override
		public void run() {
			long address;
			try {
				InetAddress add = InetAddress.getByName(ip);
				address = bytesToLong(add.getAddress());
			} catch (UnknownHostException e1) {
				_internalFinished();
				return;
			}
			if (Geolocate.this.map.containsKey(address)) {
				loc = new LatLong(Geolocate.this.map.get(address));
				found = true;
				_internalFinished();
				return;
			}
			InputStream in;
			//try {
				in = Geolocate.class.getClassLoader().getResourceAsStream(
						"data/GeoLiteCity-Blocks.csv");
				//in = new FileInputStream("bin/data/GeoLiteCity-Blocks.csv");
			//} catch (FileNotFoundException e1) {
			//	_internalFinished();
			//	return;
			//}
			String line, test;
			long start, end;
			String index = null;
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(in))) {
				while ((line = reader.readLine()) != null) {
					String[] split = line.split(",");
					if (split.length != 3)
						continue;
					try {
						test = split[0].replace("\"", "");
						start = Long.parseLong(test);
						if (address < start)
							continue;
						test = split[1].replace("\"", "");
						end = Long.parseLong(test);
						if (address > end)
							continue;
					} catch (NumberFormatException ex) {
						continue;
					}
					index = split[2].replaceAll("\"", "");
					break;
				}
				in.close();
			}
			catch (IOException e) {
			}
			if (index == null)
				return;
			in = Geolocate.class.getClassLoader().getResourceAsStream(
					"data/GeoLiteCity-Location.csv");
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
				while ((line = reader.readLine()) != null) {
					String[] split = line.split(",");
					if (split.length == 0)
						continue;
					if (split[0].equalsIgnoreCase(index)) {
						double lat, lng;
						try {
							test = split[5];
							lat = Double.parseDouble(test);
							test = split[6];
							lng = Double.parseDouble(test);
							loc = new LatLong(lat, lng);
							found = true;
							map.put(address, loc);
						} catch (NumberFormatException ex) {
						}
						break;
					}
				}
				in.close();
			}
			catch (IOException e) {
			}
			_internalFinished();
		}
	}

	public static class LatLong {
		public double latitude;
		public double longitude;

		public LatLong(LatLong loc) {
			latitude = loc.latitude;
			longitude = loc.longitude;
		}

		public LatLong(double lat, double lng) {
			latitude = lat;
			longitude = lng;
		}
	}

	private byte[] getIPBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			for (long i : map.keySet())
				oos.writeLong(i);
			return baos.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	private List<Long> getIPList(byte[] raw) {
		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		try {
			ObjectInputStream ois = new ObjectInputStream(bais);
			List<Long> list = new ArrayList<Long>();
			try {
				while (true) {
					list.add(ois.readLong());
				}
			} catch (EOFException e) {
			}
			return list;
		} catch (IOException e) {
			return null;
		}
	}

	private LatLong getLocation(byte[] raw) {
		if (raw == null)
			return null;
		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		try {
			ObjectInputStream ois = new ObjectInputStream(bais);
			double lat, lng;
			lat = ois.readDouble();
			lng = ois.readDouble();
			return new LatLong(lat, lng);
		} catch (Exception e) {
			return null;
		}
	}

	private byte[] getLocationBytes(LatLong loc) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeDouble(loc.latitude);
			oos.writeDouble(loc.longitude);
			return baos.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	protected void loadMap() {
		if (map != null)
			return;
		map = Collections.synchronizedMap(new HashMap<Long, LatLong>());
		byte[] raw = prefs.getByteArray("iplist", null);
		if (raw == null)
			return;
		List<Long> ips = getIPList(raw);
		for (long i : ips) {
			LatLong l = getLocation(prefs.getByteArray(String.valueOf(i), null));
			if (l != null)
				map.put(i, l);
		}
	}

	protected void saveMap() {
		byte[] ips = getIPBytes();
		prefs.putByteArray("iplist", ips);
		for (Entry<Long, Geolocate.LatLong> i : map.entrySet()) {
			prefs.putByteArray(String.valueOf(i.getKey()), getLocationBytes(i.getValue()));
		}
	}
}
