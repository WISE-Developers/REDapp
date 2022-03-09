/***********************************************************************
 * REDapp - TristateCheckBox.java
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

package ca.redapp.ui.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;

/**
 * A checkbox with three possible states: checked, unchecked, and intermediate.
 *
 */
public class TristateCheckBox extends JCheckBox {
	private static final long serialVersionUID = 1L;

	/** This is a type-safe enumerated type */
	public static class State { private State() { } }
	public static final State NOT_SELECTED = new State();
	public static final State SELECTED = new State();
	public static final State DONT_CARE = new State();
	
	private final TristateDecorator model;
	
	public TristateCheckBox(String text, Icon icon, State initial){
		super(text, icon);
		// Add a listener for when the mouse is pressed
		super.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				grabFocus();
				model.nextState();
			}
		});
		// Reset the keyboard action map
		ActionMap map = new ActionMapUIResource();
		map.put("pressed", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				grabFocus();
				model.nextState();
			}
		});
		map.put("released", null);
		SwingUtilities.replaceUIActionMap(this, map);
		// set the model to the adapted model
		model = new TristateDecorator(getModel());
		setModel(model);
		setState(initial);
	}
	
	public TristateCheckBox(String text, State initial) {
		this(text, null, initial);
	}

	public TristateCheckBox(String text) {
		this(text, DONT_CARE);
	}

	public TristateCheckBox() {
		this(null);
	}
	
	/** No one may add mouse listeners, not even Swing! */
	@Override
	public void addMouseListener(MouseListener l) { }

	/**
	 * Set the new state to either SELECTED, NOT_SELECTED or
	 * DONT_CARE.  If state == null, it is treated as DONT_CARE.
	 */
	public void setState(State state) { model.setState(state); }
	
	public State getState() { return model.getState(); }
	
	@Override
	public void setSelected(boolean b) {
	    if (b) {
	    	setState(SELECTED);
	    }
	    else {
	    	setState(NOT_SELECTED);
	    }
	}
	
	private class TristateDecorator implements ButtonModel {
	    private final ButtonModel other;

	    private TristateDecorator(ButtonModel other) {
	    	this.other = other;
	    }

	    private void setState(State state) {
	    	if (state == NOT_SELECTED) {
	    		other.setArmed(false);
	    		setPressed(false);
	    		setSelected(false);
	    	}
	    	else if (state == SELECTED) {
	    		other.setArmed(false);
	    		setPressed(false);
	    		setSelected(true);
	    	}
	    	else { // either "null" or DONT_CARE
	    		other.setArmed(true);
	    		setPressed(true);
	    		setSelected(true);
	    	}
	    }
	    
	    private State getState() {
	        if (isSelected() && !isArmed()) {
	        	// normal black tick
	        	return SELECTED;
	        }
	        else if (isSelected() && isArmed()) {
	        	// don't care grey tick
	        	return DONT_CARE;
	        }
	        else {
	        	// normal deselected
	        	return NOT_SELECTED;
	        }
	    }
	    
	    private void nextState() {
	        State current = getState();
	        if (current == NOT_SELECTED) {
	        	setState(SELECTED);
	        }
	        else if (current == SELECTED) {
	        	setState(NOT_SELECTED);
	        }
	        else if (current == DONT_CARE) {
	        	setState(SELECTED);
	        }
	    }
	    
	    @Override
	    public void setArmed(boolean b) { }

	    @Override
	    public void setEnabled(boolean b) {
	        setFocusable(b);
	        other.setEnabled(b);
	    }

	    @Override
	    public boolean isArmed() { return other.isArmed(); }

	    @Override
	    public boolean isSelected() { return other.isSelected(); }

	    @Override
	    public boolean isEnabled() { return other.isEnabled(); }

	    @Override
	    public boolean isPressed() { return other.isPressed(); }

	    @Override
	    public boolean isRollover() { return other.isRollover(); }

	    @Override
	    public void setSelected(boolean b) { other.setSelected(b); }

	    @Override
	    public void setPressed(boolean b) { other.setPressed(b); }

	    @Override
	    public void setRollover(boolean b) { other.setRollover(b); }

	    @Override
	    public void setMnemonic(int key) { other.setMnemonic(key); }

	    @Override
	    public int getMnemonic() { return other.getMnemonic(); }

	    @Override
	    public void setActionCommand(String s) {
	    	other.setActionCommand(s);
	    }

	    @Override
	    public String getActionCommand() {
	    	return other.getActionCommand();
	    }

	    @Override
	    public void setGroup(ButtonGroup group) {
	    	other.setGroup(group);
	    }

	    @Override
	    public void addActionListener(ActionListener l) {
	    	other.addActionListener(l);
	    }

	    @Override
	    public void removeActionListener(ActionListener l) {
	    	other.removeActionListener(l);
	    }

	    @Override
	    public void addItemListener(ItemListener l) {
	    	other.addItemListener(l);
	    }

	    @Override
	    public void removeItemListener(ItemListener l) {
	    	other.removeItemListener(l);
	    }

	    @Override
	    public void addChangeListener(ChangeListener l) {
	    	other.addChangeListener(l);
	    }

	    @Override
	    public void removeChangeListener(ChangeListener l) {
	    	other.removeChangeListener(l);
	    }

	    @Override
	    public Object[] getSelectedObjects() {
	    	return other.getSelectedObjects();
	    }
	}
}
