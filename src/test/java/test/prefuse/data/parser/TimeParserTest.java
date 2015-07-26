package test.prefuse.data.parser;

import java.sql.Time;

import org.junit.Assert;
import org.junit.Test;

import prefuse.data.parser.DataParseException;
import prefuse.data.parser.TimeParser;

public class TimeParserTest {

	@Test
	public void testGetType() {
		final TimeParser timeParser = new TimeParser();
		Assert.assertEquals(Time.class, timeParser.getType());
	}

	@Test
	public void testCanParse() {
		final TimeParser timeParser = new TimeParser();
		// good case
		final Time time = new Time(System.currentTimeMillis());
		Assert.assertTrue(timeParser.canParse(time.toString()));
		Assert.assertTrue(timeParser.canParse("1:12 AM"));
		// wrong cases
		Assert.assertFalse(timeParser.canParse(""));
		// Assert.assertFalse(timeParser.canParse("25:15:78"));
		Assert.assertFalse(timeParser.canParse("truc"));
	}

	@Test
	public void testParseTime() {
		final TimeParser timeParser = new TimeParser();
		// good case
		final Time time = new Time(System.currentTimeMillis());
		Time result;
		try {
			result = timeParser.parseTime(time.toString());
			Assert.assertNotNull(result);
			Assert.assertEquals(time.toString(), result.toString());

			result = timeParser.parseTime("1:12 AM");
			Assert.assertNotNull(result);
			Assert.assertEquals("01:12:00", result.toString());

		} catch (final DataParseException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
