package com.jon.cotgenerator.cot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* Class for getting/setting tags and attributes from an XML string. Yes I realise that regex is less easy to read, but it's a lot faster than
 * the Java libraries for XML parsing (in my experience, at least) */
public class XmlManager {
    /* Used as arguments to getElement and getAttribute to determine if an exception is thrown */
    static final int OPTIONAL = 0;
    static final int REQUIRED = 1;

    private String mXmlString;

    public XmlManager(String xml) {
        mXmlString = xml;
    }

    @Override
    public String toString() {
        return mXmlString;
    }

    public byte[] getBytes() {
        return mXmlString.getBytes();
    }

    public String getElement(String element, int required) throws CursorOnTarget.CotParsingException {
        final String regexPattern = "(?<=<" + element + ">)(.*?)(?=</" + element + ">)";
        return regexMatch(regexPattern, required);
    }

    public String getElement(String element) throws CursorOnTarget.CotParsingException {
        return getElement(element, REQUIRED);
    }

    public String getAttribute(String attribute, int required) throws CursorOnTarget.CotParsingException {
        final String regexPattern = "(?<= " + attribute + "=\")(.*?)(?=\".*/>)";
        return regexMatch(regexPattern, required);
    }

    public String getAttribute(String attribute) throws CursorOnTarget.CotParsingException {
        final String regexPattern = "(?<= " + attribute + "=\")(.*?)(?=\".*/>)";
        return regexMatch(regexPattern, REQUIRED);
    }

    private String regexMatch(String regexPattern, int required) throws CursorOnTarget.CotParsingException {
        final Matcher matcher = Pattern.compile(regexPattern).matcher(mXmlString);
        if (matcher.find()) {
            /* If the element exists, grab the first match. We only expect one in a CoT message */
            return matcher.group();
        } else if (required == OPTIONAL) {
            /* If the element doesn't exist in the XML but it's optional, return null */
            return null;
        } else if (required == REQUIRED) {
            /* If the element doesn't exist but it's required, throw exception because something's gone wrong */
            throw new CursorOnTarget.CotParsingException("No matches for regex pattern '" + regexPattern + "'");
        } else {
            throw new CursorOnTarget.CotParsingException("Something wacky has happened");
        }
    }

    public void setElement(String element, String value) {
        final String regexPattern = "(?<=<" + element + ">)(.*?)(?=</" + element + ">)";
        mXmlString = mXmlString.replaceAll(regexPattern, value);
    }

    public void setAttribute(String attribute, String value) {
        final String regexPattern = "(?<= " + attribute + "=\")(.*?)(?=\")";
        mXmlString = mXmlString.replaceAll(regexPattern, value);
    }

    public boolean elementExists(String element) {
        return mXmlString.contains("<" + element + ">") && mXmlString.contains("</" + element + ">");
    }

    public void addElement(String parentElement, String newElement, String contents) {
        if (!elementExists(parentElement)) {
            throw new IllegalStateException("Tried to place new XML element '" + newElement +
                    "' inside parent '" + parentElement + "', but this parent doesn't exist");
        }

        /* Replacing "</parent>" with "<new>contents</new></parent>" */
        final String parentEnd = "</" + parentElement + ">";
        final String newStart = "<" + newElement + ">";
        final String newEnd = "</" + newElement + ">";
        mXmlString = mXmlString.replaceAll(parentEnd, newStart + contents + newEnd + parentEnd);
    }

    public void setQuotes() {
        mXmlString = mXmlString.replaceAll("'", "\"");
    }
}
