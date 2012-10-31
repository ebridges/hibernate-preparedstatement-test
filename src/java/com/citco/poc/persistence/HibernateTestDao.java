package com.citco.poc.persistence;

import com.citco.poc.domain.HibernateTestClass;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HibernateTestDao
 *
 * @author EBridges
 * @version $Name$ ($Revision$) [Created: May 30, 2007 11:43:00 AM]
 */
public class HibernateTestDao {
    private static final Logger log = Logger.getLogger(HibernateTestDao.class);
    private SessionFactory sessionFactory;

    public static HibernateTestDao instance() {
        return new HibernateTestDao();
    }

    private HibernateTestDao() {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public void queryHqlWithBindParameters(String name, int repeatCount, Map parameters) {
        executeHqlMultipleTimes(name, parameters, repeatCount, true);
    }

    public void queryHqlWithoutBindParameters(String name, int repeatCount, Map parameters) {
        executeHqlMultipleTimes(name, parameters, repeatCount, false);
    }

    public void querySqlWithBindParameters(String name, int repeatCount, Map parameters) {
        executeSqlMultipleTimes(name, parameters, repeatCount, true);
    }

    public void querySqlWithoutBindParameters(String name, int repeatCount, Map parameters) {
        executeSqlMultipleTimes(name, parameters, repeatCount, false);
    }

    private void executeHqlMultipleTimes(String name, Map map, int count, boolean bindParameters) {
        int threshold = (int) Math.round(count * 0.10);
        long totalStart = System.currentTimeMillis();
        for (int cnt = 0; cnt < count; cnt++) {
            Session s = sessionFactory.openSession();
            Transaction t = s.beginTransaction();
            Query query;
            if (bindParameters) {
                query = bindParametersToHql(s, map);
            } else {
                query = embedParametersInHql(s, map);
            }
            try {
                long start = 0;
                if (cnt > threshold) {
                    start = System.currentTimeMillis();
                }
                List list = query.list();
                Iterator i = list.iterator();
                while (i.hasNext())
                    log.debug("[" + name + "] [" + cnt + "]: object: " + i.next().toString());
                if (cnt > threshold) {
                    long duration = System.currentTimeMillis() - start;
                    log.info("[" + name + "] [" + cnt + "]: duration (mS): " + duration);
                } else {
                    log.info("[" + name + "] [" + cnt + "]: below threshold, not timing.");
                }
            } catch (Throwable e) {
                log.error("Caught exception: " + e.getMessage(), e);
                break;
            } finally {
                t.rollback();
                s.flush();
                s.clear();
            }
        }
        long totalDuration = System.currentTimeMillis() - totalStart;
        log.info("[" + name + "] [total]: " + totalDuration);
    }

    private void executeSqlMultipleTimes(String name, Map parameters, int count, boolean bindParameters) {
        int threshold = (int) Math.round(count * 0.10);
        long totalStart = System.currentTimeMillis();
        try {
            for (int cnt = 0; cnt < count; cnt++) {
                Session s = sessionFactory.openSession();
                Connection c = s.connection();
                c.setReadOnly(true);

                PreparedStatement ps;
                if (bindParameters) {
                    ps = bindParametersToSql(c, parameters);
                } else {
                    ps = embedParametersInSql(c, parameters);
                }

                ResultSet rs = null;
                try {
                    long start = 0;
                    if (cnt > threshold)
                        start = System.currentTimeMillis();
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        HibernateTestClass data = initializeHibernateTestClass(rs);
                        log.debug("[" + name + "] [" + cnt + "]: object: " + data);
                    }
                    if (cnt > threshold) {
                        long duration = System.currentTimeMillis() - start;
                        log.info("[" + name + "] [" + cnt + "]: duration (mS): " + duration);
                    } else {
                        log.info("[" + name + "] [" + cnt + "]: below threshold, not timing.");
                    }
                } catch (Throwable e) {
                    log.error("Caught exception: " + e.getMessage(), e);
                    break;
                } finally {
                    if (null != rs)
                        rs.close();
                    if (null != ps)
                        ps.close();
                    s.flush();
                    s.clear();
                }
            }
        } catch (Throwable e) {
            log.error("Caught exception: " + e.getMessage(), e);
        }
        long totalDuration = System.currentTimeMillis() - totalStart;
        log.info("[" + name + "] [total]: " + totalDuration);
    }

    private PreparedStatement embedParametersInSql(Connection connection, Map parameters) throws Exception {
        String sql = "select * from scratch..hibernate_test_table as htc where";
        Iterator ii = parameters.keySet().iterator();
        boolean first = true;
        while (ii.hasNext()) {
            sql += (first ? " " : " and ");
            String key = (String) ii.next();
            if (parameters.get(key) instanceof String) {
                // if a string check to see if it's a 'like' query
                String val = (String) parameters.get(key);
                if (val.indexOf('%') < 0)
                    sql += "htc." + key + " = '" + val + "'";
                else
                    sql += "htc." + key + " like '" + val + "'";
            } else if (parameters.get(key) instanceof java.util.Date) {
                Date val = (Date) parameters.get(key);
                sql += "htc." + key + " = '" + toString(val) + "'";
            } else if (parameters.get(key) instanceof Number) {
                BigDecimal val = (BigDecimal) parameters.get(key);
                sql += "htc." + key + " = " + val;
            } else {
                throw new IllegalArgumentException("Got unrecognized key/value: [" + key + "/" + parameters.get(key) + "]");
            }
            if (first)
                first = false;
        }
        log.debug("[embedParametersInSql] [SQL]: [" + sql + "]");
        return connection.prepareStatement(sql);
    }

    private PreparedStatement bindParametersToSql(Connection connection, Map parameters) throws Exception {
        String sql = "select * from scratch..hibernate_test_table as htc where";
        Iterator ii = new java.util.TreeSet(parameters.keySet()).iterator();
        boolean first = true;
        while (ii.hasNext()) {
            String key = (String) ii.next();
            sql += (first ? " " : " and ");
            if (parameters.get(key) instanceof String) {
                String val = (String) parameters.get(key);
                if (val.indexOf('%') >= 0)
                    sql += "htc." + key + " like ?";
                else
                    sql += "htc." + key + " = ?";
            } else {
                sql += "htc." + key + " = ?";
            }
            if (first)
                first = false;
        }
        PreparedStatement ps = connection.prepareStatement(sql);

        int pos = 0;
        ii = new java.util.TreeSet(parameters.keySet()).iterator();
        while (ii.hasNext()) {
            pos++;
            String key = (String) ii.next();
            if (parameters.get(key) instanceof String) {
                String val = (String) parameters.get(key);
                ps.setString(pos, val);
            } else if (parameters.get(key) instanceof java.util.Date) {
                Date val = (Date) parameters.get(key);
                ps.setTimestamp(pos, new Timestamp(val.getTime()));
            } else if (parameters.get(key) instanceof BigDecimal) {
                BigDecimal val = (BigDecimal) parameters.get(key);
                ps.setBigDecimal(pos, val);
            } else if (parameters.get(key) instanceof Integer) {
                Integer val = (Integer) parameters.get(key);
                ps.setInt(pos, val.intValue());
            } else {
                throw new IllegalArgumentException("Got unrecognized key/value: [" + key + "/" + parameters.get(key) + "]");
            }
        }
        log.debug("[bindParametersToSql] [SQL]: [" + sql + "]");
        log.debug("[bindParametersToSql] [PARAMS]: [" + toString(parameters) + "]");
        return ps;
    }

    private Query embedParametersInHql(Session session, Map map) {
        String hql = "from HibernateTestClass as hibernateTestClass where ";
        Iterator ii = map.keySet().iterator();
        boolean first = true;
        while (ii.hasNext()) {
            hql += (first ? " " : " and ");
            Object key = ii.next();
            if (map.get(key) instanceof String) {
                // check if this is a 'like' query
                String val = (String) map.get(key);
                if (val.indexOf('%') < 0)
                    hql += "hibernateTestClass." + key + " = '" + val + "' ";
                else
                    hql += "hibernateTestClass." + key + " like '" + val + "' ";
            } else if (map.get(key) instanceof java.util.Date) {
                hql += "hibernateTestClass." + key + " = '" + toString((Date) map.get(key)) + "' ";
            } else {
                hql += "hibernateTestClass." + key + " = " + map.get(key) + " ";
            }
            if (first)
                first = false;
        }
        log.debug("[embedParametersInHql] [HQL]: [" + hql + "]");
        return session.createQuery(hql).setReadOnly(true);
    }

    private Query bindParametersToHql(Session session, Map parameters) {
        String hql = "from HibernateTestClass as hibernateTestClass where ";
        Iterator ii = parameters.keySet().iterator();
        boolean first = true;
        while (ii.hasNext()) {
            String key = (String) ii.next();
            hql += (first ? " " : " and ");
            if (parameters.get(key) instanceof String) {
                String val = (String) parameters.get(key);
                if (val.indexOf('%') >= 0)
                    hql += "hibernateTestClass." + key + " like :" + key;
                else
                    hql += "hibernateTestClass." + key + " = :" + key + " ";
            } else {
                hql += "hibernateTestClass." + key + " = :" + key + " ";
            }
            if (first)
                first = false;
        }
        Query q = session.createQuery(hql).setReadOnly(true);
        ii = parameters.keySet().iterator();
        while (ii.hasNext()) {
            String key = (String) ii.next();
            q.setParameter(key, parameters.get(key));
        }
        log.debug("[bindParametersToHql] [HQL]: [" + hql + "]");
        log.debug("[bindParametersToHql] [PARAMS]: [" + toString(parameters) + "]");
        return q;
    }

    private HibernateTestClass initializeHibernateTestClass(ResultSet rs) throws Exception {
        HibernateTestClass htc = new HibernateTestClass();
        htc.setAmount(rs.getBigDecimal("amount"));
        htc.setBigText(rs.getString("big_text"));
        htc.setId(new Integer(rs.getInt("id")));
        htc.setLastModified(new Date(rs.getTimestamp("last_modified").getTime()));
        htc.setName(rs.getString("name"));
        htc.setSimpleValue(new Integer(rs.getInt("simple_value")));
        return htc;
    }

    private String toString(Map map) {
        StringBuffer sb = new StringBuffer(512);
        Iterator ii = map.keySet().iterator();
        boolean first = true;
        sb.append("{");
        while (ii.hasNext()) {
            String key = (String) ii.next();
            sb.append((first ? "" : ", "));
            sb.append(key);
            sb.append(":");
            sb.append(map.get(key));
            if (first)
                first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String toString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(date);
    }
}
