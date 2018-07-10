package org.sonar.plugins.cas.util;

import java.io.*;
import java.util.Base64;

public final class Serializer {

    private Serializer() {
    }

    public static <T extends Serializable> String serialize(T serializable) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos)) {
            objectOutputStream.writeObject(serializable);
        }

        return Base64.getUrlEncoder().encodeToString(baos.toByteArray());
    }

    public static <T extends Serializable> T deserialize(Class<T> type, String data) throws IOException {
        byte[] decoded = Base64.getUrlDecoder().decode(data);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(decoded))) {
            try {
                return (T) objectInputStream.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException("failed to deserialize object", e);
            }
        }
    }

}
