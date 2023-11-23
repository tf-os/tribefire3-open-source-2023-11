document.documentElement.classList.add('javascript-enabled') // add class to HTML element to signal to CSS that certain elements can be hidden because they can be enabled again later via javascript


function Hilitor(id, tag)
{

  var targetNode = document.getElementById(id) || document.body;
  var hiliteTag = tag || "EM";
  var skipTags = new RegExp("^(?:" + hiliteTag + "|SCRIPT|FORM|SPAN)$");
  var colors = ["#ff6", "#a0ffff", "#9f9", "#f99", "#f6f"];
  var wordColor = [];
  var colorIdx = 0;
  var matchRegex = "";

  // characters to strip from start and end of the input string
  var endCharRegex = new RegExp("^[^\\\w]+|[^\\\w]+$", "g");

  // characters used to break up the input string into words
  var breakCharRegex = new RegExp("[^\\\w'-]+", "g");

  this.setRegex = function(input){
	if(input) {
	  input = input.filter(i => i).map(this._createRegex).join("|")

      matchRegex = new RegExp(input, "i");
	  console.log("Match Regex", matchRegex)
      return true;
    }
    return false;
  }
  
  this._createRegex = function(input)
  { // TODO: next 3 lines needed?
    input = input.replace(endCharRegex, "");
    input = input.replace(breakCharRegex, "|");
    input = input.replace(/^\||\|$/g, "");
	if(input) {
      var re = "\\w*" + input + "\\w*";
	  console.log("Match Regex", re)
	  return re;
    }
    return null
  };

  // recursively apply word highlighting
  this.hiliteWords = function(node)
  {
    if(node === undefined || !node) return;
    if(!matchRegex) return;
    if(skipTags.test(node.nodeName)) return;

    if(node.hasChildNodes()) {
      for(var i=0; i < node.childNodes.length; i++)
        this.hiliteWords(node.childNodes[i]);
    }
    if(node.nodeType == 3) { // NODE_TEXT
      if((nv = node.nodeValue) && (regs = matchRegex.exec(nv))) {
        if(!wordColor[regs[0].toLowerCase()]) {
          wordColor[regs[0].toLowerCase()] = colors[colorIdx++ % colors.length];
        }

        var match = document.createElement(hiliteTag);
        match.appendChild(document.createTextNode(regs[0]));
        match.style.backgroundColor = "yellow";
        match.style.fontStyle = "inherit";
        match.style.color = "#000";
        match.class = "highlight"

        var after = node.splitText(regs.index);
        after.nodeValue = after.nodeValue.substring(regs[0].length);
        node.parentNode.insertBefore(match, after);
      }
    };
  };

  // remove highlighting
  this.remove = function()
  {
    var arr = document.getElementsByTagName(hiliteTag);
    while(arr.length && (el = arr[0])) {
      var parent = el.parentNode;
      parent.replaceChild(el.firstChild, el);
      parent.normalize();
    }
  };

  // start highlighting at target node
  this.apply = function(input)
  {
    this.remove();
    if(input === undefined || !input) return;
    if(this.setRegex(input)) {
      this.hiliteWords(targetNode);
    }
  };

}

var myHilitor
var urlParams = new URLSearchParams(window.location.search);
var searchText = urlParams.get('searchText')

function toggleHighlighting(event) {
	console.log("checkbox checked in local storage  " + window.localStorage.getItem("highlighting"))
	console.log("checkbox checked will be in local storage " + event.target.checked)
	window.localStorage.clear()
  window.localStorage.setItem("highlighting", event.target.checked)
  
  if (event.target.checked) {
    console.log('checked')
    highlightSearchTerms()
  } else {
    console.log('not checked')
    myHilitor.remove()
  }
}

function highlightSearchTerms() {
	
	
	if (!searchText)
		return;
		
	console.log("User searched for '" + searchText + "'");
	
	idx = lunr.Index.load(serializedIndex);
	results = idx.search(searchText)
			
	var context = document.querySelector(".content-container")
	myHilitor = new Hilitor("full-content", "MARK");
	
	var ranges = results
		.filter (r => window.location.pathname.endsWith(r.ref))
		.flatMap (r => Object.keys(r.matchData.metadata))
		.map (term => {
			console.log("marking '" + term + "'");
		    return term.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&'); // escape regex chars
		    
		})
		//.flatMap (r => r.body.position)
		//.map (pos => ({start: pos[0], length: pos[1]}))

	if (!ranges || !ranges.length){
		return;
	}
	myHilitor.apply(ranges)
	
	
	// Scroll to first found element
	var firstMarkedElement = context.getElementsByTagName("mark")[0]
	if (firstMarkedElement)
		firstMarkedElement.scrollIntoView();
		
	var contextContainer = context.parentElement.parentElement
		console.log(context)
		console.log(window.innerHeight + contextContainer.scrollTop, context.offsetHeight)
    // If the window is not scrolled already to bottom...
	if ((window.innerHeight + contextContainer.scrollTop) < context.offsetHeight) {
		// scroll up half the window height to get a bit more context on screen
		console.log("Scrolling up by", window.innerHeight/2) 
		contextContainer.scrollTop -= window.innerHeight/2
    }
    
}

function initHighlighting(){
	try {
		var checkbox = document.getElementById("toggleHighlightingCheckbox")
		checkbox.addEventListener('change', toggleHighlighting)
		
		console.log("checkbox checked was " + checkbox.checked)
		console.log("checkbox checked will be " + window.localStorage.getItem("highlighting"))
		
		if (window.localStorage.getItem("highlighting") === "true"){
			console.log("Checking")
			checkbox.checked = true;
			
			highlightSearchTerms()
		} else {
			console.log("Unchecking")
			checkbox.checked = false;
		}
		
		document.getElementById("search-icon").classList.add("toggleEnabled")
	} catch (e) {
		console.warn("Could not highlight search term", e)
	}
}
	
	
function applyCollapsing(){
	document.querySelectorAll(".collapsed").forEach(n => {
		let prev = n.previousElementSibling;
	    if (prev && prev.tagName.match(/^H.*/)){
	    	prev.classList.add("collapsible-caption");
	    }
	})
	
	document.querySelectorAll("pre code").forEach( node => collapsePreIfLarge(node.parentElement))
	document.querySelectorAll(".collapsed").forEach(collapse)
	
}

function expand(node){
	if (!node.classList.contains("collapsed"))
		return
		
	node.classList.remove("collapsed")
	node.classList.add("expanded")
	
	var collapseButton = document.createElement("span");
	collapseButton.classList.add("collapse-button")
	
	node.appendChild(collapseButton);
	
	collapseButton.addEventListener("click", e => {
		node.removeChild(e.currentTarget);
		collapse(node)
		e.stopPropagation();
	})
}

function collapsePreIfLarge(node){
	if (node.clientHeight > 400){
		node.classList.add("collapsed")
	}
}

function collapse(node){
	node.classList.remove("expanded")
	node.classList.add("collapsed")
	
	var eventListener = function(e){
		expand(e.currentTarget)
		node.removeEventListener("click", eventListener)
		// Don't open a link that might be hidden under the current mouse position
		e.stopPropagation()
		e.preventDefault()
	}
	
	node.addEventListener("click", eventListener )
}
function setupImageModal(){
	// Get the modal
	var modal = document.getElementById("imageModal");
	var modalImg = document.getElementById("modalImage");
	
	// Get the image and insert it inside the modal - use its "alt" text as a caption
	document.querySelectorAll("#full-content img").forEach(img => { 
		img.onclick = function(){
		  modal.style.display = "block";
		  modalImg.src = this.src;
		}
	});
	
	// Get the <span> element that closes the modal
	var span = document.getElementsByClassName("close")[0];
	
	// When the user clicks on <span> (x), close the modal
	modal.onclick = function(e) { 
		if (e.target != modalImg)
	  		modal.style.display = "none";
	}
}

function noModernJavascript(){
	var isIEorEdge = document.documentMode || /Edge/.test(navigator.userAgent);
	return isIEorEdge;
}

class NavFiltering {
	constructor(navElement) {
		this.navTree = new NavNode(navElement);
		this.previousText = "";
	}

	onNavFilterTextChanged(filterText) {
		if (filterText.length == 0) {
			this.navTree.forEach(node => node.unfilter());

		} else {
			if (this.previousText.length === 0) {
				this.navTree.forEach(node => node.markOpen());
			}
			this.navTree.filter(filterText);
		}
		this.previousText = filterText;
	}
}

// ul -> translates to Node[]
// ul.li -> Node
// ul.li.a -> Node.maybeLink
// ul.li.ul -> Node.maybeUl
class NavNode {
	constructor(navOrLi) {
		this.navOrLi = navOrLi;
		this.maybeLink = navOrLi.querySelector(":scope > a");
		this.maybeUl = navOrLi.querySelector(":scope > ul");
		this.children = this._createChildren();
		this.wasOpen = null;
	}

	_createChildren() {
		return [...this.navOrLi.querySelectorAll(":scope > ul > li")].map(li => new NavNode(li));
	}

	markOpen() {
		this.wasOpen = this.maybeUl == null || this.maybeUl.style.display != "none"
	}

	filter(text) {
		this._filter(text.toLowerCase());
	}

	_filter(text) {
		let passed = this.isNav();

		this.children.forEach(child => passed |= child._filter(text));
		if (this.maybeUl != null)
			this.maybeUl.style.display = this._displayValue(passed);

		passed |= this.maybeLink != null && this.maybeLink.text.toLowerCase().includes(text)

		this.navOrLi.style.display = this._displayValue(passed);
		
		return passed;
	}

	unfilter() {
		if (this.isNav())
			return;

		this.navOrLi.style.display = "block";

		if (this.maybeUl != null) 
			this.maybeUl.style.display = this._displayValue(this.wasOpen);
	}

	_displayValue(shouldDisplay) {
		return shouldDisplay ? "block" : "none"
	}

	forEach(callback) {
		callback(this);
		this.children.forEach(child => child.forEach(callback));
	}

	isNav() {
		return this.navOrLi.tagName.toLowerCase() == "nav";
	}

}

let navFiltering;

function onNavFilterTextChanged() {
	if (navFiltering == null)
		navFiltering = new NavFiltering(document.querySelector('nav.nav'));

	const filterText = document.getElementById("navFilter").value;
	navFiltering.onNavFilterTextChanged(filterText);
}

window.onload = function () {
	console.log("Init start");
	// define fixed points where inline code may break
	document.querySelectorAll('p>code, td>code, li>code').forEach(node => node.innerHTML = node.innerHTML.replace(/[a-z][/.=:]/g, s => s + '<wbr>'))
				
	if (searchText && !noModernJavascript())
		initHighlighting();

	$('.nav').navgoco({accordion: true});
	$('#technicalLinks :has(ul)')
		.show()
	$('#technicalLinks>ul>li')
		.addClass('open')
	$(".selflink").parentsUntil('.nav').show();
	$(".selflink").parent().parentsUntil('.nav').addClass('open');
	$('.inner-container .nav').show();

	
	applyCollapsing();
	
	setupImageModal();
	
	console.log("Init end");
}
