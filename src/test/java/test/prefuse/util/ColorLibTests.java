package test.prefuse.util;

import org.junit.Assert;
import org.junit.Test;

import javafx.scene.paint.Color;
import prefuse.util.ColorLib;

public class ColorLibTests {

    @Test
    public void testGray() {
        final int gray = ColorLib.gray(200);
        final int expectedGray = -3618616;
        Assert.assertEquals(expectedGray, gray);

        final int black = ColorLib.gray(0);
        final int expectedBlack = -16777216;
        Assert.assertEquals(expectedBlack, black);
    }

    @Test
    public void testRgb() {
        final int expected = -16726016;

        int result = ColorLib.rgb(0, 200, 0);

        Assert.assertEquals(expected, result);

        result = ColorLib.rgb(0, 0, 0);
        final int black = -16777216;
        Assert.assertEquals(black, result);

    }

    @Test
    public void testRgbaIntIntIntInt() {
        final int expected = -16726016;

        final int result = ColorLib.rgba(0, 200, 0, 255);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testRgbaDoubleDoubleDoubleDouble() {
        final int expected = -16726016;

        final int result = ColorLib.rgba(0, (double) 200 / 255, 0, 1);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testColor() {
        final int expected = -16777216;

        final int result = ColorLib.color(Color.BLACK);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testRed() {
        final int onlyRed = 16711680;
        int result = ColorLib.red(onlyRed);
        Assert.assertEquals(255, result);

        final int onlyBlue = 255;
        result = ColorLib.red(onlyBlue);
        Assert.assertEquals(0, result);

        final int onlyGreen = 65280;
        result = ColorLib.red(onlyGreen);
        Assert.assertEquals(0, result);

        // red = 150, green = 150 and blue = 150;
        final int intermediateColor = 9868950;
        result = ColorLib.red(intermediateColor);
        Assert.assertEquals(150, result);
    }

    @Test
    public void testGreen() {
        final int onlyRed = 16711680;
        int result = ColorLib.green(onlyRed);
        Assert.assertEquals(0, result);

        final int onlyBlue = 255;
        result = ColorLib.green(onlyBlue);
        Assert.assertEquals(0, result);

        final int onlyGreen = 65280;
        result = ColorLib.green(onlyGreen);
        Assert.assertEquals(255, result);

        // red = 150, green = 150 and blue = 150;
        final int intermediateColor = 9868950;
        result = ColorLib.green(intermediateColor);
        Assert.assertEquals(150, result);
    }

    @Test
    public void testBlue() {
        final int onlyRed = 16711680;
        int result = ColorLib.blue(onlyRed);
        Assert.assertEquals(0, result);

        final int onlyBlue = 255;
        result = ColorLib.blue(onlyBlue);
        Assert.assertEquals(255, result);

        final int onlyGreen = 65280;
        result = ColorLib.blue(onlyGreen);
        Assert.assertEquals(0, result);

        // red = 150, green = 150 and blue = 150;
        final int intermediateColor = 9868950;
        result = ColorLib.blue(intermediateColor);
        Assert.assertEquals(150, result);
    }

    @Test
    public void testAlpha() {
        final int onlyRed = 16711680;
        double result = ColorLib.alpha(onlyRed);
        Assert.assertEquals(0, result, 0);

        final int onlyBlue = 255;
        result = ColorLib.alpha(onlyBlue);
        Assert.assertEquals(0, result, 0);

        final int onlyGreen = 65280;
        result = ColorLib.alpha(onlyGreen);
        Assert.assertEquals(0, result, 0);

        // red = 150, green = 150 and blue = 150;
        final int intermediateColor = 9868950;
        result = ColorLib.alpha(intermediateColor);
        Assert.assertEquals(0, result, 0);

        final int opaqueBlack = -16777216;
        result = ColorLib.alpha(opaqueBlack);
        Assert.assertEquals(1, result, 0);
    }

    @Test
    public void testGetColorIntIntIntInt() {
        final Color result = ColorLib.getColor(0, 0, 0, 255);

        Assert.assertEquals(Color.BLACK, result);
    }

    @Test
    public void testGetColorIntIntInt() {
        final Color result = ColorLib.getColor(0, 0, 0);

        Assert.assertEquals(Color.BLACK, result);
    }

    @Test
    public void testGetColorInt() {
        final Color result = ColorLib.getColor(-16777216);

        Assert.assertEquals(Color.BLACK, result);
    }

}
