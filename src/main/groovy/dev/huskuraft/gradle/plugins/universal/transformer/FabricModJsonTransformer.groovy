package dev.huskuraft.gradle.plugins.universal.transformer

import com.github.jengelman.gradle.plugins.shadow.transformers.CacheableTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.file.FileTreeElement

@CacheableTransformer
class FabricModJsonTransformer implements Transformer {
    @Override
    boolean canTransformResource(FileTreeElement element) {
        return false
    }

    @Override
    void transform(TransformerContext context) {
    }

    @Override
    boolean hasTransformedResource() {
        return false
    }

    @Override
    void modifyOutputStream(ZipOutputStream os, boolean preserveFileTimestamps) {

    }
}
