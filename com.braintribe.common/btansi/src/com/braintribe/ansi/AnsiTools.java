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
package com.braintribe.ansi;

import org.fusesource.jansi.internal.CLibrary;
import org.fusesource.jansi.internal.Kernel32;

import com.braintribe.logging.Logger;
import com.braintribe.utils.OsTools;

/**
 * @author peter.gazdik
 */
public class AnsiTools {

	public static final boolean IS_WINDOWS = OsTools.isWindowsOperatingSystem();

	private static final Logger log = Logger.getLogger(AnsiTools.class);
	
	// See org.fusesource.jansi.AnsiConsole
	public static final boolean IS_CYGWIN = IS_WINDOWS //
			&& startsWith(System.getenv("PWD"), "/") //
			&& !"cygwin".equals(System.getenv("TERM"));

	/* See org.fusesource.jansi.AnsiConsole - this was modified, AnsiConsole checks for equality with xterm, git bash says xterm-256color */
	public static final boolean IS_MINGW_XTERM = IS_WINDOWS //
			&& startsWith(System.getenv("MSYSTEM"), "MINGW") //
			&& startsWith(System.getenv("TERM"), "xterm");

	private static boolean startsWith(String nullableString, String prefix) {
		return nullableString != null && nullableString.startsWith(prefix);
	}

	private static Boolean colorStdout;
	private static Boolean colorStderr;

	public static boolean isAnsiStdout() {
		try {
			return tryIsAnsiStdout();
		} catch (Throwable t) {
			log.warn("Error while detecting ANSI support for std out.", t);
			return colorStdout = false;
		}
	}

	private static boolean tryIsAnsiStdout() {
		if (colorStdout != null)
			return colorStdout;

		// See org.fusesource.jansi.AnsiConsole
		if (IS_WINDOWS && !IS_CYGWIN && !IS_MINGW_XTERM)
			return colorStdout = isAnsiStdout_Win(Kernel32.STD_OUTPUT_HANDLE);
		else
			return colorStdout = CLibrary.isatty(CLibrary.STDOUT_FILENO) == 1;
	}

	public static boolean isAnsiStderr() {
		try {
			return tryIsAnsiStderr();
		} catch (Throwable t) {
			log.warn("Error while detecting ANSI support for std err.", t);
			return colorStderr = false;
		}
	}

	private static boolean tryIsAnsiStderr() {
		if (colorStderr != null)
			return colorStderr;

		// See org.fusesource.jansi.AnsiConsole
		if (IS_WINDOWS && !IS_CYGWIN && !IS_MINGW_XTERM)
			return colorStderr = isAnsiStdout_Win(Kernel32.STD_ERROR_HANDLE);
		else
			return colorStderr = CLibrary.isatty(CLibrary.STDERR_FILENO) == 1;
	}

	private static boolean isAnsiStdout_Win(int handleCode) {
		long hStdout = Kernel32.GetStdHandle(handleCode);
		// ENABLE_VIRTUAL_TERMINAL_PROCESSING
		return enableVirtualTerminal(hStdout);
	}

	private static boolean enableVirtualTerminal(long handle) {
		int mode[] = new int[1];

		return Kernel32.GetConsoleMode(handle, mode) != 0 && //
				Kernel32.SetConsoleMode(handle, mode[0] | 0x0004) != 0;
	}

}
