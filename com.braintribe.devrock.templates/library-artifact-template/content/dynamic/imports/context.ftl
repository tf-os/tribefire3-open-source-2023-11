<#-- We prefix helper variables (ONLY meant to be used in this file) with _, e.g. _groupPackage -->
<#function relocation classFullName>
    <#return "src/${classFullName?replace('.', '/')}.java">
</#function>

<#assign _groupPackage = request.groupId?replace("-", "")>

<#assign nameBaseKebab = request.artifactId?remove_ending("-processing")>
<#assign nameBaseSnake = nameBaseKebab?replace("-", "_")>
<#assign nameBasePascal = support.toPascalCase(nameBaseKebab, '-')>

<#assign basePackage = support.smartPackageName(_groupPackage, nameBaseSnake)>
<#assign modelPackage = "${basePackage}.model">
<#assign modelApiPackage = "${modelPackage}.api">
<#assign processingPackage = "${basePackage}.processing">

<#assign requestProcessorSimple = "${nameBasePascal}RequestProcessor">
<#assign requestProcessorFull = "${processingPackage}.${requestProcessorSimple}">

<#assign abstractRequestSimple = "${nameBasePascal}ServiceRequest">
<#assign abstractRequestFull = "${modelApiPackage}.${abstractRequestSimple}">
<#assign transformRequestSimple = "${nameBasePascal}TransformToUpperCase">
<#assign transformRequestFull = "${modelApiPackage}.${transformRequestSimple}">
