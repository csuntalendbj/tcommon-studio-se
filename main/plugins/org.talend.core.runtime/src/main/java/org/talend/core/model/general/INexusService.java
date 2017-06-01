package org.talend.core.model.general;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.talend.core.IService;
import org.talend.core.nexus.NexusServerBean;

public interface INexusService extends IService {
	
	Map upload(NexusServerBean nexusServerBean, String groupId, String artifactId, String version,  URL source);

	Map getMavenMetadata(NexusServerBean nexusServerBean, String groupId, String artifactId, String version);

	InputStream getContentInputStream(NexusServerBean nexusServerBean,String r, String g, String a, String v, String e);
}
