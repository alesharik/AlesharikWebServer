/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.js;

import com.alesharik.webserver.api.collections.TripleHashMap;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This class is abstraction of js class. Used for works with class(change methods, add constructor, etc)
 */
//TODO use regexp to detect methods
public class JSClass {
    private static final Pattern classRegexp = Pattern.compile("class ([^ ]*)");
    private static final Pattern extendRegexp = Pattern.compile("class ([^ ]*)");

    protected String extend;
    protected String name;
    protected String constructor;
    protected String constructorParams;
    protected TripleHashMap<String, String, String> methods;

    public JSClass(String name) {
        this(name, "");
    }

    public JSClass(String name, String extend) {
        this.name = name;
        this.extend = extend;
        constructor = "";
        constructorParams = "";
        methods = new TripleHashMap<>();
    }

    public void setExtends(String extend) {
        this.extend = extend;
    }

    public String extend() {
        return extend;
    }

    public void setConstructor(String code, String constructorParams) {
        constructor = code;
        this.constructorParams = constructorParams;
    }

    public String getConstructor() {
        return constructor;
    }

    public String getConstructorParams() {
        return constructorParams;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addMethod(String methodName, String arguments, String code) {
        methods.put(methodName, arguments, code);
    }

    public void removeMethod(String methodName) {
        methods.remove(methodName);
    }

    public String getMethodCode(String name) {
        return methods.getAddition(name);
    }

    public String getMethodArgs(String name) {
        return methods.get(name);
    }

    public TripleHashMap<String, String, String> getMethods() throws CloneNotSupportedException {
        return (TripleHashMap<String, String, String>) methods.clone();
    }

    public String getCode() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("class ");
        stringBuilder.append(name);
        if(!extend.isEmpty()) {
            stringBuilder.append(" extends ");
            stringBuilder.append(extend);
        }
        stringBuilder.append(" {\n");
        if(!constructor.isEmpty()) {
            stringBuilder.append("    constructor(");
            if(!constructorParams.isEmpty()) {
                stringBuilder.append(constructorParams);
            }
            stringBuilder.append(") { \n");
            Stream.of(constructor.split("\n")).forEach(line -> {
                stringBuilder.append("    ");
                stringBuilder.append(line);
                stringBuilder.append("\n");
            });
            stringBuilder.append("}\n");
        }
        methods.forEach((name, args, code) -> {
            stringBuilder.append(name);
            stringBuilder.append("(");
            if(args != null && !args.isEmpty()) {
                stringBuilder.append(args);
            }
            stringBuilder.append(") { \n");
            Stream.of(code.split("\n")).forEach(line -> {
                stringBuilder.append("    ");
                stringBuilder.append(line);
                stringBuilder.append("\n");
            });
            stringBuilder.append("}\n");
        });
        stringBuilder.append("}\n");
        return stringBuilder.toString();
    }

    public String getMinimizedCode() {
        return CodeFormatter.minimize(getCode(), CodeFormatter.JS_SPACE_REGEXP, CodeFormatter.JS_NEXT_LINE_REGEXP);
    }

    //TODO write this
    public static JSClass parse(String clazz) {
        String name = classRegexp.matcher(clazz).group(0).substring("class ".length());
        String extend = extendRegexp.matcher(clazz).group();
        if(!extend.isEmpty()) {
            extend = extend.substring("extends ".length());
        }
//        String methods = clazz.substring(("class " + name + " " + ((extend.isEmpty()) ? "" : "extends " + extend) + " {").length(), clazz.lastIndexOf("}") - 1);
        return new JSClass(name, extend);
    }

    @Override
    public String toString() {
        return getMinimizedCode();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof JSClass)) return false;

        JSClass jsClass = (JSClass) o;

        if(extend != null ? !extend.equals(jsClass.extend) : jsClass.extend != null) return false;
        if(name != null ? !name.equals(jsClass.name) : jsClass.name != null) return false;
        if(constructor != null ? !constructor.equals(jsClass.constructor) : jsClass.constructor != null) return false;
        if(constructorParams != null ? !constructorParams.equals(jsClass.constructorParams) : jsClass.constructorParams != null)
            return false;
        return methods != null ? methods.equals(jsClass.methods) : jsClass.methods == null;

    }

    @Override
    public int hashCode() {
        int result = extend != null ? extend.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (constructor != null ? constructor.hashCode() : 0);
        result = 31 * result + (constructorParams != null ? constructorParams.hashCode() : 0);
        result = 31 * result + (methods != null ? methods.hashCode() : 0);
        return result;
    }
}
