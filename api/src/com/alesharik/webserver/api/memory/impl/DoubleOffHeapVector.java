package com.alesharik.webserver.api.memory.impl;

import com.alesharik.webserver.api.memory.OffHeapVector;

public class DoubleOffHeapVector extends OffHeapVector<Double> {
    @Override
    protected long getElementSize() {
        return 8L; //sizeof(double)
    }

    @Override
    protected Double getUnsafe(long address) {
        return unsafe.getDouble(address);
    }

    @Override
    protected void setUnsafe(long address, Double d) {
        unsafe.putDouble(address, d);
    }

    @Override
    protected boolean elementEquals(Double t1, Double t2) {
        return t1.compareTo(t2) == 0;
    }

    public void remove(long address, double obj) {
        super.remove(address, obj);
    }
}
