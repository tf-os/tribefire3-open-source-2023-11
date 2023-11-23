function log(message) {
	console.log("contentViewDesigner: " + message); 
}

class GenericView extends $tf.view.GmActionContentView {

  widget;
  modelPath;
  accessId;
  session;
  readOnly;
  genericEntity; 
  pdUtil;
  explorerUtil;
  listenersMap;

  constructor(viewWidget, accessId) {
    super();	

	this.widget = viewWidget;
    this.accessId = accessId;
    this.pdUtil = new $pd.util.JsUtil();
    this.explorerUtil = new $tf.util.JsUtil();
    this.listenersMap = new Map();
  }
  
  setActionManager(actionManager) {
    this.widget.setActionManager(actionManager);
  }
  
  getGmContentViewActionManager() {
    return this.widget.getGmContentViewActionManager();
  }
  
  getExternalActions() {
    return this.widget.getExternalActions();
  }
  
  getActions() {
    try {
      const actions = this.widget.getActions();
      const newActions = this.explorerUtil.prepareActionProviderConfiguration(this, actions);
      return newActions;
    } catch(ex) {
      debugger;
    }
    return null;
  }
  
  isFilterExternalActions() {
    return this.widget.isFilterExternalActions();
  }
  
  configureExternalActions(externalActions) {
    this.widget.configureExternalActions(externalActions);
  }
  
  configureActionGroup(actionGroup) {
    this.widget.configureActionGroup(actionGroup);
  }
  
  addSelectionListener(listener) {
    const wrapperListener = this.pdUtil.createSelectionListener(listener, this);
    this.widget.addSelectionListener(wrapperListener);
    this.listenersMap.set(listener, wrapperListener);
  }
  
  removeSelectionListener(listener) {
    const wrapperListener = this.listenersMap.get(listener);
    this.widget.removeSelectionListener(wrapperListener);
    this.listenersMap.delete(listener);    
  }
  
  getCurrentSelection() {
    let curSelection;
    try {
      curSelection = this.widget.getCurrentSelection();
    } catch (err) {
      log("Error while getting the current Selection: " + err);
      debugger;
      return;
    }
    
    if (curSelection) {
      const list = this.explorerUtil.prepareList();
      curSelection.forEach(sel => {
        const lastEl = sel.last();
        const typeSignature = lastEl.getType().getTypeSignature(); 
        const entity = lastEl.getValue();
        let newEntity = null;
        if (entity && this.pdUtil.isEntity(entity))
          newEntity = this.explorerUtil.findEntityFromCache(this.session, entity, this.pdUtil.getReferenceByEntity(entity));
        if (newEntity) {
          const newPath = this.explorerUtil.prepareModelPath();
          const rootEl = this.explorerUtil.prepareRootPathElement(newEntity);
          this.explorerUtil.addElementToModelPath(newPath, rootEl);
          this.explorerUtil.addToCollection(newPath, list);
        }
      });
      
      return list;
    }
    
    return null;
  }
  
  getFirstSelectedItem() {
    let firstSelection;
    try {
      firstSelection = this.widget.getFirstSelectedItem();
    } catch (err) {
      log("Error while getting the first selection: " + err);
      debugger;
      return null;
    }
    
    if (!firstSelection)
      return null;
    
    const lastEl = firstSelection.last();
    const typeSignature = lastEl.getType().getTypeSignature();
    const entity = lastEl.getValue();
    
    if (entity && this.pdUtil.isEntity(entity)) {
      const newEntity = this.explorerUtil.findEntityFromCache(this.session, entity, this.pdUtil.getReferenceByEntity(entity));
      if (!newEntity)
      	return null;
      
      const newPath = this.explorerUtil.prepareModelPath();
      const rootEl = this.explorerUtil.prepareRootPathElement(newEntity);
      this.explorerUtil.addElementToModelPath(newPath, rootEl);
      return newPath;
    }
    
    return null;
  }

  configureGmSession(session) {
    this.session = session;    
    this.widget.configureGmSession(session);
  }

  configureUseCase(useCase) {
    this.widget.configureUseCase(useCase); 
  }

  getGmSession() {
    return this.session;
  }

  sendUxMessage(messageType, messageText, context) {
  	if (this.widget)
  		this.widget.sendUxMessage(messageType, messageText, context);
  }
  
  setContent(modelPath) {
    this.modelPath = modelPath;
    const newPath = this.updateContent(modelPath);

    try {
    if (this.genericEntity) {
      this.widget.setContent(newPath);
    } else {
      this.widget.setContent(null);
    }
    }catch(err) {
      log("Error while setting content: " + err);
    }      
  }
  
  updateContent(modelPath) {
    this.genericEntity = null;
    if (!modelPath)
      return null;

    const pathElement = modelPath.last();
    this.genericEntity = pathElement.getValue();
    if (!this.genericEntity)
      return null;

    const encodedEntity = this.explorerUtil.encodeData(this.genericEntity);
    const decodedEntity = this.pdUtil.decodeData(encodedEntity);
    
    const newPath = this.pdUtil.prepareModelPath();
    const rootEl = this.pdUtil.prepareRootPathElement(decodedEntity);
    this.pdUtil.addElementToModelPath(newPath, rootEl);
      
    return newPath;
  }

  addContent(modelPath) {
    //TODO: need to prepare the path same way as setContent
    this.widget.addContent(modelPath); 
  }

  isViewReady() {
    return true;
  }

  setReadOnly(readOnly) {
    this.readOnly = readOnly;
    this.widget.setReadOnly(readOnly);
  }  

  isReadOnly() {
	return this.readOnly;
  }

  getElement() {
    return this.widget.getUxElement();
  }

  getUxElement() {
    return this.widget.getUxElement();
  }

  getView() {
    return this;
  }
}

export default GenericView;