(function () {
    const cssText =
        ".hydrux-debug {" +
        "font-family: sans-serif; font-size: 15px; line-height: 18px;" +
        "}" +

        ".hydrux-debug button { " +
        "width: 100px; height: 35px; " +
        "font-weight: bold; " +
        "margin: 10px 10px 0 10px; border:0; padding:2px; " +
        "background-color:rgb(39,22,0); color: white; " +
        "border-radius: 3px; " +
        "float:right; " +
        "}" +

        ".hydrux-debug .errorElement { " +
        "margin-left: 10px; margin-right: 10px;" +
        "}";

    const style = document.createElement('style');
    style.type = 'text/css';
    if (style.styleSheet) {
        style.styleSheet.cssText = cssText;
    } else {
        style.appendChild(document.createTextNode(cssText));
    }
    document.head.appendChild(style);

    class ErrorWindow {
        constructor(titleText, errorHtml, bgColor) {
            this.titleText = titleText;
            this.errorHtml = errorHtml;
            this.bgColor = bgColor;
        }

        show() {
            const errorDiv = document.createElement("div");
            errorDiv.classList.add("hydrux-debug")
            errorDiv.style.position = "fixed";
            errorDiv.style.border = "1px solid black";
            errorDiv.style.background = this.bgColor;
            errorDiv.style.color = "white";
            errorDiv.style.top = '0';
            errorDiv.style.right = '0';
            errorDiv.style.width = "500px";
            errorDiv.style.height = "300px";

            const titleElement = document.createElement("h3");
            titleElement.classList.add("errorElement")
            titleElement.textContent = this.titleText;

            const contentDiv = document.createElement("div");
            contentDiv.classList.add("errorElement")
            contentDiv.style.overflowY = "auto"
            contentDiv.innerHTML = this.errorHtml;

            const closeButton = document.createElement("button");
            closeButton.classList.add("errorElement")
            closeButton.style.cursor = "pointer" 
            closeButton.textContent = "Close"
            closeButton.onclick = (ev) => document.body.removeChild(errorDiv);

            const maximizeButton = document.createElement("button");
            maximizeButton.style.cursor = "pointer" 
            maximizeButton.textContent = "Maximize";
            maximizeButton.onclick = (ev) => {
                errorDiv.style.width = "100%";
                errorDiv.style.height = "100%";
                setContentHeight();
            }

            errorDiv.appendChild(titleElement);
            errorDiv.appendChild(contentDiv);
            errorDiv.appendChild(closeButton);
            errorDiv.appendChild(maximizeButton);

            document.body.appendChild(errorDiv);

            setContentHeight();

            function setContentHeight() {
                const bcr = e => e.getBoundingClientRect();
                const height = Math.round(bcr(errorDiv).height - bcr(contentDiv).top - bcr(closeButton).height - 20);
                contentDiv.style.height = height + "px";
            }

            return false;
        }
    }

    window.onerror = function (msg, url, line, col, error) {
        let stackInfo = (error && error.stack) ? '<br/>' + escapeNextLineAsBr(error.stack) : "";
        const errorHtml = "Error (line " + line + "): <b>" + msg + "</b><br/>" + stackInfo;

        new ErrorWindow("Uncaught Error!!!", errorHtml, "rgb(235,120,0)").show();

        return false;
    };

    window.addEventListener('unhandledrejection', function (event) {
        new ErrorWindow("Unhandled Promise Rejection!!!", promiseErrorHtml(), "rgb(59, 28, 92)").show();

        function promiseErrorHtml() {
            const reason = event.reason;
            if (reason.StackTrace && Array.isArray(reason.StackTrace)) {
                const stackTrace = reason.StackTrace.join("<br/>");
                const backingJs = reason.backingJsObject;

                let errorHtml;
                if (backingJs && backingJs.message)
                    errorHtml = "Error : <b>" + backingJs.message + "</b><br/>";
                else if (reason.DetailMessage)
                    errorHtml = "Error : <b>" + reason.DetailMessage + "</b><br/>";

                errorHtml += stackTrace;

                if (backingJs && backingJs.stack)
                    errorHtml += "<br/><br/>Client stack:<br/>" + escapeNextLineAsBr(backingJs.stack);

                return errorHtml;
            }

            const message = reason.message ? reason.message : reason;
            let stack = ""
            if (reason.stack)
                stack = "<br/>" + escapeNextLineAsBr(reason.stack);

            return "<b>" + message + "</b>" + stack;
        }

        return false;
    });

    function escapeNextLineAsBr(text) {
        return text.replace(/(\r\n|\r|\n)/g, "<br/>")
    }
})();
