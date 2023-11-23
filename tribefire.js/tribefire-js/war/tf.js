//var window = { $T: {}, $tf: { module: { TribefireUxModuleContract: null } } };
export const $T = $wnd.$T;
export const $tf = $wnd.$tf;
export class TribefireUxModuleContract extends $wnd.$tf.module.TribefireUxModuleContract{
	constructor(){
		super();
	}
	
	/**
     * tbd.
     * @param {ComponentCreateContext} context - tbd
     * @param {JsUxComponent} denotation - tbd
     * @return {GmContentView} component - tbd
     */
	createComponent(context, denotation){};		
}
class ComponentCreateContext {
	/**
     * 
     * @param {JsUxComponent} denotation - tbd
     * @return {GmContentView} component - tbd
     */
	createComponent(denotation){};
}