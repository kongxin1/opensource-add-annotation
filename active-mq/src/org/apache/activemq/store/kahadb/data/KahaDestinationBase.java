package org.apache.activemq.store.kahadb.data;

import org.apache.activemq.protobuf.BaseMessage;

abstract class KahaDestinationBase<T> extends BaseMessage<T> {
	private KahaDestination.DestinationType f_type = KahaDestination.DestinationType.QUEUE;
	private boolean b_type;// 表示f_type是否设置
	private String f_name = null;
	private boolean b_name;// 表示f_name是否设置

	public boolean hasType() {
		return this.b_type;
	}
	public KahaDestination.DestinationType getType() {
		return this.f_type;
	}
	public KahaDestinationBase<T> setType(KahaDestination.DestinationType type) {
		loadAndClear();
		this.b_type = true;
		this.f_type = type;
		return this;
	}
	public void clearType() {
		loadAndClear();
		this.b_type = false;
		this.f_type = KahaDestination.DestinationType.QUEUE;
	}
	public boolean hasName() {
		return this.b_name;
	}
	public String getName() {
		return this.f_name;
	}
	public KahaDestinationBase<T> setName(String name) {
		loadAndClear();
		this.b_name = true;
		this.f_name = name;
		return this;
	}
	public void clearName() {
		loadAndClear();
		this.b_name = false;
		this.f_name = null;
	}
}
