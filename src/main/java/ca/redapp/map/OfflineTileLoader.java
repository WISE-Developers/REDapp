/***********************************************************************
 * REDapp - OfflineTileLoader.java
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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;

import ca.hss.general.WebDownloader;

public class OfflineTileLoader extends OsmTileLoader {

	public OfflineTileLoader(TileLoaderListener listener) {
		super(listener);
	}
	
	public OfflineTileLoader(TileLoaderListener listener, Map<String, String> headers) {
		super(listener, headers);
	}
	
	@Override
	protected void loadTileMetadata(Tile tile, URLConnection urlConn) {
		super.loadTileMetadata(tile, urlConn);
		//if there is no internet connection flag all non-cached tiles
		if (!WebDownloader.hasInternetConnection()) {
			try {
				if (!(new File(urlConn.getURL().toURI())).exists())
					tile.putValue("tile-info", "no-tile");
			}
			catch (URISyntaxException e) { }
		}
	}

	/**
	 * Override the parent method in order to apply a user agent to the tile request.
	 * The user agent is from Chrome and will hopefully stop 429 errors from the tile server.
	 * @param tile Information, including the URL, of the tile that needs loaded.
	 * @return An open URL connection for downloading the tile image.
	 */
	@Override
    protected URLConnection loadTileFromOsm(Tile tile) throws IOException {
        URL url;
        url = new URL(tile.getUrl());
        URLConnection urlConn = url.openConnection();
        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36");
        if (urlConn instanceof HttpURLConnection) {
            prepareHttpUrlConnection((HttpURLConnection) urlConn);
        }
        return urlConn;
    }
}
