/***********************************************************************
 * REDapp - LoggerFactory.java
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

package ca.redapp.util.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import ca.redapp.ui.Main;

public class LoggerFactory {
	public static Logger createLogger(String name) {
		Logger logger = Logger.getLogger(name);
		try {
			//String path = System.getProperty("java.class.path").split(";")[0];
			//LogManager.getLogManager().readConfiguration(new FileInputStream(path + "/logger.properties"));
			LogManager.getLogManager().readConfiguration(Main.class.getClassLoader().getResourceAsStream("logger.properties"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		logger.setLevel(Level.SEVERE);
		logger.addHandler(new ConsoleHandler());
		logger.addHandler(new LoggerHandler());
		try {
			//String logFilePath = System.getProperty("java.io.tmpdir") + "redapp.log";
			//Handler fileHandler = new FileHandler(logFilePath, 2000, 5);
			//System.out.println("Writing log information to : " + logFilePath);
			//fileHandler.setFormatter(new LoggerFormatter());
			//logger.addHandler(fileHandler);

			for (int i = 0; i < 1000; i++) {
				logger.log(Level.INFO, "Msg" + i);
			}
			logger.log(Level.CONFIG, "Config data");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return logger;
	}
}
