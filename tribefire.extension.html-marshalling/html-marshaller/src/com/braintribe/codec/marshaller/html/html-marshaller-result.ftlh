<html>
<head>
	<title>${value.typeSignature}</title>
	<link href="https://fonts.googleapis.com/css2?family=Roboto&display=swap" rel="stylesheet">
	<style>
		
		main {
		    width: fit-content;
		}
		
		h1 {
			font-size: 1.2rem;
			font-weight: normal;
			font-style: italic;
			padding: 5px;
			margin-bottom: 0.2rem;
		}
		
		td {
			border: 1px solid #efefef;
		    vertical-align: top;
		    position: relative;
		    padding: 0;
		}
		
		 ol {
			list-style: none;
			counter-reset: item;
			margin-block-start: 0px;
    		margin-block-end: 0px;
		 }
		 ol > li {
			counter-increment: item;
		 }
		 ol > li:before {
			content: counter(item) ".";
			font-family: monospace;
			position: absolute;
		    margin-left: -6.3ch;
		    margin-top: 2px;
			display: block;
			width: 6ch;
			text-align: right;
		 }			
			
			
		.value:target {
			border: 2px dashed #9bd4dd;
			box-shadow: 2px 1px 10px 2px rgba(0, 0, 0, 0.2);
		}
		
		.objectType, .value-note {
			color: #999;
			font-family: monospace;
			display: none;
		}
		
		.entityType th {
			font-weight: normal;
			color: #333;
			background-color: #f8f8f8;
			text-align: left;
		    font-family: monospace;			
		}
		
		.entityType table{
			width: 100%;
		}
		
		.packageName {
			/* color: #999; */
			opacity: 0.6;
		}
		
		.collapsible > tr > th > .shortName {
			color: black;
		}
		
		.hide-content > tr > th > .shortName {
			color: #137;
		}
		
		.collapsible > tr > th {
			font-size: 0.92rem;
		}
		
		td > .value > table {
			border: none;
		}
		
		table {
			border-collapse: collapse;
		}
		
		td:first-child {
			white-space: nowrap;
			width: 1%; /* This hack ensures in all major browsers that the width of the first column is exactly the width of the longest content of a cell in that column */
			padding-right: 0.3rem;
		}
		
		.key {
		    font-family: monospace;
		    color: #666;
		    font-size: 0.95rem;
		    text-align: right;
		    display: block;
		    margin-top: 4px;
		    margin-left: 2px;
		}
		
		.enumType {
			color: darkorange;	
		}
		
		.setType > ul,  .listType > ol{
			color: gray;
		    padding-inline-start: 1rem
		    
		    display: flex;
		    flex-flow: column wrap;
		    align-content: flex-start;
		}
		
		.setType > ul > li,  .listType > ol > li{
			padding-bottom: 4px;
		}
		
		.mapType > table > tbody > tr > td {
    		border: 1px solid #e8db9a;
    	}
		
		.stringType {
			/*color: #8B0000;*/
		}
		
		.booleanType {
			color: #2d9d92;
		}
		
		.integerType {
			color: green;
		}
		
		.longType {
			color: #00b500;
		}
		
		.floatType {
			color: green;
		}
		
		.doubleType {
			color: #00b500;
		}
		
		.decimalType {
			color: lime;
		}
		
		.dateType {
			color: purple;
		}
		
		tr:nth-of-type(even) {
			background-color: #ffffff90;
		}
		
		.render-type-link {
			border: none;
		}
		
		.render-type-link > a {
			font-family: monospace;
    		color: #137;
		}
		
		#tooltip {
			position: fixed;
			border: 1px solid blue;
			background-color: white;
			padding: 0.3rem;
			transition-duration: 800ms;
			transition-property: opacity;
			height: fit-content;
			bottom: 1rem;
			left: 1rem;
			display: none;
		}
		
		#tooltip.active {
			bottom: 0;
			left: 0;
			width: 100%;
			height: 10rem;
			overflow: auto;
			display: block;
		}
		
		#tooltip #advanced-information {
			display: none;
		}
		
		#tooltip .closebutton {
			display: none;
			z-index: 1;
		}
		
		#tooltip.active #advanced-information {
			display: block;
		}
		
		#tooltip.active .closebutton {
			display: block;
			position: absolute;
			right: 1rem;
		}
		
		body, html {
			margin: 0;
			padding: 0;
			font-family: 'Roboto', sans-serif;
		}
		
		.hide-content + tbody {
			display: none;
		}
		
		thead.hide-content {
		    cursor: zoom-in;
		}
		
		thead {
		    cursor: zoom-out;
		}
		
		.value {
			display: flex;
			margin: 4px;
			font-size: 0.85rem;
		}
		
		.shorten-types .packageName {
			display: none;
		}
		
		.render-type-entity {
			color: darkblue;
			/*box-shadow: 2px 1px 4px 0px rgba(0, 0, 0, 0.2);*/
			margin: 0px;
		}
		
		.collapsible > tr >th::before {
			content: "\23F7";
			padding: 0 2px;
			color: #888;
		}
		
		.collapsible.hide-content > tr >th::before {
			content: "\23F5";
			padding: 0 2px;
			color: #888;
		}
		
		.collapsible.hide-content > tr >th {
			content: "\23F5";
			padding: 0 2px;
			color: #888;
		    background-color: initial;
		}
		
		#toolbar {
			font-size: 0.8rem;
    		padding-bottom: 0.85rem;
		}
		
	</style>
	
	<script>
		function toggleContent(e) {
		
			if(e.classList.contains('hide-content')) {
				e.classList.remove('hide-content');
			} else {
				e.classList.add('hide-content');
			}
		
		}
		
		function toggleAll(e) {
			var elements = document.getElementsByClassName('collapsible');
			
			if(e.checked) {
				for(i=0; i<elements.length; i++) {
					elements[i].classList.remove('hide-content');
				}
			} else {
				for(i=0; i<elements.length; i++) {
					elements[i].classList.add('hide-content');
				}
			}
		}
		
		function shortenTypes(e) {
			var body = document.body;
			if(e.checked) {
				body.classList.add("shorten-types");
			} else {
				body.classList.remove("shorten-types");
			}
		}
		
		function expandElement(id, element) {
			if (!element) {
				element = document.getElementById(id);
				if (element.firstElementChild)
					element = element.firstElementChild;
			}
				
			if (element.nodeName === "TABLE"){
				let thead = element.getElementsByTagName("thead")[0];
				if (thead)
					thead.classList.remove('hide-content');
			}

			if (element.parentElement)
				expandElement(null, element.parentElement)
		}
		
	</script>
	
</head>
<body class="shorten-types">
	<header>
		<#assign renderType=value.renderType()/>
		<h1>
		<#if renderType == 'set'>
		Set of <@renderTypeSignature value.elementTypeSignature/>
		<#elseif renderType == 'list'>
		List of <@renderTypeSignature value.elementTypeSignature/>
		<#elseif renderType == 'map'>
		Map of <@renderTypeSignature value.keyTypeSignature/> to <@renderTypeSignature value.valueTypeSignature/>
		<#elseif renderType == 'entity'>
		<span class="packageName">${value.typePackageName()}.</span><span class="shortName">${value.value.entityType().shortName}</span>
		<#else>
		</#if>
		</h1>
	
		<nav id="toolbar">
			<input onclick="toggleAll(this)" type="checkbox" id="expand-all" name="Expand all"><label for="expand-all">Expand all</label>
			<input onclick="shortenTypes(this)" type="checkbox" checked id="shorten-types" name="Shorten types"><label for="shorten-types">Use short type names</label>
		</nav>
		
	</header>
	<main id="main">
		<@renderValue value 0/>
	</main>
	<aside id="tooltip">
		<div id="advanced-information">
			<button class="closebutton" onclick="deactivateTooltip()">Close Tooltip</button>
			<table>
				<tr><td>actual type</td><td id="actual-type-tooltip"></td></tr>
				<tr><td>property type</td><td id="expected-type-tooltip"></td></tr>
			</table>
		</div>
	</aside>
	<footer></footer>
	<script>
	
		let tooltip = document.getElementById("tooltip");
		let actualTypeTooltip = document.getElementById("actual-type-tooltip");
		let expectedTypeTooltip = document.getElementById("expected-type-tooltip");
		
		let main = document.getElementById("main");
		
		function activateTooltip(element){
			updateTooltip(element);
			tooltip.classList.add("active");
		}
		
		function deactivateTooltip(){
			tooltip.classList.remove("active");
		}
		
		function updateTooltip( element ) {
			let typeSignature = element.getAttribute("data-type-signature");
			
			if (typeSignature){
				actualTypeTooltip.textContent = element.getAttribute("data-type-signature");
				expectedTypeTooltip.textContent = element.getAttribute("data-expected-type-signature");
				
				return true;
			}
			
			return false;
		}
		
	</script>

</body>
</html>

<#macro renderTypeSignature typeSignature>

	<#if support.getPackageName(typeSignature)??> <span class="packageName">${support.getPackageName(typeSignature)}.</span></#if><span class="shortName">${support.getShortName(typeSignature)}
</#macro>

<#macro renderValue value depth>
	<#assign renderType=value.renderType()/>
	<div id="${value.runtimeId()?c}" 
		class="value ${value.objectTypeCode} render-type-${renderType}" 
		data-type-signature="${value.typeSignature}"
		data-expected-type-signature="${value.expectedTypeSignature!'?????'}"
		>
		<#if renderType == 'set'>
				<#list value.elements>
					<ul>
					<#items as element>
						<li><@renderValue element depth/></li>
					</#items>
					</ul>
				<#else>
					<span class="value-note">empty</span>
				</#list>
		<#elseif renderType == 'list'>
				<#list value.elements>
					<ol start=0>
					<#items as element>
						<li><@renderValue element depth/></li>
					</#items>
					</ol>
				<#else>
					<span class="value-note">empty</span>
				</#list>
		<#elseif renderType == 'map'>
				<#list value.entries>
				<table>
					<#items as k,v>
					<tr>
						<td><span><@renderValue k depth/></span></td>
						<td><@renderValue v depth/></td>
					</tr>
					</#items>
				</table>
				<#else>
					<span class="value-note">empty</span>
				</#list>
		<#elseif renderType == 'entity'>
			<table>
				<thead class="collapsible<#if (depth > 0)> hide-content</#if>" onclick="toggleContent(this)">
					<tr><th colspan="2"><span class="packageName">${value.typePackageName()}.</span><span class="shortName">${value.value.entityType().shortName}</span></th></tr>
				</thead>
				<tbody>
				<#list value.properties as k, v>
					<tr>
						<td onclick="activateTooltip(this.nextElementSibling.firstElementChild)"><span class=key>${k}</span></td>
						<td><@renderValue v depth+1/></td>
					</tr>
				</#list>
				</tbody>
			</table>
		<#elseif renderType == 'link'>
			<#assign linked=value.linked/>
			<a href="#${linked.runtimeId()?c}" onclick="expandElement(${linked.runtimeId()?c})">
			<#if linked.renderType() == 'entity'>
				<span class="packageName">${linked.typePackageName()}.</span><span class="shortName">${linked.value.entityType().shortName}(${linked.value.id})</span>
			<#else>
				<span class="shortName">${linked.typeSignature}</span>
			</#if>
			</a>
		<#else>
			${value.renderedString}
		</#if>
	</div>
</#macro>
