/** A named group of UI actions: one menu of a menu bar. */
export class ActionGroup {
    constructor(name, icon = null) {
        this.name = name;
        this.icon = icon;
    }

    getName() {
        return this.name;
    }

    setName(name) {
        this.name = name;
    }

    getIcon() {
        return this.icon;
    }

    setIcon(icon) {
        this.icon = icon;
    }
}
