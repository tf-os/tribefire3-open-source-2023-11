<#include "body-layout.ftlh">

<html lang=en>
<head>
	<title>${documentation.title} - 404 - File not found</title>

	<#include "common-head.ftlh">
	<link rel='stylesheet' href='${resDir}index.css'/>
	
	<script type="text/javascript">
		$(document).ready(function() {
			$('.nav').navgoco();
			
			$('#technicalLinks :has(ul)')
				.show()
			$('#technicalLinks>ul>li')
				.addClass('open')
			
			$(".selflink").parentsUntil('.nav').show();
			$(".selflink").parent().parentsUntil('.nav').addClass('open');
		});
	</script>
	
	
</head>
<body>
<@contentWrapper>
	  <main id='full-content' class='content-container'>
	  	<article>
		  	<h1>Under Construction</h1>
		  	<p>This file is referenced by other files of this documentation but does not yet exist. Please come back later</p>
		  	<h2>Files referencing this location</h2>
		  	<ul>
		  	<#list referencingMdFilesPerAsset as assetId, files>
		  		<li id="asset:${assetId}">
		  			${assetId}
			  		<ul class="file-list" >
			  		<#list files as file>
			  			<li><a href='${relativeFilePathToRoot}${file.docRelativeHtmlFileLocation}'>${file.title!"(Document without Title)"}</a></li>
			  		</#list>
			  		</ul>
		  		</li>
			</#list>
			</ul>
		</article>
	  </main>
	  
	  <nav class='menu nav'>
		<#if menu??>
		<ul>
			<li><a href=#>${menu.title}</a>
			<@recursiveSubStructure menu />
			</li>
		</ul>
		</#if>
	  </nav>
</@contentWrapper>
</body>
</html>
