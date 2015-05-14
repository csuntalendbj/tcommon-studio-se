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
package org.talend.core.repository.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.RuntimeExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.repository.IExtendRepositoryNode;
import org.talend.commons.utils.data.container.Container;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.genhtml.IHTMLDocConstants;
import org.talend.core.model.metadata.MetadataManager;
import org.talend.core.model.metadata.builder.connection.BRMSConnection;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.EDIFACTConnection;
import org.talend.core.model.metadata.builder.connection.EbcdicConnection;
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.metadata.builder.connection.FileExcelConnection;
import org.talend.core.model.metadata.builder.connection.GenericSchemaConnection;
import org.talend.core.model.metadata.builder.connection.HL7Connection;
import org.talend.core.model.metadata.builder.connection.LDAPSchemaConnection;
import org.talend.core.model.metadata.builder.connection.LdifFileConnection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.PositionalFileConnection;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.RegexpFileConnection;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.SAPIDocUnit;
import org.talend.core.model.metadata.builder.connection.SalesforceModuleUnit;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.metadata.builder.connection.ValidationRulesConnection;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.FolderType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobDocumentationItem;
import org.talend.core.model.properties.JobletDocumentationItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.model.properties.ValidationRulesConnectionItem;
import org.talend.core.model.repository.DynaEnum;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryContentHandler;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryContentManager;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.repository.RepositoryNodeProviderRegistryReader;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.model.repository.SVNConstant;
import org.talend.core.repository.i18n.Messages;
import org.talend.core.repository.model.repositoryObject.MetadataColumnRepositoryObject;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.repository.model.repositoryObject.QueryRepositoryObject;
import org.talend.core.repository.model.repositoryObject.SAPFunctionRepositoryObject;
import org.talend.core.repository.model.repositoryObject.SAPIDocRepositoryObject;
import org.talend.core.repository.model.repositoryObject.SalesforceModuleRepositoryObject;
import org.talend.core.ui.ICDCProviderService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.SubItemHelper;
import org.talend.cwm.helper.TableHelper;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.BinRepositoryNode;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.StableRepositoryNode;
import org.talend.repository.model.nodes.IProjectRepositoryNode;
import org.talend.utils.sql.ConnectionUtils;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class ProjectRepositoryNode extends RepositoryNode implements IProjectRepositoryNode {

    private RepositoryNode svnRootNode, recBinNode, sqlPatternNode, docNode, metadataNode, metadataConNode, metadataFileNode,
            metadataSAPConnectionNode, metadataEbcdicConnectionNode, metadataHL7ConnectionNode, metadataRulesNode,
            metadataBRMSConnectionNode, metadataValidationRulesNode, metadataEDIFactConnectionNode;

    private RepositoryNode refProject;

    public static boolean refProjectBool = false;

    public static boolean refreshBool = false;

    private static ProjectRepositoryNode defaultProjRepoNode;

    private final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    private org.talend.core.model.general.Project project;

    private Map<Object, List<Project>> nodeAndProject;

    private Map<String, RepositoryNode> repositoryNodeMap = new HashMap<String, RepositoryNode>();

    private String currentPerspective; // set the current perspective

    private List<FolderItem> delFolderItems = new ArrayList<FolderItem>();

    /**
     * DOC nrousseau ProjectRepositoryNode constructor comment.
     * 
     * @param object
     * @param parent
     * @param type
     */
    public ProjectRepositoryNode(org.talend.core.model.general.Project project, IRepositoryViewObject object,
            RepositoryNode parent, RepositoryNode root, ENodeType type) {
        super(object, parent, type);
        // for referenced project
        this.project = project;
        setRoot(this);
    }

    public ProjectRepositoryNode(ProjectRepositoryNode projectNode) {
        this(projectNode.getProject(), projectNode.getObject(), projectNode.getParent(), (RepositoryNode) projectNode.getRoot(),
                projectNode.getType());

        this.setProperties(EProperties.LABEL, projectNode.getProperties(EProperties.LABEL));
        this.setProperties(EProperties.CONTENT_TYPE, projectNode.getProperties(EProperties.CONTENT_TYPE));
    }

    // base project
    public ProjectRepositoryNode(IRepositoryObject object, RepositoryNode parent, ENodeType type) {
        this(ProjectManager.getInstance().getCurrentProject(), object, parent, null, type);
    }

    public static ProjectRepositoryNode getInstance() {
        if (defaultProjRepoNode == null) {
            defaultProjRepoNode = new ProjectRepositoryNode(null, null, ENodeType.STABLE_SYSTEM_FOLDER);
            defaultProjRepoNode.initialize(null);
        }
        return defaultProjRepoNode;
    }

    private String showSVNRoot() {
        String branch = ProjectManager.getInstance().getMainProjectBranch(project);
        if ("".equals(branch) || branch == null) { //$NON-NLS-1$
            return null;
        }
        if (!branch.contains(SVNConstant.NAME_TRUNK) && !branch.contains(SVNConstant.NAME_BRANCHES)
                && !branch.contains(SVNConstant.NAME_TAGS)) {
            return null;
        }
        return branch;
    }

    public void initialize(String currentPerspective) {
        this.currentPerspective = currentPerspective;
        nodeAndProject = new HashMap<Object, List<Project>>();
        IRepositoryNode curParentNode = null;

        String urlBranch = null;
        if (ProjectManager.getInstance().getCurrentBranchURL(project) != null) {
            urlBranch = showSVNRoot();
        }
        if ("".equals(urlBranch) || urlBranch == null) { //$NON-NLS-1$
            curParentNode = this;
        } else {
            List<IRepositoryNode> root = getChildren();

            svnRootNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            svnRootNode.setProperties(EProperties.LABEL, ERepositoryObjectType.SVN_ROOT + "(" + urlBranch + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            svnRootNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.SVN_ROOT);
            if (root.contains(svnRootNode)) {
                return;
            }
            root.add(svnRootNode);

            curParentNode = svnRootNode;
        }

        List<IRepositoryNode> nodes = curParentNode.getChildren();
        // 0. Recycle bin
        recBinNode = new BinRepositoryNode(this);
        nodes.add(recBinNode);

        // 7. Metadata
        metadataNode = new RepositoryNode(null, this, ENodeType.STABLE_SYSTEM_FOLDER);
        metadataNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA);
        metadataNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA);
        nodes.add(metadataNode);

        // 7.17 Rules and BRMS
        if (PluginChecker.isSurvivorshipPluginLoaded() || PluginChecker.isRulesPluginLoaded()
                || PluginChecker.isBRMSPluginLoaded()) {
            StableRepositoryNode baseRulesNode = new StableRepositoryNode(this,
                    Messages.getString("ProjectRepositoryNode.rulesManagement"), //$NON-NLS-1$
                    ECoreImage.METADATA_RULES_ICON);
            baseRulesNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_RULES_MANAGEMENT);
            metadataNode.getChildren().add(baseRulesNode);

            if (PluginChecker.isBRMSPluginLoaded()) {
                metadataBRMSConnectionNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
                metadataBRMSConnectionNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_BRMS);
                metadataBRMSConnectionNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_BRMS);
                baseRulesNode.getChildren().add(metadataBRMSConnectionNode);
            }
            // 7.14 RULES
            if (PluginChecker.isRulesPluginLoaded()) {
                metadataRulesNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
                metadataRulesNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_RULES);
                metadataRulesNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_RULES);
                baseRulesNode.getChildren().add(metadataRulesNode);
            }
        }

        // Reference Projects
        if (PluginChecker.isRefProjectLoaded() && getParent() != this && project != null
                && project.getEmfProject().getReferencedProjects().size() > 0) {
            refProject = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            refProject.setProperties(EProperties.LABEL, ERepositoryObjectType.REFERENCED_PROJECTS);
            refProject.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.REFERENCED_PROJECTS);
            nodes.add(refProject);
        }
        // *init the repository node from extension
        initExtensionRepositoryNodes(curParentNode);
        // delete the hidden nodes
        deleteHiddenNodes(nodes);

        try {
            hideHiddenNodesDependsUserRight();
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }

        collectRepositoryNodes(curParentNode);
    }

    private void deleteHiddenNodes(List<IRepositoryNode> nodes) {

        if (GlobalServiceRegister.getDefault().isServiceRegistered(IBrandingService.class)) {
            IBrandingService service = (IBrandingService) GlobalServiceRegister.getDefault().getService(IBrandingService.class);
            List<IRepositoryNode> hiddens = service.getBrandingConfiguration().getHiddenRepositoryCategory(this, "DI");
            for (IRepositoryNode node : hiddens) {
                Iterator<IRepositoryNode> it = nodes.iterator();
                while (it.hasNext()) {
                    IRepositoryNode curNode = it.next();
                    if (curNode.getContentType() != null && curNode.getContentType().equals(ERepositoryObjectType.METADATA)) {
                        for (IRepositoryNode curNode2 : curNode.getChildren()) {
                            if (curNode2.equals(node)) {
                                curNode.getChildren().remove(curNode2);
                                break;
                            }
                        }
                    }
                    if (curNode.equals(node)) {
                        it.remove();
                    }
                }
            }
        }
    }

    private void collectRepositoryNodes(IRepositoryNode curParentNode) {
        if (repositoryNodeMap == null) {
            repositoryNodeMap = new HashMap<String, RepositoryNode>();
        }
        repositoryNodeMap.clear();
        collectRepositoryNodes(curParentNode.getChildren());
    }

    private void collectRepositoryNodes(List<IRepositoryNode> nodes) {

        if (nodes != null) {
            for (IRepositoryNode node : nodes) {
                if (node.getParent() instanceof ProjectRepositoryNode) { // root node of type
                    ERepositoryObjectType roType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
                    if (roType != null) { // bin is null
                        String typeName = roType.name();
                        if (repositoryNodeMap.containsKey(typeName)) {
                            // later, will do something.
                        } else {
                            repositoryNodeMap.put(typeName, (RepositoryNode) node);
                        }
                    }
                }
                collectRepositoryNodes(node.getChildren());
            }
        }

    }

    private String[] getUserAuthorization() throws JSONException {
        User currentUser = ProxyRepositoryFactory.getInstance().getRepositoryContext().getUser();
        String addData = currentUser.getAdditionnalData();
        if (addData == null) {
            String[] userRights = {};
            return userRights;
        }
        JSONObject o = new JSONObject(addData);
        List<String> rightsAsList = new ArrayList<String>();
        Iterator<String> it = o.sortedKeys();

        while (it.hasNext()) {
            String key = it.next();
            if (o.getBoolean(key)) {
                rightsAsList.add(key);
            }
        }
        String[] rights = rightsAsList.toArray(new String[] {});
        return rights;
    }

    private void hideHiddenNodesDependsUserRight() throws JSONException {
        String[] userRights = getUserAuthorization();
        List<IRepositoryNode> nodes = new ArrayList<IRepositoryNode>(this.getChildren());
        for (IRepositoryNode node : nodes) {
            ERepositoryObjectType contentType = node.getContentType();
            if (contentType == null) {
                continue;
            }
            String[] contentRight = contentType.getUserRight();
            if (contentRight != null && contentRight.length > 0 && userRights != null && userRights.length > 0) {
                for (int i = 0; i < contentRight.length; i++) {
                    if (!ArrayUtils.contains(userRights, contentRight[i])) {
                        removeNode(this, node);
                    }
                }
            }

        }
    }

    private void removeNode(IRepositoryNode container, IRepositoryNode node) {
        List<IRepositoryNode> nodes = container.getChildren();

        if (nodes.contains(node)) {
            nodes.remove(node);
        } else {
            for (IRepositoryNode n : nodes) {
                removeNode(n, node);
            }
        }
    }

    private void initExtensionRepositoryNodes(final IRepositoryNode curParentNode) {
        Map<ERepositoryObjectType, RepositoryNode> repositoryNodeExtensionMap = new HashMap<ERepositoryObjectType, RepositoryNode>();

        RepositoryNodeProviderRegistryReader repoReader = RepositoryNodeProviderRegistryReader.getInstance();
        ERepositoryObjectType[] allDynamicTypes = repoReader.getAllDynamicTypes();

        for (ERepositoryObjectType dynamicType : allDynamicTypes) {
            ENodeType nodeType = repoReader.getNodeType(dynamicType);
            RepositoryNode dynamicNode = new RepositoryNode(null, this, nodeType);

            dynamicNode.setProperties(EProperties.LABEL, dynamicType);
            dynamicNode.setProperties(EProperties.CONTENT_TYPE, dynamicType);

            IExtendRepositoryNode extendNode = repoReader.getExtendNodeByType(dynamicType);
            if (extendNode != null) {
                dynamicNode.setIcon(extendNode.getNodeImage());

                RepositoryNode[] children = (RepositoryNode[]) extendNode.getChildren();
                if (children != null && (children.length > 0)) {
                    for (RepositoryNode nodeToAdd : children) {
                        dynamicNode.getChildren().add(nodeToAdd);
                        nodeToAdd.setParent(dynamicNode);
                        nodeToAdd.setRoot(this);
                    }
                }
            }
            repositoryNodeExtensionMap.put(dynamicType, dynamicNode);
        }

        // init the existed map for extension
        collectRepositoryNodes(curParentNode);
        //
        for (ERepositoryObjectType dynamicType : repositoryNodeExtensionMap.keySet()) {
            RepositoryNode dynamicNode = repositoryNodeExtensionMap.get(dynamicType);
            ERepositoryObjectType[] parentTypes = dynamicType.getParentTypesArray();
            if (parentTypes != null && parentTypes.length > 0) {
                for (ERepositoryObjectType parentType : parentTypes) {
                    RepositoryNode parentNode = null;
                    if (repositoryNodeExtensionMap.containsKey(parentType)) {
                        // the parent is dynamic node too.
                        parentNode = repositoryNodeExtensionMap.get(parentType);
                    } else {
                        parentNode = getRootRepositoryNode(parentType);
                    }
                    if (parentNode != null) {
                        parentNode.getChildren().add(dynamicNode);
                    }
                }
            } else { // add in root of project
                curParentNode.getChildren().add(dynamicNode);
            }
        }
    }

    /**
     * DOC nrousseau Comment method "initializeChildren".
     * 
     * @param parent
     */
    public void initializeChildren(Object parent) {
        initializeChildren(project, parent);
        if (PluginChecker.isRefProjectLoaded() && getMergeRefProject()) {
            intitializeRefProject(project.getEmfProject(), parent);
        }
    }

    private void intitializeRefProject(Project project, Object parent) {
        if (parent instanceof IRepositoryNode && ((IRepositoryNode) parent).isBin()) {
            return; // don't check the deleted item for ref-projects
        }
        for (ProjectReference refProject : (List<ProjectReference>) project.getReferencedProjects()) {
            String parentBranch = ProjectManager.getInstance().getMainProjectBranch(project);
            if (refProject.getBranch() == null || parentBranch.equals(refProject.getBranch())) {
                Project p = refProject.getReferencedProject();
                List<Project> list = nodeAndProject.get(parent);
                if (list == null) {
                    list = new ArrayList<Project>();
                    nodeAndProject.put(parent, list);
                }
                if (list.contains(p)) {
                    return;
                } else {
                    list.add(p);
                }
                initializeChildren(new org.talend.core.model.general.Project(p), parent);
                intitializeRefProject(p, parent);
            }
        }

    }

    public void clearNodeAndProjectCash() {
        nodeAndProject.clear();
    }

    public void initializeChildren(org.talend.core.model.general.Project newProject, Object parent) {
        try {
            if (parent == docNode) {
                convert(newProject, factory.getMetadata(newProject, ERepositoryObjectType.DOCUMENTATION, true), docNode,
                        ERepositoryObjectType.DOCUMENTATION, recBinNode);
            } else if (parent == metadataConNode) {
                convert(newProject, factory.getMetadata(newProject, ERepositoryObjectType.METADATA_CONNECTIONS, true),
                        metadataConNode, ERepositoryObjectType.METADATA_CONNECTIONS, recBinNode);
            } else if (parent == metadataSAPConnectionNode) {
                convert(newProject, factory.getMetadata(newProject, ERepositoryObjectType.METADATA_SAPCONNECTIONS, true),
                        metadataSAPConnectionNode, ERepositoryObjectType.METADATA_SAPCONNECTIONS, recBinNode);
            } else if (parent == metadataEbcdicConnectionNode) {
                convert(newProject, factory.getMetadata(newProject, ERepositoryObjectType.METADATA_FILE_EBCDIC, true),
                        metadataEbcdicConnectionNode, ERepositoryObjectType.METADATA_FILE_EBCDIC, recBinNode);
            } else if (parent == metadataHL7ConnectionNode) {
                convert(newProject, factory.getMetadata(newProject, ERepositoryObjectType.METADATA_FILE_HL7, true),
                        metadataHL7ConnectionNode, ERepositoryObjectType.METADATA_FILE_HL7, recBinNode);
            } else if (parent == metadataBRMSConnectionNode) {
                convert(newProject, factory.getMetadata(newProject, ERepositoryObjectType.METADATA_FILE_BRMS, true),
                        metadataBRMSConnectionNode, ERepositoryObjectType.METADATA_FILE_BRMS, recBinNode);
            } else if (parent == sqlPatternNode) {
                convert(newProject, factory.getMetadata(newProject, ERepositoryObjectType.SQLPATTERNS, true), sqlPatternNode,
                        ERepositoryObjectType.SQLPATTERNS, recBinNode);
            } else if (parent == metadataFileNode) {
                convert(newProject, factory.getMetadata(newProject, ERepositoryObjectType.METADATA_FILE_DELIMITED, true),
                        metadataFileNode, ERepositoryObjectType.METADATA_FILE_DELIMITED, recBinNode);
            } else if (parent == metadataRulesNode) { // feature 6484 added
                convert(newProject, factory.getMetadata(newProject, ERepositoryObjectType.METADATA_FILE_RULES, true),
                        metadataRulesNode, ERepositoryObjectType.METADATA_FILE_RULES, recBinNode);
            } else if (parent == metadataValidationRulesNode) {
                convert(newProject, factory.getMetadata(newProject, ERepositoryObjectType.METADATA_VALIDATION_RULES, true),
                        metadataValidationRulesNode, ERepositoryObjectType.METADATA_VALIDATION_RULES, recBinNode);
            } else if (parent == metadataEDIFactConnectionNode) {
                convert(newProject, factory.getMetadata(newProject, ERepositoryObjectType.METADATA_EDIFACT, true),
                        metadataEDIFactConnectionNode, ERepositoryObjectType.METADATA_EDIFACT, recBinNode);
            } else if (parent instanceof StableRepositoryNode
                    && ((StableRepositoryNode) parent).getContentType() == ERepositoryObjectType.CODE) {
                // do nothing
            } else if (parent == refProject) {
                if (!getMergeRefProject()) {
                    handleReferenced(refProject);
                }
            } else if (parent == recBinNode) {
                List<RepositoryNode> foldersList = new ArrayList<RepositoryNode>();
                if (newProject != null && newProject.getEmfProject() != null) {
                    List<FolderItem> folderItems = ProjectManager.getInstance().getFolders(newProject.getEmfProject());
                    for (FolderItem folder : new ArrayList<FolderItem>(folderItems)) {
                        addItemToRecycleBin(newProject, (RepositoryNode) parent, folder, foldersList);
                    }
                }
            } else if (parent instanceof RepositoryNode) {
                RepositoryNode repositoryNode = (RepositoryNode) parent;
                ERepositoryObjectType contentType = repositoryNode.getContentType();
                if (contentType != null && contentType.isResourceItem()) {
                    convert(newProject, factory.getMetadata(newProject, contentType, true), repositoryNode, contentType,
                            recBinNode);
                }
            }
        } catch (PersistenceException e) {
            RuntimeExceptionHandler.process(e);
        }
    }

    private ERepositoryObjectType getFolderContentType(FolderItem folderItem) {
        if (!folderItem.getType().equals(FolderType.SYSTEM_FOLDER_LITERAL)) {
            if (!(folderItem.getParent() instanceof FolderItem)) {
                return null; // appears only for a folder for expression builder !
            }
            return getFolderContentType((FolderItem) folderItem.getParent());
        }
        for (ERepositoryObjectType objectType : (ERepositoryObjectType[]) ERepositoryObjectType.values()) {
            String folderName;
            try {
                folderName = ERepositoryObjectType.getFolderName(objectType);
            } catch (Exception e) {
                // just catch exception to avoid the types who don't have folders
                continue;
            }
            if (folderName.contains("/")) { //$NON-NLS-1$
                String[] folders = folderName.split("/"); //$NON-NLS-1$
                FolderItem currentFolderItem = folderItem;
                boolean found = true;
                for (int i = folders.length - 1; i >= 0; i--) {
                    if (!currentFolderItem.getProperty().getLabel().equals(folders[i])) {
                        found = false;
                        break;
                    }
                    if (i > 0) {
                        if (!(currentFolderItem.getParent() instanceof FolderItem)) {
                            found = false;
                            break;
                        }
                        currentFolderItem = (FolderItem) currentFolderItem.getParent();
                    }
                }
                if (found) {
                    return objectType;
                }
            } else {
                if (folderName.equals(folderItem.getProperty().getLabel())) {
                    return objectType;
                }
            }
        }
        if (folderItem.getParent() instanceof FolderItem) {
            return getFolderContentType((FolderItem) folderItem.getParent());
        }
        return null;
    }

    private void addItemToRecycleBin(org.talend.core.model.general.Project newProject, RepositoryNode parentNode, Item item,
            List<RepositoryNode> foldersList) {
        if (isGeneratedJobItem(item)) {
            return;
        }
        ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
        RepositoryNode currentParentNode = parentNode;
        if (item instanceof FolderItem) {
            itemType = getFolderContentType((FolderItem) item);
            if (itemType == null) {
                return;
            }

            // MOD qiongli 2011-1-21 filter TDQ root folder.
            if (itemType != null && itemType.isDQItemType() && !itemType.isSharedType()) {
                return;
            }// ~
            if (item.getState().isDeleted()) {
                // need to display this folder in the recycle bin.
                Folder folder = new Folder(item.getProperty(), itemType);
                RepositoryNode folderNode = null;
                for (RepositoryNode existingFolder : foldersList) {
                    if (existingFolder.getContentType() == null) {
                        // this can appear temporary when another user has deleted a folder
                        break;
                    }
                    if (existingFolder.getContentType().equals(folder.getRepositoryObjectType())
                            && existingFolder.getProperties(EProperties.LABEL).equals(folder.getLabel())) {
                        folderNode = existingFolder;
                        break;
                    }
                }

                if (folderNode == null) {
                    folderNode = new RepositoryNode(folder, parentNode, ENodeType.SIMPLE_FOLDER);
                    folderNode.setProperties(EProperties.CONTENT_TYPE, itemType);
                    folderNode.setProperties(EProperties.LABEL, folder.getLabel());
                    foldersList.add(folderNode);
                    parentNode.getChildren().add(folderNode);
                    folderNode.setParent(parentNode);
                }

                for (Item curItem : (List<Item>) new ArrayList(((FolderItem) item).getChildren())) {
                    addItemToRecycleBin(newProject, folderNode, curItem, foldersList);
                }
                currentParentNode = folderNode;
            } else {
                for (Item curItem : (List<Item>) new ArrayList(((FolderItem) item).getChildren())) {
                    addItemToRecycleBin(newProject, parentNode, curItem, foldersList);
                }
            }
        } else if (item.getState() != null && item.getState().isDeleted()) {
            try {
                IRepositoryViewObject lastVersion = ProxyRepositoryFactory.getInstance().getLastVersion(newProject,
                        item.getProperty().getId());
                if (lastVersion != null && item.getProperty().getVersion().equals(lastVersion.getVersion())) {
                    RepositoryNode repNode = new RepositoryNode(new RepositoryViewObject(item.getProperty()), currentParentNode,
                            ENodeType.REPOSITORY_ELEMENT);
                    repNode.setProperties(EProperties.CONTENT_TYPE, itemType);
                    repNode.setProperties(EProperties.LABEL, item.getProperty().getLabel());
                    currentParentNode.getChildren().add(repNode);
                    repNode.setParent(currentParentNode);
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }

        } else {// bug 17768
            if (item instanceof ConnectionItem) {
                Connection connection = ((ConnectionItem) item).getConnection();

                // for sap
                if (connection instanceof SAPConnection) {
                    SAPConnection sapConnection = (SAPConnection) connection;
                    EList<SAPFunctionUnit> funtions = sapConnection.getFuntions();
                    if (funtions != null) {
                        for (int i = 0; i < funtions.size(); i++) {
                            SAPFunctionUnit unit = funtions.get(i);
                            if (SubItemHelper.isDeleted(unit)) {
                                RepositoryNode tableNode = createSAPNode(new RepositoryViewObject(item.getProperty()),
                                        currentParentNode, unit);
                                currentParentNode.getChildren().add(tableNode);
                                tableNode.setParent(currentParentNode);
                            } else {
                                for (MetadataTable table : ConnectionHelper.getTables(connection, unit)) {
                                    if (SubItemHelper.isDeleted(table)) {
                                        RepositoryNode tableNode = createMetatableNode(currentParentNode,
                                                new RepositoryViewObject(item.getProperty()), table,
                                                ERepositoryObjectType.METADATA_CON_TABLE);
                                        currentParentNode.getChildren().add(tableNode);
                                        tableNode.setParent(currentParentNode);
                                    }
                                }
                            }
                        }
                    }
                    EList<SAPIDocUnit> iDocs = sapConnection.getIDocs();
                    if (iDocs != null) {
                        for (int i = 0; i < iDocs.size(); i++) {
                            SAPIDocUnit unit = iDocs.get(i);
                            if (SubItemHelper.isDeleted(unit)) {
                                RepositoryNode tableNode = createSAPNode(new RepositoryViewObject(item.getProperty()),
                                        currentParentNode, unit);
                                currentParentNode.getChildren().add(tableNode);
                                tableNode.setParent(currentParentNode);
                            }
                        }
                    }
                }

                for (MetadataTable table : ConnectionHelper.getTables(connection)) {
                    if (SubItemHelper.isDeleted(table)) {
                        RepositoryNode tableNode = createMetatableNode(currentParentNode,
                                new RepositoryViewObject(item.getProperty()), table, ERepositoryObjectType.METADATA_CON_TABLE);
                        currentParentNode.getChildren().add(tableNode);
                        tableNode.setParent(currentParentNode);
                    }
                }

                if (connection != null) {
                    QueriesConnection queriesConnection = connection.getQueries();
                    if (queriesConnection != null) {
                        for (Query query : queriesConnection.getQuery()) {
                            if (SubItemHelper.isDeleted(query)) {
                                RepositoryNode queryNode = createQueryNode(currentParentNode,
                                        new RepositoryViewObject(item.getProperty()), query);
                                currentParentNode.getChildren().add(queryNode);
                                queryNode.setParent(currentParentNode);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 
     * ggu Comment method "isGeneratedJobItem".
     * 
     * feature 4393
     */
    private boolean isGeneratedJobItem(Item item) {
        if (item != null) {
            if (item instanceof JobDocumentationItem || item instanceof JobletDocumentationItem) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private void convertDocumentation(org.talend.core.model.general.Project newProject, Container generatedContainer,
            RepositoryNode parent, ERepositoryObjectType type, RepositoryNode recBinNode) {
        // for folder Documentation/generated
        // RepositoryNode generatedFolder = getRootRepositoryNode(ERepositoryObjectType.GENERATED);

        // for folder Documentation/generated/jobs
        convertDocumentation(newProject, generatedContainer, parent, type, recBinNode, ERepositoryObjectType.JOBS,
                ERepositoryObjectType.JOB_DOC);

        // for folder Documentation/generated/joblets
        convertDocumentation(newProject, generatedContainer, parent, type, recBinNode, ERepositoryObjectType.JOBLETS,
                ERepositoryObjectType.JOBLET_DOC);

        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
            ICamelDesignerCoreService service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                    ICamelDesignerCoreService.class);
            if (service.getRouteDocsType() != null && service.getRouteDocType() != null) {
                convertDocumentation(newProject, generatedContainer, parent, type, recBinNode, service.getRouteDocsType(),
                        service.getRouteDocType());
            }
        }

    }

    @SuppressWarnings("rawtypes")
    private void convertDocumentation(org.talend.core.model.general.Project newProject, Container generatedContainer,
            RepositoryNode parent, ERepositoryObjectType type, RepositoryNode recBinNode, ERepositoryObjectType parentDocType,
            ERepositoryObjectType docType) {
        RepositoryNode docsFolder = getRootRepositoryNode(parentDocType);

        Container docsNode = null;
        for (Object object : generatedContainer.getSubContainer()) {
            if (((Container) object).getLabel().equalsIgnoreCase(parentDocType.name().toLowerCase())) {
                docsNode = (Container) object;
                break;
            }
        }

        // get the files under generated/nodes.
        if (docsNode != null) {
            convert(newProject, docsNode, docsFolder, docType, recBinNode);
        }

    }

    private RepositoryNode getSQLPatternNode(String parentLabel, String label) {
        if (getMergeRefProject()) {
            List<IRepositoryNode> sqlChildren = getRootRepositoryNode(ERepositoryObjectType.SQLPATTERNS).getChildren();
            // List<RepositoryNode> sqlChildren = parent.getChildren();

            for (IRepositoryNode sqlChild : sqlChildren) {
                if (label.equalsIgnoreCase(sqlChild.toString())) {
                    return (RepositoryNode) sqlChild;
                }
                for (IRepositoryNode userDefined : sqlChild.getChildren()) {
                    if (label.equalsIgnoreCase(((RepositoryNode) userDefined).getProperties(EProperties.LABEL).toString())) {
                        if (sqlChild.toString().equalsIgnoreCase(parentLabel)) {
                            return (RepositoryNode) userDefined;
                        }
                    }

                }
            }
        }
        return null;
    }

    private List<FolderItem> getDelFolderItems(List list, List<FolderItem> delFolderItems) {
        for (Object obj : list) {
            if (obj instanceof FolderItem) {
                FolderItem folderItem = (FolderItem) obj;
                if (folderItem.getState().isDeleted()) {
                    delFolderItems.add(folderItem);
                } else if (folderItem.getChildren() != null && !folderItem.getChildren().isEmpty()) {
                    getDelFolderItems(folderItem.getChildren(), delFolderItems);
                }
            }
        }
        return delFolderItems;
    }

    private void handleDelFolderItems(org.talend.core.model.general.Project newProject, RepositoryNode parent) {
        if (!delFolderItems.isEmpty()) {
            delFolderItems.clear();
        }
        if (newProject != null && newProject.getEmfProject() != null) {
            List<FolderItem> folderItems = ProjectManager.getInstance().getFolders(newProject.getEmfProject());
            for (FolderItem folder : folderItems) {
                String folderName = folder.getProperty().getLabel();
                if (("process".equals(folderName) || "joblets".equals(folderName)) && folder.getChildren() != null
                        && !folder.getChildren().isEmpty()) {
                    getDelFolderItems(folder.getChildren(), delFolderItems);
                }
            }
        }
    }

    private void convert(org.talend.core.model.general.Project newProject, Container fromModel, RepositoryNode parent,
            ERepositoryObjectType type, RepositoryNode recBinNode) {

        if (parent == null || fromModel == null) {
            return;
        }

        if (type.equals(ERepositoryObjectType.DOCUMENTATION)) {
            handleDelFolderItems(newProject, parent);
        }

        for (Object obj : fromModel.getSubContainer()) {
            Container container = (Container) obj;
            Folder oFolder = new Folder((Property) container.getProperty(), type);
            boolean found = false;
            // add for bug TDI-26084, whether or not hide folders under job_doc/joblet_doc.
            if (ERepositoryObjectType.JOB_DOC.equals(type) || ERepositoryObjectType.JOBLET_DOC.equals(type)) {
                for (FolderItem delFolder : delFolderItems) {
                    String parentName = ((FolderItem) delFolder.getParent()).getProperty().getLabel();
                    String oFolderPath = oFolder.getPath();
                    String jobPath = "jobs" + delFolder.getState().getPath();
                    String jobPath1 = "jobs/" + delFolder.getState().getPath();
                    String jobletPath = "joblets" + delFolder.getState().getPath();
                    String jobletPath1 = "joblets/" + delFolder.getState().getPath();
                    if (oFolder.getLabel().equals(delFolder.getProperty().getLabel())) {
                        if ("process".equals(parentName) && oFolderPath.equals(jobPath)) {
                            found = true;
                        } else if ("joblets".equals(parentName) && oFolderPath.equals(jobletPath)) {
                            found = true;
                        } else if (oFolderPath.equals(jobPath1) || oFolderPath.equals(jobletPath1)) {
                            found = true;
                        }
                        break;
                    }
                }
            }
            if (found) {
                continue;
            }
            if (oFolder.getProperty() == null) {
                continue;
            }

            RepositoryNode folder = null;

            String label = container.getLabel();
            if (label.equals("bin") || label.startsWith(".")) { //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            }
            // currently, only process the docs for job, joblet and route.
            if (type.equals(ERepositoryObjectType.JOB_DOC) || type.equals(ERepositoryObjectType.JOBLET_DOC)
                    || type.equals(ERepositoryObjectType.JOBS) || type.equals(ERepositoryObjectType.JOBLETS)
                    || type.equals(ERepositoryObjectType.valueOf("ROUTE_DOCS"))//$NON-NLS-1$ 
                    || type.equals(ERepositoryObjectType.valueOf("ROUTE_DOC"))) {//$NON-NLS-1$ 
                boolean isJobDocRootFolder = ((label.indexOf("_") != -1) && (label.indexOf(".") != -1)); //$NON-NLS-1$ //$NON-NLS-2$
                boolean isPicFolderName = label.equals(IHTMLDocConstants.PIC_FOLDER_NAME);

                // Do not show job documentation root folder and Foder "pictures" on the repository view.
                if (isJobDocRootFolder || isPicFolderName) {
                    continue;
                }
            }
            // for system folder
            if (RepositoryConstants.SYSTEM_DIRECTORY.equals(label)) {
                if (getMergeRefProject()) {
                    parent.getChildren();
                    boolean existSystemFolder = false;
                    for (IRepositoryNode node : parent.getChildren()) {
                        if (RepositoryConstants.SYSTEM_DIRECTORY.equalsIgnoreCase(node.getLabel())) {
                            existSystemFolder = true;
                            break;
                        }
                    }
                    IBrandingService breaningService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                            IBrandingService.class);

                    if (!existSystemFolder && !breaningService.isPoweredOnlyCamel()) {
                        folder = new StableRepositoryNode(parent, RepositoryConstants.SYSTEM_DIRECTORY,
                                ECoreImage.FOLDER_CLOSE_ICON);
                        parent.getChildren().add(folder);
                    } else {
                        continue;
                    }
                } else {

                    folder = new StableRepositoryNode(parent, RepositoryConstants.SYSTEM_DIRECTORY, ECoreImage.FOLDER_CLOSE_ICON);
                    parent.getChildren().add(folder);
                }

            } else if (ERepositoryObjectType.GENERATED.name().equalsIgnoreCase(label)) {
                if (PluginChecker.isDocumentationPluginLoaded()) {
                    convertDocumentation(newProject, container, parent, type, recBinNode);
                }
                continue;
            } else {
                if (getMergeRefProject()) {
                    String a = parent.getProperties(EProperties.LABEL).toString();
                    if (type == ERepositoryObjectType.SQLPATTERNS) {
                        folder = getSQLPatternNode(a, label);
                    }
                    if (folder == null) {
                        if (newProject != this.project && !hasTalendItems(container)) {
                            continue;
                        }
                        folder = new RepositoryNode(oFolder, parent, ENodeType.SIMPLE_FOLDER);
                        if (factory.getStatus(oFolder) != ERepositoryStatus.DELETED) {
                            parent.getChildren().add(folder);
                        }
                    }
                } else {
                    folder = new RepositoryNode(oFolder, parent, ENodeType.SIMPLE_FOLDER);
                    if (factory.getStatus(oFolder) != ERepositoryStatus.DELETED) {
                        parent.getChildren().add(folder);
                    }
                }

            }
            folder.setProperties(EProperties.LABEL, label);
            folder.setProperties(EProperties.CONTENT_TYPE, type);
            convert(newProject, container, folder, type, recBinNode);
        }
        List<IRepositoryViewObject> validationRules = null;

        try {
            // note: could be optimized and get this only for metadata related repository types.
            // but the loss of performance is really minimized still here.
            validationRules = factory.getAll(ERepositoryObjectType.METADATA_VALIDATION_RULES);
        } catch (PersistenceException e2) {
            ExceptionHandler.process(e2);
        }

        // not folder or folders have no subFolder
        for (Object obj : fromModel.getMembers()) {
            IRepositoryViewObject repositoryObject = (IRepositoryViewObject) obj;
            try {
                if (!repositoryObject.isDeleted()) {
                    addNode(parent, type, recBinNode, repositoryObject, validationRules);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
                ExceptionHandler.log(Messages.getString(
                        "ProjectRepositoryNode.itemInvalid", repositoryObject.getRepositoryObjectType(), //$NON-NLS-1$,
                        repositoryObject.getLabel()));
            }
        }
    }

    private boolean hasTalendItems(Container container) {
        if (!container.getMembers().isEmpty()) {
            return true;
        }
        for (Object obj : container.getSubContainer()) {
            Container subContainer = (Container) obj;
            boolean hasTalendItems = hasTalendItems(subContainer);
            if (!hasTalendItems) {
                continue;
            }
            return hasTalendItems;
        }

        return false;

    }

    private void handleReferenced(RepositoryNode parent) {
        if (parent.getType().equals(ENodeType.SYSTEM_FOLDER)) {
            for (ProjectReference refProject : (List<ProjectReference>) project.getEmfProject().getReferencedProjects()) {
                String parentBranch = ProjectManager.getInstance().getMainProjectBranch(project);
                // if not a DB ref project, modified by nma, order 12519
                if (refProject.getReferencedProject().getUrl() != null
                        && refProject.getReferencedProject().getUrl().startsWith("teneo") //$NON-NLS-1$
                        || (refProject.getBranch() != null && refProject.getBranch().equals(parentBranch))) {
                    Project emfProject = refProject.getReferencedProject();
                    ProjectRepositoryNode referencedProjectNode = new ProjectRepositoryNode(
                            new org.talend.core.model.general.Project(emfProject), null, parent, this,
                            ENodeType.REFERENCED_PROJECT);
                    referencedProjectNode.setProperties(EProperties.LABEL, emfProject.getLabel());
                    referencedProjectNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.REFERENCED_PROJECTS);
                    parent.getChildren().add(referencedProjectNode);
                    // fix the bug for Ref-project
                    // TDI-23358, revert to before
                    // ProjectManager.getInstance().updateViewProjectNode(referencedProjectNode);

                    referencedProjectNode.initialize(currentPerspective);
                }
            }
        }

    }

    private void addNode(RepositoryNode parent, ERepositoryObjectType type, RepositoryNode recBinNode,
            IRepositoryViewObject repositoryObject, List<IRepositoryViewObject> validationRules) {

        boolean isAvaliableInTOS = true; // this flag filter the databaseconnections which didn't supported by TOS but
        DatabaseConnection dbMetadataConnection = null;
        // TOP
        if (type == ERepositoryObjectType.METADATA_CONNECTIONS) {
            dbMetadataConnection = (DatabaseConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            isAvaliableInTOS = EDatabaseTypeName.getTypeFromDbType(dbMetadataConnection.getDatabaseType(), false) == null ? false
                    : true;

        }

        RepositoryNode node = new RepositoryNode(repositoryObject, parent, ENodeType.REPOSITORY_ELEMENT);

        node.setProperties(EProperties.CONTENT_TYPE, type);
        node.setProperties(EProperties.LABEL, repositoryObject.getLabel());
        if ((repositoryObject.isDeleted() && parent.getObject() == null)
                || (repositoryObject.isDeleted() && !parent.getObject().isDeleted())) {
            // recBinNode.getChildren().add(node);
            // node.setParent(recBinNode);
        } else {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerCoreService.class)) {
                IDesignerCoreService designerCoreService = (IDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                        IDesignerCoreService.class);
                if (designerCoreService != null) {
                    for (IRepositoryNode repositoryNode : parent.getChildren()) {
                        if (repositoryNode.getObject() != null) {
                            if (repositoryNode.getObject().getId().equals(repositoryObject.getId())) {
                                Problem problem = new Problem();
                                problem.setDescription(type.name() + " " + repositoryNode.getObject().getLabel() + " " //$NON-NLS-1$ //$NON-NLS-2$
                                        + repositoryNode.getObject().getVersion() + " and " + repositoryObject.getLabel() + " " //$NON-NLS-1$ //$NON-NLS-2$
                                        + repositoryObject.getVersion() + " have the same ID."); //$NON-NLS-1$
                                problem.setStatus(ProblemStatus.WARNING);
                                designerCoreService.addProblems(problem);
                            }
                        }
                    }
                }
            }
            if (isAvaliableInTOS) {
                parent.getChildren().add(node);
            }
        }

        if (type == ERepositoryObjectType.METADATA_CONNECTIONS && isAvaliableInTOS) {
            createTables(recBinNode, node, repositoryObject, dbMetadataConnection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_SAPCONNECTIONS) {
            SAPConnection metadataConnection = (SAPConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_DELIMITED) {
            DelimitedFileConnection metadataConnection = (DelimitedFileConnection) ((ConnectionItem) repositoryObject
                    .getProperty().getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_POSITIONAL) {
            PositionalFileConnection metadataConnection = (PositionalFileConnection) ((ConnectionItem) repositoryObject
                    .getProperty().getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_REGEXP) {
            RegexpFileConnection metadataConnection = (RegexpFileConnection) ((ConnectionItem) repositoryObject.getProperty()
                    .getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_XML) {
            if (repositoryObject == null || repositoryObject.getProperty() == null
                    || repositoryObject.getProperty().getItem() == null) {
                System.out.println();
            }
            XmlFileConnection metadataConnection = (XmlFileConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection, validationRules);
        }

        if (type == ERepositoryObjectType.METADATA_FILE_EXCEL) {
            FileExcelConnection metadataConnection = (FileExcelConnection) ((ConnectionItem) repositoryObject.getProperty()
                    .getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection, validationRules);
        }

        if (type == ERepositoryObjectType.METADATA_FILE_LDIF) {
            LdifFileConnection metadataConnection = (LdifFileConnection) ((ConnectionItem) repositoryObject.getProperty()
                    .getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection, validationRules);
        }

        if (type == ERepositoryObjectType.METADATA_LDAP_SCHEMA) {
            LDAPSchemaConnection metadataConnection = (LDAPSchemaConnection) ((ConnectionItem) repositoryObject.getProperty()
                    .getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection, validationRules);
        }

        if (type == ERepositoryObjectType.METADATA_GENERIC_SCHEMA) {
            GenericSchemaConnection genericSchemaConnection = (GenericSchemaConnection) ((ConnectionItem) repositoryObject
                    .getProperty().getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, genericSchemaConnection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_WSDL_SCHEMA) {
            WSDLSchemaConnection genericSchemaConnection = (WSDLSchemaConnection) ((ConnectionItem) repositoryObject
                    .getProperty().getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, genericSchemaConnection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA) {
            SalesforceSchemaConnection genericSchemaConnection = (SalesforceSchemaConnection) ((ConnectionItem) repositoryObject
                    .getProperty().getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, genericSchemaConnection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_EBCDIC) {
            EbcdicConnection ebcdicConnection = (EbcdicConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, ebcdicConnection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_HL7) {
            HL7Connection hl7Connection = (HL7Connection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, hl7Connection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_FTP) {
            FTPConnection ftpConnection = (FTPConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, ftpConnection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_BRMS) {
            BRMSConnection brmsConnection = (BRMSConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, brmsConnection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_MDMCONNECTION) {
            MDMConnection mdmConnection = (MDMConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, mdmConnection, validationRules);
        }
        if (type == ERepositoryObjectType.METADATA_EDIFACT) {
            EDIFACTConnection edifactConnection = (EDIFACTConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, edifactConnection, validationRules);
        }
        for (IRepositoryContentHandler handler : RepositoryContentManager.getHandlers()) {
            handler.addNode(type, recBinNode, repositoryObject, node);
        }
    }

    /**
     * DOC tguiu Comment method "createTables".
     * 
     * @param node
     * @param repositoryObjectType TODO
     * @param iMetadataConnection
     * @param metadataConnection
     */
    private void createTables(RepositoryNode recBinNode, RepositoryNode node, final IRepositoryViewObject repObj, EList list,
            ERepositoryObjectType repositoryObjectType, List<IRepositoryViewObject> validationRules) {
        for (Object currentTable : list) {
            if (currentTable == null) {
                continue;
            }
            if (currentTable instanceof org.talend.core.model.metadata.builder.connection.MetadataTable) {
                org.talend.core.model.metadata.builder.connection.MetadataTable metadataTable = (org.talend.core.model.metadata.builder.connection.MetadataTable) currentTable;
                RepositoryNode tableNode = createMetatableNode(node, repObj, metadataTable, repositoryObjectType);
                if (SubItemHelper.isDeleted(metadataTable)) {
                    // no need to do anything here, the deleted table will be added in function addItemToRecycleBin
                } else {
                    node.getChildren().add(tableNode);
                }
                if (metadataTable.getColumns().size() > 0) {
                    createColumns(recBinNode, tableNode, repObj, metadataTable, ERepositoryObjectType.METADATA_CON_COLUMN);
                    createValidationRules(recBinNode, tableNode, repObj, metadataTable,
                            ERepositoryObjectType.METADATA_VALIDATION_RULES, validationRules);
                }
            } else if (currentTable instanceof Query) {
                Query query = (Query) currentTable;
                RepositoryNode queryNode = createQueryNode(node, repObj, query);
                if (SubItemHelper.isDeleted(query)) {
                    // recBinNode.getChildren().add(queryNode);
                } else {
                    node.getChildren().add(queryNode);
                }

            }
        }
    }

    /**
     * DOC cantoine Comment method "createTable".
     * 
     * @param node
     * @param metadataTable
     * @param repositoryObjectType TODO
     * @param iMetadataConnection
     */
    private void createTable(RepositoryNode recBinNode, RepositoryNode node, final IRepositoryViewObject repObj,
            org.talend.core.model.metadata.builder.connection.MetadataTable metadataTable,
            ERepositoryObjectType repositoryObjectType, List<IRepositoryViewObject> validationRules) {
        if (metadataTable == null) {
            return;
        }
        RepositoryNode tableNode = createMetatableNode(node, repObj, metadataTable, repositoryObjectType);
        if (TableHelper.isDeleted(metadataTable)) {
            recBinNode.getChildren().add(tableNode);
        } else {
            node.getChildren().add(tableNode);
        }
        if (metadataTable.getColumns().size() > 0) {
            createColumns(recBinNode, tableNode, repObj, metadataTable, ERepositoryObjectType.METADATA_CON_COLUMN);
            createValidationRules(recBinNode, tableNode, repObj, metadataTable, ERepositoryObjectType.METADATA_VALIDATION_RULES,
                    validationRules);
        }
    }

    private void createColumns(RepositoryNode recBinNode, RepositoryNode node, final IRepositoryViewObject repObj,
            org.talend.core.model.metadata.builder.connection.MetadataTable metadataTable,
            ERepositoryObjectType repositoryObjectType) {
        List<MetadataColumn> columnList = new ArrayList<MetadataColumn>();
        columnList.addAll(metadataTable.getColumns());
        int num = columnList.size();
        StringBuffer floderName = new StringBuffer();
        floderName.append(Messages.getString("ProjectRepositoryNode.columns"));//$NON-NLS-1$
        floderName.append("(");//$NON-NLS-1$
        floderName.append(num);
        floderName.append(")");//$NON-NLS-1$
        RepositoryNode columnsNode = new StableRepositoryNode(node, floderName.toString(), ECoreImage.FOLDER_CLOSE_ICON);
        node.getChildren().add(columnsNode);

        for (MetadataColumn column : columnList) {
            if (column == null) {
                continue;
            }
            RepositoryNode columnNode = createMataColumnNode(columnsNode, repObj, column, repositoryObjectType);
            columnsNode.getChildren().add(columnNode);

        }
    }

    private void createValidationRules(RepositoryNode recBinNode, RepositoryNode node, final IRepositoryViewObject repObj,
            org.talend.core.model.metadata.builder.connection.MetadataTable metadataTable,
            ERepositoryObjectType repositoryObjectType, List<IRepositoryViewObject> validationRules) {
        if (validationRules.isEmpty()) {
            return;
        }
        IRepositoryViewObject vo = node.getObject();
        if (vo instanceof MetadataTableRepositoryObject) {
            vo = ((MetadataTableRepositoryObject) vo).getViewObject();
        }
        if (vo != null) {
            String schema = vo.getId();
            schema = schema + " - " + metadataTable.getLabel(); //$NON-NLS-1$
            List<IRepositoryViewObject> objs = getValidationRuleObjsFromSchema(validationRules, schema);
            if (objs.size() > 0) {
                int num = objs.size();
                StringBuffer floderName = new StringBuffer();
                floderName.append(Messages.getString("ProjectRepositoryNode.validationRules")); //$NON-NLS-1$
                floderName.append("(");//$NON-NLS-1$
                floderName.append(num);
                floderName.append(")");//$NON-NLS-1$
                RepositoryNode validationRulesNode = new StableRepositoryNode(node, floderName.toString(),
                        ECoreImage.FOLDER_CLOSE_ICON);
                validationRulesNode.setProperties(EProperties.CONTENT_TYPE,
                        ERepositoryObjectType.METADATA_VALIDATIONS_RULES_FOLDER);
                node.getChildren().add(validationRulesNode);
                for (IRepositoryViewObject obj : objs) {
                    addNode(validationRulesNode, ERepositoryObjectType.METADATA_VALIDATION_RULES, recBinNode, obj,
                            validationRules);
                }
            }
        }
    }

    private List<IRepositoryViewObject> getValidationRuleObjsFromSchema(List<IRepositoryViewObject> validationRules, String schema) {
        List<IRepositoryViewObject> objs = new ArrayList<IRepositoryViewObject>();
        if (validationRules != null && validationRules.size() > 0) {
            for (IRepositoryViewObject member : validationRules) {
                if (member != null && member.getProperty() != null) {
                    Item item = member.getProperty().getItem();
                    if (item != null && item instanceof ValidationRulesConnectionItem) {
                        ValidationRulesConnectionItem validItem = (ValidationRulesConnectionItem) item;
                        ValidationRulesConnection connection = (ValidationRulesConnection) validItem.getConnection();
                        if (connection != null && schema.equals(connection.getBaseSchema()) && !objs.contains(member)) {
                            objs.add(member);
                        }
                    }
                }
            }
        }

        return objs;
    }

    private void createTables(RepositoryNode recBinNode, RepositoryNode node, final IRepositoryViewObject repObj,
            Connection metadataConnection, List<IRepositoryViewObject> validationRules) {

        // // 5.GENERIC SCHEMAS
        // RepositoryNode genericSchemaNode = new StableRepositoryNode(node, Messages
        // .getString("RepositoryContentProvider.repositoryLabel.GenericSchema"), ECoreImage.FOLDER_CLOSE_ICON);
        // node.getChildren().add(genericSchemaNode);

        if (metadataConnection instanceof DatabaseConnection) {

            // 1.Tables:
            RepositoryNode tablesNode = new StableRepositoryNode(node,
                    Messages.getString("ProjectRepositoryNode.tableSchemas"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            node.getChildren().add(tablesNode);

            // 2.VIEWS:
            RepositoryNode viewsNode = new StableRepositoryNode(node,
                    Messages.getString("ProjectRepositoryNode.viewSchemas"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            node.getChildren().add(viewsNode);

            // 3.SYNONYMS:
            RepositoryNode synonymsNode = new StableRepositoryNode(node,
                    Messages.getString("ProjectRepositoryNode.synonymSchemas"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            node.getChildren().add(synonymsNode);

            DatabaseConnection dbconn = (DatabaseConnection) metadataConnection;
            List<MetadataTable> allTables = ConnectionHelper.getTablesWithOrders(dbconn);

            // /* only refresh and show tables in current schema or catalog,see bug 0015769 */
            // Set<org.talend.core.model.metadata.builder.connection.MetadataTable> allTables = ProjectNodeHelper
            // .getTablesFromSpecifiedDataPackage(dbconn);
            // /*
            // * bug 18514,if the schema is imported from file for sas databaseconnection,need retrieve again from all
            // * datapackages
            // */
            // if (allTables.isEmpty() && dbconn.getDatabaseType().equals(EDatabaseTypeName.SAS.getDisplayName())) {
            // allTables = ConnectionHelper.getTables(dbconn);
            // }
            Iterator metadataTables = allTables.iterator();

            // Iterator metadataTables = ConnectionHelper.getTables(metadataConnection).iterator();
            while (metadataTables.hasNext()) {
                org.talend.core.model.metadata.builder.connection.MetadataTable metadataTable = (org.talend.core.model.metadata.builder.connection.MetadataTable) metadataTables
                        .next();

                // bug 17768
                if (SubItemHelper.isDeleted(metadataTable)) {
                    continue;
                }

                String typeTable = null;
                if (metadataTable != null && metadataTable.getTableType() != null) {
                    typeTable = metadataTable.getTableType();
                    if (typeTable.equals(MetadataManager.TYPE_TABLE)) {
                        createTable(recBinNode, tablesNode, repObj, metadataTable, ERepositoryObjectType.METADATA_CON_TABLE,
                                validationRules);

                    } else if (typeTable.equals(MetadataManager.TYPE_VIEW)) {
                        createTable(recBinNode, viewsNode, repObj, metadataTable, ERepositoryObjectType.METADATA_CON_TABLE,
                                validationRules);

                    } else if (typeTable.equals(MetadataManager.TYPE_SYNONYM)) {
                        createTable(recBinNode, synonymsNode, repObj, metadataTable, ERepositoryObjectType.METADATA_CON_TABLE,
                                validationRules);
                    }
                    // bug 0017782 ,db2's SYNONYM need to convert to ALIAS;
                    else if (typeTable.equals(MetadataManager.TYPE_ALIAS)) {
                        createTable(recBinNode, synonymsNode, repObj, metadataTable, ERepositoryObjectType.METADATA_CON_TABLE,
                                validationRules);
                    }
                    // else if (typeTable.equals("GENERIC_SCHEMA")) {
                    // //TODO not finished.
                    // createTable(recBinNode, tablesNode, repObj, metadataTable,
                    // ERepositoryObjectType.METADATA_CON_TABLE);
                    // }
                } else {
                    createTable(recBinNode, tablesNode, repObj, metadataTable, ERepositoryObjectType.METADATA_CON_TABLE,
                            validationRules);
                }
            }

            // if (!node.getChildren().contains(tablesNode)) {
            // node.getChildren().add(tablesNode);
            // }

            // createTables(recBinNode, node, repObj, metadataConnection.getTables());

            // 4.Queries:
            boolean isImpala = false;
            if (metadataConnection instanceof DatabaseConnection) {
                if (EDatabaseTypeName.IMPALA.getDisplayName().equals(((DatabaseConnection) metadataConnection).getDatabaseType())) {
                    isImpala = true;
                }
            }
            if (!ConnectionUtils.isHiveConnection(dbconn.getURL()) || isImpala) {
                RepositoryNode queriesNode = new StableRepositoryNode(node,
                        Messages.getString("ProjectRepositoryNode.queries"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
                node.getChildren().add(queriesNode);
                QueriesConnection queriesConnection = (metadataConnection).getQueries();
                if (queriesConnection != null) {
                    createTables(recBinNode, queriesNode, repObj, queriesConnection.getQuery(),
                            ERepositoryObjectType.METADATA_CON_TABLE, validationRules);
                }
            }

            // 5. Change Data Capture
            Item item = node.getObject().getProperty().getItem();
            if (item instanceof DatabaseConnectionItem) {
                DatabaseConnectionItem connectionItem = (DatabaseConnectionItem) item;
                DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();
                if (PluginChecker.isCDCPluginLoaded()) {
                    ICDCProviderService service = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(
                            ICDCProviderService.class);
                    if (service != null && service.canCreateCDCConnection(connection)) {
                        RepositoryNode cdcNode = new StableRepositoryNode(node,
                                Messages.getString("ProjectRepositoryNode.cdcFoundation"), //$NON-NLS-1$
                                ECoreImage.FOLDER_CLOSE_ICON);
                        node.getChildren().add(cdcNode);
                        service.createCDCTypes(recBinNode, cdcNode, connection.getCdcConns());
                    }
                }
            }
        } else if (metadataConnection instanceof SAPConnection) {
            // The sap wizard plugin is loaded
            // 1.Tables:
            createSAPTableNodes(recBinNode, repObj, metadataConnection, node, validationRules);

            // 2.Functions:
            StableRepositoryNode functionNode = new StableRepositoryNode(node,
                    Messages.getString("ProjectRepositoryNode.sapBapi"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            functionNode.setChildrenObjectType(ERepositoryObjectType.METADATA_SAP_FUNCTION);
            node.getChildren().add(functionNode);

            createSAPFunctionNodes(recBinNode, repObj, metadataConnection, functionNode, validationRules);

            // add idocs
            StableRepositoryNode iDocNode = new StableRepositoryNode(node,
                    Messages.getString("ProjectRepositoryNode.sapIDocs"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            iDocNode.setChildrenObjectType(ERepositoryObjectType.METADATA_SAP_IDOC);
            node.getChildren().add(iDocNode);
            createSAPIDocNodes(recBinNode, repObj, metadataConnection, iDocNode);
        } else if (metadataConnection instanceof SalesforceSchemaConnection) {
            createSalesforceModuleNodes(recBinNode, repObj, metadataConnection, node, validationRules);
        } else {
            Set<org.talend.core.model.metadata.builder.connection.MetadataTable> tableset = ConnectionHelper
                    .getTables(metadataConnection);
            EList tables = new BasicEList();
            tables.addAll(tableset);
            createTables(recBinNode, node, repObj, tables, ERepositoryObjectType.METADATA_CON_TABLE, validationRules);
        }
    }

    private void createSAPTableNodes(final RepositoryNode recBin, IRepositoryViewObject repObj, Connection metadataConnection,
            RepositoryNode node, List<IRepositoryViewObject> validationRules) {
        StableRepositoryNode tableContainer = new StableRepositoryNode(node,
                Messages.getString("ProjectRepositoryNode.sapTables"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
        tableContainer.setChildrenObjectType(ERepositoryObjectType.METADATA_CON_TABLE);
        tableContainer.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_SAP_TABLE);
        node.getChildren().add(tableContainer);

        List<MetadataTable> tablesWithOrders = ConnectionHelper.getTablesWithOrders(metadataConnection);
        EList tables = new BasicEList();
        tables.addAll(tablesWithOrders);
        createTables(recBin, tableContainer, repObj, tables, ERepositoryObjectType.METADATA_CON_TABLE, validationRules);

    }

    private void createSalesforceModuleNodes(final RepositoryNode recBin, IRepositoryViewObject rebObj,
            Connection metadataConnection, RepositoryNode connectionNode, List<IRepositoryViewObject> validationRules) {
        EList modules = ((SalesforceSchemaConnection) metadataConnection).getModules();
        for (int i = 0; i < modules.size(); i++) {
            SalesforceModuleUnit unit = (SalesforceModuleUnit) modules.get(i);
            RepositoryNode tableNode = createSalesforceNode(rebObj, connectionNode, unit);

            createTables(recBin, tableNode, rebObj, unit.getTables(), ERepositoryObjectType.METADATA_CON_TABLE, validationRules);
            if (SubItemHelper.isDeleted(unit)) {
                recBin.getChildren().add(tableNode);
            } else {
                connectionNode.getChildren().add(tableNode);
            }

        }
    }

    /**
     * DOC YeXiaowei Comment method "createSAPFunctionNodes".
     * 
     * @param metadataConnection
     * @param functionNode
     */
    private void createSAPFunctionNodes(final RepositoryNode recBin, IRepositoryViewObject rebObj, Connection metadataConnection,
            RepositoryNode functionNode, List<IRepositoryViewObject> validationRules) {
        EList functions = ((SAPConnection) metadataConnection).getFuntions();
        if (functions == null || functions.isEmpty()) {
            return;
        }
        for (int i = 0; i < functions.size(); i++) {
            SAPFunctionUnit unit = (SAPFunctionUnit) functions.get(i);
            RepositoryNode tableNode = createSAPNode(rebObj, functionNode, unit);

            // create input and output container

            RepositoryNode inputNode = new StableRepositoryNode(tableNode,
                    Messages.getString("ProjectRepositoryNode.sapFunctions.inputSchema"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            inputNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_SAP_FUNCTION);
            tableNode.getChildren().add(inputNode);

            createTables(recBin, inputNode, rebObj, unit.getInputTables(), ERepositoryObjectType.METADATA_CON_TABLE,
                    validationRules);

            RepositoryNode outputNode = new StableRepositoryNode(tableNode,
                    Messages.getString("ProjectRepositoryNode.sapFunctions.outputSchema"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            outputNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_SAP_FUNCTION);
            tableNode.getChildren().add(outputNode);

            createTables(recBin, outputNode, rebObj, unit.getTables(), ERepositoryObjectType.METADATA_CON_TABLE, validationRules);
            if (SubItemHelper.isDeleted(unit)) {
                // recBin.getChildren().add(tableNode);
            } else {
                functionNode.getChildren().add(tableNode);
            }

        }
    }

    /**
     * DOC zli Comment method "createSAPIDocNodes".
     * 
     * @param recBin
     * @param rebObj
     * @param metadataConnection
     * @param iDocNode
     */
    private void createSAPIDocNodes(final RepositoryNode recBin, IRepositoryViewObject rebObj, Connection metadataConnection,
            RepositoryNode iDocNode) {
        EList iDocs = ((SAPConnection) metadataConnection).getIDocs();
        if (iDocs == null || iDocs.isEmpty()) {
            return;
        }
        for (int i = 0; i < iDocs.size(); i++) {
            SAPIDocUnit unit = (SAPIDocUnit) iDocs.get(i);
            RepositoryNode tableNode = createSAPNode(rebObj, iDocNode, unit);
            if (SubItemHelper.isDeleted(unit)) {
                // recBin.getChildren().add(tableNode);
            } else {
                iDocNode.getChildren().add(tableNode);
            }

        }
    }

    private RepositoryNode createSalesforceNode(IRepositoryViewObject rebObj, RepositoryNode moduleNode, SalesforceModuleUnit unit) {
        SalesforceModuleRepositoryObject modelObj = new SalesforceModuleRepositoryObject(rebObj, moduleNode, unit);
        modelObj.setLabel(unit.getModuleName());
        RepositoryNode tableNode = new RepositoryNode(modelObj, moduleNode, ENodeType.REPOSITORY_ELEMENT);
        tableNode.setProperties(EProperties.LABEL, modelObj.getLabel());
        tableNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_SALESFORCE_MODULE);
        return tableNode;
    }

    /**
     * DOC YeXiaowei Comment method "createSAPNode".
     * 
     * @param rebObj
     * @param functionNode
     * @param unit
     * @return
     */
    private RepositoryNode createSAPNode(IRepositoryViewObject rebObj, RepositoryNode functionNode, SAPFunctionUnit unit) {
        SAPFunctionRepositoryObject modelObj = new SAPFunctionRepositoryObject(rebObj, functionNode, unit);
        modelObj.setLabel(unit.getName());
        RepositoryNode tableNode = new RepositoryNode(modelObj, functionNode, ENodeType.REPOSITORY_ELEMENT);
        tableNode.setProperties(EProperties.LABEL, modelObj.getLabel());
        tableNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_SAP_FUNCTION);
        return tableNode;
    }

    private RepositoryNode createSAPNode(IRepositoryViewObject rebObj, RepositoryNode functionNode, SAPIDocUnit unit) {
        SAPIDocRepositoryObject modelObj = new SAPIDocRepositoryObject(rebObj, functionNode, unit);
        modelObj.setLabel(unit.getName());
        RepositoryNode tableNode = new RepositoryNode(modelObj, functionNode, ENodeType.REPOSITORY_ELEMENT);
        tableNode.setProperties(EProperties.LABEL, modelObj.getLabel());
        tableNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_SAP_IDOC);
        return tableNode;
    }

    /**
     * DOC tguiu Comment method "createMetatable".
     * 
     * @param node
     * @param table
     * @param repositoryObjectType TODO
     * @param iMetadataFileDelimited
     * @return
     */
    private RepositoryNode createMetatableNode(RepositoryNode node, IRepositoryViewObject repObj,
            final org.talend.core.model.metadata.builder.connection.MetadataTable table,
            ERepositoryObjectType repositoryObjectType) {
        MetadataTableRepositoryObject modelObj = new MetadataTableRepositoryObject(repObj, table);
        RepositoryNode tableNode = new RepositoryNode(modelObj, node, ENodeType.REPOSITORY_ELEMENT);
        tableNode.setProperties(EProperties.LABEL, modelObj.getLabel());
        tableNode.setProperties(EProperties.CONTENT_TYPE, repositoryObjectType);
        return tableNode;

    }

    private RepositoryNode createMataColumnNode(RepositoryNode node, IRepositoryViewObject repObj, MetadataColumn column,
            ERepositoryObjectType repositoryObjectType) {
        MetadataColumnRepositoryObject columnObj = new MetadataColumnRepositoryObject(repObj, column);
        RepositoryNode columnNode = new RepositoryNode(columnObj, node, ENodeType.REPOSITORY_ELEMENT);
        columnNode.setProperties(EProperties.LABEL, columnObj.getLabel());
        columnNode.setProperties(EProperties.CONTENT_TYPE, repositoryObjectType);
        return columnNode;
    }

    /**
     * DOC cantoine Comment method "createQueryNode".
     * 
     * @param node
     * @param repObj
     * @param query
     * @return
     */
    private RepositoryNode createQueryNode(RepositoryNode node, IRepositoryViewObject repObj, Query query) {
        QueryRepositoryObject modelObj = new QueryRepositoryObject(repObj, query);
        RepositoryNode tableNode = new RepositoryNode(modelObj, node, ENodeType.REPOSITORY_ELEMENT);
        tableNode.setProperties(EProperties.LABEL, query.getLabel());
        tableNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CON_QUERY);
        return tableNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getProject()
     */
    @Override
    public org.talend.core.model.general.Project getProject() {
        return this.project;
    }

    @Override
    public RepositoryNode getRootRepositoryNode(ERepositoryObjectType type) {
        return getRootRepositoryNode(type, false);
    }

    @Override
    public RepositoryNode getRootRepositoryNode(ERepositoryObjectType type, boolean tryInit) {
        RepositoryNode rootTypeNode = null;
        if (type != null) {
            String typeName = type.name();
            if (repositoryNodeMap.containsKey(typeName)) {
                rootTypeNode = repositoryNodeMap.get(typeName);
                if (tryInit) {
                    initNode(rootTypeNode);
                }

            }
        }
        return rootTypeNode;
    }

    @Override
    public final void initNode(final IRepositoryNode rootTypeNode) {
        SafeRunner.run(new ISafeRunnable() {

            @Override
            public void handleException(Throwable exception) {
                ExceptionHandler.process(exception);
            }

            @Override
            public void run() throws Exception {
                if (rootTypeNode instanceof RepositoryNode && rootTypeNode.getParent() instanceof ProjectRepositoryNode
                        && !((RepositoryNode) rootTypeNode).isInitialized()) {
                    ((ProjectRepositoryNode) rootTypeNode.getParent()).initializeChildren(rootTypeNode);
                    ((RepositoryNode) rootTypeNode).setInitialized(true);
                }
            }

        });

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.nodes.IProjectRepositoryNode#getRecBinNode()
     */
    @Override
    public RepositoryNode getRecBinNode() {
        return this.recBinNode;
    }

    public boolean getMergeRefProject() {
        IPreferenceStore preferenceStore = RepositoryManager.getRepositoryPreferenceStore();
        return preferenceStore.getBoolean(IRepositoryPrefConstants.MERGE_REFERENCE_PROJECT);
    }

    @Override
    public String getLabel() {
        if (getProject() != null) {// compute branch url to add to the project label.
            String urlBranch = null;
            if (ProjectManager.getInstance().getCurrentBranchURL(project) != null) {
                urlBranch = showSVNRoot();
            }

            return getProject().getLabel() + (urlBranch != null && !"".equals(urlBranch) ? '(' + urlBranch + ')' : ""); //$NON-NLS-1$//$NON-NLS-2$
        }
        return super.getLabel();
    }

    @Override
    public void dispose() {
        boolean doDispose = isEnableDisposed();

        if (doDispose) {
            this.project = null;
            this.recBinNode = null;
        }
        // Ref
        if (refProject != null) {
            for (IRepositoryNode refP : refProject.getChildren()) {
                if (refP instanceof IProjectRepositoryNode) {
                    // dispose the ref-projects also.
                    if (refP instanceof RepositoryNode) {
                        ((RepositoryNode) refP).setEnableDisposed(doDispose);
                    }
                    refP.dispose();
                }
            }
        }
        // for all
        for (DynaEnum<? extends DynaEnum<?>> de : ERepositoryObjectType.values()) {
            if (de instanceof ERepositoryObjectType) {
                RepositoryNode rootRepositoryNode = getRootRepositoryNode((ERepositoryObjectType) de);
                if (rootRepositoryNode != null && !rootRepositoryNode.isDisposed()) {
                    // dispose the root node for type.
                    rootRepositoryNode.setEnableDisposed(doDispose);
                    rootRepositoryNode.dispose();
                }
            }
        }

        //
        if (this.repositoryNodeMap != null) {
            for (String type : this.repositoryNodeMap.keySet()) {
                RepositoryNode repositoryNode = this.repositoryNodeMap.get(type);
                if (repositoryNode != null && !repositoryNode.isDisposed()) {
                    // dispose the root node for type.
                    repositoryNode.setEnableDisposed(doDispose);
                    repositoryNode.dispose();
                }
            }
            if (doDispose) {
                this.repositoryNodeMap.clear();
            }
        }

        if (doDispose && this.nodeAndProject != null) {
            nodeAndProject.clear();
        }

        if (doDispose) {
            // use reflect to set the object to null.
            final Field[] declaredFields = this.getClass().getDeclaredFields();
            for (Field f : declaredFields) {
                f.setAccessible(true);
                try {
                    final Object object = f.get(this);
                    if (object instanceof RepositoryNode) {
                        f.set(this, null);
                    }

                } catch (IllegalArgumentException e) {
                    //
                } catch (IllegalAccessException e) {
                    //
                }
            }
        }
        super.dispose();
    }

    public void cleanup() {
        // reset it
        defaultProjRepoNode = null;
    }
}
