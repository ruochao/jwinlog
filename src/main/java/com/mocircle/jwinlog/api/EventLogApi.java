package com.mocircle.jwinlog.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mocircle.jwinlog.model.LogInfo;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Wevtapi;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.Winevt;
import com.sun.jna.platform.win32.Winevt.EVT_HANDLE;
import com.sun.jna.platform.win32.Winevt.EVT_RPC_LOGIN;
import com.sun.jna.ptr.IntByReference;

public final class EventLogApi {

	private static final Logger logger = LoggerFactory.getLogger(EventLogApi.class);

	private static final String TEMPL_BOOKMARK = "<BookmarkList><Bookmark Channel=\"%s\" RecordId=\"%d\" IsCurrent=\"True\"/></BookmarkList>";
	private static final String XPATH_PROVIDER_NAME = "Event/System/Provider/@Name";
	private static final String XPATH_RECORD_ID = "Event/System/EventRecordID";

	private static final int BUFFER_CHANNEL = 512;
	private static final int BUFFER_PUBLISHER = 512;
	private static final int BUFFER_EVENT = 1 << 14;
	private static final int BUFFER_PROVIDER_NAME = 512;
	private static final int BUFFER_RECORD_ID = 64;
	private static final int BUFFER_LOG_PROPERTY = 512;

	private EventLogApi() {
	}

	public static void closeHandle(EVT_HANDLE handle) {
		if (handle != null) {
			if (!Wevtapi.INSTANCE.EvtClose(handle)) {
				throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
			}
		}
	}

	public static EVT_HANDLE createRemoteSession(String server, String user, String domain, String password,
			int authMethod) throws Win32Exception {
		EVT_RPC_LOGIN login = new EVT_RPC_LOGIN(server, user, domain, password, authMethod);
		EVT_HANDLE handle = Wevtapi.INSTANCE.EvtOpenSession(Winevt.EVT_LOGIN_CLASS.EvtRpcLogin, login, 0, 0);
		if (handle == null) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}
		return handle;
	}

	public static String[] getChannels(EVT_HANDLE session) throws Win32Exception {
		EVT_HANDLE handle = Wevtapi.INSTANCE.EvtOpenChannelEnum(session, 0);
		if (handle == null) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}

		List<String> channelNames = new ArrayList<>();
		try {
			char[] buffer = new char[BUFFER_CHANNEL];
			IntByReference bufferUsed = new IntByReference();
			while (true) {
				boolean result = Wevtapi.INSTANCE.EvtNextChannelPath(handle, buffer.length, buffer, bufferUsed);
				if (!result) {
					int error = Kernel32.INSTANCE.GetLastError();
					if (error == WinNT.ERROR_INSUFFICIENT_BUFFER) {
						logger.debug("Insufficient buffer [channel name]: " + buffer.length + ", expected: "
								+ bufferUsed.getValue());
						buffer = new char[buffer.length * 2];
						continue;
					} else if (error == WinNT.ERROR_NO_MORE_ITEMS) {
						break;
					} else {
						throw new Win32Exception(error);
					}
				} else {
					channelNames.add(Native.toString(buffer));
				}
			}
		} finally {
			closeHandle(handle);
		}
		return channelNames.toArray(new String[channelNames.size()]);
	}

	public static String[] getPublishers(EVT_HANDLE session) {
		EVT_HANDLE publisherHandle = Wevtapi.INSTANCE.EvtOpenPublisherEnum(null, 0);
		if (publisherHandle == null) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}

		List<String> publishers = new ArrayList<>();
		try {
			char[] buffer = new char[BUFFER_PUBLISHER];
			IntByReference bufferUsed = new IntByReference();
			while (true) {
				boolean result = Wevtapi.INSTANCE.EvtNextPublisherId(publisherHandle, buffer.length, buffer,
						bufferUsed);
				if (!result) {
					int error = Kernel32.INSTANCE.GetLastError();
					if (error == WinNT.ERROR_INSUFFICIENT_BUFFER) {
						logger.debug("Insufficient buffer [publisher]: " + buffer.length + ", expected: "
								+ bufferUsed.getValue());
						buffer = new char[buffer.length * 2];
						continue;
					} else if (error == WinNT.ERROR_NO_MORE_ITEMS) {
						break;
					} else {
						throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
					}
				} else {
					publishers.add(Native.toString(buffer));
				}
			}
		} finally {
			closeHandle(publisherHandle);
		}
		return publishers.toArray(new String[publishers.size()]);
	}

	public static EVT_HANDLE createBookmark(String channelName, long recordId) {
		String bookmarkXml = String.format(TEMPL_BOOKMARK, channelName, recordId);
		EVT_HANDLE handle = Wevtapi.INSTANCE.EvtCreateBookmark(bookmarkXml);
		if (handle == null) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}
		return handle;
	}

	public static EVT_HANDLE subscribeEvent(EVT_HANDLE session, EVT_HANDLE bookmark, String channelPath, int flag)
			throws Win32Exception {
		// Signal event
		HANDLE event = Kernel32.INSTANCE.CreateEvent(null, false, false, null);
		if (event == null) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}
		EVT_HANDLE signalEvent = new EVT_HANDLE(event.getPointer());

		// Create subscription
		EVT_HANDLE handle = Wevtapi.INSTANCE.EvtSubscribe(session, signalEvent, channelPath, null, bookmark, null, null,
				flag);
		if (handle == null) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}
		return handle;
	}

	public static EVT_HANDLE[] handleEvent(EVT_HANDLE subscriptionHandle, int maxEvent) {
		EVT_HANDLE[] eventHandles = new EVT_HANDLE[maxEvent];
		IntByReference handleUsed = new IntByReference();
		boolean result = Wevtapi.INSTANCE.EvtNext(subscriptionHandle, maxEvent, eventHandles, WinNT.INFINITE, 0,
				handleUsed);
		if (!result) {
			int error = Kernel32.INSTANCE.GetLastError();
			if (error == WinNT.ERROR_NO_MORE_ITEMS) {
				return null;
			} else {
				throw new Win32Exception(error);
			}
		}
		return Arrays.copyOfRange(eventHandles, 0, handleUsed.getValue());
	}

	public static long getEventRecordId(EVT_HANDLE eventHandle) {
		Object[] values = renderEventProperties(eventHandle, new String[] { XPATH_RECORD_ID }, BUFFER_RECORD_ID);
		return (long) values[0];
	}

	public static String renderEventXml(EVT_HANDLE eventHandle) {
		Memory buffer = new Memory(BUFFER_EVENT);
		IntByReference bufferUsed = new IntByReference();
		boolean result = Wevtapi.INSTANCE.EvtRender(null, eventHandle, Winevt.EVT_RENDER_FLAGS.EvtRenderEventXml,
				(int) buffer.size(), buffer, bufferUsed, new IntByReference());
		if (!result) {
			int error = Kernel32.INSTANCE.GetLastError();
			if (error == WinNT.ERROR_INSUFFICIENT_BUFFER) {
				logger.debug("Insufficient buffer [render event]: " + buffer.size() + ", expected: "
						+ bufferUsed.getValue());
				buffer = new Memory(bufferUsed.getValue());
				result = Wevtapi.INSTANCE.EvtRender(null, eventHandle, Winevt.EVT_RENDER_FLAGS.EvtRenderEventXml,
						(int) buffer.size(), buffer, bufferUsed, new IntByReference());
				if (!result) {
					throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
				}
			} else {
				throw new Win32Exception(error);
			}
		}
		return NativeUtils.getUnicodeStringFromMemory(buffer, bufferUsed.getValue());
	}

	public static String formatEventXml(EVT_HANDLE session, EVT_HANDLE eventHandle, int locale) {
		String providerName = getProviderName(eventHandle);
		EVT_HANDLE publisherHandle = Wevtapi.INSTANCE.EvtOpenPublisherMetadata(session, providerName, null, locale, 0);
		if (publisherHandle == null) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}

		try {
			char[] buffer = new char[BUFFER_EVENT];
			IntByReference bufferUsed = new IntByReference();
			boolean result = Wevtapi.INSTANCE.EvtFormatMessage(publisherHandle, eventHandle, 0, 0, null,
					Winevt.EVT_FORMAT_MESSAGE_FLAGS.EvtFormatMessageXml, buffer.length, buffer, bufferUsed);
			if (!result) {
				int error = Kernel32.INSTANCE.GetLastError();
				if (error == WinNT.ERROR_INSUFFICIENT_BUFFER) {
					logger.debug("Insufficient buffer [format event]: " + buffer.length + ", expected: "
							+ bufferUsed.getValue());
					buffer = new char[bufferUsed.getValue()];
					result = Wevtapi.INSTANCE.EvtFormatMessage(publisherHandle, eventHandle, 0, 0, null,
							Winevt.EVT_FORMAT_MESSAGE_FLAGS.EvtFormatMessageXml, buffer.length, buffer, bufferUsed);
					if (!result) {
						throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
					}
				} else {
					throw new Win32Exception(error);
				}
			}
			return Native.toString(buffer);
		} finally {
			closeHandle(publisherHandle);
		}
	}

	public static String getProviderName(EVT_HANDLE eventHandle) {
		Object[] values = renderEventProperties(eventHandle, new String[] { XPATH_PROVIDER_NAME },
				BUFFER_PROVIDER_NAME);
		return values[0].toString();
	}

	public static Object[] renderEventProperties(EVT_HANDLE eventHandle, String[] propertyXPaths, int bufferSize) {
		EVT_HANDLE renderContext = Wevtapi.INSTANCE.EvtCreateRenderContext(propertyXPaths.length, propertyXPaths,
				Winevt.EVT_RENDER_CONTEXT_FLAGS.EvtRenderContextValues);
		if (renderContext == null) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}

		Memory buffer = new Memory(bufferSize);
		IntByReference bufferUsed = new IntByReference();
		IntByReference propCount = new IntByReference();
		try {
			boolean result = Wevtapi.INSTANCE.EvtRender(renderContext, eventHandle,
					Winevt.EVT_RENDER_FLAGS.EvtRenderEventValues, (int) buffer.size(), buffer, bufferUsed, propCount);
			if (!result) {
				int error = Kernel32.INSTANCE.GetLastError();
				if (error == WinNT.ERROR_INSUFFICIENT_BUFFER) {
					logger.debug("Insufficient buffer [event properties]: " + buffer.size() + ", expected: "
							+ bufferUsed.getValue());
					buffer = new Memory(bufferUsed.getValue());
					result = Wevtapi.INSTANCE.EvtRender(renderContext, eventHandle,
							Winevt.EVT_RENDER_FLAGS.EvtRenderEventValues, (int) buffer.size(), buffer, bufferUsed,
							propCount);
					if (!result) {
						throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
					}
				} else {
					throw new Win32Exception(error);
				}
			}
			return NativeUtils.getVariantsFromMemory(buffer, propCount.getValue());
		} finally {
			closeHandle(renderContext);
		}
	}

	public static LogInfo getLogInfo(EVT_HANDLE session, String channelName) {
		EVT_HANDLE handle = Wevtapi.INSTANCE.EvtOpenLog(session, channelName,
				Winevt.EVT_OPEN_LOG_FLAGS.EvtOpenChannelPath);
		if (handle == null) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}
		LogInfo logInfo = new LogInfo();
		try {

			FILETIME creationTime = (FILETIME) getLogInfoItem(handle, Winevt.EVT_LOG_PROPERTY_ID.EvtLogCreationTime);
			logInfo.setCreationTime(creationTime.toDate());

			FILETIME lastAccessTime = (FILETIME) getLogInfoItem(handle,
					Winevt.EVT_LOG_PROPERTY_ID.EvtLogLastAccessTime);
			logInfo.setLastAccessTime(lastAccessTime.toDate());

			FILETIME lastWriteTime = (FILETIME) getLogInfoItem(handle, Winevt.EVT_LOG_PROPERTY_ID.EvtLogLastWriteTime);
			logInfo.setLastWriteTime(lastWriteTime.toDate());

			long fileSize = (Long) getLogInfoItem(handle, Winevt.EVT_LOG_PROPERTY_ID.EvtLogFileSize);
			logInfo.setFileSize(fileSize);

			Integer logAttributes = (Integer) getLogInfoItem(handle, Winevt.EVT_LOG_PROPERTY_ID.EvtLogAttributes);
			logInfo.setLogAttributes(logAttributes);

			Long totalRecordNumber = (Long) getLogInfoItem(handle, Winevt.EVT_LOG_PROPERTY_ID.EvtLogNumberOfLogRecords);
			logInfo.setTotalRecordNumber(totalRecordNumber);

			Long oldestRecordNumber = (Long) getLogInfoItem(handle,
					Winevt.EVT_LOG_PROPERTY_ID.EvtLogOldestRecordNumber);
			logInfo.setOldestRecordNumber(oldestRecordNumber);

			BOOL isFull = (BOOL) getLogInfoItem(handle, Winevt.EVT_LOG_PROPERTY_ID.EvtLogFull);
			logInfo.setFull(isFull.booleanValue());

		} finally {
			closeHandle(handle);
		}
		return logInfo;
	}

	private static Object getLogInfoItem(EVT_HANDLE handle, int propertyId) {
		Memory buff = new Memory(BUFFER_LOG_PROPERTY);
		IntByReference used = new IntByReference();
		boolean result = Wevtapi.INSTANCE.EvtGetLogInfo(handle, propertyId, BUFFER_LOG_PROPERTY, buff, used);
		if (!result) {
			int error = Kernel32.INSTANCE.GetLastError();
			if (error == WinNT.ERROR_INSUFFICIENT_BUFFER) {
				buff = new Memory(used.getValue());
				result = Wevtapi.INSTANCE.EvtGetLogInfo(handle, propertyId, (int) buff.size(), buff, used);
				if (!result) {
					throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
				}
			} else {
				throw new Win32Exception(error);
			}
		}
		Object[] values = NativeUtils.getVariantsFromMemory(buff, 1);
		return values[0];
	}

}
