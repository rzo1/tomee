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
package org.apache.openejb.junit5;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.testing.SingleApplicationComposerBase;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;

import java.util.List;

public class ApplicationComposerPerJVMExtension extends ApplicationComposerPerXYExtensionBase implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final SingleApplicationComposerBase BASE = new SingleApplicationComposerBase();

    @Override
    void validate(ExtensionContext context) {
        if (!isPerJvm(context) && BASE.isStarted()) {
            //XXX: Future work: We might get it to work via a JVM singleton/lock, see https://github.com/apache/tomee/pull/767#discussion_r595343572
            throw new OpenEJBRuntimeException("Cannot run PER_JVM in combination with PER_ALL, PER_EACH or AUTO");
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        super.beforeAll(context);

        BASE.start(context.getTestClass().orElse(null));
        if (isPerClassLifecycle(context)) {
            doInject(context);
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (isPerMethodLifecycle(context)) {
            doInject(context);
        }
    }

    private void doInject(final ExtensionContext extensionContext) {
        TestInstances oTestInstances = extensionContext.getTestInstances()
                .orElseThrow(() -> new OpenEJBRuntimeException("No test instances available for the given extension context."));

        List<Object> testInstances = oTestInstances.getAllInstances();

        testInstances.forEach(t -> {
            try {
                BASE.composerInject(t);
            } catch (Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
        });
    }

    public static boolean isStarted() {
        return BASE.isStarted();
    }


}

