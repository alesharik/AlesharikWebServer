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

package com.alesharik.webserver.api.serial;

import com.alesharik.webserver.api.mx.bean.MXBeanManager;
import com.alesharik.webserver.exception.error.UnexpectedBehaviorError;
import com.alesharik.webserver.internals.instance.ClassInstantiator;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.alesharik.webserver.api.serial.PrimitiveSerializer.*;

/**
 * This class is main entry into Serialization API.
 * Serialization API data chunk graph:<br><pre>
 * /-------------------------------------\
 * | id (long) | Global ID of an object  |                      id == mapped class id
 * | data?     | following data fragment-------------------------------------------------------------
 * \---|----------------------------|----/                                                          |
 *     |                            |      /---------------------------------------------\      /-----------------------------------\
 *     |                   id == -2 |      |           Data = id(long) + string          |      |      Data = version + objdata     |
 *     | id == -1                   |      +---------------------------------------------+      +-----------------------------------+
 *     |                            ------>| id (long)      | Enum class id              |      | version (double) | object version |
 *     |                                   | strlen(int)    | Length of following string |      | objdata (byte[]) | object data    |
 *     ⌄                                   | str (UTF16-LE) | Enum constant name         |      \-----------------------------------/
 *   data = 0 bytes                        \---------------------------------------------/
 *   deserialized object = null
 *   </pre>
 * Object serialization algorithm: <pre>
 *       /------------------------------------------------------------------------------\
 *       |     Type    |                      How it is serialized                      |
 *       +------------------------------------------------------------------------------+
 *       | primitive   | Direct write into data                                         |
 *       | array       | length(int) + serialization based on component type            |
 *       | object      | length(int) + byte array from Serial{@link #serialize(Object)} |
 *       | null        | null data chunk(see Serialization API data chunk graph)        |
 *       | enum        | enum data chunk(see Serialization API data chunk graph)        |
 *       \------------------------------------------------------------------------------/
 *   </pre>
 */
@UtilityClass
public class Serial {
    private static final SerializationClassConversionMapImpl conversionMap = new SerializationClassConversionMapImpl();
    private static final Map<Pair<Class<?>, Double>, Serializer> serializers = new ConcurrentHashMap<>();
    private static final ThreadLocal<ByteBuffer> buffers = ThreadLocal.withInitial(() -> ByteBuffer.allocate(20));
    private static final SerialMXBeanImpl bean = new SerialMXBeanImpl();

    static {
        try {
            Class.forName("sun.reflect.MagicAccessorImpl");
        } catch (ClassNotFoundException e) {
            throw new UnexpectedBehaviorError(e);
        }
        putSerializer(Boolean.class, 0, new BooleanSerializer());
        putSerializer(Character.class, 1, new CharSerializer());
        putSerializer(Short.class, 2, new ShortSerializer());
        putSerializer(Integer.class, 3, new IntSerializer());
        putSerializer(Float.class, 4, new FloatSerializer());
        putSerializer(Long.class, 5, new LongSerializer());
        putSerializer(Double.class, 6, new DoubleSerializer());
        putSerializer(String.class, 7, new StringSerializer());
        putSerializer(Class.class, 8, new ClassSerializer());
        //some useful objects
        preGenerateSerializer(Date.class, 9);
        preGenerateSerializer(ArrayList.class, 10);
        preGenerateSerializer(LinkedList.class, 11);
        preGenerateSerializer(Vector.class, 12);
        preGenerateSerializer(HashSet.class, 13);
        preGenerateSerializer(TreeSet.class, 14);
        preGenerateSerializer(LinkedHashSet.class, 15);
        preGenerateSerializer(HashMap.class, 16);
        preGenerateSerializer(TreeMap.class, 17);
        preGenerateSerializer(LinkedHashMap.class, 18);
        preGenerateSerializer(Hashtable.class, 19);
        preGenerateSerializer(IdentityHashMap.class, 20);
        preGenerateSerializer(ConcurrentHashMap.class, 21);

        MXBeanManager.registerMXBean(bean, SerialMXBean.class, "com.alesharik.webserver.api.serial.Serial:ready=true");
    }

    /**
     * Deserialize object from stream
     *
     * @param data serialized object
     * @param <T>  object type
     * @return deserialized object
     * @throws SerializationMappingNotFoundException if there is object with unknown id in the data
     * @throws DataOverflowException if some serializer has too much data to deserialize
     * @throws DataUnderflowException if the serializer cannot read enough data for deserialization
     */
    public static <T> T deserialize(@Nonnull byte[] data) {
        if(data.length < 8) {
            bean.newUnderflow();
            throw new DataUnderflowException("Cannot read id from less than 8 bytes!");
        }

        ByteBuffer byteBuffer = buffers.get();
        byteBuffer.rewind();
        byteBuffer.put(data, 0, 8);
        byteBuffer.rewind();
        long id = byteBuffer.getLong();
        if(id == -1)
            return null;
        if(id == -2)
            return deserializeEnum(data, byteBuffer);
        else
            return deserializeObject(data, byteBuffer, id);
    }

    private static <T> T deserializeObject(@Nonnull byte[] data, ByteBuffer byteBuffer, long id) {
        if(data.length < 16) {
            bean.newUnderflow();
            throw new DataUnderflowException("Cannot read id and version from less than 16 bytes!");
        }

        byteBuffer.rewind();
        byteBuffer.put(data, 8, 8);
        byteBuffer.rewind();

        double version = byteBuffer.getDouble();
        Class<?> clazz = conversionMap.resolveConversion(id);
        if(clazz == null) {
            bean.newMappingMismatch();
            throw new SerializationMappingNotFoundException(id);
        }

        Serializer serializer = serializers.computeIfAbsent(Pair.of(clazz, version), classDoublePair -> {
            AnnotationAdapter.Adapter adapter = getAnnotationAdapter(classDoublePair.getKey());
            return SerializerFactory.create(classDoublePair.getKey(), adapter, classDoublePair.getValue());
        });
        byte[] dat = new byte[data.length - 16];
        System.arraycopy(data, 16, dat, 0, data.length - 16);
        try {
            //noinspection unchecked
            return (T) serializer.deserialize(dat);
        } catch (ArrayIndexOutOfBoundsException e) {
            bean.newUnderflow();
            throw new DataUnderflowException("Serializer threw ArrayIndexOutOfBoundException: " + e.getMessage(), e);
        } catch (DataUnderflowException e) {
            bean.newUnderflow();
            throw e;
        } catch (DataOverflowException e) {
            bean.newOverflow();
            throw e;
        } catch (SerializationMappingNotFoundException e) {
            bean.newMappingMismatch();
            throw e;
        }
    }

    @Nonnull
    private static <T> T deserializeEnum(@Nonnull byte[] data, ByteBuffer byteBuffer) {
        if(data.length < 20) {
            bean.newUnderflow();
            throw new DataUnderflowException("Cannot read id, enum id and name string length from less than 20 bytes!");
        }
        byteBuffer.rewind();
        byteBuffer.put(data, 8, 12);
        byteBuffer.rewind();

        long id = byteBuffer.getLong();
        Class<?> clazz = conversionMap.resolveConversion(id);
        if(clazz == null)
            throw new SerializationMappingNotFoundException(id);

        int size = byteBuffer.getInt();
        if(data.length < 20 + size) {
            bean.newUnderflow();
            throw new DataUnderflowException("Cannot read name string from less than 20(header) + " + size + " bytes, but first 20 bytes is present!");
        }

        byte[] nameData = new byte[size];
        System.arraycopy(data, 20, nameData, 0, size);
        String name = new String(nameData, StandardCharsets.UTF_16LE);
        //noinspection unchecked
        return (T) Enum.valueOf((Class<Enum>) clazz, name);
    }

    /**
     * Deserialize object and cast it to given type
     *
     * @param data serialized object
     * @param cast cast type class
     * @param <T>  cat type
     * @return deserialized and casted object
     * @throws SerializationMappingNotFoundException if there is object with unknown id in the data
     */
    public static <T> T deserialize(@Nonnull byte[] data, @Nonnull Class<T> cast) {
        return cast.cast(deserialize(data));
    }

    /**
     * Serialize given object into byte array
     *
     * @param object the object to serialize
     * @param <T>    object type
     * @return serialized object
     */
    public static <T extends Object & Serializable> byte[] serialize(@Nullable T object) {
        if(object == null)
            return serialize(null, -1);
        AnnotationAdapter.Adapter annotationAdapter = getAnnotationAdapter(object.getClass());
        double version = annotationAdapter.hasVersionAnnotation(object.getClass()) ? annotationAdapter.getVersion(object.getClass()) : -1;
        return serialize(object, version);
    }

    /**
     * Serialize given object using given version
     *
     * @param object  the object to serialize
     * @param version object's version
     * @param <T>     object type
     * @return serialized object
     */
    public static <T extends Object & Serializable> byte[] serialize(@Nullable T object, double version) {
        if(object == null)
            return serializeNull();
        else if(object.getClass().isEnum())
            return serializeEnum(object);
        else
            return serializeObject(object, version);
    }

    private static byte[] serializeObject(@NotNull Object object, double version) {
        ByteBuffer byteBuffer = buffers.get();
        byteBuffer.rewind();
        AnnotationAdapter.Adapter annotationAdapter = getAnnotationAdapter(object.getClass());
        Serializer serializer = serializers.computeIfAbsent(Pair.of(object.getClass(), version), classDoublePair -> SerializerFactory.create(classDoublePair.getKey(), annotationAdapter, classDoublePair.getValue()));
        byte[] ser = serializer.serialize(object);
        byte[] ret = new byte[ser.length + 16];
        byteBuffer.putLong(conversionMap.getOrCreateConversionFor(object.getClass()));
        byteBuffer.putDouble(version);
        byteBuffer.rewind();
        System.arraycopy(byteBuffer.array(), 0, ret, 0, 16);
        System.arraycopy(ser, 0, ret, 16, ser.length);
        return ret;
    }

    private static byte[] serializeEnum(@Nonnull Object object) {
        ByteBuffer byteBuffer = buffers.get();
        byteBuffer.rewind();
        byteBuffer.putLong(-2);
        byteBuffer.putLong(conversionMap.getOrCreateConversionFor(object.getClass()));
        byte[] string = ((Enum) object).name().getBytes(StandardCharsets.UTF_16LE);
        byteBuffer.putInt(string.length);
        byte[] ret = new byte[20 + string.length];
        byteBuffer.rewind();
        System.arraycopy(byteBuffer.array(), 0, ret, 0, 20);
        System.arraycopy(string, 0, ret, 20, string.length);
        return ret;
    }

    private static byte[] serializeNull() {
        ByteBuffer byteBuffer = buffers.get();
        byteBuffer.rewind();
        byteBuffer.putLong(-1);
        byte[] ret = new byte[8];
        byteBuffer.rewind();
        System.arraycopy(byteBuffer.array(), 0, ret, 0, 8);
        return ret;
    }

    /**
     * Return global conversion map
     *
     * @return global conversion map
     */
    @Nonnull
    public static SerializationClassConversionMap getConversionMap() {
        return conversionMap;
    }

    public static void unloadClassLoader(@Nonnull ClassLoader classLoader) {
        serializers.forEach((classDoublePair, serializer) -> {
            if(classDoublePair.getKey().getClassLoader() == classLoader)
                serializers.remove(classDoublePair);
        });
        SerializerFactory.unloadClassLoader(classLoader);
        conversionMap.cleanClassesFromClassLoader(classLoader);
    }

    static Serializer getSerializer(Class<?> clazz) {
        if(!Serializable.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Class " + clazz + " is not Serializable!");
        AnnotationAdapter.Adapter annotationAdapter = getAnnotationAdapter(clazz);
        double version = annotationAdapter.hasVersionAnnotation(clazz) ? annotationAdapter.getVersion(clazz) : -1;
        return serializers.computeIfAbsent(Pair.of(clazz, version), classDoublePair -> SerializerFactory.create(classDoublePair.getKey(), annotationAdapter, classDoublePair.getValue()));
    }

    private static void putSerializer(Class<?> clazz, long id, Serializer serializer) {
        conversionMap.addConversion(id, clazz);
        serializers.put(Pair.of(clazz, -1D), serializer);
    }

    private static void preGenerateSerializer(Class<?> clazz, long id) {
        conversionMap.addConversion(id, clazz);
        getSerializer(clazz);
    }

    private static AnnotationAdapter.Adapter getAnnotationAdapter(Class<?> clazz) {
        AnnotationAdapter.Adapter adapter;
        if(clazz.isAnnotationPresent(AnnotationAdapter.class))
            adapter = (AnnotationAdapter.Adapter) ClassInstantiator.instantiate(clazz.getAnnotation(AnnotationAdapter.class).value());
        else
            adapter = DefaultAnnotationAdapter.INSTANCE;
        return adapter;
    }

    private static final class SerialMXBeanImpl implements SerialMXBean {
        private final AtomicLong failedMappingCount = new AtomicLong();
        private final AtomicLong underflowCount = new AtomicLong();
        private final AtomicLong overflowCount = new AtomicLong();

        @Override
        public long getSerializerCount() {
            return Serial.serializers.size();
        }

        @Override
        public long getConversionCount() {
            return conversionMap.count();
        }

        @Override
        public String getConversionFor(long id) {
            Class<?> conversion = conversionMap.resolveConversion(id);
            return conversion == null ? "" : conversion.getName();
        }

        @Override
        public long getFailedMappingCount() {
            return failedMappingCount.get();
        }

        @Override
        public long getSizeMismatchCount() {
            return overflowCount.get() + underflowCount.get();
        }

        @Override
        public long getUnderflowCount() {
            return underflowCount.get();
        }

        @Override
        public long getOverflowCount() {
            return overflowCount.get();
        }

        void newOverflow() {
            overflowCount.incrementAndGet();
        }

        void newUnderflow() {
            underflowCount.incrementAndGet();
        }

        void newMappingMismatch() {
            failedMappingCount.incrementAndGet();
        }
    }
}
