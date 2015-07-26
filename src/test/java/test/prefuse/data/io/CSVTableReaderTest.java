package test.prefuse.data.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import prefuse.data.Table;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;
import test.prefuse.TestConfig;
import test.prefuse.data.TableTestData;

public class CSVTableReaderTest implements TableTestData {

	// public void testReadTableString() {
	// String datasrc = "/congressional_elections.csv";
	// CSVTableReader ctr = new CSVTableReader();
	// try {
	// URL dataurl = CSVTableReader.class.getResource(datasrc);
	// Table t = ctr.readTable(dataurl);
	// assertTrue(t.getRowCount() > 9000);
	// } catch ( Exception e ) {
	// fail("Error occurred: " +e);
	// }
	// }

	/*
	 * Test method for
	 * 'edu.berkeley.guir.prefuse.data.io.CSVTableReader.readTable(InputStream)'
	 */
	@Test
	public void testReadTableInputStream() {
		/*
		 * this test assume that current locale is US (it won't work in other
		 * countries like France in Europe ...). So we will force the locale to
		 * manipulate Date and Time using expected locale.
		 */
		final Locale defaultLocale = Locale.getDefault();
		if (!defaultLocale.equals(Locale.US)) {
			Locale.setDefault(Locale.US);
		}

		// prepare data
		final byte[] data = CSV_DATA.getBytes();
		final InputStream is = new ByteArrayInputStream(data);

		// parse data
		final CSVTableReader ctr = new CSVTableReader();
		Table t = null;
		try {
			t = ctr.readTable(is);
		} catch (final DataIOException e) {
			e.printStackTrace();
			Assert.fail("Data Read Exception");
		}

		final boolean verbose = TestConfig.verbose();

		// text-dump
		if (verbose) {
			System.out.println("-- Data Types -------------");
		}
		for (int c = 0, idx = -1; c < t.getColumnCount(); ++c) {
			String name = t.getColumnType(c).getName();
			if ((idx = name.lastIndexOf('.')) >= 0) {
				name = name.substring(idx + 1);
			}
			Assert.assertEquals(t.getColumnType(c), TYPES[c]);
			if (verbose) {
				System.out.print(name + "\t");
			}
		}
		if (verbose) {
			System.out.println();
		}

		if (verbose) {
			System.out.println();
		}

		if (verbose) {
			System.out.println("-- Table Data -------------");
		}
		for (int c = 0; c < t.getColumnCount(); ++c) {
			if (verbose) {
				System.out.print(t.getColumnName(c) + "\t");
			}
			Assert.assertEquals(t.getColumnName(c), HEADERS[c]);
		}
		if (verbose) {
			System.out.println();
		}
		for (int r = 0; r < t.getRowCount(); ++r) {
			for (int c = 0; c < t.getColumnCount(); ++c) {
				final Object o = t.get(r, c);
				if (verbose) {
					System.out.print(o + "\t");
				}
				Assert.assertEquals(TABLE[c][r], o);
			}
			if (verbose) {
				System.out.println();
			}
		}
		if (verbose) {
			System.out.println();
		}

		// // interface
		// JFrame f = new JFrame("CSV Loader Test");
		// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// JTable jt = new JTable(t) {
		// TableCellRenderer defr = new DefaultTableCellRenderer();
		// public TableCellRenderer getCellRenderer(int r, int c) {
		// return defr;
		// }
		// };
		// JScrollPane jsp = new JScrollPane(jt);
		// f.getContentPane().add(jsp);
		// f.pack();
		// f.setVisible(true);
	}

}
