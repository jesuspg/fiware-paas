/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U <br>
 * This file is part of FI-WARE project.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 * </p>
 * <p>
 * You may obtain a copy of the License at:<br>
 * <br>
 * http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * <p>
 * See the License for the specific language governing permissions and limitations under the License.
 * </p>
 * <p>
 * For those usages not covered by the Apache version 2.0 License please contact with opensource@tid.es
 * </p>
 */

package com.telefonica.euro_iaas.paasmanager.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import net.sf.json.JSONObject;

/**
 * Defines an attribute which can be configured.
 *
 * @author Jesus M. Movilla
 */
@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @XmlTransient
    private Long id;

    @SuppressWarnings("unused")
    @Version
    @XmlTransient
    private Long v;

    /**
     * the attribute key.
     */
    @Column(nullable = false, length = 256)
    private String key;
    /**
     * the attribute value.
     */
    @Column(nullable = false, length = 2048)
    private String value;
    /* the description of that attribute* */
    @Column(nullable = true, length = 2048)
    private String description;

    @Column(nullable = true, length = 256)
    private String type;

    /**
     */
    public Attribute() {
    }

    /**
     * @param key
     * @param value
     */
    public Attribute(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * @param key
     * @param value
     * @param description
     */
    public Attribute(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    /**
     * @param key
     * @param value
     * @param description
     */
    public Attribute(String key, String value, String description, String type) {
        this.key = key;
        this.value = value;
        this.description = description;
        this.type = type;
    }

    /**
     * The equal function.
     *
     * @see java.lang.Object#
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Attribute other = (Attribute) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    /**
     * from Json.
     *
     * @param jsonNode
     */
    @SuppressWarnings("unchecked")
    public void fromJson(JSONObject jsonNode) {
        key = jsonNode.getString("key");
        value = jsonNode.getString("value");

        if (jsonNode.containsKey("description")) {
            description = jsonNode.getString("description");
        }
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Constructs a <code>String</code> with all attributes in name = value
     * format.
     *
     * @return a <code>String</code> representation of this object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("[[Attribute]");
        sb.append("[id = ").append(this.id).append("]");
        sb.append("[v = ").append(this.v).append("]");
        sb.append("[key = ").append(this.key).append("]");
        sb.append("[value = ").append(this.value).append("]");
        sb.append("[description = ").append(this.description).append("]");
        sb.append("[type = ").append(this.type).append("]");
        sb.append("]");
        return sb.toString();
    }

    /**
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * 
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

}
