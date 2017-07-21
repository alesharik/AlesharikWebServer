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

package com.alesharik.webserver.api.statistics;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Description;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.J_Result;

import java.util.concurrent.TimeUnit;

public class BasicAverageCounterConcurrencyTest {
    @State
    public static class BasicAverageCounterState {
        static final BasicAverageCounter basicAverageCounter = new BasicAverageCounter();

        static {
            basicAverageCounter.setTimeDelay(1, TimeUnit.MILLISECONDS);
        }
    }

    @SuppressWarnings("unused")//jcstress require state
    @JCStressTest
    @Description("tests BasicAverageCounterState concurrency")
    @Outcome(id = "0", expect = Expect.ACCEPTABLE, desc = "Elapsed time < 1sec")
    @Outcome(id = "200", expect = Expect.ACCEPTABLE, desc = "Right result")
    @Outcome(id = "100", expect = Expect.ACCEPTABLE, desc = "Thread switch")
    @Outcome(id = "300", expect = Expect.ACCEPTABLE, desc = "Thread switch")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Oops!")
    public static class ConcurrencyTest {
        @Actor
        public void actor1(BasicAverageCounterState state) {
            BasicAverageCounterState.basicAverageCounter.addUnit(100);
            BasicAverageCounterState.basicAverageCounter.addUnit(300);
            BasicAverageCounterState.basicAverageCounter.update();
        }

        @Actor
        public void actor2(BasicAverageCounterState state) {
            BasicAverageCounterState.basicAverageCounter.addUnit(300);
            BasicAverageCounterState.basicAverageCounter.addUnit(100);
            BasicAverageCounterState.basicAverageCounter.update();
        }

        @Arbiter
        public void arbiter(BasicAverageCounterState state, J_Result result) {
            result.r1 = BasicAverageCounterState.basicAverageCounter.getAverage();
        }
    }
}