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

/**
 * This package contains hacking tools, which use ASM to provide functionality(extend from <code>private non-static</code> class, variable bindings), which reflection can't provide.
 * This mostly will be useful for extending private classes
 */
@Prefixes("[Hacker]")
package com.alesharik.webserver.api.agent.hack;

import com.alesharik.webserver.logger.Prefixes;