package gm.engine.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class GmLmsrXml {

    @XmlElement(name = "b")
    private Integer b;

    public Integer getB() {
        return b;
    }
}
