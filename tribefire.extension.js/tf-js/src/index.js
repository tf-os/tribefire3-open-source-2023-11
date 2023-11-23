export class TribefireJs{
     tfjs;
     /**
      * @type {TypePackage} tbd.
      */
     types;
     /**
      * @type {TfJsMetaData} tbd.
      */
     metadata;
     /**
      * @type {TfJsLiterals} tbd.
      */
     literals;
     /**
      * @type {TFJSTC} tbd.
      */
     tc;
     constructor(config){
         this.init(config);
     }
     init(config){
         this.tfjs = parent.tribefireJs.init(config);
         this.types = this.tfjs.types;
         this.metadata = this.tfjs.metadata;
         this.tc = this.tfjs.tc;
         this.literals = this.tfjs.literals;
         return this.tfjs;
     };
     /**
      * Opens a session bound to the external incremental access.
      * @param {string} accessId - The external id of the incremental access.
      * @returns {Promise<GmSession>} The employee's department.
      */
     session(accessId){
         return this.tfjs.session(accessId);
     }
     /**
      * Parses a query out of the given string.
      * @param {string} queryString - The queryString to be parsed.
      * @returns {Query} tbd.
      */
     parse(queryString){
         return this.tfjs.parse(queryString);
     }
     /**
      * Wraps a given exception into a {TribefireJsError} for more convenience.
      * @param {Error} e - The exception to be wrapped.
      * @returns {TribefireJsError} tbd.
      */
     error(e){
         return this.tfjs.error(e);
     }    
     validateUserSession(e){
         return this.tfjs.validateUserSession(e);
     }    
     logout(e){
         return this.tfjs.logout(e);
     }
     authenticate(user, password){
          return this.tfjs.authenticate(user, password);
     }
     eval(request) {
  		return this.tfjs.eval(request);
 		}
 }
 
const $gme = wnd.$GME;
 
export class TribefireUxModuleContract extends $gme.TribefireUxModuleContract{

	constructor(){
		super();
	}
	
	/**
     * tbd.
     * @param {ComponentCreateContext} context - tbd
     * @param {JsUxComponent} denotation - tbd
     * @return {GmContentView} component - tbd
     */
	createComponent(context, denotation){};
		
}

class ComponentCreateContext {
	/**
     * 
     * @param {JsUxComponent} denotation - tbd
     * @return {GmContentView} component - tbd
     */
	createComponent(denotation){};
}

/** tbd */
export const Property = class {
	/**
	* @type {String} tbd
	*/
	name;
	/**
	* @type {Object} tbd
	*/
	defaultValue;
	/**
	* @type {GenericModelType} tbd
	*/
	type;
	/**
	* @type {EntityType} tbd
	*/
	declaringType;
	/**
	* @type {boolean} tbd
	*/
	isIdentifier;
	/**
	* @type {boolean} tbd
	*/
	isIdentifying;
	/**
	* @type {boolean} tbd
	*/
	isNullable;
	/**
	* @type {boolean} tbd
	*/
	isPartition;
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @return {Object} tbd
     */
     get(arg0){}
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @param {Object} arg1 - tbd
     * @return {void} tbd
     */
     set(arg0,arg1){}
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @return {AbsenceInformation} tbd
     */
     getAbsenceInformation(arg0){}
	/**
     * tbd.
     * @return {EntityType} tbd
     */
     getDeclaringType(){}
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @return {boolean} tbd
     */
     isAbsent(arg0){}
}
/** tbd */
export const GenericModelType = class {
	/**
	* @type {String} tbd
	*/
	typeName;
	/**
	* @type {boolean} tbd
	*/
	isEnum;
	/**
	* @type {Object} tbd
	*/
	defaultValue;
	/**
	* @type {TypeCode} tbd
	*/
	typeCode;
	/**
	* @type {boolean} tbd
	*/
	isBase;
	/**
	* @type {String} tbd
	*/
	typeSignature;
	/**
	* @type {boolean} tbd
	*/
	isSimple;
	/**
	* @type {boolean} tbd
	*/
	isEntity;
	/**
	* @type {boolean} tbd
	*/
	isVd;
	/**
	* @type {boolean} tbd
	*/
	isCollection;
	/**
	* @type {boolean} tbd
	*/
	isNumber;
	/**
	* @type {boolean} tbd
	*/
	isScaler;
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {String} tbd
     */
     getSelectiveInformation(arg0){}
}
/** tbd */
export const EntityType = class {
	/**
	* @type {boolean} tbd
	*/
	isEnum;
	/**
	* @type {boolean} tbd
	*/
	isAbstract;
	/**
	* @type {boolean} tbd
	*/
	isBase;
	/**
	* @type {String} tbd
	*/
	typeSignature;
	/**
	* @type {boolean} tbd
	*/
	isSimple;
	/**
	* @type {boolean} tbd
	*/
	isVd;
	/**
	* @type {boolean} tbd
	*/
	isCollection;
	/**
	* @type {Array<Property>} tbd
	*/
	transientProperties;
	/**
	* @type {Property} tbd
	*/
	idProperty;
	/**
	* @type {Array<Property>} tbd
	*/
	customProperties;
	/**
	* @type {Array<Property>} tbd
	*/
	declaredProperties;
	/**
	* @type {List} tbd
	*/
	superTypes;
	/**
	* @type {String} tbd
	*/
	typeName;
	/**
	* @type {Array<Property>} tbd
	*/
	properties;
	/**
     * tbd.
     * @param {string} name - tbd
     * @return {Property} tbd
     */
     getProperty(name){}
	/**
     * tbd.
     * @param {EntityType} arg0 - tbd
     * @return {boolean} tbd
     */
     isAssignableFrom(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {boolean} tbd
     */
     isInstance(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {String} tbd
     */
     getSelectiveInformation(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {boolean} tbd
     */
     isValueAssignable(arg0){}
	/**
     * tbd.
     * @param {JavaScriptObject} arg0 - tbd
     * @return {GenericEntity} tbd
     */
     create(arg0){}
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @param {Object} arg1 - tbd
     * @return {EntityReference} tbd
     */
     createReference(arg0,arg1){}
}
/** tbd */
export const TribefireJsPropertyConvenience = class {
	/**
	* @type {GenericModelType} tbd
	*/
	type;
	/**
	* @type {Object} tbd
	*/
	defaultValue;
	/**
	* @type {boolean} tbd
	*/
	isAbsent;
	/**
	* @type {boolean} tbd
	*/
	isIdentifier;
	/**
	* @type {boolean} tbd
	*/
	isIdentifying;
	/**
	* @type {boolean} tbd
	*/
	isNullable;
	/**
	* @type {boolean} tbd
	*/
	isPartition;
	/**
     * tbd.
     * @param {TraversingCriterion} arg0 - tbd
     * @return {Promise<Object>} tbd
     */
     load(arg0){}
}
/** tbd */
export const GbEntityMetaDataContextBuilder = class {
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {GbPropertyMetaDataContextBuilder} tbd
     */
     property(arg0){}
	/**
     * tbd.
     * @return {GmEntityType} tbd
     */
     getGmEntityType(){}
	/**
     * tbd.
     * @param {EntityType} arg0 - tbd
     * @return {MetaData} tbd
     */
     exclusive(arg0){}
	/**
     * tbd.
     * @param {EntityType} arg0 - tbd
     * @return {List} tbd
     */
     list(arg0){}
}
/** tbd */
export const GbModelMetaDataContextBuilder = class {
	/**
     * tbd.
     * @param {Class} arg0 - tbd
     * @return {GbEntityMetaDataContextBuilder} tbd
     */
     entityClass(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {GbEntityMetaDataContextBuilder} tbd
     */
     entityTypeSignature(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {EnumMdResolver} tbd
     */
     enumTypeSignature(arg0){}
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @return {GbEntityMetaDataContextBuilder} tbd
     */
     entity(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {GbEntityMetaDataContextBuilder} tbd
     */
     entityType(arg0){}
	/**
     * tbd.
     * @param {EntityType} arg0 - tbd
     * @return {MetaData} tbd
     */
     exclusive(arg0){}
	/**
     * tbd.
     * @param {EntityType} arg0 - tbd
     * @return {List} tbd
     */
     list(arg0){}
}
/** tbd */
export const GbPropertyMetaDataContextBuilder = class {
	/**
     * tbd.
     * @param {EntityType} arg0 - tbd
     * @return {MetaData} tbd
     */
     exclusive(arg0){}
	/**
     * tbd.
     * @param {EntityType} arg0 - tbd
     * @return {List} tbd
     */
     list(arg0){}
}
/** tbd */
export const GbMetaDataContextBuilder = class {
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {MdResolver} tbd
     */
     access(arg0){}
	/**
     * tbd.
     * @param {Class} arg0 - tbd
     * @param {Object} arg1 - tbd
     * @return {MdResolver} tbd
     */
     with(arg0,arg1){}
	/**
     * tbd.
     * @param {String[]} arg0 - tbd
     * @return {MdResolver} tbd
     */
     useCases(arg0){}
	/**
     * tbd.
     * @param {boolean} arg0 - tbd
     * @return {MdResolver} tbd
     */
     lenient(arg0){}
	/**
     * tbd.
     * @param {EntityType} arg0 - tbd
     * @return {GbMdResult} tbd
     */
     meta(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {MdResolver} tbd
     */
     useCase(arg0){}
	/**
     * tbd.
     * @param {EntityType[]} arg0 - tbd
     * @return {MdResolver} tbd
     */
     ignoreSelectorsExcept(arg0){}
	/**
     * tbd.
     * @return {MdResolver} tbd
     */
     ignoreSelectors(){}
}
/** tbd */
export const GbMdResult = class {
	/**
     * tbd.
     * @return {List} tbd
     */
     list(){}
	/**
     * tbd.
     * @return {MdDescriptor} tbd
     */
     exclusiveExtended(){}
	/**
     * tbd.
     * @return {List} tbd
     */
     listExtended(){}
	/**
     * tbd.
     * @return {MetaData} tbd
     */
     exclusive(){}
}
/** tbd */
export const GbEnumMetaDataContextBuilder = class {
	/**
     * tbd.
     * @param {EntityType} arg0 - tbd
     * @return {MetaData} tbd
     */
     exclusive(arg0){}
	/**
     * tbd.
     * @param {EntityType} arg0 - tbd
     * @return {List} tbd
     */
     list(arg0){}
	/**
     * tbd.
     * @return {GmEnumType} tbd
     */
     getEnumType(){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {GbEnumConstantMetaDataContextBuilder} tbd
     */
     constant(arg0){}
}
/** tbd */
export const GbEnumConstantMetaDataContextBuilder = class {
	/**
     * tbd.
     * @param {EntityType} arg0 - tbd
     * @return {MetaData} tbd
     */
     exclusive(arg0){}
	/**
     * tbd.
     * @param {EntityType} arg0 - tbd
     * @return {List} tbd
     */
     list(arg0){}
}
/** tbd */
export const TfJsMetaData = class {
	/**
     * tbd.
     * @param {GmMetaModel} arg0 - tbd
     * @param {GmSession} arg1 - tbd
     * @return {TribefireJsModelMetaDataEditor} tbd
     */
     editor(arg0,arg1){}
	/**
     * tbd.
     * @param {GmMetaModel} arg0 - tbd
     * @return {GbModelMetaDataContextBuilder} tbd
     */
     resolver(arg0){}
}
/** tbd */
export const TribefireJsCmdResolver = class {
	/**
	* @type {GbModelMetaDataContextBuilder} tbd
	*/
	metaData;
	/**
	* @type {TribefireJsModelOracle} tbd
	*/
	modelOracle;
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {ScalarType} tbd
     */
     getIdType(arg0){}
}
/** tbd */
export const TribefireJsResourceAccessBuilder = class {
	/**
     * tbd.
     * @param {JavaScriptObject} arg0 - tbd
     * @return {GbPromise} tbd
     */
     create(arg0){}
	/**
     * tbd.
     * @param {Object[]} arg0 - tbd
     * @return {Object} tbd
     */
     url(arg0){}
}
/** tbd */
export const TfJsNestedTransaction = class {
	/**
	* @type {List} tbd
	*/
	manipulationsDone;
	/**
	* @type {List} tbd
	*/
	manipulationsUndone;
	/**
	* @type {boolean} tbd
	*/
	canRedo;
	/**
	* @type {boolean} tbd
	*/
	canUndo;
	/**
     * tbd.
     * @return {void} tbd
     */
     commit(){}
	/**
     * tbd.
     * @return {void} tbd
     */
     rollback(){}
	/**
     * tbd.
     * @return {TfJsTransactionFrame} tbd
     */
     getParentFrame(){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @return {void} tbd
     */
     undo(arg0){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @return {void} tbd
     */
     redo(arg0){}
	/**
     * tbd.
     * @return {TfJsNestedTransaction} tbd
     */
     beginNestedTransaction(){}
	/**
     * tbd.
     * @param {TransactionFrameListener} arg0 - tbd
     * @return {void} tbd
     */
     addTransactionFrameListener(arg0){}
	/**
     * tbd.
     * @param {TransactionFrameListener} arg0 - tbd
     * @return {void} tbd
     */
     removeTransactionFrameListener(arg0){}
}
/** tbd */
export const TribefireJsModelAccessory = class {
	/**
	* @type {TribefireJsCmdResolver} tbd
	*/
	cmd;
	/**
	* @type {ManagedGmSession} tbd
	*/
	modelSession;
	/**
	* @type {TribefireJsModelOracle} tbd
	*/
	oracle;
	/**
	* @type {GbModelMetaDataContextBuilder} tbd
	*/
	metaData;
	/**
	* @type {GmMetaModel} tbd
	*/
	model;
}
/** tbd */
export const TribefireJsModelTypes = class {
	/**
	* @type {Array<EntityType>} tbd
	*/
	all;
	/**
	* @type {Array<EntityType>} tbd
	*/
	onlyDeclared;
	/**
	* @type {Array<EntityType>} tbd
	*/
	onlyInherited;
	/**
	* @type {Array<EnumType>} tbd
	*/
	onlyEnums;
	/**
	* @type {Array<EntityType>} tbd
	*/
	onlyEntities;
	/**
	* @type {Object} tbd
	*/
	asGmTypes;
	/**
	* @type {Object} tbd
	*/
	asTypeOracles;
	/**
     * tbd.
     * @param {Predicate} arg0 - tbd
     * @return {TribefireJsModelTypes} tbd
     */
     filter(arg0){}
}
/** tbd */
export const GmSession = class {
	/**
	* @type {TfJsTransaction} tbd
	*/
	transaction;
	/**
	* @type {string} tbd
	*/
	accessId;
	/**
	* @type {SessionAuthorization} tbd
	*/
	sessionAuthorization;
	/**
	* @type {String} tbd
	*/
	userId;
	/**
	* @type {String} tbd
	*/
	sessionId;
	/**
	* @type {ModelEnvironment} tbd
	*/
	modelEnviroment;
	/**
	* @type {TfJsPersistenceManipulationListenerRegistry} tbd
	*/
	listeners;
	/**
	* @type {Set} tbd
	*/
	userRoles;
	/**
	* @type {TribefireJsModelAccessory} tbd
	*/
	modelAccessory;
	/**
	* @type {TribefireJsResourceAccessBuilder} tbd
	*/
	resources;
	/**
     * tbd.
     * @param {GenericEntity} entity - tbd
     * @return {Promise<GenericEntity>} tbd
     */
     find(entity){}
	/**
     * tbd.
     * @return {void} tbd
     */
     shallowifyInstances(){}
	/**
     * tbd.
     * @return {void} tbd
     */
     suspendHistory(){}
	/**
     * tbd.
     * @return {void} tbd
     */
     resumeHistory(){}
	/**
     * tbd.
     * @param {ModelEnvironment} arg0 - tbd
     * @param {AsyncCallback} arg1 - tbd
     * @return {void} tbd
     */
     configureModelEnvironment(arg0,arg1){}
	/**
     * tbd.
     * @param {GenericEntity} entity - tbd
     * @return {Promise<GenericEntity>} tbd
     */
     refresh(entity){}
	/**
     * tbd.
     * @param {GenericEntity} entity - tbd
     * @return {Promise<GenericEntity>} tbd
     */
     require(entity){}
	/**
     * tbd.
     * @param {Query} query - tbd
     * @return {Promise<TribefireJsQueryResultConvenience_entity|TribefireJsQueryResultConvenience_property|TribefireJsQueryResultConvenience_select>} tbd
     */
     query(query){}
	/**
     * tbd.
     * @param {Query} query - tbd
     * @return {Promise<TribefireJsQueryResultConvenience_entity|TribefireJsQueryResultConvenience_property|TribefireJsQueryResultConvenience_select>} tbd
     */
     queryCache(query){}
	/**
     * tbd.
     * @param {Query} query - tbd
     * @return {Promise<TribefireJsQueryResultConvenience_entity|TribefireJsQueryResultConvenience_property|TribefireJsQueryResultConvenience_select>} tbd
     */
     queryDetached(query){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {GenericEntity} tbd
     */
     findByGlobalId(arg0){}
	/**
     * tbd.
     * @return {Promise<ManipulationResponse>} tbd
     */
     commit(){}
	/**
     * tbd.
     * @param {ServiceRequest} arg0 - tbd
     * @return {Promise<Object>} tbd
     */
     eval(arg0){}
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @return {void} tbd
     */
     attach(arg0){}
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @return {void} tbd
     */
     delete(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @param {JavaScriptObject} arg1 - tbd
     * @return {GenericEntity} tbd
     */
     create(arg0,arg1){}
}
/** tbd */
export const TribefireJsResourceUrlBuilder = class {
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {ResourceUrlBuilder} tbd
     */
     fileName(arg0){}
	/**
     * tbd.
     * @return {String} tbd
     */
     asString(){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {ResourceUrlBuilder} tbd
     */
     withFileName(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {ResourceUrlBuilder} tbd
     */
     forAccess(arg0){}
	/**
     * tbd.
     * @param {boolean} arg0 - tbd
     * @return {ResourceUrlBuilder} tbd
     */
     download(arg0){}
	/**
     * tbd.
     * @param {boolean} arg0 - tbd
     * @return {ResourceUrlBuilder} tbd
     */
     forDownload(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {ResourceUrlBuilder} tbd
     */
     withDownloadName(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {ResourceUrlBuilder} tbd
     */
     withSessionId(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {ResourceUrlBuilder} tbd
     */
     sessionId(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {ResourceUrlBuilder} tbd
     */
     accessId(arg0){}
}
/** tbd */
export const TribefireJsModelOracle = class {
	/**
	* @type {Object} tbd
	*/
	dependencies;
	/**
	* @type {GmMetaModel} tbd
	*/
	metaModel;
	/**
	* @type {TribefireJsModelTypes} tbd
	*/
	types;
	/**
	* @type {GmBaseType} tbd
	*/
	baseType;
	/**
	* @type {GmStringType} tbd
	*/
	stringType;
	/**
	* @type {GmFloatType} tbd
	*/
	floatType;
	/**
	* @type {GmDoubleType} tbd
	*/
	doubleType;
	/**
	* @type {GmBooleanType} tbd
	*/
	booleanType;
	/**
	* @type {GmIntegerType} tbd
	*/
	integerType;
	/**
	* @type {GmLongType} tbd
	*/
	longType;
	/**
	* @type {GmDateType} tbd
	*/
	dateType;
	/**
	* @type {GmDecimalType} tbd
	*/
	decimalType;
	/**
	* @type {List} tbd
	*/
	simpleTypes;
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {CustomType} tbd
     */
     findType(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {GmType} tbd
     */
     findGmType(arg0){}
}
/** tbd */
export const TfJsTransactionFrame = class {
	/**
	* @type {List} tbd
	*/
	manipulationsDone;
	/**
	* @type {List} tbd
	*/
	manipulationsUndone;
	/**
	* @type {boolean} tbd
	*/
	canRedo;
	/**
	* @type {boolean} tbd
	*/
	canUndo;
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @return {void} tbd
     */
     undo(arg0){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @return {void} tbd
     */
     redo(arg0){}
	/**
     * tbd.
     * @return {TfJsNestedTransaction} tbd
     */
     beginNestedTransaction(){}
	/**
     * tbd.
     * @param {TransactionFrameListener} arg0 - tbd
     * @return {void} tbd
     */
     addTransactionFrameListener(arg0){}
	/**
     * tbd.
     * @param {TransactionFrameListener} arg0 - tbd
     * @return {void} tbd
     */
     removeTransactionFrameListener(arg0){}
}
/** tbd */
export const TribefireJsManagedSession = class {
	/**
	* @type {TribefireJsModelAccessory} tbd
	*/
	modelAccessory;
	/**
	* @type {TribefireJsResourceAccessBuilder} tbd
	*/
	resources;
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @return {void} tbd
     */
     attach(arg0){}
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @return {void} tbd
     */
     delete(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @param {JavaScriptObject} arg1 - tbd
     * @return {GenericEntity} tbd
     */
     create(arg0,arg1){}
}
/** tbd */
export const TribefireJsModelDependencies = class {
	/**
	* @type {Array<GmMetaModel>} tbd
	*/
	all;
}
/** tbd */
export const TfJsTransaction = class {
	/**
	* @type {Set} tbd
	*/
	manipulatedProperties;
	/**
	* @type {TfJsTransactionFrame} tbd
	*/
	currentTransactionFrame;
	/**
	* @type {boolean} tbd
	*/
	hasManipulations;
	/**
	* @type {List} tbd
	*/
	manipulationsDone;
	/**
	* @type {List} tbd
	*/
	manipulationsUndone;
	/**
	* @type {boolean} tbd
	*/
	canRedo;
	/**
	* @type {boolean} tbd
	*/
	canUndo;
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @return {void} tbd
     */
     undo(arg0){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @return {void} tbd
     */
     redo(arg0){}
	/**
     * tbd.
     * @return {TfJsNestedTransaction} tbd
     */
     beginNestedTransaction(){}
	/**
     * tbd.
     * @param {TransactionFrameListener} arg0 - tbd
     * @return {void} tbd
     */
     addTransactionFrameListener(arg0){}
	/**
     * tbd.
     * @param {TransactionFrameListener} arg0 - tbd
     * @return {void} tbd
     */
     removeTransactionFrameListener(arg0){}
}
/** tbd */
export const TribefireJsQueryResultConvenience_property = class {
	/**
     * tbd.
     * @return {Object} tbd
     */
     value(){}
	/**
     * tbd.
     * @return {Object} tbd
     */
     toArray(){}
	/**
     * tbd.
     * @return {Object} tbd
     */
     first(){}
	/**
     * tbd.
     * @return {List} tbd
     */
     list(){}
	/**
     * tbd.
     * @return {PropertyQueryResult} tbd
     */
     result(){}
	/**
     * tbd.
     * @return {PropertyQuery} tbd
     */
     getQuery(){}
	/**
     * tbd.
     * @return {Object} tbd
     */
     unique(){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @param {Object} arg1 - tbd
     * @return {PropertyQueryResultConvenience} tbd
     */
     setVariable(arg0,arg1){}
	/**
     * tbd.
     * @param {TraversingCriterion} arg0 - tbd
     * @return {PropertyQueryResultConvenience} tbd
     */
     setTraversingCriterion(arg0){}
}
/** tbd */
export const TribefireJsQueryResultConvenience_select = class {
	/**
     * tbd.
     * @return {Object} tbd
     */
     value(){}
	/**
     * tbd.
     * @return {Object} tbd
     */
     toArray(){}
	/**
     * tbd.
     * @return {Object} tbd
     */
     first(){}
	/**
     * tbd.
     * @return {List} tbd
     */
     list(){}
	/**
     * tbd.
     * @return {SelectQueryResult} tbd
     */
     result(){}
	/**
     * tbd.
     * @return {SelectQuery} tbd
     */
     getQuery(){}
	/**
     * tbd.
     * @return {Object} tbd
     */
     unique(){}
}
/** tbd */
export const TribefireJsQueryResultConvenience_entity = class {
	/**
     * tbd.
     * @return {Object} tbd
     */
     value(){}
	/**
     * tbd.
     * @return {Object} tbd
     */
     toArray(){}
	/**
     * tbd.
     * @return {Object} tbd
     */
     first(){}
	/**
     * tbd.
     * @return {List} tbd
     */
     list(){}
	/**
     * tbd.
     * @return {EntityQueryResult} tbd
     */
     result(){}
	/**
     * tbd.
     * @return {EntityQuery} tbd
     */
     getQuery(){}
	/**
     * tbd.
     * @return {Object} tbd
     */
     unique(){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @param {Object} arg1 - tbd
     * @return {EntityQueryResultConvenience} tbd
     */
     setVariable(arg0,arg1){}
	/**
     * tbd.
     * @param {TraversingCriterion} arg0 - tbd
     * @return {EntityQueryResultConvenience} tbd
     */
     setTraversingCriterion(arg0){}
}
/** tbd */
export const $Q = class {
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {Query} tbd
     */
     parse(arg0){}
}
/** tbd */
export const TfJsManipulationReport = class {
	/**
     * tbd.
     * @return {ManipulationResponse} tbd
     */
     getManipulationResponse(){}
	/**
     * tbd.
     * @return {Map} tbd
     */
     getInstantiations(){}
	/**
     * tbd.
     * @return {Map} tbd
     */
     getLenientManifestations(){}
}
/** tbd */
export const TfJsPersistenceManipulationListenerRegistry = class {
	/**
     * tbd.
     * @param {GenericEntity} entity - tbd
     * @param {string} propertyName - tbd
     * @param {Function} listener - tbd
     * @return {ManipulationListener} tbd
     */
     addManipulationLister(entity,propertyName,listener){}
	/**
     * tbd.
     * @param {ManipulationListener} arg0 - tbd
     * @return {void} tbd
     */
     removeManipulationLister(arg0){}
}
/** tbd */
export const TfJsCallbacks = class {
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @param {Object} arg1 - tbd
     * @return {JsCommitListenerAdapter} tbd
     */
     commit(arg0,arg1){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {JsManipulationListener} tbd
     */
     manipulation(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @param {Object} arg1 - tbd
     * @return {JsAsyncCallback} tbd
     */
     asyncCallback(arg0,arg1){}
}
/** tbd */
export const TfJsConfigSessionIdProvider = class {
	/**
     * tbd.
     * @return {String} tbd
     */
     get(){}
}
/** tbd */
export const TfJsCookieSessionIdProvider = class {
	/**
     * tbd.
     * @return {String} tbd
     */
     get(){}
}
/** tbd */
export const JsManipulationListener = class {
	/**
     * tbd.
     * @param {Manipulation} arg0 - tbd
     * @return {void} tbd
     */
     noticeManipulation(arg0){}
}
/** tbd */
export const JsAsyncCallback = class {
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {void} tbd
     */
     onSuccess(arg0){}
	/**
     * tbd.
     * @param {Throwable} arg0 - tbd
     * @return {void} tbd
     */
     onFailure(arg0){}
}
/** tbd */
export const GbCollectionTypes = class {
	/**
     * tbd.
     * @param {GenericModelType} arg0 - tbd
     * @return {ListType} tbd
     */
     list(arg0){}
	/**
     * tbd.
     * @param {GenericModelType} arg0 - tbd
     * @return {SetType} tbd
     */
     set(arg0){}
	/**
     * tbd.
     * @param {GenericModelType} arg0 - tbd
     * @param {GenericModelType} arg1 - tbd
     * @return {MapType} tbd
     */
     map(arg0,arg1){}
}
/** tbd */
export const TypePackage = class {
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {GenericModelType} tbd
     */
     get(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {EntityType} tbd
     */
     getEntityType(arg0){}
}
/** tbd */
export const GbEnumTypeImpl = class {
	/**
	* @type {Object} tbd
	*/
	defaultValue;
	/**
	* @type {String} tbd
	*/
	typeSignature;
	/**
	* @type {Enum[]} tbd
	*/
	enumValues;
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {Enum} tbd
     */
     getInstance(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {Enum} tbd
     */
     value(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {Enum} tbd
     */
     getEnumValue(arg0){}
}
/** tbd */
export const TFJSTC = class {
	/**
     * tbd.
     * @param {GenericModelType} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     root(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     map(arg0){}
	/**
     * tbd.
     * @return {TraversingCriterion} tbd
     */
     all(){}
	/**
     * tbd.
     * @param {TraversingCriterion} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     criterion(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     propertyType(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     entity(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     property(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     mapValue(arg0){}
	/**
     * tbd.
     * @return {TraversingCriterion} tbd
     */
     standard(){}
	/**
     * tbd.
     * @return {TraversingCriterion} tbd
     */
     joker(){}
	/**
     * tbd.
     * @param {TraversingCriterion} arg0 - tbd
     * @return {NegationCriterion} tbd
     */
     negation(arg0){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @param {int} arg1 - tbd
     * @return {CriterionBuilder} tbd
     */
     recursion(arg0,arg1){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     mapKey(arg0){}
	/**
     * tbd.
     * @param {TraversingCriterion[]} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     disjunction(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     listElement(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     setElement(arg0){}
	/**
     * tbd.
     * @param {TraversingCriterion[]} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     conjunction(arg0){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @param {Object} arg1 - tbd
     * @return {TraversingCriterion} tbd
     */
     propertyWithType(arg0,arg1){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @param {ComparisonOperator} arg1 - tbd
     * @param {Object} arg2 - tbd
     * @return {TraversingCriterion} tbd
     */
     valueCondition(arg0,arg1,arg2){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     level(arg0){}
	/**
     * tbd.
     * @param {TraversingCriterion[]} arg0 - tbd
     * @return {TraversingCriterion} tbd
     */
     pattern(arg0){}
}
/** tbd */
export const TribefireJsEvaluator = class {
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @return {EvalContext} tbd
     */
     eval(arg0){}
}
/** tbd */
export const TribefireJsEvalContext = class {
	/**
     * tbd.
     * @param {AsyncCallback} arg0 - tbd
     * @return {void} tbd
     */
     get(arg0){}
	/**
     * tbd.
     * @return {Object} tbd
     */
     getSyncron(){}
}
/** tbd */
export const TfJsLiterals = class {
	/**
     * tbd.
     * @param {Object[]} arg0 - tbd
     * @return {List} tbd
     */
     list(arg0){}
	/**
     * tbd.
     * @param {Object[]} arg0 - tbd
     * @return {Set} tbd
     */
     set(arg0){}
	/**
     * tbd.
     * @param {Object[]} arg0 - tbd
     * @return {Object} tbd
     */
     locale(arg0){}
	/**
     * tbd.
     * @param {Object[]} arg0 - tbd
     * @return {Map} tbd
     */
     map(arg0){}
	/**
     * tbd.
     * @param {Object[]} arg0 - tbd
     * @return {Object} tbd
     */
     defaultLocale(arg0){}
	/**
     * tbd.
     * @return {Date} tbd
     */
     now(){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @return {Integer} tbd
     */
     integer(arg0){}
	/**
     * tbd.
     * @param {float} arg0 - tbd
     * @return {Float} tbd
     */
     float(arg0){}
	/**
     * tbd.
     * @param {double} arg0 - tbd
     * @return {Double} tbd
     */
     double(arg0){}
	/**
     * tbd.
     * @param {double} arg0 - tbd
     * @return {BigDecimal} tbd
     */
     decimal(arg0){}
	/**
     * tbd.
     * @param {JsDate} arg0 - tbd
     * @return {Date} tbd
     */
     fromJsDate(arg0){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @param {int} arg1 - tbd
     * @param {int} arg2 - tbd
     * @param {int} arg3 - tbd
     * @param {int} arg4 - tbd
     * @param {int} arg5 - tbd
     * @return {Date} tbd
     */
     datetime(arg0,arg1,arg2,arg3,arg4,arg5){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @param {int} arg1 - tbd
     * @param {int} arg2 - tbd
     * @return {Date} tbd
     */
     date(arg0,arg1,arg2){}
	/**
     * tbd.
     * @param {String} arg0 - tbd
     * @param {String} arg1 - tbd
     * @return {Date} tbd
     */
     parseDate(arg0,arg1){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @param {int} arg1 - tbd
     * @param {int} arg2 - tbd
     * @return {Date} tbd
     */
     time(arg0,arg1,arg2){}
	/**
     * tbd.
     * @param {Date} arg0 - tbd
     * @param {String} arg1 - tbd
     * @return {String} tbd
     */
     printDate(arg0,arg1){}
	/**
     * tbd.
     * @param {GenericModelType} arg0 - tbd
     * @param {Object[]} arg1 - tbd
     * @return {List} tbd
     */
     typedList(arg0,arg1){}
	/**
     * tbd.
     * @param {GenericModelType} arg0 - tbd
     * @param {Object[]} arg1 - tbd
     * @return {Set} tbd
     */
     typedSet(arg0,arg1){}
	/**
     * tbd.
     * @param {GenericModelType} arg0 - tbd
     * @param {GenericModelType} arg1 - tbd
     * @param {Object[]} arg2 - tbd
     * @return {Map} tbd
     */
     typedMap(arg0,arg1,arg2){}
	/**
     * tbd.
     * @param {double} arg0 - tbd
     * @return {Long} tbd
     */
     long(arg0){}
}
/** tbd */
export const GbNumberListWrapper = class {
	/**
     * tbd.
     * @param {Number} arg0 - tbd
     * @return {boolean} tbd
     */
     add(arg0){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @param {Number} arg1 - tbd
     * @return {void} tbd
     */
     addAtIndex(arg0,arg1){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @return {boolean} tbd
     */
     removeObject(arg0){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @return {Number} tbd
     */
     removeAt(arg0){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @param {Number} arg1 - tbd
     * @return {Number} tbd
     */
     set(arg0,arg1){}
	/**
     * tbd.
     * @param {int} arg0 - tbd
     * @return {ListIterator} tbd
     */
     indexedListIterator(arg0){}
}
/** tbd */
export const GbNumberSetWrapper = class {
	/**
     * tbd.
     * @param {Number} arg0 - tbd
     * @return {boolean} tbd
     */
     add(arg0){}
}
/** tbd */
export const GrindleboneEnhancedEntityStub = class {
	/**
	* @type {long} tbd
	*/
	$runtimeId;
	/**
	* @type {EntityReference} tbd
	*/
	$reference;
	/**
	* @type {EntityType} tbd
	*/
	$type;
	/**
	* @type {GmSession} tbd
	*/
	$session;
	/**
	* @type {String} tbd
	*/
	$desc;
	/**
	* @type {void} tbd
	*/
	$session;
	/**
     * tbd.
     * @param {Evaluator} arg0 - tbd
     * @return {EvalContext} tbd
     */
     eval(arg0){}
	/**
     * tbd.
     * @return {GmSession} tbd
     */
     detach(){}
}
/** tbd */
export const TribefireJsError = class {
	/**
	* @type {Throwable} tbd
	*/
	cause;
	/**
	* @type {String} tbd
	*/
	simpleName;
	/**
	* @type {String} tbd
	*/
	name;
	/**
	* @type {String} tbd
	*/
	msg;
	/**
	* @type {String} tbd
	*/
	localizedMessage;
	/**
	* @type {StackTraceElement[]} tbd
	*/
	stackTrace;
	/**
     * tbd.
     * @return {void} tbd
     */
     printStackTrace(){}
	/**
     * tbd.
     * @return {void} tbd
     */
     log(){}
}
/** tbd */
export const TribefireJsBasicSession = class {
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @return {void} tbd
     */
     attach(arg0){}
	/**
     * tbd.
     * @param {GenericEntity} arg0 - tbd
     * @return {void} tbd
     */
     delete(arg0){}
	/**
     * tbd.
     * @param {Object} arg0 - tbd
     * @param {JavaScriptObject} arg1 - tbd
     * @return {GenericEntity} tbd
     */
     create(arg0,arg1){}
}
/** com.braintribe.model.generic.GenericEntity */
export const GenericEntity = class {
	/**
	* @type {string} More infos will follow soon.
	*/
	globalId;
	/**
	* @type {TribefireJsPropertyConvenience} More infos will follow soon.
	*/
	$globalId;
	/**
	* @type {object} More infos will follow soon.
	*/
	id;
	/**
	* @type {TribefireJsPropertyConvenience} More infos will follow soon.
	*/
	$id;
	/**
	* @type {string} More infos will follow soon.
	*/
	partition;
	/**
	* @type {TribefireJsPropertyConvenience} More infos will follow soon.
	*/
	$partition;
	/**
	* @type {TribefireJsPropertyConvenience} More infos will follow soon.
	*/
	$reflect;
	/**
	* @type {string} More infos will follow soon.
	*/
	$desc;
	/**
	* @type {EntityType} More infos will follow soon.
	*/
	$type;
	/**
	* @type {EntityReference} More infos will follow soon.
	*/
	$reference;
	/**
	* @type {GmSession} More infos will follow soon.
	*/
	$session;
}
/**
 * @returns {GenericEntity}
 */
export function genericEntity(obj){
     return obj;
}