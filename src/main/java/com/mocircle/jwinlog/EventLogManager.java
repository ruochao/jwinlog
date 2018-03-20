package com.mocircle.jwinlog;

import com.mocircle.jwinlog.model.LogInfo;

public interface EventLogManager {

	String[] getChannels();

	String[] getPublishers();

	LogInfo getLogInfo(String channelName);

	EventEntry[] getEventLogs(String channelName, long lastRecordId, int maxCount, RenderOption option);

	EventEntryIterator retrieveEventLogs(String channelName, int retrieveMode, Long lastRecordId, RenderOption option,
			int cacheSize);

	EventEntryIterator retrieveEventLogs(String channelName, int retrieveMode, Long lastRecordId, RenderOption option);

	EventEntryIterator retrieveEventLogsFromRecordId(String channelName, long lastRecordId, RenderOption option);

	EventEntryIterator retrieveEventLogsFromOldestRecord(String channelName, RenderOption option);

	EventEntryIterator retrieveEventLogsFromFuture(String channelName, RenderOption option);

}
