/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.extensions.svg;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.XMLObj;
import org.apache.fop.layoutmgr.AddLMVisitor;

/**
 * Class for SVG element objects.
 * This aids in the construction of the SVG Document.
 */
public class SVGObj extends XMLObj {
    /**
     * constructs an svg object (called by Maker).
     *
     * @param parent the parent formatting object
     */
    public SVGObj(FONode parent) {
        super(parent);
    }

    /**
     * Get the namespace for svg.
     * @return the svg namespace
     */
    public String getNameSpace() {
        return "http://www.w3.org/2000/svg";
    }

    public void acceptVisitor(AddLMVisitor aLMV) {
        aLMV.serveSVGObj(this);
    }

}

