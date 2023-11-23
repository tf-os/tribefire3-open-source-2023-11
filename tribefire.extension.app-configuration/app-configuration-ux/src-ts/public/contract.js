import GenericView from "./contentView.js";

class Contract extends $tf.module.TribefireUxModuleContract { 
    //constructor() {
    //  super();
    //}    

    createComponent(context, denotation) {
        if (denotation) 
            console.log(denotation); 

        let appUrl = "";
        let cssList = null;
        if (context) {
            console.log(context); 
            cssList = context.cssStyles;

            if (context.modulePath) {
                appUrl = context.modulePath.replace(/[^\/]*$/, '');  //extract path
                //context.modulePath.replace(/^.*(\\|\/|\:)/, '');   //extract FileName
            }
        }


        const view = new GenericView(document, appUrl, cssList);
        return view;
    }
}

export default Contract;
  