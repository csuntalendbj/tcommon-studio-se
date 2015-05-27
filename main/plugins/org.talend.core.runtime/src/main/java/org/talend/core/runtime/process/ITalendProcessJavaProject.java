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
package org.talend.core.runtime.process;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

/**
 * created by ggu on 26 Jan 2015 Detailled comment
 *
 */
public interface ITalendProcessJavaProject {

    IJavaProject getJavaProject();

    IProject getProject();

    IFile getProjectPom();

    /**
     * src/main/java
     */
    IFolder getSrcFolder();

    /**
     * src/test/java
     */
    IFolder getTestSrcFolder();

    /**
     * src/main/resources
     */
    IFolder getResourcesFolder();

    /**
     * src/test/resources
     */
    IFolder getTestResourcesFolder();

    /**
     * lib
     */
    IFolder getLibFolder();

    /**
     * target/classes
     */
    IFolder getOutputFolder();

    /**
     * target/test-classes
     */
    IFolder getTestOutputFolder();

    /**
     * src/main/assemblies
     */
    IFolder getAssembliesFolder();

    /**
     * src/main/templates
     */
    IFolder getTemplatesFolder();

    /**
     * src/main/items
     */
    IFolder getItemsFolder();

    /**
     * tests
     */
    IFolder getTestsFolder();

    /**
     * create sub folder under target.
     */
    IFolder getTargetFolder();

    /**
     * 
     * create sub folder under src/main/java.
     */
    IFolder getSrcSubFolder(IProgressMonitor monitor, String path);

    /**
     * 
     * create sub folder under src/main/resources.
     */
    IFolder getResourceSubFolder(IProgressMonitor monitor, String path);

    /**
     * create the sub folder under baseFolder.
     */
    IFolder createSubFolder(IProgressMonitor monitor, IFolder baseFolder, String path);

    /**
     * will clean the files or folders under this folder.
     */
    boolean cleanFolder(IProgressMonitor monitor, IFolder folder) throws CoreException;

    /**
     * update routines pom, if withBuild is true, will install routines jar with maven.
     * 
     */
    void updateRoutinesPom(boolean withBuild, boolean inBackgroud);

    /**
     * 
     * check and add the child module in project.
     */
    void addChildModules(boolean removeOld, String... childrenModules);

    /**
     * 
     * build the modules jobs with goals(like compile, package, install, etc).
     * 
     * if null, will build whole project. if empty (String[0]), willn't build any thing.
     * 
     * if goals is not null, will use maven to build. and if childrenModules is null, will try to build project pom.
     * else will build each modules.
     */
    void buildModules(String goals, String[] childrenModules);

    void buildModules(String goals, String[] childrenModules, String programArgs);

    /**
     * 
     * try to clean the pom_xxx.xml and the assembly_xxx.xml, also clean up the pom.xml, like modules, dependencies.
     */
    void cleanMavenFiles(IProgressMonitor monitor) throws Exception;

}
