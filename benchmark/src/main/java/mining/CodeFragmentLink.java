package mining;

/**
 * @author y-yusuke
 *
 */
public class CodeFragmentLink {

	long before_element_id;
	long after_element_id;

	public CodeFragmentLink(long before_element_id,long after_element_id){
		this.before_element_id = before_element_id;
		this.after_element_id = after_element_id;
	}

	public long getBefore_element_id() {
		return before_element_id;
	}

	public long getAfter_element_id() {
		return after_element_id;
	}
}
