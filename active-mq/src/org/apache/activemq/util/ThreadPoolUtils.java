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
package org.apache.activemq.util;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for working with thread pools {@link ExecutorService}.
 */
public final class ThreadPoolUtils {
	private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolUtils.class);
	public static final long DEFAULT_SHUTDOWN_AWAIT_TERMINATION = 10 * 1000L;

	/**
	 * Shutdown the given executor service only (ie not graceful shutdown).
	 * @see java.util.concurrent.ExecutorService#shutdown()
	 */
	public static void shutdown(ExecutorService executorService) {
		doShutdown(executorService, 0);
	}
	/**
	 * Shutdown now the given executor service aggressively.
	 * @param executorService the executor service to shutdown now
	 * @return list of tasks that never commenced execution
	 * @see java.util.concurrent.ExecutorService#shutdownNow()
	 */
	public static List<Runnable> shutdownNow(ExecutorService executorService) {
		List<Runnable> answer = null;
		if (!executorService.isShutdown()) {
			LOG.debug("Forcing shutdown of ExecutorService: {}", executorService);
			answer = executorService.shutdownNow();
			if (LOG.isTraceEnabled()) {
				LOG.trace("Shutdown of ExecutorService: {} is shutdown: {} and terminated: {}.", new Object[] {
						executorService, executorService.isShutdown(), executorService.isTerminated() });
			}
		}
		return answer;
	}
	/**
	 * Shutdown the given executor service graceful at first, and then aggressively if the await
	 * termination timeout was hit.
	 * <p/>
	 * This implementation invokes the
	 * {@link #shutdownGraceful(java.util.concurrent.ExecutorService, long)} with a timeout value of
	 * {@link #DEFAULT_SHUTDOWN_AWAIT_TERMINATION} millis.
	 */
	public static void shutdownGraceful(ExecutorService executorService) {
		doShutdown(executorService, DEFAULT_SHUTDOWN_AWAIT_TERMINATION);
	}
	/**
	 * Shutdown the given executor service graceful at first, and then aggressively if the await
	 * termination timeout was hit.
	 * <p/>
	 * Will try to perform an orderly shutdown by giving the running threads time to complete tasks,
	 * before going more aggressively by doing a
	 * {@link #shutdownNow(java.util.concurrent.ExecutorService)} which forces a shutdown. The
	 * parameter <tt>shutdownAwaitTermination</tt> is used as timeout value waiting for orderly
	 * shutdown to complete normally, before going aggressively.
	 * @param executorService the executor service to shutdown
	 * @param shutdownAwaitTermination timeout in millis to wait for orderly shutdown
	 */
	/**
	 * 关闭指定的线程池，如果在规定时间内未关闭，就强制关闭
	 * @Title: shutdownGraceful
	 * @Description: TODO
	 * @param executorService
	 * @param shutdownAwaitTermination
	 * @return: void
	 */
	public static void shutdownGraceful(ExecutorService executorService, long shutdownAwaitTermination) {
		doShutdown(executorService, shutdownAwaitTermination);
	}
	/**
	 * 关闭指定的线程池，如果在规定时间内未关闭，就强制关闭
	 * @Title: doShutdown
	 * @Description: TODO
	 * @param executorService
	 * @param shutdownAwaitTermination
	 * @return: void
	 */
	private static void doShutdown(ExecutorService executorService, long shutdownAwaitTermination) {
		// code from Apache Camel - org.apache.camel.impl.DefaultExecutorServiceManager
		if (executorService == null) {
			return;
		}
		// shutting down a thread pool is a 2 step process. First we try graceful, and if that
		// fails, then we go more aggressively
		// and try shutting down again. In both cases we wait at most the given shutdown timeout
		// value given
		// (total wait could then be 2 x shutdownAwaitTermination, but when we shutdown the 2nd time
		// we are aggressive and thus
		// we ought to shutdown much faster)
		if (!executorService.isShutdown()) {
			boolean warned = false;
			StopWatch watch = new StopWatch();
			LOG.trace("Shutdown of ExecutorService: {} with await termination: {} millis", executorService,
					shutdownAwaitTermination);
			// 下面这行代码表示线程池不再接受新的线程
			executorService.shutdown();
			if (shutdownAwaitTermination > 0) {
				try {
					// 等待线程关闭
					if (!awaitTermination(executorService, shutdownAwaitTermination)) {
						// 线程未关闭
						warned = true;
						LOG.warn("Forcing shutdown of ExecutorService: {} due first await termination elapsed.",
								executorService);
						// 强制关闭线程
						executorService.shutdownNow();
						// we are now shutting down aggressively, so wait to see if we can
						// completely shutdown or not
						// 再检测一次线程池是否已经关闭
						if (!awaitTermination(executorService, shutdownAwaitTermination)) {
							LOG.warn(
									"Cannot completely force shutdown of ExecutorService: {} due second await termination elapsed.",
									executorService);
						}
					}
				} catch (InterruptedException e) {
					warned = true;
					LOG.warn("Forcing shutdown of ExecutorService: {} due interrupted.", executorService);
					// we were interrupted during shutdown, so force shutdown
					try {
						executorService.shutdownNow();
					} finally {
						Thread.currentThread().interrupt();
					}
				}
			}
			// if we logged at WARN level, then report at INFO level when we are complete so the end
			// user can see this in the log
			if (warned) {
				LOG.info("Shutdown of ExecutorService: {} is shutdown: {} and terminated: {} took: {}.",
						new Object[] { executorService, executorService.isShutdown(), executorService.isTerminated(),
								TimeUtils.printDuration(watch.taken()) });
			} else if (LOG.isDebugEnabled()) {
				LOG.debug("Shutdown of ExecutorService: {} is shutdown: {} and terminated: {} took: {}.",
						new Object[] { executorService, executorService.isShutdown(), executorService.isTerminated(),
								TimeUtils.printDuration(watch.taken()) });
			}
		}
	}
	/**
	 * Awaits the termination of the thread pool.
	 * <p/>
	 * This implementation will log every 2nd second at INFO level that we are waiting, so the end
	 * user can see we are not hanging in case it takes longer time to terminate the pool.
	 * @param executorService the thread pool
	 * @param shutdownAwaitTermination time in millis to use as timeout
	 * @return <tt>true</tt> if the pool is terminated, or <tt>false</tt> if we timed out
	 * @throws InterruptedException is thrown if we are interrupted during the waiting
	 */
	/**
	 * 不断检测线程池是否已经关闭，直至超时
	 * @Title: awaitTermination
	 * @Description: TODO
	 * @param executorService
	 * @param shutdownAwaitTermination
	 * @return
	 * @throws InterruptedException
	 * @return: boolean
	 */
	public static boolean awaitTermination(ExecutorService executorService, long shutdownAwaitTermination)
			throws InterruptedException {
		// log progress every 5th second so end user is aware of we are shutting down
		// StopWatch中会记录开始时间，也即是创建该对象的时间
		StopWatch watch = new StopWatch();
		long interval = Math.min(2000, shutdownAwaitTermination);
		boolean done = false;
		while (!done && interval > 0) {
			// awaitTermination会不断的检测线程池是否已经关闭，只是检测
			if (executorService.awaitTermination(interval, TimeUnit.MILLISECONDS)) {
				done = true;
			} else {
				LOG.info("Waited {} for ExecutorService: {} to terminate...", TimeUtils.printDuration(watch.taken()),
						executorService);
				// recalculate interval
				// 等待interval长度的时间后再检测一次线程池是否已经关闭
				interval = Math.min(2000, shutdownAwaitTermination - watch.taken());
			}
		}
		return done;
	}
}
