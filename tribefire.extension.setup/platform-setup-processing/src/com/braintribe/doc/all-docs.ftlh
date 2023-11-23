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
	<main class=asset-tiles-container>
	<article>
		<div class=introduction>
			<h1>Content Overview</h1>
			<p>Click one of the following topics to learn more.</p>
		</div>
		
		<div class=asset-tiles>
		<#list assets as asset>
			<#assign assetMeta=metaDataOf(asset) />
        	<a href="${asset.groupId}/${asset.name}/${assetMeta.startingPoint}" class="asset-tile card">
				<#if assetMeta.imageUrl??>
        		<div class=large-icon>
					<img src="${asset.groupId}/${asset.name}/${assetMeta.imageUrl}" />
	  			</div>
				</#if>
				<h3>
					<#if assetMeta.imageUrl??>
					<img class=icon src="${asset.groupId}/${asset.name}/${assetMeta.imageUrl}" />
					</#if>
					${assetMeta.displayTitle}
				</h3>
				<div class=description>	  	
	  				${assetMeta.shortDescription!"No Description"}
	  			</div>
		  			
	  		</a>
		</#list>
		
		
		</div>
	</article>
	<#list tags>
	<article>
		<div class=introduction>
			<h2>Tags Overview</h2>
			<p>Click one of the following tags to learn more.</p>
		</div>
		
		<div class=asset-tiles>
		<#items as tag>
        	<a href="${tag.docRelativeUrl}" class="asset-tile card">
				<h3>${tag.name}<span class="doc-count">(${tag.taggedFiles?size})</span></h3>
				<div class=description>	  	
	  				${tag.description}
	  			</div>
	  		</a>
		</#items>
		
		</div>
	</article>
	</#list>
	</main>
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