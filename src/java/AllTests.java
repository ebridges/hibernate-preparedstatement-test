
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import org.apache.log4j.Logger;

/**
 * Runs all tests for the project. Test suites should be added to the method <code>AllTests#suite()</code>.
 *
 * @author EBridges
 */

public class AllTests extends TestCase {
    private final Logger log = Logger.getLogger(AllTests.class);

    public AllTests() {
        super("AllTests");
        log.debug("AllTests constructed.");
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
//        suite.addTest( <fully-qualified-test-classname>.suite() );
        suite.addTest(com.citco.poc.HibernatePreparedStatementTest.suite());
        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public String toString() {
        return "[AllTests]";
    }

    protected void setUp() throws Exception {
        super.setUp();
        log.debug("setUp() called.");
    }

    protected void tearDown() throws Exception {
        super.setUp();
        log.debug("tearDown() called.");
    }
}
