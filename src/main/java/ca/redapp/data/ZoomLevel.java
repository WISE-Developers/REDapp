/***********************************************************************
 * REDapp - ZoomLevel.java
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

package ca.redapp.data;

/**
 * The different zoom levels possible in a Google Map.
 * 
 * @author Travis Redpath
 *
 */
public enum ZoomLevel {
	ZERO(0),
	ONE(1),
	TWO(2),
	THREE(3),
	FOUR(4),
	FIVE(5),
	SIX(6),
	SEVEN(7),
	EIGHT(8),
	NINE(9),
	TEN(10),
	ELEVEN(11),
	TWELVE(12),
	THIRTEEN(13),
	FOURTEEN(14),
	FIFTEEN(15),
	SIXTEEN(16),
	SEVENTEEN(17),
	EIGHTEEN(18),
	NINETEEN(19);
	
	private int value;
	
	ZoomLevel(int val) {
		value = val;
	}
	
	public int toInt() {
		return value;
	}
	
	public static ZoomLevel fromInt(int value) {
		switch (value) {
		case 1:
			return ONE;
		case 2:
			return TWO;
		case 3:
			return THREE;
		case 4:
			return FOUR;
		case 5:
			return FIVE;
		case 6:
			return SIX;
		case 7:
			return SEVEN;
		case 8:
			return EIGHT;
		case 9:
			return NINE;
		case 10:
			return TEN;
		case 11:
			return ELEVEN;
		case 12:
			return TWELVE;
		case 13:
			return THIRTEEN;
		case 14:
			return FOURTEEN;
		case 15:
			return FIFTEEN;
		case 16:
			return SIXTEEN;
		case 17:
			return SEVENTEEN;
		case 18:
			return EIGHTEEN;
		case 19:
			return NINETEEN;
		default:
			return ZERO;
		}
	}
	
	@Override
	public String toString() {
		switch (value) {
		case 1:
			return "1";
		case 2:
			return "2";
		case 3:
			return "3";
		case 4:
			return "4";
		case 5:
			return "5";
		case 6:
			return "6";
		case 7:
			return "7";
		case 8:
			return "8";
		case 9:
			return "9";
		case 10:
			return "10";
		case 11:
			return "11";
		case 12:
			return "12";
		case 13:
			return "13";
		case 14:
			return "14";
		case 15:
			return "15";
		case 16:
			return "16";
		case 17:
			return "17";
		case 18:
			return "18";
		case 19:
			return "19";
		default:
			return "0";
		}
	}
	
	public String getDescription() {
		switch (value) {
		case 0:
			return "Whole World";
		case 9:
			return "Wide Area";
		case 13:
			return "Village or Town";
		case 16:
			return "Small Road";
		default:
			return "";
		}
	}

	public String getScale() {
		switch (value) {
		case 1:
			return "1:250000000";
		case 2:
			return "1:150000000";
		case 3:
			return "1:70000000";
		case 4:
			return "1:35000000";
		case 5:
			return "1:15000000";
		case 6:
			return "1:10000000";
		case 7:
			return "1:4000000";
		case 8:
			return "1:2000000";
		case 9:
			return "1:1000000";
		case 10:
			return "1:500000";
		case 11:
			return "1:250000";
		case 12:
			return "1:150000";
		case 13:
			return "1:70000";
		case 14:
			return "1:35000";
		case 15:
			return "1:15000";
		case 16:
			return "1:8000";
		case 17:
			return "1:4000";
		case 18:
			return "1:2000";
		case 19:
			return "1:1000";
		default:
			return "1:500000000";
		}
	}
}
