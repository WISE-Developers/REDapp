/***********************************************************************
 * REDapp - MapScalerOffline.java
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.openstreetmap.josm.data.SystemOfMeasurement;

public class MapScalerOffline extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int PADDING_LEFT = 5;
    private static final int PADDING_RIGHT = 50;
    
    private int width = 100;
    private JMapViewerOffline jvo = null;
    private boolean metric = true;
    
    /**
     * Set whether the scale bar should be shown in meters or in feet.
     * @param metric True to show the scale bar in metric (m and km),
     * false to show it in imperial (ft and mi).
     */
    public void setMetric(boolean metric) {
    	this.metric = metric;
    	repaint();
    }
	
	public MapScalerOffline(JMapViewerOffline jvo, boolean metric) { 
		super();
		this.metric = metric;
		this.jvo = jvo;
		this.setOpaque(false);
		setBounds(0, 0);
		jvo.add(this);
	}

	public void setBounds(int x, int y) {
		super.setBounds(x, y, (this.width + PADDING_LEFT + PADDING_RIGHT), 30);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		double dist100Pixel = jvo.getDist100Pixel();
		ITickMarks tickMarks = null;
		
		if (metric)
			tickMarks = new MetricTickMarks(dist100Pixel, getWidth() - PADDING_LEFT - PADDING_RIGHT);
		else
			tickMarks = new ImperialTickMarks(dist100Pixel, getWidth() - PADDING_LEFT - PADDING_RIGHT);
        
        g.setColor(Color.WHITE);
        tickMarks.paintTicks(g, 1);
        
        g.setColor(Color.BLACK);
        tickMarks.paintTicks(g, 0);
	}
	
	private static interface ITickMarks {

        /**
         * Paint the ticks to the graphics.
         * @param g The graphics to paint on.
         */
        public void paintTicks(Graphics g, int offset);
	}
	
	private static class MetricTickMarks implements ITickMarks {

        private final double dist100Pixel;
        /**
         * Distance in meters between two ticks.
         */
        private final double spacingMeter;
        private final int steps;
        private final int minorStepsPerMajor;
		
		public MetricTickMarks(double dist100Pixel, int width) {
            this.dist100Pixel = dist100Pixel;
            double lineDistance = dist100Pixel * width / 100;

            double log10 = Math.log(lineDistance) / Math.log(10);
            double spacingLog10 = Math.pow(10, Math.floor(log10));
            int minorStepsPerMajor;
            double distanceBetweenMinor;
            if (log10 - Math.floor(log10) < .75) {
                // Add 2 ticks for every full unit
                distanceBetweenMinor = spacingLog10 / 2;
                minorStepsPerMajor = 2;
            } else {
                // Add 10 ticks for every full unit
                distanceBetweenMinor = spacingLog10;
                minorStepsPerMajor = 5;
            }
            // round down to the last major step.
            int majorSteps = (int) Math.floor(lineDistance / distanceBetweenMinor / minorStepsPerMajor);
            if (majorSteps >= 4) {
                // we have many major steps, do not paint the minor now.
                this.spacingMeter = distanceBetweenMinor * minorStepsPerMajor;
                this.minorStepsPerMajor = 1;
            } else {
                this.minorStepsPerMajor = minorStepsPerMajor;
                this.spacingMeter = distanceBetweenMinor;
            }
            steps = majorSteps * this.minorStepsPerMajor;
		}
		
		@Override
        public void paintTicks(Graphics g, int offset) {
            double spacingPixel = spacingMeter / (dist100Pixel / 100);
            double textBlockedUntil = -1;
            for (int step = 0; step <= steps; step++) {
                int x = (int) (PADDING_LEFT + spacingPixel * step);
                boolean isMajor = step % minorStepsPerMajor == 0;
                int paddingY = isMajor ? 0 : 3;
                g.drawLine(x + offset, paddingY + offset, x + offset, 10 - paddingY + offset);

                if (step == 0 || step == steps) {
                    String text;
                    
                    if (step == 0) {
                        text = "0";
                    }
                    else {
                    	text = SystemOfMeasurement.METRIC.getDistText(spacingMeter * step);
                    }
                    
                    Rectangle2D bound = g.getFontMetrics().getStringBounds(text, g);
                    int left = (int) (x - bound.getWidth() / 2);
                    if (textBlockedUntil > left) {
                        left = (int) (textBlockedUntil + 5);
                    }
                    g.drawString(text, left + offset, 23 + offset);
                    textBlockedUntil = left + bound.getWidth() + 2;
                }
            }
            g.drawLine(PADDING_LEFT + offset, 5 + offset, (int) (PADDING_LEFT + spacingPixel * steps) + offset, 5 + offset);
        }
	}
	
	private static final class ImperialTickMarks implements ITickMarks {

        private final double dist100Pixel;
        /**
         * Distance in meters between two ticks.
         */
        private final double spacingFeet;
        private final int steps;
        private final int minorStepsPerMajor;
        
        private final double CONVERSION_M_TO_FT = 3.280839895;
        private final double CONVERSION_FT_TO_MI = 5280;

        /**
         * Creates a new tick mark helper.
         * @param dist100Pixel The distance of 100 pixel on the map.
         * @param width The width of the mark.
         */
        ImperialTickMarks(double dist100Pixel, int width) {
            this.dist100Pixel = dist100Pixel * CONVERSION_M_TO_FT;
            double lineDistance = this.dist100Pixel * width / 100;
            
            boolean miles = lineDistance >= CONVERSION_FT_TO_MI;
            if (miles)
            	lineDistance /= CONVERSION_FT_TO_MI;

            double log10 = Math.log(lineDistance) / Math.log(10);
            double spacingLog10 = Math.pow(10, Math.floor(log10));
            int minorStepsPerMajor;
            double distanceBetweenMinor;
            if (log10 - Math.floor(log10) < .75) {
                // Add 2 ticks for every full unit
                distanceBetweenMinor = spacingLog10 / 2;
                minorStepsPerMajor = 2;
            } else {
                // Add 10 ticks for every full unit
                distanceBetweenMinor = spacingLog10;
                minorStepsPerMajor = 5;
            }
            // round down to the last major step.
            int majorSteps = (int) Math.floor(lineDistance / distanceBetweenMinor / minorStepsPerMajor);
            double spacingSize;
            if (majorSteps >= 4) {
                // we have many major steps, do not paint the minor now.
            	spacingSize = distanceBetweenMinor * minorStepsPerMajor;
                this.minorStepsPerMajor = 1;
            } else {
                this.minorStepsPerMajor = minorStepsPerMajor;
                spacingSize = distanceBetweenMinor;
            }
            steps = majorSteps * this.minorStepsPerMajor;
            
            if (miles) {
            	spacingFeet = spacingSize * CONVERSION_FT_TO_MI;
            }
            else {
            	spacingFeet = spacingSize;
            }
        }

        /**
         * Paint the ticks to the graphics.
         * @param g The graphics to paint on.
         */
        public void paintTicks(Graphics g, int offset) {
            double spacingPixel = spacingFeet / (dist100Pixel / 100);
            double textBlockedUntil = -1;
            for (int step = 0; step <= steps; step++) {
                int x = (int) (PADDING_LEFT + spacingPixel * step);
                boolean isMajor = step % minorStepsPerMajor == 0;
                int paddingY = isMajor ? 0 : 3;
                g.drawLine(x + offset, paddingY + offset, x + offset, 10 - paddingY + offset);

                if (step == 0 || step == steps) {
                    String text;
                    
                    if (step == 0) {
                        text = "0";
                    }
                    else {
                        text = SystemOfMeasurement.IMPERIAL.getDistText(spacingFeet * step / CONVERSION_M_TO_FT);
                    }
                    
                    Rectangle2D bound = g.getFontMetrics().getStringBounds(text, g);
                    int left = (int) (x - bound.getWidth() / 2);
                    if (textBlockedUntil > left) {
                        left = (int) (textBlockedUntil + 5);
                    }
                    g.drawString(text, left + offset, 23 + offset);
                    textBlockedUntil = left + bound.getWidth() + 2;
                }
            }
            g.drawLine(PADDING_LEFT + offset, 5 + offset, (int) (PADDING_LEFT + spacingPixel * steps) + offset, 5 + offset);
        }
	}
}
