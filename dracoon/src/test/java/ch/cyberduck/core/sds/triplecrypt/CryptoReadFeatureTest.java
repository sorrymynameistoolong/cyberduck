package ch.cyberduck.core.sds.triplecrypt;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sds.SDSAttributesFinderFeature;
import ch.cyberduck.core.sds.SDSDeleteFeature;
import ch.cyberduck.core.sds.SDSEncryptionBulkFeature;
import ch.cyberduck.core.sds.SDSProtocol;
import ch.cyberduck.core.sds.SDSReadFeature;
import ch.cyberduck.core.sds.SDSSession;
import ch.cyberduck.core.sds.SDSWriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CryptoReadFeatureTest {


    @Test
    public void testPartialRead() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new Path("CD-TEST-ENCRYPTED", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.vault));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file, Path.Type.decrypted));
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, status), new DisabledConnectionCallback());
        final CryptoWriteFeature writer = new CryptoWriteFeature(session, new SDSWriteFeature(session));
        final StatusOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final VersionId version = out.getStatus();
        assertNotNull(version);
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(content.length, new SDSAttributesFinderFeature(session).find(test).getSize());
        final byte[] compare = new byte[content.length - 1000];
        final InputStream stream = new CryptoReadFeature(session, new SDSReadFeature(session)).read(test, new TransferStatus(), new ConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("ahbic3Ae");
            }
        });
        stream.skip(1000);
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(Arrays.copyOfRange(content, 1000, content.length), compare);
        new SDSDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
