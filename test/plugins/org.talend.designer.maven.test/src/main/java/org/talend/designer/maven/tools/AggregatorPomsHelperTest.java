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
package org.talend.designer.maven.tools;

import static org.junit.Assert.*;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.MavenPlugin;
import org.junit.Test;
import org.talend.designer.maven.model.TalendJavaProjectConstants;
import org.talend.repository.ProjectManager;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class AggregatorPomsHelperTest {

    @Test
    public void testAddToAndRemoveFromParentModules() throws Exception {
        String projectTechName = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IFolder pomsFolder = root.getFolder(new Path(projectTechName + "/" + TalendJavaProjectConstants.DIR_POMS));
        IFolder jobFolder = pomsFolder.getFolder("jobs").getFolder("process").getFolder("job1");
        if (!jobFolder.exists()) {
            jobFolder.create(true, true, null);
        }
        IFile jobPom = jobFolder.getFile("pom.xml");
        AggregatorPomsHelper.addToParentModules(jobPom);

        IFile processPom = pomsFolder.getFolder("jobs").getFolder("process").getFile("pom.xml");
        Model model = MavenPlugin.getMavenModelManager().readMavenModel(processPom);
        assertNotNull(model.getModules());
        assertTrue(model.getModules().contains("job1/pom.xml"));

        AggregatorPomsHelper.removeFromParentModules(jobPom);

        model = MavenPlugin.getMavenModelManager().readMavenModel(processPom);
        assertNotNull(model.getModules());
        assertFalse(model.getModules().contains("job1/pom.xml"));
    }

}
