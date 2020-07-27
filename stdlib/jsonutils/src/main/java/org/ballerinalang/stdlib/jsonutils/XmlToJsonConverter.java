/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.stdlib.jsonutils;

import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLText;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BXML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Convert Ballerina XML value into respective JSON value.
 * @since 1.2.5
 */
public class XmlToJsonConverter {

    private static final String XML_NAMESPACE_PREFIX_FRAGMENT = "xmlns:";

    private static final BType jsonMapType =
            new BMapType(TypeConstants.MAP_TNAME, BTypes.typeJSON, new BPackage(null, null, null));
    private static final String XMLNS = "xmlns";

    /**
     * Converts given xml object to the corresponding JSON value.
     *
     * @param xml                XML object to get the corresponding json
     * @param attributePrefix    Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return JSON representation of the given xml object
     */
    public static Object convertToJSON(XMLValue xml, String attributePrefix, boolean preserveNamespaces) {
        if (xml instanceof XMLText) {
            return JSONParser.parse("\"" + ((XMLText) xml).stringValue() + "\"");
        } else if (xml instanceof XMLItem) {
            return convertElement((XMLItem) xml, attributePrefix, preserveNamespaces);
        } else if (xml instanceof XMLSequence) {
            XMLSequence xmlSequence = (XMLSequence) xml;
            if (xmlSequence.isEmpty()) {
                return newJsonList();
            }
            Object seq = convertXMLSequence(xmlSequence, attributePrefix, preserveNamespaces);
            if (seq == null) {
                return newJsonList();
            }
            return seq;
        }
        return newJsonMap();
    }
    /**
     * Converts given xml object to the corresponding json.
     *
     * @param xmlItem XML element to traverse
     * @param attributePrefix Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return ObjectNode Json object node corresponding to the given xml element
     */
    private static Object convertElement(XMLItem xmlItem, String attributePrefix,
                                                               boolean preserveNamespaces) {
        MapValueImpl<String, Object> rootNode = newJsonMap();
        LinkedHashMap<String, String> attributeMap = collectAttributesAndNamespaces(xmlItem, preserveNamespaces);
        String keyValue = getElementKey(xmlItem, preserveNamespaces);
        Object children = convertXMLSequence(xmlItem.getChildrenSeq(), attributePrefix, preserveNamespaces);

        if (attributeMap.isEmpty() && children == null) {
            rootNode.put(keyValue, "");
            return rootNode;
        }
        if (children != null) {
            rootNode.put(keyValue, children);
        }
        if (!attributeMap.isEmpty()) {
            addAttributes(rootNode, attributePrefix, attributeMap);
        }
        return rootNode;
    }

    private static void addAttributes(MapValueImpl<String, Object> rootNode, String attributePrefix,
                                      LinkedHashMap<String, String> attributeMap) {
        for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
            rootNode.put(attributePrefix + entry.getKey(), entry.getValue());
        }
    }

    /**
     * Converts given xml sequence to the corresponding json.
     *
     * @param xmlSequence XML sequence to traverse
     * @param attributePrefix Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return JsonNode Json node corresponding to the given xml sequence
     */
    private static Object convertXMLSequence(XMLSequence xmlSequence, String attributePrefix,
                                             boolean preserveNamespaces) {
        List<BXML> sequence = xmlSequence.getChildrenList();
        if (sequence.isEmpty()) {
            return null;
        }

        switch (calculateMatchingJsonTypeForSequence(sequence)) {
            case SAME_KEY:
                return convertSequenceWithSameNamedElements(attributePrefix, preserveNamespaces, sequence);
            case ELEMENT_ONLY:
                return convertSequenceWithOnlyElements(attributePrefix, preserveNamespaces, sequence);
            default:
                return convertHeterogeneousSequence(attributePrefix, preserveNamespaces, sequence);
        }
    }

    private static Object convertHeterogeneousSequence(String attributePrefix, boolean preserveNamespaces,
                                                       List<BXML> sequence) {
        if (sequence.size() == 1) {
            return convertToJSON((XMLValue) sequence.get(0), attributePrefix, preserveNamespaces);
        }

        ArrayList<Object> list = new ArrayList<>();
        for (BXML bxml : sequence) {
            if (isCommentOrPi(bxml)) {
                continue;
            }
            list.add(convertToJSON((XMLValue) bxml, attributePrefix, preserveNamespaces));
        }

        if (list.isEmpty()) {
            return null;
        }
        return newJsonListFrom(list);
    }

    private static Object convertSequenceWithOnlyElements(String attributePrefix, boolean preserveNamespaces,
                                                          List<BXML> sequence) {
        MapValueImpl<String, Object> elementObj = newJsonMap();
        for (BXML bxml : sequence) {
            // Skip comments and PI items.
            if (isCommentOrPi(bxml)) {
                continue;
            }
            String elementName = getElementKey((XMLItem) bxml, preserveNamespaces);
            Object elemAsJson = convertElement((XMLItem) bxml, attributePrefix, preserveNamespaces);
            if (elemAsJson instanceof MapValueImpl) {
                @SuppressWarnings("unchecked")
                MapValueImpl<String, Object> mapVal = (MapValueImpl<String, Object>) elemAsJson;
                if (mapVal.size() == 1) {
                    Object val = mapVal.get(elementName);
                    if (val != null) {
                        elementObj.put(elementName, val);
                        continue;
                    }
                }
            }
            elementObj.put(elementName, elemAsJson);
        }
        return elementObj;
    }

    private static boolean isCommentOrPi(BXML bxml) {
        return bxml.getNodeType() == XMLNodeType.COMMENT || bxml.getNodeType() == XMLNodeType.PI;
    }

    private static Object convertSequenceWithSameNamedElements(String attributePrefix, boolean preserveNamespaces,
                                                               List<BXML> sequence) {
        String elementName = null;
        for (BXML bxml : sequence) {
            if (bxml.getNodeType() == XMLNodeType.ELEMENT) {
                elementName = bxml.getElementName();
                break;
            }
        }
        MapValueImpl<String, Object> listWrapper = newJsonMap();
        ArrayValueImpl arrayValue = convertChildrenToJsonList(sequence, attributePrefix, preserveNamespaces);
        listWrapper.put(elementName, arrayValue);
        return listWrapper;
    }

    private static ArrayValueImpl convertChildrenToJsonList(List<BXML> sequence, String prefix,
                                                            boolean preserveNamespaces) {
        List<Object> list = new ArrayList<>();
        for (BXML child : sequence) {
            if (isCommentOrPi(child)) {
                continue;
            }
            if (child.getAttributesMap().isEmpty()) {
                list.add(convertToJSON((XMLValue) child.children(), prefix, preserveNamespaces));
            } else {
                list.add(convertElement((XMLItem) child, prefix, preserveNamespaces));
            }
        }
        return newJsonListFrom(list);
    }

    private static SequenceConvertibility calculateMatchingJsonTypeForSequence(List<BXML> sequence) {
        Iterator<BXML> iterator = sequence.iterator();
        BXML next = iterator.next();
        if (next.getNodeType() == XMLNodeType.TEXT) {
            return SequenceConvertibility.LIST;
        }
        // Scan until first convertible item is found.
        while (iterator.hasNext() && (isCommentOrPi(next))) {
            next = iterator.next();
            if (next.getNodeType() == XMLNodeType.TEXT) {
                return SequenceConvertibility.LIST;
            }
        }

        String firstElementName = next.getElementName();
        int i = 0;
        boolean sameElementName = true;
        for (; iterator.hasNext(); i++) {
            BXML val = iterator.next();
            if (val.getNodeType() == XMLNodeType.ELEMENT) {
                if (!((XMLItem) val).getElementName().equals(firstElementName)) {
                    sameElementName = false;
                }
            } else if (val.getNodeType() == XMLNodeType.TEXT) {
                return SequenceConvertibility.LIST;
            } else {
                i--; // we don't want `i` to count up for comments and PI items
            }
        }
        return (sameElementName && i > 0) ? SequenceConvertibility.SAME_KEY : SequenceConvertibility.ELEMENT_ONLY;
    }

    private static ArrayValueImpl newJsonList() {
        return new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
    }

    public static ArrayValueImpl newJsonListFrom(List<Object> items) {
        return new ArrayValueImpl(items.toArray(), new BArrayType(BTypes.typeJSON));
    }

    private static MapValueImpl<String, Object> newJsonMap() {
        return new MapValueImpl<>(jsonMapType);
    }

    /**
     * Extract attributes and namespaces from the XML element.
     *
     * @param element XML element to extract attributes and namespaces
     * @param preserveNamespaces should namespace attribute be preserved
     */
    private static LinkedHashMap<String, String> collectAttributesAndNamespaces(XMLItem element,
                                                                                boolean preserveNamespaces) {
        int nsPrefixBeginIndex = XMLItem.XMLNS_URL_PREFIX.length();
        LinkedHashMap<String, String> attributeMap = new LinkedHashMap<>();
        Map<String, String> nsPrefixMap = new HashMap<>();
        for (Map.Entry<String, String> entry : element.getAttributesMap().entrySet()) {
            if (entry.getKey().startsWith(XMLItem.XMLNS_URL_PREFIX)) {
                String prefix = entry.getKey().substring(nsPrefixBeginIndex);
                String ns = entry.getValue();
                nsPrefixMap.put(ns, prefix);
                if (preserveNamespaces) {
                    if (prefix.equals(XMLNS)) {
                        attributeMap.put(prefix, ns);
                    } else {
                        attributeMap.put(XML_NAMESPACE_PREFIX_FRAGMENT + prefix, ns);
                    }
                }
            }
        }
        for (Map.Entry<String, String> entry : element.getAttributesMap().entrySet()) {
            String key = entry.getKey();
            if (preserveNamespaces && !key.startsWith(XMLItem.XMLNS_URL_PREFIX)) {
                int nsEndIndex = key.lastIndexOf('}');
                if (nsEndIndex > 0) {
                    String ns = key.substring(1, nsEndIndex);
                    String local = key.substring(nsEndIndex + 1);
                    String nsPrefix = nsPrefixMap.get(ns);
                    // `!nsPrefix.equals("xmlns")` because attributes does not belong to default namespace.
                    if (nsPrefix != null && !nsPrefix.equals(XMLNS)) {
                        attributeMap.put(nsPrefix + ":" + local, entry.getValue());
                    } else {
                        attributeMap.put(local, entry.getValue());
                    }
                } else {
                    attributeMap.put(key, entry.getValue());
                }
            } else {
                String attrName = entry.getKey();
                int endOfBrace = attrName.indexOf('}');
                if (endOfBrace > 0) {
                    String localName = attrName.substring(endOfBrace + 1);
                    if (localName.equals(XMLNS)) {
                        continue;
                    }
                    attributeMap.put(localName, entry.getValue());
                } else {
                    attributeMap.put(attrName, entry.getValue());
                }
            }
        }
        return attributeMap;
    }

    /**
     * Extract the key from the element with namespace information.
     *
     * @param xmlItem XML element for which the key needs to be generated
     * @param preserveNamespaces Whether namespace info included in the key or not
     * @return String Element key with the namespace information
     */
    private static String getElementKey(XMLItem xmlItem, boolean preserveNamespaces) {
        // Construct the element key based on the namespaces
        StringBuilder elementKey = new StringBuilder();
        QName qName = xmlItem.getQName();
        if (preserveNamespaces) {
            String prefix = qName.getPrefix();
            if (prefix != null && !prefix.isEmpty()) {
                elementKey.append(prefix).append(':');
            }
        }
        elementKey.append(qName.getLocalPart());
        return elementKey.toString();
    }

    private enum SequenceConvertibility {
        SAME_KEY, ELEMENT_ONLY, LIST
    }
}
