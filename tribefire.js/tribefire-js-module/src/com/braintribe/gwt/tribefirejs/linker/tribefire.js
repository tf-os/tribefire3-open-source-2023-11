/*!
 * tribefire.js JavaScript Library v{tf-version}
 *
 * Copyright Braintribe IT
 *
 * Date: {tf-date}
 */
console.time('start-tribefire-js');
var $wnd = window;
var $doc = $wnd.document;
{tf-script}
$wnd.$tf.version = function(){
	return "{tf-version}";
}
console.timeEnd('start-tribefire-js');
console.log("tribefire.js JavaScript Library {tf-version}");