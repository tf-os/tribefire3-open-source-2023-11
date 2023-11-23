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
package com.braintribe.gwt.gme.notification.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.model.command.Command;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.notification.InternalCommand;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.MessageWithCommand;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.notification.NotificationEventSource;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.braintribe.model.processing.notification.api.NotificationListener;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.notifying.GenericManipulationListenerRegistry;

public class NotificationFactoryImpl implements NotificationFactory {

	private ManagedGmSession session;
	private Supplier<? extends NotificationListener> listenerSupplier;
	private NotificationListener listener;

	private final Map<InternalCommand, Object> transientObjects = new HashMap<InternalCommand, Object>();

	private final ManipulationListener transientListener = new ManipulationListener() {
		@Override
		public void noticeManipulation(Manipulation manipulation) {
			if (manipulation instanceof DeleteManipulation)
				transientObjects.remove(((DeleteManipulation) manipulation).getEntity());
		}
	};

	public void setSession(ManagedGmSession session) {
		this.session = session;
	}

	public void setListener(Supplier<? extends NotificationListener> listenerSupplier) {
		this.listenerSupplier = listenerSupplier;
	}

	@Override
	public <NES extends NotificationEventSource> NES createEventSource(EntityType<NES> entityType) {
		NES nes = session.create(entityType);
		nes.setId("#" + nes.hashCode());
		return nes;
	}

	@Override
	public <C extends Command> C createCommand(EntityType<C> entityType) {
		C c = session.create(entityType);
		c.setId((long) c.hashCode());
		return c;
	}

	@Override
	public <N extends Notification> N createNotification(EntityType<N> entityType) {
		N n = session.create(entityType);
		n.setId((long) n.hashCode());
		return n;
	}

	@Override
	public <MN extends MessageNotification> MN createNotification(EntityType<MN> entityType, Level level, String message) {
		MN mn = createNotification(entityType);
		mn.setLevel(level);
		mn.setMessage(message);
		return mn;
	}

	@Override
	public <C extends Command> MessageWithCommand createNotification(EntityType<C> entityType, Level level, String message, String name) {
		C command = session.create(entityType);
		command.setId((long) command.hashCode());
		command.setName(name);
		MessageWithCommand mwc = createNotification(MessageWithCommand.T, level, message);
		mwc.setCommand(command);
		return mwc;
	}

	@Override
	public void broadcast(List<Notification> notifications, NotificationEventSource eventSource) {
		if (listener == null && listenerSupplier != null)
			listener = listenerSupplier.get();
		
		if (listener != null)
			listener.handleNotifications(notifications, eventSource);
	}

	@Override
	public GenericManipulationListenerRegistry listeners() {
		return session.listeners();
	}

	@Override
	public Object getTransientObject(InternalCommand command) {
		return transientObjects.get(command);
	}

	@Override
	public InternalCommand createTransientCommand(String name, Object object) {
		InternalCommand tc = createCommand(InternalCommand.T);
		tc.setName(name);
		if (transientObjects.put(tc, object) == null)
			listeners().entity(tc).add(transientListener);
		return tc;
	}

}
