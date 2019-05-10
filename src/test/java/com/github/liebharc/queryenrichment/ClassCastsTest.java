package com.github.liebharc.queryenrichment;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Timestamp;

public class ClassCastsTest {

    @Test
    public void castFloat() {
        Assert.assertEquals(1.0f, ClassCasts.castFloat(1.0f));
        this.assertCastFailure(() -> ClassCasts.castFloat(1.0));
    }

    @Test
    public void castDouble() {
        Assert.assertEquals(1.0, ClassCasts.castDouble(1.0));
        Assert.assertEquals(1.0, ClassCasts.castDouble(1.0f));
    }

    @Test
    public void castShort() {
        Assert.assertEquals((short)1, ClassCasts.castShort((short)1));
        this.assertCastFailure(() -> ClassCasts.castShort(1));
    }

    @Test
    public void castInteger() {
        Assert.assertEquals(1, ClassCasts.castInteger(1));
        Assert.assertEquals(1, ClassCasts.castInteger((short)1));
        this.assertCastFailure(() -> ClassCasts.castInteger(1L));
    }

    @Test
    public void castLong() {
        Assert.assertEquals(1L, ClassCasts.castLong(1L));
        Assert.assertEquals(1L, ClassCasts.castLong(1));
        Assert.assertEquals(1L, ClassCasts.castLong((short)1));
        Assert.assertEquals(1L, ClassCasts.castLong(new Timestamp(1)));
    }

    @Test
    public void castBoolean() {
        Assert.assertEquals(false, ClassCasts.castBoolean(false));
        Assert.assertEquals(true, ClassCasts.castBoolean(1L));
        Assert.assertEquals(false, ClassCasts.castBoolean(0));
        Assert.assertEquals(true, ClassCasts.castBoolean((short)1));
    }

    private void assertCastFailure(Runnable cast) {
        try {
            cast.run();
            Assert.fail("Class cast should fail");
        }
        catch(ClassCastException ex) {
            // Pass the test
        }
    }
}