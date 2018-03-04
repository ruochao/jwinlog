package com.mocircle.jwinlog;

import com.mocircle.jwinlog.api.EventLogApi;
import com.sun.jna.platform.win32.Winevt.EVT_HANDLE;

public class EventLogFactory {

	public static EventLogManager createLocalEventLogManager() {
		return new EventLogManagerImpl(null);
	}

	public static EventLogManager createRemoteEventLogManager(String server, String user, String domain,
			String password, int authMethod) {
		EVT_HANDLE session = EventLogApi.createRemoteSession(server, user, domain, password, authMethod);
		return new EventLogManagerImpl(session);
	}

}
