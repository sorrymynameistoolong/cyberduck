package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class CryptoChecksumComputeTest {

    @Test
    public void testCompute() throws Exception {
        final Path home = new Path("/vault", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Directory.class) {
                    return (T) new Directory() {
                        @Override
                        public void mkdir(final Path file) throws BackgroundException {
                            assertTrue(file.equals(home) || file.isChild(home));
                        }

                        @Override
                        public void mkdir(final Path file, final String region, final TransferStatus status) throws BackgroundException {
                            assertTrue(file.equals(home) || file.isChild(home));
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final CryptoVault vault = new CryptoVault(
                home, new DisabledPasswordStore(), new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("pwd");
            }
        }, new DisabledVaultLookupListener());
        vault.create(session, null);
        final Cryptor cryptor = vault.getCryptor();
        final FileHeader header = cryptor.fileHeaderCryptor().create();
        // DEFAULT_PIPE_SIZE=1024
        final SHA256ChecksumCompute sha = new SHA256ChecksumCompute();
        final CryptoChecksumCompute compute = new CryptoChecksumCompute(sha, vault);
        assertNotNull(compute.compute(new NullInputStream(1025L), new TransferStatus().withHeader(header)).hash);
        assertNotEquals(compute.compute(new NullInputStream(1025L), new TransferStatus().withHeader(header)),
                compute.compute(new NullInputStream(1025L), new TransferStatus().withHeader(header)));
        assertNotNull(compute.compute(new NullInputStream(0L), new TransferStatus().withHeader(header)).hash);
        final NullInputStream input = new NullInputStream(0L);
        assertEquals(compute.compute(input, new TransferStatus().withHeader(header)),
                compute.compute(input, new TransferStatus().withHeader(header)));
        assertNotEquals(compute.compute(new NullInputStream(0L), new TransferStatus().withHeader(header)),
                sha.compute(new NullInputStream(0L), new TransferStatus()));
        assertNotNull(compute.compute(new NullInputStream(0L), new TransferStatus().withHeader(null)).hash);
    }
}