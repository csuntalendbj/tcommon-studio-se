// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.maven.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathAttribute;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.osgi.service.prefs.BackingStoreException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.designer.maven.model.MavenSystemFolders;
import org.talend.designer.maven.model.TalendMavenConstants;

/**
 * DOC zwxue class global comment. Detailled comment
 */
@SuppressWarnings("restriction")
public class MavenProjectUtils {

    public static void enableMavenNature(IProgressMonitor monitor, IProject project) {
        IProjectConfigurationManager projectConfigurationManager = MavenPlugin.getProjectConfigurationManager();
        ProjectImportConfiguration importConfiguration = new ProjectImportConfiguration();
        try {
            projectConfigurationManager.enableMavenNature(project, importConfiguration.getResolverConfiguration(), monitor);
            changeClasspath(monitor, project);
            IJavaProject javaProject = JavaCore.create(project);
            clearProjectIndenpendComplianceSettings(javaProject);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    public static void disableMavenNature(IProgressMonitor monitor, IProject project) {
        try {
            IProjectConfigurationManager projectConfigurationManager = MavenPlugin.getProjectConfigurationManager();
            projectConfigurationManager.disableMavenNature(project, monitor);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    public static boolean hasMavenNature(IProject project) {
        try {
            return project.hasNature(IMavenConstants.NATURE_ID);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

    public static void updateMavenProject(IProgressMonitor monitor, IProject project) throws CoreException {
        // async way
        // MavenUpdateRequest mavenUpdateRequest = new MavenUpdateRequest(project, true, false);
        // MavenPlugin.getMavenProjectRegistry().refresh(mavenUpdateRequest);
        if (project.getName().equals(TalendMavenConstants.PROJECT_NAME)) {
            // do not update .Java project.
            return;
        }
        MavenPlugin.getProjectConfigurationManager().updateProjectConfiguration(project, monitor);

        changeClasspath(monitor, project);
        
        // only need this when pom has no parent.
        // IJavaProject javaProject = JavaCore.create(project);
        // clearProjectIndenpendComplianceSettings(javaProject);
    }

    public static void changeClasspath(IProgressMonitor monitor, IProject p) {
        try {
            IJavaProject javaProject = JavaCore.create(p);
            IClasspathEntry[] rawClasspathEntries = javaProject.getRawClasspath();
            boolean changed = false;
            boolean foundResources = false;

            for (int index = 0; index < rawClasspathEntries.length; index++) {
                IClasspathEntry entry = rawClasspathEntries[index];

                IClasspathEntry newEntry = null;
                if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    IPath path = entry.getPath();
                    if (p.getFullPath().isPrefixOf(path)) {
                        path = path.removeFirstSegments(1);
                    }

                    // src/main/resources, in order to removing the 'excluding="**"'.
                    if (MavenSystemFolders.RESOURCES.getPath().equals(path.toString())) {
                        foundResources = true;
                        newEntry = JavaCore.newSourceEntry(entry.getPath(), new IPath[0], new IPath[0], //
                                entry.getOutputLocation(), entry.getExtraAttributes());
                    }

                    // src/test/resources, in order to removing the 'excluding="**"'.
                    if (MavenSystemFolders.RESOURCES_TEST.getPath().equals(path.toString())) {
                        newEntry = JavaCore.newSourceEntry(entry.getPath(), new IPath[0], new IPath[0], //
                                entry.getOutputLocation(), entry.getExtraAttributes());
                    }

                } else if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                    // remove the special version of jre in container.
                    IPath defaultJREContainerPath = JavaRuntime.newDefaultJREContainerPath();
                    if (defaultJREContainerPath.isPrefixOf(entry.getPath())) {
                        // JavaRuntime.getDefaultJREContainerEntry(); //missing properties
                        newEntry = JavaCore.newContainerEntry(defaultJREContainerPath, entry.getAccessRules(),
                                entry.getExtraAttributes(), entry.isExported());
                    }
                }
                if (newEntry != null) {
                    rawClasspathEntries[index] = newEntry;
                    changed = true;
                }

            }
            if (!foundResources) {
                List<IClasspathEntry> list = new LinkedList<>(Arrays.asList(rawClasspathEntries));
                IFolder resources = p.getFolder("src/main/resources");
                IFolder output = p.getFolder("target/classes");
                ClasspathAttribute attribute = new ClasspathAttribute("maven.pomderived", Boolean.TRUE.toString());
                IClasspathEntry newEntry = JavaCore.newSourceEntry(resources.getFullPath(), new IPath[0], new IPath[0],
                        output.getFullPath(), new IClasspathAttribute[] { attribute });
                list.add(1, newEntry);
                rawClasspathEntries = list.toArray(new IClasspathEntry[] {});
                changed = true;
            }
            if (changed) {
                javaProject.setRawClasspath(rawClasspathEntries, monitor);
            }
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * Clear compliance settings from project, and set them into Eclipse compliance settings
     * 
     * @param javaProject
     */
    private static void clearProjectIndenpendComplianceSettings(IJavaProject javaProject) {

        Map<String, String> projectComplianceOptions = javaProject.getOptions(false);
        if (projectComplianceOptions == null || projectComplianceOptions.isEmpty()) {
            return;
        }
        String compilerCompliance = JavaUtils.getProjectJavaVersion();
        // clear compliance settings from project
        Set<String> keySet = projectComplianceOptions.keySet();
        for (String key : keySet) {
            javaProject.setOption(key, null);
        }
        if (compilerCompliance != null) {
            IEclipsePreferences eclipsePreferences = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
            // set compliance settings to Eclipse
            Map<String, String> complianceOptions = new HashMap<String, String>();
            JavaCore.setComplianceOptions(compilerCompliance, complianceOptions);
            if (!complianceOptions.isEmpty()) {
                Set<Entry<String, String>> entrySet = complianceOptions.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    eclipsePreferences.put(entry.getKey(), entry.getValue());
                }
            }
            try {
                // save changes
                eclipsePreferences.flush();
            } catch (BackingStoreException e) {
                ExceptionHandler.process(e);
            }
        }
    }

}
