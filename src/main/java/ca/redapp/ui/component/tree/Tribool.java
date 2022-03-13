/***********************************************************************
 * REDapp - Tribool.java
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

package ca.redapp.ui.component.tree;

/**
 * A boolean value with an intermediate state.
 * 
 * @author Travis Redpath
 *
 */
public enum Tribool {
	FALSE(0),
	TRUE(1),
	INDETERMINATE(2);
	
	int index;
	
	Tribool(int index) {
		this.index = index;
	}
	
	@Override
	public String toString() {
		switch (index) {
		case 1:
			return "true";
		case 2:
			return "indeterminate";
		default:
			return "false";
		}
	}
}
