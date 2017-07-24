package com.serial.util.serialization.base;

import com.serial.util.internal.InternalSerialUtils;
import com.serial.util.serialization.SerializationContext;
import com.serial.util.serialization.serializer.Serializer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class LegacySerial implements Serial {

    @NotNull
    private SerializationContext mContext;

    public LegacySerial() {
        this(SerializationContext.ALWAYS_RELEASE);
    }

    public LegacySerial(@NotNull SerializationContext context) {
        mContext = context;
    }

    /**
     * Serialize the given value and compress the result bytes. This method should be used only for values
     * that may occupy large amount of memories. Do not use this method for small objects because the
     * compression incurs performance overheads and the compressed data cannot be inspected using
     * SerializationUtils.dumpSerializedData
     *
     * @param value the value to be serialized.
     * @param serializer the serializer used to serialize value.
     * @return compressed bytes of the serialized value.
     */
    @Override
    @NotNull
    public <T> byte[] toByteArray(@Nullable T value, @NotNull Serializer<T> serializer) throws IOException {
        if (value == null) {
            return InternalSerialUtils.EMPTY_BYTE_ARRAY;
        }
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = null;
        try {
            objectOutput = new ObjectOutputStream(byteOutputStream);
            serializer.serialize(mContext, new LegacySerializerOutput(objectOutput), value);
        } catch (IOException e) {
            throw e;
        } finally {
            if (objectOutput != null) {
                try {
                    objectOutput.close();
                } catch (IOException ignore) { }
            }
        }
        return byteOutputStream.toByteArray();
    }

    /**
     * Deserialize the value that was serialized by toCompressedByteArray().
     *
     * @param bytes the bytes returned by toCompressedByteArray().
     * @param serializer the serializer used to deserialize value.
     * @return the value.
     */
    @Override
    @Nullable
    @Contract("null, _ -> null")
    public <T> T fromByteArray(@Nullable byte[] bytes, @NotNull Serializer<T> serializer) throws IOException,
            ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ObjectInputStream objectInput = null;
        try {
            objectInput = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return serializer.deserialize(mContext, new LegacySerializerInput(objectInput));
        } catch (IOException | ClassNotFoundException | IllegalStateException e) {
            throw e;
        } finally {
            if (objectInput != null) {
                try {
                    objectInput.close();
                } catch (IOException ignore) { }
            }
        }
    }

    @NotNull
    public <T> byte[] toCompressedByteArray(@Nullable T value, @NotNull Serializer<T> serializer) throws IOException {
        if (value == null) {
            return InternalSerialUtils.EMPTY_BYTE_ARRAY;
        }
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = null;
        try {
            objectOutput = new ObjectOutputStream(new GZIPOutputStream(byteOutputStream));
            serializer.serialize(mContext, new LegacySerializerOutput(objectOutput), value);
        } catch (IOException e) {
            throw e;
        } finally {
            if (objectOutput != null) {
                try {
                    objectOutput.close();
                } catch (IOException ignore) { }
            }
        }
        return byteOutputStream.toByteArray();
    }

    @Nullable
    @Contract("null, _ -> null")
    public <T> T fromCompressedByteArray(@Nullable byte[] bytes, @NotNull Serializer<T> serializer) throws IOException,
            ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ObjectInputStream objectInput = null;
        try {
            objectInput = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)));
            return serializer.deserialize(mContext, new LegacySerializerInput(objectInput));
        } catch (IOException | ClassNotFoundException | IllegalStateException e) {
            throw e;
        } finally {
            if (objectInput != null) {
                try {
                    objectInput.close();
                } catch (IOException ignore) { }
            }
        }
    }
}
