/**
 * Copyright (c) 2015, Standards Implementation & Testing Environment (SITE)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.samhsa.mhc.documentvalidator.service.validators;

import gov.samhsa.mhc.documentvalidator.service.dto.DiagnosticType;
import gov.samhsa.mhc.documentvalidator.service.dto.DocumentValidationResult;
import org.eclipse.emf.common.util.Diagnostic;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.util.CDADiagnostic;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.util.ValidationResult;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class CCDAValidatorImpl implements CCDAValidator {

    @Override
    public ArrayList<DocumentValidationResult> validateCCDA(InputStream ccdaFile) throws SAXException {
        ValidationResult result = new ValidationResult();
        createValidationResultObjectToCollectDiagnosticsProducedDuringValidation();
        try {
            CDAUtil.load(ccdaFile, result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return processValidationResults(result);
    }

    private void createValidationResultObjectToCollectDiagnosticsProducedDuringValidation() {
        ConsolPackage.eINSTANCE.eClass();
    }

    private ArrayList<DocumentValidationResult> processValidationResults(ValidationResult result) {
        ArrayList<DocumentValidationResult> results = result.getErrorDiagnostics()
                .stream()
                .map(diagnostic -> buildValidationResult(diagnostic, DiagnosticType.CCDA_ERROR))
                .collect(Collectors.toCollection(ArrayList::new));

        results.addAll(result.getWarningDiagnostics()
                .stream()
                .map(diagnostic -> buildValidationResult(diagnostic, DiagnosticType.CCDA_WARN))
                .collect(Collectors.toList()));

        results.addAll(result.getInfoDiagnostics()
                .stream()
                .map(diagnostic -> buildValidationResult(diagnostic, DiagnosticType.CCDA_INFO))
                .collect(Collectors.toList()));
        return results;
    }

    private DocumentValidationResult buildValidationResult(Diagnostic diagnostic, DiagnosticType resultType) {
        CDADiagnostic diag = new CDADiagnostic(diagnostic);
        return createNewValidationResult(diag, resultType);
    }

    private DocumentValidationResult createNewValidationResult(CDADiagnostic cdaDiag, DiagnosticType resultType) {
        return new DocumentValidationResult(cdaDiag.getMessage(), cdaDiag.getCode(), resultType);
    }
}
