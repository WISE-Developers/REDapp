/***********************************************************************
 * REDapp - MapTab.java
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

package ca.redapp.ui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.hss.general.DecimalUtils;
import ca.hss.general.OutVariable;
import ca.hss.general.WebDownloader;
import ca.hss.general.DecimalUtils.DataType;
import ca.hss.text.StringExtensions;
import ca.redapp.data.ZoomLevel;
import ca.redapp.map.CreateTileDownloadDialog;
import ca.redapp.map.CreateTileInputDialog;
import ca.redapp.map.JMapViewerOffline;
import ca.redapp.map.MapMarkerFirePerim;
import ca.redapp.map.MapMarkerOffline;
import ca.redapp.map.MapScalerOffline;
import ca.redapp.map.MapType;
import ca.redapp.ui.FbpTab.FBPTabListener;
import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RLabel;
import ca.redapp.util.ConvertUtils;
import ca.redapp.util.KmzWriter;
import ca.redapp.util.RFileChooser;
import ca.redapp.util.KmzWriter.FileDataSource;
import ca.redapp.util.KmzWriter.KMLDataSource;
import ca.redapp.util.OfflineOsmTileSource;
import ca.weather.acheron.Calculator.LocationSmall;
import ca.hss.math.Convert;
import ca.hss.math.Convert.UnitSystem;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.PolyStyle;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.util.factory.Hints;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.ReferencingObjectFactory;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapObjectImpl;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;

import static java.lang.Math.*;

public class MapTab extends REDappTab implements javax.swing.event.ChangeListener, FBPTabListener {
	private static final long serialVersionUID = 1L;
	List<LocationSmall> locationData;
	private Main app;
	private boolean initialized = false;
	private boolean needsRedraw = true;
	private ZoomLevel currentLevel = ZoomLevel.EIGHT;
	private Double currentLat = 0.0;
	private Double currentLon = 0.0;
	private boolean isLoaded = false;
	
	private JMapViewerOffline treeMap;
	private MapScalerOffline mapScaleBar;
	private long err;
	
	RButton btnWeatherStationToggle;
	RButton btnIgnitionMarkerToggle;
	RButton btnFirePerimToggle;
	
	boolean stationMarkersVisible = true;
	
	boolean fBPPassed = false;
	boolean ignitionMarkerVisible = false;
	boolean firePerimVisible = false;
	
	private JCheckBox chckbxWeatherStations;
	private JCheckBox chckbxFireMarker;
	private JCheckBox chckbxFirePerim;
	
	private MapMarkerFirePerim fireMarker = null;
	private MapPolygon firePath = null;
	
	private boolean osmWeatherStationVisible = true;
	private boolean osmMapMarkerVisible = true;
	private boolean osmMapPerimVisible = true;
	
	private MapType curMap = null;
	
	public MapTab(Main app) {
		this.app = app;
		
		if(WebDownloader.hasInternetConnection())
			curMap = MapType.fromInt(Main.prefs.getInt("map_type", MapType.OSM_OFFLINE.toInt()));
		else
			curMap = MapType.OSM_OFFLINE;
		
		initialize();
		app.fbpTab.addListener(this);
	}

	private void addStations() {
		treeMap.removeAllOfflineMapMarkers();
		try {
			for (LocationSmall loc : locationData) {
				if (loc.country.equals("CA")) {
					Double lat = loc.latitude;
					Double lon = loc.longitude;
					
					Double elevation = loc.elevation;
					elevation = Convert.convertUnit(elevation, UnitSystem.distanceMedium(Main.unitSystem()), UnitSystem.distanceMedium(UnitSystem.METRIC));
					String elev = DecimalUtils.format(elevation, DataType.FORCE_ATMOST_2) + " ";
					if (Main.unitSystem() == UnitSystem.METRIC)
						elev += Main.resourceManager.getString("ui.label.units.m");
					else
						elev += Main.resourceManager.getString("ui.label.units.ft");
					
					Coordinate coords = new Coordinate(lat, lon);
					treeMap.addMapMarker(new MapMarkerOffline(StringExtensions.capitalizeFully(loc.locationName) + ", " + lat.toString() + ", " + lon.toString() + ", " + elev, coords));
				}
			}
		} catch(Exception e) { return; }
	}

	private void clearStations() {
		treeMap.removeAllOfflineMapMarkers();
	}

	private void loadFinished() {
		needsRedraw = false;
		Double lat = app.getLatitude();
		Double lon = app.getLongitude();
		if (lat == null || lon == null && locationData != null)
			return;
		addStations();
		panTo();
		zoomTo(ZoomLevel.fromInt(Main.prefs.getInt("map_zoomlevel", 10)));
	}

	private void panTo() {
		currentLat = app.getLatitude();
		currentLon = app.getLongitude();
		if (currentLat == null || currentLon == null)
			return;

		Coordinate coords = new Coordinate(currentLat, currentLon);
		treeMap.setDisplayPosition(coords, Main.prefs.getInt("map_zoomlevel", 10));
	}

	private void zoomTo(ZoomLevel level) {
		currentLevel = level;
		Coordinate coords = new Coordinate(currentLat, currentLon);
		treeMap.setDisplayPosition(coords, currentLevel.toInt());
	}

	private Double lat1 = null;
	private Double lon1 = null;
	private Double lat = null;
	private Double lon = null;
	private Double raz = null;
	private Double maxrad = null;
	private Double minrad = null;
	private Double area = null;

	protected void getFBPValues(OutVariable<Double> lat1, OutVariable<Double> lon1,
			OutVariable<Double> lat, OutVariable<Double> lon,
			OutVariable<Double> minrad, OutVariable<Double> maxrad,
			OutVariable<Double> raz, OutVariable<String> path, OutVariable<String> area) {
		if (this.lat1 == null || this.lon1 == null || this.lat == null || this.lon == null ||
				this.raz == null || this.maxrad == null || this.minrad == null || this.area == null)
			return;
		lat1.value = this.lat1;
		lon1.value = this.lon1;
		lat.value = this.lat;
		lon.value = this.lon;
		minrad.value = this.minrad;
		maxrad.value = this.maxrad;
		raz.value = this.raz;
		String temp = DecimalUtils.format(Convert.convertUnit(this.area, UnitSystem.area(Main.unitSystem()), UnitSystem.area(UnitSystem.METRIC)), DataType.FORCE_ATMOST_2);
		if (Main.unitSystem() == UnitSystem.METRIC)
			temp += Main.resourceManager.getString("ui.label.units.ha");
		else
			temp += Main.resourceManager.getString("ui.label.units.ac");
		area.value = temp;
		URL stream = MapTab.class.getClassLoader()
				.getResource("html/flame.png");
		path.value = stream.toExternalForm();
	}

	protected void calculate(Double lat1, Double lon1, Double dh, Double df, Double db, Double raz,
			OutVariable<Double> lat, OutVariable<Double> lon, OutVariable<Double> maxrad, OutVariable<Double> minrad) {
		if (lat1 == null || lon1 == null || dh == null || df == null
				|| db == null || raz == null)
			return;
		maxrad.value = (dh + db) / 2.0;
		minrad.value = df;
		Double offsetDistance = maxrad.value - db;
		double bearing = -raz;
		double rlat1 = lat1 * Math.PI / 180.0;
		double rlon1 = lon1 * Math.PI / 180.0;
		double rbearing = bearing * Math.PI / 180.0;
		double rdistance = offsetDistance / (6371.01 * 1000);
		double rlat = asin(sin(rlat1) * cos(rdistance) + cos(rlat1)
				* sin(rdistance) * cos(rbearing));
		double rlon;

		if (cos(rlat) == 0 || abs(cos(rlat)) < 0.000001)
			rlon = rlon1;
		else
			rlon = ((rlon1 - asin(sin(rbearing) * sin(rdistance) / cos(rlat)) + PI) % (2 * PI))
					- PI;

		lat.value = rlat * 180.0 / PI;
		lon.value = rlon * 180.0 / PI;
	}

	protected void resetFBPValues(DisplayableMapTab tab) {
		lat1 = app.getLatitude();
		lon1 = app.getLongitude();
		Double dh;
		Double df;
		Double db;
		dh = tab.getDH();
		df = tab.getDF();
		db = tab.getDB();
		raz = tab.getRAZ();
		area = tab.getArea();
		OutVariable<Double> lat = new OutVariable<Double>();
		OutVariable<Double> lon = new OutVariable<Double>();
		OutVariable<Double> maxrad = new OutVariable<Double>();
		OutVariable<Double> minrad = new OutVariable<Double>();
		calculate(lat1, lon1, dh, df, db, raz, lat, lon, maxrad, minrad);
		this.lat = lat.value;
		this.lon = lon.value;
		this.maxrad = maxrad.value;
		this.minrad = minrad.value;
	}
	
	private double computeDistanceBetween(double lat1, double lon1, double lat2, double lon2) {
		double r = 6378137;
		double g1 = Math.toRadians(lat1);
		double g2 = Math.toRadians(lat2);
		double dg = Math.toRadians(lat2 - lat1);
		double dl = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dg / 2.0) * Math.sin(dg / 2) + Math.cos(g1) * Math.cos(g2) * Math.sin(dl / 2.0) * Math.sin(dl / 2.0);
		double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return r * c;
	}

	public void drawFBP(DisplayableMapTab tab) {
		if (!isLoaded)
			return;
		
		needsRedraw = true;
		if (Launcher.javaVersion.major < 9)
			btnMapExport.setEnabled(true);
		resetFBPValues(tab);
		
		fBPPassed = true;
		
		OutVariable<Double> lat1 = new OutVariable<Double>();
		OutVariable<Double> lon1 = new OutVariable<Double>();
		OutVariable<Double> lat = new OutVariable<Double>();
		OutVariable<Double> lon = new OutVariable<Double>();
		OutVariable<Double> minrad = new OutVariable<Double>();
		OutVariable<Double> maxrad = new OutVariable<Double>();
		OutVariable<Double> raz = new OutVariable<Double>();
		OutVariable<String> path = new OutVariable<String>();
		OutVariable<String> area = new OutVariable<String>();
		getFBPValues(lat1, lon1, lat, lon, minrad, maxrad, raz, path, area);

		Coordinate coords = new Coordinate(lat1.value, lon1.value);
		
        double rot = -raz.value*Math.PI/180;
        List<ICoordinate> polyPoints = new ArrayList<ICoordinate>();
        
        double latConv = computeDistanceBetween(lat.value, lon.value, lat.value + 0.1, lon.value) * 10;
        double lngConv = computeDistanceBetween(lat.value, lon.value, lat.value, lon.value + 0.1) * 10;

        double vertexCount = 100;
        double step = 360 / vertexCount;
            
        for(double i = 0; i <= 360.001; i += step) {
            double y = (maxrad.value) * Math.cos(i * Math.PI/180);
            double x = (minrad.value) * Math.sin(i * Math.PI/180);
            double lngT = (x*Math.cos(rot)-y*Math.sin(rot))/lngConv;
            double latT = (y*Math.cos(rot)+x*Math.sin(rot))/latConv;
            
            polyPoints.add(new Coordinate(lat.value + latT, lon.value + lngT));
        }
        firePath = new MapPolygonImpl(polyPoints);
        ((MapObjectImpl) firePath).setBackColor(new Color(255, 0, 0, 80));
        ((MapObjectImpl) firePath).setColor(Color.BLACK);
		
		fireMarker = new MapMarkerFirePerim(coords, path.value);
		
		treeMap.addMapPolygon(firePath);
		treeMap.addMapMarker(fireMarker);
		
		treeMap.setDisplayPosition(coords, Main.prefs.getInt("map_zoomlevel", 0));

		//app.setCurrentTab(app.mapTab);
	}

	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
	    double earthRadius = 3958.75;
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    int meterConversion = 1609;

	    return (dist * meterConversion);
	}

	public void buildPolygon(Polygon poly, double lat, double lon, double maxrad, double minrad, double raz, int numPoints) {
		double rot = -raz * Math.PI / 180.0;
		LinearRing ring = poly.createAndSetOuterBoundaryIs().createAndSetLinearRing();
		double latConv = distFrom(lat, lon, lat + 0.1, lon) * 10;
		double lngConv = distFrom(lat, lon, lat, lon + 0.1) * 10;
		double step = 360.001 / ((double)numPoints);
		for (double i = 0; i <= 360.001; i += step) {
			double y = maxrad * Math.cos(i * Math.PI / 180.0);
			double x = minrad * Math.sin(i * Math.PI / 180.0);
			double ln = (x * Math.cos(rot) - y * Math.sin(rot)) / lngConv;
			double lt = (y * Math.cos(rot) + x * Math.sin(rot)) / latConv;
			ring.addToCoordinates(lon + ln, lat + lt);
		}
	}

	protected void export() {
		OutVariable<Double> lat1 = new OutVariable<Double>();
		OutVariable<Double> lon1 = new OutVariable<Double>();
		OutVariable<Double> lat = new OutVariable<Double>();
		OutVariable<Double> lon = new OutVariable<Double>();
		OutVariable<Double> minrad = new OutVariable<Double>();
		OutVariable<Double> maxrad = new OutVariable<Double>();
		OutVariable<Double> raz = new OutVariable<Double>();
		OutVariable<String> path = new OutVariable<String>();
		OutVariable<String> area = new OutVariable<String>();
		getFBPValues(lat1, lon1, lat, lon, minrad, maxrad, raz, path, area);
		export(lat1.value, lon1.value, lat.value, lon.value, minrad.value, maxrad.value, raz.value);
	}

	public void export(double lat1, double lon1, double dh, double df, double db, double raz) {
		OutVariable<Double> lat = new OutVariable<Double>();
		OutVariable<Double> lon = new OutVariable<Double>();
		OutVariable<Double> maxrad = new OutVariable<Double>();
		OutVariable<Double> minrad = new OutVariable<Double>();
		calculate(lat1, lon1, dh, df, db, raz, lat, lon, maxrad, minrad);
		export(lat1, lon1, lat.value, lon.value, minrad.value, maxrad.value, raz);
	}

	protected void export(double lat1, double lon1, double lat, double lon, double minrad, double maxrad, double raz) {
		boolean cont = false;
		String file = null;
		String dir = Main.prefs.getString("MAP_START_DIR", System.getProperty("user.home"));
		try {
			RFileChooser chooser = RFileChooser.fileSaver();
			chooser.setCurrentDirectory(dir);
			chooser.setTitle(Main.resourceManager.getString("ui.label.map.export.title"));
			String[] extensionFilters;
			String[] extensionFiltersNames;
			extensionFilters = new String[] { "*.kmz", "*.kml", "*.shp"};
			extensionFiltersNames = new String[] { Main.resourceManager.getString("ui.label.file.kmz") + " (*.kmz)",
												   Main.resourceManager.getString("ui.label.file.kml") + " (*.kml)",
												   Main.resourceManager.getString("ui.label.file.shp") + " (*.shp)"};
			chooser.setExtensionFilters(extensionFilters, extensionFiltersNames, 0);
			int retval = chooser.showDialog(this);
			String extension = "kml";
			if (retval == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile().getAbsolutePath();
				Main.prefs.putString("MAP_START_DIR", chooser.getParentDirectory());
				extension = chooser.getSelectedExtension()[0];
				cont = true;
			}

			if(cont) {
				if (!file.endsWith(".kml") && !file.endsWith(".kmz") && !file.endsWith(".shp"))
					file += "." + extension;
				
				if(file.endsWith(".shp"))
					exportShp(file, lat, lon, minrad, maxrad, raz);
				else
					exportKm(file, lat1, lon1, lat, lon, minrad, maxrad, raz);
			}
		}
		catch (Exception e) { }
	}
	
	private void exportShp(String file, double lat, double lon, double minrad, double maxrad, double raz) {
		try {
			double rot = -raz * Math.PI / 180.0;
			double latConv = distFrom(lat, lon, lat + 0.1, lon) * 10;
			double lngConv = distFrom(lat, lon, lat, lon + 0.1) * 10;
			double step = 360.001 / 100.000;
			int j = 0;

			//construct an ellipse from its direction and minimum/maximum radius
			org.locationtech.jts.geom.Coordinate[] coordsJts = new org.locationtech.jts.geom.Coordinate[101];
			for (double i = 0; i <= 360.001; i += step) {
				double y = maxrad * Math.cos(i * Math.PI / 180.0);
				double x = minrad * Math.sin(i * Math.PI / 180.0);
				double ln = (x * Math.cos(rot) - y * Math.sin(rot)) / lngConv;
				double lt = (y * Math.cos(rot) + x * Math.sin(rot)) / latConv;
				
				if(j != 100) {
					coordsJts[j] = new org.locationtech.jts.geom.Coordinate(lon + ln, lat + lt, 0);
					
					j++;
				}
			}
			coordsJts[j] = new org.locationtech.jts.geom.Coordinate(coordsJts[0]);
			
			//create a polygon from a linear ring
			Hints theHint = new Hints(Hints.CRS, DefaultGeographicCRS.WGS84);
			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(theHint);
			org.locationtech.jts.geom.LinearRing ring = geometryFactory.createLinearRing(coordsJts);
			org.locationtech.jts.geom.Polygon polygon = geometryFactory.createPolygon(ring);
			
			SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
			ReferencingObjectFactory refFactory = new ReferencingObjectFactory();
			CoordinateReferenceSystem convertedCRS = refFactory.createFromWKT(DefaultGeographicCRS.WGS84.toWKT());
			typeBuilder.setName("testing");
			typeBuilder.setCRS(convertedCRS);
			typeBuilder.add("the_geom", org.locationtech.jts.geom.Polygon.class);
			SimpleFeatureType featureType = typeBuilder.buildFeatureType();

			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
			featureBuilder.add(polygon);

			// create empty file and prep file-writing stuff
			File theFile = new File(file);
			Map<String, Serializable> params = new HashMap<>();
			params.put("url", theFile.toURI().toURL());
			params.put("create spatial index", Boolean.TRUE);

			FileDataStoreFactorySpi factory = FileDataStoreFinder.getDataStoreFactory("shp");
			DataStore dataStore = factory.createNewDataStore(params);
			dataStore.createSchema(featureType);
			
			SimpleFeatureStore featureStore = (SimpleFeatureStore)dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
			
			Transaction t = new DefaultTransaction();
			try {
				List<SimpleFeature> features = new ArrayList<>();
				features.add(featureBuilder.buildFeature(null));
				SimpleFeatureCollection collection = new ListFeatureCollection(featureType, features);
				featureStore.addFeatures(collection);
				t.commit();
			}
			catch (IOException e) {
				try {
					t.rollback();
				}
				catch (IOException e2) { }
			}
			finally {
				t.close();
			}
			//can't find a way to stop this from being written, so delete it after writing
			Path path = Paths.get(file);
			path = path.resolveSibling(com.google.common.io.Files.getNameWithoutExtension(path.getFileName().toString()) + ".fix");
			Files.deleteIfExists(path);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void exportKm(String file, double lat1, double lon1, double lat, double lon, double minrad, double maxrad, double raz) {
		try {
			boolean iskmz = file.endsWith("z");
			File fl = new File(file);
			Kml kmlfile = new Kml();
			Document d = kmlfile.createAndSetDocument();
			d.withName(fl.getName());
			Placemark ignition = d.createAndAddPlacemark();
			ignition.withName("Ignition Point");
			if (iskmz) {
				Style is = ignition.createAndAddStyle();
				IconStyle icon = is.createAndSetIconStyle();
				icon.setScale(1.5);
				Icon i = icon.createAndSetIcon();
				i.setHref("files/flame.png");
			}
			Point pt = ignition.createAndSetPoint();
			pt.addToCoordinates(lon1, lat1);
			Placemark perim = d.createAndAddPlacemark();
			Style st = perim.createAndAddStyle();
			LineStyle ls = st.createAndSetLineStyle();
			ls.withColor("FF000000");
			ls.withWidth(2);
			PolyStyle ps = st.createAndSetPolyStyle();
			ps.withColor("590000FF");
			ps.withFill(true);
			ps.withOutline(true);
			perim.withName("Fire Perimeter");
			Polygon poly = perim.createAndSetPolygon();
			buildPolygon(poly, lat, lon, maxrad, minrad, raz, 100);
			
			if (iskmz) {
				File f = new File(getHTMLLocation().substring(7).replace("%20", " ") + "/flame.png");
				OutputStream os = new FileOutputStream(fl);
				KmzWriter.packageAsKMZ(os, new KMLDataSource(kmlfile, "doc.kml"), new FileDataSource(f, "flame.png"));
			}
			else
				kmlfile.marshal(fl);
		}
		catch (IOException e) { }
	}

	void mapRefresh() {
		if (!isLoaded || !Main.useMap())
			return;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (app.getCurrentTab() == MapTab.this) {
			loadFinished();
		}
	}

	public static File createTempDirectory() throws IOException {
		final File temp;

		temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

		if (!temp.delete())
			throw new IOException("Could not delete temporary file: " + temp.getAbsolutePath());
		if (!temp.mkdir())
			throw new IOException("Could not create temp direcotyr: " + temp.getAbsolutePath());

		return temp;
	}

	public static String getHTMLLocation() {
		File path = null; 
		try {
			path = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
		} catch (Exception e) {
			path = new File(System.getProperty("user.dir"));
			System.out.println(path);
		}
		return "file:///" + path.toString() + "/html";
	}

	// {{ UI Stuff
	private RButton btnMapZoomToIgnition;
	private RButton btnMapExport;
	private RButton btnReset;
	private RButton btnDownload;

	public void internetFound() {
		if (Main.useMap()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
				}
			});
		}
	}

	@Override
	public void onClosing() {
		super.onClosing();
	}

	protected void initialize() {
		if (initialized)
			return;
		initialized = true;

		setLayout(new BorderLayout());
		/*
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 971, 501);
		else
			setBounds(0, 0, 981, 506);
*/

		if (Main.isWindows())
			setBackground(Color.white);

		JPanel panel1 = new JPanel();
		if (Main.isWindows())
			panel1.setBounds(10, 424, 951, 50);
		else
			panel1.setBounds(10, 419, 951, 50);
		FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
		layout.setAlignOnBaseline(true);
		panel1.setLayout(layout);
		if (Main.isWindows())
			panel1.setBackground(Color.white);

		add(panel1, BorderLayout.AFTER_LAST_LINE);

		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			try {
			    Field field = Tile.class.getField("ERROR_IMAGE");
			    field.setAccessible(true);
			    Field modifiersField = Field.class.getDeclaredField("modifiers");
			    modifiersField.setAccessible(true);
			    modifiersField.set(field, field.getModifiers() & ~Modifier.FINAL);
			    
			    BufferedImage image = ImageIO.read(getClass().getResource("/images/map/error.png"));
			    
			    field.set(null, image);
			}
			catch (NoSuchFieldException|SecurityException|IllegalArgumentException|IllegalAccessException|IOException e1) { }
			return null;
		});
		treeMap = new JMapViewerOffline();
		treeMap.setBounds(10, 10, 951, 391);
		add(treeMap);
		
		JPanel legend = new JPanel();
		legend.setLayout(null);
		legend.setBorder(new TitledBorder(null, Main.resourceManager.getString("ui.label.map.legend")));
		legend.setBackground(Color.white);
		legend.setBounds(treeMap.getWidth() - 10 - 200, treeMap.getHeight() - 10 - 150, 200, 150);
		treeMap.add(legend);

		treeMap.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				legend.setBounds(treeMap.getWidth() - 10 - 200, treeMap.getHeight() - 10 - 150, 200, 150);
			}
		});
		
		mapScaleBar = new MapScalerOffline(treeMap, (Main.unitSystem() == UnitSystem.METRIC));
		mapScaleBar.setBounds(7, 10);
		
		try {
			BufferedImage fireMarker = ImageIO.read(Main.class.getResource("/html/flame.png"));
			JLabel fireMarkerImage = new JLabel(new ImageIcon(fireMarker));
			fireMarkerImage.setBounds(10, 24, 32, 32);
			legend.add(fireMarkerImage);
		}
		catch (IOException e) { }
		
		RLabel FireMarkerToggle = new RLabel(Main.resourceManager.getString("ui.label.map.ignition"));
		FireMarkerToggle.setBounds(67, 30, 150, 20);
		legend.add(FireMarkerToggle);
		
		chckbxFireMarker = new JCheckBox(Main.resourceManager.getString("ui.label.map.ignition"));
		chckbxFireMarker.setSelected(true);
		chckbxFireMarker.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				osmFireMarkerToggle();
			}
		});
		chckbxFireMarker.setBackground(Color.white);
		if (Main.isLinux())
			chckbxFireMarker.setFont(chckbxFireMarker.getFont().deriveFont(12.0f));
		chckbxFireMarker.setHorizontalTextPosition(SwingConstants.RIGHT);
		chckbxFireMarker.setBounds(42, 30, 20, 20);
		legend.add(chckbxFireMarker);
		
		try {
			BufferedImage firePerimMarker = ImageIO.read(Main.class.getResource("/html/fireperim.png"));
			JLabel firePerimMarkerImage = new JLabel(new ImageIcon(firePerimMarker));
			firePerimMarkerImage.setBounds(10, 64, 32, 32);
			legend.add(firePerimMarkerImage);
		}
		catch (IOException e) { }
		
		RLabel FirePerimToggle = new RLabel(Main.resourceManager.getString("ui.label.map.perimeter"));
		FirePerimToggle.setBounds(67, 70, 150, 20);
		legend.add(FirePerimToggle);
		
		chckbxFirePerim = new JCheckBox(Main.resourceManager.getString("ui.label.map.perimeter"));
		chckbxFirePerim.setSelected(true);
		chckbxFirePerim.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				osmFirePerimToggle();
			}
		});
		chckbxFirePerim.setBackground(Color.white);
		if (Main.isLinux())
			chckbxFirePerim.setFont(chckbxFirePerim.getFont().deriveFont(12.0f));
		chckbxFirePerim.setHorizontalTextPosition(SwingConstants.RIGHT);
		chckbxFirePerim.setBounds(42, 70, 20, 20);
		legend.add(chckbxFirePerim);
		
		try {
			BufferedImage weatherStationMarker = ImageIO.read(Main.class.getResource("/html/station_yellow.png"));
			JLabel weatherStationMarkerImage = new JLabel(new ImageIcon(weatherStationMarker));
			weatherStationMarkerImage.setBounds(10, 104, 32, 32);
			legend.add(weatherStationMarkerImage);
		}
		catch (IOException e) { }

		RLabel WeatherStationToggle = new RLabel(Main.resourceManager.getString("ui.label.map.stations"));
		WeatherStationToggle.setBounds(67, 110, 150, 20);
		legend.add(WeatherStationToggle);
		
		chckbxWeatherStations = new JCheckBox(Main.resourceManager.getString("ui.label.map.stations"));
		chckbxWeatherStations.setSelected(true);
		chckbxWeatherStations.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				osmWeatherStationToggle();
			}
		});
		chckbxWeatherStations.setHorizontalTextPosition(SwingConstants.RIGHT);
		chckbxWeatherStations.setBackground(Color.white);
		if (Main.isLinux())
			chckbxWeatherStations.setFont(chckbxWeatherStations.getFont().deriveFont(12.0f));
		chckbxWeatherStations.setBounds(42, 110, 20, 20);
		legend.add(chckbxWeatherStations);
		
		if (curMap == MapType.OSM_ONLINE) {
			btnDownload = new RButton(Main.resourceManager.getString("ui.label.map.download"));
			panel1.add(btnDownload);
			btnDownload.addActionListener((e) -> downloadTiles());
			treeMap.setTileSource(new org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource.Mapnik());
		}
		else if (curMap == MapType.WMS) {
			String url = Main.prefs.getString("wms_url", "");
			org.openstreetmap.josm.data.imagery.ImageryInfo info = new org.openstreetmap.josm.data.imagery.ImageryInfo("Sparcs Online",
					url,
					"wms", null, null);
			org.openstreetmap.josm.data.projection.Projection tileProjection = org.openstreetmap.josm.data.projection.Projections.getProjectionByCode("EPSG:4326");
			treeMap.setTileSource(new org.openstreetmap.josm.data.imagery.TemplatedWMSTileSource(info, tileProjection));
		}
		else if (curMap == MapType.OSM_OFFLINE || !WebDownloader.hasInternetConnection()) {
			try { importData(); } catch (Exception e1) {}
		}
		
		if (Main.useMap()) {
			btnMapZoomToIgnition = new RButton(Main.resourceManager.getString("ui.label.map.zoom"));
			panel1.add(btnMapZoomToIgnition);
			btnMapZoomToIgnition.addActionListener((e) -> {
				panTo();
				zoomTo(ZoomLevel.TWELVE);
			});
		}

		btnMapExport = new RButton(Main.resourceManager.getString("ui.label.map.export"));
		btnMapExport.setEnabled(false);
		panel1.add(btnMapExport);
		btnMapExport.addActionListener((e) -> export());

		btnReset = new RButton(Main.resourceManager.getString("ui.label.footer.reset"));
		btnReset.addActionListener((e) -> reset());
		panel1.add(btnReset);
		
		isLoaded = true;
	}
	
	protected void osmWeatherStationToggle() {
		boolean checked = chckbxWeatherStations.isSelected();
		
		if(checked) {
			if(!osmWeatherStationVisible) {
				addStations();
				osmWeatherStationVisible = true;
			}
		} else {
			if(osmWeatherStationVisible) {
				clearStations();
				osmWeatherStationVisible = false;
			}
		}
		treeMap.repaint();
	}

	protected void osmFireMarkerToggle() {
		boolean checked = chckbxFireMarker.isSelected();
		
		if (fBPPassed) {
			if (checked) {
				if (!osmMapMarkerVisible) {
					treeMap.addMapMarker(fireMarker);
					osmMapMarkerVisible = true;
				}
			} else {
				if (osmMapMarkerVisible) {
					treeMap.removeMapMarker(fireMarker);
					osmMapMarkerVisible = false;
				}
			}
			treeMap.repaint();
		}
			
	}

	protected void osmFirePerimToggle() {
		boolean checked = chckbxFirePerim.isSelected();
		
		if (fBPPassed) {
			if (checked) {
				if (!osmMapPerimVisible) {
					treeMap.addMapPolygon(firePath);
					osmMapPerimVisible = true;
				}
			} else {
				if (osmMapPerimVisible) {
					treeMap.removeMapPolygon(firePath);
					osmMapPerimVisible = false;
				}
			}
			treeMap.repaint();
		}
	}

	protected void weatherStationToggle() {
		if(stationMarkersVisible) {
			stationMarkersVisible = false;
			clearStations();			
		} else {
			stationMarkersVisible = true;
			addStations();
		}
		
	}

	// }}

	@Override
	public void setInternetConnected(boolean conn) { }

	@Override
	public void reset() {
		loadFinished();

		treeMap.removeAllMapPolygons();
		List<MapMarker> markers = treeMap.getMapMarkerList();
		markers.removeIf(m -> m instanceof MapMarkerFirePerim);
        treeMap.repaint();
		Coordinate coords = new Coordinate(currentLat, currentLon);
		treeMap.setDisplayPosition(coords, Main.prefs.getInt("map_zoomlevel", 10));
	}

	@Override
	public boolean supportsReset() {
		return true;
	}

	@Override
	public void onLocationChanged() {
		needsRedraw = true;
		if (app.getCurrentTab() == MapTab.this)
			loadFinished();
	}

	@Override
	public void onTimeZoneChanged() { }

	@Override
	public void onDateChanged() { }

	@Override
	public void onCurrentTabChanged() {
		if (app.getCurrentTab() == MapTab.this && needsRedraw) {
			loadFinished();
		}
	}

	protected static class JavascriptInterface {
		public int getOS() {
			if (Main.isMac())
				return 0;
			if (Main.isLinux())
				return 1;
			return 2;
		}

		public Object formatlatlng(String lat, String lng) {
			String lats = ConvertUtils.formatDegrees(Double.parseDouble(lat));
			String lngs = ConvertUtils.formatDegrees(Double.parseDouble(lng));
			return new Object[] { lats, lngs };
		}

		public String getString(String id) {
			if (id.equals("language")) {
				if (Main.resourceManager.loc.getISO3Language().contains("fr"))
					return "fr";
				if (Main.resourceManager.loc.getISO3Language().contains("es"))
					return "es";
				else
					return "en";
			}
			return Main.resourceManager.getString(id);
		}
		
		public void log(String text) {
			System.out.println("HTML: " + text);
		}
	}
	
	public void settingsUpdated() {
		addStations();
		mapScaleBar.setMetric(Main.unitSystem() == UnitSystem.METRIC);
	}

	@Override
	public void calculated(boolean isCalcd) {
		if (btnMapExport != null) {
			//if (Launcher.javaVersion.major < 9)
				btnMapExport.setEnabled(isCalcd);
		}
	}
	
	public void importData() throws MalformedURLException {
		AppDirs appDirs = AppDirsFactory.getInstance();
		String str = appDirs.getUserDataDir("REDapp", "6", "REDapp") + "\\cache\\";
		File file = new File(str);
		str = str.replace("\\", "/");
		
		String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		
		int minZoom = Integer.parseInt(directories[0]);
		int maxZoom = Integer.parseInt(directories[0]);
		
		for (String dirs : directories) {
			int dirNum = Integer.parseInt(dirs);
			
			if (dirNum < minZoom)
				minZoom = dirNum;
			
			if (dirNum > maxZoom)
				maxZoom = dirNum;
		}
		
		treeMap.setTileSource(new OfflineOsmTileSource((new File(str).toURI().toURL()).toString(), minZoom, maxZoom));
	}
	
	public void downloadTiles() {
		AppDirs appDirs = AppDirsFactory.getInstance();
		String location = appDirs.getUserDataDir("REDapp", "6", "REDapp") + "\\cache\\";
		
		CreateTileInputDialog dlg = null;
		
		double lon = 0;
		try {
			lon = app.getLongitude();
		} catch(NullPointerException e) {
			lon = Double.MAX_VALUE;
		}
		
		double lat = 0;
		try {
			lat = app.getLatitude();
		} catch(NullPointerException e) {
			lat = Double.MAX_VALUE;
		}
		

		dlg = new CreateTileInputDialog(app.frmRedapp, lon, lat);

		setDialogPosition(dlg);
		dlg.setVisible(true);	
		err = dlg.getResult();
		
		if (err == 1) {
			double minLat = dlg.getMinLat();
			double minLon = dlg.getMinLong();
			double maxLat = dlg.getMaxLat();
			double maxLon = dlg.getMaxLong();
			
			int minZoom = dlg.getMinZoom();
			int maxZoom = dlg.getMaxZoom();

			//download = TileCache.cacheBoundingBox(minLat, minLon, maxLat, maxLon, minZoom, maxZoom, location);
			
			CreateTileDownloadDialog dlg2 = null;
			dlg2 = new CreateTileDownloadDialog(app.frmRedapp, minLat, minLon, maxLat, maxLon, minZoom, maxZoom, location);
			setDialogPosition(dlg2);
			dlg2.setVisible(true);	
			err = dlg2.getResult();
		}
	}
	
	private void setDialogPosition(JDialog dlg) {
		int width = dlg.getWidth();
		int height = dlg.getHeight();
		int x = app.frmRedapp.getX();
		int y = app.frmRedapp.getY();
		int rwidth = app.frmRedapp.getWidth();
		int rheight = app.frmRedapp.getHeight();
		x = (int)(x + (rwidth / 2.0) - (width / 2.0));
		y = (int)(y + (rheight / 2.0) - (height / 2.0)) + 30;
		dlg.setLocation(x, y);
	}
}

