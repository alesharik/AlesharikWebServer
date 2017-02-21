package com.alesharik.webserver.api.memory.impl;

import com.alesharik.webserver.api.memory.OffHeapVector;

public class CharOffHeapVector extends OffHeapVector<Character> {
    @Override
    protected long getElementSize() {
        return 2; //sizeof(char) - UTF16
    }

    @Override
    protected Character getUnsafe(long address) {
        return unsafe.getChar(address);
    }

    @Override
    protected void setUnsafe(long address, Character character) {
        unsafe.putChar(address, character);
    }

    @Override
    protected boolean elementEquals(Character t1, Character t2) {
        return t1.compareTo(t2) == 0;
    }

    public void remove(long address, char obj) {
        super.remove(address, (Character) obj);
    }
}
