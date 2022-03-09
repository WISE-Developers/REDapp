/***********************************************************************
 * REDapp - OSXAdapter.java
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

import java.lang.reflect.*;

/**
 * Helper for setting handlers for when the user clicks items in OSX's menu.
 * 
 * @author Travis Redpath
 *
 */
public class OSXAdapter implements InvocationHandler {
    protected Object targetObject;
    protected Method targetMethod;
    protected String proxySignature;

    static volatile Object macOSXApplication;

    public static void setQuitHandler(Object target, Method quitHandler) {
    	setHandler(new OSXAdapter("handleQuit", target, quitHandler));
    }

    // Pass this method an Object and a Method equipped to display application options
    // They will be called when the Preferences menu item is selected from the application menu
    public static void setPreferencesHandler(Object target, Method prefsHandler) {
        boolean enablePrefsMenu = (target != null && prefsHandler != null);
        if (enablePrefsMenu) {
            setHandler(new OSXAdapter("handlePreferences", target, prefsHandler));
        }

        // If we're setting a handler, enable the Preferences menu item by calling
        // com.apple.eawt.Application reflectively
        try {
            Method enablePrefsMethod = macOSXApplication.getClass().getDeclaredMethod("setEnabledPreferencesMenu", new Class[] { boolean.class });
            enablePrefsMethod.invoke(macOSXApplication, new Object[] { Boolean.valueOf(enablePrefsMenu) });
        }
        catch (Exception ex) {
            System.err.println("OSXAdapter could not access the About Menu");
            ex.printStackTrace();
        }
    }

    public static void setAboutHandler(Object target, Method aboutHandler) {
    	boolean enableAboutMenu = (target != null && aboutHandler != null);
    	if (enableAboutMenu) {
    		setHandler(new OSXAdapter("handleAbout", target, aboutHandler));
    	}

    	try {
    		Method enableAboutMethod = macOSXApplication.getClass().getDeclaredMethod("setEnabledAboutMenu", new Class[] { boolean.class });
    		enableAboutMethod.invoke(macOSXApplication, new Object[] { Boolean.valueOf(enableAboutMenu) });
    	}
    	catch (Exception ex) {
    		System.err.println("OSXAdapter could not access the About Menu");
    		ex.printStackTrace();
    	}
    }

    // setHandler creates a Proxy object from the passed OSXAdapter and adds it as an ApplicationListener
    public static void setHandler(OSXAdapter adapter) {
        try {
            Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
            if (macOSXApplication == null) {
                macOSXApplication = applicationClass.getConstructor((Class[])null).newInstance((Object[])null);
            }
            Class<?> applicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener");
            Method addListenerMethod = applicationClass.getDeclaredMethod("addApplicationListener", new Class[] { applicationListenerClass });

            // Create a proxy object around this handler that can be reflectively added as an Apple ApplicationListener
            Object osxAdapterProxy = Proxy.newProxyInstance(OSXAdapter.class.getClassLoader(), new Class[] { applicationListenerClass }, adapter);
            addListenerMethod.invoke(macOSXApplication, new Object[] { osxAdapterProxy });
        }
        catch (ClassNotFoundException cnfe) {
            System.err.println("This version of Mac OS X does not support the Apple EAWT.  ApplicationEvent handling has been disabled (" + cnfe + ")");
        }
        catch (Exception ex) {  // Likely a NoSuchMethodException or an IllegalAccessException loading/invoking eawt.Application methods
            System.err.println("Mac OS X Adapter could not talk to EAWT:");
            ex.printStackTrace();
        }
    }

    // Each OSXAdapter has the name of the EAWT method it intends to listen for (handleAbout, for example),
    // the Object that will ultimately perform the task, and the Method to be called on that Object
    protected OSXAdapter(String ProxySignature, Object target, Method handler) {
        this.proxySignature = ProxySignature;
        this.targetObject = target;
        this.targetMethod = handler;
    }

    // Override this method to perform any operations on the event
    // that comes with the various callbacks
    // See setFileHandler above for an example
    public boolean callTarget(Object appleEvent) throws InvocationTargetException, IllegalAccessException {
        Object result = targetMethod.invoke(targetObject, (Object[])null);
        if (result == null) {
            return true;
        }
        return Boolean.valueOf(result.toString()).booleanValue();
    }

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isCorrectMethod(method, args)) {
            boolean handled = callTarget(args[0]);
            setApplicationEventHandled(args[0], handled);
        }

        // All of the ApplicationListener methods are void; return null regardless of what happens
        return null;
	}

    // Compare the method that was called to the intended method when the OSXAdapter instance was created
    // (e.g. handleAbout, handleQuit, handleOpenFile, etc.)
    protected boolean isCorrectMethod(Method method, Object[] args) {
        return (targetMethod != null && proxySignature.equals(method.getName()) && args.length == 1);
    }

    // It is important to mark the ApplicationEvent as handled and cancel the default behavior
    // This method checks for a boolean result from the proxy method and sets the event accordingly
    protected void setApplicationEventHandled(Object event, boolean handled) {
        if (event != null) {
            try {
                Method setHandledMethod = event.getClass().getDeclaredMethod("setHandled", new Class[] { boolean.class });

                // If the target method returns a boolean, use that as a hint
                setHandledMethod.invoke(event, new Object[] { Boolean.valueOf(handled) });
            }
            catch (Exception ex) {
                System.err.println("OSXAdapter was unable to handle an ApplicationEvent: " + event);
                ex.printStackTrace();
            }
        }
    }
}
