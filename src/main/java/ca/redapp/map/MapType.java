/***********************************************************************
 * REDapp - MapType.java
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

public enum MapType {
	JAVAFX, OSM_ONLINE, OSM_OFFLINE, WMS;
	
	public int toInt() {
		int i = -1;
		MapType type = this;
		
		switch(type) {
			case JAVAFX:
			case OSM_ONLINE:
				i = 1;
				break;
				
			case OSM_OFFLINE:
				i = 2;
				break;
				
			case WMS:
				i = 3;
				break;
			
			default:
				i = 2;
		}
		
		return i;
	}
	
	public static MapType fromInt(int i) {
		MapType ret;
		
		switch(i) {
			case 0:
			case 1:
				ret = OSM_ONLINE;
				break;
				
			case 2:
				ret = OSM_OFFLINE;
				break;
			
			case 3:
				ret = WMS;
				break;
			
			default:
				ret = OSM_OFFLINE;
		}
	
		return ret;
	}
}
