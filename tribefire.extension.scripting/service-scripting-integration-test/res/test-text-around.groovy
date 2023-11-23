text = $request.text;
$request.text = text.replaceAll("rr", "r-r");
return $proceedContext.proceed($request);
