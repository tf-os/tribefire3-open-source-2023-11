import { async, attr, eval_, reason, reflection, service, util } from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";

import { IHxApplication, IHxServiceProcessor, IHxServiceProcessorBinder, IHxServiceProcessorBinding } from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";

import { DispatchableRequest, ServiceRequest } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";
import { Reason } from "../com.braintribe.gm.gm-core-api-2.0~/ensure-gm-core-api.js";
import { UnsupportedOperation } from "../com.braintribe.gm.essential-reason-model-2.0~/ensure-essential-reason-model.js";
import { HxRequestDialog, HxView, HxWindowCustomizability } from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";

import Maybe = reason.Maybe;

export class HxLocalEvaluator extends eval_.EmptyEvaluator<ServiceRequest> implements IHxServiceProcessorBinder {
    readonly #application: IHxApplication;
    readonly #processors = util.newDenotationMap<ServiceRequest, IHxServiceProcessor<ServiceRequest, any>>();
    readonly #dispatchingRegistries = new Map<string, DispatchingRegistry>();

    constructor(application: IHxApplication) {
        super();
        this.#application = application;
    }
    eval<T>(request: ServiceRequest): eval_.JsEvalContext<T> {
        return new HxEvalContext(this, request);
    }

    /*
     * ServiceProcessorBinder
     */

    bindFunction<T extends ServiceRequest>(denotationType: reflection.EntityType<T>, processor: (context: service.ServiceRequestContext, request: T) => Promise<reason.Maybe<any>>): IHxServiceProcessorBinding {
        return this.bind(denotationType, this.serviceProcessor(processor));
    }

    bind<T extends ServiceRequest>(
        denotationType: reflection.EntityType<T>, processor: IHxServiceProcessor<T, any>): IHxServiceProcessorBinding {

        this.#processors.put(denotationType, processor);

        return {
            unbind: () => {
                this.#processors.remove(denotationType);
            }
        }
    }

    bindDispatchingFunction<T extends DispatchableRequest>(denotationType: reflection.EntityType<T>, serviceId: string, processor: (context: service.ServiceRequestContext, request: T) => Promise<reason.Maybe<any>>): IHxServiceProcessorBinding {
        return this.bindDispatching(denotationType, serviceId, this.serviceProcessor(processor));
    }

    bindDispatching<T extends DispatchableRequest>(
        denotationType: reflection.EntityType<T>, serviceId: string, processor: IHxServiceProcessor<T, any>): IHxServiceProcessorBinding {

        let dispatchingRegistry = this.acquireDispatchingRegistry(serviceId);
        if (!dispatchingRegistry)
            this.#dispatchingRegistries.set(serviceId, dispatchingRegistry = new DispatchingRegistry());

        dispatchingRegistry.bind(denotationType, processor);

        return {
            unbind: () => {
                dispatchingRegistry.processors.remove(denotationType);
                if (dispatchingRegistry.processors.isEmpty())
                    this.#dispatchingRegistries.delete(serviceId);
            }
        }
    }

    private serviceProcessor<SR extends ServiceRequest, R>(lambda: (ctx: service.ServiceRequestContext, req: SR) => Promise<Maybe<R>>): IHxServiceProcessor<SR, R> {
        return { process: (ctx, req) => lambda(ctx, req) }
    }


    private acquireDispatchingRegistry(serviceId: string): DispatchingRegistry {
        let dispatchingRegistry = this.#dispatchingRegistries.get(serviceId);
        if (!dispatchingRegistry)
            this.#dispatchingRegistries.set(serviceId, dispatchingRegistry = new DispatchingRegistry());
        return dispatchingRegistry;
    }

    bindDialogProcessor<T extends ServiceRequest>(
        requestType: reflection.EntityType<T>, dialogDenotation: HxRequestDialog): IHxServiceProcessorBinding {

        return this.bindFunction(requestType, async (ctx, req) => {
            const rootScope = this.#application.getRootScope();
            const dialogImpl = await rootScope.resolveRequestDialog(dialogDenotation);

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
                dialogWrapper.style.pointerEvents = "all"

            const titleElement = document.createElement("h3");
            titleElement.textContent = dialogDenotation.title;

            dialogWrapper.appendChild(titleElement);
            dialogWrapper.appendChild(dialogElement)

            const fullWindowWrapper = document.createElement("div");
            fullWindowWrapper.classList.add("hx-dialog-overlay", "hx-full-window", ...this.cssClassNamesFor(dialogDenotation));
            fullWindowWrapper.classList.add(dialogDenotation.modal ? "hx-modal" : "hx-non-modal");
            fullWindowWrapper.appendChild(dialogWrapper);

            dialogWrapper.style.left

            document.body.appendChild(fullWindowWrapper);

            if (resizable || draggable)
                // in order to be able to use window.getComputedStyle(dialogWrapper) we need to call this after it has been attached to the document
                makeDraggableAndMaybeResizable(dialogWrapper, titleElement, resizable);

            const result = await dialogImpl.process(ctx, req);

            document.body.removeChild(fullWindowWrapper);
            this.#application.releaseComponent(dialogImpl);

            return result;
        });

    }

    cssClassNamesFor(dialogDenotation: HxRequestDialog): string[] {
        const superTypes = dialogDenotation.EntityType()
            .getTransitiveSuperTypes(true, true)
            .iterable();

        return [...superTypes]
            .filter((superType) => HxView.isAssignableFrom(superType))
            .map((superType) => superType.getShortName());
    }

    resolveProcessor(request: ServiceRequest): Maybe<IHxServiceProcessor<ServiceRequest, any>> {
        let result: IHxServiceProcessor<ServiceRequest, any>;
        if (DispatchableRequest.isInstance(request))
            result = this.resolveDispatchingProcessor(request as DispatchableRequest);

        if (!result)
            result = this.#processors.find(request);

        return result ? Maybe.complete(result) : Maybe.empty(this.noProcessorFoundFor(request));
    }

    private resolveDispatchingProcessor(request: DispatchableRequest): IHxServiceProcessor<ServiceRequest, any> {
        if (!request.serviceId)
            return null;

        const registry = this.#dispatchingRegistries.get(request.serviceId);
        if (!registry)
            return null;

        return registry.processors.find(request);
    }

    private noProcessorFoundFor(request: ServiceRequest): Reason {
        let msg = "No ServiceProcessor found for request of type [" + request.EntityType().getTypeSignature() + "]";
        if (DispatchableRequest.isInstance(request)) {
            const dr = request as DispatchableRequest;
            if (dr.serviceId)
                msg += " with serviceId [" + (request as DispatchableRequest).serviceId + "]";
            else
                msg += ", a DispatchableRequest with no serviceId (maybe that's the problem?)";
        }

        msg += " -- Request: " + request;

        return reason.build(UnsupportedOperation).text(msg).toReason();
    }
}

class DispatchingRegistry {
    readonly processors = util.newDenotationMap<ServiceRequest, IHxServiceProcessor<ServiceRequest, any>>();

    bind<T extends ServiceRequest>(
        denotationType: reflection.EntityType<T>, processor: IHxServiceProcessor<ServiceRequest, any>): void {

        this.processors.put(denotationType, processor);
    }

}

class HxEvalContext<T> extends eval_.EmptyEvalContext<T> implements eval_.JsEvalContext<T>  {

    readonly #hxEvaluator: HxLocalEvaluator;
    readonly #request: ServiceRequest;
    readonly #mapAttrContext = new attr.MapAttributeContext(null);

    constructor(evaluator: HxLocalEvaluator, request: ServiceRequest) {
        super();
        this.#hxEvaluator = evaluator;
        this.#request = request;
    }

    async andGet(): Promise<T> {
        const maybe = await this.andGetReasoned();
        return maybe.get();
    }

    andGetReasoned(): Promise<reason.Maybe<T>> {
        const maybeProcessor = this.#hxEvaluator.resolveProcessor(this.#request);

        if (!maybeProcessor.isSatisfied())
            return Promise.resolve(maybeProcessor.cast());

        const processor = maybeProcessor.get();
        const context = this.newServiceRequestContext();

        return processor.process(context, this.#request);
    }

    override getReasoned(callback: async.AsyncCallback<reason.Maybe<T>>): void {
        this.andGetReasoned()
            .then(value => callback.onSuccess(value))
            .catch(e => callback.onFailure(e))
    }

    override get(callback: $tf.async.AsyncCallback<T>): void {
        this.andGet()
            .then(value => callback.onSuccess(value))
            .catch(e => callback.onFailure(e))
    }

    private newServiceRequestContext(): service.ServiceRequestContext {
        const context = new service.StandardServiceRequestContext(this.parentAttributeContext(), this.#hxEvaluator);
        context.setEvaluator(this.#hxEvaluator);

        for (const entry of this.streamAttributes().iterable())
            context.setAttribute(entry.attribute(), entry.value());

        return context;
    }

    private parentAttributeContext(): attr.AttributeContext {
        const pac = eval_.ParentAttributeContextAspect.$.find(this);
        return pac.isPresent() ? pac.get() as attr.AttributeContext : attr.AttributeContexts.peek();
    }

    override getReasonedSynchronous(): reason.Maybe<T> {
        throw new Error("Synchronous evaluation is not supported!");
    }

    override getSynchronous(): T {
        throw new Error("Synchronous evaluation is not supported!");
    }

    override setAttribute<A extends attr.TypeSafeAttribute<V>, V>(attribute: $tf.Class<A>, value: V): void {
        this.#mapAttrContext.setAttribute(attribute, value);
    }

    override findAttribute<A extends attr.TypeSafeAttribute<V>, V>(attribute: $tf.Class<A>): $tf.Optional<V> {
        return this.#mapAttrContext.findAttribute(attribute);
    }

    override getAttribute<A extends attr.TypeSafeAttribute<V>, V>(attribute: $tf.Class<A>): V {
        return this.#mapAttrContext.getAttribute(attribute);
    }

    override findOrDefault<A extends attr.TypeSafeAttribute<V>, V>(attribute: $tf.Class<A>, defaultValue: V): V {
        return this.#mapAttrContext.findOrDefault(attribute, defaultValue);
    }

    override findOrNull<A extends attr.TypeSafeAttribute<V>, V>(attribute: $tf.Class<A>): V {
        return this.#mapAttrContext.findOrNull(attribute);
    }

    override findOrSupply<A extends attr.TypeSafeAttribute<V>, V>(attribute: $tf.Class<A>, defaultValueSupplier: $tf.Supplier<V>): V {
        return this.#mapAttrContext.findOrSupply(attribute, defaultValueSupplier);
    }

    override streamAttributes(): $tf.Stream<attr.TypeSafeAttributeEntry> {
        return this.#mapAttrContext.streamAttributes();
    }

}

function makeDraggableAndMaybeResizable(element: HTMLElement, clickable: HTMLElement, resizable: boolean): void {
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
        element.style.left = "0px"
        element.style.top = "0px"
    }

    /** There is a weird behavior in browser (Chrome at least), that if you select text, press mouse button, 
     * drag and release the mouse button, you don't get the onmouseup event. You need to click to get that.
     * So if you accidentally selected the clickable text, then the window would move after mouse up until you click.
     * Super annoying. Disabling userSelect prevents this from happening. */
    clickable.style.userSelect = "none";
    clickable.style.cursor = "move"
    clickable.addEventListener("mousedown", (e) => startDragging(e, true, true, 0, 0));

    if (resizable)
        makeElementResizable();

    // while resizing, we set userSelect to none, otherwise text keeps getting selected and everything is broken shortly after
    const originalUserSelect = element.style.userSelect;
    let isActivated = false;

    function makeElementResizable(): void {
        const negativeSize = (size: string) => size ? "-" + size : 0;

        // If there is a border around element, we want to move the resizing divs to align with the border
        const left = negativeSize(style.borderLeftWidth);
        const right = negativeSize(style.borderRightWidth);
        const top = negativeSize(style.borderTopWidth);
        const bottom = negativeSize(style.borderBottomWidth);

        createResizingDiv(/*NW*/ "nwse-resize", false, false, (s) => { s.left = left; s.top = top; }, true, true, -1, -1);
        createResizingDiv(/*N */ "ns-resize", true, false, (s) => { s.left = left; s.top = top }, false, true, 0, -1);
        createResizingDiv(/*NE*/ "nesw-resize", false, false, (s) => { s.right = right; s.top = top }, false, true, 1, -1);
        createResizingDiv(/*E */ "ew-resize", false, true, (s) => { s.right = right; s.top = top }, false, false, 1, 0);
        createResizingDiv(/*SE*/ "nwse-resize", false, false, (s) => { s.right = right; s.bottom = bottom }, false, false, 1, 1);
        createResizingDiv(/*S */ "ns-resize", true, false, (s) => { s.left = left; s.bottom = bottom; }, false, false, 0, 1);
        createResizingDiv(/*SW*/ "nesw-resize", false, false, (s) => { s.left = left; s.bottom = bottom }, true, false, -1, 1);
        createResizingDiv(/*W */ "ew-resize", false, true, (s) => { s.left = left; s.top = top; }, true, false, -1, 0);

        function createResizingDiv(cursor: string, fullWidth: boolean, fullHeight: boolean, styling: (CSSStyleDeclaration) => void,
            moveLeft: boolean, moveTop: boolean, changeWidth: number, changeHeight: number): HTMLElement {

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

    function startDragging(e: MouseEvent, moveLeft: boolean, moveTop: boolean, changeWidth: number, changeHeight: number): void {
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

        function move(e: MouseEvent): void {
            // Doing the min/max with DRAG_BORDER_PROTECTION and e.clientX/Y means dragging cursor outside of browser window doesn't have an effect, 
            // so the dialog cannot be dragged away to the point it cannot be used anymore (buttons/title being out of window, thus not clickable)
            const inVisibleRange = (n: number, max: number) => Math.min(Math.max(DRAG_BORDER_PROTECTION, n), max - DRAG_BORDER_PROTECTION);

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

        function stopDragging(): void {
            isActivated = false;
            element.style.userSelect = originalUserSelect;

            document.documentElement.removeEventListener('mousemove', move);
            document.documentElement.removeEventListener('mouseup', stopDragging);
        }
    }

}

// for local-only case (for now just push notifications) we set a RequestedEndpointAspect to imply no fallback via remotifier is desired
// otherwise, build an evaluator that tries locally first, if no processor found, either return reason (in case push notification) or