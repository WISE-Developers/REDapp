/***********************************************************************
 * REDapp - NetworkConnectionThread.java
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


import java.util.List;

import ca.hss.general.WebDownloader;
import ca.weather.acheron.Calculator;
import ca.weather.acheron.Calculator.LocationSmall;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;


public class NetworkConnectionThread implements Runnable {
	public List<LocationSmall> locations = null;
	private Main app; 
	
	public NetworkConnectionThread(Main app){
		this.app = app;
	}
	
	@Override
	public void run() {
		AppDirs appDirs = AppDirsFactory.getInstance();
		String dir = appDirs.getUserDataDir("REDapp", "6", "REDapp");
		
		Calculator.setSaveDir(dir);
		
		if (WebDownloader.hasInternetConnection()) {
			locations = Calculator.getLocations();
		} else {
			locations = Calculator.getOfflineLocations();
		}
		app.internetDetected();
	}
}
