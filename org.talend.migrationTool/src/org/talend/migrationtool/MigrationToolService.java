// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.migrationtool;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.IMigrationToolService;
import org.talend.core.model.migration.IProjectMigrationTask;
import org.talend.core.model.migration.IWorkspaceMigrationTask;
import org.talend.core.prefs.PreferenceManipulator;
import org.talend.migrationtool.model.GetTasksHelper;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public class MigrationToolService implements IMigrationToolService {

    private static Logger log = Logger.getLogger(MigrationToolService.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.migration.IMigrationToolService#executeProjectTasks()
     */
    public void executeProjectTasks() {
        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        Project project = repositoryContext.getProject();

        log.trace("Migration tool: project [" + project.getLabel() + "] tasks");

        List<IProjectMigrationTask> toExecute = GetTasksHelper.getProjectTasks();
        List<String> done = new ArrayList<String>(project.getEmfProject().getMigrationTasks());

        for (IProjectMigrationTask task : toExecute) {
            if (!done.contains(task.getId())) {
                if (task.execute(project)) {
                    done.add(task.getId());
                    log.debug("Task \"" + task.getName() + "\" done");
                } else {
                    log.debug("Task \"" + task.getName() + "\" failed");
                }
            }
        }

        IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
        try {
            repFactory.setMigrationTasksDone(done);
        } catch (PersistenceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.migration.IMigrationToolService#initNewProjectTasks()
     */
    public void initNewProjectTasks() {
        // List<IProjectMigrationTask> toExecute = GetTasksHelper.getProjectTasks();
        // setTasksDone(toExecute);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.migration.IMigrationToolService#executeWorspaceTasks()
     */
    public void executeWorspaceTasks() {
        log.trace("Migration tool: workspace tasks");

        PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());
        List<IWorkspaceMigrationTask> toExecute = GetTasksHelper.getWorkspaceTasks();
        List<String> done = prefManipulator.readWorkspaceTasksDone();

        if (done.isEmpty()) {
            initNewWorkspaceTasks();
            done = prefManipulator.readWorkspaceTasksDone();
        }

        for (IWorkspaceMigrationTask task : toExecute) {
            if (!done.contains(task.getId())) {
                if (task.execute()) {
                    prefManipulator.addWorkspaceTaskDone(task.getId());
                    log.debug("Task \"" + task.getName() + "\" done");
                } else {
                    log.debug("Task \"" + task.getName() + "\" failed");
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.migration.IMigrationToolService#initNewWorkspaceTasks()
     */
    public void initNewWorkspaceTasks() {
        List<IWorkspaceMigrationTask> toExecute = GetTasksHelper.getWorkspaceTasks();
        PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());
        for (IWorkspaceMigrationTask task : toExecute) {
            prefManipulator.addWorkspaceTaskDone(task.getId());
        }
    }

}
