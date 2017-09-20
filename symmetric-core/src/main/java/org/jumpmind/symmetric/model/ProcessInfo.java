/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 *
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.symmetric.model;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jumpmind.util.AppUtils;

public class ProcessInfo implements Serializable, Comparable<ProcessInfo>, Cloneable {

    private static final long serialVersionUID = 1L;

    public static enum ProcessStatus {
        NEW("New"), QUERYING("Querying"), EXTRACTING("Extracting"), LOADING("Loading"), TRANSFERRING("Transferring"), ACKING(
                "Acking"), PROCESSING("Processing"), OK("Ok"), ERROR("Error"), CREATING("Creating");

        private String description;

        ProcessStatus(String description) {
            this.description = description;
        }

        public ProcessStatus fromDesciption(String description) {
            for (ProcessStatus status : ProcessStatus.values()) {
                if (status.description.equals(description)) {
                    return status;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return description;
        }
    };

    private ProcessInfoKey key;

    private ProcessStatus status = ProcessStatus.NEW;

    private long currentDataCount;

    private long dataCount = -1;

    private long batchCount;

    private long currentBatchId;

    private long currentBatchCount;

    private String currentChannelId;

    private boolean threadPerChannel;

    private String currentTableName;

    private transient Thread thread;

    private Date currentBatchStartTime;

    private long currentLoadId;

    private Date startTime = new Date();

    private Date lastStatusChangeTime = new Date();

    private Map<ProcessStatus, ProcessInfo> statusHistory;

    private Map<ProcessStatus, Date> statusStartHistory;

    private Date endTime;

    private long totalDataCount = 0;

    public ProcessInfo() {
        this(new ProcessInfoKey("", "", null));
    }

    public ProcessInfo(ProcessInfoKey key) {
        this.key = key;
        thread = Thread.currentThread();
    }

    public String getSourceNodeId() {
        return this.key.getSourceNodeId();
    }

    public String getTargetNodeId() {
        return this.key.getTargetNodeId();
    }

    public ProcessType getProcessType() {
        return this.key.getProcessType();
    }

    public ProcessInfoKey getKey() {
        return key;
    }

    public void setKey(ProcessInfoKey key) {
        this.key = key;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        if (statusHistory == null) {
            statusHistory = new HashMap<ProcessStatus, ProcessInfo>();
        }
        if (statusStartHistory == null) {
            statusStartHistory = new HashMap<ProcessStatus, Date>();
        }
        if (!statusStartHistory.containsKey(this.status)) {
            statusStartHistory.put(this.status, this.startTime);
        }
        statusHistory.put(this.status, this.copy());
        statusHistory.put(status, this);

        this.status = status;

        this.lastStatusChangeTime = new Date();
        if (status == ProcessStatus.OK || status == ProcessStatus.ERROR) {
            this.endTime = new Date();
        }
    }

    public long getCurrentDataCount() {
        return currentDataCount;
    }

    public void setCurrentDataCount(long dataCount) {
        this.currentDataCount = dataCount;
    }

    public long getBatchCount() {
        return batchCount;
    }

    public void setBatchCount(long batchCount) {
        this.batchCount = batchCount;
    }

    public void incrementCurrentDataCount() {
        this.currentDataCount++;
        if (totalDataCount < currentDataCount) {
            totalDataCount = currentDataCount;
        }
    }

    public void incrementBatchCount() {
        this.batchCount++;
    }

    public void incrementCurrentBatchCount() {
        this.currentBatchCount++;
    }

    public long getCurrentBatchCount() {
        return currentBatchCount;
    }

    public void setCurrentBatchCount(long currentBatchCount) {
        this.currentBatchCount = currentBatchCount;
    }

    public long getCurrentBatchId() {
        return currentBatchId;
    }

    public void setCurrentBatchId(long currentBatchId) {
        this.currentBatchId = currentBatchId;
        this.currentBatchStartTime = new Date();
        this.currentDataCount = 0;
    }

    public void setCurrentLoadId(long loadId) {
        this.currentLoadId = loadId;
    }

    public long getCurrentLoadId() {
        return currentLoadId;
    }

    public String getCurrentChannelThread() {
        if (getKey().getChannelId() != null && getKey().getChannelId().length() > 0) {
            return getKey().getChannelId();
        }
        return "";
    }

    public String getCurrentChannelId() {
        return currentChannelId;
    }

    public void setCurrentChannelId(String currentChannelId) {
        this.currentChannelId = currentChannelId;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setCurrentTableName(String currentTableName) {
        this.currentTableName = currentTableName;
    }

    public String getCurrentTableName() {
        return currentTableName;
    }

    public Date getLastStatusChangeTime() {
        return lastStatusChangeTime;
    }

    public void setDataCount(long dataCount) {
        this.dataCount = dataCount;
    }

    public long getDataCount() {
        return dataCount;
    }

    public boolean isThreadPerChannel() {
        return threadPerChannel;
    }

    public void setThreadPerChannel(boolean threadPerChannel) {
        this.threadPerChannel = threadPerChannel;
    }

    public Date getCurrentBatchStartTime() {
        if (currentBatchStartTime == null) {
            return startTime;
        } else {
            return currentBatchStartTime;
        }
    }

    public void setCurrentBatchStartTime(Date currentBatchStartTime) {
        this.currentBatchStartTime = currentBatchStartTime;
    }

    public Map<ProcessStatus, ProcessInfo> getStatusHistory() {
        return this.statusHistory;
    }

    public void setStatusHistory(Map<ProcessStatus, ProcessInfo> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public void setStatusStartHistory(Map<ProcessStatus, Date> statusStartHistory) {
        this.statusStartHistory = statusStartHistory;
    }

    public Map<ProcessStatus, Date> getStatusStartHistory() {
        return this.statusStartHistory;
    }

    public ProcessInfo getStatusHistory(ProcessStatus status) {
        return this.statusHistory == null ? null : this.statusHistory.get(status);
    }

    public Date getStatusStartHistory(ProcessStatus status) {
        return this.statusStartHistory == null ? null : this.statusStartHistory.get(status);
    }

    @Override
    public String toString() {
        return String.format("%s,status=%s,startTime=%s", key.toString(), status.toString(), startTime.toString());
    }

    public String showInError(String identityNodeId) {
        if (status == ProcessStatus.ERROR) {
            switch (key.getProcessType()) {
                case PUSH_JOB_EXTRACT:
                case PUSH_JOB_TRANSFER:
                case PULL_HANDLER_EXTRACT:
                case PULL_HANDLER_TRANSFER:                    
                    return key.getTargetNodeId();
                case PULL_JOB_LOAD:
                case PULL_JOB_TRANSFER:
                case PUSH_HANDLER_LOAD:
                case PUSH_HANDLER_TRANSFER:
                case ROUTER_JOB:
                case ROUTER_READER:
                case GAP_DETECT:                    
                    return key.getSourceNodeId();
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public int compareTo(ProcessInfo o) {
        if (status == ProcessStatus.ERROR && o.status != ProcessStatus.ERROR) {
            return -1;
        } else if (o.status == ProcessStatus.ERROR && status != ProcessStatus.ERROR) {
            return 1;
        } else if (status != ProcessStatus.OK && o.status == ProcessStatus.OK) {
            return -1;
        } else if (o.status != ProcessStatus.OK && status == ProcessStatus.OK) {
            return 1;
        } else {
            return o.startTime.compareTo(startTime);
        }
    }

    public ThreadData getThreadData() {
        if (thread != null && thread.isAlive()) {
            return getThreadData(thread.getId());
        } else {
            return null;
        }
    }

    public static ThreadData getThreadData(long threadId) {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        ThreadInfo info = threadBean.getThreadInfo(threadId, 100);
        if (info != null) {
            String threadName = info.getThreadName();
            return new ThreadData(threadName, AppUtils.formatStackTrace(info.getStackTrace()));
        } else {
            return null;
        }
    }

    public long getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(long totalDataCount) {
        this.totalDataCount = totalDataCount;
    }

    static public class ThreadData {

        public ThreadData(String threadName, String stackTrace) {
            this.threadName = threadName;
            this.stackTrace = stackTrace;
        }

        private String threadName;
        private String stackTrace;

        public String getStackTrace() {
            return stackTrace;
        }

        public String getThreadName() {
            return threadName;
        }
    }

    public ProcessInfo copy() {
        try {
            return (ProcessInfo) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
