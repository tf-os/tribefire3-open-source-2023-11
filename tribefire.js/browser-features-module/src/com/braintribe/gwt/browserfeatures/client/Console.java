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
package com.braintribe.gwt.browserfeatures.client;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

public class Console {
	
	private static DateTimeFormat dtf = DateTimeFormat.getFormat("HH:mm:ss:SSSS");
	
	public static final void logWithTime(Object obj){
		_logWithTime(obj, dtf.format(new Date()));
	}
	
	private static native final void _logWithTime(Object obj, String date) /*-{
		console.log(date + " " + obj);
	}-*/;
	
	public static void log(Object obj) {
		_log(obj);
	}
	
	private static native final void _log(Object obj) /*-{
		console.log(obj);
	}-*/;
	
	public static native final void error(Object obj) /*-{
		console.error(obj);
	}-*/;
	
	public static final void time(String process){
		if(GWT.isProdMode())
			_time(process);
	}
	
	public static final void timeEnd(String process){
		if(GWT.isProdMode())
			_timeEnd(process, false);
	}
	
//	public static final void clearAndTimeEnd(String process){
//		if(GWT.isProdMode()) _timeEnd(process, true);
//	};
	
	private static native final void _time(String process) /*-{
		console.time(process);
	}-*/;
	
	private static native final void _timeEnd(String process, boolean clear) /*-{
		if(clear)
			console.clear();
		console.timeEnd(process);
	}-*/;
	
	public static native final void debugger() /*-{
		debugger;
	}-*/;

	public static native final void clearAndLog(Object msg) /*-{
		console.clear();
		console.log(msg);
	}-*/;

}
