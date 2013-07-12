package ch.slaurent.cleanup;

import static ch.slaurent.cleanup.SourceCleanUpOptionsInitializer.REMOVE_REDUNDANT_MODIFIERS;
import static ch.slaurent.cleanup.SourceCleanUpOptionsInitializer.REMOVE_REDUNDANT_THROWS;

import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUpConfigurationUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class SourceCleanUpConfigurationUI implements ICleanUpConfigurationUI {
	private CleanUpOptions options;

	public Composite createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 1;
		createCheckbox(composite, "Remove redundant modifiers",
				REMOVE_REDUNDANT_MODIFIERS);
		createCheckbox(composite, "Remove redundant throws",
				REMOVE_REDUNDANT_THROWS);
		return composite;
	}

	private void createCheckbox(final Composite composite, final String label,
			final String optionName) {
		final Button convertButton = new Button(composite, SWT.CHECK);
		convertButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false, 1, 1));
		convertButton.setText(label);
		convertButton.setSelection(options.isEnabled(optionName));
		convertButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				options.setOption(optionName,
						convertButton.getSelection() ? CleanUpOptions.TRUE
								: CleanUpOptions.FALSE);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				options.setOption(optionName,
						convertButton.getSelection() ? CleanUpOptions.TRUE
								: CleanUpOptions.FALSE);
			}
		});
	}

	@Override
	public void setOptions(CleanUpOptions options) {
		this.options = options;
	}

	@Override
	public int getCleanUpCount() {
		return 2;
	}

	@Override
	public int getSelectedCleanUpCount() {
		int count = 0;
		count += options.isEnabled(REMOVE_REDUNDANT_MODIFIERS) ? 1 : 0;
		count += options.isEnabled(REMOVE_REDUNDANT_THROWS) ? 1 : 0;
		return count;
	}

	@Override
	public String getPreview() {
		return "N/A";
	}

}
