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

public class ApplicationComposerExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        storeDelegateInContext(context, new ApplicationComposers(context.getRequiredTestClass()));
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        List<Object> testInstances = context.getRequiredTestInstances().getAllInstances();

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
        context.getRoot().getStore(ExtensionContext.Namespace.create(ApplicationComposerExtension.class)).put("delegate", delegate);
    }

    private Object getDelegateFromContext(ExtensionContext context) {
        return context.getRoot().getStore(ExtensionContext.Namespace.create(ApplicationComposerExtension.class)).get("delegate");
    }


}
