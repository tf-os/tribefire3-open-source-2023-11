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
package com.braintribe.gwt.logging.client;

import java.util.ArrayList;
import java.util.List;

/** 
 * A simple implementation of {@link LogListener}
 * that is able to hold a defined number of events.
 * If this number would be exceeded old ones become
 * removed.
 * @author Dirk
 *
 */
public class LogEventBuffer implements LogListener {
	
	private List<LogEvent> events = new ArrayList<>();
	private int maxEvents = 200;
	private LogEventBufferListener listener;
	
	public void setMaxEvents(int maxEvents) {
		this.maxEvents = maxEvents;
	}
	
	public void appendLogEvent(LogEvent event) {
		events.add(event);
		//events.addLast(event);
		if (events.size() > maxEvents) events.remove(0);
		//if (events.size() > maxEvents) events.removeFirst();
		
		if (listener != null)
			listener.onLogEventAdded();
	}
	
	public void setListener(LogEventBufferListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void onLogEvent(LogEvent event) {
		appendLogEvent(event);
	}
	
	/**
	 * 
	 * @return The list of events in the order they were received
	 */
	public List<LogEvent> getEvents() {
		return events;
	}
	
	public interface LogEventBufferListener {
		
		public void onLogEventAdded();
		
	}
}
