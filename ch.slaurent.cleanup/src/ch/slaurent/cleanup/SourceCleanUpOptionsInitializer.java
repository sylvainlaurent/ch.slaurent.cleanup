package ch.slaurent.cleanup;

import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUpOptionsInitializer;

public class SourceCleanUpOptionsInitializer implements
		ICleanUpOptionsInitializer {
	public final static String REMOVE_REDUNDANT_MODIFIERS = "ch.slaurent.cleanup.removeRedundantModifiers";
	public final static String REMOVE_REDUNDANT_THROWS = "ch.slaurent.cleanup.removeRedundantThrows";

	public SourceCleanUpOptionsInitializer() {
	}

	public void setDefaultOptions(CleanUpOptions options) {
		options.setOption(REMOVE_REDUNDANT_MODIFIERS, CleanUpOptions.FALSE);
		options.setOption(REMOVE_REDUNDANT_THROWS, CleanUpOptions.FALSE);
	}
}
