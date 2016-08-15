'use strict';
class Pattern {
    constructor() {
        this.parameters = {};
        this.pattern = "";
        this.regexp = new RegExp("\{&.*?\}");
    }

    setParameters(parameters) {
        this.parameters = parameters;
        return this;
    }

    setPattern(pattern) {
        this.pattern = pattern;
        return this;
    }

    build() {
        let line = "";
        while ((line = this.regexp.exec(this.pattern)) != null) {
            let value = this.parameters[line[0].substring(2, line[0].length - 1)];
            this.pattern = this.pattern.replace(this.regexp, value);
        }
        return this.pattern;
    }
}