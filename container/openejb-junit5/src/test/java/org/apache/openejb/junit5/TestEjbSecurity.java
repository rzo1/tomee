/**
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

import org.apache.openejb.api.LocalClient;
import org.apache.openejb.junit.ContextConfig;
import org.apache.openejb.junit.Property;
import org.apache.openejb.junit.TestSecurity;
import org.apache.openejb.junit5.ejbs.BasicEjbLocal;
import org.apache.openejb.junit5.ejbs.SecuredEjbLocal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ejb.EJB;
import javax.ejb.EJBAccessException;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfig(properties = {
    @Property("openejb.deployments.classpath.include=.*openejb-junit.*"),
    @Property("java.naming.factory.initial=org.apache.openejb.core.LocalInitialContextFactory")
})
@ExtendWith(OpenEjbExtension.class)
@TestSecurity(
    authorized = {"RoleA"}
)
@LocalClient
public class TestEjbSecurity {
    @EJB
    private BasicEjbLocal basicEjb;

    @EJB
    private SecuredEjbLocal securedEjb;

    public TestEjbSecurity() {
    }

    @Test
    public void testEjbInjection() {
        assertNotNull(basicEjb);
        assertNotNull(securedEjb);
    }

    @Test
    public void testClassLevelSecurity() {
        assertNotNull(securedEjb);

        assertEquals("Unsecured Works", basicEjb.concat("Unsecured", "Works"));
        assertEquals("Dual Role Works", securedEjb.dualRole());
        assertEquals("RoleA Works", securedEjb.roleA());
    }

    @Test
    public void testClassLevelSecurityUnauthorized() {
        assertThrows(EJBAccessException.class, () -> {
            assertNotNull(securedEjb);
            securedEjb.roleB();
        });
    }

    @Test
    @TestSecurity(
        authorized = {"RoleB"}
    )
    public void testMethodLevelSecurity() {
        assertNotNull(securedEjb);

        assertEquals("Unsecured Works", basicEjb.concat("Unsecured", "Works"));
        assertEquals("Dual Role Works", securedEjb.dualRole());
        assertEquals("RoleB Works", securedEjb.roleB());
    }

    @Test
    @TestSecurity(
        authorized = {"RoleB"}
    )
    public void testMethodLevelSecurityUnauthorized() {
        assertThrows(EJBAccessException.class, () -> {
            assertNotNull(securedEjb);
            securedEjb.roleA();
        });
    }

    @Test
    @TestSecurity(
        authorized = {"RoleA"},
        unauthorized = {"RoleB"}
    )
    public void testMultipleSecurityRoles_RoleA() {
        assertThrows(EJBAccessException.class, () -> {
            assertNotNull(securedEjb);
            securedEjb.roleA();
        });
    }

    @Test
    @TestSecurity(
        authorized = {"RoleB"},
        unauthorized = {"RoleA"}
    )
    public void testMultipleSecurityRoles_RoleB() {
        assertThrows(EJBAccessException.class, () -> {
            assertNotNull(securedEjb);
            securedEjb.roleB();
        });
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @Test
    @TestSecurity(
        authorized = {"RoleB"}
    )
    public void testRoleAFailAuthorized() {
        assertThrows(EJBAccessException.class, () -> {
            assertNotNull(securedEjb);
            securedEjb.roleA();
        });
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @Test
    @TestSecurity(
        authorized = {"RoleA"}
    )
    public void testRoleBFailAuthorized() {
        assertThrows(EJBAccessException.class, () -> {
            assertNotNull(securedEjb);
            securedEjb.roleB();
        });
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @Test
    @TestSecurity(
        unauthorized = {"RoleA"}
    )
    public void testRoleAFailUnauthorized() {
        assertThrows(AssertionError.class, () -> {
            securedEjb.roleA();
        });
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @Test
    @TestSecurity(
        unauthorized = {"RoleB"}
    )
    public void testRoleBFailUnauthorized() {
        assertThrows(AssertionError.class, () -> {
            securedEjb.roleB();
        });
    }

    /**
     * This test was created to ensure that the statements are created correctly.
     * They are constructed in such a way as to "incorrectly" specify the annotation
     * options, and should fail with an access exception
     */
    @Test
    @TestSecurity(
        unauthorized = {TestSecurity.UNAUTHENTICATED}
    )
    public void testUnauthenticated() {
        securedEjb.roleA();
    }
}
