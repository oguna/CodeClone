package checker;

import java.util.List;

import binding.Binding;

public class Oracle {
	long id;
	long revision;
	String process;
	String filePath;
	long start;
	long end;
	String sourcecode;
	String targetClass;
	String targetMethod;
	List<Binding> binds;
	List<String> tokens;
	List<String> normalizedTokens;

	public Oracle(long id, long revision, String process,
						String filePath, long start, long end) {
		this.id = id;
		this.revision = revision;
		this.process = process;
		this.filePath = filePath;
		this.start = start;
		this.end = end;
	}

	public long getId() {
		return id;
	}
	public long getRevision() {
		return revision;
	}
	public String getProcess() {
		return process;
	}
	public String getFilePath() {
		return filePath;
	}
	public long getStart() {
		return start;
	}
	public long getEnd() {
		return end;
	}

	public String getSourcecode() {
		return sourcecode;
	}

	public String getTargetClass() {
		return targetClass;
	}

	public String getTargetMethod() {
		return targetMethod;
	}

	public void setSourcecode(String sourcecode) {
		this.sourcecode = sourcecode;
	}

	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}

	public void setTargetMethod(String targetMethod) {
		this.targetMethod = targetMethod;
	}

	public List<Binding> getBinds() {
		return binds;
	}

	public void setBinds(List<Binding> binds) {
		this.binds = binds;
	}

	public List<String> getTokens() {
		return tokens;
	}

	public List<String> getNormalizedTokens() {
		return normalizedTokens;
	}

	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}

	public void setNormalizedTokens(List<String> normalizedTokens) {
		this.normalizedTokens = normalizedTokens;
	}
}
