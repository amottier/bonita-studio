/**
 * 
 */
package org.bonitasoft.studio.common.emf.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.studio.model.form.AbstractTable;
import org.bonitasoft.studio.model.form.CheckBoxSingleFormField;
import org.bonitasoft.studio.model.form.DateFormField;
import org.bonitasoft.studio.model.form.DurationFormField;
import org.bonitasoft.studio.model.form.FileWidget;
import org.bonitasoft.studio.model.form.FormFactory;
import org.bonitasoft.studio.model.form.Group;
import org.bonitasoft.studio.model.form.ListFormField;
import org.bonitasoft.studio.model.form.NextFormButton;
import org.bonitasoft.studio.model.form.RadioFormField;
import org.bonitasoft.studio.model.form.SelectFormField;
import org.bonitasoft.studio.model.form.SuggestBox;
import org.bonitasoft.studio.model.form.TextFormField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Romain
 *
 */
public class WidgetHelperTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldGetAssociatedReturnType_ReturnStringClassName() throws Exception {
		TextFormField textField = FormFactory.eINSTANCE.createTextFormField();
		assertThat(WidgetHelper.getAssociatedReturnType(textField)).isEqualTo(String.class.getName());

		RadioFormField radioField = FormFactory.eINSTANCE.createRadioFormField();
		assertThat(WidgetHelper.getAssociatedReturnType(radioField)).isEqualTo(String.class.getName());

		SelectFormField selectField = FormFactory.eINSTANCE.createSelectFormField();
		assertThat(WidgetHelper.getAssociatedReturnType(selectField)).isEqualTo(String.class.getName());
		
		SuggestBox suggestBox = FormFactory.eINSTANCE.createSuggestBox();
		assertThat(WidgetHelper.getAssociatedReturnType(suggestBox)).isEqualTo(String.class.getName());
	}

	@Test
	public void shouldGetAssociatedReturnType_ReturnBooleanClassName() throws Exception {
		NextFormButton nextButton = FormFactory.eINSTANCE.createNextFormButton();
		assertThat(WidgetHelper.getAssociatedReturnType(nextButton)).isEqualTo(Boolean.class.getName());

		CheckBoxSingleFormField checkboxButton = FormFactory.eINSTANCE.createCheckBoxSingleFormField();
		assertThat(WidgetHelper.getAssociatedReturnType(checkboxButton)).isEqualTo(Boolean.class.getName());
	}

	@Test
	public void shouldGetAssociatedReturnType_ReturnLongClassName() throws Exception {
		DurationFormField duration = FormFactory.eINSTANCE.createDurationFormField();
		assertThat(WidgetHelper.getAssociatedReturnType(duration)).isEqualTo(Long.class.getName());
	}

	@Test
	public void shouldGetAssociatedReturnType_ReturnMapClassName() throws Exception {
		Group group = FormFactory.eINSTANCE.createGroup();
		assertThat(WidgetHelper.getAssociatedReturnType(group)).isEqualTo(Map.class.getName());
	}

	@Test
	public void shouldGetAssociatedReturnType_ReturnListClassName() throws Exception {
		ListFormField list = FormFactory.eINSTANCE.createListFormField();
		assertThat(WidgetHelper.getAssociatedReturnType(list)).isEqualTo(List.class.getName());

		AbstractTable table =  FormFactory.eINSTANCE.createDynamicTable();
		assertThat(WidgetHelper.getAssociatedReturnType(table)).isEqualTo(List.class.getName());
		
		TextFormField textField = FormFactory.eINSTANCE.createTextFormField();
		textField.setDuplicate(true);
		assertThat(WidgetHelper.getAssociatedReturnType(textField)).isEqualTo(List.class.getName());
	}

	@Test
	public void shouldGetAssociatedReturnType_ReturnDateClassName() throws Exception {
		DateFormField date = FormFactory.eINSTANCE.createDateFormField();
		assertThat(WidgetHelper.getAssociatedReturnType(date)).isEqualTo(Date.class.getName());
	}
	
	@Test
	public void shouldGetAssociatedReturnType_ReturnDocumentValueClassName() throws Exception {
		FileWidget file = FormFactory.eINSTANCE.createFileWidget();
		assertThat(WidgetHelper.getAssociatedReturnType(file)).isEqualTo(DocumentValue.class.getName());
	}
	
	@Test
	public void shouldGetAssociatedReturnType_ReturnIntegeerClassName() throws Exception {
		TextFormField text = FormFactory.eINSTANCE.createTextFormField();
		text.setReturnTypeModifier(Integer.class.getName());
		assertThat(WidgetHelper.getAssociatedReturnType(text)).isEqualTo(Integer.class.getName());
	}


}