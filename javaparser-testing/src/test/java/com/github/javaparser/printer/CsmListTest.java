package com.github.javaparser.printer;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.observer.ObservableProperty;
import com.github.javaparser.printer.concretesyntaxmodel.CsmElement;
import com.github.javaparser.printer.concretesyntaxmodel.CsmList;
import com.github.javaparser.printer.concretesyntaxmodel.CsmNone;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

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

    @Test
    public void testPreeceding() {
        ObservableProperty prop = Mockito.mock(ObservableProperty.class);
        Mockito.when(prop.isAboutNodes()).thenReturn(true);
        CsmMock preceeding = new CsmMock();
        NodeList list = new NodeList<>((Node)null);
        Mockito.when(prop.getValueAsMultipleReference(Mockito.any(Node.class))).thenReturn(list);
        CsmList test = new CsmList(prop, new CsmNone(), new CsmNone(), preceeding, new CsmNone());
        test.prettyPrint(null, null);

        Assert.assertEquals(1, preceeding.getPrettyPrintCalls());
    }

    @Test
    public void testFollowing() {
        ObservableProperty prop = Mockito.mock(ObservableProperty.class);
        Mockito.when(prop.isAboutNodes()).thenReturn(false);
        CsmMock following = new CsmMock();
        NodeList list = new NodeList<>((Node)null);
        Mockito.when(prop.getValueAsMultipleReference(Mockito.any(Node.class))).thenReturn(list);
        CsmList test = new CsmList(prop, new CsmNone(), new CsmNone(), new CsmNone(), following);
        test.prettyPrint(null, null);

        Assert.assertEquals(1, following.getPrettyPrintCalls());
    }

    @Test
    public void testSeparatorPre() {
        ObservableProperty prop = Mockito.mock(ObservableProperty.class);
        Mockito.when(prop.isAboutNodes()).thenReturn(false);
        CsmMock separatorPre = new CsmMock();
        NodeList list = new NodeList<>((Node)null);
        Mockito.when(prop.getValueAsMultipleReference(Mockito.any(Node.class))).thenReturn(list);
        CsmList test = new CsmList(prop, separatorPre, new CsmNone(), new CsmNone(), new CsmNone());
        test.prettyPrint(null, null);

        Assert.assertEquals(1, separatorPre.getPrettyPrintCalls());
    }

    @Test
    public void testEmptyPrint() {
        ObservableProperty prop = Mockito.mock(ObservableProperty.class);
        Mockito.when(prop.isAboutNodes()).thenReturn(false);
        CsmMock preceeding = new CsmMock();
        CsmMock following = new CsmMock();
        NodeList list = new NodeList<>();
        Mockito.when(prop.getValueAsMultipleReference(Mockito.any(Node.class))).thenReturn(list);
        CsmList test = new CsmList(prop, new CsmNone(), new CsmNone(), preceeding, following);
        test.prettyPrint(null, null);

        Assert.assertEquals(0, preceeding.getPrettyPrintCalls());
        Assert.assertEquals(0, following.getPrettyPrintCalls());
    }
}
