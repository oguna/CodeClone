package mining;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import normalizer.NormalizedStringCreator;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.io.SVNRepository;

public class CodeFragmentDetector {
	public CodeFragmentDetector(){
	}

	public CodeFragment execute(SVNRepository repository,String filePath, long current_revision_num, int start, int end,long id) {
		SVNNodeKind nodeKind;
		try {
			nodeKind = repository.checkPath(filePath, current_revision_num);
			if (nodeKind == SVNNodeKind.NONE || nodeKind == SVNNodeKind.DIR) {
				System.err.println("Not found.");
				return null;
			}
			SVNProperties fileProperties = new SVNProperties();
			OutputStream content = new ByteArrayOutputStream ();
			repository.getFile(filePath, current_revision_num, fileProperties, content);
			String mimeType = fileProperties.getStringValue(SVNProperty.MIME_TYPE);
			boolean isTextType = SVNProperty.isTextMimeType(mimeType);
			if (isTextType) {
				NormalizedStringCreator normalizer = new NormalizedStringCreator(content.toString());
				LinkedList<String> normalizedTokens = new LinkedList<String>();
				normalizedTokens = normalizer.execute(start,end);
				String code = trim(content.toString(),start,end);
				CodeFragment codeFragment = new CodeFragment(code,normalizedTokens,id);
				return codeFragment;
			}
			else {
				System.out.println("Not a text file.");
				return null;
			}
		} catch (SVNException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String trim(String str, int start, int end){
		String content = "";
		String[] strs = str.split("\n");
		for(int i=0 ; i < strs.length ; i++){
			if(i+1 >=start && i+1< end) content = content + strs[i] + "\n";
			else if(i+1 == end) content = content + strs[i];
		}
		return content;
	}
}
