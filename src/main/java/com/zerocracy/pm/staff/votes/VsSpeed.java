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
package com.zerocracy.pm.staff.votes;

import com.zerocracy.pmo.Pmo;
import com.zerocracy.pmo.Speed;
import java.util.Collection;
import java.util.Comparator;
import org.cactoos.iterable.Mapped;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;

/**
 * Highest speed (lowest value) wins.
 *
 * Votes for that person who is the fastest.
 * Returns 1 for fast person and 0 for slow, 0.5 - for middle.
 *
 * @since 1.0
 */
public final class VsSpeed extends VsRank<Double> {

    /**
     * Ctor.
     * @param pmo The PMO
     * @param others All other logins in the competition
     */
    @SuppressWarnings(
        {
            "PMD.CallSuperInConstructor",
            "PMD.ConstructorOnlyInitializesOrCallOtherConstructors"
        }
    )
    public VsSpeed(final Pmo pmo, final Collection<String> others) {
        super(
            new SolidMap<>(
                new Mapped<>(
                    login -> {
                        final Speed speed = new Speed(pmo, login)
                            .bootstrap();
                        final double avg;
                        if (speed.isEmpty()) {
                            avg = Double.MAX_VALUE;
                        } else {
                            avg = speed.avg();
                        }
                        return new MapEntry<>(login, avg);
                    },
                    others
                )
            ),
            Comparator.reverseOrder()
        );
    }
}
