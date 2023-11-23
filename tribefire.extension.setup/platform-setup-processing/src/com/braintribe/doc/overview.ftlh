<#include "body-layout.ftlh">

<!DOCTYPE html>
<html lang=en>
<head>
    <title>${documentation.title}</title>

	<#include "common-head.ftlh">
	<link rel='stylesheet' href='${resDir}index.css'/>

    <script src="${resDir}lunr.js"></script>
    <script src="${resDir}lunr-index.js"></script>
    
    <script>
	        
        idx = lunr.Index.load(serializedIndex);

        function search(event) {
        	
            var groupingLinkHref = document.getElementById("grouping").href
            var originalSearchText = document.getElementById("search-input").value;
            var searchText = originalSearchText;
        	groupingLinkHref = groupingLinkHref.replace(/\.html(\?searchText=.*)?/, ".html?searchText=" + searchText);
        	document.getElementById("grouping").href = groupingLinkHref
        	
        	var lunrJsSpecialChars = /(^[+-])|[\^:*~]/g;
        	
            if (!searchText.match(lunrJsSpecialChars))
            	searchText = searchText.replace(/(\S{5,})(\s|$)/g, "boosted:$1^10 title:$1^10 headings:$1^5 $1$2");

            console.log("searching: '" + searchText + "'");
            var results;
            
            try {
            	results = idx.search(searchText)
    		} catch (e) {
    			console.log("invalid lunrjs search term: '" + searchText + "'", e);
    			searchText = originalSearchText.replace(lunrJsSpecialChars, " ");
    			console.log("Searching for text without special characters: '" + searchText + "'");
    			results = idx.search(searchText)
    		}
                    	          
           	var assetLists = document.querySelectorAll('*[id^="asset:"]');
            var mdFiles = Array.from(assetLists)
            	.map(li => li.getElementsByTagName("ul")[0])
            	.flatMap(asset => Array.from(asset.children))
            
            // hide ALL asset- and mdfile elements
            assetLists.forEach(asset => asset.style.display = "none")
            mdFiles.forEach(mdfile => mdfile.style.display = "none")

            for (var i = 0; i < results.length; i++) {
                var result = results[i];
                var ref = result.ref;
                
                var mdFileElementId = "mdfile:" + ref
                console.log("mdfileid: ", mdFileElementId)
                var mdFileElement = document.getElementById(mdFileElementId);
                
                if (mdFileElement){
	                 
	                // unhide found mdfile element and the asset list entry itself
	                mdFileElement.style.display = ""
	                mdFileElement.parentElement.parentElement.style.display = ""
	                
	                var linkElement = mdFileElement.children[0]
	                linkElement.href = ref + "?searchText=" + searchText;
                }
            }
            
            if (event) {
            	event.preventDefault();
        	}
        	

            return true;
        }
        
        window.onload = function() {
			$('.nav').navgoco({accordion: true});
        
	    	var urlParams = new URLSearchParams(window.location.search);
			var searchText = urlParams.get('searchText')
			
	    	document.getElementById("search-input").value = searchText;
	    	
	    	search();
		}
    </script>
</head>
<body>
<@contentWrapper>
	<main id='search'>
		<a href=search.html id=grouping>Order results by relevance</a>
		<div id=search-bar>
			<form oninput="return search(event)" onsubmit="event.preventDefault();">
	            <input class='search-input' id="search-input" placeholder="Search..." autocomplete="off" autofocus/>            
	        </form>		
	    </div>
		<div id=total-menu>
	    	<ul class="asset-list">
	        <#list assets as asset, files>
		  		<li id="asset:${asset.groupId}:${asset.name}">
		  			<span class='asset-name'>${metaDataOf(asset).displayTitle}</span>
		 			<small class='asset-group'>(${asset.groupId}:${asset.name})</small>
			  		<ul class="file-list nav" >
			  		<#list files as file>
			  		<#if file.title??>
			  			<li id='mdfile:${file.docRelativeHtmlFileLocation}' ><a href='${file.docRelativeHtmlFileLocation}'>${file.title}</a></li>
		  			</#if>
			  		</#list>
			  		</ul>
		  		</li>
			</#list>
		  	</ul>
		</div>
	</main>
</@contentWrapper>
</body>
</html>
