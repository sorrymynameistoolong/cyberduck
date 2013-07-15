package ch.cyberduck.core.cdn;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.BackgroundException;

import java.util.List;

/**
 * CDN configuration actions.
 *
 * @version $Id$
 */
public interface DistributionConfiguration {

    /**
     * Write distribution configuration for origin.
     *
     * @param configuration Configuration
     */
    void write(Path container, Distribution configuration) throws BackgroundException;

    /**
     * Read distribution configuration of origin
     *
     * @param container Container
     * @param method    Distribution protocol
     * @return Distribution Configuration
     */
    Distribution read(Path container, Distribution.Method method) throws BackgroundException;

    /**
     * List available distribution methods for this CDN.
     *
     * @param container Container
     * @return The supported protocols
     */
    List<Distribution.Method> getMethods(Path container);

    /**
     * @return Hostname and port
     */
    Protocol getProtocol();

    /**
     * Marketing name for the distribution service
     *
     * @return Localized description
     */
    String getName();

    /**
     * @param method Distribution method
     * @return CDN name
     */
    String getName(Distribution.Method method);

    <T> T getFeature(Class<T> type, Distribution.Method method);
}
