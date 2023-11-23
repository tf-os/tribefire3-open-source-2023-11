<!DOCTYPE html>
<html>
<title>tribefireJs</title>
	<head>
        <link rel="stylesheet" type="text/css" href="styles.css"/>
        <script type="text/javascript" src="tribefire.js"></script>
        
        <style>
            html,body,iframe {padding:0;margin:0;height:100%;width:100%}
            iframe {border:none}
        </style>
        
        <script>
            window.onload=function(){
                queryString = window.location.search;
                urlParams = new URLSearchParams(queryString);
                path = urlParams.get('path')
                console.log(path);
                
                iframe = document.getElementById("tfjs-app");
                script = iframe.contentWindow.document.createElement("script");
                script.type = "module";

                script.src = path;
                iframe.contentWindow.document.head.appendChild(script);
            }
        </script>	
	</head>
	
	<body>	
	   <iframe id="tfjs-app"></iframe>		
	</body>	
</html>
