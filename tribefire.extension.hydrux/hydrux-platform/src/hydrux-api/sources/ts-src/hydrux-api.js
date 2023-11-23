export function hxApplication() {
    return $tf.hydrux.application;
}
export class IHxViews {
    static create(htmlElementFactory) {
        return {
            htmlElement: () => htmlElementFactory()
        };
    }
}
