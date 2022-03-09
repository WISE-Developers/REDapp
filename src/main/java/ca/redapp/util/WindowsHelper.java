/***********************************************************************
 * REDapp - WindowsHelper.java
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

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.WString;

/**
 * Helper class for setting Windows specific values.
 * 
 * @author Travis Redpath
 *
 */
public class WindowsHelper {
	static {
		try {
			Native.register("shell32");
		}
		catch (UnsatisfiedLinkError e) {
		}
	}

	/*public enum ProgressState {
		NO_PROGRESS(0x0),
		INDETERMINATE(0x1),
		NORMAL(0x2),
		ERROR(0x4),
		PAUSED(0x8);

		public final int value;

		ProgressState(int value) {
			this.value = value;
		}
	}*/

	/*private static final int CLSCTX_INPROC_SERVER = 0x1;
	private static final int CLSCTX_INPROC_HANDLER = 0x2;
	private static final int CLSCTX_LOCAL_SERVER = 0x4;
	private static final int CLSCTX_REMOTE_SERVER = 0x10;

	private static final int CLSCTX_SERVER = CLSCTX_INPROC_SERVER | CLSCTX_LOCAL_SERVER | CLSCTX_REMOTE_SERVER;

	public static final int S_OK                   = 0;
	public static final int S_FALSE                = 1;
	public static final int REGDB_E_CLASSNOTREG    = 0x80040154;
	public static final int CLASS_E_NOAGGREGATION  = 0x80040110;
	public static final int CO_E_NOTINITIALIZED    = 0x800401F0;*/

//	private static HWND hwnd = null;

	/**
	 * Set the current processes ID. This allows multiple instances to be combined into one
	 * taskbar icon in versions of Windows that support it.
	 * 
	 * @param appID
	 */
	public static void setCurrentProcessExplicitAppUserModelID(final String appID) {
		String os = System.getProperty("os.name");
		if (!os.contains("Windows"))
			return;
		String split[] = os.split(" ");
		if (split.length < 2)
			return;
		int ver;
		try {
			ver = Integer.parseInt(split[1]);
			if (ver < 7)
				return;
		}
		catch (NumberFormatException e) {
			return;
		}
		try {
			SetCurrentProcessExplicitAppUserModelID(new WString(appID));
		}
		catch (UnsatisfiedLinkError e) {
		}
	}

//	private static HWND FindWindow(String title) {
//		return User32.INSTANCE.FindWindow(null, title);
//	}

	public static void initialize(String title) {
//		hwnd = FindWindow(title);
//		Ole32.INSTANCE.CoInitialize(null);
	}

//	public static void setProgressState(ProgressState state) {
//		PointerByReference ref = new PointerByReference();
//		HRESULT res = Ole32.INSTANCE.CoCreateInstance(new GUID("{56FDF344-FD6D-11D0-958A-006097C9A090}"), null, CLSCTX_SERVER,
//				new GUID("{EA1AFB91-9E28-4B86-90E9-9E9F8A5EEFAF}"), ref);
//		if (res.intValue() != S_OK) {
//			return;
//		}
//		ITaskBarList3 list = (ITaskBarList3)Proxy.newProxyInstance(ITaskBarList3.class.getClassLoader(), new Class<?>[] { ITaskBarList3.class }, new ComObject(ref.getValue()));
//		list.SetProgressState(hwnd, state.value);
//		return;
//	}

	private static native NativeLong SetCurrentProcessExplicitAppUserModelID(WString appID);

//	public static interface IUnknown {
//		int QueryInterface(GUID riid, PointerByReference ppvObject);
//		int AddRef();
//		int Release();
//	}
//
//	public static @interface VTID {
//		int value();
//	}
//
//	public static interface ITaskBarList3 extends IUnknown {
//		@VTID(0)
//		int SetProgressState(
//				  /*[in]*/  HWND hwnd,
//				  /*[in]*/  int tbpFlags
//				);
//	}
//
//	public static class ComObject implements InvocationHandler {
//		private Pointer _InterfacePtr = null;
//
//		public ComObject(Pointer p) {
//			_InterfacePtr = p;
//		}
//
//		Object[] prepareArgsPlusRetVal(Method method, Object[] args) {
//			Object[] aarg;
//			if (args != null) {
//				aarg = new Object[2 + args.length];
//				for (int i = 0; i < args.length; i++) {
//					Object givenArg = args[i];
//					aarg[i + 1] = givenArg;
//				}
//			}
//			else {
//				aarg = new Object[2];
//			}
//			aarg[0] = _InterfacePtr;
//			return aarg;
//		}
//
//		Object[] prepareArgs(Method method, Object[] args) {
//			Object[] aarg;
//			if (args != null) {
//				aarg = new Object[args.length];
//				for (int i = 0; i < args.length; i++) {
//					Object givenArg = args[i];
//					aarg[i] = givenArg;
//				}
//			}
//			else {
//				aarg = new Object[1];
//			}
//			//aarg[0] = _InterfacePtr;
//			return aarg;
//		}
//
//		Object[] prepareArgs(Method method, Object[] args, IntByReference retval) {
//			Object[] aarg;
//			aarg = prepareArgs(method, args);
//			//aarg[aarg.length - 1] = retval;
//			return aarg;
//		}
//
//		public static <T extends IUnknown> T wrapNativeInterface(Pointer interfacePointer, Class<T> intrface) {
//	        return createProxy(new ComObject(interfacePointer), intrface);
//	    }
//
//	    @SuppressWarnings("unchecked")
//		private static<T> T createProxy(ComObject object, Class<T> intrface) {
//	        T p = (T) Proxy.newProxyInstance(ComObject.class.getClassLoader(), new Class<?>[] {intrface}, object);
//	        return p;
//	    }
//
//		int invokeIntCom(Method method, Object... args) {
//			int offset = 0;
//			Annotation ann = method.getAnnotation(VTID.class);
//			if (ann != null)
//				offset = ((VTID)ann).value();
//			Pointer vptr = _InterfacePtr.getPointer(0);
//			IntByReference retval = new com.sun.jna.ptr.IntByReference();
//			Object[] aarg = prepareArgs(method, args, retval);
//			boolean broke = true;
//			while (broke) {
//				try {
//					if (offset == 200)
//						broke = false;
//					Function func = Function.getFunction(vptr.getPointer(offset * Pointer.SIZE));
//					System.out.println("Offset: " + offset + " -> " + func.getName());
//					offset++;
//					//func.invokeInt(aarg);
//					//broke = false;
//				}
//				catch (Exception e) {
//					offset++;
//				}
//				catch (Error e) {
//					offset++;
//				}
//			}
//			return retval.getValue();
//		}
//
//		@Override
//		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//			if (method.getReturnType() == Integer.TYPE) {
//				return invokeIntCom(method, args);
//			}
//			return null;
//		}
//	}
}
