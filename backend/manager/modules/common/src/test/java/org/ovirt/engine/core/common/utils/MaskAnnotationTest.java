package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.annotation.Mask;

@RunWith(Parameterized.class)
public class MaskAnnotationTest {

    @Parameterized.Parameter(0)
    public String mask;
    @Parameterized.Parameter(1)
    public boolean isValidMaskFormat;
    @Parameterized.Parameter(2)
    public boolean isValidMaskValue;
    private Validator validator;

    @Before
    public void setup() {
        validator = ValidationUtils.getValidator();
    }

    @Test
    public void checkCidrFormatAnnotation() {
        MaskContainer container = new MaskContainer(mask);
        Set<ConstraintViolation<MaskContainer>> result = validator.validate(container);
        if (!isValidMaskValue && isValidMaskFormat) {
            assertEquals("Failed to validate mask's error format: " + container.getMask(),
                    EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_VALUE.name(),
                    result.iterator().next().getMessage());
        } else if (!isValidMaskFormat) {
            assertEquals("Failed to validate mask's error format: " + container.getMask(),
                    EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_FORMAT.name(),
                    result.iterator().next().getMessage());
        } else {
            assertEquals("Failed to validate mask's format: " + container.getMask(),
                    isValidMaskFormat,
                    result.isEmpty());
        }

    }

    @Test
    public void checkCidrNetworkAddressAnnotation() {
        MaskContainer container = new MaskContainer(mask);
        Set<ConstraintViolation<MaskContainer>> result = validator.validate(container);
        if (!isValidMaskFormat) {
            assertEquals("Failed to validate mask's network address error: " + container.getMask(),
                    EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_FORMAT.name(),
                    result.iterator().next().getMessage());
        } else if (!isValidMaskValue) {
            assertEquals("Failed to validate mask's  network address error: " + container.getMask(),
                    EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_VALUE.name(),
                    result.iterator().next().getMessage());
        } else {
            assertEquals("Failed to validate mask's network address: " + container.getMask(),
                    isValidMaskValue,
                    result.isEmpty());
        }

    }

    @Parameterized.Parameters
    public static Object[][] namesParams() {
        return new Object[][] {
                // Bad Format
                { "a.a.a.a", false, false },
                { "//32", false, false },
                { "33", false, false },

                // Not A Valid Mask
                { "253.0.0.32", true, false },

                // valid mask
                { "255.255.255.0", true, true },
                { "15", true, true }
        };
    }

    private static class MaskContainer {
        @Mask
        private final String mask;

        public MaskContainer(String mask) {
            this.mask = mask;
        }

        public String getMask() {
            return mask;
        }

    }

}
