/*
 * Copyright (c) 2016-2019 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.farm;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jcabi.s3.Ocket;
import com.zerocracy.Item;
import com.zerocracy.TempFiles;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import lombok.EqualsAndHashCode;
import org.cactoos.Func;
import org.cactoos.Proc;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.func.IoCheckedProc;
import org.cactoos.io.InputOf;
import org.cactoos.io.Md5DigestOf;

/**
 * Item in S3.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ReturnCountCheck (500 lines)
 */
@EqualsAndHashCode(of = {"ocket"})
@SuppressWarnings("PMD.OnlyOneReturn")
final class S3Item implements Item {

    /**
     * S3 ocket.
     */
    private final Ocket ocket;

    /**
     * Ctor.
     * @param okt Ocket
     */
    S3Item(final Ocket okt) {
        this.ocket = okt;
    }

    @Override
    public String toString() {
        return this.ocket.toString();
    }

    @Override
    public <T> T read(final Func<Path, T> reader) throws IOException {
        final Path tmp = S3Item.tempFiles();
        try {
            if (this.ocket.exists()) {
                new OcketExt(this.ocket).read(tmp);
            }
            return new IoCheckedFunc<>(reader).apply(tmp);
        } finally {
            TempFiles.INSTANCE.dispose(tmp);
        }
    }

    @Override
    public void update(final Proc<Path> writer) throws IOException {
        final Path tmp = S3Item.tempFiles();
        try {
            if (this.ocket.exists()) {
                new OcketExt(this.ocket).read(tmp);
            }
            final Md5DigestOf mdsum = new Md5DigestOf(new InputOf(tmp));
            final byte[] hbefore = mdsum.asBytes();
            new IoCheckedProc<>(writer).exec(tmp);
            final byte[] hash = mdsum.asBytes();
            if (Arrays.equals(hbefore, hash)) {
                return;
            }
            final ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(Files.size(tmp));
            meta.setContentMD5(Base64.getEncoder().encodeToString(hash));
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            new OcketExt(this.ocket).write(tmp, meta);
        } finally {
            TempFiles.INSTANCE.dispose(tmp);
        }
    }

    /**
     * Temporary file for S3 object.
     * @return Path to temp file
     * @throws IOException On failure
     */
    private static Path tempFiles() throws IOException {
        return TempFiles.INSTANCE.newFile(S3Item.class);
    }

    /**
     * Ocket extensions.
     */
    private static final class OcketExt {

        /**
         * Ocket.
         */
        private final Ocket ocket;

        /**
         * Ctor.
         * @param ocket Ocket
         */
        OcketExt(final Ocket ocket) {
            this.ocket = ocket;
        }

        /**
         * Read ocket to file.
         * @param file Output
         * @throws IOException On IO error
         */
        public void read(final Path file) throws IOException {
            try (
                final OutputStream out = new BufferedOutputStream(
                    Files.newOutputStream(
                        file, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                    )
                )
            ) {
                this.ocket.read(out);
            }
        }

        /**
         * Write file to ocket.
         * @param file File to write
         * @param meta S3 object metadata
         * @throws IOException On IO error
         */
        public void write(final Path file, final ObjectMetadata meta)
            throws IOException {
            try (
                final InputStream src = new BufferedInputStream(
                    Files.newInputStream(file, StandardOpenOption.READ)
                )
            ) {
                this.ocket.write(src, meta);
            }
        }
    }
}
