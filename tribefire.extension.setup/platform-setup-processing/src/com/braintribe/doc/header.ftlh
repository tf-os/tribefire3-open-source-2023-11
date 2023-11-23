  <a id="skipnav" href="#main-content-start" class="non-visual-only">Skip to content</a>
  <header class='header'>
    <span class='home-button'><a href=${relativeFilePathToRoot}index.html><img src='${resDir}tribefire-logo-orange.svg' alt='logo'/><span class='non-visual-only'>Back to home screen</span></a></span>
    <nav>
	    <#list headerMenuAssets>
	    <ul id=header-menu class=tile>
		    <#if javadocLocation??>
		    <li><a href=${javadocLocation} class='outsidelink api-link' target='_blank'>API</a></li>
		    </#if>
	    	<#items as asset>
	    	<#if mdFile?? && mdFile.asset == asset>
	    		<#assign class="ownAsset" />
	    	<#else>
	    		<#assign class="" />
	    	</#if>
	    	<li class='${class}'><a href="${relativeFilePathToRoot}${asset.groupId}/${asset.name}/${metaDataOf(asset).startingPoint!"##"}">${metaDataOf(asset).displayTitle}</a></li>
	    	</#items>
	    </ul>
	    </#list>
	    
	    <#list headerMenuAssets>
	    <ul class='nav dark tile tiny-screen-only'>
	    	<li class='tile'>
	    		<a href=# class='tile'>MENU</a>
		    	<ul>
				    <#if javadocLocation??>
				    <li><a href=${javadocLocation} class='outsidelink api-link' target='_blank'>API</a></li>
				    </#if>
			    	<#items as asset>
			    	<#if mdFile?? && mdFile.asset == asset>
			    		<#assign class="ownAsset" />
			    	<#else>
			    		<#assign class="" />
			    	</#if>
			    	<li class='${class}'><a href="${relativeFilePathToRoot}${asset.groupId}/${asset.name}/${metaDataOf(asset).startingPoint!"##"}">${metaDataOf(asset).displayTitle}</a></li>
			    	</#items>
			    </ul>
		    </li>
	    </ul>
	    </#list>
    </nav>
	<form id=search-bar action="${relativeFilePathToRoot}search.html" method="get" class='tile' role='search'>
		<div class='tile' id='search-icon'>
			<input type=image src="${resDir}search.svg" value="search" alt='search icon' />
			<label id="toggleIcon" class="switch" title="Toggle Highlighting">
				<span class="non-visual-only">Toggle Highlighting (this takes immediate effect)</span>
				<input type=checkbox id="toggleHighlightingCheckbox" />
				<div class="slider"></div>
			</label>
		</div>
		<label>
			<span class="non-visual-only">Search in all documentation</span>
	        <input class='search-input tile' placeholder="Search in documentation..." name="searchText" />
        </label>            
    </form>
  </header>