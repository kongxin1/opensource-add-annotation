package org.apache.activemq.store.kahadb.data;

import org.apache.activemq.protobuf.BaseMessage;
import org.apache.activemq.protobuf.Buffer;

abstract class KahaAddScheduledJobCommandBase<T> extends BaseMessage<T> {
	private String f_scheduler = null;
	private boolean b_scheduler;
	private String f_jobId = null;
	private boolean b_jobId;
	private long f_startTime = 0L;
	private boolean b_startTime;
	private String f_cronEntry = null;
	private boolean b_cronEntry;
	private long f_delay = 0L;
	private boolean b_delay;
	private long f_period = 0L;
	private boolean b_period;
	private int f_repeat = 0;
	private boolean b_repeat;
	private Buffer f_payload = null;
	private boolean b_payload;
	private long f_nextExecutionTime = 0L;
	private boolean b_nextExecutionTime;

	public boolean hasScheduler() {
		return this.b_scheduler;
	}
	public String getScheduler() {
		return this.f_scheduler;
	}
	public T setScheduler(String scheduler) {
		loadAndClear();
		this.b_scheduler = true;
		this.f_scheduler = scheduler;
		return this;
	}
	public void clearScheduler() {
		loadAndClear();
		this.b_scheduler = false;
		this.f_scheduler = null;
	}
	public boolean hasJobId() {
		return this.b_jobId;
	}
	public String getJobId() {
		return this.f_jobId;
	}
	public T setJobId(String jobId) {
		loadAndClear();
		this.b_jobId = true;
		this.f_jobId = jobId;
		return this;
	}
	public void clearJobId() {
		loadAndClear();
		this.b_jobId = false;
		this.f_jobId = null;
	}
	public boolean hasStartTime() {
		return this.b_startTime;
	}
	public long getStartTime() {
		return this.f_startTime;
	}
	public T setStartTime(long startTime) {
		loadAndClear();
		this.b_startTime = true;
		this.f_startTime = startTime;
		return this;
	}
	public void clearStartTime() {
		loadAndClear();
		this.b_startTime = false;
		this.f_startTime = 0L;
	}
	public boolean hasCronEntry() {
		return this.b_cronEntry;
	}
	public String getCronEntry() {
		return this.f_cronEntry;
	}
	public T setCronEntry(String cronEntry) {
		loadAndClear();
		this.b_cronEntry = true;
		this.f_cronEntry = cronEntry;
		return this;
	}
	public void clearCronEntry() {
		loadAndClear();
		this.b_cronEntry = false;
		this.f_cronEntry = null;
	}
	public boolean hasDelay() {
		return this.b_delay;
	}
	public long getDelay() {
		return this.f_delay;
	}
	public T setDelay(long delay) {
		loadAndClear();
		this.b_delay = true;
		this.f_delay = delay;
		return this;
	}
	public void clearDelay() {
		loadAndClear();
		this.b_delay = false;
		this.f_delay = 0L;
	}
	public boolean hasPeriod() {
		return this.b_period;
	}
	public long getPeriod() {
		return this.f_period;
	}
	public T setPeriod(long period) {
		loadAndClear();
		this.b_period = true;
		this.f_period = period;
		return this;
	}
	public void clearPeriod() {
		loadAndClear();
		this.b_period = false;
		this.f_period = 0L;
	}
	public boolean hasRepeat() {
		return this.b_repeat;
	}
	public int getRepeat() {
		return this.f_repeat;
	}
	public T setRepeat(int repeat) {
		loadAndClear();
		this.b_repeat = true;
		this.f_repeat = repeat;
		return this;
	}
	public void clearRepeat() {
		loadAndClear();
		this.b_repeat = false;
		this.f_repeat = 0;
	}
	public boolean hasPayload() {
		return this.b_payload;
	}
	public Buffer getPayload() {
		return this.f_payload;
	}
	public T setPayload(Buffer payload) {
		loadAndClear();
		this.b_payload = true;
		this.f_payload = payload;
		return this;
	}
	public void clearPayload() {
		loadAndClear();
		this.b_payload = false;
		this.f_payload = null;
	}
	public boolean hasNextExecutionTime() {
		return this.b_nextExecutionTime;
	}
	public long getNextExecutionTime() {
		return this.f_nextExecutionTime;
	}
	public T setNextExecutionTime(long nextExecutionTime) {
		loadAndClear();
		this.b_nextExecutionTime = true;
		this.f_nextExecutionTime = nextExecutionTime;
		return this;
	}
	public void clearNextExecutionTime() {
		loadAndClear();
		this.b_nextExecutionTime = false;
		this.f_nextExecutionTime = 0L;
	}
}
