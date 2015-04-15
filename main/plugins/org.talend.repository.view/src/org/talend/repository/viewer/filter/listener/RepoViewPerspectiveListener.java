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
package org.talend.repository.viewer.filter.listener;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IPerspectiveListener3;
import org.eclipse.ui.IPerspectiveListener4;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.talend.repository.navigator.RepoViewCommonViewer;
import org.talend.repository.viewer.filter.PerspectiveFilterHelper;
import org.talend.repository.viewer.filter.RepositoryNodeFilterHelper;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class RepoViewPerspectiveListener implements IPerspectiveListener, IPerspectiveListener2, IPerspectiveListener3,
        IPerspectiveListener4 {

    private final CommonViewer commonViewer;

    public RepoViewPerspectiveListener(final CommonViewer commonViewer) {
        super();
        this.commonViewer = commonViewer;
    }

    protected CommonViewer getCommonViewer() {
        return this.commonViewer;
    }

    protected CommonNavigator getCommonNavigator() {
        return getCommonViewer().getCommonNavigator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPerspectiveListener4#perspectivePreDeactivate(org.eclipse.ui.IWorkbenchPage,
     * org.eclipse.ui.IPerspectiveDescriptor)
     */
    @Override
    public void perspectivePreDeactivate(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
        //
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPerspectiveListener3#perspectiveOpened(org.eclipse.ui.IWorkbenchPage,
     * org.eclipse.ui.IPerspectiveDescriptor)
     */
    @Override
    public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
        //
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPerspectiveListener3#perspectiveClosed(org.eclipse.ui.IWorkbenchPage,
     * org.eclipse.ui.IPerspectiveDescriptor)
     */
    @Override
    public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
        checkListener();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPerspectiveListener3#perspectiveDeactivated(org.eclipse.ui.IWorkbenchPage,
     * org.eclipse.ui.IPerspectiveDescriptor)
     */
    @Override
    public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
        //
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPerspectiveListener3#perspectiveSavedAs(org.eclipse.ui.IWorkbenchPage,
     * org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IPerspectiveDescriptor)
     */
    @Override
    public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective,
            IPerspectiveDescriptor newPerspective) {
        //
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage,
     * org.eclipse.ui.IPerspectiveDescriptor)
     */
    @Override
    public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
        doPerspectiveFilter(); // when switch the perspecitve. or open new one too.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage,
     * org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
     */
    @Override
    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
        checkListener();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage,
     * org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
     */
    @Override
    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef,
            String changeId) {
        // after open studio
        if (partRef.getId().equals(getCommonViewer().getNavigatorContentService().getViewerId())
                && IWorkbenchPage.CHANGE_VIEW_SHOW.equals(changeId)) { // only for repository view when show
            doPerspectiveFilter();
        }
        checkListener();
    }

    private void doPerspectiveFilter() {
        final CommonViewer commonViewer2 = getCommonViewer();
        if (commonViewer2 instanceof RepoViewCommonViewer) {
            RepositoryNodeFilterHelper.filter(commonViewer2, RepositoryNodeFilterHelper.isActivedFilter(),
                    PerspectiveFilterHelper.isActivedPerspectiveFilter());

            ((RepoViewCommonViewer) commonViewer2).fireRefreshNodePerspectiveLisenter();
        }
    }

    private void checkListener() {
        // if viewer is completly removed from all views then remove this from perspective listeners.
        if (getCommonViewer().getControl().isDisposed()) {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().removePerspectiveListener(this);
        }// else do nothing
    }

}
