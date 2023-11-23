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
package com.braintribe.model.openapi.v3_0.reference;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.openapi.v3_0.OpenapiComponents;
import com.braintribe.model.openapi.v3_0.OpenapiDiscriminator;
import com.braintribe.model.openapi.v3_0.OpenapiEncoding;
import com.braintribe.model.openapi.v3_0.OpenapiInfo;
import com.braintribe.model.openapi.v3_0.OpenapiParameter;
import com.braintribe.model.openapi.v3_0.OpenapiPath;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.OpenapiServer;
import com.braintribe.model.openapi.v3_0.OpenapiServerVariable;
import com.braintribe.model.openapi.v3_0.meta.OpenapiContact;
import com.braintribe.model.openapi.v3_0.reference.utils.AbstractComponentsTest;
import com.braintribe.model.openapi.v3_0.reference.utils.TestApiContext;

public class AdvancedCyclingComponentsTest extends AbstractComponentsTest {
	@Test
	public void testSimple(){
		
		// One cycle with 4 elements:
		// - 4 (fake) OpenapiSchemas of OpenapiComponents -> OpenapiParameter -> OpenapiPath -> OpenapiContact -> OpenapiComponents
		// Another cycle with 2 elements starting at OpenapiPath:
		// - OpenapiDiscriminator -> OpenapiEncoding -> OpenapiDiscriminator
		// 2 more branches (no cycles)
		// - OpenapiDiscriminator -> OpenapiServer -> OpenapiInfo
		// - OpenapiContact -> OpenapiServerVariable
		OpenapiSchema schemaRef = schemaRef(rootContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T, c2 -> {
				OpenapiSchema ref2 = schemaRef(c2, OpenapiPath.T, c3 -> {
					OpenapiSchema ref3 = schemaRef(c3, OpenapiContact.T, c4 -> {
						OpenapiSchema ref4 = alreadyPresentSchemaRef(c4, OpenapiComponents.T);
						
						OpenapiSchema branchRef = schemaRef(c4, OpenapiServerVariable.T);
						OpenapiSchema comp = createComponentWithItemsRef(ref4);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema refSecondCycle = schemaRef(c3, OpenapiDiscriminator.T, c4 -> {
						OpenapiSchema refSecondCycle2 = schemaRef(c4, OpenapiEncoding.T, c5 -> {
							OpenapiSchema refSecondCycle3 = alreadyPresentSchemaRef(c5, OpenapiDiscriminator.T);
								
							return createComponentWithItemsRef(refSecondCycle3);
						});

						OpenapiSchema branchRef = schemaRef(c4, OpenapiServer.T, c6 -> {
							OpenapiSchema branchRef2 = schemaRef(c6, OpenapiInfo.T);
							
							return createComponentWithItemsRef(branchRef2);
						});
						
						OpenapiSchema comp = createComponentWithItemsRef(refSecondCycle2);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema comp = createComponentWithItemsRef(ref3);
					comp.setAdditionalProperties(refSecondCycle);
					
					return comp;
				});
				
				return createComponentWithItemsRef(ref2);
			});
			
			return createComponentWithItemsRef(ref);
		});
		
		assertAll(schemaRef, rootContext);
		
	}

	private OpenapiSchema createComponentWithItemsRef(OpenapiSchema refSecondCycle3) {
		OpenapiSchema compSecondCycle3 = OpenapiSchema.T.create();
		compSecondCycle3.setItems(refSecondCycle3);
		return compSecondCycle3;
	}
	
	
	private OpenapiSchema assertCycle1(OpenapiSchema componentsRef, TestApiContext context) {
		// Cycle 1
		OpenapiSchema componentsComponent = getSchemaComponent(OpenapiComponents.T, context, componentsRef);
		OpenapiSchema parameterRef = componentsComponent.getItems();
		OpenapiSchema parameterComponent = getSchemaComponent(OpenapiParameter.T, context, parameterRef);
		OpenapiSchema pathRef = parameterComponent.getItems();
		OpenapiSchema pathComponent = getSchemaComponent(OpenapiPath.T, context, pathRef);
		OpenapiSchema contactRef = pathComponent.getItems();
		OpenapiSchema contactComponent = getSchemaComponent(OpenapiContact.T, context, contactRef);
		OpenapiSchema componentsRef2 = contactComponent.getItems();
		OpenapiSchema componentsComponent2 = getSchemaComponent(OpenapiComponents.T, context, componentsRef2);
		
		assertThat(componentsComponent).isSameAs(componentsComponent2);
		
		return pathComponent;
	}
	
	private OpenapiSchema assertCycle2(TestApiContext context, OpenapiSchema pathComponent) {
		// Cycle 2
		OpenapiSchema discRef = pathComponent.getAdditionalProperties();
		OpenapiSchema discComponent = getSchemaComponent(OpenapiDiscriminator.T, context, discRef);
		OpenapiSchema encodingRef = discComponent.getItems();
		OpenapiSchema encodingComponent = getSchemaComponent(OpenapiEncoding.T, context, encodingRef);
		
		assertThat(encodingComponent.getItems().get$ref()).isSameAs(discRef.get$ref());
		
		return discComponent;
	}
	
	private void assertBranch1(TestApiContext context, OpenapiSchema discComponent) {
		// Branch 1
		OpenapiSchema serverRef = discComponent.getAdditionalProperties();
		OpenapiSchema serverComponent = getSchemaComponent(OpenapiServer.T, context, serverRef);
		OpenapiSchema infoRef = serverComponent.getItems();
		OpenapiSchema infoComponent = getSchemaComponent(OpenapiInfo.T, context, infoRef);
	}
	
	private void assertBranch2(TestApiContext context, OpenapiSchema pathComponent) {
		// get contact again from path. We can just assume it's correct because it must have been tested before
		OpenapiSchema contactComponent = getSchemaComponentFromRef(pathComponent.getItems());
		
		// Branch 2
		OpenapiSchema variableRef = contactComponent.getAdditionalProperties();
		OpenapiSchema variableComponent = getSchemaComponent(OpenapiServerVariable.T, context, variableRef);
	}
	
	private void assertAll(OpenapiSchema schemaRef, TestApiContext context) {
		OpenapiSchema pathComponent = assertCycle1(schemaRef, context);
		OpenapiSchema discComponent = assertCycle2(context, pathComponent);
		assertBranch1(context, discComponent);
		assertBranch2(context, pathComponent);
	}
	
	@Test
	public void testMultipleContexts() {
		
		TestApiContext childContext = rootContext.childContext("CHILD");
		
		OpenapiSchema schemaRef = schemaRef(childContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T, c2 -> {
				OpenapiSchema ref2 = schemaRef(c2, OpenapiPath.T, c3 -> {
					OpenapiSchema ref3 = schemaRef(c3, OpenapiContact.T, c4 -> {
						OpenapiSchema ref4 = alreadyPresentSchemaRef(c4, OpenapiComponents.T);
						
						OpenapiSchema branchRef = schemaRef(c4, OpenapiServerVariable.T);
						OpenapiSchema comp = createComponentWithItemsRef(ref4);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema refSecondCycle = schemaRef(c3, OpenapiDiscriminator.T, c4 -> {
						OpenapiSchema refSecondCycle2 = schemaRef(c4, OpenapiEncoding.T, c5 -> {
							OpenapiSchema refSecondCycle3 = alreadyPresentSchemaRef(c5, OpenapiDiscriminator.T);
								
							return createComponentWithItemsRef(refSecondCycle3);
						});

						OpenapiSchema branchRef = schemaRef(c4, OpenapiServer.T, c6 -> {
							OpenapiSchema branchRef2 = schemaRef(c6, OpenapiInfo.T);
							
							return createComponentWithItemsRef(branchRef2);
						});
						
						OpenapiSchema comp = createComponentWithItemsRef(refSecondCycle2);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema comp = createComponentWithItemsRef(ref3);
					comp.setAdditionalProperties(refSecondCycle);
					
					return comp;
				});
				
				return createComponentWithItemsRef(ref2);
			});
			
			return createComponentWithItemsRef(ref);
		});
		
		assertAll(schemaRef, rootContext);
	}
	
	@Test
	public void testMultipleContextsWithChangesBranch1() {
		// Changes in branch one affect Cycle 2 which again affects Cycle 1 but they don't affect branch 2
		
		TestApiContext childContext = rootContext.childContext("CHILD");
		
		OpenapiSchema schemaRef = schemaRef(childContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T, c2 -> {
				OpenapiSchema ref2 = schemaRef(c2, OpenapiPath.T, c3 -> {
					OpenapiSchema ref3 = schemaRef(c3, OpenapiContact.T, c4 -> {
						OpenapiSchema ref4 = alreadyPresentSchemaRef(c4, OpenapiComponents.T);
						
						OpenapiSchema branchRef = schemaRef(c4, OpenapiServerVariable.T);
						OpenapiSchema comp = createComponentWithItemsRef(ref4);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema refSecondCycle = schemaRef(c3, OpenapiDiscriminator.T, c4 -> {
						OpenapiSchema refSecondCycle2 = schemaRef(c4, OpenapiEncoding.T, c5 -> {
							OpenapiSchema refSecondCycle3 = alreadyPresentSchemaRef(c5, OpenapiDiscriminator.T);
								
							return createComponentWithItemsRef(refSecondCycle3);
						});

						OpenapiSchema branchRef = schemaRef(c4, OpenapiServer.T, c6 -> {
							OpenapiSchema branchRef2 = schemaRef(c6, OpenapiInfo.T, c7 -> {
								OpenapiSchema infoComp = OpenapiSchema.T.create();
								
								if (c7 == childContext) {
									infoComp.setDescription("Changed in child context");
								}
								
								return infoComp;
							});
							
							return createComponentWithItemsRef(branchRef2);
						});
						
						OpenapiSchema comp = createComponentWithItemsRef(refSecondCycle2);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema comp = createComponentWithItemsRef(ref3);
					comp.setAdditionalProperties(refSecondCycle);
					
					return comp;
				});
				
				return createComponentWithItemsRef(ref2);
			});
			
			return createComponentWithItemsRef(ref);
		});
		
		OpenapiSchema pathComponent = assertCycle1(schemaRef, childContext);
		OpenapiSchema discComponent = assertCycle2(childContext, pathComponent);
		assertBranch1(childContext, discComponent);
		assertBranch2(rootContext, pathComponent);
	}
	
	@Test
	public void testMultipleContextsWithChangesBranch2() {
		// Changes in Branch 2 should affect cycle one only
		
		TestApiContext childContext = rootContext.childContext("CHILD");
		
		OpenapiSchema schemaRef = schemaRef(childContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T, c2 -> {
				OpenapiSchema ref2 = schemaRef(c2, OpenapiPath.T, c3 -> {
					OpenapiSchema ref3 = schemaRef(c3, OpenapiContact.T, c4 -> {
						OpenapiSchema ref4 = alreadyPresentSchemaRef(c4, OpenapiComponents.T);
						
						OpenapiSchema branchRef = schemaRef(c4, OpenapiServerVariable.T, c5 -> {
							OpenapiSchema infoComp = OpenapiSchema.T.create();
							
							if (c5 == childContext) {
								infoComp.setDescription("Changed in child context");
							}
							
							return infoComp;
						});
						OpenapiSchema comp = createComponentWithItemsRef(ref4);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema refSecondCycle = schemaRef(c3, OpenapiDiscriminator.T, c4 -> {
						OpenapiSchema refSecondCycle2 = schemaRef(c4, OpenapiEncoding.T, c5 -> {
							OpenapiSchema refSecondCycle3 = alreadyPresentSchemaRef(c5, OpenapiDiscriminator.T);
								
							return createComponentWithItemsRef(refSecondCycle3);
						});

						OpenapiSchema branchRef = schemaRef(c4, OpenapiServer.T, c6 -> {
							OpenapiSchema branchRef2 = schemaRef(c6, OpenapiInfo.T);
							
							return createComponentWithItemsRef(branchRef2);
						});
						
						OpenapiSchema comp = createComponentWithItemsRef(refSecondCycle2);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema comp = createComponentWithItemsRef(ref3);
					comp.setAdditionalProperties(refSecondCycle);
					
					return comp;
				});
				
				return createComponentWithItemsRef(ref2);
			});
			
			return createComponentWithItemsRef(ref);
		});
		
		OpenapiSchema pathComponent = assertCycle1(schemaRef, childContext);
		OpenapiSchema discComponent = assertCycle2(rootContext, pathComponent);
		assertBranch1(rootContext, discComponent);
		assertBranch2(childContext, pathComponent);
	}
	
	@Test
	public void testMultipleContextsWithChangesCycle1() {
		
		// Changes in Cycle 1 shouldn't affect anything else
		
		TestApiContext childContext = rootContext.childContext("CHILD");
		
		OpenapiSchema schemaRef = schemaRef(childContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T, c2 -> {
				assertThat(c2).isSameAs(c);
				OpenapiSchema ref2 = schemaRef(c2, OpenapiPath.T, c3 -> {
					OpenapiSchema ref3 = schemaRef(c3, OpenapiContact.T, c4 -> {
						OpenapiSchema ref4 = alreadyPresentSchemaRef(c4, OpenapiComponents.T);
						
						OpenapiSchema branchRef = schemaRef(c4, OpenapiServerVariable.T);
						OpenapiSchema comp = createComponentWithItemsRef(ref4);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					assertThat(c3).isSameAs(c2).isSameAs(c);
					
					OpenapiSchema refSecondCycle = schemaRef(c3, OpenapiDiscriminator.T, c4 -> {
						assertThat(c4).isSameAs(c3).isSameAs(c2).isSameAs(c);
						
						OpenapiSchema refSecondCycle2 = schemaRef(c4, OpenapiEncoding.T, c5 -> {
							OpenapiSchema refSecondCycle3 = alreadyPresentSchemaRef(c5, OpenapiDiscriminator.T);
							
							assertThat(c5).isSameAs(c4).isSameAs(c3).isSameAs(c2).isSameAs(c);
								
							return createComponentWithItemsRef(refSecondCycle3);
						});

						OpenapiSchema branchRef = schemaRef(c4, OpenapiServer.T, c6 -> {
							OpenapiSchema branchRef2 = schemaRef(c6, OpenapiInfo.T);
							
							return createComponentWithItemsRef(branchRef2);
						});
						
						OpenapiSchema comp = createComponentWithItemsRef(refSecondCycle2);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema comp = createComponentWithItemsRef(ref3);
					comp.setAdditionalProperties(refSecondCycle);
					
					if (c3 == childContext) {
						comp.setDescription("Changed in Child context");
					}
					
					return comp;
				});
				
				return createComponentWithItemsRef(ref2);
			});
			
			return createComponentWithItemsRef(ref);
		});
		
		OpenapiSchema pathComponent = assertCycle1(schemaRef, childContext);
		OpenapiSchema discComponent = assertCycle2(rootContext, pathComponent);
		assertBranch1(rootContext, discComponent);
		assertBranch2(rootContext, pathComponent);
	}
	
	@Test
	public void testMultipleContextsWithChangesCycle2() {
		
		// Changes in Cycle 2 should only affect Cycle 1
		
		TestApiContext childContext = rootContext.childContext("CHILD");
		
		OpenapiSchema schemaRef = schemaRef(childContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T, c2 -> {
				OpenapiSchema ref2 = schemaRef(c2, OpenapiPath.T, c3 -> {
					OpenapiSchema ref3 = schemaRef(c3, OpenapiContact.T, c4 -> {
						OpenapiSchema ref4 = alreadyPresentSchemaRef(c4, OpenapiComponents.T);
						
						OpenapiSchema branchRef = schemaRef(c4, OpenapiServerVariable.T);
						OpenapiSchema comp = createComponentWithItemsRef(ref4);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema refSecondCycle = schemaRef(c3, OpenapiDiscriminator.T, c4 -> {
						OpenapiSchema refSecondCycle2 = schemaRef(c4, OpenapiEncoding.T, c5 -> {
							OpenapiSchema refSecondCycle3 = alreadyPresentSchemaRef(c5, OpenapiDiscriminator.T);
							
							OpenapiSchema comp = createComponentWithItemsRef(refSecondCycle3);

							if (c5 == childContext) {
								comp.setDescription("Changed in Child context");
							}
							
							return comp;
						});
						
						OpenapiSchema branchRef = schemaRef(c4, OpenapiServer.T, c6 -> {
							OpenapiSchema branchRef2 = schemaRef(c6, OpenapiInfo.T);
							
							return createComponentWithItemsRef(branchRef2);
						});
						
						OpenapiSchema comp = createComponentWithItemsRef(refSecondCycle2);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema comp = createComponentWithItemsRef(ref3);
					comp.setAdditionalProperties(refSecondCycle);
					
					return comp;
				});
				
				return createComponentWithItemsRef(ref2);
			});
			
			return createComponentWithItemsRef(ref);
		});
		
		OpenapiSchema pathComponent = assertCycle1(schemaRef, childContext);
		OpenapiSchema discComponent = assertCycle2(childContext, pathComponent);
		assertBranch1(rootContext, discComponent);
		assertBranch2(rootContext, pathComponent);
	}
	
	@Test
	public void testBuilder() {
		
		// Changes in Cycle 2 should only affect Cycle 1
		
		TestApiContext childContext = rootContext.childContext("CHILD");
		
		ComponentTestBuilder builder = new ComponentTestBuilder(this);
		
		ComponentTestBuilder builderAtPath = builder //
			.add(OpenapiParameter.T) //
				.add(OpenapiPath.T);
		
		ComponentTestBuilder builderAtContact = builderAtPath //
			.add(OpenapiContact.T);
		
		builderAtContact.addBranch(OpenapiServerVariable.T);
		builderAtContact.addAlreadyExisting(OpenapiComponents.T);
		
		ComponentTestBuilder builderAtDiscriminator = builderAtPath //
			.addBranch(OpenapiDiscriminator.T);
		
		builderAtDiscriminator
				.add(OpenapiEncoding.T)
					.changeForContext(childContext)
					.addAlreadyExisting(OpenapiDiscriminator.T);
		
		builderAtDiscriminator
			.addBranch(OpenapiServer.T)
				.add(OpenapiInfo.T);
		
		
		OpenapiSchema schemaRef = builder.buildRef(childContext);
		
		OpenapiSchema pathComponent = assertCycle1(schemaRef, childContext);
		OpenapiSchema discComponent = assertCycle2(childContext, pathComponent);
		assertBranch1(rootContext, discComponent);
		assertBranch2(rootContext, pathComponent);
	}
	
	/* TODO: Enable when multiple parent contexts are supported 
	@Test
	public void testContextMess() {
		
		OpenapiOperationContext childContext = new OpenapiOperationContext(rootContext, false, "CHILD");
		OpenapiOperationContext grandchildContext = new OpenapiOperationContext(childContext, false, "CHILD2");
		OpenapiOperationContext greatgrandchildContext = new OpenapiOperationContext(grandchildContext, false, "CHILD3");
		OpenapiOperationContext greatgreatgrandchildContext = new OpenapiOperationContext(greatgrandchildContext, false, "CHILD4");
		
		OpenapiOperationContext relativeContext = new OpenapiOperationContext(childContext, false, "CHILD_R");
		OpenapiOperationContext relativeChildContext = new OpenapiOperationContext(relativeContext, false, "CHILD_R2");
		
		greatgrandchildContext.getParentContexts().add(relativeChildContext);
		
		ComponentTestBuilder builder = new ComponentTestBuilder(this);
		
		ComponentTestBuilder builderAtPath = builder //
				.add(OpenapiParameter.T) //
				.add(OpenapiPath.T);
		
		ComponentTestBuilder builderAtContact = builderAtPath //
				.add(OpenapiContact.T);
		
		builderAtContact.addBranch(OpenapiServerVariable.T).changeForContexts("RELATIVE CHILD CHANGE", relativeChildContext, greatgrandchildContext, greatgreatgrandchildContext);
		builderAtContact.addAlreadyExisting(OpenapiComponents.T);
		
		ComponentTestBuilder builderAtDiscriminator = builderAtPath //
				.addBranch(OpenapiDiscriminator.T);
		
		builderAtDiscriminator
			.add(OpenapiEncoding.T)
			.changeForContexts("CHILD CHANGE", childContext, grandchildContext, greatgrandchildContext, greatgreatgrandchildContext, relativeContext, relativeChildContext)
			.addAlreadyExisting(OpenapiDiscriminator.T);
		
		builderAtDiscriminator
		.addBranch(OpenapiServer.T)
		.add(OpenapiInfo.T);
		
		OpenapiSchema schemaRef = builder.buildRef(greatgreatgrandchildContext);
		
		OpenapiSchema pathComponent = assertCycle1(schemaRef, relativeChildContext);
		OpenapiSchema discComponent = assertCycle2(childContext, pathComponent);
		assertBranch1(rootContext, discComponent);
		assertBranch2(relativeChildContext, pathComponent);
	}
	*/
	@Test
	public void testClosureVariableIssue() {
		
		TestApiContext childContext = rootContext.childContext("CHILD");
		
		OpenapiSchema schemaRef = schemaRef(childContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T, c2 -> {
				OpenapiSchema ref2 = schemaRef(c2, OpenapiPath.T, c3 -> {
					OpenapiSchema ref3 = schemaRef(c3, OpenapiContact.T, c4 -> {
						OpenapiSchema ref4 = alreadyPresentSchemaRef(c4, OpenapiComponents.T);
						
						OpenapiSchema branchRef = schemaRef(c4, OpenapiServerVariable.T);
						OpenapiSchema comp = createComponentWithItemsRef(ref4);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema refSecondCycle = schemaRef(c3, OpenapiDiscriminator.T, c4 -> {
						OpenapiSchema refSecondCycle2 = schemaRef(c4, OpenapiEncoding.T, c5 -> {
							OpenapiSchema refSecondCycle3 = alreadyPresentSchemaRef(c5, OpenapiDiscriminator.T);
							
							OpenapiSchema comp = createComponentWithItemsRef(refSecondCycle3);
							
							// java-reference from further up the component-reference chain
							// This one must be from the same context
							comp.setAdditionalProperties(ref3);
							
							// assert that they are from the same context
							// TODO: maybe do this outside this factory lambda later
							assertThat(schemaRef(c4, OpenapiContact.T)).isSameAs(ref3);

							return comp;
						});
						
						OpenapiSchema branchRef = schemaRef(c4, OpenapiServer.T, c6 -> {
							OpenapiSchema branchRef2 = schemaRef(c6, OpenapiInfo.T);
							
							return createComponentWithItemsRef(branchRef2);
						});
						
						OpenapiSchema comp = createComponentWithItemsRef(refSecondCycle2);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema comp = createComponentWithItemsRef(ref3);
					comp.setAdditionalProperties(refSecondCycle);
					
					return comp;
				});
				
				return createComponentWithItemsRef(ref2);
			});
			
			return createComponentWithItemsRef(ref);
		});
		
		assertAll(schemaRef, rootContext);
	}
	
	@Test
	public void testNestedCycles(){
		TestApiContext childContext = rootContext.childContext("CHILD");

		// One cycle with 4 elements:
		// - 4 (fake) OpenapiSchemas of OpenapiComponents -> OpenapiParameter -> OpenapiPath -> OpenapiContact -> OpenapiComponents
		// Another cycle with 2 elements starting at OpenapiPath:
		// - OpenapiDiscriminator -> OpenapiEncoding -> OpenapiDiscriminator
		// Another cycle referencing from OpenapiParameter to OpenapiComponents
		// 2 more branches (no cycles)
		// - OpenapiDiscriminator -> OpenapiServer -> OpenapiInfo
		// - OpenapiContact -> OpenapiServerVariable
		OpenapiSchema schemaRef = schemaRef(childContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T, c2 -> {
				OpenapiSchema ref2 = schemaRef(c2, OpenapiPath.T, c3 -> {
					OpenapiSchema ref3 = schemaRef(c3, OpenapiContact.T, c4 -> {
						OpenapiSchema ref4 = alreadyPresentSchemaRef(c4, OpenapiComponents.T);
						
						OpenapiSchema branchRef = schemaRef(c4, OpenapiServerVariable.T);
						OpenapiSchema comp = createComponentWithItemsRef(ref4);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema refSecondCycle = schemaRef(c3, OpenapiDiscriminator.T, c4 -> {
						OpenapiSchema refSecondCycle2 = schemaRef(c4, OpenapiEncoding.T, c5 -> {
							OpenapiSchema refSecondCycle3 = alreadyPresentSchemaRef(c5, OpenapiDiscriminator.T);
								
							return createComponentWithItemsRef(refSecondCycle3);
						});

						OpenapiSchema branchRef = schemaRef(c4, OpenapiServer.T, c6 -> {
							OpenapiSchema branchRef2 = schemaRef(c6, OpenapiInfo.T);
							
							OpenapiSchema inf = createComponentWithItemsRef(branchRef2);
							inf.setAdditionalProperties(alreadyPresentSchemaRef(c2, OpenapiComponents.T));
							return inf;
						});
						
						OpenapiSchema comp = createComponentWithItemsRef(refSecondCycle2);
						comp.setAdditionalProperties(branchRef);
						
						return comp;
					});
					
					OpenapiSchema comp = createComponentWithItemsRef(ref3);
					comp.setAdditionalProperties(refSecondCycle);
					
					if (c2 == childContext) {
						comp.setDescription("Changed in Child context");
					}
					
					return comp;
				});
				
				return createComponentWithItemsRef(ref2);
			});
			
			return createComponentWithItemsRef(ref);
		});

		// Because Cycle 2 references to Cycle 3 which again references to the start they all must be created by the same context 
		OpenapiSchema pathComponent = assertCycle1(schemaRef, childContext);
		OpenapiSchema discComponent = assertCycle2(childContext, pathComponent);
		
		assertBranch2(rootContext, pathComponent); // Only Branch 2 is independent

		// What was Branch 1 in other tests is no a third cycle back to the start
		OpenapiSchema serverRef = discComponent.getAdditionalProperties();
		OpenapiSchema serverComponent = getSchemaComponent(OpenapiServer.T, childContext, serverRef);
		OpenapiSchema infoRef = serverComponent.getItems();
		OpenapiSchema infoComponent = getSchemaComponent(OpenapiInfo.T, rootContext, infoRef);
		OpenapiSchema originalRef = serverComponent.getAdditionalProperties();
		assertThat(originalRef.get$ref()).isEqualTo(schemaRef.get$ref());
	}
}
