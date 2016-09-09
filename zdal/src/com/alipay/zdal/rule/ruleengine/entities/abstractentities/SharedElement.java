/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.entities.abstractentities;

/**
 * ��Ҫ�ṩ��һЩ�����ķ���
 * 
 * ���ڵ����
 * 
 * 
 */
public abstract class SharedElement implements Cloneable, OneToMany {

    private String id;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getId() {
        return id;
    }

    public void init() {
    }

    /**
     * ����û�ͨ��map�ķ�ʽ�����ӽڵ㣬����init�Ĺ����лὫmap��key��Ϊ�ӽڵ��id���ý�����
     * ����û�����list�ķ�ʽ�����ӽڵ㣬��list���±��string���Ϊ�ӽڵ��id.
     */
    public void setId(String id) {
        this.id = id;
    }

    public void put(OneToManyEntry oneToManyEntry) {
        //do nothing 
        throw new IllegalArgumentException("should not be here");
    }
}
