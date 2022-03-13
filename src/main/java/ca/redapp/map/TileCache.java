/***********************************************************************
 * REDapp - TileCache.java
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

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.util.concurrent.RateLimiter;

public class TileCache {
	
	private static final String protocol = "http://";
	private static final String[] nodes = { "a", "b", "c" };
	private static final String site = ".tile.openstreetmap.org";
    
    private static class TileLocation {
    	final int x;
    	final int y;
    	final int zoom;
    	
    	TileLocation(int x, int y, int zoom) {
    		this.x = x;
    		this.y = y;
    		this.zoom = zoom;
    	}
    	
    	@Override
    	public String toString() {
        	return "/" + zoom + "/" + x + "/" + y + ".png";
    	}
    }
    
    private static TileLocation getTileLocation(double lat, double lon, int zoom) {
    	int xtile = (int)Math.floor((lon + 180) / 360 * (1 << zoom));
    	int ytile = (int)Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
    	if (xtile < 0)
    		xtile = 0;
    	if (xtile >= (1 << zoom))
    		xtile = ((1 << zoom) - 1);
    	if (ytile < 0)
    		ytile = 0;
    	if (ytile >= (1 << zoom))
    		ytile = ((1 << zoom) - 1);
    	return new TileLocation(xtile, ytile, zoom);
    }
    
    /**
     * Cache map tiles within a given bounding box for offline usage.
     * @param minLat The minimum latitude of the bounding box.
     * @param minLon The minimum longitude of the bounding box.
     * @param maxLat The maximum latitude of the bounding box.
     * @param maxLon The maximum longitude of the bounding box.
     * @param minZoom The minimum zoom level to cache.
     * @param maxZoom The maximum zoom level to cache.
     * @param location The file location to store the tiles.
     */
    public static CompletableFuture<Boolean> cacheBoundingBox(final double minLat, final double minLon, final double maxLat, final double maxLon,
    		final int minZoom, final int maxZoom, final String location, AtomicBoolean cancel) {
    	return CompletableFuture.supplyAsync(() -> {
    		RateLimiter limiter = RateLimiter.create(1.0);
    		Random r = new Random(System.currentTimeMillis());
    		for (int zoom = minZoom; zoom <= maxZoom && !(cancel.get()); zoom++) {
    			TileLocation minTile = getTileLocation(minLat, minLon, zoom);
    			TileLocation maxTile = getTileLocation(maxLat, maxLon, zoom);
    			int minX = Math.min(minTile.x, maxTile.x);
    			int maxX = Math.max(minTile.x, maxTile.x);
    			int minY = Math.min(minTile.y, maxTile.y);
    			int maxY = Math.max(minTile.y, maxTile.y);
    			for (int x = minX; x <= maxX && !(cancel.get()); x++) {
    				for (int y = minY; y <= maxY && !(cancel.get()); y++) {
    					TileLocation here = new TileLocation(x, y, zoom);
        				try {
        					limiter.acquire();
        					URL website = new URL(protocol + nodes[r.nextInt(nodes.length)] + site + here.toString());
        					Path path = Paths.get(location + here.toString());
        					Files.createDirectories(path.getParent());
        					try (InputStream in = website.openStream()) {
        						Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        					}
        				}
        				catch (Exception e) {
        					throw new CompletionException(e);
        				}
    				}
    			}
    		}
    		return true;
    	});
    }
}
