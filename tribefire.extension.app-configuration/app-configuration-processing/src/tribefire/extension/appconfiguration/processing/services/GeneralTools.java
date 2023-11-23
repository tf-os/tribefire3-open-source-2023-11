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
package tribefire.extension.appconfiguration.processing.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.NullSafe;

/**
 * Provides general helpers and convenience methods which could also moved to a more general place.
 */
public class GeneralTools {

	private GeneralTools() {
		// no need to instantiate
	}

	/**
	 * Returns a list containing the passed <code>elements</code>. In contrast to {@link List#of(Object...)} the returned list is modifiable.
	 */
	public static List<String> list(String... elements) {
		return Arrays.asList(elements);
	}

	/**
	 * Finds duplicates in the passed <code>list</code>.
	 */
	public static <E> List<E> duplicates(List<E> list) {
		Set<E> set = new HashSet<>();
		List<E> duplicates = new ArrayList<>();

		for (E element : list) {
			if (!set.add(element)) {
				duplicates.add(element);
			}
		}
		return duplicates;
	}

	/**
	 * Inserts the <code>element</code> into the <code>list</code> at the right position based on the order defined by the specified
	 * <code>comparator</code>.
	 */
	public static <T> void insertOrdered(T element, List<T> list, Comparator<T> comparator) {
		list.add(
				list.isEmpty() ? 0
						: IntStream.range(0, list.size()).filter(i -> comparator.compare(element, list.get(i)) < 0).findFirst().orElse(list.size()),
				element);
	}

	/**
	 * Returns the distinct, sorted union of the two passed collections.
	 */
	public static <E extends Comparable<? super E>> List<E> distinctSortedUnion(Collection<E> collection1, Collection<E> collection2) {
		List<E> result = new ArrayList<>();
		result.addAll(collection1);
		result.addAll(collection2);
		return result.stream().distinct().sorted().collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Returns the <code>count</code> followed by a space and the <code>name</code> with or without plural <code>s</code>, dependent on the
	 * <code>count</code>. Example: "1 thing", "2 things"
	 */
	public static String countAndName(int count, String name) {
		return CommonTools.getCountAndSingularOrPlural(count, name);
	}

	/**
	 * Delegates to {@link #countAndName(int, String)} passing the <code>collection</code> size.
	 */
	public static String countAndName(Collection<?> collection, String name) {
		return countAndName(collection.size(), name);
	}

	/**
	 * Returns a reason with the <code>mainMessage</code> as text and a sub reason for each detail message.
	 */
	public static Reason reason(String mainMessage, List<String> detailMessages) {
		Reason reason = Reason.T.create();
		reason.setText(mainMessage);
		for (String detailMessage : NullSafe.iterable(detailMessages)) {
			Reason subReason = Reason.T.create();
			subReason.setText(detailMessage);
			reason.getReasons().add(subReason);
		}
		return reason;
	}

	/**
	 * Simple interface used to create {@link GenericEntity} instances. This class is used in methods which do not need a session, but only a way to
	 * create entities. Even if the main code uses a session, respective unit tests don't have to.
	 */
	@FunctionalInterface
	interface EntityCreator {
		/**
		 * Creates a new instance of the specified <code>type</code>.
		 */
		<T extends GenericEntity> T create(EntityType<T> type);
	}

	/**
	 * Session based {@link EntityCreator} implementation.
	 */
	public static class SessionEntityCreator implements EntityCreator {
		private final GmSession session;

		/**
		 * Creates a new instance passing the <code>session</code> to be used to creates entity instances.
		 */
		public SessionEntityCreator(GmSession session) {
			this.session = session;
		}

		/**
		 * Creates a new instance of the specified <code>type</code> using {@link GmSession#create(EntityType)}.
		 */
		@Override
		public <T extends GenericEntity> T create(EntityType<T> type) {
			return session.create(type);
		}
	}

	/**
	 * Sessionless {@link EntityCreator} implementation.
	 */
	public static class SessionlessEntityCreator implements EntityCreator {

		/**
		 * Creates a new instance of the specified <code>type</code> using {@link EntityType#create()}.
		 */
		@Override
		public <T extends GenericEntity> T create(EntityType<T> type) {
			return type.create();
		}
	}
}
