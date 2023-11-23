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
			$("#front .nav>li").addClass("open").children().show()
			$('.collapse-all-button').click(function () {
				$(this).parent().navgoco('toggle', false);
			});
		});
	</script>
	

</head>
<body>
<@contentWrapper>
	<article id=tag-container>
		<header class=introduction>
			<h1>Docs tagged "${tag.name}"</h1>
			<p>${tag.description}</p>
			<h2>${mdFiles?size} files</h2>
			<hr />
		</header>
		<div class=tags>
			<#list mdFiles as mdFile>
			<section class="md-file">
		    	<h3><a href="${relativeFilePathToRoot}${mdFile.docRelativeHtmlFileLocation}">
					${mdFile.title}
		  		</a></h3>
				<div class=description>	  	
	  				${mdFile.displayInfo.shortDescription!"No Description"}
	  			</div>
	  			
	  			<#list mdFile.tags as mdFileTag>
	  				<a href="${mdFileTag.id}.html" class="tag">${mdFileTag.name}</a>
	  			</#list>
			</section>
			</#list>
		</div>
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