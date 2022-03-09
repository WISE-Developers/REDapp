/***********************************************************************
 * REDapp - MapMarkerOffline.java
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.Style;

public class MapMarkerOffline extends MapMarkerDot {
	
	private boolean annotationVisible = false;

    public MapMarkerOffline(Coordinate coord) {
        this(null, null, coord);
    }

    public MapMarkerOffline(String name, Coordinate coord) {
        this(null, name, coord);
    }

    public MapMarkerOffline(Layer layer, Coordinate coord) {
        this(layer, null, coord);
    }

    public MapMarkerOffline(Layer layer, String name, Coordinate coord) {
        this(layer, name, coord, getDefaultStyle());
    }

    public MapMarkerOffline(Color color, double lat, double lon) {
        this(null, null, lat, lon);
        setColor(color);
    }

    public MapMarkerOffline(double lat, double lon) {
        this(null, null, lat, lon);
    }

    public MapMarkerOffline(Layer layer, double lat, double lon) {
        this(layer, null, lat, lon);
    }

    public MapMarkerOffline(Layer layer, String name, double lat, double lon) {
        this(layer, name, new Coordinate(lat, lon), getDefaultStyle());
    }

    public MapMarkerOffline(Layer layer, String name, Coordinate coord, Style style) {
        super(layer, name, coord, style);
    }
    
    @Override
    public void paint(Graphics g, Point position, int radius) {
        int sizeH = radius;
        int size = sizeH * 2;

        if (g instanceof Graphics2D && getBackColor() != null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(getBackColor());
            g.fillOval(position.x - sizeH, position.y - sizeH, size, size);
            g2.setComposite(oldComposite);
        }
        g.setColor(getColor());
        g.drawOval(position.x - sizeH, position.y - sizeH, size, size);
        
        if ((getLayer() == null || getLayer().isVisibleTexts()) && annotationVisible)
        	paintText(g, position);
    }

	public boolean isTextVisible() {
		return annotationVisible;
	}
	
	public void setTextVisible(boolean visible) {
		annotationVisible = visible;
	}
}
