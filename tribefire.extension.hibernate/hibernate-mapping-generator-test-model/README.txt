
-----------------------------------------------------------------------------------------------------------------------
-- MULTIPLE -- com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.multiple.* ------------------
-----------------------------------------------------------------------------------------------------------------------

        GE                | Multiple inheritance scenario. G, H and E explicitly extends multiple entities.
	    |                 | 
	    SI                | G and H shall be elected as top level hibernate classes, due to instantiability
		|                 | (leaf nodes).
		A                 | 
	   / \                | G and H mappings must include all the properties from the super types alongside
	  B   C               | with its own properties.
	 / \ / \              | 
	D   E   F             | 
	|___|___|             | 
	  |   |               | 
	  G   H               | 

		                   
-----------------------------------------------------------------------------------------------------------------------
-- REDUNDANT -- com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.redundant.* ----------------
-----------------------------------------------------------------------------------------------------------------------


        GE                | Exactly like 'multiple', but every entity declares all the transitive dependencies 
	    |                 | redundantly.
	    SI                |    
		|                 | e.g.: the entity H, instead of extending D, E and F, also 
		A                 |       extends explicitly every other indirect super types:
	   / \                |       B, C, A, SI, GE
	  B   C               | 
	 / \ / \              | G and H shall be elected as top level hibernate classes, due to instantiability
	D   E   F             | (leaf nodes).
	|___|___|             | 
	  |   |               | G and H mappings must include all the properties from the super types alongside
	  G   H               | with its own properties.

		                   
-----------------------------------------------------------------------------------------------------------------------
-- MERGEABLE -- com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.mergeable.* ----
-----------------------------------------------------------------------------------------------------------------------

        GE                | B and C are elected as top level, due to the references to them in X and Y respectively.
	    |                 | 
	    SI                | But B and C cannot be top levels, as E would participate in both hierarchies.   
		|                 | 
		A                 | Thus A, being the common super type of both B and C, shall be elected as the top level.
	   / \                | 
 X<>--B   C--<>Y          | 
	 / \ / \              | 
	D   E   F             | 
	|___|___|             | 
	  |   |               | 
	  G   H               | 

		                  
-----------------------------------------------------------------------------------------------------------------------
-- AMBIGUOUS -- com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlapping.ambiguous.* ----
-----------------------------------------------------------------------------------------------------------------------


          GE              | Similar to 'mergeable', but instead of just one common type, B and C share 4 common types: 
     _____|_____	      | A1, A2, A3 and A4.
     |  |   |  |          | 
     A1 A2  A3 A4         | Only one shall be elected the top level.
    / \_|___|__/          | 
   A	|   |             | A1 is discarded, as it would bring to the hibernate hierarchy a new entity (A)
        |   |             | 
   X<>--B   C--<>Y        | Between A2, A3 and A4, A3 will be chosen, as it contains the larger number of properties.
   	   / \ / \            | 
   	  D   E   F           | 
   	  |___|___|           | 
   	    |   |             | 
   	    G   H             | 


	  
	  