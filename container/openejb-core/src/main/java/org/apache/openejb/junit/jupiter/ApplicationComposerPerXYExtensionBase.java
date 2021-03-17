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
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;

import java.util.List;

abstract class ApplicationComposerPerXYExtensionBase extends ApplicationComposerExtensionBase implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ApplicationComposerPerXYExtensionBase.class.getName());

    private final Object[] modules;

    public ApplicationComposerPerXYExtensionBase() {
        this((Object[]) null);
    }

    public ApplicationComposerPerXYExtensionBase(Object... modules) {
        this.modules = modules;
    }

    abstract void validate(ExtensionContext context);

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        AfterAllReleaser releaser = context.getStore(NAMESPACE).get(AfterAllReleaser.class, AfterAllReleaser.class);
        if (releaser != null) {
            releaser.run(context);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        AfterEachReleaser releaser = context.getStore(NAMESPACE).get(AfterEachReleaser.class, AfterEachReleaser.class);
        if (releaser != null) {
            releaser.run(context);
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        validate(extensionContext);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        //no-op
    }

    void doInit(final ExtensionContext extensionContext) {
        Class<?> oClazz = extensionContext.getTestClass()
                .orElseThrow(() -> new OpenEJBRuntimeException("Could not get test class from the given extension context."));

        extensionContext.getStore(NAMESPACE).put(ApplicationComposers.class,
                new ApplicationComposers(oClazz, this.modules));

    }

    void doStart(final ExtensionContext extensionContext) {
        TestInstances oTestInstances = extensionContext.getTestInstances()
                .orElseThrow(() -> new OpenEJBRuntimeException("No test instances available for the given extension context."));

        List<Object> testInstances = oTestInstances.getAllInstances();

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

    void addAfterAllReleaser(ExtensionContext context) {
        context.getStore(NAMESPACE).put(AfterAllReleaser.class, new AfterAllReleaser(NAMESPACE));
    }

    void addAfterEachReleaser(ExtensionContext context) {
        context.getStore(NAMESPACE).put(AfterEachReleaser.class, new AfterEachReleaser(NAMESPACE));
    }


}

