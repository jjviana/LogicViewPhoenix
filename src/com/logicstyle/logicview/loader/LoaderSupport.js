import { EventListenerList } from '../../../../compat/util/EventListenerList.js';
import { ModelLoadListener } from './ModelLoadListener.js';

/**
 * The listener plumbing that every original loader carried as copy-and-paste
 * NetBeans generated code (addModelLoadListener / fireModelLoadListener...).
 */
export const ModelLoadListenerSupport = {
    addModelLoadListener(listener) {
        if (this.listenerList == null) {
            this.listenerList = new EventListenerList();
        }
        this.listenerList.add(ModelLoadListener, listener);
    },

    removeModelLoadListener(listener) {
        this.listenerList && this.listenerList.remove(ModelLoadListener, listener);
    },

    fireModelLoadListenerLoadStarting(event) {
        for (const l of this.listenerList ? this.listenerList.getListeners(ModelLoadListener) : []) {
            l.loadStarting(event);
        }
    },

    fireModelLoadListenerLoadFinished(event) {
        for (const l of this.listenerList ? this.listenerList.getListeners(ModelLoadListener) : []) {
            l.loadFinished(event);
        }
    },

    fireModelLoadListenerLoadingPercentComplete(percent) {
        for (const l of this.listenerList ? this.listenerList.getListeners(ModelLoadListener) : []) {
            l.loadingPercentComplete(this, percent);
        }
    },
};

/** Parses an XML model description, throwing `message + details` on syntax errors. */
export function parseXml(text, parsingExceptionMessage) {
    const doc = new DOMParser().parseFromString(text, 'application/xml');
    const error = doc.getElementsByTagName('parsererror')[0];
    if (error) {
        throw new Error(parsingExceptionMessage + error.textContent);
    }
    return doc;
}

/** JDOM's Element.getChild(name) */
export function getChild(element, name) {
    for (const c of element.children) {
        if (c.tagName === name) {
            return c;
        }
    }
    return null;
}

/** JDOM's Element.getChildren(name) */
export function getChildren(element, name) {
    return [...element.children].filter((c) => c.tagName === name);
}

/** JDOM's Element.getAttributeValue(name): null when missing. */
export function getAttributeValue(element, name) {
    return element.hasAttribute(name) ? element.getAttribute(name) : null;
}
