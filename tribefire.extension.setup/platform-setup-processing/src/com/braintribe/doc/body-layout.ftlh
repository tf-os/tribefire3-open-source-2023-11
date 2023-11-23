<#macro contentWrapper>
	<div class='container'>
	  <#include "header.ftlh">
	  <div class='below-header'>
	  	<a name='main-content-start' id='main-content-start'></a> 
	  	<div class='inner-container' id='main-content-wrapper'>
		  <#nested>
	  	</div>
	  	<#include "footer.ftlh">
	  </div>
	</div>
</#macro>

<#macro renderLink link htmlAttributes="">
	<a href='${link.url!"#"}' title='${link.title}' ${htmlAttributes}>${link.text}</a>
</#macro>


<#macro recursiveStructure element>
	<#if element.markdownFile??>
	  <#if mdFile?? && element.markdownFile == mdFile>
		<a href='#' class='selflink file'>${element.title}</a>
	  <#else>
		<a href='${relativeFilePathToRoot + element.url}' class='file'>${element.title}</a>
	  </#if>
	<#else>
		<a href='#' class='folder'>${element.title}</a>
	</#if>

	<#if element.subElements?? && element.subElements?has_content>
  	<@recursiveSubStructure element />
  	<#else>
  	</#if>
</#macro>

<#macro recursiveSubStructure element>
  <ul>
  <#list element.subElements as subElement>
  	<#if !subElement.hidden && subElement.title??>
    <li><@recursiveStructure subElement /></li>
    </#if>
  </#list>
  </ul>
</#macro>

<#macro wizardLink text file="">
	<#if file?has_content>
		<a href="${file.htmlFileName}" class="wizard-step" title="${file.title}">${text}</a>
	<#else>
		<a class="hidden wizard-step">${text}</a>
	</#if>
</#macro>
