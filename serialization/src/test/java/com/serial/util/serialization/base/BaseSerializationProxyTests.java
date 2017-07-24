package com.serial.util.serialization.base;

import com.serial.util.SerializationTestUtils;
import com.serial.util.serialization.SerializationContext;
import com.serial.util.serialization.serializer.ObjectSerializer;
import com.serial.util.serialization.serializer.Serializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;

public class BaseSerializationProxyTests {
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        SerializationTestUtils.checkSerializationComparingFieldByField(new SerializableObject(0, null),
                SerializableObject.SERIALIZER);
        SerializationTestUtils.checkSerializationComparingFieldByField(new SerializableObject(42, "test"),
                SerializableObject.SERIALIZER);
    }

    public static class SerializableObject implements Serializable {
        public static final Serializer<SerializableObject> SERIALIZER = new SerializableObjectSerializer();

        public final int number;
        public final String string;

        SerializableObject(int number, @Nullable String string) {
            this.number = number;
            this.string = string;
        }

        @NotNull
        protected Object writeReplace() {
            return new SerializationProxy(this);
        }

        private static class SerializationProxy extends BaseSerializationProxy<SerializableObject> {
            private static final long serialVersionUID = 7592205251950924553L;

            @SuppressWarnings("checkstyle:redundantmodifier")
            public SerializationProxy() {
                super(SERIALIZER, false);
            }

            SerializationProxy(@NotNull SerializableObject object) {
                super(SERIALIZER, object, false);
            }
        }

        private static class SerializableObjectSerializer extends ObjectSerializer<SerializableObject> {
            @Override
            protected void serializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerOutput output,
                    @NotNull SerializableObject object) throws IOException {
                output.writeInt(object.number)
                        .writeString(object.string);
            }

            @NotNull
            @Override
            protected SerializableObject deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber)
                    throws IOException, ClassNotFoundException {
                return new SerializableObject(input.readInt(), input.readString());
            }
        }
    }
}
