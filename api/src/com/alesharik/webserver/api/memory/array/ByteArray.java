package com.alesharik.webserver.api.memory.array;

import com.alesharik.webserver.api.memory.Array;

public final class ByteArray extends Array<Byte> {
    @Override
    protected long getElementSize() {
        return 1;
    }

    @Override
    protected Byte getUnsafe(long address) {
        return unsafe.getByte(address);
    }

    @Override
    protected void setUnsafe(long address, Byte aByte) {
        unsafe.putByte(address, aByte);
    }

    @Override
    protected boolean elementEquals(Byte t1, Byte t2) {
        return t1.equals(t2);
    }
}
