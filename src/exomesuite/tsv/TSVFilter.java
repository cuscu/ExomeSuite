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
package exomesuite.tsv;

/**
 * This class represents a filter for a TSV file.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class TSVFilter {

    private String value;
    private Connector selectedConnector;
    private int selectedIndex = -1;

    /**
     * Gets the value of the filter.
     *
     * @return the value of the filter.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets a value for the filter.
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The selectedConnector of the filter.
     *
     * @return
     */
    public Connector getSelectedConnector() {
        return selectedConnector;
    }

    public void setSelectedConnector(Connector selectedConnector) {
        this.selectedConnector = selectedConnector;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    /**
     * Returns true in case this line passes this filter or this filter can NOT be applied due to
     * field<->selectedConnector<->value incompatibilities.
     *
     * @param line
     * @return true if passes the filter or the filter cannot be applied, false otherwise.
     */
    public boolean filter(String[] line) {
        if (selectedIndex < 0) {
            return true;
        }
        String stringValue = line[selectedIndex];
        double doubleValue = Double.MIN_VALUE;
        try {
            doubleValue = Double.valueOf(stringValue);
        } catch (Exception e) {
        }
        switch (selectedConnector) {
            case CONTAINS:
                if (stringValue != null) {
                    return value.contains(stringValue);
                }
                break;
            case DIFFERS:
                if (stringValue != null) {
                    return !value.equals(stringValue);
                }
                break;
            case EQUALS:
                if (doubleValue > Double.MIN_VALUE) {
                    try {
                        return Double.valueOf(value) == doubleValue;
                    } catch (NumberFormatException e) {
                        return true;
                    }
                } else if (stringValue != null) {
                    return stringValue.equals(value);
                }
                break;
            case GREATER:
                if (doubleValue > Double.MIN_VALUE) {
                    try {
                        return doubleValue > Double.valueOf(value);
                    } catch (NumberFormatException e) {
                        return true;
                    }
                }
                break;
            case LESS:
                if (doubleValue > Double.MIN_VALUE) {
                    try {
                        return doubleValue < Double.valueOf(value);
                    } catch (NumberFormatException e) {
                        return true;
                    }
                }
                break;
            case MATCHES:
                if (stringValue != null) {
                    return stringValue.matches(value);
                }
        }
        return false;
    }

    /**
     * The type of relation between the filter value and the field value.
     */
    public enum Connector {

        /**
         * Equals to (String or natural number).
         */
        EQUALS {

                    @Override
                    public String toString() {
                        return "is equals to";
                    }

                },
        /**
         * Contains (String).
         */
        CONTAINS {

                    @Override
                    public String toString() {
                        return "contains";
                    }
                },
        /**
         * Greater than (number).
         */
        GREATER {

                    @Override
                    public String toString() {
                        return "is greater than";
                    }

                },
        /**
         * Less than (number).
         */
        LESS {

                    @Override
                    public String toString() {
                        return "is less than";
                    }

                },
        /**
         * Regular expression (String).
         */
        MATCHES {

                    @Override
                    public String toString() {
                        return "matches";
                    }

                },
        /**
         * Different (String, ¿number?).
         */
        DIFFERS {

                    @Override
                    public String toString() {
                        return "differs from";
                    }

                }
    };

}
