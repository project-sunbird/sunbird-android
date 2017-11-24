package org.sunbird.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created on 4/7/17.
 *
 * @author anil
 */
public class SerializableUtil {

    public static byte[] serialize(final Serializable obj) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        serialize(obj, baos);
        return baos.toByteArray();
    }

    public static void serialize(final Serializable obj, final OutputStream outputStream) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            out.writeObject(obj);
        } catch (Exception ex) {
        }
    }

    public static <T> T deserialize(final byte[] bytes) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            final T obj = (T) in.readObject();
            return obj;
        } catch (Exception ex) {
            return null;
        }
    }

}
