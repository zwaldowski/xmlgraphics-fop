/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AreaClass;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.fo.properties.FOPropertyMapping;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AreaClass;
import org.apache.fop.layout.LinkSet;
import org.apache.fop.fo.flow.Marker;

// Java
import java.util.Iterator;
import org.xml.sax.Attributes;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * base class for representation of formatting objects and their processing
 */
public class FObj extends FONode {
    public PropertyList properties;
    protected PropertyManager propMgr;
    protected String areaClass = AreaClass.UNASSIGNED;

    /**
     * value of marker before layout begins
     */
    public final static int START = -1000;

    /**
     * value of marker after break-after
     */
    public final static int BREAK_AFTER = -1001;

    /**
     * where the layout was up to.
     * for FObjs it is the child number
     * for FOText it is the character number
     */
    protected int marker = START;

    protected ArrayList children = new ArrayList();    // made public for searching for id's

    protected boolean isInTableCell = false;

    protected int forcedStartOffset = 0;
    protected int forcedWidth = 0;

    protected int widows = 0;
    protected int orphans = 0;

    protected LinkSet linkSet;

    // count of areas generated-by/returned-by
    public int areasGenerated = 0;

    // markers
    protected HashMap markers;

    public FObj(FONode parent) {
        super(parent);
        markers = new HashMap();
        if (parent instanceof FObj)
            this.areaClass = ((FObj)parent).areaClass;
    }

    public void setName(String str) {
        name = "fo:" + str;
    }

    protected static PropertyListBuilder plb = null;

    protected PropertyListBuilder getListBuilder() {
        if(plb == null) {
            plb = new PropertyListBuilder();
            plb.addList(FOPropertyMapping.getGenericMappings());

            for (Iterator iter = FOPropertyMapping.getElementMappings().iterator();
                    iter.hasNext(); ) {
                String elem = (String)iter.next();
                plb.addElementList(elem, FOPropertyMapping.getElementMapping(elem));
            }
        }
        return plb;
    }

    /**
     * Handle the attributes for this element.
     * The attributes must be used immediately as the sax attributes
     * will be altered for the next element.
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        String uri = "http://www.w3.org/1999/XSL/Format";
        FONode par = parent;
        while(par != null && !(par instanceof FObj)) {
            par = par.parent;
        }
        PropertyList props = null;
        if(par != null) {
            props = ((FObj)par).properties;
        }
        properties =
                    getListBuilder().makeList(uri, name, attlist,
                                                props, (FObj)par);
        properties.setFObj(this);
        this.propMgr = makePropertyManager(properties);
        setWritingMode();
    }

    protected PropertyManager makePropertyManager(PropertyList propertyList) {
        return new PropertyManager(propertyList);
    }

    protected void addChild(FONode child) {
        children.add(child);
    }

    /**
     * lets outside sources access the property list
     * first used by PageNumberCitation to find the "id" property
     * @param name - the name of the desired property to obtain
     * @return the property
     */
    public Property getProperty(String name) {
        return (properties.get(name));
    }

    /**
     * Return the "content width" of the areas generated by this FO.
     * This is used by percent-based properties to get the dimension of
     * the containing block.
     * If an FO has a property with a percentage value, that value
     * is usually calculated on the basis of the corresponding dimension
     * of the area which contains areas generated by the FO.
     * NOTE: subclasses of FObj should implement this to return a reasonable
     * value!
     */
    public int getContentWidth() {
        return 0;
    }

    /**
     * removes property id
     * @param idReferences the id to remove
     */
    public void removeID(IDReferences idReferences) {
        if (((FObj)this).properties.get("id") == null
                || ((FObj)this).properties.get("id").getString() == null)
            return;
        idReferences.removeID(((FObj)this).properties.get("id").getString());
        int numChildren = this.children.size();
        for (int i = 0; i < numChildren; i++) {
            FONode child = (FONode)children.get(i);
            if ((child instanceof FObj)) {
                ((FObj)child).removeID(idReferences);
            }
        }
    }

    public boolean generatesReferenceAreas() {
        return false;
    }


    public boolean generatesInlineAreas() {
        return true;
    }

    /**
     * Set writing mode for this FO.
     * Find nearest ancestor, including self, which generates
     * reference areas and use the value of its writing-mode property.
     * If no such ancestor is found, use the value on the root FO.
     */
    protected void setWritingMode() {
        FObj p;
        FONode parent;
        for (p = this;
                !p.generatesReferenceAreas() && (parent = p.getParent()) != null && (parent instanceof FObj);
                p = (FObj)parent);
        this.properties.setWritingMode(p.getProperty("writing-mode").getEnum());
    }

    /**
     * Return a LayoutManager responsible for laying out this FObj's content.
     * Must override in subclasses if their content can be laid out.
     */
    public LayoutManager getLayoutManager() {
	return null;
    }

    /**
     * Return an iterator over all the children of this FObj.
     * @return A ListIterator.
     */
    public ListIterator getChildren() {
	return children.listIterator();
    }

    /**
     * Return an iterator over the object's children starting
     * at the pased node.
     * @param childNode First node in the iterator
     * @return A ListIterator or null if childNode isn't a child of
     * this FObj.
     */
    public ListIterator getChildren(FONode childNode) {
	int i = children.indexOf(childNode);
	if (i >= 0) {
	    return children.listIterator(i);
	}
	else return null;
    }

    public void setIsInTableCell() {
        this.isInTableCell = true;
        // made recursive by Eric Schaeffer
        for (int i = 0; i < this.children.size(); i++) {
            Object obj = this.children.get(i);
            if(obj instanceof FObj) {
                FObj child = (FObj)obj;
                child.setIsInTableCell();
            }
        }
    }
    
    public void forceStartOffset(int offset) {
        this.forcedStartOffset = offset; 
        // made recursive by Eric Schaeffer
        for (int i = 0; i < this.children.size(); i++) {
            Object obj = this.children.get(i);
            if(obj instanceof FObj) {
                FObj child = (FObj)obj;
                child.forceStartOffset(offset);
            }
        }
    }

    public void forceWidth(int width) {
        this.forcedWidth = width;
        // made recursive by Eric Schaeffer
        for (int i = 0; i < this.children.size(); i++) {
            Object obj = this.children.get(i);
            if(obj instanceof FObj) {
                FObj child = (FObj)obj;
                child.forceWidth(width);
            }
        }
    }

    public void resetMarker() {
        this.marker = START;
        int numChildren = this.children.size();
        for (int i = 0; i < numChildren; i++) {
            Object obj = this.children.get(i);
            if(obj instanceof FObj) {
                FObj child = (FObj)obj;
                child.resetMarker();
            }
        }
    }

    public void setWidows(int wid) {
        widows = wid;
    }
    
    public void setOrphans(int orph) {
        orphans = orph;
    }

    public void removeAreas() {
        // still to do
    }

    public void setLinkSet(LinkSet linkSet) {
        this.linkSet = linkSet;
        for (int i = 0; i < this.children.size(); i++) {
            Object obj = this.children.get(i);
            if(obj instanceof FObj) {
                FObj child = (FObj)obj;
                child.setLinkSet(linkSet);
            }
        }
    }

    public LinkSet getLinkSet() {
        return this.linkSet;
    }

    /**
     * At the start of a new span area layout may be partway through a
     * nested FO, and balancing requires rollback to this known point.
     * The snapshot records exactly where layout is at.
     * @param snapshot a ArrayList of markers (Integer)
     * @returns the updated ArrayList of markers (Integers)
     */
    public ArrayList getMarkerSnapshot(ArrayList snapshot) {
        snapshot.add(new Integer(this.marker));

        // terminate if no kids or child not yet accessed
        if (this.marker < 0)
            return snapshot;
        else if (children.isEmpty())
            return snapshot;
        else
            return ((FObj)children.get(this.marker)).getMarkerSnapshot(snapshot);
    }

    /**
     * When balancing occurs, the flow layout() method restarts at the
     * point specified by the current marker snapshot, which is retrieved
     * and restored using this method.
     * @param snapshot the ArrayList of saved markers (Integers)
     */
    public void rollback(ArrayList snapshot) {
        this.marker = ((Integer)snapshot.get(0)).intValue();
        snapshot.remove(0);

        if (this.marker == START) {
            // make sure all the children of this FO are also reset
            resetMarker();
            return;
        } else if ((this.marker == -1) || children.isEmpty())
            return;

        int numChildren = this.children.size();

        if (this.marker <= START) {
            return;
        }

        for (int i = this.marker + 1; i < numChildren; i++) {
            Object obj = this.children.get(i);
            if(obj instanceof FObj) {
                FObj child = (FObj)obj;
                child.resetMarker();
            }
        }
        ((FObj)children.get(this.marker)).rollback(snapshot);
    }


    public void addMarker(Marker marker) throws FOPException {
        String mcname = marker.getMarkerClassName();
        if (!markers.containsKey(mcname) && children.isEmpty()) {
            markers.put(mcname, marker);
        } else {
            log.error("fo:marker must be an initial child,"
                                   + "and 'marker-class-name' must be unique for same parent");
            throw new FOPException("fo:marker must be an initial child,"
                                   + "and 'marker-class-name' must be unique for same parent");
        }
    }

    public boolean hasMarkers() {
        return !markers.isEmpty();
    }

    public ArrayList getMarkers() {
        return new ArrayList(markers.values());
    }
}

