package ch.slaurent.cleanup;

import static ch.slaurent.cleanup.SourceCleanUpOptionsInitializer.REMOVE_REDUNDANT_MODIFIERS;

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
		final Button convertButton = new Button(composite, SWT.CHECK);
		convertButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false, 1, 1));
		convertButton.setText("Remove redundant modifiers");
		convertButton.setSelection(options
				.isEnabled(REMOVE_REDUNDANT_MODIFIERS));
		convertButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				options.setOption(REMOVE_REDUNDANT_MODIFIERS, convertButton
						.getSelection() ? CleanUpOptions.TRUE
						: CleanUpOptions.FALSE);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				options.setOption(REMOVE_REDUNDANT_MODIFIERS, convertButton
						.getSelection() ? CleanUpOptions.TRUE
						: CleanUpOptions.FALSE);
			}
		});

		return composite;
	}

	@Override
	public void setOptions(CleanUpOptions options) {
		this.options = options;
	}

	@Override
	public int getCleanUpCount() {
		return 1;
	}

	@Override
	public int getSelectedCleanUpCount() {
		return options.isEnabled(REMOVE_REDUNDANT_MODIFIERS) ? 1 : 0;
	}

	@Override
	public String getPreview() {
		return "N/A";
	}

}
