var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __classPrivateFieldSet = (this && this.__classPrivateFieldSet) || function (receiver, state, value, kind, f) {
    if (kind === "m") throw new TypeError("Private method is not writable");
    if (kind === "a" && !f) throw new TypeError("Private accessor was defined without a setter");
    if (typeof state === "function" ? receiver !== state || !f : !state.has(receiver)) throw new TypeError("Cannot write private member to an object whose class did not declare it");
    return (kind === "a" ? f.call(receiver, value) : f ? f.value = value : state.set(receiver, value)), value;
};
var __classPrivateFieldGet = (this && this.__classPrivateFieldGet) || function (receiver, state, kind, f) {
    if (kind === "a" && !f) throw new TypeError("Private accessor was defined without a getter");
    if (typeof state === "function" ? receiver !== state || !f : !state.has(receiver)) throw new TypeError("Cannot read private member from an object whose class did not declare it");
    return kind === "m" ? f : kind === "a" ? f.call(receiver) : f ? f.value : state.get(receiver);
};
var _HxLocalEvaluator_application, _HxLocalEvaluator_processors, _HxLocalEvaluator_dispatchingRegistries, _HxEvalContext_hxEvaluator, _HxEvalContext_request, _HxEvalContext_mapAttrContext;
import { attr, eval_, reason, service, util } from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import { DispatchableRequest } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";
import { UnsupportedOperation } from "../com.braintribe.gm.essential-reason-model-2.0~/ensure-essential-reason-model.js";
import { HxView, HxWindowCustomizability } from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";
var Maybe = reason.Maybe;
export class HxLocalEvaluator extends eval_.EmptyEvaluator {
    constructor(application) {
        super();
        _HxLocalEvaluator_application.set(this, void 0);
        _HxLocalEvaluator_processors.set(this, util.newDenotationMap());
        _HxLocalEvaluator_dispatchingRegistries.set(this, new Map());
        __classPrivateFieldSet(this, _HxLocalEvaluator_application, application, "f");
    }
    eval(request) {
        return new HxEvalContext(this, request);
    }
    /*
     * ServiceProcessorBinder
     */
    bindFunction(denotationType, processor) {
        return this.bind(denotationType, this.serviceProcessor(processor));
    }
    bind(denotationType, processor) {
        __classPrivateFieldGet(this, _HxLocalEvaluator_processors, "f").put(denotationType, processor);
        return {
            unbind: () => {
                __classPrivateFieldGet(this, _HxLocalEvaluator_processors, "f").remove(denotationType);
            }
        };
    }
    bindDispatchingFunction(denotationType, serviceId, processor) {
        return this.bindDispatching(denotationType, serviceId, this.serviceProcessor(processor));
    }
    bindDispatching(denotationType, serviceId, processor) {
        let dispatchingRegistry = this.acquireDispatchingRegistry(serviceId);
        if (!dispatchingRegistry)
            __classPrivateFieldGet(this, _HxLocalEvaluator_dispatchingRegistries, "f").set(serviceId, dispatchingRegistry = new DispatchingRegistry());
        dispatchingRegistry.bind(denotationType, processor);
        return {
            unbind: () => {
                dispatchingRegistry.processors.remove(denotationType);
                if (dispatchingRegistry.processors.isEmpty())
                    __classPrivateFieldGet(this, _HxLocalEvaluator_dispatchingRegistries, "f").delete(serviceId);
            }
        };
    }
    serviceProcessor(lambda) {
        return { process: (ctx, req) => lambda(ctx, req) };
    }
    acquireDispatchingRegistry(serviceId) {
        let dispatchingRegistry = __classPrivateFieldGet(this, _HxLocalEvaluator_dispatchingRegistries, "f").get(serviceId);
        if (!dispatchingRegistry)
            __classPrivateFieldGet(this, _HxLocalEvaluator_dispatchingRegistries, "f").set(serviceId, dispatchingRegistry = new DispatchingRegistry());
        return dispatchingRegistry;
    }
    bindDialogProcessor(requestType, dialogDenotation) {
        return this.bindFunction(requestType, (ctx, req) => __awaiter(this, void 0, void 0, function* () {
            const rootScope = __classPrivateFieldGet(this, _HxLocalEvaluator_application, "f").getRootScope();
            const dialogImpl = yield rootScope.resolveRequestDialog(dialogDenotation);
            const customizability = dialogDenotation.windowCustomizability;
            const resizable = customizability == HxWindowCustomizability.resizable;
            const draggable = customizability == HxWindowCustomizability.draggable;
            const dialogElement = dialogImpl.htmlElement();
            dialogElement.style.boxSizing = "border-box";
            dialogElement.style.margin = "0";
            dialogElement.style.width = "100%";
            dialogElement.style.height = "100%";
            const dialogWrapper = document.createElement("div");
            dialogWrapper.style.boxSizing = "border-box"; // this makes computation easier
            dialogWrapper.classList.add("hx-dialog", "hx-flex-column");
            if (!dialogDenotation.modal)
                dialogWrapper.style.pointerEvents = "all";
            const titleElement = document.createElement("h3");
            titleElement.textContent = dialogDenotation.title;
            dialogWrapper.appendChild(titleElement);
            dialogWrapper.appendChild(dialogElement);
            const fullWindowWrapper = document.createElement("div");
            fullWindowWrapper.classList.add("hx-dialog-overlay", "hx-full-window", ...this.cssClassNamesFor(dialogDenotation));
            fullWindowWrapper.classList.add(dialogDenotation.modal ? "hx-modal" : "hx-non-modal");
            fullWindowWrapper.appendChild(dialogWrapper);
            dialogWrapper.style.left;
            document.body.appendChild(fullWindowWrapper);
            if (resizable || draggable)
                // in order to be able to use window.getComputedStyle(dialogWrapper) we need to call this after it has been attached to the document
                makeDraggableAndMaybeResizable(dialogWrapper, titleElement, resizable);
            const result = yield dialogImpl.process(ctx, req);
            document.body.removeChild(fullWindowWrapper);
            __classPrivateFieldGet(this, _HxLocalEvaluator_application, "f").releaseComponent(dialogImpl);
            return result;
        }));
    }
    cssClassNamesFor(dialogDenotation) {
        const superTypes = dialogDenotation.EntityType()
            .getTransitiveSuperTypes(true, true)
            .iterable();
        return [...superTypes]
            .filter((superType) => HxView.isAssignableFrom(superType))
            .map((superType) => superType.getShortName());
    }
    resolveProcessor(request) {
        let result;
        if (DispatchableRequest.isInstance(request))
            result = this.resolveDispatchingProcessor(request);
        if (!result)
            result = __classPrivateFieldGet(this, _HxLocalEvaluator_processors, "f").find(request);
        return result ? Maybe.complete(result) : Maybe.empty(this.noProcessorFoundFor(request));
    }
    resolveDispatchingProcessor(request) {
        if (!request.serviceId)
            return null;
        const registry = __classPrivateFieldGet(this, _HxLocalEvaluator_dispatchingRegistries, "f").get(request.serviceId);
        if (!registry)
            return null;
        return registry.processors.find(request);
    }
    noProcessorFoundFor(request) {
        let msg = "No ServiceProcessor found for request of type [" + request.EntityType().getTypeSignature() + "]";
        if (DispatchableRequest.isInstance(request)) {
            const dr = request;
            if (dr.serviceId)
                msg += " with serviceId [" + request.serviceId + "]";
            else
                msg += ", a DispatchableRequest with no serviceId (maybe that's the problem?)";
        }
        msg += " -- Request: " + request;
        return reason.build(UnsupportedOperation).text(msg).toReason();
    }
}
_HxLocalEvaluator_application = new WeakMap(), _HxLocalEvaluator_processors = new WeakMap(), _HxLocalEvaluator_dispatchingRegistries = new WeakMap();
class DispatchingRegistry {
    constructor() {
        this.processors = util.newDenotationMap();
    }
    bind(denotationType, processor) {
        this.processors.put(denotationType, processor);
    }
}
class HxEvalContext extends eval_.EmptyEvalContext {
    constructor(evaluator, request) {
        super();
        _HxEvalContext_hxEvaluator.set(this, void 0);
        _HxEvalContext_request.set(this, void 0);
        _HxEvalContext_mapAttrContext.set(this, new attr.MapAttributeContext(null));
        __classPrivateFieldSet(this, _HxEvalContext_hxEvaluator, evaluator, "f");
        __classPrivateFieldSet(this, _HxEvalContext_request, request, "f");
    }
    andGet() {
        return __awaiter(this, void 0, void 0, function* () {
            const maybe = yield this.andGetReasoned();
            return maybe.get();
        });
    }
    andGetReasoned() {
        const maybeProcessor = __classPrivateFieldGet(this, _HxEvalContext_hxEvaluator, "f").resolveProcessor(__classPrivateFieldGet(this, _HxEvalContext_request, "f"));
        if (!maybeProcessor.isSatisfied())
            return Promise.resolve(maybeProcessor.cast());
        const processor = maybeProcessor.get();
        const context = this.newServiceRequestContext();
        return processor.process(context, __classPrivateFieldGet(this, _HxEvalContext_request, "f"));
    }
    getReasoned(callback) {
        this.andGetReasoned()
            .then(value => callback.onSuccess(value))
            .catch(e => callback.onFailure(e));
    }
    get(callback) {
        this.andGet()
            .then(value => callback.onSuccess(value))
            .catch(e => callback.onFailure(e));
    }
    newServiceRequestContext() {
        const context = new service.StandardServiceRequestContext(this.parentAttributeContext(), __classPrivateFieldGet(this, _HxEvalContext_hxEvaluator, "f"));
        context.setEvaluator(__classPrivateFieldGet(this, _HxEvalContext_hxEvaluator, "f"));
        for (const entry of this.streamAttributes().iterable())
            context.setAttribute(entry.attribute(), entry.value());
        return context;
    }
    parentAttributeContext() {
        const pac = eval_.ParentAttributeContextAspect.$.find(this);
        return pac.isPresent() ? pac.get() : attr.AttributeContexts.peek();
    }
    getReasonedSynchronous() {
        throw new Error("Synchronous evaluation is not supported!");
    }
    getSynchronous() {
        throw new Error("Synchronous evaluation is not supported!");
    }
    setAttribute(attribute, value) {
        __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").setAttribute(attribute, value);
    }
    findAttribute(attribute) {
        return __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").findAttribute(attribute);
    }
    getAttribute(attribute) {
        return __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").getAttribute(attribute);
    }
    findOrDefault(attribute, defaultValue) {
        return __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").findOrDefault(attribute, defaultValue);
    }
    findOrNull(attribute) {
        return __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").findOrNull(attribute);
    }
    findOrSupply(attribute, defaultValueSupplier) {
        return __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").findOrSupply(attribute, defaultValueSupplier);
    }
    streamAttributes() {
        return __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").streamAttributes();
    }
}
_HxEvalContext_hxEvaluator = new WeakMap(), _HxEvalContext_request = new WeakMap(), _HxEvalContext_mapAttrContext = new WeakMap();
function makeDraggableAndMaybeResizable(element, clickable, resizable) {
    // resizing border is made of resizing divs - they form an invisible border of he element's box (i.e. content+border)
    const RESIZING_BORDER_SIZE = "15px";
    // the point you start dragging can only be moved this number of pixels close to the window border
    const DRAG_BORDER_PROTECTION = 2;
    // min size the element can be resized to
    const MIN_ELEMENT_WIDTH = 100;
    const MIN_ELEMENT_HEIGHT = 100;
    const style = window.getComputedStyle(element);
    if (style.position === "static") {
        element.style.position = "relative";
        element.style.left = "0px";
        element.style.top = "0px";
    }
    /** There is a weird behavior in browser (Chrome at least), that if you select text, press mouse button,
     * drag and release the mouse button, you don't get the onmouseup event. You need to click to get that.
     * So if you accidentally selected the clickable text, then the window would move after mouse up until you click.
     * Super annoying. Disabling userSelect prevents this from happening. */
    clickable.style.userSelect = "none";
    clickable.style.cursor = "move";
    clickable.addEventListener("mousedown", (e) => startDragging(e, true, true, 0, 0));
    if (resizable)
        makeElementResizable();
    // while resizing, we set userSelect to none, otherwise text keeps getting selected and everything is broken shortly after
    const originalUserSelect = element.style.userSelect;
    let isActivated = false;
    function makeElementResizable() {
        const negativeSize = (size) => size ? "-" + size : 0;
        // If there is a border around element, we want to move the resizing divs to align with the border
        const left = negativeSize(style.borderLeftWidth);
        const right = negativeSize(style.borderRightWidth);
        const top = negativeSize(style.borderTopWidth);
        const bottom = negativeSize(style.borderBottomWidth);
        createResizingDiv(/*NW*/ "nwse-resize", false, false, (s) => { s.left = left; s.top = top; }, true, true, -1, -1);
        createResizingDiv(/*N */ "ns-resize", true, false, (s) => { s.left = left; s.top = top; }, false, true, 0, -1);
        createResizingDiv(/*NE*/ "nesw-resize", false, false, (s) => { s.right = right; s.top = top; }, false, true, 1, -1);
        createResizingDiv(/*E */ "ew-resize", false, true, (s) => { s.right = right; s.top = top; }, false, false, 1, 0);
        createResizingDiv(/*SE*/ "nwse-resize", false, false, (s) => { s.right = right; s.bottom = bottom; }, false, false, 1, 1);
        createResizingDiv(/*S */ "ns-resize", true, false, (s) => { s.left = left; s.bottom = bottom; }, false, false, 0, 1);
        createResizingDiv(/*SW*/ "nesw-resize", false, false, (s) => { s.left = left; s.bottom = bottom; }, true, false, -1, 1);
        createResizingDiv(/*W */ "ew-resize", false, true, (s) => { s.left = left; s.top = top; }, true, false, -1, 0);
        function createResizingDiv(cursor, fullWidth, fullHeight, styling, moveLeft, moveTop, changeWidth, changeHeight) {
            const result = document.createElement("div");
            styling(result.style);
            result.style.position = "absolute";
            result.style.width = fullWidth ? "100%" : RESIZING_BORDER_SIZE;
            result.style.height = fullHeight ? "100%" : RESIZING_BORDER_SIZE;
            result.style.cursor = cursor;
            // make sure corners are above horizontal/vertical divs
            result.style.zIndex = (!fullWidth && !fullHeight) ? "1" : "0";
            result.addEventListener("mousedown", (e) => startDragging(e, moveLeft, moveTop, changeWidth, changeHeight));
            element.appendChild(result);
            return result;
        }
    }
    function startDragging(e, moveLeft, moveTop, changeWidth, changeHeight) {
        if (isActivated)
            return;
        isActivated = true;
        element.style.userSelect = "none";
        const startMouseX = e.clientX;
        const startMouseY = e.clientY;
        const bcr = element.getBoundingClientRect();
        const startLeft = Math.round(bcr.left);
        const startTop = Math.round(bcr.top);
        const startWidth = Math.round(bcr.width);
        const startHeight = Math.round(bcr.height);
        document.documentElement.addEventListener('mousemove', move);
        document.documentElement.addEventListener('mouseup', stopDragging);
        function move(e) {
            // Doing the min/max with DRAG_BORDER_PROTECTION and e.clientX/Y means dragging cursor outside of browser window doesn't have an effect, 
            // so the dialog cannot be dragged away to the point it cannot be used anymore (buttons/title being out of window, thus not clickable)
            const inVisibleRange = (n, max) => Math.min(Math.max(DRAG_BORDER_PROTECTION, n), max - DRAG_BORDER_PROTECTION);
            const diffX = inVisibleRange(e.clientX, document.body.clientWidth) - startMouseX;
            const diffY = inVisibleRange(e.clientY, document.body.clientHeight) - startMouseY;
            if (moveLeft)
                element.style.left = startLeft + diffX + "px";
            if (moveTop)
                element.style.top = startTop + diffY + "px";
            if (changeWidth)
                element.style.width = Math.max(MIN_ELEMENT_WIDTH, startWidth + changeWidth * diffX) + "px";
            if (changeHeight)
                element.style.height = Math.max(MIN_ELEMENT_HEIGHT, startHeight + changeHeight * diffY) + "px";
        }
        function stopDragging() {
            isActivated = false;
            element.style.userSelect = originalUserSelect;
            document.documentElement.removeEventListener('mousemove', move);
            document.documentElement.removeEventListener('mouseup', stopDragging);
        }
    }
}
// for local-only case (for now just push notifications) we set a RequestedEndpointAspect to imply no fallback via remotifier is desired
// otherwise, build an evaluator that tries locally first, if no processor found, either return reason (in case push notification) or
