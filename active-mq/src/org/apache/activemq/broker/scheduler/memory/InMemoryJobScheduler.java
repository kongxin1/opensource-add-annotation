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
 * 定时任务调度器
 * @ClassName: InMemoryJobScheduler
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月11日 下午10:27:22
 */
public class InMemoryJobScheduler implements JobScheduler {
	private static final Logger LOG = LoggerFactory.getLogger(InMemoryJobScheduler.class);
	private static final IdGenerator ID_GENERATOR = new IdGenerator();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final String name;
	// 需要执行的任务集合，key表示的是执行时间，value表示的是在key要求的时间开始执行的任务集合
	private final TreeMap<Long, ScheduledTask> jobs = new TreeMap<Long, ScheduledTask>();
	private final AtomicBoolean started = new AtomicBoolean(false);
	private final AtomicBoolean dispatchEnabled = new AtomicBoolean(false);
	private final List<JobListener> jobListeners = new CopyOnWriteArrayList<JobListener>();
	// 与每个 Timer 对象相对应的是单个后台线程，用于顺序地执行所有计时器任务，所有的计时器在同一个后台线程中运行，如果前面的计时器执行慢，会影响后面计时器的执行
	private final Timer timer = new Timer();

	public InMemoryJobScheduler(String name) {
		this.name = name;
	}
	@Override
	public String getName() throws Exception {
		return name;
	}
	/**
	 * 启动本对象，就是修改started和dispatchEnabled属性，dispatchEnabled属性表示任务可以分发，其实就是可以调用任务监听器
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
	 * 终止计时器，同时清除任务集合jobs中的所有任务
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
	 * 获取下一个任务的开始执行时间
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
	 * 获取下一个工作集合
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
	 * 获得本对象中所有需要执行的任务
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
	 * 获取所有任务开始执行时间在入参要求的范围之间的任务
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
	 * 根据cronEntry解析出当前命令下一次的执行时间，然后创建出一个InMemoryJob任务，并将该任务添加到jobs集合中
	 * @Title: doSchedule
	 * @Description: TODO
	 * @param jobId表示任务的任务号
	 * @param payload具体作用不清楚
	 * @param cronEntry表示cron命令，也就是linux中的crontab命令
	 * @param delay表示延迟多长时间执行
	 * @param period表示命令的执行周期
	 * @param repeat和period搭配
	 *            ，如果没有设置cron命令，那么如果需要重复执行，下一次执行时间就是通过period计算的，repeat会延迟设置了cron命令的任务，需要repeat
	 *            =0时，设置cron命令的任务才能执行
	 * @throws IOException
	 * @return: void
	 */
	private void doSchedule(final String jobId, final ByteSequence payload, final String cronEntry, long delay,
			long period, int repeat) throws IOException {
		long startTime = System.currentTimeMillis();
		long executionTime = 0;
		// round startTime - so we can schedule more jobs at the same time
		// 对开始时间进行近似，以便于将更多的任务安排在相同的时间执行
		startTime = (startTime / 1000) * 1000;
		if (cronEntry != null && cronEntry.length() > 0) {
			try {
				// cronEntry是按照Linux中的cron命令来写的，下面的代码可以对这个命令进行分析
				// 得到大于等于当前时间的下一个执行时间
				executionTime = CronParser.getNextScheduledTime(cronEntry, startTime);
			} catch (MessageFormatException e) {
				throw new IOException(e.getMessage());
			}
		}
		// 等于0，表示没有设置cron命令
		if (executionTime == 0) {
			// start time not set by CRON - so it it to the current time
			executionTime = startTime;
		}
		// 以延迟实行时间为主
		if (delay > 0) {
			executionTime += delay;
		} else {
			executionTime += period;
		}
		// 创建一个内存任务
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
		// 在ScheduledTask对象中增加新任务
		try {
			ScheduledTask task = jobs.get(executionTime);
			if (task == null) {
				task = new ScheduledTask(executionTime);
				task.add(newJob);
				jobs.put(task.getExecutionTime(), task);
				// 将任务加入到定时器中
				timer.schedule(task, new Date(newJob.getNextTime()));
			} else {
				task.add(newJob);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	/**
	 * 根据当前的任务的下一次执行时间，将任务添加到jobs集合中，并添加到timer定时器中
	 * @Title: doReschedule
	 * @Description: TODO
	 * @param job
	 * @param nextExecutionTime
	 * @return: void
	 */
	private void doReschedule(InMemoryJob job, long nextExecutionTime) {
		job.setNextTime(nextExecutionTime);
		// 增加执行次数
		job.incrementExecutionCount();
		// 减少循环执行的次数
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
				// 将任务加入到定时器中
				timer.schedule(task, new Date(task.getExecutionTime()));
			} else {
				task.add(job);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	/**
	 * 根据任务id将对应的任务删除
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
	 * 将开始执行时间在入参要求的范围内的任务删除
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
	 * 计算当前任务的下一次执行时间，如果设置了cron，就以cron命令为主，如果没有设置，那么检查repeat字段，是否还需要重复执行，如果需要，那么就在当前时间上加上周期字段的值，
	 * 就是下一次的执行时间了
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
	 * 分发任务其实就是调用任务监听器
	 * @Title: dispatch
	 * @Description: TODO
	 * @param job
	 * @throws IllegalStateException
	 * @throws IOException
	 * @return: void
	 */
	private void dispatch(InMemoryJob job) throws IllegalStateException, IOException {
		// 是否可以分发任务，如果可以就会调用任务监听器
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
	 * 表示定时任务集合，定时任务在下面的对象中执行
	 * @ClassName: ScheduledTask
	 * @Description: TODO
	 * @author: 孔新
	 * @date: 2016年7月11日 下午9:18:07
	 */
	private class ScheduledTask extends TimerTask {
		// 存储了在同一时间需要执行的所有任务，key是任务id
		private final Map<String, InMemoryJob> jobs = new TreeMap<String, InMemoryJob>();
		private final long executionTime;

		public ScheduledTask(long executionTime) {
			this.executionTime = executionTime;
		}
		/**
		 * 返回下一次的执行时间
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
		 * 添加新的任务
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
		 * 定时器会执行下面的这个方法，这个方法会调用外部对象中的监听器，根据任务是否还需要执行，将任务加到下个定时器中
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
					// 将外部对象的任务集合删除
					InMemoryJobScheduler.this.jobs.remove(executionTime);
				} finally {
					lock.writeLock().unlock();
				}
				long nextExecutionTime = 0;
				for (InMemoryJob job : jobs.values()) {
					// 开始循环执行需要执行的所有任务
					if (!isStarted()) {
						break;
					}
					// 得到当前任务的执行次数
					int repeat = job.getRepeat();
					// 计算当前任务的下一次执行时间
					nextExecutionTime = calculateNextExecutionTime(job, currentTime, repeat);
					if (!job.isCron()) {
						// 如果没有设置了cron命令
						// 调用任务监听器
						dispatch(job);
						if (repeat != 0) {
							// 下面应该加上一个判断，来确定当前需要执行的任务的下一个执行时间是否已经过期
							// 我认为这是一个bug
							// 如果任务还需要循环，就将任务添加到jobs集合中
							// Reschedule for the next time, the scheduler will take care of
							// updating the repeat counter on the update.
							doReschedule(job, nextExecutionTime);
						}
					} else {
						if (repeat == 0) {
							// 如果设置了cron命令，而且repeat=0后，才能执行任务
							// This is a non-repeating Cron entry so we can fire and forget it.
							dispatch(job);
						}
						// 如果下一次执行时间已经过期，那么该任务就不再执行了
						// 我觉得这么设计不合理，就算下次执行时间过期也应该执行才对
						if (nextExecutionTime > currentTime) {
							// Reschedule the cron job as a new event, if the cron entry signals
							// a repeat then it will be stored separately and fired as a normal
							// event with decrementing repeat.
							// 将任务加入到下一次执行中
							doReschedule(job, nextExecutionTime);
							// 如果设置了repeat和cron命令，那么这个任务不会立即执行，而是等到repeat等于0之后，才会执行，但是repeat不为0，那么会将该任务加入到repeat个timer定时器中，一旦repeat=0，这些repeat个任务将会开始执行
							if (repeat != 0) {
								// we have a separate schedule to run at this time
								// so the cron job is used to set of a separate schedule
								// hence we won't fire the original cron job to the
								// listeners but we do need to start a separate schedule
								// 产生一个唯一的字符串id
								String jobId = ID_GENERATOR.generateId();
								ByteSequence payload = new ByteSequence(job.getPayload());
								// 将这个任务当做一个新任务，加入到timer定时器中，此时任务的repeat已经减了1
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
