package ch.slaurent.cleanup;

import static ch.slaurent.cleanup.SourceCleanUpOptionsInitializer.REMOVE_REDUNDANT_MODIFIERS;
import static ch.slaurent.cleanup.SourceCleanUpOptionsInitializer.REMOVE_REDUNDANT_THROWS;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

public class MultiCleanUpFix implements ICleanUpFix {
	private CleanUpContext context;
	private final CleanUpOptions options;

	public MultiCleanUpFix(CleanUpContext context, CleanUpOptions options) {
		this.context = context;
		this.options = options;
	}

	public CompilationUnitChange createChange(IProgressMonitor progressMonitor)
			throws CoreException {

		CompilationUnit cu = context.getAST();
		ICompilationUnit icu = context.getCompilationUnit();
		String source = context.getCompilationUnit().getSource();
		Document doc = new Document(source);

		ASTRewrite rewrite = ASTRewrite.create(cu.getAST());

		cu.accept(new RedundantModifierRemover(rewrite));

		TextEdit edits = rewrite.rewriteAST(doc, icu.getJavaProject()
				.getOptions(true));
		CompilationUnitChange change = new CompilationUnitChange(
				"Remove redundant modifiers", icu);
		change.setEdit(edits);

		return change;
	}

	private class RedundantModifierRemover extends ASTVisitor {
		private ASTRewrite astRewrite;
		private ITypeBinding runtimeExceptionBinding;
		private ITypeBinding errorBinding;
		private boolean visitingMethod = false;

		RedundantModifierRemover(ASTRewrite astRewrite) {
			this.astRewrite = astRewrite;
			runtimeExceptionBinding = astRewrite.getAST().resolveWellKnownType(
					"java.lang.RuntimeException");
			errorBinding = astRewrite.getAST().resolveWellKnownType(
					"java.lang.Error");
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			visitingMethod = true;
			if (options.isEnabled(REMOVE_REDUNDANT_THROWS)) {
				@SuppressWarnings("unchecked")
				List<Name> thrownExceptions = node.thrownExceptions();
				for (Name name : thrownExceptions) {
					ITypeBinding typeBinding = name.resolveTypeBinding();
					if (typeBinding
							.isSubTypeCompatible(runtimeExceptionBinding)
							|| typeBinding.isSubTypeCompatible(errorBinding)) {
						astRewrite.remove(name, null);
					}

				}
			}
			return true;
		}

		@Override
		public boolean visit(Modifier node) {
			if (visitingMethod && options.isEnabled(REMOVE_REDUNDANT_MODIFIERS)) {
				if (node.isAbstract() || node.isPublic()) {
					astRewrite.remove(node, null);
				}
			}
			return true;
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			return node.isInterface();
		}

		@Override
		public void endVisit(MethodInvocation node) {
			visitingMethod = false;
		}

	}
}
