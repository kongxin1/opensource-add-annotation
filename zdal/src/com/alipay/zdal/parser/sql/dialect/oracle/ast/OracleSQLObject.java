/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.parser.sql.dialect.oracle.ast;

import com.alipay.zdal.parser.sql.ast.SQLObject;
import com.alipay.zdal.parser.sql.dialect.oracle.visitor.OracleASTVisitor;

/**
 * 
 * @author ����
 * @version $Id: OracleSQLObject.java, v 0.1 2012-11-17 ����3:41:25 Exp $
 */
public interface OracleSQLObject extends SQLObject {

    void accept0(OracleASTVisitor visitor);
}
