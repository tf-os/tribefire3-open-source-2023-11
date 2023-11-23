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
package com.braintribe.testing.model.test.technical.features.multipleinheritance;

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * An entity that has references to all other entities from this package, which are used to test multiple inheritance.
 *
 * @author michael.lafite
 */

public interface MultipleInheritanceEntity extends GenericEntity {

	EntityType<MultipleInheritanceEntity> T = EntityTypes.T(MultipleInheritanceEntity.class);

	A getA();
	void setA(A a);

	B getB();
	void setB(B b);

	C getC();
	void setC(C c);

	D getD();
	void setD(D d);

	E getE();
	void setE(E e);

	F getF();
	void setF(F f);

	F1 getF1();
	void setF1(F1 f1);

	F2 getF2();
	void setF2(F2 f2);

	ABC getAbc();
	void setAbc(ABC abc);

	AB getAb();
	void setAb(AB ab);

	BC getBc();
	void setBc(BC bc);

	AB_BC_but_not_ABC getAb_BC_but_not_ABC();
	void setAb_BC_but_not_ABC(AB_BC_but_not_ABC ab_BC_but_not_ABC);

	AB_BC_ABC getAb_BC_ABC();
	void setAb_BC_ABC(AB_BC_ABC ab_BC_ABC);

	ABCDEF getAbcdef();
	void setAbcdef(ABCDEF abcdef);

	ExtendedABCDEF getExtendedABCDEF();
	void setExtendedABCDEF(ExtendedABCDEF extendedABCDEF);

	FurtherExtendedABCDEF getFurtherExtendedABCDEF();
	void setFurtherExtendedABCDEF(FurtherExtendedABCDEF furtherExtendedABCDEF);

	List<AB> getAbList();
	void setAbList(List<AB> abList);

	Set<ABCDEF> getAbcdefSet();
	void setAbcdefSet(Set<ABCDEF> abcdefSet);
}
