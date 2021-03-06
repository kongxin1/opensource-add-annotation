package org.apache.activemq.store.kahadb.data;

import org.apache.activemq.protobuf.BaseMessage;
import org.apache.activemq.protobuf.Buffer;

abstract class KahaAddMessageCommandBase<T> extends BaseMessage<T> {
	private KahaTransactionInfo f_transactionInfo = null;
	private KahaDestination f_destination = null;
	private String f_messageId = null;
	private boolean b_messageId;
	private Buffer f_message = null;
	private boolean b_message;
	private int f_priority = 4;
	private boolean b_priority;
	private boolean f_prioritySupported = false;
	private boolean b_prioritySupported;

	public boolean hasTransactionInfo() {
		return this.f_transactionInfo != null;
	}
	public KahaTransactionInfo getTransactionInfo() {
		if (this.f_transactionInfo == null) {
			this.f_transactionInfo = new KahaTransactionInfo();
		}
		return this.f_transactionInfo;
	}
	public T setTransactionInfo(KahaTransactionInfo transactionInfo) {
		loadAndClear();
		this.f_transactionInfo = transactionInfo;
		return this;
	}
	public void clearTransactionInfo() {
		loadAndClear();
		this.f_transactionInfo = null;
	}
	public boolean hasDestination() {
		return this.f_destination != null;
	}
	public KahaDestination getDestination() {
		if (this.f_destination == null) {
			this.f_destination = new KahaDestination();
		}
		return this.f_destination;
	}
	public T setDestination(KahaDestination destination) {
		loadAndClear();
		this.f_destination = destination;
		return this;
	}
	public void clearDestination() {
		loadAndClear();
		this.f_destination = null;
	}
	public boolean hasMessageId() {
		return this.b_messageId;
	}
	public String getMessageId() {
		return this.f_messageId;
	}
	public T setMessageId(String messageId) {
		loadAndClear();
		this.b_messageId = true;
		this.f_messageId = messageId;
		return this;
	}
	public void clearMessageId() {
		loadAndClear();
		this.b_messageId = false;
		this.f_messageId = null;
	}
	public boolean hasMessage() {
		return this.b_message;
	}
	public Buffer getMessage() {
		return this.f_message;
	}
	public T setMessage(Buffer message) {
		loadAndClear();
		this.b_message = true;
		this.f_message = message;
		return this;
	}
	public void clearMessage() {
		loadAndClear();
		this.b_message = false;
		this.f_message = null;
	}
	public boolean hasPriority() {
		return this.b_priority;
	}
	public int getPriority() {
		return this.f_priority;
	}
	public T setPriority(int priority) {
		loadAndClear();
		this.b_priority = true;
		this.f_priority = priority;
		return this;
	}
	public void clearPriority() {
		loadAndClear();
		this.b_priority = false;
		this.f_priority = 4;
	}
	public boolean hasPrioritySupported() {
		return this.b_prioritySupported;
	}
	public boolean getPrioritySupported() {
		return this.f_prioritySupported;
	}
	public T setPrioritySupported(boolean prioritySupported) {
		loadAndClear();
		this.b_prioritySupported = true;
		this.f_prioritySupported = prioritySupported;
		return this;
	}
	public void clearPrioritySupported() {
		loadAndClear();
		this.b_prioritySupported = false;
		this.f_prioritySupported = false;
	}
}
