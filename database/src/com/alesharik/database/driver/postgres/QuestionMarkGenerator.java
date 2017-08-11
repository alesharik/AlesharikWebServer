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

package com.alesharik.database.driver.postgres;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class caches question marks strings
 */
@UtilityClass
final class QuestionMarkGenerator {
    private static final Map<Integer, String> questionMarks = new ConcurrentHashMap<>();

    public static String getQuestionMarks(int count) {
        if(questionMarks.containsKey(count))
            return questionMarks.get(count);
        else {
            StringBuilder str = new StringBuilder();
            boolean notFirst = false;
            for(int i = 0; i < count; i++) {
                if(notFirst)
                    str.append(", ");
                else
                    notFirst = true;
                str.append('?');
            }
            String end = str.toString();
            questionMarks.put(count, end);
            return end;
        }
    }
}
