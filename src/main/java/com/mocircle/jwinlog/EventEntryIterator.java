package com.mocircle.jwinlog;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mocircle.jwinlog.api.EventLogApi;
import com.mocircle.jwinlog.api.RenderApi;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.Winevt.EVT_HANDLE;

public class EventEntryIterator implements Iterator<EventEntry> {

	private static final Logger logger = LoggerFactory.getLogger(EventEntryIterator.class);

	private EVT_HANDLE session;
	private String channelName;
	private int retrieveMode;
	private Long lastRecordId;
	private int cacheSize;
	private RenderOption option;

	private Queue<EventEntry> cacheQueue = new LinkedList<>();

	public EventEntryIterator(EVT_HANDLE session, String channelName, int retrieveMode, Long lastRecordId,
			RenderOption option, int cacheSize) {
		this.session = session;
		this.channelName = channelName;
		this.retrieveMode = retrieveMode;
		this.lastRecordId = lastRecordId;
		this.option = option;
		this.cacheSize = cacheSize;

		// Reset last record id
		if (retrieveMode != EventRetrieveMode.AFTER_RECORD_ID) {
			this.lastRecordId = null;
		}
	}

	public String getChannelName() {
		return channelName;
	}

	public int getRetrieveMode() {
		return retrieveMode;
	}

	public Long getLastRecordId() {
		return lastRecordId;
	}

	public RenderOption getRenderOption() {
		return option;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	@Override
	public boolean hasNext() {
		if (!cacheQueue.isEmpty()) {
			return true;
		} else {
			return fillEvents();
		}
	}

	@Override
	public EventEntry next() {
		if (!cacheQueue.isEmpty()) {
			EventEntry lastEvent = cacheQueue.poll();
			if (lastEvent != null) {
				lastRecordId = lastEvent.getRecordId();
			}
			return lastEvent;
		} else {
			if (fillEvents()) {
				return cacheQueue.poll();
			} else {
				return null;
			}
		}
	}

	// Return true if finds any event put into queue
	private boolean fillEvents() {
		int flag = lastRecordId == null ? retrieveMode : EventRetrieveMode.AFTER_RECORD_ID;
		EVT_HANDLE bookmark = null;
		if (lastRecordId != null) {
			bookmark = EventLogApi.createBookmark(channelName, lastRecordId);
		}
		EVT_HANDLE subscription = EventLogApi.subscribeEvent(session, bookmark, channelName, flag);

		EVT_HANDLE[] eventHandles = EventLogApi.handleEvent(subscription, cacheSize);
		EventLogApi.closeHandle(bookmark);
		EventLogApi.closeHandle(subscription);
		if (eventHandles == null || eventHandles.length == 0) {
			return false;
		}
		int fillEvents = 0;
		for (EVT_HANDLE eventHandle : eventHandles) {
			try {
				EventEntry event = RenderApi.render(session, eventHandle, option);
				cacheQueue.add(event);
				fillEvents++;
			} catch (Win32Exception e) {
				logger.warn("Cannot render event", e);
			} finally {
				EventLogApi.closeHandle(eventHandle);
			}
		}
		return fillEvents > 0;
	}
}
