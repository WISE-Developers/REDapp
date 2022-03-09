/***********************************************************************
 * REDapp - RBaseTextField.java
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.basic.BasicFormattedTextFieldUI;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import ca.redapp.ui.Main;
import ca.redapp.util.REDappLogger;

abstract class RBaseTextField<T extends JTextField> extends JPanel implements MouseListener, DocumentListener, CaretListener {
	private static final long serialVersionUID = 1L;
	
	protected T textField;
	private Border normal = BorderFactory.createLineBorder(new Color(153, 153, 153), 1);
	private Border hover = BorderFactory.createLineBorder(new Color(168, 69, 69), 1);
	private Image image = null;
	private Color background = null;
	private JMenuItem pasteItem;
	private JMenuItem copyItem;
	private JMenuItem cutItem;
	private JMenuItem deleteItem;
	private JMenuItem selectAllItem;
	private JMenuItem redoItem;
	private JMenuItem undoItem;
	private UndoManager undoManager;
	
	public RBaseTextField(T field) {
		textField = field;
		initialize();
	}

	private void initialize() {
		TextUI ui = textField.getUI();
		String cls = ui.getClass().toString();
		if (cls.contains("SynthFormattedTextFieldUI")) {
			textField.setUI(new BasicFormattedTextFieldUI());
		}
		setPreferredSize(textField.getPreferredSize());
		super.setMinimumSize(new Dimension(0, 20));
		super.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		textField.setOpaque(false);
		textField.setBorder(BorderFactory.createEmptyBorder());
		textField.setBounds(4, 0, this.getWidth() - 8, this.getHeight());
		textField.setBackground(new Color(0, 0, 0, 0));
		textField.setHorizontalAlignment(SwingConstants.RIGHT);
		String os = System.getProperty("os.name").toLowerCase();

		if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0)
			textField.setFont(textField.getFont().deriveFont(12.0f));

		undoManager = new UndoManager();
		textField.getDocument().addUndoableEditListener(undoManager);

		JPopupMenu menu = new JPopupMenu();
		undoItem = new JMenuItem(undoManager.getUndoPresentationName());
		//UndoItem.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/UndoHS.png"))));
		undoItem.setEnabled(false);
		undoItem.setAccelerator(KeyStroke.getKeyStroke('Z', KeyEvent.CTRL_DOWN_MASK));
		undoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				undoManager.undo();
			}
		});
		menu.add(undoItem);
		redoItem = new JMenuItem(undoManager.getRedoPresentationName());
		//RedoItem.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/RedoHS.png"))));
		redoItem.setEnabled(false);
		redoItem.setAccelerator(KeyStroke.getKeyStroke('Y', KeyEvent.CTRL_DOWN_MASK));
		redoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				undoManager.redo();
			}
		});
		menu.add(redoItem);
		menu.add(new JPopupMenu.Separator());
		cutItem = new JMenuItem("Cut");
		//CutItem.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/CutHS.png"))));
		cutItem.setEnabled(false);
		cutItem.setAccelerator(KeyStroke.getKeyStroke('X', KeyEvent.CTRL_DOWN_MASK));
		cutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textField.cut();
				textChanged();
			}
		});
		menu.add(cutItem);
		copyItem = new JMenuItem("Copy");
		//CopyItem.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/CopyHS.png"))));
		copyItem.setEnabled(false);
		copyItem.setAccelerator(KeyStroke.getKeyStroke('C', KeyEvent.CTRL_DOWN_MASK));
		copyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textField.copy();
			}
		});
		menu.add(copyItem);
		pasteItem = new JMenuItem("Paste");
		//PasteItem.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/PasteHS.png"))));
		pasteItem.setAccelerator(KeyStroke.getKeyStroke('V', KeyEvent.CTRL_DOWN_MASK));
		pasteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textField.paste();
				textChanged();
			}
		});
		menu.add(pasteItem);
		deleteItem = new JMenuItem("Delete");
		//DeleteItem.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(Import.class.getResource("/images/icons/DeleteHS.png"))));
		deleteItem.setEnabled(false);
		deleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textField.replaceSelection("");
				textChanged();
			}
		});
		menu.add(deleteItem);
		menu.add(new JPopupMenu.Separator());
		selectAllItem = new JMenuItem("Select All");
		selectAllItem.setAccelerator(KeyStroke.getKeyStroke('A', KeyEvent.CTRL_DOWN_MASK));
		selectAllItem.setEnabled(false);
		selectAllItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textField.requestFocus();
				textField.selectAll();
				caretUpdate(null);
			}
		});
		menu.add(selectAllItem);
		textField.setComponentPopupMenu(menu);
		textField.getDocument().addDocumentListener(this);
		textField.addCaretListener(this);

		super.setBackground(new Color(0, 0, 0, 0));
		setLayout(null);
		try {
			image = Toolkit.getDefaultToolkit().getImage(RTextField.class.getResource(Main.resourceManager.getImagePath("ui.field.back")));
		}
		catch (Exception e) {
			REDappLogger.error("Error loading image.", e);
		}
		add(textField);
		setBorder(normal);
		textField.addMouseListener(this);
	}

	public Document getDocument() {
		return textField.getDocument();
	}

	public void setHorizontalAlignment(int alignment) {
		textField.setHorizontalAlignment(alignment);
	}

	public void setCaretPosition(int position) {
		textField.setCaretPosition(position);
	}

	public void addActionListener(java.awt.event.ActionListener listener) {
		textField.addActionListener(listener);
	}
	
	protected abstract int getTextLength();

	private void textChanged() {
		if (getTextLength() > 0)
			selectAllItem.setEnabled(true);
		else
			selectAllItem.setEnabled(false);
		if (textField.isEnabled() && textField.isEditable()) {
			if (undoManager.canUndo())
				undoItem.setEnabled(true);
			else
				undoItem.setEnabled(false);
			if (undoManager.canRedo())
				redoItem.setEnabled(true);
			else
				redoItem.setEnabled(false);
		}
		else {
			redoItem.setEnabled(false);
			undoItem.setEnabled(false);
		}
	}

	@Override
	public void setMinimumSize(Dimension minimumSize) {
		super.setMinimumSize(new Dimension(minimumSize.width, 20));
	}

	@Override
	public void setMaximumSize(Dimension maximumSize) {
		super.setMaximumSize(new Dimension(maximumSize.width, 20));
	}

	@Override
	public void setPreferredSize(Dimension preferredSize) {
		super.setPreferredSize(new Dimension(preferredSize.width, 20));
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		if (textField.getSelectedText() != null && textField.getSelectedText().length() > 0) {
			copyItem.setEnabled(true);
			if (textField.isEditable() && textField.isEnabled()) {
				cutItem.setEnabled(true);
				deleteItem.setEnabled(true);
			}
		}
		else {
			copyItem.setEnabled(false);
			cutItem.setEnabled(false);
			deleteItem.setEnabled(false);
		}
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		textField.setBounds(4, 0, width - 8, height);
	}

	@Override
	public void setBounds(Rectangle rect) {
		setBounds(rect.x, rect.y, rect.width, rect.height);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (background != null) {
			g.setColor(background);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
		else if (!textField.isEnabled() || !textField.isEditable()) {
			g.setColor(new Color(245, 245, 245));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
		else if (image != null) {
			g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		textField.setEnabled(enabled);
		if (!enabled) {
			pasteItem.setEnabled(false);
			cutItem.setEnabled(false);
			undoItem.setEnabled(false);
			redoItem.setEnabled(false);
		}
		else if (textField.isEditable()) {
			pasteItem.setEnabled(true);
			cutItem.setEnabled(true);
			undoItem.setEnabled(true);
			redoItem.setEnabled(true);
		}
		invalidate();
		repaint();
	}

	@Override
	public boolean isEnabled() {
		return textField.isEnabled();
	}

	public void setColumns(int columns) {
		textField.setColumns(columns);
	}

	public void setText(String text) {
		textField.setText(text);
		undoManager.discardAllEdits();
		undoItem.setEnabled(false);
		redoItem.setEnabled(false);
	}

	public String getText() {
		return textField.getText();
	}

	public void selectAll() {
		textField.selectAll();
	}

	public void setEditable(boolean edit) {
		textField.setEditable(edit);
		if (!edit) {
			pasteItem.setEnabled(false);
			cutItem.setEnabled(false);
			undoItem.setEnabled(false);
			redoItem.setEnabled(false);
		}
		else if (textField.isEnabled()) {
			pasteItem.setEnabled(true);
			cutItem.setEnabled(true);
			undoItem.setEnabled(true);
			redoItem.setEnabled(true);
		}
		invalidate();
		repaint();
	}

	public boolean isEditable() {
		return textField.isEditable();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (isEnabled())
			setBorder(hover);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setBorder(normal);
	}

	@Override
	public void setBackground(Color clr) {
		background = clr;
		invalidate();
		repaint();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		textChanged();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		textChanged();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		textChanged();
	}
	
	/**
	 * Add a listener for text change events.
	 * @param listener
	 */
	public void addChangeListener(ChangeListener listener) {
		Objects.requireNonNull(listener);
		DocumentListener dl = new DocumentListener() {
			private int lastChange = 0;
			private int lastNotifiedChange = 0;
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				lastChange++;
				SwingUtilities.invokeLater(() -> {
					if (lastNotifiedChange != lastChange) {
						lastNotifiedChange = lastChange;
						listener.stateChanged(new ChangeEvent(textField));
					}
				});
			}
		};
		textField.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
			Document d1 = (Document)e.getOldValue();
			Document d2 = (Document)e.getNewValue();
			if (d1 != null) d1.removeDocumentListener(dl);
			if (d2 != null) d2.addDocumentListener(dl);
			dl.changedUpdate(null);
		});
		Document d = textField.getDocument();
		if (d != null) d.addDocumentListener(dl);
	}

	@Override
	public void setToolTipText(String text) {
		textField.setToolTipText(text);
	}

	public boolean equalsForTabs(Component c) {
		return c == textField;
	}

	public Component componentForTabs() {
		return textField;
	}
}
