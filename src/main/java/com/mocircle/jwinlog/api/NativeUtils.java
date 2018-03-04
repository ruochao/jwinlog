package com.mocircle.jwinlog.api;

import java.nio.charset.StandardCharsets;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.Winevt;

public final class NativeUtils {

	private NativeUtils() {
	}

	public static Object[] getVariantsFromMemory(Memory memory, int count) {
		Object[] objs = new Object[count];
		for (int i = 0; i < count; i++) {
			Winevt.EVT_VARIANT var = new Winevt.EVT_VARIANT();
			var.use(memory.share(var.size() * i));
			var.read();
			objs[i] = var.getValue();
		}
		return objs;
	}

	public static String getUnicodeStringFromMemory(Memory memory, int used) {
		return StandardCharsets.UTF_16LE.decode(memory.getByteBuffer(0, used)).toString();
	}
}
