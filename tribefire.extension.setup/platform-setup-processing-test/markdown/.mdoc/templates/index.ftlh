<#include "body-layout.ftlh">

<!DOCTYPE html>
<html>
<head>
    <title>AAAAAAAAAAA ${documentation.title} </title>
	<#include "common-head.ftlh">


</head>
<body background="${resDir}docbg_dark.jpg">
<@contentWrapper>
		<div id=front class='content-container center-content'>
			<div id=greeting>
			<#if greeting??>
				${greeting?no_esc}
			<#else>
				<h1>TECH. DOCUMENTATION. </h1>
				<h2>  CONCEPTS. TUTORIALS. REFERENCES.</h2>
				<br />
				<p class="lead"> Our documentation is made for techies by techies. Combined with the training presentations and videos, it provides you with all necessary concepts and tutorials for a deep understanding of tribefire.
				<p class="lead"> For a quick start, use one of the following:
				<br /> <br />
			</#if>
			</div>
			<div id=search-bar-front>
				<form action="search.html" method="get">
		            <input class='search-input' name='searchText' id="search-input" placeholder="Search..." autocomplete="off" autofocus/>            
		        </form>		
		    </div>
			<div id=entrypoints class='center-content'>				
				<#list documentation.entrypoints as entrypoint>
				<div class='entrypoint'>
				 <span class='asset-name'>${entrypoint.title}</span>
				 <div class='entrypoint-image'>
				 	<#if entrypoint.imageUrl??>
				 	<img src='${entrypoint.imageUrl}' />
				 	</#if>
				 </div>
				 <a href="${entrypoint.targetUrl}" > Go! </a>
			 	</div>
				</#list>
		 	</div>
			
		</div>
		<div class='menu'>
        	<ul class="nav">
            <#list assets as asset, element>
            	<li>
		  			<@recursiveStructure element />
		  		</li>
			</#list>
		  	</ul>
		</div>
	</@contentWrapper>
</body>
</html>


<#macro recursiveStructure element>
	<a href='${element.url!"#"}'>${element.text}</a>
	<#if element.subElements?has_content>
  	<@recursiveSubStructure element />
  	</#if>
</#macro>

<#macro recursiveSubStructure element>
  <ul>
  <#list element.subElements as subElement>
    <li><@recursiveStructure subElement /></li>
  </#list>
  </ul>
</#macro>