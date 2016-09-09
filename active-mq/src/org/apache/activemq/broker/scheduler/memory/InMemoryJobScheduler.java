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
package org.apache.activemq.broker.scheduler.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jms.MessageFormatException;

import org.apache.activemq.broker.scheduler.CronParser;
import org.apache.activemq.broker.scheduler.Job;
import org.apache.activemq.broker.scheduler.JobListener;
import org.apache.activemq.broker.scheduler.JobScheduler;
import org.apache.activemq.broker.scheduler.JobSupport;
import org.apache.activemq.util.ByteSequence;
import org.apache.activemq.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an in-memory JobScheduler instance.
 */
/**
 * ��ʱ���������
 * @ClassName: InMemoryJobScheduler
 * @Description: TODO
 * @author: ����
 * @date: 2016��7��11�� ����10:27:22
 */
public class InMemoryJobScheduler implements JobScheduler {
	private static final Logger LOG = LoggerFactory.getLogger(InMemoryJobScheduler.class);
	private static final IdGenerator ID_GENERATOR = new IdGenerator();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final String name;
	// ��Ҫִ�е����񼯺ϣ�key��ʾ����ִ��ʱ�䣬value��ʾ������keyҪ���ʱ�俪ʼִ�е����񼯺�
	private final TreeMap<Long, ScheduledTask> jobs = new TreeMap<Long, ScheduledTask>();
	private final AtomicBoolean started = new AtomicBoolean(false);
	private final AtomicBoolean dispatchEnabled = new AtomicBoolean(false);
	private final List<JobListener> jobListeners = new CopyOnWriteArrayList<JobListener>();
	// ��ÿ�� Timer �������Ӧ���ǵ�����̨�̣߳�����˳���ִ�����м�ʱ���������еļ�ʱ����ͬһ����̨�߳������У����ǰ��ļ�ʱ��ִ��������Ӱ������ʱ����ִ��
	private final Timer timer = new Timer();

	public InMemoryJobScheduler(String name) {
		this.name = name;
	}
	@Override
	public String getName() throws Exception {
		return name;
	}
	/**
	 * ���������󣬾����޸�started��dispatchEnabled���ԣ�dispatchEnabled���Ա�ʾ������Էַ�����ʵ���ǿ��Ե������������
	 * @Title: start
	 * @Description: TODO
	 * @throws Exception
	 * @return: void
	 */
	public void start() throws Exception {
		if (started.compareAndSet(false, true)) {
			startDispatching();
			LOG.trace("JobScheduler[{}] started", name);
		}
	}
	/**
	 * ��ֹ��ʱ����ͬʱ������񼯺�jobs�е���������
	 * @Title: stop
	 * @Description: TODO
	 * @throws Exception
	 * @return: void
	 */
	public void stop() throws Exception {
		if (started.compareAndSet(true, false)) {
			stopDispatching();
			timer.cancel();
			jobs.clear();
			LOG.trace("JobScheduler[{}] stopped", name);
		}
	}
	public boolean isStarted() {
		return started.get();
	}
	public boolean isDispatchEnabled() {
		return dispatchEnabled.get();
	}
	@Override
	public void startDispatching() throws Exception {
		dispatchEnabled.set(true);
	}
	@Override
	public void stopDispatching() throws Exception {
		dispatchEnabled.set(false);
	}
	@Override
	public void addListener(JobListener listener) throws Exception {
		this.jobListeners.add(listener);
	}
	@Override
	public void removeListener(JobListener listener) throws Exception {
		this.jobListeners.remove(listener);
	}
	@Override
	public void schedule(String jobId, ByteSequence payload, long delay) throws Exception {
		doSchedule(jobId, payload, "", 0, delay, 0);
	}
	@Override
	public void schedule(String jobId, ByteSequence payload, String cronEntry) throws Exception {
		doSchedule(jobId, payload, cronEntry, 0, 0, 0);
	}
	@Override
	public void schedule(String jobId, ByteSequence payload, String cronEntry, long delay, long period, int repeat)
			throws Exception {
		doSchedule(jobId, payload, cronEntry, delay, period, repeat);
	}
	@Override
	public void remove(long time) throws Exception {
		doRemoveRange(time, time);
	}
	@Override
	public void remove(String jobId) throws Exception {
		doRemoveJob(jobId);
	}
	@Override
	public void removeAllJobs() throws Exception {
		doRemoveRange(0, Long.MAX_VALUE);
	}
	@Override
	public void removeAllJobs(long start, long finish) throws Exception {
		doRemoveRange(start, finish);
	}
	@Override
	/**
	 * ��ȡ��һ������Ŀ�ʼִ��ʱ��
	 */
	public long getNextScheduleTime() throws Exception {
		long nextExecutionTime = -1L;
		lock.readLock().lock();
		try {
			if (!jobs.isEmpty()) {
				nextExecutionTime = jobs.entrySet().iterator().next().getKey();
			}
		} finally {
			lock.readLock().unlock();
		}
		return nextExecutionTime;
	}
	@Override
	/**
	 * ��ȡ��һ����������
	 */
	public List<Job> getNextScheduleJobs() throws Exception {
		List<Job> result = new ArrayList<Job>();
		lock.readLock().lock();
		try {
			if (!jobs.isEmpty()) {
				result.addAll(jobs.entrySet().iterator().next().getValue().getAllJobs());
			}
		} finally {
			lock.readLock().unlock();
		}
		return result;
	}
	@Override
	/**
	 * ��ñ�������������Ҫִ�е�����
	 */
	public List<Job> getAllJobs() throws Exception {
		final List<Job> result = new ArrayList<Job>();
		this.lock.readLock().lock();
		try {
			for (Map.Entry<Long, ScheduledTask> entry : jobs.entrySet()) {
				result.addAll(entry.getValue().getAllJobs());
			}
		} finally {
			this.lock.readLock().unlock();
		}
		return result;
	}
	@Override
	/**
	 * ��ȡ��������ʼִ��ʱ�������Ҫ��ķ�Χ֮�������
	 */
	public List<Job> getAllJobs(long start, long finish) throws Exception {
		final List<Job> result = new ArrayList<Job>();
		this.lock.readLock().lock();
		try {
			for (Map.Entry<Long, ScheduledTask> entry : jobs.entrySet()) {
				long jobTime = entry.getKey();
				if (start <= jobTime && jobTime <= finish) {
					result.addAll(entry.getValue().getAllJobs());
				}
			}
		} finally {
			this.lock.readLock().unlock();
		}
		return result;
	}
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	@Override
	public String toString() {
		return "JobScheduler: " + name;
	}
	/**
	 * ����cronEntry��������ǰ������һ�ε�ִ��ʱ�䣬Ȼ�󴴽���һ��InMemoryJob���񣬲�����������ӵ�jobs������
	 * @Title: doSchedule
	 * @Description: TODO
	 * @param jobId��ʾ����������
	 * @param payload�������ò����
	 * @param cronEntry��ʾcron���Ҳ����linux�е�crontab����
	 * @param delay��ʾ�ӳٶ೤ʱ��ִ��
	 * @param period��ʾ�����ִ������
	 * @param repeat��period����
	 *            �����û������cron�����ô�����Ҫ�ظ�ִ�У���һ��ִ��ʱ�����ͨ��period����ģ�repeat���ӳ�������cron�����������Ҫrepeat
	 *            =0ʱ������cron������������ִ��
	 * @throws IOException
	 * @return: void
	 */
	private void doSchedule(final String jobId, final ByteSequence payload, final String cronEntry, long delay,
			long period, int repeat) throws IOException {
		long startTime = System.currentTimeMillis();
		long executionTime = 0;
		// round startTime - so we can schedule more jobs at the same time
		// �Կ�ʼʱ����н��ƣ��Ա��ڽ����������������ͬ��ʱ��ִ��
		startTime = (startTime / 1000) * 1000;
		if (cronEntry != null && cronEntry.length() > 0) {
			try {
				// cronEntry�ǰ���Linux�е�cron������д�ģ�����Ĵ�����Զ����������з���
				// �õ����ڵ��ڵ�ǰʱ�����һ��ִ��ʱ��
				executionTime = CronParser.getNextScheduledTime(cronEntry, startTime);
			} catch (MessageFormatException e) {
				throw new IOException(e.getMessage());
			}
		}
		// ����0����ʾû������cron����
		if (executionTime == 0) {
			// start time not set by CRON - so it it to the current time
			executionTime = startTime;
		}
		// ���ӳ�ʵ��ʱ��Ϊ��
		if (delay > 0) {
			executionTime += delay;
		} else {
			executionTime += period;
		}
		// ����һ���ڴ�����
		InMemoryJob newJob = new InMemoryJob(jobId);
		newJob.setStart(startTime);
		newJob.setCronEntry(cronEntry);
		newJob.setDelay(delay);
		newJob.setPeriod(period);
		newJob.setRepeat(repeat);
		newJob.setNextTime(executionTime);
		newJob.setPayload(payload.getData());
		LOG.trace("JobScheduler adding job[{}] to fire at: {}", jobId, JobSupport.getDateTime(executionTime));
		lock.writeLock().lock();
		// ��ScheduledTask����������������
		try {
			ScheduledTask task = jobs.get(executionTime);
			if (task == null) {
				task = new ScheduledTask(executionTime);
				task.add(newJob);
				jobs.put(task.getExecutionTime(), task);
				// ��������뵽��ʱ����
				timer.schedule(task, new Date(newJob.getNextTime()));
			} else {
				task.add(newJob);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	/**
	 * ���ݵ�ǰ���������һ��ִ��ʱ�䣬��������ӵ�jobs�����У�����ӵ�timer��ʱ����
	 * @Title: doReschedule
	 * @Description: TODO
	 * @param job
	 * @param nextExecutionTime
	 * @return: void
	 */
	private void doReschedule(InMemoryJob job, long nextExecutionTime) {
		job.setNextTime(nextExecutionTime);
		// ����ִ�д���
		job.incrementExecutionCount();
		// ����ѭ��ִ�еĴ���
		job.decrementRepeatCount();
		LOG.trace("JobScheduler rescheduling job[{}] to fire at: {}", job.getJobId(),
				JobSupport.getDateTime(nextExecutionTime));
		lock.writeLock().lock();
		try {
			ScheduledTask task = jobs.get(nextExecutionTime);
			if (task == null) {
				task = new ScheduledTask(nextExecutionTime);
				task.add(job);
				jobs.put(task.getExecutionTime(), task);
				// ��������뵽��ʱ����
				timer.schedule(task, new Date(task.getExecutionTime()));
			} else {
				task.add(job);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	/**
	 * ��������id����Ӧ������ɾ��
	 * @Title: doRemoveJob
	 * @Description: TODO
	 * @param jobId
	 * @throws IOException
	 * @return: void
	 */
	private void doRemoveJob(String jobId) throws IOException {
		this.lock.writeLock().lock();
		try {
			Iterator<Map.Entry<Long, ScheduledTask>> scheduled = jobs.entrySet().iterator();
			while (scheduled.hasNext()) {
				Map.Entry<Long, ScheduledTask> entry = scheduled.next();
				ScheduledTask task = entry.getValue();
				if (task.remove(jobId)) {
					LOG.trace("JobScheduler removing job[{}]", jobId);
					if (task.isEmpty()) {
						task.cancel();
						scheduled.remove();
					}
					return;
				}
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}
	/**
	 * ����ʼִ��ʱ�������Ҫ��ķ�Χ�ڵ�����ɾ��
	 * @Title: doRemoveRange
	 * @Description: TODO
	 * @param start
	 * @param end
	 * @throws IOException
	 * @return: void
	 */
	private void doRemoveRange(long start, long end) throws IOException {
		this.lock.writeLock().lock();
		try {
			Iterator<Map.Entry<Long, ScheduledTask>> scheduled = jobs.entrySet().iterator();
			while (scheduled.hasNext()) {
				Map.Entry<Long, ScheduledTask> entry = scheduled.next();
				long executionTime = entry.getKey();
				if (start <= executionTime && executionTime <= end) {
					ScheduledTask task = entry.getValue();
					task.cancel();
					scheduled.remove();
				}
				// Don't look beyond the end range.
				if (end < executionTime) {
					break;
				}
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}
	private boolean canDispatch() {
		return isStarted() && isDispatchEnabled();
	}
	/**
	 * ���㵱ǰ�������һ��ִ��ʱ�䣬���������cron������cron����Ϊ�������û�����ã���ô���repeat�ֶΣ��Ƿ���Ҫ�ظ�ִ�У������Ҫ����ô���ڵ�ǰʱ���ϼ��������ֶε�ֵ��
	 * ������һ�ε�ִ��ʱ����
	 * @Title: calculateNextExecutionTime
	 * @Description: TODO
	 * @param job
	 * @param currentTime
	 * @param repeat
	 * @return
	 * @throws MessageFormatException
	 * @return: long
	 */
	private long calculateNextExecutionTime(InMemoryJob job, long currentTime, int repeat)
			throws MessageFormatException {
		long result = currentTime;
		String cron = job.getCronEntry();
		if (cron != null && cron.length() > 0) {
			result = CronParser.getNextScheduledTime(cron, result);
		} else if (job.getRepeat() != 0) {
			result += job.getPeriod();
		}
		return result;
	}
	/**
	 * �ַ�������ʵ���ǵ������������
	 * @Title: dispatch
	 * @Description: TODO
	 * @param job
	 * @throws IllegalStateException
	 * @throws IOException
	 * @return: void
	 */
	private void dispatch(InMemoryJob job) throws IllegalStateException, IOException {
		// �Ƿ���Էַ�����������Ծͻ�������������
		if (canDispatch()) {
			LOG.debug("Firing: {}", job);
			for (JobListener l : jobListeners) {
				l.scheduledJob(job.getJobId(), new ByteSequence(job.getPayload()));
			}
		}
	}

	/*
	 * A TimerTask instance that can aggregate the execution of a number scheduled Jobs and handle
	 * rescheduling the jobs that require it.
	 */
	/**
	 * ��ʾ��ʱ���񼯺ϣ���ʱ����������Ķ�����ִ��
	 * @ClassName: ScheduledTask
	 * @Description: TODO
	 * @author: ����
	 * @date: 2016��7��11�� ����9:18:07
	 */
	private class ScheduledTask extends TimerTask {
		// �洢����ͬһʱ����Ҫִ�е���������key������id
		private final Map<String, InMemoryJob> jobs = new TreeMap<String, InMemoryJob>();
		private final long executionTime;

		public ScheduledTask(long executionTime) {
			this.executionTime = executionTime;
		}
		/**
		 * ������һ�ε�ִ��ʱ��
		 * @Title: getExecutionTime
		 * @Description: TODO
		 * @return
		 * @return: long
		 */
		public long getExecutionTime() {
			return executionTime;
		}
		/**
		 * @return a Collection containing all the managed jobs for this task.
		 */
		public Collection<InMemoryJob> getAllJobs() {
			return new ArrayList<InMemoryJob>(jobs.values());
		}
		/**
		 * @return true if the internal list of jobs has become empty.
		 */
		public boolean isEmpty() {
			return jobs.isEmpty();
		}
		/**
		 * Adds the job to the internal list of scheduled Jobs managed by this task.
		 * @param newJob the new job to add to the list of Jobs.
		 */
		/**
		 * ����µ�����
		 * @Title: add
		 * @Description: TODO
		 * @param newJob
		 * @return: void
		 */
		public void add(InMemoryJob newJob) {
			this.jobs.put(newJob.getJobId(), newJob);
		}
		/**
		 * Removes the job from the internal list of scheduled Jobs managed by this task.
		 * @param jobId the job ID to remove from the list of Jobs.
		 * @return true if the job was removed from the list of managed jobs.
		 */
		public boolean remove(String jobId) {
			return jobs.remove(jobId) != null;
		}
		@Override
		/**
		 * ��ʱ����ִ�����������������������������ⲿ�����еļ����������������Ƿ���Ҫִ�У�������ӵ��¸���ʱ����
		 */
		public void run() {
			if (!isStarted()) {
				return;
			}
			try {
				long currentTime = System.currentTimeMillis();
				lock.writeLock().lock();
				try {
					// Remove this entry as it will now fire any scheduled jobs, if new
					// jobs or rescheduled jobs land in the same time slot we want them
					// to go into a new ScheduledTask in the Timer instance.
					// ���ⲿ��������񼯺�ɾ��
					InMemoryJobScheduler.this.jobs.remove(executionTime);
				} finally {
					lock.writeLock().unlock();
				}
				long nextExecutionTime = 0;
				for (InMemoryJob job : jobs.values()) {
					// ��ʼѭ��ִ����Ҫִ�е���������
					if (!isStarted()) {
						break;
					}
					// �õ���ǰ�����ִ�д���
					int repeat = job.getRepeat();
					// ���㵱ǰ�������һ��ִ��ʱ��
					nextExecutionTime = calculateNextExecutionTime(job, currentTime, repeat);
					if (!job.isCron()) {
						// ���û��������cron����
						// �������������
						dispatch(job);
						if (repeat != 0) {
							// ����Ӧ�ü���һ���жϣ���ȷ����ǰ��Ҫִ�е��������һ��ִ��ʱ���Ƿ��Ѿ�����
							// ����Ϊ����һ��bug
							// ���������Ҫѭ�����ͽ�������ӵ�jobs������
							// Reschedule for the next time, the scheduler will take care of
							// updating the repeat counter on the update.
							doReschedule(job, nextExecutionTime);
						}
					} else {
						if (repeat == 0) {
							// ���������cron�������repeat=0�󣬲���ִ������
							// This is a non-repeating Cron entry so we can fire and forget it.
							dispatch(job);
						}
						// �����һ��ִ��ʱ���Ѿ����ڣ���ô������Ͳ���ִ����
						// �Ҿ�����ô��Ʋ����������´�ִ��ʱ�����ҲӦ��ִ�вŶ�
						if (nextExecutionTime > currentTime) {
							// Reschedule the cron job as a new event, if the cron entry signals
							// a repeat then it will be stored separately and fired as a normal
							// event with decrementing repeat.
							// ��������뵽��һ��ִ����
							doReschedule(job, nextExecutionTime);
							// ���������repeat��cron�����ô������񲻻�����ִ�У����ǵȵ�repeat����0֮�󣬲Ż�ִ�У�����repeat��Ϊ0����ô�Ὣ��������뵽repeat��timer��ʱ���У�һ��repeat=0����Щrepeat�����񽫻Ὺʼִ��
							if (repeat != 0) {
								// we have a separate schedule to run at this time
								// so the cron job is used to set of a separate schedule
								// hence we won't fire the original cron job to the
								// listeners but we do need to start a separate schedule
								// ����һ��Ψһ���ַ���id
								String jobId = ID_GENERATOR.generateId();
								ByteSequence payload = new ByteSequence(job.getPayload());
								// �����������һ�������񣬼��뵽timer��ʱ���У���ʱ�����repeat�Ѿ�����1
								schedule(jobId, payload, "", job.getDelay(), job.getPeriod(), job.getRepeat());
							}
						}
					}
				}
			} catch (Throwable e) {
				LOG.error("Error while processing scheduled job(s).", e);
			}
		}
	}
}
