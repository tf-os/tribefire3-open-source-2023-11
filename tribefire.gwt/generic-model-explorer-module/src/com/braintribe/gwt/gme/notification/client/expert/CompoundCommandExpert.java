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
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.command.Command;
import com.braintribe.model.command.CompoundCommand;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.notification.api.CommandExpert;

public class CompoundCommandExpert implements CommandExpert<CompoundCommand> {
	private static final Logger logger = new Logger(CompoundCommandExpert.class);
	
	private Supplier<? extends GmExpertRegistry> gmCommandRegistrySupplier;
	private GmExpertRegistry gmCommandRegistry;
	
	@Required
	public void setCommandRegistry(Supplier<? extends GmExpertRegistry> gmCommandRegistrySupplier) {
		this.gmCommandRegistrySupplier = gmCommandRegistrySupplier;
	}
	
	@Override
	public void handleCommand(CompoundCommand compoundCommand) {
		if (gmCommandRegistry == null)
			gmCommandRegistry = gmCommandRegistrySupplier.get();
		
		for (Command command : compoundCommand.getCommands()) {
			CommandExpert<Command> ce = gmCommandRegistry.findExpert(CommandExpert.class).<CommandExpert<Command>>forInstance(command);
			if (ce != null)
				ce.handleCommand(command);
			else
				logger.info("No expert found for: " + command);
		}
	}

}
