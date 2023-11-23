window.handleOnload = function(img){
	s = img.style;
	s.objectFit = "contain";
	s2 = img.parentElement.style;
	s.width = "";
	s.height = "";
	max = parseInt(img.getAttribute("maxheight"));
	if((img.width/img.height) == 1) {
		w = Math.min(max, img.width);
		h = Math.min(max, img.height);
		s.width = w + "px";
		s.height= h + "px";
		s2.alignItems = "center";
	}else {
		if(img.width > max)
			s.width = max + "px";
		else
			s.width = img.width;
		s.height = "";
		if((img.width/img.height) < 1)
			s2.alignItems = "flex-start";
		if((img.width/img.height) > 1)
			s2.alignItems = "center";
	}
}

window.changePreview = function(el, attr, showVideo){
	uniqueid = el.getAttribute("uniqueid");
	img = document.getElementById("thumbnailPreview-"+uniqueid);
	video = document.getElementById("thumbnailPreviewVideo-"+uniqueid);
	if(img && video){
		if(showVideo){
			video.currentTime = 0;
			video.style = "";
			video.style.display = "block";
			if(img.width > img.height)
				video.style.maxWidth = "200px";
			else
				video.style.maxHeight = "200px";
			img.style.display = "none";
			if(!video.src){			
				evl = function() {
    				video.poster = img.getAttribute(attr);    				
    				video.removeEventListener("error", evl);
				};
				video.addEventListener("error",evl);
				video.src = img.getAttribute(attr) + "&preferredMimeType=video/mp4";
			}
		}else{
			video.style.display = "none";
			img.style.display = "block";	
		}
	}
}

/*
function changePreview(el, attr){
	uniqueid = el.getAttribute("uniqueid");
	img = document.getElementById("thumbnailPreview-"+uniqueid);	
	if(img){
		newsrc = img.getAttribute(attr);
		if(img.getAttribute("src") != newsrc){		
			img.setAttribute("src", newsrc);
		}
	}		
}

window.changePreview = function(el, attr, showVideo){
	uniqueid = el.getAttribute("uniqueid");
	img = document.getElementById("thumbnailPreview-"+uniqueid);
	if(img){
		newsrc = img.getAttribute(attr);		
		if(showVideo){
			video = document.getElementById("thumbnailPreviewVideo-"+uniqueid);	
			if(video == null){		
				var xhttp = new XMLHttpRequest();
				xhttp.onreadystatechange = function() {
				    if (this.readyState == 4) {
				    	if(this.status == 200 && this.getResponseHeader("content-type") == "video/mp4"){
				    		window.showVideo(uniqueid);
				    	}else{
				    		img.style.display = "flex";	
				    		if(img.getAttribute("src") != newsrc){	
								img.setAttribute("src", newsrc);
							}
						}
				    }
				};
				xhttp.open("GET", newsrc + "&preferredMimeType=video/mp4", true);
				xhttp.send();
			}else{
				video.currentTime = 0;
				video.style.display = "flex";
				img.style.display = "none";
			}			
		}else{
			video = document.getElementById("thumbnailPreviewVideo-"+uniqueid);	
			if(video != null){
				video.style.display = "none";					
			}	
			img.style.display = "flex";		
			if(img.getAttribute("src") != newsrc){					
				img.setAttribute("src", newsrc);
			}
		}
	}	
}

window.showVideo = function(uniqueid){
	video = document.getElementById("thumbnailPreviewVideo-"+uniqueid);	
	if(video == null){
		video = document.createElement("video");
		video.volume = 0;
		video.loop = "loop";
		video.id = "thumbnailPreviewVideo-"+uniqueid;
		video.autoplay = "autoplay";
		video.classList.add("thumbnailPanelPreviewVideo");
		video.src = newsrc + "&preferredMimeType=video/mp4";
		img.parentElement.appendChild(video);
	}
	video.currentTime = 0;
	video.style.display = "flex";
	img.style.display = "none";
}
*/