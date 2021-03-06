/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.xml.encryption.validator;

import org.opensaml.xml.encryption.CipherData;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;

/**
 * Checks {@link org.opensaml.xml.encryption.CipherData} for Schema compliance. 
 */
public class CipherDataSchemaValidator implements Validator<CipherData> {

    /** {@inheritDoc} */
    public void validate(CipherData xmlObject) throws ValidationException {
        validateChildrenPresence(xmlObject);
    }

    /**
     * Validate that required children are present.
     * 
     * @param xmlObject the object to validate
     * @throws ValidationException  thrown if the object is invalid
     */
    protected void validateChildrenPresence(CipherData xmlObject) throws ValidationException {
        if (xmlObject.getCipherValue() == null && xmlObject.getCipherReference() == null) {
            throw new ValidationException("CipherData did not contain either a CipherValue or CipherReference child");
        }
        if (xmlObject.getCipherValue() != null && xmlObject.getCipherReference() != null) {
            throw new ValidationException("CipherData contained both a CipherValue and a CipherReference child");
        }
    }
}
