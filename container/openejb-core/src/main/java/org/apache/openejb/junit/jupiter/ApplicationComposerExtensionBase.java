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

import org.apache.openejb.junit.RunWithApplicationComposer;
import org.apache.openejb.junit.RunWithSingleApplicationComposer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ApplicationComposerExtensionBase {

    boolean isPerClass(final ExtensionContext context) {
        return context.getTestInstanceLifecycle()
                .map(it -> it.equals(TestInstance.Lifecycle.PER_CLASS))
                .orElse(false);
    }

    ExtensionMode getMode(final ExtensionContext context) {
        if (context.getTestClass().isPresent()) {
            Class<?> clazz = context.getTestClass().get();

            RunWithApplicationComposer a = clazz.getAnnotation(RunWithApplicationComposer.class);
            if (a != null) {
                return a.mode();
            }

            RunWithSingleApplicationComposer b = clazz.getAnnotation(RunWithSingleApplicationComposer.class);
            if (b != null) {
                return b.mode();
            }

        }
        return ExtensionMode.AUTO;

    }
}
