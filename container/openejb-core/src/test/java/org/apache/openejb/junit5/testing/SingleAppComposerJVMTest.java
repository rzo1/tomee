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
package org.apache.openejb.junit5.testing;

import org.apache.openejb.junit.RunWithApplicationComposer;
import org.apache.openejb.junit.jupiter.ExtensionMode;
import org.apache.openejb.loader.SystemInstance;
import org.junit.jupiter.api.Test;
import org.apache.openejb.testing.Application;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWithApplicationComposer(mode = ExtensionMode.PER_JVM)
public class SingleAppComposerJVMTest {

    @Application //inject app from other test-case, should be present due to same JVM
    private SingleAppComposerTest.ScanApp app;

    @Test
    public void run() {
        assertNotNull(app);
        assertEquals("Set-Via-SingleAppComposerTest-In-Same-JVM", SystemInstance.get().getProperty("key"));
    }

}
