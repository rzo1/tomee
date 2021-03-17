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

import org.apache.openejb.testing.ApplicationComposers;
import org.junit.jupiter.api.extension.ExtensionContext;

public abstract class AfterReleaserBase extends ApplicationComposerExtensionBase {

    private final ExtensionContext.Namespace namespace;

    AfterReleaserBase(ExtensionContext.Namespace namespace) {
        this.namespace = namespace;
    }

    void run(final ExtensionContext extensionContext) throws Exception {
        doRelease(extensionContext);
    }

    void doRelease(final ExtensionContext extensionContext) throws Exception {
        extensionContext.getStore(namespace).get(ApplicationComposers.class, ApplicationComposers.class).after();
    }

}
