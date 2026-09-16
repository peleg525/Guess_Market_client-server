package gm.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import gm.engine.dto.EventDetailDto;
import gm.engine.dto.LmsrEventDetailDto;
import gm.engine.dto.OrderBookEventDetailDto;

import java.lang.reflect.Type;

/**
 * {@link EventDetailDto} is abstract with two concrete subtypes ({@link LmsrEventDetailDto},
 * {@link OrderBookEventDetailDto}) and no discriminator field of its own - the Java client tells
 * them apart with {@code instanceof}, which does not survive a naive JSON round-trip. This adapter
 * adds a {@code "type"} field on the way out and reads it on the way back in. The exact same class
 * (by design, a small self-contained file with no other dependencies) also lives in the gm-client
 * module: server and client must agree on the wire format, and this was simpler and more obviously
 * correct than introducing a fourth shared Maven module just for ~40 lines of JSON glue.
 */
public final class GsonFactory {

    public static Gson create() {
        return new GsonBuilder()
                .registerTypeAdapter(EventDetailDto.class, new EventDetailAdapter())
                .create();
    }

    private static final class EventDetailAdapter implements JsonSerializer<EventDetailDto>, JsonDeserializer<EventDetailDto> {
        @Override
        public JsonElement serialize(EventDetailDto src, Type typeOfSrc, JsonSerializationContext context) {
            boolean lmsr = src instanceof LmsrEventDetailDto;
            JsonElement element = lmsr
                    ? context.serialize(src, LmsrEventDetailDto.class)
                    : context.serialize(src, OrderBookEventDetailDto.class);
            JsonObject object = element.getAsJsonObject();
            object.addProperty("type", lmsr ? "LMSR" : "ORDER_BOOK");
            return object;
        }

        @Override
        public EventDetailDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject object = json.getAsJsonObject();
            String type = object.has("type") ? object.get("type").getAsString() : null;
            if ("LMSR".equals(type)) {
                return context.deserialize(json, LmsrEventDetailDto.class);
            } else if ("ORDER_BOOK".equals(type)) {
                return context.deserialize(json, OrderBookEventDetailDto.class);
            }
            throw new JsonParseException("Missing or unknown event \"type\" in JSON: " + type);
        }
    }

    private GsonFactory() {
    }
}
