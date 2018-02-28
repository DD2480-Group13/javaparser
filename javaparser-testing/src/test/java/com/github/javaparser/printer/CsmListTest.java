package com.github.javaparser.printer;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.observer.ObservableProperty;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.printer.concretesyntaxmodel.CsmElement;
import com.github.javaparser.printer.concretesyntaxmodel.CsmList;
import com.github.javaparser.printer.concretesyntaxmodel.CsmNone;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Iterator;

public class CsmListTest {

    private class CsmMock implements CsmElement {
        private int prettyPrintCalls = 0;

        @Override
        public void prettyPrint(Node node, SourcePrinter printer) {
            ++prettyPrintCalls;
        }

        public int getPrettyPrintCalls() {
            return prettyPrintCalls;
        }
    }

    private final static Node ANY_NODE = new Expression() {
        @Override
        public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
            return null;
        }

        @Override
        public <A> void accept(VoidVisitor<A> v, A arg) {

        }
    };

    @Test
    public void testPreeceding() {
        //Contract: Ensures that the branch for preceeding is reached indicated by CsmMock::prettyPrint is called
        ObservableProperty prop = Mockito.mock(ObservableProperty.class);
        Mockito.when(prop.isAboutNodes()).thenReturn(true);
        CsmMock preceeding = new CsmMock();
        NodeList list = Mockito.mock(NodeList.class);
        Mockito.when(list.isEmpty()).thenReturn(false);
        Mockito.when(list.size()).thenReturn(0);
        Mockito.when(prop.getValueAsMultipleReference(Mockito.any(Node.class))).thenReturn(list);
        CsmList test = new CsmList(prop, new CsmNone(), new CsmNone(), preceeding, new CsmNone());
        test.prettyPrint(ANY_NODE, null);

        Assert.assertEquals(1, preceeding.getPrettyPrintCalls());
    }

    @Test
    public void testFollowing() {
        //Contract: Ensures that the branch for following is reached indicated by CsmMock::prettyPrint is called
        ObservableProperty prop = Mockito.mock(ObservableProperty.class);
        Mockito.when(prop.isAboutNodes()).thenReturn(true);
        CsmMock following = new CsmMock();
        NodeList list = Mockito.mock(NodeList.class);
        Mockito.when(list.isEmpty()).thenReturn(false);
        Mockito.when(list.size()).thenReturn(0);
        Mockito.when(prop.getValueAsMultipleReference(Mockito.any(Node.class))).thenReturn(list);
        CsmList test = new CsmList(prop, new CsmNone(), new CsmNone(), new CsmNone(), following);
        test.prettyPrint(ANY_NODE, null);

        Assert.assertEquals(1, following.getPrettyPrintCalls());
    }

    @Test
    public void testPreceedingNotAboutNode() {
        //Contract: Ensures that the branch for preceeding is reached indicated by CsmMock::prettyPrint is called, when property is NOT about Nodes
        ObservableProperty prop = Mockito.mock(ObservableProperty.class);
        Mockito.when(prop.isAboutNodes()).thenReturn(false);
        CsmMock preceeding = new CsmMock();
        NodeList list = Mockito.mock(NodeList.class);
        Mockito.when(list.isEmpty()).thenReturn(false);
        Iterator it = Mockito.mock(Iterator.class);
        Mockito.when(it.hasNext()).thenReturn(false);
        Mockito.when(list.iterator()).thenReturn(it);
        Mockito.when(prop.getValueAsCollection(Mockito.any(Node.class))).thenReturn(list);
        CsmList test = new CsmList(prop, new CsmNone(), new CsmNone(), preceeding, new CsmNone());
        test.prettyPrint(ANY_NODE, null);

        Assert.assertEquals(1, preceeding.getPrettyPrintCalls());
    }

    @Test
    public void testFollowingNotAboutNode() {
        //Contract: Ensures that the branch for following is reached indicated by CsmMock::prettyPrint is called, when property is NOT about Nodes
        ObservableProperty prop = Mockito.mock(ObservableProperty.class);
        Mockito.when(prop.isAboutNodes()).thenReturn(false);
        CsmMock following = new CsmMock();
        NodeList list = Mockito.mock(NodeList.class);
        Mockito.when(list.isEmpty()).thenReturn(false);
        Iterator it = Mockito.mock(Iterator.class);
        Mockito.when(it.hasNext()).thenReturn(false);
        Mockito.when(list.iterator()).thenReturn(it);
        Mockito.when(prop.getValueAsCollection(Mockito.any(Node.class))).thenReturn(list);
        CsmList test = new CsmList(prop, new CsmNone(), new CsmNone(), new CsmNone(), following);
        test.prettyPrint(ANY_NODE, null);

        Assert.assertEquals(1, following.getPrettyPrintCalls());
    }

    @Test
    public void testEmptyPrint() {
        //Contract: Ensures that prettyPrint is not called because of empty nodeList, CsmMock::prettyPrint is not called
        ObservableProperty prop = Mockito.mock(ObservableProperty.class);
        Mockito.when(prop.isAboutNodes()).thenReturn(false);
        CsmMock preceeding = new CsmMock();
        CsmMock following = new CsmMock();
        NodeList list = new NodeList<>();
        Mockito.when(prop.getValueAsMultipleReference(Mockito.any(Node.class))).thenReturn(list);
        CsmList test = new CsmList(prop, new CsmNone(), new CsmNone(), preceeding, following);
        test.prettyPrint(ANY_NODE, null);

        Assert.assertEquals(0, preceeding.getPrettyPrintCalls());
        Assert.assertEquals(0, following.getPrettyPrintCalls());
    }
}
