package ch.cyberduck.core.sftp;

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
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class SFTPHomeDirectoryServiceTest {

    @Test
    public void testFind() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertEquals(new Path("/home/jenkins", EnumSet.of(Path.Type.directory)), new SFTPHomeDirectoryService(session).find());
        session.close();
    }

    @Test
    public void testFindWithWorkdir() throws Exception {
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)),
                new SFTPHomeDirectoryService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "sandbox"));
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)),
                new SFTPHomeDirectoryService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "/sandbox"));
    }
}
