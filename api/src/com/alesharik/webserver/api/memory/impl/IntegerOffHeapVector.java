package com.alesharik.webserver.api.memory.impl;

import com.alesharik.webserver.api.memory.OffHeapVector;

public class IntegerOffHeapVector extends OffHeapVector<Integer> {
    @Override
    protected long getElementSize() {
        return 4L; //sizeof(int)
    }

    @Override
    protected Integer getUnsafe(long address) {
        return unsafe.getInt(address);
    }

    @Override
    protected void setUnsafe(long address, Integer integer) {
        unsafe.putInt(address, integer);
    }

    @Override
    protected boolean elementEquals(Integer t1, Integer t2) {
        return t1.compareTo(t2) == 0;
    }

    public void remove(long address, int obj) {
        super.remove(address, (Integer) obj);
    }
}
