/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
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
package org.bonitasoft.studio.validation.ui.view;

import java.util.Arrays;
import java.util.Map;

import org.bonitasoft.studio.common.jface.TableColumnSorter;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.model.form.Form;
import org.bonitasoft.studio.model.process.Element;
import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.model.process.diagram.part.ProcessDiagramEditorUtil;
import org.bonitasoft.studio.validation.constraints.ValidationContentProvider;
import org.bonitasoft.studio.validation.i18n.Messages;
import org.eclipse.core.internal.resources.Marker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Florine Boudin
 *
 */
public class ValidationViewPart extends ViewPart implements ISelectionListener,ISelectionChangedListener,ISelectionProvider{

	public static String ID = "org.bonitasoft.studio.validation.view";

	
	
	
	
	private TableViewer tableViewer;
	private ISelectionProvider selectionProvider;
	private TableViewerColumn severityColumn;
	private ValidationViewAction validateAction;
	
	
	/**
	 * 
	 */
	public ValidationViewPart() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {

		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 5).create());
		


		createTopComposite(mainComposite);
		createTableComposite(mainComposite);
		createValidateButton(mainComposite);

		ISelectionService ss = getSite().getWorkbenchWindow().getSelectionService();
		ss.addPostSelectionListener(this);
		if(getSite().getPage().getActiveEditor() != null){
			selectionProvider =  getSite().getPage().getActiveEditor().getEditorSite().getSelectionProvider();
			getSite().setSelectionProvider(this);

		}
		
		TableColumnSorter sorter = new TableColumnSorter(tableViewer) ;
		sorter.setColumn(severityColumn.getColumn()) ;
	}

	private void createValidateButton(Composite mainComposite) {

		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		IWorkbenchPage activePage =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		validateAction = new ValidationViewAction();
		validateAction.setActivePage(activePage);
		validateAction.setTableViewer(tableViewer);
		
		toolBar.add(validateAction);
		

	}

	/**
	 * 
	 * @param mainComposite
	 */
	private void createTableComposite(Composite mainComposite) {
		Composite tableComposite = new Composite(mainComposite, SWT.NONE);
		tableComposite.setLayout(GridLayoutFactory.fillDefaults().create());
		tableComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());


		tableViewer = new TableViewer(tableComposite,SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION) ;
		tableViewer.getTable().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(400, SWT.DEFAULT).create());
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(1));
		layout.addColumnData(new ColumnWeightData(5));
		layout.addColumnData(new ColumnWeightData(11));
		tableViewer.getTable().setLayout(layout);
		addSeverityDescriptionColumn();
		addElementNameColumn();
		addErrorDescriptionColumn();

		tableViewer.setContentProvider(new ValidationContentProvider());
		IEditorPart activeEditor = getSite().getPage().getActiveEditor();
		tableViewer.setInput(activeEditor);

		tableViewer.addSelectionChangedListener(this);


	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {

	}


	/**
	 * 
	 * @param mainComposite
	 */
	protected void createTopComposite(Composite mainComposite) {
		final Composite topComposite = new Composite(mainComposite, SWT.NONE);
		topComposite.setLayout(GridLayoutFactory.fillDefaults().extendedMargins(5, 5, 5, 0).create());
		topComposite.setLayoutData(GridDataFactory.fillDefaults().create());
	}

	/**
	 * 
	 */
	private void addElementNameColumn(){
		TableViewerColumn elements = new TableViewerColumn(tableViewer, SWT.NONE);
		elements.getColumn().setText(Messages.validationViewElementColumnName);
		elements.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				Marker marker = (Marker) element;
				try {

					String elementId = (String) marker.getAttribute(org.eclipse.gmf.runtime.common.core.resources.IMarker.ELEMENT_ID);

					if (elementId == null || !(getSite().getPage().getActiveEditor() instanceof DiagramEditor)) {
						return "";
					}
					DiagramEditor editor = (DiagramEditor) getSite().getPage().getActiveEditor();
					EObject targetView = editor.getDiagram().eResource().getEObject(elementId);
					if (targetView == null) {
						return "";
					}

					if(targetView instanceof Shape){

						Shape targetShape = (Shape)targetView;
						Element elem = (Element)targetShape.getElement();
						return elem.getName();

					}else {
						if(editor.getDiagramEditPart().resolveSemanticElement() instanceof Form){
						return ((Form)editor.getDiagramEditPart().resolveSemanticElement()).getName();
						}else if(editor.getDiagramEditPart().resolveSemanticElement() instanceof MainProcess){
							return ((MainProcess)editor.getDiagramEditPart().resolveSemanticElement()).getName();
						}
					}
				} catch (CoreException e) {
					BonitaStudioLog.error(e);
					return "";
				}
				return "";
			}
		});

	}

	private void addErrorDescriptionColumn(){
		TableViewerColumn elements = new TableViewerColumn(tableViewer, SWT.NONE);
		elements.getColumn().setText(Messages.validationViewDescriptionColumnName);
		elements.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {

				Marker marker = (Marker) element;
				try {
					return (String) marker.getAttribute("message");
				} catch (CoreException e) {
					return "";
				}
			}
		});
	}


	private void addSeverityDescriptionColumn(){
		severityColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		severityColumn.getColumn().setText(Messages.validationViewSeverityColumnName);
		severityColumn.setLabelProvider(new SeverityColumnLabelProvider());
	}


	@Override
	public void dispose() {

	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {

	}

	@Override
	public ISelection getSelection() {
		return selectionProvider.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selectionProvider.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		selectionProvider.setSelection(selection);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if(event.getSelection() instanceof StructuredSelection && ((StructuredSelection)event.getSelection()).getFirstElement() instanceof Marker){
			Marker m =(Marker) ((StructuredSelection)event.getSelection()).getFirstElement();

			String elementId = m.getAttribute(org.eclipse.gmf.runtime.common.core.resources.IMarker.ELEMENT_ID, null);
			if (elementId == null || !(getSite().getPage().getActiveEditor() instanceof DiagramEditor)) {
				return ;
			}
			DiagramEditor editor = (DiagramEditor) getSite().getPage().getActiveEditor();
			Map editPartRegistry = editor.getDiagramGraphicalViewer()
					.getEditPartRegistry();
			EObject targetView = editor.getDiagram().eResource()
					.getEObject(elementId);
			if (targetView == null) {
				return ;
			}
			EditPart targetEditPart = (EditPart) editPartRegistry.get(targetView);
			if (targetEditPart != null) {
				ProcessDiagramEditorUtil.selectElementsInDiagram(editor,
						Arrays.asList(new EditPart[] { targetEditPart }));
			}
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if(selection instanceof StructuredSelection && !tableViewer.getTable().isDisposed()){
			Object selectedEP = ((StructuredSelection) selection).getFirstElement();
			if(selectedEP instanceof IGraphicalEditPart){
				IEditorPart editorPart = getSite().getPage().getActiveEditor();
				if(editorPart != null && !editorPart.equals(tableViewer.getInput())){
					selectionProvider = editorPart.getEditorSite().getSelectionProvider();
					tableViewer.setInput(editorPart);


				}else if(editorPart != null && editorPart.equals(tableViewer.getInput())){

					tableViewer.refresh();
				}
				tableViewer.getTable().layout(true,true);
			}

			// change Validate Action
			IWorkbenchPage activePage =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			validateAction = new ValidationViewAction();
			validateAction.setActivePage(activePage);
			validateAction.setTableViewer(tableViewer);
		}
	}

	

}
