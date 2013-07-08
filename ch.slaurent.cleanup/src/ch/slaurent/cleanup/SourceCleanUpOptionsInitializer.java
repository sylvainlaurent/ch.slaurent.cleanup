package ch.slaurent.cleanup;

import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUpOptionsInitializer;

public class SourceCleanUpOptionsInitializer implements
		ICleanUpOptionsInitializer {
	public final static String REMOVE_REDUNDANT_MODIFIERS = "ch.slaurent.cleanup.removeRedundantModifiers";

	public SourceCleanUpOptionsInitializer() {
	}

	public void setDefaultOptions(CleanUpOptions options) {
		options.setOption(REMOVE_REDUNDANT_MODIFIERS, CleanUpOptions.FALSE);
	}
}
