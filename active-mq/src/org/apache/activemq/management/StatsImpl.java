/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;

/**
 * Base class for a Stats implementation
 */
/**
 * 计数器集合类，它里面有一个Set集合，用于存储计数器，可以将该类看作是一个计数器集合，不过它也实现了一般计数器的接口，所以该类也可以看做是一个普通的计数器，只不过它里面存储了多个计数器
 * @ClassName: StatsImpl
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月6日 下午11:23:48
 */
public class StatsImpl extends StatisticImpl implements Stats, Resettable {
	// use a Set instead of a Map - to conserve Space
	// 计数器集合
	private Set<StatisticImpl> set;

	public StatsImpl() {
		this(new CopyOnWriteArraySet<StatisticImpl>());
	}
	public StatsImpl(Set<StatisticImpl> set) {
		super("stats", "many", "Used only as container, not Statistic");
		this.set = set;
	}
	/**
	 * 重置计数器集合中的所有计数器，重置计数器需要实现Resettable接口，没有实现该接口的计数器不能重置
	 */
	public void reset() {
		Statistic[] stats = getStatistics();
		int size = stats.length;
		for (int i = 0; i < size; i++) {
			Statistic stat = stats[i];
			if (stat instanceof Resettable) {
				Resettable r = (Resettable) stat;
				r.reset();
			}
		}
	}
	/**
	 * 根据入参得到对应的计数器，每个计数器都有一个名字
	 */
	public Statistic getStatistic(String name) {
		for (StatisticImpl stat : this.set) {
			if (stat.getName() != null && stat.getName().equals(name)) {
				return stat;
			}
		}
		return null;
	}
	/**
	 * 得到本计数器中所有计数器的名字
	 */
	public String[] getStatisticNames() {
		List<String> names = new ArrayList<String>();
		for (StatisticImpl stat : this.set) {
			names.add(stat.getName());
		}
		String[] answer = new String[names.size()];
		names.toArray(answer);
		return answer;
	}
	/**
	 * 得到所有的计数器
	 */
	public Statistic[] getStatistics() {
		Statistic[] answer = new Statistic[this.set.size()];
		set.toArray(answer);
		return answer;
	}
	/**
	 * 添加新的计数器
	 * @Title: addStatistic
	 * @Description: TODO
	 * @param name
	 * @param statistic
	 * @return: void
	 */
	protected void addStatistic(String name, StatisticImpl statistic) {
		this.set.add(statistic);
	}
}
