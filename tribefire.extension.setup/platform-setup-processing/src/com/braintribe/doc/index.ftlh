<#include "body-layout.ftlh">

<!DOCTYPE html>
<html lang=en>
<head>
    <title>${documentation.title}</title>
	<link rel='stylesheet' href='${resDir}index.css'/>
	<#include "common-head.ftlh">

	<script type="text/javascript">
		$(document).ready(function() {
			$('.nav').navgoco({accordion: true});
		});
	</script>
	
</head>
<body>
<@contentWrapper>
		<article id=front class='center-content'>
			<header id=greeting>
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
			</header>
			<div id=entrypoints class='center-content'>				
				<#list documentation.entrypoints as entrypoint>
				<#assign e=entrypoint.displayInfo >
				 <a href="${entrypoint.targetUrl}" class='entrypoint card'>
					 <span class='asset-name'>${e.displayTitle}</span>
					 <div class='entrypoint-image'>
					 	<#if e.imageUrl??>
					 	<img src='${entrypoint.imageUrl}' />
					 	</#if>
					 </div>
				 </a>
				</#list>
		 	</div>
		    
		    <a href="all-docs.html" id="all-docs-link">> All Docs</a>
		</article>
	</@contentWrapper>
</body>
</html>


<#macro recursiveStructure element>
	<@renderLink element />
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