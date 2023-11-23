# `web-form`

`web-form` is data driven [web-component](https://developer.mozilla.org/en-US/docs/Web/Web_Components) used to simplify handling of forms in modern web application development

`form fields` and `initial values` are defined using `json` which eliminates writing of html form markup manually. If config `json` is fetched from backend, changing form (adding or removing field, changing data type or validation) can be handled solely by backend and wouldn't require frontend redeployment

styling is encapsulated like in any other `web-component`

[css variables](https://developer.mozilla.org/en-US/docs/Web/CSS/Using_CSS_custom_properties) can be used to penetrate encapsulation and reach elements inside [shadow-tree](https://developer.mozilla.org/en-US/docs/Web/Web_Components/Using_shadow_DOM)

[::part](https://developer.mozilla.org/en-US/docs/Web/CSS/::part) css selector will also work in latest Firefox and Chrome, but note it has not been standardized yet and it's support may
be dropped in the future

## Browser support

Click [here](https://developer.mozilla.org/en-US/docs/Web/Web_Components) to check web-components browser support

## Must be imported into `html` before usage

```html
<script src="cdn-server/web-form.js"></script>
```

## How to create using `html`

```html
<dynamic-form></dynamic-form>
```

## How to select using `javascript`

`dynamic-form` can be selected by tag name:

```js
const dynamicForm = document.querySelector("dynamic-form");
```

or using any other standard selector like `id` or `class`

```js
const dynamicForm = document.getElementById("my-dynamic-form-id");
```

## How to create using `javascript`

```js
const dynamicForm = document.createElement("dynamic-form");
document.body.appendChild(dynamicForm);
```

## `slots` _(can be set via `html` and `javascript`)_

you can learn more about slots [here](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/slot)

static slots are

- `header` - inserted above form content defined by formfields property
- `footer` - inserted bellow form content

```html
<dynamic-form>
  <div slot="header">
    <h2>Hello from the "header" slot</h2>
  </div>
</dynamic-form>
```

dynamic slots can be created dynamically using `formfields` property or using javascript

```html
<dynamic-form>
  <img
    slot="image-slot"
    src="https://cdn.svgporn.com/logos/nodejs-icon.svg"
    alt="nodejs-logo"
  />
</dynamic-form>
```

## `formfields` property _(can be set via `javascript` only)_

type: `Array<Object>`

requred: `false`, but dynamic-form does not make much sense without this property

using formfields we can create form fields and action buttons from js object<br />
Every form field have label and name properties. `label` is used to describe field to user, `name` is used for submitting form field value

supported form fields types are:

- `input`
  - `type`: "text" | "password" | "number" | "checkbox" | "email" | "date"
- `textarea`
  - `cols`: number
  - `rows`: number
- `button`
  - `label`: string
  - `action`: "submit" | "reset" | "discard"
- `button`
  - `label`: string
  - `action`: "submit" | "reset" | "discard"
- `slot` is used to create web-component slot inside form which makes inserting custom elements into form easier and can be done via html or javascript
  - `name`: string

`form-fields-sample.json` :

```json
[
  {
    "type": "input",
    "props": {
      "name": "startDate",
      "type": "date",
      "label": "Start date"
    }
  },
  {
    "type": "textarea",
    "props": {
      "name": "userMessage",
      "label": "Your message",
      "rows": 10,
      "cols": 20
    }
  },
  {
    "type": "slot",
    "props": {
      "name": "image-slot"
    }
  },
  {
    "type": "button",
    "props": {
      "name": "submitButton",
      "action": "submit",
      "label": "Send"
    }
  }
]
```

```html
<dynamic-form>
  <img
    slot="image-slot"
    src="https://cdn.svgporn.com/logos/nodejs-icon.svg"
    alt="nodejs-logo"
  />
</dynamic-form>

<script>
  const dynamicForm = document.querySelector("dynamic-form");
  fetch("form-fields-sample.json")
    .then(response => response.json())
    .then(formfields => {
      dynamicForm.formfields = formfields;
    });
</script>
```

please note that `img` in example above will be styled by top-level css

for more complex example see [dynamic-form-fields.json](../../docs/dynamic-form-fields.json)

## `initialvalues` property _(can be set via `javascript` only)_

type: `{ [key: string]: any }`

requred: `false`

```js
dynamicForm.initialvalues = {
  startData: "2020-02-01T12:34:56.789Z",
  userMessage: "Some placeholder text..."
};
```

properties within `initialvalues`, not defined by `formfields`, will be ignored

resetting form will load `initialvalues` into form fields

## `css` property _(can be set via `html` and `javascript`)_

type: `string`

requred: `false`

to style form and it's children use `css` property which should be string containing content of .css file. all selectors are supported and encapulsated within `dynamic-form`'s shadow-root

`dynamic-form` also supports `style` property which will style host component not form or other shadow-dom's compoennts

```html
<dynamic-form
  css="@media screen and (min-width: 800px) { form { opacity: 0.9 } }"
>
</dynamic-form>
```

## Events

Supported events are

- `change` - fired after a value in any form field has changed. will fire after initialvalues changes as well
- `submit` - fired when form has been submitted. you need at least one `button[action=submit` in `formfields` property for `submit` to be fired (look at example bellow)
- `discard` - fired when `button[action=discard]` has been pressed

```json
{
  "type": "button",
  "props": {
    "name": "submitButton",
    "action": "submit",
    "label": "Send"
  }
}
```

event related data will be stored in `event.detail`

to add event listener use `dynamic-form`'s `addEventListener` method. don't forget ro `removeEventListener` on life cycle end to prevent memory leaks

```js
const changeHandler = event => { ... }

// on life cycle start
dynamicForm.addEventListener("change", changeHandler)

// on life cycle end
dynamicForm.removeEventListener("change", changeHandler)
```

## Roadmap

- required fields
- validation configured by JSON and handled by `dynamic-form` itself
- support for more fields, like:
  - Select (with autocomplete)
  - Multiselect (with autocomplete)
  - Select with relational data (static and loaded dynamically)
- Remove property `formfields props` property and lift all its properties one level up **(breaking change)**
