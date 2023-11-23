<#include "body-layout.ftlh">

<html lang=en>
<head>
	<title>${documentation.title} - ${mdFile.title!mdFile.assetId}</title>
	
	<#include "common-head.ftlh">
	
	<link rel='stylesheet' href='${resDir}content.css'/>
	
   	<script src="${resDir}lunr.js"></script>
    <script src="${resDir}lunr-index.js"></script>
    
    <link rel="stylesheet" href="${resDir}highlight-custom.css">
	<script src="${resDir}highlight.pack.js"></script>
	<script>hljs.highlightAll();</script>
    
    <script src="${resDir}content.js"></script>

	<#if wizard??>
	<script>
		registerWizardKeyNavigation("${(wizard.prevFile.htmlFileName)!('')}", "${(wizard.nextFile.htmlFileName)!('')}");
	</script>
	</#if>
</head>
<body>
<@contentWrapper>
    <main id='full-content' class='content-container'>
    	<article>
		<#if wizard??>
			<div class="wizard-wrapper">
				<div class="wizard-nav-wrapper">
					<div class="wizard-nav">
						<@wizardLink '&#x1F868;'?no_esc wizard.prevFile />
						<div class="wizard-title">${wizard.displayTitle} <span class="wizard-page-numbers">${wizard.index} of ${wizard.total}</span></div>
						<@wizardLink '&#x1F86A;'?no_esc wizard.nextFile />
					</div>
				</div>
				<div class="wizard-content">
				${content?no_esc}
				</div>
			</div>
		<#else>
			${content?no_esc}
		</#if>
		</article>

		<#if mdFile.tags?has_content>
			<div id='tags'>
				<#list mdFile.tags as tag>
					<a href="${relativeFilePathToRoot}${tag.docRelativeUrl}" class="tag">${tag.name}</a>
				</#list>
			</div>
		</#if>
	</main>

	<div class='menu'>
	<nav class='nav'>
		<#if menu??>
    	<input id="navFilter" class="navFilter" placeholder="Filter..." name="menuFilter" oninput="onNavFilterTextChanged()" />
		<ul>
			<li><a href=#>${menu.title}</a>
			<@recursiveSubStructure menu />
			</li>
		</ul>
		</#if>
	</nav>
	
	<#if referencedMdFiles?has_content || referencingMdFiles?has_content || mdFile.referencingExternalLinks?has_content || mdFile.referencingJavadocLinks?has_content>
	<aside class='nav' id='technicalLinks'>
	  <ul>
		<li><a href=#>See also...</a>
		  <ul>
			<#list referencingMdFiles![]>
		    <li><a href=#>Incoming Links</a>
		      <ul>
		      	<#items as reference>
				<li><a href='${relativeFilePathToRoot}${reference.docRelativeHtmlFileLocation}' title='${reference.title!"<technical file>"} - ${metaDataOf(reference.asset).displayTitle}' class="file">
					${reference.title!"<technical file>"} <i> - ${metaDataOf(reference.asset).displayTitle}</i>
				</a></li>
				</#items>
			  </ul>
			</li>
			</#list>
			<#list referencedMdFiles![]>
		    <li><a href=#>Outgoing Links</a>
		      <ul>
		      	<#items as reference>
				<li><a href='${relativeFilePathToRoot}${reference.docRelativeHtmlFileLocation}' title='${reference.title!"<technical file>"} - ${metaDataOf(reference.asset).displayTitle}' class="file">
					${reference.title!"<technical file>"} <i> - ${metaDataOf(reference.asset).displayTitle}</i>
				</a></li>
				</#items>
			  </ul>
			</li>
			</#list>
			<#list mdFile.referencingExternalLinks![]>
		    <li><a href=#>External Links</a>
		      <ul>
		      	<#items as reference>
				<li><@renderLink reference "target=_blank class=outsidelink"/></li>
				</#items>
			  </ul>
			</li>
			</#list>
			<#list mdFile.referencingJavadocLinks![]>
		    <li><a href=#>JavaDoc</a>
		      <ul>
		      	<#items as reference>
				<li><@renderLink reference "target=_blank class=javadoclink"/></li>
				</#items>
			  </ul>
			</li>
			</#list>
		  </ul>
		</li>
	  </ul>
	</aside>
	</#if>
	</div>
	
	<!-- The Modal -->
	<div id="imageModal" class="modal">
	
	  <!-- The Close Button -->
	  <span class="close">&times;</span>
	
	  <!-- Modal Content (The Image) -->
	  <img class="modal-content" id="modalImage">
	
	</div>
</@contentWrapper>
</body>
</html>
