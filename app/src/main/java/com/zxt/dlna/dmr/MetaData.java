package com.zxt.dlna.dmr;

import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lantern on 2017/8/13.
 */

public class MetaData {
    private static enum State {
        IGNORE,
        TYPE,
        NAME,
        ARTIST,
        ALBUM,
        PICTURE
    }
    private static final Map<String, State> States = new HashMap<String, State>(){
        {
            put("upnp:class", State.TYPE);
            put("dc:title", State.NAME);
            put("dc:creator", State.ARTIST);
            put("upnp:artist", State.ARTIST);
            put("upnp:album", State.ALBUM);
            put("upnp:albumArtURI", State.PICTURE);
        }
    };
    public String name;
    public String type;
    public String artist;
    public String album;
    public String picture;

    public static MetaData parse(String metaData) throws SAXException {
        final MetaData data = new MetaData();
        Xml.parse(metaData, new DefaultHandler(){
            State state = State.IGNORE;
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                state = State.IGNORE;
                if(qName != null){
                    State state0 = States.get(qName);
                    if(state0 != null){
                        state = state0;
                    }
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                if(state != State.IGNORE){
                    String text = new String(ch, start, length);
                    switch (state){
                        case NAME:
                            data.name = text;
                            break;
                        case TYPE:
                            if (text.startsWith("object.item.videoItem")) {
                                data.type = MediaType.VIDEO;
                            } else if (text.startsWith("object.item.imageItem")) {
                                data.type = MediaType.IMAGE;
                            } else if (text.startsWith("object.item.audioItem")) {
                                data.type = MediaType.AUDIO;
                            }
                            break;
                        case ARTIST:
                            data.artist = text;
                            break;
                        case ALBUM:
                            data.album = text;
                            break;
                        case PICTURE:
                            data.picture = text;
                            break;
                    }
                }
                super.characters(ch, start, length);
            }
        });

        return data;
    }
}
