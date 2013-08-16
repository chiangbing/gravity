package com.hanborq.gravity.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Read worker hosts from "rooms".
 */
public class RoomHostsReader {
    private static final Logger LOG = LoggerFactory.getLogger(RoomHostsReader.class);

    private URL hostFile;
    private Set<String> workers;

    private ClassLoader classLoader;
    {
        classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = RoomHostsReader.class.getClassLoader();
        }
    }

    public RoomHostsReader(String filename) throws IOException {
        this.hostFile = classLoader.getResource(filename);
        this.workers = new HashSet<String>();
        refresh();
    }

    public synchronized void refresh() throws IOException {
        InputStream fis = hostFile.openStream();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] hosts = line.split("[ \t\n\f\r]+");
                for (String host : hosts) {
                    if (host == null) {
                        continue;
                    }

                    host = host.trim();
                    if (host.startsWith("#")) {
                        // Everything from now on is a comment
                        break;
                    }
                    if (!host.isEmpty()) {
                        LOG.info("Adding " + host + " to the list of rooms from " + hostFile);
                        workers.add(host);
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            fis.close();
        }
    }

    public synchronized Set<String> getWorkers() {
        return workers;
    }
}
