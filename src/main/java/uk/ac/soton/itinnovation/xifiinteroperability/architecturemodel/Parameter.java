/**
 * Copyright 2005-2014 Restlet
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 *
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 *
 * Restlet is a registered trademark of Restlet S.A.S.
 */

/**
 * Modified by pjg@it-innovation.soton.ac.uk
 * Simply made this class serializble; no other changes.
 */


package uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel;

import java.io.IOException;
import java.io.Serializable;
import org.restlet.engine.util.SystemUtils;
import org.restlet.util.NamedValue;

/**
 * Multi-usage parameter. Note that the name and value properties are thread
 * safe, stored in volatile members.
 *
 * @author Jerome Louvel
 */
public class Parameter implements Comparable<Parameter>, NamedValue<String>, Serializable {

    /**
     * Portable serializable class.
     */
    public static final long serialVersionUID = 1L;

    /** The first object. */
    private String name;

    /** The second object. */
    private String value;

    /**
     * Creates a parameter.
     *
     * @param name
     *            The parameter name buffer.
     * @param value
     *            The parameter value buffer (can be null).
     * @return The created parameter.
     */
    public static Parameter create(final CharSequence name, final CharSequence value) {
        if (value == null) {
            return new Parameter(name.toString(), null);
        } else {
            return new Parameter(name.toString(), value.toString());
        }
    }

    /**
     * Default constructor.
     */
    public Parameter() {
        this(null, null);
    }

    /**
     * Preferred constructor.
     *
     * @param newName
     *            The name.
     * @param val
     *            The value.
     */
    public Parameter(final String newName, final String val) {
        this.name = newName;
        this.value = val;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.restlet.data.NamedValue#compareTo(org.restlet.data.Parameter)
     */
    @Override
    public final int compareTo(final Parameter param) {
        return getName().compareTo(param.getName());
    }

    /**
     * Encodes the parameter into the target buffer.
     *
     * @param buffer
     *            The target buffer.
     * @param characterSet
     *            The character set to use.
     * @throws IOException Error encoding parameter.
     */
    public final void encode(final Appendable buffer, final CharacterSet characterSet)
            throws IOException {
        if (getName() != null) {
            buffer.append(Reference.encode(getName(), characterSet));

            if (getValue() != null) {
                buffer.append('=');
                buffer.append(Reference.encode(getValue(), characterSet));
            }
        }
    }

    /**
     * Encodes the parameter as a string.
     *
     * @param characterSet
     *            The character set to use.
     * @return The encoded string?
     * @throws IOException The error message: I/O exception during encoding.
     */
    public final String encode(final CharacterSet characterSet) throws IOException {
        final StringBuilder strBuild = new StringBuilder();
        encode(strBuild, characterSet);
        return strBuild.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj) {
        // if obj == this no need to go further
        boolean result = (obj == this);

        if (!result) {
            result = obj instanceof Parameter;

            // if obj isn't a parameter or is null don't evaluate further
            if (result) {
                final Parameter that = (Parameter) obj;
                result = (((that.getName() == null) && (getName() == null)) || ((getName() != null) && getName()
                        .equals(that.getName())));

                // if names are both null or equal continue
                if (result) {
                    result = (((that.getValue() == null) && (getValue() == null)) || ((getValue() != null) && getValue()
                            .equals(that.getValue())));
                }
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.restlet.data.NamedValue#getName()
     */
    @Override
    public final String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.restlet.data.NamedValue#getValue()
     */
    @Override
    public final String getValue() {
        return this.value;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return SystemUtils.hashCode(getName(), getValue());
    }

    /**
     * Set the parameter name of the parameter.
     *
     * @param newName The new name for the parameter.
     * @see org.restlet.data.NamedValue#setName(java.lang.String)
     */
    public final void setName(final String newName) {
        this.name = newName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.restlet.data.NamedValue#setValue(java.lang.String)
     */
    @Override
    public final void setValue(final String val) {
        this.value = val;
    }

    @Override
    public final String toString() {
        return "[" + getName() + "=" + getValue() + "]";
    }

}