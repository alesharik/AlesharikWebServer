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

package com.alesharik.webserver.test;

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationCodeElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationFunctionElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObjectArray;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class ConfigHelper {
    public static TypedObj create(String name, String type) {
        return new TypedObj(name, type);
    }

    public static Obj object(String name) {
        return new Obj(name);
    }

    public static Arr array(String name) {
        return new Arr(name);
    }

    public static ConfigurationElement wrap(String name, int i) {
        return new ConfigurationPrimitive.Int() {
            @Override
            public int value() {
                return i;
            }

            @Override
            public java.lang.String getName() {
                return name;
            }
        };
    }

    public static ConfigurationElement wrap(String name, long i) {
        return new ConfigurationPrimitive.Long() {
            @Override
            public long value() {
                return i;
            }

            @Override
            public java.lang.String getName() {
                return name;
            }
        };
    }

    public static ConfigurationElement wrap(String name, String i) {
        return new ConfigurationPrimitive.String() {
            @Override
            public java.lang.String value() {
                return i;
            }

            @Override
            public java.lang.String getName() {
                return name;
            }
        };
    }

    public static ConfigurationElement wrap(String name, char i) {
        return new ConfigurationPrimitive.Char() {
            @Override
            public char value() {
                return i;
            }

            @Override
            public java.lang.String getName() {
                return name;
            }
        };
    }

    public static ConfigurationElement wrap(String name, float i) {
        return new ConfigurationPrimitive.Float() {
            @Override
            public float value() {
                return i;
            }

            @Override
            public java.lang.String getName() {
                return name;
            }
        };
    }

    public static ConfigurationElement wrap(String name, double i) {
        return new ConfigurationPrimitive.Double() {
            @Override
            public double value() {
                return i;
            }

            @Override
            public java.lang.String getName() {
                return name;
            }
        };
    }

    public static ConfigurationElement wrap(String name, boolean i) {
        return new ConfigurationPrimitive.Boolean() {
            @Override
            public boolean value() {
                return i;
            }

            @Override
            public java.lang.String getName() {
                return name;
            }
        };
    }

    public static ConfigurationElement wrap(String name, short i) {
        return new ConfigurationPrimitive.Short() {
            @Override
            public short value() {
                return i;
            }

            @Override
            public java.lang.String getName() {
                return name;
            }
        };
    }

    public static ConfigurationElement wrap(String name, byte i) {
        return new ConfigurationPrimitive.Byte() {
            @Override
            public byte value() {
                return i;
            }

            @Override
            public java.lang.String getName() {
                return name;
            }
        };
    }

    public static ConfigurationFunctionElement function(String name, String instruction) {
        return new ConfigurationFunctionElement() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getCodeInstruction() {
                return instruction;
            }
        };
    }

    public static ConfigurationCodeElement code(String name, String lang, String code) {
        return new ConfigurationCodeElement() {
            @Override
            public String getLanguageName() {
                return lang;
            }

            @Override
            public String getCode() {
                return code;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    @Getter
    public static final class TypedObj extends Obj implements ConfigurationTypedObject {
        private final String type;

        public TypedObj(String name, String type) {
            super(name);
            this.type = type;
        }
    }

    @RequiredArgsConstructor
    public static class Obj implements ConfigurationObject {
        @Getter
        private final String name;
        private final Map<String, ConfigurationElement> elements = new HashMap<>();

        @Nullable
        @Override
        public ConfigurationElement getElement(String name) {
            return elements.get(name);
        }

        @Nullable
        @Override
        public <V extends ConfigurationElement> V getElement(String name, Class<V> clazz) {
            return clazz.cast(elements.get(name));
        }

        @Override
        public Set<String> getNames() {
            return elements.keySet();
        }

        @Override
        public Map<String, ConfigurationElement> getEntries() {
            return elements;
        }

        @Override
        public int getSize() {
            return elements.size();
        }

        @Override
        public boolean hasKey(String name) {
            return elements.containsKey(name);
        }

        public Obj add(ConfigurationElement element) {
            elements.put(element.getName(), element);
            return this;
        }
    }

    @RequiredArgsConstructor
    public static final class Arr implements ConfigurationObjectArray {
        @Getter
        private final String name;
        private final List<ConfigurationElement> elements = new ArrayList<>();

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public ConfigurationElement get(int index) {
            return elements.get(index);
        }

        @Override
        public ConfigurationElement[] getElements() {
            return elements.toArray(new ConfigurationElement[0]);
        }

        @Override
        public void append(ConfigurationElement element) {
            elements.add(element);
        }

        public Arr add(ConfigurationElement e) {
            elements.add(e);
            return this;
        }

        @Nonnull
        @Override
        public Iterator<ConfigurationElement> iterator() {
            return elements.iterator();
        }
    }
}
