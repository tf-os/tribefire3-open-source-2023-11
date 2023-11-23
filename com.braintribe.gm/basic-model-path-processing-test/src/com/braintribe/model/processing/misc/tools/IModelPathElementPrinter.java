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
package com.braintribe.model.processing.misc.tools;

import com.braintribe.model.generic.path.api.IModelPathElement;

/**
 * {@link IModelPathElement} console printer
 *
 */
public class IModelPathElementPrinter {

	public static void print(IModelPathElement element) {
		IModelPathElementPrinter.printHelper(element);
	}

	private static void printHelper(IModelPathElement element) {

		IModelPathElement currentElement = element;
		while (currentElement != null) {

			printElement(currentElement);
			currentElement = currentElement.getPrevious();

		}

	}

	private static void printElement(IModelPathElement element) {

		// TODO adjust formatting

		println("Element" + "(" + number(element) + ") [");

		println("\t Element Type: " + element.getElementType().name());

		println("\t Type: " + element.getType());

		println("\t Value: " + element.getValue());

		println("\t Depth: " + element.getDepth());

		println("]");
	}

	private static int number(IModelPathElement element) {
		return System.identityHashCode(element);
	}

	private static void println(Object s) {
		print(s + "\n");
	}

	private static void print(Object s) {
		System.out.print(s);
	}

}
