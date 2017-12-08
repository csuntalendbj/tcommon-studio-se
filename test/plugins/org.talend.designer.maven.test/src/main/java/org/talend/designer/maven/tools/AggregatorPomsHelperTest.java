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

import org.junit.Test;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.repository.ProjectManager;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class AggregatorPomsHelperTest {

    @Test
    public void testGetJobProjectName() {
        Project project = ProjectManager.getInstance().getCurrentProject();
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setLabel("Job1");
        property.setVersion("1.0");
        String jobProjectName = AggregatorPomsHelper.getJobProjectName(project, property);
        assertEquals("AUTO_LOGIN_PROJECT_JOB1_1.0", jobProjectName);
    }

    @Test
    public void getJobProjectFolderName() {
        String label = "Job1";
        String version = "1.0";
        String jobFolderName = AggregatorPomsHelper.getJobProjectFolderName(label, version);
        assertEquals("job1_1.0", jobFolderName);
    }

    @Test
    public void testgetJobProjectId() {
        String id = "abcde-_e";
        String version = "1.0";
        String jobProjectId = AggregatorPomsHelper.getJobProjectId(id, version);
        assertEquals("abcde-_e|1.0", jobProjectId);
    }

}
