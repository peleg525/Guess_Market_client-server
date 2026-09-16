package gm.engine.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class GmOptionsXml {

    @XmlElement(name = "GM-option")
    private List<GmOptionXml> option = new ArrayList<>();

    public List<GmOptionXml> getOption() {
        return option;
    }
}
