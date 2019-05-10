package com.github.liebharc.queryenrichment

import org.junit.Assert
import org.junit.Test

import java.sql.Timestamp

class ClassCastsTest {

    @Test
    fun castFloat() {
        Assert.assertEquals(1.0f, ClassCasts.castFloat(1.0f))
        this.assertCastFailure { ClassCasts.castFloat(1.0) }
    }

    @Test
    fun castDouble() {
        Assert.assertEquals(1.0, ClassCasts.castDouble(1.0))
        Assert.assertEquals(1.0, ClassCasts.castDouble(1.0f))
    }

    @Test
    fun castShort() {
        Assert.assertEquals(1.toShort(), ClassCasts.castShort(1.toShort()))
        this.assertCastFailure(cast = { ClassCasts.castShort(1) })
    }

    @Test
    fun castInteger() {
        Assert.assertEquals(1, ClassCasts.castInteger(1))
        Assert.assertEquals(1, ClassCasts.castInteger(1.toShort()))
        this.assertCastFailure { ClassCasts.castInteger(1L) }
    }

    @Test
    fun castLong() {
        Assert.assertEquals(1L, ClassCasts.castLong(1L))
        Assert.assertEquals(1L, ClassCasts.castLong(1))
        Assert.assertEquals(1L, ClassCasts.castLong(1.toShort()))
        Assert.assertEquals(1L, ClassCasts.castLong(Timestamp(1)))
    }

    @Test
    fun castBoolean() {
        Assert.assertEquals(false, ClassCasts.castBoolean(false))
        Assert.assertEquals(true, ClassCasts.castBoolean(1L))
        Assert.assertEquals(false, ClassCasts.castBoolean(0))
        Assert.assertEquals(true, ClassCasts.castBoolean(1.toShort()))
    }

    private fun assertCastFailure(cast: () -> Any?) {
        try {
            cast()
            Assert.fail("Class cast should fail")
        } catch (ex: ClassCastException) {
            // Pass the test
        }

    }
}