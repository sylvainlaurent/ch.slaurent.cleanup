package ch.slaurent.cleanup;

import static ch.slaurent.cleanup.SourceCleanUpOptionsInitializer.REMOVE_REDUNDANT_MODIFIERS;
import static ch.slaurent.cleanup.SourceCleanUpOptionsInitializer.REMOVE_REDUNDANT_THROWS;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
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
import org.eclipse.text.edits.TextEditGroup;

public class MultiCleanUpFix implements ICleanUpFix {
	private CleanUpContext context;
	private final CleanUpOptions options;

	public MultiCleanUpFix(CleanUpContext context, CleanUpOptions options) {
		this.context = context;
		this.options = options;
	}

	@Override
	public CompilationUnitChange createChange(IProgressMonitor progressMonitor)
			throws CoreException {

		CompilationUnit cu = context.getAST();
		ICompilationUnit icu = context.getCompilationUnit();
		String source = context.getCompilationUnit().getSource();
		Document doc = new Document(source);

		ASTRewrite rewrite = ASTRewrite.create(cu.getAST());

		cu.accept(new RedundantKeywordsRemover(rewrite));

		TextEdit edits = rewrite.rewriteAST(doc, icu.getJavaProject()
				.getOptions(true));
		CompilationUnitChange change = new CompilationUnitChange(
				"Remove redundant modifiers", icu);
		change.setEdit(edits);

		return change;
	}

	private class RedundantKeywordsRemover extends ASTVisitor {
		private ASTRewrite astRewrite;
		private ITypeBinding runtimeExceptionBinding;
		private ITypeBinding errorBinding;
		private Deque<TypeDeclaration> typesBeingVisited = new LinkedList<TypeDeclaration>();
		TextEditGroup modifierRemoval = new TextEditGroup(
				"Remove unneeded modifiers");
		TextEditGroup throwsRemoval = new TextEditGroup(
				"Remove declaration of unchecked exceptions");

		RedundantKeywordsRemover(ASTRewrite astRewrite) {
			this.astRewrite = astRewrite;
			runtimeExceptionBinding = astRewrite.getAST().resolveWellKnownType(
					"java.lang.RuntimeException");
			errorBinding = astRewrite.getAST().resolveWellKnownType(
					"java.lang.Error");
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			if (options.isEnabled(REMOVE_REDUNDANT_THROWS)) {
				@SuppressWarnings("unchecked")
				List<Name> thrownExceptions = node.thrownExceptions();
				for (Name name : thrownExceptions) {
					ITypeBinding typeBinding = name.resolveTypeBinding();
					if (typeBinding
							.isSubTypeCompatible(runtimeExceptionBinding)
							|| typeBinding.isSubTypeCompatible(errorBinding)) {
						astRewrite.remove(name, throwsRemoval);
					}

				}
			}
			if (options.isEnabled(REMOVE_REDUNDANT_MODIFIERS)) {
				@SuppressWarnings("unchecked")
				List<IExtendedModifier> modifiers = node.modifiers();
				if (typesBeingVisited.getLast().isInterface()) {
					for (IExtendedModifier extModifier : modifiers) {
						if (!extModifier.isModifier()) {
							continue;
						}
						Modifier modifier = (Modifier) extModifier;
						if (modifier.isPublic() || modifier.isAbstract())
							astRewrite.remove(modifier, modifierRemoval);
					}
				} else {
					Modifier privateModifier = null;
					Modifier finalModifier = null;
					for (IExtendedModifier extModifier : modifiers) {
						if (!extModifier.isModifier()) {
							continue;
						}
						Modifier modifier = (Modifier) extModifier;
						if (modifier.isPrivate())
							privateModifier = modifier;
						if (modifier.isFinal())
							finalModifier = modifier;
					}
					if (privateModifier != null && finalModifier != null) {
						// method is private final, just remove final
						astRewrite.remove(finalModifier, modifierRemoval);
					}

				}
			}
			return true;
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			typesBeingVisited.add(node);

			if (node.isInterface()) {
				@SuppressWarnings("unchecked")
				List<IExtendedModifier> modifiers = node.modifiers();

				for (IExtendedModifier extModifier : modifiers) {
					if (!extModifier.isModifier()) {
						continue;
					}
					Modifier modifier = (Modifier) extModifier;
					if (modifier.isStatic())
						astRewrite.remove(modifier, new TextEditGroup("toto"));
				}
			}
			return true;
		}

		@Override
		public void endVisit(TypeDeclaration node) {
			typesBeingVisited.removeLast();
		}

		@Override
		public boolean visit(AnnotationTypeDeclaration node) {
			return false;
		}

		@Override
		public boolean visit(EnumDeclaration node) {
			return false;
		}

		@Override
		public void endVisit(MethodDeclaration node) {
		}

	}
}
