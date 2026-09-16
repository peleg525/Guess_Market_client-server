package gm.engine.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class GmEventsXml {

    @XmlElement(name = "GM-event")
    private List<GmEventXml> event = new ArrayList<>();

    public List<GmEventXml> getEvent() {
        return event;
    }
}
