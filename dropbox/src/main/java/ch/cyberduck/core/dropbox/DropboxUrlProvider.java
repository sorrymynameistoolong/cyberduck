package ch.cyberduck.core.dropbox;

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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;

import org.apache.log4j.Logger;

import java.net.URI;
import java.text.MessageFormat;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;

public class DropboxUrlProvider implements UrlProvider {
    private static final Logger log = Logger.getLogger(DropboxUrlProvider.class);

    private final DropboxSession session;

    public DropboxUrlProvider(final DropboxSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(file.isFile()) {
            try {
                final String link = new DbxUserFilesRequests(session.getClient()).getTemporaryLink(file.getAbsolute()).getLink();
                list.add(new DescriptiveUrl(URI.create(link), DescriptiveUrl.Type.http,
                        MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Temporary"))));
            }
            catch(DbxException e) {
                log.warn(String.format("Failure retrieving shared link. %s", e.getMessage()));
            }
        }
        return list;
    }
}
