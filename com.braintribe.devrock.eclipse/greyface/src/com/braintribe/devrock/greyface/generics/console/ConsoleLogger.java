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
package com.braintribe.devrock.greyface.generics.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


public class ConsoleLogger {
	 // the message console stream used to output to the console view
	private MessageConsoleStream mConsoleStream = null;
	
	 public ConsoleLogger() {
		 super(); 
		 MessageConsole myConsole = findConsole("Ant");
		 mConsoleStream = myConsole.newMessageStream(); 
	}
	 
	public void log(String message) {
		 mConsoleStream.println(message);
	}

	
	//
	 public static MessageConsole findConsole(String name) {
		 IConsoleManager conMan = ConsolePlugin.getDefault().getConsoleManager();
		// scan for console 
		 IConsole[] existing = conMan.getConsoles();
		 for (int i = 0; i < existing.length; i++)
			 if (name.equals(existing[i].getName()))
				 return (MessageConsole) existing[i];
		 
		 //no console found, so create a new one
		 MessageConsole myConsole = new MessageConsole( name, null);
		 conMan.addConsoles( new IConsole[]{ myConsole});
		 return myConsole;
	 }
}
 	
