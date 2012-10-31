package com.citco.poc;

import com.citco.poc.persistence.HibernateTestDao;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * HibernatePreparedStatementTest
 *
 * @author EBridges
 * @version $Name$ ($Revision$) [Created: May 30, 2007 12:29:08 PM]
 */
public class HibernatePreparedStatementTest extends TestCase {
    private Logger log = Logger.getLogger(HibernatePreparedStatementTest.class);
    private static final String PROPERTIES = "/hibernate-test.properties";
    private HibernateTestDao dao;
    private int repeatCount;


    public HibernatePreparedStatementTest() throws IOException {
        super(HibernatePreparedStatementTest.class.getName());
        Properties properties = new Properties();
        InputStream is = getClass().getResourceAsStream(PROPERTIES);
        if(null == is) {
            log.error("Unable to locate properties file on classpath using ["+PROPERTIES+"].");
            throw new IllegalArgumentException("Unable to locate properties file on classpath using ["+PROPERTIES+"].");
        }
        properties.load( is );
        repeatCount = Integer.parseInt(properties.getProperty("application.test-run-count", "100"));
        log.info("Query repeat count: ["+repeatCount+"]");
    }

    public void testQueryHqlWithTwoBoundParameters() {
        Map p = new HashMap();
        p.put("amount",new BigDecimal("0.125"));
        p.put("bigText","aaaaaaaaa");
        dao.queryHqlWithBindParameters("QueryHqlWithTwoBoundParameters", repeatCount, p);
    }

    public void testQueryHqlWithTwoHardcodedParameters() {
        Map p = new HashMap();
        p.put("amount",new BigDecimal("0.125"));
        p.put("bigText","aaaaaaaaa");
        dao.queryHqlWithoutBindParameters("QueryHqlWithTwoHardcodedParameters",repeatCount,p);
    }

    public void testQuerySqlWithTwoBoundParameters() {
        Map p = new HashMap();
        p.put("amount",new BigDecimal("0.125"));
        p.put("big_text","aaaaaaaaa");
        dao.querySqlWithBindParameters("QuerySqlWithTwoBoundParameters",repeatCount,p);
    }

    public void testQuerySqlWithTwoHardcodedParameters() {
        Map p = new HashMap();
        p.put("amount",new BigDecimal("0.125"));
        p.put("big_text","aaaaaaaaa");
        dao.querySqlWithoutBindParameters("QuerySqlWithTwoHardcodedParameters",repeatCount,p);
    }

    public void testLikeQueryHqlWithOneBoundParameter() {
        Map p = new HashMap();
        p.put("bigText","%aaaaaaaaa");
        dao.queryHqlWithBindParameters("LikeQueryHqlWithOneBoundParameter",repeatCount, p);
    }

    public void testLikeQueryHqlWithOneHardcodedParameter() {
        Map p = new HashMap();
        p.put("bigText","%aaaaaaaaa");
        dao.queryHqlWithoutBindParameters("LikeQueryHqlWithOneHardcodedParameter",repeatCount,p);
    }

    public void testLikeQuerySqlWithOneBoundParameter() {
        Map p = new HashMap();
        p.put("big_text","%aaaaaaaaa");
        dao.querySqlWithBindParameters("LikeQuerySqlWithOneBoundParameter",repeatCount,p);
    }

    public void testLikeQuerySqlWithOneHardcodedParameter() {
        Map p = new HashMap();
        p.put("big_text","%aaaaaaaaa");
        dao.querySqlWithoutBindParameters("LikeQuerySqlWithOneHardcodedParameter",repeatCount,p);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dao = HibernateTestDao.instance();
    }

    protected void tearDown() throws Exception {
        dao = null;
        super.tearDown();
    }

    public static Test suite() {
        return new TestSuite(HibernatePreparedStatementTest.class);
    }
}
