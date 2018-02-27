/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2016 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License 
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package com.github.javaparser.printer.concretesyntaxmodel;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.observer.ObservableProperty;
import com.github.javaparser.coveragetool.CoverageTool;
import com.github.javaparser.printer.ConcreteSyntaxModel;
import com.github.javaparser.printer.SourcePrinter;

import java.util.Collection;
import java.util.Iterator;

public class CsmList implements CsmElement {
    private final ObservableProperty property;
    private final CsmElement separatorPost;
    private final CsmElement separatorPre;
    private final CsmElement preceeding;
    private final CsmElement following;

    public ObservableProperty getProperty() {
        return property;
    }

    public CsmElement getSeparatorPost() {
        return separatorPost;
    }

    public CsmElement getSeparatorPre() {
        return separatorPre;
    }

    public CsmElement getPreceeding() {
        return preceeding;
    }

    public CsmElement getFollowing() {
        return following;
    }

    public CsmList(ObservableProperty property, CsmElement separator) {
        this(property, new CsmNone(), separator, new CsmNone(), new CsmNone());
    }

    public CsmList(ObservableProperty property) {
        this(property, new CsmNone(), new CsmNone(), new CsmNone(), new CsmNone());
    }

    public CsmList(ObservableProperty property, CsmElement separatorPre, CsmElement separatorPost, CsmElement preceeding, CsmElement following) {
        this.property = property;
        this.separatorPre = separatorPre;
        this.separatorPost = separatorPost;
        this.preceeding = preceeding;
        this.following = following;
    }

    @Override
    public void prettyPrint(Node node, SourcePrinter printer) {
        if (property.isAboutNodes()) {
            CoverageTool.makeCovered("CsmList 1");
            NodeList nodeList = property.getValueAsMultipleReference(node);
            if (nodeList == null) {
                CoverageTool.makeCovered("CsmList 2");
                return;
            }
            CoverageTool.makeCovered("CsmList 3");
            if (!nodeList.isEmpty() && preceeding != null) {
                CoverageTool.makeCovered("CsmList 4");
                preceeding.prettyPrint(node, printer);
            }
            CoverageTool.makeCovered("CsmList 5");
            for (int i = 0; i < nodeList.size(); i++) {
                CoverageTool.makeCovered("CsmList 6");
                if (separatorPre != null && i != 0) {
                    CoverageTool.makeCovered("CsmList 7");
                    separatorPre.prettyPrint(node, printer);
                }
                ConcreteSyntaxModel.genericPrettyPrint(nodeList.get(i), printer);
                if (separatorPost != null && i != (nodeList.size() - 1)) {
                    CoverageTool.makeCovered("CsmList 8");
                    separatorPost.prettyPrint(node, printer);
                }
            }
            if (!nodeList.isEmpty() && following != null) {
                CoverageTool.makeCovered("CsmList 9");
                following.prettyPrint(node, printer);
            }
        } else {
            CoverageTool.makeCovered("CsmList 10");
            Collection<?> values = property.getValueAsCollection(node);
            if (values == null) {
                CoverageTool.makeCovered("CsmList 11");
                return;
            }
            CoverageTool.makeCovered("CsmList 12");
            if (!values.isEmpty() && preceeding != null) {
                CoverageTool.makeCovered("CsmList 13");
                preceeding.prettyPrint(node, printer);
            }
            for (Iterator it = values.iterator(); it.hasNext(); ) {
                CoverageTool.makeCovered("CsmList 14");
                if (separatorPre != null && it.hasNext()) {
                    CoverageTool.makeCovered("CsmList 15");
                    separatorPre.prettyPrint(node, printer);
                }
                printer.print(PrintingHelper.printToString(it.next()));
                if (separatorPost != null && it.hasNext()) {
                    CoverageTool.makeCovered("CsmList 16");
                    separatorPost.prettyPrint(node, printer);
                }
            }
            if (!values.isEmpty() && following != null) {
                CoverageTool.makeCovered("CsmList 17");
                following.prettyPrint(node, printer);
            }
        }
    }
}
