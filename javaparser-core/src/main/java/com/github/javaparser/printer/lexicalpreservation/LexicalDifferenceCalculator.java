package com.github.javaparser.printer.lexicalpreservation;

import com.github.javaparser.GeneratedJavaParserConstants;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.observer.ObservableProperty;
import com.github.javaparser.coveragetool.CoverageTool;
import com.github.javaparser.printer.ConcreteSyntaxModel;
import com.github.javaparser.printer.Printable;
import com.github.javaparser.printer.SourcePrinter;
import com.github.javaparser.printer.concretesyntaxmodel.*;
import com.github.javaparser.printer.lexicalpreservation.changes.*;

import java.util.*;

class LexicalDifferenceCalculator {

    /**
     * The ConcreteSyntaxModel represents the general format. This model is a calculated version of the ConcreteSyntaxModel,
     * with no condition, no lists, just tokens and node children.
     */
    static class CalculatedSyntaxModel {
        final List<CsmElement> elements;

        CalculatedSyntaxModel(List<CsmElement> elements) {
            this.elements = elements;
        }

        public CalculatedSyntaxModel from(int index) {
            List<CsmElement> newList = new LinkedList<>();
            newList.addAll(elements.subList(index, elements.size()));
            return new CalculatedSyntaxModel(newList);
        }

        @Override
        public String toString() {
            return "CalculatedSyntaxModel{" +
                    "elements=" + elements +
                    '}';
        }

        CalculatedSyntaxModel sub(int start, int end) {
            return new CalculatedSyntaxModel(elements.subList(start, end));
        }

        void removeIndentationElements() {
            elements.removeIf(el -> el instanceof CsmIndent || el instanceof CsmUnindent);
        }
    }

    static class CsmChild implements CsmElement {
        private final Node child;

        public Node getChild() {
            return child;
        }

        CsmChild(Node child) {
            this.child = child;
        }

        @Override
        public void prettyPrint(Node node, SourcePrinter printer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "child(" + child.getClass().getSimpleName()+")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CsmChild csmChild = (CsmChild) o;

            return child.equals(csmChild.child);
        }

        @Override
        public int hashCode() {
            return child.hashCode();
        }
    }

    Difference calculateListRemovalDifference(ObservableProperty observableProperty, NodeList nodeList, int index) {
        Node container = nodeList.getParentNodeForChildren();
        CsmElement element = ConcreteSyntaxModel.forClass(container.getClass());
        CalculatedSyntaxModel original = calculatedSyntaxModelForNode(element, container);
        CalculatedSyntaxModel after = calculatedSyntaxModelAfterListRemoval(element, observableProperty, nodeList, index);
        return Difference.calculate(original, after);
    }

    Difference calculateListAdditionDifference(ObservableProperty observableProperty, NodeList nodeList, int index, Node nodeAdded) {
        Node container = nodeList.getParentNodeForChildren();
        CsmElement element = ConcreteSyntaxModel.forClass(container.getClass());
        CalculatedSyntaxModel original = calculatedSyntaxModelForNode(element, container);
        CalculatedSyntaxModel after = calculatedSyntaxModelAfterListAddition(element, observableProperty, nodeList, index, nodeAdded);
        return Difference.calculate(original, after);
    }

    Difference calculateListReplacementDifference(ObservableProperty observableProperty, NodeList nodeList, int index, Node newValue) {
        Node container = nodeList.getParentNodeForChildren();
        CsmElement element = ConcreteSyntaxModel.forClass(container.getClass());
        CalculatedSyntaxModel original = calculatedSyntaxModelForNode(element, container);
        CalculatedSyntaxModel after = calculatedSyntaxModelAfterListReplacement(element, observableProperty, nodeList, index, newValue);
        return Difference.calculate(original, after);
    }

    public void calculatePropertyChange(NodeText nodeText, Node observedNode, ObservableProperty property, Object oldValue, Object newValue) {
        if (nodeText == null) {
            throw new NullPointerException();
        }
        CsmElement element = ConcreteSyntaxModel.forClass(observedNode.getClass());
        CalculatedSyntaxModel original = calculatedSyntaxModelForNode(element, observedNode);
        CalculatedSyntaxModel after = calculatedSyntaxModelAfterPropertyChange(element, observedNode, property, oldValue, newValue);
        Difference difference = Difference.calculate(original, after);
        difference.apply(nodeText, observedNode);
    }

    // Visible for testing
    CalculatedSyntaxModel calculatedSyntaxModelForNode(CsmElement csm, Node node) {
        List<CsmElement> elements = new LinkedList<>();
        calculatedSyntaxModelForNode(csm, node, elements, new NoChange());
        return new CalculatedSyntaxModel(elements);
    }

    CalculatedSyntaxModel calculatedSyntaxModelForNode(Node node) {
        return calculatedSyntaxModelForNode(ConcreteSyntaxModel.forClass(node.getClass()), node);
    }

    private void calculatedSyntaxModelForNode(CsmElement csm, Node node, List<CsmElement> elements, Change change) {
        CoverageTool.makeCovered("calculatedSyntaxModelForNode 1");
        if (csm instanceof CsmSequence) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 2");
            CsmSequence csmSequence = (CsmSequence) csm;
            csmSequence.getElements().forEach(e -> calculatedSyntaxModelForNode(e, node, elements, change));
        } else if (csm instanceof CsmComment) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 3");
            // nothing to do
        } else if (csm instanceof CsmSingleReference) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 4");
            CsmSingleReference csmSingleReference = (CsmSingleReference)csm;
            Node child;
            if (change instanceof PropertyChange && ((PropertyChange)change).getProperty() == csmSingleReference.getProperty()) {
                CoverageTool.makeCovered("calculatedSyntaxModelForNode 5");
                child = (Node)((PropertyChange)change).getNewValue();
            } else {
                CoverageTool.makeCovered("calculatedSyntaxModelForNode 6");
                child = csmSingleReference.getProperty().getValueAsSingleReference(node);
            }
            if (child != null) {
                CoverageTool.makeCovered("calculatedSyntaxModelForNode 7");
                elements.add(new CsmChild(child));
            } else {
                CoverageTool.makeCovered("calculatedSyntaxModelForNode 8");
            }
        } else if (csm instanceof CsmNone) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 9");
            // nothing to do
        } else if (csm instanceof CsmToken) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 10");
            elements.add(csm);
        } else if (csm instanceof CsmOrphanCommentsEnding) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 11");
            // nothing to do
        } else if (csm instanceof CsmList) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 12");
            CsmList csmList = (CsmList) csm;
            if (csmList.getProperty().isAboutNodes()) {
                CoverageTool.makeCovered("calculatedSyntaxModelForNode 13");
                Object rawValue = change.getValue(csmList.getProperty(), node);
                NodeList nodeList;
                if (rawValue instanceof Optional) {
                    CoverageTool.makeCovered("calculatedSyntaxModelForNode 14");
                    Optional optional = (Optional)rawValue;
                    if (optional.isPresent()) {
                        CoverageTool.makeCovered("calculatedSyntaxModelForNode 15");
                        if (!(optional.get() instanceof NodeList)) {
                            CoverageTool.makeCovered("calculatedSyntaxModelForNode 16");
                            throw new IllegalStateException("Expected NodeList, found " + optional.get().getClass().getCanonicalName());
                        } else {
                            CoverageTool.makeCovered("calculatedSyntaxModelForNode 17");
                        }
                        nodeList = (NodeList) optional.get();
                    } else {
                        CoverageTool.makeCovered("calculatedSyntaxModelForNode 18");
                        nodeList = new NodeList();
                    }
                } else {
                    CoverageTool.makeCovered("calculatedSyntaxModelForNode 19");
                    if (!(rawValue instanceof NodeList)) {
                        CoverageTool.makeCovered("calculatedSyntaxModelForNode 20");
                        throw new IllegalStateException("Expected NodeList, found " + rawValue.getClass().getCanonicalName());
                    } else {
                        CoverageTool.makeCovered("calculatedSyntaxModelForNode 21");
                    }
                    nodeList = (NodeList) rawValue;
                }
                if (!nodeList.isEmpty()) {
                    CoverageTool.makeCovered("calculatedSyntaxModelForNode 22");
                    calculatedSyntaxModelForNode(csmList.getPreceeding(), node, elements, change);
                    for (int i = 0; i < nodeList.size(); i++) {
                        CoverageTool.makeCovered("calculatedSyntaxModelForNode 23");
                        if (i != 0) {
                            CoverageTool.makeCovered("calculatedSyntaxModelForNode 24");
                            calculatedSyntaxModelForNode(csmList.getSeparatorPre(), node, elements, change);
                        } else {
                            CoverageTool.makeCovered("calculatedSyntaxModelForNode 25");
                        }
                        elements.add(new CsmChild(nodeList.get(i)));
                        if (i != (nodeList.size() - 1)) {
                            CoverageTool.makeCovered("calculatedSyntaxModelForNode 26");
                            calculatedSyntaxModelForNode(csmList.getSeparatorPost(), node, elements, change);
                        } else {
                            CoverageTool.makeCovered("calculatedSyntaxModelForNode 27");
                        }

                    }
                    calculatedSyntaxModelForNode(csmList.getFollowing(), node, elements, change);
                } else {
                    CoverageTool.makeCovered("calculatedSyntaxModelForNode 28");
                }
            } else {
                CoverageTool.makeCovered("calculatedSyntaxModelForNode 29");
                Collection collection = (Collection) change.getValue(csmList.getProperty(), node);
                if (!collection.isEmpty()) {
                    CoverageTool.makeCovered("calculatedSyntaxModelForNode 30");
                    calculatedSyntaxModelForNode(csmList.getPreceeding(), node, elements, change);

                    boolean first = true;
                    for (Iterator it = collection.iterator(); it.hasNext(); ) {
                        CoverageTool.makeCovered("calculatedSyntaxModelForNode 31");
                        if (!first) {
                            CoverageTool.makeCovered("calculatedSyntaxModelForNode 32");
                            calculatedSyntaxModelForNode(csmList.getSeparatorPre(), node, elements, change);
                        } else {
                            CoverageTool.makeCovered("calculatedSyntaxModelForNode 33");
                        }
                        Object value = it.next();
                        if (value instanceof Modifier) {
                            CoverageTool.makeCovered("calculatedSyntaxModelForNode 34");
                            Modifier modifier = (Modifier)value;
                            elements.add(new CsmToken(toToken(modifier)));
                        } else {
                            CoverageTool.makeCovered("calculatedSyntaxModelForNode 35");
                            throw new UnsupportedOperationException(it.next().getClass().getSimpleName());
                        }
                        if (it.hasNext()) {
                            CoverageTool.makeCovered("calculatedSyntaxModelForNode 36");
                            calculatedSyntaxModelForNode(csmList.getSeparatorPost(), node, elements, change);
                        } else {
                            CoverageTool.makeCovered("calculatedSyntaxModelForNode 37");
                        }
                        first = false;
                    }
                    calculatedSyntaxModelForNode(csmList.getFollowing(), node, elements, change);
                } else {
                    CoverageTool.makeCovered("calculatedSyntaxModelForNode 38");
                }
            }
        } else if (csm instanceof CsmConditional) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 39");
            CsmConditional csmConditional = (CsmConditional) csm;
            boolean satisfied = change.evaluate(csmConditional, node);
            if (satisfied) {
                CoverageTool.makeCovered("calculatedSyntaxModelForNode 40");
                calculatedSyntaxModelForNode(csmConditional.getThenElement(), node, elements, change);
            } else {
                CoverageTool.makeCovered("calculatedSyntaxModelForNode 41");
                calculatedSyntaxModelForNode(csmConditional.getElseElement(), node, elements, change);
            }
        } else if (csm instanceof CsmIndent) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 42");
            elements.add(csm);
        } else if (csm instanceof CsmUnindent) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 43");
            elements.add(csm);
        } else if (csm instanceof CsmAttribute) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 44");
            CsmAttribute csmAttribute = (CsmAttribute) csm;
            Object value = change.getValue(csmAttribute.getProperty(), node);
            String text = value.toString();
            if (value instanceof Printable) {
                CoverageTool.makeCovered("calculatedSyntaxModelForNode 45");
                text = ((Printable) value).asString();
            } else {
                CoverageTool.makeCovered("calculatedSyntaxModelForNode 46");
            }
            elements.add(new CsmToken(csmAttribute.getTokenType(node, value.toString()), text));
        } else if ((csm instanceof CsmString) && (node instanceof StringLiteralExpr)) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 47");
            elements.add(new CsmToken(GeneratedJavaParserConstants.STRING_LITERAL,
                    "\"" + ((StringLiteralExpr) node).getValue() + "\""));
        } else if (csm instanceof CsmMix) {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 48");
            CsmMix csmMix = (CsmMix)csm;
            List<CsmElement> mixElements = new LinkedList<>();
            csmMix.getElements().forEach(e -> calculatedSyntaxModelForNode(e, node, mixElements, change));
            elements.add(new CsmMix(mixElements));
        } else {
            CoverageTool.makeCovered("calculatedSyntaxModelForNode 49");
            throw new UnsupportedOperationException(csm.getClass().getSimpleName()+ " " + csm);
        }
    }

    private int toToken(Modifier modifier) {
        switch (modifier) {
            case PUBLIC:
                return GeneratedJavaParserConstants.PUBLIC;
            case PRIVATE:
                return GeneratedJavaParserConstants.PRIVATE;
            case PROTECTED:
                return GeneratedJavaParserConstants.PROTECTED;
            case STATIC:
                return GeneratedJavaParserConstants.STATIC;
            case FINAL:
                return GeneratedJavaParserConstants.FINAL;
            case ABSTRACT:
                return GeneratedJavaParserConstants.ABSTRACT;
            default:
                throw new UnsupportedOperationException(modifier.name());
        }
    }

    ///
    /// Methods that calculate CalculatedSyntaxModel
    ///

    // Visible for testing
    CalculatedSyntaxModel calculatedSyntaxModelAfterPropertyChange(Node node, ObservableProperty property, Object oldValue, Object newValue) {
        return calculatedSyntaxModelAfterPropertyChange(ConcreteSyntaxModel.forClass(node.getClass()), node, property, oldValue, newValue);
    }

    // Visible for testing
    CalculatedSyntaxModel calculatedSyntaxModelAfterPropertyChange(CsmElement csm, Node node, ObservableProperty property, Object oldValue, Object newValue) {
        List<CsmElement> elements = new LinkedList<>();
        calculatedSyntaxModelForNode(csm, node, elements, new PropertyChange(property, oldValue, newValue));
        return new CalculatedSyntaxModel(elements);
    }

    // Visible for testing
    CalculatedSyntaxModel calculatedSyntaxModelAfterListRemoval(CsmElement csm, ObservableProperty observableProperty, NodeList nodeList, int index) {
        List<CsmElement> elements = new LinkedList<>();
        Node container = nodeList.getParentNodeForChildren();
        calculatedSyntaxModelForNode(csm, container, elements, new ListRemovalChange(observableProperty, index));
        return new CalculatedSyntaxModel(elements);
    }

    // Visible for testing
    CalculatedSyntaxModel calculatedSyntaxModelAfterListAddition(CsmElement csm, ObservableProperty observableProperty, NodeList nodeList, int index, Node nodeAdded) {
        List<CsmElement> elements = new LinkedList<>();
        Node container = nodeList.getParentNodeForChildren();
        calculatedSyntaxModelForNode(csm, container, elements, new ListAdditionChange(observableProperty, index, nodeAdded));
        return new CalculatedSyntaxModel(elements);
    }

    // Visible for testing
    CalculatedSyntaxModel calculatedSyntaxModelAfterListAddition(Node container, ObservableProperty observableProperty, int index, Node nodeAdded) {
        CsmElement csm = ConcreteSyntaxModel.forClass(container.getClass());
        Object rawValue = observableProperty.getRawValue(container);
        if (!(rawValue instanceof NodeList)) {
            throw new IllegalStateException("Expected NodeList, found " + rawValue.getClass().getCanonicalName());
        }
        NodeList nodeList = (NodeList)rawValue;
        return calculatedSyntaxModelAfterListAddition(csm, observableProperty, nodeList, index, nodeAdded);
    }

    // Visible for testing
    CalculatedSyntaxModel calculatedSyntaxModelAfterListRemoval(Node container, ObservableProperty observableProperty, int index) {
        CsmElement csm = ConcreteSyntaxModel.forClass(container.getClass());
        Object rawValue = observableProperty.getRawValue(container);
        if (!(rawValue instanceof NodeList)) {
            throw new IllegalStateException("Expected NodeList, found " + rawValue.getClass().getCanonicalName());
        }
        NodeList nodeList = (NodeList)rawValue;
        return calculatedSyntaxModelAfterListRemoval(csm, observableProperty, nodeList, index);
    }

    // Visible for testing
    private CalculatedSyntaxModel calculatedSyntaxModelAfterListReplacement(CsmElement csm, ObservableProperty observableProperty, NodeList nodeList, int index, Node newValue) {
        List<CsmElement> elements = new LinkedList<>();
        Node container = nodeList.getParentNodeForChildren();
        calculatedSyntaxModelForNode(csm, container, elements, new ListReplacementChange(observableProperty, index, newValue));
        return new CalculatedSyntaxModel(elements);
    }

}
