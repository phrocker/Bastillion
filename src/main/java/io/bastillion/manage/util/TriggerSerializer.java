/**
 * Copyright (C) 2015 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.bastillion.manage.auditing.rules.Trigger;
import io.bastillion.manage.model.AuditWrapper;

import java.lang.reflect.Type;
import java.util.Date;

public class TriggerSerializer implements JsonSerializer<Object> {
    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        if (typeOfSrc.equals(Trigger.class)) {
            Trigger wrapper = (Trigger) src;
            object.addProperty("trigger", wrapper.getAction().toString());
            object.addProperty("description", wrapper.getDescription());
        }
        return object;
    }
}
