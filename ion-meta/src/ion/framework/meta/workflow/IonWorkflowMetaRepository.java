package ion.framework.meta.workflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ion.core.IWorkflowMeta;
import ion.core.IWorkflowMetaRepository;
import ion.core.IonException;
import ion.framework.workflow.plain.StoredWorkflowModel;

public class IonWorkflowMetaRepository implements IWorkflowMetaRepository {
	
	private File workflowDirectory;
	
	private final static String globalKey = "--GLOBAL--";

	private Map<String, Map<String, IWorkflowMeta>> workflowModels;
	
	public void setWorkflowDirectory(File workflowDirectory) throws IonException {
		this.workflowDirectory = workflowDirectory;
		try {
	    setWorkflowModels();
    } catch (UnsupportedEncodingException | FileNotFoundException e) {
	    throw new IonException(e);
    }
	}

	public IonWorkflowMetaRepository() {
		this.workflowModels = new HashMap<String, Map<String,IWorkflowMeta>>();
  }

	private void setWorkflowModels() throws UnsupportedEncodingException, FileNotFoundException{
		if(workflowDirectory != null && workflowDirectory.isDirectory() && workflowDirectory.exists()){
			Gson gs = new GsonBuilder().serializeNulls().create();
			File[] fl = workflowDirectory.listFiles();
			for (File f: fl){
					Reader r = new InputStreamReader(new FileInputStream(f),"UTF-8");
					StoredWorkflowModel wm = gs.fromJson(r,StoredWorkflowModel.class);
					String c = wm.wfClass;
					if (c == null || c.isEmpty())
						c = globalKey;
					if (!workflowModels.containsKey(c))
						workflowModels.put(c, new HashMap<String, IWorkflowMeta>());
					workflowModels.get(c).put(wm.name, new IonWorkflowMeta(wm));
			}
		}
	}

	@Override
	public IWorkflowMeta getWorkflow(String className, String name) {
		if (workflowModels.containsKey(className))
			if (workflowModels.get(className).containsKey(name))
				return workflowModels.get(className).get(name);
		return null;
	}

	@Override
	public Collection<IWorkflowMeta> getClassWorkflows(String className) {
		Collection<IWorkflowMeta> result = new LinkedList<IWorkflowMeta>();
		if (workflowModels.containsKey(className))
			result.addAll(workflowModels.get(className).values());

		return result;
	}

	@Override
	public IWorkflowMeta getGlobalWorkflow(String name) {
		return getWorkflow(globalKey, name);
	}

	@Override
	public Collection<IWorkflowMeta> getGlobalWorkflows() {
		return getClassWorkflows(globalKey);
	}
}