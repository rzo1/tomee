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
import org.apache.openejb.testing.SingleApplicationComposerBase;
import org.junit.jupiter.api.extension.*;

import java.util.List;
import java.util.Optional;

public class SingleApplicationComposerExtension extends ApplicationComposerExtensionBase implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

    private static final SingleApplicationComposerBase BASE = new SingleApplicationComposerBase();

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        BASE.close();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        BASE.start(context.getTestClass().orElse(null));
        if (isPerClass(context)) {
            doInject(context);
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        if (!isPerClass(context)) {
            doInject(context);
        }
    }

    private void doInject(final ExtensionContext extensionContext) {
        Optional<TestInstances> oTestInstances = extensionContext.getTestInstances();

        if (!oTestInstances.isPresent()) {
            throw new OpenEJBRuntimeException("No test instances available for the given extension context.");
        }

        List<Object> testInstances = oTestInstances.get().getAllInstances();

        testInstances.forEach(t -> {
            try {
                BASE.composerInject(t);
            } catch (Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
        });
    }
}
