package com.mocircle.jwinlog.test;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class WindowsOnlyTestRunner extends BlockJUnit4ClassRunner {

	private static final boolean IS_WINDOWS = System.getProperty("os.name").startsWith("Windows");

	public WindowsOnlyTestRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	public void run(RunNotifier notifier) {
		if (IS_WINDOWS) {
			super.run(notifier);
		}
	}

}
