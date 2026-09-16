package gm.engine.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class GmCommissionXml {

    @XmlValue
    private Integer value;

    @XmlAttribute(name = "type")
    private String type;

    public Integer getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
