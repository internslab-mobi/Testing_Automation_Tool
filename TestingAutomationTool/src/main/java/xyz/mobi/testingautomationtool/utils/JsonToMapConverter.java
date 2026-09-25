//package xyz.mobi.testingautomationtool.utils;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.persistence.AttributeConverter;
//import jakarta.persistence.Converter;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Converter
//public class JsonToMapConverter implements AttributeConverter<Map<String, Object>, String> {
//
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public String convertToDatabaseColumn(Map<String, Object> attribute) {
//        if (attribute == null || attribute.isEmpty()) {
//            return "{}";
//        }
//        try {
//            return objectMapper.writeValueAsString(attribute);
//        } catch (Exception e) {
//            log.error("Error converting Map to JSON string: ", e);
//            return "{}";
//        }
//    }
//
//    @Override
//    public Map<String, Object> convertToEntityAttribute(String dbData) {
//        if (dbData == null || dbData.trim().isEmpty() || dbData.equals("{}")) {
//            return new HashMap<>();
//        }
//        try {
//            return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
//        } catch (Exception e) {
//            log.error("Error converting JSON string to Map: ", e);
//            return new HashMap<>();
//        }
//    }
//}
