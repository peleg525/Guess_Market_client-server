package gm.server;

import gm.engine.dto.LoadResultDto;
import gm.engine.exception.GmException;
import gm.engine.exception.GmOperationException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Accepts an events-file upload as {@code multipart/form-data} with two parts: a plain
 * {@code username} form field and a {@code file} part carrying the XML. Uses the servlet
 * container's built-in multipart support ({@link Part}) rather than a third-party library, per
 * the assignment's instructions. The file content is read straight into memory and handed to the
 * engine - it is never written to disk, as required.
 */
@WebServlet("/api/files")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class UploadServlet extends ApiServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Part usernamePart = request.getPart("username");
            Part filePart = request.getPart("file");
            if (usernamePart == null || filePart == null) {
                throw new GmOperationException("The upload request must include both a \"username\" field and a \"file\" part.");
            }

            String username = readAsString(usernamePart);
            String fileName = filePart.getSubmittedFileName();
            byte[] content = readAllBytes(filePart);

            LoadResultDto result = EngineHolder.ENGINE.uploadEventsFile(username, fileName, content);
            writeJson(response, result);
        } catch (GmException e) {
            handleEngineException(response, e);
        } catch (Exception e) {
            handleUnexpectedException(response, e);
        }
    }

    private String readAsString(Part part) throws IOException {
        return new String(readAllBytes(part), StandardCharsets.UTF_8).trim();
    }

    private byte[] readAllBytes(Part part) throws IOException {
        try (InputStream in = part.getInputStream()) {
            return in.readAllBytes();
        }
    }
}
