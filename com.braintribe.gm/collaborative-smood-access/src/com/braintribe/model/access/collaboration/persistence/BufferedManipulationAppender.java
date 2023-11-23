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
package com.braintribe.model.access.collaboration.persistence;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.VoidManipulation;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceAppender;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;

/**
 * @author peter.gazdik
 */
public class BufferedManipulationAppender {

	public static final int LIMIT = 100;

	private final PersistenceAppender delegate;
	private final ManipulationMode mode;

	private final List<Manipulation> buffer = newList();

	public BufferedManipulationAppender(PersistenceAppender delegate, ManipulationMode mode) {
		this.delegate = delegate;
		this.mode = mode;
	}

	/* package */ void append(Manipulation manipulation) {
		buffer.add(manipulation);
		if (buffer.size() >= LIMIT)
			flush(false);
	}

	/* package */ void flush() {
		flush(true);
	}

	private void flush(boolean endTransaction) {
		try {
			tryFlush();
			if (endTransaction)
				delegate.append(VoidManipulation.T.create(), null);

		} catch (ManipulationPersistenceException e) {
			Exceptions.uncheckedAndContextualize(e, "Error while ", GenericModelException::new);
		}
	}

	private void tryFlush() throws ManipulationPersistenceException {
		switch (buffer.size()) {
			case 0:
				return;
			case 1:
				delegate.append(first(buffer), mode);
				return;
			default:
				delegate.append(compound(buffer), mode);
		}

		buffer.clear();
	}

}
