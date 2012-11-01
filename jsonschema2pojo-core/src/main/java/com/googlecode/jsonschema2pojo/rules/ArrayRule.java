/**
 * Copyright © 2010-2011 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.jsonschema2pojo.rules;

import static org.apache.commons.lang.StringUtils.*;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonschema2pojo.Schema;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

/**
 * Applies the "type":"array" schema rule.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.5">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.5</a>
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.15">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.15</a>
 */
public class ArrayRule implements SchemaRule<JPackage, JClass> {

    private final RuleFactory ruleFactory;

    protected ArrayRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * When constructs of type "array" appear in the schema, these are mapped to
     * Java collections in the generated POJO. If the array is marked as having
     * "uniqueItems" then the resulting Java type is {@link Set}, if not, then
     * the resulting Java type is {@link List}. The schema given by "items" will
     * decide the generic type of the collection.
     * <p>
     * If the "items" property requires newly generated types, then the type
     * name will be the singular version of the nodeName (unless overridden by
     * the javaType property) e.g.
     * <p>
     * 
     * <pre>
     *  "fooBars" : {"type":"array", "uniqueItems":"true", "items":{type:"object"}}
     *  ==>
     *  {@code Set<FooBar> getFooBars(); }
     * </pre>
     * 
     * @param nodeName
     *            the name of the property which has type "array"
     * @param node
     *            the schema "type" node
     * @param jpackage
     *            the package into which newly generated types should be added
     * @return the Java type associated with this array rule, either {@link Set}
     *         or {@link List}, narrowed by the "items" type
     */
    @Override
    public JClass apply(String nodeName, JsonNode node, JPackage jpackage, Schema schema) {

        boolean uniqueItems = node.has("uniqueItems") && node.get("uniqueItems").asBoolean();
        boolean rootSchemaIsArray = !schema.isGenerated();

        JType itemType;
        if (node.has("items")) {
            itemType = ruleFactory.getSchemaRule().apply(makeSingular(nodeName), node.get("items"), jpackage, schema);
        } else {
            itemType = jpackage.owner().ref(Object.class);
        }

        JClass arrayType;
        if (uniqueItems) {
            arrayType = jpackage.owner().ref(Set.class).narrow(itemType);
        } else {
            arrayType = jpackage.owner().ref(List.class).narrow(itemType);
        }

        if (rootSchemaIsArray) {
            schema.setJavaType(arrayType);
        }

        return arrayType;
    }

    private String makeSingular(String nodeName) {

        if (endsWith(nodeName, "ies")) {
            return removeEnd(nodeName, "ies") + "y";
        } else {
            return removeEnd(removeEnd(nodeName, "s"), "S");
        }

    }

}
