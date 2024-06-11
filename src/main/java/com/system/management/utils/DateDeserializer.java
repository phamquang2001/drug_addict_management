package com.system.management.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.system.management.utils.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

/* Class dùng để chuyển đổi dữ liệu STRING truyền xuống thành biến kiểu DATE */

@Slf4j
public class DateDeserializer extends StdDeserializer<Date> {

    private static final String[] DATE_FORMATS = new String[]{
            "dd/MM/yyyy",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy HH",
            "dd-MM-yyyy",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm",
            "dd-MM-yyyy HH",
            "ddMMyyyyHHmmss"
    };

    public DateDeserializer() {
        this(null);
    }

    public DateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Date deserialize(JsonParser parser, DeserializationContext ctx) throws BadRequestException {
        try {
            JsonNode node = parser.getCodec().readTree(parser);
            String value = node.textValue();
            return DateUtils.parseDateStrictly(value, DATE_FORMATS);
        } catch (Exception e) {
            throw new BadRequestException("Định dạng chuỗi không đúng để convert thành Date");
        }
    }
}
