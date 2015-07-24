package test.prefuse.data.io.sql;

import java.util.logging.Logger;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import prefuse.data.Table;
import prefuse.data.io.sql.ConnectionFactory;
import prefuse.data.io.sql.DatabaseDataSource;
import prefuse.util.ui.JPrefuseTable;
import test.prefuse.TestConfig;

public class MySQLConnectionTest {

	// logger
	private static final Logger s_logger = Logger.getLogger(MySQLConnectionTest.class.getName());

	private Table m_table;

	/**
	 * We could ignore this test as it needs an existing MySQL database. And
	 * MySQL does not support in memory use.
	 */
	@Test
	@Ignore
	public void testLoadFromMySQLDatabase() {
		final String host = "localhost";
		final String database = "friendster";
		final String user = "anonymous";
		final String password = "";

		final String keyField = "uid";
		final String query1 = "SELECT profiles.* FROM profiles, graph WHERE "
				+ "(graph.uid1 = 186297 AND profiles.uid = graph.uid2)";
		final String query2 = "SELECT profiles.* FROM profiles, graph WHERE "
				+ "(graph.uid1 = 21721 AND profiles.uid = graph.uid2)";

		// String query = "SELECT gender, age, COUNT(*) FROM profiles GROUP BY
		// gender,age";

		s_logger.info(TestConfig.memUse());

		Table t = null;
		try {
			final DatabaseDataSource db = ConnectionFactory.getMySQLConnection(host, database, user, password);

			s_logger.info(TestConfig.memUse());

			t = db.getData(t, query1, keyField);
			db.loadData(t, query2, keyField);

		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail("Error connecting to database");
		}

		// text-dump
		final StringBuffer sbuf = new StringBuffer('\n');
		sbuf.append("--[Table: ").append(t.getRowCount()).append(" rows, ").append(t.getColumnCount())
				.append(" cols]--\n");
		for (int c = 0, idx = -1; c < t.getColumnCount(); ++c) {
			String name = t.getColumnType(c).getName();
			if ((idx = name.lastIndexOf('.')) >= 0) {
				name = name.substring(idx + 1);
			}
			sbuf.append(c).append("\t").append(name).append("\t").append(t.getColumnName(c)).append('\n');
		}
		sbuf.append('\n');
		sbuf.append(TestConfig.memUse()).append('\n');
		s_logger.info(sbuf.toString());

		this.m_table = t;
	}

	public static void main(final String[] args) {
		final MySQLConnectionTest test = new MySQLConnectionTest();
		test.testLoadFromMySQLDatabase();

		final JFrame f = JPrefuseTable.showTableWindow(test.m_table);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
