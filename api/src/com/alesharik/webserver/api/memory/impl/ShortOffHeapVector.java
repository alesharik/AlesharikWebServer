package com.alesharik.webserver.api.memory.impl;

import com.alesharik.webserver.api.memory.OffHeapVector;

public class ShortOffHeapVector extends OffHeapVector<Short> {
    @Override
    protected long getElementSize() {
        return 2L; //sizeof(short)
    }

    @Override
    protected Short getUnsafe(long address) {
        return unsafe.getShort(address);
    }

    @Override
    protected void setUnsafe(long address, Short s) {
        unsafe.putShort(address, s);
    }

    @Override
    protected boolean elementEquals(Short t1, Short t2) {
        return t1.compareTo(t2) == 0;
    }

    public void remove(long address, short obj) {
        super.remove(address, (Short) obj);
    }
}
