package com.rebra.dto.external;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ItemsDeserializer extends JsonDeserializer<HolidayApiResponse.Items> {

    @Override
    public HolidayApiResponse.Items deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.getCurrentToken();
        
        // 빈 문자열인 경우
        if (token == JsonToken.VALUE_STRING) {
            String value = p.getValueAsString();
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
        }
        
        // 정상적인 객체인 경우
        if (token == JsonToken.START_OBJECT) {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            return mapper.readValue(p, HolidayApiResponse.Items.class);
        }
        
        // 그 외의 경우
        return null;
    }
}