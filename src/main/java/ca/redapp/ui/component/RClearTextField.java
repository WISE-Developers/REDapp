/***********************************************************************
 * REDapp - RClearTextField.java
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import ca.redapp.ui.component.RTextField.RTextFieldListener;

/**
 * A text field that can be cleared back to a default value.
 * 
 * @author Travis Redpath
 */
public class RClearTextField extends JPanel implements RTextFieldListener {
	private static final long serialVersionUID = 1L;
	private JButton m_ClearButton;
	private String defaultText = "";
	private RTextField m_TextField;

	public RClearTextField() {
		super();

		m_TextField = new RTextField();
		m_TextField.addRTextFieldListener(this);
		m_ClearButton = new JButton();
		m_ClearButton.setIcon(new ImageIcon(RClearTextField.class.getResource("/images/icons/clear.png")));
		m_ClearButton.setRolloverEnabled(true);
		m_ClearButton.setRolloverIcon(new ImageIcon(RClearTextField.class.getResource("/images/icons/clear_hover.png")));
		m_ClearButton.setVisible(true);
		m_ClearButton.setFocusPainted(false);
		m_ClearButton.setContentAreaFilled(false);
		m_ClearButton.setBorderPainted(false);
		setLayout(null);
		add(m_ClearButton);
		add(m_TextField);
		setOpaque(false);
		m_ClearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_ClearButton.setVisible(false);
				m_TextField.setText(defaultText);
				m_TextField.requestFocus();
			}
		});
		addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				resetSizes();
			}

			@Override
			public void componentShown(ComponentEvent e) { }
			@Override
			public void componentMoved(ComponentEvent e) { }
			@Override
			public void componentHidden(ComponentEvent e) { }
		});
		m_TextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				resetSizes();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				resetSizes();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				resetSizes();
			}
		});
		setMinimumSize(new Dimension(40, 20));
		resetSizes();
	}

	public Document getDocument() {
		return m_TextField.getDocument();
	}

	public RTextField getTextField() {
		return m_TextField;
	}

	public String getText() {
		return m_TextField.getText();
	}

	public void setText(String text) {
		m_TextField.setText(text);
		resetSizes();
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		resetSizes();
	}

	private void resetSizes() {
		if (m_TextField.getText().compareTo(defaultText) != 0)
			m_ClearButton.setVisible(true);
		else
			m_ClearButton.setVisible(false);
		m_ClearButton.setBounds(getWidth() - 18, (int)(((double)(getHeight() - 16)) / 2.0), 16, 16);
		if (m_ClearButton.isVisible())
			m_TextField.setBounds(0, 0, getWidth() - 20, getHeight());
		else
			m_TextField.setBounds(0, 0, getWidth(), getHeight());
	}

	public final void setDefaultText(String text) {
		defaultText = text;
		resetSizes();
	}

	public final String getDefaultText() {
		return defaultText;
	}

	public final void resetDefaultText() {
		defaultText = "";
	}

	@Override
	public void redrawn() {
		m_ClearButton.repaint();
	}

	public void addActionListener(ActionListener listener) {
		m_TextField.addActionListener(listener);
	}

	public boolean equalsForTabs(Component c) {
		return m_TextField.equalsForTabs(c);
	}

	public Component componentForTabs() {
		return m_TextField.componentForTabs();
	}
}
