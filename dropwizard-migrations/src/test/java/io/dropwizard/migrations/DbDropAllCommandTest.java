package io.dropwizard.migrations;

import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
class DbDropAllCommandTest extends AbstractMigrationTest {

    private final DbDropAllCommand<TestMigrationConfiguration> dropAllCommand = new DbDropAllCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml");

    @Test
    void testRun() throws Exception {
        final String databaseUrl = getDatabaseUrl();
        final TestMigrationConfiguration conf = createConfiguration(databaseUrl);

        // Create some data
        new DbMigrateCommand<>(
            TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml")
            .run(null, new Namespace(Collections.emptyMap()), conf);

        try (Handle handle = Jdbi.create(databaseUrl, "sa", "").open()) {
            assertThat(tableExists(handle, "PERSONS"))
                .isTrue();
        }

        // Drop it
        dropAllCommand.run(null, new Namespace(Collections.emptyMap()), conf);

        try (Handle handle = Jdbi.create(databaseUrl, "sa", "").open()) {
            assertThat(tableExists(handle, "PERSONS"))
                .isFalse();
        }
    }

    @Test
    void testHelpPage() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        createSubparser(dropAllCommand).printHelp(new PrintWriter(new OutputStreamWriter(out, UTF_8), true));
        assertThat(out.toString(UTF_8.name())).isEqualTo(String.format(
            "usage: db drop-all [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]%n" +
                "          [--schema SCHEMA] --confirm-delete-everything [file]%n" +
                "%n" +
                "Delete all user-owned objects from the database.%n" +
                "%n" +
                "positional arguments:%n" +
                "  file                   application configuration file%n" +
                "%n" +
                "named arguments:%n" +
                "  -h, --help             show this help message and exit%n" +
                "  --migrations MIGRATIONS-FILE%n" +
                "                         the file containing  the  Liquibase migrations for%n" +
                "                         the application%n" +
                "  --catalog CATALOG      Specify  the   database   catalog   (use  database%n" +
                "                         default if omitted)%n" +
                "  --schema SCHEMA        Specify the database schema  (use database default%n" +
                "                         if omitted)%n" +
                "  --confirm-delete-everything%n" +
                "                         indicate you  understand  this  deletes everything%n" +
                "                         in your database%n"));
    }
}
