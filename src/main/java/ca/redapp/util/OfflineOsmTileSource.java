/***********************************************************************
 * REDapp - OfflineOsmTileSource.java
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

import org.openstreetmap.gui.jmapviewer.tilesources.AbstractOsmTileSource;

public class OfflineOsmTileSource extends AbstractOsmTileSource
{

       private final int minZoom;
       private final int maxZoom;
       public OfflineOsmTileSource(String path, int minZoom, int maxZoom) {
       super("Offline from ", path, path);
       this.minZoom = minZoom;
       this.maxZoom = maxZoom;
       }
       @Override
       public int getMaxZoom() {
       return maxZoom;
       }
       @Override
       public int getMinZoom() {
       return minZoom;
       }
}