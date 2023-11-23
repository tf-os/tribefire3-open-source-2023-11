// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.commons.preferences;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.braintribe.logging.Logger;

/**
 * an implementation of a password string editor that displays its values only as
 * masked text.
 * 
 * @author pit 
 *
 */
public class PasswordFieldEditor extends StringFieldEditor {

	private static Logger log = Logger.getLogger(PasswordFieldEditor.class);
		
	private Text text;
	private int validateStrategy = VALIDATE_ON_KEY_STROKE;
	private int textLimit = UNLIMITED;

	public PasswordFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);	
	}

	public PasswordFieldEditor(String name, String labelText, int width, Composite parent) {
		super(name, labelText, width, parent);
		
	}

	public PasswordFieldEditor(String name, String labelText, int width, int strategy, Composite parent) {
		super(name, labelText, width, strategy, parent);
		
	}

	
	@Override
	public Text getTextControl(Composite parent) {
		if (text == null) {
			text = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
			text.setFont(parent.getFont());
			switch (validateStrategy) {
				case VALIDATE_ON_KEY_STROKE:
					text.addKeyListener(new KeyAdapter() {

						/*
						 * (non-Javadoc)
						 * 
						 * @see
						 * org.eclipse.swt.events.KeyAdapter#keyReleased(org
						 * .eclipse.swt.events.KeyEvent)
						 */
						@Override
						public void keyReleased(KeyEvent e) {
							valueChanged();
						}
					});

					break;
				case VALIDATE_ON_FOCUS_LOST:
					text.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							clearErrorMessage();
						}
					});
					text.addFocusListener(new FocusAdapter() {
						@Override
						public void focusGained(FocusEvent e) {
							refreshValidState();
						}

						@Override
						public void focusLost(FocusEvent e) {
							valueChanged();
							clearErrorMessage();
						}
					});
					break;
				default:
					log.warn("Unknown validation strategy [" + validateStrategy + "]");
					break;
			}
			text.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent event) {
					text = null;
				}
			});
			if (textLimit > 0) {// Only set limits above 0 - see SWT spec
				text.setTextLimit(textLimit);
			}
		} else {
			checkParent(text, parent);
		}
		return text;
	}

}
