package dev.huskuraft.universal.gradle.task.modification;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.jar.JarEntry;

/**
 * A modification that updates the value of a specific field in a TOML file using NightConfig.
 */
public abstract class TomlModification implements Modification {

    /** The file extension for TOML files. */
    public static final String TOML_EXTENSION = ".toml";

    static {
        Config.setInsertionOrderPreserved(true);
    }

    @Override
    public boolean appliesTo(JarEntry entry) {
        // Only apply to .toml files
        return entry.getName().endsWith(TOML_EXTENSION);
    }

    @Override
    public byte[] apply(byte[] input) {
        // Parse the input TOML file
        var parser = new TomlParser();

        var config = Config.wrap(new LinkedHashMap<>(), InMemoryFormat.defaultInstance());
        parser.parse(new InputStreamReader(new ByteArrayInputStream(input), StandardCharsets.UTF_8), config, ParsingMode.MERGE);

        // Apply the modification to the Config
        modifyToml(config);

        // Write the modified Config to the output stream
        try (var outputStream = new ByteArrayOutputStream();
             var writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            var configWriter = new TomlWriter();
            configWriter.setIndent(IndentStyle.NONE);
            configWriter.setLenientWithBareKeys(false);
            configWriter.setOmitIntermediateLevels(true);
            configWriter.write(config, writer);
            writer.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write modified TOML to byte array", e);
        }
    }

    /**
     * Abstract method to modify the Config.
     *
     * @param config The Config to modify.
     */
    protected abstract void modifyToml(Config config);
}
