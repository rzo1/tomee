package org.apache.openejb.junit5.testing.order;

import org.apache.openejb.junit.RunWithApplicationComposer;
import org.apache.openejb.junit5.testing.SingleAppComposerJVMTest;
import org.apache.openejb.junit5.testing.SingleAppComposerTest;
import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;

public class AppComposerTestClassOrderer implements ClassOrderer {

    @Override
    public void orderClasses(ClassOrdererContext classOrdererContext) {
        classOrdererContext.getClassDescriptors().sort(Comparator.comparingInt(AppComposerTestClassOrderer::getOrder));
    }

    /*
     * The order, we want here is
     *
     * 1. Everything, which is not @RunWithApplicationComposer
     * 2. Everything, which is @RunWithApplicationComposer
     * 3. SingleAppComposerTest before SingleAppComposerJVMTest (to test same JVM)
     */
    private static int getOrder(ClassDescriptor classDescriptor) {
        if (classDescriptor.findAnnotation(RunWithApplicationComposer.class).isPresent()) {

            if(classDescriptor.getTestClass().equals(SingleAppComposerTest.class)) {
                return 3;
            }

            if(classDescriptor.getTestClass().equals(SingleAppComposerJVMTest.class)) {
                return 4;
            }

            return 2;
        } else {
            return 1;
        }
    }
}
