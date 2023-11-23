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
package com.braintribe.model.processing.securityservice.basic.test.base;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.processing.securityservice.api.exceptions.UserNotFoundException;
import com.braintribe.model.processing.securityservice.basic.user.UserInternalService;
import com.braintribe.model.processing.securityservice.basic.user.UserInternalServiceImpl;
import com.braintribe.model.securityservice.credentials.identification.EmailIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.user.User;

/**
 * Tests on {@link UserInternalService} internal service operations
 */
public class UserInternalServiceTestBase extends SecurityServiceTest {

	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#findUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	valid property name/value matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link User} shall be returned with same {@link User#getId()} object passed on as property value to {@link UserInternalServiceImpl#findUser(String, String)}
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testFindUserById() {
		testFindUserByName("john.smith");
		testFindUserByName("mary.williams");
		testFindUserByName("robert.taylor");
		testFindUserByName("steven.brown");
	}
	
	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#findUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	valid property name/value matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link User} shall be returned with same {@link User#getEmail()} object passed on as property value to {@link UserInternalServiceImpl#findUser(String, String)}
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testFindUserByEmail() {
		testFindUserByEmail("john.smith@braintribe.com");
		testFindUserByEmail("mary.williams@braintribe.com");
		testFindUserByEmail("robert.taylor@braintribe.com");
		testFindUserByEmail("steven.brown@braintribe.com");
	}
	
	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#findUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	valid property name/value matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link User} shall be returned with same {@link User#getFirstName()} object passed on as property value to {@link UserInternalServiceImpl#findUser(String, String)}
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testFindUserByFirstName() {
		testFindUserByFirstName("John");
		testFindUserByFirstName("Mary");
		testFindUserByFirstName("Robert");
		testFindUserByFirstName("Steven");
	}
	
	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#findUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	valid property name/value matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link User} shall be returned with same {@link User#getLastName()} object passed on as property value to {@link UserInternalServiceImpl#findUser(String, String)}
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testFindUserByLastName() {
		testFindUserByLastName("Smith");
		testFindUserByLastName("Williams");
		testFindUserByLastName("Taylor");
		testFindUserByLastName("Brown");
	}
	
	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#retrieveUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	valid property name/value matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link User} shall be returned with same {@link User#getId()} object passed on as property value to {@link UserInternalServiceImpl#retrieveUser(String, String)}
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testRetrieveUserById() {
		testRetrieveUserByName("john.smith");
		testRetrieveUserByName("mary.williams");
		testRetrieveUserByName("robert.taylor");
		testRetrieveUserByName("steven.brown");
	}
	
	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#retrieveUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	valid property name/value matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link User} shall be returned with same {@link User#getEmail()} object passed on as property value to {@link UserInternalServiceImpl#retrieveUser(String, String)}
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testRetrieveUserByEmail() {
		testRetrieveUserByEmail("john.smith@braintribe.com");
		testRetrieveUserByEmail("mary.williams@braintribe.com");
		testRetrieveUserByEmail("robert.taylor@braintribe.com");
		testRetrieveUserByEmail("steven.brown@braintribe.com");
	}
	
	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#retrieveUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	valid property name/value matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link User} shall be returned with same {@link User#getFirstName()} object passed on as property value to {@link UserInternalServiceImpl#retrieveUser(String, String)}
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testRetrieveUserByFirstName() {
		testRetrieveUserByFirstName("John");
		testRetrieveUserByFirstName("Mary");
		testRetrieveUserByFirstName("Robert");
		testRetrieveUserByFirstName("Steven");
	}

	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#retrieveUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	valid property name/value matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link User} shall be returned with same {@link User#getLastName()} object passed on as property value to {@link UserInternalServiceImpl#retrieveUser(String, String)}
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testRetrieveUserByLastName() {
		testRetrieveUserByLastName("Smith");
		testRetrieveUserByLastName("Williams");
		testRetrieveUserByLastName("Taylor");
		testRetrieveUserByLastName("Brown");
	}

	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#retrieveUser(UserIdentification)}
	 * 
	 * <p>
	 * Input: 
	 * 	{@link UserNameIdentification} matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link User} shall be returned with same {@link User#getId()} object passed on as the {@link UserNameIdentification} to {@link UserInternalServiceImpl#retrieveUser(UserIdentification)}
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testRetrieveUserWithUserNameIdentification() {
		testRetrieveUserWithUserNameIdentification("john.smith");
		testRetrieveUserWithUserNameIdentification("mary.williams");
		testRetrieveUserWithUserNameIdentification("robert.taylor");
		testRetrieveUserWithUserNameIdentification("steven.brown");
	}

	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#retrieveUser(UserIdentification)}
	 * 
	 * <p>
	 * Input: 
	 * 	{@link EmailIdentification} matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link User} shall be returned with same {@link User#getEmail()} object passed on as the {@link UserNameIdentification} to {@link UserInternalServiceImpl#retrieveUser(UserIdentification)}
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testRetrieveUserWithEmailIdentification() {
		testRetrieveUserWithEmailIdentification("john.smith@braintribe.com");
		testRetrieveUserWithEmailIdentification("mary.williams@braintribe.com");
		testRetrieveUserWithEmailIdentification("robert.taylor@braintribe.com");
		testRetrieveUserWithEmailIdentification("steven.brown@braintribe.com");
	}

	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#findUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	 property name/values NOT matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@code null} shall be returned
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testFindUserByInexistentId() {
		testFindUserByInexistentId(null);
		testFindUserByInexistentId(empty);
		
		testFindUserByInexistentId("john.smith\t");
		testFindUserByInexistentId("\tjohn.smith\t");
		testFindUserByInexistentId("\tjohn.smith");
		testFindUserByInexistentId("John.smith");
		testFindUserByInexistentId("JOHN.SMITH");
		testFindUserByInexistentId("john.smith@braintribe.com");
		testFindUserByInexistentId("John");
		testFindUserByInexistentId("Smith");
		
		testFindUserByInexistentId("mary.williams\t");
		testFindUserByInexistentId("\tmary.williams\t");
		testFindUserByInexistentId("\tmary.williams");
		testFindUserByInexistentId("Mary.williams");
		testFindUserByInexistentId("MARY.WILLIAMS");
		testFindUserByInexistentId("mary.williams@braintribe.com");
		testFindUserByInexistentId("Mary");
		testFindUserByInexistentId("Williams");
		
		testFindUserByInexistentId("robert.taylor\t");
		testFindUserByInexistentId("\trobert.taylor\t");
		testFindUserByInexistentId("\trobert.taylor");
		testFindUserByInexistentId("Robert.taylor");
		testFindUserByInexistentId("ROBERT.TAYLOR");
		testFindUserByInexistentId("robert.taylor@braintribe.com");
		testFindUserByInexistentId("Robert");
		testFindUserByInexistentId("Taylor");
		
		testFindUserByInexistentId("steven.brown\t");
		testFindUserByInexistentId("\tsteven.brown\t");
		testFindUserByInexistentId("\tsteven.brown");
		testFindUserByInexistentId("Steven.brown");
		testFindUserByInexistentId("STEVEN.BROWN");
		testFindUserByInexistentId("steven.brown@braintribe.com");
		testFindUserByInexistentId("Steven");
		testFindUserByInexistentId("Brown");
	}

	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#findUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	 property name/values NOT matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions:
	 * <ul> 
	 * 	 <li>{@code null} shall be returned
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testFindUserByInexistentEmail() {
		testFindUserByInexistentEmail(null);
		testFindUserByInexistentEmail(empty);
		                        
		testFindUserByInexistentEmail("john.smith@braintribe.com\t");
		testFindUserByInexistentEmail("\tjohn.smith@braintribe.com\t");
		testFindUserByInexistentEmail("\tjohn.smith@braintribe.com");
		testFindUserByInexistentEmail("John.smith@braintribe.com");
		testFindUserByInexistentEmail("JOHN.SMITH@BRAINTRIBE.COM");
		testFindUserByInexistentEmail("john.smith");
		testFindUserByInexistentEmail("John");
		testFindUserByInexistentEmail("Smith");
		                        
		testFindUserByInexistentEmail("mary.williams@braintribe.com\t");
		testFindUserByInexistentEmail("\tmary.williams@braintribe.com\t");
		testFindUserByInexistentEmail("\tmary.williams@braintribe.com");
		testFindUserByInexistentEmail("Mary.williams@braintribe.com");
		testFindUserByInexistentEmail("MARY.WILLIAMS@BRAINTRIBE.COM");
		testFindUserByInexistentEmail("mary.williams");
		testFindUserByInexistentEmail("Mary");
		testFindUserByInexistentEmail("Williams");
		                        
		testFindUserByInexistentEmail("robert.taylor@braintribe.com\t");
		testFindUserByInexistentEmail("\trobert.taylor@braintribe.com\t");
		testFindUserByInexistentEmail("\trobert.taylor@braintribe.com");
		testFindUserByInexistentEmail("Robert.taylor@braintribe.com");
		testFindUserByInexistentEmail("ROBERT.TAYLOR@BRAINTRIBE.COM");
		testFindUserByInexistentEmail("robert.taylor");
		testFindUserByInexistentEmail("Robert");
		testFindUserByInexistentEmail("Taylor");
		                        
		testFindUserByInexistentEmail("steven.brown@braintribe.com\t");
		testFindUserByInexistentEmail("\tsteven.brown@braintribe.com\t");
		testFindUserByInexistentEmail("\tsteven.brown@braintribe.com");
		testFindUserByInexistentEmail("Steven.brown@braintribe.com");
		testFindUserByInexistentEmail("STEVEN.BROWN@BRAINTRIBE.COM");
		testFindUserByInexistentEmail("steven.brown");
		testFindUserByInexistentEmail("Steven");
		testFindUserByInexistentEmail("Brown");
	}

	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#findUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	 property name/values NOT matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@code null} shall be returned
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test 
	public void testFindUserByInexistentFirstName() {
		testFindUserByInexistentFirstName(empty);
		testFindUserByInexistentFirstName("Smith");
		testFindUserByInexistentFirstName("Williams");
		testFindUserByInexistentFirstName("Taylor");
		testFindUserByInexistentFirstName("Brown");
		testFindUserByInexistentFirstName("James");
		testFindUserByInexistentFirstName("Maria");
		testFindUserByInexistentFirstName("Richard");
		testFindUserByInexistentFirstName("Lisa");
	}

	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#findUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	 property name/values NOT matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions:
	 * <ul> 
	 * 	 <li>{@code null} shall be returned
	 * 	 <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test 
	public void testFindUserByInexistentLastName() { 
		testFindUserByInexistentLastName(null);
		testFindUserByInexistentLastName(empty);
		testFindUserByInexistentLastName("John");
		testFindUserByInexistentLastName("Mary");
		testFindUserByInexistentLastName("Robert");
		testFindUserByInexistentLastName("Steven");
		testFindUserByInexistentLastName("Johnson");
		testFindUserByInexistentLastName("Davis");
		testFindUserByInexistentLastName("Wilson");
		testFindUserByInexistentLastName("Johnson");
	}

	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#retrieveUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	 property name/values NOT matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions:
	 * <ul> 
	 * 	 <li>{@link UserNotFoundException} shall be thrown
	 * </ul>
	 */
	@Test
	public void testRetrieveUserByInexistentId() {
		testRetrieveUserByInexistentId(null);
		testRetrieveUserByInexistentId(empty);
		
		testRetrieveUserByInexistentId("john.smith\t");
		testRetrieveUserByInexistentId("\tjohn.smith\t");
		testRetrieveUserByInexistentId("\tjohn.smith");
		testRetrieveUserByInexistentId("John.smith");
		testRetrieveUserByInexistentId("JOHN.SMITH");
		testRetrieveUserByInexistentId("john.smith@braintribe.com");
		testRetrieveUserByInexistentId("John");
		testRetrieveUserByInexistentId("Smith");
		
		testRetrieveUserByInexistentId("mary.williams\t\t");
		testRetrieveUserByInexistentId("\tmary.williams\t");
		testRetrieveUserByInexistentId("\tmary.williams");
		testRetrieveUserByInexistentId("Mary.williams");
		testRetrieveUserByInexistentId("MARY.WILLIAMS");
		testRetrieveUserByInexistentId("mary.williams@braintribe.com");
		testRetrieveUserByInexistentId("Mary");
		testRetrieveUserByInexistentId("Williams");
		
		testRetrieveUserByInexistentId("robert.taylor\t");
		testRetrieveUserByInexistentId("\trobert.taylor\t");
		testRetrieveUserByInexistentId("\trobert.taylor");
		testRetrieveUserByInexistentId("Robert.taylor");
		testRetrieveUserByInexistentId("ROBERT.TAYLOR");
		testRetrieveUserByInexistentId("robert.taylor@braintribe.com");
		testRetrieveUserByInexistentId("Robert");
		testRetrieveUserByInexistentId("Taylor");
		
		testRetrieveUserByInexistentId("steven.brown\t");
		testRetrieveUserByInexistentId("\tsteven.brown\t");
		testRetrieveUserByInexistentId("\tsteven.brown");
		testRetrieveUserByInexistentId("Steven.brown");
		testRetrieveUserByInexistentId("STEVEN.BROWN");
		testRetrieveUserByInexistentId("steven.brown@braintribe.com");
		testRetrieveUserByInexistentId("Steven");
		testRetrieveUserByInexistentId("Brown");
	}
	
	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#retrieveUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	 property name/values NOT matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions:
	 * <ul> 
	 * 	 <li>{@link UserNotFoundException} shall be thrown
	 * </ul>
	 */
	@Test
	public void testRetrieveUserByInexistentEmail() {
		testRetrieveUserByInexistentEmail(null);
		testRetrieveUserByInexistentEmail(empty);
		                        
		testRetrieveUserByInexistentEmail("john.smith@braintribe.com\t");
		testRetrieveUserByInexistentEmail("\tjohn.smith@braintribe.com\t");
		testRetrieveUserByInexistentEmail("\tjohn.smith@braintribe.com");
		testRetrieveUserByInexistentEmail("John.smith@braintribe.com");
		testRetrieveUserByInexistentEmail("JOHN.SMITH@BRAINTRIBE.COM");
		testRetrieveUserByInexistentEmail("john.smith");
		testRetrieveUserByInexistentEmail("John");
		testRetrieveUserByInexistentEmail("Smith");
		                        
		testRetrieveUserByInexistentEmail("mary.williams@braintribe.com\t");
		testRetrieveUserByInexistentEmail("\tmary.williams@braintribe.com\t");
		testRetrieveUserByInexistentEmail("\tmary.williams@braintribe.com");
		testRetrieveUserByInexistentEmail("Mary.williams@braintribe.com");
		testRetrieveUserByInexistentEmail("MARY.WILLIAMS@BRAINTRIBE.COM");
		testRetrieveUserByInexistentEmail("mary.williams");
		testRetrieveUserByInexistentEmail("Mary");
		testRetrieveUserByInexistentEmail("Williams");
		                        
		testRetrieveUserByInexistentEmail("robert.taylor@braintribe.com\t");
		testRetrieveUserByInexistentEmail("\trobert.taylor@braintribe.com\t");
		testRetrieveUserByInexistentEmail("\trobert.taylor@braintribe.com");
		testRetrieveUserByInexistentEmail("Robert.taylor@braintribe.com");
		testRetrieveUserByInexistentEmail("ROBERT.TAYLOR@BRAINTRIBE.COM");
		testRetrieveUserByInexistentEmail("robert.taylor");
		testRetrieveUserByInexistentEmail("Robert");
		testRetrieveUserByInexistentEmail("Taylor");
		                        
		testRetrieveUserByInexistentEmail("steven.brown@braintribe.com\t");
		testRetrieveUserByInexistentEmail("\tsteven.brown@braintribe.com\t");
		testRetrieveUserByInexistentEmail("\tsteven.brown@braintribe.com");
		testRetrieveUserByInexistentEmail("Steven.brown@braintribe.com");
		testRetrieveUserByInexistentEmail("STEVEN.BROWN@BRAINTRIBE.COM");
		testRetrieveUserByInexistentEmail("steven.brown");
		testRetrieveUserByInexistentEmail("Steven");
		testRetrieveUserByInexistentEmail("Brown");
	}
	
	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#retrieveUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	 property name/values NOT matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link UserNotFoundException} shall be thrown
	 * </ul>
	 */
	@Test 
	public void testRetrieveUserByInexistentFirstName() {
		testRetrieveUserByInexistentFirstName(empty);
		testRetrieveUserByInexistentFirstName("Smith");
		testRetrieveUserByInexistentFirstName("Williams");
		testRetrieveUserByInexistentFirstName("Taylor");
		testRetrieveUserByInexistentFirstName("Brown");
		testRetrieveUserByInexistentFirstName("James");
		testRetrieveUserByInexistentFirstName("Maria");
		testRetrieveUserByInexistentFirstName("Richard");
		testRetrieveUserByInexistentFirstName("Lisa");
	}
	
	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#retrieveUser(String, String)}
	 * 
	 * <p>
	 * Input: 
	 * 	 property name/values NOT matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link UserNotFoundException} shall be thrown
	 * </ul>
	 */
	@Test 
	public void testRetrieveUserByInexistentLastName() { 
		testRetrieveUserByInexistentLastName(null);
		testRetrieveUserByInexistentLastName(empty);
		testRetrieveUserByInexistentLastName("John");
		testRetrieveUserByInexistentLastName("Mary");
		testRetrieveUserByInexistentLastName("Robert");
		testRetrieveUserByInexistentLastName("Steven");
		testRetrieveUserByInexistentLastName("Johnson");
		testRetrieveUserByInexistentLastName("Davis");
		testRetrieveUserByInexistentLastName("Wilson");
		testRetrieveUserByInexistentLastName("Johnson");
	}
	
	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#retrieveUser(UserIdentification)}
	 * 
	 * <p>
	 * Input: 
	 * 	{@link UserNameIdentification} NOT matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link UserNotFoundException} shall be thrown
	 * </ul>
	 */
	@Test 
	public void testRetrieveUserWithInexistentUserNameIdentification() {
		testRetrieveUserWithInexistentUserNameIdentification(null);
		testRetrieveUserWithInexistentUserNameIdentification(empty);
		
		testRetrieveUserWithInexistentUserNameIdentification("john.smith\t");
		testRetrieveUserWithInexistentUserNameIdentification("\tjohn.smith\t");
		testRetrieveUserWithInexistentUserNameIdentification("\tjohn.smith");
		testRetrieveUserWithInexistentUserNameIdentification("John.smith");
		testRetrieveUserWithInexistentUserNameIdentification("JOHN.SMITH");
		testRetrieveUserWithInexistentUserNameIdentification("john.smith@braintribe.com");
		testRetrieveUserWithInexistentUserNameIdentification("John");
		testRetrieveUserWithInexistentUserNameIdentification("Smith");
		
		testRetrieveUserWithInexistentUserNameIdentification("mary.williams\t");
		testRetrieveUserWithInexistentUserNameIdentification("\tmary.williams\t");
		testRetrieveUserWithInexistentUserNameIdentification("\tmary.williams");
		testRetrieveUserWithInexistentUserNameIdentification("Mary.williams");
		testRetrieveUserWithInexistentUserNameIdentification("MARY.WILLIAMS");
		testRetrieveUserWithInexistentUserNameIdentification("mary.williams@braintribe.com");
		testRetrieveUserWithInexistentUserNameIdentification("Mary");
		testRetrieveUserWithInexistentUserNameIdentification("Williams");
		
		testRetrieveUserWithInexistentUserNameIdentification("robert.taylor\t");
		testRetrieveUserWithInexistentUserNameIdentification("\trobert.taylor\t");
		testRetrieveUserWithInexistentUserNameIdentification("\trobert.taylor");
		testRetrieveUserWithInexistentUserNameIdentification("Robert.taylor");
		testRetrieveUserWithInexistentUserNameIdentification("ROBERT.TAYLOR");
		testRetrieveUserWithInexistentUserNameIdentification("robert.taylor@braintribe.com");
		testRetrieveUserWithInexistentUserNameIdentification("Robert");
		testRetrieveUserWithInexistentUserNameIdentification("Taylor");
		
		testRetrieveUserWithInexistentUserNameIdentification("steven.brown\t");
		testRetrieveUserWithInexistentUserNameIdentification("\tsteven.brown\t");
		testRetrieveUserWithInexistentUserNameIdentification("\tsteven.brown");
		testRetrieveUserWithInexistentUserNameIdentification("Steven.brown");
		testRetrieveUserWithInexistentUserNameIdentification("STEVEN.BROWN");
		testRetrieveUserWithInexistentUserNameIdentification("steven.brown@braintribe.com");
		testRetrieveUserWithInexistentUserNameIdentification("Steven");
		testRetrieveUserWithInexistentUserNameIdentification("Brown");
	}

	/**
	 * Target: 
	 * 	{@link UserInternalServiceImpl#retrieveUser(UserIdentification)}
	 * 
	 * <p>
	 * Input: 
	 * 	{@link EmailIdentification} NOT matching existing {@link User} objects
	 * 
	 * <p>
	 * Assertions: 
	 * <ul>
	 * 	 <li>{@link UserNotFoundException} shall be thrown
	 * </ul>
	 */
	@Test 
	public void testRetrieveUserWithInexistentEmailIdentification() {
		testRetrieveUserWithInexistentEmailIdentification(null);
		testRetrieveUserWithInexistentEmailIdentification(empty);
		                        
		testRetrieveUserWithInexistentEmailIdentification("john.smith@braintribe.com\t");
		testRetrieveUserWithInexistentEmailIdentification("\tjohn.smith@braintribe.com\t");
		testRetrieveUserWithInexistentEmailIdentification("\tjohn.smith@braintribe.com");
		testRetrieveUserWithInexistentEmailIdentification("John.smith@braintribe.com");
		testRetrieveUserWithInexistentEmailIdentification("JOHN.SMITH@BRAINTRIBE.COM");
		testRetrieveUserWithInexistentEmailIdentification("john.smith");
		testRetrieveUserWithInexistentEmailIdentification("John");
		testRetrieveUserWithInexistentEmailIdentification("Smith");
		                        
		testRetrieveUserWithInexistentEmailIdentification("mary.williams@braintribe.com\t");
		testRetrieveUserWithInexistentEmailIdentification("\tmary.williams@braintribe.com\t");
		testRetrieveUserWithInexistentEmailIdentification("\tmary.williams@braintribe.com");
		testRetrieveUserWithInexistentEmailIdentification("Mary.williams@braintribe.com");
		testRetrieveUserWithInexistentEmailIdentification("MARY.WILLIAMS@BRAINTRIBE.COM");
		testRetrieveUserWithInexistentEmailIdentification("mary.williams");
		testRetrieveUserWithInexistentEmailIdentification("Mary");
		testRetrieveUserWithInexistentEmailIdentification("Williams");
		                        
		testRetrieveUserWithInexistentEmailIdentification("robert.taylor@braintribe.com\t");
		testRetrieveUserWithInexistentEmailIdentification("\trobert.taylor@braintribe.com\t");
		testRetrieveUserWithInexistentEmailIdentification("\trobert.taylor@braintribe.com");
		testRetrieveUserWithInexistentEmailIdentification("Robert.taylor@braintribe.com");
		testRetrieveUserWithInexistentEmailIdentification("ROBERT.TAYLOR@BRAINTRIBE.COM");
		testRetrieveUserWithInexistentEmailIdentification("robert.taylor");
		testRetrieveUserWithInexistentEmailIdentification("Robert");
		testRetrieveUserWithInexistentEmailIdentification("Taylor");
		                        
		testRetrieveUserWithInexistentEmailIdentification("steven.brown@braintribe.com\t");
		testRetrieveUserWithInexistentEmailIdentification("\tsteven.brown@braintribe.com\t");
		testRetrieveUserWithInexistentEmailIdentification("\tsteven.brown@braintribe.com");
		testRetrieveUserWithInexistentEmailIdentification("Steven.brown@braintribe.com");
		testRetrieveUserWithInexistentEmailIdentification("STEVEN.BROWN@BRAINTRIBE.COM");
		testRetrieveUserWithInexistentEmailIdentification("steven.brown");
		testRetrieveUserWithInexistentEmailIdentification("Steven");
		testRetrieveUserWithInexistentEmailIdentification("Brown");
	}

	private void testFindUserByName(String name) {
		testFindUser("name", name);
	}

	private void testFindUserByEmail(String email) {
		testFindUser("email", email);
	} 

	private void testFindUserByFirstName(String firstName) {
		testFindUser("firstName", firstName);
	}

	private void testFindUserByLastName(String lastName) {
		testFindUser("lastName", lastName);
	}

	private void testRetrieveUserByName(String name) {
		testRetrieveUser("name", name);
	}

	private void testRetrieveUserByEmail(String email) {
		testRetrieveUser("email", email);
	} 

	private void testRetrieveUserByFirstName(String firstName) {
		testRetrieveUser("firstName", firstName);
	}

	private void testRetrieveUserByLastName(String lastName) {
		testRetrieveUser("lastName", lastName);
	}
	
	private void testFindUserByInexistentId(String id) {
		testFindInexistentUser("id", id);
	}
	
	private void testFindUserByInexistentEmail(String email) {
		testFindInexistentUser("email", email);
	} 
	
	private void testFindUserByInexistentFirstName(String firstName) {
		testFindInexistentUser("firstName", firstName);
	}
	
	private void testFindUserByInexistentLastName(String lastName) {
		testFindInexistentUser("lastName", lastName);
	}
	
	private void testRetrieveUserByInexistentId(String id) {
		testRetrieveInexistentUser("id", id);
	}
	
	private void testRetrieveUserByInexistentEmail(String email) {
		testRetrieveInexistentUser("email", email);
	} 
	
	private void testRetrieveUserByInexistentFirstName(String firstName) {
		testRetrieveInexistentUser("firstName", firstName);
	}
	
	private void testRetrieveUserByInexistentLastName(String lastName) {
		testRetrieveInexistentUser("lastName", lastName);
	}

	private void testRetrieveUserWithUserNameIdentification(String userName) {
		this.testRetrieveUser(createUserNameIdentification(userName));
	}
	
	private void testRetrieveUserWithEmailIdentification(String email) {
		this.testRetrieveUser(createEmailIdentification(email));
	}

	private void testRetrieveUserWithInexistentUserNameIdentification(String userName) {
		this.testRetrieveInexistentUser(createUserNameIdentification(userName));
	}
	
	private void testRetrieveUserWithInexistentEmailIdentification(String email) {
		this.testRetrieveInexistentUser(createEmailIdentification(email));
	}
	
	private void testFindUser(String property, String value) {
		try {
			User user = getUserInternalService().findUser(property, value);
			Assert.assertNotNull("unexpected null user returned from findUser(String, String) call", user);
			assertEntityProperty(user, property, value);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(String.format("unexpected %s thrown during findUser(String, String) call with property: %s value: %s exception message: %s", 
					e.getClass().getSimpleName(), property, value, e.getMessage()));
		}
	}
	
	private void testFindInexistentUser(String property, String value) {
		try {
			User user = getUserInternalService().findUser(property, value);
			Assert.assertNull("unexpected user returned from findUser(String, String) call: "+user, user);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(String.format("unexpected %s thrown during findUser(String, String) call with property: %s value: %s exception message: %s", 
					e.getClass().getSimpleName(), property, value, e.getMessage()));
		}
	}
	
	private void testRetrieveUser(String property, String value) {
		try {
			User user = getUserInternalService().retrieveUser(property, value);
			Assert.assertNotNull("unexpected null user returned from retrieveUser(String, String) call", user);
			assertEntityProperty(user, property, value);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(String.format("unexpected %s thrown during findUser(String, String) call with property: %s value: %s exception message: %s", 
					e.getClass().getSimpleName(), property, value, e.getMessage()));
		}
	}
	
	private void testRetrieveUser(UserIdentification userIdentification) {
		try {
			User user = getUserInternalService().retrieveUser(userIdentification);
			Assert.assertNotNull("unexpected null user returned from retrieveUser(UserIdentification) call", user);
			
			//TODO: review this assertion.
			//assertUserIdentification(userIdentification, user);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(String.format("unexpected %s thrown during retrieveUser(UserIdentification) call with userIdentification %s. exception message: %s", 
					e.getClass().getSimpleName(), userIdentification, e.getMessage()));
		}
	}
	
	private void testRetrieveInexistentUser(String property, String value) {
		try {
			getUserInternalService().retrieveUser(property, value);
			Assert.fail(String.format("retrieveUser(String, String) call with property: %s value: %s should have failed", property, value));
		} catch (Exception e) {
			Assert.assertTrue(String.format("unexpected %s thrown during retrieveUser(String, String) call with property: %s value: %s exception message: %s", 
					e.getClass().getSimpleName(), property, value, e.getMessage()), e instanceof UserNotFoundException);
		}
	}
	
	private void testRetrieveInexistentUser(UserIdentification userIdentification) {
		try {
			getUserInternalService().retrieveUser(userIdentification);
			Assert.fail(String.format("retrieveUser(UserIdentification) call with userIdentification: %s should have failed", userIdentification));
		} catch (Exception e) {
			Assert.assertTrue(String.format("unexpected %s thrown during retrieveUser(UserIdentification) call with userIdentification: %s exception message: %s", 
					e.getClass().getSimpleName(), userIdentification, e.getMessage()), e instanceof UserNotFoundException);
		}
	}

	private UserInternalService getUserInternalService() { 
		return new UserInternalServiceImpl(context.contract().authGmSession());
	}
	
}
