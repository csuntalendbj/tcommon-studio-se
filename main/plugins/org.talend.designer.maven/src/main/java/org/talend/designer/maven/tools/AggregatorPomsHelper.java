// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.maven.tools;

import static org.talend.designer.maven.model.TalendJavaProjectConstants.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.MavenPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.maven.launch.MavenPomCommandLauncher;
import org.talend.designer.maven.model.TalendJavaProjectConstants;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.template.MavenTemplateManager;
import org.talend.designer.maven.tools.creator.CreateMavenBeanPom;
import org.talend.designer.maven.tools.creator.CreateMavenPigUDFPom;
import org.talend.designer.maven.tools.creator.CreateMavenRoutinePom;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.RepositoryConstants;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class AggregatorPomsHelper {

    private Project project;

    public AggregatorPomsHelper(Project project) {
        this.project = project;
    }

    public void createRootPom(IFolder folder, IProgressMonitor monitor) throws Exception {
        IFile pomFile = folder.getFile(TalendMavenConstants.POM_FILE_NAME);
        if (!pomFile.exists()) {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(MavenTemplateManager.KEY_PROJECT_NAME, project.getTechnicalLabel());
            Model model = MavenTemplateManager.getCodeProjectTemplateModel(parameters);
            PomUtil.savePom(monitor, model, pomFile);
        }
    }

    public void installRootPom(boolean current) throws Exception {
        IFile pomFile = getProjectPomsFolder().getFile(TalendMavenConstants.POM_FILE_NAME);
        installPom(pomFile, current);
    }

    public void installPom(IFile pomFile, boolean current) throws Exception {
        Model model = MavenPlugin.getMaven().readModel(pomFile.getLocation().toFile());
        if (!isPomInstalled(model.getGroupId(), model.getArtifactId(), model.getVersion())) {
            MavenPomCommandLauncher launcher = new MavenPomCommandLauncher(pomFile, TalendMavenConstants.GOAL_INSTALL);
            if (current) {
                Map<String, Object> argumentsMap = new HashMap<>();
                argumentsMap.put(TalendProcessArgumentConstant.ARG_PROGRAM_ARGUMENTS, "-N"); // $NON-NLS-N$
                launcher.setArgumentsMap(argumentsMap);
            }
            launcher.execute(new NullProgressMonitor());
        }
    }

    public boolean isRootPomInstalled() {
        return isPomInstalled(PomIdsHelper.getProjectGroupId(project), PomIdsHelper.getProjectArtifactId(),
                PomIdsHelper.getProjectVersion(project));
    }

    public boolean isPomInstalled(String groupId, String artifactId, String version) {
        String mvnUrl = MavenUrlHelper.generateMvnUrl(groupId, artifactId, version, MavenConstants.PACKAGING_POM, null);
        return PomUtil.isAvailable(mvnUrl);
    }

    public IFolder getProjectPomsFolder() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        return workspace.getRoot().getFolder(new Path(project.getTechnicalLabel() + "/" + DIR_POMS)); //$NON-NLS-1$
    }

    public void createAggregatorFolderPom(IFolder folder, String folderName, String groupId, IProgressMonitor monitor)
            throws Exception {
        if (folder != null) {
            IFile pomFile = folder.getFile(TalendMavenConstants.POM_FILE_NAME);
            Model model = MavenTemplateManager.getAggregatorFolderTemplateModel(pomFile, groupId, folderName,
                    project.getTechnicalLabel());
            PomUtil.savePom(monitor, model, pomFile);
        }
    }

    public IFolder getDeploymentsFolder() {
        return getProjectPomsFolder().getFolder(DIR_DEPLOYMENTS);
    }

    public static void updateCodeProjectPom(IProgressMonitor monitor, ERepositoryObjectType type, IFile pomFile)
            throws Exception {
        if (type != null) {
            if (ERepositoryObjectType.ROUTINES == type) {
                createRoutinesPom(pomFile, monitor);
            } else if (ERepositoryObjectType.PIG_UDF == type) {
                createPigUDFsPom(pomFile, monitor);
            } else {
                if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                    ICamelDesignerCoreService service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                            .getService(ICamelDesignerCoreService.class);
                    ERepositoryObjectType beanType = service.getBeansType();
                    if (beanType != null && beanType == type) {
                        createBeansPom(pomFile, monitor);
                    }
                }
            }
        }
    }

    public static void createRoutinesPom(IFile pomFile, IProgressMonitor monitor) throws Exception {
        CreateMavenRoutinePom createTemplatePom = new CreateMavenRoutinePom(pomFile);
        createTemplatePom.create(monitor);
    }

    public static void createPigUDFsPom(IFile pomFile, IProgressMonitor monitor) throws Exception {
        CreateMavenPigUDFPom createTemplatePom = new CreateMavenPigUDFPom(pomFile);
        createTemplatePom.create(monitor);
    }

    public static void createBeansPom(IFile pomFile, IProgressMonitor monitor) throws Exception {
        CreateMavenBeanPom createTemplatePom = new CreateMavenBeanPom(pomFile);
        createTemplatePom.create(monitor);
    }

    public void createUserDefinedFolderPom(IFile pomFile, String folderName, String groupId, IProgressMonitor monitor) {
        // TODO
    }

    public static void addToParentModules(IFile pomFile) throws Exception {
        IFile parentPom = getParentModulePomFile(pomFile);
        if (parentPom != null) {
            IPath relativePath = pomFile.getParent().getLocation().makeRelativeTo(parentPom.getParent().getLocation());
            Model model = MavenPlugin.getMaven().readModel(parentPom.getContents());
            List<String> modules = model.getModules();
            if (modules == null) {
                modules = new ArrayList<>();
                model.setModules(modules);
            }
            if (!modules.contains(relativePath.toPortableString())) {
                modules.add(relativePath.toPortableString());
                PomUtil.savePom(null, model, parentPom);
            }
        }
    }

    public static void removeFromParentModules(IFile pomFile) throws Exception {
        IFile parentPom = getParentModulePomFile(pomFile);
        if (parentPom != null) {
            IPath relativePath = pomFile.getParent().getLocation().makeRelativeTo(parentPom.getParent().getLocation());
            Model model = MavenPlugin.getMaven().readModel(parentPom.getContents());
            List<String> modules = model.getModules();
            if (modules == null) {
                modules = new ArrayList<>();
                model.setModules(modules);
            }
            if (modules != null && modules.contains(relativePath.toPortableString())) {
                modules.remove(relativePath.toPortableString());
                PomUtil.savePom(null, model, parentPom);
            }
        }
    }

    private static IFile getParentModulePomFile(IFile pomFile) {
        IFile parentPom = null;
        if (pomFile == null || pomFile.getParent() == null || pomFile.getParent().getParent() == null) {
            return null;
        }
        if (pomFile.getParent().getName().equals(TalendMavenConstants.PROJECT_NAME)) {
            // ignore .Java project
            return null;
        }
        IContainer parentPomFolder = pomFile.getParent();
        int nb=10;
        while(parentPomFolder != null && !parentPomFolder.getName().equals(RepositoryConstants.POMS_DIRECTORY)) {
            parentPomFolder = parentPomFolder.getParent();
            nb--;
            if (nb <0) {
                // only to avoid infinite loop in case there is some folder issues (poms folder not found)
                return null;
            }
        }
        if (parentPomFolder != null) {
            try {
                for (IResource file : parentPomFolder.members()) {
                    if (file.getName().equals(TalendMavenConstants.POM_FILE_NAME)) {
                        parentPom = (IFile) file;
                        break;
                    }
                }
            } catch (CoreException e) {
                ExceptionHandler.process(e);
            }
        }
        return parentPom;
    }

    public void refreshAggregatorFolderPom(IFile pomFile) throws Exception {
        boolean isModified = false;
        Model model = MavenPlugin.getMaven().readModel(pomFile.getContents());
        List<String> modules = model.getModules();
        if (modules != null) {
            ListIterator<String> iterator = modules.listIterator();
            while (iterator.hasNext()) {
                String module = iterator.next();
                File modulePomFile = pomFile.getLocation().removeLastSegments(1).append(module).toFile();
                if (!modulePomFile.exists()) {
                    iterator.remove();
                    isModified = true;
                }
            }
            if (isModified) {
                PomUtil.savePom(null, model, pomFile);
            }
        }
    }

    public void createCIPom(IFile pomFile, IProgressMonitor monitor) throws Exception {
        Model model = new Model();
        model.setModelVersion("4.0.0"); //$NON-NLS-1$
        model.setGroupId(TalendMavenConstants.DEFAULT_GROUP_ID);
        model.setArtifactId("sources.generator"); //$NON-NLS-1$
        model.setVersion(PomIdsHelper.getProjectVersion());
        model.setPackaging(TalendMavenConstants.PACKAGING_POM);

        model.setBuild(new Build());
        Plugin plugin = new Plugin();
        plugin.setGroupId(TalendMavenConstants.DEFAULT_GROUP_ID);
        plugin.setArtifactId("ci.builder"); //$NON-NLS-1$
        plugin.setVersion("${project.version}"); //$NON-NLS-1$

        List<PluginExecution> executions = new ArrayList<>();
        PluginExecution pe = new PluginExecution();
        pe.setPhase("generate-sources"); //$NON-NLS-1$
        pe.addGoal("generate"); //$NON-NLS-1$
        executions.add(pe);
        plugin.setExecutions(executions);
        model.getBuild().addPlugin(plugin);

        PomUtil.savePom(null, model, pomFile);
    }

    public IFile getProjectRootPom(Project project) {
        if (project == null) {
            project = ProjectManager.getInstance().getCurrentProject();
        }
        return getProjectPomsFolder().getFile(TalendMavenConstants.POM_FILE_NAME);
    }

    public IFolder getProcessesFolder() {
        return getProjectPomsFolder().getFolder(DIR_JOBS);
    }

    public IFolder getCodesFolder() {
        return getProjectPomsFolder().getFolder(DIR_CODES);
    }

    public IFolder getProcessFolder(ERepositoryObjectType type) {
        return getProcessesFolder().getFolder(type.getFolder());
    }

    public static IPath getJobProjectPath(Property property, String realVersion) {
        // without create/open project
        String projectTechName = ProjectManager.getInstance().getProject(property).getTechnicalLabel();
        Project project = ProjectManager.getInstance().getProjectFromProjectTechLabel(projectTechName);
        String version = realVersion == null ? property.getVersion() : realVersion;
        IPath path = ItemResourceUtil.getItemRelativePath(property);
        IFolder processTypeFolder = new AggregatorPomsHelper(project)
                .getProcessFolder(ERepositoryObjectType.getItemType(property.getItem()));
        path = processTypeFolder.getLocation().append(path);
        path = path.append(AggregatorPomsHelper.getJobProjectFolderName(property.getLabel(), version));
        return path;
    }

    public static String getJobProjectName(Project project, Property property) {
        return project.getTechnicalLabel() + "_" + getJobProjectFolderName(property).toUpperCase(); //$NON-NLS-1$
    }

    public static String getJobProjectFolderName(Property property) {
        return getJobProjectFolderName(property.getLabel(), property.getVersion());
    }

    public static String getJobProjectFolderName(String label, String version) {
        return label.toLowerCase() + "_" + version; //$NON-NLS-1$
    }

    public static String getJobProjectId(String id, String version) {
        return id + "|" + version; //$NON-NLS-1$
    }

    public static String getJobProjectId(Property property) {
        return getJobProjectId(property.getId(), property.getVersion());
    }

    public static void checkJobPomCreation(ITalendProcessJavaProject jobProject) throws CoreException {
        Model model = MavenPlugin.getMavenModelManager().readMavenModel(jobProject.getProjectPom());
        boolean useTempPom = TalendJavaProjectConstants.TEMP_POM_ARTIFACT_ID.equals(model.getArtifactId());
        jobProject.setUseTempPom(useTempPom);
    }

}
