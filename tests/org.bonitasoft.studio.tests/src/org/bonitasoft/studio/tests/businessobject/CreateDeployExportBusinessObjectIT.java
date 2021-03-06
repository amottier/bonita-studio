/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.studio.tests.businessobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField.Type;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.studio.businessobject.core.repository.BusinessObjectModelFileStore;
import org.bonitasoft.studio.businessobject.core.repository.BusinessObjectModelRepositoryStore;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.gef.finder.SWTBotGefTestCase;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keyboard;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Romain Bioteau
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CreateDeployExportBusinessObjectIT extends SWTBotGefTestCase {

    private Keyboard keyboard;

    private File tmpFile;

    private BusinessObjectModelRepositoryStore bdmStore;

    @Override
    @Before
    public void setUp() {
        String layout = "EN_US";
        if (Platform.getOS().equals(Platform.OS_MACOSX)) {
            layout = "MAC_" + layout;
        }
        SWTBotPreferences.KEYBOARD_LAYOUT = layout;
        keyboard = KeyboardFactory.getSWTKeyboard();
        tmpFile = new File(System.getProperty("java.io.tmpdir"), "bdm.zip");

        bdmStore = RepositoryManager.getInstance().getRepositoryStore(BusinessObjectModelRepositoryStore.class);
        final BusinessObjectModelFileStore businessObjectModelFileStore = bdmStore.getChild(BusinessObjectModelFileStore.DEFAULT_BDM_FILENAME);
        if (businessObjectModelFileStore != null) {
            businessObjectModelFileStore.delete();
        }
    }

    @Override
    @After
    public void tearDown() throws Exception {
        tmpFile.delete();
    }

    @Test
    public void shouldCreateAndPublishABusinessObject() throws Exception {
        // Create a business object
        bot.menu("Development").menu("Business Data Model").menu("Manage...").click();
        bot.waitUntil(Conditions.shellIsActive("Manage Business Data Model"));

        bot.textWithLabel("Package").setText("org.model.test");

        // Add Employee Business Object
        final String listBOGroupTitle = "List of Business Objects";
        bot.buttonInGroup("Add", listBOGroupTitle).click();
        SWTBotTable table = bot.tableInGroup(listBOGroupTitle);
        table.click(0, 0);
        bot.text("BusinessObject1").typeText("Employee");
        keyboard.pressShortcut(Keystrokes.CR);

        // Add attributes
        addAttribute("Employee", "firstName", FieldType.STRING.name(), 0);
        setAttributeLength("Employee", "firstName", "25");
        setMandatory("Employee", "firstName");

        addAttribute("Employee", "lastaNme", FieldType.STRING.name(), 1);
        addAttribute("Employee", "birthDate", FieldType.DATE.name(), 2);
        addAttribute("Employee", "age", FieldType.INTEGER.name(), 3);
        addAttribute("Employee", "married", FieldType.BOOLEAN.name(), 4);
        addAttribute("Employee", "resume", FieldType.TEXT.name(), 5);
        addAttribute("Employee", "salary", FieldType.DOUBLE.name(), 6);
        addAttribute("Employee", "skills", FieldType.STRING.name(), 7);
        setMultiple("Employee", "skills");

        addAttribute("Employee", "manager", "Employee", 8);
        setRelationType("Employee", "manager", "Aggregation");

        // Add constraint
        addConstraint("Employee", "FIRSTLASTNAMEUNIQUE", new String[] { "firstName -- STRING", "lastaNme -- STRING" }, 0);

        // Add index
        addIndex("Employee", "NAMEINDEX", new String[] { "firstName -- STRING", "lastaNme -- STRING" }, 0);

        SWTBotShell activeShell = bot.activeShell();
        bot.button(IDialogConstants.FINISH_LABEL).click();
        bot.waitUntil(Conditions.shellCloses(activeShell), 50000);

        // Edit business object
        bot.menu("Development").menu("Business Data Model").menu("Manage...").click();
        table = bot.tableInGroup(listBOGroupTitle);

        final SWTBotTable attributeTable = bot.tableInGroup("Employee");
        attributeTable.click(1, 0);
        bot.textInGroup("lastaNme", "Employee").typeText("lastName");
        keyboard.pressShortcut(Keystrokes.CR);
        activeShell = bot.activeShell();
        bot.button(IDialogConstants.FINISH_LABEL).click();
        bot.button(IDialogConstants.OK_LABEL).click();
        bot.waitUntil(Conditions.shellIsActive("Validation failed"));
        bot.button(IDialogConstants.OK_LABEL).click();

        editConstraint("Employee", new String[] { "firstName -- STRING", "lastName -- STRING" }, 0);
        editIndex("Employee", "NAMEINDEX", new String[] { "lastName -- STRING" }, 0);

        // Add custom query
        final Map<String, String> queryParam = new HashMap<String, String>();
        queryParam.put("maxSalary", Double.class.getName());
        addCustomQuery("Employee", "findByMaxSalary", "SELECT e FROM Employee e WHERE e.salary < :maxSalary", queryParam, "Multiple (java.util.List)", 0);

        bot.button(IDialogConstants.FINISH_LABEL).click();
        bot.button(IDialogConstants.OK_LABEL).click();
        bot.waitUntil(Conditions.shellCloses(activeShell), 30000);

        // Validate model content
        validateBDMContent();

        // Export business data model
        exportBDM();

    }

    protected void validateBDMContent() {
        final BusinessObjectModelFileStore fStore = bdmStore.getChild(BusinessObjectModelFileStore.DEFAULT_BDM_FILENAME);
        assertThat(fStore).isNotNull();
        final BusinessObjectModel businessObjectModel = fStore.getContent();
        assertThat(businessObjectModel).isNotNull();
        assertThat(businessObjectModel.getBusinessObjects()).extracting("qualifiedName").containsOnly("org.model.test.Employee");
        final BusinessObject employeeBusinessObject = businessObjectModel.getBusinessObjects().get(0);
        assertThat(employeeBusinessObject.getFields())
                .extracting("name", "type")
                .contains(tuple("firstName", FieldType.STRING),
                        tuple("lastName", FieldType.STRING),
                        tuple("birthDate", FieldType.DATE),
                        tuple("age", FieldType.INTEGER),
                        tuple("married", FieldType.BOOLEAN),
                        tuple("resume", FieldType.TEXT),
                        tuple("salary", FieldType.DOUBLE),
                        tuple("skills", FieldType.STRING));
        assertThat(employeeBusinessObject.getFields())
                .extractingResultOf("isNullable", Boolean.class)
                .containsExactly(false,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true);
        assertThat(employeeBusinessObject.getFields())
                .extractingResultOf("isCollection", Boolean.class)
                .containsExactly(false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        true,
                        false);
        assertThat(employeeBusinessObject.getFields())
                .extracting("name", "type")
                .contains(tuple("manager", Type.AGGREGATION));
        assertThat(((SimpleField) employeeBusinessObject.getFields().get(0)).getLength()).isEqualTo(25);

        assertThat(employeeBusinessObject.getUniqueConstraints()).hasSize(1);
        assertThat(employeeBusinessObject.getUniqueConstraints().get(0).getName()).isEqualTo("FIRSTLASTNAMEUNIQUE");
        assertThat(employeeBusinessObject.getUniqueConstraints().get(0).getFieldNames()).contains("firstName", "lastName");

        assertThat(employeeBusinessObject.getIndexes()).extracting("name", "fieldNames")
                .containsExactly(tuple("NAMEINDEX", Arrays.asList("lastName", "firstName")));

        assertThat(employeeBusinessObject.getQueries()).extracting("name", "content", "returnType")
                .containsExactly(tuple("findByMaxSalary", "SELECT e FROM Employee e WHERE e.salary < :maxSalary", List.class.getName()));
        assertThat(employeeBusinessObject.getQueries().get(0).getQueryParameters()).extracting("name", "className")
                .containsExactly(tuple("maxSalary", Double.class.getName()));
    }

    protected void addCustomQuery(final String boName, final String queryName, final String content, final Map<String, String> queryParam,
            final String returnType, final int queryIndex) {
        bot.tabItem("Queries").activate();
        bot.radio("Custom").click();
        bot.buttonInGroup("Add", boName).click();
        final SWTBotTable table = bot.tableInGroup(boName);

        table.click(queryIndex, 0);
        bot.textInGroup("query1", boName).typeText(queryName);

        table.click(queryIndex, 1);
        bot.button("...").click();
        bot.waitUntil(Conditions.shellIsActive("Create query"));
        bot.styledText().setText(content);

        final SWTBotTable paramTableBot = bot.table();
        final int rowCount = paramTableBot.rowCount();
        final List<String> items = new ArrayList<String>();
        for (int i = 0; i < rowCount; i++) {
            items.add(paramTableBot.getTableItem(i).getText());
        }
        if (!items.isEmpty()) {
            paramTableBot.select(items.toArray(new String[items.size()]));
        }
        bot.button("Delete").click();

        int index = 0;
        for (final Entry<String, String> paramEntry : queryParam.entrySet()) {
            bot.button("Add").click();
            paramTableBot.click(index, 0);
            bot.text("param1").typeText(paramEntry.getKey());

            paramTableBot.click(index, 1);
            bot.ccomboBox(String.class.getName()).setSelection(paramEntry.getValue());
            keyboard.pressShortcut(Keystrokes.CR);
            index++;
        }

        bot.comboBoxWithLabel("Result type").setSelection(returnType);
        bot.button(IDialogConstants.OK_LABEL).click();
    }

    protected void setMandatory(final String boName, final String attributeName) {
        bot.tabItem("Attributes").activate();
        final SWTBotTable attributeTable = bot.tableInGroup(boName);
        final int attributeIndex = attributeTable.indexOf(attributeName, 0);
        attributeTable.click(attributeIndex, 3);
    }

    protected void setMultiple(final String boName, final String attributeName) {
        bot.tabItem("Attributes").activate();
        final SWTBotTable attributeTable = bot.tableInGroup(boName);
        final int attributeIndex = attributeTable.indexOf(attributeName, 0);
        attributeTable.click(attributeIndex, 2);
    }

    protected void setAttributeLength(final String boName, final String attributeName, final String length) {
        bot.tabItem("Attributes").activate();
        final SWTBotTable attributeTable = bot.tableInGroup(boName);
        final int attributeIndex = attributeTable.indexOf(attributeName, 0);
        attributeTable.select(attributeIndex);
        bot.comboBoxInGroup("Details for " + attributeName).setText(length);
    }

    protected void setRelationType(final String boName, final String attributeName, final String relationType) {
        bot.tabItem("Attributes").activate();
        final SWTBotTable attributeTable = bot.tableInGroup(boName);
        final int attributeIndex = attributeTable.indexOf(attributeName, 0);
        attributeTable.select(attributeIndex);
        bot.comboBoxInGroup("Details for " + attributeName).setSelection(relationType);
    }

    protected void addAttribute(final String boName, final String attributeName, final String type, final int attributeIndex) {
        bot.tabItem("Attributes").activate();
        bot.buttonInGroup("Add", boName).click();
        final SWTBotTable attributeTable = bot.tableInGroup(boName);

        attributeTable.click(attributeIndex, 0);
        bot.textInGroup("attribute1", boName).typeText(attributeName);

        attributeTable.click(attributeIndex, 1);
        bot.ccomboBoxInGroup(boName).setSelection(type);
        keyboard.pressShortcut(Keystrokes.CR);
    }

    protected void addConstraint(final String boName, final String constraintName, final String[] selectFields, final int constraintIndex) {
        bot.tabItem("Unique constraints").activate();
        bot.buttonInGroup("Add", boName).click();
        final SWTBotTable table = bot.tableInGroup(boName);

        table.click(constraintIndex, 0);
        bot.textInGroup("UNIQUE_CONSTRAINT_1", boName).typeText(constraintName);

        table.click(constraintIndex, 1);
        bot.button("...").click();
        bot.waitUntil(Conditions.shellIsActive("Select attributes"));
        for (final String f : selectFields) {
            bot.table().getTableItem(f).check();
        }
        bot.button(IDialogConstants.OK_LABEL).click();
    }

    protected void editConstraint(final String boName, final String[] selectFields, final int constraintIndex) {
        bot.tabItem("Unique constraints").activate();
        final SWTBotTable table = bot.tableInGroup(boName);
        table.click(constraintIndex, 0);
        table.click(constraintIndex, 1);
        bot.button("...").click();
        bot.waitUntil(Conditions.shellIsActive("Select attributes"));
        for (final String f : selectFields) {
            bot.table().getTableItem(f).check();
        }
        bot.button(IDialogConstants.OK_LABEL).click();
    }

    protected void addIndex(final String boName, final String indexName, final String[] selectFields, final int indexIndex) {
        bot.tabItem("Indexes").activate();
        bot.buttonInGroup("Add", boName).click();
        final SWTBotTable table = bot.tableInGroup(boName);

        table.click(indexIndex, 0);
        bot.textInGroup("INDEX_1", boName).typeText(indexName);

        table.click(indexIndex, 1);
        bot.button("...").click();
        bot.waitUntil(Conditions.shellIsActive("Select attributes for " + indexName));
        bot.tableWithLabel("Available attributes").select(selectFields);
        bot.button("Add").click();
        bot.button(IDialogConstants.OK_LABEL).click();
    }

    protected void editIndex(final String boName, final String indexName, final String[] selectFields, final int indexIndex) {
        bot.tabItem("Indexes").activate();
        final SWTBotTable table = bot.tableInGroup(boName);
        table.click(indexIndex, 1);
        bot.button("...").click();
        bot.waitUntil(Conditions.shellIsActive("Select attributes for " + indexName));
        bot.tableWithLabel("Available attributes").select(selectFields);
        bot.button("Add").click();
        bot.tableWithLabel("Indexed attributes").select(selectFields);
        bot.button("Up").click();
        bot.button(IDialogConstants.OK_LABEL).click();
    }

    protected void exportBDM() {
        bot.menu("Development").menu("Business Data Model").menu("Export...").click();
        assertTrue("Export button should be enabled", bot.button("Export").isEnabled());

        bot.text().setText("/User/FakePath/");
        assertFalse("Export button should be disabled", bot.button("Export").isEnabled());

        bot.text().setText(tmpFile.getParentFile().getAbsolutePath());
        assertTrue("Export button should be enabled", bot.button("Export").isEnabled());

        bot.button("Export").click();

        bot.waitUntil(Conditions.shellIsActive("Export completed"), 30000);
        bot.button(IDialogConstants.OK_LABEL).click();

        assertThat(tmpFile).exists();
        assertThat(tmpFile.length()).isGreaterThan(0);
        assertThat(tmpFile.getName()).endsWith(".zip").contains("bdm");
    }

}
