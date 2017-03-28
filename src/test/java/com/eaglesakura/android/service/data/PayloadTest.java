package com.eaglesakura.android.service.data;

import com.eaglesakura.android.service.UnitTestCase;

import org.junit.Test;

import junit.framework.Assert;

public class PayloadTest extends UnitTestCase {

    @Test
    public void Payload2String() {
        final String TEST_STRING = "ABCあいうえおDEF";
        Payload payload = Payload.fromString(TEST_STRING);
        String deserialized = Payload.deserializeStringOrNull(payload);

        Assert.assertEquals(TEST_STRING, deserialized);
    }

}
