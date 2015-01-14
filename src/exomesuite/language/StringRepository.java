/*
 * Copyright (C) 2015 unidad03
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
package exomesuite.language;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pascual Lorente Arencibia <pasculorente@gmail.com>
 */
public class StringRepository {

    private Map<String, StringProperty> strings;

    public StringRepository() {
        setLocale(Locale.US);
    }

    private StringProperty getString(String key) {
        return strings.get(key);
    }

    private void setLocale(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("ExomeSuite", locale);
        bundle.keySet().forEach(key -> {
            StringProperty property = strings.get(key);
            if (property == null) {
                property = new SimpleStringProperty();
                strings.put(key, property);
            }
            property.set(bundle.getString(key));
        });
    }

}
