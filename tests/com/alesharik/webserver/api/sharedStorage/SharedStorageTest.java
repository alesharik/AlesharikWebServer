package com.alesharik.webserver.api.sharedStorage;

import com.alesharik.webserver.api.sharedStorage.annotations.SharedValueGetter;
import com.alesharik.webserver.api.sharedStorage.annotations.SharedValueSetter;
import com.alesharik.webserver.api.sharedStorage.annotations.UseSharedStorage;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

public class SharedStorageTest {

    @Test
    public void testStorage() {
        Test1 test1 = new Test1();
        test1.set("asdf");
        assertTrue("asdf".equals(test1.get()));
        test1.set1("qwer");
        assertTrue("qwer".equals(test1.get1()));
    }

    @UseSharedStorage("test")
    public static class Test1 {
        @SharedValueGetter("asd")
        public String get() {
            return "";
        }

        @SharedValueSetter("asd")
        public void set(String value) {

        }

        @SharedValueGetter("asdd")
        public String get1() {
            return "";
        }

        @SharedValueSetter("asdd")
        public void set1(String value) {

        }
    }

    @Test
    public void concurrentTestStorage() throws InterruptedException {
        final Test2 test2 = new Test2();
        Thread thread = new Thread(() -> test2.set("asd"));
        thread.start();
        thread.join();
        AtomicBoolean correct = new AtomicBoolean(false);
        Thread thread1 = new Thread(() -> correct.set("asd".equals(test2.get())));
        thread1.start();
        thread1.join();
        assertTrue(correct.get());
    }

    @UseSharedStorage("test1")
    public static class Test2 {
        @SharedValueGetter("asd")
        public String get() {
            return "";
        }

        @SharedValueSetter("asd")
        public void set(String value) {

        }
    }
}
