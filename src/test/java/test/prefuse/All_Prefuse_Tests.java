package test.prefuse;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ test.prefuse.data.All_PrefuseData_Tests.class,
		test.prefuse.data.column.All_PrefuseDataColumn_Tests.class,
		test.prefuse.data.expression.All_PrefuseDataExpression_Tests.class,
		test.prefuse.data.io.All_PrefuseDataIO_Tests.class, test.prefuse.data.util.All_PrefuseDataUtil_Tests.class,
		test.prefuse.visual.All_PrefuseVisual_Tests.class })
public class All_Prefuse_Tests {

}
