/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.contract.ui.property.constraint;

import org.bonitasoft.studio.common.jface.AbstractLabelDecorator;
import org.bonitasoft.studio.contract.core.validation.ContractConstraintExpressionValidationRule;
import org.bonitasoft.studio.contract.core.validation.ContractConstraintInputsValidationRule;
import org.bonitasoft.studio.model.process.ContractConstraint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.graphics.Image;


/**
 * @author Romain Bioteau
 *
 */
public class ConstraintExpressionValidationLabelDecorator extends AbstractLabelDecorator {

    private final ContractConstraintExpressionValidationRule expressionValidationRule;
    private final ContractConstraintInputsValidationRule dependenciesValidationRule;

    public ConstraintExpressionValidationLabelDecorator() {
        expressionValidationRule = new ContractConstraintExpressionValidationRule();
        dependenciesValidationRule = new ContractConstraintInputsValidationRule();
    }

    @Override
    public Image decorateImage(final Image image, final Object element) {
        return getDecoratorImage(element);
    }

    protected Image getDecoratorImage(final Object element) {
        final ContractConstraint contractConstraint = (ContractConstraint) element;
        final String name = contractConstraint.getName();
        final IStatus status = expressionValidationRule.validate(contractConstraint);
        final IStatus dependenciesStatus = dependenciesValidationRule.validate(contractConstraint);
        if (!status.isOK()) {
            return getErrorDecorator().getImage();
        } else if (!dependenciesStatus.isOK()
                && dependenciesStatus.getMessage().contains(name)) {
                return getErrorDecorator().getImage();
        }
        return null;
    }


    protected FieldDecoration getErrorDecorator() {
        return FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
    }

    @Override
    public String decorateText(final String text, final Object element) {
        final ContractConstraint constraint = (ContractConstraint) element;
        final IStatus status = expressionValidationRule.validate(constraint);
        if (!status.isOK()) {
            return status.getMessage();
        }
        return null;
    }
}