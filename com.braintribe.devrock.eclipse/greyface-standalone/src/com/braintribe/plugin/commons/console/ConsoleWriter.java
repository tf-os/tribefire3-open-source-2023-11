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
/*
 * Created on 2006-mar-06
 *
 * 
 */
package com.braintribe.plugin.commons.console;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Create an instance of this class in any of your plugin classes.
 *
 * Use it as follows ...
 *
 * ConsoleDisplayMgr.getDefault().println("Some error msg", ConsoleDisplayMgr.MSG_ERROR);
 * ...
 * ...
 * ConsoleDisplayMgr.getDefault().clear();
 * ...
 * 
 * @author pit
 */
public class ConsoleWriter
{
	private static ConsoleWriter fDefault = null;
	private String fTitle = null;
	private MessageConsole fMessageConsole = null;

	public static final int MSG_INFORMATION = 1;
	public static final int MSG_ERROR = 2;
	public static final int MSG_WARNING = 3;

	Color INFO = null;
	Color WARNING = null;
	Color ERROR = null;
	
	public static final int VERBOSE = 0;
	public static final int TACITURN = 1;
	public static final int QUIET = 2;
	
	private int mode = QUIET;
		
	public static ConsoleWriter consoleWriter = null;

	public ConsoleWriter(String messageTitle, int mode)
	{
		// Set the colors for the Console.
		// MSG_INFO color.
		INFO = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);

		// MSG_WARN color.
		WARNING = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA);

		// MSG_WARNING color
		ERROR = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

		fDefault = this;
		fTitle = messageTitle;
		
		this.mode = mode;
		consoleWriter = this;
	}

	public static ConsoleWriter getDefault() {
		return fDefault;
	}

	public static void write( String msg, int msgKind) {
		if (consoleWriter != null)
			consoleWriter.println(msg, msgKind);
	}
	
	public void println(String msg, int msgKind) {
		if( msg == null ) return;
		
		boolean inhibited = false;
		switch (mode) {
			case QUIET: 
				inhibited = true;
				break;
			case TACITURN:
				if ((msgKind != MSG_ERROR) && (msgKind != MSG_WARNING))
					inhibited = true;
				break;
			case VERBOSE:
				inhibited = false;
				break;						
		}

		/* if console-view in Java-perspective is not active, then show it and
		 * then display the message in the console attached to it */
		if( !displayConsoleView() )
		{
			/*If an exception occurs while displaying in the console, then just diplay atleast the same in a message-box */
			if (inhibited == false)
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error", msg);
			return;
		}

		/* display message on console */
		
		if (inhibited == false)
			getNewMessageConsoleStream(msgKind).println(msg);
	}

	public void clear(){
		IDocument document = getMessageConsole().getDocument();
		if (document != null) {
			document.set("");
		}
	}

	private boolean displayConsoleView() {
		try
		{
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if( activeWorkbenchWindow != null )
			{
				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
				if( activePage != null )
					activePage.showView(IConsoleConstants.ID_CONSOLE_VIEW, null, IWorkbenchPage.VIEW_VISIBLE);
			}

		} catch (PartInitException partEx) {
			return false;
		}

		return true;
	}

	private MessageConsoleStream getNewMessageConsoleStream(int msgKind) {
		Color swtColor = null;
				
		switch (msgKind) {
		case MSG_INFORMATION:
			swtColor = INFO;
		
			break;
		case MSG_ERROR:
			swtColor = ERROR;
		
			break;
		case MSG_WARNING:
			swtColor = WARNING;
					
			break;
		default: 
			swtColor = INFO;					
		}
		MessageConsoleStream msgConsoleStream = getMessageConsole().newMessageStream();
		try {
			msgConsoleStream.setColor(swtColor);
		}
		catch(SWTException e)
		{
			e.printStackTrace();
		}
		return msgConsoleStream;
	}

	private MessageConsole getMessageConsole() {
		if( fMessageConsole == null )
			createMessageConsoleStream(fTitle);

		return fMessageConsole;
	}

	private void createMessageConsoleStream(String title) {
		fMessageConsole = new MessageConsole(title, null);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ fMessageConsole });
	}
	
	public static ConsoleWriter createConsoleWriter(String consoleTitle, int consoleMode){
		/*
		IPreferenceStore store = PreferencesActivator.getDefault().getPreferenceStore();
		String consoleModePreference = store.getString( PreferenceConstants.CONSOLE_OUTPUT);
				
		if (consoleModePreference.equals( PreferenceConstants.CONSOLE_QUIET))
			consoleMode = ConsoleWriter.QUIET;
		else
			if (consoleModePreference.equals( PreferenceConstants.CONSOLE_TACITURN))
				consoleMode = ConsoleWriter.TACITURN;
			else
				if (consoleModePreference.equals( PreferenceConstants.CONSOLE_VERBOSE))
					consoleMode = ConsoleWriter.VERBOSE;
			
			*/
		return consoleWriter = new ConsoleWriter( consoleTitle, consoleMode);		
	}
	
	public static ConsoleWriter createConsoleWriter(String consoleTitle) {
		return createConsoleWriter(consoleTitle, ConsoleWriter.TACITURN);		
	}
}
