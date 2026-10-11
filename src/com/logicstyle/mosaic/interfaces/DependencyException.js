/** Thrown by Component.init() when a required component is missing. */
export class DependencyException extends Error {
    constructor(msgOrRequired, required) {
        super(typeof msgOrRequired === 'string' ? msgOrRequired : 'Missing dependency: ' + msgOrRequired.getName());
        this.name = 'DependencyException';
        this.required = typeof msgOrRequired === 'string' ? required : msgOrRequired;
    }

    getRequired() {
        return this.required;
    }
}
