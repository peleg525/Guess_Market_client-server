package gm.engine.xml;

import gm.engine.exception.GmFileException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Reads and validates a Guess Market XML file (Exercise 3 format), turning it into a list of
 * validated {@link GmEventXml} entries ready to be built into real {@link gm.engine.model.Event}
 * objects by the caller (which alone knows the uploading user's name and the next event id to
 * assign - neither of which appears in the file anymore).
 * <p>
 * Unlike Exercise 1/2, the file never touches disk: it arrives as an in-memory byte array from an
 * HTTP upload and is validated straight out of memory. Validation happens in three stages, and
 * every problem found at a given stage is reported together rather than stopping at the first one:
 * <ol>
 *     <li>file name / content presence</li>
 *     <li>XSD schema validity</li>
 *     <li>application-level rules the schema cannot express (commission range, option count,
 *     positive b/d, and event names that collide with each other or with events already known to
 *     the engine)</li>
 * </ol>
 */
public class GmFileLoader {

    private static final String SCHEMA_RESOURCE = "/gm/engine/xml/GM-EX3-Schema.xsd";
    private static final int MIN_COMMISSION = 0;
    private static final int MAX_COMMISSION = 90;
    private static final int REQUIRED_OPTION_COUNT = 2;

    public List<GmEventXml> load(String fileName, byte[] xmlContent, Set<String> existingEventNamesLowerCase) {
        validateFileNameAndContent(fileName, xmlContent);
        GuessMarketXml root = parseWithSchemaValidation(xmlContent);
        List<GmEventXml> events = (root.getEvents() == null) ? List.of() : root.getEvents().getEvent();

        List<String> problems = new ArrayList<>();
        validateEvents(events, existingEventNamesLowerCase, problems);
        if (!problems.isEmpty()) {
            throw new GmFileException(problems);
        }
        return events;
    }

    private void validateFileNameAndContent(String fileName, byte[] xmlContent) {
        if (fileName == null || fileName.isBlank()) {
            throw new GmFileException("No file was selected.");
        }
        if (!fileName.trim().toLowerCase(Locale.ROOT).endsWith(".xml")) {
            throw new GmFileException("The file name must end with a \".xml\" extension: \"" + fileName + "\"");
        }
        if (xmlContent == null || xmlContent.length == 0) {
            throw new GmFileException("The file \"" + fileName + "\" is empty.");
        }
    }

    private GuessMarketXml parseWithSchemaValidation(byte[] xmlContent) {
        List<String> schemaProblems = new ArrayList<>();
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema;
            try (InputStream xsdStream = GmFileLoader.class.getResourceAsStream(SCHEMA_RESOURCE)) {
                schema = schemaFactory.newSchema(new StreamSource(xsdStream));
            }
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                    // ignored: warnings do not make the file invalid
                }

                @Override
                public void error(SAXParseException exception) {
                    schemaProblems.add(describe(exception));
                }

                @Override
                public void fatalError(SAXParseException exception) {
                    schemaProblems.add(describe(exception));
                }
            });
            validator.validate(new StreamSource(new ByteArrayInputStream(xmlContent)));
        } catch (SAXException | IOException e) {
            schemaProblems.add("The file could not be parsed as XML: " + e.getMessage());
        }

        if (!schemaProblems.isEmpty()) {
            throw new GmFileException(schemaProblems);
        }

        try {
            JAXBContext context = JAXBContext.newInstance(GuessMarketXml.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (GuessMarketXml) unmarshaller.unmarshal(new ByteArrayInputStream(xmlContent));
        } catch (JAXBException e) {
            throw new GmFileException("The file could not be read: " + e.getMessage());
        }
    }

    private String describe(SAXParseException e) {
        return "Line " + e.getLineNumber() + ": " + e.getMessage();
    }

    private void validateEvents(List<GmEventXml> events, Set<String> existingEventNamesLowerCase, List<String> problems) {
        Map<String, Integer> nameCountsInFile = new HashMap<>();

        for (GmEventXml event : events) {
            String label = describeEvent(event);
            String lowerName = event.getName() == null ? null : event.getName().trim().toLowerCase(Locale.ROOT);
            if (lowerName != null) {
                nameCountsInFile.merge(lowerName, 1, Integer::sum);
                if (existingEventNamesLowerCase.contains(lowerName)) {
                    problems.add("Event " + label + " has the same name as an event that already exists "
                            + "in the system. Event names must be unique across the whole system.");
                }
            }

            Integer commission = event.getCommission() != null ? event.getCommission().getValue() : null;
            if (commission != null && (commission < MIN_COMMISSION || commission > MAX_COMMISSION)) {
                problems.add("Event " + label + " has an invalid commission of " + commission
                        + "%. Commission must be between " + MIN_COMMISSION + " and " + MAX_COMMISSION + " (inclusive).");
            }

            int optionCount = (event.getOptions() != null) ? event.getOptions().getOption().size() : 0;
            if (optionCount != REQUIRED_OPTION_COUNT) {
                problems.add("Event " + label + " must have exactly two options, but has " + optionCount + ".");
            }

            GmMethodXml method = event.getMethod();
            if (method != null && method.getLmsr() != null) {
                Integer b = method.getLmsr().getB();
                if (b != null && b <= 0) {
                    problems.add("Event " + label + " has a non-positive liquidity value (b=" + b
                            + "). b must be a positive integer.");
                }
            } else if (method != null && method.getOrderBook() != null) {
                GmOrderBookXml ob = method.getOrderBook();
                if (ob.getD() != null && ob.getD() <= 0) {
                    problems.add("Event " + label + " has a non-positive base value (d=" + ob.getD()
                            + "). d must be a positive integer.");
                }
                if (ob.getInitial() != null && ob.getInitial() < 0) {
                    problems.add("Event " + label + " has a negative initial investment (" + ob.getInitial() + ").");
                }
            }
        }

        nameCountsInFile.forEach((name, count) -> {
            if (count > 1) {
                problems.add("Event name '" + name + "' is used by " + count
                        + " events in this file. Event names must be unique.");
            }
        });
    }

    private String describeEvent(GmEventXml event) {
        String namePart = (event.getName() != null) ? "'" + event.getName() + "'" : "(no name)";
        return namePart;
    }
}
