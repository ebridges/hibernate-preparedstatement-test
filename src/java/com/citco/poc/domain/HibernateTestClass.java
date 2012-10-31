package com.citco.poc.domain;

import java.util.Date;
import java.math.BigDecimal;

/**
 * HibernateTestClass
 *
 * @author EBridges
 * @version $Name$ ($Revision$) [Created: May 30, 2007 11:40:34 AM]
 */
public class HibernateTestClass {
    private Integer id;
    private String name;
    private Date lastModified;
    private Integer simpleValue;
    private String bigText;
    private BigDecimal amount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Integer getSimpleValue() {
        return simpleValue;
    }

    public void setSimpleValue(Integer simpleValue) {
        this.simpleValue = simpleValue;
    }

    public String getBigText() {
        return bigText;
    }

    public void setBigText(String bigText) {
        this.bigText = bigText;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        HibernateTestClass that = (HibernateTestClass) o;

        return name.equals(that.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        String msg = "[HibernateTestClass]: ";
        msg += "[id:"+id+",name:"+name+"]";
        return msg;
    }
}

