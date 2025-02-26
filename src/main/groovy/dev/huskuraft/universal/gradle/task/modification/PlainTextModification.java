package dev.huskuraft.universal.gradle.task.modification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;

/**
 * A modification that updates the content of a plain text file.
 */
public abstract class PlainTextModification implements Modification {

    /** The file extension for plain text files. */
    public static final String TXT_EXTENSION = ".txt";

    @Override
    public boolean appliesTo(JarEntry entry) {
        // Only apply to .txt files
        return entry.getName().endsWith(TXT_EXTENSION);
    }

    @Override
    public byte[] apply(byte[] input) {
        // Read the input text file
        String content;
        try (var inputStream = new ByteArrayInputStream(input);
             var reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[1024];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, length);
            }
            content = builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read plain text from byte array", e);
        }

        // Apply the modification to the content
        String modifiedContent = modifyText(content);

        // Write the modified content to the output stream
        try (var outputStream = new ByteArrayOutputStream();
             var writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.write(modifiedContent);
            writer.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write modified plain text to byte array", e);
        }
    }

    /**
     * Abstract method to modify the text content.
     *
     * @param content The original text content.
     * @return The modified text content.
     */
    protected abstract String modifyText(String content);
}
