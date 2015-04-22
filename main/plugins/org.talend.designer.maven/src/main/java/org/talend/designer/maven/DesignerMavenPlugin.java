// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.maven;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class DesignerMavenPlugin implements BundleActivator {

    private static DesignerMavenPlugin plugin;

    private static BundleContext context;

    public BundleContext getContext() {
        return context;
    }

    public static DesignerMavenPlugin getPlugin() {
        return plugin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        this.plugin = this;
        DesignerMavenPlugin.context = bundleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        DesignerMavenPlugin.context = null;
    }

}
