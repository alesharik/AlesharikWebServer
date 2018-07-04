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

package com.alesharik.webserver.module.http;

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import com.alesharik.webserver.extension.module.ConfigurationError;
import com.alesharik.webserver.extension.module.configuration.CustomDeserializer;
import com.alesharik.webserver.extension.module.configuration.ElementDeserializer;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

@Immutable
@CustomDeserializer(PortRange.Deserializer.class)
public class PortRange implements Serializable {
    private static final long serialVersionUID = 3060816768811688047L;

    @Getter
    protected final int lower;
    @Getter
    protected final int upper;

    /**
     * @param lower from value
     * @param upper to value
     * @throws IllegalArgumentException if lower value > upper value
     */
    public PortRange(int lower, int upper) {
        if(lower < 0)
            throw new IllegalArgumentException("Lower value can't be < 0!");
        if(lower > upper)
            throw new IllegalArgumentException("Lower value must be lower than upper value!");
        this.lower = lower;
        this.upper = upper;
    }

    public PortRange(int port) {
        this(port, port);
    }

    public static final class Deserializer implements ElementDeserializer {

        @Nullable
        @Override
        public Object deserialize(@Nonnull ConfigurationElement element, @Nonnull ScriptElementConverter converter) throws ConfigurationError {
            return deserializeObject(element, converter);
        }

        @NotNull
        public static PortRange deserializeObject(@Nonnull ConfigurationElement element, @Nonnull ScriptElementConverter converter) {
            if(element instanceof ConfigurationPrimitive.Int)
                return new PortRange(((ConfigurationPrimitive.Int) element).value());
            else if(element instanceof ConfigurationPrimitive.Short)
                return new PortRange(((ConfigurationPrimitive.Short) element).value());
            else if(element instanceof ConfigurationObject) {
                int from = getFrom((ConfigurationObject) element, converter);
                int to = getTo((ConfigurationObject) element, converter, from);
                return new PortRange(from, to);
            } else if(converter.isExecutable(element)) {
                Object o = converter.execute(element, Object.class);
                if(o instanceof Integer)
                    return new PortRange((Integer) o);
                else if(o instanceof PortRange)
                    return (PortRange) o;
                else
                    throw new ConfigurationError("Can't parse type " + o);
            }
            throw new ConfigurationError("Can't parse configuration element " + element);
        }

        private static int getFrom(@Nonnull ConfigurationObject element, @Nonnull ScriptElementConverter converter) {
            ConfigurationElement fromElement = element.getElement("from");
            if(fromElement == null)
                throw new ConfigurationError("PortRange's from element can't be null!");
            else if(converter.isExecutable(fromElement))
                return converter.execute(fromElement, Integer.class);
            else if(fromElement instanceof ConfigurationPrimitive.Int)
                return ((ConfigurationPrimitive.Int) fromElement).value();
            else if(fromElement instanceof ConfigurationPrimitive.Short)
                return ((ConfigurationPrimitive.Short) fromElement).value();
            else
                throw new ConfigurationError("PortRange's from element must be an integer!");
        }

        private static int getTo(@Nonnull ConfigurationObject element, @Nonnull ScriptElementConverter converter, int from) {
            ConfigurationElement toElement = element.getElement("to");
            if(toElement == null)
                return from;
            else if(converter.isExecutable(toElement))
                return converter.execute(toElement, Integer.class);
            else if(toElement instanceof ConfigurationPrimitive.Int)
                return ((ConfigurationPrimitive.Int) toElement).value();
            else if(toElement instanceof ConfigurationPrimitive.Short)
                return ((ConfigurationPrimitive.Short) toElement).value();
            else
                throw new ConfigurationError("PortRange's from element must be an integer!");
        }
    }
}
