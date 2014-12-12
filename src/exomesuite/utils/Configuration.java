/*
 * Copyright (C) 2014 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package exomesuite.utils;

import exomesuite.MainViewController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores key/value entries and ensures the changes are automatically reflected in disk. it also
 * provide a Listener {@link ConfigurationListener} to implement for listening on properties changes
 * (add/update). Keys are lowercased and properties are case insensitive.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class Configuration {

    /**
     * Properties in disk.
     */
    private final File file;
    /**
     * Properties in memory.
     */
    private final Properties properties = new Properties();
    /**
     * The list of listeners.
     */
    private final Set<ConfigurationListener> listeners = new HashSet();

    /**
     * Creates a new Configuration using this file. If file exists, tries to load the properties
     * that contains. If file does not exist, creates it.
     *
     * @param file the configuration file.
     */
    public Configuration(File file) {
        this.file = file;
        if (file.exists()) {
            try {
                properties.load(new FileInputStream(file));
            } catch (FileNotFoundException ex) {
//                MainViewController.printException(ex);
                MainViewController.printMessage("Properties file can not be accessed: "
                        + file.getAbsolutePath(), "warning");
//                Logger.getLogger(OS.class.getName()).log(Level.SEVERE, "Check permissions", ex);
            } catch (IOException ex) {
                MainViewController.printMessage("Config file is corrupted: "
                        + file.getAbsolutePath(), "error");
//                Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                File parent = file.getParentFile();
                parent.mkdirs();
                file.createNewFile();
            } catch (IOException ex) {
                MainViewController.printException(ex);
//                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Sets the property. If it does not exist, creates a new one. Key is lowercased.
     *
     * @param key the property name
     * @param value the property new value
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        try {
            properties.store(new FileOutputStream(file), null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
        }
        listeners.forEach(l -> l.configurationChanged(this, key));
    }

    /**
     * Gets the key property.
     *
     * @param key
     * @return the value of the property if exists or null.
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets the key property, if key property does not exist, gets the default value.
     *
     * @param key the property name
     * @param defaultValue a value to return if property does not exist
     * @return the property or the default value if it does not exist
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Indicates if the property exists.
     *
     * @param key the name of the property
     * @return true if property exists, false otherwise
     */
    public boolean containsProperty(String key) {
        return properties.containsKey(key);
    }

    /**
     * Adds this listener to the list of listeners. If this listener was already in list, does
     * nothing.
     *
     * @param listener the listener to add.
     */
    public void addListener(ConfigurationListener listener) {
        listeners.add(listener);
    }

    /**
     * removes this listener from the list of listeners.
     *
     * @param listener the listener to remove.
     */
    public void removeListener(ConfigurationListener listener) {
        listeners.remove(listener);
    }

    /**
     * The interface to implement for listening to properties changes.
     */
    public interface ConfigurationListener {

        /**
         * Called when a key has changed.
         *
         * @param configuration the configuration changed.
         * @param keyChanged the key changed.
         */
        public void configurationChanged(Configuration configuration, String keyChanged);
    }

}
