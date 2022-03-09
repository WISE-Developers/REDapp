/***********************************************************************
 * REDapp - PropertyDefiner.java
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

import ch.qos.logback.core.PropertyDefinerBase;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class PropertyDefiner extends PropertyDefinerBase {
	
	private String property;
	
	public void setPropertyKey(String property) {
		this.property = property;
	}

	@Override
	public String getPropertyValue() {
		if (property.equals("logback_dir")) {
			AppDirs appDirs = AppDirsFactory.getInstance();
			return appDirs.getUserDataDir("REDapp", "6", "REDapp");
		}
		return "";
	}
}
