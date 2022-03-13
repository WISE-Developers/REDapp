/***********************************************************************
 * REDapp - Launcher.java
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

package ca.redapp.ui;

import java.awt.Color;
import java.awt.EventQueue;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;

import javax.swing.UIManager;

import ca.redapp.util.OSXAdapter;
import ca.redapp.util.REDappLogger;
import ca.redapp.util.WindowsHelper;
import ca.redapp.util.Macify;

public class Launcher {
	public static Macify mac;
	private static Main window;
	private static Launcher launcher;
	public static JavaVersion javaVersion = JavaVersion.VERSION_INVALID;
	public static boolean debugJavaFX = false;
	public static boolean activationAdded = true;
	
	public static enum JavaVersion {
		VERSION_INVALID(-1),
		VERSION_8(8),
		VERSION_9(9),
		VERSION_10(10),
		VERSION_11(11),
		VERSION_GT_10(100);
		
		public final int major;
		
		JavaVersion(int major) {
			this.major = major;
		}
		
		public static JavaVersion getCurrentVersion() {
			String version = System.getProperty("java.version");
		    int pos = version.indexOf('.');
		    if (pos < 0) {
		    	int vers = (int)Double.parseDouble(version);
		    	if (vers == 11)
		    		return JavaVersion.VERSION_11;
		    	else if (vers > 11)
		    		return JavaVersion.VERSION_GT_10;
		    	else
		    		return JavaVersion.VERSION_INVALID;
		    }
		    int pos2 = version.indexOf('.', pos + 1);
		    int majorVersion = (int)Double.parseDouble(version.substring(0, pos));
		    int minorVersion = (int)Double.parseDouble(version.substring(pos + 1, pos2));
		    if (majorVersion == 1 && minorVersion == 8)
	    		return JavaVersion.VERSION_8;
		    else if (majorVersion == 9)
		    	return JavaVersion.VERSION_9;
		    else if (majorVersion == 10)
		    	return JavaVersion.VERSION_10;
		    else if (majorVersion == 11)
		    	return JavaVersion.VERSION_11;
		    else if (majorVersion > 10)
		    	return JavaVersion.VERSION_GT_10;
		    else
		    	return JavaVersion.VERSION_INVALID;
		}
	}

	/**
	 * Launch the application.
	 *
	 * If launching from Eclipse add -swtclasspath as an argument in your run configuration
	 * to disable dynamic loading of the swt library.
	 * If running using Java 9 add `--add-modules java.activation` to the VM arguments.
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].compareToIgnoreCase("-debug") == 0) {
					debugJavaFX = true;
				}
			}
		}

		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "REDapp");

		mac = new Macify();
		mac.setProperties("REDapp");
		
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") >= 0) {
			WindowsHelper.setCurrentProcessExplicitAppUserModelID("ca.hss.app.redapp");
		}
		REDappLogger.initialize();

		final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();

		javaVersion = JavaVersion.getCurrentVersion();

		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			if (classLoader != null) {
				Class<?> jfxPanel = classLoader.loadClass("javafx.embed.swing.JFXPanel");
				@SuppressWarnings("unused")
				Object o = jfxPanel.getConstructor().newInstance();
				if (debugJavaFX)
					System.out.println("JavaFX loaded");
			}
		}
		catch (ClassNotFoundException e) {
			if (debugJavaFX)
				System.out.println("Failed to create a JavaFX class");
		}
		catch (UnsupportedClassVersionError e) {
			if (debugJavaFX)
				System.out.println("Invalid version to create a JavaFX class");
		} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e) {
			e.printStackTrace();
		}

		//System.out.println(UseJavaFX ? "Using Java FX" : "Using SWT");

		if (mac.isMac()) {
			launcher = new Launcher();
			try {
				OSXAdapter.setPreferencesHandler(launcher, launcher.getClass().getDeclaredMethod("showSettings"));
			}
			catch (SecurityException|NoSuchMethodException e) {
			}
			try {
				OSXAdapter.setQuitHandler(launcher, launcher.getClass().getDeclaredMethod("quitApplication"));
			}
			catch (SecurityException|NoSuchMethodException e) {
			}
			try {
				OSXAdapter.setAboutHandler(launcher, launcher.getClass().getDeclaredMethod("showAssumptions"));
			}
			catch (SecurityException|NoSuchMethodException e) {
			}
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					//UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
					UIManager.put("RGroupBox.bordercolor", new Color(0xa8, 0x45, 0x45));
					UIManager.put("RGroupBox.textcolor", new Color(0xb5, 0x35, 0x35));
					UIManager.put("RGroupBox.backcolor", new Color(245, 245, 245));

					window = new Main(bean.getInputArguments());
					window.frmRedapp.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void showSettings() {
		Settings settings = new Settings(window, Main.prefs);
		settings.setVisible(true);
	}

	public void showAssumptions() {
		Assumptions.showAssumptionsDialog(window, Main.prefs);
	}

	public void quitApplication() {
		window.frmRedapp.dispose();
	}
}
