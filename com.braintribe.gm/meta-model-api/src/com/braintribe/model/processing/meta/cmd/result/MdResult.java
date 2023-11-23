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
package com.braintribe.model.processing.meta.cmd.result;

import java.util.List;

import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.cmd.CmdRuntimeException;
import com.braintribe.model.processing.meta.cmd.extended.MdDescriptor;

/**
 * @author peter.gazdik
 */
public interface MdResult<M extends MetaData, D extends MdDescriptor> {

	/**
	 * Returns a single meta data resolved according to current state of the context. If multiple meta data are active,
	 * one with highest priority is taken. The priority is the same as the one used for ordering the result of
	 * {@link #list()}. This means, in case at least some meta data is active, invoking this method produces the same
	 * result as invoking {@code list().get(0)} (as long as some meta data is active). In case no meta data is active,
	 * the method returns {@code null}.
	 */
	M exclusive() throws CmdRuntimeException;

	/** Returns same meta-data as the {@link #exclusive()} method, only wrapped in the {@link MdDescriptor}. */
	D exclusiveExtended() throws CmdRuntimeException;

	/**
	 * Returns all active {@link MetaData} resolved according to current state of the context sorted according to their
	 * priority. Note that this priority is not just the value of {@link MetaData#getConflictPriority()}, but has
	 * slightly more complex rules.
	 * <p>
	 * In case we are dealing with inheritance (entity or property), the meta data are primarily ordered according to
	 * their owner type distance from the type we are resolving for. For example, if I have A extends B, and B defines a
	 * property P, the P is also a property of A. In that case, when resolving the meta data for A#P, anything specified
	 * directly for entity type A has higher priority than meta data attached to entity type B. In general, the distance
	 * is determined by the order of iteration through all the super-types of A. If we have multiple meta data defined
	 * for the one entity type, then these meta data are sorted with respect to the
	 * {@linkplain MetaData#getConflictPriority()} value.
	 * <p>
	 * Note that there is a flag for inheritable meta-data which makes the order of iteration not being considered - see
	 * {@link MetaData#setImportant(boolean)}
	 * <p>
	 * If no meta data is active, empty list is returned.
	 */
	List<M> list() throws CmdRuntimeException;

	/** Returns same meta-data as the {@link #list()} method, only wrapped in the {@link MdDescriptor}. */
	List<D> listExtended() throws CmdRuntimeException;
	
}
