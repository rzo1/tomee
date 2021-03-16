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
import org.apache.openejb.testing.SingleApplicationComposerBase;
import org.junit.jupiter.api.extension.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ApplicationComposerExtension extends ApplicationComposerExtensionBase implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ApplicationComposerExtension.class.getName());
    private static final SingleApplicationComposerBase BASE = new SingleApplicationComposerBase();

    private final Object[] modules;

    public ApplicationComposerExtension() {
        this((Object) null);
    }

    public ApplicationComposerExtension(Object... modules) {
        this.modules = modules;
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        if (isPerJvm(context)) {
            BASE.start(context.getTestClass().orElse(null));
        } else if (isPerAll(context)) {
            doStart(context);
        } else if (isPerDefault(context)) {
            if (isPerClass(context)) {
                doStart(context);
            }
        }

        if (isPerClass(context)) {
            doInject(context);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (isPerJvm(context) || isPerAll(context)) {
            doRelease(context);
        } else if (isPerDefault(context) && isPerClass(context)) {
            doRelease(context);
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {

        if (isPerEach(context)) {
            doStart(context);
        } else if (isPerDefault(context) && !isPerClass(context)) {
            doStart(context);
        }

        if (!isPerClass(context)) {
            doInject(context);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (isPerEach(context)) {
            doRelease(context);
        } else if (isPerDefault(context) && !isPerClass(context)) {
            doRelease(context);
        }
    }

    private void doRelease(final ExtensionContext extensionContext) throws Exception {
        if (isPerJvm(extensionContext)) {
            //FIXME how to release / close after last test class execution?
            BASE.close();
        } else {
            extensionContext.getStore(NAMESPACE).get(ApplicationComposers.class, ApplicationComposers.class).after();
        }
    }

    private void doStart(final ExtensionContext extensionContext) {

        Class<?> oClazz = extensionContext.getTestClass()
                .orElseThrow(() -> new OpenEJBRuntimeException("Could not get test class from the given extension context."));

        extensionContext.getStore(NAMESPACE).put(ApplicationComposers.class,
                new ApplicationComposers(oClazz, this.modules));

    }

    private void doInject(final ExtensionContext extensionContext) {
        TestInstances oTestInstances = extensionContext.getTestInstances()
                .orElseThrow(() -> new OpenEJBRuntimeException("No test instances available for the given extension context."));

        List<Object> testInstances = oTestInstances.getAllInstances();

        if (isPerJvm(extensionContext)) {
            testInstances.forEach(t -> {
                try {
                    BASE.composerInject(t);
                } catch (Exception e) {
                    throw new OpenEJBRuntimeException(e);
                }
            });
        } else {
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
    }
}
