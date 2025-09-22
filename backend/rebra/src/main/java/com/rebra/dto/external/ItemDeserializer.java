package com.rebra.dto.external;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemDeserializer extends JsonDeserializer<List<HolidayApiResponse.HolidayItem>> {

    @Override
    public List<HolidayApiResponse.HolidayItem> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.getCurrentToken();
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        
        if (token == JsonToken.START_ARRAY) {
            // 배열인 경우
            return mapper.readValue(p, new TypeReference<List<HolidayApiResponse.HolidayItem>>() {});
        } else if (token == JsonToken.START_OBJECT) {
            // 단일 객체인 경우
            HolidayApiResponse.HolidayItem item = mapper.readValue(p, HolidayApiResponse.HolidayItem.class);
            List<HolidayApiResponse.HolidayItem> items = new ArrayList<>();
            items.add(item);
            return items;
        } else {
            // 그 외의 경우 (null, 빈 문자열 등)
            return new ArrayList<>();
        }
    }
}