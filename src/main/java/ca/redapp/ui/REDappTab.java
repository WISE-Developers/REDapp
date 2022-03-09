/***********************************************************************
 * REDapp - REDappTab.java
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

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;

import javax.swing.JPanel;

import ca.redapp.ui.component.RTextField;

public abstract class REDappTab extends JPanel {
	private static final long serialVersionUID = 1L;
	protected ArrayList<Component> tabOrder = new ArrayList<Component>();

	protected abstract void initialize();

	/**
	 * Set whether or not an Internet connection is present.
	 * @param conn
	 */
	public abstract void setInternetConnected(boolean conn);
	
	/**
	 * Called when the settings have been changed.
	 */
	public abstract void settingsUpdated();

	/**
	 * Reset the values on the form.
	 */
	public abstract void reset();

	/**
	 * Does the tab support the reset command.
	 */
	public abstract boolean supportsReset();

	/**
	 * Called when the latitude or longitude has been changed.
	 */
	public abstract void onLocationChanged();

	/**
	 * Called when the timezone has been changed.
	 */
	public abstract void onTimeZoneChanged();

	/**
	 * Called when the date has been changed.
	 */
	public abstract void onDateChanged();

	/**
	 * Called when the current tab is changed.
	 */
	public abstract void onCurrentTabChanged();
	
	/**
	 * Called when REDapp is closing.
	 */
	public void onClosing() { }

	/**
	 * Returns the last Component in the traversal cycle. This method is used to determine the next Component to focus when traversal wraps in the reverse direction.
	 * @param aContainer the focus cycle root or focus traversal policy provider whose last Component is to be returned
	 * @return the last Component in the traversal cycle of aContainer, or null if no suitable Component can be found
	 */
	public Component getLastComponent(Container aContainer) {
		if (tabOrder.size() == 0)
			return null;
		return tabOrder.get(tabOrder.size() - 1);
	}

	/**
	 * Returns the first Component in the traversal cycle. This method is used to determine the next Component to focus when traversal wraps in the forward direction.
	 * @param aContainer the focus cycle root or focus traversal policy provider whose first Component is to be returned
	 * @return the first Component in the traversal cycle of aContainer, or null if no suitable Component can be found
	 */
	public Component getFirstComponent(Container aContainer) {
		if (tabOrder.size() == 0)
			return null;
		return tabOrder.get(0);
	}

	/**
	 * Returns the default Component to focus. This Component will be the first to receive focus when traversing down into a new focus traversal cycle rooted at aContainer.
	 * @param aContainer the focus cycle root or focus traversal policy provider whose default Component is to be returned
	 * @return the default Component in the traversal cycle of aContainer, or null if no suitable Component can be found
	 */
	public Component getDefaultComponent(Container aContainer) {
		if (tabOrder.size() == 0)
			return null;
		return tabOrder.get(0);
	}

	/**
	 * Returns the Component that should receive the focus before aComponent.
	 * aContainer must be a focus cycle root of aComponent or a focus traversal policy provider.
	 * @param aContainer a focus cycle root of aComponent or focus traversal policy provider
	 * @param aComponent a (possibly indirect) child of aContainer, or aContainer itself
	 * @return the Component that should receive the focus before aComponent, or null if no suitable Component can be found
	 */
	public Component getComponentBefore(Container aContainer, Component aComponent) {
		Component retval = null;
		for (int i = 0; i < tabOrder.size(); i++) {
			boolean equals;
			if (tabOrder.get(i) instanceof RTextField)
				equals = ((RTextField)tabOrder.get(i)).equalsForTabs(aComponent);
			else
				equals = tabOrder.get(i) == aComponent;
			if (equals) {
				int j;
				if (i == 0)
					j = tabOrder.size() - 1;
				else
					j = i - 1;
				retval = tabOrder.get(j);
				while (retval != tabOrder.get(i)) {
					if (retval.isEnabled())
						break;
					j--;
					if (j < 0)
						j = tabOrder.size() - 1;
					retval = tabOrder.get(j);
				}
				break;
			}
		}
		if (retval instanceof RTextField)
			return ((RTextField) retval).componentForTabs();
		return retval;
	}

	/**
	 * Returns the Component that should receive the focus after aComponent.
	 * aContainer must be a focus cycle root of aComponent or a focus traversal policy provider.
	 * @param aContainer a focus cycle root of aComponent or focus traversal policy provider
	 * @param aComponent a (possibly indirect) child of aContainer, or aContainer itself
	 * @return the Component that should receive the focus after aComponent, or null if no suitable Component can be found
	 */
	public Component getComponentAfter(Container aContainer, Component aComponent) {
		Component retval = null;
		for (int i = 0; i < tabOrder.size(); i++) {
			boolean equals;
			if (tabOrder.get(i) instanceof RTextField)
				equals = ((RTextField)tabOrder.get(i)).equalsForTabs(aComponent);
			else
				equals = tabOrder.get(i) == aComponent;
			if (equals) {
				int j;
				if (i == tabOrder.size() - 1)
					j = 0;
				else
					j = i + 1;
				retval = tabOrder.get(j);
				while (retval != tabOrder.get(i)) {
					if (retval.isEnabled())
						break;
					j++;
					if (j == tabOrder.size())
						j = 0;
					retval = tabOrder.get(j);
				}
				break;
			}
		}
		if (retval instanceof RTextField)
			return ((RTextField) retval).componentForTabs();
		return retval;
	}
}
