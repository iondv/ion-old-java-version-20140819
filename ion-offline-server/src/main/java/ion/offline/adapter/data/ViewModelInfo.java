package ion.offline.adapter.data;

public class ViewModelInfo {
	private String className;
	
	private String modelType;
	
	private String modificationDate;
	
	private String downloadUrl;
	
	public ViewModelInfo(String className, String modelType, String modificationDate, String downloadUrl){
		this.className = className;
		this.modelType = modelType;
		this.modificationDate = modificationDate;
		this.downloadUrl = downloadUrl;		
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getModelType() {
		return modelType;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;
	}

	public String getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(String modificationDate) {
		this.modificationDate = modificationDate;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
}
