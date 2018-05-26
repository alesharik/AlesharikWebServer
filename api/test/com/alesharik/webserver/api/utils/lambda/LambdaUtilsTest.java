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

package com.alesharik.webserver.api.utils.lambda;

import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.test.TestUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.alesharik.webserver.api.utils.lambda.LambdaUtils.*;
import static com.alesharik.webserver.api.utils.lambda.LambdaUtils.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class LambdaUtilsTest {
    @Test
    public void testWhenWithBatch() throws Exception {
        List<TestContainer> testList = new ArrayList<>();

        TestContainer2 test = new TestContainer2("test");
        val action = action(TestContainer.class, test);
        TestContainer2 ret = when(action).batch(testContainerBatch -> testContainerBatch
                .then(testList::add)
                .then(testList::add));
        assertEquals(test, ret);

        TestContainer testContainer = new TestContainer("sdfaasdsadsdasda");
        fire(action, testContainer);

        assertEquals(2, testList.size());
        assertEquals(testContainer, testList.get(0));
        assertEquals(testContainer, testList.get(1));
    }

    @Test
    public void testWhenWithThen() throws Exception {
        List<TestContainer> testList = new ArrayList<>();

        TestContainer2 test = new TestContainer2("test");
        val action = action(TestContainer.class, test);
        TestContainer2 ret = when(action).then(testList::add);
        assertEquals(test, ret);

        TestContainer testContainer = new TestContainer("sdfaasdsadsdasda");
        fire(action, testContainer);

        assertEquals(1, testList.size());
        assertEquals(testContainer, testList.get(0));
    }

    @Test
    public void testLazySingleton() throws Exception {
        Supplier<TestContainer> mock = mock(Supplier.class);
        Mockito.when(mock.get()).thenReturn(new TestContainer(Utils.getRandomString(10)));

        Supplier<TestContainer> supplier = lazySingleton(mock);

        assertEquals(supplier.get(), supplier.get());
        verify(mock, times(1)).get();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhenWithCustomAction() throws Exception {
        Action action = mock(Action.class);
        when(action).then(System.out::println);
    }

    @Test(expected = IllegalStateException.class)
    public void testWhenOverflowOnThenMethodWhenCalledWhenMethodTwoTimes() throws Exception {
        val action = action(TestContainer.class, new RuntimeException());
        When<TestContainer, RuntimeException> when = when(action);
        when.then(System.out::println);
        when.then(System.out::println);
    }

    @Test(expected = IllegalStateException.class)
    public void testWhenOverflowOnThenMethodWhenCalledBatchAndWhenMethods() throws Exception {
        val action = action(TestContainer.class, new RuntimeException());
        When<TestContainer, RuntimeException> when = when(action);
        when.batch(testContainerBatch -> testContainerBatch.then(System.out::println));
        when.then(System.out::println);
    }

    @Test(expected = IllegalStateException.class)
    public void testWhenOverflowOnBatchMethodWhenCalledBatchMethodTwoTimes() throws Exception {
        val action = action(TestContainer.class, new RuntimeException());
        When<TestContainer, RuntimeException> when = when(action);
        when.batch(testContainerBatch -> testContainerBatch.then(System.out::println));
        when.batch(testContainerBatch -> testContainerBatch.then(System.out::println));
    }

    @Test(expected = IllegalStateException.class)
    public void testWhenOverflowOnBatchMethodWhenCalledWhenAndBatchMethods() throws Exception {
        val action = action(TestContainer.class, new RuntimeException());
        When<TestContainer, RuntimeException> when = when(action);
        when.then(System.out::println);
        when.batch(testContainerBatch -> testContainerBatch.then(System.out::println));
    }

    @Test
    public void testSimpleAction() throws Exception {
        val act = action(TestContainer.class);
        TestContainer container = new TestContainer("asdf");
        assertNull(when(act).then(testContainer -> assertEquals(container, testContainer)));
        act.call(container);
    }

    @Test
    public void testUtility() throws Exception {
        TestUtils.assertUtilityClass(LambdaUtils.class);
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    private static final class TestContainer {
        private final String test;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    private static final class TestContainer2 {
        private final String test;
    }
}