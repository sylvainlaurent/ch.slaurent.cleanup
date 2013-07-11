package ch.slaurent.cleanup;

import static ch.slaurent.cleanup.SourceCleanUpOptionsInitializer.REMOVE_REDUNDANT_MODIFIERS;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class SourceCleanUp implements ICleanUp {
	private CleanUpOptions options;

	public SourceCleanUp() {

	}

	@Override
	public void setOptions(CleanUpOptions options) {
		this.options = options;
	}

	@Override
	public CleanUpRequirements getRequirements() {
		return new CleanUpRequirements(true, false, false, null);
	}

	@Override
	public String[] getStepDescriptions() {
		List<String> steps = new ArrayList<String>();
		if (options.isEnabled(REMOVE_REDUNDANT_MODIFIERS)) {
			steps.add("Remove redundant modifiers");
		}

		return steps.toArray(new String[steps.size()]);
	}

	@Override
	public RefactoringStatus checkPreConditions(IJavaProject project,
			ICompilationUnit[] compilationUnits, IProgressMonitor monitor)
			throws CoreException {
		return new RefactoringStatus();
	}

	@Override
	public ICleanUpFix createFix(CleanUpContext context) throws CoreException {
		if (options.isEnabled(REMOVE_REDUNDANT_MODIFIERS)) {
			return new RemoveRedundantModifiersCleanUpFix(
					context, options);
		}
		return null;
	}

	@Override
	public RefactoringStatus checkPostConditions(IProgressMonitor monitor)
			throws CoreException {
		return new RefactoringStatus();
	}

}
