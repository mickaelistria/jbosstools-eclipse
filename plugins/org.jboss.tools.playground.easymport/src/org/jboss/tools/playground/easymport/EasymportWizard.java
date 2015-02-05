package org.jboss.tools.playground.easymport;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;

public class EasymportWizard extends Wizard implements IImportWizard {

	private File initialSelection;
	private Set<IWorkingSet> initialWorkingSets = new HashSet<IWorkingSet>();
	private EasymportWizardPage page;
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if (selection != null) {
			for (Object item : selection.toList()) {
				File asFile = toFile(item);
				if (asFile != null && this.initialSelection == null) {
					this.initialSelection = asFile;
				} else {
					IWorkingSet asWorkingSet = toWorkingSet(item);
					if (asWorkingSet != null) {
						this.initialWorkingSets.add(asWorkingSet);
					}
				}
			}
		}
	}
	
	private File toFile(Object o) {
		if (o instanceof File) {
			return (File)o;
		} else if (o instanceof IResource) {
			return ((IResource)o).getLocation().toFile();
		} else if (o instanceof IAdaptable) {
			IResource resource = (IResource) ((IAdaptable)o).getAdapter(IResource.class);
			if (resource != null) {
				return resource.getLocation().toFile();
			}
		}
		return null;
	}
	
	private IWorkingSet toWorkingSet(Object o) {
		if (o instanceof IWorkingSet) {
			return (IWorkingSet)o;
		} else if (o instanceof IAdaptable) {
			return (IWorkingSet) ((IAdaptable)o).getAdapter(IWorkingSet.class);
		}
		return null;
	}
	
	@Override
	public void addPages() {
		this.page = new EasymportWizardPage(this.initialSelection, this.initialWorkingSets);
		this.page.setWizard(this);
		addPage(this.page);
	}

	@Override
	public boolean performFinish() {
		getDialogSettings().put(EasymportWizardPage.ROOT_DIRECTORY, page.getSelectedRootDirectory().getAbsolutePath());
		final File directory = this.page.getSelectedRootDirectory();
		final Set<IWorkingSet> workingSets = this.page.getSelectedWorkingSets();
		Display.getCurrent().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				new OpenFolderCommand().importProjectsFromDirectory(directory, workingSets);
			}
		});
		return true;
	}

	@Override
	public IDialogSettings getDialogSettings() {
		IDialogSettings dialogSettings = super.getDialogSettings();
		if (dialogSettings == null) {
			dialogSettings = Activator.getDefault().getDialogSettings();
			setDialogSettings(dialogSettings);
		}
		return dialogSettings;
	}

}
