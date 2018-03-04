package com.mocircle.jwinlog;

import com.sun.jna.platform.win32.Winevt;

public interface EventRetrieveMode {

	int FROM_OLDEST = Winevt.EVT_SUBSCRIBE_FLAGS.EvtSubscribeStartAtOldestRecord;

	int AFTER_RECORD_ID = Winevt.EVT_SUBSCRIBE_FLAGS.EvtSubscribeStartAfterBookmark;

	int FROM_FUTURE = Winevt.EVT_SUBSCRIBE_FLAGS.EvtSubscribeToFutureEvents;

}
