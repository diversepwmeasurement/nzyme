/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package horse.wtf.nzyme.alerts;

import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.dot11.probes.Dot11ProbeConfiguration;

import java.util.*;

public class AlertTestHelper {

    public static final String CLEAR_QUERY = "DELETE FROM alerts";

    protected static final Dot11ProbeConfiguration CONFIG_STANDARD = Dot11ProbeConfiguration.create(
            "mockProbe1",
            ImmutableList.of(),
            "test1",
            "wlan0",
            ImmutableList.of(),
            1,
            "foo",
            false,
            ImmutableList.of(),
            ImmutableList.of()
    );


}
