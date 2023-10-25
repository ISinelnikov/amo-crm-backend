package oss.backend.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class MappingUtils {
    private static final Logger logger = LoggerFactory.getLogger(MappingUtils.class);

    public static final String EMPTY_JSON = "{}";

    public static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private MappingUtils() {
    }

    public static class ByteArraySerializer extends JsonSerializer<byte[]> {
        @Override
        public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value != null) {
                gen.writeString(Base64.getEncoder().encodeToString(value));
            } else {
                gen.writeNull();
            }
        }
    }

    public static <T> T convertObjectWithType(Object object, Class<T> type) {
        return OBJECT_MAPPER.convertValue(object, type);
    }

    @Nullable
    public static <T> T parseJsonToInstance(@Nullable String json, Class<T> type) {
        if (StringUtils.hasText(json)) {
            try {
                return OBJECT_MAPPER.readValue(json, type);
            } catch (IOException ex) {
                logger.error("Can't parse json: '{}' to instance.", json, ex);
            }
        }
        return null;
    }

    public static <T> Collection<T> parseJsonToCollection(@Nullable String json, Class<T> type) {
        if (StringUtils.hasText(json)) {
            try {
                CollectionType collectionType = OBJECT_MAPPER.getTypeFactory()
                        .constructCollectionType(ArrayList.class, Class.forName(type.getName()));
                return OBJECT_MAPPER.readValue(json, collectionType);
            } catch (IOException | ClassNotFoundException ex) {
                logger.error("Can't parse json: '{}' to collection.", json, ex);
            }
        }
        return Collections.emptyList();
    }

    public static String convertObjectToJson(@Nullable Object object) {
        if (object != null) {
            try {
                return OBJECT_MAPPER.writeValueAsString(object);
            } catch (JsonProcessingException ex) {
                logger.error("Can't create json for object: {}.", object);
            }
        }
        return EMPTY_JSON;
    }
}
