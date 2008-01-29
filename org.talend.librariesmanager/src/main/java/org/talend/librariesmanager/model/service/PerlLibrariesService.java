// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.MessageBoxExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.ModuleNeeded.ELibraryInstallStatus;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.librariesmanager.Activator;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.librariesmanager.prefs.PreferencesUtilities;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class PerlLibrariesService extends AbstractLibrariesService {

    private static Logger log = Logger.getLogger(PerlLibrariesService.class);

    private static boolean isLibSynchronized;

    @Override
    public String getLibrariesPath() {
        return PreferencesUtilities.getLibrariesPath(ECodeLanguage.PERL);
    }

    @Override
    public URL getRoutineTemplate() {
        return Activator.BUNDLE.getEntry("resources/perl/" + SOURCE_PERL_ROUTINES_FOLDER + "/Template.pm");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.general.ILibrariesService#getSystemRoutines()
     */
    public List<URL> getSystemRoutines() {
        return FilesUtils.getFilesFromFolder(Activator.BUNDLE, "resources/perl/" + SOURCE_PERL_ROUTINES_FOLDER + "/system/",
                ".pm");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.general.ILibrariesService#getTalendRoutines()
     */
    public URL getTalendRoutinesFolder() throws IOException {
        URL url = Activator.BUNDLE.getEntry("resources/perl/talend");
        return FileLocator.toFileURL(url);
    }

    public List<URL> getTalendRoutines() {
        return FilesUtils.getFilesFromFolder(Activator.BUNDLE, "resources/perl/talend", "");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.general.ILibrariesService#syncLibraries()
     */
    public void syncLibraries() {
        File target = new File(getLibrariesPath());
        try {
            // 1. Talend libraries:
            File source = new File(FileLocator.resolve(Activator.BUNDLE.getEntry("resources/perl/")).getFile());
            FilesUtils.copyFolder(source, target, false, FilesUtils.getExcludeSystemFilesFilter(), null, true);

            // 2. Components libraries
            IComponentsService service = (IComponentsService) GlobalServiceRegister.getDefault().getService(
                    IComponentsService.class);
            File componentsLibraries = new File(service.getComponentsFactory().getComponentPath().getFile());
            SpecificFilesUtils.copySpecificSubFolder(componentsLibraries, target, FilesUtils.getExcludeSystemFilesFilter(),
                    FilesUtils.getAcceptPMFilesFilter(), "modules");

            log.debug("Perl libraries synchronization done");
            this.isLibSynchronized = true;

        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }

    public void checkInstalledLibraries() {
        List<ModuleNeeded> toCheck = ModulesNeededProvider.getModulesNeeded();

        if (toCheck.isEmpty()) {
            return;
        }

        // This map contains perl module name as keys and list of object using it as values :
        Map<String, List<ModuleNeeded>> componentsByModules = new HashMap<String, List<ModuleNeeded>>();

        String[] params = new String[] {};
        for (ModuleNeeded current : toCheck) {
            String moduleName = current.getModuleName();
            List<ModuleNeeded> listForThisModule = componentsByModules.get(moduleName);
            if (listForThisModule == null) {
                // We have a new perl module to check :
                listForThisModule = new ArrayList<ModuleNeeded>();
                // Add it in the map :
                componentsByModules.put(moduleName, listForThisModule);
                // And in the params perl command line :
                params = (String[]) ArrayUtils.add(params, MODULE_PARAM_KEY + moduleName);
            }
            // Add this import in the perl module list :
            listForThisModule.add(current);

            // Set the status to unknow as after treatment, modules not in perl response are unknown
            current.setStatus(ELibraryInstallStatus.UNKNOWN);
        }

        try {
            // String checkPerlModuleAbsolutePathOLD = FileLocator
            // .toFileURL(Activator.BUNDLE.getEntry(CHECK_PERL_MODULE_RELATIVE_PATH)).getPath();
            //            
            String checkPerlModuleAbsolutePath = getLibrariesPath() + CHECK_PERL_MODULE_RELATIVE_PATH;

            StringBuffer out = new StringBuffer();
            StringBuffer err = new StringBuffer();

            IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                    IRunProcessService.class);
            service.perlExec(out, err, new Path(checkPerlModuleAbsolutePath), null, Level.DEBUG, "", null, -1, -1, params);

            analyzeResponse(out, componentsByModules);

            if (err.length() > 0) {
                throw new ProcessorException(err.toString());
            }

            // } catch (IOException e) {
            // ExceptionHandler.process(e);
        } catch (ProcessorException e) {
            MessageBoxExceptionHandler.process(e);
        }

    }

    private static final String CHECK_PERL_MODULE_RELATIVE_PATH = "/check_modules.pl";

    private static final String MODULE_PARAM_KEY = "--module=";

    private static final String RESULT_SEPARATOR = " => ";

    private static final String RESULT_KEY_KO = "KO";

    private static final String RESULT_KEY_OK = "OK";

    private void analyzeResponse(StringBuffer buff, Map<String, List<ModuleNeeded>> componentsByModules) {

        String[] lines = buff.toString().split("\n");
        for (String line : lines) {
            if (line != null && line.length() > 0) {
                // Treat a perl response line :
                String[] elts = line.split(RESULT_SEPARATOR);

                List<ModuleNeeded> componentsToTreat = componentsByModules.get(elts[0]);

                if (componentsToTreat != null) {
                    // Define status regarding the perl response :
                    ELibraryInstallStatus status = ELibraryInstallStatus.UNKNOWN;
                    if (elts[1].startsWith(RESULT_KEY_OK)) {
                        status = ELibraryInstallStatus.INSTALLED;
                    } else if (elts[1].startsWith(RESULT_KEY_KO)) {
                        status = ELibraryInstallStatus.NOT_INSTALLED;
                    }

                    // Step on objects using this module and set their status :
                    for (ModuleNeeded current : componentsToTreat) {
                        current.setStatus(status);
                    }
                }
            }

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.general.ILibrariesService#isLibSynchronized()
     */
    public boolean isLibSynchronized() {
        return this.isLibSynchronized;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.general.ILibrariesService#getJavaLibrariesPath()
     */
    public String getJavaLibrariesPath() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.general.ILibrariesService#getPerlLibrariesPath()
     */
    public String getPerlLibrariesPath() {
        // TODO Auto-generated method stub
        return null;
    }

}
