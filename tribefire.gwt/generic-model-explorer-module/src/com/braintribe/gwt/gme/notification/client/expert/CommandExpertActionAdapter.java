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
package com.braintribe.gwt.gme.notification.client.expert;

import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.model.command.Command;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.google.gwt.core.client.Scheduler;

public class CommandExpertActionAdapter<T extends Command> implements CommandExpert<T> {

	public static final String COMMAND_PROPERTY = "command";

	private Supplier<? extends Action> actionProvider;

	@Required
	public void setActionProvider(Supplier<? extends Action> actionProvider) {
		this.actionProvider = actionProvider;
	}

	@Override
	public synchronized void handleCommand(final T command) {
		TriggerInfo triggerInfo = new TriggerInfo();
		triggerInfo.put(COMMAND_PROPERTY, command);
		Scheduler.ScheduledCommand cmd = new ScheduledActionCommand(actionProvider.get(), triggerInfo);
		Scheduler.get().scheduleDeferred(cmd);
	}

	private class ScheduledActionCommand implements Scheduler.ScheduledCommand {

		private final Action action;
		private final TriggerInfo triggerInfo;

		public ScheduledActionCommand(Action action, TriggerInfo triggerInfo) {
			this.action = action;
			this.triggerInfo = triggerInfo;
		}

		@Override
		public void execute() {
			action.perform(triggerInfo);
		}

	}

}
