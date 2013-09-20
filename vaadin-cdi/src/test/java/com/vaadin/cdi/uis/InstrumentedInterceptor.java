/*
 * Copyright 2000-2013 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.cdi.uis;

import java.util.concurrent.atomic.AtomicInteger;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 */
public class InstrumentedInterceptor {

    private final static AtomicInteger INTERCEPTION_COUNTER = new AtomicInteger(
            0);

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext)
            throws Exception {
        System.out.println("---invoked: " + invocationContext.getMethod());
        INTERCEPTION_COUNTER.incrementAndGet();
        return invocationContext.proceed();

    }

    public static int getCounter() {
        return INTERCEPTION_COUNTER.get();
    }

    public static void reset() {
        INTERCEPTION_COUNTER.set(0);
    }
}
