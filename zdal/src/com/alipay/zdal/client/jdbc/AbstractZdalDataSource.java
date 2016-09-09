/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.alipay.zdal.client.config.DataSourceParameter;
import com.alipay.zdal.client.config.ZdalConfig;
import com.alipay.zdal.client.config.ZdalConfigListener;
import com.alipay.zdal.client.config.ZdalDataSourceConfig;
import com.alipay.zdal.client.config.controller.ZdalSignalResource;
import com.alipay.zdal.client.controller.SpringBasedDispatcherImpl;
import com.alipay.zdal.client.datasource.keyweight.ZdalDataSourceKeyWeightRandom;
import com.alipay.zdal.client.datasource.keyweight.ZdalDataSourceKeyWeightRumtime;
import com.alipay.zdal.client.dispatcher.SqlDispatcher;
import com.alipay.zdal.client.exceptions.ZdalClientException;
import com.alipay.zdal.client.jdbc.dbselector.EquityDbManager;
import com.alipay.zdal.client.jdbc.dbselector.OneDBSelector;
import com.alipay.zdal.client.jdbc.dbselector.PriorityDbGroupSelector;
import com.alipay.zdal.common.Closable;
import com.alipay.zdal.common.Constants;
import com.alipay.zdal.common.DBType;
import com.alipay.zdal.common.RuntimeConfigHolder;
import com.alipay.zdal.common.jdbc.sorter.DB2ExceptionSorter;
import com.alipay.zdal.common.jdbc.sorter.MySQLExceptionSorter;
import com.alipay.zdal.common.jdbc.sorter.OracleExceptionSorter;
import com.alipay.zdal.common.lang.StringUtil;
import com.alipay.zdal.common.util.TableSuffixTypeEnum;
import com.alipay.zdal.datasource.LocalTxDataSourceDO;
import com.alipay.zdal.datasource.ZDataSource;
import com.alipay.zdal.parser.DefaultSQLParser;
import com.alipay.zdal.parser.SQLParser;
import com.alipay.zdal.rule.bean.GroovyTableDatabaseMapProvider;
import com.alipay.zdal.rule.bean.LogicTable;
import com.alipay.zdal.rule.bean.SimpleLogicTable;
import com.alipay.zdal.rule.bean.SimpleTableDatabaseMapProvider;
import com.alipay.zdal.rule.bean.SimpleTableMapProvider;
import com.alipay.zdal.rule.bean.SimpleTableTwoColumnsMapProvider;
import com.alipay.zdal.rule.bean.ZdalRoot;
import com.alipay.zdal.rule.config.beans.AppRule;
import com.alipay.zdal.rule.config.beans.ShardRule;
import com.alipay.zdal.rule.config.beans.Suffix;
import com.alipay.zdal.rule.config.beans.SuffixManager;
import com.alipay.zdal.rule.config.beans.TableRule;
import com.alipay.zdal.rule.config.beans.TableRule.ParseException;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.ListSharedElement.DEFAULT_LIST_RESULT_STRAGETY;

/**
 * 
 * @author ����
 * @version $Id: AbstractZdalDataSource.java, v 0.1 2013-1-30 ����09:56:01 Exp $
 */
public abstract class AbstractZdalDataSource extends ZdalDataSourceConfig
		implements DataSource, Closable, ZdalConfigListener {

	private RuntimeConfigHolder<ZdalRuntime> runtimeConfigHolder = new RuntimeConfigHolder<ZdalRuntime>();
	private SqlDispatcher writeDispatcher;
	private SqlDispatcher readDispatcher;
	private Map<String, ? extends Object> dataSourcePoolConfig;
	/** rwRule */
	private Map<String, ? extends Object> rwDataSourcePoolConfig;
	private Map<String, ZdalDataSourceKeyWeightRandom> keyWeightMapConfig;
	private Map<String, String> keyWeightConfig;
	private int retryingTimes = 4;

	private AppRule appRule;

	/**
	 * ����Դ���ϣ�key������Դ��ʶ��value ��DataSource����Ŀǰ���õ��� ZdataSource
	 */
	protected Map<String, ZDataSource> dataSourcesMap = new HashMap<String, ZDataSource>();

	private ZdalSignalResource zdalSignalResource = null;

	/**
	 * Ȩ������rwp�ֱ�����Ȩ�ء�дȨ�ء�������3������ִ�С��˳�򣬺�����Ը�һ�����֡�
	 * ����ĸ�����֣���Ӧ���ֵĬ��Ϊ0������ĸ�������ֲ����֣���Ӧ���Ĭ��ֵ��return˵��
	 * 
	 * @param weight
	 *            ��ʽ
	 * @return int[0] R���������(Ĭ��10), int[1] W���������(Ĭ��10), int[2] P���������(Ĭ��0);
	 *         R20W10 --> int[]{20,10,0} rp2w30 --> int[]{10,30,2}
	 */
	private static final Pattern weightPattern_r = Pattern
			.compile("[Rr](\\d*)");
	private static final Pattern weightPattern_w = Pattern
			.compile("[Ww](\\d*)");
	private static final Pattern weightPattern_p = Pattern
			.compile("[Pp](\\d*)");
	private static final Pattern weightPattern_q = Pattern
			.compile("[Qq](\\d*)");

	private static enum WeightRWPQEnum {
		rWeight(0), wWeight(1), readPriority(2), writePriority(3);
		private Integer value;

		public Integer value() {
			return value;
		}

		WeightRWPQEnum(Integer value) {
			this.value = value;
		}
	}

	/**
	 * ��������Դ.
	 */
	/**
	 * @see com.alipay.zdal.common.Closable#close()
	 */
	public final void close() throws Throwable {
		if (!this.dataSourcesMap.isEmpty()) {
			for (DataSource dataSource : this.dataSourcesMap.values()) {
				try {
					((ZDataSource) dataSource).destroy();
				} catch (Throwable e) {
					CONFIG_LOGGER
							.error("##Error, ZdalDataSource tried to close datasource occured unexpected exception.",
									e);
				}
			}
			this.dataSourcesMap.clear();
		}

		if (zdalSignalResource != null) {
			zdalSignalResource.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alipay.zdal.client.config.ZdalConfigListener#resetWeight(java.util
	 * .Map)
	 */
	public void resetWeight(Map<String, String> keyWeights) {
		this.resetZdalDataSource(keyWeights);
	}

	/**
	 * @see com.alipay.zdal.client.config.ZdalDataSourceConfig#initDataSources(com.alipay.zdal.client.config.ZdalConfig)
	 */
	protected final void initDataSources(ZdalConfig zdalConfig) {
		if (zdalConfig.getDataSourceParameters() == null
				|| zdalConfig.getDataSourceParameters().isEmpty()) {
			throw new ZdalClientException(
					"ERROR ## the datasource parameter is empty");
		}

		for (Entry<String, DataSourceParameter> entry : zdalConfig
				.getDataSourceParameters().entrySet()) {
			try {
				ZDataSource zDataSource = new ZDataSource(createDataSourceDO(
						entry.getValue(), zdalConfig.getDbType(), appDsName
								+ "." + entry.getKey()));
				this.dataSourcesMap.put(entry.getKey(), zDataSource);
			} catch (Exception e) {
				throw new ZdalClientException("ERROR ## create dsName = "
						+ entry.getKey() + " dataSource failured", e);
			}
		}
		if (dbConfigType.isShard()) {
			this.dataSourcePoolConfig = getFailoverDataSourcePoolConfig(zdalConfig
					.getLogicPhysicsDsNames());// �����߼�������Ķ�Ӧ��ϵ.
			this.appRule = zdalConfig.getAppRootRule();
			this.appRule.init();
			initForAppRule(appRule);
		} else if (dbConfigType.isShardFailover()) {
			this.dataSourcePoolConfig = getFailoverDataSourcePoolConfig(zdalConfig
					.getLogicPhysicsDsNames());// �����߼�������Ķ�Ӧ��ϵ.
			this.keyWeightConfig = zdalConfig.getFailoverRules();
			this.appRule = zdalConfig.getAppRootRule();
			this.appRule.init();
			initForAppRule(appRule);
			CONFIG_LOGGER.warn("WARN ## the shardFailoverWeight of "
					+ appDsName + " is :" + getReceivDataStr(keyWeightConfig));
		} else if (dbConfigType.isShardGroup()) {
			this.rwDataSourcePoolConfig = zdalConfig.getGroupRules();
			this.appRule = zdalConfig.getAppRootRule();
			this.appRule.init();
			initForAppRule(appRule);
			CONFIG_LOGGER.warn("WARN ## the shardGroupWeight of " + appDsName
					+ " is :" + getReceivDataStr(zdalConfig.getGroupRules()));
		} else if (dbConfigType.isGroup()) {
			this.rwDataSourcePoolConfig = zdalConfig.getGroupRules();
			this.initForLoadBalance(zdalConfig.getDbType());
			CONFIG_LOGGER.warn("WARN ## the GroupWeight of " + appDsName
					+ " is :" + getReceivDataStr(zdalConfig.getGroupRules()));
		}

		this.initConfigListener();
	}

	/**
	 * ����failover��̬�л��Ĺ���,��Ҫ�ڷֲ�ʽ�����¿��Զ�̬��������zookeeper.
	 */
	private void initConfigListener() {
		zdalSignalResource = new ZdalSignalResource(this);
	}

	private void initForLoadBalance(DBType dbType) {
		Map<String, DBSelector> dsSelectors = this
				.buildRwDbSelectors(this.rwDataSourcePoolConfig);
		this.runtimeConfigHolder.set(new ZdalRuntime(dsSelectors));
		this.setDbTypeForDBSelector(dbType);
	}

	/**
	 * ����failover���߼�����Դ����������Դ�Ķ�Ӧ��ϵ.
	 * 
	 * @param logicPhysicsDsNames
	 * @return
	 */
	private Map<String, DataSource> getFailoverDataSourcePoolConfig(
			Map<String, String> logicPhysicsDsNames) {
		Map<String, DataSource> logicDataSourcesMap = new HashMap<String, DataSource>();
		for (Map.Entry<String, String> mEntry : logicPhysicsDsNames.entrySet()) {
			String key = mEntry.getKey().trim();
			String value = mEntry.getValue().trim();
			logicDataSourcesMap.put(key, dataSourcesMap.get(value));
		}
		return logicDataSourcesMap;
	}

	protected void initForAppRule(AppRule appRule) {
		Map<String, DBSelector> dsSelectors = this.rwDataSourcePoolConfig == null ? buildDbSelectors(this.dataSourcePoolConfig)
				: this.buildRwDbSelectors(this.rwDataSourcePoolConfig);
		this.runtimeConfigHolder.set(new ZdalRuntime(dsSelectors));

		// ��Ӱ�����Դkey�����Ȩ����������
		if (keyWeightConfig != null && !keyWeightConfig.isEmpty()) {
			// ������������������Դ��Ȩ����Ϣ
			Map<String, ? extends Object> dataSourceKeyConfig = this.rwDataSourcePoolConfig == null ? this.dataSourcePoolConfig
					: this.rwDataSourcePoolConfig;
			keyWeightMapConfig = ZdalDataSourceKeyWeightRumtime
					.buildKeyWeightConfig(keyWeightConfig, dataSourceKeyConfig);
			if (keyWeightMapConfig == null) {
				throw new IllegalStateException("����Դkey������Ȩ�����ô���,zdal��ʼ��ʧ�ܣ�");
			}
		}
		this.initForDispatcher(appRule);
	}

	private void initForDispatcher(AppRule appRule) {
		SQLParser parser = new DefaultSQLParser();
		this.writeDispatcher = buildSqlDispatcher(appRule.getMasterRule(),
				parser);
		this.readDispatcher = buildSqlDispatcher(appRule.getSlaveRule(), parser);
	}

	private SqlDispatcher buildSqlDispatcher(ShardRule shardRule,
			SQLParser parser) {
		if (shardRule == null)
			return null;

		ZdalRoot zdalRoot = new ZdalRoot();
		zdalRoot.setDBType(this.dbType);
		Map<String/* key */, LogicTable> logicTableMap = new HashMap<String, LogicTable>();
		if (shardRule.getTableRules() != null) {
			for (Map.Entry<String/* �߼����� */, TableRule> e : shardRule
					.getTableRules().entrySet()) {
				setDbTypeForDbIndex(this.dbType, e.getValue().getDbIndexArray());
				LogicTable logicTable = toLogicTable(e.getValue());
				logicTable.setLogicTableName(e.getKey());
				logicTable.setDBType(this.dbType);
				// logicTable.init(); //ZdalRoot.init()������logicTable.init()
				logicTableMap.put(e.getKey(), logicTable);
			}
		}
		zdalRoot.setLogicTableMap(logicTableMap);
		if (shardRule.getDefaultDbIndex() != null) {
			zdalRoot.setDefaultDBSelectorID(shardRule.getDefaultDbIndex());
		}
		zdalRoot.init(appDsName);
		return buildSqlDispatcher(parser, zdalRoot);
	}

	private void setDbTypeForDbIndex(DBType dbType, String[] dbIndexes) {
		Map<String, DBSelector> dbSelectors = this.runtimeConfigHolder.get().dbSelectors;

		for (String dbIndex : dbIndexes) {
			DBSelector dbs = dbSelectors.get(dbIndex);
			if (dbs == null) {
				throw new IllegalArgumentException("�������ô���[" + dbIndex
						+ "]��dataSourcePool��û������");
			}
			dbs.setDbType(dbType);
			// bug fixed by fanzeng. ��ΪzdalĬ�ϵ�dbType ��mysql������
			// �����ȼ�����ѡ��db��ʱ���������db�����쳣��
			// priorityDbGroupSelector�������ڲ���װ�ĶԵȿ�� dbtypeȥѡ��
			// excetptionSorter,���db������oracle�ģ�
			// bug fixed ֮ǰ����δ��ʼ��EquityDbManager��dbtype�����»���Ĭ�ϵ� mysql����ȥѡ��
			if (dbs instanceof PriorityDbGroupSelector) {
				EquityDbManager[] equityDbmanager = ((PriorityDbGroupSelector) dbs)
						.getPriorityGroups();
				if (equityDbmanager == null) {
					throw new IllegalArgumentException("���ȼ��ĶԵȿⲢδ��ʼ�����������ã�");
				}
				for (int i = 0; i < equityDbmanager.length; i++) {
					equityDbmanager[i].setDbType(dbType);
				}
			}

		}
	}

	/**
	 * Ϊload balance ����dbType
	 * 
	 * @param dbType
	 */
	private void setDbTypeForDBSelector(DBType dbType) {
		Map<String, DBSelector> dbSelectors = this.runtimeConfigHolder.get().dbSelectors;
		int i = 0;
		String[] dbIndexes = new String[dbSelectors.size()];
		for (Map.Entry<String, DBSelector> dbselector : dbSelectors.entrySet()) {
			dbIndexes[i++] = dbselector.getKey().trim();
		}
		setDbTypeForDbIndex(dbType, dbIndexes);
	}

	/**
	 * 
	 * @return
	 */
	private SimpleTableMapProvider getTableMapProvider(TableRule tableRule) {
		SimpleTableMapProvider simpleTableMapProvider = null;
		SuffixManager suffixManager = tableRule.getSuffixManager();
		Suffix suf = suffixManager.getSuffix(0);
		if (suf.getTbType().equals(
				TableSuffixTypeEnum.twoColumnForEachDB.getValue())) {
			simpleTableMapProvider = new SimpleTableTwoColumnsMapProvider();
			SimpleTableTwoColumnsMapProvider twoColumns = (SimpleTableTwoColumnsMapProvider) simpleTableMapProvider;
			Suffix suf2 = suffixManager.getSuffix(1);
			twoColumns.setFrom2(suf2.getTbSuffixFrom());
			twoColumns.setTo2(suf2.getTbSuffixTo());
			twoColumns.setWidth2(suf2.getTbSuffixWidth());
			twoColumns.setPadding2(suf2.getTbSuffixPadding());
		} else if (TableSuffixTypeEnum.dbIndexForEachDB.getValue().equals(
				suf.getTbType())) {
			simpleTableMapProvider = new SimpleTableDatabaseMapProvider();
		} else if (TableSuffixTypeEnum.groovyTableList.getValue().equals(
				suf.getTbType())
				|| TableSuffixTypeEnum.groovyThroughAllDBTableList.getValue()
						.equals(suf.getTbType())
				|| TableSuffixTypeEnum.groovyAdjustTableList.getValue().equals(
						suf.getTbType())) {
			simpleTableMapProvider = new GroovyTableDatabaseMapProvider();
			try {
				GroovyTableDatabaseMapProvider groovyTableDatabaseMapProvider = (GroovyTableDatabaseMapProvider) simpleTableMapProvider;
				groovyTableDatabaseMapProvider.setTbType(suf.getTbType());
				groovyTableDatabaseMapProvider.setExpression(suffixManager
						.getExpression());
				groovyTableDatabaseMapProvider.setTbPreffix(tableRule
						.getTbPreffix());
				// �趨db�ĸ�������ʵ��groovy�ķֱ���ȷֲ���ʱ����õ���
				groovyTableDatabaseMapProvider.setDbNumber(tableRule
						.getDbIndexCount());
			} catch (ParseException e) {
				throw new ZdalClientException("ERROR ## Tbsuffix�����������⣡������", e);
			}

		} else {
			simpleTableMapProvider = new SimpleTableMapProvider();
		}
		return simpleTableMapProvider;
	}

	private LogicTable toLogicTable(TableRule tableRule) {
		SimpleLogicTable st = new SimpleLogicTable();
		st.setAllowReverseOutput(tableRule.isAllowReverseOutput());
		st.setDatabases(tableRule.getDbIndexes());
		if (tableRule.getDbRuleArray() != null) {
			List<Object> dbRules = new ArrayList<Object>(
					tableRule.getDbRuleArray().length);
			for (Object obj : tableRule.getDbRuleArray()) {
				dbRules.add((String) obj);
			}
			st.setDatabaseRuleStringList(dbRules);
		}
		if (tableRule.getTbRuleArray() != null) {
			List<Object> tbRules = new ArrayList<Object>(
					tableRule.getTbRuleArray().length);
			for (Object obj : tableRule.getTbRuleArray()) {
				tbRules.add((String) obj);
			}
			st.setTableRuleStringList(tbRules);
			// �����2�е��������2�е��࣬������ǰ���߼���
			st.setSimpleTableMapProvider(getTableMapProvider(tableRule));
			SuffixManager suffixManager = tableRule.getSuffixManager();
			Suffix suf = suffixManager.getSuffix(0);

			// �ֱ������ڣ������ñ��׺���ԣ��������κ�һ�����ԣ��ͱ�ʾ��simpleTableMapProvider
			st.setFrom(suf.getTbSuffixFrom());
			st.setTo(suf.getTbSuffixTo());
			st.setWidth(suf.getTbSuffixWidth());
			st.setPadding(suf.getTbSuffixPadding());
			st.setTablesNumberForEachDatabases(suf.getTbNumForEachDb());
		}
		if (tableRule.getUniqueKeyArray() != null) {
			st.setUniqueKeys(Arrays.asList(tableRule.getUniqueKeyArray()));
		}
		if (tableRule.isDisableFullTableScan()) {
			st.setDefaultListResultStragety(DEFAULT_LIST_RESULT_STRAGETY.NONE);
		} else {
			st.setDefaultListResultStragety(DEFAULT_LIST_RESULT_STRAGETY.FULL_TABLE_SCAN);
		}

		return st;
	}

	private SpringBasedDispatcherImpl buildSqlDispatcher(SQLParser parser,
			ZdalRoot zdalRoot) {
		if (zdalRoot != null) {
			SpringBasedDispatcherImpl dispatcher = new SpringBasedDispatcherImpl();
			dispatcher.setParser(parser);
			dispatcher.setRoot(zdalRoot);
			return dispatcher;
		} else {
			return null;
		}
	}

	private DBSelector buildDbSelector(String dbIndex,
			DataSource[] dataSourceArray) {
		Map<String, DataSource> map = new HashMap<String, DataSource>(
				dataSourceArray.length);
		for (int i = 0; i < dataSourceArray.length; i++) {
			map.put(dbIndex + Constants.DBINDEX_DSKEY_CONN_CHAR + i,
					dataSourceArray[i]);
		}
		EquityDbManager dbSelector = new EquityDbManager(dbIndex, map);
		dbSelector.setAppDsName(appDsName);
		return dbSelector;
	}

	private DBSelector buildDbSelector(String dbIndex,
			List<DataSource> dataSourceList) {
		Map<String, DataSource> map = new HashMap<String, DataSource>(
				dataSourceList.size());
		for (int i = 0, n = dataSourceList.size(); i < n; i++) {
			map.put(dbIndex + Constants.DBINDEX_DSKEY_CONN_CHAR + i,
					dataSourceList.get(i));
		}
		EquityDbManager dbSelector = new EquityDbManager(dbIndex, map);
		dbSelector.setAppDsName(appDsName);
		return dbSelector;
	}

	@SuppressWarnings("unchecked")
	private Map<String, DBSelector> buildDbSelectors(
			Map<String, ? extends Object> dataSourcePool) {
		Map<String, DBSelector> dsSelectors = new HashMap<String, DBSelector>();
		for (Map.Entry<String, ? extends Object> e : dataSourcePool.entrySet()) {
			if (e.getValue() instanceof DataSource) {
				OneDBSelector selector = new OneDBSelector(e.getKey(),
						(DataSource) e.getValue());
				selector.setAppDsName(appDsName);
				dsSelectors.put(e.getKey(), selector);
			} else if (e.getValue() instanceof DataSource[]) {
				dsSelectors
						.put(e.getKey(),
								buildDbSelector(e.getKey(),
										(DataSource[]) e.getValue()));
			} else if (e.getValue() instanceof List) {
				dsSelectors.put(
						e.getKey(),
						buildDbSelector(e.getKey(),
								(List<DataSource>) e.getValue()));
			} else if (e.getValue() instanceof DBSelector) {
				dsSelectors.put(e.getKey(), (DBSelector) e.getValue());
			} else if (e.getValue() instanceof String) {
				String[] dbs = ((String) e.getValue()).split(","); // ֧���Զ��ŷָ��Ķ������ԴID
				if (dbs.length == 1) {
					int index = dbs[0].indexOf(":");
					String dsbeanId = index == -1 ? dbs[0] : dbs[0].substring(
							0, index);// ����DSȥ������Ҫ��Ȩ��
					DataSource dataSource = getDataSourceObject(dsbeanId);
					OneDBSelector selector = new OneDBSelector(e.getKey(),
							dataSource);
					selector.setAppDsName(appDsName);
					dsSelectors.put(e.getKey(), selector);
				} else {
					DataSource[] dsArray = new DataSource[dbs.length];
					for (int i = 0; i < dbs.length; i++) {
						dsArray[i] = getDataSourceObject(dbs[i]);
					}
					dsSelectors.put(e.getKey(),
							buildDbSelector(e.getKey(), dsArray));
				}
			}
			dsSelectors.get(e.getKey()).setDbType(this.dbType);
		}
		return dsSelectors;
	}

	/**
	 * ��ȡ ����Դ
	 * 
	 * @return
	 */
	private DataSource getDataSourceObject(String dsName) {
		DataSource dataSource = null;
		if (StringUtil.isBlank(dsName)) {
			throw new IllegalArgumentException("The dsName can't be null!");
		}

		dataSource = this.dataSourcesMap.get(dsName.trim());

		if (dataSource == null) {
			throw new IllegalArgumentException(
					"The dataSource can't be null,dsName=" + dsName);
		}
		return dataSource;
	}

	/**
	 * ����ʽ����ģʽ��֧�֣�
	 * �ֿ����λ������һ��dbgroup��dbgroup���ְ��������д�⣬��ʱ���򲻷�masterRule��slaveRule
	 * ��ֻ��һ��oneRule
	 * 
	 * ÿ��key��Ӧdbgroup�У�ÿ��������ж�д���Լ�Ȩ�أ���ʽ���� <entry key="slave_0"
	 * value="slaver_db1_a:RW    ,slaver_db1_b:R" /> <entry key="slave_1"
	 * value="slaver_db2_a:R10W  ,slaver_db2_b:R20" /> <entry key="slave_2"
	 * value="slaver_db3_a:R10W10,slaver_db3_b:R20W0" /> <entry key="slave_3"
	 * value="slaver_db4_a:R10W20,slaver_db3_b:R20W10" /> <!-- ���� --> <entry
	 * key="slave_4" value="slaver_db5_a,slaver_db5_b" /><!-- ���� --> <entry
	 * key="slave_5" value="slaver_db6" /> * ��Ӧ��Ȩ�أ� slave_0=R10W10,R10W0
	 * slave_1=R10W10,R20W0 slave_3=R10W20,R20W10 slave_4=R10W10,R10W10
	 * slave_5=RW
	 * 
	 * 
	 * ����������
	 * ��oneRule��ֳ�masterRule��slaveRule����oneRule�е�dbIndex�ֱ���masterRule�м�_w��׺
	 * ����slaveRule�м�_r��׺ tabaleA: <property name="dbIndexes"
	 * value="slave_0,slave_1,slave_2,slave_3" />
	 * 
	 * master.tabaleA: <property name="dbIndexes"
	 * value="slave_0_w,slave_1_w,slave_2_w,slave_3_w" /> slaver.tabaleA:
	 * <property name="dbIndexes"
	 * value="slave_0_r,slave_1_r,slave_2_r,slave_3_r" />
	 * ��dbindex��ÿ������Դ�Ķ�д���ԣ���Ȩ�ز�ֵ�master_dbindex ��slave_dbindex
	 * master��slave�ľ�������Դ�г����еģ�ֻ�� <entry key="slave_0_w"
	 * value="slaver_db1_a:10,slaver_db1_b:0" /> <entry key="slave_0_r"
	 * value="slaver_db1_a:10,slaver_db1_b:10" /> <entry key="slave_1_w"
	 * value="slaver_db2_a:10,slaver_db2_b:0" /> <entry key="slave_1_r"
	 * value="slaver_db2_a:10,slaver_db2_b:20" /> <entry key="slave_2_w"
	 * value="slaver_db3_a:10,slaver_db3_b:0" /> <entry key="slave_2_r"
	 * value="slaver_db3_a:10,slaver_db3_b:20" /> <entry key="slave_3_w"
	 * value="slaver_db4_a:20,slaver_db3_b:10" /> <!-- ���� --> <entry
	 * key="slave_3_r" value="slaver_db4_a:10,slaver_db3_b:20" /> <!-- ���� -->
	 * <entry key="slave_4_w" value="slaver_db5_a:10,slaver_db5_b:10" /><!-- ����
	 * --> <entry key="slave_4_r" value="slaver_db5_a:10,slaver_db5_b:10" /><!--
	 * ���� --> <entry key="slave_5_w" value="slaver_db6" /> <entry
	 * key="slave_5_r" value="slaver_db6" />
	 * 
	 * Ȩ�����ͣ� slave_1=R10W10,R20W0 |--> slave_1_w[10,0], slave_1_r[10,20] ��Ϊ��
	 * slave_1=R10W10,R0,W0 |--> slave_1_w[10,0], slave_1_r[10,0]
	 * 
	 * @param dataSourcePool
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, DBSelector> buildRwDbSelectors(
			Map<String, ? extends Object> dataSourcePool) {
		Map<String, DBSelector> dsSelectors = new HashMap<String, DBSelector>();
		for (Map.Entry<String, ? extends Object> e : dataSourcePool.entrySet()) {
			String rdbIndex = e.getKey() + AppRule.DBINDEX_SUFFIX_READ; // "_r";
			String wdbIndex = e.getKey() + AppRule.DBINDEX_SUFFIX_WRITE;// "_w";
			if (e.getValue() instanceof DataSource) {
				OneDBSelector rSelector = new OneDBSelector(rdbIndex,
						(DataSource) e.getValue());
				rSelector.setAppDsName(appDsName);
				dsSelectors.put(rdbIndex, rSelector);
				OneDBSelector wSelector = new OneDBSelector(wdbIndex,
						(DataSource) e.getValue());
				wSelector.setAppDsName(appDsName);
				dsSelectors.put(wdbIndex, wSelector);
			} else if (e.getValue() instanceof DataSource[]) {
				dsSelectors.put(rdbIndex,
						buildDbSelector(rdbIndex, (DataSource[]) e.getValue()));
				dsSelectors.put(wdbIndex,
						buildDbSelector(wdbIndex, (DataSource[]) e.getValue()));
			} else if (e.getValue() instanceof List) {
				dsSelectors.put(
						rdbIndex,
						buildDbSelector(rdbIndex,
								(List<DataSource>) e.getValue()));
				dsSelectors.put(
						wdbIndex,
						buildDbSelector(wdbIndex,
								(List<DataSource>) e.getValue()));
			} else if (e.getValue() instanceof DBSelector) {
				dsSelectors.put(rdbIndex, (DBSelector) e.getValue());
				dsSelectors.put(wdbIndex, (DBSelector) e.getValue());
			} else if (e.getValue() instanceof String) {// ֻ�����String���͵���Ч.
				parse(dsSelectors, e.getKey(), (String) e.getValue());
			}
			dsSelectors.get(rdbIndex).setDbType(this.dbType);
			dsSelectors.get(wdbIndex).setDbType(this.dbType);
		}
		CONFIG_LOGGER.warn("warn ## \n"
				+ showDbSelectors(dsSelectors, dataSourcePool));
		return dsSelectors;
	}

	private String showDbSelectors(Map<String, DBSelector> dsSelectors,
			Map<String, ? extends Object> dataSourcePool) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, ? extends Object> e : dataSourcePool.entrySet()) {
			sb.append("[").append(e.getKey()).append("=").append(e.getValue())
					.append("]");
		}
		sb.append("\nconvert to:\n");
		for (Map.Entry<String, DBSelector> e : dsSelectors.entrySet()) {
			if (e.getValue() instanceof EquityDbManager) {
				EquityDbManager db = (EquityDbManager) e.getValue();
				sb.append(e.getKey()).append("=").append(db.getWeights())
						.append("\n");
			} else if (e.getValue() instanceof PriorityDbGroupSelector) {
				PriorityDbGroupSelector selector = (PriorityDbGroupSelector) e
						.getValue();
				sb.append(selector.getId() + ": \n");
				EquityDbManager[] dbs = selector.getPriorityGroups();
				for (EquityDbManager db : dbs) {
					sb.append(db.getId()).append(db.getWeights()).append("\n");
				}
			}

		}

		return sb.toString();
	}

	/**
	 * �������һ�飬�������ȶ������洢����Դ�� ͬһ�������ѡȡһ������ͬ���ϸ����ȼ�����ѡȡ��ֻ�и߼��������Ż�����ѡȡ�ͼ��������Դ
	 * 
	 * ֻдrwʱ��pr=pw=0Ĭ��ֵ������Ҷ���ͬһ���ڣ�������ѡȡ,ֻдp ʱ��pr=pw=p dbs =
	 * slaver_db3_a:R10W10p10,slaver_db3_b:R20W0p5 �Զ���д���ֱ�ּ���pr pw dbs =
	 * slaver_db3_a:R10W10pr10pw2,slaver_db3_b:R20W0pr5pw10
	 * 
	 * @param databaseSources
	 * @param dbIndex
	 * @param dsSelectors
	 */
	@SuppressWarnings("unchecked")
	private void parseDbSelector(String[] databaseSources, String dbIndex,
			Map<String, DBSelector> dsSelectors, WeightRWPQEnum rwPriority) {

		Map<Integer, Map<String, DataSource>> initDataSourceGroups = new HashMap<Integer, Map<String, DataSource>>(
				1);
		Map<Integer, Map<String, Integer>> weightGroups = new HashMap<Integer, Map<String, Integer>>(
				1);

		for (int i = 0; i < databaseSources.length; i++) {
			// ����ÿ��DataSource��Ȩ��
			String dsKey = dbIndex + Constants.DBINDEX_DSKEY_CONN_CHAR + i;
			String[] beanIdAndWeight = databaseSources[i].split(":"); // dbs[i]=slaver_db3_a:R10W10
			DataSource dataSource = (DataSource) this.getDataSourceObject(super
					.getZdalConfig().getLogicPhysicsDsNames()
					.get(beanIdAndWeight[0].trim()));// �߼��������ӳ��
			int[] weightRWPQ = parseWeightRW(beanIdAndWeight.length == 2 ? beanIdAndWeight[1]
					: null);

			// ��ñ���������ݿ��飬û���򴴽�
			Map<String, DataSource> initDataSources = initDataSourceGroups
					.get(weightRWPQ[rwPriority.value()]);
			if (initDataSources == null) {
				initDataSources = new HashMap<String, DataSource>(
						databaseSources.length);
				initDataSourceGroups.put(weightRWPQ[rwPriority.value()],
						initDataSources);
			}

			// ��ñ������Ȩ���飬û���򴴽�
			Map<String, Integer> weights = weightGroups
					.get(weightRWPQ[rwPriority.value()]);
			if (weights == null) {
				weights = new HashMap<String, Integer>(databaseSources.length);
				weightGroups.put(weightRWPQ[rwPriority.value()], weights);
			}

			weights.put(dsKey, weightRWPQ[rwPriority.value() - 2]);
			initDataSources.put(dsKey, dataSource);
		}

		if (initDataSourceGroups.size() == 1) {
			Map<String, DataSource> rInitDataSources = initDataSourceGroups
					.values().toArray(new Map[1])[0];
			Map<String, Integer> rWeights = weightGroups.values().toArray(
					new Map[1])[0];
			EquityDbManager equityDbManager = new EquityDbManager(dbIndex,
					rInitDataSources, rWeights);
			if (dbType != null) {
				equityDbManager.setDbType(dbType);
			}
			equityDbManager.setAppDsName(appDsName);
			dsSelectors.put(dbIndex, equityDbManager);
		} else {
			List<Integer> rpriorityKeys = new ArrayList<Integer>(
					initDataSourceGroups.size());
			rpriorityKeys.addAll(initDataSourceGroups.keySet());
			Collections.sort(rpriorityKeys);
			EquityDbManager[] rpriorityGroups = new EquityDbManager[rpriorityKeys
					.size()];

			for (int i = 0; i < rpriorityGroups.length; i++) {
				Integer key = rpriorityKeys.get(i);
				Map<String, DataSource> rInitDataSources = initDataSourceGroups
						.get(key);
				Map<String, Integer> rWeights = weightGroups.get(key);
				EquityDbManager equityDbManager = new EquityDbManager(dbIndex,
						rInitDataSources, rWeights);
				if (dbType != null)
					equityDbManager.setDbType(dbType);
				equityDbManager.setAppDsName(appDsName);
				rpriorityGroups[i] = equityDbManager;

			}
			dsSelectors.put(dbIndex, new PriorityDbGroupSelector(dbIndex,
					rpriorityGroups));
		}
	}

	// <entry key="slave_2" value="slaver_db3_a:R10W10,slaver_db3_b:R20W0" />
	private void parse(Map<String, DBSelector> dsSelectors, String dbIndex,
			String commaDbs) {
		String rdbIndex = dbIndex + AppRule.DBINDEX_SUFFIX_READ; // "_r";
		String wdbIndex = dbIndex + AppRule.DBINDEX_SUFFIX_WRITE;// "_w";
		String[] dbs = commaDbs.split(","); // ֧���Զ��ŷָ��Ķ������ԴID
		// ���ֻ��һ��DataSource������OneDBSelector
		if (dbs.length == 1) {
			int index = dbs[0].indexOf(":");
			String dsbeanId = index == -1 ? dbs[0] : dbs[0].substring(0, index);// ����DSȥ������Ҫ��Ȩ��
			DataSource ds = this.getDataSourceObject(super.getZdalConfig()
					.getLogicPhysicsDsNames().get(dsbeanId.trim()));// �߼��������ӳ��
			OneDBSelector selectorRead = new OneDBSelector(rdbIndex, ds);
			selectorRead.setAppDsName(appDsName);
			OneDBSelector selectorWrite = new OneDBSelector(wdbIndex, ds);
			selectorWrite.setAppDsName(appDsName);
			dsSelectors.put(rdbIndex, selectorRead);
			dsSelectors.put(wdbIndex, selectorWrite);
		} else {
			// �ֱ����д������Դ
			parseDbSelector(dbs, wdbIndex, dsSelectors,
					WeightRWPQEnum.writePriority);
			parseDbSelector(dbs, rdbIndex, dsSelectors,
					WeightRWPQEnum.readPriority);
		}
	}

	private int[] parseWeightRW(String weight) {
		if (weight == null) {
			return new int[] { 10, 10, 0, 0 }; // Ĭ�϶�д���򿪣���д��ΪP0��
		}
		int r, w, p, q;
		weight = weight.trim().toLowerCase(); // ͳ�Ƶ�Сд�����������
		if (weight.indexOf('R') == -1 && weight.indexOf('r') == -1) {
			r = 0;
		} else {
			r = parseNumber(weightPattern_r, weight, 10);
		}

		if (weight.indexOf('W') == -1 && weight.indexOf('w') == -1) {
			w = 0;
		} else {
			w = parseNumber(weightPattern_w, weight, 10);
		}
		if (weight.indexOf('P') == -1 && weight.indexOf('p') == -1) {
			p = 0;
		} else {
			p = parseNumber(weightPattern_p, weight, 0);
		}
		if (weight.indexOf('Q') == -1 && weight.indexOf('q') == -1) {
			q = 0;
		} else {
			q = parseNumber(weightPattern_q, weight, 0);
		}

		return new int[] { r, w, p, q };

	}

	private int parseNumber(Pattern p, String weight, int defaultValue) {
		Matcher m = p.matcher(weight);
		if (!m.find()) {// ������matches()�Ͳ��У�
			throw new IllegalArgumentException(
					"Ȩ�����ò���������ʽ[Rr](\\d*)[Ww](\\d*)[Pp](\\d*)[Qq](\\d*)��"
							+ weight);
		}
		if (m.group(1).length() == 0) {
			return defaultValue;
		} else {
			return Integer.parseInt(m.group(1));
		}
	}

	/**
	 * reset zdatasource��failoverRule��readWriteRule,Ŀǰtair����Դ��֧���ؽ�.
	 * 
	 * @param zdalConfig
	 */
	public void resetZdalDataSource(Map<String, String> keyWeights) {
		try {
			long startReset = System.currentTimeMillis();
			if (keyWeightConfig != null && !keyWeightConfig.isEmpty()) {
				this.resetKeyWeightConfig(keyWeights);
				String resetKeyWeightResults = getReceivDataStr(keyWeights);
				// �����е�Ȩ�ص�����Ϻ��ٴ�ӡ����־
				CONFIG_LOGGER.warn("WARN ## resetKeyWeightConfig[" + appDsName
						+ "]:" + resetKeyWeightResults);
				CONFIG_LOGGER.warn("WARN ## reset the config success,cost "
						+ (System.currentTimeMillis() - startReset)
						+ " ms,the appDsName = " + appDsName);
				this.keyWeightConfig = keyWeights;
			} else if (rwDataSourcePoolConfig != null
					&& !rwDataSourcePoolConfig.isEmpty()) {
				this.resetDbWeight(keyWeights);
				String dbWeightConfigs = getReceivDataStr(keyWeights);
				// �����е�Ȩ�ص�����Ϻ��ٴ�ӡ����־
				CONFIG_LOGGER.warn("WARN ## resetRwDataSourceConfig["
						+ appDsName + "]:" + dbWeightConfigs);
				CONFIG_LOGGER.warn("WARN ## reset the config success,cost "
						+ (System.currentTimeMillis() - startReset)
						+ " ms,the appDsName = " + appDsName);
				this.rwDataSourcePoolConfig = keyWeights;
			} else {
				throw new ZdalClientException(
						"ERROR ## only keyWeightConfig,rwDataSourcePoolConfig can reset weight");
			}

		} catch (Exception e) {
			throw new ZdalClientException("ERROR ## appDsName = "
					+ zdalConfig.getAppDsName() + " reset config failured ", e);
		}
	}

	/**
	 * zdal reset rw weight
	 * 
	 * @param p
	 */
	private void resetDbWeight(Map<String, String> p) {
		Map<String, DBSelector> dbSelectors = this.runtimeConfigHolder.get().dbSelectors;
		for (Map.Entry<String, String> entrySet : p.entrySet()) {
			String dbIndex = ((String) entrySet.getKey()).trim();
			String commaWeights = ((String) entrySet.getValue()).trim();

			if (this.rwDataSourcePoolConfig != null
					&& this.rwDataSourcePoolConfig.get(dbIndex) != null) {
				// readwriteRule��ʽ��weight
				resetRwDbWeight(dbIndex, dbSelectors, commaWeights);
			} else if (this.dataSourcePoolConfig != null
					&& dataSourcePoolConfig.get(dbIndex) != null) {
				String[] rdwds = commaWeights.split(",");
				int[] weights = new int[rdwds.length];
				for (int i = 0; i < rdwds.length; i++) {
					weights[i] = Integer.parseInt(rdwds[i]);
				}
				resetDbWeight(dbIndex, dbSelectors, weights);
			}
		}
	}

	/**
	 * @param commaWeights
	 *            : R10W10,R10W0 ��ʽ��dskey0=r10w10,r10w0
	 */
	private void resetRwDbWeight(String dbIndex,
			Map<String, DBSelector> dbSelectors, String commaWeights) {
		String[] rdwds = commaWeights.split(",");
		int[] rWeights = new int[rdwds.length];
		int[] wWeights = new int[rdwds.length];
		for (int i = 0; i < rdwds.length; i++) {
			int[] weightRW = parseWeightRW(rdwds[i]);
			rWeights[i] = weightRW[0];
			wWeights[i] = weightRW[1];
		}
		resetDbWeight(dbIndex + AppRule.DBINDEX_SUFFIX_READ, dbSelectors,
				rWeights);
		resetDbWeight(dbIndex + AppRule.DBINDEX_SUFFIX_WRITE, dbSelectors,
				wWeights);
	}

	private void resetDbWeight(String dbIndex,
			Map<String, DBSelector> dbSelectors, int[] weights) {
		DBSelector dbSelector = dbSelectors.get(dbIndex);
		if (dbSelector == null) {
			throw new ZdalClientException(
					"ERROR ## Couldn't find dbIndex in current datasoures. dbIndex:"
							+ dbIndex);
		}
		Map<String, Integer> weightMap = new HashMap<String, Integer>(
				weights.length);
		for (int i = 0; i < weights.length; i++) {
			weightMap.put(dbIndex + Constants.DBINDEX_DSKEY_CONN_CHAR + i,
					weights[i]);
		}
		dbSelector.setWeight(weightMap);
	}

	/**
	 * added by fanzeng. changed by boya. ����p��ʽ���� group_00=ds0:10,ds1:0
	 * group_01=ds2:10,ds3:0 group_02=ds4:0,ds5:10 һ��ֻ��һ�����ʱ���õ�����Ȩ�أ�Ĭ��Ϊ10
	 * 
	 * @param p
	 *            ���͹���������
	 */
	protected void resetKeyWeightConfig(Map<String, String> p) {
		// Map<String, ZdalDataSourceKeyWeightRandom> keyWeightMapHolder =
		// GetDataSourceSequenceRules
		// .getKeyWeightRuntimeConfigHoder().get().getKeyWeightMapHolder();
		Map<String, ZdalDataSourceKeyWeightRandom> keyWeightMapHolder = keyWeightMapConfig;
		for (Entry<String, String> entrySet : p.entrySet()) {
			String groupKey = entrySet.getKey();
			String value = entrySet.getValue();
			if (StringUtil.isBlank(groupKey) || StringUtil.isBlank(value)) {
				throw new ZdalClientException("ERROR ## ����ԴgroupKey="
						+ groupKey + "����Ȩ��������Ϣ����Ϊ��,value=" + value);
			}
			String[] keyWeightStr = value.split(",");
			String[] weightKeys = new String[keyWeightStr.length];
			int[] weights = new int[keyWeightStr.length];
			for (int i = 0; i < keyWeightStr.length; i++) {
				if (StringUtil.isBlank(keyWeightStr[i])) {
					throw new ZdalClientException("ERROR ## ����ԴkeyWeightStr["
							+ i + "]����Ȩ��������Ϣ����Ϊ��.");
				}
				String[] keyAndWeight = keyWeightStr[i].split(":");
				if (keyAndWeight.length != 2) {
					throw new ZdalClientException(
							"ERROR ## ����Դkey��������Ȩ�ش���,keyWeightStr[" + i + "]="
									+ keyWeightStr[i] + ".");
				}
				String key = keyAndWeight[0];
				String weightStr = keyAndWeight[1];
				if (StringUtil.isBlank(key) || StringUtil.isBlank(weightStr)) {
					CONFIG_LOGGER.error("ERROR ## ����Դ����Ȩ��������Ϣ����Ϊ��,key=" + key
							+ ",weightStr=" + weightStr);
					return;
				}
				weightKeys[i] = key.trim();
				weights[i] = Integer.parseInt(weightStr.trim());
			}
			// ���� groupKey�Լ���Ӧ��keyAndWeightMapȥ��ѯ
			ZdalDataSourceKeyWeightRandom weightRandom = keyWeightMapHolder
					.get(groupKey);
			if (weightRandom == null) {
				throw new ZdalClientException(
						"ERROR ## �����͵İ�����Դkey����Ȩ�������е�key����,�Ƿ���groupKey="
								+ groupKey);
			}
			for (String newKey : weightKeys) {
				if (weightRandom.getWeightConfig() == null
						|| !weightRandom.getWeightConfig().containsKey(newKey)) {
					throw new ZdalClientException("�����͵�����Դ����" + groupKey
							+ "Ȩ�������а��������ڸ��������Դ��ʶ,key=" + newKey);
				}
			}
			if (weightKeys.length != weightRandom.getDataSourceNumberInGroup()) {
				throw new ZdalClientException("�����͵İ�����Դkey����Ȩ�������У�����groupKey="
						+ groupKey + "����������Դ�������� ,size=" + weightKeys.length
						+ ",the size should be "
						+ weightRandom.getDataSourceNumberInGroup());
			}
			// ���ݸ����groupKey�Լ���Ӧ��keyAndWeightMap����TDataSourceKeyWeightRandom
			ZdalDataSourceKeyWeightRandom TDataSourceKeyWeightRandom = new ZdalDataSourceKeyWeightRandom(
					weightKeys, weights);
			keyWeightMapHolder.put(groupKey, TDataSourceKeyWeightRandom);
		}
		// ���ñ��ص�keyWeightMapCofig���ԣ�ȫ����Ի������ڸ�����
		this.keyWeightMapConfig = keyWeightMapHolder;
	}

	/**
	 * added for zdal
	 * 
	 * @param p
	 * @return
	 */
	private String getReceivDataStr(Map<String, String> p) {
		String str = "";
		if (p != null) {
			StringBuilder sb = new StringBuilder();
			for (Entry<String, String> entry : p.entrySet()) {
				String key = entry.getKey().trim();
				String value = entry.getValue().trim();
				sb.append(key).append("=").append(value).append(";");
			}
			str = sb.toString();
		}
		return str;
	}

	/**
	 * zdataconsole�е�������Ϣת����ZdataSource��������Ϣ .
	 * 
	 * @param parameter
	 * @return
	 */
	private LocalTxDataSourceDO createDataSourceDO(
			DataSourceParameter parameter, DBType dbType, String dsName)
			throws Exception {
		LocalTxDataSourceDO dsDo = new LocalTxDataSourceDO();
		dsDo.setDsName(dsName);
		dsDo.setConnectionURL(parameter.getJdbcUrl());
		dsDo.setUserName(parameter.getUserName());
		// dsDo.setEncPassword(parameter.getPassword());
		dsDo.setPassWord(parameter.getPassword());// �������ĵ�����.
		dsDo.setMinPoolSize(parameter.getMinConn());
		dsDo.setMaxPoolSize(parameter.getMaxConn());
		dsDo.setDriverClass(parameter.getDriverClass());
		dsDo.setBlockingTimeoutMillis(parameter.getBlockingTimeoutMillis());
		dsDo.setIdleTimeoutMinutes(parameter.getIdleTimeoutMinutes());
		dsDo.setPreparedStatementCacheSize(parameter
				.getPreparedStatementCacheSize());
		dsDo.setQueryTimeout(parameter.getQueryTimeout());
		dsDo.getConnectionProperties().putAll(
				parameter.getConnectionProperties());
		dsDo.setPrefill(parameter.getPrefill());
		if (dbType.isMysql()) {
			dsDo.setExceptionSorterClassName(MySQLExceptionSorter.class
					.getName());
		} else if (dbType.isOracle()) {
			dsDo.setExceptionSorterClassName(OracleExceptionSorter.class
					.getName());
		} else if (dbType.isDB2()) {
			dsDo.setExceptionSorterClassName(DB2ExceptionSorter.class.getName());
			if (!(parameter.getCheckValidConnectionSQL() == null || parameter
					.getCheckValidConnectionSQL().length() == 0)) {
				// ������֤����,cxt-dengzhiyong
				dsDo.setValidateOnMatch(true);
				// dsDo.setValidConnectionCheckerClassName(CheckValidConnectionSQL.class.getName());
				dsDo.setCheckValidConnectionSQL(parameter.getCheckValidConnectionSQL());
				// ������֤����,cxt-dengzhiyong
			}
		} else {
			throw new ZdalClientException(
					"ERROR ## the DbType must be mysql/oracle/db2.");
		}
		dsDo.setConnectionProperties(parameter.getConnectionProperties());
		return dsDo;

	}

	/**
	 * ����Դ��connection�ķ�װ.
	 * 
	 * @see javax.sql.DataSource#getConnection()
	 */
	public Connection getConnection() throws SQLException {
		if (inited.get() == false) {
			throw new ZdalClientException(
					"ERROR ## the ZdalDataSource has not init");
		}
		ZdalConnection connection = new ZdalConnection();
		this.buildTconnection(connection);
		return connection;
	}

	/**
	 * @see javax.sql.DataSource#getConnection(java.lang.String,
	 *      java.lang.String)
	 */
	public Connection getConnection(String username, String password)
			throws SQLException {
		if (inited.get() == false) {
			throw new ZdalClientException(
					"ERROR ## the ZdalDataSource has not init");
		}
		ZdalConnection connection = new ZdalConnection(username, password);
		this.buildTconnection(connection);
		return connection;
	}

	/**
	 * �������ӣ�����Ҫ�Ĳ������õ�ZdalConnection��ȥ��Ȼ�����ø� ZdalStatement ����
	 * 
	 * @param connection
	 */
	private void buildTconnection(ZdalConnection connection) {
		ZdalRuntime rt = this.runtimeConfigHolder.get();
		connection.setDataSourcePool(rt == null ? null : rt.dbSelectors);
		connection.setWriteDispatcher(this.writeDispatcher);
		connection.setReadDispatcher(this.readDispatcher);
		connection.setRetryingTimes(this.retryingTimes);
		connection.setDbConfigType(this.dbConfigType);
		connection.setAppDsName(appDsName);
	}

	/**
	 * @see javax.sql.CommonDataSource#getLoginTimeout()
	 */
	public int getLoginTimeout() throws SQLException {
		throw new UnsupportedOperationException("getLoginTimeout");
	}

	/**
	 * @see javax.sql.CommonDataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
		throw new UnsupportedOperationException("setLoginTimeout");
	}

	/**
	 * @see javax.sql.CommonDataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws SQLException {
		throw new UnsupportedOperationException("getLogWriter");
	}

	/**
	 * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(PrintWriter out) throws SQLException {
		throw new UnsupportedOperationException("setLogWriter");
	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("isWrapperFor");
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException("unwrap");
	}

	public Map<String, ZdalDataSourceKeyWeightRandom> getKeyWeightMapConfig() {
		return keyWeightMapConfig;
	}

	public RuntimeConfigHolder<ZdalRuntime> getRuntimeConfigHolder() {
		return runtimeConfigHolder;
	}

	public SqlDispatcher getWriteDispatcher() {
		return writeDispatcher;
	}

	public SqlDispatcher getReadDispatcher() {
		return readDispatcher;
	}

	public AppRule getAppRule() {
		return appRule;
	}

	public Map<String, String> getKeyWeightConfig() {
		return keyWeightConfig;
	}

	public Map<String, ? extends Object> getRwDataSourcePoolConfig() {
		return rwDataSourcePoolConfig;
	}

	public Map<String, ZDataSource> getDataSourcesMap() {
		return dataSourcesMap;
	}

	public ZdalSignalResource getZdalSignalResource() {
		return zdalSignalResource;
	}

	public int getRetryingTimes() {
		return retryingTimes;
	}

	public void setRetryingTimes(int retryingTimes) {
		this.retryingTimes = retryingTimes;
	}

	public Map<String, ? extends Object> getDataSourcePoolConfig() {
		return dataSourcePoolConfig;
	}

}
