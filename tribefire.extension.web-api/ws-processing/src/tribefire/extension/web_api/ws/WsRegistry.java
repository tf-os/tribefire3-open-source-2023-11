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
package tribefire.extension.web_api.ws;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.websocket.Session;


/**
 * A class used by the {@link WsServer} that helps maintaining (registering, removing, finding) 
 * open websocket session along with according Client informations.
 */
public class WsRegistry {

	private Map<Session, WsRegistrationEntry> registry = new ConcurrentHashMap<>();
			
	/**
	 * Registers an entry for the given websocket session along with passed clientInfo.
	 */
	public void register (WsClientInfo info, Session session) {
		registry.put(session, new WsRegistrationEntry(info, session));
	}
	
	/**
	 * Removes the registered entry registered for passed websocket session.
	 * @return the removed entry or null if no entry found for passed session.
	 */
	public WsRegistrationEntry remove(Session session) {
		WsRegistrationEntry e = findEntry(session);
		if (e == null) {
			return null;
		}
		registry.remove(e.getSession());
		return e;
	}
	/**
	 * Removes all registered entries matching the given Predicate.
	 * @return the removed entries.
	 */
	public Set<WsRegistrationEntry> remove(Predicate<WsRegistrationEntry> predicate) {
		Set<WsRegistrationEntry> entries = findEntries(predicate);
		for (WsRegistrationEntry e : entries) {
			remove(e.getSession());
		}
		return entries;
	}

	/**
	 * Returns the entry registered for the given websocket session. 
	 * If no entry is registered this method returns null.
	 */
	public WsRegistrationEntry findEntry(Session session) {
		return registry.get(session);
	}

	/**
	 * Returns a collection of registered entries matching the given Predicate. 
	 */
	public Set<WsRegistrationEntry> findEntries(Predicate<WsRegistrationEntry> predicate) {
		return registry
				.values()
				.stream()
				.filter((e) -> predicate.test(e))
				.collect(Collectors.toSet());
	}

	/**
	 * The entry object holding all necessary informations associated to a websocket session.
	 */
	public class WsRegistrationEntry {
		
		private WsClientInfo clientInfo;
		private Session session;
		
		public WsRegistrationEntry(WsClientInfo clientInfo, Session session) {
			this.clientInfo = clientInfo;
			this.session = session;		
		}
		
		public WsClientInfo getClientInfo() {
			return clientInfo;
		}
		
		public Session getSession() {
			return session;
		}
		
	}
	
}
