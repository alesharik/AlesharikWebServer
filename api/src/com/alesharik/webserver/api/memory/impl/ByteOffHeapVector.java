package com.alesharik.webserver.api.memory.impl;

import com.alesharik.webserver.api.memory.OffHeapVector;

public final class ByteOffHeapVector extends OffHeapVector<Byte> {
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

    public void remove(long address, byte obj) {
        super.remove(address, (Byte) obj);
    }
}
