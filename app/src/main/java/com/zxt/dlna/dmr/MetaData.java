package com.zxt.dlna.dmr;

import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by lantern on 2017/8/13.
 */

public class MetaData {
    public String name;
    public String type;

    public static MetaData parse(String metaData) throws SAXException {
        final MetaData data = new MetaData();
        Xml.parse(metaData, new DefaultHandler(){
            int state = 0;
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                state = 0;
                if("dc:title".equals(qName)){
                    state = 1;
                }
                if("upnp:class".equals(qName)){
                    state = 2;
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                if(state != 0){
                    String text = new String(ch, start, length);
                    switch (state){
                        case 1:
                            data.name = text;
                            break;
                        case 2:
                            switch (text){
                                case "object.item.videoItem":
                                    data.type = MediaType.VIDEO;
                                    break;
                                case "object.item.imageItem":
                                    data.type = MediaType.IMAGE;
                                    break;
                                case "object.item.audioItem":
                                    data.type = MediaType.AUDIO;
                                    break;
                            }
                            break;
                    }
                }
                super.characters(ch, start, length);
            }
        });

        return data;
    }
}
