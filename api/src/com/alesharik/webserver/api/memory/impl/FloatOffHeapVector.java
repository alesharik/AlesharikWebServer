package com.alesharik.webserver.api.memory.impl;

import com.alesharik.webserver.api.memory.OffHeapVector;

public class FloatOffHeapVector extends OffHeapVector<Float> {
    @Override
    protected long getElementSize() {
        return 4; //sizeof(float)
    }

    @Override
    protected Float getUnsafe(long address) {
        return unsafe.getFloat(address);
    }

    @Override
    protected void setUnsafe(long address, Float f) {
        unsafe.putFloat(address, f);
    }

    @Override
    protected boolean elementEquals(Float t1, Float t2) {
        return t1.compareTo(t2) == 0;
    }

    public void remove(long address, float obj) {
        super.remove(address, obj);
    }
}
