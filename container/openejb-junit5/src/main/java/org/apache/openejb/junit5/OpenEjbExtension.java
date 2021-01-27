package org.apache.openejb.junit5;

import org.apache.openejb.junit.TestSecurity;
import org.apache.openejb.junit.context.OpenEjbTestContext;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class OpenEjbExtension implements BeforeEachCallback, AfterEachCallback {

    private OpenEjbTestContext classTestContext;

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {

        TestSecurity testSecurity = null;
        if (extensionContext.getTestMethod().isPresent() && extensionContext.getTestMethod().get().isAnnotationPresent(TestSecurity.class)) {
            testSecurity = extensionContext.getTestMethod().get().getAnnotation(TestSecurity.class);
        } else if (extensionContext.getTestClass().isPresent() && extensionContext.getTestClass().get().isAnnotationPresent(TestSecurity.class)) {
            testSecurity = extensionContext.getTestClass().get().getAnnotation(TestSecurity.class);
        }

        if (extensionContext.getTestInstance().isPresent()) {
            // no security to run as, just create a normal statement
            if (testSecurity == null ||
                    (testSecurity.authorized().length == 0 && testSecurity.unauthorized().length == 0)) {
                classTestContext = newTestContext(extensionContext, null);
                classTestContext.configureTest(extensionContext.getTestInstance().get());
            }
            // security roles specified, create separate statements for them all
            else {
                String[] authorized = testSecurity.authorized();
                String[] unauthorized = testSecurity.authorized();

                //FIXME implement multiple testing for security annotated test
            }
        }

    }

    public OpenEjbTestContext newTestContext(ExtensionContext extensionContext, final String roleName) {
        if (!extensionContext.getTestMethod().isPresent()) {
            if (classTestContext == null) {
                classTestContext = new OpenEjbTestContext(extensionContext.getTestClass().get());
            }
            return classTestContext;
        } else {
            return new OpenEjbTestContext(extensionContext.getTestMethod().get(), roleName);
        }
    }


    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        if (classTestContext != null) {
            classTestContext.close();
        }
    }
}
