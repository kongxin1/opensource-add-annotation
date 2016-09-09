/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.parser.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.alipay.zdal.parser.exceptions.SqlParserException;
import com.alipay.zdal.parser.sql.ast.SQLExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLBinaryOpExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLBinaryOperator;
import com.alipay.zdal.parser.sql.ast.expr.SQLCharExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLIdentifierExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLInListExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLIntegerExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLMethodInvokeExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLNCharExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLNumberExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLPropertyExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLVariantRefExpr;
import com.alipay.zdal.parser.sql.ast.statement.SQLUpdateSetItem;
import com.alipay.zdal.parser.sql.ast.statement.SQLUpdateStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLInsertStatement.ValuesClause;
import com.alipay.zdal.parser.sql.stat.TableStat;
import com.alipay.zdal.parser.sql.stat.TableStat.Mode;
import com.alipay.zdal.parser.sql.visitor.SQLASTVisitor;
import com.alipay.zdal.parser.sql.visitor.SchemaStatVisitor;

/**
 * ����druid��sqlparser��һ����չ������ʵ��zdal��parserģ���ڽ����Ժ󷵻ؽ�����Ķ�����.
 * @author xiaoqing.zhouxq
 * @version $Id: ZdalSchemaStatVisitor.java, v 0.1 2012-5-25 ����08:58:02 xiaoqing.zhouxq Exp $
 */
public class ZdalSchemaStatVisitor extends SchemaStatVisitor implements SQLASTVisitor {

    public final static String     OFFSET              = "OFFSET";

    public final static String     ROWCOUNT            = "ROWCOUNT";

    /** ��sql�����еİ󶨲����б�����mysql��limit��oracle��rownum���󶨵Ĳ��� */
    private List<BindVarCondition> bindVarConditions   = new ArrayList<BindVarCondition>();

    /** ��sql�еĲ���ֱ����ֵ�Ĳ����б�. */
    private List<BindVarCondition> noBindVarConditions = new ArrayList<BindVarCondition>();

    /** 
     * ��sql����б�����Ӧ�ı�ı���.
     * @see com.alibaba.druid.sql.visitor.SchemaStatVisitor#clearAliasMap()
     */
    public void clearAliasMap() {
    }

    /** 
     * ������druid����update��sqlʱû�з��ز�������.
     * @see com.alipay.zdal.parser.sql.visitor.SchemaStatVisitor#visit(com.alipay.zdal.parser.sql.ast.statement.SQLUpdateStatement)
     */
    public boolean visit(SQLUpdateStatement x) {
        setAliasMap();

        setMode(x, Mode.Update);

        String ident = x.getTableName().toString();
        setCurrentTable(ident);

        TableStat stat = getTableStat(ident);
        stat.incrementUpdateCount();

        Map<String, String> aliasMap = getAliasMap();
        aliasMap.put(ident, ident);

        accept(x.getItems());
        //��update��whereǰ���ȡ�󶨲���.
        acceptSQLUpdateSetItem(x.getItems());
        accept(x.getWhere());

        return false;
    }

    /** 
     * @see com.alipay.zdal.parser.sql.visitor.SchemaStatVisitor#visit(com.alipay.zdal.parser.sql.ast.expr.SQLBinaryOpExpr)
     */
    public boolean visit(SQLBinaryOpExpr x) {
        x.getLeft().setParent(x);
        x.getRight().setParent(x);

        switch (x.getOperator()) {
            case Equality:
            case NotEqual:
            case GreaterThan:
            case GreaterThanOrEqual:
            case LessThan:
            case LessThanOrEqual:
            case LessThanOrEqualOrGreaterThan:
            case LessThanOrGreater:
            case Like:
            case NotLike:
            case Is:
            case IsNot:
                handleCondition(x.getLeft(), x.getOperator().name);
                handleCondition(x.getRight(), x.getOperator().name);
                handleCondition(x);
                handleCondition(x.getLeft(), x.getOperator().name, x.right);

                handleRelationship(x.getLeft(), x.getOperator().name, x.getRight());
                break;
            default:
                break;
        }

        return true;
    }

    /** 
     * @see com.alipay.zdal.parser.sql.visitor.SchemaStatVisitor#visit(com.alipay.zdal.parser.sql.ast.expr.SQLInListExpr)
     */
    public boolean visit(SQLInListExpr x) {
        handCondition(x);
        return true;
    }

    /**
     * ��ȡin�еİ󶨲���.
     * @param x
     */
    private void handCondition(SQLInListExpr x) {
        //        SQLIdentifierExpr left = (SQLIdentifierExpr) x.getExpr();
        String columnName = null;
        if (x.getExpr() instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr left = (SQLIdentifierExpr) x.getExpr();
            columnName = left.getName();
        }
        if (x.getExpr() instanceof SQLPropertyExpr) {
            SQLPropertyExpr left = (SQLPropertyExpr) x.getExpr();
            columnName = left.getName();
        }
        boolean not = x.isNot();
        String operator = (not == true) ? " not in " : " in ";
        List<SQLExpr> targetList = x.getTargetList();
        for (SQLExpr sqlExpr : targetList) {
            if (sqlExpr instanceof SQLVariantRefExpr) {
                SQLVariantRefExpr bind = (SQLVariantRefExpr) sqlExpr;
                int index = bind.getIndex();
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(columnName);
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(operator);
                bindVarCondition.setIndex(index);
                bindVarConditions.add(bindVarCondition);
            } else if (sqlExpr instanceof SQLIntegerExpr) {
                SQLIntegerExpr right = (SQLIntegerExpr) sqlExpr;
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(columnName);
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(operator);
                bindVarCondition.setValue((Comparable<?>) right.getNumber());
                noBindVarConditions.add(bindVarCondition);
            } else if (sqlExpr instanceof SQLNumberExpr) {
                SQLNumberExpr right = (SQLNumberExpr) sqlExpr;
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(columnName);
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(operator);
                bindVarCondition.setValue((Comparable<?>) right.getNumber());
                noBindVarConditions.add(bindVarCondition);
            } else if (sqlExpr instanceof SQLCharExpr) {
                SQLCharExpr right = (SQLCharExpr) sqlExpr;
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(columnName);
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(operator);
                bindVarCondition.setValue(right.getText());
                noBindVarConditions.add(bindVarCondition);
            } else if (sqlExpr instanceof SQLNCharExpr) {
                SQLNCharExpr right = (SQLNCharExpr) sqlExpr;
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(columnName);
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(operator);
                bindVarCondition.setValue(right.getText());
                noBindVarConditions.add(bindVarCondition);
            }
        }
        x.getAttributes();
    }

    private BindVarCondition getExistCondition(String columnName) {
        for (BindVarCondition condition : bindVarConditions) {
            if (condition.getColumnName().equals(columnName)) {
                return condition;
            }
        }
        return null;
    }

    public void handleCondition(SQLBinaryOpExpr x) {
        //��ȡwhere��������İ󶨲���.
        if ((x.getRight() instanceof SQLVariantRefExpr)
            && ((x.getLeft() instanceof SQLIdentifierExpr) || (x.getLeft() instanceof SQLPropertyExpr))) {
            SQLVariantRefExpr right = (SQLVariantRefExpr) x.getRight();
            BindVarCondition bindVarCondition = new BindVarCondition();
            String columnName = null;
            if (x.getLeft() instanceof SQLIdentifierExpr) {
                columnName = ((SQLIdentifierExpr) x.getLeft()).getName();
            } else if (x.getLeft() instanceof SQLPropertyExpr) {
                columnName = ((SQLPropertyExpr) x.getLeft()).getName();
            }
            int index = columnName.indexOf('.');
            if (index > -1) {
                columnName = columnName.substring(index + 1);
            }
            bindVarCondition.setColumnName(columnName);
            bindVarCondition.setTableName(getCurrentTable());
            bindVarCondition.setOperator(x.getOperator().name);
            bindVarCondition.setIndex(right.getIndex());
            bindVarCondition.setParent(x.getParent());
            if (bindVarConditions.isEmpty()) {
                bindVarConditions.add(bindVarCondition);
            } else {
                BindVarCondition last = getExistCondition(bindVarCondition.getColumnName());
                if (last != null && last.getParent() instanceof SQLBinaryOpExpr) {
                    if (last.getParent().equals(bindVarCondition.getParent())) {
                        SQLBinaryOpExpr opExpr = (SQLBinaryOpExpr) last.getParent();
                        if (opExpr.getOperator() == SQLBinaryOperator.BooleanOr) {
                            bindVarCondition.setOp(-1);
                        } else if (opExpr.getOperator() == SQLBinaryOperator.BooleanAnd) {
                            bindVarCondition.setOp(1);
                        }
                    } else {
                        if (last.getParent().toString().contains(x.getParent().toString())) {
                            bindVarCondition.setOp(1);
                        } else if (x.getParent().toString().contains(last.getParent().toString())) {
                            bindVarCondition.setOp(1);
                        } else {
                            bindVarCondition.setOp(-1);
                        }
                    }
                }
                bindVarConditions.add(bindVarCondition);
            }

        } else if (x.getRight() instanceof SQLIntegerExpr) {
            if (x.getLeft() instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr left = (SQLIdentifierExpr) x.getLeft();
                SQLIntegerExpr right = (SQLIntegerExpr) x.getRight();
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(left.getName());
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(x.getOperator().name);
                bindVarCondition.setValue((Comparable<?>) right.getNumber());
                noBindVarConditions.add(bindVarCondition);
            }
            if (x.getLeft() instanceof SQLPropertyExpr) {
                SQLPropertyExpr left = (SQLPropertyExpr) x.getLeft();
                SQLIntegerExpr right = (SQLIntegerExpr) x.getRight();
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(left.getName());
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(x.getOperator().name);
                bindVarCondition.setValue((Comparable<?>) right.getNumber());
                noBindVarConditions.add(bindVarCondition);
            }
        } else if (x.getRight() instanceof SQLNumberExpr) {
            if (x.getLeft() instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr left = (SQLIdentifierExpr) x.getLeft();
                SQLNumberExpr right = (SQLNumberExpr) x.getRight();
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(left.getName());
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(x.getOperator().name);
                bindVarCondition.setValue((Comparable<?>) right.getNumber());
                noBindVarConditions.add(bindVarCondition);
            }
            if (x.getLeft() instanceof SQLPropertyExpr) {
                SQLPropertyExpr left = (SQLPropertyExpr) x.getLeft();
                SQLNumberExpr right = (SQLNumberExpr) x.getRight();
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(left.getName());
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(x.getOperator().name);
                bindVarCondition.setValue((Comparable<?>) right.getNumber());
                noBindVarConditions.add(bindVarCondition);
            }
        } else if (x.getRight() instanceof SQLCharExpr) {
            if (x.getLeft() instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr left = (SQLIdentifierExpr) x.getLeft();
                SQLCharExpr right = (SQLCharExpr) x.getRight();
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(left.getName());
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(x.getOperator().name);
                bindVarCondition.setValue(right.getText());
                noBindVarConditions.add(bindVarCondition);
            }
            if (x.getLeft() instanceof SQLPropertyExpr) {
                SQLPropertyExpr left = (SQLPropertyExpr) x.getLeft();
                SQLCharExpr right = (SQLCharExpr) x.getRight();
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(left.getName());
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(x.getOperator().name);
                bindVarCondition.setValue(right.getText());
                noBindVarConditions.add(bindVarCondition);
            }
        } else if (x.getRight() instanceof SQLNCharExpr) {
            SQLIdentifierExpr left = (SQLIdentifierExpr) x.getLeft();
            SQLNCharExpr right = (SQLNCharExpr) x.getRight();
            BindVarCondition bindVarCondition = new BindVarCondition();
            bindVarCondition.setColumnName(left.getName());
            bindVarCondition.setTableName(getCurrentTable());
            bindVarCondition.setOperator(x.getOperator().name);
            bindVarCondition.setValue(right.getText());
            noBindVarConditions.add(bindVarCondition);
        } else if (x.getRight() instanceof SQLMethodInvokeExpr) {//value = null
            //            SQLIdentifierExpr left = (SQLIdentifierExpr) x.getLeft();
            //            BindVarCondition bindVarCondition = new BindVarCondition();
            //            bindVarCondition.setColumnName(left.getName());
            //            bindVarCondition.setTableName(getCurrentTable());
            //            bindVarCondition.setOperator(x.getOperator().name);
            //            noBindVarConditions.add(bindVarCondition);
        }
    }

    /**
     * ��ȡupdate whereǰ��İ󶨲���.
     * @param items
     */
    protected void acceptSQLUpdateSetItem(List<SQLUpdateSetItem> items) {
        for (SQLUpdateSetItem item : items) {
            if (item.getValue() instanceof SQLVariantRefExpr) {
                String columnName = item.getColumn().toString();
                int index = columnName.indexOf('.');
                if (index > -1) {
                    columnName = columnName.substring(index + 1);
                }
                SQLVariantRefExpr expr = (SQLVariantRefExpr) (item.getValue());
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(columnName);
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setIndex(expr.getIndex());
                bindVarConditions.add(bindVarCondition);
            }
            if (item.getValue() instanceof SQLBinaryOpExpr) {
                String columnName = item.getColumn().toString();
                int index = columnName.indexOf('.');
                if (index > -1) {
                    columnName = columnName.substring(index + 1);
                }
                acceptSQLBinaryOpExpr(columnName, (SQLBinaryOpExpr) item.getValue());
                //                SQLBinaryOpExpr expr = (SQLBinaryOpExpr) (item.getValue());
                //                SQLVariantRefExpr expr1 = (SQLVariantRefExpr) expr.getRight();
                //
                //                BindVarCondition bindVarCondition = new BindVarCondition();
                //                bindVarCondition.setColumnName(columnName);
                //                bindVarCondition.setTableName(getCurrentTable());
                //                bindVarCondition.setIndex(expr1.getIndex());
                //                bindVarConditions.add(bindVarCondition);
            }
        }
    }

    private void acceptSQLBinaryOpExpr(String columnName, SQLBinaryOpExpr binaryOpExpr) {
        if (binaryOpExpr.getLeft() instanceof SQLVariantRefExpr) {
            SQLVariantRefExpr left = (SQLVariantRefExpr) binaryOpExpr.getLeft();
            BindVarCondition bindVarCondition = new BindVarCondition();
            bindVarCondition.setColumnName(columnName);
            bindVarCondition.setTableName(getCurrentTable());
            bindVarCondition.setIndex(left.getIndex());
            bindVarConditions.add(bindVarCondition);
        } else if (binaryOpExpr.getLeft() instanceof SQLBinaryOpExpr) {
            acceptSQLBinaryOpExpr(columnName, (SQLBinaryOpExpr) binaryOpExpr.getLeft());
        }
        if (binaryOpExpr.getRight() instanceof SQLVariantRefExpr) {
            SQLVariantRefExpr right = (SQLVariantRefExpr) binaryOpExpr.getRight();
            BindVarCondition bindVarCondition = new BindVarCondition();
            bindVarCondition.setColumnName(columnName);
            bindVarCondition.setTableName(getCurrentTable());
            bindVarCondition.setIndex(right.getIndex());
            bindVarConditions.add(bindVarCondition);
        } else if (binaryOpExpr.getRight() instanceof SQLBinaryOpExpr) {
            acceptSQLBinaryOpExpr(columnName, (SQLBinaryOpExpr) binaryOpExpr.getRight());
        }
    }

    /**
     * ��ȡinsert�İ󶨲���.
     * @param columns ��������.
     * @param valuesClauses ����ֵ.
     */
    protected void acceptInsertValueClauses(List<SQLExpr> columns, List<ValuesClause> valuesClauses) {
        if (columns == null || valuesClauses == null) {
            throw new IllegalArgumentException(
                "ERROR ## the insert sql's columns or valurClauses is null");
        }
        if (valuesClauses == null || valuesClauses.isEmpty()) {
            return;
        }
        List<SQLExpr> values = valuesClauses.get(0).getValues();
        if (columns.size() != values.size()) {
            throw new SqlParserException(
                "ERROR ## the insert sql's column size is not equal bind value size");
        }
        int index = 0;
        for (SQLExpr value : values) {
            if (value instanceof SQLVariantRefExpr) {
                SQLIdentifierExpr columnExpr = (SQLIdentifierExpr) (columns.get(index));
                SQLVariantRefExpr varExpr = (SQLVariantRefExpr) value;
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(columnExpr.getName());
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(SQLBinaryOperator.Equality.name);//��insert��Ĭ����=
                bindVarCondition.setIndex(varExpr.getIndex());
                bindVarConditions.add(bindVarCondition);
            } else if (value instanceof SQLIntegerExpr) {
                SQLIdentifierExpr columnExpr = (SQLIdentifierExpr) (columns.get(index));
                SQLIntegerExpr right = (SQLIntegerExpr) value;
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(columnExpr.getName());
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(SQLBinaryOperator.Equality.name);//��insert��Ĭ����=
                bindVarCondition.setValue((Comparable<?>) right.getNumber());

                noBindVarConditions.add(bindVarCondition);
            } else if (value instanceof SQLNumberExpr) {
                SQLIdentifierExpr columnExpr = (SQLIdentifierExpr) (columns.get(index));
                SQLNumberExpr right = (SQLNumberExpr) value;
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(columnExpr.getName());
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(SQLBinaryOperator.Equality.name);//��insert��Ĭ����=
                bindVarCondition.setValue((Comparable<?>) right.getNumber());
                noBindVarConditions.add(bindVarCondition);
            } else if (value instanceof SQLCharExpr) {
                SQLIdentifierExpr columnExpr = (SQLIdentifierExpr) (columns.get(index));
                SQLCharExpr right = (SQLCharExpr) value;
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(columnExpr.getName());
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(SQLBinaryOperator.Equality.name);//��insert��Ĭ����=
                bindVarCondition.setValue(right.getText());
                noBindVarConditions.add(bindVarCondition);
            } else if (value instanceof SQLNCharExpr) {
                SQLIdentifierExpr columnExpr = (SQLIdentifierExpr) (columns.get(index));
                SQLNCharExpr right = (SQLNCharExpr) value;
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(columnExpr.getName());
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(SQLBinaryOperator.Equality.name);//��insert��Ĭ����=
                bindVarCondition.setValue(right.getText());
                noBindVarConditions.add(bindVarCondition);
            } else if (value instanceof SQLMethodInvokeExpr) {//value = null
                SQLIdentifierExpr columnExpr = (SQLIdentifierExpr) (columns.get(index));
                BindVarCondition bindVarCondition = new BindVarCondition();
                bindVarCondition.setColumnName(columnExpr.getName());
                bindVarCondition.setTableName(getCurrentTable());
                bindVarCondition.setOperator(SQLBinaryOperator.Equality.name);//��insert��Ĭ����=
                noBindVarConditions.add(bindVarCondition);
            }
            index++;
        }
    }

    /**
     * ����super.getMode()��protected�ģ�������Ҫת��һ��.
     * @return
     */
    public Mode getSqlMode() {
        return getMode();
    }

    /**
     * ����BindVarCondition�е�index��������.
     * @return
     */
    public List<BindVarCondition> getBindVarConditions() {
        Collections.sort(bindVarConditions, new BindVarConditionComparable());
        return bindVarConditions;
    }

    public List<BindVarCondition> getNoBindVarConditions() {
        return noBindVarConditions;
    }

}
