/***********************************************************************
 * REDapp - StatsDataType.java
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

import ca.redapp.ui.Launcher;
import ca.redapp.ui.Main;

/**
 * All the data types that can be displayed in the statistics table.
 * 
 * @author Travis Redpath
 *
 */
public enum StatsDataType {
	IMPORT_TEMPERATURE,
	IMPORT_RH,
	IMPORT_WD,
	IMPORT_PRECIP,
	IMPORT_WS,
	IMPORT_DEWPOINT,

	HFFMC,
	HISI,
	HFWI,

	FFMC,
	DMC,
	DC,
	ISI,
	BUI,
	FWI,
	DSR,

	ROSt,
	ROSeq,
	HFI,
	CFB,
	SFC,
	CFC,
	TFC,

	RSO,
	FROS,
	BROS,
	CSI,
	FFI,
	BFI,
	//DT,
	DH,
	DF,
	DB,
	LB,
	AREA,
	PERIMITER,
	
	MIN_TEMP,
	MAX_TEMP,
	MIN_WS,
	MAX_WS,
	DAY_RH,
	DAY_PRECIP,
	DAY_WD,
	
	DAY_FFMC,
	DAY_DMC,
	DAY_DC,
	DAY_ISI,
	DAY_BUI,
	DAY_FWI,
    DAY_DSR,
	
	NOON_TEMPERATURE,
	NOON_RH,
	NOON_WD,
	NOON_PRECIP,
	NOON_WS,
	NOON_DEWPOINT,
	NOON_FFMC,
	NOON_DMC,
	NOON_DC,
	NOON_ISI,
	NOON_BUI,
	NOON_FWI,
    NOON_DSR,
    
    D_FBP_FMC,
    D_FBP_ISI,
    D_FBP_WSV,
    D_FBP_RAZ,

    NOON_ROSt,
    NOON_ROSeq,
    NOON_HFI,
    NOON_CFB,
    NOON_SFC,
    NOON_CFC,
    NOON_TFC,

    NOON_RSO,
    NOON_FROS,
    NOON_BROS,
    NOON_CSI,
    NOON_FFI,
    NOON_BFI,
    //NOON_DT,
    NOON_DH,
    NOON_DF,
    NOON_DB,
    NOON_LB,
    NOON_AREA,
    NOON_PERIMITER,
	
	H_FBP_FMC,
	H_FBP_ISI,
	H_FBP_WSV,
	H_FBP_RAZ,
	
	H_SUNRISE,
	H_SOLAR_NOON,
	H_SUNSET,
	
	SUNRISE,
	SOLAR_NOON,
	SUNSET,
	
	NOON_SUNRISE,
	NOON_SOLAR_NOON,
	NOON_SUNSET,

	UNKNOWN;

	@Override
	public String toString() {
		String retval = Main.resourceManager.getString(getResourceId());
		if (retval.indexOf("<sub>") >= 0)
			retval = "<html><body>" + retval;
		return retval;
	}

	public String getResourceId() {
		switch (this) {
		case IMPORT_TEMPERATURE:
			return "ui.label.weather.abbv.temp";
		case IMPORT_RH:
			return "ui.label.weather.abbv.rh";
		case IMPORT_WD:
			return "ui.label.weather.abbv.wd";
		case IMPORT_PRECIP:
			return "ui.label.weather.abbv.precip";
		case IMPORT_WS:
			return "ui.label.weather.abbv.ws";
		case IMPORT_DEWPOINT:
			return "ui.label.weather.abbv.dew";

		case HFFMC:
			return "ui.label.fire.hffmc";
		case HISI:
			return "ui.label.fire.hisi";
		case HFWI:
			return "ui.label.fire.hfwi";
		case FFMC:
			return "ui.label.fire.ffmc";
		case DMC:
			return "ui.label.fire.dmc";
		case DC:
			return "ui.label.fire.dc";
		case ISI:
			return "ui.label.fire.isi";
		case BUI:
			return "ui.label.fire.bui";
		case FWI:
			return "ui.label.fire.fwi";
		case DSR:
		    return "ui.label.fire.dsr";

		case ROSt:
		case NOON_ROSt:
			return "ui.label.fire.rost";
		case ROSeq:
		case NOON_ROSeq:
			return "ui.label.fire.roseq";
		case HFI:
		case NOON_HFI:
			return "ui.label.fire.hfi";
		case CFB:
		case NOON_CFB:
			return "ui.label.fire.cfb";
		case SFC:
		case NOON_SFC:
			return "ui.label.fire.sfc";
		case CFC:
		case NOON_CFC:
			return "ui.label.fire.cfc";
		case TFC:
		case NOON_TFC:
			return "ui.label.fire.tfc";

		case RSO:
		case NOON_RSO:
			return "ui.label.fire.rso";
		case FROS:
		case NOON_FROS:
			return "ui.label.fire.fros";
		case BROS:
		case NOON_BROS:
			return "ui.label.fire.bros";
		case CSI:
		case NOON_CSI:
			return "ui.label.fire.csi";
		case FFI:
		case NOON_FFI:
			return "ui.label.fire.ffi";
		case BFI:
		case NOON_BFI:
			return "ui.label.fire.bfi";
		case DH:
		case NOON_DH:
			return "ui.label.fire.dh";
		//case DT:
		//case NOON_DT:
		//	return "ui.label.fire.dt";
		case DF:
		case NOON_DF:
			return "ui.label.fire.df";
		case DB:
		case NOON_DB:
			return "ui.label.fire.db";
		case LB:
		case NOON_LB:
			return "ui.label.fire.lb";
		case AREA:
		case NOON_AREA:
			return "ui.label.fire.area";
		case PERIMITER:
		case NOON_PERIMITER:
			return "ui.label.fire.abbv.perim";
			
		case MIN_TEMP:
			return "ui.label.weather.abbv.mintemp";
		case MAX_TEMP:
			return "ui.label.weather.abbv.maxtemp";
		case MIN_WS:
			return "ui.label.weather.abbv.minws";
		case MAX_WS:
			return "ui.label.weather.abbv.maxws";
		case DAY_RH:
			return "ui.label.weather.abbv.dayrh";
		case DAY_PRECIP:
			return "ui.label.weather.abbv.precip";
		case DAY_WD:
			return "ui.label.weather.abbv.wd";
		case DAY_FFMC:
			return "ui.label.fire.ffmc";
		case DAY_DMC:
			return "ui.label.fire.dmc";
		case DAY_DC:
			return "ui.label.fire.dc";
		case DAY_ISI:
			return "ui.label.fire.isi";
		case DAY_BUI:
			return "ui.label.fire.bui";
		case DAY_FWI:
			return "ui.label.fire.fwi";
        case DAY_DSR:
            return "ui.label.fire.dsr";
		
		case NOON_TEMPERATURE:
			return "ui.label.weather.abbv.temp";
		case NOON_RH:
			return "ui.label.weather.abbv.rh";
		case NOON_WD:
			return "ui.label.weather.abbv.wd";
		case NOON_PRECIP:
			return "ui.label.weather.abbv.precip";
		case NOON_WS:
			return "ui.label.weather.abbv.ws";
		case NOON_DEWPOINT:
			return "ui.label.weather.abbv.dew";
		case NOON_DC:
			return "ui.label.fire.dc";
		case NOON_DMC:
			return "ui.label.fire.dmc";
		case NOON_FFMC:
			return "ui.label.fire.ffmc";
		case NOON_BUI:
			return "ui.label.fire.bui";
		case NOON_ISI:
			return "ui.label.fire.isi";
		case NOON_FWI:
			return "ui.label.fire.fwi";
        case NOON_DSR:
            return "ui.label.fire.dsr";

		//Redmine #532
		//case FBP_FMC:
		case H_FBP_FMC:
		//case NOON_FBP_FMC:
		case D_FBP_FMC:
			return "ui.label.fire.fmc";
		//case FBP_ISI:
		case H_FBP_ISI:
		//case NOON_FBP_ISI:
		case D_FBP_ISI:
			return "ui.label.fire.isifbp";
		//case FBP_WSV:
		case H_FBP_WSV:
		//case NOON_FBP_WSV:
		case D_FBP_WSV:
			return "ui.label.fire.wsv";
		//case FBP_RAZ:
		case H_FBP_RAZ:
		//case NOON_FBP_RAZ:
		case D_FBP_RAZ:
			return "ui.label.fire.raz";
			
		case SUNRISE:
		case H_SUNRISE:
		case NOON_SUNRISE:
			return "ui.label.fwi.desc.sunrise";
		case SOLAR_NOON:
		case H_SOLAR_NOON:
		case NOON_SOLAR_NOON:
			return "ui.label.fwi.desc.solarnoon";		
		case SUNSET:
		case H_SUNSET:
		case NOON_SUNSET:
			return "ui.label.fwi.desc.sunset";
		    
		default:
			return "";
		}
	}

	public String getToolTipResourceId() {
		switch (this) {
		case IMPORT_TEMPERATURE:
			return "ui.label.weather.temp";
		case IMPORT_RH:
			return "ui.label.weather.rh";
		case IMPORT_WD:
			return "ui.label.weather.wd";
		case IMPORT_PRECIP:
			return "ui.label.weather.precip";
		case IMPORT_WS:
			return "ui.label.weather.ws";
		case IMPORT_DEWPOINT:
			return "ui.label.weather.dew";

		case HFFMC:
			return "ui.label.fire.desc.hffmc";
		case HISI:
			return "ui.label.fire.desc.hisi";
		case HFWI:
			return "ui.label.fire.desc.hfwi";
		case FFMC:
			return "ui.label.fire.desc.ffmc";
		case DMC:
			return "ui.label.fire.desc.dmc";
		case DC:
			return "ui.label.fire.desc.dc";
		case ISI:
			return "ui.label.fire.desc.isi";
		case BUI:
			return "ui.label.fire.desc.bui";
		case FWI:
			return "ui.label.fire.desc.fwi";
		case DSR:
		    return "ui.label.fire.desc.dsr";

		case ROSt:
        case NOON_ROSt:
			return "ui.label.fire.desc.rost";
		case ROSeq:
        case NOON_ROSeq:
			return "ui.label.fire.desc.roseq";
		case HFI:
        case NOON_HFI:
			return "ui.label.fire.desc.hfi";
		case CFB:
        case NOON_CFB:
			return "ui.label.fire.desc.cfb";
		case SFC:
        case NOON_SFC:
			return "ui.label.fire.desc.sfc";
		case CFC:
        case NOON_CFC:
			return "ui.label.fire.desc.cfc";
		case TFC:
        case NOON_TFC:
			return "ui.label.fire.desc.tfc";

		case RSO:
        case NOON_RSO:
			return "ui.label.fire.desc.rso";
		case FROS:
        case NOON_FROS:
			return "ui.label.fire.desc.fros";
		case BROS:
        case NOON_BROS:
			return "ui.label.fire.desc.bros";
		case CSI:
        case NOON_CSI:
			return "ui.label.fire.desc.csi";
		case FFI:
        case NOON_FFI:
			return "ui.label.fire.desc.ffi";
		case BFI:
        case NOON_BFI:
			return "ui.label.fire.desc.bfi";
		case DH:
        case NOON_DH:
			return "ui.label.fire.desc.dh";
		//case DT:
	        //case DT:
		//	return "ui.label.fire.desc.dt";
		case DF:
        case NOON_DF:
			return "ui.label.fire.desc.df";
		case DB:
        case NOON_DB:
			return "ui.label.fire.desc.db";
		case LB:
        case NOON_LB:
			return "ui.label.fire.desc.lb";
		case AREA:
        case NOON_AREA:
			return "ui.label.fire.desc.area";
		case PERIMITER:
        case NOON_PERIMITER:
			return "ui.label.fire.desc.perim";
			
		
		case MIN_TEMP:
			return "ui.label.weather.mintemp";
		case MAX_TEMP:
			return "ui.label.weather.maxtemp";
		case MIN_WS:
			return "ui.label.weather.minws";
		case MAX_WS:
			return "ui.label.weather.maxws";
		case DAY_RH:
			return "ui.label.weather.dayrh";
		case DAY_PRECIP:
			return "ui.label.weather.precip";
		case DAY_WD:
			return "ui.label.weather.wd";

		case DAY_FFMC:
			return "ui.label.fire.desc.ffmc";
		case DAY_DMC:
			return "ui.label.fire.desc.dmc";
		case DAY_DC:
			return "ui.label.fire.desc.dc";
		case DAY_ISI:
			return "ui.label.fire.desc.isi";
		case DAY_BUI:
			return "ui.label.fire.desc.bui";
		case DAY_FWI:
			return "ui.label.fire.desc.fwi";
        case DAY_DSR:
            return "ui.label.fire.desc.dsr";
			
		case NOON_TEMPERATURE:
			return "ui.label.weather.temp";
		case NOON_RH:
			return "ui.label.weather.rh";
		case NOON_WD:
			return "ui.label.weather.wd";
		case NOON_PRECIP:
			return "ui.label.weather.precip";
		case NOON_WS:
			return "ui.label.weather.ws";
		case NOON_DEWPOINT:
			return "ui.label.weather.dew";
		case NOON_DC:
			return "ui.label.fire.desc.dc";
		case NOON_DMC:
			return "ui.label.fire.desc.dmc";
		case NOON_FFMC:
			return "ui.label.fire.desc.ffmc";
		case NOON_BUI:
			return "ui.label.fire.desc.bui";
		case NOON_ISI:
			return "ui.label.fire.desc.isi";
		case NOON_FWI:
			return "ui.label.fire.desc.fwi";
        case NOON_DSR:
            return "ui.label.fire.desc.dsr";

		//Redmine #532
		//case FBP_FMC:
		case H_FBP_FMC:
		//case NOON_FBP_FMC:
        case D_FBP_FMC:
			return "ui.label.fire.fmc";
		//case FBP_ISI:
		case H_FBP_ISI:
		//case NOON_FBP_ISI:
        case D_FBP_ISI:
			return "ui.label.fire.isifbp";
		//case FBP_WSV:
		case H_FBP_WSV:
		//case NOON_FBP_WSV:
        case D_FBP_WSV:
			return "ui.label.fire.wsv";
		//case FBP_RAZ:
		case H_FBP_RAZ:
		//case NOON_FBP_RAZ:
        case D_FBP_RAZ:
			return "ui.label.fire.raz";
			
		case SUNRISE:
		case H_SUNRISE:
		case NOON_SUNRISE:
			return "ui.label.fwi.desc.sunrise";
		case SOLAR_NOON:
		case H_SOLAR_NOON:
		case NOON_SOLAR_NOON:
			return "ui.label.fwi.desc.solarnoon";		
		case SUNSET:
		case H_SUNSET:
		case NOON_SUNSET:
			return "ui.label.fwi.desc.sunset";
			
		default:
			return "";
		}
	}

	public String toolTip() {
		return Main.resourceManager.getString(getToolTipResourceId());
	}

	public String settingsString() {
		switch (this) {
		case IMPORT_TEMPERATURE:
			return "IMPORTTEMP";
		case IMPORT_RH:
			return "IMPORTRH";
		case IMPORT_WD:
			return "IMPORTWD";
		case IMPORT_PRECIP:
			return "IMPORTPRECIP";
		case IMPORT_WS:
			return "IMPORWS";
		case IMPORT_DEWPOINT:
			return "IMPORTDEWPOINT";
		case HFFMC:
			return "HFFMC";
		case HISI:
			return "HISI";
		case HFWI:
			return "HFWI";
		case FFMC:
			return "FFMC";
		case DMC:
			return "DMC";
		case DC:
			return "DC";
		case ISI:
			return "ISI";
		case BUI:
			return "BUI";
		case FWI:
			return "FWI";
		case DSR:
		    return "DSR";
		case ROSt:
			return "ROST";
		case ROSeq:
			return "ROSEQ";
		case HFI:
			return "HFI";
		case CFB:
			return "CFB";
		case SFC:
			return "SFC";
		case CFC:
			return "CFC";
		case TFC:
			return "TFC";
		case RSO:
			return "RSO";
		case FROS:
			return "FROS";
		case BROS:
			return "BROS";
		case CSI:
			return "CSI";
		case FFI:
			return "FFI";
		case BFI:
			return "BFI";
		//case DT:
		//	return "DT";
		case DH:
			return "DH";
		case DF:
			return "DF";
		case DB:
			return "DB";
		case LB:
			return "LB";
		case AREA:
			return "FAREA";
		case PERIMITER:
			return "FPERIM";

        case NOON_ROSt:
            return "NOON_ROST";
        case NOON_ROSeq:
            return "NOON_ROSEQ";
        case NOON_HFI:
            return "NOON_HFI";
        case NOON_CFB:
            return "NOON_CFB";
        case NOON_SFC:
            return "NOON_SFC";
        case NOON_CFC:
            return "NOON_CFC";
        case NOON_TFC:
            return "NOON_TFC";
        case NOON_RSO:
            return "NOON_RSO";
        case NOON_FROS:
            return "NOON_FROS";
        case NOON_BROS:
            return "NOON_BROS";
        case NOON_CSI:
            return "NOON_CSI";
        case NOON_FFI:
            return "NOON_FFI";
        case NOON_BFI:
            return "NOON_BFI";
        //case NOON_DT:
        //  return "NOON_DT";
        case NOON_DH:
            return "NOON_DH";
        case NOON_DF:
            return "NOON_DF";
        case NOON_DB:
            return "NOON_DB";
        case NOON_LB:
            return "NOON_LB";
        case NOON_AREA:
            return "NOON_FAREA";
        case NOON_PERIMITER:
            return "NOON_FPERIM";
		
		case MIN_TEMP:
			return "DAY_MIN_TEMP";
		case MAX_TEMP:
			return "DAY_MAX_TEMP";
		case MIN_WS:
			return "DAY_MIN_WS";
		case MAX_WS:
			return "DAY_MAX_WS";
		case DAY_RH:
			return "DAY_RH";
		case DAY_PRECIP:
			return "DAY_PRECIP";
		case DAY_WD:
			return "DAY_WD";

		case DAY_FFMC:
			return "DAY_FFMC";
		case DAY_DMC:
			return "DAY_DMC";
		case DAY_DC:
			return "DAY_DC";
		case DAY_ISI:
			return "DAY_ISI";
		case DAY_BUI:
			return "DAY_BUI";
		case DAY_FWI:
			return "DAY_FWI";
        case DAY_DSR:
            return "DAY_DSR";

		case NOON_TEMPERATURE:
			return "NOONTEMP";
		case NOON_RH:
			return "NOONRH";
		case NOON_WD:
			return "NOONWD";
		case NOON_PRECIP:
			return "NOONPRECIP";
		case NOON_WS:
			return "NOONWS";
		case NOON_DEWPOINT:
			return "NOONDEWPOINT";
		case NOON_FFMC:
			return "NOONFFMC";
		case NOON_DMC:
			return "NOONDMC";
		case NOON_DC:
			return "NOONDC";
		case NOON_ISI:
			return "NOONISI";
		case NOON_BUI:
			return "NOONBUI";
		case NOON_FWI:
			return "NOONFWI";
        case NOON_DSR:
            return "NOONDSR";
			
		//Redmine #532
		/*case FBP_FMC:
			return "FBP_FMC";
		case FBP_ISI:
			return "FBP_ISI";
		case FBP_WSV:
			return "FBP_WSV";
		case FBP_RAZ:
			return "FBP_RAZ";*/
		case H_FBP_FMC:
			return "H_FBP_FMC";
		case H_FBP_ISI:
			return "H_FBP_ISI";
		case H_FBP_WSV:
			return "H_FBP_WSV";
		case H_FBP_RAZ:
			return "H_FBP_RAZ";
        case D_FBP_FMC:
            return "D_FBP_FMC";
        case D_FBP_ISI:
            return "D_FBP_ISI";
        case D_FBP_WSV:
            return "D_FBP_WSV";
        case D_FBP_RAZ:
            return "D_FBP_RAZ";
		/*case NOON_FBP_FMC:
			return "NOON_FBP_FMC";
		case NOON_FBP_ISI:
			return "NOON_FBP_ISI";
		case NOON_FBP_WSV:
			return "NOON_FBP_WSV";
		case NOON_FBP_RAZ:
			return "NOON_FBP_RAZ";*/
			
		case SUNRISE:
			return "SUNRISE";
		case SOLAR_NOON:
			return "SOLAR_NOON";
		case SUNSET:
			return "SUNSET";
		case H_SUNRISE:
			return "H_SUNRISE";
		case H_SOLAR_NOON:
			return "H_SOLAR_NOON";
		case H_SUNSET:
			return "H_SUNSET";
		case NOON_SUNRISE:
			return "NOON_SUNRISE";
		case NOON_SOLAR_NOON:
			return "NOON_SOLAR_NOON";
		case NOON_SUNSET:
			return "NOON_SUNSET";
			
		default:
			return "";
		}
	}

	public static StatsDataType fromSettingsString(String val) {
		if (val.compareTo("IMPORTTEMP") == 0)
			return IMPORT_TEMPERATURE;
		if (val.compareTo("IMPORTRH") == 0)
			return IMPORT_RH;
		if (val.compareTo("IMPORTWD") == 0)
			return IMPORT_WD;
		if (val.compareTo("IMPORTPRECIP") == 0)
			return IMPORT_PRECIP;
		if (val.compareTo("IMPORWS") == 0)
			return IMPORT_WS;
		if (val.compareTo("IMPORTDEWPOINT") == 0)
			return IMPORT_DEWPOINT;
		if (val.compareTo("HFFMC") == 0)
			return HFFMC;
		if (val.compareTo("HISI") == 0)
			return HISI;
		if (val.compareTo("HFWI") == 0)
			return HFWI;
		if (val.compareTo("FFMC") == 0)
			return FFMC;
		if (val.compareTo("DMC") == 0)
			return DMC;
		if (val.compareTo("DC") == 0)
			return DC;
		if (val.compareTo("ISI") == 0)
			return ISI;
		if (val.compareTo("BUI") == 0)
			return BUI;
		if (val.compareTo("FWI") == 0)
			return FWI;
		if (val.compareTo("DSR") == 0)
		    return DSR;
		if (val.compareTo("ROST") == 0)
			return ROSt;
		if (val.compareTo("ROSEQ") == 0)
			return ROSeq;
		if (val.compareTo("HFI") == 0)
			return HFI;
		if (val.compareTo("CFB") == 0)
			return CFB;
		if (val.compareTo("SFC") == 0)
			return SFC;
		if (val.compareTo("CFC") == 0)
			return CFC;
		if (val.compareTo("TFC") == 0)
			return TFC;
		if (val.compareTo("RSO") == 0)
			return RSO;
		if (val.compareTo("FROS") == 0)
			return FROS;
		if (val.compareTo("BROS") == 0)
			return BROS;
		if (val.compareTo("CSI") == 0)
			return CSI;
		if (val.compareTo("FFI") == 0)
			return FFI;
		if (val.compareTo("BFI") == 0)
			return BFI;
		//if (val.compareTo("DT") == 0)
		//	return DT;
		if (val.compareTo("DH") == 0)
			return DH;
		if (val.compareTo("DF") == 0)
			return DF;
		if (val.compareTo("DB") == 0)
			return DB;
		if (val.compareTo("LB") == 0)
			return LB;
		if (val.compareTo("FAREA") == 0)
			return AREA;
		if (val.compareTo("FPERIM") == 0)
			return PERIMITER;

        if (val.compareTo("NOON_ROST") == 0)
            return NOON_ROSt;
        if (val.compareTo("NOON_ROSEQ") == 0)
            return NOON_ROSeq;
        if (val.compareTo("NOON_HFI") == 0)
            return NOON_HFI;
        if (val.compareTo("NOON_CFB") == 0)
            return NOON_CFB;
        if (val.compareTo("NOON_SFC") == 0)
            return NOON_SFC;
        if (val.compareTo("NOON_CFC") == 0)
            return NOON_CFC;
        if (val.compareTo("NOON_TFC") == 0)
            return NOON_TFC;
        if (val.compareTo("NOON_RSO") == 0)
            return NOON_RSO;
        if (val.compareTo("NOON_FROS") == 0)
            return NOON_FROS;
        if (val.compareTo("NOON_BROS") == 0)
            return NOON_BROS;
        if (val.compareTo("NOON_CSI") == 0)
            return NOON_CSI;
        if (val.compareTo("NOON_FFI") == 0)
            return NOON_FFI;
        if (val.compareTo("NOON_BFI") == 0)
            return NOON_BFI;
        //if (val.compareTo("NOON_DT") == 0)
        //  return NOON_DT;
        if (val.compareTo("NOON_DH") == 0)
            return NOON_DH;
        if (val.compareTo("NOON_DF") == 0)
            return NOON_DF;
        if (val.compareTo("NOON_DB") == 0)
            return NOON_DB;
        if (val.compareTo("NOON_LB") == 0)
            return NOON_LB;
        if (val.compareTo("NOON_FAREA") == 0)
            return NOON_AREA;
        if (val.compareTo("NOON_FPERIM") == 0)
            return NOON_PERIMITER;
		
		if (val.compareTo("DAY_MIN_TEMP") == 0)
			return MIN_TEMP;
		if (val.compareTo("DAY_MAX_TEMP") == 0)
			return MAX_TEMP;
		if (val.compareTo("DAY_MIN_WS") == 0)
			return MIN_WS;
		if (val.compareTo("DAY_MAX_WS") == 0)
			return MAX_WS;
		if (val.compareTo("DAY_RH") == 0)
			return DAY_RH;
		if (val.compareTo("DAY_PRECIP") == 0)
			return DAY_PRECIP;
		if (val.compareTo("DAY_WD") == 0)
			return DAY_WD;
		if (val.compareTo("DAY_FFMC") == 0)
			return DAY_FFMC;
		if (val.compareTo("DAY_DMC") == 0)
			return DAY_DMC;
		if (val.compareTo("DAY_DC") == 0)
			return DAY_DC;
		if (val.compareTo("DAY_ISI") == 0)
			return DAY_ISI;
		if (val.compareTo("DAY_BUI") == 0)
			return DAY_BUI;
		if (val.compareTo("DAY_FWI") == 0)
			return DAY_FWI;
        if (val.compareTo("DAY_DSR") == 0)
            return DAY_DSR;
		if (val.compareTo("NOONTEMP") == 0)
			return NOON_TEMPERATURE;
		if (val.compareTo("NOONRH") == 0)
			return NOON_RH;
		if (val.compareTo("NOONWD") == 0)
			return NOON_WD;
		if (val.compareTo("NOONPRECIP") == 0)
			return NOON_PRECIP;
		if (val.compareTo("NOONWS") == 0)
			return NOON_WS;
		if (val.compareTo("NOONDEWPOINT") == 0)
			return NOON_DEWPOINT;
		if (val.compareTo("NOONDC") == 0)
			return NOON_DC;
		if (val.compareTo("NOONDMC") == 0)
			return NOON_DMC;
		if (val.compareTo("NOONFFMC") == 0)
			return NOON_FFMC;
		if (val.compareTo("NOONBUI") == 0)
			return NOON_BUI;
		if (val.compareTo("NOONISI") == 0)
			return NOON_ISI;
		if (val.compareTo("NOONFWI") == 0)
			return NOON_FWI;
        if (val.compareTo("NOONDSR") == 0)
            return NOON_DSR;

		if (val.compareTo("H_FBP_FMC") == 0)
			return H_FBP_FMC;
		if (val.compareTo("H_FBP_ISI") == 0)
			return H_FBP_ISI;
		if (val.compareTo("H_FBP_WSV") == 0)
			return H_FBP_WSV;
		if (val.compareTo("H_FBP_RAZ") == 0)
			return H_FBP_RAZ;

        if (val.compareTo("D_FBP_FMC") == 0)
            return D_FBP_FMC;
        if (val.compareTo("D_FBP_ISI") == 0)
            return D_FBP_ISI;
        if (val.compareTo("D_FBP_WSV") == 0)
            return D_FBP_WSV;
        if (val.compareTo("D_FBP_RAZ") == 0)
            return D_FBP_RAZ;
		
		if (val.compareTo("SUNRISE") == 0)
			return SUNRISE;
		if (val.compareTo("SOLAR_NOON") == 0)
			return SOLAR_NOON;
		if (val.compareTo("SUNSET") == 0)
			return SUNSET;
		if (val.compareTo("H_SUNRISE") == 0)
			return H_SUNRISE;
		if (val.compareTo("H_SOLAR_NOON") == 0)
			return H_SOLAR_NOON;
		if (val.compareTo("H_SUNSET") == 0)
			return H_SUNSET;
		if (val.compareTo("NOON_SUNRISE") == 0)
			return NOON_SUNRISE;
		if (val.compareTo("NOON_SOLAR_NOON") == 0)
			return NOON_SOLAR_NOON;
		if (val.compareTo("NOON_SUNSET") == 0)
			return NOON_SUNSET;
		
		return UNKNOWN;
	}

	public boolean isHourlyData() {
		switch (this) {
		case HFFMC:
		case HISI:
		case HFWI:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Get the group that this data type belongs to.
	 */
	public DataTypeGroup dataTypeGroup() {
		switch (this) {
		
		case FFMC:
		case DMC:
		case DC:
		case ISI:
		case BUI:
		case FWI:
		case DSR:
		case HFFMC:
		case HISI:
		case HFWI:
			return DataTypeGroup.FWI_SYSTEM;
			
		case H_FBP_FMC:
		case H_FBP_ISI:
		case H_FBP_WSV:
		case H_FBP_RAZ:
			return DataTypeGroup.H_FBP_SYSTEM;
			
		case ROSt:
		case ROSeq:
		case HFI:
		case CFB:
		case SFC:
		case CFC:
		case TFC:
			return DataTypeGroup.FBP_SYSTEM_PRIMARY;
		case RSO:
		case FROS:
		case BROS:
		case CSI:
		case FFI:
		case BFI:
		//case DT:
		case DH:
		case DF:
		case DB:
		case LB:
		case AREA:
		case PERIMITER:
			return DataTypeGroup.FBP_SYSTEM_SECONDARY;
			
		case MIN_TEMP:
		case MAX_TEMP:
		case MIN_WS:
		case MAX_WS:
		case DAY_RH:
		case DAY_PRECIP:
		case DAY_WD:
			return DataTypeGroup.DAY_WEATHER;
		case DAY_FFMC:
		case DAY_DMC:
		case DAY_DC:
		case DAY_ISI:
		case DAY_BUI:
		case DAY_FWI:
        case DAY_DSR:
			return DataTypeGroup.DAY_FWI_SYSTEM;
		case NOON_TEMPERATURE:
		case NOON_DEWPOINT:
		case NOON_RH:
		case NOON_WS:
		case NOON_WD:
		case NOON_PRECIP:
			return DataTypeGroup.NOON_WEATHER;
		case NOON_FFMC:
		case NOON_DMC:
		case NOON_DC:
		case NOON_ISI:
		case NOON_BUI:
		case NOON_FWI:
		case NOON_DSR:
			return DataTypeGroup.NOON_FWI_SYSTEM;

        case D_FBP_FMC:
        case D_FBP_ISI:
        case D_FBP_WSV:
        case D_FBP_RAZ:
            return DataTypeGroup.D_FBP_SYSTEM;
            
        case NOON_ROSt:
        case NOON_ROSeq:
        case NOON_HFI:
        case NOON_CFB:
        case NOON_SFC:
        case NOON_CFC:
        case NOON_TFC:
            return DataTypeGroup.NOON_FBP_SYSTEM_PRIMARY;
        case NOON_RSO:
        case NOON_FROS:
        case NOON_BROS:
        case NOON_CSI:
        case NOON_FFI:
        case NOON_BFI:
        //case NOON_DT:
        case NOON_DH:
        case NOON_DF:
        case NOON_DB:
        case NOON_LB:
        case NOON_AREA:
        case NOON_PERIMITER:
            return DataTypeGroup.NOON_FBP_SYSTEM_SECONDARY;
			
		case SUNRISE:
		case SOLAR_NOON:
		case SUNSET:
			return DataTypeGroup.SOLAR_VALUES;
			
		case H_SUNRISE:
		case H_SOLAR_NOON:
		case H_SUNSET:
			return DataTypeGroup.H_SOLAR_VALUES;
		case NOON_SUNRISE:
		case NOON_SOLAR_NOON:
		case NOON_SUNSET:
			return DataTypeGroup.NOON_SOLAR_VALUES;
			
		default:
			return DataTypeGroup.WEATHER;
		}
	}

	public static enum DataTypeGroup {
		WEATHER,
		DAY_WEATHER,
		NOON_WEATHER,
		FWI_SYSTEM,
		DAY_FWI_SYSTEM,
		NOON_FWI_SYSTEM,
		//FBP_SYSTEM,
		H_FBP_SYSTEM,
		//NOON_FBP_SYSTEM,
		FBP_SYSTEM_PRIMARY,
		FBP_SYSTEM_SECONDARY,
        D_FBP_SYSTEM,
        NOON_FBP_SYSTEM_PRIMARY,
        NOON_FBP_SYSTEM_SECONDARY,
		SOLAR_VALUES,
		H_SOLAR_VALUES,
		NOON_SOLAR_VALUES;

		@Override
		public String toString() {
			return Main.resourceManager.getString(getResourceId());
		}
		
		public boolean isDaily() {
			switch (this) {
			case DAY_WEATHER:
			case DAY_FWI_SYSTEM:
			//case FBP_SYSTEM:
			case SOLAR_VALUES:
				return true;
			default:
				return false;
			}
		}
		
		public boolean isNoon() {
			switch (this) {
			case NOON_WEATHER:
			case NOON_FWI_SYSTEM:
			//case NOON_FBP_SYSTEM:
			case NOON_SOLAR_VALUES:
			case D_FBP_SYSTEM:
			case NOON_FBP_SYSTEM_PRIMARY:
			case NOON_FBP_SYSTEM_SECONDARY:
				return true;
			default:
				return false;
			}
		}

		public String getResourceId() {
			switch (this) {
			case FWI_SYSTEM:
				return "ui.label.stats.fwisystem";

			case H_FBP_SYSTEM:
            case D_FBP_SYSTEM:
				return "ui.label.stats.fbpsystem";
				
			case SOLAR_VALUES:
			case H_SOLAR_VALUES:
			case NOON_SOLAR_VALUES:
				return "ui.label.stats.solarvalues";
				
			case DAY_FWI_SYSTEM:
				return "ui.label.stats.fwisystem";
			case NOON_FWI_SYSTEM:
				return "ui.label.stats.fwisystem";
			case FBP_SYSTEM_PRIMARY:
            case NOON_FBP_SYSTEM_PRIMARY:
				return Launcher.mac.isMac() ? "ui.label.stats.fbpsystempshort" : "ui.label.stats.fbpsystemp";
			case FBP_SYSTEM_SECONDARY:
            case NOON_FBP_SYSTEM_SECONDARY:
				return Launcher.mac.isMac() ? "ui.label.stats.fbpsystemsshort" : "ui.label.stats.fbpsystems";
			case DAY_WEATHER:
				return "ui.label.stats.export.daily";
			default:
				return "ui.label.stats.weathersystem";
			}
		}
	}
}
