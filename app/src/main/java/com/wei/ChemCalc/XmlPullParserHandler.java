package com.wei.ChemCalc;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
/*

Last Modified by Wei Shi 12/07/2015

*/

public class XmlPullParserHandler {
    private List<Element> elements= new ArrayList<Element>();
    private Element element;
    private String text;

    public List<Element> getElement() {
        return elements;
    }

    public List<Element> parse(InputStream is) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(is, null);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equalsIgnoreCase("element")) {
                            // create a new instance of element
                            element = new Element();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
/*
    Integer _atomicNumber;
    String _symbol;
    String _name;
    Double _molarMass;

 */
                    case XmlPullParser.END_TAG:
                        if (tagName.equalsIgnoreCase("element")) {
                            // add element object to list
                            elements.add(element);
                        }
                        else if (tagName.equalsIgnoreCase("atomicNumber")) {
                            element.setAtomicNumber(Integer.parseInt(text));
                        }
                        else if (tagName.equalsIgnoreCase("symbol")) {
                            element.setSymbol(text);
                        }
                        else if (tagName.equalsIgnoreCase("name")) {

                            element.setName(text);
                        }
                        else if (tagName.equalsIgnoreCase("molarMass")) {

                            element.setMolarMass(Double.parseDouble(text));

                        }
                        break;

                    default:
                        break;
                }
                eventType = parser.next();
            }

        }
        catch (XmlPullParserException e) {e.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}

        return elements;
    }
}