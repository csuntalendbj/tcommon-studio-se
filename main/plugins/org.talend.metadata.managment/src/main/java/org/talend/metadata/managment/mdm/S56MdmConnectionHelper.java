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
package org.talend.metadata.managment.mdm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis.client.Stub;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.classloader.ClassLoaderFactory;
import org.talend.core.classloader.DynamicClassLoader;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.utils.ReflectionUtils;

/**
 * created by wchen on Apr 15, 2015 Detailled comment
 *
 */
public class S56MdmConnectionHelper extends AbsMdmConnectionHelper {

    @Override
    public Stub checkConnection(String url, String universe, String userName, String password) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Stub stub = null;
        try {
            DynamicClassLoader classLoader = ClassLoaderFactory.getClassLoader(MDMVersions.MDM_S56.name());
            Thread.currentThread().setContextClassLoader(classLoader);
            stub = getStub(classLoader, url, universe, userName, password);
        } catch (Exception e) {
            ExceptionHandler.process(e);
            stub = null;
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        return stub;
    }

    private Stub getStub(DynamicClassLoader classLoader, String url, String universe, String userName, String password)
            throws Exception {
        Stub stub = null;
        Object serviceLocator = ReflectionUtils.newInstance("org.talend.mdm.webservice.XtentisServiceLocator", classLoader, null);
        ReflectionUtils.invokeMethod(serviceLocator, "setXtentisPortEndpointAddress", new Object[] { url });
        Object invokeMethod = ReflectionUtils.invokeMethod(serviceLocator, "getXtentisPort", new Object[0]);
        if (invokeMethod instanceof Stub) {
            stub = (Stub) invokeMethod;
            if (universe == null || universe.trim().length() == 0) {
                stub.setUsername(userName);
            } else {
                stub.setUsername(universe + "/" + userName); //$NON-NLS-1$
            }
            stub.setPassword(password);
            Object wsping = ReflectionUtils.newInstance("org.talend.mdm.webservice.WSPing", null);
            ReflectionUtils.invokeMethod(stub, "ping", new Object[] { wsping });
        }
        return stub;
    }

    /**
     * 
     * DOC wchen Comment method "getPKs". used to call getDataModelPKs,getDataClusterPKs,getUniversePKs
     * 
     * @param stub
     * @param modelOrContainerMethod
     * @param modelOrContainerClass
     * @return
     */
    @Override
    public List<String> getPKs(Stub stub, String modelOrContainerMethod, String modelOrContainerClass) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        List<String> dataModelStrs = new ArrayList<String>();
        try {
            DynamicClassLoader classLoader = ClassLoaderFactory.getClassLoader(MDMVersions.MDM_S56.name());
            Thread.currentThread().setContextClassLoader(classLoader);
            Object modelPK = ReflectionUtils.newInstance(modelOrContainerClass, classLoader, new Object[] { "" });
            Object dataModels = ReflectionUtils.invokeMethod(stub, modelOrContainerMethod, new Object[] { modelPK });
            if (dataModels instanceof Object[]) {
                Object[] dataModelArray = (Object[]) dataModels;
                for (Object dataModel : dataModelArray) {
                    Object pk = ReflectionUtils.invokeMethod(dataModel, "getPk", new Object[0]);
                    if (pk instanceof String) {
                        dataModelStrs.add((String) pk);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        return dataModelStrs;

    }

    @Override
    public void initConcept(MDMConnection mdmConn, File file) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        String userName = mdmConn.getUsername();
        String password = mdmConn.getValue(mdmConn.getPassword(), false);
        String server = mdmConn.getServer();
        String port = mdmConn.getPort();
        String universe = mdmConn.getUniverse();
        String datamodel = mdmConn.getDatamodel();
        try {
            DynamicClassLoader classLoader = ClassLoaderFactory.getClassLoader(MDMVersions.MDM_S56.name());
            Thread.currentThread().setContextClassLoader(classLoader);
            String url = "http://" + server + ":" + port + "/talend/TalendPort";
            Stub stub = getStub(classLoader, url, universe, userName, password);
            if (stub == null) {
                return;
            }
            stub.setUsername(userName);
            stub.setPassword(password);

            Object wsping = ReflectionUtils.newInstance("org.talend.mdm.webservice.WSPing", classLoader, null);
            ReflectionUtils.invokeMethod(stub, "ping", new Object[] { wsping });

            if (universe != null && !"".equals(universe)) { //$NON-NLS-1$
                stub.setUsername(universe + "/" + userName); //$NON-NLS-1$
                stub.setPassword(password);
            } else {
                stub.setUsername(userName);
                stub.setPassword(password);
            }

            // find data model pk
            Object wsModelPKs = ReflectionUtils.newInstance("org.talend.mdm.webservice.WSRegexDataModelPKs", classLoader,
                    new Object[] { "" });
            Object dataModelPKs = ReflectionUtils.invokeMethod(stub, "getDataModelPKs", new Object[] { wsModelPKs });
            if (dataModelPKs == null) {
                return;
            }
            Object findDataModelPK = null;
            if (dataModelPKs instanceof Object[]) {
                Object[] dataModelPKArray = (Object[]) dataModelPKs;
                for (Object dataModelPK : dataModelPKArray) {
                    Object pk = ReflectionUtils.invokeMethod(dataModelPK, "getPk", new Object[0]);
                    if (datamodel != null && datamodel.equals(pk)) {
                        findDataModelPK = dataModelPK;
                        break;
                    }
                }
            }

            if (findDataModelPK == null) {
                return;
            }

            // find data model
            Object wsDataModel = ReflectionUtils.newInstance("org.talend.mdm.webservice.WSGetDataModel", classLoader,
                    new Object[] { findDataModelPK });
            Object dataModel = ReflectionUtils.invokeMethod(stub, "getDataModel", new Object[] { wsDataModel });
            if (dataModel == null) {
                return;
            }
            Object xsdSchema = ReflectionUtils.invokeMethod(dataModel, "getXsdSchema", new Object[0]);
            if (xsdSchema instanceof String) {
                writeInFile(file, (String) xsdSchema);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

    }

    @Override
    public String getXsdSchema(Stub stub, String resName) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        String xsdSchema = "";
        try {
            DynamicClassLoader classLoader = ClassLoaderFactory.getClassLoader(MDMVersions.MDM_S56.name());
            Thread.currentThread().setContextClassLoader(classLoader);
            Object wsModelPKs = ReflectionUtils.newInstance("org.talend.mdm.webservice.WSDataModelPK", classLoader,
                    new Object[] { resName });
            Object wsDataModel = ReflectionUtils.newInstance("org.talend.mdm.webservice.WSGetDataModel", classLoader,
                    new Object[] { wsModelPKs });
            Object dataModel = ReflectionUtils.invokeMethod(stub, "getDataModel", new Object[] { wsDataModel });
            if (dataModel != null) {
                Object xsdSchemaObj = ReflectionUtils.invokeMethod(dataModel, "getXsdSchema", new Object[0]);
                if (xsdSchemaObj instanceof String) {
                    xsdSchema = (String) xsdSchemaObj;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        return xsdSchema;
    }

    private static void writeInFile(File file, String schema) {
        StringReader reader = new StringReader(schema);

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            char[] c = new char[1024];
            int l = 0;
            while ((l = reader.read(c)) != -1) {
                writer.write(c, 0, l);
            }
            writer.flush();
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
