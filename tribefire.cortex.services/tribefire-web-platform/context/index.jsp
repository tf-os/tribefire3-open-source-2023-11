<html>

<head>
	<% 
		String tfsUrl = com.braintribe.model.processing.bootstrapping.TribefireRuntime.getProperty("TRIBEFIRE_LANDING_PAGE_URL", "home"); 
	%>
    <meta http-equiv="refresh" content=0;URL="<%out.print(tfsUrl);%>">
</head>

<body>
</body>

</html>
