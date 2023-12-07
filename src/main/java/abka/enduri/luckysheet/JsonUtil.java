package abka.enduri.luckysheet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JsonUtil {
    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        mapper.registerModule(javaTimeModule);
        mapper.registerModule(new Jdk8Module());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public static <E> E toObject(Object obj, Class<E> clazz) {
        try {
            if (obj instanceof String) {
                if (clazz == String.class) {
                    return (E) obj;
                } else {
                    return mapper.readValue((String) obj, clazz);
                }
            } else if (obj instanceof JsonNode) {
                return mapper.readValue(obj.toString(), clazz);
            } else {
                return mapper.readValue(mapper.writeValueAsString(obj), clazz);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 转成List泛型对象.
     *
     * @param <E>   the type parameter
     * @param obj   源数据，可以是Json字符串、JsonNode或其它Java对象
     * @param clazz 目标对象类型
     * @return 目标对象 list
     */
    public static <E> List<E> toList(Object obj, Class<E> clazz) {
        return (List<E>) toGeneric(obj, List.class, clazz);
    }

    /**
     * 转成泛型对象.
     *
     * @param obj              源数据，可以是Json字符串、JsonNode或其它Java对象
     * @param parametrized     目标对象类型
     * @param parameterClasses 目标对象泛型类型
     * @return 目标对象 object
     */
    public static Object toGeneric(Object obj, Class<?> parametrized, Class... parameterClasses) {
        JavaType type = mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
        return toGeneric(obj, type);
    }

    private static Object toGeneric(Object obj, JavaType type) {
        try {
            if (obj instanceof String) {
                return mapper.readValue((String) obj, type);
            } else if (obj instanceof JsonNode) {
                return mapper.readValue(obj.toString(), type);
            } else {
                return mapper.readValue(mapper.writeValueAsString(obj), type);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
