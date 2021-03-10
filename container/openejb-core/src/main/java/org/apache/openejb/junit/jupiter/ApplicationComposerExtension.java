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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ApplicationComposerExtension extends ApplicationComposerExtensionBase implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ApplicationComposerExtension.class.getName());

    @Override
    public void beforeAll(ExtensionContext context) {
        if (isPerClass(context)) {
            doStart(context);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        final ExtensionContext.Store store = context.getStore(NAMESPACE);

        if (isPerClass(context)) {
            store.get(ApplicationComposers.class, ApplicationComposers.class).after();
        }
    }


    @Override
    public void beforeEach(ExtensionContext context) {
        if (!isPerClass(context)) {
            doStart(context);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (!isPerClass(context)) {
            context.getStore(NAMESPACE).get(ApplicationComposers.class, ApplicationComposers.class).after();
        }
    }

    private void doStart(final ExtensionContext extensionContext) {

        Optional<Class<?>> oClazz = extensionContext.getTestClass();

        if (!oClazz.isPresent()) {
            throw new OpenEJBRuntimeException("Could not get test class from the given extension context.");
        }

        extensionContext.getStore(NAMESPACE).put(ApplicationComposers.class,
                new ApplicationComposers(oClazz.get(), getAdditionalModules(oClazz.get())));

        doInject(extensionContext);
    }

    private void doInject(final ExtensionContext extensionContext) {
        Optional<TestInstances> oTestInstances = extensionContext.getTestInstances();

        if (!oTestInstances.isPresent()) {
            throw new OpenEJBRuntimeException("No test instances available for the given extension context.");
        }

        List<Object> testInstances = oTestInstances.get().getAllInstances();

        ApplicationComposers delegate = extensionContext.getStore(NAMESPACE)
                .get(ApplicationComposers.class, ApplicationComposers.class);

        testInstances.forEach(t -> {
            try {
                delegate.before(t);
            } catch (Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
        });

    }

    private Object[] getAdditionalModules(final Class<?> clazz) {
        return Arrays.stream(clazz.getClasses())
                .map(this::newInstance)
                .filter(Objects::nonNull)
                .toArray();
    }

    private Object newInstance(final Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            //no-op
        }
        return null;
    }



}
