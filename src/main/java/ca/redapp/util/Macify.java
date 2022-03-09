/***********************************************************************
 * REDapp - macify.java
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

import java.awt.Image;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Helper class for setting OSX specific values.
 * 
 * @author Travis Redpath
 *
 */
public class Macify {
	private Object application;
	@SuppressWarnings({ "unused" })
	private Class<?> applicationClassListener;
	
	@SuppressWarnings("deprecation")
	public Macify() {
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			try {
				final File file = new File("/System/Library/Java");
				if (file.exists()) {
					ClassLoader scl = ClassLoader.getSystemClassLoader();
					Class<?> clc = scl.getClass();
					if (URLClassLoader.class.isAssignableFrom(clc)) {
						Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
						addUrl.setAccessible(true);
						addUrl.invoke(scl, new Object[]{ file.toURL() });
					}
				}
				
				Class<?> appClass = Class.forName("com.apple.eawt.Application");
				application = appClass.getMethod("getApplication", new Class[0]).invoke(null, new Object[0]);
				applicationClassListener = Class.forName("com.apple.eawt.ApplicationListener");
			}
			catch (ClassNotFoundException|IllegalAccessException|NoSuchMethodException|InvocationTargetException|MalformedURLException e) {
			}
			return null;
		});
	}
	
	public boolean isMac() {
		return application != null;
	}
	
	public void setProperties(String name) {
		if (isMac()) {
			setEnabledAboutMenu(false);
		}
	}

    public void setEnabledAboutMenu(boolean enabled) {
        if (isMac()) {
            callMethod(application, "setEnabledAboutMenu", new Class[] { Boolean.TYPE }, new Object[] { Boolean.valueOf(enabled) });
        }
    }
	
	public void setDockIconImage(Image img) {
		if (isMac()) {
			try {
				Method setDockIconImage = application.getClass().getMethod("setDockIconImage", Image.class);
				try {
					setDockIconImage.invoke(application, img);
				}
				catch (IllegalAccessException|InvocationTargetException e) { }
			}
			catch (NoSuchMethodException mnfe) { }
		}
	}

	private Object callMethod(Object object, String methodname, Class<?>[] classes, Object[] arguments) {
        try {
            if (classes == null) {
                classes = new Class[arguments.length];
                for (int i = 0; i < classes.length; i++) {
                    classes[i] = arguments[i].getClass();
                }
            }
            Method addListnerMethod = object.getClass().getMethod(methodname, classes);
            return addListnerMethod.invoke(object, arguments);
        }
        catch (NoSuchMethodException|IllegalAccessException|InvocationTargetException e) {
        	throw new RuntimeException(e);
        }
    }
}
