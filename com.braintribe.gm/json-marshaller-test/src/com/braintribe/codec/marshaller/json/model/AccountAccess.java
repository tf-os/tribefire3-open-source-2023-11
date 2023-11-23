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
package com.braintribe.codec.marshaller.json.model;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface AccountAccess extends GenericEntity {
	
	EntityType<AccountAccess> T = EntityTypes.T(AccountAccess.class);
	
	String 	getAvailableAccounts();
	void   	setAvailableAccounts(String value);
	
	String 	getAllPsd2();
	void 	setAllPsd2(String value);
	
	String 	getAvailableAccountsWithBalances();
	void	setAvailableAccountsWithBalances(String value);
	
	List<AccountReference> getBalances();
	void 	setBalances(List<AccountReference> value);
	
	List<AccountReference> getAccounts();
	void 	setAccounts(List<AccountReference> value);
	
	List<AccountReference> getTransactions();
	void 	setTransactions(List<AccountReference> value);

}
