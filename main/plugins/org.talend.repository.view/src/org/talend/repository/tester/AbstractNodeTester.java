// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.tester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryNode;

public abstract class AbstractNodeTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof RepositoryNode) {
            Boolean testProperty = testProperty(receiver, property, args, expectedValue);
            if (testProperty != null) {
                return testProperty.booleanValue();
            }
            Assert.isTrue(false);// cause we should never be here
        }
        return false;
    }

    /**
     * 
     * DOC ggu Comment method "testProperty".
     * 
     * @param receiver
     * @param property
     * @param args
     * @param expectedValue
     * @return if null, will assert it, if not null, will return the value;
     */
    protected abstract Boolean testProperty(Object receiver, String property, Object[] args, Object expectedValue);

    public boolean isTypeTopNode(RepositoryNode repositoryNode, ERepositoryObjectType type) {
        boolean flag = isTypeNode(repositoryNode, type);
        if (flag && repositoryNode.getType() == IRepositoryNode.ENodeType.SYSTEM_FOLDER) {
            RepositoryNode parent = repositoryNode.getParent();
            if (parent != null) {
                if (parent instanceof ProjectRepositoryNode) {
                    return true;
                } else {
                    // need more test
                    // ERepositoryObjectType parentContentType = parent.getContentType();
                    // if (parentContentType != null && !parentContentType.equals(repositoryNode.getContentType())) {
                    // return true; // not same type
                    // }
                }

            }
        }
        return false;
    }

    public boolean isTypeNode(RepositoryNode repositoryNode, ERepositoryObjectType type) {
        ERepositoryObjectType contentType = getNodeContentType(repositoryNode);
        return contentType != null && contentType.equals(type);
    }

    public boolean isTypeNode(RepositoryNode repositoryNode) {
        ERepositoryObjectType contentType = getNodeContentType(repositoryNode);
        return contentType != null && (ERepositoryObjectType.getAllTypesOfProcess().contains(contentType));
    }

    public ERepositoryObjectType getNodeContentType(RepositoryNode repositoryNode) {
        return repositoryNode != null ? (ERepositoryObjectType) repositoryNode.getProperties(EProperties.CONTENT_TYPE) : null;
    }
}
