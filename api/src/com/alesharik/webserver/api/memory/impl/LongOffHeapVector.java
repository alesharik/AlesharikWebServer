package com.alesharik.webserver.api.memory.impl;

import com.alesharik.webserver.api.memory.OffHeapVector;

public class LongOffHeapVector extends OffHeapVector<Long> {
    @Override
    protected long getElementSize() {
        return 8L; //sizeof(long)
    }

    @Override
    protected Long getUnsafe(long address) {
        return unsafe.getLong(address);
    }

    @Override
    protected void setUnsafe(long address, Long l) {
        unsafe.putLong(address, l);
    }

    @Override
    protected boolean elementEquals(Long t1, Long t2) {
        return t1.compareTo(t2) == 0;
    }

    public void remove(long address, long obj) {
        super.remove(address, (Long) obj);
    }
}
