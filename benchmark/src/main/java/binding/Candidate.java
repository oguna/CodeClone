package binding;

/**
 * @author y-yusuke
 *
 */
public class Candidate {
	long id;
	long codeFragmentId;
	long revision;
	String process;
	String repositoryRootURL;
	String filePath;
	long revisionIdentifier;
	long start;
	long end;
	String targetClass;
	String targetMethod;

	public Candidate(){
	}
	public long getId() {
		return id;
	}
	public long getCodeFragmentId() {
		return codeFragmentId;
	}
	public long getRevision() {
		return revision;
	}
	public String getProcess() {
		return process;
	}
	public String getRepositoryRootURL() {
		return repositoryRootURL;
	}
	public String getFilePath() {
		return filePath;
	}
	public long getRevisionIdentifier() {
		return revisionIdentifier;
	}
	public long getStart() {
		return start;
	}
	public long getEnd() {
		return end;
	}
	public String getTargetClass() {
		return targetClass;
	}
	public String getTargetMethod() {
		return targetMethod;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setCodeFragmentId(long codeFragmentId) {
		this.codeFragmentId = codeFragmentId;
	}
	public void setRevision(long revision) {
		this.revision = revision;
	}
	public void setProcess(String process) {
		this.process = process;
	}
	public void setRepositoryRootURL(String repositoryRootURL) {
		this.repositoryRootURL = repositoryRootURL;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public void setRevisionIdentifier(long revisionIdentifier) {
		this.revisionIdentifier = revisionIdentifier;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public void setEnd(long end) {
		this.end = end;
	}
	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}
	public void setTargetMethod(String targetMethod) {
		this.targetMethod = targetMethod;
	}
}
