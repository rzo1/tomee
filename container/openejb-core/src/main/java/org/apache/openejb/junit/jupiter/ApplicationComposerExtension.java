/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.junit.jupiter;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.testing.ApplicationComposers;
import org.junit.jupiter.api.extension.*;

import java.util.List;
import java.util.Optional;

public class ApplicationComposerExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ApplicationComposerExtension.class.getName());

    @Override
    public void beforeAll(ExtensionContext context) {
        Optional<Class<?>> oClazz = context.getTestClass();

        if (!oClazz.isPresent()) {
            throw new RuntimeException("Could not get test class from extension context");
        }

        storeDelegateInContext(context, new ApplicationComposers(oClazz.get()));
    }

    @Override
    public void beforeEach(ExtensionContext context) {

        Optional<TestInstances> oTestInstances = context.getTestInstances();

        if(!oTestInstances.isPresent()) {
            throw new OpenEJBRuntimeException("Not test instances available for given test context.");
        }

        List<Object> testInstances = oTestInstances.get().getAllInstances();

        Object delegate = getDelegateFromContext(context);

        if (delegate instanceof ApplicationComposers) {
            testInstances.forEach(t -> {
                try {
                    ((ApplicationComposers) delegate).before(t);
                } catch (Exception e) {
                    throw new OpenEJBRuntimeException(e);
                }
            });
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Object delegate = getDelegateFromContext(context);
        if (delegate instanceof ApplicationComposers) {
            try {
                ((ApplicationComposers) delegate).after();
            } catch (final Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
    }

    private void storeDelegateInContext(ExtensionContext context, ApplicationComposers delegate) {
        context.getStore(NAMESPACE).put("delegate", delegate);
    }

    private Object getDelegateFromContext(ExtensionContext context) {
        return context.getStore(NAMESPACE).get("delegate");
    }


}
