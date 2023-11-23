<#include "body-layout.ftlh">

<!DOCTYPE html>
<html lang=en>
<head>
    <title>${documentation.title}</title>
    
	<#include "common-head.ftlh">
	<link rel='stylesheet' href='${resDir}index.css'/>
	<link rel='stylesheet' href='${resDir}pagination.css'/>
    
    <script src="${resDir}pagination.min.js"></script>
    <script src="${resDir}lunr.js"></script>
    <script src="${resDir}lunr-index.js"></script>
    <script>
    var markdownFiles = {
	    <#list assets as asset, files>			  	
		<#list files as file>
			"${file.docRelativeHtmlFileLocation}" : {
				"asset" : "${metaDataOf(asset).displayTitle}",
				"title" : "${file.title!file.docRelativeHtmlFileLocation}"
			},
		</#list>
		</#list>
    }
    
    </script>
    <script>
	        
        idx = lunr.Index.load(serializedIndex);
        
        let maxResults = 10;
        
        function renderResults(results){
            var searchText = document.getElementById("search-input").value;        
        	var searchResults = document.getElementById('search-results');
        	searchResults.innerHTML = "";
            
            for (var i = 0; i < results.length; i++) {
                var result = results[i];
                var ref = result.ref;
                var divNode = document.createElement("li");
                var anchorNode = document.createElement("a");
                anchorNode.href = ref + "?searchText=" + searchText;
                anchorNode.target = "content-frame";
                
                var mdFile = markdownFiles[ref];
                
                anchorNode.innerHTML = "<b>" + mdFile.asset + "</b> - " +  mdFile.title
                
                divNode.appendChild(anchorNode);
                searchResults.appendChild(divNode);
            }
            
            searchResults.style.height = searchResults.querySelector("li").offsetHeight * maxResults + "px"
            
        }

        function search(event) {
        	var searchResults = document.getElementById('search-results');   
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
    		
    		results = results.filter(r => markdownFiles[r.ref]);
                       
			if (results.length == 0){
				searchResults.innerHTML = "<b>No results found.</b>";
				$('#pagination-bar').html("")
			} else {
				$('#pagination-bar')
					.pagination({
					    dataSource: results,
					    callback: renderResults,
					    pageRange: 1,
					    pageSize: maxResults,
					    className: "paginationjs-big"
					})
					
				$('.paginationjs')
					.addClass("paginationjs-big");

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
		<a href=overview.html id=grouping>Group results by asset</a>
		<div class=search-bar>
			<form oninput="return search(event)" onsubmit="event.preventDefault();">
	            <input class='search-input' id="search-input" placeholder="Search..." autocomplete="off" autofocus/>            
	        </form>		
	    </div>
	    <ul id='search-results' class='nav'>
	    	<h2>Search results</h2>
	    </ul>
	    <div id="pagination-bar">
	    </div>
	</main>
</@contentWrapper>
</body>
</html>
