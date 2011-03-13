/**
 *     Aedict - an EDICT browser for Android
 Copyright (C) 2009 Martin Vysny
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package sk.baka.aedict.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.util.Log;

/**
 * Manages background services.
 * 
 * @author Martin Vysny
 */
public class BackgroundService implements Closeable {
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final ConcurrentMap<String, Object> runningOrScheduled = new ConcurrentHashMap<String, Object>();
	private final ConcurrentMap<String, Status> taskStatus = new ConcurrentHashMap<String, Status>();
	private static final Object OBJ = new Object();
	public static enum StatusEnum {
		NotScheduled,
		Scheduled,
		Running,
		Finished;
	}
	public static class Status {
		public final StatusEnum status;
		public final Throwable error;
		public Status(final StatusEnum status, Throwable error) {
			this.status = status;
			this.error = error;
		}
	}
	/**
	 * Returns status of a task identified by a key.
	 * @param key the task key.
	 * @return null if the task was not yet run 
	 */
	public Status getStatus(final String key) {
		final Status result = taskStatus.get(key);
		if(result==null){
			return new Status(StatusEnum.NotScheduled, null);
		}
		return result;
	}
	/**
	 * Schedules given task for execution.
	 * @param key the task key. The scheduler will reject to schedule task with a key if any task with the same key is already scheduled for execution, or being executed.
	 * @param r the task to run.
	 */
	public void schedule(final String key, final Callable<Void> r) {
		if (runningOrScheduled.putIfAbsent(key, OBJ) == null) {
			taskStatus.put(key, new Status(StatusEnum.Scheduled, null));
			executor.submit(new Runnable() {

				@Override
				public void run() {
					taskStatus.put(key, new Status(StatusEnum.Running, null));
					try {
						r.call();
						taskStatus.put(key, new Status(StatusEnum.Finished, null));
					} catch (Throwable t) {
						Log.e("BackgroundService", "Task " + key + " failed", t);
						taskStatus.put(key, new Status(StatusEnum.Finished, t));
					}
					runningOrScheduled.remove(key);
				}
			});
		}
	}

	@Override
	public void close() throws IOException {
		executor.shutdownNow();
		try {
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new IOExceptionWithCause("Interrupted while waiting for thread termination", e);
		}
	}
}
