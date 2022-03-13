/***********************************************************************
 * REDapp - JMapViewerOffline.java
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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;


public class JMapViewerOffline extends JMapViewer {
	private static final long serialVersionUID = 1L;

	public JMapViewerOffline () {
		super();
		 new DefaultMapController(this){
			    @Override
			    public void mouseClicked(MouseEvent e) {
			        Point p = e.getPoint();
			        isPointValid(p);
			    }

			    private void isPointValid(Point p) {
			    	List<MapMarker> markers = map.getMapMarkerList();
			    	ICoordinate clickCoords = map.getPosition(p);
			    	double zoom = (double)map.getZoom();
		        	double offset = (double)2.1 / (zoom == 0 ? 0.75 : zoom);
		        	
			    	
			        for (MapMarker x : markers) {
			        	if (x instanceof MapMarkerOffline) {
				        	MapMarkerOffline offlineMarker = (MapMarkerOffline) x;
				        	if ((clickCoords.getLat() <= x.getLat() + offset && clickCoords.getLat() >= x.getLat() - offset) && (clickCoords.getLon() <= x.getLon() + offset && clickCoords.getLon() >= x.getLon() - offset)) {
				        		offlineMarker.setTextVisible(!offlineMarker.isTextVisible());
				        		map.repaint();
				        	}
				        	else {
				        		offlineMarker.setTextVisible(false);
				        	map.repaint();
				        	}
			        	}
			        }  
			    }
			}.setMovementMouseButton(MouseEvent.BUTTON1);
		setTileLoader(new OfflineTileLoader(this));
	}
	
	/**
	 * Remove all MapMarkerOffline instances from the marker list.
	 */
	public void removeAllOfflineMapMarkers() {
		for (int i = getMapMarkerList().size() - 1; i >= 0; i--) {
			if (getMapMarkerList().get(i) instanceof MapMarkerOffline)
				getMapMarkerList().remove(i);
		}
	}

	public static double greatCircleDistance(ICoordinate ll1, ICoordinate ll2) {
	    double earthRadius = 3958.75;
	    double dLat = Math.toRadians(ll2.getLat()-ll1.getLat());
	    double dLng = Math.toRadians(ll2.getLon()-ll1.getLon());
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(ll1.getLat())) * Math.cos(Math.toRadians(ll2.getLat())) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    return (dist * 1609.34);
	}
	
	/**
	 * Get the distance in meters that correspond to 100px on the screen.
	 * @return The distance in meters that correspond to 100px on the screen.
	 */
	public double getDist100Pixel() {
		int w = getWidth() / 2;
		int h = getHeight() / 2;
		ICoordinate ll1 = getPosition(w-50, h);
		ICoordinate ll2 = getPosition(w+50, h);
		double gcd = greatCircleDistance(ll1, ll2);
		if (gcd <= 0.0)
			return 0.1;
		return gcd;
	}
	
	/**
	 * Move the zoom controls to the right hand side of the map.
	 */
	private void updateZoomControlLocations() {
		zoomOutButton.setBounds(getBounds().width - 4 - zoomOutButton.getWidth(), 155, zoomOutButton.getWidth(), zoomOutButton.getHeight());
		zoomInButton.setBounds(getBounds().width - 8 - zoomInButton.getWidth() - zoomOutButton.getWidth(), 155, zoomInButton.getWidth(), zoomInButton.getHeight());
		zoomSlider.setBounds(getBounds().width - 10 - zoomSlider.getWidth(), 10, zoomSlider.getWidth(), zoomSlider.getHeight());
	}
	
	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect);
		updateZoomControlLocations();
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		updateZoomControlLocations();
	}
}
