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
 
package org.apache.fop.area;

/**
 * Area tree extension interface.
 * This interface is used by area tree extensions that are handled
 * by the renderer.
 * When this extension is handled by the area tree it is rendered
 * according to the three possibilities, IMMEDIATELY, AFTER_PAGE
 * or END_OF_DOC.
 */
public interface TreeExt {
    /**
     * Render this extension immediately when
     * being handled by the area tree.
     */
    public static final int IMMEDIATELY = 0;

    /**
     * Render this extension after the next page is rendered
     * or prepared when being handled by the area tree.
     */
    public static final int AFTER_PAGE = 1;

    /**
     * Render this extension at the end of the document once
     * all pages have been fully rendered.
     */
    public static final int END_OF_DOC = 2;

}
