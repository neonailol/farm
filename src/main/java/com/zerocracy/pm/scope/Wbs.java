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
package com.zerocracy.pm.scope;

import com.zerocracy.ItemXml;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import org.cactoos.scalar.NumberOf;
import org.cactoos.time.DateAsText;
import org.cactoos.time.DateOf;
import org.xembly.Directives;

/**
 * WBS.
 *
 * <p>The WBS is a hierarchical decomposition of the total scope
 * of work to be carried out by the project team to accomplish
 * the project objectives and create the required deliverables.
 * The WBS organizes and defines the total scope of the project,
 * and represents the work specified in the current approved
 * project scope statement.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class Wbs {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Wbs(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     */
    public Wbs bootstrap() {
        return this;
    }

    /**
     * Add job to WBS.
     * @param job The job to add
     * @throws IOException If fails
     */
    public void add(final String job) throws IOException {
        if (this.exists(job)) {
            throw new SoftException(
                new Par("Job %s is already in scope").say(job)
            );
        }
        this.item().update(
            new Directives()
                .xpath(String.format("/wbs[not(job[@id='%s'])]", job))
                .strict(1)
                .add("job")
                .attr("id", job)
                .add("role").set("DEV").up()
                .add("created").set(new DateAsText().asString())
        );
    }

    /**
     * Add job to WBS with an author.
     * @param job The job to add
     * @param author The job author
     * @throws IOException If fails
     */
    public void add(final String job, final String author) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Remove job from WBS.
     * @param job The job to remove
     * @throws IOException If fails
     */
    public void remove(final String job) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par("Job %s was not in scope").say(job)
            );
        }
        this.item().update(
            new Directives().xpath(Wbs.xpath(job)).strict(1).remove()
        );
    }

    /**
     * This job exists in WBS?
     * @param job The job to check
     * @return TRUE if it exists
     * @throws IOException If fails
     */
    public boolean exists(final String job) throws IOException {
        return this.item().exists(Wbs.xpath(job));
    }

    /**
     * List all jobs.
     * @return List of all jobs
     * @throws IOException If fails
     */
    public Collection<String> iterate() throws IOException {
        return this.item().xpath("/wbs/job/@id");
    }

    /**
     * Set job role.
     * @param job The job to add
     * @param role The role
     * @throws IOException If fails
     */
    public void role(final String job, final String role) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par("Job %s doesn't exist, can't set role").say(job)
            );
        }
        this.item().update(
            new Directives()
                .xpath(String.format("/wbs/job[@id='%s']/role", job))
                .set(role)
        );
    }

    /**
     * Get job role.
     * @param job The job to add
     * @return The role
     * @throws IOException If fails
     */
    public String role(final String job) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par("Job %s doesn't exist, can't get role").say(job)
            );
        }
        return this.item().xpath(
            String.format("/wbs/job[@id='%s']/role/text()", job)
        ).get(0);
    }

    /**
     * Get job creating time.
     * @param job The job to add
     * @return The time when it was added to WBS
     * @throws IOException If fails
     */
    public Date created(final String job) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par("Job %s doesn't exist, can't check date").say(job)
            );
        }
        return new DateOf(
            this.item().xpath(
                String.format("/wbs/job[@id='%s']/created/text()", job)
            ).get(0)
        ).value();
    }

    /**
     * Get job author.
     * @param job The job to get the author
     * @return The author that added it to the WBS
     * @throws IOException If fails
     */
    public String author(final String job) throws IOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * WBS size.
     * @return Size if WBS
     * @throws IOException If faisls
     */
    public int size() throws IOException {
        return new NumberOf(
            this.item().xpath("count(/wbs/job)").get(0)
        ).intValue();
    }

    /**
     * XPath to find a job.
     * @param job The job
     * @return XPath
     */
    private static String xpath(final String job) {
        return String.format("/wbs/job[@id='%s']", job);
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private ItemXml item() throws IOException {
        return new ItemXml(this.project.acq("wbs.xml"), "pm/scope/wbs");
    }
}
