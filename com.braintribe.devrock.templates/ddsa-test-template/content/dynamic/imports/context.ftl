<#-- We prefix helper variables (ONLY meant to be used in this file) with _, e.g. _groupPackage -->
<#function relocation classFullName>
    <#return "src/${classFullName?replace('.', '/')}.java">
</#function>

<#assign _groupPackage = request.groupId?replace('-', '')>

<#assign nameBaseKebab = request.artifactId?remove_ending('-test')?remove_ending('-processing')>
<#assign nameBaseSnake = nameBaseKebab?replace('-', '_')>
<#assign nameBasePascal = support.toPascalCase(nameBaseKebab, '-')>
<#assign nameBaseCamel = nameBasePascal?uncap_first>

<#assign basePackage = support.smartPackageName(_groupPackage, nameBaseSnake)>
<#if request.artifactId?ends_with('-processing-test')>
    <#assign _baseOrProcessingPackage = "${basePackage}.processing">
<#else>
    <#assign _baseOrProcessingPackage = "${basePackage}">
</#if>

<#assign testBasePackage = _baseOrProcessingPackage + ".base">
<#assign wirePackage = testBasePackage + '.wire'>
<#assign wireContractPackage = wirePackage + '.contract'>
<#assign wireSpacePackage = wirePackage + '.space'>

<#assign classNamePrefix = support.toPascalCase(request.artifactId, '-')>

<#assign wireModuleSimple = classNamePrefix + 'WireModule'>
<#assign wireModuleFull = wirePackage + '.' + wireModuleSimple>

<#assign wireContractSimple = classNamePrefix + 'Contract'>
<#assign wireContractFull = wireContractPackage + '.' + wireContractSimple>

<#assign wireSpaceSimple = classNamePrefix + 'Space'>
<#assign wireSpaceFull = wireSpacePackage + '.' + wireSpaceSimple>

<#assign testBaseSimple = classNamePrefix + 'Base'>
<#assign testBaseFull = testBasePackage + '.' + testBaseSimple>

<#assign transformToUpperCaseTestSimple = nameBasePascal + 'TransformToUpperCaseTest'>
<#assign transformToUpperCaseTestFull = _baseOrProcessingPackage + '.' + transformToUpperCaseTestSimple>

<#assign modelPackage = "${basePackage}.model">
<#assign modelDeploymentPackage = "${modelPackage}.deployment">
<#assign modelApiPackage = "${modelPackage}.api">
<#assign processingPackage = "${basePackage}.processing">

<#assign abstractRequestSimple = "${nameBasePascal}ServiceRequest">
<#assign abstractRequestFull = "${modelApiPackage}.${abstractRequestSimple}">
<#assign transformRequestSimple = "${nameBasePascal}TransformToUpperCase">
<#assign transformRequestFull = "${modelApiPackage}.${transformRequestSimple}">

<#assign serviceProcessorSimple = "${nameBasePascal}ServiceProcessor">
<#assign serviceProcessorFull = "${modelDeploymentPackage}.${serviceProcessorSimple}">
<#assign requestProcessorSimple = "${nameBasePascal}RequestProcessor">
<#assign requestProcessorFull = "${processingPackage}.${requestProcessorSimple}">
<#assign requestProcessorMethod = "${nameBaseCamel}RequestProcessor">