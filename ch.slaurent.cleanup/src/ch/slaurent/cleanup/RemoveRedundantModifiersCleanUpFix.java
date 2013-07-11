package ch.slaurent.cleanup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

public class RemoveRedundantModifiersCleanUpFix implements ICleanUpFix {
	private CleanUpContext context;
	private final CleanUpOptions options;

	public RemoveRedundantModifiersCleanUpFix(CleanUpContext context,
			CleanUpOptions options) {
		this.context = context;
		this.options = options;
	}

	public CompilationUnitChange createChange(IProgressMonitor progressMonitor)
			throws CoreException {

		CompilationUnit cu = context.getAST();
		ICompilationUnit icu = context.getCompilationUnit();
		String source = context.getCompilationUnit().getSource();
		Document doc = new Document(source);
		cu.recordModifications();
		cu.accept(new RedundantModifierRemover());

		TextEdit edits = cu.rewrite(doc, icu.getJavaProject().getOptions(true));
		CompilationUnitChange change = new CompilationUnitChange(
				"Remove redundant modifiers", icu);
		change.setEdit(edits);

		return change;
	}

	// private void work() throws CoreException {
	// IType[] allTypes = compilationUnit.getAllTypes();
	// for (IType type : allTypes) {
	// if (type.isInterface() && Flags.isPublic(type.getFlags())) {
	// for (IMethod method : type.getMethods()) {
	// if (Flags.isPublic(method.getFlags())) {
	// // TODO remove public
	// System.out.println("found public method " + method);
	// }
	// if (Flags.isAbstract(method.getFlags())) {
	// // TODO remove public
	// System.out.println("found abstract method " + method);
	// }
	// }
	// }
	// }
	// }

	private static class RedundantModifierRemover extends ASTVisitor {
		private boolean visitingMethod = false;


		@Override
		public boolean visit(MethodDeclaration node) {
			visitingMethod = true;
			return true;
		}

		@Override
		public boolean visit(Modifier node) {
			if (visitingMethod) {
				if (node.isAbstract() || node.isPublic()) {
					node.delete();
				}
			}
			return true;
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			return node.isInterface() && Modifier.isPublic(node.getModifiers());
		}

		@Override
		public void endVisit(MethodInvocation node) {
			visitingMethod = false;
		}

		@Override
		public void endVisit(Modifier node) {
			// TODO Auto-generated method stub
			super.endVisit(node);
		}

	}
}
