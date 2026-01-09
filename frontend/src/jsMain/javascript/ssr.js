import { Window } from "happy-dom";

let start = performance.now();

const windowObj = new Window({
    url: "http://localhost",
});

windowObj.document.documentElement.innerHTML = `
  <head></head>
  <body>
    <div id="root"></div>
  </body>
`;

function copyWindowToGlobal(windowObj, target = globalThis) {
    target["window"] = windowObj;
    target["document"] = windowObj.document;

    const props = new Set([
        ...Object.getOwnPropertyNames(windowObj),
        ...Object.getOwnPropertySymbols(windowObj),
    ]);

    for (const key of props) {
        if (key in target) continue;

        try {
            const desc = Object.getOwnPropertyDescriptor(windowObj, key);
            if (!desc) continue;
            Object.defineProperty(target, key, desc);
        } catch {
            try {
                target[key] = windowObj[key];
            } catch {
                // ignorieren
            }
        }
    }
}

copyWindowToGlobal(windowObj);

globalThis.navigator ??= windowObj.navigator;
globalThis.location ??= windowObj.location;

const { SSRApi } = await import(
    "./../../../build/compileSync/js/main/developmentExecutable/kotlin/technology-speaks-frontend.mjs"
);

console.log(SSRApi.getInstance().renderToString("/home"));
console.log(windowObj.document.body.innerHTML)

console.log(`SSR took ${performance.now() - start}ms`);