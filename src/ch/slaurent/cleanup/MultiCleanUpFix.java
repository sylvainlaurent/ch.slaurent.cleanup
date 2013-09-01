package ch.slaurent.cleanup;

import static ch.slaurent.cleanup.SourceCleanUpOptionsInitializer.REMOVE_REDUNDANT_MODIFIERS;
import static ch.slaurent.cleanup.SourceCleanUpOptionsInitializer.REMOVE_REDUNDANT_THROWS;

import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
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
    private final CleanUpContext context;
    private final CleanUpOptions options;

    public MultiCleanUpFix(CleanUpContext context, CleanUpOptions options) {
        this.context = context;
        this.options = options;
    }

    @Override
    public CompilationUnitChange createChange(IProgressMonitor progressMonitor) throws CoreException {

        final CompilationUnit cu = context.getAST();
        final ICompilationUnit icu = context.getCompilationUnit();
        final String source = context.getCompilationUnit().getSource();
        final Document doc = new Document(source);

        final ASTRewrite rewrite = ASTRewrite.create(cu.getAST());

        cu.accept(new RedundantKeywordsRemover(rewrite));

        final TextEdit edits = rewrite.rewriteAST(doc, icu.getJavaProject().getOptions(true));
        final CompilationUnitChange change = new CompilationUnitChange("Remove redundant modifiers", icu);
        change.setEdit(edits);

        return change;
    }

    private class RedundantKeywordsRemover extends ASTVisitor {
        private final ASTRewrite astRewrite;
        private final ITypeBinding runtimeExceptionBinding;
        private final ITypeBinding errorBinding;
        private final Deque<TypeDeclaration> typesBeingVisited = new LinkedList<TypeDeclaration>();
        private final TextEditGroup modifierRemoval = new TextEditGroup("Remove unneeded modifiers");
        private final TextEditGroup throwsRemoval = new TextEditGroup("Remove declaration of unchecked exceptions");

        RedundantKeywordsRemover(ASTRewrite astRewrite) {
            this.astRewrite = astRewrite;
            runtimeExceptionBinding = astRewrite.getAST().resolveWellKnownType("java.lang.RuntimeException");
            errorBinding = astRewrite.getAST().resolveWellKnownType("java.lang.Error");
        }

        @Override
        public boolean visit(TypeDeclaration node) {
            typesBeingVisited.add(node);

            if (node.isInterface()) {
                @SuppressWarnings("unchecked")
                final List<IExtendedModifier> modifiers = node.modifiers();

                for (final IExtendedModifier extModifier : modifiers) {
                    if (!extModifier.isModifier()) {
                        continue;
                    }
                    final Modifier modifier = (Modifier) extModifier;
                    if (modifier.isStatic()) {
                        astRewrite.remove(modifier, new TextEditGroup("toto"));
                    }
                }
            }
            return true;
        }

        @Override
        public void endVisit(TypeDeclaration node) {
            typesBeingVisited.removeLast();
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            if (options.isEnabled(REMOVE_REDUNDANT_THROWS)) {
                @SuppressWarnings("unchecked")
                final List<Name> thrownExceptions = node.thrownExceptions();
                final Map<ITypeBinding, Name> typeToNameMap = new IdentityHashMap<ITypeBinding, Name>();

                for (final Name name : thrownExceptions) {
                    final ITypeBinding typeBinding = name.resolveTypeBinding();
                    typeToNameMap.put(typeBinding, name);
                }
                for (final Entry<ITypeBinding, Name> entry : typeToNameMap.entrySet()) {
                    if (entry.getKey().isSubTypeCompatible(runtimeExceptionBinding)
                            || entry.getKey().isSubTypeCompatible(errorBinding)) {
                        astRewrite.remove(entry.getValue(), throwsRemoval);
                    } else {
                        // check if subtype of another declared exception
                        for (final Entry<ITypeBinding, Name> entry2 : typeToNameMap.entrySet()) {
                            if (entry.getKey() != entry2.getKey()
                                    && entry.getKey().isSubTypeCompatible(entry2.getKey())) {
                                astRewrite.remove(entry.getValue(), throwsRemoval);
                            }
                        }
                    }

                }
            }
            if (options.isEnabled(REMOVE_REDUNDANT_MODIFIERS)) {
                @SuppressWarnings("unchecked")
                final List<IExtendedModifier> modifiers = node.modifiers();
                if (typesBeingVisited.getLast().isInterface()) {
                    for (final IExtendedModifier extModifier : modifiers) {
                        if (!extModifier.isModifier()) {
                            continue;
                        }
                        final Modifier modifier = (Modifier) extModifier;
                        if (modifier.isPublic() || modifier.isAbstract()) {
                            astRewrite.remove(modifier, modifierRemoval);
                        }
                    }
                } else {
                    Modifier privateModifier = null;
                    Modifier finalModifier = null;
                    for (final IExtendedModifier extModifier : modifiers) {
                        if (!extModifier.isModifier()) {
                            continue;
                        }
                        final Modifier modifier = (Modifier) extModifier;
                        if (modifier.isPrivate()) {
                            privateModifier = modifier;
                        }
                        if (modifier.isFinal()) {
                            finalModifier = modifier;
                        }
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
        public boolean visit(FieldDeclaration node) {
            if (options.isEnabled(REMOVE_REDUNDANT_MODIFIERS) && typesBeingVisited.getLast().isInterface()) {
                @SuppressWarnings("unchecked")
                final List<IExtendedModifier> modifiers = node.modifiers();
                for (final IExtendedModifier extModifier : modifiers) {
                    if (!extModifier.isModifier()) {
                        continue;
                    }
                    final Modifier modifier = (Modifier) extModifier;
                    if (modifier.isPublic() || modifier.isFinal() || modifier.isStatic()) {
                        astRewrite.remove(modifier, modifierRemoval);
                    }
                }
            }
            return false;
        }

        @Override
        public boolean visit(AnnotationTypeDeclaration node) {
            return false;
        }

        @Override
        public boolean visit(EnumDeclaration node) {
            return false;
        }

    }
}
