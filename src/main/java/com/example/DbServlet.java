package com.example;

import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.Instant;
import org.jboss.logging.Logger;


@WebServlet(urlPatterns = {"/hello", "/db"})
public class DbServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(DbServlet.class.getName());

    private static String env(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.trim().isEmpty()) ? def : v.trim();
    }

    private static DataSource lookupDs() throws Exception {
        // Keep code DB-agnostic: only JNDI name is configurable
        String jndi = env("APP_DS_JNDI", "java:jboss/datasources/AppDS");
        return (DataSource) new InitialContext().lookup(jndi);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/plain; charset=UTF-8");
        String path = req.getServletPath();

        LOG.debugf("DB demo invoked. JNDI=%s", env("APP_DS_JNDI", "java:jboss/datasources/AppDS"));

        try (PrintWriter out = resp.getWriter()) {
            if ("/hello".equals(path)) {
                out.println("Hello from JBoss EAP 7.4!");
                out.println("Try /db to test DataSource connectivity.");
                out.println("JNDI: " + env("APP_DS_JNDI", "java:jboss/datasources/AppDS"));
                return;
            }

            // /db
            DataSource ds = lookupDs();

            try (Connection c = ds.getConnection()) {
                c.setAutoCommit(true);

                // Portable-ish DDL (avoid SERIAL/AUTO_INCREMENT for demo)
                try (Statement st = c.createStatement()) {
                    st.executeUpdate("CREATE TABLE IF NOT EXISTS demo_ping (" +
                            "id INTEGER PRIMARY KEY, " +
                            "ts TIMESTAMP NOT NULL" +
                            ")");
                }

                int id = (int)(Instant.now().toEpochMilli() & 0x7fffffff);

                try (PreparedStatement ps =
                             c.prepareStatement("INSERT INTO demo_ping (id, ts) VALUES (?, ?)")) {
                    ps.setInt(1, id);
                    ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                    ps.executeUpdate();
                } catch (SQLException insertEx) {
                    // If ID collides (rare), just continue; demo isn't about keys
                }

                long count = 0;
                try (Statement st = c.createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM demo_ping")) {
                    if (rs.next()) count = rs.getLong(1);
                }

                out.println("DATASOURCE CONNECT OK");
                out.println("Rows in demo_ping: " + count);
            }
        } catch (Exception e) {
            resp.setStatus(500);
            try (PrintWriter out = resp.getWriter()) {
                out.println("ERROR:");
                out.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }
}

