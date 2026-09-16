package gm.engine;

import gm.engine.dto.LoadResultDto;
import gm.engine.exception.GmFileException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GmFileLoaderTest {

    private static final String TESTFILES = "../testfiles/";

    private byte[] read(String name) throws IOException {
        return Files.readAllBytes(Path.of(TESTFILES + name));
    }

    @Test
    void uploadsSmallFile() throws IOException {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Mm");
        LoadResultDto result = engine.uploadEventsFile("Mm", "small.xml", read("small.xml"));
        assertEquals(1, result.getAddedEventCount());
        assertEquals(1, result.getTotalEventCount());
    }

    @Test
    void uploadsMultipleFile() throws IOException {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Mm");
        LoadResultDto result = engine.uploadEventsFile("Mm", "multiple.xml", read("multiple.xml"));
        assertEquals(3, result.getAddedEventCount());
        assertEquals(3, result.getTotalEventCount());
    }

    @Test
    void filesAccumulateAcrossUploads() throws IOException {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Mm1");
        engine.login("Mm2");
        engine.uploadEventsFile("Mm1", "small.xml", read("small.xml"));
        LoadResultDto second = engine.uploadEventsFile("Mm2", "multiple.xml", read("multiple.xml"));
        assertEquals(3, second.getAddedEventCount());
        assertEquals(4, second.getTotalEventCount());
        assertEquals(4, engine.getEvents(gm.engine.dto.EventFilter.all()).size());
    }

    @Test
    void uploaderBecomesMarketMakerOfEveryEventInTheFile() throws IOException {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Mm");
        engine.uploadEventsFile("Mm", "multiple.xml", read("multiple.xml"));
        for (var summary : engine.getEvents(gm.engine.dto.EventFilter.all())) {
            assertEquals("Mm", summary.getMarketMakerUsername());
        }
        assertEquals(3, engine.getUserDetail("Mm").getParticipations().size());
    }

    @Test
    void rejectsDuplicateEventNameAcrossUploads() throws IOException {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Mm");
        engine.uploadEventsFile("Mm", "small.xml", read("small.xml"));
        GmFileException ex = assertThrows(GmFileException.class,
                () -> engine.uploadEventsFile("Mm", "small.xml", read("small.xml")));
        assertTrue(ex.getMessage().contains("already exists"), ex.getMessage());
        assertEquals(1, engine.getEvents(gm.engine.dto.EventFilter.all()).size());
    }

    @Test
    void rejectsInvalidCommission() throws IOException {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Mm");
        GmFileException ex = assertThrows(GmFileException.class,
                () -> engine.uploadEventsFile("Mm", "invalid-commission.xml", read("invalid-commission.xml")));
        assertTrue(ex.getMessage().contains("commission of 95"), ex.getMessage());
    }

    @Test
    void rejectsUploadFromUnknownUser() throws IOException {
        GmEngineImpl engine = new GmEngineImpl();
        byte[] content = read("small.xml");
        assertThrows(gm.engine.exception.GmOperationException.class,
                () -> engine.uploadEventsFile("NeverLoggedIn", "small.xml", content));
    }

    @Test
    void failedUploadDoesNotChangeExistingData() throws IOException {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Mm");
        engine.uploadEventsFile("Mm", "small.xml", read("small.xml"));
        assertThrows(GmFileException.class,
                () -> engine.uploadEventsFile("Mm", "invalid-commission.xml", read("invalid-commission.xml")));
        assertEquals(1, engine.getEvents(gm.engine.dto.EventFilter.all()).size());
    }
}
