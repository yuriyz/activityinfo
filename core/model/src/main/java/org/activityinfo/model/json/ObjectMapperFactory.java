package org.activityinfo.model.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.IsResource;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.table.TableData;
import org.activityinfo.model.table.TableModel;

/**
 * Provides an {@link com.fasterxml.jackson.databind.ObjectMapper} instance configured with
 * the necessary custom serializers/deserializes for ActivityInfo's model objects.
 */
public class ObjectMapperFactory {

    private static ObjectMapper INSTANCE;

    public static ObjectMapper get() {
        if(INSTANCE == null) {

            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Resource.class, new ResourceSerializer());
            module.addSerializer(IsResource.class, new IsResourceSerializer());
            module.addSerializer(IsRecord.class, new IsRecordSerializer());
            module.addDeserializer(Resource.class, new ResourceDeserializer());
            module.addDeserializer(TableModel.class, new IsRecordDeserializer<TableModel>(TableModel.class));
            module.addSerializer(TableData.class, new TableDataSerializer());
            module.addDeserializer(TableData.class, new TableDataDeserializer());
            mapper.registerModule(module);
            INSTANCE = mapper;
        }
        return INSTANCE;

    }

}
