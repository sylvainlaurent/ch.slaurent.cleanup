package ch.slaurent.cleanup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.text.edits.ReplaceEdit;

public class RemoveRedundantModifiersCleanUpFix implements ICleanUpFix {
    private ICompilationUnit compilationUnit;
    private final CleanUpOptions options;

    public RemoveRedundantModifiersCleanUpFix(ICompilationUnit compilationUnit, CleanUpOptions options) {
        this.compilationUnit = compilationUnit;
        this.options = options;
    }

    public CompilationUnitChange createChange(IProgressMonitor progressMonitor)
            throws CoreException {
        if (options.isEnabled(SourceCleanUpOptionsInitializer.REMOVE_REDUNDANT_MODIFIERS)) {
            String source = compilationUnit.getSource();
            ReplaceEdit edit = new ReplaceEdit(0, source.length(), source);
            CompilationUnitChange change = new CompilationUnitChange("Remove redundant modifiers", compilationUnit);
            change.setEdit(edit);
            return change;
        }
        return null;
    }

}
