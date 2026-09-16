package gm.engine.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * Exercise 3 schema: events no longer carry their own {@code id} (the server assigns one on
 * upload) and are no longer paired with a separate {@code GM-users} section - the uploader
 * becomes the market maker of every event in the file.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class GmEventXml {

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "commission")
    private GmCommissionXml commission;

    @XmlElement(name = "GM-options")
    private GmOptionsXml options;

    @XmlElement(name = "GM-method")
    private GmMethodXml method;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public GmCommissionXml getCommission() {
        return commission;
    }

    public GmOptionsXml getOptions() {
        return options;
    }

    public GmMethodXml getMethod() {
        return method;
    }
}
