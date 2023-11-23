import "../tribefire.extension.web-components.web-components-1.0~/app-configuration/index.js";

// TODO: clean this after next core-stable release -
const BaseClass = $tf.view.GmContentView ?? $tf.view.AbstractGmContentView;
console.log({ BaseClass });
class GenericView extends BaseClass {
  myApp;
  topEl;
  session;
  genericEntity;
  gmeUtil;
  modelPath;
  isAppReady;
  isChunkReady;
  appUrl;
  cssList;

  constructor(parentDocument, appUrl, cssList) {
    super();

    this.isAppReady = false;
    this.isChunkReady = false;
    this.appUrl = appUrl;
    this.cssList = cssList;

    if (parentDocument == null) this.init(document);
    else this.init(parentDocument);
  }

  init(parentDocument) {
    this.topEl = parentDocument.createElement("app-configuration");
    this.topEl.id = "js-content-view";
    this.topEl.slot = this.appUrl;

    this.gmeUtil = new $tf.util.JsUtil();

    this.gmeUtil.addToCollection(this.appUrl + "css/app.css", this.cssList);

    this.isAppReady = true;
    this.isChunkReady = true;
  }

  configureApp() {
    this.myApp.$children[0].configureGmContentView(this.getView());
  }

  injectScript(src, id, destinationEl) {
    return new Promise((resolve, reject) => {
      const script = document.createElement("script");
      script.src = src;
      script.id = id;
      script.addEventListener("load", resolve);
      script.addEventListener("error", (e) => reject(e.error));
      destinationEl.appendChild(script);
    });
  }

  updateAppElement() {
    // const el = this.topEl.firstChild;
    // this.myApp = this.topEl;
  }

  configureGmSession(session) {
    this.session = session;

    if (!this.myApp) this.updateAppElement();
    if (!this.myApp) return;

    this.myApp.$children[0].configureGmSession(this.session);
    this.configureApp();
  }

  getGmSession() {
    return this.session;
  }

  setContent(modelPath) {
    this.modelPath = modelPath;
    this.updateContent(modelPath);

    // const canEdit = this.genericEntity?.Session()
    //   .getSessionAuthorization()
    //   .getUserRoles()
    //   .toArray()
    //   .includes('vgn-admin');

    const canEdit = true;

    this.topEl.content = this.genericEntity;
    this.topEl.can_add_localization = canEdit;
    this.topEl.can_edit_localization_name = canEdit;
    this.topEl.can_add_localization_key = canEdit;
    this.topEl.can_edit_localization_key = canEdit;
    this.topEl.can_add_theme = canEdit;
    this.topEl.can_edit_theme_name = canEdit;
    this.topEl.can_add_theme_key = canEdit;
    this.topEl.can_edit_theme_key = canEdit;
  }

  updateContent(modelPath) {
    this.genericEntity = null;
    if (modelPath) {
      const pathElement = this.gmeUtil.getLastElement(modelPath);
      if (pathElement) {
        this.genericEntity = this.gmeUtil.getElementValue(pathElement);
      }
    }
  }

  prepareModelPath(entity) {
    if (!entity) return null;

    let path = this.gmeUtil.prepareModelPath();
    let element = this.gmeUtil.prepareRootPathElement(entity);
    this.gmeUtil.addElementToModelPath(path, element);

    return path;
  }

  deselectAll() {
    if (this.myApp) this.myApp.$children[0].deselectAll();
  }

  addSelectionListener(listener) {
    if (!this.myApp) this.updateAppElement();
    if (!this.myApp) return;

    this.myApp.$children[0].addSelectionListener(listener);
  }

  removeSelectionListener(listener) {
    if (!this.myApp) this.updateAppElement();
    if (!this.myApp) return;

    this.myApp.$children[0].removeSelectionListener(listener);
  }

  getFirstSelectedItem() {
    if (!this.myApp) return null;

    var entity = this.myApp.$children[0].getFirstSelectedItem();
    var path = this.prepareModelPath(entity);
    return path;
  }

  getCurrentSelection() {
    if (!this.myApp) return null;

    var elements = this.myApp.$children[0].getCurrentSelection();
    if (!elements || elements.length == 0) return null;

    var paths = this.gmeUtil.prepareList();
    for (var i = 0; i < elements.length; i++) {
      var path = this.prepareModelPath(elements[i]);
      this.gmeUtil.addToCollection(path, paths);
    }

    return paths;
  }

  getView() {
    return this;
  }

  getElement() {
    return this.topEl;
  }

  getUxElement() {
    return this.getElement();
  }

  detachUxElement() {
    //const el  = this.getElement();
    //el.parentNode.removeChild(el);
  }

  getContentPath() {
    return this.modelPath;
  }

  isViewReady() {
    return this.isAppReady && this.isChunkReady;
  }
}

export default GenericView;
