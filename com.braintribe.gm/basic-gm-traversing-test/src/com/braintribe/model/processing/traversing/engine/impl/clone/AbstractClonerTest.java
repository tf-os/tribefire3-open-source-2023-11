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
package com.braintribe.model.processing.traversing.engine.impl.clone;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.traversing.engine.GMT;
import com.braintribe.model.processing.traversing.engine.api.usecase.AbsentifySkipUseCase;
import com.braintribe.model.processing.traversing.engine.api.usecase.DefaultSkipUseCase;
import com.braintribe.model.processing.traversing.engine.api.usecase.ReferenceSkipUseCase;
import com.braintribe.model.processing.traversing.engine.impl.misc.model.ComplexTraversingObject;
import com.braintribe.model.processing.traversing.engine.impl.misc.model.EnumA;
import com.braintribe.model.processing.traversing.engine.impl.misc.model.SimpleTraversingObject;
import com.braintribe.model.processing.traversing.engine.impl.misc.printer.EntityPrinter;
import com.braintribe.model.processing.traversing.engine.impl.skip.conditional.ConditionalSkipper;

public abstract class AbstractClonerTest {

	private EntityPrinter entityPrinter;
	
	//TODO fix this overloading
	protected Object cloneWithNoSkipper(Object assembly) throws Exception{
		Cloner cloner = new Cloner();
		GMT.traverse().visitor(cloner).breadthFirstWalk().doFor(assembly);
		return cloner.getClonedValue();
	}
	
	protected Object cloneWithOneSkipper(ConditionalSkipper skipper, Object assembly) throws Exception{
		Cloner cloner = new Cloner();
		GMT.traverse().visitor(skipper).visitor(cloner).breadthFirstWalk().doFor(assembly);
		return cloner.getClonedValue();
	}
	
	protected Object cloneWithTwoSkippers(ConditionalSkipper firstSkipper, ConditionalSkipper secondSkipper, Object assembly) throws Exception{
		Cloner cloner = new Cloner();
		GMT.traverse().visitor(firstSkipper).visitor(secondSkipper).visitor(cloner).breadthFirstWalk().doFor(assembly);
		return cloner.getClonedValue();
	}
	
	protected void compareClonersWithThreeSkipUseCase(ConditionalSkipper skipper, Object assembly,TraversingCriterion tc, EntityType<?> entityType) throws Exception{
		compareClonersWithThreeSkipUseCase(skipper, assembly,tc,entityType,  new boolean[]{true,true,true});
	}
	
	protected void compareClonersWithThreeSkipUseCase(ConditionalSkipper skipper, Object assembly,TraversingCriterion tc, EntityType<?> entityType, boolean [] executionArray) throws Exception{
		if(executionArray[0]){
			// validate skipping
			System.out.println("DefaultSkipUseCase");
			skipper.setSkipUseCase(DefaultSkipUseCase.INSTANCE);
			compareCloners(skipper, assembly, tc, StrategyOnCriterionMatch.skip, entityType);
		}
		if(executionArray[1]){
			// validate absentifying
			System.out.println("AbsentifySkipUseCase");
			skipper.setSkipUseCase(AbsentifySkipUseCase.INSTANCE);
			compareCloners(skipper, assembly, tc, StrategyOnCriterionMatch.partialize,entityType);
		}
		if(executionArray[2]){
			// validate reference
			System.out.println("ReferenceSkipUseCase");
			skipper.setSkipUseCase(ReferenceSkipUseCase.INSTANCE);
			compareCloners(skipper, assembly, tc, StrategyOnCriterionMatch.reference,entityType);
		}

	}
	
	
	protected void compareCloners(ConditionalSkipper skipper, Object assembly,TraversingCriterion tc, StrategyOnCriterionMatch strategy,EntityType<?> entityType) throws Exception{
		Object newClone = cloneWithOneSkipper(skipper, assembly);
		Object oldClone = oldClone(assembly, tc, strategy, entityType);
		compareCloningResults(assembly, newClone, oldClone, entityType);
	}
	
	private void compareCloningResults(Object original, Object newClone, Object oldClone, EntityType<?> entityType){
		// print to reset hash maps
		String originalString = printOriginalEntity((GenericEntity) original, entityType);
		
		entityPrinter.resetForClone();
		entityPrinter.resetForOriginal();
		String newString = printEntity((GenericEntity) newClone, entityType);		
		newString = adjustHashId(entityPrinter.getVisistedIdhash(),newString,entityPrinter.isHashIdExistsPreviously());
		entityPrinter.resetForClone();
		String oldString = printEntity((GenericEntity) oldClone, entityType);
		oldString = adjustHashId(entityPrinter.getVisistedIdhash(), oldString,entityPrinter.isHashIdExistsPreviously());
		
		// TODO maybe create a method that will adjust the string for original strings
		System.out.println("orginal:" + originalString);
		System.out.println("old:" + oldString);
		System.out.println("new:" + newString);
		
		assertThat(newString).isEqualTo(oldString);	
	}
	
	protected void compareCloningResults(Object original, Object firstClone, EntityType<?> entityType){
		// print to reset hash maps
		String originalString = printOriginalEntity((GenericEntity) original, entityType);
		
		entityPrinter.resetForClone();
		entityPrinter.resetForOriginal();
		String firstString = printEntity((GenericEntity) firstClone, entityType);		
		firstString = adjustHashId(entityPrinter.getVisistedIdhash(),firstString,entityPrinter.isHashIdExistsPreviously());

		originalString = adjustHashId(entityPrinter.getOriginalVisistedIdhash(), originalString,entityPrinter.isHashIdExistsPreviously());
		
		// TODO maybe create a method that will adjust the string for original strings
		System.out.println(originalString);
		System.out.println(firstString);
		
		assertThat(originalString).isEqualTo(firstString);	
	}
	
	private static  String adjustHashId(List<String> visitedIdHash, String represntation, boolean anyHashExists){
		
		// TODO make this algorithm smarter
		if(anyHashExists){
		
			for(String hash : visitedIdHash){
				String pattern = hash;
				Pattern replace = Pattern.compile(pattern);
				java.util.regex.Matcher matcher = replace.matcher(represntation);
				represntation = matcher.replaceAll(pattern.replace('a', 'o'));
			}
			
		}
		
		String pattern = "@hashId_(\\d)+";
		Pattern replace = Pattern.compile(pattern);
		java.util.regex.Matcher matcher = replace.matcher(represntation);
		represntation = matcher.replaceAll("UniqueId");
		
		if(anyHashExists){
			
			for(String hash : visitedIdHash ){
				String pattern2 = hash.replace('a', 'o');
				Pattern replace2 = Pattern.compile(pattern2);
				java.util.regex.Matcher matcher2 = replace2.matcher(represntation);
				represntation = matcher2.replaceAll(hash);
			}
		}
		
		return represntation;
	}
	
	
	private static Object oldClone(Object root,TraversingCriterion tc, StrategyOnCriterionMatch strategy, EntityType<?> entityType) {
		StandardCloningContext cc = new StandardCloningContext();
		cc.setMatcher(matcher(tc));
		
		return entityType.clone(cc, root, strategy);

	}
	
	private static Matcher matcher(TraversingCriterion tc) {
		StandardMatcher matcher = new StandardMatcher();
		matcher.setCheckOnlyProperties(false);
		matcher.setCriterion(tc);
		return matcher;
	}
	
	
	protected String printOriginalEntity(GenericEntity entity, EntityType<?> entityType){
		entityPrinter = new EntityPrinter();
		return printEntity(entity, entityType);
		
	}
	
	protected String printEntity(GenericEntity entity, EntityType<?> entityType){
		return entityPrinter.buildToString(entity,  entityType);
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T getAssemblyComplexObjectWithCollections() {
		
		ComplexTraversingObject c1 = ComplexTraversingObject.T.create();
		ComplexTraversingObject c2 = ComplexTraversingObject.T.create();
		ComplexTraversingObject c3 = ComplexTraversingObject.T.create();
		ComplexTraversingObject c4 = ComplexTraversingObject.T.create();
		ComplexTraversingObject c5 = ComplexTraversingObject.T.create();
		ComplexTraversingObject c6 = ComplexTraversingObject.T.create();
		ComplexTraversingObject c7 = ComplexTraversingObject.T.create();
		
		c1.setName("c1");
		c2.setName("c2");
		c3.setName("c3");
		c4.setName("c4");
		c5.setName("c5");
		c6.setName("c6");
		c7.setName("c7");

		List<ComplexTraversingObject> list = new ArrayList<ComplexTraversingObject>();
		list.add(c2);
		list.add(c3);
		list.add(null);
		c1.setListComplex(list);
		
		Set<ComplexTraversingObject> set = new HashSet<ComplexTraversingObject>();
		set.add(c4);
		c1.setSetComplex(set);
		
		Map<ComplexTraversingObject,ComplexTraversingObject> map = new HashMap<ComplexTraversingObject, ComplexTraversingObject>();
		map.put(c5, c6);
		c1.setMapComplexComplex(map);
		
		Map<Integer,ComplexTraversingObject> simpleMap = new HashMap<Integer, ComplexTraversingObject>();
		simpleMap.put(1, c7);
		c1.setMapIntComplex(simpleMap);
		
		c1.setEnumComplex(EnumA.one);
		c2.setEnumComplex(EnumA.two);
		c3.setEnumComplex(EnumA.three);
		c4.setEnumComplex(EnumA.one);
		c5.setEnumComplex(EnumA.two);
		c6.setEnumComplex(EnumA.three);
		c7.setEnumComplex(EnumA.one);
		
		return (T) c1;
	}

	protected ComplexTraversingObject getAssemblyComplexObjectWithList() {
		ComplexTraversingObject c1 = newComplexObject("c1");
		ComplexTraversingObject c2 = newComplexObject("c2");
		ComplexTraversingObject c3 = newComplexObject("c3");

		c1.setEnumComplex(EnumA.two);
		c1.setListComplex(asList(c2, c3));
		
		return c1;
	}

	protected ComplexTraversingObject newComplexObject(String name) {
		ComplexTraversingObject c1 = ComplexTraversingObject.T.create();
		c1.setName(name);
		return c1;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T getAssemblyComplexObjectWithSet() {
		
		ComplexTraversingObject c1 = ComplexTraversingObject.T.create();
		ComplexTraversingObject c2 = ComplexTraversingObject.T.create();
		ComplexTraversingObject c3 = ComplexTraversingObject.T.create();
		ComplexTraversingObject c4 = ComplexTraversingObject.T.create();
		
		c1.setName("c1");
		c2.setName("c2");
		c3.setName("c3");
		c4.setName("c4");

		Set<ComplexTraversingObject> set = new HashSet<ComplexTraversingObject>();
		set.add(c2);
		//TODO item in set are not ordered, so either fix ordering, or create a different method to compare them, i.e. not strings
	//	set.add(c3);
	//	set.add(c4);
		c1.setSetComplex(set);
			
		c1.setEnumComplex(EnumA.two);
		
		return (T) c1;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T getAssemblyComplexObjectWithObjectAndList() {
		
		ComplexTraversingObject c1 = ComplexTraversingObject.T.create();
		ComplexTraversingObject c2 = ComplexTraversingObject.T.create();
		ComplexTraversingObject c3 = ComplexTraversingObject.T.create();
		ComplexTraversingObject c4 = ComplexTraversingObject.T.create();
		
		SimpleTraversingObject s1 = SimpleTraversingObject.T.create();
		
		c1.setName("c1");
		c2.setName("c2");
		c3.setName("c3");
		c4.setName("c4");

		
		s1.setName("s1");
		
		List<ComplexTraversingObject> list = new ArrayList<ComplexTraversingObject>();
		list.add(c2);
		list.add(c3);
			
		c1.setEnumComplex(EnumA.two);
		
		c4.setEnumComplex(EnumA.three);
		
		s1.setNumber(new Integer(3));
		s1.setComplexObject(c4);
		
		c1.setSimpleObject(s1);
		
		return (T) c1;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T getAssemblySimpleObject() {
		
		ComplexTraversingObject c1 = ComplexTraversingObject.T.create();
		
		SimpleTraversingObject s1 = SimpleTraversingObject.T.create();
		
		c1.setName("c1");
		
		s1.setName("s1");
					
		c1.setEnumComplex(EnumA.two);
		
	//	s1.setDate(new Date());
		s1.setNumber(new Integer(3));
	//	s1.setComplexObject(c1);
		
		
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);
		
		s1.setListInt(list);
		
		return (T) s1;
	}
	
	/**
	 * Generates an Assembly consisting of one {@link ComplexTraversingObject} that has N listItems, N SetItems, N MapItems, each with M children 
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getAssemblyStressComplexObjectWithCollections(int collectionSize,int collectionDepth) {
		
		ComplexTraversingObject c1 = ComplexTraversingObject.T.create();
		
		// add list
		List<ComplexTraversingObject> list = new ArrayList<ComplexTraversingObject>();
		for(int i = 0; i < collectionSize; i++){
			ComplexTraversingObject c = null;
			if(collectionDepth > 1){
				c = getAssemblyStressComplexObjectWithCollections(collectionSize,collectionDepth -1);
			}
			else
			{
				c =  ComplexTraversingObject.T.create();
			}
			c.setName("List item_" +i +"_"+collectionDepth);
			list.add(c);	
		}
		c1.setListComplex(list);
		
		
		// add set
		Set<ComplexTraversingObject> set = new HashSet<ComplexTraversingObject>();
		for(int i = 0; i < collectionSize; i++){
			ComplexTraversingObject c = null;
			if(collectionDepth > 1){
				c = getAssemblyStressComplexObjectWithCollections(collectionSize,collectionDepth -1);
			}
			else
			{
				c =  ComplexTraversingObject.T.create();
			}
			c.setName("Set item_" +i +"_"+collectionDepth);
			set.add(c);	
		}
		c1.setSetComplex(set);
		
		
		// add map
		Map<ComplexTraversingObject,ComplexTraversingObject> map = new HashMap<ComplexTraversingObject, ComplexTraversingObject>();
		for(int i = 0; i < collectionSize; i++){
			ComplexTraversingObject key = null;
			if(collectionDepth > 1){
				key = getAssemblyStressComplexObjectWithCollections(collectionSize,collectionDepth -1);
			}
			else
			{
				key =  ComplexTraversingObject.T.create();
			}
			key.setName("Map Key item_" +i+"_"+collectionDepth);
			ComplexTraversingObject value = null;
			if(collectionDepth > 1){
				value = getAssemblyStressComplexObjectWithCollections(collectionSize,collectionDepth -1);
			}
			else
			{
				value =  ComplexTraversingObject.T.create();
			}
			value.setName("Map Value item_" +i+"_"+collectionDepth);
			map.put(key, value);
		}
		c1.setMapComplexComplex(map);
		
		// add simple map
		Map<Integer,ComplexTraversingObject> simpleMap = new HashMap<Integer, ComplexTraversingObject>();
		for(int i = 0; i < collectionSize; i++){
			ComplexTraversingObject value = null;
			if(collectionDepth > 1){
				value = getAssemblyStressComplexObjectWithCollections(collectionSize,collectionDepth -1);
			}
			else
			{
				value =  ComplexTraversingObject.T.create();
			}
			value.setName("Map Value item_" +i+"_"+collectionDepth);
			simpleMap.put(i, value);
		}
		c1.setMapIntComplex(simpleMap);
		
		return (T) c1;
	}
	
}
