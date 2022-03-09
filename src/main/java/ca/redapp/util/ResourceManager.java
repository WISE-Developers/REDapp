/***********************************************************************
 * REDapp - ResourceManager.java
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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import ca.hss.text.TranslationCallback;

/**
 * A manager for getting locale dependent resources.
 */
public class ResourceManager extends TranslationCallback {
	private ResourceBundle textResourceBundle;
	private ResourceBundle imageResourceBundle;
	public final Locale loc;

	/**
	 * Create a manager with the specified locale.
	 * 
	 * @param loc the locale of the manager
	 */
	public ResourceManager(Locale loc) {
		this.loc = loc;
		textResourceBundle = ResourceBundle.getBundle("text", loc);
		imageResourceBundle = ResourceBundle.getBundle("images", loc);
	}

	/**
	 * Get a locale dependent string. If the string doesn't exist in the current locale
	 * it is retrieved from the English locale.
	 * 
	 * @param key the strings key
	 * @return
	 */
	public String getString(String key) {
		String result = key;

		try {
			result = textResourceBundle.getString(key);
		} catch (Exception ex) {
			REDappLogger.info(ex.getMessage());
			if (!loc.getLanguage().equals("en")) {
				try {
					result = ResourceBundle.getBundle("text", Locale.ENGLISH).getString(key);
				}
				catch (Exception e) {
					REDappLogger.error("Error loading string", ex);
				}
			}
		}

		return result;
	}
	
	/**
	 * Get a locale dependent string. If the string doesn't exist in the current locale
	 * it is retrieved from the English locale. Parameters may have been specified in
	 * the string in form <code>'{0}'</code> which will be replaces by the values in
	 * <code>params</code>.
	 * 
	 * @param key the string's key
	 * @param params the parameters that will replace values in the string
	 * @return
	 */
	public String getString(String key, Object... params) {
		String result = key;
		
		try {
			result = MessageFormat.format(textResourceBundle.getString(key), params);
		}
		catch (Exception ex) {
			REDappLogger.error(ex.getMessage());
			if (!loc.getLanguage().equals("en")) {
				try {
					result = MessageFormat.format(ResourceBundle.getBundle("text", Locale.ENGLISH).getString(key), params);
				}
				catch (Exception e) {
					REDappLogger.error("Error loading string", ex);
				}
			}
		}
		
		return result;
	}

	/**
	 * Get an image resource that may be locale dependent.
	 * 
	 * @param key
	 * @return
	 */
	public String getImagePath(String key) {
		String result = key;

		try {
			result = imageResourceBundle.getString(key);
		}
		catch (Exception ex) {
			REDappLogger.error(ex.getMessage());
		}

		return result;
	}

	@Override
	public String translate(String id) {
		return getString(id);
	}
}
