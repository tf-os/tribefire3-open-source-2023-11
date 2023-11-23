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
package com.braintribe.model.processing.deployment.hibernate.testmodel.dbnaming;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface InvalidDbNamesEntity extends StandardIdentifiable {

	EntityType<InvalidDbNamesEntity> T = EntityTypes.T(InvalidDbNamesEntity.class);

	// @formatter:off
	String getA();
	void setA(String a);

	String getAbort();
	void setAbort(String abort);

	String getAbs();
	void setAbs(String abs);

	String getAbsolute();
	void setAbsolute(String absolute);

	String getAccess();
	void setAccess(String access);

	String getAction();
	void setAction(String action);

	String getAda();
	void setAda(String ada);

	String getAdd();
	void setAdd(String add);

	String getAdmin();
	void setAdmin(String admin);

	String getAfter();
	void setAfter(String after);

	String getAggregate();
	void setAggregate(String aggregate);

	String getAlias();
	void setAlias(String alias);

	String getAll();
	void setAll(String all);

	String getAllocate();
	void setAllocate(String allocate);

	String getAlso();
	void setAlso(String also);

	String getAlter();
	void setAlter(String alter);

	String getAlways();
	void setAlways(String always);

	String getAnalyse();
	void setAnalyse(String analyse);

	String getAnalyze();
	void setAnalyze(String analyze);

	String getAnd();
	void setAnd(String and);

	String getAny();
	void setAny(String any);

	String getAre();
	void setAre(String are);

	String getArray();
	void setArray(String array);

	String getAs();
	void setAs(String as);

	String getAsc();
	void setAsc(String asc);

	String getAsensitive();
	void setAsensitive(String asensitive);

	String getAssertion();
	void setAssertion(String assertion);

	String getAssignment();
	void setAssignment(String assignment);

	String getAsymmetric();
	void setAsymmetric(String asymmetric);

	String getAt();
	void setAt(String at);

	String getAtomic();
	void setAtomic(String atomic);

	String getAttribute();
	void setAttribute(String attribute);

	String getAttributes();
	void setAttributes(String attributes);

	String getAudit();
	void setAudit(String audit);

	String getAuthorization();
	void setAuthorization(String authorization);

	String getAuto_increment();
	void setAuto_increment(String auto_increment);

	String getAvg();
	void setAvg(String avg);

	String getAvg_row_length();
	void setAvg_row_length(String avg_row_length);

	String getBackup();
	void setBackup(String backup);

	String getBackward();
	void setBackward(String backward);

	String getBefore();
	void setBefore(String before);

	String getBegin();
	void setBegin(String begin);

	String getBernoulli();
	void setBernoulli(String bernoulli);

	String getBetween();
	void setBetween(String between);

	String getBigint();
	void setBigint(String bigint);

	String getBinary();
	void setBinary(String binary);

	String getBit();
	void setBit(String bit);

	String getBit_length();
	void setBit_length(String bit_length);

	String getBitvar();
	void setBitvar(String bitvar);

	String getBlob();
	void setBlob(String blob);

	String getBool();
	void setBool(String bool);

	String getBoolean();
	void setBoolean(String _boolean);

	String getBoth();
	void setBoth(String both);

	String getBreadth();
	void setBreadth(String breadth);

	String getBreak();
	void setBreak(String _break);

	String getBrowse();
	void setBrowse(String browse);

	String getBulk();
	void setBulk(String bulk);

	String getBy();
	void setBy(String by);

	String getC();
	void setC(String c);

	String getCache();
	void setCache(String cache);

	String getCall();
	void setCall(String call);

//  String getCalled() ;
//	void setCalled(String called) ;
//
//	String getCardinality() ;
//	void setCardinality(String cardinality) ;

	String getCascade();
	void setCascade(String cascade);

//  String getCascaded();
//	void setCascaded(String cascaded);

	String getCase();
	void setCase(String _case);

	String getCast();
	void setCast(String cast);

//	String getCatalog();
//	void setCatalog(String catalog);
//
//	String getCatalog_name();
//	void setCatalog_name(String catalog_name);

	String getCeil();
	void setCeil(String ceil);

//	String getCeiling();
//	void setCeiling(String ceiling);
//
//	String getChain();
//	void setChain(String chain);
//
//	String getChange();
//	void setChange(String change);

	String getChar();
	void setChar(String _char);

	String getChar_length();
	void setChar_length(String char_length);

	String getCharacter();
	void setCharacter(String character);
	
//	String getCharacter_length();
//	void setCharacter_length(String character_length);
//
//	String getCharacter_set_catalog();
//	void setCharacter_set_catalog(String character_set_catalog);
//
//	String getCharacter_set_name();
//	void setCharacter_set_name(String character_set_name);
//
//	String getCharacter_set_schema();
//	void setCharacter_set_schema(String character_set_schema);
//
//	String getCharacteristics();
//	void setCharacteristics(String characteristics);
//
//	String getCharacters();
//	void setCharacters(String characters);	
	
	String getCheck();
	void setCheck(String check);

	String getChecked();
	void setChecked(String checked);

	String getCheckpoint();
	void setCheckpoint(String checkpoint);

	String getChecksum();
	void setChecksum(String checksum);
	
//	String get_class();
//	void set_class(String _class);
//
//	String getClass_origin();
//	void setClass_origin(String class_origin);
	
	String getClob();
	void setClob(String clob);

//	String getClose();
//	void setClose(String close);
//
//	String getCluster();
//	void setCluster(String cluster);
//
//	String getClustered();
//	void setClustered(String clustered);
//
//	String getCoalesce();
//	void setCoalesce(String coalesce);
//
//	String getCobol();
//	void setCobol(String cobol);
//
//	String getCollate();
//	void setCollate(String collate);
//
//	String getCollation();
//	void setCollation(String collation);
//
//	String getCollation_catalog();
//	void setCollation_catalog(String collation_catalog);
//
//	String getCollation_name();
//	void setCollation_name(String collation_name);
//
//	String getCollation_schema();
//	void setCollation_schema(String collation_schema);
//
//	String getCollect();
//	void setCollect(String collect);
	
	String getColumn_name();
	void setColumn_name(String column_name);

	String getColumns();
	void setColumns(String columns);

//	String getCommand_function();
//	void setCommand_function(String command_function);
//
//	String getCommand_function_code();
//	void setCommand_function_code(String command_function_code);
//
//	String getComment();
//	void setComment(String comment);

	String getCommit();
	void setCommit(String commit);

//	String getCommitted();
//	void setCommitted(String committed);
//
//	String getCompletion();
//	void setCompletion(String completion);
//
//	String getCompress();
//	void setCompress(String compress);
//
//	String getCompute();
//	void setCompute(String compute);
//
//	String getCondition();
//	void setCondition(String condition);
//
//	String getCondition_number();
//	void setCondition_number(String condition_number);
//
//	String getConnect();
//	void setConnect(String connect);
//
//	String getConnection();
//	void setConnection(String connection);
//
//	String getConnection_name();
//	void setConnection_name(String connection_name);
//
//	String getConstraint();
//	void setConstraint(String constraint);
//
//	String getConstraint_catalog();
//	void setConstraint_catalog(String constraint_catalog);
//
//	String getConstraint_name();
//	void setConstraint_name(String constraint_name);
//
//	String getConstraint_schema();
//	void setConstraint_schema(String constraint_schema);
//
//	String getConstraints();
//	void setConstraints(String constraints);
//
//	String getConstructor();
//	void setConstructor(String constructor);
//
//	String getContains();
//	void setContains(String contains);
//
//	String getContainstable();
//	void setContainstable(String containstable);
//
//	String get_continue();
//	void set_continue(String _continue);
//
//	String getConversion();
//	void setConversion(String conversion);
//
//	String getConvert();
//	void setConvert(String convert);
//
//	String getCopy();
//	void setCopy(String copy);
//
//	String getCorr();
//	void setCorr(String corr);
//
//	String getCorresponding();
//	void setCorresponding(String corresponding);
	
	String getCount();
	void setCount(String count);

	// String getCovar_pop() ;
	// void setCovar_pop(String covar_pop) ;
	// String getCovar_samp() ;
	// void setCovar_samp(String covar_samp) ;
	String getCreate();
	void setCreate(String create);

	// String getCreatedb() ;
	// void setCreatedb(String createdb) ;
	// String getCreaterole() ;
	// void setCreaterole(String createrole) ;
	// String getCreateuser() ;
	// void setCreateuser(String createuser) ;
	// String getCross() ;
	// void setCross(String cross) ;
	// String getCsv() ;
	// void setCsv(String csv) ;
	// String getCube() ;
	// void setCube(String cube) ;
	// String getCume_dist() ;
	// void setCume_dist(String cume_dist) ;
	String getCurrent();
	void setCurrent(String current);

	String getCurrent_date();
	void setCurrent_date(String current_date);

	// String getCurrent_default_transform_group() ;
	// void setCurrent_default_transform_group(
	// String current_default_transform_group) ;
	String getCurrent_path();
	void setCurrent_path(String current_path);

	String getCurrent_role();
	void setCurrent_role(String current_role);

	String getCurrent_time();
	void setCurrent_time(String current_time);

	String getCurrent_timestamp();
	void setCurrent_timestamp(String current_timestamp);

	// String getCurrent_transform_group_for_type() ;
	// void setCurrent_transform_group_for_type(
	// String current_transform_group_for_type) ;
	String getCurrent_user();
	void setCurrent_user(String current_user);

	// String getCursor() ;
	// void setCursor(String cursor) ;
	// String getCursor_name() ;
	// void setCursor_name(String cursor_name) ;
	// String getCycle() ;
	// void setCycle(String cycle) ;
	String getData();
	void setData(String data);

	String getDatabase();
	void setDatabase(String database);

	String getDatabases();
	void setDatabases(String databases);

	String getDate();
	void setDate(String date);

	String getDatetime();
	void setDatetime(String datetime);

	// String getDatetime_interval_code() ;
	// void setDatetime_interval_code(String datetime_interval_code) ;
	// String getDatetime_interval_precision() ;
	// void setDatetime_interval_precision(String datetime_interval_precision) ;
	// String getDay() ;
	// void setDay(String day) ;
	// String getDay_hour() ;
	// void setDay_hour(String day_hour) ;
	// String getDay_microsecond() ;
	// void setDay_microsecond(String day_microsecond) ;
	// String getDay_minute() ;
	// void setDay_minute(String day_minute) ;
	// String getDay_second() ;
	// void setDay_second(String day_second) ;
	// String getDayofmonth() ;
	// void setDayofmonth(String dayofmonth) ;
	// String getDayofweek() ;
	// void setDayofweek(String dayofweek) ;
	// String getDayofyear() ;
	// void setDayofyear(String dayofyear) ;
	// String getDbcc() ;
	// void setDbcc(String dbcc) ;
	// String getDeallocate() ;
	// void setDeallocate(String deallocate) ;
	// String getDec() ;
	// void setDec(String dec) ;
	String getDecimal();
	void setDecimal(String decimal);

	// String getDeclare() ;
	// void setDeclare(String declare) ;
	// String get_default() ;
	// void set_default(String _default) ;
	// String getDefaults() ;
	// void setDefaults(String defaults) ;
	// String getDeferrable() ;
	// void setDeferrable(String deferrable) ;
	// String getDeferred() ;
	// void setDeferred(String deferred) ;
	// String getDefined() ;
	// void setDefined(String defined) ;
	// String getDefiner() ;
	// void setDefiner(String definer) ;
	// String getDegree() ;
	// void setDegree(String degree) ;
	// String getDelay_key_write() ;
	// void setDelay_key_write(String delay_key_write) ;
	// String getDelayed() ;
	// void setDelayed(String delayed) ;
	// String getDelete() ;
	// void setDelete(String delete) ;
	// String getDelimiter() ;
	// void setDelimiter(String delimiter) ;
	// String getDelimiters() ;
	// void setDelimiters(String delimiters) ;
	// String getDense_rank() ;
	// void setDense_rank(String dense_rank) ;
	// String getDeny() ;
	// void setDeny(String deny) ;
	// String getDepth() ;
	// void setDepth(String depth) ;
	// String getDeref() ;
	// void setDeref(String deref) ;
	// String getDerived() ;
	// void setDerived(String derived) ;
	String getDesc();
	void setDesc(String desc);

	// String getDescribe() ;
	// void setDescribe(String describe) ;
	// String getDescriptor() ;
	// void setDescriptor(String descriptor) ;
	// String getDestroy() ;
	// void setDestroy(String destroy) ;
	// String getDestructor() ;
	// void setDestructor(String destructor) ;
	// String getDeterministic() ;
	// void setDeterministic(String deterministic) ;
	// String getDiagnostics() ;
	// void setDiagnostics(String diagnostics) ;
	// String getDictionary() ;
	// void setDictionary(String dictionary) ;
	// String getDisable() ;
	// void setDisable(String disable) ;
	// String getDisconnect() ;
	// void setDisconnect(String disconnect) ;
	// String getDisk() ;
	// void setDisk(String disk) ;
	// String getDispatch() ;
	// void setDispatch(String dispatch) ;
	String getDistinct();
	void setDistinct(String distinct);

	// String getDistinctrow() ;
	// void setDistinctrow(String distinctrow) ;
	// String getDistributed() ;
	// void setDistributed(String distributed) ;
	// String getDiv() ;
	// void setDiv(String div) ;
	// String get_do() ;
	// void set_do(String _do) ;
	// String getDomain() ;
	// void setDomain(String domain) ;
	// String get_double() ;
	// void set_double(String _double) ;
	String getDrop();
	void setDrop(String drop);

	String getDual();
	void setDual(String dual);

	// String getDummy() ;
	// void setDummy(String dummy) ;
	// String getDump() ;
	// void setDump(String dump) ;
	// String getDynamic() ;
	// void setDynamic(String dynamic) ;
	// String getDynamic_function() ;
	// void setDynamic_function(String dynamic_function) ;
	// String getDynamic_function_code() ;
	// void setDynamic_function_code(String dynamic_function_code) ;
	// String getEach() ;
	// void setEach(String each) ;
	// String getElement() ;
	// void setElement(String element) ;
	// String get_else() ;
	// void set_else(String _else) ;
	// String getElseif() ;
	// void setElseif(String elseif) ;
	String getEnable();
	void setEnable(String enable);

	String getEnclosed();
	void setEnclosed(String enclosed);

	String getEncoding();
	void setEncoding(String encoding);

	String getEncrypted();
	void setEncrypted(String encrypted);

	// String get_end() ;
	// void set_end(String _end) ;
	// String get_endexec() ;
	// void set_endexec(String _endexec) ;
	// String get_enum() ;
	// void set_enum(String _enum) ;
	String getEquals();
	void setEquals(String equals);

	String getErrlvl();
	void setErrlvl(String errlvl);

	String getEscape();
	void setEscape(String escape);

	String getEscaped();
	void setEscaped(String escaped);

	String getEvery();
	void setEvery(String every);

	String getExcept();
	void setExcept(String except);

	String getException();
	void setException(String exception);

	// String getExclude() ;
	// void setExclude(String exclude) ;
	// String getExcluding() ;
	// void setExcluding(String excluding) ;
	// String getExclusive() ;
	// void setExclusive(String exclusive) ;
	String getExec();
	void setExec(String exec);

	String getExecute();
	void setExecute(String execute);

	String getExisting();
	void setExisting(String existing);

	String getExists();
	void setExists(String exists);

	String getExit();
	void setExit(String exit);

	String getExp();
	void setExp(String exp);

	String getExplain();
	void setExplain(String explain);

	// String getExternal() ;
	// void setExternal(String external) ;
	// String getExtract() ;
	// void setExtract(String extract) ;
	// String get_false() ;
	// void set_false(String _false) ;
	// String getFetch() ;
	// void setFetch(String fetch) ;
	// String getFields() ;
	// void setFields(String fields) ;
	// String getFile() ;
	// void setFile(String file) ;
	// String getFillfactor() ;
	// void setFillfactor(String fillfactor) ;
	// String getFilter() ;
	// void setFilter(String filter) ;
	// String get_final() ;
	// void set_final(String _final) ;
	// String getFirst() ;
	// void setFirst(String first) ;
	// String get_float() ;
	// void set_float(String _float) ;
	// String getFloat4() ;
	// void setFloat4(String float4) ;
	// String getFloat8() ;
	// void setFloat8(String float8) ;
	// String getFloor() ;
	// void setFloor(String floor) ;
	// String getFlush() ;
	// void setFlush(String flush) ;
	// String getFollowing() ;
	// void setFollowing(String following) ;
	// String get_for() ;
	// void set_for(String _for) ;
	// String getForce() ;
	// void setForce(String force) ;
	// String getForeign() ;
	// void setForeign(String foreign) ;
	// String getFortran() ;
	// void setFortran(String fortran) ;
	// String getForward() ;
	// void setForward(String forward) ;
	// String getFound() ;
	// void setFound(String found) ;
	// String getFree() ;
	// void setFree(String free) ;
	// String getFreetext() ;
	// void setFreetext(String freetext) ;
	// String getFreetexttable() ;
	// void setFreetexttable(String freetexttable) ;
	// String getFreeze() ;
	// void setFreeze(String freeze) ;
	String getFrom();
	void setFrom(String from);

	// String getFull() ;
	// void setFull(String full) ;
	String getFulltext();
	void setFulltext(String fulltext);

	String getFunction();
	void setFunction(String function);

	// String getFusion() ;
	// void setFusion(String fusion) ;
	// String getG() ;
	// void setG(String g) ;
	// String getGeneral() ;
	// void setGeneral(String general) ;
	// String getGenerated() ;
	// void setGenerated(String generated) ;
	// String getGet() ;
	// void setGet(String get) ;
	// String getGlobal() ;
	// void setGlobal(String global) ;
	// String getGo() ;
	// void setGo(String go) ;
	// String get_goto() ;
	// void set_goto(String _goto) ;
	String getGrant();
	void setGrant(String grant);

	// String getGranted() ;
	// void setGranted(String granted) ;
	// String getGrants() ;
	// void setGrants(String grants) ;
	// String getGreatest() ;
	// void setGreatest(String greatest) ;
	String getGroup();
	void setGroup(String group);

	// String getGrouping() ;
	// void setGrouping(String grouping) ;
	// String getHandler() ;
	// void setHandler(String handler) ;
	String getHaving();
	void setHaving(String having);

	// String getHeader() ;
	// void setHeader(String header) ;
	// String getHeap() ;
	// void setHeap(String heap) ;
	// String getHierarchy() ;
	// void setHierarchy(String hierarchy) ;
	// String getHigh_priority() ;
	// void setHigh_priority(String high_priority) ;
	// String getHold() ;
	// void setHold(String hold) ;
	// String getHoldlock() ;
	// void setHoldlock(String holdlock) ;
	// String getHost() ;
	// void setHost(String host) ;
	// String getHosts() ;
	// void setHosts(String hosts) ;
	// String getHour() ;
	// void setHour(String hour) ;
	// String getHour_microsecond() ;
	// void setHour_microsecond(String hour_microsecond) ;
	// String getHour_minute() ;
	// void setHour_minute(String hour_minute) ;
	// String getHour_second() ;
	// void setHour_second(String hour_second) ;
	// String getIdentified() ;
	// void setIdentified(String identified) ;
	// String getIdentity() ;
	// void setIdentity(String identity) ;
	// String getIdentity_insert() ;
	// void setIdentity_insert(String identity_insert) ;
	// String getIdentitycol() ;
	// void setIdentitycol(String identitycol) ;
	// String get_if() ;
	// void set_if(String _if) ;
	// String getIgnore() ;
	// void setIgnore(String ignore) ;
	String getIlike();
	void setIlike(String ilike);

	// String getImmediate() ;
	// void setImmediate(String immediate) ;
	// String getImmutable() ;
	// void setImmutable(String immutable) ;
	// String getImplementation() ;
	// void setImplementation(String implementation) ;
	// String getImplicit() ;
	// void setImplicit(String implicit) ;
	// String getIn() ;
	// void setIn(String in) ;
	// String getInclude() ;
	// void setInclude(String include) ;
	// String getIncluding() ;
	// void setIncluding(String including) ;
	// String getIncrement() ;
	// void setIncrement(String increment) ;
	String getIndex();
	void setIndex(String index);

	// String getIndicator() ;
	// void setIndicator(String indicator) ;
	// String getInfile() ;
	// void setInfile(String infile) ;
	// String getInfix() ;
	// void setInfix(String infix) ;
	// String getInherit() ;
	// void setInherit(String inherit) ;
	// String getInherits() ;
	// void setInherits(String inherits) ;
	// String getInitial() ;
	// void setInitial(String initial) ;
	// String getInitialize() ;
	// void setInitialize(String initialize) ;
	// String getInitially() ;
	// void setInitially(String initially) ;
	String getInner();
	void setInner(String inner);

	// String getInout() ;
	// void setInout(String inout) ;
	// String getInput() ;
	// void setInput(String input) ;
	// String getInsensitive() ;
	// void setInsensitive(String insensitive) ;
	String getInsert();
	void setInsert(String insert);

	// String getInsert_id() ;
	// void setInsert_id(String insert_id) ;
	// String getInstance() ;
	// void setInstance(String instance) ;
	// String getInstantiable() ;
	// void setInstantiable(String instantiable) ;
	// String getInstead() ;
	// void setInstead(String instead) ;
	// String get_int() ;
	// void set_int(String _int) ;
	// String getInt_() ;
	// void setInt_(String int_) ;
	// String getInt1() ;
	// void setInt1(String int1) ;
	// String getInt2() ;
	// void setInt2(String int2) ;
	// String getInt3() ;
	// void setInt3(String int3) ;
	// String getInt4() ;
	// void setInt4(String int4) ;
	// String getInt8() ;
	// void setInt8(String int8) ;
	// String getInteger() ;
	// void setInteger(String integer) ;
	// String getIntersect() ;
	// void setIntersect(String intersect) ;
	// String getIntersection() ;
	// void setIntersection(String intersection) ;
	// String getInterval() ;
	// void setInterval(String interval) ;
	// String getInto() ;
	// void setInto(String into) ;
	// String getInvoker() ;
	// void setInvoker(String invoker) ;
	// String getIs() ;
	// void setIs(String is) ;
	// String getIsam() ;
	// void setIsam(String isam) ;
	// String getIsnull() ;
	// void setIsnull(String isnull) ;
	// String getIsolation() ;
	// void setIsolation(String isolation) ;
	// String getIterate() ;
	// void setIterate(String iterate) ;
	String getJoin();
	void setJoin(String join);

	// String getK() ;
	// void setK(String k) ;
	// String getKey() ;
	// void setKey(String key) ;
	// String getKey_member() ;
	// void setKey_member(String key_member) ;
	// String getKey_type() ;
	// void setKey_type(String key_type) ;
	// String getKeys() ;
	// void setKeys(String keys) ;
	// String getKill() ;
	// void setKill(String kill) ;
	// String getLancompiler() ;
	// void setLancompiler(String lancompiler) ;
	// String getLanguage() ;
	// void setLanguage(String language) ;
	// String getLarge() ;
	// void setLarge(String large) ;
	// String getLast() ;
	// void setLast(String last) ;
	// String getLast_insert_id() ;
	// void setLast_insert_id(String last_insert_id) ;
	// String getLateral() ;
	// void setLateral(String lateral) ;
	// String getLeading() ;
	// void setLeading(String leading) ;
	// String getLeast() ;
	// void setLeast(String least) ;
	// String getLeave() ;
	// void setLeave(String leave) ;
	// String getLeft() ;
	// void setLeft(String left) ;
	// String getLength() ;
	// void setLength(String length) ;
	// String getLess() ;
	// void setLess(String less) ;
	// String getLevel() ;
	// void setLevel(String level) ;
	// String getLike() ;
	// void setLike(String like) ;
	// String getLimit() ;
	// void setLimit(String limit) ;
	// String getLineno() ;
	// void setLineno(String lineno) ;
	// String getLines() ;
	// void setLines(String lines) ;
	// String getListen() ;
	// void setListen(String listen) ;
	// String getLn() ;
	// void setLn(String ln) ;
	// String getLoad() ;
	// void setLoad(String load) ;
	// String getLocal() ;
	// void setLocal(String local) ;
	// String getLocaltime() ;
	// void setLocaltime(String localtime) ;
	// String getLocaltimestamp() ;
	// void setLocaltimestamp(String localtimestamp) ;
	// String getLocation() ;
	// void setLocation(String location) ;
	// String getLocator() ;
	// void setLocator(String locator) ;
	// String getLock() ;
	// void setLock(String lock) ;
	// String getLogin() ;
	// void setLogin(String login) ;
	// String getLogs() ;
	// void setLogs(String logs) ;
	// String get_long() ;
	// void set_long(String _long) ;
	// String getLongblob() ;
	// void setLongblob(String longblob) ;
	// String getLongtext() ;
	// void setLongtext(String longtext) ;
	// String getLoop() ;
	// void setLoop(String loop) ;
	// String getLow_priority() ;
	// void setLow_priority(String low_priority) ;
	// String getLower() ;
	// void setLower(String lower) ;
	// String getM() ;
	// void setM(String m) ;
	// String getMap() ;
	// void setMap(String map) ;
	// String getMatch() ;
	// void setMatch(String match) ;
	// String getMatched() ;
	// void setMatched(String matched) ;
	// String getMax() ;
	// void setMax(String max) ;
	// String getMax_rows() ;
	// void setMax_rows(String max_rows) ;
	// String getMaxextents() ;
	// void setMaxextents(String maxextents) ;
	// String getMaxvalue() ;
	// void setMaxvalue(String maxvalue) ;
	// String getMediumblob() ;
	// void setMediumblob(String mediumblob) ;
	// String getMediumint() ;
	// void setMediumint(String mediumint) ;
	// String getMediumtext() ;
	// void setMediumtext(String mediumtext) ;
	// String getMember() ;
	// void setMember(String member) ;
	// String getMerge() ;
	// void setMerge(String merge) ;
	// String getMessage_length() ;
	// void setMessage_length(String message_length) ;
	// String getMessage_octet_length() ;
	// void setMessage_octet_length(String message_octet_length) ;
	// String getMessage_text() ;
	// void setMessage_text(String message_text) ;
	// String getMethod() ;
	// void setMethod(String method) ;
	// String getMiddleint() ;
	// void setMiddleint(String middleint) ;
	// String getMin() ;
	// void setMin(String min) ;
	// String getMin_rows() ;
	// void setMin_rows(String min_rows) ;
	// String getMinus() ;
	// void setMinus(String minus) ;
	// String getMinute() ;
	// void setMinute(String minute) ;
	// String getMinute_microsecond() ;
	// void setMinute_microsecond(String minute_microsecond) ;
	// String getMinute_second() ;
	// void setMinute_second(String minute_second) ;
	// String getMinvalue() ;
	// void setMinvalue(String minvalue) ;
	// String getMlslabel() ;
	// void setMlslabel(String mlslabel) ;
	// String getMod() ;
	// void setMod(String mod) ;
	// String getMode() ;
	// void setMode(String mode) ;
	// String getModifies() ;
	// void setModifies(String modifies) ;
	// String getModify() ;
	// void setModify(String modify) ;
	// String getModule() ;
	// void setModule(String module) ;
	// String getMonth() ;
	// void setMonth(String month) ;
	// String getMonthname() ;
	// void setMonthname(String monthname) ;
	// String getMore() ;
	// void setMore(String more) ;
	// String getMove() ;
	// void setMove(String move) ;
	// String getMultiset() ;
	// void setMultiset(String multiset) ;
	// String getMumps() ;
	// void setMumps(String mumps) ;
	// String getMyisam() ;
	// void setMyisam(String myisam) ;
	// String getName() ;
	// void setName(String name) ;
	// String getNames() ;
	// void setNames(String names) ;
	// String getNational() ;
	// void setNational(String national) ;
	// String getNatural() ;
	// void setNatural(String natural) ;
	String getNchar();
	void setNchar(String nchar);

	String getNclob();
	void setNclob(String nclob);

	// String getNesting() ;
	// void setNesting(String nesting) ;
	// String get_new() ;
	// void set_new(String _new) ;
	// String getNext() ;
	// void setNext(String next) ;
	// String getNo() ;
	// void setNo(String no) ;
	// String getNo_write_to_binlog() ;
	// void setNo_write_to_binlog(String no_write_to_binlog) ;
	// String getNoaudit() ;
	// void setNoaudit(String noaudit) ;
	// String getNocheck() ;
	// void setNocheck(String nocheck) ;
	// String getNocompress() ;
	// void setNocompress(String nocompress) ;
	// String getNocreatedb() ;
	// void setNocreatedb(String nocreatedb) ;
	// String getNocreaterole() ;
	// void setNocreaterole(String nocreaterole) ;
	// String getNocreateuser() ;
	// void setNocreateuser(String nocreateuser) ;
	// String getNoinherit() ;
	// void setNoinherit(String noinherit) ;
	// String getNologin() ;
	// void setNologin(String nologin) ;
	// String getNonclustered() ;
	// void setNonclustered(String nonclustered) ;
	// String getNone() ;
	// void setNone(String none) ;
	// String getNormalize() ;
	// void setNormalize(String normalize) ;
	// String getNormalized() ;
	// void setNormalized(String normalized) ;
	// String getNosuperuser() ;
	// void setNosuperuser(String nosuperuser) ;

	String getNot();
	void setNot(String not);

	// String getNothing() ;
	// void setNothing(String nothing) ;
	// String getNotify() ;
	// void setNotify(String notify) ;
	// String getNotnull() ;
	// void setNotnull(String notnull) ;
	// String getNowait() ;
	// void setNowait(String nowait) ;
	// String get_null() ;
	// void set_null(String _null) ;

	String getNullable();
	void setNullable(String nullable);

	// String getNullif() ;
	// void setNullif(String nullif) ;
	// String getNulls() ;
	// void setNulls(String nulls) ;

	String getNumber();
	void setNumber(String number);

	String getNumeric();
	void setNumeric(String numeric);

	String getObject();
	void setObject(String object);

	// String getOctet_length() ;
	// void setOctet_length(String octet_length) ;
	// String getOctets() ;
	// void setOctets(String octets) ;
	// String getOf() ;
	// void setOf(String of) ;
	// String getOff() ;
	// void setOff(String off) ;
	// String getOffline() ;
	// void setOffline(String offline) ;
	// String getOffset() ;
	// void setOffset(String offset) ;
	// String getOffsets() ;
	// void setOffsets(String offsets) ;
	// String getOids() ;
	// void setOids(String oids) ;
	// String getOld() ;
	// void setOld(String old) ;
	// String getOn() ;
	// void setOn(String on) ;
	// String getOnline() ;
	// void setOnline(String online) ;
	// String getOnly() ;
	// void setOnly(String only) ;
	// String getOpen() ;
	// void setOpen(String open) ;
	// String getOpendatasource() ;
	// void setOpendatasource(String opendatasource) ;
	// String getOpenquery() ;
	// void setOpenquery(String openquery) ;
	// String getOpenrowset() ;
	// void setOpenrowset(String openrowset) ;
	// String getOpenxml() ;
	// void setOpenxml(String openxml) ;
	// String getOperation() ;
	// void setOperation(String operation) ;
	// String getOperator() ;
	// void setOperator(String operator) ;
	// String getOptimize() ;
	// void setOptimize(String optimize) ;
	// String getOption() ;
	// void setOption(String option) ;
	// String getOptionally() ;
	// void setOptionally(String optionally) ;
	// String getOptions() ;
	// void setOptions(String options) ;
	// String getOr() ;
	// void setOr(String or) ;
	// String getOrder() ;
	// void setOrder(String order) ;
	// String getOrdering() ;
	// void setOrdering(String ordering) ;
	// String getOrdinality() ;
	// void setOrdinality(String ordinality) ;
	// String getOthers() ;
	// void setOthers(String others) ;
	// String getOut() ;
	// void setOut(String out) ;
	// String getOuter() ;
	// void setOuter(String outer) ;
	// String getOutfile() ;
	// void setOutfile(String outfile) ;
	// String getOutput() ;
	// void setOutput(String output) ;
	// String getOver() ;
	// void setOver(String over) ;
	// String getOverlaps() ;
	// void setOverlaps(String overlaps) ;
	// String getOverlay() ;
	// void setOverlay(String overlay) ;
	// String getOverriding() ;
	// void setOverriding(String overriding) ;
	// String getOwner() ;
	// void setOwner(String owner) ;
	// String getPack_keys() ;
	// void setPack_keys(String pack_keys) ;
	// String getPad() ;
	// void setPad(String pad) ;
	// String getParameter() ;
	// void setParameter(String parameter) ;
	// String getParameter_mode() ;
	// void setParameter_mode(String parameter_mode) ;
	// String getParameter_name() ;
	// void setParameter_name(String parameter_name) ;
	// String getParameter_ordinal_position() ;
	// void setParameter_ordinal_position(String parameter_ordinal_position) ;
	// String getParameter_specific_catalog() ;
	// void setParameter_specific_catalog(String parameter_specific_catalog) ;
	// String getParameter_specific_name() ;
	// void setParameter_specific_name(String parameter_specific_name) ;
	// String getParameter_specific_schema() ;
	// void setParameter_specific_schema(String parameter_specific_schema) ;
	// String getParameters() ;
	// void setParameters(String parameters) ;
	// String getPartial() ;
	// void setPartial(String partial) ;
	// String getPartition() ;
	// void setPartition(String partition) ;
	// String getPascal() ;
	// void setPascal(String pascal) ;
	// String getPassword() ;
	// void setPassword(String password) ;
	// String getPath() ;
	// void setPath(String path) ;
	// String getPctfree() ;
	// void setPctfree(String pctfree) ;
	// String getPercent() ;
	// void setPercent(String percent) ;
	// String getPercent_rank() ;
	// void setPercent_rank(String percent_rank) ;
	// String getPercentile_cont() ;
	// void setPercentile_cont(String percentile_cont) ;
	// String getPercentile_disc() ;
	// void setPercentile_disc(String percentile_disc) ;
	// String getPlacing() ;
	// void setPlacing(String placing) ;

	String getPlan();
	void setPlan(String plan);

	// String getPli() ;
	// void setPli(String pli) ;
	// String getPosition() ;
	// void setPosition(String position) ;
	// String getPostfix() ;
	// void setPostfix(String postfix) ;
	// String getPower() ;
	// void setPower(String power) ;
	// String getPreceding() ;
	// void setPreceding(String preceding) ;
	// String getPrecision() ;
	// void setPrecision(String precision) ;
	// String getPrefix() ;
	// void setPrefix(String prefix) ;
	// String getPreorder() ;
	// void setPreorder(String preorder) ;
	// String getPrepare() ;
	// void setPrepare(String prepare) ;
	// String getPrepared() ;
	// void setPrepared(String prepared) ;
	// String getPreserve() ;
	// void setPreserve(String preserve) ;
	// String getPrimary() ;
	// void setPrimary(String primary) ;
	// String getPrint() ;
	// void setPrint(String print) ;
	// String getPrior() ;
	// void setPrior(String prior) ;
	// String getPrivileges() ;
	// void setPrivileges(String privileges) ;
	// String getProc() ;
	// void setProc(String proc) ;
	// String getProcedural() ;
	// void setProcedural(String procedural) ;
	String getProcedure();
	void setProcedure(String procedure);

	// String getProcess() ;
	// void setProcess(String process) ;
	// String getProcesslist() ;
	// void setProcesslist(String processlist) ;
	// String get_() ;
	// void set_(String _) ;
	// String getPurge() ;
	// void setPurge(String purge) ;
	// String getQuote() ;
	// void setQuote(String quote) ;
	// String getRaid0() ;
	// void setRaid0(String raid0) ;
	// String getRaiserror() ;
	// void setRaiserror(String raiserror) ;
	// String getRange() ;
	// void setRange(String range) ;
	// String getRank() ;
	// void setRank(String rank) ;
	// String getRaw() ;
	// void setRaw(String raw) ;
	String getRead();
	void setRead(String read);

	// String getReads() ;
	// void setReads(String reads) ;
	// String getReadtext() ;
	// void setReadtext(String readtext) ;
	// String getReal() ;
	// void setReal(String real) ;
	// String getRecheck() ;
	// void setRecheck(String recheck) ;
	// String getReconfigure() ;
	// void setReconfigure(String reconfigure) ;
	// String getRecursive() ;
	// void setRecursive(String recursive) ;
	// String getRef() ;
	// void setRef(String ref) ;
	// String getReferences() ;
	// void setReferences(String references) ;
	// String getReferencing() ;
	// void setReferencing(String referencing) ;
	// String getRegexp() ;
	// void setRegexp(String regexp) ;
	// String getRegr_avgx() ;
	// void setRegr_avgx(String regr_avgx) ;
	// String getRegr_avgy() ;
	// void setRegr_avgy(String regr_avgy) ;
	// String getRegr_count() ;
	// void setRegr_count(String regr_count) ;
	// String getRegr_intercept() ;
	// void setRegr_intercept(String regr_intercept) ;
	// String getRegr_r2() ;
	// void setRegr_r2(String regr_r2) ;
	// String getRegr_slope() ;
	// void setRegr_slope(String regr_slope) ;
	// String getRegr_sxx() ;
	// void setRegr_sxx(String regr_sxx) ;
	// String getRegr_sxy() ;
	// void setRegr_sxy(String regr_sxy) ;
	// String getRegr_syy() ;
	// void setRegr_syy(String regr_syy) ;
	// String getReindex() ;
	// void setReindex(String reindex) ;
	// String getRelative() ;
	// void setRelative(String relative) ;
	// String getRelease() ;
	// void setRelease(String release) ;
	// String getReload() ;
	// void setReload(String reload) ;
	// String getRename() ;
	// void setRename(String rename) ;
	// String getRepeat() ;
	// void setRepeat(String repeat) ;
	// String getRepeatable() ;
	// void setRepeatable(String repeatable) ;
	// String getReplace() ;
	// void setReplace(String replace) ;
	// String getReplication() ;
	// void setReplication(String replication) ;
	// String getRequire() ;
	// void setRequire(String require) ;
	// String getReset() ;
	// void setReset(String reset) ;
	// String getResignal() ;
	// void setResignal(String resignal) ;
	// String getResource() ;
	// void setResource(String resource) ;
	// String getRestart() ;
	// void setRestart(String restart) ;
	// String getRestore() ;
	// void setRestore(String restore) ;
	// String getRestrict() ;
	// void setRestrict(String restrict) ;

	String getResult();
	void setResult(String result);

	// String get_return() ;
	// void set_return(String _return) ;
	// String getReturned_cardinality() ;
	// void setReturned_cardinality(String returned_cardinality) ;
	// String getReturned_length() ;
	// void setReturned_length(String returned_length) ;
	// String getReturned_octet_length() ;
	// void setReturned_octet_length(String returned_octet_length) ;
	// String getReturned_sqlstate() ;
	// void setReturned_sqlstate(String returned_sqlstate) ;
	// String getReturns() ;
	// void setReturns(String returns) ;
	// String getRevoke() ;
	// void setRevoke(String revoke) ;
	// String getRight() ;
	// void setRight(String right) ;
	// String getRlike() ;
	// void setRlike(String rlike) ;

	String getRole();
	void setRole(String role);

	String getRollback();
	void setRollback(String rollback);

	// String getRollup() ;
	// void setRollup(String rollup) ;
	// String getRoutine() ;
	// void setRoutine(String routine) ;
	// String getRoutine_catalog() ;
	// void setRoutine_catalog(String routine_catalog) ;
	// String getRoutine_name() ;
	// void setRoutine_name(String routine_name) ;
	// String getRoutine_schema() ;
	// void setRoutine_schema(String routine_schema) ;

	String getRow();
	void setRow(String row);

	String getRow_count();
	void setRow_count(String row_count);

	String getRow_number();
	void setRow_number(String row_number);

	String getRowcount();
	void setRowcount(String rowcount);

	String getRowguidcol();
	void setRowguidcol(String rowguidcol);

	String getRowid();
	void setRowid(String rowid);

	String getRownum();
	void setRownum(String rownum);

	String getRows();
	void setRows(String rows);

	// String getRule() ;
	// void setRule(String rule) ;

	String getSave();
	void setSave(String save);

	String getSavepoint();
	void setSavepoint(String savepoint);

	// String getScale() ;
	// void setScale(String scale) ;

	String getSchema();
	void setSchema(String schema);

	// String getSchema_name() ;
	// void setSchema_name(String schema_name) ;
	// String getSchemas() ;
	// void setSchemas(String schemas) ;
	// String getScope() ;
	// void setScope(String scope) ;
	// String getScope_catalog() ;
	// void setScope_catalog(String scope_catalog) ;
	// String getScope_name() ;
	// void setScope_name(String scope_name) ;
	// String getScope_schema() ;
	// void setScope_schema(String scope_schema) ;
	// String getScroll() ;
	// void setScroll(String scroll) ;

	String getSearch();
	void setSearch(String search);

	String getSecond();
	void setSecond(String second);

	// String getSecond_microsecond() ;
	// void setSecond_microsecond(String second_microsecond) ;
	// String getSection() ;
	// void setSection(String section) ;
	// String getSecurity() ;
	// void setSecurity(String security) ;

	String getSelect();
	void setSelect(String select);

	// String getSelf() ;
	// void setSelf(String self) ;
	// String getSensitive() ;
	// void setSensitive(String sensitive) ;
	// String getSeparator() ;
	// void setSeparator(String separator) ;

	String getSequence();
	void setSequence(String sequence);

	// String getSerializable() ;
	// void setSerializable(String serializable) ;
	// String getServer_name() ;
	// void setServer_name(String server_name) ;

	String getSession();
	void setSession(String session);

	String getSession_user();
	void setSession_user(String session_user);

	String getSet();
	void setSet(String set);

	// String getSetof() ;
	// void setSetof(String setof) ;
	// String getSets() ;
	// void setSets(String sets) ;
	// String getSetuser() ;
	// void setSetuser(String setuser) ;
	// String getShare() ;
	// void setShare(String share) ;
	// String getShow() ;
	// void setShow(String show) ;
	// String getShutdown() ;
	// void setShutdown(String shutdown) ;
	// String getSignal() ;
	// void setSignal(String signal) ;
	// String getSimilar() ;
	// void setSimilar(String similar) ;
	// String getSimple() ;
	// void setSimple(String simple) ;
	// String getSize() ;
	// void setSize(String size) ;
	// String getSmallint() ;
	// void setSmallint(String smallint) ;
	// String getSome() ;
	// void setSome(String some) ;
	// String getSoname() ;
	// void setSoname(String soname) ;
	// String getSource() ;
	// void setSource(String source) ;
	// String getSpace() ;
	// void setSpace(String space) ;
	// String getSpatial() ;
	// void setSpatial(String spatial) ;
	// String getSpecific() ;
	// void setSpecific(String specific) ;
	// String getSpecific_name() ;
	// void setSpecific_name(String specific_name) ;
	// String getSpecifictype() ;
	// void setSpecifictype(String specifictype) ;

	String getSql();
	void setSql(String sql);

	// String getSql_big_result() ;
	// void setSql_big_result(String sql_big_result) ;
	// String getSql_big_selects() ;
	// void setSql_big_selects(String sql_big_selects) ;
	// String getSql_big_tables() ;
	// void setSql_big_tables(String sql_big_tables) ;
	// String getSql_calc_found_rows() ;
	// void setSql_calc_found_rows(String sql_calc_found_rows) ;
	// String getSql_log_off() ;
	// void setSql_log_off(String sql_log_off) ;
	// String getSql_log_update() ;
	// void setSql_log_update(String sql_log_update) ;
	// String getSql_low_priority_updates() ;
	// void setSql_low_priority_updates(String sql_low_priority_updates) ;
	// String getSql_select_limit() ;
	// void setSql_select_limit(String sql_select_limit) ;
	// String getSql_small_result() ;
	// void setSql_small_result(String sql_small_result) ;
	// String getSql_warnings() ;
	// void setSql_warnings(String sql_warnings) ;

	String getSqlca();
	void setSqlca(String sqlca);

	String getSqlcode();
	void setSqlcode(String sqlcode);

	String getSqlerror();
	void setSqlerror(String sqlerror);

	// String getSqlexception() ;
	// void setSqlexception(String sqlexception) ;
	// String getSqlstate() ;
	// void setSqlstate(String sqlstate) ;
	// String getSqlwarning() ;
	// void setSqlwarning(String sqlwarning) ;
	// String getSqrt() ;
	// void setSqrt(String sqrt) ;
	// String getSsl() ;
	// void setSsl(String ssl) ;
	// String getStable() ;
	// void setStable(String stable) ;
	// String getStart() ;
	// void setStart(String start) ;
	// String getStarting() ;
	// void setStarting(String starting) ;
	// String getState() ;
	// void setState(String state) ;

	String getStatement();
	void setStatement(String statement);

	// String get_static() ;
	// void set_static(String _static) ;
	// String getStatistics() ;
	// void setStatistics(String statistics) ;
	// String getStatus() ;
	// void setStatus(String status) ;
	// String getStddev_pop() ;
	// void setStddev_pop(String stddev_pop) ;
	// String getStddev_samp() ;
	// void setStddev_samp(String stddev_samp) ;
	// String getStdin() ;
	// void setStdin(String stdin) ;
	// String getStdout() ;
	// void setStdout(String stdout) ;
	// String getStorage() ;
	// void setStorage(String storage) ;
	// String getStraight_join() ;
	// void setStraight_join(String straight_join) ;
	// String getStrict() ;
	// void setStrict(String strict) ;
	String getString();
	void setString(String string);

	// String getStructure() ;
	// void setStructure(String structure) ;
	// String getStyle() ;
	// void setStyle(String style) ;
	// String getSubclass_origin() ;
	// void setSubclass_origin(String subclass_origin) ;
	// String getSublist() ;
	// void setSublist(String sublist) ;
	// String getSubmultiset() ;
	// void setSubmultiset(String submultiset) ;
	String getSubstring();
	void setSubstring(String substring);

	String getSuccessful();
	void setSuccessful(String successful);

	String getSum();
	void setSum(String sum);

	// String getSuperuser() ;
	// void setSuperuser(String superuser) ;
	// String getSymmetric() ;
	// void setSymmetric(String symmetric) ;
	String getSynonym();
	void setSynonym(String synonym);

	String getSysdate();
	void setSysdate(String sysdate);

	String getSysid();
	void setSysid(String sysid);

	String getSystem();
	void setSystem(String system);

	String getSystem_user();
	void setSystem_user(String system_user);

	String getTable();
	void setTable(String table);

	String getTable_name();
	void setTable_name(String table_name);

	String getTables();
	void setTables(String tables);

	// String getTablesample() ;
	// void setTablesample(String tablesample) ;
	String getTablespace();
	void setTablespace(String tablespace);

	String getTemp();
	void setTemp(String temp);

	String getTemplate();
	void setTemplate(String template);

	String getTemporary();
	void setTemporary(String temporary);

	// String getTerminate() ;
	// void setTerminate(String terminate) ;
	// String getTerminated() ;
	// void setTerminated(String terminated) ;
	String getText();
	void setText(String text);

	// String getTextsize() ;
	// void setTextsize(String textsize) ;
	// String getThan() ;
	// void setThan(String than) ;
	// String getThen() ;
	// void setThen(String then) ;
	// String getTies() ;
	// void setTies(String ties) ;
	String getTime();
	void setTime(String time);

	String getTimestamp();
	void setTimestamp(String timestamp);

	// String getTimezone_hour() ;
	// void setTimezone_hour(String timezone_hour) ;
	// String getTimezone_minute() ;
	// void setTimezone_minute(String timezone_minute) ;
	String getTinyblob();
	void setTinyblob(String tinyblob);

	String getTinyint();
	void setTinyint(String tinyint);

	String getTinytext();
	void setTinytext(String tinytext);

	// String getTo() ;
	// void setTo(String to) ;
	// String getToast() ;
	// void setToast(String toast) ;
	// String getTop() ;
	// void setTop(String top) ;
	// String getTop_level_count() ;
	// void setTop_level_count(String top_level_count) ;
	// String getTrailing() ;
	// void setTrailing(String trailing) ;
	// String getTran() ;
	// void setTran(String tran) ;
	// String getTransaction() ;
	// void setTransaction(String transaction) ;
	// String getTransaction_active() ;
	// void setTransaction_active(String transaction_active) ;
	// String getTransactions_committed() ;
	// void setTransactions_committed(String transactions_committed) ;
	// String getTransactions_rolled_back() ;
	// void setTransactions_rolled_back(String transactions_rolled_back) ;
	// String getTransform() ;
	// void setTransform(String transform) ;
	// String getTransforms() ;
	// void setTransforms(String transforms) ;
	// String getTranslate() ;
	// void setTranslate(String translate) ;
	// String getTranslation() ;
	// void setTranslation(String translation) ;
	// String getTreat() ;
	// void setTreat(String treat) ;
	String getTrigger();
	void setTrigger(String trigger);

	// String getTrigger_catalog() ;
	// void setTrigger_catalog(String trigger_catalog) ;
	// String getTrigger_name() ;
	// void setTrigger_name(String trigger_name) ;
	// String getTrigger_schema() ;
	// void setTrigger_schema(String trigger_schema) ;
	// String getTrim() ;
	// void setTrim(String trim) ;
	// String get_true() ;
	// void set_true(String _true) ;
	// String getTruncate() ;
	// void setTruncate(String truncate) ;
	// String getTrusted() ;
	// void setTrusted(String trusted) ;
	// String getTsequal() ;
	// void setTsequal(String tsequal) ;
	// String getType() ;
	// void setType(String type) ;
	// String getUescape() ;
	// void setUescape(String uescape) ;
	// String getUid() ;
	// void setUid(String uid) ;
	// String getUnbounded() ;
	// void setUnbounded(String unbounded) ;
	// String getUncommitted() ;
	// void setUncommitted(String uncommitted) ;
	// String getUnder() ;
	// void setUnder(String under) ;
	// String getUndo() ;
	// void setUndo(String undo) ;
	// String getUnencrypted() ;
	// void setUnencrypted(String unencrypted) ;
	String getUnion();
	void setUnion(String union);

	String getUnique();
	void setUnique(String unique);

	// String getUnknown() ;
	// void setUnknown(String unknown) ;
	// String getUnlisten() ;
	// void setUnlisten(String unlisten) ;
	// String getUnlock() ;
	// void setUnlock(String unlock) ;
	// String getUnnamed() ;
	// void setUnnamed(String unnamed) ;
	// String getUnnest() ;
	// void setUnnest(String unnest) ;
	// String getUnsigned() ;
	// void setUnsigned(String unsigned) ;
	// String getUntil() ;
	// void setUntil(String until) ;
	String getUpdate();
	void setUpdate(String update);

	// String getUpdatetext() ;
	// void setUpdatetext(String updatetext) ;
	String getUpper();
	void setUpper(String upper);

	String getUsage();
	void setUsage(String usage);

	String getUse();
	void setUse(String use);

	String getUser();
	void setUser(String user);

	// String getUser_defined_type_catalog() ;
	// void setUser_defined_type_catalog(String user_defined_type_catalog) ;
	// String getUser_defined_type_code() ;
	// void setUser_defined_type_code(String user_defined_type_code) ;
	// String getUser_defined_type_name() ;
	// void setUser_defined_type_name(String user_defined_type_name) ;
	// String getUser_defined_type_schema() ;
	// void setUser_defined_type_schema(String user_defined_type_schema) ;
	// String getUsing() ;
	// void setUsing(String using) ;
	// String getUtc_date() ;
	// void setUtc_date(String utc_date) ;
	// String getUtc_time() ;
	// void setUtc_time(String utc_time) ;
	// String getUtc_timestamp() ;
	// void setUtc_timestamp(String utc_timestamp) ;
	// String getVacuum() ;
	// void setVacuum(String vacuum) ;
	// String getValid() ;
	// void setValid(String valid) ;
	// String getValidate() ;
	// void setValidate(String validate) ;
	String getValidator();
	void setValidator(String validator);

	String getValue();
	void setValue(String value);

	// String getValues() ;
	// void setValues(String values) ;
	// String getVar_pop() ;
	// void setVar_pop(String var_pop) ;
	// String getVar_samp() ;
	// void setVar_samp(String var_samp) ;
	// String getVarbinary() ;
	// void setVarbinary(String varbinary) ;
	String getVarchar();
	void setVarchar(String varchar);

	String getVarchar2();
	void setVarchar2(String varchar2);

	// String getVarcharacter() ;
	// void setVarcharacter(String varcharacter) ;
	String getVariable();
	void setVariable(String variable);

	// String getVariables() ;
	// void setVariables(String variables) ;
	// String getVarying() ;
	// void setVarying(String varying) ;
	// String getVerbose() ;
	// void setVerbose(String verbose) ;
	// String getView() ;
	// void setView(String view) ;
	// String get_volatile() ;
	// void set_volatile(String _volatile) ;
	// String getWaitfor() ;
	// void setWaitfor(String waitfor) ;
	// String getWhen() ;
	// void setWhen(String when) ;
	// String getWhenever() ;
	// void setWhenever(String whenever) ;
	String getWhere();
	void setWhere(String where);

	// String get_while() ;
	// void set_while(String _while) ;
	// String getWidth_bucket() ;
	// void setWidth_bucket(String width_bucket) ;
	// String getWindow() ;
	// void setWindow(String window) ;
	// String getWith() ;
	// void setWith(String with) ;
	// String getWithin() ;
	// void setWithin(String within) ;
	// String getWithout() ;
	// void setWithout(String without) ;
	// String getWork() ;
	// void setWork(String work) ;
	// String getWrite() ;
	// void setWrite(String write) ;
	// String getWritetext() ;
	// void setWritetext(String writetext) ;
	// String getX509() ;
	// void setX509(String x509) ;
	// String getXor() ;
	// void setXor(String xor) ;
	String getYear();
	void setYear(String year);
	// String getYear_month() ;
	// void setYear_month(String year_month) ;
	// String getZerofill() ;
	// void setZerofill(String zerofill) ;
	// String getZone() ;
	// void setZone(String zone) ;
	// @formatter:on

}
