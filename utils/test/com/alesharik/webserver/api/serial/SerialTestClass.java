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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerialTestClass {
    /*
     * Copyright (c) 2017 SpaceToad and the BuildCraft team
     * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
     * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
     */

    @EqualsAndHashCode
    @ToString
    @Getter
    public static class JsonRule implements Serializable {
        public List<JsonSelector> selectors;
        public List<RequiredExtractor> requiredExtractors;
        public List<BlockPos> requiredBlockOffsets;
        public List<String> ignoredProperties;
        public List<BlockPos> updateBlockOffsets;
        public String placeBlock;
        public List<String> canBeReplacedWithBlocks;
        public NBTTagCompound replaceNbt;
        public boolean ignore; // blacklist for blocks
        public boolean capture = false; // whitelist for entities

        public JsonRule() {
            selectors = new ArrayList<>();
            for(int i = 0; i < RandomUtils.nextInt(10, 20); i++) {
                selectors.add(new JsonSelector());
            }
            requiredExtractors = new ArrayList<>();
            for(int i = 0; i < RandomUtils.nextInt(10, 20); i++) {
                requiredExtractors.add(new RequiredExtractorImpl());
            }
            requiredBlockOffsets = null;
            ignoredProperties = null;
            updateBlockOffsets = new ArrayList<>();
            for(int i = 0; i < RandomUtils.nextInt(20, 30); i++) {
                updateBlockOffsets.add(new BlockPos());
            }
            placeBlock = RandomStringUtils.random(12);
            canBeReplacedWithBlocks = null;
            replaceNbt = new NBTTagCompound();
            ignore = true;
        }
    }

    public static class Block implements Serializable {
        private static final Block INSTANCE = new Block();

        private Object writeReplace() {
            return INSTANCE;
        }

        private Object readResolve() {
            return INSTANCE;
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object obj) {
            return INSTANCE == obj;
        }

        @Override
        public int hashCode() {
            return INSTANCE.hashCode();
        }
    }

    public interface RequiredExtractor extends Serializable {

    }

    @Getter
    @EqualsAndHashCode
    @ToString
    public static class RequiredExtractorImpl implements RequiredExtractor {
        private final String test;

        public RequiredExtractorImpl() {
            test = RandomStringUtils.random(12);
        }
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    public static class NBTTagCompound implements Serializable {
        private final Map<String, String> data = new HashMap<>();

        public NBTTagCompound() {
            for(int i = 0; i < RandomUtils.nextInt(10, 20); i++) {
                data.put(RandomStringUtils.random(10), RandomStringUtils.random(10));
            }
        }
    }

    @EqualsAndHashCode
    @ToString
    @Getter
    public static class BlockPos implements Externalizable {
        private int x;
        private int y;
        private int z;

        public BlockPos() {
            x = RandomUtils.nextInt();
            y = RandomUtils.nextInt();
            z = RandomUtils.nextInt();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(x);
            out.writeInt(y);
            out.writeInt(z);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            x = in.readInt();
            y = in.readInt();
            z = in.readInt();
        }
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    public static class JsonSelector implements Serializable {
        private final String base;
        private final List<Expression> expressions;

        public JsonSelector() {
            base = RandomStringUtils.random(12);
            expressions = new ArrayList<>();
            expressions.add(new Expression());
        }
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    public static class Expression implements Serializable {
        private final String key;
        private final ExpressionType type;

        public Expression() {
            key = RandomStringUtils.random(12);
            type = RandomUtils.nextInt(0, 2) == 0 ? ExpressionType.A : ExpressionType.B;
        }
    }

    public enum ExpressionType {
        A,
        B
    }
}
