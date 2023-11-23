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
package com.braintribe.model.processing.manipulation.basic.tools;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;

/** @deprecated use GMML stringifiers, either explicitly or via {@link Manipulation#stringify()}. */
@Deprecated
public class ManipulationStringifier {

	// ########################################
	// ## . . . . . . Public API . . . . . . ##
	// ########################################

	/**
	 * Creates a string representation of the given Manipulation. This method currently does not properly support local
	 * references.<br />
	 * So the passed Manipulation should already be remotified. @see {@link ManipulationRemotifier} <br />
	 * This method returns and empty string in case the passed Manipulation is null or an empty
	 * {@link CompoundManipulation} is passed.
	 * 
	 * @param manipulation
	 *            The manipulation to be stringified
	 * @return The string representation of the passed manipulation
	 */
	public static String stringify(Manipulation manipulation) {
		return stringify(manipulation, true);
	}

	public static String stringify(Manipulation manipulation, boolean transaction) {
		return with(manipulation).transaction(transaction).stringify();
	}

	public static ManipulationStringifierBuilder with(Manipulation manipulation) {
		return new BuilderImpl(manipulation);
	}

	// ########################################
	// ## . . . . . Implementation . . . . . ##
	// ########################################

	private final StringBuilder builder;
	private final boolean withFullSignatures;

	ManipulationStringifier(BuilderImpl builder) {
		this.withFullSignatures = builder.fullSignatures;
		this.builder = new StringBuilder();
	}

	String stringifyImpl(Manipulation manipulation, boolean transaction) {
		stringifyManipulation(manipulation, transaction);
		return builder.toString();
	}

	void stringifyManipulation(Manipulation manipulation, boolean transaction) {
		if (manipulation == null) {
			return;
		}

		switch (manipulation.manipulationType()) {
			case COMPOUND:
				stringify((CompoundManipulation) manipulation, transaction);
				break;
			case CHANGE_VALUE:
				stringify((ChangeValueManipulation) manipulation);
				stringifyLineBreak();
				break;
			case CLEAR_COLLECTION:
				stringify((ClearCollectionManipulation) manipulation);
				stringifyLineBreak();
				break;
			case DELETE:
				stringify((DeleteManipulation) manipulation);
				stringifyLineBreak();
				break;
			case INSTANTIATION:
				stringify((InstantiationManipulation) manipulation);
				stringifyLineBreak();
				break;
			case ADD:
				stringify((AddManipulation) manipulation);
				stringifyLineBreak();
				break;
			case REMOVE:
				stringify((RemoveManipulation) manipulation);
				stringifyLineBreak();
				break;
			default:
				break;

		}
	}

	private StringBuilder stringifyLineBreak() {
		return builder.append('\n');
	}

	private void stringify(RemoveManipulation rm) {
		// Person(1).children - {
		// 1: ~Person(1314234)
		// 2: Person(2)
		// }
		stringify(rm.getOwner());
		builder.append(" - ");
		stringify(rm.getItemsToRemove());
	}

	private void stringify(AddManipulation am) {
		// Person(1).children + {
		// 1: ~Person(1314234)
		// 2: Person(2)
		// }
		stringify(am.getOwner());
		builder.append(" + ");
		stringify(am.getItemsToAdd());
	}

	private void stringify(InstantiationManipulation im) {
		// new ~Person(1)
		builder.append("new ");
		stringify(im.getEntity());
	}

	private void stringify(DeleteManipulation dm) {
		// delete Person(1)
		builder.append("delete ");
		stringify(dm.getEntity());
	}

	private void stringify(ClearCollectionManipulation ccm) {
		// clear Person(1).children
		builder.append("clear ");
		stringify(ccm.getOwner());
	}

	private void stringify(ChangeValueManipulation cvm) {
		// Person(1).name = john.doe
		Owner owner = cvm.getOwner();
		stringify(owner);
		builder.append(" = ");
		stringify(cvm.getNewValue());
	}

	private void stringify(CompoundManipulation compoundManipulation, boolean transaction) {
		for (Manipulation nestedManipulation : compoundManipulation.getCompoundManipulationList()) {
			if (transaction)
				builder.append("{\n");
			stringifyImpl(nestedManipulation, false);
			if (transaction)
				builder.append("}\n");
		}
	}

	private void stringify(Object value) {
		if (value instanceof EntityReference) {
			stringify((EntityReference) value);

		} else if (value instanceof EntityProperty) {
			stringify((EntityProperty) value);

		} else if (value instanceof Set<?>) {
			stringify((Set<?>) value);

		} else if (value instanceof List<?>) {
			stringify((List<?>) value);

		} else if (value instanceof Map<?, ?>) {
			stringify((Map<?, ?>) value);

		} else {
			builder.append(value);
		}
	}

	private void stringify(EntityReference reference) {
		// persistent: Person(1)
		// preliminary: ~Person(11234234)
		if (reference instanceof PreliminaryEntityReference)
			builder.append('~');

		String signature = reference.getTypeSignature();
		if (!withFullSignatures)
			signature = signature.substring(signature.lastIndexOf('.') + 1);

		builder.append(signature);
		builder.append('(');
		builder.append(reference.getRefId());
		builder.append(')');
	}

	private void stringify(EntityProperty property) {
		// Person(1).name
		stringify(property.getReference());
		builder.append('.');
		builder.append(property.getPropertyName());
	}

	private void stringify(Map<?, ?> map) {
		// 1: ~Person(1314234)
		// 2: Person(2)
		builder.append('{');
		if (map.size() > 1) {
			stringifyLineBreak();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				builder.append("  ");
				stringify(entry.getKey());
				builder.append(": ");
				stringify(entry.getValue());
				builder.append(',');
				stringifyLineBreak();
			}
			stringifyLineBreak();
		} else {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				stringify(entry.getKey());
				builder.append(": ");
				stringify(entry.getValue());
				builder.append(',');
			}
		}
		builder.append('}');

	}

	private void stringify(Set<?> set) {
		// (a,b,c)
		builder.append('(');
		if (set.size() > 3) {
			stringifyLineBreak();
			for (Object e : set) {
				builder.append("  ");
				stringify(e);
				builder.append(',');
				stringifyLineBreak();
			}
			stringifyLineBreak();
		} else {
			for (Object e : set) {
				stringify(e);
				builder.append(',');
			}
		}
		builder.append(')');

	}

	private void stringify(List<?> list) {
		// [1,2,3]
		builder.append('[');
		if (list.size() > 3) {
			stringifyLineBreak();
			for (Object e : list) {
				builder.append("  ");
				stringify(e);
				builder.append(',');
				stringifyLineBreak();
			}
			stringifyLineBreak();
		} else {
			for (Object e : list) {
				stringify(e);
				builder.append(',');
			}
		}
		builder.append(']');
	}

	private static class BuilderImpl implements ManipulationStringifierBuilder {
		public final Manipulation manipulation;
		public boolean transaction;
		public boolean fullSignatures;

		public BuilderImpl(Manipulation manipulation) {
			this.manipulation = manipulation;
		}

		@Override
		public ManipulationStringifierBuilder transaction(boolean transaction) {
			this.transaction = transaction;
			return this;
		}

		@Override
		public ManipulationStringifierBuilder fullSignatures(boolean fullSignatures) {
			this.fullSignatures = fullSignatures;
			return this;
		}

		@Override
		public String stringify() {
			return new ManipulationStringifier(this).stringifyImpl(manipulation, transaction);
		}
	}

}
