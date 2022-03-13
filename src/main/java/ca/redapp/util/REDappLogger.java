/***********************************************************************
 * REDapp - REDappLogger.java
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class REDappLogger {
	private static Logger logger = null;
	
	public static synchronized void initialize() {
		if (logger == null)
			logger = LoggerFactory.getLogger(ResourceManager.class.getName());
	}
	
	public static void info(String message) {
		initialize();
		logger.info(message);
	}
	
	public static void error(String message) {
		initialize();
		logger.error(message);
	}
	
	public static void error(String message, Exception exc) {
		initialize();
		logger.error(message, exc);
	}
}
