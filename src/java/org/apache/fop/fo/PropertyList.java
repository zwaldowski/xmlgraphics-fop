/*
 * $Id: PropertyList.java,v 1.20 2003/03/05 21:48:01 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo;

import java.util.HashMap;
import org.apache.fop.fo.properties.WritingMode;
import org.apache.fop.apps.FOPException;

/**
 * Class containing the collection of properties for a given FObj.
 */
public class PropertyList extends HashMap {

    // writing-mode values
    private byte[] wmtable = null;

    // absolute directions and dimensions
    /** constant for direction "left" */
    public static final int LEFT = 0;
    /** constant for direction "right" */
    public static final int RIGHT = 1;
    /** constant for direction "top" */
    public static final int TOP = 2;
    /** constant for direction "bottom" */
    public static final int BOTTOM = 3;
    /** constant for dimension "height" */
    public static final int HEIGHT = 4;
    /** constant for dimension "width" */
    public static final int WIDTH = 5;

    // directions relative to writing-mode
    /** constant for direction "start" */
    public static final int START = 0;
    /** constant for direction "end" */
    public static final int END = 1;
    /** constant for direction "before" */
    public static final int BEFORE = 2;
    /** constant for direction "after" */
    public static final int AFTER = 3;
    /** constant for dimension "block-progression-dimension" */
    public static final int BLOCKPROGDIM = 4;
    /** constant for dimension "inline-progression-dimension" */
    public static final int INLINEPROGDIM = 5;

    private static final String[] ABS_NAMES = new String[] {
        "left", "right", "top", "bottom", "height", "width"
    };

    private static final String[] REL_NAMES = new String[] {
        "start", "end", "before", "after", "block-progression-dimension",
        "inline-progression-dimension"
    };

    private static final HashMap WRITING_MODE_TABLES = new HashMap(4);
    {
        WRITING_MODE_TABLES.put(new Integer(WritingMode.LR_TB),    /* lr-tb */
        new byte[] {
            START, END, BEFORE, AFTER, BLOCKPROGDIM, INLINEPROGDIM
        });
        WRITING_MODE_TABLES.put(new Integer(WritingMode.RL_TB),    /* rl-tb */
        new byte[] {
            END, START, BEFORE, AFTER, BLOCKPROGDIM, INLINEPROGDIM
        });
        WRITING_MODE_TABLES.put(new Integer(WritingMode.TB_RL),    /* tb-rl */
        new byte[] {
            AFTER, BEFORE, START, END, INLINEPROGDIM, BLOCKPROGDIM
        });
    }

    private PropertyListBuilder builder;
    private PropertyList parentPropertyList = null;
    private String namespace = "";
    private String element = "";
    private FObj fobj = null;

    /**
     * Basic constructor.
     * @param parentPropertyList the PropertyList belongin to the new objects
     * parent
     * @param space name of namespace
     * @param el name of element
     */
    public PropertyList(PropertyList parentPropertyList, String space,
                        String el) {
        this.parentPropertyList = parentPropertyList;
        this.namespace = space;
        this.element = el;
    }

    /**
     * @param fobj the FObj object to which this propertyList should be attached
     */
    public void setFObj(FObj fobj) {
        this.fobj = fobj;
    }

    /**
     * @return the FObj object to which this propertyList is attached
     */
    public FObj getFObj() {
        return this.fobj;
    }

    /**
     * @return the FObj object attached to the parentPropetyList
     */
    public FObj getParentFObj() {
        if (parentPropertyList != null) {
            return parentPropertyList.getFObj();
        } else {
            return null;
        }
    }

    /**
     * Return the value explicitly specified on this FO.
     * @param propertyName The name of the property whose value is desired.
     * It may be a compound name, such as space-before.optimum.
     * @return The value if the property is explicitly set or set by
     * a shorthand property, otherwise null.
     */
    public Property getExplicitOrShorthand(String propertyName) {
        /* Handle request for one part of a compound property */
        int sepchar = propertyName.indexOf('.');
        String baseName;
        if (sepchar > -1) {
            baseName = propertyName.substring(0, sepchar);
        } else {
            baseName = propertyName;
        }
        Property p = getExplicitBaseProp(baseName);
        if (p == null) {
            p = builder.getShorthand(this, namespace, element, baseName);
        }
        if (p != null && sepchar > -1) {
            return builder.getSubpropValue(namespace, element, baseName, p,
                                           propertyName.substring(sepchar
                                           + 1));
        }
        return p;
    }

    /**
     * Return the value explicitly specified on this FO.
     * @param propertyName The name of the property whose value is desired.
     * It may be a compound name, such as space-before.optimum.
     * @return The value if the property is explicitly set, otherwise null.
     */
    public Property getExplicit(String propertyName) {
        /* Handle request for one part of a compound property */
        int sepchar = propertyName.indexOf('.');
        if (sepchar > -1) {
            String baseName = propertyName.substring(0, sepchar);
            Property p = getExplicitBaseProp(baseName);
            if (p != null) {
                return this.builder.getSubpropValue(namespace, element,
                                                    baseName, p,
                                                    propertyName.substring(sepchar
                                                    + 1));
            } else {
                return null;
            }
        }
        return (Property)super.get(propertyName);
    }

    /**
     * Return the value explicitly specified on this FO.
     * @param propertyName The name of the base property whose value is desired.
     * @return The value if the property is explicitly set, otherwise null.
     */
    public Property getExplicitBaseProp(String propertyName) {
        return (Property)super.get(propertyName);
    }

    /**
     * Return the value of this property inherited by this FO.
     * Implements the inherited-property-value function.
     * The property must be inheritable!
     * @param propertyName The name of the property whose value is desired.
     * @return The inherited value, otherwise null.
     */
    public Property getInherited(String propertyName) {
        if (builder != null) {
            if (parentPropertyList != null
                    && builder.isInherited(namespace, element,
                                           propertyName)) {
                return parentPropertyList.get(propertyName);
            } else {
                // return the "initial" value
                try {
                    return builder.makeProperty(this, namespace, element,
                                                propertyName);
                } catch (org.apache.fop.apps.FOPException e) {
                    //log.error("Exception in getInherited(): property="
                    //                       + propertyName + " : " + e);
                }
            }
        }
        return null;    // No builder or exception in makeProperty!
    }

    /*
     * If the property is a relative property with a corresponding absolute
     * value specified, the absolute value is used. This is also true of
     * the inheritance priority (I think...)
     * If the property is an "absolute" property and it isn't specified, then
     * we try to compute it from the corresponding relative property: this
     * happends in computeProperty.
     */
    private Property findProperty(String propertyName, boolean bTryInherit) {
        Property p = null;
        if (builder.isCorrespondingForced(this, namespace, element,
                                          propertyName)) {
            p = builder.computeProperty(this, namespace, element,
                                        propertyName);
        } else {
            p = getExplicitBaseProp(propertyName);
            if (p == null) {
                p = this.builder.computeProperty(this, namespace, element,
                                                 propertyName);
            }
            if (p == null) {    // check for shorthand specification
                p = builder.getShorthand(this, namespace, element,
                                         propertyName);
            }
            if (p == null
                    && bTryInherit) {    // else inherit (if has parent and is inheritable)
                if (this.parentPropertyList != null
                        && builder.isInherited(namespace, element,
                                               propertyName)) {
                    p = parentPropertyList.findProperty(propertyName, true);
                }
            }
        }
        return p;
    }


    /**
     * Return the property on the current FlowObject if it is specified, or if a
     * corresponding property is specified. If neither is specified, it returns null.
     * @param propertyName name of property
     * @return the Property corresponding to that name
     */
    public Property getSpecified(String propertyName) {
        return get(propertyName, false, false);
    }


    /**
     * Return the property on the current FlowObject. If it isn't set explicitly,
     * this will try to compute it based on other properties, or if it is
     * inheritable, to return the inherited value. If all else fails, it returns
     * the default value.
     * @param propertyName property name
     * @return the Property corresponding to that name
     */
    public Property get(String propertyName) {
        return get(propertyName, true, true);
    }

    /**
     * Return the property on the current FlowObject. Depending on the passed flags,
     * this will try to compute it based on other properties, or if it is
     * inheritable, to return the inherited value. If all else fails, it returns
     * the default value.
     */
    private Property get(String propertyName, boolean bTryInherit,
                         boolean bTryDefault) {

        if (builder == null) {
            //log.error("OH OH, builder has not been set");
        }
            /* Handle request for one part of a compound property */
        int sepchar = propertyName.indexOf('.');
        String subpropName = null;
        if (sepchar > -1) {
            subpropName = propertyName.substring(sepchar + 1);
            propertyName = propertyName.substring(0, sepchar);
        }

        Property p = findProperty(propertyName, bTryInherit);
        if (p == null && bTryDefault) {    // default value for this FO!
            try {
                p = this.builder.makeProperty(this, namespace, element,
                                              propertyName);
            } catch (FOPException e) {
                // don't know what to do here
            }
        }

        // if value is inherit then get computed value from
        // parent
        if (p != null && "inherit".equals(p.getSpecifiedValue())) {
            if (this.parentPropertyList != null) {
                p = parentPropertyList.get(propertyName, true, false);
            }
        }

        if (subpropName != null && p != null) {
            return this.builder.getSubpropValue(namespace, element,
                                                propertyName, p, subpropName);
        } else {
            return p;
        }
    }

    /**
     *
     * @param builder the PropertyListBuilder to attache to this object
     */
    public void setBuilder(PropertyListBuilder builder) {
        this.builder = builder;
    }

    /**
     * @return the namespace of this element
     */
    public String getNameSpace() {
        return namespace;
    }

    /**
     * @return element name for this
     */
    public String getElement() {
        return element;
    }

    /**
     * Return the "nearest" specified value for the given property.
     * Implements the from-nearest-specified-value function.
     * @param propertyName The name of the property whose value is desired.
     * @return The computed value if the property is explicitly set on some
     * ancestor of the current FO, else the initial value.
     */
    public Property getNearestSpecified(String propertyName) {
        Property p = null;
        for (PropertyList plist = this; p == null && plist != null;
                plist = plist.parentPropertyList) {
            p = plist.getExplicit(propertyName);
        }
        if (p == null) {
            // If no explicit setting found, return initial (default) value.
            try {
                p = this.builder.makeProperty(this, namespace, element,
                                              propertyName);
            } catch (FOPException e) {
                //log.error("Exception in getNearestSpecified(): property="
                //                       + propertyName + " : " + e);
            }
        }
        return p;
    }

    /**
     * Return the value of this property on the parent of this FO.
     * Implements the from-parent function.
     * @param propertyName The name of the property whose value is desired.
     * @return The computed value on the parent or the initial value if this
     * FO is the root or is in a different namespace from its parent.
     */
    public Property getFromParent(String propertyName) {
        if (parentPropertyList != null) {
            return parentPropertyList.get(propertyName);
        } else if (builder != null) {
            // return the "initial" value
            try {
                return builder.makeProperty(this, namespace, element,
                                            propertyName);
            } catch (org.apache.fop.apps.FOPException e) {
                //log.error("Exception in getFromParent(): property="
                //                       + propertyName + " : " + e);
            }
        }
        return null;    // No builder or exception in makeProperty!
    }

    /**
     * Uses the stored writingMode.
     * @param absdir an absolute direction (top, bottom, left, right)
     * @return the corresponding writing model relative direction name
     * for the flow object.
     */
    public String wmAbsToRel(int absdir) {
        if (wmtable != null) {
            return REL_NAMES[wmtable[absdir]];
        } else {
            return "";
        }
    }

    /**
     * Uses the stored writingMode.
     * @param reldir a writing mode relative direction (start, end, before, after)
     * @return the corresponding absolute direction name for the flow object.
     */
    public String wmRelToAbs(int reldir) {
        if (wmtable != null) {
            for (int i = 0; i < wmtable.length; i++) {
                if (wmtable[i] == reldir) {
                    return ABS_NAMES[i];
                }
            }
        }
        return "";
    }

    /**
     * Set the writing mode traits for the FO with this property list.
     * @param writingMode the writing-mode property to be set for this object
     */
    public void setWritingMode(int writingMode) {
        this.wmtable = (byte[])WRITING_MODE_TABLES.get(new Integer(writingMode));
    }

}

