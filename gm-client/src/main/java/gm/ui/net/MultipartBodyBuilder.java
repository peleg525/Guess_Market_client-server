package gm.ui.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Builds a {@code multipart/form-data} request body by hand, since the assignment explicitly asks
 * for the file upload to avoid third-party libraries (no Apache Commons FileUpload etc.) - only
 * {@link java.net.http.HttpClient} and plain byte arrays.
 */
public final class MultipartBodyBuilder {

    private final String boundary = "----gm-boundary-" + UUID.randomUUID();
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public MultipartBodyBuilder addField(String name, String value) {
        writeAscii("--" + boundary + "\r\n");
        writeAscii("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        writeBytes(value.getBytes(StandardCharsets.UTF_8));
        writeAscii("\r\n");
        return this;
    }

    public MultipartBodyBuilder addFile(String name, String fileName, byte[] content) {
        writeAscii("--" + boundary + "\r\n");
        writeAscii("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n");
        writeAscii("Content-Type: application/octet-stream\r\n\r\n");
        writeBytes(content);
        writeAscii("\r\n");
        return this;
    }

    public String contentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    public HttpRequest.BodyPublisher build() {
        writeAscii("--" + boundary + "--\r\n");
        return HttpRequest.BodyPublishers.ofByteArray(out.toByteArray());
    }

    private void writeAscii(String text) {
        writeBytes(text.getBytes(StandardCharsets.US_ASCII));
    }

    private void writeBytes(byte[] bytes) {
        try {
            out.write(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
