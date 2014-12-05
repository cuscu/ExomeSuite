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
package exomesuite.vcf;

/**
 * This class represents a filter for a VCF file. The filter is characterized by a field (CHROM,
 * POS, DP...), a connector (greater than, equals...) and a value. When a variant is passed to the
 * filter it is read: variant.field connector value (variant.chrom is equals to 7).
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class VCFFilter {

    private String value;
    private Connector connector;
    private Field field;
    private String selectedInfo;

    public VCFFilter() {
        connector = Connector.EQUALS;
        field = Field.CHROMOSOME;
    }

    public VCFFilter(Connector connector, Field field, String selectedInfo) {
        this.connector = connector;
        this.field = field;
        this.selectedInfo = selectedInfo;
    }

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
     * The connector of the filter.
     *
     * @return
     */
    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getSelectedInfo() {
        return selectedInfo;
    }

    public void setSelectedInfo(String selectedInfo) {
        this.selectedInfo = selectedInfo;
    }

    /**
     * Returns true in case this variant passes this filter or filter can NOT be applied due to
     * field<->connector<->value incompatibilities.
     *
     * @param variant the variant to filter.
     * @return true if passes the filter or the filter cannot be applied, false otherwise.
     */
    public boolean filter(Variant variant) {
        if (field == null) {
            return true;
        }
        // Get the value (one of the Field.values())
        String stringValue = null;
        double doubleValue = Double.MIN_VALUE;
        switch (field) {
            case CHROMOSOME:
                stringValue = variant.getChrom();
                break;
            case POSITION:
                doubleValue = variant.getPos();
                break;
            case QUALITY:
                doubleValue = variant.getQual();
                break;
            case FILTER:
                stringValue = variant.getFilter();
                break;
            case ID:
                stringValue = variant.getId();
            case INFO:
                String[] content = variant.getInfo().split(";");
                // variant.getInfo = "DP=10;MQ=23,43;H2"
                // content[0] = "DP=10"
                // content[1] = "MQ=23,43"
                // content[2] = "H2"
                for (String s : content) {
                    if (s.startsWith(selectedInfo)) {
                        if (s.contains("=")) {
                            // Case DP=10 or MQ=23,43 or REF=G
                            stringValue = s.split("=")[1];
                            try {
                                doubleValue = Double.valueOf(stringValue.split(",")[0]);
                            } catch (NumberFormatException e) {
                                // If not a number
                            }
                        } else {
                            // Case H2
                            stringValue = s;
                        }
                        break;
                    }
                }
                break;
        }
        switch (connector) {
            case CONTAINS:
                if (stringValue != null) {
                    return stringValue.contains(value);
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
         * Contains (String)
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

    /**
     * The field from the VCF.
     */
    public enum Field {

        CHROMOSOME,
        POSITION,
        QUALITY,
        INFO,
        FILTER,
        ID
    }

}
