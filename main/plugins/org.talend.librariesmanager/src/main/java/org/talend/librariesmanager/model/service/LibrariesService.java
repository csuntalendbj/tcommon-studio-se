// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.librariesmanager.model.service;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.exception.BusinessException;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.general.ModuleNeeded.ELibraryInstallStatus;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.Problem;
import org.talend.designer.codegen.PigTemplate;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class LibrariesService implements ILibrariesService {

    private static ILibrariesService javaService = new JavaLibrariesService();

    public LibrariesService() {

    }

    private ILibrariesService getLibrariesService() {
        return javaService;
    }

    @Override
    public void deployLibrary(URL source) throws IOException {
        this.getLibrariesService().deployLibrary(source);
    }

    @Override
    public void deployLibrarys(URL[] source) throws IOException {
        this.getLibrariesService().deployLibrarys(source);
    }

    @Override
    public String getPerlLibrariesPath() {
        return null;
    }

    @Override
    public ELibraryInstallStatus getLibraryStatus(String libName) throws BusinessException {
        return this.getLibrariesService().getLibraryStatus(libName);
    }

    @Override
    public List<Problem> getProblems(INode node, IElement element) {
        return this.getLibrariesService().getProblems(node, element);
    }

    @Override
    public URL getRoutineTemplate() {
        return this.getLibrariesService().getRoutineTemplate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.general.ILibrariesService#getPigudfTemplate()
     */
    @Override
    public URL getPigudfTemplate(PigTemplate template) {
        return this.getLibrariesService().getPigudfTemplate(template);
    }

    @Override
    public URL getBeanTemplate() {
        return this.getLibrariesService().getBeanTemplate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.general.ILibrariesService#getSqlPatternTemplate()
     */
    @Override
    public URL getSqlPatternTemplate() {
        return this.getLibrariesService().getSqlPatternTemplate();
    }

    @Override
    public List<URL> getSystemRoutines() {
        return this.getLibrariesService().getSystemRoutines();
    }

    @Override
    public List<URL> getTalendRoutinesFolder() throws IOException {
        return this.getLibrariesService().getTalendRoutinesFolder();
    }

    @Override
    public List<URL> getTalendBeansFolder() throws IOException {
        return this.getLibrariesService().getTalendBeansFolder();
    }

    @Override
    public List<URL> getTalendRoutines() {
        return this.getLibrariesService().getTalendRoutines();
    }

    @Override
    public void syncLibraries(IProgressMonitor... monitorWrap) {
        this.getLibrariesService().syncLibraries(monitorWrap);
    }

    @Override
    public void checkLibraries() {
        this.getLibrariesService().checkLibraries();
    }

    @Override
    public void addChangeLibrariesListener(IChangedLibrariesListener listener) {
        this.getLibrariesService().addChangeLibrariesListener(listener);
    }

    @Override
    public void removeChangeLibrariesListener(IChangedLibrariesListener listener) {
        this.getLibrariesService().removeChangeLibrariesListener(listener);
    }

    @Override
    public void resetModulesNeeded() {
        this.getLibrariesService().resetModulesNeeded();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.general.ILibrariesService#undeployLibrary(java.net.URL)
     */
    @Override
    public void undeployLibrary(String path) throws IOException {
        this.getLibrariesService().undeployLibrary(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.general.ILibrariesService#isLibSynchronized()
     */
    @Override
    public boolean isLibSynchronized() {
        return this.getLibrariesService().isLibSynchronized();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.general.ILibrariesService#getSystemSQLPatterns()
     */
    @Override
    public List<URL> getSystemSQLPatterns() {
        return this.getLibrariesService().getSystemSQLPatterns();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.core.model.general.ILibrariesService#resetModulesNeededForCurrentJob(org.talend.core.model.properties
     * .Item)
     */
    @Override
    public void updateModulesNeededForCurrentJob(IProcess process) {
        this.getLibrariesService().updateModulesNeededForCurrentJob(process);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.core.model.general.ILibrariesService#syncLibrariesFromLibs(org.eclipse.core.runtime.IProgressMonitor
     * [])
     */
    @Override
    public void syncLibrariesFromLibs(IProgressMonitor... monitorWrap) {
        this.getLibrariesService().syncLibrariesFromLibs(monitorWrap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.core.model.general.ILibrariesService#syncLibrariesFromApp(org.eclipse.core.runtime.IProgressMonitor[])
     */
    @Override
    public void syncLibrariesFromApp(IProgressMonitor... monitorWrap) {
        this.getLibrariesService().syncLibrariesFromApp(monitorWrap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.general.ILibrariesService#cleanLibs()
     */
    @Override
    public void cleanLibs() {
        this.getLibrariesService().cleanLibs();
    }
}
