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
package tribefire.extension.messaging.test.comparison;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import tribefire.extension.messaging.service.utils.PropertyVisitor;
import tribefire.extension.messaging.test.comparison.model.Complex;
import tribefire.extension.messaging.test.comparison.model.ComplexWithCollectionOfSimple;
import tribefire.extension.messaging.test.comparison.model.Simple;

public class PropertyVisitorTest {
	private static final String SUCCESS = "success";

	@Test // Simple object property (first tier embedment)
	public void visitPrimitiveFirst() {
		String propertyPathToVisit = "partition";
		Simple obj = getSimple();

		Object result = new PropertyVisitor().visit(obj, propertyPathToVisit);
		assertEquals(SUCCESS, result);
	}

	@Test // Complex object -> simple object -> property (second tier embedment)
	public void visitComplexSecond() {
		String propertyPathToVisit = "simple.partition";
		Complex obj = Complex.T.create();
		obj.setSimple(getSimple());

		Object result = new PropertyVisitor().visit(obj, propertyPathToVisit);
		assertEquals(SUCCESS, result);

	}

	@Test // Complex object -> simple object -> property (third tier embedment)
	public void visitComplexListThird() {
		String propertyPathToVisit = "listSimple.partition";
		ComplexWithCollectionOfSimple obj = getComplex("simpleList");

		Object result = new PropertyVisitor().visit(obj, propertyPathToVisit);
		assertEquals(List.of(SUCCESS, SUCCESS), result);
	}

	@Test // Complex object -> simple object -> property (third tier embedment)
	public void visitComplexMapThird() {
		String propertyPathToVisit = "mapSimple.partition";
		ComplexWithCollectionOfSimple obj = getComplex("simpleMap");

		Object result = new PropertyVisitor().visit(obj, propertyPathToVisit);
		assertEquals(List.of(SUCCESS, SUCCESS), result);
	}

	@Test // Complex object -> simple object -> property (third tier embedment, only element indexed 1 should be
			// visited)
	public void visitIndexedObjectsProperty() {
		String propertyPathToVisit = "listSimple[1].partition";
		ComplexWithCollectionOfSimple obj = getComplex("simpleList");

		Object result = new PropertyVisitor().visit(obj, propertyPathToVisit);
		assertEquals(SUCCESS, result);
	}

	@Test // Complex object -> simple object -> property (third tier embedment, only element with key: 2 should be
			// visited)
	public void visitIndexedObjectsPropertyMap() {
		String propertyPathToVisit = "mapSimple(2).partition";
		ComplexWithCollectionOfSimple obj = getComplex("simpleMap");

		Object result = new PropertyVisitor().visit(obj, propertyPathToVisit);
		assertEquals(SUCCESS, result);
	}

	private ComplexWithCollectionOfSimple getComplex(String objToInsert) {
		ComplexWithCollectionOfSimple obj = ComplexWithCollectionOfSimple.T.create();
		switch (objToInsert) {
			case "simpleList" -> obj.setListSimple(List.of(getSimple(), getSimple()));
			case "simpleSet" -> obj.setSetSimple(Set.of(getSimple(), getSimple()));
			case "simpleMap" -> obj.setMapSimple(Map.of("1", getSimple(), "2", getSimple()));
			default -> throw new IllegalStateException("Unexpected value: " + objToInsert);
		}
		return obj;
	}

	private Simple getSimple() {
		Simple obj = Simple.T.create();
		obj.setPartition(SUCCESS);
		return obj;
	}
}
