package com.eaglesakura.android.service.data;

import com.eaglesakura.android.service.CiJUnitTester;
import com.eaglesakura.android.service.dummy.TestData;

import org.junit.Test;

import junit.framework.Assert;

public class PayloadTest extends CiJUnitTester {

    @Test
    public void Payload2String() {
        final String TEST_STRING = "ABCあいうえおDEF";
        Payload payload = Payload.fromString(TEST_STRING);
        String deserialized = Payload.deserializeStringOrNull(payload);

        Assert.assertEquals(TEST_STRING, deserialized);
    }

    @Test
    public void Payload2Protobuf() {
        TestData.SimpleMessage.Builder builder = TestData.SimpleMessage.newBuilder();
        builder.setStringValue("this is test value");

        Payload payload = Payload.fromProtobuf(builder.build());
        TestData.SimpleMessage msg = Payload.deserializeMessageOrNull(payload, TestData.SimpleMessage.class);
        Assert.assertNotNull(msg.getStringValue(), builder.getStringValue());
    }
}
