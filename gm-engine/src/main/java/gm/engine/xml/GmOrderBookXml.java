package gm.engine.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class GmOrderBookXml {

    @XmlAttribute(name = "initial")
    private Integer initial;

    @XmlAttribute(name = "d")
    private Integer d;

    @XmlAttribute(name = "allow-mint")
    private String allowMint;

    public Integer getInitial() {
        return initial;
    }

    public Integer getD() {
        return d;
    }

    public String getAllowMint() {
        return allowMint;
    }
}
