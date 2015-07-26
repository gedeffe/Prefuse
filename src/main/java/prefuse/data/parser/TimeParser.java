package prefuse.data.parser;

import java.sql.Time;
import java.text.DateFormat;

/**
 * DataParser instance that parses Date values as java.util.Time instances,
 * representing a particular time (but no specific date). This class uses a
 * backing {@link java.text.DateFormat} instance to perform parsing. The
 * DateFormat instance to use can be passed in to the constructor, or by default
 * the DateFormat returned by {@link java.text.DateFormat#getTimeInstance(int)}
 * with an argument of {@link java.text.DateFormat#SHORT} is used.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class TimeParser extends DateParser {

	/**
	 * Create a new TimeParser.
	 */
	public TimeParser() {
		this(DateFormat.getTimeInstance(DateFormat.SHORT));
	}

	/**
	 * Create a new TimeParser.
	 *
	 * @param dateFormat
	 *            the DateFormat instance to use for parsing
	 */
	public TimeParser(final DateFormat dateFormat) {
		super(dateFormat);
	}

	/**
	 * Returns java.sql.Time.class.
	 *
	 * @see prefuse.data.parser.DataParser#getType()
	 */
	@Override
	public Class getType() {
		return Time.class;
	}

	/**
	 * @see prefuse.data.parser.DataParser#canParse(java.lang.String)
	 */
	@Override
	public boolean canParse(final String val) {
		try {
			this.parseTime(val);
			return true;
		} catch (final DataParseException e) {
			return false;
		}
	}

	/**
	 * @see prefuse.data.parser.DataParser#parse(java.lang.String)
	 */
	@Override
	public Object parse(final String val) throws DataParseException {
		return this.parseTime(val);
	}

	/**
	 * Parse a Time value from a text string.
	 *
	 * @param text
	 *            the text string to parse
	 * @return the parsed Time value
	 * @throws DataParseException
	 *             if an error occurs during parsing
	 */
	public Time parseTime(final String text) throws DataParseException {
		this.m_pos.setErrorIndex(0);
		this.m_pos.setIndex(0);

		// parse the data value, convert to the wrapper type
		Time t = null;
		try {
			t = Time.valueOf(text);
			this.m_pos.setIndex(text.length());
		} catch (final IllegalArgumentException e) {
			t = null;
		}
		if (t == null) {
			final java.util.Date d1 = this.m_dfmt.parse(text, this.m_pos);
			if (d1 != null) {
				t = new Time(d1.getTime());
				// don't forget to update index
				this.m_pos.setIndex(text.length());
			}
		}

		// date format will parse substrings successfully, so we need
		// to check the position to make sure the whole value was used
		if ((t == null) || (this.m_pos.getIndex() < text.length())) {
			throw new DataParseException("Could not parse Date: " + text);
		} else {
			return t;
		}
	}

} // end of class TimeParser
