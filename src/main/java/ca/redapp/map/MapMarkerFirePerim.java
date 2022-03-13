/***********************************************************************
 * REDapp - MapMarkerFirePerim.java
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.Style;

import ca.redapp.util.REDappLogger;

public class MapMarkerFirePerim extends MapMarkerDot {
	
	private BufferedImage img;

    public MapMarkerFirePerim(Coordinate coord, String flamePath) {
        this(null, null, coord, flamePath);
    }

    public MapMarkerFirePerim(String name, Coordinate coord, String flamePath) {
        this(null, name, coord, flamePath);
    }

    public MapMarkerFirePerim(Layer layer, Coordinate coord, String flamePath) {
        this(layer, null, coord, flamePath);
    }

    public MapMarkerFirePerim(Layer layer, String name, Coordinate coord, String flamePath) {
        this(layer, name, coord, getDefaultStyle(), flamePath);
    }

    public MapMarkerFirePerim(Color color, double lat, double lon, String flamePath) {
        this(null, null, lat, lon, flamePath);
        setColor(color);
    }

    public MapMarkerFirePerim(double lat, double lon, String flamePath) {
        this(null, null, lat, lon, flamePath);
    }

    public MapMarkerFirePerim(Layer layer, double lat, double lon, String flamePath) {
        this(layer, null, lat, lon, flamePath);
    }

    public MapMarkerFirePerim(Layer layer, String name, double lat, double lon, String flamePath) {
        this(layer, name, new Coordinate(lat, lon), getDefaultStyle(), flamePath);
    }

    public MapMarkerFirePerim(Layer layer, String name, Coordinate coord, Style style, String flamePath) {
        super(layer, name, coord, style);
        String tempPath = null;
        URL tempUrl = null;
        if (flamePath != null) {
	        if (flamePath.startsWith("file:/")) {
	        	try {
	        		tempUrl = new URL(flamePath);
				}
	        	catch (MalformedURLException e) {
					REDappLogger.error("Unable to convert URL to file path", e);
				}
	        }
	        else if (flamePath.startsWith("jar:file")) {
        		int index = flamePath.indexOf("!");
        		if (index > 0) {
        			String temp = flamePath.substring(index + 1);
        			tempUrl = getClass().getResource(temp);
        		}
        		else
        			tempPath = flamePath;
	        }
	        else
	        	tempPath = flamePath;
        }
        
        if (tempPath != null) {
			try {
				img = ImageIO.read(new File(tempPath));
			}
			catch (IOException e) {
        		REDappLogger.error("Failed to load flame image", e);
			}
        }
        else if (tempUrl != null) {
        	try {
        		img = ImageIO.read(tempUrl);
        	}
			catch (IOException e) {
        		REDappLogger.error("Failed to load flame image", e);
			}
        }
    }

    @Override
    public void paint(Graphics g, Point position, int radius) {    	        
        int sizeH = 12;
        int size = sizeH * 2;

        if (img != null) {
			g.drawImage(img, position.x - sizeH, position.y - size, size, size, null);
        }
        else {
            if (g instanceof Graphics2D && getBackColor() != null) {
                Graphics2D g2 = (Graphics2D) g;
                Composite oldComposite = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                g2.setPaint(Color.RED);
                g.fillOval(position.x - sizeH, position.y - sizeH, size, size);
                g2.setComposite(oldComposite);
            }
            g.setColor(getColor());
            g.drawOval(position.x - sizeH, position.y - sizeH, size, size);
        }
    }
}
