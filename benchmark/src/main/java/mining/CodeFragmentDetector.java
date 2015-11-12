package mining;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;

import normalizer.NormalizedStringCreator;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.io.SVNRepository;

/**
 * @author y-yusuke
 *
 */
public class CodeFragmentDetector {
	public CodeFragmentDetector(){
	}

	/**
	 * 特定のリビジョンの変更されたメソッドを抽出
	 * @param repository
	 * @param filePath
	 * @param current_revision_num
	 * @param start
	 * @param end
	 * @param id
	 * @return メソッドのコード片，正規化したコード片，Code_Fragment_Id
	 */
	public CodeFragment execute(SVNRepository repository,
													String filePath, long current_revision_num,
													int start, int end,long id) {
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
				//正規化処理
				NormalizedStringCreator normalizer = new NormalizedStringCreator(content.toString());
				List<String> normalizedTokens = normalizer.execute(start,end);
				//変更されたメソッドの抽出
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

	/**
	 * 変更されたメソッドのみを抽出
	 * @param str
	 * @param start
	 * @param end
	 * @return 変更されたメソッド
	 */
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
