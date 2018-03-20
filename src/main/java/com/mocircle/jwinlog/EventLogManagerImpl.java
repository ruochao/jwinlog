package com.mocircle.jwinlog;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mocircle.jwinlog.api.EventLogApi;
import com.mocircle.jwinlog.api.RenderApi;
import com.mocircle.jwinlog.model.LogInfo;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.Winevt;
import com.sun.jna.platform.win32.Winevt.EVT_HANDLE;

class EventLogManagerImpl implements EventLogManager {

	private static final Logger logger = LoggerFactory.getLogger(EventLogManagerImpl.class);

	private static final int DEFAULT_ITERATOR_CACHE_SIZE = 512;

	private EVT_HANDLE session;

	public EventLogManagerImpl(EVT_HANDLE session) {
		this.session = session;
	}

	@Override
	public String[] getChannels() {
		return EventLogApi.getChannels(session);
	}

	@Override
	public String[] getPublishers() {
		return EventLogApi.getPublishers(session);
	}

	@Override
	public LogInfo getLogInfo(String channelName) {
		return EventLogApi.getLogInfo(session, channelName);
	}

	@Override
	public EventEntry[] getEventLogs(String channelName, long lastRecordId, int maxCount, RenderOption option) {
		EVT_HANDLE bookmark = EventLogApi.createBookmark(channelName, lastRecordId);
		EVT_HANDLE subscriptionHandle = EventLogApi.subscribeEvent(session, bookmark, channelName,
				Winevt.EVT_SUBSCRIBE_FLAGS.EvtSubscribeStartAfterBookmark);

		EVT_HANDLE[] eventHandles = EventLogApi.handleEvent(subscriptionHandle, maxCount);
		if (eventHandles == null) {
			return null;
		}
		List<EventEntry> events = new ArrayList<>();
		for (EVT_HANDLE eventHandle : eventHandles) {
			try {
				EventEntry event = RenderApi.render(session, eventHandle, option);
				events.add(event);
			} catch (Win32Exception e) {
				logger.warn("Cannot render event", e);
			} finally {
				EventLogApi.closeHandle(eventHandle);
			}
		}

		// TODO better close in finally
		EventLogApi.closeHandle(bookmark);
		EventLogApi.closeHandle(subscriptionHandle);
		return events.toArray(new EventEntry[events.size()]);
	}

	@Override
	public EventEntryIterator retrieveEventLogs(String channelName, int retrieveMode, Long lastRecordId,
			RenderOption option, int cacheSize) {
		return new EventEntryIterator(session, channelName, retrieveMode, lastRecordId, option, cacheSize);
	}

	@Override
	public EventEntryIterator retrieveEventLogs(String channelName, int retrieveMode, Long lastRecordId,
			RenderOption option) {
		return new EventEntryIterator(session, channelName, retrieveMode, lastRecordId, option,
				DEFAULT_ITERATOR_CACHE_SIZE);
	}

	@Override
	public EventEntryIterator retrieveEventLogsFromRecordId(String channelName, long lastRecordId,
			RenderOption option) {
		return retrieveEventLogs(channelName, EventRetrieveMode.AFTER_RECORD_ID, lastRecordId, option,
				DEFAULT_ITERATOR_CACHE_SIZE);
	}

	@Override
	public EventEntryIterator retrieveEventLogsFromOldestRecord(String channelName, RenderOption option) {
		return retrieveEventLogs(channelName, EventRetrieveMode.FROM_OLDEST, null, option, DEFAULT_ITERATOR_CACHE_SIZE);
	}

	@Override
	public EventEntryIterator retrieveEventLogsFromFuture(String channelName, RenderOption option) {
		return retrieveEventLogs(channelName, EventRetrieveMode.FROM_FUTURE, null, option, DEFAULT_ITERATOR_CACHE_SIZE);
	}

}
