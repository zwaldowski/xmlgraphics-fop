/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.ColorType;

/**
 * Store all background related properties on an FO.
 * Public "structure" allows direct member access.
 */
public class BackgroundProps {
    public int backAttachment;
    public ColorType backColor;
    public String backImage;
    public int backRepeat;
    public Length backPosHorizontal;
    public Length backPosVertical;

    public BackgroundProps() {}

}
