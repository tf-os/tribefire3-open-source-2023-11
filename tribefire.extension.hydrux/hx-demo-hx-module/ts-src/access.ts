import * as tf from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxDemoDepM from "../tribefire.extension.hydrux.hx-demo-hx-deployment-model-2.1~/ensure-hx-demo-hx-deployment-model.js";
import * as hxDepM from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";

import { HxDemoEntity } from "../tribefire.extension.hydrux.hx-demo-data-model-2.1~/ensure-hx-demo-data-model.js";

export function bindAccess(context: hx.IHxModuleBindingContext): void {
    context.componentBinder().bindView(hxDemoDepM.HxDemoAccessView, createAccessView);
}

async function createAccessView(denotation: hxDemoDepM.HxDemoAccessView, context: hx.IHxComponentCreationContext): Promise<hx.IHxView> {
    context.application().loadCss(denotation.module, "access.css");

    const sessionSupplierDenotation = hxDepM.HxAccessSessionFactory.create();
    const sessionFactory = await context.scope().resolveSessionFactory(sessionSupplierDenotation);
    const session = sessionFactory.newSession();

    const app = new AccessApp(denotation, session);

    return hx.IHxTools.view(() => app.div);
}

class AccessApp {

    private readonly denotation: hxDemoDepM.HxDemoAccessView;
    private readonly session: tf.session.PersistenceGmSession;

    public div: HTMLDivElement;

    private searchInput: HTMLInputElement;
    private searchButton: HTMLButtonElement;

    private createInput: HTMLInputElement;
    private createButton: HTMLButtonElement;
    
    private uploadInput: HTMLInputElement;
    private uploadButton: HTMLButtonElement;

    private results: HTMLDivElement;

    constructor(denotation: hxDemoDepM.HxDemoAccessView, session: tf.session.PersistenceGmSession) {
        this.denotation = denotation;
        this.session = session;

        this.initDivs();
    }

    private initDivs(): void {
        const div = document.createElement("div");
        div.classList.add("hx-demo-access")

        div.innerHTML =
            '<div style="margin-bottom: 4px"><input id="createInput" type="text"><button id="createButton" type="button">New</button></div>' +
            '<div style="margin-bottom: 4px"><input id="searchInput" type="text"><button id="searchButton" type="button">Search</button></div>' +
            '<div style="margin-bottom: 4px"><input id="uploadInput" type="file"><button id="uploadButton" type="button">Upload</button></div>' +
            '<div style="margin-bottom: 4px"><span id="uploadArea" class="fileUploadArea" style="display: inline-block; border: 1px solid red"></span><button id="uploadButton" type="button">Upload</button></div>' +
            '<div id="results" style="margin-top: 5px"></div>';

        this.div = div;
        this.createInput = div.querySelector("#createInput");
        this.createButton = div.querySelector("#createButton");

        this.searchInput = div.querySelector("#searchInput");
        this.searchButton = div.querySelector("#searchButton");

        this.uploadInput = div.querySelector("#uploadInput");
        this.uploadButton = div.querySelector("#uploadButton");

        this.results = div.querySelector("#results");

        this.createButton.addEventListener("click", () => this.onCreateClick())
        this.searchButton.addEventListener("click", () => this.onSearchClick())
        this.uploadButton.addEventListener("click", () => this.onUploadClick())
    }

    private async onCreateClick(): Promise<void> {
        const name = this.createInput.value;
        if (!name || name.length < 2)
            return;

        this.createInput.value = null;

        const newEntity = this.session.create(HxDemoEntity);
        newEntity.name = name;

        await this.session.commit();

        this.onSearchClick();
    }

    private async onSearchClick(): Promise<void> {
        let query = "from tribefire.extension.hydrux.demo.model.data.HxDemoEntity";

        const text = this.searchInput.value;
        if (text)
            query += " where name like '*" + text + "*'";

        query += " order by name limit " + this.denotation.maxRows;

        const queryResult = await this.session.query().entitiesString(query);
        const entities = queryResult.list() as tf.lang.List<HxDemoEntity>;


        if (entities.isEmpty()) {
            this.results.innerHTML = "No entities found!";
            return;
        }

        this.results.textContent = "";

        for (const entity of entities.iterable()) {
            const delButton = document.createElement("span");
            delButton.classList.add("del-button");
            delButton.textContent = "X";

            const rowDiv = document.createElement("div");
            rowDiv.style.margin = "3px";
            rowDiv.textContent = entity.name;
            rowDiv.appendChild(delButton);

            delButton.addEventListener("click", () => this.deleteEntity(entity, rowDiv))

            this.results.appendChild(rowDiv);
        }

        if (queryResult.entityQueryResult().hasMore) {
            const andMore = document.createElement("div");
            andMore.textContent = "and more...";

            this.results.appendChild(andMore);
        }
    }

    private async deleteEntity(entity: HxDemoEntity, entityElement: HTMLElement): Promise<void> {
        this.session.deleteEntity(entity);
        await this.session.commit();

        this.results.removeChild(entityElement);

        this.onSearchClick();
    }

    private async onUploadClick(): Promise<void> {
        const files = this.uploadInput.files;
        if (!files || files.length == 0)
            return;

        // Doesn't even seem to be possible
        if (files.length > 1)
            return;

        const _stored = await this.session.resources().create().storeFile(files[0] as any);
        const stored = _stored.getAtIndex(0);

        console.log("Resource stored with id: " + stored.id);
    }    

}

